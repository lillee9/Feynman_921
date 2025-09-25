<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 头部 -->
    <header class="header">
      <div class="exam-title">
        考试中 - 《{{ exerciseData.title || "AI练习题" }}》
      </div>
      <div class="timer">剩余 {{ formatTime(remainingTime) }}</div>
    </header>

    <!-- 主体容器 - 横向布局 -->
    <div class="main-content">
      <!-- 左侧导航 -->
      <div class="nav-panel">
        <div class="nav-title">题目导航</div>
        <div class="question-nav">
          <button
            v-for="(question, index) in questions"
            :key="index"
            @click="goToQuestion(index)"
            class="nav-item"
            :class="{
              answered:
                isQuestionAnswered(index) &&
                index !== currentQuestionIndex &&
                !markedQuestions.includes(index),
              current:
                index === currentQuestionIndex &&
                !markedQuestions.includes(index),
              unanswered:
                !isQuestionAnswered(index) &&
                index !== currentQuestionIndex &&
                !markedQuestions.includes(index),
              marked: markedQuestions.includes(index),
            }"
          >
            {{ index + 1 }}
          </button>
        </div>
      </div>

      <!-- 中间内容区 -->
      <div class="content-area">
        <!-- 进度提示 -->
        <div class="progress-info">
          共 {{ questions.length }} 题，已答 {{ answeredCount }} 题，标记
          {{ markedQuestions.length }} 题
        </div>

        <!-- 题目卡片 -->
        <div v-if="questions.length > 0" class="question-card">
          <div class="question-header">
            <div class="question-type">
              {{ getQuestionTypeText(currentQuestion.type) }}
            </div>
            <div>
              第{{ currentQuestionIndex + 1 }}题 / 共{{ questions.length }}题
            </div>
          </div>
          <div class="question-title">
            {{
              currentQuestion.questionContent ||
              currentQuestion.question ||
              currentQuestion.questionText ||
              currentQuestion.content ||
              currentQuestion.title ||
              currentQuestion.text ||
              "题目内容加载中..."
            }}
          </div>

          <!-- 调试信息已移除，保留console.log用于开发调试 -->

          <!-- 选择题选项 -->
          <div
            v-if="
              currentQuestion.type === 'CHOICE' ||
              currentQuestion.type === 'SINGLE_CHOICE'
            "
            class="options"
          >
            <!-- 选项渲染调试信息已移除 -->

            <div
              v-for="(option, optionIndex) in getQuestionOptions(
                currentQuestion
              )"
              :key="optionIndex"
              class="option"
              :class="{
                selected:
                  userAnswers[currentQuestionIndex] ===
                  String.fromCharCode(65 + optionIndex),
              }"
              @click="
                userAnswers[currentQuestionIndex] = String.fromCharCode(
                  65 + optionIndex
                )
              "
            >
              <input
                type="radio"
                :name="`question-${currentQuestionIndex}`"
                :value="String.fromCharCode(65 + optionIndex)"
                v-model="userAnswers[currentQuestionIndex]"
              />
              <span
                >{{ String.fromCharCode(65 + optionIndex) }}. {{ option }}</span
              >
            </div>
          </div>

          <!-- 多选题选项 -->
          <div
            v-else-if="
              currentQuestion.type === 'MULTIPLE_CHOICE' ||
              currentQuestion.type === 'MULTI_CHOICE'
            "
            class="options"
          >
            <!-- 多选题选项渲染调试信息已移除 -->

            <div
              v-for="(option, optionIndex) in getQuestionOptions(
                currentQuestion
              )"
              :key="optionIndex"
              class="option"
              :class="{
                selected:
                  userAnswers[currentQuestionIndex] &&
                  userAnswers[currentQuestionIndex].includes(
                    String.fromCharCode(65 + optionIndex)
                  ),
              }"
            >
              <input
                type="checkbox"
                :value="String.fromCharCode(65 + optionIndex)"
                v-model="userAnswers[currentQuestionIndex]"
              />
              <span
                >{{ String.fromCharCode(65 + optionIndex) }}. {{ option }}</span
              >
            </div>
          </div>

          <!-- 选项为空时的提示 -->
          <div
            v-if="
              (currentQuestion.type === 'CHOICE' ||
                currentQuestion.type === 'SINGLE_CHOICE' ||
                currentQuestion.type === 'MULTIPLE_CHOICE' ||
                currentQuestion.type === 'MULTI_CHOICE') &&
              getQuestionOptions(currentQuestion).length === 0
            "
            class="no-options"
          >
            <div class="text-center text-gray-500 py-4">
              <p>❌ 选项数据为空</p>
              <p class="text-sm">请联系管理员检查题目配置</p>
            </div>
          </div>

          <!-- 填空题输入框 -->
          <div v-else-if="currentQuestion.type === 'FILL_BLANK'">
            <input
              v-model="userAnswers[currentQuestionIndex]"
              type="text"
              class="text-input"
              placeholder="请输入填空答案..."
            />
          </div>

          <!-- 简答题输入框 -->
          <div v-else-if="currentQuestion.type === 'SHORT_ANSWER'">
            <textarea
              v-model="userAnswers[currentQuestionIndex]"
              class="text-input textarea-input"
              rows="6"
              placeholder="请详细回答问题..."
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 右侧操作面板 -->
      <div class="action-panel">
        <button @click="toggleMark" class="action-btn btn-warning">
          {{
            markedQuestions.includes(currentQuestionIndex)
              ? "取消标记"
              : "标记题目"
          }}
        </button>

        <button
          @click="previousQuestion"
          :disabled="currentQuestionIndex === 0"
          class="action-btn btn-secondary"
        >
          上一题
        </button>

        <button
          @click="nextQuestion"
          :disabled="currentQuestionIndex === questions.length - 1"
          class="action-btn btn-primary"
        >
          下一题
        </button>

        <button @click="submitAnswers" class="action-btn btn-success">
          提交答卷
        </button>
      </div>
    </div>

    <!-- 结果展示模态框 -->
    <div
      v-if="showResults"
      class="fixed inset-0 bg-gray-900 bg-opacity-30 flex items-center justify-center p-4 z-50"
    >
      <div
        class="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] overflow-hidden flex flex-col"
      >
        <!-- 模态框头部 -->
        <div
          class="p-6 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-purple-50"
        >
          <div class="flex items-center justify-between">
            <h2 class="text-2xl font-bold text-gray-800">📊 答题结果</h2>
            <button
              @click="closeResults"
              class="text-gray-400 hover:text-gray-600 transition-colors p-2 hover:bg-white rounded-full"
            >
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            </button>
          </div>

          <!-- 得分展示 -->
          <div class="mt-6 p-6 bg-white rounded-xl shadow-sm">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 text-center">
              <div>
                <div class="text-4xl font-bold text-blue-600 mb-2">
                  {{ Math.round((correctCount / questions.length) * 100) }}%
                </div>
                <div class="text-gray-600 font-medium">总得分</div>
              </div>
              <div>
                <div class="text-4xl font-bold text-green-600 mb-2">
                  {{ correctCount }}
                </div>
                <div class="text-gray-600 font-medium">正确题数</div>
              </div>
              <div>
                <div class="text-4xl font-bold text-gray-600 mb-2">
                  {{ questions.length }}
                </div>
                <div class="text-gray-600 font-medium">总题数</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 模态框内容 -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="space-y-6">
            <div
              v-for="(question, index) in questions"
              :key="index"
              class="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow duration-200"
            >
              <div class="flex items-start justify-between mb-4">
                <h3 class="font-semibold text-gray-800 text-lg">
                  第{{ index + 1 }}题
                </h3>
                <span
                  class="px-4 py-2 rounded-full text-sm font-medium"
                  :class="{
                    'bg-green-100 text-green-800': isAnswerCorrect(index),
                    'bg-red-100 text-red-800': !isAnswerCorrect(index),
                  }"
                >
                  {{ isAnswerCorrect(index) ? "✅ 正确" : "❌ 错误" }}
                </span>
              </div>

              <p class="text-gray-700 mb-4 text-lg leading-relaxed">
                {{ question.questionContent || question.question }}
              </p>

              <div class="space-y-3 bg-gray-50 rounded-lg p-4">
                <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                  <span class="text-gray-500 font-medium min-w-20"
                    >你的答案:</span
                  >
                  <span
                    class="font-medium"
                    :class="{
                      'text-green-600': isAnswerCorrect(index),
                      'text-red-600': !isAnswerCorrect(index),
                    }"
                  >
                    {{ userAnswers[index] || "未作答" }}
                  </span>
                </div>
                <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                  <span class="text-gray-500 font-medium min-w-20"
                    >正确答案:</span
                  >
                  <span class="text-green-600 font-semibold">{{
                    question.correctAnswer || question.answer
                  }}</span>
                </div>
                <div
                  v-if="question.analysis || question.explanation"
                  class="flex flex-col gap-2"
                >
                  <span class="text-gray-500 font-medium">解析:</span>
                  <span class="text-gray-700 leading-relaxed">{{
                    question.analysis || question.explanation
                  }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 模态框底部 -->
        <div class="p-6 border-t border-gray-200 bg-gray-50">
          <div class="flex flex-col sm:flex-row justify-center gap-4">
            <button
              @click="restartExercise"
              class="px-8 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-semibold shadow-md hover:shadow-lg"
            >
              🔄 重新开始
            </button>
            <button
              @click="$router.push('/')"
              class="px-8 py-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors font-semibold shadow-md hover:shadow-lg"
            >
              🏠 返回首页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from "vue-router";
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useUserStore } from "@/stores/user";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 响应式数据
const questions = ref([]);
const exerciseData = ref({});
const currentQuestionIndex = ref(0);
const userAnswers = ref([]);
const showResults = ref(false);
const markedQuestions = ref([]);
const remainingTime = ref(4800); // 默认80分钟 = 4800秒，将根据传入的考试时长动态设置
const timer = ref(null);

// 计算属性
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || {};
});

const isAllAnswered = computed(() => {
  return userAnswers.value.every((answer) => {
    if (Array.isArray(answer)) {
      return answer.length > 0;
    }
    return answer && typeof answer === "string" && answer.trim() !== "";
  });
});

const correctCount = computed(() => {
  return questions.value.filter((question, index) => {
    return isAnswerCorrect(index);
  }).length;
});

const answeredCount = computed(() => {
  return userAnswers.value.filter((answer) => {
    if (Array.isArray(answer)) {
      return answer.length > 0;
    }
    return answer && typeof answer === "string" && answer.trim() !== "";
  }).length;
});

// 工具函数
const getQuestionTypeText = (type) => {
  const typeMap = {
    SINGLE_CHOICE: "单选题",
    CHOICE: "单选题", // 兼容旧格式
    MULTI_CHOICE: "多选题",
    MULTIPLE_CHOICE: "多选题", // 兼容旧格式
    FILL_BLANK: "填空题",
    SHORT_ANSWER: "简答题",
    JUDGE: "判断题",
  };
  return typeMap[type] || "未知题型";
};

const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    EASY: "简单",
    MEDIUM: "中等",
    HARD: "困难",
  };
  return difficultyMap[difficulty] || difficulty;
};

const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  return `${hours.toString().padStart(2, "0")}:${minutes
    .toString()
    .padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

const isQuestionAnswered = (index) => {
  const answer = userAnswers.value[index];
  if (Array.isArray(answer)) {
    return answer.length > 0;
  }
  return answer && typeof answer === "string" && answer.trim() !== "";
};

const goToQuestion = (index) => {
  currentQuestionIndex.value = index;
};

const saveAnswer = () => {
  // 答案已经通过v-model自动保存，这里可以添加提示
  console.log("答案已暂存");
};

const toggleMark = () => {
  const index = currentQuestionIndex.value;
  const markIndex = markedQuestions.value.indexOf(index);
  if (markIndex > -1) {
    markedQuestions.value.splice(markIndex, 1);
  } else {
    markedQuestions.value.push(index);
  }
};

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else {
      clearInterval(timer.value);
      // 时间到自动提交
      submitAnswers();
    }
  }, 1000);
};

const stopTimer = () => {
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
};

const isAnswerCorrect = (index) => {
  const question = questions.value[index];
  const userAnswer = userAnswers.value[index];

  if (!question || !userAnswer) return false;

  const correctAnswer = question.correctAnswer || question.answer;

  // 对于选择题，直接比较选项字母
  if (question.type === "CHOICE" || question.type === "SINGLE_CHOICE") {
    return (
      userAnswer.trim().toUpperCase() === correctAnswer.trim().toUpperCase()
    );
  }

  // 对于多选题，比较选中的选项数组
  if (question.type === "MULTIPLE_CHOICE" || question.type === "MULTI_CHOICE") {
    if (Array.isArray(userAnswer)) {
      const userAnswerSorted = userAnswer.sort().join("");
      const correctAnswerSorted = correctAnswer.split("").sort().join("");
      return userAnswerSorted === correctAnswerSorted;
    }
    return false;
  }

  // 对于填空题，进行精确的字符串比较
  if (question.type === "FILL_BLANK") {
    return (
      userAnswer.trim().toLowerCase() === correctAnswer.trim().toLowerCase()
    );
  }

  // 对于简答题，检查关键词匹配（如果有keyPoints）或进行基本比较
  if (question.type === "SHORT_ANSWER") {
    if (question.keyPoints && Array.isArray(question.keyPoints)) {
      // 检查用户答案是否包含关键要点
      const userAnswerLower = userAnswer.trim().toLowerCase();
      return question.keyPoints.some((point) =>
        userAnswerLower.includes(point.toLowerCase())
      );
    }
    // 如果没有关键要点，进行基本的字符串比较
    return userAnswer
      .trim()
      .toLowerCase()
      .includes(correctAnswer.trim().toLowerCase());
  }

  return false;
};

// 导航函数
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
  }
};

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--;
  }
};

const submitAnswers = async () => {
  if (!isAllAnswered.value) {
    alert("请完成所有题目后再提交");
    return;
  }

  // 保存答题记录到数据库
  await saveAnswersToDatabase();

  showResults.value = true;
};

const closeResults = () => {
  showResults.value = false;
};

const restartExercise = () => {
  currentQuestionIndex.value = 0;
  userAnswers.value = new Array(questions.value.length).fill("");
  showResults.value = false;
};

// 初始化
onMounted(() => {
  console.log("🚀 Exercise 页面开始初始化");

  try {
    // 从 sessionStorage 中读取数据
    if (route.query.dataKey) {
      console.log("✅ 发现 query.dataKey:", route.query.dataKey);

      const storedData = sessionStorage.getItem(route.query.dataKey);
      if (storedData) {
        console.log("✅ 从 sessionStorage 读取到数据");

        const data = JSON.parse(storedData);
        console.log("📝 解析后的数据:", data);
        console.log("🔍 调试 - 原始数据中的examPaperId:", data.examPaperId);

        questions.value = data.questions || [];

        // 详细调试questions数组
        console.log("📚 题目数据详细调试:");
        console.log("  - data.questions原始数据:", data.questions);
        console.log("  - data.questions类型:", typeof data.questions);
        console.log(
          "  - data.questions是否为数组:",
          Array.isArray(data.questions)
        );
        console.log("  - questions.value:", questions.value);
        console.log("  - questions.value类型:", typeof questions.value);
        console.log(
          "  - questions.value是否为数组:",
          Array.isArray(questions.value)
        );
        console.log("  - questions.value长度:", questions.value.length);

        if (questions.value.length > 0) {
          console.log("  - 第一题数据:", questions.value[0]);
          console.log("  - 第一题字段:", Object.keys(questions.value[0]));
          console.log("  - 第一题ID:", questions.value[0].id);
          console.log(
            "  - 第一题内容:",
            questions.value[0].content || questions.value[0].questionText
          );

          // 详细验证题目数据结构
          console.log("\n🔍 题目数据结构验证:");
          questions.value.forEach((question, index) => {
            console.log(`\n  题目 ${index + 1}:`, {
              id: question.id,
              type: question.type,
              content: question.content || question.questionText,
              hasOptions: !!(
                question.options ||
                question.questionOptions ||
                question.choices ||
                question.optionA
              ),
              optionFields: {
                options: !!question.options,
                questionOptions: !!question.questionOptions,
                choices: !!question.choices,
                optionA: !!question.optionA,
                optionB: !!question.optionB,
                optionC: !!question.optionC,
                optionD: !!question.optionD,
                optionList: !!question.optionList,
              },
              optionsCount: getQuestionOptions(question).length,
            });

            // 详细分析选项数据结构
            console.log(`\n  📋 题目 ${index + 1} 选项数据详细分析:`);
            console.log("    - 原始题目对象:", question);
            console.log("    - questionOptions字段:", question.questionOptions);
            console.log(
              "    - questionOptions类型:",
              typeof question.questionOptions
            );
            if (
              question.questionOptions &&
              typeof question.questionOptions === "object"
            ) {
              console.log(
                "    - questionOptions.options:",
                question.questionOptions.options
              );
              console.log(
                "    - questionOptions.options类型:",
                typeof question.questionOptions.options
              );
              console.log(
                "    - questionOptions.options是否为数组:",
                Array.isArray(question.questionOptions.options)
              );
              if (Array.isArray(question.questionOptions.options)) {
                console.log(
                  "    - questionOptions.options内容:",
                  question.questionOptions.options
                );
                console.log(
                  "    - questionOptions.options长度:",
                  question.questionOptions.options.length
                );
              }
            }

            // 检查选项数据完整性
            const options = getQuestionOptions(question);
            if (options.length === 0) {
              console.warn(`  ⚠️ 题目 ${index + 1} 没有选项数据!`);
              console.log("    - 所有可能的选项字段检查:");
              console.log("      - options:", question.options);
              console.log("      - questionOptions:", question.questionOptions);
              console.log("      - choices:", question.choices);
              console.log("      - optionList:", question.optionList);
              console.log("      - optionA:", question.optionA);
              console.log("      - optionB:", question.optionB);
              console.log("      - optionC:", question.optionC);
              console.log("      - optionD:", question.optionD);
            } else {
              console.log(
                `  ✅ 题目 ${index + 1} 有 ${options.length} 个选项:`,
                options
              );
            }
          });
        } else {
          console.log("  - ❌ 题目数组为空!");
        }

        exerciseData.value = {
          type: data.type,
          difficulty: data.difficulty,
          total: data.total,
          examDuration: data.examDuration || 80, // 考试时长，默认80分钟
          examPaperId: data.examPaperId, // 确保传递试卷ID
        };

        console.log("🔍 调试 - 设置后的exerciseData:", exerciseData.value);

        // 设置考试时长（将分钟转换为秒）
        const durationInMinutes = data.examDuration || 80;
        remainingTime.value = durationInMinutes * 60;
        console.log(
          "⏰ 设置考试时长:",
          durationInMinutes,
          "分钟 (",
          remainingTime.value,
          "秒)"
        );

        // 初始化用户答案数组，多选题初始化为空数组
        userAnswers.value = questions.value.map((q) =>
          q.type === "MULTIPLE_CHOICE" || q.type === "MULTI_CHOICE" ? [] : ""
        );

        console.log("✅ 成功加载题目数量:", questions.value.length);

        // 详细检查每个题目的选项数据
        console.log("🔍 详细检查每个题目的选项数据:");
        questions.value.forEach((question, index) => {
          console.log(`\n📝 题目 ${index + 1}:`, {
            id: question.id,
            type: question.type,
            hasQuestionOptions: !!question.questionOptions,
            hasOptions: !!question.options,
            questionOptionsType: typeof question.questionOptions,
            optionsType: typeof question.options,
            questionOptionsContent: question.questionOptions,
            optionsContent: question.options,
            allFields: Object.keys(question),
          });

          const options = getQuestionOptions(question);
          console.log(`  🎯 getQuestionOptions返回:`, options);
          console.log(`  📊 选项数量: ${options.length}`);

          if (
            options.length === 0 &&
            (question.type === "CHOICE" ||
              question.type === "SINGLE_CHOICE" ||
              question.type === "MULTIPLE_CHOICE" ||
              question.type === "MULTI_CHOICE")
          ) {
            console.error(`  ❌ 选择题没有选项数据! 题目${index + 1}`);
          }
        });

        // 如果题目数组为空，显示警告
        if (questions.value.length === 0) {
          console.warn("⚠️ 警告：题目数组为空，可能导致页面显示问题");
          ElMessage.warning("未找到题目数据，请检查试卷配置");
        }

        // 启动计时器
        startTimer();

        // 清除临时存储
        sessionStorage.removeItem(route.query.dataKey);
        console.log("🗑️ 已清除临时存储");
      } else {
        console.warn("❌ sessionStorage 中没有找到对应数据");
        // 可以考虑重定向到首页或显示错误信息
        router.push("/");
      }
    } else {
      console.warn("❌ 未找到数据键");
      router.push("/");
    }
  } catch (error) {
    console.error("❌ 解析题目数据时出错:", error);
    router.push("/");
  }
});

// 获取Cookie中的token
const getCookie = (name) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(";").shift();
  return null;
};

// 获取题目选项
const getQuestionOptions = (question) => {
  if (!question) {
    console.log("🔍 getQuestionOptions: 题目对象为空");
    return [];
  }

  console.log("🔍 getQuestionOptions 调试:");
  console.log("  - 题目ID:", question.id);
  console.log("  - 题目类型:", question.type);
  console.log("  - 题目所有字段:", Object.keys(question));
  console.log("  - 题目完整内容:", question);

  // 优先处理后端返回的questionOptions字段
  if (
    question.questionOptions &&
    typeof question.questionOptions === "object"
  ) {
    if (Array.isArray(question.questionOptions.options)) {
      console.log(
        "  ✅ 找到questionOptions.options字段，选项数量:",
        question.questionOptions.options.length
      );
      return question.questionOptions.options;
    }
    if (Array.isArray(question.questionOptions)) {
      console.log(
        "  ✅ 找到questionOptions数组字段，选项数量:",
        question.questionOptions.length
      );
      return question.questionOptions;
    }
  }

  // 处理后端返回的options字段（可能是字符串形式的JSON数组）
  if (question.options) {
    if (Array.isArray(question.options)) {
      console.log(
        "  ✅ 找到options数组字段，选项数量:",
        question.options.length
      );
      return question.options;
    }
    if (typeof question.options === "string") {
      try {
        const parsedOptions = JSON.parse(question.options);
        if (Array.isArray(parsedOptions)) {
          console.log(
            "  ✅ 解析options字符串成功，选项数量:",
            parsedOptions.length
          );
          return parsedOptions;
        }
      } catch (e) {
        console.log("  ❌ 解析options字符串失败:", e.message);
      }
    }
  }

  if (question.choices && Array.isArray(question.choices)) {
    console.log("  ✅ 找到choices字段，选项数量:", question.choices.length);
    return question.choices;
  }

  // 检查字符串形式的选项数组
  if (question.optionList && typeof question.optionList === "string") {
    try {
      const parsedOptions = JSON.parse(question.optionList);
      if (Array.isArray(parsedOptions)) {
        console.log(
          "  ✅ 解析optionList字符串，选项数量:",
          parsedOptions.length
        );
        return parsedOptions;
      }
    } catch (e) {
      console.log("  ❌ 解析optionList失败:", e.message);
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
    console.log("  ✅ 从optionA-F字段提取选项，选项数量:", options.length);
    return options;
  }

  // 检查其他可能的选项字段
  const possibleOptionFields = [
    "answerOptions",
    "questionChoices",
    "optionContent",
    "answerChoices",
  ];
  for (const field of possibleOptionFields) {
    if (question[field] && Array.isArray(question[field])) {
      console.log(`  ✅ 找到${field}字段，选项数量:`, question[field].length);
      return question[field];
    }
  }

  console.log("  ❌ 未找到任何选项数据");
  console.log("  - 可能的选项字段检查结果:");
  console.log("    - options:", question.options);
  console.log("    - questionOptions:", question.questionOptions);
  console.log("    - choices:", question.choices);
  console.log("    - optionList:", question.optionList);
  console.log("    - optionA:", question.optionA);
  console.log("    - optionB:", question.optionB);

  return [];
};

// 保存答题记录到数据库
const saveAnswersToDatabase = async () => {
  try {
    const token = getCookie("token");
    if (!token) {
      console.warn("未找到用户token，无法保存答题记录");
      return;
    }

    // 调试：检查exerciseData的完整内容
    console.log("🔍 调试 - exerciseData完整内容:", exerciseData.value);
    console.log("🔍 调试 - examPaperId值:", exerciseData.value.examPaperId);
    console.log(
      "🔍 调试 - examPaperId类型:",
      typeof exerciseData.value.examPaperId
    );
    console.log("🔍 调试 - isBackupId标记:", exerciseData.value.isBackupId);

    // 确保试卷ID不为空
    let paperId = exerciseData.value.examPaperId;
    let isBackupId = exerciseData.value.isBackupId || false;

    if (
      !paperId ||
      paperId === "" ||
      paperId === null ||
      paperId === undefined
    ) {
      console.error("❌ 试卷ID为空，尝试获取备用ID");
      console.error("🔍 详细错误信息:");
      console.error(
        "  - exerciseData.examPaperId值:",
        exerciseData.value.examPaperId
      );
      console.error("  - exerciseData完整对象:", exerciseData.value);
      console.error(
        "  - sessionStorage中的数据:",
        sessionStorage.getItem(route.query.dataKey)
      );

      // 尝试从其他可能的来源获取试卷ID
      const alternativeIds = [
        exerciseData.value.id,
        exerciseData.value.paperId,
        exerciseData.value.examId,
        route.query.paperId,
        route.query.examPaperId,
      ];

      console.error("🔍 尝试的备用ID来源:", alternativeIds);
      const foundId = alternativeIds.find(
        (id) => id && id !== "" && id !== null && id !== undefined
      );

      if (foundId) {
        console.warn("⚠️ 使用备用ID:", foundId);
        paperId = foundId;
        isBackupId = true;
        exerciseData.value.examPaperId = foundId;
        exerciseData.value.isBackupId = true;
      } else {
        // 生成最后的备用ID
        paperId =
          "temp_exercise_" +
          Date.now() +
          "_" +
          Math.random().toString(36).substr(2, 9);
        isBackupId = true;
        console.warn("🔧 生成最终备用ID:", paperId);

        ElMessage.warning({
          message:
            "试卷ID获取失败，使用临时ID继续。答题记录可能无法保存到数据库，但不影响答题功能。",
          duration: 6000,
          showClose: true,
        });

        exerciseData.value.examPaperId = paperId;
        exerciseData.value.isBackupId = true;
      }
    }

    console.log("✅ 试卷ID验证通过:", paperId);
    console.log("📋 ID类型:", isBackupId ? "备用ID" : "正常ID");

    // 如果是备用ID，给用户提示但仍尝试保存
    if (isBackupId) {
      console.warn("⚠️ 使用备用ID保存答题记录，可能无法正常保存到数据库");
    }

    // 构建答题记录数据
    const answerData = {
      userId: userStore.id || 1, // 从用户store获取用户ID，如果没有则使用默认值1
      paperId: paperId,
      startTime: new Date(
        Date.now() - (exerciseData.value.examDuration || 80) * 60 * 1000
      ).toISOString(),
      endTime: new Date().toISOString(),
      isBackupId: isBackupId, // 标记是否为备用ID
      paperName: exerciseData.value.paperName || "未命名试卷",
      answers: questions.value.map((question, index) => ({
        questionId: question.id || index + 1, // 使用题目ID或索引
        userAnswer: Array.isArray(userAnswers.value[index])
          ? userAnswers.value[index].join(",")
          : userAnswers.value[index] || "",
        timeSpent: 0, // 可以记录每题的答题时间
      })),
    };

    console.log("📝 准备保存答题记录:", answerData);
    console.log("📝 试卷ID检查:", answerData.paperId ? "✅ 有效" : "❌ 为空");
    console.log("📝 备用ID标记:", answerData.isBackupId);

    try {
      const response = await fetch("/api/exam/results/submit", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        credentials: "include",
        body: JSON.stringify(answerData),
      });

      const result = await response.json();

      if (response.ok && result.code === 200) {
        console.log("答题记录保存成功:", result.data);
        if (isBackupId) {
          ElMessage.success({
            message:
              "答题完成！由于使用临时ID，记录已尝试保存但可能无法持久化到数据库。",
            duration: 5000,
          });
        } else {
          ElMessage.success("答题记录保存成功！");
        }
      } else {
        console.error("保存答题记录失败:", result);
        if (isBackupId) {
          ElMessage.info({
            message:
              "答题完成！由于使用临时ID，记录无法保存到数据库，但答题过程已完成。",
            duration: 5000,
          });
        } else {
          ElMessage.error(
            "答题记录保存失败：" + (result.message || "未知错误")
          );
        }
      }
    } catch (saveError) {
      console.error("保存答题记录网络异常:", saveError);
      if (isBackupId) {
        ElMessage.info({
          message:
            "答题完成！由于网络问题和临时ID，记录无法保存，但答题过程已完成。",
          duration: 5000,
        });
      } else {
        ElMessage.error("网络异常，答题记录保存失败");
      }
    }
  } catch (error) {
    console.error("保存答题记录异常:", error);
  }
};

// 组件销毁时清理计时器
onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
/* 基础样式 */
body {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
}

.container {
  display: flex;
  height: 100vh;
}

/* 头部样式 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.exam-title {
  font-size: 20px;
  font-weight: bold;
}

.timer {
  font-size: 18px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
}

/* 导航面板样式 */
.nav-panel {
  width: 250px;
  background: white;
  padding: 15px;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  flex-shrink: 0;
}

.nav-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
  text-align: center;
}

.question-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 10px;
}

.nav-item {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  font-size: 12px;
  transition: all 0.3s ease;
  background: white;
}

.nav-item.current {
  background: #667eea;
  color: white;
}

.nav-item.answered {
  background: #4caf50;
  color: white;
}

.nav-item.marked {
  background: #ff9800;
  color: white;
}

.nav-item.unanswered {
  background: #f0f0f0;
  color: #666;
}

/* 内容区域样式 */
.content-area {
  flex: 1;
  padding: 20px;
  background: #f5f5f5;
  overflow-y: auto;
}

.progress-info {
  margin-bottom: 15px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  text-align: center;
}

/* 题目卡片样式 */
.question-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.question-type {
  background: #667eea;
  color: white;
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 12px;
}

.question-title {
  font-size: 18px;
  font-weight: bold;
  margin: 15px 0;
  color: #333;
}

/* 选项样式 */
.options {
  margin: 20px 0;
}

.option {
  display: flex;
  align-items: center;
  margin: 12px 0;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.option.selected {
  border-color: #667eea;
  background: #e8f0fe;
}

.option input {
  margin-right: 10px;
}

/* 输入框样式 */
.text-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.text-input:focus {
  outline: none;
  border-color: #667eea;
}

.textarea-input {
  min-height: 100px;
  resize: vertical;
}

/* 操作面板样式 */
.action-panel {
  width: 200px;
  background: white;
  padding: 20px;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.action-btn {
  width: 100%;
  padding: 12px;
  margin: 8px 0;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
}

.btn-warning {
  background: #ff9800;
  color: white;
}

.btn-success {
  background: #4caf50;
  color: white;
}

.btn-primary:hover {
  background: #5a6fd8;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-warning:hover {
  background: #e68900;
}

.btn-success:hover {
  background: #45a049;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  background: #cccccc;
  color: #666;
  cursor: not-allowed;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    height: auto;
  }

  .nav-panel {
    width: 100%;
    order: 1;
  }

  .content-area {
    order: 2;
    padding: 15px;
  }

  .action-panel {
    width: 100%;
    order: 3;
    flex-direction: row;
    justify-content: space-around;
    padding: 15px;
  }

  .action-btn {
    flex: 1;
    margin: 0 5px;
  }

  .question-nav {
    grid-template-columns: repeat(6, 1fr);
  }

  .nav-item {
    width: 35px;
    height: 35px;
    font-size: 11px;
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 70px);
  margin-top: 70px;
}

.badge {
  @apply w-7 h-7 flex items-center justify-center text-sm rounded-full cursor-pointer transition-all;
}

.badge.done {
  @apply bg-green-500 text-white;
}

.badge.current {
  @apply bg-blue-500 text-white;
}

.badge.marked {
  @apply bg-yellow-500 text-white;
}

.nav {
  @apply flex-shrink-0 bg-white border-r border-gray-200 p-2;
  flex: 0 0 120px;
}

.content {
  @apply flex-1 p-4 flex flex-col;
}

.actions {
  @apply flex-shrink-0 bg-white border-l border-gray-200 p-4;
  flex: 0 0 160px;
}

.question-box {
  @apply bg-white rounded border border-gray-200 p-4 mb-3 shadow-sm;
}

/* 过渡动画 */
.transition-all {
  transition: all 0.3s ease;
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 输入框获焦时的样式 */
textarea:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 按钮悬停效果 */
button:not(:disabled):hover {
  transform: translateY(-1px);
}

button:not(:disabled):active {
  transform: translateY(0);
}

header {
  background: #1890ff;
  color: #fff;
  padding: 12px 24px;
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav button {
  display: block;
  width: 100%;
  margin-bottom: 6px;
}

label {
  display: block;
  margin: 6px 0;
}
</style>
