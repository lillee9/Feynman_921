<template>
  <div class="chart-container">
    <!-- 顶部导航栏（保持不变） -->
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

    <!-- 整体内容 -->
    <div class="page-wrapper">
      <!-- 上半部分 -->
      <div class="top-section">
        <div class="left-info">
          <div class="update-info">
            <span>数据更新时间：{{ chartData.updateTime }}</span>
            <span>总参与人数：{{ chartData.totalUsers }}</span>
          </div>
        </div>
        <div class="right-pies">
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
        </div>
      </div>

      <!-- 下半部分 -->
      <div class="bottom-section">
        <div class="left-bar">
          <div class="chart-card">
            <h3 class="chart-title">柱形图示例</h3>
            <div ref="barChart" class="chart-placeholder"></div>
          </div>
        </div>
        <div class="right-line">
          <div class="chart-card">
            <h3 class="chart-title">折线图示例</h3>
            <div ref="lineChart" class="chart-placeholder"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import { UserFilled, CaretBottom } from "@element-plus/icons-vue";

const chartData = ref({
  updateTime: "2025-08-13",
  totalUsers: 200,
  easy: [40, 30, 20, 10],
  medium: [35, 25, 25, 15],
  hard: [20, 30, 30, 20],
});

const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);
const barChart = ref(null);
const lineChart = ref(null);

const initPieChart = (dom, data) => {
  const chart = echarts.init(dom.value);
  chart.setOption({
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
      },
    ],
  });
};

const initBarChart = (dom) => {
  const chart = echarts.init(dom.value);
  chart.setOption({
    tooltip: {},
    xAxis: { data: ["一月", "二月", "三月", "四月", "五月"] },
    yAxis: {},
    series: [
      {
        type: "bar",
        data: [5, 20, 36, 10, 10],
      },
    ],
  });
};

const initLineChart = (dom) => {
  const chart = echarts.init(dom.value);
  chart.setOption({
    tooltip: {},
    xAxis: { type: "category", data: ["一月", "二月", "三月", "四月", "五月"] },
    yAxis: { type: "value" },
    series: [
      {
        type: "line",
        data: [5, 15, 25, 35, 20],
      },
    ],
  });
};

onMounted(() => {
  initPieChart(easyChart, chartData.value.easy);
  initPieChart(mediumChart, chartData.value.medium);
  initPieChart(hardChart, chartData.value.hard);
  initBarChart(barChart);
  initLineChart(lineChart);
});
</script>

<style scoped>
.page-wrapper {
  display: flex;
  flex-direction: column;
  padding-top: 80px;
}

/* 上半部分 */
.top-section {
  display: flex;
  border-bottom: 4px solid #ccc;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.left-info {
  flex: 1;
  border-right: 4px solid #ccc;
  padding: 20px;
}

.right-pies {
  flex: 2;
  padding: 20px;
}

/* 下半部分 */
.bottom-section {
  display: flex;
  border-top: 4px solid #ccc;
  padding-top: 20px;
}

.left-bar {
  flex: 1;
  border-right: 4px solid #ccc;
  padding: 20px;
}

.right-line {
  flex: 1;
  padding: 20px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
}

.chart-title {
  margin-bottom: 10px;
}

.chart-placeholder {
  width: 100%;
  height: 300px;
}
</style>
