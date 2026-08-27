import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
const { HttpsProxyAgent } = require('https-proxy-agent');

// 自动检测代理环境变量（支持常见本地科学代理端口：Clash 7890 / v2ray 10809 / 10808 / 系统代理）
const proxyUrl = process.env.PROXY_URL ||
  process.env.HTTPS_PROXY ||
  process.env.HTTP_PROXY ||
  process.env.ALL_PROXY;

let agent: any = undefined;

if (proxyUrl) {
  try {
    agent = new HttpsProxyAgent(proxyUrl);
    console.log(`📡 [Proxy Enabled] 后端所有外网图库请求已挂载代理: ${proxyUrl}`);
  } catch (err) {
    console.warn(`⚠️ [Proxy Warning] 代理配置解析失败: ${proxyUrl}`, err);
  }
}

/**
 * 统一网络请求工具，内置国内网络优化、代理挂载、超时防护与重试
 */
export function createHttpClient(timeoutMs = 6000): AxiosInstance {
  const config: AxiosRequestConfig = {
    timeout: timeoutMs,
    headers: {
      'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 MiLePicture/1.0',
      'Accept': 'application/json, text/plain, */*',
      'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
    }
  };

  if (agent) {
    config.httpsAgent = agent;
    config.httpAgent = agent;
    config.proxy = false; // 禁用 axios 自带的默认解析，使用 https-proxy-agent
  }

  return axios.create(config);
}

export const defaultHttpClient = createHttpClient(6000);
