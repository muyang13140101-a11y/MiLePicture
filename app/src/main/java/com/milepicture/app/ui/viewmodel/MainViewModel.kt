package com.milepicture.app.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.milepicture.app.data.api.ApiClient
import com.milepicture.app.data.api.MiLePictureApiService
import com.milepicture.app.data.model.*
import com.milepicture.app.data.repository.FavoritesRepository
import com.milepicture.app.utils.ImageDownloadHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: MiLePictureApiService
        get() = ApiClient.getService()
    private val favoritesRepo = FavoritesRepository(application)

    fun updateServerUrl(newUrl: String) {
        ApiClient.updateBaseUrl(newUrl)
        _errorMessage.value = null
        loadInitialData()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTagId = MutableStateFlow("all")
    val selectedTagId: StateFlow<String> = _selectedTagId.asStateFlow()

    private val _tags = MutableStateFlow<List<PopularTag>>(emptyList())
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

    private val _sources = MutableStateFlow<List<SourceInfo>>(emptyList())
    val sources: StateFlow<List<SourceInfo>> = _sources.asStateFlow()

    private val _selectedSourceFilter = MutableStateFlow<String?>(null)
    val selectedSourceFilter: StateFlow<String?> = _selectedSourceFilter.asStateFlow()

    private val _onlyPublicDomain = MutableStateFlow(false)
    val onlyPublicDomain: StateFlow<Boolean> = _onlyPublicDomain.asStateFlow()

    init {
        // 读取本地持久化收藏列表
        _favorites.value = favoritesRepo.loadFavorites()
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                // 1. 加载标签
                val tagsRes = apiService.getPopularTags()
                _tags.value = tagsRes.tags

                // 2. 加载来源信息
                val sourcesRes = apiService.getSources()
                _sources.value = sourcesRes.sources

                // 3. 执行首屏搜索
                search("art", 1)
            } catch (e: Exception) {
                _errorMessage.value = "连接后端服务失败: ${e.localizedMessage ?: "请确认 server 已启动并在同局域网"}"
            }
        }
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
        favoritesRepo.saveFavorites(current) // 持久化保存
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

    private fun search(query: String, page: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val licenseParam = if (_onlyPublicDomain.value) "public_domain_cc0" else null
                val response = apiService.searchImages(
                    query = query,
                    sources = _selectedSourceFilter.value,
                    license = licenseParam,
                    page = page,
                    pageSize = 24
                )
                _images.value = response.items
            } catch (e: Exception) {
                _errorMessage.value = "搜索异常: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
