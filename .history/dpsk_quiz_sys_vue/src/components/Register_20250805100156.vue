<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="register-title">创建您的学习账号</h1>
      <p class="register-subtitle">一个账号即可访问所有DPSK学习服务</p>

      <form class="register-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input
            v-model="registerForm.username"
            type="text"
            class="form-input"
            :class="{ error: errors.username }"
            placeholder="请输入用户名"
            @blur="validateUsername"
            @input="clearError('username')"
          />
          <span v-if="errors.username" class="error-message">{{
            errors.username
          }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="registerForm.password"
            type="password"
            class="form-input"
            :class="{ error: errors.password }"
            placeholder="请输入密码"
            @blur="validatePassword"
            @input="clearError('password')"
          />
          <span v-if="errors.password" class="error-message">{{
            errors.password
          }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">确认密码</label>
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            class="form-input"
            :class="{ error: errors.confirmPassword }"
            placeholder="请确认密码"
            @blur="validateConfirmPassword"
            @input="clearError('confirmPassword')"
          />
          <span v-if="errors.confirmPassword" class="error-message">{{
            errors.confirmPassword
          }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">电子邮箱</label>
          <input
            v-model="registerForm.email"
            type="email"
            class="form-input"
            :class="{ error: errors.email }"
            placeholder="name@example.com"
            @blur="validateEmail"
            @input="clearError('email')"
          />
          <span v-if="errors.email" class="error-message">{{
            errors.email
          }}</span>
        </div>
        <!-- 新增角色选择字段 -->
        <div class="form-group">
          <label class="form-label">用户类型</label>
          <div class="role-selector">
            <el-radio-group v-model="registerForm.role">
              <el-radio label="user" class="role-option">
                <span class="role-label">普通用户</span>
              </el-radio>
              <el-radio label="admin" class="role-option">
                <span class="role-label">管理员</span>
              </el-radio>
            </el-radio-group>
          </div>
        </div>
        <!-- 新增管理员密钥字段，仅在选择管理员时显示 -->
        <div v-if="registerForm.role === 'admin'" class="form-group">
          <label class="form-label">管理员密钥</label>
          <input
            v-model="registerForm.adminKey"
            type="password"
            class="form-input"
            :class="{ error: errors.adminKey }"
            placeholder="请输入管理员注册密钥"
            @blur="validateAdminKey"
            @input="clearError('adminKey')"
          />
          <span v-if="errors.adminKey" class="error-message">{{
            errors.adminKey
          }}</span>
        </div>

        <button
          type="submit"
          class="register-btn"
          :class="{ loading: loading }"
          :disabled="loading"
        >
          <span v-if="!loading">继续</span>
          <div v-else class="loading-spinner"></div>
        </button>
      </form>

      <div class="divider">
        <span>或者</span>
      </div>

      <div class="login-section">
        <p class="login-text">已有DPSK账户？</p>
        <button type="button" class="login-link" @click="handleLogin">
          立即登录
        </button>
      </div>
    </div>

    <!-- Background Elements - 与登录页面一致 -->
    <div class="bg-gradient"></div>
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";

const ADMIN_KEY = "123";
const registerForm = ref({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  role: "user", // 新增角色字段
  adminKey: "", // 新增管理员密钥字段
});

const router = useRouter();

const errors = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  adminKey: "", // 新增管理员密钥错误字段
});

const loading = ref(false);

const validateAdminKey = () => {
  // 只有在选择管理员角色时才验证密钥
  if (registerForm.value.role === "admin") {
    const adminKey = registerForm.value.adminKey.trim();
    if (!adminKey) {
      errors.adminKey = "请输入管理员密钥";
    } else if (adminKey !== ADMIN_KEY) {
      // 在前端校验密钥
      errors.adminKey = "管理员密钥不正确";
    } else {
      errors.adminKey = "";
    }
    return !errors.adminKey;
  }
  return true;
};
const validateUsername = () => {
  const username = registerForm.value.username.trim();
  if (!username) {
    errors.username = "请输入用户名";
  } else if (username.length < 3 || username.length > 16) {
    errors.username = "用户名长度应在 3-16 个字符之间";
  } else {
    errors.username = "";
  }
  return !errors.username;
};

const validatePassword = () => {
  const password = registerForm.value.password;
  if (!password) {
    errors.password = "请输入密码";
  } else if (password.length < 6 || password.length > 20) {
    errors.password = "密码长度应在 6-20 个字符之间";
  } else {
    errors.password = "";
  }
  return !errors.password;
};

const validateConfirmPassword = () => {
  const confirmPassword = registerForm.value.confirmPassword;
  if (!confirmPassword) {
    errors.confirmPassword = "请确认密码";
  } else if (confirmPassword !== registerForm.value.password) {
    errors.confirmPassword = "两次输入的密码不一致";
  } else {
    errors.confirmPassword = "";
  }
  return !errors.confirmPassword;
};

const validateEmail = () => {
  const email = registerForm.value.email.trim();
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!email) {
    errors.email = "请输入电子邮箱";
  } else if (!emailRegex.test(email)) {
    errors.email = "请输入有效的电子邮箱地址";
  } else {
    errors.email = "";
  }
  return !errors.email;
};

const clearError = (field) => {
  errors[field] = "";
};

const validateForm = () => {
  const isUsernameValid = validateUsername();
  const isPasswordValid = validatePassword();
  const isConfirmPasswordValid = validateConfirmPassword();
  const isEmailValid = validateEmail();
  const isAdminKeyValid = validateAdminKey();

  return (
    isUsernameValid &&
    isPasswordValid &&
    isConfirmPasswordValid &&
    isEmailValid &&
    isAdminKeyValid
  );
};

const showMessage = (message, type = "info") => {
  // 简单的消息提示实现
  const messageEl = document.createElement("div");
  messageEl.textContent = message;
  messageEl.className = `message message-${type}`;
  messageEl.style.cssText = `
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background: ${
          type === "success"
            ? "#34C759"
            : type === "error"
            ? "#FF3B30"
            : "#007AFF"
        };
        color: white;
        padding: 12px 24px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        z-index: 1000;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
  document.body.appendChild(messageEl);

  setTimeout(() => {
    messageEl.remove();
  }, 3000);
};

const handleRegister = async () => {
  if (!validateForm()) return;

  loading.value = true;

  const userDto = {
    username: registerForm.value.username.trim(),
    password: registerForm.value.password,
    email: registerForm.value.email.trim(),
  };

  try {
    const response = await fetch("/api/user/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userDto),
    });

    const res = await response.json();

    if (res.message === "success") {
      showMessage("注册成功，正在跳转...", "success");
      setTimeout(() => {
        router.push("/login");
      }, 1500);
    } else {
      showMessage(res.data || "注册失败，请重试", "error");
    }
  } catch (error) {
    showMessage("网络错误，请检查您的连接", "error");
  } finally {
    loading.value = false;
  }
};

const handleLogin = () => {
  router.push("/login");
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto",
    "Helvetica Neue", Arial, sans-serif;
}

/* 背景渐变层 - 与登录页面一致 */
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.8) 0%,
    rgba(118, 75, 162, 0.8) 100%
  );
  backdrop-filter: blur(100px);
}

/* 浮动形状背景 - 与登录页面一致 */
.bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.1),
    rgba(255, 255, 255, 0.05)
  );
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 10%;
  animation-delay: -2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 20%;
  animation-delay: -4s;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.register-card {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
}

.register-title {
  font-size: 32px;
  font-weight: 600;
  text-align: center;
  color: #1d1d1f;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.register-subtitle {
  font-size: 17px;
  color: #86868b;
  text-align: center;
  margin: 0 0 40px 0;
  line-height: 1.4;
  font-weight: 400;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.2px;
}

.form-input {
  height: 52px;
  padding: 0 20px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  font-size: 17px;
  background: rgba(249, 249, 249, 0.8);
  transition: all 0.2s ease;
  outline: none;
  color: #1d1d1f;
  font-weight: 400;
}

.form-input:hover {
  border-color: rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

.form-input:focus {
  border-color: #007aff;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.form-input.error {
  border-color: #ff3b30;
}

.form-input.error:focus {
  box-shadow: 0 0 0 3px rgba(255, 59, 48, 0.1);
}

.form-input::placeholder {
  color: #86868b;
}

.error-message {
  font-size: 14px;
  color: #ff3b30;
  font-weight: 400;
}

.register-btn {
  height: 52px;
  background: #007aff;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.register-btn:hover:not(:disabled) {
  background: #0051d5;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.4);
}

.register-btn:active:not(:disabled) {
  transform: translateY(0);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.divider {
  position: relative;
  text-align: center;
  margin: 32px 0;
  color: #86868b;
  font-size: 15px;
}

.divider::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: rgba(0, 0, 0, 0.1);
}

.divider span {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 16px;
  position: relative;
}

.login-section {
  text-align: center;
}

.login-text {
  font-size: 15px;
  color: #86868b;
  margin: 0 0 16px 0;
}

.login-link {
  width: 100%;
  height: 52px;
  background: transparent;
  border: 1px solid rgba(0, 122, 255, 0.3);
  border-radius: 12px;
  font-size: 17px;
  font-weight: 500;
  color: #007aff;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-link:hover {
  background: rgba(0, 122, 255, 0.05);
  border-color: #007aff;
  transform: translateY(-1px);
}

.login-link:active {
  transform: translateY(0);
}

@media (max-width: 480px) {
  .register-card {
    width: 90%;
    margin: 20px;
    padding: 32px 24px;
  }

  .register-title {
    font-size: 28px;
  }
}
</style>
