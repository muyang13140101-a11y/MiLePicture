import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);

export class MetAdapter extends BaseAdapter {
  readonly id: ProviderId = 'met';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'painting');
      const query = translateQueryToEnglish(rawQuery);
      
      const searchRes = await http.get('https://collectionapi.metmuseum.org/public/collection/v1/search', {
        params: {
          q: query,
          hasImages: true,
          isPublicDomain: true, // 只检索公有领域
        },
      });

      const objectIDs = searchRes.data.objectIDs || [];
      const page = intent.page || 1;
      const pageSize = Math.min(intent.pageSize || 10, 15);
      const start = (page - 1) * pageSize;
      const selectedIDs = objectIDs.slice(start, start + pageSize);

      if (selectedIDs.length === 0) {
        return { providerId: this.id, status: 'ok', items: [], totalEstimated: objectIDs.length };
      }

      // 并发获取每个 Object 的详情（设超时3500ms，避免卡死）
      const detailHttp = createHttpClient(3500);
      const detailsPromises = selectedIDs.map(async (id: number) => {
        try {
          const detailRes = await detailHttp.get(`https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}`);
          const obj = detailRes.data;
          if (!obj || !obj.primaryImageSmall) return null;

          const tags = (obj.tags || []).map((t: any) => t.term).filter(Boolean);
          if (obj.medium) tags.push(obj.medium);
          if (obj.culture) tags.push(obj.culture);

          const item: UnifiedImage = {
            id: `met:${obj.objectID}`,
            source: 'met',
            sourceAssetId: String(obj.objectID),
            kind: 'artwork',
            title: obj.title || 'Untitled Artwork',
            altText: obj.title,
            width: 1000,
            height: 800,
            aspectRatio: 1.25,
            tags: tags.slice(0, 8),
            renditions: {
              thumbnail: obj.primaryImageSmall,
              preview: obj.primaryImage || obj.primaryImageSmall,
              large: obj.primaryImage || obj.primaryImageSmall,
            },
            creator: {
              name: obj.artistDisplayName || 'Unknown Artist',
              profileUrl: obj.artistWikidata_URL || undefined,
            },
            landingPageUrl: obj.objectURL || 'https://www.metmuseum.org/art/collection',
            license: {
              class: 'public_domain',
              code: 'CC0-1.0',
              url: 'https://creativecommons.org/publicdomain/zero/1.0/',
              attributionText: 'The Metropolitan Museum of Art (Open Access / CC0)',
              evidence: 'public_domain_flag',
            },
            actionPolicy: {
              canShowInSearch: true,
              canOfferDownload: false,
              canSetAsWallpaper: false,
              requiresAttribution: false,
            },
          };
          return item;
        } catch {
          return null;
        }
      });

      const resolved = await Promise.all(detailsPromises);
      const items = resolved.filter((x: UnifiedImage | null): x is UnifiedImage => x !== null);

      return {
        providerId: this.id,
        status: 'ok',
        items,
        totalEstimated: objectIDs.length,
      };
    } catch (err: any) {
      return {
        providerId: this.id,
        status: 'error',
        items: [],
        errorMessage: err.message,
      };
    }
  }
}
