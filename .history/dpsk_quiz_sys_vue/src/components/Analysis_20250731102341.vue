<template>
  <div class="analysis-container">
    <!-- 顶部导航栏 (与其他页面保持一致) -->
    <nav class="navbar">
      <div class="nav-content">
        <div class="logo">
          <div class="logo-icon"></div>
          <span class="logo-text">Deep Quiz</span>
        </div>
        <div class="nav-links">
          <a href="#" class="nav-link">主页</a>
          <a href="#" class="nav-link">题目生成</a>
          <a href="#" class="nav-link active">错题分析</a>
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
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">简单难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
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
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">中等难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
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
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <h2 class="section-title">困难难度排行</h2>
              <p class="section-desc">前五名高分用户</p>
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
                <el-icon class="analysis-icon"><LightbulbFilled /></el-icon>
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
import { ref } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import {
  UserFilled,
  CaretBottom,
  Trophy,
  Histogram,
  Document,
  WarningFilled,
  LightbulbFilled,
} from "@element-plus/icons-vue";

// 排行榜模拟数据
const easyRankings = ref([
  { username: "张三", score: 98 },
  { username: "李四", score: 95 },
  { username: "王五", score: 92 },
  { username: "赵六", score: 90 },
  { username: "钱七", score: 88 },
]);

const mediumRankings = ref([
  { username: "孙八", score: 94 },
  { username: "周九", score: 91 },
  { username: "吴十", score: 89 },
  { username: "郑一", score: 87 },
  { username: "王二", score: 85 },
]);

const hardRankings = ref([
  { username: "冯三", score: 90 },
  { username: "陈四", score: 88 },
  { username: "褚五", score: 85 },
  { username: "卫六", score: 82 },
  { username: "蒋七", score: 80 },
]);

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

// 分析答卷
const analyzeAnswers = async () => {
  analyzing.value = true;
  const loading = ElLoading.service({
    lock: true,
    text: "正在分析答卷...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 2000));

    // 模拟后端返回数据
    analysisResult.value = {
      weakPoints: [
        "JavaScript闭包概念",
        "Vue组件通信",
        "异步编程模式",
        "CSS Flex布局",
        "HTTP状态码",
      ],
      suggestions: [
        "加强JavaScript高级特性的学习，特别是闭包和原型链相关知识",
        "多练习Vue组件间通信方式，包括props、emit、vuex和provide/inject",
        "深入理解异步编程模型，学习Promise、async/await的高级应用",
        "系统学习CSS布局技术，特别是Flex和Grid布局的使用场景",
        "熟悉常用HTTP状态码和RESTful API设计规范",
      ],
    };

    ElMessage.success("答卷分析完成");
  } catch (error) {
    console.error("分析失败:", error);
    ElMessage.error("分析失败，请重试");
  } finally {
    analyzing.value = false;
    loading.close();
  }
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

/* 导航栏样式 (与其他页面保持一致) */
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

/* 通用部分样式 (与其他页面保持一致) */
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
