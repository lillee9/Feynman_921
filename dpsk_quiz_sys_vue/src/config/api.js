// API配置文件
// 后端配置了context-path=/api，所以API_BASE_URL需要包含/api前缀
export const API_BASE_URL = 'http://localhost:8080/api'

// 其他API相关配置
export const API_TIMEOUT = 10000

// API响应状态码
export const API_STATUS = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
}