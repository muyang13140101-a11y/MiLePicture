import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { exec } from 'child_process';
import { getSourcesMetadata } from './config/providers';
import { AggregatorService } from './services/AggregatorService';
import { SearchIntent, ProviderId } from './types/image';

dotenv.config();

const app = express();
const port = parseInt(process.env.PORT || '3000', 10);
const aggregator = new AggregatorService();

app.use(cors());
app.use(express.json());

// 1. 服务健康检查与欢迎页
app.get('/', (req, res) => {
  res.json({
    app: 'MiLePicture Aggregator Backend',
    version: '1.1.0',
    status: 'running',
    time: new Date().toISOString(),
    proxyConfigured: !!(process.env.PROXY_URL || process.env.HTTPS_PROXY || process.env.HTTP_PROXY),
    endpoints: [
      'GET /v1/sources',
      'GET /v1/search?q=nature&page=1',
      'GET /v1/popular-tags'
    ]
  });
});

// 2. 获取所有图库数据源的状态与元数据
app.get('/v1/sources', (req, res) => {
  res.json({
    sources: getSourcesMetadata(),
  });
});

// 3. 热门推荐标签
app.get('/v1/popular-tags', (req, res) => {
  res.json({
    tags: [
      { id: 'all', name: '全部', query: 'art' },
      { id: 'landscape', name: '自然风景', query: 'landscape mountain' },
      { id: 'museum', name: '大都会艺术', query: 'painting' },
      { id: 'cyberpunk', name: '赛博朋克', query: 'cyberpunk neon' },
      { id: 'illustration', name: '插画矢量', query: 'illustration' },
      { id: 'anime', name: '二次元插图', query: 'anime' },
      { id: 'animals', name: '萌宠飞鸟', query: 'animals wildlife' },
      { id: 'architecture', name: '现代建筑', query: 'modern architecture' },
      { id: 'space', name: '宇宙星空', query: 'galaxy space stars' },
      { id: 'minimalist', name: '极简纯粹', query: 'minimalism texture' },
    ]
  });
});

// 4. 多图库统一安全聚合搜索接口
app.get('/v1/search', async (req, res) => {
  try {
    const q = typeof req.query.q === 'string' ? req.query.q.trim() : '';
    const tagsParam = typeof req.query.tags === 'string' ? req.query.tags.split(',').filter(Boolean) : [];
    const sourceParam = typeof req.query.sources === 'string' ? (req.query.sources.split(',').filter(Boolean) as ProviderId[]) : undefined;
    const orientation = typeof req.query.orientation === 'string' ? (req.query.orientation as any) : undefined;
    const licenseFilter = typeof req.query.license === 'string' ? (req.query.license as any) : 'all';
    const page = parseInt(req.query.page as string, 10) || 1;
    const pageSize = parseInt(req.query.pageSize as string, 10) || 20;

    const intent: SearchIntent = {
      query: q,
      includeTags: tagsParam,
      sourceIds: sourceParam,
      orientation,
      licenseFilter,
      safeMode: true, // 始终开启安全模式
      page,
      pageSize,
    };

    const result = await aggregator.search(intent);
    res.json(result);
  } catch (err: any) {
    console.error('Search aggregation error:', err);
    res.status(500).json({ error: 'Search aggregation failed', message: err.message });
  }
});

// 5. 启动服务监听 (绑定 0.0.0.0，确保局域网所有设备均可接入)
app.listen(port, '0.0.0.0', () => {
  console.log(`======================================================`);
  console.log(`🚀 MiLePicture 聚合后端服务已启动: http://localhost:${port}`);
  console.log(`📡 局域网真机直连地址: http://192.168.1.5:${port}`);
  console.log(`📱 Android 模拟器通信地址: http://10.0.2.2:${port}`);
  console.log(`======================================================`);

  // 自动尝试为 USB 连接的 Android 真机打通 ADB 端口反向代理通道
  const adbPath = process.env.LOCALAPPDATA ? `${process.env.LOCALAPPDATA}\\Android\\Sdk\\platform-tools\\adb.exe` : 'adb';
  exec(`"${adbPath}" reverse tcp:${port} tcp:${port}`, (err) => {
    if (!err) {
      console.log(`⚡ [ADB Reverse Tunneling] 已自动打通 USB 隧道: 手机现在亦可通过 http://127.0.0.1:${port} 秒连本机后端！`);
    }
  });
});
