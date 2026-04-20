import axios, { AxiosError } from 'axios';

const API_KEY_STORAGE = 'tcpfw_api_key';

export function getApiKey(): string {
  return localStorage.getItem(API_KEY_STORAGE) ?? '';
}

export function setApiKey(key: string): void {
  localStorage.setItem(API_KEY_STORAGE, key);
}

export function clearApiKey(): void {
  localStorage.removeItem(API_KEY_STORAGE);
}

const client = axios.create({ baseURL: '/api' });

client.interceptors.request.use(config => {
  config.headers = config.headers ?? {};
  config.headers['X-API-Key'] = getApiKey();
  return config;
});

client.interceptors.response.use(
  res => res,
  (err: AxiosError) => {
    if (err.response?.status === 401) {
      clearApiKey();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default client;
