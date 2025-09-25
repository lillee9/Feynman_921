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

    <!-- 上半部分 -->
    <el-row :gutter="20" class="top-section">
      <!-- 左三分之一：update-info -->
      <el-col :span="8">
        <div class="update-info">
          <span>数据更新时间：{{ chartData.updateTime }}</span>
          <span>总参与人数：{{ chartData.totalUsers }}</span>
        </div>
      </el-col>

      <!-- 右三分之二：三个饼形图 -->
      <el-col :span="16">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="chart-card">
              <h3 class="chart-title">简单难度情况分布</h3>
              <div ref="easyChart" class="chart-placeholder"></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-card">
              <h3 class="chart-title">中等难度情况分布</h3>
              <div ref="mediumChart" class="chart-placeholder"></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-card">
              <h3 class="chart-title">困难难度情况分布</h3>
              <div ref="hardChart" class="chart-placeholder"></div>
            </div>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <!-- 下半部分 -->
    <el-row :gutter="20" class="bottom-section">
      <!-- 左二分之一：柱形图 -->
      <el-col :span="12">
        <div class="chart-card">
          <h3 class="chart-title">题型统计柱形图</h3>
          <div ref="barChart" class="chart-placeholder"></div>
        </div>
      </el-col>

      <!-- 右二分之一：折线图 -->
      <el-col :span="12">
        <div class="chart-card">
          <h3 class="chart-title">成绩变化折线图</h3>
          <div ref="lineChart" class="chart-placeholder"></div>
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
  updateTime: "2025-08-13 10:00",
  totalUsers: 123,
});

// 模拟柱形图 & 折线图数据
const barData = {
  categories: ["单选题", "多选题", "判断题", "填空题"],
  values: [40, 32, 20, 15],
};
const lineData = {
  months: ["1月", "2月", "3月", "4月", "5月", "6月"],
  scores: [78, 82, 85, 80, 88, 90],
};

// DOM 引用
const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);
const barChart = ref(null);
const lineChart = ref(null);

const initPieChart = (dom, data) => {
  const chart = echarts.init(dom.value);
  const option = {
    tooltip: { trigger: "item" },
    legend: {
      textStyle: { fontWeight: "bold" },
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        label: { fontSize: 18, fontWeight: "bold", color: "#4a5568" },
        type: "pie",
        radius: "70%",
        data: [
          { value: data[0], name: "优秀" },
          { value: data[1], name: "良好" },
          { value: data[2], name: "及格" },
          { value: data[3], name: "不及格" },
        ],
      },
    ],
  };
  chart.setOption(option);
};

const initBarChart = () => {
  const chart = echarts.init(barChart.value);
  chart.setOption({
    tooltip: {},
    xAxis: { type: "category", data: barData.categories },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: barData.values }],
  });
};

const initLineChart = () => {
  const chart = echarts.init(lineChart.value);
  chart.setOption({
    tooltip: { trigger: "axis" },
    xAxis: { type: "category", data: lineData.months },
    yAxis: { type: "value" },
    series: [{ type: "line", data: lineData.scores, smooth: true }],
  });
};

onMounted(() => {
  // 模拟数据
  chartData.value.easy = [10, 20, 30, 40];
  chartData.value.medium = [15, 25, 35, 25];
  chartData.value.hard = [5, 15, 25, 55];

  initPieChart(easyChart, chartData.value.easy);
  initPieChart(mediumChart, chartData.value.medium);
  initPieChart(hardChart, chartData.value.hard);
  initBarChart();
  initLineChart();
});
</script>

<style scoped>
/* 保留导航栏样式不变 */

/* 原有样式保留 + 新布局间距 */
.top-section {
  margin-bottom: 40px;
}
.bottom-section {
  margin-bottom: 40px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.chart-placeholder {
  width: 100%;
  height: 300px;
}
</style>
