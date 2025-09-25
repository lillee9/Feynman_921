<template>
  <div class="chart-container">
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
}
.chart-placeholder {
  width: 100%;
  height: 300px;
}
</style>
