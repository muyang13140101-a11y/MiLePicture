package com.milepicture.app.data.model

import com.google.gson.annotations.SerializedName

data class UnifiedImage(
    val id: String,
    val source: String,
    val sourceAssetId: String = "",
    val kind: String?,
    val title: String?,
    val altText: String?,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Float?,
    val tags: List<String> = emptyList(),
    val color: String?,
    val renditions: Renditions,
    val creator: Creator,
    val landingPageUrl: String,
    val license: LicenseInfo,
    val actionPolicy: ActionPolicy
)

data class Renditions(
    val thumbnail: String,
    val preview: String?,
    val large: String?
)

data class Creator(
    val name: String?,
    val profileUrl: String?
)

data class LicenseInfo(
    @SerializedName("class") val licenseClass: String,
    val code: String?,
    val version: String?,
    val url: String?,
    val attributionText: String?,
    val evidence: String?
)

data class ActionPolicy(
    val canShowInSearch: Boolean = true,
    val canOfferDownload: Boolean = false,
    val canSetAsWallpaper: Boolean = false,
    val requiresAttribution: Boolean = false
)

data class SearchResponse(
    val items: List<UnifiedImage>,
    val sources: List<SourceStatusItem>,
    val page: Int
)

data class SourceStatusItem(
    val id: String,
    val status: String,
    val count: Int,
    val error: String?
)

data class SourcesResponse(
    val sources: List<SourceInfo>
)

data class SourceInfo(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val releaseState: String,
    val requiresKey: Boolean,
    val isKeyConfigured: Boolean,
    val licenseHighlights: String
)

data class PopularTag(
    val id: String,
    val name: String,
    val query: String
)

data class PopularTagsResponse(
    val tags: List<PopularTag>
)
