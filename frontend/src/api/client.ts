import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const token = sessionStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    // 后端可能直接返回 data，也可能包装在 ApiResponse 中
    return response.data;
  },
  (error) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 403) {
        window.location.href = '/';
      }
    }
    return Promise.reject(error);
  },
);

export default http;
