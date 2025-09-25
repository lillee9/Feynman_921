<template>
  <div class="chart-container">
    <!-- 顶部导航栏（与Quiz.vue一致） -->
    <el-header class="navbar">
      <div class="nav-content">
        <div class="logo">
          <div class="logo-icon"></div>
          <span class="logo-text">Deep Quiz</span>
        </div>
        <div class="nav-links">
          <router-link to="/home" class="nav-link">主页</router-link>
          <router-link to="/analysis" class="nav-link">错题分析</router-link>
          <a href="#" class="nav-link">题目生成</a>
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
    </el-header>

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">培训成果统计</h1>
    </div>

    <!-- 三个饼形图布局 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="chart-card">
          <h3 class="chart-title">简单难度</h3>
          <div ref="easyChart" class="chart-placeholder"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3 class="chart-title">中等难度</h3>
          <div ref="mediumChart" class="chart-placeholder"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <h3 class="chart-title">困难难度</h3>
          <div ref="hardChart" class="chart-placeholder"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import { UserFilled, CaretBottom } from "@element-plus/icons-vue";

// 模拟数据（实际需从后端接口获取）
const mockData = {
  easy: [20, 30, 40, 10], // 优秀、良好、及格、不及格比例
  medium: [15, 25, 50, 10],
  hard: [10, 20, 45, 25],
};

// 图表容器引用
const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);

// 初始化图表函数
const initChart = (dom, title, data) => {
  const chart = echarts.init(dom.value);
  const option = {
    title: { text: title, left: "center" },
    tooltip: { trigger: "item" },
    legend: { orient: "vertical", left: "left" },
    series: [
      {
        type: "pie",
        radius: "70%",
        data: [
          { value: data[0], name: "优秀" },
          { value: data[1], name: "良好" },
          { value: data[2], name: "及格" },
          { value: data[3], name: "不及格" },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
  chart.setOption(option);
  return chart;
};

onMounted(() => {
  // 初始化三个图表
  initChart(easyChart, "简单难度成绩分布", mockData.easy);
  initChart(mediumChart, "中等难度成绩分布", mockData.medium);
  initChart(hardChart, "困难难度成绩分布", mockData.hard);
});
</script>

<style scoped>
/* 导航栏样式（与Quiz.vue完全一致） */
.navbar {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-link {
  color: #1d1d1f;
  font-size: 16px;
}

.nav-link:hover {
  color: #667eea;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.username {
  color: #1d1d1f;
}
.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.logo-icon {
  width: 32px;
  height: 32px;
  background: white;
}
.logo-text {
  font-size: 20px;
  font-weight: 500;
  color: white;
}
.nav-links {
  display: flex;
  gap: 2rem;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.dropdown-icon {
  font-size: 0.8rem;
  color: white;
}

/* 页面主体样式 */
.chart-container {
  padding: 20px;
  background: #f5f7fa;
}
.page-header {
  margin-bottom: 30px;
}
.page-title {
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}
.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.chart-title {
  font-size: 18px;
  color: #606266;
  margin-bottom: 15px;
  font-weight: 500;
}
.chart-placeholder {
  width: 100%;
  height: 300px;
}
</style>
