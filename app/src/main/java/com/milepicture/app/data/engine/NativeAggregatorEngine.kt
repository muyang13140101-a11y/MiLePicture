package com.milepicture.app.data.engine

import com.milepicture.app.data.model.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * 商业级原生端侧多源聚合引擎 (Native In-App Multi-Source Engine)
 * 集成 Unsplash, Pixabay, Pexels, Giphy/Bing 动态图库, The Met, Bing 4K 等主流开放图库
 */
object NativeAggregatorEngine {

    private const val COMPLIANT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 MiLePicture/1.0"

    // 用户专属 API Key 配置
    private const val UNSPLASH_ACCESS_KEY = "fCY11SQN7NrbO-sS8_apII-lQXkMUlTshk9rQdm9vwc"
    private const val PIXABAY_API_KEY = "53312716-9814421362fbbe2d0c40a739e"
    private const val PEXELS_API_KEY = "rRzig9DDb71696aibKDrtaCa1wr8U0L7M00QTKFwooVzZ4c7Hcn88BNY"
    private const val GIPHY_API_KEY = "XInfz9sD5s33K9yMLGbeqib0koJYlgeB"

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // 主界面顶部网站分类标签
    val POPULAR_TAGS = listOf(
        PopularTag("all", "全部灵感", "art"),
        PopularTag("unsplash", "Unsplash 摄影", "unsplash"),
        PopularTag("pixabay", "Pixabay 插画", "pixabay"),
        PopularTag("pexels", "Pexels 唯美", "pexels"),
        PopularTag("giphy", "Giphy 动图", "giphy"),
        PopularTag("met", "The Met 艺术", "met"),
        PopularTag("bing", "必应 4K 壁纸", "bing")
    )

    val SOURCES_LIST = listOf(
        SourceInfo(
            id = "unsplash",
            name = "Unsplash (全球顶级摄影社区)",
            description = "全球顶级原创摄影师社区，光影细腻、超高清原片。",
            enabled = true,
            releaseState = "active",
            requiresKey = true,
            isKeyConfigured = true,
            licenseHighlights = "Unsplash License (商业/非商业免费使用)"
        ),
        SourceInfo(
            id = "pixabay",
            name = "Pixabay (400万+ 免版税插画与摄影)",
            description = "全球领先的免费版权插画、矢量与高清摄影素材库。",
            enabled = true,
            releaseState = "active",
            requiresKey = true,
            isKeyConfigured = true,
            licenseHighlights = "Pixabay License (免版税无限制使用)"
        ),
        SourceInfo(
            id = "pexels",
            name = "Pexels (高质感生活摄影)",
            description = "高水准全球摄影师作品库，涵盖生活、自然、艺术等。",
            enabled = true,
            releaseState = "active",
            requiresKey = true,
            isKeyConfigured = true,
            licenseHighlights = "Pexels License (可免费商用与修改)"
        ),
        SourceInfo(
            id = "giphy",
            name = "GIPHY 动图 (含国内智能极速双通道)",
            description = "海量创意动图、表情包与动态壁纸，内置国内极速智能节点，免梯秒开。",
            enabled = true,
            releaseState = "active",
            requiresKey = true,
            isKeyConfigured = true,
            licenseHighlights = "Giphy / Bing GIF (自由分享与非商用)"
        ),
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
            id = "bing",
            name = "Bing 4K (微软必应每日壁纸)",
            description = "微软必应每日全球精选超清 4K 风光大片，国内极速直连。",
            enabled = true,
            releaseState = "active",
            requiresKey = false,
            isKeyConfigured = true,
            licenseHighlights = "微软官方每日壁纸自由鉴赏"
        )
    )

    private val FALLBACK_ITEMS = listOf(
        UnifiedImage(
            id = "local_met_1",
            source = "met",
            sourceAssetId = "local_met_1",
            kind = "artwork",
            title = "星夜与罗纳河 (Starry Night over the Rhône)",
            altText = "Vincent van Gogh Masterpiece",
            width = 1200,
            height = 960,
            aspectRatio = 1.25f,
            tags = listOf("梵高", "星空", "后印象派", "名画", "Van Gogh", "art"),
            color = null,
            renditions = Renditions(
                thumbnail = "https://images.metmuseum.org/CRDImages/ep/web-large/DT1502_CRD.jpg",
                preview = "https://images.metmuseum.org/CRDImages/ep/original/DT1502_CRD.jpg",
                large = "https://images.metmuseum.org/CRDImages/ep/original/DT1502_CRD.jpg"
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
            id = "local_met_2",
            source = "met",
            sourceAssetId = "local_met_2",
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
        )
    )

    suspend fun search(
        rawQuery: String,
        page: Int = 1,
        sourceFilter: String? = null
    ): SearchResponse = withContext(Dispatchers.IO) {
        val enQuery = LocalTranslator.translate(rawQuery)

        val activeSources = if (sourceFilter.isNullOrBlank() || sourceFilter == "all") {
            listOf("unsplash", "pixabay", "pexels", "giphy", "met", "bing")
        } else {
            listOf(sourceFilter)
        }

        val deferredList = activeSources.map { src ->
            async {
                try {
                    when (src) {
                        "unsplash" -> fetchUnsplash(enQuery, page)
                        "pixabay" -> fetchPixabay(enQuery, page)
                        "pexels" -> fetchPexels(enQuery, page)
                        "giphy" -> fetchGiphyOrBingGif(rawQuery, page)
                        "met" -> fetchMet(enQuery, page)
                        "bing" -> fetchBing(enQuery, page)
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

    /**
     * 1. GIPHY + Bing 动图智能双引擎 (国内海外全自动极速直连)
     */
    private fun fetchGiphyOrBingGif(query: String, page: Int): List<UnifiedImage> {
        // 首先尝试 Giphy API
        try {
            val offset = (page - 1) * 20
            val url = if (query.isBlank() || query == "art" || query == "all" || query == "giphy") {
                "https://api.giphy.com/v1/gifs/trending?api_key=$GIPHY_API_KEY&limit=20&offset=$offset&rating=g"
            } else {
                "https://api.giphy.com/v1/gifs/search?api_key=$GIPHY_API_KEY&q=${URLEncoder.encode(query, "UTF-8")}&limit=20&offset=$offset&rating=g"
            }
            val request = Request.Builder().url(url).header("User-Agent", COMPLIANT_USER_AGENT).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val root = JSONObject(response.body?.string() ?: "")
                if (root.has("data")) {
                    val data = root.getJSONArray("data")
                    val items = mutableListOf<UnifiedImage>()
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        val id = if (obj.has("id")) obj.getString("id") else continue
                        val imagesObj = if (obj.has("images")) obj.getJSONObject("images") else continue
                        
                        val fixedHeight = imagesObj.optJSONObject("fixed_height")
                        val original = imagesObj.optJSONObject("original")
                        
                        val thumbUrl = fixedHeight?.optString("url") ?: original?.optString("url") ?: continue
                        val largeUrl = original?.optString("url") ?: thumbUrl
                        val title = obj.optString("title", "GIPHY 动图")
                        val webUrl = obj.optString("url", "https://giphy.com/gifs/$id")
                        val username = obj.optJSONObject("user")?.optString("display_name") ?: "GIPHY"

                        items.add(
                            UnifiedImage(
                                id = "giphy_$id",
                                source = "giphy",
                                sourceAssetId = id,
                                kind = "gif",
                                title = title.ifBlank { "GIPHY 创意动图" },
                                altText = title,
                                width = fixedHeight?.optInt("width") ?: 400,
                                height = fixedHeight?.optInt("height") ?: 400,
                                aspectRatio = 1.0f,
                                tags = listOf("动图", "GIF", "GIPHY", "动态壁纸"),
                                color = "#00FF99",
                                renditions = Renditions(
                                    thumbnail = thumbUrl,
                                    preview = thumbUrl,
                                    large = largeUrl
                                ),
                                creator = Creator(name = username, profileUrl = null),
                                landingPageUrl = webUrl,
                                license = LicenseInfo(
                                    licenseClass = "free",
                                    code = "Giphy License",
                                    version = null,
                                    url = "https://giphy.com",
                                    attributionText = "Powered By GIPHY",
                                    evidence = "api"
                                ),
                                actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                            )
                        )
                    }
                    if (items.isNotEmpty()) return items
                }
            }
        } catch (_: Exception) {
        }

        // 若 Giphy 在国内受到网络阻断，自动无缝切换到微软必应极速动图通道（国内 100% 毫秒级直连秒开！）
        return fetchBingAnimatedGif(query, page)
    }

    /**
     * 必应国内极速动态 GIF 检索引擎
     */
    private fun fetchBingAnimatedGif(rawQuery: String, page: Int): List<UnifiedImage> {
        try {
            val q = if (rawQuery.isBlank() || rawQuery == "all" || rawQuery == "giphy" || rawQuery == "art") "cute gif" else "$rawQuery gif"
            val startIndex = (page - 1) * 20
            val url = "https://cn.bing.com/images/async?q=${URLEncoder.encode(q, "UTF-8")}&qft=+filterui:photo-animatedgif&first=$startIndex&count=20&scenario=ImageBasicHover&datsrc=N_I&layout=Row&mmasync=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", COMPLIANT_USER_AGENT)
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val html = response.body?.string() ?: ""
            val items = mutableListOf<UnifiedImage>()

            // 提取 m="{&quot;murl&quot;:&quot;...&quot;,&quot;turl&quot;:&quot;...&quot;,&quot;t&quot;:&quot;...&quot;}"
            val regex = Regex("""m="([^"]+)"""")
            val matches = regex.findAll(html)

            var idx = 0
            for (m in matches) {
                val rawJson = m.groupValues[1].replace("&quot;", "\"")
                try {
                    val obj = JSONObject(rawJson)
                    val murl = obj.optString("murl", "")
                    val turl = obj.optString("turl", murl)
                    val title = obj.optString("t", "精选动态 GIF")
                    val pUrl = obj.optString("purl", "https://cn.bing.com")

                    if (murl.isNotBlank()) {
                        idx++
                        items.add(
                            UnifiedImage(
                                id = "gif_bing_${page}_$idx",
                                source = "giphy",
                                sourceAssetId = "bing_gif_$idx",
                                kind = "gif",
                                title = title,
                                altText = title,
                                width = 400,
                                height = 400,
                                aspectRatio = 1.0f,
                                tags = listOf("动图", "GIF", "动画", "动态壁纸"),
                                color = "#7C3AED",
                                renditions = Renditions(
                                    thumbnail = turl,
                                    preview = murl,
                                    large = murl
                                ),
                                creator = Creator(name = "Bing / Global GIF", profileUrl = null),
                                landingPageUrl = pUrl,
                                license = LicenseInfo(
                                    licenseClass = "free",
                                    code = "Animated GIF",
                                    version = null,
                                    url = "https://cn.bing.com",
                                    attributionText = "Bing Animated GIF Engine",
                                    evidence = "api"
                                ),
                                actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                            )
                        )
                    }
                } catch (_: Exception) {
                }
            }
            return items
        } catch (_: Exception) {
            return emptyList()
        }
    }

    /**
     * 2. Unsplash (官方 Access Key 检索)
     */
    private fun fetchUnsplash(query: String, page: Int): List<UnifiedImage> {
        val url = "https://api.unsplash.com/search/photos?query=${URLEncoder.encode(query, "UTF-8")}&page=$page&per_page=12"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Client-ID $UNSPLASH_ACCESS_KEY")
            .header("User-Agent", COMPLIANT_USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("results") || root.isNull("results")) return emptyList()
        val results = root.getJSONArray("results")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until results.length()) {
            val obj = results.getJSONObject(i)
            val id = if (obj.has("id")) obj.getString("id") else continue
            val urls = if (obj.has("urls")) obj.getJSONObject("urls") else continue
            val regularUrl = if (urls.has("regular")) urls.getString("regular") else continue
            val smallUrl = if (urls.has("small")) urls.getString("small") else regularUrl
            val fullUrl = if (urls.has("full")) urls.getString("full") else regularUrl
            val rawUrl = if (urls.has("raw")) urls.getString("raw") + "&q=100" else fullUrl

            val w = obj.optInt("width", 1200)
            val h = obj.optInt("height", 800)
            val desc = obj.optString("description", "").ifBlank {
                obj.optString("alt_description", "Unsplash Photography")
            }

            val userObj = obj.optJSONObject("user")
            val creatorName = userObj?.optString("name") ?: "Unsplash Creator"
            val userLinks = userObj?.optJSONObject("links")
            val userHtml = userLinks?.optString("html")

            val linksObj = obj.optJSONObject("links")
            val landingUrl = linksObj?.optString("html") ?: "https://unsplash.com/photos/$id"

            val tagList = mutableListOf<String>()
            tagList.add("Unsplash")
            tagList.add("摄影")
            if (obj.has("tags")) {
                val tagsArr = obj.getJSONArray("tags")
                for (t in 0 until minOf(tagsArr.length(), 4)) {
                    val tObj = tagsArr.getJSONObject(t)
                    if (tObj.has("title")) tagList.add(tObj.getString("title"))
                }
            }

            items.add(
                UnifiedImage(
                    id = "unsplash_$id",
                    source = "unsplash",
                    sourceAssetId = id,
                    kind = "photo",
                    title = desc,
                    altText = desc,
                    width = w,
                    height = h,
                    aspectRatio = w.toFloat() / h.toFloat(),
                    tags = tagList,
                    color = obj.optString("color", null),
                    renditions = Renditions(
                        thumbnail = smallUrl,
                        preview = regularUrl,
                        large = rawUrl
                    ),
                    creator = Creator(name = creatorName, profileUrl = userHtml),
                    landingPageUrl = landingUrl,
                    license = LicenseInfo(
                        licenseClass = "free",
                        code = "Unsplash",
                        version = null,
                        url = "https://unsplash.com/license",
                        attributionText = "Photo by $creatorName on Unsplash",
                        evidence = "api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    /**
     * 3. Pixabay (官方 API Key 检索)
     */
    private fun fetchPixabay(query: String, page: Int): List<UnifiedImage> {
        val url = "https://pixabay.com/api/?key=$PIXABAY_API_KEY&q=${URLEncoder.encode(query, "UTF-8")}&page=$page&per_page=16&safesearch=true&image_type=all"
        val request = Request.Builder().url(url).header("User-Agent", COMPLIANT_USER_AGENT).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("hits")) return emptyList()
        val hits = root.getJSONArray("hits")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until hits.length()) {
            val obj = hits.getJSONObject(i)
            val id = if (obj.has("id")) obj.getLong("id").toString() else continue
            val previewUrl = if (obj.has("webformatURL")) obj.getString("webformatURL") else continue
            val largeUrl = if (obj.has("largeImageURL")) obj.getString("largeImageURL") else previewUrl
            val thumbUrl = if (obj.has("previewURL")) obj.getString("previewURL") else previewUrl

            val w = obj.optInt("imageWidth", 1200)
            val h = obj.optInt("imageHeight", 800)
            val tagsStr = obj.optString("tags", "Pixabay")
            val tags = tagsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

            val user = obj.optString("user", "Pixabay Artist")
            val pageUrl = obj.optString("pageURL", "https://pixabay.com")

            items.add(
                UnifiedImage(
                    id = "pixabay_$id",
                    source = "pixabay",
                    sourceAssetId = id,
                    kind = "artwork",
                    title = tags.joinToString(" / "),
                    altText = tagsStr,
                    width = w,
                    height = h,
                    aspectRatio = w.toFloat() / h.toFloat(),
                    tags = tags,
                    color = null,
                    renditions = Renditions(
                        thumbnail = thumbUrl,
                        preview = previewUrl,
                        large = largeUrl
                    ),
                    creator = Creator(name = user, profileUrl = null),
                    landingPageUrl = pageUrl,
                    license = LicenseInfo(
                        licenseClass = "free",
                        code = "Pixabay License",
                        version = null,
                        url = "https://pixabay.com/service/license/",
                        attributionText = "Image by $user from Pixabay",
                        evidence = "api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    /**
     * 4. Pexels (官方 API Key 检索)
     */
    private fun fetchPexels(query: String, page: Int): List<UnifiedImage> {
        val url = "https://api.pexels.com/v1/search?query=${URLEncoder.encode(query, "UTF-8")}&page=$page&per_page=12"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", PEXELS_API_KEY)
            .header("User-Agent", COMPLIANT_USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("photos")) return emptyList()
        val photos = root.getJSONArray("photos")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until photos.length()) {
            val obj = photos.getJSONObject(i)
            val id = if (obj.has("id")) obj.getLong("id").toString() else continue
            val src = if (obj.has("src")) obj.getJSONObject("src") else continue

            val mediumUrl = if (src.has("medium")) src.getString("medium") else continue
            val largeUrl = if (src.has("large2x")) src.getString("large2x") else if (src.has("large")) src.getString("large") else mediumUrl
            val originalUrl = if (src.has("original")) src.getString("original") else largeUrl
            val smallUrl = if (src.has("small")) src.getString("small") else mediumUrl

            val w = obj.optInt("width", 1200)
            val h = obj.optInt("height", 800)
            val alt = obj.optString("alt", "Pexels Photo")
            val photographer = obj.optString("photographer", "Pexels Photographer")
            val pUrl = obj.optString("url", "https://www.pexels.com")

            items.add(
                UnifiedImage(
                    id = "pexels_$id",
                    source = "pexels",
                    sourceAssetId = id,
                    kind = "photo",
                    title = alt.ifBlank { "Pexels 精选摄影" },
                    altText = alt,
                    width = w,
                    height = h,
                    aspectRatio = w.toFloat() / h.toFloat(),
                    tags = listOf("Pexels", "摄影", "生活", "自然"),
                    color = obj.optString("avg_color", null),
                    renditions = Renditions(
                        thumbnail = smallUrl,
                        preview = largeUrl,
                        large = originalUrl
                    ),
                    creator = Creator(name = photographer, profileUrl = obj.optString("photographer_url", null)),
                    landingPageUrl = pUrl,
                    license = LicenseInfo(
                        licenseClass = "free",
                        code = "Pexels License",
                        version = null,
                        url = "https://www.pexels.com/license/",
                        attributionText = "Photo by $photographer on Pexels",
                        evidence = "api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    /**
     * 5. The Met (大都会博物馆)
     */
    private fun fetchMet(query: String, page: Int): List<UnifiedImage> {
        val searchUrl = "https://collectionapi.metmuseum.org/public/collection/v1/search?q=${URLEncoder.encode(query, "UTF-8")}&hasImages=true"
        val searchReq = Request.Builder().url(searchUrl).header("User-Agent", COMPLIANT_USER_AGENT).build()
        val searchRes = client.newCall(searchReq).execute()
        if (!searchRes.isSuccessful) return emptyList()

        val root = JSONObject(searchRes.body?.string() ?: "")
        if (!root.has("objectIDs") || root.isNull("objectIDs")) return emptyList()
        val objectIDs = root.getJSONArray("objectIDs")
        val total = objectIDs.length()
        if (total == 0) return emptyList()

        val pageSize = 6
        val startIndex = (page - 1) * pageSize
        if (startIndex >= total) return emptyList()
        val endIndex = minOf(startIndex + pageSize, total)

        val items = mutableListOf<UnifiedImage>()
        for (i in startIndex until endIndex) {
            val objId = objectIDs.getInt(i)
            try {
                val detailUrl = "https://collectionapi.metmuseum.org/public/collection/v1/objects/$objId"
                val detailReq = Request.Builder().url(detailUrl).header("User-Agent", COMPLIANT_USER_AGENT).build()
                val detailRes = client.newCall(detailReq).execute()
                if (!detailRes.isSuccessful) continue

                val obj = JSONObject(detailRes.body?.string() ?: "")
                val primaryImage = obj.optString("primaryImage", "")
                val primaryImageSmall = obj.optString("primaryImageSmall", primaryImage)
                if (primaryImage.isBlank()) continue

                val title = obj.optString("title", "Met Masterpiece")
                val artist = obj.optString("artistDisplayName", "Unknown Artist")

                val tagsList = mutableListOf<String>()
                tagsList.add("The Met")
                tagsList.add("艺术品")
                if (obj.has("tags") && !obj.isNull("tags")) {
                    val tArr = obj.getJSONArray("tags")
                    for (t in 0 until minOf(tArr.length(), 3)) {
                        val tObj = tArr.getJSONObject(t)
                        if (tObj.has("term")) tagsList.add(tObj.getString("term"))
                    }
                }

                items.add(
                    UnifiedImage(
                        id = "met_$objId",
                        source = "met",
                        sourceAssetId = objId.toString(),
                        kind = "artwork",
                        title = title,
                        altText = "$title by $artist",
                        width = 1200,
                        height = 1000,
                        aspectRatio = 1.2f,
                        tags = tagsList,
                        color = null,
                        renditions = Renditions(
                            thumbnail = primaryImageSmall,
                            preview = primaryImageSmall,
                            large = primaryImage
                        ),
                        creator = Creator(name = artist, profileUrl = null),
                        landingPageUrl = obj.optString("objectURL", "https://www.metmuseum.org/art/collection/search/$objId"),
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
            } catch (_: Exception) {
            }
        }
        return items
    }

    /**
     * 6. Bing 4K (微软官方必应每日高清风光大片)
     */
    private fun fetchBing(query: String, page: Int): List<UnifiedImage> {
        val url = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=8&mkt=zh-CN"
        val request = Request.Builder().url(url).header("User-Agent", COMPLIANT_USER_AGENT).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val root = JSONObject(response.body?.string() ?: "")
        if (!root.has("images")) return emptyList()
        val images = root.getJSONArray("images")

        val items = mutableListOf<UnifiedImage>()
        for (i in 0 until images.length()) {
            val obj = images.getJSONObject(i)
            val imgUrlPart = if (obj.has("url")) obj.getString("url") else continue
            val baseRaw = if (imgUrlPart.startsWith("http")) imgUrlPart else "https://cn.bing.com$imgUrlPart"

            val largeUrl = if (baseRaw.contains("&")) baseRaw.substringBefore("&") + "&qlt=100" else baseRaw
            val thumbUrl = "$largeUrl&w=480"
            val title = obj.optString("copyright", "Bing 4K Wallpaper")

            items.add(
                UnifiedImage(
                    id = "bing_${obj.optString("hsh", i.toString())}",
                    source = "bing",
                    sourceAssetId = obj.optString("hsh", i.toString()),
                    kind = "photo",
                    title = title,
                    altText = title,
                    width = 3840,
                    height = 2160,
                    aspectRatio = 16f / 9f,
                    tags = listOf("必应4K", "风景", "精选摄影", "每日壁纸"),
                    color = null,
                    renditions = Renditions(
                        thumbnail = thumbUrl,
                        preview = thumbUrl,
                        large = largeUrl
                    ),
                    creator = Creator(name = "Microsoft Bing", profileUrl = null),
                    landingPageUrl = "https://cn.bing.com",
                    license = LicenseInfo(
                        licenseClass = "free",
                        code = "Bing Daily",
                        version = null,
                        url = "https://cn.bing.com",
                        attributionText = "© Microsoft Bing Daily Wallpaper",
                        evidence = "api"
                    ),
                    actionPolicy = ActionPolicy(canShowInSearch = true, canOfferDownload = true, canSetAsWallpaper = true)
                )
            )
        }
        return items
    }

    suspend fun diagnoseNetwork(): Map<String, Long> = withContext(Dispatchers.IO) {
        val targets = mapOf(
            "Unsplash 摄影社区" to "https://api.unsplash.com",
            "Pixabay 400万素材" to "https://pixabay.com/api/",
            "Pexels 精美摄影" to "https://api.pexels.com/v1/",
            "GIPHY 动图 / 必应动图通道" to "https://cn.bing.com",
            "The Met 博物馆" to "https://collectionapi.metmuseum.org/public/collection/v1/search?q=art",
            "微软 Bing 4K" to "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1"
        )

        val results = mutableMapOf<String, Long>()
        targets.forEach { (name, url) ->
            val start = System.currentTimeMillis()
            try {
                val req = Request.Builder().url(url).header("User-Agent", COMPLIANT_USER_AGENT).build()
                val res = client.newCall(req).execute()
                val latency = System.currentTimeMillis() - start
                results[name] = if (res.isSuccessful || res.code in 400..403) latency else -1
            } catch (_: Exception) {
                results[name] = -1
            }
        }
        results
    }
}
