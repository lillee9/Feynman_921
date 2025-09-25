<template>
  <el-container style="height: 100%; background-color: #f5f7fa">
    <!-- 主体区域 -->
    <el-main style="padding: 20px; display: flex; justify-content: center">
      <el-card
        class="quiz-card"
        shadow="hover"
        style="width: 100%; max-width: 800px"
      >
        <div class="chart-container">
          <h2 class="section-title">用户答题统计</h2>
          <p class="section-desc">
            不同难度下用户答题情况的优秀、良好、及格、不及格分布
          </p>

          <!-- 饼图展示区域 -->
          <div class="charts-wrapper">
            <!-- 简单难度 -->
            <div class="chart-item">
              <h3>简单难度</h3>
              <div class="chart-placeholder">
                <div class="chart-legend">
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #67c23a"
                    ></div>
                    <span>优秀 ({{ stats.easy.excellent }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #909399"
                    ></div>
                    <span>良好 ({{ stats.easy.good }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #e6a23c"
                    ></div>
                    <span>及格 ({{ stats.easy.pass }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #f56c6c"
                    ></div>
                    <span>不及格 ({{ stats.easy.fail }})</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 中等难度 -->
            <div class="chart-item">
              <h3>中等难度</h3>
              <div class="chart-placeholder">
                <div class="chart-legend">
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #67c23a"
                    ></div>
                    <span>优秀 ({{ stats.medium.excellent }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #909399"
                    ></div>
                    <span>良好 ({{ stats.medium.good }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #e6a23c"
                    ></div>
                    <span>及格 ({{ stats.medium.pass }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #f56c6c"
                    ></div>
                    <span>不及格 ({{ stats.medium.fail }})</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 困难难度 -->
            <div class="chart-item">
              <h3>困难难度</h3>
              <div class="chart-placeholder">
                <div class="chart-legend">
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #67c23a"
                    ></div>
                    <span>优秀 ({{ stats.hard.excellent }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #909399"
                    ></div>
                    <span>良好 ({{ stats.hard.good }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #e6a23c"
                    ></div>
                    <span>及格 ({{ stats.hard.pass }})</span>
                  </div>
                  <div class="legend-item">
                    <div
                      class="legend-color"
                      style="background-color: #f56c6c"
                    ></div>
                    <span>不及格 ({{ stats.hard.fail }})</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from "vue";

// 模拟统计数据
const stats = ref({
  easy: {
    excellent: 45,
    good: 32,
    pass: 18,
    fail: 5,
  },
  medium: {
    excellent: 30,
    good: 35,
    pass: 25,
    fail: 10,
  },
  hard: {
    excellent: 15,
    good: 25,
    pass: 35,
    fail: 25,
  },
});
</script>

<style scoped>
.chart-container {
  width: 100%;
}

.section-title {
  text-align: center;
  color: #303133;
  margin-bottom: 10px;
}

.section-desc {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
}

.charts-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: space-between;
}

.chart-item {
  flex: 1;
  min-width: 200px;
}

.chart-item h3 {
  text-align: center;
  margin-bottom: 15px;
  color: #606266;
}

.chart-placeholder {
  height: 200px;
  background-color: #f0f2f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  position: relative;
}

.chart-placeholder::before {
  content: "饼图展示区域";
  color: #c0c4cc;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

@media (max-width: 768px) {
  .charts-wrapper {
    flex-direction: column;
  }

  .chart-item {
    min-width: 100%;
  }
}
</style>
