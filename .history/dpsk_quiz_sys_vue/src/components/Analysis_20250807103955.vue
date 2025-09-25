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
              <!-- <p class="section-desc">前五名高分用户</p> -->
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
              <!-- <p class="section-desc">前五名高分用户</p> -->
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
              <!-- <p class="section-desc">前五名高分用户</p> -->
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
      <!-- 试卷列表区域 -->
      <section class="rankings-section">
        <div class="rankings-grid">
          <div class="ranking-card">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Document /></el-icon>
              </div>
              <h2 class="section-title">我的试卷列表</h2>
            </div>

            <div v-if="loading" class="loading-container">
              <el-loading spinner="el-icon-loading"></el-loading>
            </div>

            <div class="paper-list" v-loading="loading">
              <div v-for="paper in papers" :key="paper.id" class="paper-item">
                <div class="paper-info" @click="showPaperDetails(paper)">
                  <h3 class="paper-title">
                    {{ paper.paperName }}
                  </h3>
                  <p class="paper-difficulty">
                    难度：{{ formatDifficulty(paper.paperDifficulty) }}
                  </p>
                  <p class="paper-time">提交时间：{{ paper.endTime }}</p>
                </div>
                <div class="paper-score">
                  得分：<span class="score-value">{{ paper.totalScore }}</span>
                </div>
              </div>
            </div>
            <el-dialog
              v-model="showPaperInfoDialog"
              width="70%"
              height="60%"
              append-to-body
            >
              <div class="paper-detail">
                <p>试卷名称：{{ currentPaper.paperName }}</p>
                <p>
                  难度：{{ formatDifficulty(currentPaper.paperDifficulty) }}
                </p>
                <p>提交时间：{{ currentPaper.endTime }}</p>
                <!-- <p>总分：{{ currentPaper.totalScore }}</p> -->
                <div class="score-time-section">
                  <div class="score-time-card">
                    <div class="section-header">
                      <div class="section-icon">
                        <el-icon><DataAnalysis /></el-icon>
                      </div>
                      <h2 class="section-title">答题数据统计</h2>
                      <!-- <p class="section-desc">本次考试的详细数据</p> -->
                    </div>

                    <div class="stats-grid">
                      <div class="stat-item">
                        <div class="stat-label">总得分</div>
                        <div class="stat-value">
                          {{ currentPaper.totalScore }} 分
                        </div>
                      </div>
                      <div class="stat-item">
                        <div class="stat-label">答题用时</div>
                        <div class="stat-value">
                          {{ currentPaper.duration }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <section class="analysis-section">
                  <div class="analysis-card">
                    <div class="section-header">
                      <div class="section-icon">
                        <el-icon><Histogram /></el-icon>
                      </div>
                      <h2 class="section-title">个人答题分析</h2>
                      <!-- <p class="section-desc">查看你的知识掌握情况和提升建议</p> -->
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

                    <div v-if="analysisResult" class="analysis-results">
                      <div
                        v-if="analysisResult.weakPoints"
                        class="analysis-item"
                      >
                        <h3 class="analysis-title">
                          <el-icon class="analysis-icon"
                            ><WarningFilled
                          /></el-icon>
                          用户欠缺知识点分析
                        </h3>
                        <pre class="knowledge-content">{{
                          analysisResult.weakPoints
                        }}</pre>
                      </div>

                      <div
                        v-if="analysisResult.suggestions"
                        class="analysis-item"
                      >
                        <h3 class="analysis-title">
                          <el-icon class="analysis-icon"
                            ><InfoFilled
                          /></el-icon>
                          学习建议
                        </h3>
                        <pre class="suggestions">{{
                          analysisResult.suggestions
                        }}</pre>
                      </div>

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
              </div>
            </el-dialog>
          </div>
        </div>
      </section>
      <!-- 新增答题数据统计区域
      <section class="score-time-section">
        <div class="score-time-card">
          <div class="section-header">
            <div class="section-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <h2 class="section-title">答题数据统计</h2>
            <p class="section-desc">本次考试的详细数据</p>
          </div>

          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">总得分</div>
              <div class="stat-value">{{ score }} 分</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">答题用时</div>
              <div class="stat-value">{{ timeSpent }}</div>
            </div>
          </div>
        </div>
      </section> -->
      <!-- 答卷分析区域 -->
      <!-- <section class="analysis-section">
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
          </div> -->

      <!-- 分析结果区域 -->
      <!-- <div v-if="analysisResult" class="analysis-results"> -->
      <!-- 调试信息 -->
      <!-- <div class="debug-info" v-if="analyzing">
              <p>缓冲区内容: {{ tempBuffer }}</p>
              <p>缓冲区长度: {{ tempBuffer.length }}</p>
            </div> -->

      <!-- 欠缺知识点分析 -->
      <!-- <div v-if="analysisResult.weakPoints" class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><WarningFilled /></el-icon>
                用户欠缺知识点分析
              </h3>
              <pre class="knowledge-content">{{
                analysisResult.weakPoints
              }}</pre>
            </div> -->

      <!-- 用户建议 -->
      <!-- <div v-if="analysisResult.suggestions" class="analysis-item">
              <h3 class="analysis-title">
                <el-icon class="analysis-icon"><InfoFilled /></el-icon>
                学习建议
              </h3>
              <pre class="suggestions">{{ analysisResult.suggestions }}</pre>
            </div> -->

      <!-- 当没有任何结果时的提示 -->
      <!-- <div
              v-if="
                !analysisResult.weakPoints &&
                !analysisResult.suggestions &&
                !analyzing
              "
              class="empty-state"
            >
              暂无分析数据，请点击上方"开始分析"按钮
            </div> -->
      <!-- </div>
        </div>
      </section> -->
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
import { useUserStore } from "../stores/user";
// 排行榜数据 - 初始化为空数组
const easyRankings = ref([]);
const mediumRankings = ref([]);
const hardRankings = ref([]);
// 添加用户ID和试卷ID (实际应用中应从登录状态或路由参数获取)
// const userId = ref(1); // 示例用户ID，实际应从登录信息获取
const userStore = useUserStore();
const userId = ref(userStore.id);
const paperId = ref(1); // 示例试卷ID，实际应根据选择的试卷获取
const score = ref(); // 示例分数，实际应从接口获取
const timeSpent = ref(); // 示例用时，实际应从接口获取
const papers = ref([]);
const loading = ref(false);
const showPaperInfoDialog = ref(false);
const currentPaper = ref({});
// SSE事件源引用
const eventSource = ref(null);
onMounted(() => {
  refreshRanking("easy", false);
  refreshRanking("medium", false);
  refreshRanking("hard", false);
  fetchScoreData();
  fetchPapers();
});

const fetchPapers = async () => {
  if (!userId.value) {
    ElMessage.error("请先登录！"); // 使用Element Plus提示（假设项目已集成）
    return;
  }
  try {
    loading.value = true;
    const response = await axios.get(
      `/api/exam-analysis/papers?userId=${userId.value}`
    );
    papers.value = response.data;
  } catch (error) {
    ElMessage.error("获取试卷列表失败");
  } finally {
    loading.value = false;
  }
};
// 新增获取成绩数据方法
const fetchScoreData = () => {
  axios
    .get("/api/exam-analysis/score", {
      params: {
        userId: userId.value,
        paperId: paperId.value,
      },
    })
    .then((response) => {
      score.value = response.data.score;
      timeSpent.value = response.data.duration;
    })
    .catch((error) => {
      console.error("获取成绩数据失败:", error);
      ElMessage.error("无法获取考试数据");
    });
};
// 添加tempBuffer状态变量
const tempBuffer = ref("");
// 组件卸载时清理SSE连接
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close();
  }
});

// 弹窗
const showPaperDetails = (paper) => {
  currentPaper.value = paper; // 通过.value访问ref变量
  showPaperInfoDialog.value = true; // 显示模态框
};
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
  console.log("currentPaper.value", currentPaper.value);
  paperId.value = currentPaper.value.paperId;

  if (!userId.value || !paperId.value) {
    ElMessage.error("用户ID和试卷ID不能为空");
    return;
  }
  console.log("userid", userId.value);
  console.log("paperid", paperId.value);
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
const formatDifficulty = (difficulty) => {
  switch (difficulty) {
    case "easy":
      return "简单";
    case "medium":
      return "中等";
    case "difficult":
      return "困难";
    default:
      return difficulty; // 未知值保持原样
  }
};
</script>
<style scoped>
/* 新增样式 */
.score-time-section {
  margin: 40px auto;
  max-width: 1200px;
  padding: 0 24px;
}

.score-time-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
  margin-top: 32px;
}

.stat-item {
  text-align: center;
  padding: 24px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 16px;
}

.stat-label {
  font-size: 16px;
  color: #667eea;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
}

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

.analysis-results {
  min-height: 200px;
  padding: 20px;
}

.analysis-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.weak-points,
.suggestions {
  padding: 16px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符 */
}

/* 添加知识点内容样式 */
.knowledge-content {
  padding: 16px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符 */
}

.suggestions {
  padding: 16px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符 */
}

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
  font-size: 48px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 24px;
  line-height: 1.2;
}

.hero-highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 20px;
  color: #86868b;
  margin-bottom: 40px;
  line-height: 1.6;
}

/* 排行榜区域 */
.rankings-section {
  margin: 40px auto;
  max-width: 1200px;
  padding: 0 24px;
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

.section-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  position: relative;
}

.section-header.with-refresh {
  justify-content: space-between;
  padding-right: 40px;
}

.section-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.section-desc {
  font-size: 16px;
  color: #86868b;
  margin: 4px 0 0 64px;
}

.refresh-button {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86868b;
  transition: all 0.3s ease;
}

.refresh-button:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-50%) rotate(180deg);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  transition: background-color 0.3s ease;
}

.ranking-item:hover {
  background: rgba(102, 126, 234, 0.05);
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  background: #f3f4f6;
  color: #6b7280;
}

.rank-number.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffa500);
  color: #1d1d1f;
}

.rank-number.rank-second {
  background: linear-gradient(135deg, #c0c0c0, #a9a9a9);
  color: #1d1d1f;
}

.rank-number.rank-third {
  background: linear-gradient(135deg, #cd7f32, #a0522d);
  color: #1d1d1f;
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
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.rank-username {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
}

.rank-score {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

/* 分析区域 */
.analysis-section {
  margin: 40px auto;
  max-width: 1200px;
  padding: 0 24px;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.analysis-actions {
  text-align: center;
  margin: 40px 0;
}

.analysis-button {
  padding: 16px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.analysis-button:hover:not(.loading) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.analysis-button.loading {
  opacity: 0.8;
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

.analysis-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 20px 0;
}

.analysis-icon {
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .rankings-grid {
    grid-template-columns: 1fr;
  }

  .nav-content {
    flex-direction: column;
    gap: 16px;
    height: auto;
    padding: 16px;
  }

  .nav-links {
    gap: 16px;
  }

  .hero-section {
    padding: 100px 16px 40px;
  }

  .ranking-card,
  .analysis-card {
    padding: 24px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .section-desc {
    margin-left: 0;
  }
}
.paper-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.paper-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.2s;
}

.paper-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.paper-title {
  font-size: 1rem;
  color: #303133;
  margin-bottom: 0.4rem;
}

.paper-time {
  font-size: 0.9rem;
  color: #606266;
}

.paper-score {
  flex-shrink: 0;
  margin-left: 20px;
}

.score-value {
  color: #409eff;
  font-weight: 600;
}
.loading-container {
  text-align: center;
  padding: 2rem;
  color: #606266;
}
.paper-detail {
  padding: 20px; /* 内容与弹窗边缘的间距 */
}
.paper-detail p {
  font-size: 16px; /* 基础字体大小 */
  color: #333; /* 深灰色文字（比默认更清晰） */
  line-height: 1.6; /* 行高（提升阅读流畅性） */
  margin: 10px 0; /* 段落上下间距 */
}
.paper-detail p:first-child {
  font-weight: bold; /* 试卷名称加粗 */
  font-size: 18px; /* 标题级字体稍大 */
  color: #2c3e50; /* 深蓝色突出标题 */
}
</style>
