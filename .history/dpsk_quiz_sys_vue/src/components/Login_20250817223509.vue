<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <h1 class="login-title">登录您的账户</h1>
        <p class="login-subtitle">输入您的用户名和密码以继续</p>
      </div>

      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        @keyup.enter="handleLogin"
        class="login-form"
      >
        <el-form-item prop="username" class="form-item">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            clearable
            class="apple-input"
          />
        </el-form-item>

        <el-form-item prop="password" class="form-item">
          <el-input
            v-model="loginForm.password"
            placeholder="密码"
            type="password"
            show-password
            clearable
            class="apple-input"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button
            type="primary"
            class="login-btn"
            @click="handleLogin"
            :loading="loading"
            size="large"
          >
            <span v-if="!loading">登录</span>
          </el-button>
        </div>
      </el-form>

      <div class="divider">
        <span>或</span>
      </div>

      <div class="register-section">
        <p class="register-text">还没有账户？</p>
        <el-button class="register-btn" @click="handleRegister" size="large">
          创建新账户
        </el-button>
      </div>
    </div>

    <!-- Background Elements -->
    <div class="bg-gradient"></div>
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { API_BASE_URL } from "../config/api";

const router = useRouter();
const userStore = useUserStore();

const loginForm = ref({
  username: "",
  password: "",
});

const loginRules = ref({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 16, message: "长度在 3 到 16 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
});

const loginFormRef = ref(null);
const loading = ref(false);

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      const userDto = {
        username: loginForm.value.username,
        password: loginForm.value.password,
      };

      fetch("/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(userDto),
      })
        .then((response) => {
          if (!response.ok) throw new Error("Server failed");
          return response.json();
        })
        .then((res) => {
          if (res.message === "success") {
            const token = res.data.token;
            document.cookie = `token=${token}; path=/; max-age=${24 * 60 * 60}`;
            userStore.setUserInfo(
              res.data.user_id,
              res.data.username,
              res.data.email,
              res.data.role
            );
            console.log("Pinia:", userStore.id);
            ElMessage.success("登录成功");
            router.push("/home");
          } else {
            ElMessage.error(res.message);
          }
        })
        .catch((error) => {
          ElMessage.error("登录失败");
          console.error("Error:", error);
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
};

const handleRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
.login-container {
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

.login-card {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(255, 255, 255, 0.05);
  z-index: 10;
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.apple-logo {
  display: inline-flex;
  margin-bottom: 24px;
  color: #1d1d1f;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
}

.login-subtitle {
  font-size: 17px;
  color: #86868b;
  margin: 0;
  font-weight: 400;
}

.login-form {
  margin-bottom: 32px;
}

.form-item {
  margin-bottom: 20px;
}

:deep(.apple-input .el-input__wrapper) {
  background: rgba(249, 249, 249, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 16px 20px;
  font-size: 17px;
  transition: all 0.2s ease;
  box-shadow: none;
}

:deep(.apple-input .el-input__wrapper:hover) {
  border-color: rgba(0, 0, 0, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

:deep(.apple-input.is-focus .el-input__wrapper) {
  border-color: #007aff;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

:deep(.apple-input .el-input__inner) {
  color: #1d1d1f;
  font-size: 17px;
  font-weight: 400;
}

:deep(.apple-input .el-input__inner::placeholder) {
  color: #86868b;
}

.form-actions {
  margin-top: 32px;
}

.login-btn {
  width: 100%;
  height: 52px;
  background: #007aff;
  border: none;
  border-radius: 12px;
  font-size: 17px;
  font-weight: 600;
  color: white;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.login-btn:hover {
  background: #0051d5;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

:deep(.login-btn.is-loading) {
  background: #007aff;
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

.register-section {
  text-align: center;
}

.register-text {
  font-size: 15px;
  color: #86868b;
  margin: 0 0 16px 0;
}

.register-btn {
  width: 100%;
  height: 52px;
  background: transparent;
  border: 1px solid rgba(0, 122, 255, 0.3);
  border-radius: 12px;
  font-size: 17px;
  font-weight: 500;
  color: #007aff;
  transition: all 0.2s ease;
}

.register-btn:hover {
  background: rgba(0, 122, 255, 0.05);
  border-color: #007aff;
  transform: translateY(-1px);
}

.register-btn:active {
  transform: translateY(0);
}

/* Error message styling */
:deep(.el-form-item__error) {
  color: #ff3b30;
  font-size: 14px;
  font-weight: 400;
}

/* Responsive design */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    margin: 20px;
    padding: 32px 24px;
  }

  .login-title {
    font-size: 28px;
  }
}
</style>
