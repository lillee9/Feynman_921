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
    <div class="update-info">
      <span>数据更新时间：{{ chartData.updateTime }}</span>
      <span>总参与人数：{{ chartData.totalUsers }}</span>
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

const chartData = ref({
  easy: [],
  medium: [],
  hard: [],
});

// 新增API请求方法
const fetchChartData = async () => {
  try {
    const response = await fetch("/api/chart");
    const data = await response.json();
    chartData.value = data;

    // 更新图表
    initChart(easyChart, "简单难度成绩分布", chartData.value.easy);
    initChart(mediumChart, "中等难度成绩分布", chartData.value.medium);
    initChart(hardChart, "困难难度成绩分布", chartData.value.hard);
  } catch (error) {
    console.error("获取数据失败:", error);
  }
};

onMounted(() => {
  fetchChartData(); // 替换原有的initChart调用
});
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
  initChart(easyChart, "简单难度成绩分布", chartData.value.easy);
  initChart(mediumChart, "中等难度成绩分布", chartData.value.medium);
  initChart(hardChart, "困难难度成绩分布", chartData.value.hard);
});
</script>

<style scoped>
/* 导航栏样式（与Quiz.vue完全一致） */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
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
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}
.nav-links {
  display: flex;
  gap: 32px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: background-color 0.3s ease;
}
.dropdown-icon {
  color: #86868b;
}

.chart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

/* 标题样式统一 */
.page-title {
  font-size: 48px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 24px;
}

.chart-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica,
    Arial, sans-serif;
}
.page-title {
  font-size: 36px;
}

.page-header {
  margin-bottom: 30px;
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
