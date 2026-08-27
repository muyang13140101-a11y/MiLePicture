import { ProviderId, SourceInfo } from '../types/image';

export interface ProviderPolicy {
  enabled: boolean;
  releaseState: 'enabled' | 'pending_approval' | 'disabled';
  search: boolean;
  directBinaryHotlink: 'required' | 'temporary_only' | 'not_assumed';
  allowSetAsWallpaper: boolean;
  adultMode: 'forced_off' | 'provider_default';
  licenseMode: 'work_level' | 'source_policy' | 'public_domain_flag' | 'unknown';
}

export const PROVIDER_POLICY: Record<ProviderId, ProviderPolicy> = {
  openverse: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'not_assumed',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'work_level',
  },
  met: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'not_assumed',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'public_domain_flag',
  },
  wikimedia: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'not_assumed',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'work_level',
  },
  unsplash: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'required',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'source_policy',
  },
  pixabay: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'temporary_only',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'source_policy',
  },
  wallhaven: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'not_assumed',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'unknown',
  },
  pexels: {
    enabled: true,
    releaseState: 'enabled',
    search: true,
    directBinaryHotlink: 'not_assumed',
    allowSetAsWallpaper: false,
    adultMode: 'forced_off',
    licenseMode: 'source_policy',
  },
};

export function getSourcesMetadata(): SourceInfo[] {
  return [
    {
      id: 'unsplash',
      name: 'Unsplash',
      description: '全球高品质创作者摄影社区',
      enabled: PROVIDER_POLICY.unsplash.enabled,
      releaseState: PROVIDER_POLICY.unsplash.releaseState,
      requiresKey: true,
      isKeyConfigured: Boolean(process.env.UNSPLASH_ACCESS_KEY),
      licenseHighlights: 'Unsplash 免费商业/非商业授权（需保留摄影师署名）',
    },
    {
      id: 'pixabay',
      name: 'Pixabay',
      description: '超过 400 万张免版税插画、矢量与高清摄影',
      enabled: PROVIDER_POLICY.pixabay.enabled,
      releaseState: PROVIDER_POLICY.pixabay.releaseState,
      requiresKey: true,
      isKeyConfigured: Boolean(process.env.PIXABAY_API_KEY),
      licenseHighlights: 'Pixabay Content License (遵从24小时缓存协议)',
    },
    {
      id: 'pexels',
      name: 'Pexels',
      description: '全球精选免版税摄影与视频素材库',
      enabled: PROVIDER_POLICY.pexels.enabled,
      releaseState: PROVIDER_POLICY.pexels.releaseState,
      requiresKey: true,
      isKeyConfigured: Boolean(process.env.PEXELS_API_KEY),
      licenseHighlights: 'Pexels License (可免费商用与个人使用，需保留署名)',
    },
    {
      id: 'wallhaven',
      name: 'Wallhaven',
      description: '顶级高清壁纸社区 (严格执行 SFW 纯净安全过滤)',
      enabled: PROVIDER_POLICY.wallhaven.enabled,
      releaseState: PROVIDER_POLICY.wallhaven.releaseState,
      requiresKey: false,
      isKeyConfigured: Boolean(process.env.WALLHAVEN_API_KEY),
      licenseHighlights: '社区壁纸分享（保留原作者与作品页）',
    },
    {
      id: 'openverse',
      name: 'Openverse',
      description: 'Creative Commons 开放许可海量多媒体图库',
      enabled: PROVIDER_POLICY.openverse.enabled,
      releaseState: PROVIDER_POLICY.openverse.releaseState,
      requiresKey: false,
      isKeyConfigured: true,
      licenseHighlights: 'CC0 / CC-BY / 开放共享协议，标明作品级授权',
    },
    {
      id: 'met',
      name: 'The Met (大都会博物馆)',
      description: '纽约大都会艺术博物馆公有领域馆藏作品与经典艺术',
      enabled: PROVIDER_POLICY.met.enabled,
      releaseState: PROVIDER_POLICY.met.releaseState,
      requiresKey: false,
      isKeyConfigured: true,
      licenseHighlights: 'Public Domain (公有领域 CC0)',
    },
    {
      id: 'wikimedia',
      name: 'Wikimedia Commons (维基共享)',
      description: '维基媒体基金会自由授权图片与历史纪实档案',
      enabled: PROVIDER_POLICY.wikimedia.enabled,
      releaseState: PROVIDER_POLICY.wikimedia.releaseState,
      requiresKey: false,
      isKeyConfigured: true,
      licenseHighlights: 'GFDL / CC-BY-SA / Public Domain',
    },
  ];
}
