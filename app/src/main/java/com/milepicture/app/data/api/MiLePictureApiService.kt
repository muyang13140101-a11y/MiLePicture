package com.milepicture.app.data.api

import com.milepicture.app.data.model.PopularTagsResponse
import com.milepicture.app.data.model.SearchResponse
import com.milepicture.app.data.model.SourcesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MiLePictureApiService {

    @GET("v1/sources")
    suspend fun getSources(): SourcesResponse

    @GET("v1/popular-tags")
    suspend fun getPopularTags(): PopularTagsResponse

    @GET("v1/search")
    suspend fun searchImages(
        @Query("q") query: String? = null,
        @Query("tags") tags: String? = null,
        @Query("sources") sources: String? = null,
        @Query("orientation") orientation: String? = null,
        @Query("license") license: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): SearchResponse
}
