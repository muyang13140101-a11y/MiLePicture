import axios from 'axios';
import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';

export class PexelsAdapter extends BaseAdapter {
  readonly id: ProviderId = 'pexels';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    const apiKey = process.env.PEXELS_API_KEY;
    if (!apiKey) {
      return {
        providerId: this.id,
        status: 'disabled',
        items: [],
        errorMessage: 'Pexels API Key 未配置。请在 server/.env 中填入 PEXELS_API_KEY',
      };
    }

    try {
      const query = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'nature');
      const page = intent.page || 1;
      const perPage = Math.min(intent.pageSize || 15, 30);

      const params: Record<string, any> = {
        query,
        page,
        per_page: perPage,
      };

      if (intent.orientation) {
        params.orientation = intent.orientation === 'landscape' ? 'landscape' : intent.orientation === 'portrait' ? 'portrait' : 'square';
      }

      const res = await axios.get('https://api.pexels.com/v1/search', {
        params,
        headers: {
          Authorization: apiKey,
        },
        timeout: 8000,
      });

      const photos = res.data.photos || [];
      const items: UnifiedImage[] = photos.map((photo: any) => {
        const width = photo.width || 1200;
        const height = photo.height || 800;

        return {
          id: `pexels:${photo.id}`,
          source: 'pexels',
          sourceAssetId: String(photo.id),
          kind: 'photo',
          title: photo.alt || `Pexels Photo ${photo.id}`,
          altText: photo.alt,
          width,
          height,
          aspectRatio: width / height,
          tags: [photo.alt || 'photo', 'Pexels', 'Free'].filter(Boolean),
          color: photo.avg_color || undefined,
          renditions: {
            thumbnail: photo.src?.medium || photo.src?.small || photo.src?.tiny,
            preview: photo.src?.large || photo.src?.large2x || photo.src?.medium,
            large: photo.src?.original || photo.src?.large2x,
          },
          creator: {
            name: photo.photographer || 'Pexels Photographer',
            profileUrl: photo.photographer_url || undefined,
          },
          landingPageUrl: photo.url || 'https://www.pexels.com',
          license: {
            class: 'source_license',
            code: 'Pexels License',
            url: 'https://www.pexels.com/license/',
            attributionText: `Photo by ${photo.photographer} on Pexels`,
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
        totalEstimated: res.data.total_results,
      };
    } catch (err: any) {
      const isRateLimit = err.response?.status === 429;
      return {
        providerId: this.id,
        status: isRateLimit ? 'rate_limited' : 'error',
        items: [],
        errorMessage: err.response?.data?.message || err.message,
      };
    }
  }
}
