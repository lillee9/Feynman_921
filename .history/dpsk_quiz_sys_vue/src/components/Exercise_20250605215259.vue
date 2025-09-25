<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="container mx-auto px-4 py-6 max-w-6xl">
      <!-- 头部信息 -->
      <div class="bg-white rounded-xl shadow-lg p-6 mb-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="flex-1">
            <h1 class="text-3xl font-bold text-gray-800 mb-3">AI练习题</h1>
            <div class="flex flex-wrap items-center gap-3 text-sm">
              <span class="bg-blue-100 text-blue-800 px-4 py-2 rounded-full font-medium">
                {{ getQuestionTypeText(exerciseData.type) }}
              </span>
              <span class="bg-green-100 text-green-800 px-4 py-2 rounded-full font-medium">
                {{ getDifficultyText(exerciseData.difficulty) }}
              </span>
              <span class="bg-purple-100 text-purple-800 px-4 py-2 rounded-full font-medium">
                共 {{ exerciseData.total }} 题
              </span>
            </div>
          </div>
          <div class="text-center md:text-right">
            <div class="text-3xl font-bold text-gray-800 mb-1">
              {{ currentQuestionIndex + 1 }} / {{ questions.length }}
            </div>
            <div class="text-sm text-gray-500">当前题目</div>
          </div>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="bg-white rounded-xl shadow-lg p-6 mb-6">
        <div class="flex items-center justify-between mb-3">
          <span class="text-lg font-semibold text-gray-700">答题进度</span>
          <span class="text-lg font-bold text-blue-600">
            {{ Math.round(((currentQuestionIndex + 1) / questions.length) * 100) }}%
          </span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-3">
          <div 
            class="bg-gradient-to-r from-blue-500 to-purple-600 h-3 rounded-full transition-all duration-500 ease-out"
            :style="{ width: ((currentQuestionIndex + 1) / questions.length) * 100 + '%' }"
          ></div>
        </div>
      </div>

      <!-- 题目区域 -->
      <div v-if="questions.length > 0" class="bg-white rounded-xl shadow-lg mb-6">
        <div class="p-8">
          <!-- 题目标题 -->
          <div class="flex items-center mb-6 pb-4 border-b border-gray-100">
            <div class="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-full flex items-center justify-center font-bold text-lg mr-4 shadow-lg">
              {{ currentQuestionIndex + 1 }}
            </div>
            <div>
              <span class="text-lg font-semibold text-blue-600 block">
                {{ getQuestionTypeText(currentQuestion.type) }}
              </span>
              <span class="text-sm text-gray-500">请仔细阅读题目后作答</span>
            </div>
          </div>

          <!-- 题目内容 -->
          <div class="mb-8">
            <h2 class="text-xl md:text-2xl font-medium text-gray-800 leading-relaxed mb-6">
              {{ currentQuestion.question }}
            </h2>
          </div>

          <!-- 选择题选项 -->
          <div v-if="currentQuestion.type === 'CHOICE'" class="space-y-4">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              class="relative"
            >
              <input
                :id="`option-${index}`"
                v-model="userAnswers[currentQuestionIndex]"
                :value="option.charAt(0)"
                type="radio"
                class="sr-only"
              />
              <label
                :for="`option-${index}`"
                class="flex items-center p-5 border-2 rounded-xl cursor-pointer transition-all duration-300 hover:bg-gray-50 hover:shadow-md"
                :class="{
                  'border-blue-500 bg-blue-50 shadow-md': userAnswers[currentQuestionIndex] === option.charAt(0),
                  'border-gray-200': userAnswers[currentQuestionIndex] !== option.charAt(0)
                }"
              >
                <div
                  class="w-6 h-6 rounded-full border-2 mr-4 flex items-center justify-center transition-all duration-200"
                  :class="{
                    'border-blue-500 bg-blue-500': userAnswers[currentQuestionIndex] === option.charAt(0),
                    'border-gray-300': userAnswers[currentQuestionIndex] !== option.charAt(0)
                  }"
                >
                  <div
                    v-if="userAnswers[currentQuestionIndex] === option.charAt(0)"
                    class="w-3 h-3 bg-white rounded-full"
                  ></div>
                </div>
                <span class="text-gray-800 text-lg">{{ option }}</span>
              </label>
            </div>
          </div>

          <!-- 填空题输入框 -->
          <div v-else-if="currentQuestion.type === 'FILL_BLANK'" class="space-y-4">
            <div class="bg-gray-50 rounded-lg p-4 mb-4">
              <p class="text-sm text-gray-600 mb-2">💡 填空题答题提示：</p>
              <p class="text-sm text-gray-500">请在下方输入框中填写你的答案，注意答案的准确性</p>
            </div>
            <textarea
              v-model="userAnswers[currentQuestionIndex]"
              class="w-full p-5 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:outline-none resize-none text-lg transition-all duration-200 focus:shadow-lg"
              rows="4"
              placeholder="请输入你的答案..."
            ></textarea>
          </div>

          <!-- 简答题输入框 -->
          <div v-else-if="currentQuestion.type === 'SHORT_ANSWER'" class="space-y-4">
            <div class="bg-gray-50 rounded-lg p-4 mb-4">
              <p class="text-sm text-gray-600 mb-2">📝 简答题答题提示：</p>
              <p class="text-sm text-gray-500">请详细回答问题，表达清晰，逻辑完整</p>
            </div>
            <textarea
              v-model="userAnswers[currentQuestionIndex]"
              class="w-full p-5 border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:outline-none resize-none text-lg transition-all duration-200 focus:shadow-lg"
              rows="6"
              placeholder="请详细回答问题..."
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="bg-white rounded-xl shadow-lg p-6">
        <div class="flex flex-col sm:flex-row justify-between items-center gap-4">
          <button
            @click="previousQuestion"
            :disabled="currentQuestionIndex === 0"
            class="flex items-center px-6 py-3 bg-gray-100 text-gray-600 rounded-lg transition-all duration-200 hover:bg-gray-200 hover:shadow-md disabled:opacity-50 disabled:cursor-not-allowed w-full sm:w-auto"
          >
            <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
            上一题
          </button>

          <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
            <button
              v-if="currentQuestionIndex < questions.length - 1"
              @click="nextQuestion"
              :disabled="!userAnswers[currentQuestionIndex]"
              class="flex items-center justify-center px-8 py-3 bg-blue-500 text-white rounded-lg transition-all duration-200 hover:bg-blue-600 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed font-semibold"
            >
              下一题
              <svg class="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </button>
            
            <button
              v-else
              @click="submitAnswers"
              :disabled="!isAllAnswered"
              class="flex items-center justify-center px-8 py-3 bg-green-500 text-white rounded-lg transition-all duration-200 hover:bg-green-600 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed font-semibold"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              提交答案
            </button>
          </div>
        </div>
      </div>

      <!-- 结果展示模态框 -->
      <div v-if="showResults" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div class="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] overflow-hidden flex flex-col">
          <!-- 模态框头部 -->
          <div class="p-6 border-b border-gray-200 bg-gradient-to-r from-blue-50 to-purple-50">
            <div class="flex items-center justify-between">
              <h2 class="text-2xl font-bold text-gray-800">📊 答题结果</h2>
              <button
                @click="closeResults"
                class="text-gray-400 hover:text-gray-600 transition-colors p-2 hover:bg-white rounded-full"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
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
                  <h3 class="font-semibold text-gray-800 text-lg">第{{ index + 1 }}题</h3>
                  <span
                    class="px-4 py-2 rounded-full text-sm font-medium"
                    :class="{
                      'bg-green-100 text-green-800': isAnswerCorrect(index),
                      'bg-red-100 text-red-800': !isAnswerCorrect(index)
                    }"
                  >
                    {{ isAnswerCorrect(index) ? '✅ 正确' : '❌ 错误' }}
                  </span>
                </div>
                
                <p class="text-gray-700 mb-4 text-lg leading-relaxed">{{ question.question }}</p>
                
                <div class="space-y-3 bg-gray-50 rounded-lg p-4">
                  <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                    <span class="text-gray-500 font-medium min-w-20">你的答案:</span>
                    <span
                      class="font-medium"
                      :class="{
                        'text-green-600': isAnswerCorrect(index),
                        'text-red-600': !isAnswerCorrect(index)
                      }"
                    >
                      {{ userAnswers[index] || '未作答' }}
                    </span>
                  </div>
                  <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                    <span class="text-gray-500 font-medium min-w-20">正确答案:</span>
                    <span class="text-green-600 font-semibold">{{ question.answer }}</span>
                  </div>
                  <div v-if="question.explanation" class="flex flex-col gap-2">
                    <span class="text-gray-500 font-medium">解析:</span>
                    <span class="text-gray-700 leading-relaxed">{{ question.explanation }}</span>
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
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const questions = ref([])
const exerciseData = ref({})
const currentQuestionIndex = ref(0)
const userAnswers = ref([])
const showResults = ref(false)

// 计算属性
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || {}
})

const isAllAnswered = computed(() => {
  return userAnswers.value.every(answer => answer && answer.trim())
})

const correctCount = computed(() => {
  return questions.value.filter((question, index) => {
    return isAnswerCorrect(index)
  }).length
})

// 工具函数
const getQuestionTypeText = (type) => {
  const typeMap = {
    'CHOICE': '选择题',
    'FILL_BLANK': '填空题',
    'SHORT_ANSWER': '简答题'
  }
  return typeMap[type] || type
}

const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

const isAnswerCorrect = (index) => {
  const question = questions.value[index]
  const userAnswer = userAnswers.value[index]
  
  if (!question || !userAnswer) return false
  
  // 对于选择题，直接比较选项字母
  if (question.type === 'CHOICE') {
    return userAnswer.trim().toUpperCase() === question.answer.trim().toUpperCase()
  }
  
  // 对于填空题和简答题，进行基本的字符串比较
  return userAnswer.trim().toLowerCase() === question.answer.trim().toLowerCase()
}

// 导航函数
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const submitAnswers = () => {
  if (!isAllAnswered.value) {
    alert('请完成所有题目后再提交')
    return
  }
  showResults.value = true
}

const closeResults = () => {
  showResults.value = false
}

const restartExercise = () => {
  currentQuestionIndex.value = 0
  userAnswers.value = new Array(questions.value.length).fill('')
  showResults.value = false
}

// 初始化
onMounted(() => {
  console.log('🚀 Exercise 页面开始初始化')
  
  try {
    // 从 sessionStorage 中读取数据
    if (route.query.dataKey) {
      console.log('✅ 发现 query.dataKey:', route.query.dataKey)
      
      const storedData = sessionStorage.getItem(route.query.dataKey)
      if (storedData) {
        console.log('✅ 从 sessionStorage 读取到数据')
        
        const data = JSON.parse(storedData)
        console.log('📝 解析后的数据:', data)
        
        questions.value = data.questions || []
        exerciseData.value = {
          type: data.type,
          difficulty: data.difficulty,
          total: data.total
        }
        
        // 初始化用户答案数组
        userAnswers.value = new Array(questions.value.length).fill('')
        
        console.log('✅ 成功加载题目数量:', questions.value.length)
        
        // 清除临时存储
        sessionStorage.removeItem(route.query.dataKey)
        console.log('🗑️ 已清除临时存储')
      } else {
        console.warn('❌ sessionStorage 中没有找到对应数据')
        // 可以考虑重定向到首页或显示错误信息
        router.push('/')
      }
    } else {
      console.warn('❌ 未找到数据键')
      router.push('/')
    }
  } catch (error) {
    console.error('❌ 解析题目数据时出错:', error)
    router.push('/')
  }
})
</script>

<style scoped>
/* 确保容器有正确的宽度和间距 */
.container {
  width: 100%;
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

/* 响应式调整 */
@media (max-width: 640px) {
  .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }
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
</style>