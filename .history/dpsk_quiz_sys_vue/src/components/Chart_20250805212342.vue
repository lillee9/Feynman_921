<template>
  <div class="chart-container">
    <!-- 顶部导航栏（与analysis.vue完全一致） -->
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

    <!-- 原Chart.vue内容 -->
    <div class="page-title">培训成果统计</div>
    <el-row :gutter="24" class="chart-grid">
      <el-col :span="8" v-for="(level, index) in difficultyLevels" :key="index">
        <div class="chart-card">
          <h3 class="chart-title">{{ level }}难度成绩分布</h3>
          <div class="chart-wrapper" :ref="`${level}ChartRef`"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import * as echarts from "echarts";
import { UserFilled, CaretBottom } from "@element-plus/icons-vue";

// 模拟数据（需替换为真实接口数据）
const mockData = {
  easy: [
    ["优秀", 25],
    ["良好", 35],
    ["及格", 25],
    ["不及格", 15],
  ],
  medium: [
    ["优秀", 20],
    ["良好", 30],
    ["及格", 30],
    ["不及格", 20],
  ],
  hard: [
    ["优秀", 15],
    ["良好", 25],
    ["及格", 35],
    ["不及格", 25],
  ],
};

const difficultyLevels = ["简单", "中等", "困难"];
const chartRefs = ref({});

onMounted(() => {
  difficultyLevels.forEach((level) => {
    const chartDom = chartRefs.value[`${level}ChartRef`][0];
    const myChart = echarts.init(chartDom);
    const option = {
      series: [
        {
          type: "pie",
          radius: "70%",
          data: mockData[level.toLowerCase()],
          label: {
            show: true,
            formatter: "{b}: {c}%",
          },
        },
      ],
    };
    myChart.setOption(option);
  });
});

// 导出图标供模板使用
const icons = { UserFilled, CaretBottom };
</script>

<style scoped>
/* 导航栏样式（与analysis.vue一致） */
.navbar {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-icon {
  width: 32px;
  height: 32px;
  background: #409eff;
  border-radius: 4px;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.nav-links {
  display: flex;
  gap: 24px;
}
.nav-link {
  color: #606266;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-link:hover {
  color: #409eff;
}
.user-section {
  position: relative;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.user-avatar {
  width: 32px;
  height: 32px;
  background: #f0f2f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.username {
  color: #606266;
}
.dropdown-icon {
  font-size: 12px;
}

/* 原Chart.vue样式 */
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 24px 0;
  padding: 0 24px;
}
.chart-grid {
  padding: 0 24px;
}
.chart-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
  height: 300px;
}
.chart-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}
.chart-wrapper {
  height: 240px;
}
</style>
