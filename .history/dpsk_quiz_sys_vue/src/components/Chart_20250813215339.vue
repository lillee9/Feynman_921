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
            <span>数据更新时间：{{ chartData.updateTime }}</span>
            <span>总参与人数：{{ chartData.totalUsers }}</span>
          </div>
        </div>

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

      <!-- 分割线 -->
      <div class="divider"></div>

      <!-- 下半部分 -->
      <div class="lower-section">
        <!-- 左二分之一：柱形图 -->
        <div class="bar-chart-section">
          <div class="chart-card">
            <h3 class="chart-title">题目类型正确率</h3>
            <div ref="barChart" class="chart-placeholder"></div>
          </div>
        </div>

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

const chartData = ref({
  updateTime: "2023-11-15 14:30:45",
  totalUsers: 1245,
  easy: [120, 80, 60, 40],
  medium: [90, 70, 50, 30],
  hard: [60, 50, 40, 30],
  questionTypes: ["选择题", "填空题", "判断题", "简答题", "编程题"],
  correctRates: [0.85, 0.72, 0.68, 0.55, 0.45],
  dailyData: ["11/10", "11/11", "11/12", "11/13", "11/14", "11/15"],
  dailyCorrect: [65, 72, 68, 80, 75, 82],
});

// 图表容器引用
const easyChart = ref(null);
const mediumChart = ref(null);
const hardChart = ref(null);
const barChart = ref(null);
const lineChart = ref(null);

// 初始化饼图
const initPieChart = (dom, data, title) => {
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
        label: {
          fontSize: 18,
          fontWeight: "bold",
          color: "#4a5568",
          textShadowColor: "rgba(255,255,255,0.8)",
          textShadowBlur: 2,
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

// 初始化柱形图
const initBarChart = () => {
  const chart = echarts.init(barChart.value);
  const option = {
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    xAxis: {
      type: "category",
      data: chartData.value.questionTypes,
      axisLabel: {
        rotate: 30,
        fontWeight: "bold",
      },
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: "{value}%",
      },
    },
    series: [
      {
        name: "正确率",
        type: "bar",
        data: chartData.value.correctRates.map((rate) =>
          (rate * 100).toFixed(1)
        ),
        itemStyle: {
          color: function (params) {
            const colorList = [
              "#5470C6",
              "#91CC75",
              "#EE6666",
              "#FAC858",
              "#73C0DE",
            ];
            return colorList[params.dataIndex];
          },
        },
        label: {
          show: true,
          position: "top",
          formatter: "{c}%",
        },
      },
    ],
  };
  chart.setOption(option);
};

// 初始化折线图
const initLineChart = () => {
  const chart = echarts.init(lineChart.value);
  const option = {
    tooltip: {
      trigger: "axis",
    },
    xAxis: {
      type: "category",
      data: chartData.value.dailyData,
      axisLabel: {
        fontWeight: "bold",
      },
    },
    yAxis: {
      type: "value",
      axisLabel: {
        formatter: "{value}%",
      },
    },
    series: [
      {
        name: "正确率",
        type: "line",
        data: chartData.value.dailyCorrect,
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        itemStyle: {
          color: "#67C23A",
        },
        lineStyle: {
          width: 3,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(103, 194, 58, 0.3)" },
            { offset: 1, color: "rgba(103, 194, 58, 0.1)" },
          ]),
        },
      },
    ],
  };
  chart.setOption(option);
};

onMounted(() => {
  initPieChart(easyChart, chartData.value.easy);
  initPieChart(mediumChart, chartData.value.medium);
  initPieChart(hardChart, chartData.value.hard);
  initBarChart();
  initLineChart();
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

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 主布局容器 */
.main-layout {
  position: fixed;
  top: 60px; /* 导航栏高度 */
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: auto;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 20px;
  padding: 0 20px;
}

/* 上半部分布局 */
.upper-section {
  display: flex;
  flex: 1;
  min-height: 0; /* 防止flex项目溢出 */
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 20px;
}

.info-section {
  flex: 1;
  padding-right: 20px;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.pie-charts-section {
  flex: 2;
  padding-left: 20px;
}

/* 下半部分布局 */
.lower-section {
  display: flex;
  flex: 1;
  min-height: 0; /* 防止flex项目溢出 */
  padding-top: 20px;
}

.bar-chart-section {
  flex: 1;
  padding-right: 20px;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.line-chart-section {
  flex: 1;
  padding-left: 20px;
}

/* 数据信息样式 */
.update-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.update-info span {
  font-size: 20px;
  font-weight: 500;
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 8px;
}

.update-info span::before {
  content: "";
  width: 6px;
  height: 6px;
  background: #667eea;
  border-radius: 50%;
}

/* 图表卡片样式 */
.chart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.chart-title {
  font-size: 18px;
  color: #606266;
  margin-bottom: 15px;
  font-weight: 700;
  text-align: center;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

/* 分割线 */
.divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.1);
  margin: 0 -20px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .upper-section {
    flex-direction: column;
  }

  .lower-section {
    flex-direction: column;
  }

  .info-section {
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
    padding-right: 0;
    padding-bottom: 20px;
    margin-bottom: 20px;
  }

  .pie-charts-section {
    padding-left: 0;
  }

  .bar-chart-section {
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
    padding-right: 0;
    padding-bottom: 20px;
    margin-bottom: 20px;
  }

  .line-chart-section {
    padding-left: 0;
  }
}

@media (max-width: 768px) {
  .update-info span {
    font-size: 16px;
  }

  .page-title {
    font-size: 28px;
  }
}
</style>
