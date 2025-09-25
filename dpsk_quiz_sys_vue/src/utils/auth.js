// 认证相关工具函数
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 从Cookie中获取token
 * @returns {string|null} token值或null
 */
export const getToken = () => {
  const value = `; ${document.cookie}`
  const parts = value.split(`; token=`)
  if (parts.length === 2) {
    return parts.pop().split(';').shift()
  }
  return null
}

/**
 * 设置token到Cookie
 * @param {string} token - JWT token
 * @param {number} maxAge - 过期时间（秒），默认24小时
 */
export const setToken = (token, maxAge = 24 * 60 * 60) => {
  document.cookie = `token=${token}; path=/; max-age=${maxAge}`
}

/**
 * 清除token
 */
export const clearToken = () => {
  document.cookie = 'token=; path=/; max-age=0'
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export const isAuthenticated = () => {
  const token = getToken()
  return !!token
}

/**
 * 获取带有认证头的请求配置
 * @returns {Object} 包含Authorization头的配置对象
 */
export const getAuthHeaders = () => {
  const token = getToken()
  return {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  }
}

/**
 * 检查认证状态，如果未登录则跳转到登录页
 * @param {Object} router - Vue Router实例
 * @returns {boolean} 是否已认证
 */
export const requireAuth = (router) => {
  if (!isAuthenticated()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

/**
 * 处理API响应，检查是否需要重新登录
 * @param {Response} response - fetch响应对象
 * @param {Object} router - Vue Router实例
 * @returns {Promise<any>} 处理后的响应数据
 */
export const handleApiResponse = async (response, router) => {
  if (response.status === 401) {
    clearToken()
    ElMessage.error('登录已过期，请重新登录')
    router.push('/login')
    throw new Error('Unauthorized')
  }
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }
  
  const data = await response.json()
  
  // 检查业务层面的认证错误
  if (data.code === 401 || data.message === 'Unauthorized') {
    clearToken()
    ElMessage.error('登录已过期，请重新登录')
    router.push('/login')
    throw new Error('Unauthorized')
  }
  
  return data
}

/**
 * 发起认证请求的封装函数
 * @param {string} url - 请求URL
 * @param {Object} options - 请求选项
 * @param {Object} router - Vue Router实例
 * @returns {Promise<any>} 响应数据
 */
export const authFetch = async (url, options = {}, router) => {
  const headers = {
    ...getAuthHeaders(),
    ...options.headers
  }
  
  const response = await fetch(url, {
    ...options,
    headers
  })
  
  return handleApiResponse(response, router)
}