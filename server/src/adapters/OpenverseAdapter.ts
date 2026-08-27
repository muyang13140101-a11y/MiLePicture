import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage, LicenseClass } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);

export class OpenverseAdapter extends BaseAdapter {
  readonly id: ProviderId = 'openverse';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'art');
      const query = translateQueryToEnglish(rawQuery);

      const params: Record<string, any> = {
        q: query,
        page: intent.page || 1,
        page_size: Math.min(intent.pageSize || 20, 30),
        mature: false, // 强制过滤敏感内容
      };

      if (intent.licenseFilter === 'public_domain_cc0') {
        params.license = 'pdm,cc0';
      }

      const response = await http.get('https://api.openverse.org/v1/images/', {
        params,
      });

      const results = response.data.results || [];
      const items: UnifiedImage[] = results.map((item: any) => {
        let licenseClass: LicenseClass = 'unknown';
        const lic = (item.license || '').toLowerCase();
        if (lic === 'cc0' || lic === 'pdm') licenseClass = 'public_domain';
        else if (lic.startsWith('by')) licenseClass = 'cc_by';
        else if (lic.startsWith('by-sa')) licenseClass = 'cc_by_sa';
        else if (lic.startsWith('by-nc')) licenseClass = 'cc_noncommercial';

        const width = item.width || 800;
        const height = item.height || 600;

        return {
          id: `openverse:${item.id}`,
          source: 'openverse',
          sourceAssetId: item.id,
          kind: 'photo',
          title: item.title || 'Openverse Artwork',
          altText: item.title,
          width,
          height,
          aspectRatio: width / height,
          tags: item.tags ? item.tags.map((t: any) => (typeof t === 'string' ? t : t.name)).filter(Boolean).slice(0, 8) : [],
          color: undefined,
          renditions: {
            thumbnail: item.thumbnail || item.url,
            preview: item.url,
            large: item.url,
          },
          creator: {
            name: item.creator || 'Openverse Contributor',
            profileUrl: item.creator_url || undefined,
          },
          landingPageUrl: item.foreign_landing_url || item.url,
          license: {
            class: licenseClass,
            code: item.license,
            version: item.license_version,
            url: item.license_url,
            attributionText: item.attribution,
            evidence: 'api',
          },
          actionPolicy: {
            canShowInSearch: true,
            canOfferDownload: false,
            canSetAsWallpaper: false,
            requiresAttribution: licenseClass !== 'public_domain',
          },
        };
      });

      return {
        providerId: this.id,
        status: 'ok',
        items,
        totalEstimated: response.data.result_count,
      };
    } catch (err: any) {
      return {
        providerId: this.id,
        status: err.response?.status === 429 ? 'rate_limited' : 'error',
        items: [],
        errorMessage: err.message,
      };
    }
  }
}
