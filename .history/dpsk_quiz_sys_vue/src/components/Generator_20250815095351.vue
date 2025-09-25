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
          <a href="#" class="nav-link">主页</a>
          <a href="#" class="nav-link">题目生成</a>
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
            智能
            <span class="hero-highlight">题目生成</span>
          </h1>
          <p class="hero-subtitle">上传学习资料，AI为您量身定制专属题目</p>
        </div>
      </section>

      <!-- 生成器卡片 -->
      <section class="generator-section">
        <div class="generator-card">
          <!-- 生成方式选择 -->
          <div class="generation-mode-section">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Setting /></el-icon>
              </div>
              <h2 class="section-title">选择生成方式</h2>
              <p class="section-desc">选择适合您的题目生成方式</p>
            </div>

            <div class="mode-tabs">
              <div
                v-for="mode in generationModes"
                :key="mode.value"
                class="mode-tab"
                :class="{ active: selectedMode === mode.value }"
                @click="selectMode(mode.value)"
              >
                <div class="mode-icon">
                  <el-icon><component :is="mode.icon" /></el-icon>
                </div>
                <div class="mode-content">
                  <h3 class="mode-title">{{ mode.title }}</h3>
                  <p class="mode-desc">{{ mode.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 上传区域 (仅在文件上传模式显示) -->
          <div v-if="selectedMode === 'file'" class="upload-section">
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

          <!-- 知识库选择区域 (仅在知识库模式显示) -->
          <div v-if="selectedMode === 'knowledge'" class="knowledge-section">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><Collection /></el-icon>
              </div>
              <h2 class="section-title">选择知识库内容</h2>
              <p class="section-desc">从已有知识库中选择文档和章节</p>
            </div>

            <div class="knowledge-selection">
              <div class="knowledge-documents">
                <h3 class="selection-title">选择文档</h3>
                <el-checkbox-group
                  v-model="selectedDocuments"
                  class="document-list"
                >
                  <el-checkbox
                    v-for="doc in knowledgeDocuments"
                    :key="doc.documentId"
                    :label="doc.documentId"
                    class="document-item"
                    @change="onDocumentChange"
                  >
                    <div class="document-info">
                      <div class="document-name">{{ doc.documentName }}</div>
                      <div class="document-meta">
                        <span class="file-size">{{
                          formatFileSize(doc.fileSize)
                        }}</span>
                        <span class="upload-time">{{
                          formatDate(doc.uploadTime)
                        }}</span>
                      </div>
                    </div>
                  </el-checkbox>
                </el-checkbox-group>
              </div>

              <div
                v-if="availableChapters.length > 0"
                class="knowledge-chapters"
              >
                <h3 class="selection-title">选择章节</h3>
                <el-checkbox-group
                  v-model="selectedChapters"
                  class="chapter-list"
                >
                  <el-checkbox
                    v-for="chapter in availableChapters"
                    :key="chapter"
                    :label="chapter"
                    class="chapter-item"
                  >
                    {{ chapter }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </div>

          <!-- AI生成区域 (仅在AI模式显示) -->
          <div v-if="selectedMode === 'ai'" class="ai-section">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><MagicStick /></el-icon>
              </div>
              <h2 class="section-title">AI智能生成</h2>
              <p class="section-desc">描述您想要的题目主题和要求</p>
            </div>

            <div class="ai-inputs">
              <div class="input-group">
                <label class="input-label">题目主题</label>
                <el-input
                  v-model="aiTopic"
                  placeholder="请输入题目主题，如：贷后催收法律法规、催收话术技巧等"
                  class="topic-input"
                />
              </div>

              <div class="input-group">
                <label class="input-label">具体要求</label>
                <el-input
                  v-model="aiRequirements"
                  type="textarea"
                  :rows="4"
                  placeholder="请描述具体要求，如：重点考察法律条文理解、实际应用场景等"
                  class="requirements-input"
                />
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
              <div class="param-card full-width">
                <label class="param-label">题目类型配置</label>
                <div class="question-type-config">
                  <div
                    v-for="type in questionTypes"
                    :key="type.value"
                    class="type-config-item"
                  >
                    <div class="type-header">
                      <el-checkbox
                        v-model="type.enabled"
                        :label="type.label"
                        @change="updateQuestionTypeConfig"
                      />
                    </div>
                    <div v-if="type.enabled" class="type-count">
                      <el-input-number
                        v-model="type.count"
                        :min="1"
                        :max="50"
                        placeholder="数量"
                        size="small"
                        @change="updateQuestionTypeConfig"
                      />
                      <span class="count-label">道题</span>
                    </div>
                  </div>
                </div>
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

              <div class="param-card">
                <label class="param-label">考试时长</label>
                <div class="duration-input-group">
                  <el-input-number
                    v-model="examDuration"
                    :min="10"
                    :max="300"
                    :step="5"
                    placeholder="时长"
                    class="duration-input"
                  />
                  <span class="duration-unit">分钟</span>
                </div>
                <div class="duration-presets">
                  <button
                    v-for="preset in [30, 60, 80, 120, 180]"
                    :key="preset"
                    @click="examDuration = preset"
                    class="preset-btn"
                    :class="{ active: examDuration === preset }"
                  >
                    {{ preset }}分钟
                  </button>
                </div>
              </div>

              <div class="param-card">
                <label class="param-label">试卷名称</label>
                <el-input
                  v-model="paperName"
                  placeholder="请输入试卷名称，便于日后查找"
                  class="paper-name-input"
                  maxlength="50"
                  show-word-limit
                  clearable
                />
                <div class="param-tip">
                  <el-icon><Document /></el-icon>
                  <span>试卷名称将用于存储和管理，建议使用有意义的名称</span>
                </div>
              </div>

              <div class="param-card">
                <label class="param-label">试卷过期时间</label>
                <el-date-picker
                  v-model="examExpireTime"
                  type="datetime"
                  placeholder="选择试卷过期时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :disabled-date="disabledDate"
                  :disabled-hours="disabledHours"
                  :disabled-minutes="disabledMinutes"
                  class="expire-time-picker"
                  clearable
                />
                <div class="expire-time-presets">
                  <button
                    v-for="preset in expireTimePresets"
                    :key="preset.label"
                    @click="setExpireTimePreset(preset.days)"
                    class="preset-btn"
                    :class="{ active: isExpireTimePresetActive(preset.days) }"
                  >
                    {{ preset.label }}
                  </button>
                </div>
                <div class="debug-section">
                  <button
                    @click="showExpireTimeDebug"
                    class="debug-btn"
                    type="button"
                  >
                    <el-icon><View /></el-icon>
                    查看过期时间
                  </button>
                </div>
                <div class="param-tip">
                  <el-icon><Setting /></el-icon>
                  <span>设置试卷的有效期，过期后将无法参与考试</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 生成按钮 -->
          <div class="action-section">
            <button
              class="generate-button"
              :disabled="
                !canGenerate || questionCount === 0 || !questionDifficulty
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
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElLoading } from "element-plus";
import { useUserStore } from "../stores/user";
import {
  UploadFilled,
  UserFilled,
  CaretBottom,
  Setting,
  Star,
  Collection,
  MagicStick,
  Document,
  FolderOpened,
  View,
} from "@element-plus/icons-vue";

const router = useRouter();

// 文件上传配置
const maxSizeMB = 10;
const supportedFormats = ".pdf,.doc,.docx,.txt,.ppt,.png,.pptx";
const supportedFormatsText = "PDF、Word、PPT、TXT";

const selectedFile = ref(null);
const generating = ref(false);
const uploadProgress = ref(0);

// 生成方式
const selectedMode = ref("file");
const generationModes = ref([
  {
    value: "file",
    title: "文件上传生成",
    description: "上传文档，基于文档内容生成题目",
    icon: "UploadFilled",
  },
  {
    value: "knowledge",
    title: "知识库生成",
    description: "从已有知识库中选择内容生成题目",
    icon: "Collection",
  },
  {
    value: "ai",
    title: "AI直接生成",
    description: "通过AI大模型直接生成指定主题的题目",
    icon: "MagicStick",
  },
]);

// 知识库相关
const knowledgeDocuments = ref([]);
const selectedDocuments = ref([]);

// 生成的题目
const generatedQuestions = ref([]);
const selectedChapters = ref([]);
const availableChapters = ref([]);

// AI生成相关
const aiTopic = ref("");
const aiRequirements = ref("");

// 计算属性：是否可以生成
const canGenerate = computed(() => {
  switch (selectedMode.value) {
    case "file":
      return selectedFile.value && uploadProgress.value >= 100;
    case "knowledge":
      return selectedDocuments.value.length > 0;
    case "ai":
      return aiTopic.value.trim() !== "";
    default:
      return false;
  }
});
const uploadStatus = ref("");
const uploadStatusText = ref("");

// 题目参数
const questionType = ref("");
const questionCount = ref("");
const questionDifficulty = ref("");
const examDuration = ref(80); // 考试时长，默认80分钟
const paperName = ref(""); // 试卷名称
const examExpireTime = ref(""); // 试卷过期时间

const questionTypes = ref([
  { value: "SINGLE_CHOICE", label: "单选题", enabled: false, count: 5 },
  { value: "MULTI_CHOICE", label: "多选题", enabled: false, count: 3 },
  { value: "FILL_BLANK", label: "填空题", enabled: false, count: 3 },
  { value: "SHORT_ANSWER", label: "简答题", enabled: false, count: 2 },
]);

// 更新题目类型配置
const updateQuestionTypeConfig = () => {
  const enabledTypes = questionTypes.value.filter((type) => type.enabled);
  if (enabledTypes.length > 0) {
    questionCount.value = enabledTypes.reduce(
      (sum, type) => sum + type.count,
      0
    );
  } else {
    questionCount.value = 0;
  }
};

const questionCounts = [2, 5, 10, 15, 20, 25, 30];

const questionDifficulties = [
  { value: "EASY", label: "简单" },
  { value: "MEDIUM", label: "中等" },
  { value: "HARD", label: "困难" },
];

// 过期时间预设选项
const expireTimePresets = [
  { label: "7天", days: 7 },
  { label: "30天", days: 30 },
  { label: "90天", days: 90 },
  { label: "180天", days: 180 },
  { label: "1年", days: 365 },
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

  // 文件格式检查
  if (!supportedExtensions.includes(extension)) {
    ElMessage.error({
      message: `不支持的文件格式: ${extension}，请上传 ${supportedFormatsText} 格式的文件`,
      duration: 5000,
    });
    return false;
  }

  // 文件大小检查
  const isLtSize = file.size / 1024 / 1024 <= maxSizeMB;
  if (!isLtSize) {
    ElMessage.error({
      message: `文件大小不能超过 ${maxSizeMB}MB，当前文件大小: ${(
        file.size /
        1024 /
        1024
      ).toFixed(2)}MB`,
      duration: 5000,
    });
    return false;
  }

  // 文件类型提示
  if (extension === "doc" || extension === "docx") {
    ElMessage.info({
      message: "Word文档将进行文本解析处理",
      duration: 3000,
    });
  } else if (extension === "txt") {
    ElMessage.info({
      message: "TXT文件将直接读取文本内容",
      duration: 3000,
    });
  } else {
    ElMessage.info({
      message: "图片/PDF文件将进行OCR识别处理",
      duration: 3000,
    });
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

// 选择生成模式
const selectMode = (mode) => {
  selectedMode.value = mode;
  // 重置相关状态
  if (mode === "knowledge" && knowledgeDocuments.value.length === 0) {
    loadKnowledgeDocuments();
  }
};

// 加载知识库文档
const loadKnowledgeDocuments = async () => {
  try {
    // 获取token
    const token = getCookie("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    const response = await fetch("/api/kb/documents?page=1&size=100", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    const result = await response.json();

    if (response.ok && result.code === 200) {
      knowledgeDocuments.value = result.data.content || [];
    } else {
      ElMessage.error("加载知识库文档失败：" + (result.message || "未知错误"));
    }
  } catch (error) {
    ElMessage.error("加载知识库文档失败：" + error.message);
  }
};

// 获取Cookie中的token
const getCookie = (name) => {
  const cookieArray = document.cookie.split("; ");
  const cookie = cookieArray.find((row) => row.startsWith(name + "="));
  return cookie ? cookie.split("=")[1] : null;
};

// 上传文件到知识库数据库
const uploadFileToKnowledgeBase = async (file) => {
  console.log("🚀 开始上传文件到知识库...");
  console.log("📁 文件信息:", {
    name: file.name,
    size: file.size,
    type: file.type,
    lastModified: new Date(file.lastModified).toLocaleString(),
  });

  // 检查网络连接
  if (!navigator.onLine) {
    console.error("❌ 网络连接检查失败: 当前离线状态");
    throw new Error("网络连接失败，请检查网络连接后重试");
  }
  console.log("✅ 网络连接检查通过");

  const token = getCookie("token");
  console.log("🔑 Token检查:", token ? "已获取到token" : "未获取到token");
  if (!token) {
    console.error("❌ Token验证失败");
    throw new Error("请先登录");
  }

  // 获取用户ID
  const userStore = useUserStore();
  const userId = userStore.id;
  console.log("👤 用户信息:", {
    userId: userId,
    username: userStore.username,
    email: userStore.email,
  });

  if (!userId) {
    console.error("❌ 用户ID获取失败");
    throw new Error("无法获取用户信息，请重新登录");
  }

  // 构建FormData
  const formData = new FormData();
  formData.append("file", file);
  formData.append("documentName", file.name);
  formData.append("uploadUserId", userId.toString());
  formData.append("description", `通过试卷生成器上传的文件: ${file.name}`);

  console.log("📦 FormData内容:");
  for (let [key, value] of formData.entries()) {
    if (key === "file") {
      console.log(`  ${key}:`, {
        name: value.name,
        size: value.size,
        type: value.type,
      });
    } else {
      console.log(`  ${key}:`, value);
    }
  }

  console.log("🌐 发送请求到: /api/kb/documents/upload");
  console.log("📋 请求配置:", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token.substring(0, 20)}...`,
    },
    credentials: "include",
  });

  try {
    const response = await fetch("/api/kb/documents/upload", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
      credentials: "include",
    });

    console.log("📡 收到响应:", {
      status: response.status,
      statusText: response.statusText,
      ok: response.ok,
      headers: Object.fromEntries(response.headers.entries()),
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ HTTP响应错误:", {
        status: response.status,
        statusText: response.statusText,
        errorText: errorText,
      });

      let errorMessage = `文件上传失败 (HTTP ${response.status})`;
      if (response.status === 401) {
        errorMessage = "认证失败，请重新登录";
      } else if (response.status === 403) {
        errorMessage = "权限不足，无法上传文件";
      } else if (response.status === 413) {
        errorMessage = "文件过大，请选择较小的文件";
      } else if (response.status === 415) {
        errorMessage = "不支持的文件格式";
      } else if (response.status >= 500) {
        errorMessage = "服务器内部错误，请稍后重试";
      } else if (errorText) {
        errorMessage += `: ${errorText}`;
      }

      throw new Error(errorMessage);
    }

    const result = await response.json();
    console.log("📄 响应数据:", result);

    if (result.code !== 200) {
      console.error("❌ 业务逻辑错误:", {
        code: result.code,
        message: result.message,
        data: result.data,
      });
      throw new Error(result.message || "文件上传失败");
    }

    console.log("✅ 文件上传成功:", result.data);

    // 自动触发文档处理
    try {
      console.log("🔄 开始处理文档:", result.data.documentId);
      await processDocument(result.data.documentId);
    } catch (processError) {
      console.warn("文档处理失败:", processError);
      ElMessage.warning("文件上传成功，但处理可能需要一些时间");
    }

    return result.data;
  } catch (error) {
    console.error("❌ 上传过程中发生错误:", {
      name: error.name,
      message: error.message,
      stack: error.stack,
    });

    // 重新抛出错误，保持原有的错误处理流程
    if (error.name === "TypeError" && error.message.includes("fetch")) {
      throw new Error("网络连接失败，请检查网络连接后重试");
    }
    throw error;
  }
};

// 处理文档函数
const processDocument = async (documentId) => {
  try {
    const token = getCookie("token");
    const response = await fetch(`/api/kb/documents/${documentId}/process`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    if (response.ok) {
      console.log("✅ 文档处理完成:", documentId);
      ElMessage.success("文档处理完成");
    } else {
      console.warn(
        "⚠️ 文档处理响应异常:",
        response.status,
        response.statusText
      );
      ElMessage.warning("文档上传成功，但处理可能需要一些时间");
    }
  } catch (error) {
    console.error("❌ 处理文档失败:", error);
    ElMessage.warning("文档上传成功，但处理可能需要一些时间");
  }
};

// 过期时间相关方法
const setExpireTimePreset = (days) => {
  const now = new Date();
  const expireDate = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);
  examExpireTime.value = expireDate
    .toISOString()
    .slice(0, 19)
    .replace("T", " ");
};

// 检查是否为当前选中的预设时间
const isExpireTimePresetActive = (days) => {
  if (!examExpireTime.value) return false;
  const selectedTime = new Date(examExpireTime.value);
  const now = new Date();
  const presetTime = new Date(now.getTime() + days * 24 * 60 * 60 * 1000);
  // 允许1小时的误差
  const timeDiff = Math.abs(selectedTime.getTime() - presetTime.getTime());
  return timeDiff < 60 * 60 * 1000;
};

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

// 禁用过去的小时
const disabledHours = () => {
  const now = new Date();
  const selectedDate = examExpireTime.value
    ? new Date(examExpireTime.value)
    : null;
  if (!selectedDate) return [];

  // 如果是今天，禁用当前小时之前的小时
  if (selectedDate.toDateString() === now.toDateString()) {
    const hours = [];
    for (let i = 0; i < now.getHours(); i++) {
      hours.push(i);
    }
    return hours;
  }
  return [];
};

// 禁用过去的分钟
const disabledMinutes = (hour) => {
  const now = new Date();
  const selectedDate = examExpireTime.value
    ? new Date(examExpireTime.value)
    : null;
  if (!selectedDate) return [];

  // 如果是今天且是当前小时，禁用当前分钟之前的分钟
  if (
    selectedDate.toDateString() === now.toDateString() &&
    hour === now.getHours()
  ) {
    const minutes = [];
    for (let i = 0; i <= now.getMinutes(); i++) {
      minutes.push(i);
    }
    return minutes;
  }
  return [];
};

// 调试功能：显示当前设置的过期时间
const showExpireTimeDebug = () => {
  const currentTime = new Date().toLocaleString("zh-CN");
  let expireTimeInfo = "";

  if (examExpireTime.value) {
    const expireDate = new Date(examExpireTime.value);
    expireTimeInfo = `设置的过期时间: ${expireDate.toLocaleString("zh-CN")}\n`;
    expireTimeInfo += `ISO格式: ${expireDate.toISOString()}\n`;
    expireTimeInfo += `时间戳: ${expireDate.getTime()}\n`;

    const timeDiff = expireDate.getTime() - Date.now();
    const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));
    const hoursDiff = Math.floor(
      (timeDiff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    );
    expireTimeInfo += `距离过期: ${daysDiff}天${hoursDiff}小时`;
  } else {
    expireTimeInfo = "未设置过期时间\n默认将使用: 当前时间 + 30天";
  }

  ElMessage.info({
    message: `当前时间: ${currentTime}\n${expireTimeInfo}`,
    duration: 8000,
    showClose: true,
  });

  // 同时在控制台输出详细信息
  console.log("=== 过期时间调试信息 ===");
  console.log("当前时间:", currentTime);
  console.log("examExpireTime.value:", examExpireTime.value);
  if (examExpireTime.value) {
    console.log("过期时间对象:", new Date(examExpireTime.value));
    console.log("ISO格式:", new Date(examExpireTime.value).toISOString());
  }
  console.log("========================");
};

// 文档选择变化时加载章节
const onDocumentChange = async () => {
  if (selectedDocuments.value.length === 0) {
    availableChapters.value = [];
    return;
  }

  try {
    const token = getCookie("token");
    if (!token) {
      ElMessage.error("请先登录");
      return;
    }

    const chapterSets = new Set();

    for (const docId of selectedDocuments.value) {
      const response = await fetch(`/api/kb/chunks/document/${docId}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        credentials: "include",
      });

      const result = await response.json();

      if (response.ok && result.code === 200) {
        result.data.forEach((chunk) => {
          if (chunk.chapterName) {
            chapterSets.add(chunk.chapterName);
          }
        });
      }
    }

    availableChapters.value = Array.from(chapterSets).sort();
  } catch (error) {
    ElMessage.error("加载章节失败：" + error.message);
  }
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
};

// 格式化日期
const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString("zh-CN");
};

// 保存试卷到数据库
const saveExamPaper = async (questions) => {
  try {
    // 使用用户输入的试卷名称，如果为空则使用自动生成的标题
    const finalPaperName = paperName.value.trim() || generateExamTitle();

    // 计算过期时间
    const finalExpireTime = examExpireTime.value
      ? new Date(examExpireTime.value).toISOString()
      : new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString();

    // 调试输出：显示发送到后端的过期时间数据
    console.log("=== 前端发送数据调试 ===");
    console.log(
      "用户设置的过期时间 (examExpireTime.value):",
      examExpireTime.value
    );
    console.log("最终发送的过期时间 (finalExpireTime):", finalExpireTime);
    console.log("过期时间对象:", new Date(finalExpireTime));
    console.log("=========================");

    const examPaperData = {
      paperName: finalPaperName,
      difficulty: questionDifficulty.value.toLowerCase(), // 转换为小写，匹配后端枚举
      totalQuestion: questions.length,
      totalScore: questions.length * 5, // 默认每题5分
      expireTime: finalExpireTime, // 使用用户设置的过期时间，默认30天

      // 根据生成模式添加特定字段
      generationType: selectedMode.value,
      title: finalPaperName,
      description: generateExamDescription(),
      timeLimit: examDuration.value || 60, // 使用设置的考试时长
      chapterScope:
        selectedMode.value === "knowledge" ? selectedChapters.value : [],

      ...(selectedMode.value === "knowledge" && {
        documentIds: selectedDocuments.value,
        chapters: selectedChapters.value,
      }),

      ...(selectedMode.value === "ai" && {
        topic: aiTopic.value,
        requirements: aiRequirements.value,
      }),

      questions: questions.map((q) => ({
        questionText: q.question,
        questionType: q.type || questionType.value.toUpperCase(),
        options: q.options || [],
        correctAnswer: q.answer,
        explanation: q.explanation || "",
        score: 5, // 默认每题5分
      })),
    };

    const token = getCookie("token");
    if (!token) {
      ElMessage.error("请先登录");
      return null;
    }

    const response = await fetch("/api/exam/papers/create", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      credentials: "include",
      body: JSON.stringify(examPaperData),
    });

    const result = await response.json();

    if (response.ok && result.code === 200) {
      // 调试输出：保存成功后的信息
      console.log("=== 试卷保存成功调试 ===");
      console.log("保存成功，返回的试卷数据:", result.data);
      if (result.data && result.data.expireTime) {
        console.log("后端返回的过期时间:", result.data.expireTime);
        console.log("过期时间对象:", new Date(result.data.expireTime));
      }
      console.log("=========================");

      ElMessage.success("试卷已保存到数据库");
      // 统一字段名：将paperId映射为id
      return {
        id: result.data.paperId,
        paperName: result.data.paperName,
        ...result.data,
      };
    } else {
      console.error("保存试卷失败:", result);
      console.error("  - HTTP状态:", response.status);
      console.error("  - 响应码:", result.code);
      console.error("  - 错误信息:", result.message);

      // 根据错误类型提供不同的用户提示
      let userMessage = "试卷保存失败";
      if (result.message) {
        if (result.message.includes("数据库")) {
          userMessage = "数据库连接异常，试卷将使用临时ID";
        } else if (result.message.includes("权限")) {
          userMessage = "权限不足，请重新登录后再试";
        } else {
          userMessage = `保存失败: ${result.message}`;
        }
      }

      ElMessage.warning(userMessage);

      // 返回一个临时ID，确保不会导致后续流程中断
      const tempId = "temp_" + Date.now();
      console.log("🔧 使用临时试卷ID:", tempId);
      console.log("🔧 临时试卷数据:", {
        id: tempId,
        paperName: examPaperData.paperName,
      });
      return { id: tempId, paperName: examPaperData.paperName };
    }
  } catch (error) {
    console.error("保存试卷异常:", error);
    console.error("  - 错误类型:", error.name);
    console.error("  - 错误信息:", error.message);
    console.error("  - 错误堆栈:", error.stack);

    // 根据错误类型提供不同的用户提示
    let userMessage = "试卷保存异常，但不影响答题";
    if (error.message) {
      if (error.message.includes("网络")) {
        userMessage = "网络连接异常，试卷将使用临时ID";
      } else if (error.message.includes("timeout")) {
        userMessage = "请求超时，试卷将使用临时ID";
      } else if (error.message.includes("fetch")) {
        userMessage = "服务器连接失败，试卷将使用临时ID";
      }
    }

    ElMessage.warning(userMessage);

    // 返回一个临时ID，确保不会导致后续流程中断
    const tempId = "temp_" + Date.now();
    console.log("🔧 使用临时试卷ID:", tempId);
    console.log("🔧 临时试卷数据:", {
      id: tempId,
      paperName: examPaperData.paperName,
    });
    return { id: tempId, paperName: examPaperData.paperName };
  }
};

// 生成试卷标题
const generateExamTitle = () => {
  const timestamp = new Date().toLocaleString("zh-CN");
  let title = "";

  if (selectedMode.value === "file") {
    title = `基于文件生成的${questionType.value}试卷`;
  } else if (selectedMode.value === "knowledge") {
    const docNames = knowledgeDocuments.value
      .filter((doc) => selectedDocuments.value.includes(doc.documentId))
      .map((doc) => doc.documentName)
      .slice(0, 2)
      .join("、");
    title = `基于知识库(${docNames})的${questionType.value}试卷`;
  } else if (selectedMode.value === "ai") {
    title = `AI生成的${aiTopic.value}${questionType.value}试卷`;
  }

  return `${title} - ${timestamp}`;
};

// 生成试卷描述
const generateExamDescription = () => {
  let description = `题目类型：${questionType.value}，难度：${questionDifficulty.value}，题目数量：${questionCount.value}道。`;

  if (selectedMode.value === "knowledge" && selectedChapters.value.length > 0) {
    description += ` 涉及章节：${selectedChapters.value.join("、")}。`;
  } else if (selectedMode.value === "ai" && aiRequirements.value.trim()) {
    description += ` 特殊要求：${aiRequirements.value.trim()}`;
  }

  return description;
};

// 页面加载时初始化
onMounted(() => {
  loadKnowledgeDocuments();
  // 设置默认过期时间为30天后
  setExpireTimePreset(30);
});

// 处理流式响应的通用函数
const processStreamResponse = async (response, loadingInstance = null) => {
  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  const questions = [];
  let buffer = "";

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
              console.log(`收到第${questions.length}道题目:`, parsed.question);

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

  return questions;
};

const generateQuestions = async () => {
  // 验证基本参数
  const enabledTypes = questionTypes.value.filter((type) => type.enabled);
  if (enabledTypes.length === 0 || !questionDifficulty.value) {
    ElMessage.warning("请完善题目设置");
    return;
  }

  // 根据模式验证特定参数
  if (selectedMode.value === "file" && !selectedFile.value) {
    ElMessage.warning("请先上传文件");
    return;
  }

  if (selectedMode.value === "knowledge") {
    if (selectedDocuments.value.length === 0) {
      ElMessage.warning("请选择知识库文档");
      return;
    }

    // 验证选中的文档是否有效
    const invalidDocuments = selectedDocuments.value.filter((docId) => {
      const doc = knowledgeDocuments.value.find((d) => d.documentId === docId);
      return !doc || doc.processStatus !== "success";
    });

    if (invalidDocuments.length > 0) {
      ElMessage.warning("选中的文档中包含未处理完成的文档，请重新选择");
      return;
    }

    console.log("📋 知识库模式验证通过:");
    console.log("  - 选中文档数量:", selectedDocuments.value.length);
    console.log("  - 选中文档ID:", selectedDocuments.value);
    console.log("  - 选中章节数量:", selectedChapters.value.length);
    console.log("  - 选中章节:", selectedChapters.value);
  }

  if (selectedMode.value === "ai" && !aiTopic.value.trim()) {
    ElMessage.warning("请输入题目主题");
    return;
  }

  generating.value = true;
  const loading = ElLoading.service({
    lock: true,
    text: "正在生成题目...",
    background: "rgba(0, 0, 0, 0.7)",
  });

  try {
    let response;

    if (selectedMode.value === "file") {
      // 文件上传模式 - 支持多种题型
      const allQuestions = [];

      // 判断文件类型
      const fileName = selectedFile.value.name.toLowerCase();
      const isImageFile =
        fileName.endsWith(".png") ||
        fileName.endsWith(".jpg") ||
        fileName.endsWith(".jpeg");
      const isDocumentFile =
        fileName.endsWith(".txt") ||
        fileName.endsWith(".doc") ||
        fileName.endsWith(".docx") ||
        fileName.endsWith(".pdf");

      // 先将文件上传到知识库数据库
      try {
        loading.text = "正在上传文件到知识库...";
        await uploadFileToKnowledgeBase(selectedFile.value);
        ElMessage.success("文件已成功上传到知识库");
      } catch (uploadError) {
        console.warn("文件上传到知识库失败:", uploadError);
        ElMessage.warning("文件上传到知识库失败，但将继续生成题目");
      }

      loading.text = "正在生成题目...";

      for (const type of enabledTypes) {
        const formData = new FormData();

        // 根据文件类型选择参数名和接口
        if (isImageFile) {
          formData.append("image", selectedFile.value);
        } else if (isDocumentFile) {
          formData.append("document", selectedFile.value);
        } else {
          ElMessage.error("不支持的文件格式");
          return;
        }

        formData.append("questionType", type.value.toUpperCase());
        formData.append("questionCount", type.count);
        formData.append("difficulty", questionDifficulty.value.toUpperCase());

        const token = getCookie("token");
        if (!token) {
          ElMessage.error("请先登录");
          return;
        }

        // 根据文件类型选择API接口
        const apiEndpoint = isImageFile
          ? "/api/question/generate"
          : "/api/question/generate-from-document";

        const typeResponse = await fetch(apiEndpoint, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        });

        if (!typeResponse.ok) {
          const errorText = await typeResponse.text();
          console.error("❌ 知识库题目生成请求失败:");
          console.error("  - HTTP状态码:", typeResponse.status);
          console.error("  - 状态文本:", typeResponse.statusText);
          console.error("  - 错误响应:", errorText);
          console.error(
            "  - 请求URL:",
            "/api/question/generate-from-knowledge"
          );
          console.error("  - 请求参数:", JSON.stringify(requestBody, null, 2));
          throw new Error(`生成${type.label}失败: ${errorText}`);
        }

        console.log("✅ 知识库题目生成请求成功，开始处理流式响应...");

        // 处理该题型的流式响应
        const typeQuestions = await processStreamResponse(
          typeResponse,
          loading
        );
        allQuestions.push(...typeQuestions);
      }

      // 合并所有题目
      generatedQuestions.value = allQuestions;
    } else if (selectedMode.value === "knowledge") {
      // 知识库模式 - 支持多种题型
      const allQuestions = [];

      for (const type of enabledTypes) {
        const requestBody = {
          documentIds: selectedDocuments.value,
          chapters:
            selectedChapters.value.length > 0 ? selectedChapters.value : null,
          questionType: type.value.toUpperCase(),
          questionCount: parseInt(type.count),
          difficulty: questionDifficulty.value.toUpperCase(),
        };

        // 添加详细的请求参数日志
        console.log("🚀 发送知识库题目生成请求:");
        console.log(
          "  - 题型:",
          type.label,
          "(" + type.value.toUpperCase() + ")"
        );
        console.log("  - 题目数量:", type.count);
        console.log("  - 难度:", questionDifficulty.value.toUpperCase());
        console.log("  - 文档ID列表:", requestBody.documentIds);
        console.log("  - 章节列表:", requestBody.chapters);
        console.log("  - 完整请求体:", JSON.stringify(requestBody, null, 2));

        const token = getCookie("token");
        if (!token) {
          ElMessage.error("请先登录");
          return;
        }

        const typeResponse = await fetch(
          "/api/question/generate-from-knowledge",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(requestBody),
          }
        );

        if (!typeResponse.ok) {
          const errorText = await typeResponse.text();
          throw new Error(`生成${type.label}失败: ${errorText}`);
        }

        // 处理该题型的流式响应
        const typeQuestions = await processStreamResponse(
          typeResponse,
          loading
        );
        allQuestions.push(...typeQuestions);
      }

      // 合并所有题目
      generatedQuestions.value = allQuestions;
    } else if (selectedMode.value === "ai") {
      // AI直接生成模式 - 支持多种题型
      const allQuestions = [];

      for (const type of enabledTypes) {
        const requestBody = {
          topic: aiTopic.value.trim(),
          requirements: aiRequirements.value.trim(),
          questionType: type.value.toUpperCase(),
          questionCount: parseInt(type.count),
          difficulty: questionDifficulty.value.toUpperCase(),
        };

        const token = getCookie("token");
        if (!token) {
          ElMessage.error("请先登录");
          return;
        }

        const typeResponse = await fetch("/api/question/generate-ai", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(requestBody),
        });

        if (!typeResponse.ok) {
          const errorText = await typeResponse.text();
          throw new Error(`生成${type.label}失败: ${errorText}`);
        }

        // 处理该题型的流式响应
        const typeQuestions = await processStreamResponse(typeResponse);
        allQuestions.push(...typeQuestions);
      }

      // 合并所有题目
      generatedQuestions.value = allQuestions;
    }

    // 多题型模式已在上面处理完成，无需额外处理

    // 保存试卷到数据库
    let examPaper = await saveExamPaper(generatedQuestions.value);

    // 验证试卷ID的有效性并提供备用方案
    console.log("🔍 试卷保存结果验证:");
    console.log("  - examPaper对象:", examPaper);
    console.log("  - examPaper.id:", examPaper?.id);
    console.log("  - examPaper.id类型:", typeof examPaper?.id);

    // 检查试卷ID是否有效（包括临时ID）
    const isValidId =
      examPaper &&
      examPaper.id &&
      (typeof examPaper.id === "string" || typeof examPaper.id === "number") &&
      examPaper.id.toString().trim() !== "";

    // 如果试卷ID无效，生成备用临时ID
    if (!isValidId) {
      console.error("❌ 试卷ID获取失败，生成备用临时ID");
      const backupId =
        "temp_backup_" +
        Date.now() +
        "_" +
        Math.random().toString(36).substr(2, 9);
      examPaper = {
        id: backupId,
        paperName: generateExamTitle() || "未命名试卷",
        isBackup: true,
      };
      console.log("🔧 生成备用试卷ID:", backupId);
      ElMessage.warning({
        message:
          "试卷保存异常，使用临时ID继续，答题功能正常但记录可能无法保存到数据库",
        duration: 6000,
        showClose: true,
      });
    } else {
      console.log("✅ 试卷ID验证通过:", examPaper.id);
      // 如果是临时ID，给用户一个提示
      if (examPaper.id.toString().startsWith("temp_")) {
        console.log("ℹ️ 使用临时试卷ID，答题记录可能无法正常保存到数据库");
        ElMessage.info({
          message: "试卷已生成，但保存到数据库时出现问题，答题功能正常",
          duration: 4000,
        });
      }
    }

    // 确保examPaperId始终有有效值
    const finalExamPaperId = examPaper?.id || "temp_fallback_" + Date.now();

    const dataToPass = {
      questions: generatedQuestions.value,
      type: questionType.value,
      difficulty: questionDifficulty.value,
      total: generatedQuestions.value.length,
      examPaperId: finalExamPaperId, // 确保始终有有效的试卷ID
      examDuration: examDuration.value, // 添加考试时长
      paperName: examPaper?.paperName || "未命名试卷",
      isBackupId: examPaper?.isBackup || false, // 标记是否为备用ID
    };

    console.log("📦 准备传递给 Exercise 页面的完整数据:", dataToPass);

    const jsonString = JSON.stringify(dataToPass);
    console.log("📦 JSON 字符串长度:", jsonString.length);

    console.log("🚀 开始跳转到 PaperPreview 页面...");
    const storageKey = "temp_questions_" + Date.now();
    sessionStorage.setItem(storageKey, jsonString);
    console.log("💾 数据已存储到 sessionStorage，key:", storageKey);

    router.push({
      name: "PaperPreview",
      query: {
        dataKey: storageKey,
      },
    });
    console.log("✅ 跳转命令已执行");
  } catch (error) {
    console.error("生成失败：", error);

    let errorMessage = "生成失败";
    if (error.message) {
      if (error.message.includes("OCR")) {
        errorMessage = "OCR识别失败，请确保图片清晰或尝试其他格式文件";
      } else if (error.message.includes("Word")) {
        errorMessage = "Word文档解析失败，请检查文档格式是否正确";
      } else if (error.message.includes("token")) {
        errorMessage = "认证失败，请重新登录";
      } else if (error.message.includes("网络")) {
        errorMessage = "网络连接失败，请检查网络连接";
      } else {
        errorMessage = error.message;
      }
    }

    ElMessage.error({
      message: errorMessage,
      duration: 5000,
      showClose: true,
    });
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

/* 知识库选择区域样式 */
.knowledge-section {
  margin-bottom: 80px;
}

.knowledge-selection {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.knowledge-documents,
.knowledge-chapters {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
  background: rgba(248, 250, 252, 0.5);
}

.selection-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px 0;
}

.document-list,
.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.document-item {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  background: white;
}

.document-item:hover {
  border-color: #667eea;
  background-color: rgba(102, 126, 234, 0.05);
}

.document-info {
  margin-left: 8px;
}

.document-name {
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.document-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #86868b;
}

.chapter-item {
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  transition: all 0.3s ease;
  background: white;
}

.chapter-item:hover {
  border-color: #667eea;
  background-color: rgba(102, 126, 234, 0.05);
}

/* AI生成区域样式 */
.ai-section {
  margin-bottom: 80px;
}

.ai-inputs {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
}

.topic-input,
.requirements-input {
  border-radius: 12px;
}

/* 生成方式选择样式 */
.generation-mode-section {
  margin-bottom: 80px;
}

.mode-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.mode-tab {
  background: rgba(248, 250, 252, 0.8);
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.mode-tab:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
}

.mode-tab.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.2);
}

.mode-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 24px;
  color: white;
}

.mode-tab.active .mode-icon {
  transform: scale(1.1);
}

.mode-content {
  flex: 1;
}

.mode-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.mode-desc {
  font-size: 14px;
  color: #86868b;
  line-height: 1.5;
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

.param-card.full-width {
  grid-column: 1 / -1;
}

.question-type-config {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 12px;
}

.type-config-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.type-config-item:hover {
  background: rgba(102, 126, 234, 0.05);
  border-color: rgba(102, 126, 234, 0.2);
}

.type-header {
  flex: 1;
}

.type-count {
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-label {
  color: #86868b;
  font-size: 14px;
}

/* 考试时长组件样式 */
.duration-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.duration-input {
  flex: 1;
  min-width: 120px;
}

.duration-unit {
  color: #86868b;
  font-size: 14px;
  font-weight: 500;
}

.expire-time-picker {
  width: 100%;
  margin-bottom: 12px;
}

.expire-time-picker :deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.expire-time-picker :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

.expire-time-picker :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #667eea;
  box-shadow: 0 0 0 1px #667eea inset;
}

.duration-presets,
.expire-time-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preset-btn {
  padding: 8px 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: rgba(248, 250, 252, 0.8);
  color: #86868b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.preset-btn:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  color: #667eea;
  transform: translateY(-1px);
}

.preset-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.preset-btn.active:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
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

/* 试卷名称输入框样式 */
.paper-name-input {
  width: 100%;
  margin-bottom: 12px;
}

.param-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #86868b;
  font-size: 13px;
  line-height: 1.4;
  margin-top: 8px;
}

.param-tip .el-icon {
  color: #667eea;
  font-size: 16px;
}

/* 调试区域样式 */
.debug-section {
  margin: 12px 0;
  text-align: center;
}

.debug-btn {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.debug-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: #667eea;
  transform: translateY(-1px);
}

.debug-btn .el-icon {
  font-size: 14px;
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
