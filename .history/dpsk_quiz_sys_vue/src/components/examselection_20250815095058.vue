<template>
  <div class="exam-selection">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">技能测试</h1>
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
        <select
          v-model="selectedStatus"
          @change="handleFilter"
          class="filter-select"
        >
          <option value="">全部状态</option>
          <option value="available">可参加</option>
          <option value="completed">已完成</option>
          <option value="in_progress">进行中</option>
        </select>

        <select
          v-model="selectedDifficulty"
          @change="handleFilter"
          class="filter-select"
        >
          <option value="">全部难度</option>
          <option value="beginner">初级</option>
          <option value="intermediate">中级</option>
          <option value="advanced">高级</option>
        </select>

        <select v-model="sortBy" @change="handleSort" class="filter-select">
          <option value="name">按名称排序</option>
          <option value="difficulty">按难度排序</option>
          <option value="duration">按时长排序</option>
          <option value="created_at">按创建时间排序</option>
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
        <p>{{ searchKeyword ? "没有找到匹配的考试" : "暂时没有可用的考试" }}</p>
      </div>

      <div v-else class="exam-grid">
        <div
          v-for="exam in paginatedExams"
          :key="exam.id"
          class="exam-card"
          :class="{
            completed: exam.status === 'completed',
            'in-progress': exam.status === 'in_progress',
            available: exam.status === 'available',
          }"
        >
          <div class="exam-header">
            <h3 class="exam-title">{{ exam.name }}</h3>
            <span class="exam-status" :class="exam.status">
              {{ getStatusText(exam.status) }}
            </span>
          </div>

          <div class="exam-description">
            <p>{{ exam.description || "暂无描述" }}</p>
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
                <span class="info-label">难度等级:</span>
                <span class="info-value difficulty" :class="exam.difficulty">
                  {{ getDifficultyText(exam.difficulty) }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">及格分数:</span>
                <span class="info-value">{{ exam.passingScore || 60 }} 分</span>
              </div>
            </div>
            <div class="info-row" v-if="exam.startTime || exam.endTime">
              <div class="info-item" v-if="exam.startTime">
                <span class="info-label">开始时间:</span>
                <span class="info-value">{{
                  formatDateTime(exam.startTime)
                }}</span>
              </div>
              <div class="info-item" v-if="exam.endTime">
                <span class="info-label">结束时间:</span>
                <span class="info-value">{{
                  formatDateTime(exam.endTime)
                }}</span>
              </div>
            </div>
          </div>

          <!-- 考试进度 -->
          <div v-if="exam.progress" class="exam-progress">
            <div class="progress-info">
              <span class="progress-label">完成进度</span>
              <span class="progress-percentage"
                >{{ exam.progress.percentage }}%</span
              >
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: exam.progress.percentage + '%' }"
              ></div>
            </div>
            <div class="progress-details">
              <span
                >已答题: {{ exam.progress.answered }}/{{
                  exam.progress.total
                }}</span
              >
              <span v-if="exam.progress.score !== undefined"
                >得分: {{ exam.progress.score }}</span
              >
            </div>
          </div>

          <div class="exam-actions">
            <button
              v-if="exam.status === 'available'"
              @click="startExam(exam)"
              class="btn btn-primary"
            >
              开始考试
            </button>

            <button
              v-else-if="exam.status === 'in_progress'"
              @click="continueExam(exam)"
              class="btn btn-warning"
            >
              继续考试
            </button>

            <button
              v-else-if="exam.status === 'completed'"
              @click="viewResults(exam)"
              class="btn btn-success"
            >
              查看结果
            </button>

            <button @click="previewExam(exam)" class="btn btn-secondary">
              预览试卷
            </button>
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
    <div
      v-if="showConfirmModal"
      class="modal-overlay"
      @click="closeConfirmModal"
    >
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
                <li>及格分数: {{ selectedExam?.passingScore || 60 }} 分</li>
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
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

export default {
  name: "ExamSelection",
  setup() {
    const router = useRouter();

    // 响应式数据
    const loading = ref(false);
    const exams = ref([]);
    const searchKeyword = ref("");
    const selectedStatus = ref("");
    const selectedDifficulty = ref("");
    const sortBy = ref("name");
    const currentPage = ref(1);
    const pageSize = ref(6);
    const showConfirmModal = ref(false);
    const selectedExam = ref(null);

    // 模拟考试数据
    const mockExams = [
      {
        id: 1,
        name: "JavaScript 基础测试",
        description: "测试 JavaScript 基础语法、变量、函数等核心概念",
        status: "available",
        difficulty: "beginner",
        duration: 45,
        questionCount: 20,
        passingScore: 70,
        startTime: "2024-01-15 09:00:00",
        endTime: "2024-12-31 23:59:59",
      },
      {
        id: 2,
        name: "Vue.js 进阶开发",
        description: "深入测试 Vue.js 组件开发、状态管理、路由等高级特性",
        status: "in_progress",
        difficulty: "intermediate",
        duration: 90,
        questionCount: 35,
        passingScore: 75,
        progress: {
          percentage: 65,
          answered: 23,
          total: 35,
          score: 78,
        },
      },
      {
        id: 3,
        name: "Node.js 后端开发",
        description: "测试 Node.js 服务器开发、API 设计、数据库操作等技能",
        status: "completed",
        difficulty: "advanced",
        duration: 120,
        questionCount: 40,
        passingScore: 80,
        progress: {
          percentage: 100,
          answered: 40,
          total: 40,
          score: 85,
        },
      },
      {
        id: 4,
        name: "React 组件开发",
        description: "测试 React 组件设计、Hooks 使用、状态管理等核心技能",
        status: "available",
        difficulty: "intermediate",
        duration: 75,
        questionCount: 30,
        passingScore: 75,
      },
      {
        id: 5,
        name: "CSS 样式设计",
        description: "测试 CSS 布局、动画、响应式设计等前端样式技能",
        status: "available",
        difficulty: "beginner",
        duration: 60,
        questionCount: 25,
        passingScore: 70,
      },
    ];

    // 计算属性
    const filteredExams = computed(() => {
      let result = exams.value;

      // 搜索过滤
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        result = result.filter(
          (exam) =>
            exam.name.toLowerCase().includes(keyword) ||
            (exam.description &&
              exam.description.toLowerCase().includes(keyword))
        );
      }

      // 状态过滤
      if (selectedStatus.value) {
        result = result.filter((exam) => exam.status === selectedStatus.value);
      }

      // 难度过滤
      if (selectedDifficulty.value) {
        result = result.filter(
          (exam) => exam.difficulty === selectedDifficulty.value
        );
      }

      // 排序
      result.sort((a, b) => {
        switch (sortBy.value) {
          case "name":
            return a.name.localeCompare(b.name);
          case "difficulty":
            const difficultyOrder = {
              beginner: 1,
              intermediate: 2,
              advanced: 3,
            };
            return (
              difficultyOrder[a.difficulty] - difficultyOrder[b.difficulty]
            );
          case "duration":
            return (a.duration || 0) - (b.duration || 0);
          case "created_at":
            return new Date(b.created_at || 0) - new Date(a.created_at || 0);
          default:
            return 0;
        }
      });

      return result;
    });

    const totalPages = computed(() => {
      return Math.ceil(filteredExams.value.length / pageSize.value);
    });

    const paginatedExams = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filteredExams.value.slice(start, end);
    });

    const visiblePages = computed(() => {
      const pages = [];
      const total = totalPages.value;
      const current = currentPage.value;

      if (total <= 7) {
        for (let i = 1; i <= total; i++) {
          pages.push(i);
        }
      } else {
        if (current <= 4) {
          for (let i = 1; i <= 5; i++) {
            pages.push(i);
          }
          pages.push("...", total);
        } else if (current >= total - 3) {
          pages.push(1, "...");
          for (let i = total - 4; i <= total; i++) {
            pages.push(i);
          }
        } else {
          pages.push(1, "...");
          for (let i = current - 1; i <= current + 1; i++) {
            pages.push(i);
          }
          pages.push("...", total);
        }
      }

      return pages;
    });

    // 方法
    const loadExams = async () => {
      loading.value = true;
      try {
        // 模拟 API 调用
        await new Promise((resolve) => setTimeout(resolve, 1000));
        exams.value = mockExams;
      } catch (error) {
        console.error("加载考试列表失败:", error);
      } finally {
        loading.value = false;
      }
    };

    const handleSearch = () => {
      currentPage.value = 1;
    };

    const handleFilter = () => {
      currentPage.value = 1;
    };

    const handleSort = () => {
      currentPage.value = 1;
    };

    const goToPage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
      }
    };

    const getStatusText = (status) => {
      const statusMap = {
        available: "可参加",
        in_progress: "进行中",
        completed: "已完成",
      };
      return statusMap[status] || "未知";
    };

    const getDifficultyText = (difficulty) => {
      const difficultyMap = {
        beginner: "初级",
        intermediate: "中级",
        advanced: "高级",
      };
      return difficultyMap[difficulty] || "未知";
    };

    const formatDateTime = (dateTime) => {
      if (!dateTime) return "";
      const date = new Date(dateTime);
      return date.toLocaleString("zh-CN");
    };

    const startExam = (exam) => {
      selectedExam.value = exam;
      showConfirmModal.value = true;
    };

    const continueExam = (exam) => {
      console.log("继续考试:", exam);
      router.push(`/exam/${exam.id}`);
    };

    const viewResults = (exam) => {
      console.log("查看结果:", exam);
      router.push(`/exam-result/${exam.id}`);
    };

    const previewExam = (exam) => {
      console.log("预览试卷:", exam);
      router.push(`/exam-preview/${exam.id}`);
    };

    const closeConfirmModal = () => {
      showConfirmModal.value = false;
      selectedExam.value = null;
    };

    const confirmStartExam = () => {
      if (selectedExam.value) {
        console.log("确认开始考试:", selectedExam.value);
        router.push(`/exam/${selectedExam.value.id}`);
      }
      closeConfirmModal();
    };

    // 生命周期
    onMounted(() => {
      loadExams();
    });

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
    };
  },
};
</script>

<style scoped>
.exam-selection {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
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
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-left: 4px solid #3498db;
}

.exam-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
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
  background: #e8f5e8;
  color: #27ae60;
}

.exam-status.in_progress {
  background: #fef9e7;
  color: #f39c12;
}

.exam-status.completed {
  background: #e8f4fd;
  color: #3498db;
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
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-warning {
  background: #f39c12;
  color: white;
}

.btn-warning:hover {
  background: #e67e22;
  transform: translateY(-2px);
}

.btn-success {
  background: #27ae60;
  color: white;
}

.btn-success:hover {
  background: #229954;
  transform: translateY(-2px);
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
  transform: translateY(-2px);
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
  font-weight: 600;
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
  width: 40px;
  height: 40px;
  border: 2px solid #e1e8ed;
  background: white;
  color: #2c3e50;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
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
  align-items: center;
  justify-content: center;
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
  padding: 20px 25px;
  border-bottom: 1px solid #e1e8ed;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.3rem;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #95a5a6;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-close:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 25px;
}

.exam-details h4 {
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 1.2rem;
}

.exam-desc {
  color: #7f8c8d;
  margin-bottom: 20px;
  line-height: 1.6;
}

.exam-rules h5 {
  color: #2c3e50;
  margin: 0 0 15px 0;
  font-size: 1rem;
}

.exam-rules ul {
  margin: 0;
  padding-left: 20px;
}

.exam-rules li {
  color: #7f8c8d;
  margin-bottom: 8px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px 25px;
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

  .info-row {
    flex-direction: column;
    gap: 10px;
  }

  .exam-actions {
    flex-direction: column;
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
</style>
