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

    <!-- 主内容区域 -->
    <div class="main-layout">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">培训成果统计</h1>
      </div>

      <!-- 上半部分 -->
      <div class="upper-section">
        <!-- 左三分之一：数据信息 -->
        <div class="info-section">
          <div class="update-info">
            <span>数据更新时间：{{ infoUpdateTimeChart }}</span>
            <span>总参与人数：{{ infoTotalUserChart }}</span>
            <span>昨日答题人数：{{ infoYesterdayUserChart }}</span>
          </div>
        </div>

        <!-- 垂直分割线 -->
        <div class="vertical-divider"></div>

        <!-- 右三分之二：三个饼图 -->
        <div class="pie-charts-section">
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

      <!-- 水平分割线 -->
      <div class="horizontal-divider"></div>

      <!-- 下半部分 -->
      <div class="lower-section">
        <!-- 左二分之一：柱形图 -->
        <div class="bar-chart-section">
          <div class="chart-card">
            <h3 class="chart-title">题目类型正确率</h3>
            <div
              ref="barChart"
              class="chart-placeholder"
              style="height: 300px"
            ></div>
          </div>
        </div>

        <!-- 垂直分割线 -->
        <div class="vertical-divider"></div>

        <!-- 右二分之一：折线图 -->
        <div class="line-chart-section">
          <div class="chart-card">
            <h3 class="chart-title">每日答题趋势</h3>
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

const chartData = ref(null);

// 图表容器引用
const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);
const barChart = ref(null);
const lineChart = ref(null);
const infoTotalUserChart = ref("");
const infoUpdateTimeChart = ref(0);
const infoYesterdayUserChart = ref(0);
// 初始化饼图
const initPieChart = (dom, data) => {
  const chart = echarts.init(dom);
  const option = {
    tooltip: { trigger: "item" },
    legend: {
      textStyle: { fontWeight: "bold" },
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        label: {
          // show: false,
          // fontSize: 10,
          // fontWeight: "bold",
          // color: "#4a5568",
          // textShadowColor: "rgba(255,255,255,0.8)",
          // textShadowBlur: 2,
          show: false,
        },
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

// 初始化折线图
const initLineChart = (dom, data) => {
  const chart = echarts.init(dom);
  const option = {
    tooltip: {
      trigger: "axis",
      formatter: "日期：{b0}<br/>答题人数：{c0}",
    },
    xAxis: {
      type: "category",
      data: data.dates, // 改为日期数组
      axisLabel: { fontWeight: "bold" },
    },
    yAxis: {
      type: "value",
      axisLabel: { formatter: "{value}人" }, // 修改单位
      max: Math.max(...data.dailyParticipants) + 10,
    },
    series: [
      {
        name: "答题人数",
        type: "line",
        data: data.dailyParticipants, // 改用答题人数数据
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        itemStyle: { color: "#409EFF" }, // 调整颜色为蓝色系
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(64, 158, 255, 0.3)" },
            { offset: 1, color: "rgba(64, 158, 255, 0.1)" },
          ]),
        },
      },
    ],
  };
  chart.setOption(option);
};
// 初始化柱形图
const initBarChart = () => {
  if (!barChart.value) return;
  const myChart = echarts.init(barChart.value);

  // 将后端数据转成 echarts 需要的格式
  const difficulties = ["easy", "medium", "hard"];
  const typeKeys = ["single", "multi", "judge"];
  const typeLabels = { single: "单选", multi: "多选", judge: "判断" };

  const series = typeKeys.map((typeKey) => ({
    name: typeLabels[typeKey],
    type: "bar",
    data: difficulties.map((diffKey) => {
      const val = chartData.value.barChart[diffKey]?.[typeKey];
      return val === -1
        ? { value: 0, noData: true }
        : { value: val, noData: false };
    }),
    itemStyle: {
      color: (params) => {
        return params.data.noData ? "#ccc" : undefined;
      },
    },
    label: {
      show: true,
      position: "top",
      formatter: (params) => {
        return params.data.noData
          ? "暂无数据"
          : `${params.data.value.toFixed(2)}%`;
      },
    },
  }));

  const option = {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
        shadowStyle: {
          color: "rgba(150,150,150,0.1)",
        },
      },
    },
    legend: {
      type: "scroll",
      right: 20,
      top: 15,
      orient: "vertical",
      itemGap: 12,
      textStyle: {
        color: "#666",
      },
      data: Object.values(typeLabels),
    },
    grid: {
      left: "2%",
      right: "18%", // 为垂直图例留出空间
      top: "15%",
      bottom: "12%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      axisLabel: {
        interval: 0,
        rotate: 0,
      },
      data: ["简单", "中等", "困难"],
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: "{value} %",
        margin: 8,
      },
      min: 0,
      max: 100,
      splitNumber: 5,
    },
    series: series.map((item) => ({
      ...item,
      label: {
        show: true,
        position: "top",
        color: "#666",
      },
      barMaxWidth: 28,
    })),
  };

  myChart.setOption(option);
  window.addEventListener("resize", () => myChart.resize());
};
// API请求方法
const fetchChartData = async () => {
  try {
    const response = await fetch("/api/chart");
    const data = await response.json();
    chartData.value = data;
    console.log("获取数据成功:", chartData.value);
    // 更新信息图表

    infoTotalUserChart.value = chartData.value.totalUsers;
    infoUpdateTimeChart.value = chartData.value.updateTime;
    infoYesterdayUserChart.value = chartData.value.yesterdayUsers;

    // 更新图表
    initPieChart(easyChart.value, chartData.value.easy);
    initPieChart(mediumChart.value, chartData.value.medium);
    initPieChart(hardChart.value, chartData.value.hard);
    initLineChart(lineChart.value, chartData.value.lineChart);

    initBarChart();
  } catch (error) {
    console.error("获取数据失败:", error);
  }
};

onMounted(() => {
  fetchChartData();
});
</script>

<style scoped>
/* 导航栏样式（保持不变） */
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
  text-decoration: none;
  font-size: 16px;
  font-weight: 400;
  transition: color 0.3s ease;
  padding: 8px 0;
  position: relative;
}

.nav-link:hover {
  color: #667eea;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.username {
  font-size: 16px;
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

/* 主布局容器 */
.main-layout {
  max-width: 1800px; /* 从1400px增加到1600px */
  margin: 80px auto 40px;
  padding: 30px; /* 增加内边距 */
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.page-title {
  font-size: 42px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 20px;
  text-align: center;
}

/* 上半部分布局 */
/* 上半部分布局 - 增加高度 */
.upper-section {
  display: flex;
  min-height: 450px; /* 从400px增加到450px */
  margin-bottom: 10px;
}

.info-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.pie-charts-section {
  flex: 2;
  padding: 20px;
}

/* 下半部分布局 */
.lower-section {
  display: flex;
  min-height: 450px;
  margin-top: 10px;
}

.bar-chart-section,
.line-chart-section {
  flex: 1;
  padding: 20px;
}

/* 数据信息样式 */
.update-info {
  display: flex;
  flex-direction: column;
  gap: 25px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 550px;
}

.update-info span {
  font-size: 24px;
  font-weight: 600;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-info span::before {
  content: "";
  width: 10px;
  height: 10px;
  background: #667eea;
  border-radius: 50%;
}

/* 图表卡片样式 */
.chart-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  height: 100%;
}

.chart-title {
  font-size: 22px;
  color: #606266;
  margin-bottom: 20px;
  font-weight: 700;
  text-align: center;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

/* 分割线样式 */
.horizontal-divider {
  height: 8px;
  background: linear-gradient(
    to right,
    transparent,
    rgba(0, 0, 0, 0.15),
    transparent
  );
  margin: 10px 0;
}

.vertical-divider {
  width: 8px;
  background: linear-gradient(
    to bottom,
    transparent,
    rgba(0, 0, 0, 0.15),
    transparent
  );
  margin: 0 10px;
}

/* 响应式设计 */
@media (max-width: 1800px) {
  .main-layout {
    margin: 80px 40px 40px;
  }
}

@media (max-width: 992px) {
  .upper-section {
    flex-direction: column;
  }

  .lower-section {
    flex-direction: column;
  }

  .vertical-divider {
    width: 100%;
    height: 3px;
    margin: 15px 0;
    background: linear-gradient(
      to right,
      transparent,
      rgba(0, 0, 0, 0.15),
      transparent
    );
  }
}

@media (max-width: 768px) {
  .update-info span {
    font-size: 18px;
  }

  .page-title {
    font-size: 32px;
  }

  .chart-title {
    font-size: 18px;
  }
}
</style>
