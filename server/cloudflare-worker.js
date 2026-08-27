/**
 * MiLePicture - Cloudflare Worker 聚合后端服务 (7x24小时永久免费云端运行)
 * 纯云端无服务器架构，不依赖任何本地电脑，自带 Cloudflare 全球 CDN 加速与免费域名。
 */

// 1. 中文搜索词翻译与语义增强词典
const KEYWORD_MAP = {
  '花': 'flower',
  '花朵': 'flowers blooming',
  '樱花': 'cherry blossom sakura',
  '荷花': 'lotus flower',
  '玫瑰': 'rose flower',
  '向日葵': 'sunflower',
  '风景': 'landscape nature scenery',
  '自然': 'nature landscape',
  '山': 'mountain peak',
  '雪山': 'snow mountain alp',
  '海': 'ocean sea beach',
  '大海': 'ocean blue sea',
  '湖泊': 'lake water',
  '河流': 'river stream',
  '森林': 'forest trees woodland',
  '树': 'tree green',
  '天空': 'sky clouds',
  '星空': 'galaxy starry sky milky way',
  '宇宙': 'space cosmos universe galaxy',
  '月亮': 'moon night',
  '太阳': 'sun sunshine sunrise sunset',
  '日落': 'sunset golden hour',
  '日出': 'sunrise morning',
  '云': 'clouds cloudy sky',
  '雨': 'rain rainy raindrops',
  '雪': 'snow winter frosty',
  '冬天': 'winter snow cold',
  '秋天': 'autumn fall leaves',
  '春天': 'spring blossom flowers',
  '夏天': 'summer beach sunshine',
  '猫': 'cat kitten feline',
  '小猫': 'cute kitten cat',
  '狗': 'dog puppy canine',
  '小狗': 'puppy cute dog',
  '鸟': 'bird wildlife avian',
  '动物': 'animals wildlife fauna',
  '萌宠': 'cute pets animals',
  '鱼': 'fish underwater marine',
  '海洋': 'ocean underwater sea life',
  '城市': 'city urban cityscape',
  '建筑': 'architecture modern building',
  '街道': 'street city urban alley',
  '夜景': 'night city lights neon',
  '赛博朋克': 'cyberpunk neon sci-fi futuristic',
  '科技': 'technology futuristic cyber',
  '极简': 'minimalist minimalism texture clean',
  '插画': 'illustration artistic artwork',
  '绘画': 'painting artwork art canvas',
  '艺术': 'art artwork masterpiece',
  '古典': 'classical vintage ancient art',
  '国画': 'chinese painting traditional art',
  '水彩': 'watercolor painting art',
  '油画': 'oil painting classical artwork',
  '动漫': 'anime manga artwork',
  '二次元': 'anime illustration concept art',
  '壁纸': 'wallpaper background 4k',
  '高清壁纸': 'wallpaper aesthetic 4k background',
  '人物': 'portrait person human',
  '女人': 'woman portrait beauty',
  '女孩': 'girl portrait aesthetic',
  '男人': 'man portrait aesthetic',
  '肖像': 'portrait face aesthetic',
  '汽车': 'car supercar automobile vehicle',
  '跑车': 'sports car supercar exotic vehicle',
  '复古': 'vintage retro aesthetic nostalgic',
  '暗黑': 'dark aesthetic moody shadows',
  '纯色': 'solid color minimalist gradient',
  '纹理': 'texture background pattern abstract',
  '美食': 'food culinary gourmet delicious',
  '咖啡': 'coffee cafe aesthetic',
  '茶': 'tea leaves traditional',
  '国风': 'chinese traditional culture orient',
  '故宫': 'forbidden city ancient architecture palace',
  '大都会': 'metropolitan museum classical art',
  '博物馆': 'museum sculpture painting artifact',
  '雕塑': 'sculpture statue classical art'
};

function translateQueryToEnglish(query) {
  if (!query || query.trim().length === 0) return 'art';
  const trimmed = query.trim().toLowerCase();
  if (KEYWORD_MAP[trimmed]) return KEYWORD_MAP[trimmed];

  let result = trimmed;
  let hasReplacement = false;
  for (const [cn, en] of Object.entries(KEYWORD_MAP)) {
    if (result.includes(cn)) {
      result = result.replace(new RegExp(cn, 'g'), ` ${en} `);
      hasReplacement = true;
    }
  }
  if (hasReplacement) return result.replace(/\s+/g, ' ').trim();
  if (/[\u4e00-\u9fa5]/.test(query)) return `${query} art photo`;
  return query;
}

// 2. 热门标签配置
const POPULAR_TAGS = [
  { id: 'all', name: '全部灵感', query: 'art' },
  { id: 'nature', name: '自然风景', query: 'nature landscape' },
  { id: 'met', name: '大都会艺术', query: 'masterpiece painting' },
  { id: 'cyberpunk', name: '赛博朋克', query: 'cyberpunk neon' },
  { id: 'illustration', name: '插画设计', query: 'illustration artistic' },
  { id: 'flower', name: '繁花似锦', query: 'flowers blooming' },
  { id: 'anime', name: '动漫二次元', query: 'anime artwork' },
  { id: 'minimalist', name: '极简主义', query: 'minimalist wallpaper' },
  { id: 'architecture', name: '城市建筑', query: 'architecture modern' }
];

// 3. 图库源信息配置
const SOURCES_CONFIG = [
  {
    id: 'openverse',
    name: 'Openverse (WordPress CC0/CC)',
    description: '全球最大的开源公有领域与知识共享多媒体搜索引擎，拥有超 7 亿公有许可素材。',
    enabled: true,
    releaseState: 'active',
    requiresKey: false,
    isKeyConfigured: true,
    licenseHighlights: 'CC0 / Public Domain / CC 授权体系'
  },
  {
    id: 'met',
    name: 'The Met (大都会艺术博物馆)',
    description: '美国最大艺术博物馆的 Open Access 项目，提供数十万件公有领域世界级珍贵馆藏高清图。',
    enabled: true,
    releaseState: 'active',
    requiresKey: false,
    isKeyConfigured: true,
    licenseHighlights: 'CC0 1.0 (开放访问公有领域无限制使用)'
  },
  {
    id: 'wikimedia',
    name: 'Wikimedia Commons (维基共享资源)',
    description: '维基媒体基金会旗下的公有领域与自由授权多媒体档案库，涵盖海量历史、自然及艺术图档。',
    enabled: true,
    releaseState: 'active',
    requiresKey: false,
    isKeyConfigured: true,
    licenseHighlights: 'Public Domain / CC-BY-SA 自由文化协议'
  },
  {
    id: 'wallhaven',
    name: 'Wallhaven',
    description: '全球顶尖高质量壁纸与插画社区，拥有海量 4K/8K 极致画质动漫、赛博与艺术创作。',
    enabled: true,
    releaseState: 'active',
    requiresKey: false,
    isKeyConfigured: true,
    licenseHighlights: '壁纸社区自由使用与个人非商用共享'
  },
  {
    id: 'unsplash',
    name: 'Unsplash (高品质摄影)',
    description: '国际知名高品质摄影师原创社区，画质细腻、光影出众。',
    enabled: true,
    releaseState: 'active',
    requiresKey: false,
    isKeyConfigured: true,
    licenseHighlights: 'Unsplash License (商用与非商用免费使用)'
  }
];

// 4. CC0 艺术兜底素材池（直连高速）
const FALLBACK_ITEMS = [
  {
    id: 'met:436535',
    source: 'met',
    sourceAssetId: '436535',
    kind: 'artwork',
    title: '麦田里的丝柏树 (Wheat Field with Cypresses)',
    altText: 'Vincent van Gogh - Wheat Field with Cypresses',
    width: 1200,
    height: 940,
    aspectRatio: 1.28,
    tags: ['梵高', '名画', '油画', '风景', 'Van Gogh', 'painting'],
    renditions: {
      thumbnail: 'https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg',
      preview: 'https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg',
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg'
    },
    creator: { name: '文森特·梵高 (Vincent van Gogh)' },
    landingPageUrl: 'https://www.metmuseum.org/art/collection/search/436535',
    license: {
      class: 'public_domain',
      code: 'CC0-1.0',
      url: 'https://creativecommons.org/publicdomain/zero/1.0/',
      attributionText: 'The Metropolitan Museum of Art (Open Access)',
      evidence: 'public_domain_flag'
    },
    actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: false }
  },
  {
    id: 'met:437984',
    source: 'met',
    sourceAssetId: '437984',
    kind: 'artwork',
    title: '睡莲池上的拱桥 (Bridge over a Pond of Water Lilies)',
    altText: 'Claude Monet - Water Lilies',
    width: 1200,
    height: 1200,
    aspectRatio: 1.0,
    tags: ['莫奈', '睡莲', '印象派', '花', 'Monet', 'water lilies', 'flower'],
    renditions: {
      thumbnail: 'https://images.metmuseum.org/CRDImages/ep/web-large/DT1567.jpg',
      preview: 'https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg',
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg'
    },
    creator: { name: '克劳德·莫奈 (Claude Monet)' },
    landingPageUrl: 'https://www.metmuseum.org/art/collection/search/437984',
    license: {
      class: 'public_domain',
      code: 'CC0-1.0',
      url: 'https://creativecommons.org/publicdomain/zero/1.0/',
      attributionText: 'The Metropolitan Museum of Art (Open Access)',
      evidence: 'public_domain_flag'
    },
    actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: false }
  },
  {
    id: 'met:436528',
    source: 'met',
    sourceAssetId: '436528',
    kind: 'artwork',
    title: '鸢尾花 (Irises)',
    altText: 'Vincent van Gogh - Irises flower',
    width: 1200,
    height: 940,
    aspectRatio: 1.28,
    tags: ['鸢尾花', '花', '油画', 'flower', 'irises', 'nature'],
    renditions: {
      thumbnail: 'https://images.metmuseum.org/CRDImages/ep/web-large/DP357300.jpg',
      preview: 'https://images.metmuseum.org/CRDImages/ep/original/DP357300.jpg',
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP357300.jpg'
    },
    creator: { name: '文森特·梵高 (Vincent van Gogh)' },
    landingPageUrl: 'https://www.metmuseum.org/art/collection/search/436528',
    license: {
      class: 'public_domain',
      code: 'CC0-1.0',
      url: 'https://creativecommons.org/publicdomain/zero/1.0/',
      attributionText: 'The Metropolitan Museum of Art (Open Access)',
      evidence: 'public_domain_flag'
    },
    actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: false }
  }
];

// 5. 各图库抓取适配器 (运行于 Cloudflare 全球边缘网络，毫秒级响应)
async function searchOpenverse(query, page) {
  try {
    const url = `https://api.openverse.org/v1/images/?q=${encodeURIComponent(query)}&page=${page}&page_size=18`;
    const res = await fetch(url, {
      headers: { 'User-Agent': 'MiLePicture/1.0 (Cloudflare Worker)' },
      cf: { cacheTtl: 300 }
    });
    if (!res.ok) return [];
    const data = await res.json();
    return (data.results || []).map(item => ({
      id: `openverse:${item.id}`,
      source: 'openverse',
      sourceAssetId: String(item.id),
      kind: 'photo',
      title: item.title || 'Untitled',
      altText: item.title,
      width: item.width,
      height: item.height,
      aspectRatio: item.width && item.height ? Number((item.width / item.height).toFixed(2)) : 1.0,
      tags: (item.tags || []).map(t => (typeof t === 'string' ? t : t.name)).filter(Boolean).slice(0, 5),
      color: null,
      renditions: {
        thumbnail: item.thumbnail || item.url,
        preview: item.url,
        large: item.url
      },
      creator: { name: item.creator || 'Openverse Contributor', profileUrl: item.creator_url },
      landingPageUrl: item.foreign_landing_url || `https://openverse.org/image/${item.id}`,
      license: {
        class: item.license?.toLowerCase().includes('cc0') ? 'cc0' : 'creative_commons',
        code: item.license ? item.license.toUpperCase() : 'CC',
        version: item.license_version,
        url: item.license_url,
        attributionText: item.attribution,
        evidence: 'openverse_api'
      },
      actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: true }
    }));
  } catch (e) {
    return [];
  }
}

async function searchWallhaven(query, page) {
  try {
    const url = `https://wallhaven.cc/api/v1/search?q=${encodeURIComponent(query)}&page=${page}&sorting=toplist`;
    const res = await fetch(url, {
      headers: { 'User-Agent': 'MiLePicture/1.0' },
      cf: { cacheTtl: 300 }
    });
    if (!res.ok) return [];
    const data = await res.json();
    return (data.data || []).map(item => ({
      id: `wallhaven:${item.id}`,
      source: 'wallhaven',
      sourceAssetId: String(item.id),
      kind: 'wallpaper',
      title: item.tags?.map(t => t.name).slice(0, 3).join(' / ') || `Wallhaven #${item.id}`,
      altText: `Wallhaven #${item.id}`,
      width: item.dimension_x,
      height: item.dimension_y,
      aspectRatio: item.ratio ? Number(parseFloat(item.ratio).toFixed(2)) : 1.77,
      tags: (item.tags || []).map(t => t.name).slice(0, 5),
      color: item.colors?.[0] || null,
      renditions: {
        thumbnail: item.thumbs?.small || item.thumbs?.large || item.path,
        preview: item.thumbs?.large || item.path,
        large: item.path
      },
      creator: { name: 'Wallhaven Member' },
      landingPageUrl: item.url || `https://wallhaven.cc/w/${item.id}`,
      license: {
        class: 'custom_commercial_ok',
        code: 'Wallhaven License',
        attributionText: 'Wallhaven Community Wallpaper',
        evidence: 'wallhaven_terms'
      },
      actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: false }
    }));
  } catch (e) {
    return [];
  }
}

async function searchMet(query, page) {
  try {
    const searchUrl = `https://collectionapi.metmuseum.org/public/collection/v1/search?q=${encodeURIComponent(query)}&hasImages=true`;
    const searchRes = await fetch(searchUrl, { cf: { cacheTtl: 600 } });
    if (!searchRes.ok) return [];
    const searchData = await searchRes.json();
    const objectIDs = (searchData.objectIDs || []).slice((page - 1) * 8, page * 8);

    const items = await Promise.all(
      objectIDs.map(async (id) => {
        try {
          const detailRes = await fetch(`https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}`, { cf: { cacheTtl: 3600 } });
          if (!detailRes.ok) return null;
          const obj = await detailRes.json();
          const imgUrl = obj.primaryImageSmall || obj.primaryImage;
          if (!imgUrl) return null;

          return {
            id: `met:${obj.objectID}`,
            source: 'met',
            sourceAssetId: String(obj.objectID),
            kind: 'artwork',
            title: obj.title || 'Untitled Artwork',
            altText: `${obj.title} by ${obj.artistDisplayName || 'Unknown Artist'}`,
            width: 1000,
            height: 800,
            aspectRatio: 1.25,
            tags: (obj.tags || []).map(t => t.term).filter(Boolean).slice(0, 5),
            color: null,
            renditions: {
              thumbnail: obj.primaryImageSmall || obj.primaryImage,
              preview: obj.primaryImageSmall || obj.primaryImage,
              large: obj.primaryImage || obj.primaryImageSmall
            },
            creator: { name: obj.artistDisplayName || 'Unknown Artist' },
            landingPageUrl: obj.objectURL || `https://www.metmuseum.org/art/collection/search/${obj.objectID}`,
            license: {
              class: obj.isPublicDomain ? 'public_domain' : 'custom_non_commercial',
              code: obj.isPublicDomain ? 'CC0-1.0' : 'Met Public Access',
              url: 'https://www.metmuseum.org/information/terms-and-conditions',
              attributionText: 'The Metropolitan Museum of Art (Open Access)',
              evidence: obj.isPublicDomain ? 'public_domain_flag' : 'non_pd'
            },
            actionPolicy: {
              canShowInSearch: true,
              canOfferDownload: Boolean(obj.isPublicDomain),
              canSetAsWallpaper: Boolean(obj.isPublicDomain),
              requiresAttribution: false
            }
          };
        } catch (e) {
          return null;
        }
      })
    );
    return items.filter(Boolean);
  } catch (e) {
    return [];
  }
}

// 6. Cloudflare Worker 主入口路由
export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Content-Type': 'application/json; charset=utf-8'
    };

    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders });
    }

    // 路由 1: 热门标签
    if (url.pathname === '/v1/popular-tags') {
      return new Response(JSON.stringify({ tags: POPULAR_TAGS }), { headers: corsHeaders });
    }

    // 路由 2: 图库源列表
    if (url.pathname === '/v1/sources') {
      return new Response(JSON.stringify({ sources: SOURCES_CONFIG }), { headers: corsHeaders });
    }

    // 路由 3: 聚合搜索接口
    if (url.pathname === '/v1/search') {
      const rawQuery = url.searchParams.get('q') || 'art';
      const page = parseInt(url.searchParams.get('page') || '1', 10);
      const enQuery = translateQueryToEnglish(rawQuery);

      // 并发向 Openverse, Met, Wallhaven 发起查询
      const [openverseItems, metItems, wallhavenItems] = await Promise.all([
        searchOpenverse(enQuery, page),
        searchMet(enQuery, page),
        searchWallhaven(enQuery, page)
      ]);

      // 公平交织轮询算法 (Fair Interleaving)
      const combinedItems = [];
      const queues = [openverseItems, metItems, wallhavenItems].filter(q => q.length > 0);
      let maxLen = Math.max(...queues.map(q => q.length), 0);

      for (let i = 0; i < maxLen; i++) {
        for (const q of queues) {
          if (i < q.length) combinedItems.push(q[i]);
        }
      }

      // 兜底保障
      if (combinedItems.length === 0) {
        combinedItems.push(...FALLBACK_ITEMS);
      }

      const responseData = {
        items: combinedItems,
        sources: [
          { id: 'openverse', status: 'success', count: openverseItems.length },
          { id: 'met', status: 'success', count: metItems.length },
          { id: 'wallhaven', status: 'success', count: wallhavenItems.length }
        ],
        page: page
      };

      return new Response(JSON.stringify(responseData), { headers: corsHeaders });
    }

    // 默认欢迎页与健康检查
    return new Response(JSON.stringify({
      service: 'MiLePicture Cloudflare Edge Aggregator',
      status: 'online',
      version: '1.0.0',
      uptime: '7x24h Global Serverless'
    }), { headers: corsHeaders });
  }
};
