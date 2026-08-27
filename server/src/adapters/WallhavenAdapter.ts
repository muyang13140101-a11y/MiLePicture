import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);

export class WallhavenAdapter extends BaseAdapter {
  readonly id: ProviderId = 'wallhaven';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'nature');
      const query = translateQueryToEnglish(rawQuery);
      const apiKey = process.env.WALLHAVEN_API_KEY;

      const headers: Record<string, string> = {};
      if (apiKey) {
        headers['X-API-Key'] = apiKey;
      }

      const params: Record<string, any> = {
        q: query,
        page: intent.page || 1,
        purity: '100', // 严格强制 SFW 纯净模式（符合移动商店内容合规）
        categories: '111', // General, Anime, People
        sorting: intent.query ? 'relevance' : 'toplist',
        topRange: '1M',
      };

      if (intent.orientation === 'landscape') {
        params.ratios = '16x9,16x10,21x9';
      } else if (intent.orientation === 'portrait') {
        params.ratios = '9x16,10x16';
      }

      const res = await http.get('https://wallhaven.cc/api/v1/search', {
        params,
        headers,
      });

      const list = res.data.data || [];
      const items: UnifiedImage[] = list.map((item: any) => {
        const width = item.dimension_x || 1920;
        const height = item.dimension_y || 1080;

        return {
          id: `wallhaven:${item.id}`,
          source: 'wallhaven',
          sourceAssetId: item.id,
          kind: 'wallpaper',
          title: `Wallhaven #${item.id}`,
          altText: item.id,
          width,
          height,
          aspectRatio: width / height,
          tags: (item.tags || []).map((t: any) => t.name).filter(Boolean),
          color: (item.colors && item.colors[0]) || undefined,
          renditions: {
            thumbnail: item.thumbs?.small || item.thumbs?.large || item.path,
            preview: item.thumbs?.large || item.path,
            large: item.path,
          },
          creator: {
            name: item.uploader?.username || 'Wallhaven Member',
          },
          landingPageUrl: item.url || `https://wallhaven.cc/w/${item.id}`,
          license: {
            class: 'unknown',
            code: 'Wallhaven Community (SFW)',
            url: 'https://wallhaven.cc/help/terms',
            attributionText: 'Wallhaven Wallpaper Community',
            evidence: 'source_policy',
          },
          actionPolicy: {
            canShowInSearch: true,
            canOfferDownload: false,
            canSetAsWallpaper: false,
            requiresAttribution: true,
          },
        };
      });

      return {
        providerId: this.id,
        status: 'ok',
        items,
        totalEstimated: res.data.meta?.total,
      };
    } catch (err: any) {
      return {
        providerId: this.id,
        status: err.response?.status === 429 ? 'rate_limited' : 'error',
        items: [],
        errorMessage: err.response?.data?.error || err.message,
      };
    }
  }
}
