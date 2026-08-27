package com.milepicture.app.data.engine

/**
 * 手机本地内置的高性能中英文语义映射与翻译引擎
 * 完全在手机本地运行，0 毫秒延迟，无需联网即可将中文搜索词转换为高精准英文关键词
 */
object LocalTranslator {

    private val KEYWORD_MAP = mapOf(
        "花" to "flower",
        "花朵" to "flowers blooming",
        "樱花" to "cherry blossom sakura",
        "荷花" to "lotus flower",
        "玫瑰" to "rose flower",
        "向日葵" to "sunflower",
        "风景" to "landscape nature scenery",
        "自然" to "nature landscape",
        "山" to "mountain peak",
        "雪山" to "snow mountain alp",
        "海" to "ocean sea beach",
        "大海" to "ocean blue sea",
        "湖泊" to "lake water",
        "河流" to "river stream",
        "森林" to "forest trees woodland",
        "树" to "tree green",
        "天空" to "sky clouds",
        "星空" to "galaxy starry sky milky way",
        "宇宙" to "space cosmos universe galaxy",
        "月亮" to "moon night",
        "太阳" to "sun sunshine sunrise sunset",
        "日落" to "sunset golden hour",
        "日出" to "sunrise morning",
        "云" to "clouds cloudy sky",
        "雨" to "rain rainy raindrops",
        "雪" to "snow winter frosty",
        "冬天" to "winter snow cold",
        "秋天" to "autumn fall leaves",
        "春天" to "spring blossom flowers",
        "夏天" to "summer beach sunshine",
        "猫" to "cat kitten feline",
        "小猫" to "cute kitten cat",
        "狗" to "dog puppy canine",
        "小狗" to "puppy cute dog",
        "鸟" to "bird wildlife avian",
        "动物" to "animals wildlife fauna",
        "萌宠" to "cute pets animals",
        "鱼" to "fish underwater marine",
        "海洋" to "ocean underwater sea life",
        "城市" to "city urban cityscape",
        "建筑" to "architecture modern building",
        "街道" to "street city urban alley",
        "夜景" to "night city lights neon",
        "赛博朋克" to "cyberpunk neon sci-fi futuristic",
        "科技" to "technology futuristic cyber",
        "极简" to "minimalist minimalism texture clean",
        "插画" to "illustration artistic artwork",
        "绘画" to "painting artwork art canvas",
        "艺术" to "art artwork masterpiece",
        "古典" to "classical vintage ancient art",
        "国画" to "chinese painting traditional art",
        "水彩" to "watercolor painting art",
        "油画" to "oil painting classical artwork",
        "动漫" to "anime manga artwork",
        "二次元" to "anime illustration concept art",
        "壁纸" to "wallpaper background 4k",
        "高清壁纸" to "wallpaper aesthetic 4k background",
        "人物" to "portrait person human",
        "女人" to "woman portrait beauty",
        "女孩" to "girl portrait aesthetic",
        "男人" to "man portrait aesthetic",
        "肖像" to "portrait face aesthetic",
        "汽车" to "car supercar automobile vehicle",
        "跑车" to "sports car supercar exotic vehicle",
        "复古" to "vintage retro aesthetic nostalgic",
        "暗黑" to "dark aesthetic moody shadows",
        "纯色" to "solid color minimalist gradient",
        "纹理" to "texture background pattern abstract",
        "美食" to "food culinary gourmet delicious",
        "咖啡" to "coffee cafe aesthetic",
        "茶" to "tea leaves traditional",
        "国风" to "chinese traditional culture orient",
        "故宫" to "forbidden city ancient architecture palace",
        "大都会" to "metropolitan museum classical art",
        "博物馆" to "museum sculpture painting artifact",
        "雕塑" to "sculpture statue classical art"
    )

    private val CHINESE_REGEX = Regex("[\\u4e00-\\u9fa5]")

    fun translate(query: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return "art"

        // 1. 精准匹配
        KEYWORD_MAP[trimmed.lowercase()]?.let { return it }

        // 2. 包含词替换
        var result = trimmed.lowercase()
        var hasMatch = false
        for ((cn, en) in KEYWORD_MAP) {
            if (result.contains(cn)) {
                result = result.replace(cn, " $en ")
                hasMatch = true
            }
        }

        if (hasMatch) {
            return result.replace(Regex("\\s+"), " ").trim()
        }

        // 3. 中文字符兜底
        if (CHINESE_REGEX.containsMatchIn(trimmed)) {
            return "$trimmed art photo"
        }

        return trimmed
    }
}
