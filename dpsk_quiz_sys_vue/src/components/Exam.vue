<template>
  <div class="exam-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载考试...</p>
    </div>
    
    <div v-else-if="exam" class="exam-content">
      <!-- 考试头部信息 -->
      <div class="exam-header">
        <h1 class="exam-title">{{ exam.title }}</h1>
        <div class="exam-info">
          <span class="time-remaining">剩余时间: {{ formatTime(timeRemaining) }}</span>
          <span class="question-progress">{{ currentQuestionIndex + 1 }} / {{ exam.questions.length }}</span>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-section">
        <div class="question-header">
          <h2 class="question-title">第{{ currentQuestionIndex + 1 }}题</h2>
          <span class="question-type">{{ getQuestionTypeText(currentQuestion.type) }}</span>
        </div>
        
        <div class="question-content">
          <p class="question-text">{{ currentQuestion.content }}</p>
          
          <!-- 选择题选项 -->
          <div v-if="currentQuestion.type === 'SINGLE_CHOICE' || currentQuestion.type === 'MULTIPLE_CHOICE'" class="options-list">
            <div 
              v-for="(option, index) in getQuestionOptions(currentQuestion)" 
              :key="index"
              class="option-item"
              :class="{ 'selected': isOptionSelected(index) }"
              @click="selectOption(index)"
            >
              <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
              <span class="option-text">{{ option }}</span>
            </div>
          </div>
          
          <!-- 填空题 -->
          <div v-else-if="currentQuestion.type === 'FILL_BLANK'" class="fill-blank-section">
            <textarea 
              v-model="answers[currentQuestionIndex]"
              class="fill-blank-input"
              placeholder="请输入答案..."
              rows="4"
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 导航按钮 -->
      <div class="navigation-section">
        <button 
          @click="previousQuestion" 
          :disabled="currentQuestionIndex === 0"
          class="nav-btn prev-btn"
        >
          上一题
        </button>
        
        <button 
          v-if="currentQuestionIndex < exam.questions.length - 1"
          @click="nextQuestion"
          class="nav-btn next-btn"
        >
          下一题
        </button>
        
        <button 
          v-else
          @click="submitExam"
          class="nav-btn submit-btn"
        >
          提交考试
        </button>
      </div>

      <!-- 题目导航 -->
      <div class="question-nav">
        <div class="nav-title">题目导航</div>
        <div class="nav-grid">
          <button
            v-for="(question, index) in exam.questions"
            :key="index"
            @click="goToQuestion(index)"
            class="nav-item"
            :class="{
              'current': index === currentQuestionIndex,
              'answered': isQuestionAnswered(index)
            }"
          >
            {{ index + 1 }}
          </button>
        </div>
      </div>
    </div>

    <!-- 提交确认弹窗 -->
    <div v-if="showSubmitModal" class="modal-overlay" @click="closeSubmitModal">
      <div class="modal-content" @click.stop>
        <h3>确认提交考试</h3>
        <p>您确定要提交考试吗？提交后将无法修改答案。</p>
        <div class="modal-actions">
          <button @click="closeSubmitModal" class="cancel-btn">取消</button>
          <button @click="confirmSubmit" class="confirm-btn">确认提交</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { API_BASE_URL } from '../config/api'

export default {
  name: 'Exam',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(true)
    const exam = ref(null)
    const currentQuestionIndex = ref(0)
    const answers = ref([])
    const timeRemaining = ref(0)
    const showSubmitModal = ref(false)
    const timer = ref(null)
    
    const currentQuestion = computed(() => {
      return exam.value?.questions[currentQuestionIndex.value] || null
    })
    
    const getToken = () => {
      return document.cookie
        .split('; ')
        .find(row => row.startsWith('token='))
        ?.split('=')[1] || ''
    }
    
    const loadExam = async () => {
      try {
        const token = getToken()
        if (!token) {
          throw new Error('无token, 请重新登录')
        }
        
        // 获取试卷基本信息
        const paperResponse = await fetch(`${API_BASE_URL}/exam/papers/${route.params.id}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (!paperResponse.ok) {
          throw new Error('获取试卷信息失败')
        }
        
        const paperResult = await paperResponse.json()
        if (paperResult.code !== 200) {
          throw new Error(paperResult.message || '获取试卷信息失败')
        }
        
        // 获取试卷题目列表
        const questionsResponse = await fetch(`${API_BASE_URL}/exam-question/paper/${route.params.id}?showAnswer=false`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (!questionsResponse.ok) {
          throw new Error('获取题目列表失败')
        }
        
        const questionsResult = await questionsResponse.json()
        if (questionsResult.code !== 200) {
          throw new Error(questionsResult.message || '获取题目列表失败')
        }
        
        // 合并试卷信息和题目列表
        exam.value = {
          ...paperResult.data,
          questions: questionsResult.data || []
        }
        
        // 初始化答案数组
        answers.value = new Array(exam.value.questions.length).fill('')
        
        // 设置考试时间（假设默认60分钟，如果API返回了duration则使用API的值）
        timeRemaining.value = (exam.value.duration || 60) * 60 // 转换为秒
        startTimer()
        
      } catch (error) {
        console.error('加载考试出错:', error)
      } finally {
        loading.value = false
      }
    }
    
    const startTimer = () => {
      timer.value = setInterval(() => {
        if (timeRemaining.value > 0) {
          timeRemaining.value--
        } else {
          // 时间到，自动提交
          confirmSubmit()
        }
      }, 1000)
    }
    
    const formatTime = (seconds) => {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }
    
    const getQuestionTypeText = (type) => {
      const typeMap = {
        // 标准大写类型（与后端QuestionType枚举一致）
        'SINGLE_CHOICE': '单选题',
        'MULTI_CHOICE': '多选题',
        'FILL_BLANK': '填空题',
        'SHORT_ANSWER': '简答题',
        'JUDGE': '判断题',
        
        // 兼容旧格式
        'MULTIPLE_CHOICE': '多选题'
      }
      return typeMap[type] || type
    }
    
    // 处理不同格式的题目选项数据
    const getQuestionOptions = (question) => {
      // 处理questionOptions对象中的options数组
      if (question.questionOptions) {
        if (question.questionOptions.options && Array.isArray(question.questionOptions.options)) {
          return question.questionOptions.options
        }
        // 如果questionOptions本身就是数组
        if (Array.isArray(question.questionOptions)) {
          return question.questionOptions
        }
      }
      
      // 处理后端返回的options字段
      if (question.options) {
        if (Array.isArray(question.options)) {
          return question.options
        }
        if (typeof question.options === 'string') {
          try {
            const parsedOptions = JSON.parse(question.options)
            if (Array.isArray(parsedOptions)) {
              return parsedOptions
            }
          } catch (e) {
            console.error('解析options字符串失败:', e.message)
          }
        }
      }
      
      // 检查其他可能的选项字段
      if (question.choices && Array.isArray(question.choices)) {
        return question.choices
      }
      
      // 检查字符串形式的选项数组
      if (question.optionList && typeof question.optionList === 'string') {
        try {
          const parsedOptions = JSON.parse(question.optionList)
          if (Array.isArray(parsedOptions)) {
            return parsedOptions
          }
        } catch (e) {
          console.error('解析optionList失败:', e.message)
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
        return options
      }
      
      // 检查其他可能的选项字段
      const possibleOptionFields = ['answerOptions', 'questionChoices', 'optionContent', 'answerChoices']
      for (const field of possibleOptionFields) {
        if (question[field] && Array.isArray(question[field])) {
          return question[field]
        }
      }
      
      console.warn('未找到任何选项数据，题目ID:', question.id)
      return []
    }
    
    const isOptionSelected = (optionIndex) => {
      const answer = answers.value[currentQuestionIndex.value]
      if (currentQuestion.value.type === 'SINGLE_CHOICE') {
        return answer === optionIndex
      } else if (currentQuestion.value.type === 'MULTIPLE_CHOICE') {
        return Array.isArray(answer) && answer.includes(optionIndex)
      }
      return false
    }
    
    const selectOption = (optionIndex) => {
      if (currentQuestion.value.type === 'SINGLE_CHOICE') {
        answers.value[currentQuestionIndex.value] = optionIndex
      } else if (currentQuestion.value.type === 'MULTIPLE_CHOICE') {
        let currentAnswer = answers.value[currentQuestionIndex.value]
        if (!Array.isArray(currentAnswer)) {
          currentAnswer = []
        }
        
        const index = currentAnswer.indexOf(optionIndex)
        if (index > -1) {
          currentAnswer.splice(index, 1)
        } else {
          currentAnswer.push(optionIndex)
        }
        
        answers.value[currentQuestionIndex.value] = currentAnswer
      }
    }
    
    const isQuestionAnswered = (questionIndex) => {
      const answer = answers.value[questionIndex]
      if (Array.isArray(answer)) {
        return answer.length > 0
      }
      return answer !== '' && answer !== null && answer !== undefined
    }
    
    const previousQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--
      }
    }
    
    const nextQuestion = () => {
      if (currentQuestionIndex.value < exam.value.questions.length - 1) {
        currentQuestionIndex.value++
      }
    }
    
    const goToQuestion = (index) => {
      currentQuestionIndex.value = index
    }
    
    const submitExam = () => {
      showSubmitModal.value = true
    }
    
    const closeSubmitModal = () => {
      showSubmitModal.value = false
    }
    
    const confirmSubmit = async () => {
      try {
        const token = getToken()
        const response = await fetch(`${API_BASE_URL}/exam/submit`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            paperId: route.params.id,
            answers: answers.value
          })
        })
        
        if (response.ok) {
          const result = await response.json()
          // 跳转到结果页面
          router.push(`/exam-result/${result.id || route.params.id}`)
        } else {
          console.error('提交考试失败:', response.statusText)
        }
      } catch (error) {
        console.error('提交考试出错:', error)
      }
    }
    
    onMounted(() => {
      loadExam()
    })
    
    onUnmounted(() => {
      if (timer.value) {
        clearInterval(timer.value)
      }
    })
    
    return {
      loading,
      exam,
      currentQuestionIndex,
      currentQuestion,
      answers,
      timeRemaining,
      showSubmitModal,
      getQuestionOptions,
      formatTime,
      getQuestionTypeText,
      isOptionSelected,
      selectOption,
      isQuestionAnswered,
      previousQuestion,
      nextQuestion,
      goToQuestion,
      submitExam,
      closeSubmitModal,
      confirmSubmit
    }
  }
}
</script>

<style scoped>
.exam-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.exam-header {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exam-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0;
}

.exam-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.time-remaining {
  background: #e74c3c;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
}

.question-progress {
  background: #3498db;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
}

.question-section {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #ecf0f1;
}

.question-title {
  font-size: 1.4rem;
  color: #2c3e50;
  margin: 0;
}

.question-type {
  background: #95a5a6;
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
}

.question-text {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #34495e;
  margin-bottom: 25px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option-item:hover {
  border-color: #3498db;
  background-color: #f8f9fa;
}

.option-item.selected {
  border-color: #3498db;
  background-color: #e3f2fd;
}

.option-label {
  font-weight: bold;
  color: #3498db;
  margin-right: 12px;
  min-width: 25px;
}

.option-text {
  flex: 1;
  color: #2c3e50;
}

.fill-blank-section {
  margin-top: 20px;
}

.fill-blank-input {
  width: 100%;
  padding: 15px;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  font-size: 1rem;
  resize: vertical;
  min-height: 120px;
}

.fill-blank-input:focus {
  outline: none;
  border-color: #3498db;
}

.navigation-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.nav-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.prev-btn {
  background: #95a5a6;
  color: white;
}

.prev-btn:hover:not(:disabled) {
  background: #7f8c8d;
}

.prev-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.next-btn {
  background: #3498db;
  color: white;
}

.next-btn:hover {
  background: #2980b9;
}

.submit-btn {
  background: #27ae60;
  color: white;
}

.submit-btn:hover {
  background: #229954;
}

.question-nav {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 15px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(50px, 1fr));
  gap: 10px;
}

.nav-item {
  width: 50px;
  height: 50px;
  border: 2px solid #ecf0f1;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
}

.nav-item:hover {
  border-color: #3498db;
}

.nav-item.current {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.nav-item.answered {
  background: #27ae60;
  color: white;
  border-color: #27ae60;
}

.nav-item.answered.current {
  background: #3498db;
  border-color: #3498db;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.modal-content h3 {
  color: #2c3e50;
  margin-bottom: 15px;
}

.modal-content p {
  color: #7f8c8d;
  margin-bottom: 25px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.cancel-btn {
  padding: 10px 20px;
  border: 2px solid #95a5a6;
  background: white;
  color: #95a5a6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #95a5a6;
  color: white;
}

.confirm-btn {
  padding: 10px 20px;
  border: 2px solid #e74c3c;
  background: #e74c3c;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-btn:hover {
  background: #c0392b;
  border-color: #c0392b;
}
</style>