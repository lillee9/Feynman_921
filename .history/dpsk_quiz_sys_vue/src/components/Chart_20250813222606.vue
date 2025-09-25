<template>
  <div class="chart-container">
    <!-- 导航栏（保持原样） -->
    <nav class="navbar">
      <h1>导航栏</h1>
    </nav>

    <!-- 主体部分 -->
    <div class="main-content">
      <!-- 上半部分 -->
      <div class="top-section">
        <!-- 区域 1 -->
        <div class="section section-1">
          <div class="update-info">
            <span>数据更新时间：{{ chartData.updateTime }}</span>
            <span>总参与人数：{{ chartData.totalUsers }}</span>
          </div>
        </div>
        <!-- 区域 2 -->
        <div class="section section-2">
          <div class="pie-charts">
            <div
              v-for="(chart, index) in pieCharts"
              :key="index"
              class="pie-chart"
            >
              <h3>{{ chart.title }}</h3>
              <div class="chart-placeholder">饼形图 {{ index + 1 }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 下半部分 -->
      <div class="bottom-section">
        <!-- 区域 3 -->
        <div class="section section-3">
          <h3>柱形图</h3>
          <div class="chart-placeholder">柱形图数据展示</div>
        </div>
        <!-- 区域 4 -->
        <div class="section section-4">
          <h3>折线图</h3>
          <div class="chart-placeholder">折线图数据展示</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      chartData: {
        updateTime: "2025-08-13 12:00",
        totalUsers: 1234,
      },
      pieCharts: [
        { title: "饼形图 1" },
        { title: "饼形图 2" },
        { title: "饼形图 3" },
      ],
    };
  },
};
</script>

<style scoped>
.chart-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.navbar {
  background-color: #333;
  color: #fff;
  padding: 10px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-section,
.bottom-section {
  display: flex;
  flex: 1;
  border-top: 1px solid #ccc;
}

.top-section {
  border-bottom: 1px solid #ccc;
}

.section {
  padding: 20px;
}

.section-1 {
  flex: 1;
  border-right: 1px solid #ccc;
}

.section-2 {
  flex: 2;
}

.section-3 {
  flex: 1;
  border-right: 1px solid #ccc;
}

.section-4 {
  flex: 1;
}

.update-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pie-charts {
  display: flex;
  justify-content: space-around;
}

.pie-chart {
  text-align: center;
}

.chart-placeholder {
  background-color: #f5f5f5;
  border: 1px dashed #ccc;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
