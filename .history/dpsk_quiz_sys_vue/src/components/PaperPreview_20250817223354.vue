<template>
  <div class="paper-preview-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <div class="logo">
          <div class="logo-icon"></div>
          <span class="logo-text">Deep Quiz</span>
        </div>
        <div class="nav-links">
          <a href="#" class="nav-link" @click="goToHome">主页</a>
          <a href="#" class="nav-link" @click="goToGenerator">题目生成</a>
          <a href="#" class="nav-link">错题分析</a>
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
            试卷
            <span class="hero-highlight">预览</span>
          </h1>
          <p class="hero-subtitle">查看生成的试卷内容</p>
        </div>
      </section>

      <!-- 当前试卷预览 -->
      <section v-if="currentPaper" class="current-paper-section">
        <div class="section-header">
          <div class="section-icon">
            <el-icon><Document /></el-icon>
          </div>
          <h2 class="section-title">当前生成的试卷</h2>
          <p class="section-desc">刚刚生成的试卷内容预览</p>
        </div>

        <div class="paper-card current-paper">
          <div class="paper-header">
            <h3 class="paper-title">{{ currentPaper.title }}</h3>
            <div class="paper-meta">
              <span class="paper-info"
                >{{ currentPaper.questions.length }}道题</span
              >
              <span class="paper-info">{{
                currentPaper.difficulty || "中等"
              }}</span>
              <span class="paper-info">{{
                currentPaper.type || "综合练习"
              }}</span>
            </div>
          </div>

          <div class="paper-description">
            {{ currentPaper.description }}
          </div>

          <div class="questions-preview">
            <h4 class="preview-title">题目预览</h4>
            <div class="questions-list">
              <div
                v-for="(question, index) in currentPaper.questions.slice(0, 3)"
                :key="index"
                class="question-item"
              >
                <div class="question-number">{{ index + 1 }}.</div>
                <div class="question-content">
                  <div class="question-text">
                    {{
                      question.question ||
                      question.questionContent ||
                      question.content
                    }}
                  </div>
                  <div
                    v-if="getQuestionOptions(question).length > 0"
                    class="question-options"
                  >
                    <div
                      v-for="(option, optIndex) in getQuestionOptions(question)"
                      :key="optIndex"
                      class="option-item"
                    >
                      {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div
              v-if="currentPaper.questions.length > 3"
              class="more-questions"
            >
              还有 {{ currentPaper.questions.length - 3 }} 道题目...
            </div>
          </div>

          <div class="paper-actions">
            <el-button
              type="primary"
              size="large"
              @click="startExam(currentPaper)"
            >
              <el-icon><Edit /></el-icon>
              开始答题
            </el-button>
            <el-button size="large" @click="viewFullPaper(currentPaper)">
              <el-icon><View /></el-icon>
              查看完整试卷
            </el-button>
          </div>
        </div>
      </section>
    </main>

    <!-- 试卷详情弹窗 -->
    <el-dialog
      v-model="showPaperDetail"
      :title="selectedPaper?.paperName || selectedPaper?.title"
      width="80%"
      :before-close="closePaperDetail"
    >
      <div v-if="selectedPaper" class="paper-detail">
        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">题目数量：</span>
            <span class="meta-value"
              >{{
                selectedPaper.totalQuestion || selectedPaper.questions?.length
              }}道</span
            >
          </div>
          <div class="meta-item">
            <span class="meta-label">难度等级：</span>
            <span class="meta-value">{{ selectedPaper.difficulty }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">创建时间：</span>
            <span class="meta-value">{{
              formatDate(selectedPaper.createTime)
            }}</span>
          </div>
        </div>

        <div class="detail-description">
          <h4>试卷描述</h4>
          <p>{{ selectedPaper.description || "暂无描述" }}</p>
        </div>

        <div v-if="selectedPaper.questions" class="detail-questions">
          <h4>题目列表</h4>
          <div class="questions-list">
            <div
              v-for="(question, index) in selectedPaper.questions"
              :key="index"
              class="question-item detailed"
            >
              <div class="question-number">{{ index + 1 }}.</div>
              <div class="question-content">
                <div class="question-text">
                  {{
                    question.question ||
                    question.questionContent ||
                    question.content
                  }}
                </div>
                <div
                  v-if="getQuestionOptions(question).length > 0"
                  class="question-options"
                >
                  <div
                    v-for="(option, optIndex) in getQuestionOptions(question)"
                    :key="optIndex"
                    class="option-item"
                  >
                    {{ String.fromCharCode(65 + optIndex) }}. {{ option }}
                  </div>
                </div>
                <div v-if="question.answer" class="question-answer">
                  <strong>答案：</strong>{{ question.answer }}
                </div>
                <div v-if="question.explanation" class="question-explanation">
                  <strong>解析：</strong>{{ question.explanation }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closePaperDetail">关闭</el-button>
          <el-button type="primary" @click="startExam(selectedPaper)">
            <el-icon><Edit /></el-icon>
            开始答题
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import {
  UserFilled,
  CaretBottom,
  Document,
  Edit,
  View,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useRouter, useRoute } from "vue-router";
import { API_BASE_URL } from "../config/api";
import { getToken, requireAuth, authFetch } from "../utils/auth";

const router = useRouter();
const route = useRoute();

// 响应式数据
const currentPaper = ref(null);
const showPaperDetail = ref(false);
const selectedPaper = ref(null);

// 检查认证状态
const checkAuth = () => {
  return requireAuth(router);
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return "未知时间";
  return new Date(dateString).toLocaleString("zh-CN");
};

// 从后端API获取试卷数据
const loadPaperData = async (paperId) => {
  if (!checkAuth()) return;

  try {
    console.log("🔍 PaperPreview - 获取试卷数据，paperId:", paperId);

    // 首先获取试卷基本信息
    const paperResult = await authFetch(
      `${API_BASE_URL}/exam/papers/${paperId}`,
      {
        method: "GET",
      },
      router
    );

    console.log("📦 PaperPreview - 试卷基本信息API响应:", paperResult);

    if (paperResult.code !== 200) {
      ElMessage.error(
        "获取试卷基本信息失败：" + (paperResult.message || "未知错误")
      );
      return;
    }

    // 然后获取试卷题目列表
    const questionsResult = await authFetch(
      `${API_BASE_URL}/exam-question/paper/${paperId}`,
      {
        method: "GET",
      },
      router
    );

    console.log("📦 PaperPreview - 题目列表API响应:", questionsResult);

    // 合并试卷信息和题目数据
    const paperData = paperResult.data;
    const questions = questionsResult.code === 200 ? questionsResult.data : [];

    currentPaper.value = {
      id: paperData.paperId,
      examPaperId: paperData.paperId,
      title: paperData.paperName || generatePaperTitle(paperData),
      description: paperData.description || generatePaperDescription(paperData),
      questions: questions,
      difficulty: paperData.difficulty || "中等",
      type: paperData.type || "综合练习",
      totalQuestion: questions.length || paperData.totalQuestion || 0,
      timeLimit: paperData.duration || 60,
    };
    console.log("✅ PaperPreview - 试卷数据设置完成:", currentPaper.value);
  } catch (error) {
    console.error("❌ PaperPreview - 获取试卷数据异常:", error);
    ElMessage.error("获取试卷数据异常：" + error.message);
  }
};

// 页面加载时初始化
onMounted(async () => {
  // 优先从URL参数获取paperId
  const paperId = route.query.paperId;
  if (paperId) {
    console.log("🎯 PaperPreview - 从URL参数获取paperId:", paperId);
    await loadPaperData(paperId);
    return;
  }

  // 兼容原有的sessionStorage方式
  const dataKey = route.query.dataKey;
  if (dataKey) {
    const storedData = sessionStorage.getItem(dataKey);
    if (storedData) {
      try {
        const paperData = JSON.parse(storedData);
        currentPaper.value = {
          ...paperData,
          title: generatePaperTitle(paperData),
          description: generatePaperDescription(paperData),
        };
        // 清除临时存储
        sessionStorage.removeItem(dataKey);
      } catch (error) {
        console.error("解析试卷数据失败:", error);
      }
    }
  }
});

// 生成试卷标题
const generatePaperTitle = (paperData) => {
  const timestamp = new Date().toLocaleString("zh-CN");
  return `${paperData.type || "综合"}试卷 - ${timestamp}`;
};

// 生成试卷描述
const generatePaperDescription = (paperData) => {
  return `题目类型：${paperData.type || "综合"}，难度：${
    paperData.difficulty || "中等"
  }，题目数量：${paperData.total || paperData.questions?.length || 0}道。`;
};

// 开始考试
const startExam = (paper) => {
  // 添加详细的调试日志
  console.log("🚀 开始考试 - startExam方法被调用");
  console.log("📝 paper对象完整结构:", paper);
  console.log("🔍 paper对象所有字段:", Object.keys(paper || {}));
  console.log("🆔 paper.id值:", paper?.id);
  console.log("🆔 paper.examPaperId值:", paper?.examPaperId);
  console.log("🆔 paper.paperId值:", paper?.paperId);
  console.log("🆔 paper.paperid值:", paper?.paperid);
  console.log("🆔 paper对象类型:", typeof paper);

  // 尝试多种方式获取试卷ID
  const possibleIds = [
    paper?.id,
    paper?.examPaperId,
    paper?.paperId,
    paper?.paperid,
    paper?.examId,
    paper?.paper_id,
  ];

  console.log("🔍 所有可能的ID值:", possibleIds);
  const validId = possibleIds.find(
    (id) => id !== undefined && id !== null && id !== ""
  );
  console.log("✅ 找到的有效ID:", validId);

  // 准备考试数据
  const examData = {
    examPaperId: validId || paper?.id || paper?.examPaperId,
    questions: paper?.questions || [],
    type: paper?.type || "综合",
    difficulty: paper?.difficulty || "中等",
    total: paper?.totalQuestion || paper?.questions?.length || 0,
    examDuration: paper?.timeLimit || paper?.examDuration || 60,
  };

  console.log("📦 准备的考试数据:", examData);
  console.log("🔍 examData.examPaperId最终值:", examData.examPaperId);

  // 存储到sessionStorage
  const storageKey = "exam_data_" + Date.now();
  sessionStorage.setItem(storageKey, JSON.stringify(examData));

  // 跳转到考试页面
  router.push({
    name: "Exercise",
    query: {
      dataKey: storageKey,
    },
  });
};

// 查看完整试卷
const viewFullPaper = async (paper) => {
  if (paper.questions) {
    // 如果已有题目数据，直接显示
    selectedPaper.value = paper;
    showPaperDetail.value = true;
  } else {
    // 需要从后端获取题目数据
    if (!checkAuth()) return;

    try {
      const result = await authFetch(
        `${API_BASE_URL}/exam-question/paper/${paper.id}`,
        {
          method: "GET",
        },
        router
      );

      if (result.code === 200) {
        selectedPaper.value = {
          ...paper,
          questions: result.data,
        };
        showPaperDetail.value = true;
      } else {
        ElMessage.error("获取试卷详情失败：" + (result.message || "未知错误"));
      }
    } catch (error) {
      ElMessage.error("获取试卷详情异常：" + error.message);
    }
  }
};

// 关闭试卷详情弹窗
const closePaperDetail = () => {
  showPaperDetail.value = false;
  selectedPaper.value = null;
};

// 获取题目选项
const getQuestionOptions = (question) => {
  if (!question) {
    return [];
  }

  // 优先处理后端返回的questionOptions字段
  if (
    question.questionOptions &&
    typeof question.questionOptions === "object"
  ) {
    if (Array.isArray(question.questionOptions.options)) {
      return question.questionOptions.options;
    }
    if (Array.isArray(question.questionOptions)) {
      return question.questionOptions;
    }
  }

  // 处理后端返回的options字段（可能是字符串形式的JSON数组）
  if (question.options) {
    if (Array.isArray(question.options)) {
      return question.options;
    }
    if (typeof question.options === "string") {
      try {
        const parsedOptions = JSON.parse(question.options);
        if (Array.isArray(parsedOptions)) {
          return parsedOptions;
        }
      } catch (e) {
        console.log("解析options字符串失败:", e.message);
      }
    }
  }

  if (question.choices && Array.isArray(question.choices)) {
    return question.choices;
  }

  // 检查字符串形式的选项数组
  if (question.optionList && typeof question.optionList === "string") {
    try {
      const parsedOptions = JSON.parse(question.optionList);
      if (Array.isArray(parsedOptions)) {
        return parsedOptions;
      }
    } catch (e) {
      console.log("解析optionList失败:", e.message);
    }
  }

  // 如果选项是对象形式，尝试提取值
  if (
    question.optionA ||
    question.optionB ||
    question.optionC ||
    question.optionD
  ) {
    const options = [];
    if (question.optionA) options.push(question.optionA);
    if (question.optionB) options.push(question.optionB);
    if (question.optionC) options.push(question.optionC);
    if (question.optionD) options.push(question.optionD);
    if (question.optionE) options.push(question.optionE);
    if (question.optionF) options.push(question.optionF);
    return options;
  }

  return [];
};

// 导航方法
const goToHome = () => {
  router.push("/home");
};

const goToGenerator = () => {
  router.push("/generator");
};
</script>

<style scoped>
/* 基础样式 */
.paper-preview-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 导航栏样式 */
.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #1d1d1f;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: #1d1d1f;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  cursor: pointer;
}

.nav-link:hover {
  color: #667eea;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
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
  font-weight: 500;
  color: #1d1d1f;
}

/* 主体内容 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
}

/* Hero Section */
.hero-section {
  text-align: center;
  padding: 80px 0 60px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 16px;
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
}

/* 区域样式 */
.current-paper-section,
.history-papers-section {
  margin-bottom: 80px;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  color: white;
  font-size: 32px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.section-desc {
  font-size: 18px;
  color: #86868b;
}

/* 试卷卡片样式 */
.paper-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.paper-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.current-paper {
  margin-bottom: 48px;
}

.paper-header {
  margin-bottom: 16px;
}

.paper-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.paper-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.paper-info {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
}

.paper-time {
  background: rgba(134, 134, 139, 0.1);
  color: #86868b;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.paper-description {
  color: #86868b;
  margin-bottom: 24px;
  line-height: 1.6;
}

/* 题目预览 */
.questions-preview {
  margin-bottom: 32px;
}

.preview-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.questions-list {
  space-y: 16px;
}

.question-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  margin-bottom: 16px;
}

.question-item.detailed {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.question-number {
  font-weight: 600;
  color: #667eea;
  min-width: 24px;
}

.question-content {
  flex: 1;
}

.question-text {
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 8px;
  line-height: 1.5;
}

.question-options {
  margin-top: 8px;
}

.option-item {
  color: #86868b;
  margin-bottom: 4px;
  padding-left: 16px;
}

.question-answer {
  margin-top: 12px;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8px;
  color: #667eea;
  font-size: 14px;
}

.question-explanation {
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(134, 134, 139, 0.1);
  border-radius: 8px;
  color: #86868b;
  font-size: 14px;
  line-height: 1.4;
}

.more-questions {
  text-align: center;
  color: #86868b;
  font-style: italic;
  margin-top: 16px;
}

/* 操作按钮 */
.paper-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 弹窗样式 */
.paper-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 14px;
  color: #86868b;
  font-weight: 500;
}

.meta-value {
  font-size: 16px;
  color: #1d1d1f;
  font-weight: 600;
}

.detail-description {
  margin-bottom: 24px;
}

.detail-description h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.detail-questions h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

/* Element Plus 样式覆盖 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 500;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.custom-dropdown {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .main-content {
    padding: 0 16px;
  }

  .hero-title {
    font-size: 36px;
  }

  .paper-card {
    padding: 24px 16px;
  }

  .paper-actions {
    flex-direction: column;
  }

  .detail-meta {
    grid-template-columns: 1fr;
  }
}
</style>
