<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 头部 -->
    <header class="header">
      <div class="exam-title">
        考试中 - 《{{ exerciseData.title || 'AI练习题' }}》
        <span class="difficulty-badge" :class="{
          'difficulty-easy': (exerciseData.difficulty || exerciseData.difficultyLevel) === 'EASY',
          'difficulty-medium': (exerciseData.difficulty || exerciseData.difficultyLevel) === 'MEDIUM',
          'difficulty-hard': (exerciseData.difficulty || exerciseData.difficultyLevel) === 'HARD'
        }">
          {{ getDifficultyText(exerciseData.difficulty || exerciseData.difficultyLevel) }}
        </span>
      </div>
      <div class="timer">
        剩余 {{ formatTime(remainingTime) }}
      </div>
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
              'answered': isQuestionAnswered(index) && index !== currentQuestionIndex && !markedQuestions.includes(index),
              'current': index === currentQuestionIndex && !markedQuestions.includes(index),
              'unanswered': !isQuestionAnswered(index) && index !== currentQuestionIndex && !markedQuestions.includes(index),
              'marked': markedQuestions.includes(index)
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
          共 {{ questions.length }} 题，已答 {{ answeredCount }} 题，标记 {{ markedQuestions.length }} 题
        </div>

        <!-- 题目卡片 -->
        <div v-if="questions.length > 0" class="question-card">
          <div class="question-header">
            <div class="question-type">{{ getQuestionTypeText(currentQuestion.type || currentQuestion.questionType) }}</div>
            <div>第{{ currentQuestionIndex + 1 }}题 / 共{{ questions.length }}题</div>
          </div>
          <div class="question-title">
            {{ currentQuestion.questionContent || currentQuestion.question || currentQuestion.questionText || currentQuestion.content || currentQuestion.title || currentQuestion.text || '题目内容加载中...' }}
          </div>
          
          <!-- 调试信息已移除，保留console.log用于开发调试 -->

          <!-- 选择题选项 -->
          <div v-if="(currentQuestion.type || currentQuestion.questionType) === 'CHOICE' || (currentQuestion.type || currentQuestion.questionType) === 'SINGLE_CHOICE'" class="options">
            <!-- 选项渲染调试信息已移除 -->
            
            <div 
              v-for="(option, optionIndex) in getQuestionOptions(currentQuestion)" 
              :key="optionIndex"
              class="option"
              :class="{
                'selected': userAnswers[currentQuestionIndex] === String.fromCharCode(65 + optionIndex)
              }"
              @click="userAnswers[currentQuestionIndex] = String.fromCharCode(65 + optionIndex)"
            >
              <input 
                type="radio" 
                :name="`question-${currentQuestionIndex}`"
                :value="String.fromCharCode(65 + optionIndex)"
                v-model="userAnswers[currentQuestionIndex]"
              >
              <span>{{ String.fromCharCode(65 + optionIndex) }}. {{ option }}</span>
            </div>
          </div>

          <!-- 多选题选项 -->
          <div v-else-if="(currentQuestion.type || currentQuestion.questionType) === 'MULTIPLE_CHOICE' || (currentQuestion.type || currentQuestion.questionType) === 'MULTI_CHOICE'" class="options">
            <!-- 多选题选项渲染调试信息已移除 -->
            
            <div 
              v-for="(option, optionIndex) in getQuestionOptions(currentQuestion)" 
              :key="optionIndex"
              class="option"
              :class="{
                'selected': userAnswers[currentQuestionIndex] && userAnswers[currentQuestionIndex].includes(String.fromCharCode(65 + optionIndex))
              }"
              @click="toggleMultipleChoice(String.fromCharCode(65 + optionIndex))"
            >
              <input 
                type="checkbox" 
                :value="String.fromCharCode(65 + optionIndex)"
                :checked="userAnswers[currentQuestionIndex] && userAnswers[currentQuestionIndex].includes(String.fromCharCode(65 + optionIndex))"
                @change="toggleMultipleChoice(String.fromCharCode(65 + optionIndex))"
              >
              <span>{{ String.fromCharCode(65 + optionIndex) }}. {{ option }}</span>
            </div>
          </div>
          
          <!-- 选项为空时的提示 -->
          <div v-if="((currentQuestion.type || currentQuestion.questionType) === 'CHOICE' || (currentQuestion.type || currentQuestion.questionType) === 'SINGLE_CHOICE' || (currentQuestion.type || currentQuestion.questionType) === 'MULTIPLE_CHOICE' || (currentQuestion.type || currentQuestion.questionType) === 'MULTI_CHOICE') && getQuestionOptions(currentQuestion).length === 0" class="no-options">
            <div class="text-center text-gray-500 py-4">
              <p>❌ 选项数据为空</p>
              <p class="text-sm">请联系管理员检查题目配置</p>
            </div>
          </div>

          <!-- 填空题输入框 -->
          <div v-else-if="(currentQuestion.type || currentQuestion.questionType) === 'FILL_BLANK'">
            <input
              v-model="userAnswers[currentQuestionIndex]"
              type="text"
              class="text-input"
              placeholder="请输入填空答案..."
            />
          </div>

          <!-- 简答题输入框 -->
          <div v-else-if="(currentQuestion.type || currentQuestion.questionType) === 'SHORT_ANSWER'">
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
        <button
           @click="toggleMark"
           class="action-btn btn-warning"
         >
           {{ markedQuestions.includes(currentQuestionIndex) ? '取消标记' : '标记题目' }}
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

        <button
           @click="submitAnswers"
           class="action-btn btn-success"
         >
           提交答卷
         </button>
      </div>
    </div>

    <!-- 结果展示模态框 -->
      <div v-if="showResults" class="fixed inset-0 bg-gray-900 bg-opacity-30 flex items-center justify-center p-4 z-50">
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
                
                <p class="text-gray-700 mb-4 text-lg leading-relaxed">{{ question.questionContent || question.question }}</p>
                
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
                    <span class="text-green-600 font-semibold">{{ question.correctAnswer || question.answer }}</span>
                  </div>
                  <div v-if="question.analysis || question.explanation" class="flex flex-col gap-2">
                    <span class="text-gray-500 font-medium">解析:</span>
                    <span class="text-gray-700 leading-relaxed">{{ question.analysis || question.explanation }}</span>
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
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_BASE_URL } from '../config/api'
import { getToken, requireAuth, authFetch } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 认证检查函数
const checkAuth = () => {
  const token = getToken()
  if (!token) {
    ElMessage.error('请先登录')
    router.push('/login')
    return false
  }
  
  // 检查userStore是否正确初始化
  if (!userStore.id || userStore.id === 0) {
    console.warn('⚠️ userStore未正确初始化，尝试从token解析用户信息')
    // 这里可以添加从token解析用户信息的逻辑
    // 或者提示用户重新登录
    ElMessage.warning('用户信息异常，请重新登录')
    router.push('/login')
    return false
  }
  
  console.log('✅ 用户认证检查通过，用户ID:', userStore.id)
  return true
}

// 响应式数据
const questions = ref([])
const exerciseData = ref({})
const currentQuestionIndex = ref(0)
const userAnswers = ref([])
const showResults = ref(false)
const markedQuestions = ref([])
const remainingTime = ref(4800) // 默认80分钟 = 4800秒，将根据传入的考试时长动态设置
const timer = ref(null)
const isRetakeMode = ref(false) // 标记是否为重新考试模式，避免重复提醒

// 计算属性
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value] || {}
})

const isAllAnswered = computed(() => {
  return userAnswers.value.every(answer => {
    if (Array.isArray(answer)) {
      return answer.length > 0
    }
    return answer && typeof answer === 'string' && answer.trim() !== ''
  })
})

const correctCount = computed(() => {
  return questions.value.filter((question, index) => {
    return isAnswerCorrect(index)
  }).length
})

const answeredCount = computed(() => {
  return userAnswers.value.filter(answer => {
    if (Array.isArray(answer)) {
      return answer.length > 0
    }
    return answer && typeof answer === 'string' && answer.trim() !== ''
  }).length
})

// 工具函数
const getQuestionTypeText = (type) => {
  // 如果type为空，直接返回未知题型
  if (!type) {
    console.warn('⚠️ getQuestionTypeText: 题目类型为空')
    return '未知题型'
  }
  
  // 记录原始类型值用于调试
  const originalType = type
  
  // 尝试标准化类型值（转为大写并去除空格）
  if (typeof type === 'string') {
    type = type.toUpperCase().trim()
  }
  
  // 扩展类型映射表，包含更多可能的类型值
  const typeMap = {
    // 标准类型
    'SINGLE_CHOICE': '单选题',
    'CHOICE': '单选题', // 兼容旧格式
    'MULTI_CHOICE': '多选题',
    'MULTIPLE_CHOICE': '多选题', // 兼容旧格式
    'FILL_BLANK': '填空题',
    'SHORT_ANSWER': '简答题',
    'JUDGE': '判断题',
    
    // 数字类型映射
    '0': '单选题',
    '1': '多选题',
    '2': '填空题',
    '3': '简答题',
    '4': '判断题',
    
    // 小写类型映射
    'single_choice': '单选题',
    'choice': '单选题',
    'multi_choice': '多选题',
    'multiple_choice': '多选题',
    'fill_blank': '填空题',
    'short_answer': '简答题',
    'judge': '判断题',
    
    // 其他可能的类型值
    'SINGLE': '单选题',
    'MULTIPLE': '多选题',
    'FILL': '填空题',
    'FILLBLANK': '填空题',
    'SHORT': '简答题',
    'ESSAY': '简答题',
    'TRUE_FALSE': '判断题',
    'TRUEFALSE': '判断题',
    'TF': '判断题'
  }
  
  // 尝试直接映射
  const mappedType = typeMap[type]
  if (mappedType) {
    return mappedType
  }
  
  // 如果直接映射失败，尝试部分匹配
  if (typeof type === 'string') {
    if (type.includes('SINGLE') || type.includes('CHOICE') && !type.includes('MULTI')) {
      return '单选题'
    }
    if (type.includes('MULTI') || type.includes('MULTIPLE')) {
      return '多选题'
    }
    if (type.includes('FILL') || type.includes('BLANK')) {
      return '填空题'
    }
    if (type.includes('SHORT') || type.includes('ANSWER') || type.includes('ESSAY')) {
      return '简答题'
    }
    if (type.includes('JUDGE') || type.includes('TRUE') || type.includes('FALSE')) {
      return '判断题'
    }
  }
  
  // 如果所有尝试都失败，记录详细日志并返回未知题型
  console.warn(`⚠️ getQuestionTypeText: 未能识别的题目类型 "${originalType}"(${typeof originalType})`)
  return '未知题型'
}

const getDifficultyText = (difficulty) => {
  const difficultyMap = {
    'EASY': '简单',
    'MEDIUM': '中等', 
    'HARD': '困难',
    // 支持后端返回的小写格式
    'easy': '简单',
    'medium': '中等',
    'hard': '困难'
  }
  return difficultyMap[difficulty] || difficulty
}

const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const isQuestionAnswered = (index) => {
  const answer = userAnswers.value[index]
  if (Array.isArray(answer)) {
    return answer.length > 0
  }
  return answer && typeof answer === 'string' && answer.trim() !== ''
}

const goToQuestion = (index) => {
  currentQuestionIndex.value = index
}

const saveAnswer = () => {
  // 答案已经通过v-model自动保存，这里可以添加提示
  console.log('答案已暂存')
}

const toggleMark = () => {
  const index = currentQuestionIndex.value
  const markIndex = markedQuestions.value.indexOf(index)
  if (markIndex > -1) {
    markedQuestions.value.splice(markIndex, 1)
  } else {
    markedQuestions.value.push(index)
  }
}

// 处理多选题选择，确保不会产生重复值
const toggleMultipleChoice = (optionValue) => {
  const currentAnswers = userAnswers.value[currentQuestionIndex.value] || []
  const answerIndex = currentAnswers.indexOf(optionValue)
  
  if (answerIndex > -1) {
    // 如果已选中，则移除
    currentAnswers.splice(answerIndex, 1)
  } else {
    // 如果未选中，则添加
    currentAnswers.push(optionValue)
  }
  
  // 确保答案数组的唯一性
  userAnswers.value[currentQuestionIndex.value] = [...new Set(currentAnswers)]
  
  console.log('多选题答案更新:', {
    questionIndex: currentQuestionIndex.value,
    optionValue: optionValue,
    currentAnswers: userAnswers.value[currentQuestionIndex.value]
  })
}

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer.value)
      // 时间到自动提交
      submitAnswers()
    }
  }, 1000)
}

const stopTimer = () => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
}

const isAnswerCorrect = (index) => {
  const question = questions.value[index]
  const userAnswer = userAnswers.value[index]
  
  console.log(`🔍 判分调试 - 题目 ${index + 1}:`, {
    questionId: question?.questionId,
    questionType: question?.questionType || question?.type,
    userAnswer: userAnswer,
    userAnswerType: typeof userAnswer,
    correctAnswer: question?.correctAnswer,
    correctAnswerType: typeof question?.correctAnswer,
    correctAnswerIsArray: Array.isArray(question?.correctAnswer),
    analysis: question?.analysis,
    allQuestionFields: question ? Object.keys(question) : 'question为空'
  })
  
  if (!question || userAnswer === null || userAnswer === undefined || userAnswer === '') {
    console.log(`❌ 判分失败 - 题目 ${index + 1}: 题目或用户答案为空`, {
      question: !!question,
      userAnswer: userAnswer,
      userAnswerType: typeof userAnswer
    })
    return false
  }
  
  // 修复字段名匹配问题：使用后端返回的correctAnswer字段
  const correctAnswer = question.correctAnswer
  
  if (!correctAnswer && correctAnswer !== 0 && correctAnswer !== false) {
    console.log(`❌ 判分失败 - 题目 ${index + 1}: 没有正确答案数据`, {
      correctAnswer: correctAnswer,
      correctAnswerType: typeof correctAnswer
    })
    return false
  }
  
  console.log(`🎯 判分数据详细分析 - 题目 ${index + 1}:`, {
    用户答案: userAnswer,
    用户答案类型: typeof userAnswer,
    用户答案是否为数组: Array.isArray(userAnswer),
    正确答案: correctAnswer,
    正确答案类型: typeof correctAnswer,
    正确答案是否为数组: Array.isArray(correctAnswer),
    正确答案长度: correctAnswer?.length,
    题目类型: question.questionType || question.type
  })
  
  // 统一使用questionType字段（后端标准字段）
  const questionType = question.questionType || question.type
  
  // 对于选择题，直接比较选项字母
  if (questionType === 'SINGLE_CHOICE') {
    // 确保两个答案都转换为字符串进行比较
    const userAnswerStr = String(userAnswer).trim().toUpperCase()
    const correctAnswerStr = String(correctAnswer).trim().toUpperCase()
    const result = userAnswerStr === correctAnswerStr
    console.log(`✅ 单选题判分详细结果 - 题目 ${index + 1}:`, {
      result: result,
      userAnswerOriginal: userAnswer,
      userAnswerStr: userAnswerStr,
      correctAnswerOriginal: correctAnswer,
      correctAnswerStr: correctAnswerStr,
      comparison: `'${userAnswerStr}' === '${correctAnswerStr}'`
    })
    return result
  }
  
  // 对于多选题，比较选中的选项数组
  if (questionType === 'MULTI_CHOICE') {
    if (Array.isArray(userAnswer)) {
      const userAnswerSorted = userAnswer.map(a => String(a).trim().toUpperCase()).sort().join('')
      
      // 处理后端返回的正确答案格式
      let correctAnswerArray = []
      const correctAnswerStr = String(correctAnswer)
      
      if (correctAnswerStr.includes(',')) {
        // 逗号分隔的多个答案
        correctAnswerArray = correctAnswerStr.split(',').map(a => a.trim().toUpperCase())
      } else if (correctAnswerStr.length > 1 && /^[A-Z]+$/.test(correctAnswerStr.toUpperCase())) {
        // 连续字母如"AC"
        correctAnswerArray = correctAnswerStr.toUpperCase().split('')
      } else {
        // 单个答案
        correctAnswerArray = [correctAnswerStr.trim().toUpperCase()]
      }
      
      const correctAnswerSorted = correctAnswerArray.sort().join('')
      const result = userAnswerSorted === correctAnswerSorted
      
      console.log(`✅ 多选题判分详细结果 - 题目 ${index + 1}:`, {
        result: result,
        userAnswerOriginal: userAnswer,
        userAnswerSorted: userAnswerSorted,
        correctAnswerOriginal: correctAnswer,
        correctAnswerStr: correctAnswerStr,
        correctAnswerArray: correctAnswerArray,
        correctAnswerSorted: correctAnswerSorted,
        comparison: `'${userAnswerSorted}' === '${correctAnswerSorted}'`
      })
      return result
    }
    console.log(`❌ 多选题判分失败 - 题目 ${index + 1}: 用户答案不是数组`, {
      userAnswer: userAnswer,
      userAnswerType: typeof userAnswer
    })
    return false
  }
  
  // 对于判断题
  if (questionType === 'JUDGE') {
    // 确保两个答案都转换为字符串进行比较
    const userAnswerStr = String(userAnswer).trim().toUpperCase()
    const correctAnswerStr = String(correctAnswer).trim().toUpperCase()
    const result = userAnswerStr === correctAnswerStr
    console.log(`✅ 判断题判分详细结果 - 题目 ${index + 1}:`, {
      result: result,
      userAnswerOriginal: userAnswer,
      userAnswerStr: userAnswerStr,
      correctAnswerOriginal: correctAnswer,
      correctAnswerStr: correctAnswerStr,
      comparison: `'${userAnswerStr}' === '${correctAnswerStr}'`
    })
    return result
  }
  
  // 对于填空题，进行精确的字符串比较
  if (questionType === 'FILL_BLANK') {
    const userAnswerStr = String(userAnswer).trim().toLowerCase()
    const correctAnswerStr = String(correctAnswer).trim().toLowerCase()
    const result = userAnswerStr === correctAnswerStr
    
    console.log(`✅ 填空题判分详细结果 - 题目 ${index + 1}:`, {
      result: result,
      userAnswerOriginal: userAnswer,
      userAnswerStr: userAnswerStr,
      correctAnswerOriginal: correctAnswer,
      correctAnswerStr: correctAnswerStr,
      comparison: `'${userAnswerStr}' === '${correctAnswerStr}'`
    })
    return result
  }
  
  // 对于简答题，检查关键词匹配（如果有keyPoints）或进行基本比较
  if (questionType === 'SHORT_ANSWER') {
    if (question.keyPoints && Array.isArray(question.keyPoints)) {
      // 检查用户答案是否包含关键要点
      const userAnswerStr = String(userAnswer).trim().toLowerCase()
      const result = question.keyPoints.some(point => 
        userAnswerStr.includes(String(point).toLowerCase())
      )
      console.log(`✅ 简答题判分详细结果 - 题目 ${index + 1}:`, {
        result: result,
        method: '关键词匹配',
        userAnswerOriginal: userAnswer,
        userAnswerStr: userAnswerStr,
        keyPoints: question.keyPoints
      })
      return result
    }
    // 如果没有关键要点，进行基本的字符串比较
    const userAnswerStr = String(userAnswer).trim().toLowerCase()
    const correctAnswerStr = String(correctAnswer).trim().toLowerCase()
    const result = userAnswerStr.includes(correctAnswerStr)
    
    console.log(`✅ 简答题判分详细结果 - 题目 ${index + 1}:`, {
      result: result,
      method: '字符串包含',
      userAnswerOriginal: userAnswer,
      userAnswerStr: userAnswerStr,
      correctAnswerOriginal: correctAnswer,
      correctAnswerStr: correctAnswerStr,
      comparison: `'${userAnswerStr}'.includes('${correctAnswerStr}')`
    })
    return result
  }
  
  console.log(`❌ 未知题型判分失败 - 题目 ${index + 1}: ${questionType}`)
  return false
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

const submitAnswers = async () => {
  if (!isAllAnswered.value) {
    alert('请完成所有题目后再提交')
    return
  }
  
  // 保存答题记录到数据库
  await saveAnswersToDatabase()
  
  showResults.value = true
}

const closeResults = () => {
  showResults.value = false
}

const restartExercise = async () => {
  // 询问用户是否确认重新开始考试
  try {
    await ElMessageBox.confirm(
      '重新开始考试将清除当前答题记录并删除之前提交的考试记录。确定要重新开始吗？',
      '重新开始考试确认',
      {
        confirmButtonText: '确认重新开始',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )
    
    // 用户确认后，尝试删除之前的考试记录
    if (userStore.id && exerciseData.value.examPaperId) {
      try {
        // 首先获取考试结果以获得resultId
        const examResultResponse = await authFetch(`${API_BASE_URL}/exam/results/user/${userStore.id}/paper/${exerciseData.value.examPaperId}`, {
          method: 'GET'
        }, router)
        
        if (examResultResponse.code === 200 && examResultResponse.data && examResultResponse.data.length > 0) {
          const resultId = examResultResponse.data[0].resultId
          
          // 使用正确的删除API
          const deleteResult = await authFetch(`${API_BASE_URL}/exam/results/${resultId}`, {
            method: 'DELETE'
          }, router)
          
          if (deleteResult.code === 200) {
            console.log('✅ 成功删除之前的考试记录')
          } else {
            console.warn('⚠️ 删除之前的考试记录失败，但继续重新开始:', deleteResult.message)
          }
        } else {
          console.warn('⚠️ 未找到要删除的考试记录，但继续重新开始')
        }
      } catch (deleteError) {
        console.warn('⚠️ 删除之前的考试记录时发生异常，但继续重新开始:', deleteError)
      }
    }
    
    // 重置考试状态
    currentQuestionIndex.value = 0
    userAnswers.value = questions.value.map(q => {
      const questionType = q.type || q.questionType || ''
      // 多选题初始化为空数组
      if (questionType === 'MULTIPLE_CHOICE' || questionType === 'MULTI_CHOICE' || 
          questionType === 'CHECKBOX' || questionType === 'MULTI_SELECT') {
        return []
      }
      // 其他题目类型（单选、填空、简答）初始化为空字符串
      return ''
    })
    showResults.value = false
    markedQuestions.value = []
    
    // 重置计时器
    stopTimer()
    const durationInMinutes = exerciseData.value.examDuration || 80
    remainingTime.value = durationInMinutes * 60
    startTimer()
    
    ElMessage.success('考试已重新开始！')
    
  } catch (error) {
    // 用户取消或其他错误
    if (error !== 'cancel') {
      console.error('重新开始考试时发生错误:', error)
      ElMessage.error('重新开始考试失败，请稍后重试')
    }
  }
}

// 检查用户是否已参加过该试卷考试
const checkExamStatus = async (paperId, userId) => {
  if (!paperId || !userId) {
    console.log('📋 跳过考试状态检查：缺少必要参数')
    return { hasParticipated: false }
  }
  
  try {
    console.log('🔍 检查考试状态 - paperId:', paperId, 'userId:', userId)
    
    // 使用正确的GET请求和路径参数格式
    const result = await authFetch(`${API_BASE_URL}/exam/results/check/user/${userId}/paper/${paperId}`, {
      method: 'GET'
    }, router)
    
    console.log('📊 考试状态检查结果:', result)
    
    if (result.code === 200) {
      // 后端返回的是布尔值，需要适配前端的数据结构
      const hasParticipated = result.data === true
      
      if (hasParticipated) {
        // 如果用户已参加，获取考试结果详情
        try {
          const examResultResponse = await authFetch(`${API_BASE_URL}/exam/results/user/${userId}/paper/${paperId}`, {
            method: 'GET'
          }, router)
          
          if (examResultResponse.code === 200 && examResultResponse.data && examResultResponse.data.length > 0) {
            const examResult = examResultResponse.data[0]
            return {
              hasParticipated: true,
              examResult: examResult,
              submitTime: examResult.submitTime
            }
          }
        } catch (detailError) {
          console.warn('⚠️ 获取考试结果详情失败:', detailError)
        }
      }
      
      return {
        hasParticipated: hasParticipated,
        examResult: null,
        submitTime: null
      }
    } else {
      console.warn('⚠️ 考试状态检查失败:', result.message)
      return { hasParticipated: false }
    }
  } catch (error) {
    console.error('❌ 考试状态检查异常:', error)
    return { hasParticipated: false }
  }
}

// 处理已参加考试的情况
const handleExistingExam = async (examStatus, paperId) => {
  try {
    const action = await ElMessageBox.confirm(
      `您已经参加过该试卷考试！\n\n提交时间: ${examStatus.submitTime ? new Date(examStatus.submitTime).toLocaleString() : '未知'}\n\n您可以选择：\n• 查看之前的考试结果\n• 重新参加考试（将覆盖之前的记录）`,
      '发现已有考试记录',
      {
        confirmButtonText: '重新参加考试',
        cancelButtonText: '查看考试结果',
        distinguishCancelAndClose: true,
        type: 'warning',
        center: true
      }
    )
    
    // 用户选择重新参加考试
    if (action === 'confirm') {
      // 直接重置考试状态，不再尝试删除记录
      console.log('🔄 用户选择重新参加考试，直接重置状态')
      
      // 设置重新考试模式标记，避免提交时重复提醒
      isRetakeMode.value = true
      
      // 重置考试状态（参考restartExam方法的逻辑）
      currentQuestionIndex.value = 0
      userAnswers.value = questions.value.map(q => {
        const questionType = q.type || q.questionType || ''
        // 多选题初始化为空数组
        if (questionType === 'MULTIPLE_CHOICE' || questionType === 'MULTI_CHOICE' || 
            questionType === 'CHECKBOX' || questionType === 'MULTI_SELECT') {
          return []
        }
        // 其他题目类型（单选、填空、简答）初始化为空字符串
        return ''
      })
      showResults.value = false
      markedQuestions.value = []
      
      // 重置计时器
      stopTimer()
      const durationInMinutes = exerciseData.value.examDuration || 80
      remainingTime.value = durationInMinutes * 60
      startTimer()
      
      ElMessage.success('考试已重新开始！提交时将覆盖之前的记录。')
      return { allowContinue: true }
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户选择查看考试结果，跳转到结果页面
      ElMessage.info('正在跳转到考试结果页面...')
      router.push(`/exam-result/${paperId}`)
      return { allowContinue: false }
    } else if (error === 'close') {
      // 用户关闭对话框，返回上一页
      router.back()
      return { allowContinue: false }
    } else {
      console.error('处理已有考试记录时发生错误:', error)
      ElMessage.error('处理考试记录时发生错误')
      return { allowContinue: false }
    }
  }
}

// 从后端API获取试卷数据
const loadPaperData = async (paperId) => {
  if (!checkAuth()) return
  
  try {
    // 首先获取试卷基本信息
    const paperResult = await authFetch(`${API_BASE_URL}/exam/papers/${paperId}`, {
      method: 'GET'
    }, router)
    
    console.log('📋 试卷基本信息API响应:', paperResult)
    console.log('📋 试卷数据详细结构:', paperResult.data)
    
    if (paperResult.code !== 200) {
      throw new Error(paperResult.message || '获取试卷基本信息失败')
    }
    
    // 然后获取试卷题目列表（练习模式需要显示正确答案和解析）
    const questionsResult = await authFetch(`${API_BASE_URL}/exam-question/paper/${paperId}?showAnswer=true`, {
      method: 'GET'
    }, router)
    
    console.log('📝 题目列表API响应:', questionsResult)
    console.log('📝 题目数据详细结构:', questionsResult.data)
    
    if (questionsResult.code === 200 && Array.isArray(questionsResult.data)) {
      console.log('📝 题目数量:', questionsResult.data.length)
      
      // 详细分析每个题目的数据结构
      questionsResult.data.forEach((question, index) => {
        console.log(`\n🔍 题目 ${index + 1} 详细分析:`)
        console.log('  - 题目ID:', question.questionId || question.id)
        console.log('  - 题目类型:', question.questionType)
        console.log('  - 题目内容:', question.questionText)
        console.log('  - questionOptions字段:', question.questionOptions)
        console.log('  - questionOptions类型:', typeof question.questionOptions)
        
        if (question.questionOptions && typeof question.questionOptions === 'object') {
          console.log('  - questionOptions详细内容:', question.questionOptions)
          console.log('  - questionOptions.options:', question.questionOptions.options)
          console.log('  - questionOptions.options类型:', typeof question.questionOptions.options)
          console.log('  - questionOptions.options是否为数组:', Array.isArray(question.questionOptions.options))
          
          if (Array.isArray(question.questionOptions.options)) {
            console.log('  - 选项数量:', question.questionOptions.options.length)
            console.log('  - 选项内容:', question.questionOptions.options)
          }
        }
        
        console.log('  - 所有字段:', Object.keys(question))
        
        // 详细调试correctAnswer字段
        console.log('\n  🎯 correctAnswer字段详细分析:')
        console.log('    - correctAnswer值:', question.correctAnswer)
        console.log('    - correctAnswer类型:', typeof question.correctAnswer)
        console.log('    - correctAnswer是否为数组:', Array.isArray(question.correctAnswer))
        
        if (question.correctAnswer !== null && question.correctAnswer !== undefined) {
          const correctAnswerStr = String(question.correctAnswer)
          console.log('    - correctAnswer字符串形式:', correctAnswerStr)
          console.log('    - correctAnswer长度:', correctAnswerStr.length)
          console.log('    - 是否包含逗号:', correctAnswerStr.includes(','))
          
          if (correctAnswerStr.includes(',')) {
            const splitAnswers = correctAnswerStr.split(',')
            console.log('    - 逗号分隔后的答案:', splitAnswers)
            console.log('    - 分隔后的答案数量:', splitAnswers.length)
          }
          
          // 分析答案格式
          if (/^[A-Z]$/.test(correctAnswerStr.trim())) {
            console.log('    - 答案格式: 单个选项字母')
          } else if (/^[A-Z,\s]+$/.test(correctAnswerStr.trim())) {
            console.log('    - 答案格式: 多个选项字母（逗号分隔）')
          } else {
            console.log('    - 答案格式: 文本内容或其他格式')
          }
        } else {
          console.log('    - ⚠️ correctAnswer为空或未定义')
        }
        
        // 检查题目类型映射
        const mappedType = getQuestionTypeText(question.questionType)
        console.log('  - 题目类型映射结果:', mappedType)
        if (mappedType === '未知题型') {
          console.warn(`  ⚠️ 题目 ${index + 1} 类型映射失败! 原始类型: "${question.questionType}"`)
        }
      })
    }
    
    // 合并试卷信息和题目数据
    const paperData = paperResult.data
    const questions = questionsResult.code === 200 ? questionsResult.data : []
    
    console.log('📊 最终返回的数据结构:')
    console.log('  - 试卷ID:', paperData.paperId)
    console.log('  - 试卷名称:', paperData.paperName)
    console.log('  - 试卷难度:', paperData.difficulty)
    console.log('  - 考试时长:', paperData.duration)
    console.log('  - 题目数量:', questions.length)
    
    return {
      paperId: paperData.paperId,
      paperName: paperData.paperName,
      description: paperData.description,
      difficulty: paperData.difficulty,
      duration: paperData.duration,
      totalQuestion: questions.length || paperData.totalQuestion,
      questions: questions
    }
    
  } catch (error) {
    console.error('❌ 加载试卷数据失败:', error)
    throw error
  }
}

// 初始化
onMounted(async () => {
  console.log('🚀 Exercise 页面开始初始化')
  
  try {
    // 优先从URL参数获取paperId
    if (route.query.paperId) {
      console.log('✅ 发现 query.paperId:', route.query.paperId)
      
      // 检查用户是否已参加过该试卷考试
      if (userStore.id) {
        const examStatus = await checkExamStatus(route.query.paperId, userStore.id)
        
        if (examStatus.hasParticipated) {
          console.log('⚠️ 用户已参加过该试卷考试')
          const handleResult = await handleExistingExam(examStatus, route.query.paperId)
          
          if (!handleResult.allowContinue) {
            console.log('🚫 用户选择不继续考试，停止初始化')
            return
          }
        }
      }
      
      const paperData = await loadPaperData(route.query.paperId)
      console.log('📝 从API获取的数据:', paperData)
      
      questions.value = paperData.questions || []
      exerciseData.value = {
        title: paperData.paperName || '考试试卷',
        type: paperData.type,
        difficulty: paperData.difficulty,
        total: paperData.totalQuestion || paperData.questions?.length || 0,
        examDuration: paperData.duration || 80,
        examPaperId: paperData.paperId
      }
      
      // 设置考试时长（将分钟转换为秒）
      const durationInMinutes = paperData.duration || 80
      remainingTime.value = durationInMinutes * 60
      
      // 初始化用户答案数组
      userAnswers.value = questions.value.map(q => {
        const questionType = q.type || q.questionType || ''
        // 多选题初始化为空数组
        if (questionType === 'MULTIPLE_CHOICE' || questionType === 'MULTI_CHOICE' || 
            questionType === 'CHECKBOX' || questionType === 'MULTI_SELECT') {
          return []
        }
        // 其他题目类型（单选、填空、简答）初始化为空字符串
        return ''
      })
      
      console.log('✅ 成功加载题目数量:', questions.value.length)
      startTimer()
      
    } else if (route.query.dataKey) {
      // 兼容旧的sessionStorage方式
      console.log('✅ 发现 query.dataKey:', route.query.dataKey)
      
      const storedData = sessionStorage.getItem(route.query.dataKey)
      if (storedData) {
        console.log('✅ 从 sessionStorage 读取到数据')
        
        const data = JSON.parse(storedData)
        console.log('📝 解析后的数据:', data)
        
        // 检查考试状态（如果有试卷ID）
        if (data.examPaperId) {
          console.log('🔍 检查考试状态，试卷ID:', data.examPaperId)
          const examStatus = await checkExamStatus(data.examPaperId)
          if (examStatus.hasParticipated) {
            console.log('⚠️ 用户已参加过该试卷考试')
            const shouldContinue = await handleExistingExam(examStatus)
            if (!shouldContinue) {
              console.log('🚫 用户选择不继续，终止初始化')
              return
            }
          }
        }
        
        questions.value = data.questions || []
        
        // 详细调试questions数组
        console.log('📚 题目数据详细调试:')
        console.log('  - data.questions原始数据:', data.questions)
        console.log('  - data.questions类型:', typeof data.questions)
        console.log('  - data.questions是否为数组:', Array.isArray(data.questions))
        console.log('  - questions.value:', questions.value)
        console.log('  - questions.value类型:', typeof questions.value)
        console.log('  - questions.value是否为数组:', Array.isArray(questions.value))
        console.log('  - questions.value长度:', questions.value.length)
        
        if (questions.value.length > 0) {
          console.log('  - 第一题数据:', questions.value[0])
          console.log('  - 第一题字段:', Object.keys(questions.value[0]))
          console.log('  - 第一题ID:', questions.value[0].id)
          console.log('  - 第一题内容:', questions.value[0].content || questions.value[0].questionText)
          
          // 详细验证题目数据结构
        console.log('\n🔍 题目数据结构验证:')
        questions.value.forEach((question, index) => {
          console.log(`\n  题目 ${index + 1}:`, {
            id: question.id,
            type: question.type,
            questionType: question.questionType,
            typeFromAPI: question.type || question.questionType,
            content: question.content || question.questionText,
            hasOptions: !!(question.options || question.questionOptions || question.choices || question.optionA),
            optionFields: {
              options: !!question.options,
              questionOptions: !!question.questionOptions,
              choices: !!question.choices,
              optionA: !!question.optionA,
              optionB: !!question.optionB,
              optionC: !!question.optionC,
              optionD: !!question.optionD,
              optionList: !!question.optionList
            },
            optionsCount: getQuestionOptions(question).length
          })
          
          // 专门调试题目类型映射
          const actualType = question.type || question.questionType
          const mappedType = getQuestionTypeText(actualType)
          console.log(`  🏷️ 题目 ${index + 1} 类型映射:`, {
            原始类型: actualType,
            映射结果: mappedType,
            是否未知: mappedType === '未知题型'
          })
          
          if (mappedType === '未知题型') {
            console.warn(`  ⚠️ 题目 ${index + 1} 类型映射失败!`)
            console.warn(`    - 原始type值: "${actualType}"`)
            console.warn(`    - type字段类型: ${typeof actualType}`)
            console.warn(`    - 所有可能的类型字段:`, {
              type: question.type,
              questionType: question.questionType,
              typeId: question.typeId,
              category: question.category
            })
          }
            
            // 详细分析选项数据结构
            console.log(`\n  📋 题目 ${index + 1} 选项数据详细分析:`)
            console.log('    - 原始题目对象:', question)
            console.log('    - questionOptions字段:', question.questionOptions)
            console.log('    - questionOptions类型:', typeof question.questionOptions)
            if (question.questionOptions && typeof question.questionOptions === 'object') {
              console.log('    - questionOptions.options:', question.questionOptions.options)
              console.log('    - questionOptions.options类型:', typeof question.questionOptions.options)
              console.log('    - questionOptions.options是否为数组:', Array.isArray(question.questionOptions.options))
              if (Array.isArray(question.questionOptions.options)) {
                console.log('    - questionOptions.options内容:', question.questionOptions.options)
                console.log('    - questionOptions.options长度:', question.questionOptions.options.length)
              }
            }
            
            // 检查选项数据完整性
            const options = getQuestionOptions(question)
            if (options.length === 0) {
              console.warn(`  ⚠️ 题目 ${index + 1} 没有选项数据!`)
              console.log('    - 所有可能的选项字段检查:')
              console.log('      - options:', question.options)
              console.log('      - questionOptions:', question.questionOptions)
              console.log('      - choices:', question.choices)
              console.log('      - optionList:', question.optionList)
              console.log('      - optionA:', question.optionA)
              console.log('      - optionB:', question.optionB)
              console.log('      - optionC:', question.optionC)
              console.log('      - optionD:', question.optionD)
            } else {
              console.log(`  ✅ 题目 ${index + 1} 有 ${options.length} 个选项:`, options)
            }
          })
        } else {
          console.log('  - ❌ 题目数组为空!')
        }
        
        exerciseData.value = {
          type: data.type,
          difficulty: data.difficulty,
          total: data.total,
          examDuration: data.examDuration || 80, // 考试时长，默认80分钟
          examPaperId: data.examPaperId // 确保传递试卷ID
        }
        
        console.log('🔍 调试 - 设置后的exerciseData:', exerciseData.value)
        
        // 设置考试时长（将分钟转换为秒）
        const durationInMinutes = data.examDuration || 80
        remainingTime.value = durationInMinutes * 60
        console.log('⏰ 设置考试时长:', durationInMinutes, '分钟 (', remainingTime.value, '秒)')
        
        // 初始化用户答案数组，根据题目类型正确初始化
        userAnswers.value = questions.value.map(q => {
          const questionType = q.type || q.questionType || ''
          // 多选题初始化为空数组
          if (questionType === 'MULTIPLE_CHOICE' || questionType === 'MULTI_CHOICE' || 
              questionType === 'CHECKBOX' || questionType === 'MULTI_SELECT') {
            return []
          }
          // 其他题目类型（单选、填空、简答）初始化为空字符串
          return ''
        })
        
        console.log('🎯 用户答案数组初始化完成:', userAnswers.value)
        
        console.log('✅ 成功加载题目数量:', questions.value.length)
        
        // 详细检查每个题目的选项数据
        console.log('🔍 详细检查每个题目的选项数据:')
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
            allFields: Object.keys(question)
          })
          
          const options = getQuestionOptions(question)
          console.log(`  🎯 getQuestionOptions返回:`, options)
          console.log(`  📊 选项数量: ${options.length}`)
          
          if (options.length === 0 && (question.type === 'CHOICE' || question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE' || question.type === 'MULTI_CHOICE')) {
            console.error(`  ❌ 选择题没有选项数据! 题目${index + 1}`)
          }
        })
        
        // 如果题目数组为空，显示警告
        if (questions.value.length === 0) {
          console.warn('⚠️ 警告：题目数组为空，可能导致页面显示问题')
          ElMessage.warning('未找到题目数据，请检查试卷配置')
        }
        
        // 启动计时器
        startTimer()
        
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

// 获取题目选项
const getQuestionOptions = (question) => {
  if (!question) {
    console.log('🔍 getQuestionOptions: 题目对象为空')
    return []
  }
  
  console.log('🔍 getQuestionOptions 调试:')
  console.log('  - 题目ID:', question.questionId || question.id)
  console.log('  - 题目类型:', question.questionType || question.type)
  console.log('  - 题目所有字段:', Object.keys(question))
  console.log('  - questionOptions字段:', question.questionOptions)
  console.log('  - questionOptions类型:', typeof question.questionOptions)
  
  // 优先处理后端返回的questionOptions字段（Map<String, Object>格式）
  // 根据后端convertToResponse方法，选项数据存储在questionOptions.options中
  if (question.questionOptions && typeof question.questionOptions === 'object') {
    console.log('  🔍 questionOptions详细结构:', question.questionOptions)
    
    // 检查是否有options字段（这是后端标准格式）
    if (question.questionOptions.options && Array.isArray(question.questionOptions.options)) {
      console.log('  ✅ 找到questionOptions.options字段，选项数量:', question.questionOptions.options.length)
      console.log('  ✅ 选项内容:', question.questionOptions.options)
      return question.questionOptions.options
    }
    
    // 如果questionOptions本身就是数组（兼容性处理）
    if (Array.isArray(question.questionOptions)) {
      console.log('  ✅ questionOptions本身是数组，选项数量:', question.questionOptions.length)
      return question.questionOptions
    }
      
      // 如果questionOptions是Map格式，尝试从Map中提取选项
      const optionKeys = Object.keys(question.questionOptions)
      console.log('  🔍 questionOptions的键:', optionKeys)
      
      // 检查是否有标准的选项键（A, B, C, D等）
      const standardOptionKeys = ['A', 'B', 'C', 'D', 'E', 'F']
      const foundOptionKeys = standardOptionKeys.filter(key => 
        question.questionOptions.hasOwnProperty(key) && question.questionOptions[key]
      )
      
      if (foundOptionKeys.length > 0) {
        const options = foundOptionKeys.map(key => question.questionOptions[key])
        console.log('  ✅ 从Map中提取选项 (A-F键)，选项数量:', options.length)
        console.log('  ✅ 选项内容:', options)
        return options
      }
      
      // 检查是否有数字键（0, 1, 2, 3等）
      const numericKeys = ['0', '1', '2', '3', '4', '5']
      const foundNumericKeys = numericKeys.filter(key => 
        question.questionOptions.hasOwnProperty(key) && question.questionOptions[key]
      )
      
      if (foundNumericKeys.length > 0) {
        const options = foundNumericKeys.map(key => question.questionOptions[key])
        console.log('  ✅ 从Map中提取选项 (数字键)，选项数量:', options.length)
        console.log('  ✅ 选项内容:', options)
        return options
      }
      
      // 如果Map中有其他键，尝试提取所有非空值
      const allValues = Object.values(question.questionOptions).filter(value => 
        value && typeof value === 'string' && value.trim() !== ''
      )
      
      if (allValues.length > 0) {
        console.log('  ✅ 从Map中提取所有值作为选项，选项数量:', allValues.length)
        console.log('  ✅ 选项内容:', allValues)
        return allValues
      }
  }
  
  // 如果questionOptions本身就是数组
  if (Array.isArray(question.questionOptions)) {
    console.log('  ✅ 找到questionOptions数组字段，选项数量:', question.questionOptions.length)
    return question.questionOptions
  }
  
  // 处理后端返回的options字段（可能是字符串形式的JSON数组）
  if (question.options) {
    if (Array.isArray(question.options)) {
      console.log('  ✅ 找到options数组字段，选项数量:', question.options.length)
      return question.options
    }
    if (typeof question.options === 'string') {
      try {
        const parsedOptions = JSON.parse(question.options)
        if (Array.isArray(parsedOptions)) {
          console.log('  ✅ 解析options字符串成功，选项数量:', parsedOptions.length)
          return parsedOptions
        }
      } catch (e) {
        console.log('  ❌ 解析options字符串失败:', e.message)
      }
    }
  }
  
  if (question.choices && Array.isArray(question.choices)) {
    console.log('  ✅ 找到choices字段，选项数量:', question.choices.length)
    return question.choices
  }
  
  // 检查字符串形式的选项数组
  if (question.optionList && typeof question.optionList === 'string') {
    try {
      const parsedOptions = JSON.parse(question.optionList)
      if (Array.isArray(parsedOptions)) {
        console.log('  ✅ 解析optionList字符串，选项数量:', parsedOptions.length)
        return parsedOptions
      }
    } catch (e) {
      console.log('  ❌ 解析optionList失败:', e.message)
    }
  }
  
  // 如果选项是对象形式，尝试提取值
  if (question.optionA || question.optionB || question.optionC || question.optionD) {
    const options = []
    if (question.optionA) options.push(question.optionA)
    if (question.optionB) options.push(question.optionB)
    if (question.optionC) options.push(question.optionC)
    if (question.optionD) options.push(question.optionD)
    if (question.optionE) options.push(question.optionE)
    if (question.optionF) options.push(question.optionF)
    console.log('  ✅ 从optionA-F字段提取选项，选项数量:', options.length)
    return options
  }
  
  // 检查其他可能的选项字段
  const possibleOptionFields = ['answerOptions', 'questionChoices', 'optionContent', 'answerChoices']
  for (const field of possibleOptionFields) {
    if (question[field] && Array.isArray(question[field])) {
      console.log(`  ✅ 找到${field}字段，选项数量:`, question[field].length)
      return question[field]
    }
  }
  
  console.log('  ❌ 未找到任何选项数据')
  console.log('  - 可能的选项字段检查结果:')
  console.log('    - options:', question.options)
  console.log('    - questionOptions:', question.questionOptions)
  console.log('    - choices:', question.choices)
  console.log('    - optionList:', question.optionList)
  console.log('    - optionA:', question.optionA)
  console.log('    - optionB:', question.optionB)
  
  return []
}

// 保存答题记录到数据库
const saveAnswersToDatabase = async () => {
  if (!checkAuth()) {
    console.warn('未找到用户token，无法保存答题记录')
    return
  }
  
  try {
    
    // 调试：检查exerciseData的完整内容
    console.log('🔍 调试 - exerciseData完整内容:', exerciseData.value)
    console.log('🔍 调试 - examPaperId值:', exerciseData.value.examPaperId)
    console.log('🔍 调试 - examPaperId类型:', typeof exerciseData.value.examPaperId)
    console.log('🔍 调试 - isBackupId标记:', exerciseData.value.isBackupId)
    
    // 确保试卷ID不为空
    let paperId = exerciseData.value.examPaperId
    let isBackupId = exerciseData.value.isBackupId || false
    
    if (!paperId || paperId === '' || paperId === null || paperId === undefined) {
      console.error('❌ 试卷ID为空，尝试获取备用ID')
      console.error('🔍 详细错误信息:')
      console.error('  - exerciseData.examPaperId值:', exerciseData.value.examPaperId)
      console.error('  - exerciseData完整对象:', exerciseData.value)
      console.error('  - sessionStorage中的数据:', sessionStorage.getItem(route.query.dataKey))
      
      // 尝试从其他可能的来源获取试卷ID
      const alternativeIds = [
        exerciseData.value.id,
        exerciseData.value.paperId,
        exerciseData.value.examId,
        route.query.paperId,
        route.query.examPaperId
      ]
      
      console.error('🔍 尝试的备用ID来源:', alternativeIds)
      const foundId = alternativeIds.find(id => id && id !== '' && id !== null && id !== undefined)
      
      if (foundId) {
        console.warn('⚠️ 使用备用ID:', foundId)
        paperId = foundId
        isBackupId = true
        exerciseData.value.examPaperId = foundId
        exerciseData.value.isBackupId = true
      } else {
        // 生成最后的备用ID（使用纯数字格式以兼容后端Long类型）
        paperId = Date.now() + Math.floor(Math.random() * 1000)
        isBackupId = true
        console.warn('🔧 生成最终备用ID:', paperId)
        
        ElMessage.warning({
          message: '试卷ID获取失败，使用临时ID继续。答题记录可能无法保存到数据库，但不影响答题功能。',
          duration: 6000,
          showClose: true
        })
        
        exerciseData.value.examPaperId = paperId
        exerciseData.value.isBackupId = true
      }
    }
    
    console.log('✅ 试卷ID验证通过:', paperId)
    console.log('📋 ID类型:', isBackupId ? '备用ID' : '正常ID')
    
    // 如果是备用ID，给用户提示但仍尝试保存
    if (isBackupId) {
      console.warn('⚠️ 使用备用ID保存答题记录，可能无法正常保存到数据库')
    }
    
    // 检查用户是否已登录
    if (!userStore.id || userStore.id === 0) {
      console.error('❌ 用户未登录或用户ID无效:', userStore.id)
      ElMessage.error('请先登录后再进行答题')
      router.push('/login')
      return
    }
    
    console.log('✅ 用户ID验证通过:', userStore.id)
    
    // 构建答题记录数据
    const answerData = {
      userId: userStore.id, // 从用户store获取用户ID
      paperId: paperId,
      startTime: new Date(Date.now() - (exerciseData.value.examDuration || 80) * 60 * 1000).toISOString(),
      endTime: new Date().toISOString(),
      isBackupId: isBackupId, // 标记是否为备用ID
      paperName: exerciseData.value.paperName || '未命名试卷',
      answers: questions.value.map((question, index) => {
        // 确保正确获取questionId
        const questionId = question.questionId || question.id || (index + 1)
        
        // 确保userAnswer格式正确
        let userAnswer = ''
        if (Array.isArray(userAnswers.value[index])) {
          // 多选题：先去重再转换为逗号分隔的字符串
          const uniqueAnswers = [...new Set(userAnswers.value[index])]
          userAnswer = uniqueAnswers.join(',')
        } else {
          // 单选题、填空题等：直接使用字符串值
          userAnswer = userAnswers.value[index] || ''
        }
        
        console.log(`题目${index + 1} - ID: ${questionId}, 用户答案: "${userAnswer}", 原始答案:`, userAnswers.value[index])
        
        return {
          questionId: questionId,
          userAnswer: userAnswer,
          timeSpent: 0 // 可以记录每题的答题时间
        }
      })
    }
    
    // 如果是重新考试模式，直接添加override参数
    if (isRetakeMode.value) {
      answerData.override = true
      console.log('🔄 重新考试模式，添加override参数:', answerData.override)
    }
    
    console.log('📝 准备保存答题记录:', answerData)
    console.log('📝 试卷ID检查:', answerData.paperId ? '✅ 有效' : '❌ 为空')
    console.log('📝 备用ID标记:', answerData.isBackupId)
    console.log('📝 用户ID:', answerData.userId)
    console.log('📝 答案数量:', answerData.answers.length)
    console.log('📝 答案详情:', answerData.answers)
    
    // 验证每个答案的格式
    answerData.answers.forEach((answer, index) => {
      console.log(`📝 答案${index + 1}验证:`, {
        questionId: answer.questionId,
        userAnswer: answer.userAnswer,
        userAnswerType: typeof answer.userAnswer,
        userAnswerLength: answer.userAnswer ? answer.userAnswer.length : 0,
        isEmpty: !answer.userAnswer || answer.userAnswer === ''
      })
    })
    
    try {
      const result = await authFetch(`${API_BASE_URL}/exam/results/submit`, {
        method: 'POST',
        body: JSON.stringify(answerData)
      }, router)
      
      if (result.code === 200) {
        console.log('答题记录保存成功:', result.data)
        if (isBackupId) {
          ElMessage.success({
            message: '答题完成！由于使用临时ID，记录已尝试保存但可能无法持久化到数据库。',
            duration: 5000
          })
        } else {
          ElMessage.success('答题记录保存成功！')
        }
      } else {
        console.error('保存答题记录失败:', result)
        
        // 检查是否为重复提交错误
        const errorMessage = result.message || '未知错误'
        if (errorMessage.includes('您已经参加过该试卷考试') || errorMessage.includes('重复提交')) {
          // 如果已经是重新考试模式，直接使用override模式提交，不再弹出提醒
          if (isRetakeMode.value) {
            console.log('🔄 重新考试模式，直接覆盖提交')
            console.log('🔍 调试 - isRetakeMode.value:', isRetakeMode.value)
            try {
              // 添加覆盖标记到请求数据中
              const overrideData = {
                ...answerData,
                override: true // 添加覆盖标记
              }
              console.log('🔍 调试 - overrideData.override:', overrideData.override)
              console.log('🔍 调试 - overrideData完整对象:', JSON.stringify(overrideData, null, 2))
              
              // 直接重新提交，让后端处理覆盖逻辑
              const retryResult = await authFetch(`${API_BASE_URL}/exam/results/submit`, {
                method: 'POST',
                body: JSON.stringify(overrideData)
              }, router)
              
              if (retryResult.code === 200) {
                ElMessage.success('重新考试成功！答题记录已保存。')
                // 重置重新考试模式标记
                isRetakeMode.value = false
              } else {
                ElMessage.error('重新提交失败：' + (retryResult.message || '未知错误'))
              }
            } catch (retakeError) {
              console.error('重新考试处理异常:', retakeError)
              ElMessage.error('重新考试处理失败，请稍后重试')
            }
          } else {
            // 非重新考试模式，弹出确认框询问用户
            ElMessageBox.confirm(
              '您已经参加过这份试卷的考试。是否要重新参加考试？重新参加将会覆盖之前的考试记录。',
              '重复考试提醒',
              {
                confirmButtonText: '重新考试',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
              }
            ).then(async () => {
              // 用户选择重新考试 - 直接覆盖提交，不删除记录
              try {
                console.log('🔄 用户选择重新考试，尝试覆盖提交')
                
                // 添加覆盖标记到请求数据中
                const overrideData = {
                  ...answerData,
                  override: true // 添加覆盖标记
                }
                console.log('🔍 调试 - 用户确认重新考试，overrideData.override:', overrideData.override)
                console.log('🔍 调试 - 用户确认重新考试，overrideData完整对象:', JSON.stringify(overrideData, null, 2))
                
                // 直接重新提交，让后端处理覆盖逻辑
                const retryResult = await authFetch(`${API_BASE_URL}/exam/results/submit`, {
                  method: 'POST',
                  body: JSON.stringify(overrideData)
                }, router)
                
                if (retryResult.code === 200) {
                  ElMessage.success('重新考试成功！答题记录已保存。')
                } else {
                  ElMessage.error('重新提交失败：' + (retryResult.message || '未知错误'))
                }
              } catch (retakeError) {
                console.error('重新考试处理异常:', retakeError)
                ElMessage.error('重新考试处理失败，请稍后重试')
              }
            }).catch(() => {
              // 用户取消重新考试
              ElMessage.info('已取消重新考试')
            })
          }
        } else {
          // 其他类型的错误
          if (isBackupId) {
            ElMessage.info({
              message: '答题完成！由于使用临时ID，记录无法保存到数据库，但答题过程已完成。',
              duration: 5000
            })
          } else {
            ElMessage.error('答题记录保存失败：' + errorMessage)
          }
        }
      }
    } catch (saveError) {
      console.error('保存答题记录网络异常:', saveError)
      if (isBackupId) {
        ElMessage.info({
          message: '答题完成！由于网络问题和临时ID，记录无法保存，但答题过程已完成。',
          duration: 5000
        })
      } else {
        ElMessage.error('网络异常，答题记录保存失败')
      }
    }
  } catch (error) {
    console.error('保存答题记录异常:', error)
  }
}

// 组件销毁时清理计时器
onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped>
/* 基础样式 */
body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.exam-title {
  font-size: 20px;
  font-weight: bold;
}

.timer {
  font-size: 18px;
  background: rgba(255,255,255,0.2);
  padding: 8px 16px;
  border-radius: 20px;
}

/* 导航面板样式 */
.nav-panel {
  width: 250px;
  background: white;
  padding: 15px;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
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
  background: #4CAF50;
  color: white;
}

.nav-item.marked {
  background: #FF9800;
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
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
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
  box-shadow: -2px 0 10px rgba(0,0,0,0.1);
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
  background: #FF9800;
  color: white;
}

.btn-success {
  background: #4CAF50;
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

.difficulty-badge {
  display: inline-block;
  margin-left: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
  color: white;
}

.difficulty-easy {
  background-color: #4caf50;
}

.difficulty-medium {
  background-color: #ff9800;
}

.difficulty-hard {
  background-color: #f44336;
}
</style>