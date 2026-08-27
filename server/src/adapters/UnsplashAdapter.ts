import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);

export class UnsplashAdapter extends BaseAdapter {
  readonly id: ProviderId = 'unsplash';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    const accessKey = process.env.UNSPLASH_ACCESS_KEY;
    if (!accessKey) {
      return {
        providerId: this.id,
        status: 'disabled',
        items: [],
        errorMessage: 'Unsplash API Key 未配置。请在 server/.env 中填入 UNSPLASH_ACCESS_KEY',
      };
    }

    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'nature');
      const query = translateQueryToEnglish(rawQuery);

      const res = await http.get('https://api.unsplash.com/search/photos', {
        params: {
          query,
          page: intent.page || 1,
          per_page: Math.min(intent.pageSize || 15, 30),
          orientation: intent.orientation === 'square' ? 'squarish' : intent.orientation,
        },
        headers: {
          Authorization: `Client-ID ${accessKey}`,
          'Accept-Version': 'v1',
        },
      });

      const results = res.data.results || [];
      const items: UnifiedImage[] = results.map((photo: any) => {
        const width = photo.width || 1200;
        const height = photo.height || 800;

        return {
          id: `unsplash:${photo.id}`,
          source: 'unsplash',
          sourceAssetId: photo.id,
          kind: 'photo',
          title: photo.description || photo.alt_description || 'Unsplash Photo',
          altText: photo.alt_description,
          width,
          height,
          aspectRatio: width / height,
          tags: (photo.tags || []).map((t: any) => t.title).filter(Boolean).slice(0, 8),
          color: photo.color,
          renditions: {
            thumbnail: photo.urls?.small || photo.urls?.thumb,
            preview: photo.urls?.regular,
            large: photo.urls?.full || photo.urls?.regular,
          },
          creator: {
            name: photo.user?.name || 'Unsplash Photographer',
            profileUrl: photo.user?.links?.html,
          },
          landingPageUrl: photo.links?.html || 'https://unsplash.com',
          license: {
            class: 'source_license',
            code: 'Unsplash License',
            url: 'https://unsplash.com/license',
            attributionText: `Photo by ${photo.user?.name || 'Photographer'} on Unsplash`,
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
        totalEstimated: res.data.total,
      };
    } catch (err: any) {
      return {
        providerId: this.id,
        status: err.response?.status === 403 || err.response?.status === 429 ? 'rate_limited' : 'error',
        items: [],
        errorMessage: err.message,
      };
    }
  }
}
