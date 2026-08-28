package com.milepicture.app.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.milepicture.app.data.engine.NativeAggregatorEngine
import com.milepicture.app.data.model.*
import com.milepicture.app.data.repository.FavoritesRepository
import com.milepicture.app.data.repository.SearchHistoryRepository
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.milepicture.app.data.engine.NetworkLogItem
import com.milepicture.app.data.engine.NetworkLogger
import com.milepicture.app.utils.ImageDownloadHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesRepo = FavoritesRepository(application)
    private val searchHistoryRepo = SearchHistoryRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTagId = MutableStateFlow("all")
    val selectedTagId: StateFlow<String> = _selectedTagId.asStateFlow()

    private val _tags = MutableStateFlow<List<PopularTag>>(NativeAggregatorEngine.POPULAR_TAGS)
    val tags: StateFlow<List<PopularTag>> = _tags.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _images = MutableStateFlow<List<UnifiedImage>>(emptyList())
    val images: StateFlow<List<UnifiedImage>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _favorites = MutableStateFlow<List<UnifiedImage>>(emptyList())
    val favorites: StateFlow<List<UnifiedImage>> = _favorites.asStateFlow()

    private val _sources = MutableStateFlow<List<SourceInfo>>(NativeAggregatorEngine.SOURCES_LIST)
    val sources: StateFlow<List<SourceInfo>> = _sources.asStateFlow()

    private val _selectedSourceFilter = MutableStateFlow<String?>(null)
    val selectedSourceFilter: StateFlow<String?> = _selectedSourceFilter.asStateFlow()

    // 实时网络与 Bug 诊断日志流
    val networkLogs: StateFlow<List<NetworkLogItem>> = NetworkLogger.logsFlow

    // 缓存大小统计
    private val _cacheSizeText = MutableStateFlow("计算中...")
    val cacheSizeText: StateFlow<String> = _cacheSizeText.asStateFlow()

    // 网络健康诊断状态
    private val _diagnosticResults = MutableStateFlow<Map<String, Long>>(emptyMap())
    val diagnosticResults: StateFlow<Map<String, Long>> = _diagnosticResults.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    // 分页状态与智能静默预加载缓存 (Prefetch Buffer)
    private var currentPage = 1
    private var isLastPage = false
    private var currentActiveQuery = "art"
    private var isPrefetching = false
    private var prefetchedBuffer: List<UnifiedImage>? = null

    init {
        _favorites.value = favoritesRepo.loadFavorites()
        _searchHistory.value = searchHistoryRepo.getSearchHistory()
        refreshCacheSize()
        loadInitialData()
    }

    fun loadInitialData() {
        _tags.value = NativeAggregatorEngine.POPULAR_TAGS
        _sources.value = NativeAggregatorEngine.SOURCES_LIST
        search("art", 1)
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onTagSelect(tag: PopularTag) {
        _selectedTagId.value = tag.id
        _searchQuery.value = ""
        _selectedSourceFilter.value = if (tag.id == "all") null else tag.id
        search("art", 1)
    }

    fun onSearchTriggered() {
        val q = _searchQuery.value.ifBlank { "art" }
        if (_searchQuery.value.isNotBlank()) {
            searchHistoryRepo.addHistory(_searchQuery.value.trim())
            _searchHistory.value = searchHistoryRepo.getSearchHistory()
        }
        search(q, 1)
    }

    fun onHistoryItemClick(historyQuery: String) {
        _selectedTagId.value = "all"
        _selectedSourceFilter.value = null
        _searchQuery.value = historyQuery
        onSearchTriggered()
    }

    fun removeSearchHistoryItem(query: String) {
        searchHistoryRepo.removeHistory(query)
        _searchHistory.value = searchHistoryRepo.getSearchHistory()
    }

    fun clearSearchHistory() {
        searchHistoryRepo.clearHistory()
        _searchHistory.value = emptyList()
        Toast.makeText(getApplication(), "已清空搜索历史", Toast.LENGTH_SHORT).show()
    }

    suspend fun refreshCurrent() {
        val q = _searchQuery.value.ifBlank { currentActiveQuery }
        searchInternal(q, 1)
    }

    fun setSourceFilter(sourceId: String?) {
        _selectedSourceFilter.value = sourceId
        onSearchTriggered()
    }

    fun loadNextPage() {
        if (_isLoading.value || _isLoadingMore.value || isLastPage) return

        viewModelScope.launch {
            // 1. 如果后台已经提前预取好了下一页数据，立刻 0ms 瞬间合并上屏
            val cached = prefetchedBuffer
            if (!cached.isNullOrEmpty()) {
                prefetchedBuffer = null
                currentPage++
                val existingIds = _images.value.map { it.id }.toSet()
                val uniqueNew = cached.filterNot { it.id in existingIds }
                if (uniqueNew.isNotEmpty()) {
                    _images.value = _images.value + uniqueNew
                    preloadImagesToCoil(uniqueNew)
                    // 消费完后立即静默预取再下一页
                    triggerBackgroundPrefetch(currentPage + 1)
                    return@launch
                }
            }

            // 2. 否则进行正常网络分页请求
            _isLoadingMore.value = true
            try {
                val nextPage = currentPage + 1
                val response = NativeAggregatorEngine.search(
                    rawQuery = currentActiveQuery,
                    page = nextPage,
                    sourceFilter = _selectedSourceFilter.value
                )

                val newItems = response.items
                if (newItems.isEmpty()) {
                    isLastPage = true
                } else {
                    currentPage = nextPage
                    val existingIds = _images.value.map { it.id }.toSet()
                    val uniqueNew = newItems.filterNot { it.id in existingIds }
                    _images.value = _images.value + uniqueNew
                    preloadImagesToCoil(uniqueNew)
                    // 静默预加载再下一页
                    triggerBackgroundPrefetch(currentPage + 1)
                }
            } catch (_: Exception) {
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * 智能后台静默预取下一页数据 (后台默默准备好，用户下滑时无需等待)
     */
    fun triggerBackgroundPrefetch(pageToPrefetch: Int) {
        if (isPrefetching || isLastPage) return
        isPrefetching = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = NativeAggregatorEngine.search(
                    rawQuery = currentActiveQuery,
                    page = pageToPrefetch,
                    sourceFilter = _selectedSourceFilter.value
                )
                if (response.items.isNotEmpty()) {
                    val existingIds = _images.value.map { it.id }.toSet()
                    val unique = response.items.filterNot { it.id in existingIds }
                    prefetchedBuffer = unique
                    // 在后台提前让 Coil 下载并解码大图和缩略图
                    preloadImagesToCoil(unique)
                }
            } catch (_: Exception) {
            } finally {
                isPrefetching = false
            }
        }
    }

    /**
     * 商业级 Coil 缓存预热 (提前在内存与磁盘中解码好缩略图和大图，点击详情秒开 0 延迟)
     */
    private fun preloadImagesToCoil(items: List<UnifiedImage>) {
        val context = getApplication<Application>()
        val loader = Coil.imageLoader(context)

        items.take(12).forEach { img ->
            // 预热缩略图
            val thumbReq = ImageRequest.Builder(context)
                .data(img.renditions.thumbnail)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            loader.enqueue(thumbReq)

            // 预热高清预览图（用户点击进入大图时 0 延迟秒开）
            val largeUrl = img.renditions.preview ?: img.renditions.large
            if (!largeUrl.isNullOrBlank() && largeUrl != img.renditions.thumbnail) {
                val largeReq = ImageRequest.Builder(context)
                    .data(largeUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(largeReq)
            }
        }
    }

    fun toggleFavorite(image: UnifiedImage) {
        val current = _favorites.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == image.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            Toast.makeText(getApplication(), "已取消收藏", Toast.LENGTH_SHORT).show()
        } else {
            current.add(0, image)
            Toast.makeText(getApplication(), "❤️ 已收藏至 ${image.source.uppercase()} 文件夹", Toast.LENGTH_SHORT).show()
        }
        _favorites.value = current
        favoritesRepo.saveFavorites(current)
    }

    fun isFavorite(imageId: String): Boolean {
        return _favorites.value.any { it.id == imageId }
    }

    fun downloadImage(image: UnifiedImage) {
        val downloadUrl = image.renditions.large ?: image.renditions.preview ?: image.renditions.thumbnail
        viewModelScope.launch {
            ImageDownloadHelper.downloadImageToGallery(
                context = getApplication(),
                imageUrl = downloadUrl,
                title = image.title ?: "image",
                source = image.source,
                onProgress = { _isDownloading.value = it },
                onResult = { _, msg ->
                    Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun runNetworkDiagnostics() {
        viewModelScope.launch {
            _isDiagnosing.value = true
            try {
                val results = NativeAggregatorEngine.diagnoseNetwork()
                _diagnosticResults.value = results
                Toast.makeText(getApplication(), "网络健康诊断完成", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "诊断异常: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                _isDiagnosing.value = false
            }
        }
    }

    fun copyLogsToClipboard() {
        val summary = NetworkLogger.exportSummary()
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MiLePicture Network Logs", summary)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "📋 已复制实时网络与 Bug 诊断日志到剪贴板", Toast.LENGTH_LONG).show()
    }

    fun clearNetworkLogs() {
        NetworkLogger.clear()
        Toast.makeText(getApplication(), "已清空网络诊断日志", Toast.LENGTH_SHORT).show()
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val cacheDir = getApplication<Application>().cacheDir
            val size = getFolderSize(cacheDir)
            val formatted = when {
                size > 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
                size > 1024 -> String.format("%.1f KB", size / 1024.0)
                else -> "$size B"
            }
            _cacheSizeText.value = formatted
        }
    }

    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = Coil.imageLoader(getApplication())
                imageLoader.memoryCache?.clear()
                imageLoader.diskCache?.clear()

                val cacheDir = getApplication<Application>().cacheDir
                deleteDir(cacheDir)
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "✨ 缓存已全部清理完毕", Toast.LENGTH_SHORT).show()
                }
                refreshCacheSize()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "清理缓存失败: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var size: Long = 0
        file.listFiles()?.forEach { child ->
            size += if (child.isDirectory) getFolderSize(child) else child.length()
        }
        return size
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            dir.listFiles()?.forEach { child ->
                deleteDir(child)
            }
        }
        return dir?.delete() ?: false
    }

    private fun search(query: String, page: Int) {
        viewModelScope.launch {
            searchInternal(query, page)
        }
    }

    private suspend fun searchInternal(query: String, page: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        currentPage = 1
        isLastPage = false
        currentActiveQuery = query
        prefetchedBuffer = null

        try {
            val response = NativeAggregatorEngine.search(
                rawQuery = query,
                page = page,
                sourceFilter = _selectedSourceFilter.value
            )
            _images.value = response.items
            // 首次搜索完毕后，立即静默预热大图 + 预取第 2 页
            preloadImagesToCoil(response.items)
            triggerBackgroundPrefetch(2)
        } catch (e: Exception) {
            _errorMessage.value = "搜索异常: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }
}
