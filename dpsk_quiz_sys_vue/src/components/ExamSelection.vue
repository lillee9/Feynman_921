<template>
  <div class="exam-selection">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <div class="logo">
          <div class="logo-icon"></div>
          <span class="logo-text">贷后催收培训</span>
        </div>
        <div class="nav-links">
          <router-link to="/home" class="nav-link">主页</router-link>
          <router-link to="/exam-selection" class="nav-link">在线考试</router-link>
          <router-link to="/analysis" class="nav-link">考后分析</router-link>
        </div>
        <div class="user-section">
          <el-dropdown>
            <div class="user-trigger">
              <div class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="username">{{ username }}</span>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">在线考试</h1>
      <p class="page-subtitle">选择适合的考试来测试您的技能水平</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索考试名称或描述..."
          class="search-input"
          @input="handleSearch"
        />
        <i class="search-icon">🔍</i>
      </div>
      
      <div class="filter-controls">
        <select v-model="selectedStatus" @change="handleFilter" class="filter-select">
          <option value="">全部状态</option>
          <option value="available">可参加</option>
          <option value="completed">已完成</option>
          <option value="in_progress">进行中</option>
        </select>
        
        <select v-model="selectedDifficulty" @change="handleFilter" class="filter-select">
          <option value="">全部难度</option>
          <option value="simple">简单</option>
          <option value="medium">中等</option>
          <option value="hard">困难</option>
        </select>
        
        <select v-model="sortBy" @change="handleSort" class="filter-select">
          <option value="name">按名称排序</option>
            <option value="difficulty">按难度排序</option>
            <option value="duration">按时长排序</option>
            <option value="created_at_desc">最新试卷在前</option>
            <option value="created_at_asc">最早试卷在前</option>
        </select>
      </div>
    </div>

    <!-- 考试列表 -->
    <div class="exam-list">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载考试列表...</p>
      </div>
      
      <div v-else-if="paginatedExams.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无考试</h3>
        <p>{{ searchKeyword ? '没有找到匹配的考试' : '暂时没有可用的考试' }}</p>
      </div>
      
      <div v-else class="exam-grid">
        <div
          v-for="exam in paginatedExams"
          :key="exam.id"
          class="exam-card"
          :class="{
            'completed': exam.publishStatus === 'completed',
            'in-progress': exam.publishStatus === 'in_progress',
            'available': exam.publishStatus === 'available'
          }"
        >
          <div class="exam-header">
            <h3 class="exam-title">{{ exam.name }}</h3>
            <button 
              class="publish-status-btn" 
              :class="{ 'published': exam.publishStatus === 'published', 'draft': exam.publishStatus === 'draft' }"
              @click="togglePublishStatus(exam)"
              :title="exam.publishStatus === 'published' ? '点击取消发布' : '点击发布试卷'"
            >
              {{ exam.publishStatus === 'published' ? '已发布' : '未发布' }}
            </button>
          </div>
          
          <div class="exam-description">
            <p>{{ exam.description || '暂无描述' }}</p>
          </div>
          
          <div class="exam-info">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">题目数量:</span>
                <span class="info-value">{{ exam.questionCount || 0 }} 题</span>
              </div>
              <div class="info-item">
                <span class="info-label">考试时长:</span>
                <span class="info-value">{{ exam.duration || 60 }} 分钟</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">总分:</span>
                <span class="info-value">{{ exam.totalScore || 100 }} 分</span>
              </div>
              <div class="info-item">
                <span class="info-label">难度等级:</span>
                <span class="info-value difficulty" :class="exam.difficulty">
                  {{ getDifficultyText(exam.difficulty) }}
                </span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-item" v-if="exam.type">
                <span class="info-label">类型:</span>
                <span class="info-value">{{ exam.type }}</span>
              </div>
            </div>
            <div class="info-row" v-if="exam.subject || exam.createdBy">
              <div class="info-item" v-if="exam.subject">
                <span class="info-label">科目:</span>
                <span class="info-value">{{ exam.subject }}</span>
              </div>
              <div class="info-item" v-if="exam.createdBy">
                <span class="info-label">创建者:</span>
                <span class="info-value">{{ exam.createdBy }}</span>
              </div>
            </div>
            <div class="info-row" v-if="exam.startTime || exam.endTime || exam.createdAt">
              <div class="info-item" v-if="exam.startTime">
                <span class="info-label">开始时间:</span>
                <span class="info-value">{{ formatDateTime(exam.startTime) }}</span>
              </div>
              <div class="info-item" v-if="exam.endTime">
                <span class="info-label">结束时间:</span>
                <span class="info-value">{{ formatDateTime(exam.endTime) }}</span>
              </div>
            </div>
            <div class="info-row" v-if="exam.createdAt">
              <div class="info-item">
                <span class="info-label">创建时间:</span>
                <span class="info-value">{{ formatDateTime(exam.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 考试进度 -->
          <div v-if="exam.progress" class="exam-progress">
            <div class="progress-info">
              <span class="progress-label">完成进度</span>
              <span class="progress-percentage">{{ exam.progress.percentage }}%</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill" 
                :style="{ width: exam.progress.percentage + '%' }"
              ></div>
            </div>
            <div class="progress-details">
              <span>已答题: {{ exam.progress.answered }}/{{ exam.progress.total }}</span>
              <span v-if="exam.progress.score !== undefined">得分: {{ exam.progress.score }}</span>
            </div>
          </div>
          
          <div class="exam-actions">
            <div class="exam-main-actions">
              <div class="primary-actions">
                <button
                  v-if="exam.publishStatus === 'published'"
                  @click="startExam(exam)"
                  class="btn btn-primary"
                >
                  开始考试
                </button>
                
                <button
                  v-else-if="exam.publishStatus === 'draft'"
                  disabled
                  class="btn btn-secondary"
                  title="试卷未发布，无法参加考试"
                >
                  未发布
                </button>
                
                <button
                  @click="previewExam(exam)"
                  class="btn btn-preview"
                >
                  预览试卷
                </button>
              </div>
            </div>
            <div class="exam-management-actions">
              <button 
                @click="showRenameModal(exam)"
                class="btn btn-management"
                title="重命名试卷"
              >
                <i class="icon-edit">✏️</i>
              </button>
              <button 
                @click="showDeleteModal(exam)"
                class="btn btn-management btn-danger"
                :class="{ 'btn-disabled': exam.publishStatus === 'published' }"
                :disabled="exam.publishStatus === 'published'"
                :title="exam.publishStatus === 'published' ? '已发布的试卷不能删除，请先取消发布' : '删除试卷'"
              >
                <i class="icon-delete">🗑️</i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页控件 -->
    <div v-if="totalPages > 1" class="pagination">
      <button
        @click="goToPage(currentPage - 1)"
        :disabled="currentPage === 1"
        class="pagination-btn"
      >
        上一页
      </button>
      
      <div class="pagination-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="goToPage(page)"
          :class="['pagination-number', { active: page === currentPage }]"
        >
          {{ page }}
        </button>
      </div>
      
      <button
        @click="goToPage(currentPage + 1)"
        :disabled="currentPage === totalPages"
        class="pagination-btn"
      >
        下一页
      </button>
    </div>

    <!-- 考试确认模态框 -->
    <div v-if="showConfirmModal" class="modal-overlay" @click="closeConfirmModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认开始考试</h3>
          <button @click="closeConfirmModal" class="modal-close">×</button>
        </div>
        
        <div class="modal-body">
          <div class="exam-details">
            <h4>{{ selectedExam?.name }}</h4>
            <p class="exam-desc">{{ selectedExam?.description }}</p>
            
            <div class="exam-rules">
              <h5>考试须知:</h5>
              <ul>
                <li>考试时长: {{ selectedExam?.duration || 60 }} 分钟</li>
                <li>题目数量: {{ selectedExam?.questionCount || 0 }} 题</li>

                <li>考试过程中请勿刷新页面或关闭浏览器</li>
                <li>考试开始后将自动计时，请合理安排答题时间</li>
                <li>提交后无法修改答案，请仔细检查后再提交</li>
              </ul>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeConfirmModal" class="btn btn-secondary">
            取消
          </button>
          <button @click="confirmStartExam" class="btn btn-primary">
            确认开始
          </button>
        </div>
      </div>
    </div>

    <!-- 重命名试卷模态框 -->
    <div v-if="showRenameModalFlag" class="modal-overlay" @click="closeRenameModal">
      <div class="modal-content" @click.stop>
        <h3>重命名试卷</h3>
        <div class="form-group">
          <label for="newPaperName">试卷名称：</label>
          <input 
            id="newPaperName"
            v-model="newPaperName" 
            type="text" 
            class="form-input"
            placeholder="请输入新的试卷名称"
            @keyup.enter="confirmRename"
          />
        </div>
        <div class="modal-actions">
          <button @click="closeRenameModal" class="btn btn-secondary">取消</button>
          <button @click="confirmRename" class="btn btn-primary" :disabled="!newPaperName.trim()">确认重命名</button>
        </div>
      </div>
    </div>

    <!-- 删除试卷确认模态框 -->
    <div v-if="showDeleteModalFlag" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content" @click.stop>
        <h3>确认删除试卷</h3>
        <p>您确定要删除试卷 "{{ selectedExamForManagement?.name }}" 吗？</p>
        <p class="modal-warning">此操作不可撤销，删除后将无法恢复试卷数据。</p>
        <div class="modal-actions">
          <button @click="closeDeleteModal" class="btn btn-secondary">取消</button>
          <button @click="confirmDelete" class="btn btn-danger">确认删除</button>
        </div>
      </div>
    </div>
    </main>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '../config/api'
import { getToken, requireAuth, authFetch } from '../utils/auth'

export default {
  name: 'ExamSelection',
  setup() {
    const router = useRouter()
    
    // 认证检查函数
    const checkAuth = () => {
      const token = getToken()
      if (!token) {
        router.push('/login')
        return false
      }
      return true
    }
    
    // 响应式数据
    const loading = ref(false)
    const exams = ref([])
    const searchKeyword = ref('')
    const selectedStatus = ref('')
    const selectedDifficulty = ref('')
    const sortBy = ref('name')
    const currentPage = ref(1)
    const pageSize = ref(6)
    const showConfirmModal = ref(false)
    const selectedExam = ref(null)
    
    // 试卷管理相关状态
    const showRenameModalFlag = ref(false)
    const showDeleteModalFlag = ref(false)
    const selectedExamForManagement = ref(null)
    const newPaperName = ref('')
    
    // 计算属性
    const filteredExams = computed(() => {
      let result = exams.value
      
      // 搜索过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(exam => 
          exam.name.toLowerCase().includes(keyword) ||
          (exam.description && exam.description.toLowerCase().includes(keyword))
        )
      }
      
      // 状态过滤
      if (selectedStatus.value) {
        result = result.filter(exam => exam.publishStatus === selectedStatus.value)
      }
      
      // 难度过滤
      if (selectedDifficulty.value) {
        result = result.filter(exam => exam.difficulty === selectedDifficulty.value)
      }
      
      // 排序
      result.sort((a, b) => {
        switch (sortBy.value) {
          case 'name':
            return a.name.localeCompare(b.name)
          case 'difficulty':
            const difficultyOrder = { 'beginner': 1, 'intermediate': 2, 'advanced': 3 }
            return difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty]
          case 'duration':
            return (a.duration || 0) - (b.duration || 0)
          case 'created_at_desc':
             return new Date(b.generateTime || 0) - new Date(a.generateTime || 0) // 最新在前（降序）
           case 'created_at_asc':
             return new Date(a.generateTime || 0) - new Date(b.generateTime || 0) // 最早在前（升序）
          default:
            return 0
        }
      })
      
      return result
    })
    
    const totalPages = computed(() => {
      return Math.ceil(filteredExams.value.length / pageSize.value)
    })
    
    const paginatedExams = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredExams.value.slice(start, end)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i)
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i)
          }
          pages.push('...', total)
        } else if (current >= total - 3) {
          pages.push(1, '...')
          for (let i = total - 4; i <= total; i++) {
            pages.push(i)
          }
        } else {
          pages.push(1, '...')
          for (let i = current - 1; i <= current + 1; i++) {
            pages.push(i)
          }
          pages.push('...', total)
        }
      }
      
      return pages
    })
    
    // 方法
    const loadExams = async () => {
      if (!checkAuth()) return
      
      loading.value = true
      try {
        const result = await authFetch(`${API_BASE_URL}/exam/papers/available`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }, router)
        console.log('API响应:', result)
        
        if (result.code === 200 && result.data) {
          // 转换后端数据格式为前端需要的格式
          exams.value = result.data.map(paper => ({
            id: paper.paperId,
            name: paper.paperName,
            description: paper.description || '暂无描述',
            publishStatus: paper.status || 'draft', // 统一使用publishStatus字段
            difficulty: mapDifficulty(paper.difficulty),
            duration: paper.duration || 60,
            questionCount: paper.questionCount || paper.totalQuestion || 0,
            passingScore: paper.passingScore || 60,
            startTime: paper.startTime,
            endTime: paper.endTime,
            totalScore: paper.totalScore || 100,
            createdAt: paper.generateTime, // 添加创建时间字段映射
            generateTime: paper.generateTime // 添加generateTime字段以支持排序
          }))
        } else {
          console.error('API返回错误:', result.message)
          exams.value = []
        }
      } catch (error) {
        console.error('加载考试列表失败:', error)
        exams.value = []
      } finally {
        loading.value = false
      }
    }
    
    // 映射难度等级
    const mapDifficulty = (difficulty) => {
      const difficultyMap = {
        'easy': 'simple',
        'medium': 'medium', 
        'hard': 'hard'
      }
      return difficultyMap[difficulty] || 'simple'
    }
    
    const handleSearch = () => {
      currentPage.value = 1
    }
    
    const handleFilter = () => {
      currentPage.value = 1
    }
    
    const handleSort = () => {
      currentPage.value = 1
    }
    
    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        'available': '可参加',
        'in_progress': '进行中',
        'completed': '已完成'
      }
      return statusMap[status] || '未知'
    }
    
    const getDifficultyText = (difficulty) => {
      const difficultyMap = {
        'simple': '简单',
        'medium': '中等',
        'hard': '困难'
      }
      return difficultyMap[difficulty] || '未知'
    }
    
    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN')
    }
    
    const startExam = (exam) => {
      selectedExam.value = exam
      showConfirmModal.value = true
    }
    
    const continueExam = (exam) => {
      console.log('继续考试:', exam)
      router.push(`/exam/${exam.id}`)
    }
    
    const viewResults = (exam) => {
      console.log('查看结果:', exam)
      router.push(`/exam-result/${exam.id}`)
    }
    
    const previewExam = (exam) => {
      console.log('预览试卷:', exam)
      // 跳转到PaperPreview页面，传递试卷ID作为参数
      router.push({ name: 'PaperPreview', query: { paperId: exam.id } })
    }
    
    const closeConfirmModal = () => {
      showConfirmModal.value = false
      selectedExam.value = null
    }
    
    const confirmStartExam = () => {
      if (selectedExam.value) {
        console.log('确认开始考试:', selectedExam.value)
        // 跳转到Exercise页面，传递试卷ID作为参数
        router.push({ name: 'Exercise', query: { paperId: selectedExam.value.id } })
      }
      closeConfirmModal()
    }
    
    // 试卷管理模态框控制
    const showRenameModal = (exam) => {
      selectedExamForManagement.value = exam
      newPaperName.value = exam.name
      showRenameModalFlag.value = true
    }
    
    const closeRenameModal = () => {
      showRenameModalFlag.value = false
      selectedExamForManagement.value = null
      newPaperName.value = ''
    }
    
    const showDeleteModal = (exam) => {
      // 检查试卷是否已发布
      if (exam.publishStatus === 'published') {
        alert('⚠️ 删除限制\n\n已发布的试卷不能直接删除。\n\n解决方案：\n1. 请先在试卷管理页面取消发布该试卷\n2. 取消发布后即可正常删除\n\n这样做是为了保护正在进行的考试数据。')
        return
      }
      selectedExamForManagement.value = exam
      showDeleteModalFlag.value = true
    }
    
    const closeDeleteModal = () => {
      showDeleteModalFlag.value = false
      selectedExamForManagement.value = null
    }
    
    const confirmRename = async () => {
       if (!newPaperName.value.trim()) return
       if (!checkAuth()) return
       
       try {
         const result = await authFetch(`${API_BASE_URL}/exam/papers/${selectedExamForManagement.value.id}`, {
           method: 'PUT',
           body: JSON.stringify({
             ...selectedExamForManagement.value,
             name: newPaperName.value.trim()
           })
         }, router)
         
         if (result.code === 200) {
           // 更新本地数据
           const exam = exams.value.find(e => e.id === selectedExamForManagement.value.id)
           if (exam) {
             exam.name = newPaperName.value.trim()
           }
           alert('试卷重命名成功！')
         } else {
           alert('重命名失败：' + result.message)
         }
         closeRenameModal()
       } catch (error) {
         console.error('重命名失败:', error)
         alert('重命名失败，请检查网络连接')
         closeRenameModal()
       }
     }
     
     const togglePublishStatus = async (exam) => {
       if (!checkAuth()) return
       
       const isPublished = exam.publishStatus === 'published'
       const action = isPublished ? 'unpublish' : 'publish'
       const actionText = isPublished ? '取消发布' : '发布'
       
       try {
         const result = await authFetch(`${API_BASE_URL}/exam/papers/${exam.id}/${action}`, {
           method: 'POST'
         }, router)
         
         if (result.code === 200) {
           // 更新本地数据
           exam.publishStatus = isPublished ? 'draft' : 'published'
           alert(`试卷${actionText}成功！`)
         } else {
           alert(`${actionText}失败：` + (result.message || '未知错误'))
         }
       } catch (error) {
         console.error(`${actionText}失败:`, error)
         alert(`${actionText}失败，请检查网络连接`)
       }
     }
     
     const confirmDelete = async () => {
       if (!checkAuth()) return
       
       // 检查试卷发布状态
       if (selectedExamForManagement.value.publishStatus === 'published') {
         alert('已发布的试卷不能直接删除，请先取消发布后再删除。')
         closeDeleteModal()
         return
       }
       
       try {
         const result = await authFetch(`${API_BASE_URL}/exam/papers/${selectedExamForManagement.value.id}`, {
           method: 'DELETE'
         }, router)
         
         if (result.code === 200) {
           // 从本地数据中移除
           exams.value = exams.value.filter(e => e.id !== selectedExamForManagement.value.id)
           alert('试卷删除成功！')
         } else {
           alert('删除失败：' + (result.message || '未知错误'))
         }
         closeDeleteModal()
       } catch (error) {
         console.error('删除失败:', error)
         let errorMessage = '❌ 删除失败\n\n'
         
         if (error.message && error.message.includes('400')) {
           errorMessage += '原因：已发布的试卷不能删除\n\n解决方案：\n1. 前往试卷管理页面\n2. 取消发布该试卷\n3. 返回此页面重新删除\n\n💡 提示：这样做是为了保护正在进行的考试。'
         } else if (error.message && error.message.includes('404')) {
           errorMessage += '原因：试卷不存在或已被删除\n\n可能的情况：\n• 试卷已被其他用户删除\n• 试卷ID无效\n\n建议：刷新页面查看最新状态'
         } else if (error.message && error.message.includes('403')) {
           errorMessage += '原因：权限不足\n\n可能的情况：\n• 您不是试卷的创建者\n• 登录状态已过期\n\n建议：重新登录后再试'
         } else {
           errorMessage += '原因：网络连接异常\n\n建议：\n• 检查网络连接\n• 稍后重试\n• 联系技术支持'
         }
         
         alert(errorMessage)
         closeDeleteModal()
       }
     }
    
    // 导航栏相关方法
    const goToHome = () => {
      router.push('/')
    }
    
    const goToAnalysis = () => {
      router.push('/analysis')
    }
    
    const logout = () => {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      ElMessage.success('退出登录成功')
      router.push('/login')
    }
    
    // 获取用户名
    const username = ref(localStorage.getItem('username') || '用户')
    
    // 生命周期
    onMounted(() => {
      loadExams()
    })
    
    return {
      loading,
      exams,
      searchKeyword,
      selectedStatus,
      selectedDifficulty,
      sortBy,
      currentPage,
      pageSize,
      showConfirmModal,
      selectedExam,
      // 试卷管理相关
      showRenameModalFlag,
      showDeleteModalFlag,
      selectedExamForManagement,
      newPaperName,
      filteredExams,
      totalPages,
      paginatedExams,
      visiblePages,
      loadExams,
      handleSearch,
      handleFilter,
      handleSort,
      goToPage,
      getStatusText,
      getDifficultyText,
      formatDateTime,
      startExam,
      continueExam,
      viewResults,
      previewExam,
      closeConfirmModal,
      confirmStartExam,
      // 试卷管理方法
      showRenameModal,
      closeRenameModal,
      showDeleteModal,
      closeDeleteModal,
      confirmRename,
      confirmDelete,
      togglePublishStatus,
      mapDifficulty,
      // 导航栏相关
      goToHome,
      goToAnalysis,
      logout,
      username,
      UserFilled,
      CaretBottom
    }
  }
}
</script>

<style scoped>
/* 导航栏样式 */
.navbar {
  background: white;
  padding: 0;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #2c3e50;
  font-weight: 600;
  font-size: 1.5rem;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
}

.logo-text {
  font-weight: 700;
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: 30px;
  align-items: center;
}

.nav-link {
  color: #2c3e50;
  text-decoration: none;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.nav-link.active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.15);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-trigger:hover {
  background: rgba(102, 126, 234, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.username {
  font-weight: 500;
  margin: 0 4px;
}

.dropdown-icon {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.main-content {
  padding-top: 0;
}

.exam-selection {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: white;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 600;
  text-shadow: none;
}

.page-subtitle {
  font-size: 1.1rem;
  color: #7f8c8d;
  margin: 0;
}

.search-filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.search-box {
  position: relative;
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  padding: 12px 45px 12px 15px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.search-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #95a5a6;
  font-size: 18px;
}

.filter-controls {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-select {
  padding: 10px 15px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #3498db;
}

.exam-list {
  margin-bottom: 30px;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-state h3 {
  color: #2c3e50;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 25px;
}

.exam-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-left: 4px solid #3498db;
}

.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.exam-card.completed {
  border-left-color: #27ae60;
}

.exam-card.in-progress {
  border-left-color: #f39c12;
}

.exam-card.available {
  border-left-color: #3498db;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.exam-title {
  font-size: 1.3rem;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
  flex: 1;
  margin-right: 15px;
}

.exam-status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.exam-status.available {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.exam-status.in_progress {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.exam-status.completed {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
}

/* 发布状态按钮样式 */
.publish-status-btn {
  font-size: 0.85rem;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
  color: white;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.publish-status-btn.published {
  background-color: #2ecc71;
}

.publish-status-btn.published:hover {
  background-color: #27ae60;
}

.publish-status-btn.draft {
  background-color: #95a5a6;
}

.publish-status-btn.draft:hover {
  background-color: #7f8c8d;
}

.exam-description {
  margin-bottom: 20px;
}

.exam-description p {
  color: #7f8c8d;
  line-height: 1.6;
  margin: 0;
}

.exam-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.info-item {
  flex: 1;
}

.info-label {
  font-size: 13px;
  color: #95a5a6;
  display: block;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 600;
}

.exam-details {
  margin: 12px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #3b82f6;
}

.exam-details > div {
  margin-bottom: 6px;
  font-size: 13px;
}

.exam-details > div:last-child {
  margin-bottom: 0;
}

.exam-details strong {
  color: #374151;
  margin-right: 6px;
}

.exam-total-score {
  background: #dcfce7;
  color: #166534;
  font-weight: 600;
}

.info-value.difficulty.beginner {
  color: #27ae60;
}

.info-value.difficulty.intermediate {
  color: #f39c12;
}

.info-value.difficulty.advanced {
  color: #e74c3c;
}

.exam-progress {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-label {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 600;
}

.progress-percentage {
  font-size: 14px;
  color: #3498db;
  font-weight: 600;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #e1e8ed;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3498db, #2980b9);
  transition: width 0.3s ease;
}

.progress-details {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #7f8c8d;
}

.exam-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.exam-main-actions {
  display: flex;
  gap: 8px;
  flex: 1;
}

.primary-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.exam-management-actions {
  display: flex;
  gap: 4px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  position: relative;
  overflow: hidden;
  min-width: 110px;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.btn:hover::before {
  left: 100%;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.btn-warning:hover {
  background: linear-gradient(135deg, #ee82f0 0%, #f3455a 100%);
}

.btn-success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.btn-success:hover {
  background: linear-gradient(135deg, #3d9bfe 0%, #00e0fe 100%);
}

.btn-preview {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #333;
  font-weight: 600;
}

.btn-preview:hover {
  background: linear-gradient(135deg, #96e6e2 0%, #fec4d1 100%);
  color: #222;
}

.btn-management {
  padding: 8px;
  min-width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  color: #6c757d;
  font-size: 14px;
}

.btn-management:hover {
  background: #e9ecef;
  color: #495057;
}

.btn-management.btn-danger {
  background: #fff5f5;
  border-color: #fed7d7;
  color: #e53e3e;
}

.btn-management.btn-danger:hover {
  background: #fed7d7;
  color: #c53030;
}

.btn-management.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #ccc !important;
}

.btn-management.btn-disabled:hover {
  background-color: #ccc !important;
  transform: none;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 30px;
}

.pagination-btn {
  padding: 10px 15px;
  border: 2px solid #e1e8ed;
  background: white;
  color: #2c3e50;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #3498db;
  color: #3498db;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-numbers {
  display: flex;
  gap: 5px;
}

.pagination-number {
  padding: 10px 12px;
  border: 2px solid #e1e8ed;
  background: white;
  color: #2c3e50;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 40px;
}

.pagination-number:hover {
  border-color: #3498db;
  color: #3498db;
}

.pagination-number.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
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
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e1e8ed;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #95a5a6;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-close:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 20px;
}

.exam-details h4 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.exam-desc {
  color: #7f8c8d;
  margin-bottom: 20px;
  line-height: 1.6;
}

.exam-rules {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
}

.exam-rules h5 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.exam-rules ul {
  margin: 0;
  padding-left: 20px;
}

.exam-rules li {
  color: #7f8c8d;
  margin-bottom: 5px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e1e8ed;
}

@media (max-width: 768px) {
  .exam-selection {
    padding: 15px;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .exam-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-controls {
    flex-direction: column;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .exam-actions {
    flex-direction: column;
  }
  
  .primary-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .btn {
    width: 100%;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
  
  .modal-content {
    width: 95%;
    margin: 20px;
  }
}

@media (max-width: 480px) {
  .info-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .pagination-numbers {
    flex-wrap: wrap;
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e1e8ed;
}

.modal-actions button:last-child {
  margin-left: 8px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.modal-warning {
  color: #dc2626;
  font-size: 14px;
  margin: 10px 0;
}
</style>