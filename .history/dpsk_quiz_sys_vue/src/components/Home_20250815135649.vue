<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <div class="logo">
          <div class="logo-icon"></div>
          <span class="logo-text">Deep Quiz</span>
        </div>
        <div class="nav-links">
          <router-link to="/home" class="nav-link">主页</router-link>
          <router-link to="/analysis" class="nav-link">错题分析</router-link>
        </div>
        <div class="user-section">
          <el-dropdown>
            <div class="user-trigger">
              <div class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="username">{{ username }}</span>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item @click="handleProfile"
                  >个人中心</el-dropdown-item
                >
                <el-dropdown-item @click="handleLogout"
                  >退出登录</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            贷后催收培训
            <span class="hero-highlight">智能助手</span>
          </h1>
          <p class="hero-subtitle">
            专业的AI催收培训平台，提升催收技能，规范催收流程
          </p>
          <div class="search-container">
            <div class="search-wrapper">
              <el-input
                v-model="searchQuery"
                placeholder="输入你的问题..."
                class="hero-search"
                size="large"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
              </el-input>
              <button class="search-button" @click="handleSearch">
                <el-icon><Search /></el-icon>
              </button>
            </div>
          </div>
          <div class="action-buttons">
            <el-upload
              class="upload-component"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :accept="supportedFormats"
              :before-upload="beforeUpload"
            >
              <button class="primary-button">
                <el-icon><Upload /></el-icon>
                上传文件
              </button>
            </el-upload>
          </div>
          <p class="file-support-text">
            支持 {{ supportedFormatsText }} 格式，最大 {{ maxSizeMB }}MB
          </p>
        </div>
      </section>

      <!-- 功能卡片区域 -->
      <section class="features-section">
        <div class="section-header">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">为催收培训场景专业定制</p>
        </div>

        <div class="features-grid">
          <div
            v-if="userStore.role === 'admin'"
            class="feature-card large-card clickable"
            @click="goToGenerator"
          >
            <div class="card-content">
              <div class="card-icon homework-icon">
                <el-icon><Document /></el-icon>
              </div>
              <h3 class="card-title">题目生成</h3>
              <p class="card-description">
                上传催收案例智能出题，涵盖话术、法规、心理分析等培训内容
              </p>
            </div>
            <div class="card-visual">
              <div class="visual-element"></div>
            </div>
          </div>

          <div
            v-if="userStore.role === 'admin'"
            class="feature-card medium-card clickable"
            @click="goToKnowledgeManagement"
          >
            <div class="card-content">
              <div class="card-icon camera-icon">
                <el-icon><PictureFilled /></el-icon>
              </div>
              <h3 class="card-title">知识库管理</h3>
              <p class="card-description">
                管理催收准则等文件，智能解析文档内容为知识块
              </p>
            </div>
          </div>
          <div
            v-if="userStore.role === 'user'"
            class="feature-card large-card clickable"
            @click="goToChat"
          >
            <div class="card-content">
              <div class="card-icon chat-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <h3 class="card-title">催收咨询</h3>
              <p class="card-description">
                专业催收顾问实时解答，提供话术指导和合规建议
              </p>
            </div>
          </div>
          <div
            v-if="userStore.role === 'user'"
            class="feature-card small-card clickable"
            @click="goToQuiz"
          >
            <div class="card-content">
              <div class="card-icon quiz-icon">
                <el-icon><Edit /></el-icon>
              </div>
              <h3 class="card-title">技能测试</h3>
              <p class="card-description">检验催收培训成果</p>
            </div>
          </div>
          <div
            v-if="userStore.role === 'user'"
            class="feature-card medium-card clickable"
            @click="goToAnalysis"
          >
            <div class="card-content">
              <div class="card-icon camera-icon">
                <el-icon><PictureFilled /></el-icon>
              </div>
              <h3 class="card-title">错题分析</h3>
              <p class="card-description">分析答卷情况，提供学习意见</p>
            </div>
          </div>

          <div
            v-if="userStore.role === 'admin'"
            class="feature-card small-card clickable"
            @click="goToChart"
          >
            <!-- 新增clickable类和点击事件 -->
            <div class="card-content">
              <div class="card-icon quiz-icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <h3 class="card-title">培训情况</h3>
              <p class="card-description">查看培训整体数据概览</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 最近活动 -->
      <section class="activity-section">
        <div class="section-header">
          <h2 class="section-title">最近活动</h2>
        </div>
        <div class="activity-list">
          <div
            v-for="(activity, index) in activities"
            :key="index"
            class="activity-item"
          >
            <div class="activity-time">{{ activity.timestamp }}</div>
            <div class="activity-content">{{ activity.content }}</div>
          </div>
        </div>
      </section>
    </main>
    <!-- 搜索结果弹窗 -->
    <el-dialog
      v-model="showSearchResults"
      title="搜索结果"
      width="80%"
      :before-close="closeSearchResults"
    >
      <div v-if="searchResults.length === 0" class="no-results">
        <el-empty description="未找到相关内容" />
      </div>
      <div v-else class="search-results-list">
        <div
          v-for="(result, index) in searchResults"
          :key="index"
          class="search-result-item"
          @click="viewSearchResult(result)"
        >
          <div class="result-header">
            <h4 class="result-title">
              {{ result.documentName || "未知文档" }}
            </h4>
            <span class="result-chapter">{{
              result.chapterName || "默认章节"
            }}</span>
          </div>
          <div class="result-content">
            {{ result.content.substring(0, 200)
            }}{{ result.content.length > 200 ? "..." : "" }}
          </div>
          <div class="result-meta">
            <span class="content-length"
              >内容长度: {{ result.contentLength }} 字符</span
            >
            <span class="create-time"
              >创建时间:
              {{ new Date(result.createTime).toLocaleDateString() }}</span
            >
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeSearchResults">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from "vue";
import {
  // 补充PieChart图标导入
  UserFilled,
  CaretBottom,
  Search,
  Upload,
  Camera,
  Document,
  PictureFilled,
  ChatDotRound,
  Edit,
  PieChart, // 新增
} from "@element-plus/icons-vue";
import { ElMessage, ElLoading } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";

const userStore = useUserStore();
const router = useRouter();

const username = ref(userStore.username);
const searchQuery = ref("");
const searchResults = ref([]);
const showSearchResults = ref(false);

// 配置参数
const maxSizeMB = 10;
const maxWidth = 8192;
const maxHeight = 8192;
const supportedFormats = ".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png,.bmp,.ofd";
const supportedFormatsText = "PDF、Word、TXT、JPG、JPEG、PNG、BMP、OFD";

// 模拟的历史记录数据
const activities = ref([
  {
    content: "催收话术培训：电话沟通技巧练习",
    timestamp: "2025-08-13 19:20",
  },
  {
    content: "法律法规学习：《民法典》相关条款解读",
    timestamp: "2025-08-15 18:30",
  },
  {
    content: "案例分析：逾期客户心理分析与应对策略",
    timestamp: "2025-08-17 16:45",
  },
]);

// 获取cookie的工具函数
const getCookie = (name) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(";").shift();
  return null;
};

const handleFileChange = async (uploadFile) => {
  try {
    await validateFile(uploadFile.raw);
    const loading = ElLoading.service({
      lock: true,
      text: "正在上传文件到知识库...",
      background: "rgba(0, 0, 0, 0.7)",
    });

    try {
      const userId = userStore.id;
      const formData = new FormData();
      formData.append("file", uploadFile.raw);
      formData.append("documentName", uploadFile.raw.name);
      formData.append("uploadUserId", userId);
      formData.append("description", "从主页上传的文档");

      // 获取token
      const token = getCookie("token");
      if (!token) {
        ElMessage.error("请先登录");
        return;
      }

      const response = await fetch("/api/kb/documents/upload", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        credentials: "include",
        body: formData,
      });

      const result = await response.json();

      if (response.ok && result.code === 200) {
        ElMessage.success("文件上传成功！正在解析文档内容...");

        // 调用文档处理接口
        const processResponse = await fetch(
          `/api/kb/documents/${result.data.documentId}/process`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
            credentials: "include",
          }
        );

        const processResult = await processResponse.json();

        if (processResponse.ok && processResult.code === 200) {
          ElMessage.success("文档已成功解析并存储到知识库！");
          console.log("文档处理结果:", processResult.data);
        } else {
          ElMessage.warning(
            "文档上传成功，但解析失败：" + (processResult.message || "未知错误")
          );
        }
      } else {
        throw new Error(result.message || "上传失败");
      }
    } finally {
      loading.close();
    }
  } catch (error) {
    ElMessage.error(error.message);
  }
};

const beforeUpload = (file) => {
  return validateFile(file).catch((error) => {
    ElMessage.error(error.message);
    return false;
  });
};

const validateFile = (file) => {
  return new Promise((resolve, reject) => {
    const extension = file.name.split(".").pop().toLowerCase();
    const supportedExtensions = supportedFormats.replace(/\./g, "").split(",");

    if (!supportedExtensions.includes(extension)) {
      reject(
        new Error(
          `不支持的文件格式: ${extension}，请上传 ${supportedFormatsText} 格式的文件`
        )
      );
      return;
    }

    const isLtSize = file.size / 1024 / 1024 <= maxSizeMB;
    if (!isLtSize) {
      reject(new Error(`文件大小不能超过 ${maxSizeMB}MB!`));
      return;
    }

    if (extension !== "pdf" && file.type.startsWith("image/")) {
      const img = new Image();
      img.onload = () => {
        if (img.width > maxWidth || img.height > maxHeight) {
          reject(new Error(`图片尺寸不能超过 ${maxWidth}x${maxHeight} 像素!`));
        } else {
          resolve();
        }
      };
      img.onerror = () => {
        resolve();
      };
      img.src = URL.createObjectURL(file);
    } else {
      resolve();
    }
  });
};

const goToChat = () => {
  router.push("/chat");
};

const goToGenerator = () => {
  router.push("/generator");
};
const goToQuizChart = () => {
  router.push("/quizChart");
};
const handleTakePhoto = () => {
  ElMessage.info("启动相机功能");
};

const handleProfile = () => {
  ElMessage.info("进入个人中心");
};

const handleLogout = () => {
  ElMessage.success("退出登录");
  document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
  userStore.clearUserInfo();
  localStorage.removeItem("userId");
  router.push("/login");
};

const goToQuiz = () => {
  // 调试信息：输出所有cookie
  console.log("🍪 当前所有Cookie:", document.cookie);

  // 检查用户是否已登录
  const token = getCookie("token");
  console.log("🔑 获取到的token:", token);

  if (!token) {
    console.log("❌ 没有找到token，跳转到登录页面");
    ElMessage.error("请先登录");
    router.push("/login");
    return;
  }

  // 跳转到考试选择页面
  console.log("✅ Token存在，跳转到考试选择页面");
  router.push("/exam-selection");
};
const goToKnowledgeManagement = () => {
  // 检查用户是否已登录
  const token = getCookie("token");

  if (!token) {
    ElMessage.error("请先登录");
    router.push("/login");
    return;
  }

  // 跳转到知识库管理页面
  router.push("/knowledge-management");
};

// 搜索功能
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning("请输入搜索关键词");
    return;
  }

  try {
    const loading = ElLoading.service({
      lock: true,
      text: "正在搜索知识库...",
      background: "rgba(0, 0, 0, 0.7)",
    });

    const response = await fetch(
      `/api/kb/chunks/search?keyword=${encodeURIComponent(searchQuery.value)}`,
      {
        method: "GET",
        credentials: "include",
      }
    );

    const result = await response.json();

    if (response.ok && result.code === 200) {
      searchResults.value = result.data;
      showSearchResults.value = true;

      if (result.data.length === 0) {
        ElMessage.info("未找到相关内容");
      } else {
        ElMessage.success(`找到 ${result.data.length} 条相关内容`);
      }
    } else {
      throw new Error(result.message || "搜索失败");
    }

    loading.close();
  } catch (error) {
    ElMessage.error(error.message);
  }
};

// 关闭搜索结果
const closeSearchResults = () => {
  showSearchResults.value = false;
  searchResults.value = [];
};

// 查看搜索结果详情
const viewSearchResult = (chunk) => {
  ElMessage.info("查看详情功能开发中");
  console.log("查看知识块详情:", chunk);
};

const goToAnalysis = () => {
  // 新增跳转方法
  router.push("/analysis"); // 跳转到Chart.vue对应的路由
};
const goToChart = () => {
  if (userStore.role !== "admin") {
    ElMessage.error("权限不足");
    return;
    s;
  }
  router.push("/chart").catch((err) => {
    console.error("路由跳转失败:", err);
    ElMessage.error("访问被拒绝");
  });
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
}

/* 导航栏 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: #1d1d1f;
  text-decoration: none;
  font-size: 16px;
  font-weight: 400;
  transition: color 0.3s ease;
}

.nav-link:hover {
  color: #667eea;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: background-color 0.3s ease;
}

.user-trigger:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.username {
  font-size: 16px;
  color: #1d1d1f;
}

.dropdown-icon {
  color: #86868b;
}

/* 主内容区域 */
.main-content {
  padding-top: 60px;
}

/* Hero Section */
.hero-section {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 24px;
}

.hero-content {
  max-width: 800px;
}

.hero-title {
  font-size: clamp(48px, 8vw, 96px);
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 24px;
  line-height: 1.1;
}

.hero-highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 24px;
  color: #86868b;
  margin-bottom: 48px;
  font-weight: 400;
}

.search-container {
  margin-bottom: 32px;
}

.search-wrapper {
  position: relative;
  max-width: 600px;
  margin: 0 auto;
}

.hero-search {
  width: 100%;
  height: 56px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.hero-search :deep(.el-input__wrapper) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0 60px 0 20px;
}

.hero-search :deep(.el-input__inner) {
  font-size: 18px;
  color: #1d1d1f;
}

.search-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.search-button:hover {
  transform: translateY(-50%) scale(1.1);
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 16px;
}

.primary-button,
.secondary-button {
  padding: 16px 32px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.primary-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.primary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.secondary-button {
  background: rgba(255, 255, 255, 0.9);
  color: #1d1d1f;
  border: 1px solid rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(20px);
}

.secondary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.upload-component {
  display: inline-block;
}

.file-support-text {
  color: #86868b;
  font-size: 14px;
}

/* 功能区域 */
.features-section {
  padding: 120px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-title {
  font-size: 48px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 20px;
  color: #86868b;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(3, 300px);
  gap: 24px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 32px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.feature-card.clickable {
  cursor: pointer;
}

.large-card {
  grid-column: span 8;
  grid-row: span 2;
}

.medium-card {
  grid-column: span 4;
  grid-row: span 1;
}

.small-card {
  grid-column: span 4;
  grid-row: span 1;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 24px;
  color: white;
}

.homework-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.camera-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.chat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.quiz-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.card-description {
  font-size: 16px;
  color: #86868b;
  line-height: 1.5;
}

.card-visual {
  position: absolute;
  right: -50px;
  bottom: -50px;
  width: 200px;
  height: 200px;
  opacity: 0.1;
}

.visual-element {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

/* 活动区域 */
.activity-section {
  padding: 80px 24px 120px;
  max-width: 1200px;
  margin: 0 auto;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  transition: all 0.3s ease;
}

.activity-item:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.activity-time {
  color: #86868b;
  font-size: 14px;
  min-width: 120px;
}

.activity-content {
  color: #1d1d1f;
  font-size: 16px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .features-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }

  .large-card,
  .medium-card,
  .small-card {
    grid-column: span 1;
    grid-row: span 1;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
  }

  .activity-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* Element Plus 自定义样式 */
.custom-dropdown {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
}

/* 搜索结果样式 */
.search-results-list {
  max-height: 500px;
  overflow-y: auto;
}

.search-result-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-result-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.result-title {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.result-chapter {
  background: #f0f9ff;
  color: #0369a1;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.result-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
  font-size: 14px;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.no-results {
  text-align: center;
  padding: 40px 0;
}
</style>
