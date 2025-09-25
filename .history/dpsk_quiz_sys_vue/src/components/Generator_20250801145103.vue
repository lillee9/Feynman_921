<template>
  <div class="generator-container">
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
            智能
            <span class="hero-highlight">题目生成</span>
          </h1>
          <p class="hero-subtitle">上传学习资料，AI为您量身定制专属题目</p>
        </div>
      </section>

      <!-- 生成器卡片 -->
      <section class="generator-section">
        <div class="generator-card">
          <!-- 上传区域 -->
          <div class="upload-section">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><UploadFilled /></el-icon>
              </div>
              <h2 class="section-title">上传学习资料</h2>
              <p class="section-desc">支持多种格式，智能识别内容生成题目</p>
            </div>

            <el-upload
              class="upload-area"
              drag
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :before-upload="beforeUpload"
              :accept="supportedFormats"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <el-icon><UploadFilled /></el-icon>
                </div>
                <div class="upload-text">
                  <span class="upload-main-text">拖拽文件到此处</span>
                  <span class="upload-sub-text">或点击选择文件</span>
                </div>
              </div>
              <template #tip>
                <div class="upload-tip">
                  支持 {{ supportedFormatsText }} 格式，大小不超过
                  {{ maxSizeMB }}MB
                </div>
              </template>
            </el-upload>

            <!-- 上传进度 -->
            <div v-if="uploadProgress > 0" class="progress-section">
              <div class="progress-wrapper">
                <el-progress
                  :percentage="uploadProgress"
                  :status="uploadStatus"
                  :stroke-width="8"
                  :show-text="false"
                />
                <span class="progress-text">{{ uploadStatusText }}</span>
              </div>
            </div>
          </div>

          <!-- 参数设置区域 -->
          <div class="params-section">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Setting /></el-icon>
              </div>
              <h2 class="section-title">题目设置</h2>
              <p class="section-desc">自定义题目类型、数量和难度</p>
            </div>

            <div class="params-grid">
              <div class="param-card">
                <label class="param-label">题目类型</label>
                <el-select
                  v-model="questionType"
                  placeholder="选择题型"
                  class="param-select"
                >
                  <el-option
                    v-for="item in questionTypes"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>

              <div class="param-card">
                <label class="param-label">题目数量</label>
                <el-select
                  v-model="questionCount"
                  placeholder="选择数量"
                  class="param-select"
                >
                  <el-option
                    v-for="item in questionCounts"
                    :key="item"
                    :label="`${item} 道题`"
                    :value="item"
                  />
                </el-select>
              </div>

              <div class="param-card">
                <label class="param-label">题目难度</label>
                <el-select
                  v-model="questionDifficulty"
                  placeholder="选择难度"
                  class="param-select"
                >
                  <el-option
                    v-for="item in questionDifficulties"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </div>
            </div>
          </div>

          <!-- 生成按钮 -->
          <div class="action-section">
            <button
              class="generate-button"
              :disabled="
                !selectedFile ||
                uploadProgress < 100 ||
                !questionType ||
                !questionCount ||
                !questionDifficulty
              "
              :class="{ loading: generating }"
              @click="generateQuestions"
            >
              <span v-if="!generating" class="button-content">
                <el-icon><Star /></el-icon>
                开始生成题目
              </span>
              <span v-else class="button-content">
                <div class="loading-spinner"></div>
                正在生成中...
              </span>
            </button>
            <p class="action-tip">确保所有设置完成后点击生成</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElLoading } from "element-plus";
import {
  UploadFilled,
  UserFilled,
  CaretBottom,
  Setting,
  Star,
} from "@element-plus/icons-vue";

const router = useRouter();

// 文件上传配置
const maxSizeMB = 10;
const supportedFormats = ".pdf,.doc,.docx,.txt,.ppt,.png,.pptx";
const supportedFormatsText = "PDF、Word、PPT、TXT";

const selectedFile = ref(null);
const generating = ref(false);
const uploadProgress = ref(0);
const uploadStatus = ref("");
const uploadStatusText = ref("");

// 题目参数
const questionType = ref("");
const questionCount = ref("");
const questionDifficulty = ref("");

const questionTypes = [
  { value: "CHOICE", label: "选择题" },
  { value: "FILL_BLANK", label: "填空题" },
  { value: "SHORT_ANSWER", label: "简答题" },
];

const questionCounts = [2, 5, 10, 15, 20, 25, 30];

const questionDifficulties = [
  { value: "EASY", label: "简单" },
  { value: "MEDIUM", label: "中等" },
  { value: "HARD", label: "困难" },
];

const handleFileChange = (file) => {
  selectedFile.value = file.raw;
  uploadProgress.value = 0;
  uploadStatus.value = "";
  uploadStatusText.value = "";
  simulateUploadProgress();
};

const beforeUpload = (file) => {
  const extension = file.name.split(".").pop().toLowerCase();
  const supportedExtensions = supportedFormats.replace(/\./g, "").split(",");
  if (!supportedExtensions.includes(extension)) {
    ElMessage.error(
      `不支持的文件格式: ${extension}，请上传 ${supportedFormatsText} 格式的文件`
    );
    return false;
  }
  const isLtSize = file.size / 1024 / 1024 <= maxSizeMB;
  if (!isLtSize) {
    ElMessage.error(`文件大小不能超过 ${maxSizeMB}MB!`);
    return false;
  }
  return true;
};

const simulateUploadProgress = () => {
  uploadStatus.value = "success";
  uploadStatusText.value = "上传中...";
  const interval = setInterval(() => {
    if (uploadProgress.value < 100) {
      uploadProgress.value += 10;
    } else {
      clearInterval(interval);
      uploadStatusText.value = "上传成功";
      ElMessage.success("文件上传成功");
    }
  }, 300);
};

const generateQuestions = async () => {
  if (!selectedFile.value) {
    ElMessage.warning("请先上传文件");
    return;
  }

  if (
    !questionType.value ||
    !questionCount.value ||
    !questionDifficulty.value
  ) {
    ElMessage.warning("请完善题目设置");
    return;
  }

  generating.value = true;
  const loading = ElLoading.service({
    lock: true,
    text: "正在生成题目...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  try {
    const formData = new FormData();
    formData.append("image", selectedFile.value);
    formData.append("questionType", questionType.value.toUpperCase());
    formData.append("questionCount", questionCount.value);
    formData.append("difficulty", questionDifficulty.value.toUpperCase());

    const response = await fetch("/api/question/generate", {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "生成题目失败");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    const questions = [];
    let buffer = "";
    let loadingInstance = loading;

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");

      buffer = lines.pop() || "";

      for (const line of lines) {
        if (line.startsWith("data:")) {
          const data = line.slice(5).trim();

          if (data === "end" || data === "[DONE]") {
            console.log("接收到结束信号");
            break;
          }

          if (data && data !== "") {
            try {
              const parsed = JSON.parse(data);
              console.log("解析到数据:", parsed);

              if (parsed.type === "progress") {
                if (loadingInstance) {
                  loadingInstance.text = parsed.message || "正在生成题目...";
                }
                console.log("进度更新:", parsed.progress + "%");
              } else if (parsed.type === "error") {
                throw new Error(parsed.error || "生成过程中发生错误");
              } else if (parsed.type === "summary") {
                console.log("生成完成，共", parsed.total, "道题目");
                ElMessage.success(`成功生成${parsed.total}道题目`);
              } else if (parsed.question && parsed.answer) {
                questions.push(parsed);
                console.log(
                  `收到第${questions.length}道题目:`,
                  parsed.question
                );

                if (loadingInstance) {
                  loadingInstance.text = `正在生成题目... (${questions.length}道)`;
                }
              } else if (parsed.streamData === true) {
                console.log("忽略流式片段:", parsed.content);
              } else {
                console.log("收到其他类型数据:", parsed);
              }
            } catch (parseError) {
              console.log("跳过非JSON数据:", data);
            }
          }
        }
      }
    }

    if (questions.length === 0) {
      throw new Error("没有生成任何有效题目");
    }

    console.log("🎯 最终收集到的题目:", questions);
    console.log("🎯 题目数量:", questions.length);

    questions.forEach((q, index) => {
      console.log(`📋 准备传递的题目 ${index + 1}:`, {
        id: q.id,
        type: q.type,
        question: q.question?.substring(0, 50) + "...",
        optionsCount: q.options?.length,
        answer: q.answer,
        hasExplanation: !!q.explanation,
      });
    });

    const dataToPass = {
      questions: questions,
      type: questionType.value,
      difficulty: questionDifficulty.value,
      total: questions.length,
    };

    console.log("📦 准备传递给 Exercise 页面的完整数据:", dataToPass);

    const jsonString = JSON.stringify(dataToPass);
    console.log("📦 JSON 字符串长度:", jsonString.length);
    console.log("📦 JSON 字符串前100个字符:", jsonString.substring(0, 100));

    console.log("🚀 开始跳转到 Exercise 页面...");
    const storageKey = "temp_questions_" + Date.now();
    sessionStorage.setItem(storageKey, jsonString);
    console.log("💾 数据已存储到 sessionStorage，key:", storageKey);

    router.push({
      name: "Exercise",
      query: {
        dataKey: storageKey,
      },
    });
    console.log("✅ 跳转命令已执行");
  } catch (error) {
    console.error("生成失败：", error);
    ElMessage.error(error.message || "生成失败");
  } finally {
    generating.value = false;
    loading.close();
  }
};
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.generator-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
}

/* 导航栏样式（与Home页面一致） */
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

/* 生成器区域 */
.generator-section {
  padding: 0 24px 120px;
  max-width: 1000px;
  margin: 0 auto;
}

.generator-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 48px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

/* 区域头部 */
.section-header {
  text-align: center;
  margin-bottom: 48px;
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
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.section-desc {
  font-size: 18px;
  color: #86868b;
  line-height: 1.5;
}

/* 上传区域 */
.upload-section {
  margin-bottom: 80px;
}

.upload-area {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.upload-content {
  padding: 60px 40px;
  text-align: center;
}

.upload-icon {
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

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-main-text {
  font-size: 20px;
  font-weight: 500;
  color: #1d1d1f;
}

.upload-sub-text {
  font-size: 16px;
  color: #86868b;
}

.upload-tip {
  margin-top: 16px;
  color: #86868b;
  font-size: 14px;
  text-align: center;
}

/* 进度条 */
.progress-section {
  margin-top: 32px;
}

.progress-wrapper {
  max-width: 400px;
  margin: 0 auto;
  text-align: center;
}

.progress-text {
  margin-top: 12px;
  color: #86868b;
  font-size: 14px;
}

/* 参数设置区域 */
.params-section {
  margin-bottom: 80px;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
  max-width: 800px;
  margin: 0 auto;
}

.param-card {
  background: rgba(248, 250, 252, 0.8);
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  transition: all 0.3s ease;
}

.param-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.param-label {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.param-select {
  width: 100%;
}

/* 操作区域 */
.action-section {
  text-align: center;
}

.generate-button {
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

.generate-button:not(:disabled):hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.4);
}

.generate-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.button-content {
  display: flex;
  align-items: center;
  gap: 12px;
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

.action-tip {
  margin-top: 16px;
  color: #86868b;
  font-size: 14px;
}

/* Element Plus 样式覆盖 */
:deep(.el-upload-dragger) {
  background: rgba(248, 250, 252, 0.8);
  border: 2px dashed rgba(102, 126, 234, 0.3);
  border-radius: 24px;
  transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

:deep(.el-select) {
  --el-select-border-color-hover: #667eea;
  --el-select-input-focus-border-color: #667eea;
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-select-dropdown) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 12px;
}

:deep(.el-progress-bar__outer) {
  background-color: rgba(102, 126, 234, 0.2);
  border-radius: 8px;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
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

  .generator-card {
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

  .section-desc {
    font-size: 16px;
  }

  .params-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .param-card {
    padding: 24px;
  }

  .generate-button {
    padding: 16px 32px;
    font-size: 16px;
    min-width: 200px;
  }

  .upload-content {
    padding: 40px 24px;
  }

  .upload-icon {
    width: 60px;
    height: 60px;
    font-size: 24px;
  }

  .upload-main-text {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 80px 24px 40px;
  }

  .generator-section {
    padding: 0 16px 80px;
  }

  .generator-card {
    padding: 24px 16px;
  }

  .section-header {
    margin-bottom: 32px;
  }

  .upload-section,
  .params-section {
    margin-bottom: 48px;
  }
}
</style>
