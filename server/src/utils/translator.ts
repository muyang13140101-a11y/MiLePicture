/**
 * 智能多语言搜索关键词翻译与增强工具
 * 针对海外英文图库 API（Openverse, The Met, Wikimedia, Unsplash, Wallhaven 等），
 * 将中文搜索词自动映射为高精准度英文检索词，大幅提升国内用户搜索匹配率与出图丰富度。
 */

const KEYWORD_MAP: Record<string, string> = {
  '花': 'flower',
  '花朵': 'flowers blooming',
  '樱花': 'cherry blossom sakura',
  '荷花': 'lotus flower',
  '玫瑰': 'rose flower',
  '向日葵': 'sunflower',
  '风景': 'landscape nature scenery',
  '自然': 'nature landscape',
  '山': 'mountain peak',
  '雪山': 'snow mountain alp',
  '海': 'ocean sea beach',
  '大海': 'ocean blue sea',
  '湖泊': 'lake water',
  '河流': 'river stream',
  '森林': 'forest trees woodland',
  '树': 'tree green',
  '天空': 'sky clouds',
  '星空': 'galaxy starry sky milky way',
  '宇宙': 'space cosmos universe galaxy',
  '月亮': 'moon night',
  '太阳': 'sun sunshine sunrise sunset',
  '日落': 'sunset golden hour',
  '日出': 'sunrise morning',
  '云': 'clouds cloudy sky',
  '雨': 'rain rainy raindrops',
  '雪': 'snow winter frosty',
  '冬天': 'winter snow cold',
  '秋天': 'autumn fall leaves',
  '春天': 'spring blossom flowers',
  '夏天': 'summer beach sunshine',
  '猫': 'cat kitten feline',
  '小猫': 'cute kitten cat',
  '狗': 'dog puppy canine',
  '小狗': 'puppy cute dog',
  '鸟': 'bird wildlife avian',
  '动物': 'animals wildlife fauna',
  '萌宠': 'cute pets animals',
  '鱼': 'fish underwater marine',
  '海洋': 'ocean underwater sea life',
  '城市': 'city urban cityscape',
  '建筑': 'architecture modern building',
  '街道': 'street city urban alley',
  '夜景': 'night city lights neon',
  '赛博朋克': 'cyberpunk neon sci-fi futuristic',
  '科技': 'technology futuristic cyber',
  '极简': 'minimalist minimalism texture clean',
  '插画': 'illustration artistic artwork',
  '绘画': 'painting artwork art canvas',
  '艺术': 'art artwork masterpiece',
  '古典': 'classical vintage ancient art',
  '国画': 'chinese painting traditional art',
  '水彩': 'watercolor painting art',
  '油画': 'oil painting classical artwork',
  '动漫': 'anime manga artwork',
  '二次元': 'anime illustration concept art',
  '壁纸': 'wallpaper background 4k',
  '高清壁纸': 'wallpaper aesthetic 4k background',
  '人物': 'portrait person human',
  '女人': 'woman portrait beauty',
  '女孩': 'girl portrait aesthetic',
  '男人': 'man portrait aesthetic',
  '肖像': 'portrait face aesthetic',
  '汽车': 'car supercar automobile vehicle',
  '跑车': 'sports car supercar exotic vehicle',
  '复古': 'vintage retro aesthetic nostalgic',
  '暗黑': 'dark aesthetic moody shadows',
  '纯色': 'solid color minimalist gradient',
  '纹理': 'texture background pattern abstract',
  '美食': 'food culinary gourmet delicious',
  '咖啡': 'coffee cafe aesthetic',
  '茶': 'tea leaves traditional',
  '国风': 'chinese traditional culture orient',
  '故宫': 'forbidden city ancient architecture palace',
  '大都会': 'metropolitan museum classical art',
  '博物馆': 'museum sculpture painting artifact',
  '雕塑': 'sculpture statue classical art'
};

export function translateQueryToEnglish(query: string): string {
  if (!query || query.trim().length === 0) return 'art';
  
  const trimmed = query.trim().toLowerCase();
  
  // 1. 精确匹配词库
  if (KEYWORD_MAP[trimmed]) {
    return KEYWORD_MAP[trimmed];
  }

  // 2. 包含词替换与分词匹配
  let result = trimmed;
  let hasReplacement = false;
  
  for (const [cn, en] of Object.entries(KEYWORD_MAP)) {
    if (result.includes(cn)) {
      result = result.replace(new RegExp(cn, 'g'), ` ${en} `);
      hasReplacement = true;
    }
  }

  if (hasReplacement) {
    return result.replace(/\s+/g, ' ').trim();
  }

  // 3. 如果包含中文字符但未在字典中命中，附加通用艺术素材检索词作为保底
  const containsChinese = /[\u4e00-\u9fa5]/.test(query);
  if (containsChinese) {
    return `${query} art photo`;
  }

  return query;
}
