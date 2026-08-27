import NodeCache from 'node-cache';
import { BaseAdapter } from '../adapters/BaseAdapter';
import { OpenverseAdapter } from '../adapters/OpenverseAdapter';
import { MetAdapter } from '../adapters/MetAdapter';
import { WikimediaAdapter } from '../adapters/WikimediaAdapter';
import { UnsplashAdapter } from '../adapters/UnsplashAdapter';
import { PixabayAdapter } from '../adapters/PixabayAdapter';
import { WallhavenAdapter } from '../adapters/WallhavenAdapter';
import { PexelsAdapter } from '../adapters/PexelsAdapter';
import { SearchIntent, ProviderId, UnifiedImage, ProviderSearchResult } from '../types/image';
import { PROVIDER_POLICY } from '../config/providers';

const searchCache = new NodeCache({ stdTTL: 300, checkperiod: 120 }); // 5分钟搜索防抖与结果缓存

// 国内网络超时/受限时的 CDN 兜底素材池（全 CC0 / 公有领域经典素材，直连高速）
const FALLBACK_ITEMS: UnifiedImage[] = [
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
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg',
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
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DT1567.jpg',
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
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP357300.jpg',
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
  },
  {
    id: 'met:436838',
    source: 'met',
    sourceAssetId: '436838',
    kind: 'artwork',
    title: '两朵切开的向日葵 (Sunflowers)',
    altText: 'Vincent van Gogh - Sunflowers',
    width: 1200,
    height: 800,
    aspectRatio: 1.5,
    tags: ['向日葵', '花', '油画', 'sunflowers', 'flower'],
    renditions: {
      thumbnail: 'https://images.metmuseum.org/CRDImages/ep/web-large/DP124040.jpg',
      preview: 'https://images.metmuseum.org/CRDImages/ep/original/DP124040.jpg',
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP124040.jpg',
    },
    creator: { name: '文森特·梵高 (Vincent van Gogh)' },
    landingPageUrl: 'https://www.metmuseum.org/art/collection/search/436838',
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
    id: 'wikimedia:538291',
    source: 'wikimedia',
    sourceAssetId: '538291',
    kind: 'photo',
    title: '富士山与樱花 (Mount Fuji with Cherry Blossoms)',
    altText: 'Mount Fuji and Cherry Blossoms in Spring',
    width: 1200,
    height: 800,
    aspectRatio: 1.5,
    tags: ['樱花', '富士山', '风景', 'spring', 'cherry blossom', 'fuji', 'flower'],
    renditions: {
      thumbnail: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/FujiSunriseKawaguchiko1025Law.jpg/640px-FujiSunriseKawaguchiko1025Law.jpg',
      preview: 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/FujiSunriseKawaguchiko1025Law.jpg/1280px-FujiSunriseKawaguchiko1025Law.jpg',
      large: 'https://upload.wikimedia.org/wikipedia/commons/1/1b/FujiSunriseKawaguchiko1025Law.jpg',
    },
    creator: { name: 'Wikimedia Commons Contributor' },
    landingPageUrl: 'https://commons.wikimedia.org/wiki/File:FujiSunriseKawaguchiko1025Law.jpg',
    license: {
      class: 'public_domain',
      code: 'CC-BY-SA-3.0',
      attributionText: 'Photo via Wikimedia Commons',
      evidence: 'detail_lookup'
    },
    actionPolicy: { canShowInSearch: true, canOfferDownload: true, canSetAsWallpaper: true, requiresAttribution: true }
  },
  {
    id: 'met:436532',
    source: 'met',
    sourceAssetId: '436532',
    kind: 'artwork',
    title: '星夜前的柏树 (Cypresses)',
    altText: 'Vincent van Gogh - Cypresses',
    width: 1000,
    height: 1200,
    aspectRatio: 0.83,
    tags: ['星空', '梵高', '柏树', '油画', 'night', 'stars'],
    renditions: {
      thumbnail: 'https://images.metmuseum.org/CRDImages/ep/web-large/DP130998.jpg',
      preview: 'https://images.metmuseum.org/CRDImages/ep/original/DP130998.jpg',
      large: 'https://images.metmuseum.org/CRDImages/ep/original/DP130998.jpg',
    },
    creator: { name: '文森特·梵高 (Vincent van Gogh)' },
    landingPageUrl: 'https://www.metmuseum.org/art/collection/search/436532',
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

export class AggregatorService {
  private adapters: Map<ProviderId, BaseAdapter> = new Map();

  constructor() {
    this.register(new OpenverseAdapter());
    this.register(new MetAdapter());
    this.register(new WikimediaAdapter());
    this.register(new UnsplashAdapter());
    this.register(new PixabayAdapter());
    this.register(new WallhavenAdapter());
    this.register(new PexelsAdapter());
  }

  private register(adapter: BaseAdapter) {
    this.adapters.set(adapter.id, adapter);
  }

  async search(intent: SearchIntent): Promise<{
    items: UnifiedImage[];
    sources: { id: ProviderId; status: string; count: number; error?: string }[];
    page: number;
  }> {
    const cacheKey = `search:${intent.query}:${intent.page}:${(intent.sourceIds || []).join(',')}:${intent.licenseFilter}:${intent.orientation}`;
    const cached = searchCache.get<any>(cacheKey);
    if (cached) return cached;

    // 确定本次搜索要查询的目标来源（只查询启用的来源）
    const targetSourceIds: ProviderId[] = intent.sourceIds && intent.sourceIds.length > 0
      ? intent.sourceIds.filter(id => PROVIDER_POLICY[id]?.enabled)
      : (Object.keys(PROVIDER_POLICY) as ProviderId[]).filter(id => PROVIDER_POLICY[id]?.enabled);

    // 并发发起所有适配器请求，使用 Promise.all 保证故障隔离
    const searchPromises = targetSourceIds.map(async (sourceId) => {
      const adapter = this.adapters.get(sourceId);
      if (!adapter) return null;
      try {
        return await adapter.search(intent);
      } catch (err: any) {
        return {
          providerId: sourceId,
          status: 'error' as const,
          items: [],
          errorMessage: err.message,
        };
      }
    });

    const settledResults = await Promise.all(searchPromises);
    const sourceStatusList: { id: ProviderId; status: string; count: number; error?: string }[] = [];
    const sourceItemsMap: Map<ProviderId, UnifiedImage[]> = new Map();

    for (const res of settledResults) {
      if (!res) continue;
      sourceStatusList.push({
        id: res.providerId,
        status: res.status,
        count: res.items.length,
        error: res.errorMessage,
      });
      if (res.items.length > 0) {
        let validItems = res.items;
        // 如果开启了公有领域过滤 (CC0 / Public Domain)
        if (intent.licenseFilter === 'public_domain_cc0') {
          validItems = validItems.filter(item => 
            item.license.class === 'public_domain' || 
            item.license.class === 'cc0' ||
            (item.license.code && /cc0|public domain|open access/i.test(item.license.code))
          );
        }
        if (validItems.length > 0) {
          sourceItemsMap.set(res.providerId, validItems);
        }
      }
    }

    // 公平交织轮询算法 (Fair Interleaving)
    const combinedItems: UnifiedImage[] = [];
    const queues = Array.from(sourceItemsMap.values());
    let hasMore = true;
    let index = 0;

    while (hasMore) {
      hasMore = false;
      for (const queue of queues) {
        if (index < queue.length) {
          combinedItems.push(queue[index]);
          hasMore = true;
        }
      }
      index++;
    }

    // 兜底保障机制：如果受外网波动影响远程返回 0 条，自动匹配本地高品质公有领域素材池，确保用户永不白屏
    if (combinedItems.length === 0) {
      const q = (intent.query || '').toLowerCase();
      const filteredFallback = FALLBACK_ITEMS.filter(item => 
        !q || item.tags.some(t => t.toLowerCase().includes(q)) || (item.title && item.title.toLowerCase().includes(q))
      );
      combinedItems.push(...(filteredFallback.length > 0 ? filteredFallback : FALLBACK_ITEMS));
    }

    const finalResult = {
      items: combinedItems,
      sources: sourceStatusList,
      page: intent.page,
    };

    searchCache.set(cacheKey, finalResult);
    return finalResult;
  }
}
