<template>
  <div class="exam-preview-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载试卷预览...</p>
    </div>
    
    <div v-else-if="exam" class="preview-content">
      <!-- 试卷头部信息 -->
      <div class="exam-header">
        <div class="header-content">
          <h1 class="exam-title">{{ exam.title }}</h1>
          <div class="exam-meta">
            <div class="meta-item">
              <i class="icon">⏱️</i>
              <span>考试时长: {{ exam.duration }} 分钟</span>
            </div>
            <div class="meta-item">
              <i class="icon">📝</i>
              <span>题目数量: {{ exam.questions?.length || 0 }} 题</span>
            </div>
            <div class="meta-item">
              <i class="icon">⭐</i>
              <span>难度: {{ getDifficultyText(exam.difficulty) }}</span>
            </div>
            <div class="meta-item">
              <i class="icon">💯</i>
              <span>总分: {{ exam.totalScore }} 分</span>
            </div>
          </div>
          <div class="exam-description" v-if="exam.description">
            <h3>考试说明</h3>
            <p>{{ exam.description }}</p>
          </div>
        </div>
        <div class="header-actions">
          <button @click="goBack" class="back-btn">
            <i class="icon">←</i>
            返回
          </button>
          <button @click="startExam" class="start-btn">
            <i class="icon">🚀</i>
            开始考试
          </button>
        </div>
      </div>

      <!-- 题目列表 -->
      <div class="questions-section">
        <h2 class="section-title">试卷题目预览</h2>
        
        <div class="questions-list">
          <div 
            v-for="(question, index) in exam.questions || []" 
            :key="index"
            class="question-card"
          >
            <div class="question-header">
              <div class="question-number">第{{ index + 1 }}题</div>
              <div class="question-info">
                <span class="question-type">{{ getQuestionTypeText(question.type) }}</span>
                <span class="question-score">{{ question.score || 5 }}分</span>
              </div>
            </div>
            
            <div class="question-content">
              <p class="question-text">{{ question.content }}</p>
              
              <!-- 选择题选项预览 -->
              <div v-if="question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE'" class="options-preview">
                <div 
                  v-for="(option, optionIndex) in question.options" 
                  :key="optionIndex"
                  class="option-item"
                >
                  <span class="option-label">{{ String.fromCharCode(65 + optionIndex) }}.</span>
                  <span class="option-text">{{ option }}</span>
                </div>
              </div>
              
              <!-- 填空题提示 -->
              <div v-else-if="question.type === 'FILL_BLANK'" class="fill-blank-preview">
                <div class="blank-placeholder">
                  <i class="icon">✏️</i>
                  <span>此题需要填写文字答案</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 考试须知 -->
      <div class="exam-rules">
        <h3 class="rules-title">考试须知</h3>
        <ul class="rules-list">
          <li>请仔细阅读每道题目，确保理解题意后再作答</li>
          <li>单选题只能选择一个答案，多选题可以选择多个答案</li>
          <li>填空题请填写准确的答案，注意大小写和标点符号</li>
          <li>考试过程中请保持网络连接稳定</li>
          <li>考试时间到后系统将自动提交答案</li>
          <li>提交后无法修改答案，请谨慎操作</li>
        </ul>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <h3>加载失败</h3>
      <p>{{ error || '无法加载试卷信息，请稍后重试' }}</p>
      <button @click="loadExam" class="retry-btn">重新加载</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { API_BASE_URL } from '../config/api'

export default {
  name: 'ExamPreview',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const loading = ref(true)
    const exam = ref(null)
    const error = ref('')
    
    const getToken = () => {
      return document.cookie
        .split('; ')
        .find(row => row.startsWith('token='))
        ?.split('=')[1] || ''
    }
    
    const loadExam = async () => {
      try {
        loading.value = true
        
        const token = getToken()
        if (!token) {
          throw new Error('无token, 请重新登录')
        }
        
        // 获取试卷基本信息
        const paperResponse = await fetch(`${API_BASE_URL}/exam/papers/${route.params.id}`, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
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
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
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
      } catch (err) {
        error.value = err.message
        console.error('加载试卷失败:', err)
        exam.value = null
      } finally {
        loading.value = false
      }
    }
    
    const getDifficultyText = (difficulty) => {
      const difficultyMap = {
        'EASY': '简单',
        'MEDIUM': '中等',
        'HARD': '困难',
        'simple': '简单',
        'medium': '中等',
        'hard': '困难'
      }
      return difficultyMap[difficulty] || difficulty
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
    
    const goBack = () => {
      router.go(-1)
    }
    
    const startExam = () => {
      router.push(`/exam/${route.params.id}`)
    }
    
    onMounted(() => {
      loadExam()
    })
    
    return {
      loading,
      exam,
      error,
      loadExam,
      getDifficultyText,
      getQuestionTypeText,
      goBack,
      startExam
    }
  }
}
</script>

<style scoped>
.exam-preview-container {
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
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
}

.header-content {
  flex: 1;
}

.exam-title {
  font-size: 2.2rem;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 600;
}

.exam-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.meta-item .icon {
  font-size: 1.2rem;
}

.meta-item span {
  color: #2c3e50;
  font-weight: 500;
}

.exam-description {
  background: #e8f4fd;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.exam-description h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.1rem;
}

.exam-description p {
  color: #34495e;
  line-height: 1.6;
  margin: 0;
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 120px;
}

.back-btn, .start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn {
  background: #95a5a6;
  color: white;
}

.back-btn:hover {
  background: #7f8c8d;
  transform: translateY(-2px);
}

.start-btn {
  background: #27ae60;
  color: white;
}

.start-btn:hover {
  background: #229954;
  transform: translateY(-2px);
}

.questions-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 25px;
  text-align: center;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #3498db;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #ecf0f1;
}

.question-number {
  font-size: 1.3rem;
  font-weight: bold;
  color: #3498db;
}

.question-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.question-type {
  background: #3498db;
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
  font-weight: 500;
}

.question-score {
  background: #e74c3c;
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
  font-weight: 500;
}

.question-text {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 500;
}

.options-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  background: #f8f9fa;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.option-item:hover {
  background: #e3f2fd;
  border-color: #3498db;
}

.option-label {
  font-weight: bold;
  color: #3498db;
  margin-right: 12px;
  min-width: 25px;
}

.option-text {
  color: #2c3e50;
  flex: 1;
}

.fill-blank-preview {
  background: #f8f9fa;
  border: 2px dashed #bdc3c7;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
}

.blank-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #7f8c8d;
  font-style: italic;
}

.blank-placeholder .icon {
  font-size: 1.5rem;
}

.exam-rules {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #f39c12;
}

.rules-title {
  color: #2c3e50;
  font-size: 1.4rem;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.rules-title::before {
  content: '📋';
  font-size: 1.2rem;
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rules-list li {
  padding: 12px 0;
  border-bottom: 1px solid #ecf0f1;
  color: #34495e;
  line-height: 1.5;
  position: relative;
  padding-left: 25px;
}

.rules-list li:last-child {
  border-bottom: none;
}

.rules-list li::before {
  content: '•';
  color: #f39c12;
  font-weight: bold;
  position: absolute;
  left: 0;
  top: 12px;
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.error-state h3 {
  color: #e74c3c;
  margin-bottom: 15px;
  font-size: 1.5rem;
}

.error-state p {
  color: #7f8c8d;
  margin-bottom: 25px;
  font-size: 1.1rem;
}

.retry-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .exam-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .exam-meta {
    grid-template-columns: 1fr;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .question-info {
    align-self: flex-end;
  }
}
</style>