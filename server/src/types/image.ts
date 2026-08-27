export type ProviderId =
  | 'openverse'
  | 'met'
  | 'wikimedia'
  | 'unsplash'
  | 'pixabay'
  | 'wallhaven'
  | 'pexels';

export type LicenseClass =
  | 'public_domain'
  | 'cc0'
  | 'cc_by'
  | 'cc_by_sa'
  | 'cc_noncommercial'
  | 'source_license'
  | 'unknown';

export interface UnifiedImage {
  id: string; // 唯一格式: `${source}:${sourceAssetId}`
  source: ProviderId;
  sourceAssetId: string;
  kind: 'photo' | 'illustration' | 'vector' | 'artwork' | 'wallpaper' | 'unknown';
  title?: string;
  altText?: string;
  width?: number;
  height?: number;
  aspectRatio?: number;
  tags: string[];
  color?: string;
  renditions: {
    thumbnail: string;
    preview?: string;
    large?: string;
  };
  creator: {
    name?: string;
    profileUrl?: string;
  };
  landingPageUrl: string;
  license: {
    class: LicenseClass;
    code?: string;
    version?: string;
    url?: string;
    attributionText?: string;
    evidence: 'api' | 'detail_lookup' | 'source_policy' | 'public_domain_flag' | 'unknown';
  };
  actionPolicy: {
    canShowInSearch: boolean;
    canOfferDownload: boolean;
    canSetAsWallpaper: boolean;
    requiresAttribution: boolean | 'unknown';
  };
}

export interface SearchIntent {
  query?: string;
  includeTags?: string[];
  excludeTags?: string[];
  type?: 'photo' | 'illustration' | 'vector' | 'artwork' | 'wallpaper';
  orientation?: 'landscape' | 'portrait' | 'square';
  sourceIds?: ProviderId[];
  licenseFilter?: 'all' | 'public_domain_cc0' | 'commercial_candidates';
  safeMode: boolean;
  page: number;
  pageSize: number;
}

export interface ProviderSearchResult {
  providerId: ProviderId;
  status: 'ok' | 'degraded' | 'rate_limited' | 'disabled' | 'error';
  items: UnifiedImage[];
  totalEstimated?: number;
  errorMessage?: string;
}

export interface SourceInfo {
  id: ProviderId;
  name: string;
  description: string;
  enabled: boolean;
  releaseState: 'enabled' | 'pending_approval' | 'disabled';
  requiresKey: boolean;
  isKeyConfigured: boolean;
  licenseHighlights: string;
}
