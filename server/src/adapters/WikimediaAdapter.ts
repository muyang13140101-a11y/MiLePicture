import { BaseAdapter } from './BaseAdapter';
import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';
import { createHttpClient } from '../utils/httpClient';
import { translateQueryToEnglish } from '../utils/translator';

const http = createHttpClient(6000);

export class WikimediaAdapter extends BaseAdapter {
  readonly id: ProviderId = 'wikimedia';

  async search(intent: SearchIntent): Promise<ProviderSearchResult> {
    try {
      const rawQuery = intent.query || (intent.includeTags && intent.includeTags.length > 0 ? intent.includeTags.join(' ') : 'architecture');
      const query = translateQueryToEnglish(rawQuery);
      const pageSize = Math.min(intent.pageSize || 15, 20);

      const params = {
        action: 'query',
        generator: 'search',
        gsrsearch: query,
        gsrnamespace: '6', // File namespace
        gsrlimit: pageSize,
        prop: 'imageinfo',
        iiprop: 'url|size|extmetadata|thumbmime',
        iiurlwidth: 600,
        format: 'json',
        origin: '*',
      };

      const res = await http.get('https://commons.wikimedia.org/w/api.php', {
        params,
      });

      const pages = res.data?.query?.pages || {};
      const items: UnifiedImage[] = [];

      for (const key of Object.keys(pages)) {
        const p = pages[key];
        const info = p.imageinfo && p.imageinfo[0];
        if (!info || !info.thumburl) continue;

        const ext = info.extmetadata || {};
        const title = (p.title || '').replace(/^File:/i, '');
        const artist = ext.Artist?.value ? ext.Artist.value.replace(/<[^>]*>?/gm, '') : 'Wikimedia Commons';
        const licenseShort = ext.LicenseShortName?.value || 'CC-BY-SA';
        const width = info.width || 800;
        const height = info.height || 600;

        items.push({
          id: `wikimedia:${p.pageid}`,
          source: 'wikimedia',
          sourceAssetId: String(p.pageid),
          kind: 'photo',
          title: title,
          altText: title,
          width,
          height,
          aspectRatio: width / height,
          tags: [title.split(' ')[0], 'Wikimedia', 'Commons'].filter(Boolean),
          renditions: {
            thumbnail: info.thumburl,
            preview: info.url || info.thumburl,
            large: info.url,
          },
          creator: {
            name: artist.slice(0, 50),
          },
          landingPageUrl: info.descriptionurl || `https://commons.wikimedia.org/wiki/${encodeURIComponent(p.title)}`,
          license: {
            class: licenseShort.toLowerCase().includes('public') ? 'public_domain' : 'cc_by_sa',
            code: licenseShort,
            attributionText: `Photo via Wikimedia Commons: ${artist}`,
            evidence: 'detail_lookup',
          },
          actionPolicy: {
            canShowInSearch: true,
            canOfferDownload: false,
            canSetAsWallpaper: false,
            requiresAttribution: true,
          },
        });
      }

      return {
        providerId: this.id,
        status: 'ok',
        items,
        totalEstimated: items.length,
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
