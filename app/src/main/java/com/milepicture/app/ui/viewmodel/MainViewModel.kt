package com.milepicture.app.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milepicture.app.data.engine.NativeAggregatorEngine
import com.milepicture.app.data.model.*
import com.milepicture.app.data.repository.FavoritesRepository
import com.milepicture.app.utils.ImageDownloadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesRepo = FavoritesRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTagId = MutableStateFlow("all")
    val selectedTagId: StateFlow<String> = _selectedTagId.asStateFlow()

    private val _tags = MutableStateFlow<List<PopularTag>>(NativeAggregatorEngine.POPULAR_TAGS)
    val tags: StateFlow<List<PopularTag>> = _tags.asStateFlow()

    private val _images = MutableStateFlow<List<UnifiedImage>>(emptyList())
    val images: StateFlow<List<UnifiedImage>> = _images.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    private val _onlyPublicDomain = MutableStateFlow(false)
    val onlyPublicDomain: StateFlow<Boolean> = _onlyPublicDomain.asStateFlow()

    // 网络健康诊断状态 (源名称 -> 延迟毫秒数, -1 表示超时)
    private val _diagnosticResults = MutableStateFlow<Map<String, Long>>(emptyMap())
    val diagnosticResults: StateFlow<Map<String, Long>> = _diagnosticResults.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    init {
        _favorites.value = favoritesRepo.loadFavorites()
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
        search(tag.query, 1)
    }

    fun onSearchTriggered() {
        val q = _searchQuery.value.ifBlank { "art" }
        search(q, 1)
    }

    fun setSourceFilter(sourceId: String?) {
        _selectedSourceFilter.value = sourceId
        onSearchTriggered()
    }

    fun togglePublicDomainOnly() {
        _onlyPublicDomain.value = !_onlyPublicDomain.value
        onSearchTriggered()
    }

    fun toggleFavorite(image: UnifiedImage) {
        val current = _favorites.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == image.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
            Toast.makeText(getApplication(), "已取消收藏", Toast.LENGTH_SHORT).show()
        } else {
            current.add(0, image)
            Toast.makeText(getApplication(), "❤️ 已添加至收藏", Toast.LENGTH_SHORT).show()
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

    /**
     * 运行网络健康检测 (/health 诊断)
     */
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

    private fun search(query: String, page: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 原生端侧直连聚合搜索，0 中间服务器依赖
                val response = NativeAggregatorEngine.search(
                    rawQuery = query,
                    page = page,
                    sourceFilter = _selectedSourceFilter.value
                )

                var items = response.items
                if (_onlyPublicDomain.value) {
                    items = items.filter { it.license.licenseClass == "public_domain" || it.license.licenseClass == "cc0" }
                }

                _images.value = items
            } catch (e: Exception) {
                _errorMessage.value = "搜索异常: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
