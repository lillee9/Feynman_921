<template>
  <div class="chart-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="nav-content">
        <div class="logo">
          <el-icon class="logo-icon"><Opportunity /></el-icon>
          <span>在线培训系统</span>
        </div>
        <div class="nav-links">
          <router-link to="/">首页</router-link>
          <router-link to="/analysis">错题分析</router-link>
          <router-link to="/quiz">题目生成</router-link>
        </div>
        <el-dropdown class="user-menu">
          <span class="user-name">
            <el-icon><User /></el-icon>
            用户名
          </span>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <h2 class="page-title">培训成果统计</h2>

      <!-- 数据状态提示 -->
      <div v-if="loading" class="loading-tip">
        <el-icon class="loading-icon"><Loading /></el-icon>
        数据加载中...
      </div>

      <div v-else>
        <!-- 更新时间 -->
        <div class="update-time">
          最后更新：{{ new Date(chartData.updateTime).toLocaleString() }}
        </div>

        <!-- 图表容器 -->
        <el-row :gutter="20">
          <el-col
            :span="8"
            v-for="(item, index) in chartData.difficulties"
            :key="index"
          >
            <div class="chart-card">
              <div class="chart-title">{{ item.difficulty }}难度成绩分布</div>
              <div class="chart-wrapper" :id="'chart' + index"></div>
              <div class="total-users">总参与人数：{{ item.totalUsers }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, nextTick } from "vue";
import * as echarts from "echarts";
import { User, Opportunity, Loading } from "@element-plus/icons-vue";

// 响应式数据
const loading = ref(true);
const chartData = reactive({
  updateTime: null,
  difficulties: [],
});

// 获取图表数据
const fetchChartData = async () => {
  try {
    const response = await fetch("/api/chart");
    if (!response.ok) throw new Error("数据获取失败");

    const data = await response.json();

    // 数据校验
    if (!data.updateTime || !Array.isArray(data.difficulties)) {
      throw new Error("无效的数据格式");
    }

    Object.assign(chartData, data);
    loading.value = false;

    // 等待DOM更新后初始化图表
    nextTick(() => {
      initCharts();
    });
  } catch (error) {
    console.error("数据加载失败:", error);
    ElMessage.error("数据加载失败：" + error.message);
  }
};

// 初始化图表
const initCharts = () => {
  chartData.difficulties.forEach((item, index) => {
    const chartDom = document.getElementById("chart" + index);
    const myChart = echarts.init(chartDom);

    const option = {
      tooltip: {
        trigger: "item",
      },
      series: [
        {
          type: "pie",
          radius: ["40%", "70%"],
          data: item.scores.map((score) => ({
            value: score.count,
            name: `${score.range}分`,
          })),
          label: {
            formatter: "{b}: {d}%",
          },
        },
      ],
    };

    myChart.setOption(option);
  });
};

// 生命周期钩子
onMounted(() => {
  fetchChartData();
});
</script>

<style scoped>
.navbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  color: #409eff;
}

.logo-icon {
  font-size: 28px;
  margin-right: 10px;
}

.nav-links {
  flex: 1;
  margin-left: 40px;
}

.nav-links a {
  margin: 0 20px;
  color: #606266;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: #409eff;
}

.user-menu {
  cursor: pointer;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.page-title {
  color: #303133;
  margin-bottom: 30px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.chart-wrapper {
  height: 300px;
}

.loading-tip {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.loading-icon {
  font-size: 24px;
  margin-right: 10px;
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.update-time {
  color: #909399;
  margin-bottom: 20px;
  font-size: 14px;
}

.total-users {
  text-align: center;
  color: #409eff;
  margin-top: 15px;
  font-weight: 500;
}
</style>
