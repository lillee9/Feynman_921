// 检查前端浏览器中的token状态和有效性

/**
 * 从Cookie中获取token
 * @returns {string|null} token值或null
 */
function getToken() {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; token=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  return null;
}

/**
 * 解析JWT token
 * @param {string} token - JWT token
 * @returns {Object|null} 解析后的payload或null
 */
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('解析JWT失败:', e);
    return null;
  }
}

/**
 * 检查token是否过期
 * @param {Object} payload - JWT payload
 * @returns {boolean} 是否过期
 */
function isTokenExpired(payload) {
  if (!payload || !payload.exp) {
    return true;
  }
  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp < currentTime;
}

// 主函数：检查token状态
function checkTokenStatus() {
  console.log('===== Token 状态检查 =====');
  
  // 1. 检查token是否存在
  const token = getToken();
  if (!token) {
    console.error('❌ Token不存在，需要重新登录');
    return false;
  }
  console.log('✅ Token存在');
  console.log('Token值:', token);
  
  // 2. 解析token
  const payload = parseJwt(token);
  if (!payload) {
    console.error('❌ Token格式无效，无法解析');
    return false;
  }
  console.log('✅ Token解析成功');
  console.log('Payload:', payload);
  
  // 3. 检查token是否过期
  if (isTokenExpired(payload)) {
    console.error(`❌ Token已过期，过期时间: ${new Date(payload.exp * 1000).toLocaleString()}`);
    return false;
  }
  console.log(`✅ Token有效，过期时间: ${new Date(payload.exp * 1000).toLocaleString()}`);
  
  // 4. 检查用户信息
  if (payload.aud) {
    console.log(`✅ 用户ID: ${payload.aud}`);
  }
  
  return true;
}

// 执行检查
const isValid = checkTokenStatus();
console.log('===== 检查结果 =====');
console.log(isValid ? '✅ Token有效' : '❌ Token无效或已过期，需要重新登录');

// 提示如何使用此脚本
console.log('\n===== 使用说明 =====');
console.log('请在浏览器控制台中运行此脚本检查token状态');
console.log('如果token无效，请尝试重新登录获取新token');