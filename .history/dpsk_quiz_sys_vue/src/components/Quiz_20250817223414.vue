<template>
  <el-container style="height: 100vh; background-color: #f5f7fa">
    <!-- 顶部导航栏 -->
    <el-header
      style="
        background-color: #409eff;
        color: white;
        display: flex;
        align-items: center;
        justify-content: space-between;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
      "
    >
      <div style="font-size: 20px; font-weight: 500">在线答题</div>
      <div class="quiz-info">
        <span>剩余时间：{{ formatTime(remainingTime) }}</span>
        <span>题目：{{ currentQuestionIndex + 1 }}/{{ totalQuestions }}</span>
      </div>
    </el-header>

    <!-- 主体区域 -->
    <el-main style="padding: 20px; display: flex; justify-content: center">
      <el-card class="quiz-card" shadow="hover">
        <!-- 文件上传区域 -->
        <div v-if="!questions.length" class="upload-section">
          <h2 class="section-title">上传学习资料</h2>
          <p class="section-desc">上传您的学习资料，AI将为您生成相关题目</p>

          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="true"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :accept="supportedFormats"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传 {{ supportedFormatsText }} 文件，大小不超过
                {{ maxSizeMB }}MB
              </div>
            </template>
          </el-upload>

          <el-button
            type="primary"
            class="generate-btn"
            :loading="generating"
            :disabled="!selectedFile"
            @click="generateQuestions"
          >
            生成题目
          </el-button>
        </div>

        <!-- 题目内容 -->
        <div v-else class="question-content">
          <h2 class="question-title">第 {{ currentQuestionIndex + 1 }} 题</h2>
          <div class="question-text">{{ currentQuestion.question }}</div>

          <!-- 选项列表 -->
          <div class="options-list">
            <el-radio-group v-model="selectedAnswer" class="options-group">
              <el-radio
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                :label="option.key"
                class="option-item"
              >
                {{ option.key }}. {{ option.content }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 解析区域 -->
          <div v-if="showExplanation" class="explanation-section">
            <h3>题目解析</h3>
            <div class="explanation-content">
              <p>{{ currentQuestion.explanation }}</p>
              <div class="knowledge-points">
                <h4>相关知识点：</h4>
                <el-tag
                  v-for="(point, index) in currentQuestion.knowledgePoints"
                  :key="index"
                  class="knowledge-tag"
                >
                  {{ point }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button
              type="primary"
              :disabled="currentQuestionIndex === 0"
              @click="handlePrevQuestion"
            >
              上一题
            </el-button>
            <el-button
              type="primary"
              :disabled="currentQuestionIndex === totalQuestions - 1"
              @click="handleNextQuestion"
            >
              下一题
            </el-button>
            <el-button
              type="success"
              :disabled="!isAllAnswered"
              @click="handleSubmit"
            >
              提交答案
            </el-button>
            <el-button type="info" @click="showExplanation = !showExplanation">
              {{ showExplanation ? "隐藏解析" : "查看解析" }}
            </el-button>
          </div>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import { UploadFilled } from "@element-plus/icons-vue";

// 文件上传相关配置
const maxSizeMB = 10;
const supportedFormats = ".pdf,.doc,.docx,.txt";
const supportedFormatsText = "PDF、Word、TXT";

const selectedFile = ref(null);
const generating = ref(false);
const showExplanation = ref(false);
const isQuizStarted = ref(false);

// 题目数据
const questions = ref([]);
const currentQuestionIndex = ref(0);
const selectedAnswer = ref("");
const answers = ref([]);
const remainingTime = ref(3600); // 1小时
let timer = null;

// 计算属性
const currentQuestion = computed(
  () => questions.value[currentQuestionIndex.value]
);
const totalQuestions = computed(() => questions.value.length);
const isAllAnswered = computed(
  () => answers.value.filter(Boolean).length === totalQuestions.value
);

// 格式化时间
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  return `${hours.toString().padStart(2, "0")}:${minutes
    .toString()
    .padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

// 文件上传处理
const handleFileChange = (file) => {
  selectedFile.value = file.raw;
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

// 生成题目
const generateQuestions = async () => {
  if (!selectedFile.value) {
    ElMessage.warning("请先上传文件");
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
    formData.append("file", selectedFile.value);

    const response = await fetch("/api/quiz/generate", {
      method: "POST",
      credentials: "include",
      body: formData,
    });

    if (!response.ok) {
      throw new Error("生成题目失败");
    }

    const result = await response.json();
    questions.value = result.data.questions;
    ElMessage.success("题目生成成功");
    // 开始计时
    startTimer();
  } catch (error) {
    ElMessage.error(error.message || "生成题目失败");
  } finally {
    generating.value = false;
    loading.close();
  }
};

// 开始计时器
const startTimer = () => {
  isQuizStarted.value = true;
  remainingTime.value = 3600; // 重置为1小时
  if (timer) {
    clearInterval(timer);
  }
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else {
      clearInterval(timer);
      ElMessage.warning("答题时间已到，系统将自动提交");
      handleSubmit();
    }
  }, 1000);
};

// 处理上一题
const handlePrevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
    selectedAnswer.value = answers.value[currentQuestionIndex.value] || "";
  }
};

// 处理下一题
const handleNextQuestion = () => {
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    answers.value[currentQuestionIndex.value] = selectedAnswer.value;
    currentQuestionIndex.value++;
    selectedAnswer.value = answers.value[currentQuestionIndex.value] || "";
  }
};

// 处理提交
const handleSubmit = async () => {
  answers.value[currentQuestionIndex.value] = selectedAnswer.value;

  try {
    const response = await fetch("/api/quiz/submit", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        answers: answers.value,
        questions: questions.value,
      }),
    });

    if (!response.ok) {
      throw new Error("提交失败");
    }

    const result = await response.json();
    ElMessage.success(
      `答题完成！得分：${result.data.score}/${totalQuestions.value}`
    );
  } catch (error) {
    ElMessage.error(error.message || "提交失败");
  }
};

// 监听答案变化
watch(selectedAnswer, (newValue) => {
  answers.value[currentQuestionIndex.value] = newValue;
});

// 组件挂载时不再自动启动计时器
onMounted(() => {
  // 如果是从其他页面返回，且有未完成的答题，则恢复计时
  if (questions.value.length > 0) {
    startTimer();
  }
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

<style scoped>
.quiz-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.upload-section {
  text-align: center;
  padding: 40px 20px;
}

.section-title {
  color: #409eff;
  margin-bottom: 10px;
}

.section-desc {
  color: #606266;
  margin-bottom: 30px;
}

.upload-area {
  width: 100%;
  max-width: 500px;
  margin: 0 auto 20px;
}

.generate-btn {
  margin-top: 20px;
}

.quiz-info {
  display: flex;
  gap: 20px;
}

.question-content {
  margin-bottom: 30px;
}

.question-title {
  color: #409eff;
  margin-bottom: 15px;
}

.question-text {
  font-size: 18px;
  line-height: 1.6;
  color: #303133;
}

.options-list {
  margin: 30px 0;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.option-item {
  padding: 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.option-item:hover {
  background-color: #f5f7fa;
}

.explanation-section {
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.explanation-section h3 {
  color: #409eff;
  margin-bottom: 15px;
}

.explanation-content {
  color: #606266;
  line-height: 1.6;
}

.knowledge-points {
  margin-top: 15px;
}

.knowledge-points h4 {
  margin-bottom: 10px;
  color: #303133;
}

.knowledge-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #409eff;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #409eff;
  border-color: #409eff;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>
