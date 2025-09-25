<template>
  <div class="analysis-container">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
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
    </nav>

    <!-- 主体区域 -->
    <main class="main-content">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            答题情况
            <span class="hero-highlight">统计</span>
          </h1>
          <p class="hero-subtitle">查看不同难度下用户答题情况的分布</p>
        </div>
      </section>

      <!-- 答题情况统计区域 -->
      <section class="rankings-section">
        <div class="rankings-grid">
          <!-- 简单难度答题情况 -->
          <div class="ranking-card">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <h2 class="section-title">简单难度答题情况</h2>
              <p class="section-desc">用户答题情况分布</p>
            </div>

            <div class="chart-container">
              <div ref="easyChart" class="chart"></div>
            </div>
          </div>

          <!-- 中等难度答题情况 -->
          <div class="ranking-card">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <h2 class="section-title">中等难度答题情况</h2>
              <p class="section-desc">用户答题情况分布</p>
            </div>

            <div class="chart-container">
              <div ref="mediumChart" class="chart"></div>
            </div>
          </div>

          <!-- 困难难度答题情况 -->
          <div class="ranking-card">
            <div class="section-header">
              <div class="section-icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <h2 class="section-title">困难难度答题情况</h2>
              <p class="section-desc">用户答题情况分布</p>
            </div>

            <div class="chart-container">
              <div ref="hardChart" class="chart"></div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import { UserFilled, CaretBottom, PieChart } from "@element-plus/icons-vue";

// 定义图表引用
const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);

// 模拟数据
const easyData = [
  { value: 10, name: "优秀" },
  { value: 15, name: "良好" },
  { value: 20, name: "及格" },
  { value: 5, name: "不及格" },
];

const mediumData = [
  { value: 8, name: "优秀" },
  { value: 12, name: "良好" },
  { value: 18, name: "及格" },
  { value: 12, name: "不及格" },
];

const hardData = [
  { value: 5, name: "优秀" },
  { value: 10, name: "良好" },
  { value: 15, name: "及格" },
  { value: 20, name: "不及格" },
];

// 初始化图表
const initChart = (chartRef, data) => {
  const chart = echarts.init(chartRef);
  const option = {
    tooltip: {
      trigger: "item",
    },
    legend: {
      top: "5%",
      left: "center",
    },
    series: [
      {
        name: "答题情况",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 40,
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  };
  chart.setOption(option);
  return chart;
};

onMounted(() => {
  // 初始化三个图表
  initChart(easyChart.value, easyData);
  initChart(mediumChart.value, mediumData);
  initChart(hardChart.value, hardData);
});
</script>

<style scoped>
@import "./Analysis.vue";

.chart-container {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
