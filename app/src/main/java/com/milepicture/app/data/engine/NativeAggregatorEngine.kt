package com.milepicture.app.data.engine

import com.milepicture.app.data.model.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 商业级原生端侧聚合引擎 (Native In-App Aggregator Engine)
 * 使用 Android 原生高性能 JSON 解析与 OkHttp 并发，直接与各大图库公开 API 通信，0 后端依赖。
 */
object NativeAggregatorEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val POPULAR_TAGS = listOf(
        PopularTag("all", "全部灵感", "art"),
        PopularTag("nature", "自然风景", "nature landscape"),
        PopularTag("met", "大都会艺术", "masterpiece painting"),
        PopularTag("cyberpunk", "赛博朋克", "cyberpunk neon"),
        PopularTag("illustration", "插画设计", "illustration artistic"),
        PopularTag("flower", "繁花似锦", "flowers blooming"),
        PopularTag("anime", "动漫二次元", "anime artwork"),
        PopularTag("minimalist", "极简主义", "minimalist wallpaper"),
        PopularTag("architecture", "城市建筑", "architecture modern")
    )

    val SOURCES_LIST = listOf(
        SourceInfo(
            id = "met",
            name = "The Met (大都会艺术博物馆)",
            description = "美国最大艺术博物馆 Open Access 珍贵馆藏高清图档，公有领域无限制使用。",
            enabled = true,
            releaseState = "active",
            requiresKey = false,
            isKeyConfigured = true,
            licenseHighlights = "CC0 1.0 (开放访问公有领域)"
        ),
        SourceInfo(
            id = "openverse",
            name = "Openverse (WordPress CC0/CC)",
            description = "全球最大的开源公有领域与知识共享多媒体搜索引擎，收录超 7 亿公有许可素材。",
            enabled = true,
            releaseState = "active",
            requiresKey = false,
            isKeyConfigured = true,
            licenseHighlights = "CC0 / Public Domain / CC 授权"
        ),
        SourceInfo(
            id = "wikimedia",
            name = "Wikimedia Commons (维基共享)",
            description = "维基媒体基金会旗下公有领域与自由授权多媒体档案库，海量艺术与历史图档。",
            enabled = true,
            releaseState = "active",
            requiresKey = false,
            isKeyConfigured = true,
            licenseHighlights = "Public Domain / CC-BY-SA 自由文化协议"
        ),
        SourceInfo(
            id = "wallhaven",
            name = "Wallhaven (极清壁纸社区)",
            description = "全球顶尖高质量壁纸与数字艺术社区，拥有海量 4K/8K 顶级画质创作。",
            enabled = true,
            releaseState = "active",
            requiresKey = false,
            isKeyConfigured = true,
            licenseHighlights = "壁纸社区自由使用与个人非商用共享"
        )
    )

    private val FALLBACK_ITEMS = listOf(
        UnifiedImage(
            id = "met:436535",
            source = "met",
            sourceAssetId = "436535",
            kind = "artwork",
            title = "麦田里的丝柏树 (Wheat Field with Cypresses)",
            altText = "Vincent van Gogh - Wheat Field with Cypresses",
            width = 1200,
            height = 940,
            aspectRatio = 1.28f,
            tags = listOf("梵高", "名画", "油画", "风景", "Van Gogh"),
            color = null,
            renditions = Renditions(
                thumbnail = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
                preview = "https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg",
                large = "https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg"
            ),
            creator = Creator(name = "文森特·梵高 (Vincent van Gogh)", profileUrl = null),
            landingPageUrl = "https://www.metmuseum.org/art/collection/search/436535",
            license = LicenseInfo(
                licenseClass = "public_domain",
                code = "CC0-1.0",
                version = null,
                url = "https://creativecommons.org/publicdomain/zero/1.0/",
                attributionText = "The Metropolitan Museum of Art (Open Access)",
                evidence = "public_domain_flag"
            ),
            actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
        ),
        UnifiedImage(
            id = "met:437984",
            source = "met",
            sourceAssetId = "437984",
            kind = "artwork",
            title = "睡莲池上的拱桥 (Bridge over a Pond of Water Lilies)",
            altText = "Claude Monet - Water Lilies",
            width = 1200,
            height = 1200,
            aspectRatio = 1.0f,
            tags = listOf("莫奈", "睡莲", "印象派", "花", "Monet", "flower"),
            color = null,
            renditions = Renditions(
                thumbnail = "https://images.metmuseum.org/CRDImages/ep/web-large/DT1567.jpg",
                preview = "https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg",
                large = "https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg"
            ),
            creator = Creator(name = "克劳德·莫奈 (Claude Monet)", profileUrl = null),
            landingPageUrl = "https://www.metmuseum.org/art/collection/search/437984",
            license = LicenseInfo(
                licenseClass = "public_domain",
                code = "CC0-1.0",
                version = null,
                url = "https://creativecommons.org/publicdomain/zero/1.0/",
                attributionText = "The Metropolitan Museum of Art (Open Access)",
                evidence = "public_domain_flag"
            ),
            actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
        ),
        UnifiedImage(
            id = "met:436528",
            source = "met",
            sourceAssetId = "436528",
            kind = "artwork",
            title = "鸢尾花 (Irises)",
            altText = "Vincent van Gogh - Irises flower",
            width = 1200,
            height = 940,
            aspectRatio = 1.28f,
            tags = listOf("鸢尾花", "花", "油画", "flower", "irises"),
            color = null,
            renditions = Renditions(
                thumbnail = "https://images.metmuseum.org/CRDImages/ep/web-large/DP357300.jpg",
                preview = "https://images.metmuseum.org/CRDImages/ep/original/DP357300.jpg",
                large = "https://images.metmuseum.org/CRDImages/ep/original/DP357300.jpg"
            ),
            creator = Creator(name = "文森特·梵高 (Vincent van Gogh)", profileUrl = null),
            landingPageUrl = "https://www.metmuseum.org/art/collection/search/436528",
            license = LicenseInfo(
                licenseClass = "public_domain",
                code = "CC0-1.0",
                version = null,
                url = "https://creativecommons.org/publicdomain/zero/1.0/",
                attributionText = "The Metropolitan Museum of Art (Open Access)",
                evidence = "public_domain_flag"
            ),
            actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
        ),
        UnifiedImage(
            id = "met:436838",
            source = "met",
            sourceAssetId = "436838",
            kind = "artwork",
            title = "向日葵 (Sunflowers)",
            altText = "Vincent van Gogh - Sunflowers",
            width = 1200,
            height = 800,
            aspectRatio = 1.5f,
            tags = listOf("向日葵", "花", "油画", "sunflowers", "flower"),
            color = null,
            renditions = Renditions(
                thumbnail = "https://images.metmuseum.org/CRDImages/ep/web-large/DP124040.jpg",
                preview = "https://images.metmuseum.org/CRDImages/ep/original/DP124040.jpg",
                large = "https://images.metmuseum.org/CRDImages/ep/original/DP124040.jpg"
            ),
            creator = Creator(name = "文森特·梵高 (Vincent van Gogh)", profileUrl = null),
            landingPageUrl = "https://www.metmuseum.org/art/collection/search/436838",
            license = LicenseInfo(
                licenseClass = "public_domain",
                code = "CC0-1.0",
                version = null,
                url = "https://creativecommons.org/publicdomain/zero/1.0/",
                attributionText = "The Metropolitan Museum of Art (Open Access)",
                evidence = "public_domain_flag"
            ),
            actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
        )
    )

    suspend fun search(
        rawQuery: String,
        page: Int = 1,
        sourceFilter: String? = null
    ): SearchResponse = withContext(Dispatchers.IO) {
        val enQuery = LocalTranslator.translate(rawQuery)

        val activeSources = if (sourceFilter.isNullOrBlank()) {
            listOf("met", "openverse", "wikimedia", "wallhaven")
        } else {
            listOf(sourceFilter)
        }

        val deferredList = activeSources.map { src ->
            async {
                try {
                    when (src) {
                        "met" -> fetchMet(enQuery, page)
                        "openverse" -> fetchOpenverse(enQuery, page)
                        "wikimedia" -> fetchWikimedia(enQuery, page)
                        "wallhaven" -> fetchWallhaven(enQuery, page)
                        else -> emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

        val results = deferredList.awaitAll()
        val queues = results.filter { it.isNotEmpty() }

        val combined = mutableListOf<UnifiedImage>()
        val maxLen = queues.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxLen) {
            for (q in queues) {
                if (i < q.size) combined.add(q[i])
            }
        }

        if (combined.isEmpty()) {
            val qLower = rawQuery.lowercase()
            val matchedFallback = FALLBACK_ITEMS.filter { item ->
                rawQuery.isBlank() || item.tags.any { it.lowercase().contains(qLower) } || (item.title?.lowercase()?.contains(qLower) == true)
            }
            combined.addAll(if (matchedFallback.isNotEmpty()) matchedFallback else FALLBACK_ITEMS)
        }

        val statusList = activeSources.mapIndexed { index, id ->
            val count = results.getOrNull(index)?.size ?: 0
            SourceStatusItem(id = id, status = if (count > 0) "success" else "fallback", count = count, error = null)
        }

        SearchResponse(items = combined, sources = statusList, page = page)
    }

    private fun fetchMet(query: String, page: Int): List<UnifiedImage> {
        val searchUrl = "https://collectionapi.metmuseum.org/public/collection/v1/search?q=${URLEncoder.encode(query, "UTF-8")}&hasImages=true"
        val request = Request.Builder().url(searchUrl).header("User-Agent", "MiLePicture/1.0").build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val jsonStr = response.body?.string() ?: return emptyList()
        val root = JSONObject(jsonStr)
        if (!root.has("objectIDs") || root.isNull("objectIDs")) return emptyList()
        val objectIDsArray = root.getJSONArray("objectIDs")

        val total = objectIDsArray.length()
        val startIndex = (page - 1) * 8
        if (startIndex >= total) return emptyList()
        val endIndex = minOf(startIndex + 8, total)

        val items = mutableListOf<UnifiedImage>()
        for (i in startIndex until endIndex) {
            val objId = objectIDsArray.getInt(i)
            try {
                val detailUrl = "https://collectionapi.metmuseum.org/public/collection/v1/objects/$objId"
                val dReq = Request.Builder().url(detailUrl).header("User-Agent", "MiLePicture/1.0").build()
                val dRes = client.newCall(dReq).execute()
                if (dRes.isSuccessful) {
                    val dJson = JSONObject(dRes.body?.string() ?: "")
                    val smallImg = if (dJson.has("primaryImageSmall") && !dJson.isNull("primaryImageSmall")) dJson.getString("primaryImageSmall") else null
                    val primaryImg = if (dJson.has("primaryImage") && !dJson.isNull("primaryImage")) dJson.getString("primaryImage") else null
                    val finalImg = smallImg ?: primaryImg

                    if (!finalImg.isNullOrBlank()) {
                        val title = if (dJson.has("title") && !dJson.isNull("title")) dJson.getString("title") else "Untitled Masterpiece"
                        val artist = if (dJson.has("artistDisplayName") && !dJson.isNull("artistDisplayName")) dJson.getString("artistDisplayName") else "Unknown Master"
                        val isPd = if (dJson.has("isPublicDomain")) dJson.getBoolean("isPublicDomain") else true
                        val objectURL = if (dJson.has("objectURL") && !dJson.isNull("objectURL")) dJson.getString("objectURL") else "https://www.metmuseum.org/art/collection/search/$objId"

                        items.add(
                            UnifiedImage(
                                id = "met:$objId",
                                source = "met",
                                sourceAssetId = objId.toString(),
                                kind = "artwork",
                                title = title,
                                altText = "$title by $artist",
                                width = 1000,
                                height = 800,
                                aspectRatio = 1.25f,
                                tags = listOf("The Met", "Masterpiece", "Classic"),
                                color = null,
                                renditions = Renditions(
                                    thumbnail = smallImg ?: finalImg,
                                    preview = finalImg,
                                    large = primaryImg ?: finalImg
                                ),
                                creator = Creator(name = artist, profileUrl = null),
                                landingPageUrl = objectURL,
                                license = LicenseInfo(
                                    licenseClass = if (isPd) "public_domain" else "custom",
                                    code = if (isPd) "CC0-1.0" else "Met Access",
                                    version = null,
                                    url = "https://www.metmuseum.org/information/terms-and-conditions",
                                    attributionText = "The Metropolitan Museum of Art (Open Access)",
                                    evidence = "public_domain_flag"
                                ),
                                actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = isPd, canSetAsWallpaper = isPd)
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }
        return items
    }

    private fun fetchOpenverse(query: String, page: Int): List<UnifiedImage> {
        val url = "https://api.openverse.org/v1/images/?q=${URLEncoder.encode(query, "UTF-8")}&page=$page&page_size=18"
        val request = Request.Builder().url(url).header("User-Agent", "MiLePicture/1.0").build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val json = JSONObject(response.body?.string() ?: "")
        if (!json.has("results") || json.isNull("results")) return emptyList()
        val results = json.getJSONArray("results")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until results.length()) {
            val obj = results.getJSONObject(i)
            val id = if (obj.has("id")) obj.getString("id") else continue
            val imgUrl = if (obj.has("url")) obj.getString("url") else continue
            val thumb = if (obj.has("thumbnail") && !obj.isNull("thumbnail")) obj.getString("thumbnail") else imgUrl
            val title = if (obj.has("title") && !obj.isNull("title")) obj.getString("title") else "Untitled"
            val width = if (obj.has("width") && !obj.isNull("width")) obj.optInt("width") else null
            val height = if (obj.has("height") && !obj.isNull("height")) obj.optInt("height") else null
            val ratio = if (width != null && height != null && height > 0) (width.toFloat() / height).coerceIn(0.5f, 2.0f) else 1.0f
            val licenseCode = if (obj.has("license") && !obj.isNull("license")) obj.getString("license").uppercase() else "CC"
            val creator = if (obj.has("creator") && !obj.isNull("creator")) obj.getString("creator") else "Openverse Contributor"
            val landingUrl = if (obj.has("foreign_landing_url") && !obj.isNull("foreign_landing_url")) obj.getString("foreign_landing_url") else "https://openverse.org/image/$id"
            val attribution = if (obj.has("attribution") && !obj.isNull("attribution")) obj.getString("attribution") else "Openverse"

            items.add(
                UnifiedImage(
                    id = "openverse:$id",
                    source = "openverse",
                    sourceAssetId = id,
                    kind = "photo",
                    title = title,
                    altText = title,
                    width = width,
                    height = height,
                    aspectRatio = ratio,
                    tags = listOf("Openverse", "CC"),
                    color = null,
                    renditions = Renditions(thumbnail = thumb, preview = imgUrl, large = imgUrl),
                    creator = Creator(name = creator, profileUrl = null),
                    landingPageUrl = landingUrl,
                    license = LicenseInfo(
                        licenseClass = if (licenseCode.contains("CC0")) "cc0" else "creative_commons",
                        code = licenseCode,
                        version = null,
                        url = null,
                        attributionText = attribution,
                        evidence = "openverse_api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    private fun fetchWikimedia(query: String, page: Int): List<UnifiedImage> {
        val url = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=${URLEncoder.encode(query, "UTF-8")}&gsrnamespace=6&gsrlimit=14&prop=imageinfo&iiprop=url|size&format=json"
        val request = Request.Builder().url(url).header("User-Agent", "MiLePicture/1.0 (Mobile App)").build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("query") || root.isNull("query")) return emptyList()
        val queryObj = root.getJSONObject("query")
        if (!queryObj.has("pages") || queryObj.isNull("pages")) return emptyList()
        val pagesObj = queryObj.getJSONObject("pages")

        val items = mutableListOf<UnifiedImage>()
        val keys = pagesObj.keys()
        while (keys.hasNext()) {
            val pageId = keys.next()
            val obj = pagesObj.getJSONObject(pageId)
            val title = (if (obj.has("title")) obj.getString("title") else "Wikimedia Image").replace("File:", "")
            if (!obj.has("imageinfo") || obj.isNull("imageinfo")) continue
            val imageInfoArray = obj.getJSONArray("imageinfo")
            if (imageInfoArray.length() == 0) continue

            val info = imageInfoArray.getJSONObject(0)
            val imgUrl = if (info.has("url")) info.getString("url") else continue
            val width = if (info.has("width")) info.optInt("width") else null
            val height = if (info.has("height")) info.optInt("height") else null
            val ratio = if (width != null && height != null && height > 0) (width.toFloat() / height).coerceIn(0.5f, 2.0f) else 1.0f
            val descUrl = if (info.has("descriptionurl")) info.getString("descriptionurl") else "https://commons.wikimedia.org/wiki/File:$title"

            items.add(
                UnifiedImage(
                    id = "wikimedia:$pageId",
                    source = "wikimedia",
                    sourceAssetId = pageId,
                    kind = "photo",
                    title = title,
                    altText = title,
                    width = width,
                    height = height,
                    aspectRatio = ratio,
                    tags = listOf("Wikimedia", "Commons"),
                    color = null,
                    renditions = Renditions(thumbnail = imgUrl, preview = imgUrl, large = imgUrl),
                    creator = Creator(name = "Wikimedia Contributor", profileUrl = null),
                    landingPageUrl = descUrl,
                    license = LicenseInfo(
                        licenseClass = "public_domain",
                        code = "CC-BY-SA",
                        version = null,
                        url = null,
                        attributionText = "Wikimedia Commons",
                        evidence = "wikimedia_api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    private fun fetchWallhaven(query: String, page: Int): List<UnifiedImage> {
        val url = "https://wallhaven.cc/api/v1/search?q=${URLEncoder.encode(query, "UTF-8")}&page=$page&sorting=toplist"
        val request = Request.Builder().url(url).header("User-Agent", "MiLePicture/1.0").build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("data") || root.isNull("data")) return emptyList()
        val dataArray = root.getJSONArray("data")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until dataArray.length()) {
            val obj = dataArray.getJSONObject(i)
            val id = if (obj.has("id")) obj.getString("id") else continue
            val imgPath = if (obj.has("path")) obj.getString("path") else continue
            val thumbs = if (obj.has("thumbs")) obj.getJSONObject("thumbs") else null
            val thumb = if (thumbs != null && thumbs.has("small")) thumbs.getString("small") else imgPath
            val preview = if (thumbs != null && thumbs.has("large")) thumbs.getString("large") else imgPath
            val dimX = if (obj.has("dimension_x")) obj.optInt("dimension_x") else null
            val dimY = if (obj.has("dimension_y")) obj.optInt("dimension_y") else null
            val ratio = if (dimX != null && dimY != null && dimY > 0) (dimX.toFloat() / dimY).coerceIn(0.5f, 2.0f) else 1.77f
            val landingUrl = if (obj.has("url")) obj.getString("url") else "https://wallhaven.cc/w/$id"

            items.add(
                UnifiedImage(
                    id = "wallhaven:$id",
                    source = "wallhaven",
                    sourceAssetId = id,
                    kind = "wallpaper",
                    title = "Wallhaven #$id",
                    altText = "Wallhaven Wallpaper $id",
                    width = dimX,
                    height = dimY,
                    aspectRatio = ratio,
                    tags = listOf("Wallpaper", "4K"),
                    color = null,
                    renditions = Renditions(thumbnail = thumb, preview = preview, large = imgPath),
                    creator = Creator(name = "Wallhaven Member", profileUrl = null),
                    landingPageUrl = landingUrl,
                    license = LicenseInfo(
                        licenseClass = "custom",
                        code = "Wallhaven License",
                        version = null,
                        url = null,
                        attributionText = "Wallhaven Community",
                        evidence = "wallhaven_api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    suspend fun diagnoseNetwork(): Map<String, Long> = withContext(Dispatchers.IO) {
        val targets = mapOf(
            "The Met 博物馆 (公有领域)" to "https://collectionapi.metmuseum.org/public/collection/v1/objects/436535",
            "Openverse 图库 (WordPress)" to "https://api.openverse.org/v1/images/?page_size=1",
            "Wikimedia 维基共享" to "https://commons.wikimedia.org/w/api.php?action=query&meta=siteinfo&format=json",
            "Wallhaven 壁纸库" to "https://wallhaven.cc/api/v1/search?sorting=toplist"
        )

        val results = mutableMapOf<String, Long>()
        for ((name, targetUrl) in targets) {
            val start = System.currentTimeMillis()
            try {
                val req = Request.Builder().url(targetUrl).header("User-Agent", "MiLePicture/1.0").build()
                val res = client.newCall(req).execute()
                val duration = System.currentTimeMillis() - start
                results[name] = if (res.isSuccessful) duration else -1L
            } catch (_: Exception) {
                results[name] = -1L
            }
        }
        results
    }
}
