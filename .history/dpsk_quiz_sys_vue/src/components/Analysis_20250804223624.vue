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
            <!-- 调试信息 -->
            <!-- <div class="debug-info" v-if="analyzing">
              <p>缓冲区内容: {{ tempBuffer }}</p>
              <p>缓冲区长度: {{ tempBuffer.length }}</p>
            </div> -->

            <!-- 欠缺知识点分析 -->
            <div v-if="analysisResult.weakPoints" class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><WarningFilled /></el-icon>
                用户欠缺知识点分析
              </h3>
              <pre class="knowledge-content">{{
                analysisResult.weakPoints
              }}</pre>
            </div>

            <!-- 用户建议 -->
            <div v-if="analysisResult.suggestions" class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><InfoFilled /></el-icon>
                学习建议
              </h3>
              <pre class="suggestions">{{ analysisResult.suggestions }}</pre>
            </div>

            <!-- 当没有任何结果时的提示 -->
            <div
              v-if="
                !analysisResult.weakPoints &&
                !analysisResult.suggestions &&
                !analyzing
              "
              class="empty-state"
            >
              暂无分析数据，请点击上方"开始分析"按钮
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
// 添加tempBuffer状态变量
const tempBuffer = ref("");
// 组件卸载时清理SSE连接
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close();
  }
});

// 分析状态和结果
const analyzing = ref(false);
const analysisResult = ref({
  weakPoints: "",
  suggestions: "", // 确保初始化为空字符串而非数组
});
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
  if (!userId.value || !paperId.value) {
    ElMessage.error("用户ID和试卷ID不能为空");
    return;
  }

  analyzing.value = true;
  // 重置分析结果和缓冲区
  analysisResult.value = {
    weakPoints: "",
    suggestions: "",
  };
  tempBuffer.value = "";

  const loading = ElLoading.service({
    lock: true,
    text: "正在分析答卷...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  console.log("开始分析答卷...");
  console.log(`用户ID: ${userId.value}, 试卷ID: ${paperId.value}`);

  // 关闭现有SSE连接（如果存在）
  if (eventSource.value) {
    console.log("关闭现有的SSE连接");
    eventSource.value.close();
  }

  const url = `/api/exam-analysis/analyze?userId=${userId.value}&paperId=${paperId.value}`;
  console.log(`创建新的SSE连接: ${url}`);
  eventSource.value = new EventSource(url);

  eventSource.value.onopen = () => {
    console.log("SSE连接已建立");
  };

  eventSource.value.onmessage = (event) => {
    console.log("收到SSE原始消息:", event);

    // 结束信号
    if (event.data === "end") {
      console.log("收到结束信号");

      // 最后处理缓冲区内容
      processBufferContent();

      // 如果缓冲区还有内容，且没有分配到任何部分
      if (tempBuffer.value.trim()) {
        console.log("缓冲区剩余内容:", tempBuffer.value.trim());

        // 如果知识点部分为空，将剩余内容分配给知识点
        if (!analysisResult.value.weakPoints) {
          console.log("将剩余内容分配给知识点分析");
          analysisResult.value.weakPoints = tempBuffer.value.trim();
        }
        // 如果建议部分为空，将剩余内容分配给建议
        else if (!analysisResult.value.suggestions) {
          console.log("将剩余内容分配给学习建议");
          analysisResult.value.suggestions = tempBuffer.value.trim();
        }

        tempBuffer.value = "";
      }

      eventSource.value.close();
      console.log("SSE连接已关闭");
      ElMessage.success("答卷分析完成");
      analyzing.value = false;
      loading.close();
      return;
    }
    try {
      console.log("解析JSON数据:", event.data);
      const result = JSON.parse(event.data);

      if (result.content) {
        console.log("收到内容片段:", result.content);
        tempBuffer.value += result.content;
        console.log("当前缓冲区内容:", tempBuffer.value);

        // 尝试处理缓冲区内容
        processBufferContent();
      }
    } catch (e) {
      console.error("解析分析结果失败:", e, "原始数据:", event.data);
    }
  };

  // 处理缓冲区内容的独立函数
  const processBufferContent = () => {
    console.log("处理缓冲区内容...");

    // 更灵活的分隔符匹配
    const knowledgeRegex = /【用户欠缺知识点分析】([\s\S]*?)(?=【用户建议】)/;
    const suggestionRegex = /【用户建议】([\s\S]*)/;

    const knowledgeMatch = knowledgeRegex.exec(tempBuffer.value);
    if (knowledgeMatch && knowledgeMatch[1]) {
      analysisResult.value.weakPoints = knowledgeMatch[1].trim();
      tempBuffer.value = tempBuffer.value.replace(knowledgeRegex, "");
    } else {
      console.log("未找到知识点分析内容");
    }

    const suggestionMatch = suggestionRegex.exec(tempBuffer.value);
    if (suggestionMatch && suggestionMatch[1]) {
      analysisResult.value.suggestions = suggestionMatch[1].trim();
      tempBuffer.value = tempBuffer.value.replace(suggestionRegex, "");
    } else {
      console.log("未找到建议内容");
    }

    // 强制更新视图
    analysisResult.value = { ...analysisResult.value };
  };

  eventSource.value.onerror = (error) => {
    console.error("SSE连接错误:", error);
    console.log("SSE连接状态:", eventSource.value.readyState);

    ElMessage.error("分析过程发生错误，请重试");

    if (eventSource.value) {
      eventSource.value.close();
      console.log("SSE连接因错误关闭");
    }

    analyzing.value = false;
    loading.close();
  };
};
</script>

<style scoped>
/* 调试信息样式 */
.debug-info {
  background-color: #f8f8f8;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  font-family: monospace;
  font-size: 14px;
  color: #666;
  overflow-wrap: break-word;
}

.debug-info p {
  margin: 5px 0;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

/* 统一分析结果容器样式 */
.analysis-results {
  min-height: 200px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.analysis-item {
  margin-bottom: 20px;
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 统一知识点和建议内容样式 */
.weak-points,
.suggestions {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  padding: 20px;
  background: white;
  white-space: pre-wrap;
  word-break: break-word;
  border-left: 4px solid;
}

/* 知识点特定样式 */
.weak-points {
  border-left-color: #667eea;
}

/* 建议特定样式 */
.suggestions {
  border-left-color: #4caf50;
}

/* 统一列表项样式 */
.weak-point-item,
.suggestion-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  align-items: flex-start;
  padding: 8px 0;
}

/* 统一序号样式 */
.weak-point-number,
.suggestion-number {
  width: 26px;
  height: 26px;
  min-width: 26px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  margin-top: 2px;
}

.suggestion-number {
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
}

/* 统一内容样式 */
.weak-point-content,
.suggestion-content {
  flex: 1;
  padding: 2px 0;
}

/* 标题样式 */
.analysis-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  padding: 16px 20px;
  background: rgba(248, 250, 252, 0.9);
  border-bottom: 1px solid #eee;
}

.analysis-icon {
  font-size: 20px;
}

/* 基础布局样式 */
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

/* 主内容区域 */
.main-content {
  padding-top: 60px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .weak-points,
  .suggestions {
    padding: 16px;
    font-size: 14px;
  }

  .weak-point-item,
  .suggestion-item {
    gap: 8px;
  }

  .analysis-title {
    font-size: 16px;
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .weak-point-number,
  .suggestion-number {
    width: 22px;
    height: 22px;
    min-width: 22px;
    font-size: 12px;
  }
}
</style>
