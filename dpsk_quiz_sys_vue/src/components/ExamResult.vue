<template>
  <div class="exam-result-container">
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载考试结果...</p>
    </div>
    
    <div v-else-if="result" class="result-content">
      <!-- 结果头部 -->
      <div class="result-header">
        <div class="result-icon">
          <span v-if="result.passed" class="success-icon">🎉</span>
          <span v-else class="fail-icon">😔</span>
        </div>
        <h1 class="result-title">
          {{ result.passed ? '恭喜通过考试！' : '很遗憾，未通过考试' }}
        </h1>
        <p class="result-subtitle">{{ result.examTitle }}</p>
      </div>

      <!-- 成绩概览 -->
      <div class="score-overview">
        <div class="score-card main-score">
          <div class="score-label">总分</div>
          <div class="score-value">{{ result.score }}/{{ result.totalScore }}</div>
          <div class="score-percentage">{{ getPercentage(result.score, result.totalScore) }}%</div>
        </div>
        
        <div class="score-stats">
          <div class="stat-item">
            <div class="stat-icon">✅</div>
            <div class="stat-content">
              <div class="stat-value">{{ result.correctCount }}</div>
              <div class="stat-label">答对题数</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon">❌</div>
            <div class="stat-content">
              <div class="stat-value">{{ result.wrongCount }}</div>
              <div class="stat-label">答错题数</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon">⏱️</div>
            <div class="stat-content">
              <div class="stat-value">{{ formatDuration(result.duration) }}</div>
              <div class="stat-label">用时</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon">📊</div>
            <div class="stat-content">
              <div class="stat-value">{{ result.accuracy }}%</div>
              <div class="stat-label">正确率</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 成绩等级 -->
      <div class="grade-section">
        <div class="grade-card" :class="getGradeClass(result.grade)">
          <div class="grade-icon">{{ getGradeIcon(result.grade) }}</div>
          <div class="grade-content">
            <div class="grade-title">成绩等级</div>
            <div class="grade-value">{{ getGradeText(result.grade) }}</div>
            <div class="grade-description">{{ getGradeDescription(result.grade) }}</div>
          </div>
        </div>
      </div>

      <!-- 题目详情 -->
      <div class="questions-detail" v-if="result.questionResults && result.questionResults.length > 0">
        <h2 class="section-title">答题详情</h2>
        
        <div class="filter-tabs">
          <button 
            @click="filterType = 'all'"
            :class="{ active: filterType === 'all' }"
            class="filter-tab"
          >
            全部 ({{ result.questionResults.length }})
          </button>
          <button 
            @click="filterType = 'correct'"
            :class="{ active: filterType === 'correct' }"
            class="filter-tab correct"
          >
            正确 ({{ result.correctCount }})
          </button>
          <button 
            @click="filterType = 'wrong'"
            :class="{ active: filterType === 'wrong' }"
            class="filter-tab wrong"
          >
            错误 ({{ result.wrongCount }})
          </button>
        </div>
        
        <div class="questions-list">
          <div 
            v-for="(questionResult, index) in filteredQuestions" 
            :key="index"
            class="question-result-card"
            :class="{ correct: questionResult.correct, wrong: !questionResult.correct }"
          >
            <div class="question-header">
              <div class="question-number">第{{ questionResult.questionIndex + 1 }}题</div>
              <div class="question-status">
                <span class="status-icon">{{ questionResult.correct ? '✅' : '❌' }}</span>
                <span class="status-text">{{ questionResult.correct ? '正确' : '错误' }}</span>
                <span class="question-score">{{ questionResult.score }}/{{ questionResult.maxScore }}分</span>
              </div>
            </div>
            
            <div class="question-content">
              <p class="question-text">{{ questionResult.question.content }}</p>
              
              <!-- 选择题答案对比 -->
              <div v-if="questionResult.question.type === 'SINGLE_CHOICE' || questionResult.question.type === 'MULTIPLE_CHOICE'" class="choice-comparison">
                <div class="options-list">
                  <div 
                    v-for="(option, optionIndex) in questionResult.question.options" 
                    :key="optionIndex"
                    class="option-item"
                    :class="{
                      'user-selected': isUserSelected(questionResult.userAnswer, optionIndex),
                      'correct-answer': isCorrectAnswer(questionResult.correctAnswer, optionIndex),
                      'wrong-selection': isUserSelected(questionResult.userAnswer, optionIndex) && !isCorrectAnswer(questionResult.correctAnswer, optionIndex)
                    }"
                  >
                    <span class="option-label">{{ String.fromCharCode(65 + optionIndex) }}.</span>
                    <span class="option-text">{{ option }}</span>
                    <div class="option-indicators">
                      <span v-if="isUserSelected(questionResult.userAnswer, optionIndex)" class="user-indicator">你的答案</span>
                      <span v-if="isCorrectAnswer(questionResult.correctAnswer, optionIndex)" class="correct-indicator">正确答案</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 填空题答案对比 -->
              <div v-else-if="questionResult.question.type === 'FILL_BLANK'" class="fill-comparison">
                <div class="answer-comparison">
                  <div class="user-answer">
                    <label>你的答案:</label>
                    <div class="answer-content" :class="{ wrong: !questionResult.correct }">
                      {{ questionResult.userAnswer || '未作答' }}
                    </div>
                  </div>
                  <div class="correct-answer">
                    <label>正确答案:</label>
                    <div class="answer-content correct">
                      {{ questionResult.correctAnswer }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button @click="goBack" class="action-btn secondary">
          <i class="icon">←</i>
          返回试卷列表
        </button>
        <button @click="retakeExam" class="action-btn primary">
          <i class="icon">🔄</i>
          重新考试
        </button>
        <button @click="downloadResult" class="action-btn success">
          <i class="icon">📄</i>
          下载成绩单
        </button>
      </div>
    </div>

    <div v-else class="error-state">
      <div class="error-icon">❌</div>
      <h3>加载失败</h3>
      <p>无法加载考试结果，请稍后重试</p>
      <button @click="loadResult" class="retry-btn">重新加载</button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { API_BASE_URL } from '../config/api'

export default {
  name: 'ExamResult',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    
    const loading = ref(true)
    const result = ref(null)
    const filterType = ref('all')
    
    const getToken = () => {
      return document.cookie
        .split('; ')
        .find(row => row.startsWith('token='))
        ?.split('=')[1] || ''
    }
    
    const loadResult = async () => {
      loading.value = true
      try {
        const token = getToken()
        const paperId = route.params.id
        const userId = userStore.id
        
        if (!userId) {
          console.error('用户ID不存在，请重新登录')
          router.push('/login')
          return
        }
        
        console.log('请求参数:', { userId, paperId })
        const response = await fetch(`${API_BASE_URL}/exam/results/user/${userId}/paper/${paperId}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const responseData = await response.json()
          console.log('后端返回的原始数据:', responseData)
          
          // 提取ResponseMessage中的data字段
          const examResultData = responseData.data
          console.log('提取的考试结果数据:', examResultData)
          
          if (examResultData) {
            // 处理字段名映射
            const mappedResult = {
              resultId: examResultData.resultId,
              userId: examResultData.userId,
              paperId: examResultData.paperId,
              examTitle: examResultData.examTitle || '考试',
              examId: examResultData.paperId, // 用于重新考试
              examDate: examResultData.submitTime || new Date().toISOString(),
              
              // 字段映射：后端userScore -> 前端score
              score: examResultData.userScore || 0,
              totalScore: examResultData.totalScore || 0,
              
              // 字段映射：后端timeSpent -> 前端duration
              duration: examResultData.timeSpent || 0,
              
              // 字段映射：后端correctCount -> 前端correctCount
              correctCount: examResultData.correctCount || 0,
              
              // 计算错误题数：totalQuestions - correctCount
              wrongCount: (examResultData.totalQuestions || 0) - (examResultData.correctCount || 0),
              
              // 计算正确率
              accuracy: examResultData.totalQuestions > 0 
                ? Math.round((examResultData.correctCount / examResultData.totalQuestions) * 100)
                : 0,
              
              // 判断是否通过（假设60分及格）
              passed: examResultData.totalScore > 0 
                ? (examResultData.userScore / examResultData.totalScore) >= 0.6
                : false,
              
              // 计算等级
              grade: calculateGrade(examResultData.userScore, examResultData.totalScore),
              
              // 题目结果详情
              questionResults: examResultData.questionResults ? examResultData.questionResults.map((qr, index) => ({
                questionIndex: index,
                questionId: qr.questionId,
                question: {
                  content: qr.questionContent,
                  type: qr.questionType || 'SINGLE_CHOICE',
                  options: qr.options || []
                },
                userAnswer: qr.userAnswer,
                correctAnswer: qr.correctAnswer,
                correct: qr.isCorrect || false,
                score: qr.score || 0,
                maxScore: qr.maxScore || 0
              })) : []
            }
            
            console.log('映射后的结果数据:', mappedResult)
            result.value = mappedResult
          } else {
            console.error('后端返回的data字段为空')
            result.value = null
          }
        } else {
          console.error('加载考试结果失败:', response.statusText)
          result.value = null
        }
      } catch (error) {
        console.error('加载考试结果出错:', error)
        result.value = null
      } finally {
        loading.value = false
      }
    }
    
    // 计算成绩等级的辅助函数
    const calculateGrade = (userScore, totalScore) => {
      if (totalScore === 0) return 'F'
      const percentage = (userScore / totalScore) * 100
      if (percentage >= 90) return 'A'
      if (percentage >= 80) return 'B'
      if (percentage >= 70) return 'C'
      if (percentage >= 60) return 'D'
      return 'F'
    }
    
    const filteredQuestions = computed(() => {
      if (!result.value?.questionResults) return []
      
      switch (filterType.value) {
        case 'correct':
          return result.value.questionResults.filter(q => q.correct)
        case 'wrong':
          return result.value.questionResults.filter(q => !q.correct)
        default:
          return result.value.questionResults
      }
    })
    
    const getPercentage = (score, total) => {
      return Math.round((score / total) * 100)
    }
    
    const formatDuration = (seconds) => {
      const hours = Math.floor(seconds / 3600)
      const minutes = Math.floor((seconds % 3600) / 60)
      const secs = seconds % 60
      
      if (hours > 0) {
        return `${hours}小时${minutes}分钟${secs}秒`
      } else if (minutes > 0) {
        return `${minutes}分钟${secs}秒`
      } else {
        return `${secs}秒`
      }
    }
    
    const getGradeClass = (grade) => {
      const gradeClasses = {
        'A': 'grade-a',
        'B': 'grade-b',
        'C': 'grade-c',
        'D': 'grade-d',
        'F': 'grade-f'
      }
      return gradeClasses[grade] || 'grade-f'
    }
    
    const getGradeIcon = (grade) => {
      const gradeIcons = {
        'A': '🏆',
        'B': '🥈',
        'C': '🥉',
        'D': '📚',
        'F': '😞'
      }
      return gradeIcons[grade] || '📊'
    }
    
    const getGradeText = (grade) => {
      const gradeTexts = {
        'A': '优秀',
        'B': '良好',
        'C': '中等',
        'D': '及格',
        'F': '不及格'
      }
      return gradeTexts[grade] || grade
    }
    
    const getGradeDescription = (grade) => {
      const descriptions = {
        'A': '表现优异，继续保持！',
        'B': '表现良好，再接再厉！',
        'C': '表现中等，还有提升空间',
        'D': '刚好及格，需要加强学习',
        'F': '未达标准，建议重新学习'
      }
      return descriptions[grade] || ''
    }
    
    const isUserSelected = (userAnswer, optionIndex) => {
      if (Array.isArray(userAnswer)) {
        return userAnswer.includes(optionIndex)
      }
      return userAnswer === optionIndex
    }
    
    const isCorrectAnswer = (correctAnswer, optionIndex) => {
      if (Array.isArray(correctAnswer)) {
        return correctAnswer.includes(optionIndex)
      }
      return correctAnswer === optionIndex
    }
    
    const goBack = () => {
      router.push('/exam-selection')
    }
    
    const retakeExam = () => {
      router.push(`/exercise?paperId=${result.value.examId}`)
    }
    
    const downloadResult = () => {
      // 实现下载成绩单功能
      const content = generateResultReport()
      const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `考试成绩单_${result.value.examTitle}_${new Date().toLocaleDateString()}.txt`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    }
    
    const generateResultReport = () => {
      const r = result.value
      return `
考试成绩单
==========================================
考试名称: ${r.examTitle}
考试时间: ${new Date(r.examDate).toLocaleString()}
总分: ${r.score}/${r.totalScore} (${getPercentage(r.score, r.totalScore)}%)
正确率: ${r.accuracy}%
用时: ${formatDuration(r.duration)}
成绩等级: ${getGradeText(r.grade)}
考试结果: ${r.passed ? '通过' : '未通过'}

答题详情:
------------------------------------------
${r.questionResults.map((q, i) => 
  `第${i + 1}题: ${q.correct ? '✓' : '✗'} (${q.score}/${q.maxScore}分)\n题目: ${q.question.content}\n你的答案: ${q.userAnswer || '未作答'}\n正确答案: ${q.correctAnswer}\n`
).join('\n')}
==========================================
生成时间: ${new Date().toLocaleString()}
      `.trim()
    }
    
    onMounted(() => {
      loadResult()
    })
    
    return {
      loading,
      result,
      filterType,
      filteredQuestions,
      loadResult,
      getPercentage,
      formatDuration,
      getGradeClass,
      getGradeIcon,
      getGradeText,
      getGradeDescription,
      isUserSelected,
      isCorrectAnswer,
      goBack,
      retakeExam,
      downloadResult
    }
  }
}
</script>

<style scoped>
.exam-result-container {
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

.result-header {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.result-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.result-title {
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 600;
}

.result-title {
  color: #27ae60;
}

.result-subtitle {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin: 0;
}

.score-overview {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.main-score {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 8px 25px rgba(52, 152, 219, 0.3);
}

.score-label {
  font-size: 1.2rem;
  margin-bottom: 15px;
  opacity: 0.9;
}

.score-value {
  font-size: 3rem;
  font-weight: bold;
  margin-bottom: 10px;
}

.score-percentage {
  font-size: 1.5rem;
  opacity: 0.9;
}

.score-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-item {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 2rem;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.grade-section {
  margin-bottom: 30px;
}

.grade-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 25px;
}

.grade-icon {
  font-size: 3rem;
}

.grade-title {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.grade-value {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.grade-description {
  color: #7f8c8d;
  font-size: 1rem;
}

.grade-a { border-left: 6px solid #f39c12; }
.grade-a .grade-value { color: #f39c12; }

.grade-b { border-left: 6px solid #3498db; }
.grade-b .grade-value { color: #3498db; }

.grade-c { border-left: 6px solid #95a5a6; }
.grade-c .grade-value { color: #95a5a6; }

.grade-d { border-left: 6px solid #e67e22; }
.grade-d .grade-value { color: #e67e22; }

.grade-f { border-left: 6px solid #e74c3c; }
.grade-f .grade-value { color: #e74c3c; }

.section-title {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 25px;
  text-align: center;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
  justify-content: center;
}

.filter-tab {
  padding: 10px 20px;
  border: 2px solid #ecf0f1;
  background: white;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.filter-tab:hover {
  border-color: #3498db;
}

.filter-tab.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.filter-tab.correct.active {
  background: #27ae60;
  border-color: #27ae60;
}

.filter-tab.wrong.active {
  background: #e74c3c;
  border-color: #e74c3c;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.question-result-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #bdc3c7;
}

.question-result-card.correct {
  border-left-color: #27ae60;
}

.question-result-card.wrong {
  border-left-color: #e74c3c;
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
  color: #2c3e50;
}

.question-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-icon {
  font-size: 1.2rem;
}

.status-text {
  font-weight: 500;
}

.question-score {
  background: #95a5a6;
  color: white;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
}

.question-text {
  font-size: 1.1rem;
  line-height: 1.6;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 500;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 15px;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.option-item.correct-answer {
  background: #d5f4e6;
  border-color: #27ae60;
}

.option-item.user-selected {
  background: #e3f2fd;
  border-color: #3498db;
}

.option-item.wrong-selection {
  background: #fdeaea;
  border-color: #e74c3c;
}

.option-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.option-label {
  font-weight: bold;
  margin-right: 12px;
  min-width: 25px;
}

.option-text {
  flex: 1;
}

.option-indicators {
  display: flex;
  gap: 8px;
}

.user-indicator {
  background: #3498db;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.8rem;
}

.correct-indicator {
  background: #27ae60;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.8rem;
}

.fill-comparison {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.answer-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.user-answer, .correct-answer {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-answer label, .correct-answer label {
  font-weight: bold;
  color: #2c3e50;
}

.answer-content {
  padding: 12px 15px;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  background: #f8f9fa;
  min-height: 50px;
  display: flex;
  align-items: center;
}

.answer-content.correct {
  background: #d5f4e6;
  border-color: #27ae60;
  color: #27ae60;
}

.answer-content.wrong {
  background: #fdeaea;
  border-color: #e74c3c;
  color: #e74c3c;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px 25px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.action-btn.secondary {
  background: #95a5a6;
  color: white;
}

.action-btn.secondary:hover {
  background: #7f8c8d;
}

.action-btn.primary {
  background: #3498db;
  color: white;
}

.action-btn.primary:hover {
  background: #2980b9;
}

.action-btn.success {
  background: #27ae60;
  color: white;
}

.action-btn.success:hover {
  background: #229954;
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
  .score-overview {
    grid-template-columns: 1fr;
  }
  
  .score-stats {
    grid-template-columns: 1fr;
  }
  
  .grade-card {
    flex-direction: column;
    text-align: center;
  }
  
  .answer-comparison {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
  
  .filter-tabs {
    flex-wrap: wrap;
  }
}
</style>