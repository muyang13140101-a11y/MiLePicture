import { ProviderId, SearchIntent, ProviderSearchResult, UnifiedImage } from '../types/image';

export abstract class BaseAdapter {
  abstract readonly id: ProviderId;
  abstract search(intent: SearchIntent): Promise<ProviderSearchResult>;
  
  async getDetail(sourceAssetId: string): Promise<UnifiedImage | null> {
    return null;
  }
}
