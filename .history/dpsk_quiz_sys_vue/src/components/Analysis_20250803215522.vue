<template>
  <div class="analysis-container">
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
          <a href="#" class="nav-link">题目生成</a>
        </div>
        <div class="user-section">
          <el-dropdown>
            <div class="user-trigger">
              <div class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="username">同学</span>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 主体区域 -->
    <main class="main-content">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            成绩
            <span class="hero-highlight">分析</span>
          </h1>
          <p class="hero-subtitle">
            查看排行榜和个人答题分析，发现知识盲点，提升学习效率
          </p>
        </div>
      </section>

      <!-- 排行榜区域 -->
      <section class="rankings-section">
        <div class="rankings-grid">
          <!-- 简单难度排行榜 -->
          <div class="ranking-card">
            <div class="section-header with-refresh">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">简单难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
              <button class="refresh-button" @click="refreshRanking('easy')">
                <el-icon><Refresh /></el-icon>
              </button>
            </div>

            <div class="ranking-list">
              <div
                v-for="(item, index) in easyRankings"
                :key="index"
                class="ranking-item"
              >
                <div class="rank-number" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="user-info">
                    <div class="user-avatar-small">
                      <el-icon><UserFilled /></el-icon>
                    </div>
                    <span class="rank-username">{{ item.username }}</span>
                  </div>
                  <div class="rank-score">{{ item.score }}分</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 中等难度排行榜 -->
          <div class="ranking-card">
            <div class="section-header with-refresh">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">中等难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
              <button class="refresh-button" @click="refreshRanking('medium')">
                <el-icon><Refresh /></el-icon>
              </button>
            </div>

            <div class="ranking-list">
              <div
                v-for="(item, index) in mediumRankings"
                :key="index"
                class="ranking-item"
              >
                <div class="rank-number" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="user-info">
                    <div class="user-avatar-small">
                      <el-icon><UserFilled /></el-icon>
                    </div>
                    <span class="rank-username">{{ item.username }}</span>
                  </div>
                  <div class="rank-score">{{ item.score }}分</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 困难难度排行榜 -->
          <div class="ranking-card">
            <div class="section-header with-refresh">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">困难难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
              <button class="refresh-button" @click="refreshRanking('hard')">
                <el-icon><Refresh /></el-icon>
              </button>
            </div>

            <div class="ranking-list">
              <div
                v-for="(item, index) in hardRankings"
                :key="index"
                class="ranking-item"
              >
                <div class="rank-number" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="user-info">
                    <div class="user-avatar-small">
                      <el-icon><UserFilled /></el-icon>
                    </div>
                    <span class="rank-username">{{ item.username }}</span>
                  </div>
                  <div class="rank-score">{{ item.score }}分</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 答卷分析区域 -->
      <section class="analysis-section">
        <div class="analysis-card">
          <div class="section-header">
            <div class="section-icon">
              <el-icon><Histogram /></el-icon>
            </div>
            <h2 class="section-title">个人答题分析</h2>
            <p class="section-desc">查看你的知识掌握情况和提升建议</p>
          </div>

          <div class="analysis-actions">
            <button
              class="analysis-button"
              :class="{ loading: analyzing }"
              @click="analyzeAnswers"
            >
              <span v-if="!analyzing" class="button-content">
                <el-icon><Document /></el-icon>
                答卷分析
              </span>
              <span v-else class="button-content">
                <div class="loading-spinner"></div>
                分析中...
              </span>
            </button>
          </div>

          <!-- 分析结果区域 -->
          <div v-if="analysisResult" class="analysis-results">
            <!-- 欠缺知识点分析 -->
            <div class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><WarningFilled /></el-icon>
                用户欠缺知识点分析
              </h3>
              <div class="knowledge-points">
                <div
                  v-for="(point, index) in analysisResult.weakPoints"
                  :key="index"
                  class="knowledge-tag"
                >
                  {{ point }}
                </div>
              </div>
            </div>

            <!-- 用户建议 -->
            <div class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><InfoFilled /></el-icon>
                学习建议
              </h3>
              <div class="suggestions">
                <div
                  v-for="(suggestion, index) in analysisResult.suggestions"
                  :key="index"
                  class="suggestion-item"
                >
                  <div class="suggestion-number">{{ index + 1 }}</div>
                  <p class="suggestion-text">{{ suggestion }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import axios from "axios";
import {
  UserFilled,
  CaretBottom,
  Trophy,
  Histogram,
  Document,
  WarningFilled,
  InfoFilled,
  Refresh,
} from "@element-plus/icons-vue";

// 排行榜数据 - 初始化为空数组
const easyRankings = ref([]);
const mediumRankings = ref([]);
const hardRankings = ref([]);
// 添加用户ID和试卷ID (实际应用中应从登录状态或路由参数获取)
const userId = ref(1); // 示例用户ID，实际应从登录信息获取
const paperId = ref(1); // 示例试卷ID，实际应根据选择的试卷获取
// SSE事件源引用
const eventSource = ref(null);
onMounted(() => {
  refreshRanking("easy", false);
  refreshRanking("medium", false);
  refreshRanking("hard", false);
});
// 组件卸载时清理SSE连接
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close();
  }
});
// 分析状态和结果
const analyzing = ref(false);
const analysisResult = ref(null);

// 获取排行榜名次样式
const getRankClass = (index) => {
  if (index === 0) return "rank-first";
  if (index === 1) return "rank-second";
  if (index === 2) return "rank-third";
  return "";
};

// 刷新排行榜数据

const refreshRanking = (difficulty, showMessage = true) => {
  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: "刷新中...",
    background: "rgba(0, 0, 0, 0.7)",
    target: document.querySelector(
      `.ranking-card:nth-child(${
        difficulty === "easy" ? 1 : difficulty === "medium" ? 2 : 3
      })`
    ),
  });

  // 发送API请求获取最新排行榜数据
  axios
    .get("/api/rankings", { params: { difficulty } })
    .then((response) => {
      const newRankings = response.data;
      // 根据难度更新对应的排行榜数据
      switch (difficulty) {
        case "easy":
          easyRankings.value = newRankings;
          break;
        case "medium":
          mediumRankings.value = newRankings;
          break;
        case "hard":
          hardRankings.value = newRankings;
          break;
      }
      if (showMessage) {
        ElMessage.success(
          `${
            difficulty === "easy"
              ? "简单"
              : difficulty === "medium"
              ? "中等"
              : "困难"
          }难度排行榜已刷新`
        );
      }
    })
    .catch((error) => {
      console.error("刷新排行榜失败:", error);
      ElMessage.error("刷新失败，请重试");
    })
    .finally(() => {
      loading.close();
    });
};

// 分析答卷
const analyzeAnswers = () => {
  // 添加参数验证
  if (!userId.value || !paperId.value) {
    ElMessage.error("用户ID和试卷ID不能为空");
    return;
  }
  analyzing.value = true;
  const loading = ElLoading.service({
    lock: true,
    text: "正在分析答卷...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  // 关闭现有SSE连接（如果存在）
  if (eventSource.value) {
    eventSource.value.close();
  }

  // 创建新的SSE连接
  const url = `/api/exam-analysis/analyze?userId=${userId.value}&paperId=${paperId.value}`;
  console.log("请求分析URL:", url); // 添加调试日志
  eventSource.value = new EventSource(url);

  // 监听SSE消息事件
  eventSource.value.onmessage = (event) => {
    console.log("收到SSE消息:", event.data); // 添加调试日志

    // 检查是否为结束信号
    if (event.data === "end") {
      eventSource.value.close();
      ElMessage.success("答卷分析完成");
      analyzing.value = false;
      loading.close();
    } else {
      // 解析分析结果
      try {
        const result = JSON.parse(event.data);
        analysisResult.value = result;
      } catch (e) {
        console.error("解析分析结果失败:", e, "原始数据:", event.data);
        ElMessage.error("分析结果格式错误");
        eventSource.value.close();
        analyzing.value = false;
        loading.close();
      }
    }
  };

  // 监听SSE错误事件 - 改进版
  eventSource.value.onerror = (error) => {
    console.error("SSE连接错误:", error);

    // 尝试获取HTTP错误状态
    if (eventSource.value.readyState === EventSource.CLOSED) {
      ElMessage.error("无法连接到分析服务，请检查网络或联系管理员");
    } else {
      ElMessage.error("分析过程发生错误，请重试");
    }

    eventSource.value.close();
    analyzing.value = false;
    loading.close();
  };
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.analysis-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
}

/* 导航栏样式 */
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
  padding: 8px 0;
  position: relative;
}

.nav-link:hover {
  color: #667eea;
}

.nav-link.active {
  color: #667eea;
  font-weight: 500;
}

.nav-link.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 1px;
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
  padding: 120px 24px 60px;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: clamp(48px, 6vw, 72px);
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
  font-size: 20px;
  color: #86868b;
  margin-bottom: 48px;
  font-weight: 400;
}

/* 排行榜区域 */
.rankings-section {
  padding: 0 24px 60px;
  max-width: 1200px;
  margin: 0 auto;
}

.rankings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 32px;
}

.ranking-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

/* 排行榜标题区域样式 */
.section-header.with-refresh {
  display: flex;
  flex-direction: column;
  /* 将align-items从flex-start改为center实现垂直居中 */
  align-items: center;
  position: relative;
  /* 添加文本居中 */
  text-align: center;
}
/* 刷新按钮样式 - 已放大尺寸 */
.refresh-button {
  position: absolute;
  top: 0;
  right: 0;
  background: transparent;
  border: none;
  color: #667eea;
  cursor: pointer;
  padding: 12px; /* 进一步增加内边距 */
  border-radius: 4px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px; /* 进一步增大图标尺寸 */
}

.refresh-button:hover {
  background-color: rgba(102, 126, 234, 0.1);
  transform: rotate(90deg);
}
/* 排行榜项目样式 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.8);
  transition: transform 0.2s ease;
}

.ranking-item:hover {
  transform: translateX(4px);
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  margin-right: 16px;
}

.rank-first {
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
}

.rank-second {
  background: linear-gradient(135deg, #c0c0c0 0%, #a9a9a9 100%);
}

.rank-third {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
}

.rank-number:not(.rank-first):not(.rank-second):not(.rank-third) {
  background: rgba(102, 126, 234, 0.2);
  color: #667eea;
}

.rank-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-small {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.rank-username {
  font-size: 16px;
  color: #1d1d1f;
  font-weight: 500;
}

.rank-score {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
}

/* 分析区域 */
.analysis-section {
  padding: 0 24px 120px;
  max-width: 1000px;
  margin: 0 auto;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 48px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.analysis-actions {
  text-align: center;
  margin: 24px 0 40px;
}

.analysis-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 28px;
  padding: 20px 48px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
  justify-content: center;
}

.analysis-button:not(:disabled):hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.4);
}

.analysis-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 分析结果区域 */
.analysis-results {
  margin-top: 32px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.analysis-item {
  padding: 24px;
  border-radius: 24px;
  background: rgba(248, 250, 252, 0.8);
}

.analysis-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.analysis-icon {
  color: #667eea;
}

/* 知识点标签样式 */
.knowledge-points {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.knowledge-tag {
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

/* 建议列表样式 */
.suggestions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.suggestion-item {
  display: flex;
  gap: 16px;
}

.suggestion-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.suggestion-text {
  font-size: 16px;
  color: #1d1d1f;
  line-height: 1.5;
}

/* 通用部分样式 */
.section-header {
  text-align: center;
  margin-bottom: 32px;
}

.section-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 32px;
  color: white;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.section-desc {
  font-size: 16px;
  color: #86868b;
  line-height: 1.5;
}

/* 加载动画 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .rankings-grid {
    grid-template-columns: 1fr;
  }

  .ranking-card,
  .analysis-card {
    padding: 32px 24px;
    border-radius: 24px;
  }

  .section-icon {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .section-title {
    font-size: 24px;
  }

  .analysis-button {
    padding: 16px 32px;
    font-size: 16px;
    min-width: 200px;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 80px 24px 40px;
  }

  .rankings-section,
  .analysis-section {
    padding: 0 16px 80px;
  }

  .ranking-card,
  .analysis-card {
    padding: 24px 16px;
  }

  .section-header {
    margin-bottom: 24px;
  }

  .ranking-item {
    padding: 10px 12px;
  }

  .rank-username,
  .rank-score {
    font-size: 14px;
  }
}
</style>
