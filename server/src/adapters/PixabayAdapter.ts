import NodeCache from 'node-cache';
import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);
// Pixabay 官方开发者条款硬性要求：请求响应必须缓存 24 小时 (86400秒)
const pixabayCache = new NodeCache({ stdTTL: 86400, checkperiod: 3600 });

export class PixabayAdapter extends BaseAdapter {
  readonly id: ProviderId = 'pixabay';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    const apiKey = process.env.PIXABAY_API_KEY;
    if (!apiKey) {
      return {
        providerId: this.id,
        status: 'disabled',
        items: [],
        errorMessage: 'Pixabay API Key 未配置。请在 server/.env 中填入 PIXABAY_API_KEY',
      };
    }

    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'wallpaper');
      const query = translateQueryToEnglish(rawQuery).slice(0, 100);
      const page = intent.page || 1;
      const pageSize = Math.min(Math.max(intent.pageSize || 20, 3), 50);
      const cacheKey = `pixabay:${query}:${page}:${pageSize}:${intent.orientation || 'all'}`;

      const cached = pixabayCache.get<UnifiedImage[]>(cacheKey);
      if (cached) {
        return {
          providerId: this.id,
          status: 'ok',
          items: cached,
        };
      }

      const res = await http.get('https://pixabay.com/api/', {
        params: {
          key: apiKey,
          q: query,
          page,
          per_page: pageSize,
          safesearch: 'true',
          image_type: intent.type === 'illustration' ? 'illustration' : intent.type === 'vector' ? 'vector' : 'all',
          orientation: intent.orientation === 'landscape' ? 'horizontal' : intent.orientation === 'portrait' ? 'vertical' : 'all',
        },
      });

      const hits = res.data.hits || [];
      const items: UnifiedImage[] = hits.map((hit: any) => {
        const width = hit.imageWidth || 1280;
        const height = hit.imageHeight || 720;
        const tags = (hit.tags || '').split(',').map((t: string) => t.trim()).filter(Boolean);

        return {
          id: `pixabay:${hit.id}`,
          source: 'pixabay',
          sourceAssetId: String(hit.id),
          kind: hit.type === 'illustration' ? 'illustration' : hit.type === 'vector' ? 'vector' : 'photo',
          title: tags.slice(0, 3).join(' / ') || 'Pixabay Image',
          altText: hit.tags,
          width,
          height,
          aspectRatio: width / height,
          tags,
          renditions: {
            thumbnail: hit.previewURL || hit.webformatURL,
            preview: hit.webformatURL,
            large: hit.largeImageURL || hit.webformatURL,
          },
          creator: {
            name: hit.user || 'Pixabay Creator',
            profileUrl: hit.user_id ? `https://pixabay.com/users/${hit.user}-${hit.user_id}/` : undefined,
          },
          landingPageUrl: hit.pageURL || 'https://pixabay.com',
          license: {
            class: 'source_license',
            code: 'Pixabay Content License',
            url: 'https://pixabay.com/service/license-summary/',
            attributionText: `Image by ${hit.user} from Pixabay`,
            evidence: 'source_policy',
          },
          actionPolicy: {
            canShowInSearch: true,
            canOfferDownload: false,
            canSetAsWallpaper: false,
            requiresAttribution: false,
          },
        };
      });

      pixabayCache.set(cacheKey, items);

      return {
        providerId: this.id,
        status: 'ok',
        items,
        totalEstimated: res.data.totalHits,
      };
    } catch (err: any) {
      return {
        providerId: this.id,
        status: err.response?.status === 429 ? 'rate_limited' : 'error',
        items: [],
        errorMessage: err.response?.data || err.message,
      };
    }
  }
}
