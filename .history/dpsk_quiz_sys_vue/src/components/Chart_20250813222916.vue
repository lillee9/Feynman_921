<template>
  <div class="chart-container">
    <!-- 导航栏（保持不变） -->
    <el-navbar
      title="统计分析"
      left-text="返回"
      @left-click="goBack"
    ></el-navbar>

    <!-- 整体内容区域（上下分割线容器） -->
    <div class="content-wrapper">
      <!-- 上半部分（区域1和2） -->
      <el-row :gutter="20" class="top-section">
        <!-- 区域1：左1/3 更新信息 -->
        <el-col :span="8" class="section-border">
          <div class="update-info">
            <span>数据更新时间：{{ chartData.updateTime }}</span>
            <span>总参与人数：{{ chartData.totalUsers }}</span>
          </div>
        </el-col>

        <!-- 区域2：右2/3 三个饼图 -->
        <el-col :span="16">
          <el-row :gutter="20">
            <el-col
              :span="8"
              v-for="(item, index) in chartData.difficultyData"
              :key="index"
            >
              <el-card>
                <div class="chart-title">{{ item.difficulty }}难度成绩分布</div>
                <div :ref="`pieChart${index}`" class="chart-container"></div>
              </el-card>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <!-- 下半部分（区域3和4） -->
      <el-row :gutter="20" class="bottom-section">
        <!-- 区域3：左1/2 柱形图 -->
        <el-col :span="12" class="section-border">
          <el-card>
            <div class="chart-title">各题型平均得分（模拟）</div>
            <div ref="barChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 区域4：右1/2 折线图 -->
        <el-col :span="12">
          <el-card>
            <div class="chart-title">最近5次测试平均分趋势（模拟）</div>
            <div ref="lineChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";

export default {
  data() {
    return {
      chartData: {
        updateTime: "2024-06-10 14:30",
        totalUsers: 120,
        difficultyData: [
          { difficulty: "简单", data: [70, 30] },
          { difficulty: "中等", data: [60, 40] },
          { difficulty: "困难", data: [50, 50] },
        ],
      },
      barOption: {
        xAxis: {
          type: "category",
          data: ["单选", "多选", "填空", "简答"],
        },
        yAxis: {
          type: "value",
        },
        series: [
          {
            data: [85, 78, 80, 65],
            type: "bar",
            color: "#5470C6",
          },
        ],
      },
      lineOption: {
        xAxis: {
          type: "category",
          data: ["第1次", "第2次", "第3次", "第4次", "第5次"],
        },
        yAxis: {
          type: "value",
        },
        series: [
          {
            data: [75, 82, 78, 85, 80],
            type: "line",
            color: "#91CC75",
          },
        ],
      },
    };
  },
  mounted() {
    this.chartData.difficultyData.forEach((_, index) => {
      const chart = echarts.init(this.$refs[`pieChart${index}`][0]);
      chart.setOption({
        series: [
          {
            type: "pie",
            data: [
              { value: _.data[0], name: "及格" },
              { value: _.data[1], name: "不及格" },
            ],
          },
        ],
      });
      this.$once("hook:beforeDestroy", () => chart.dispose());
    });

    const barChart = echarts.init(this.$refs.barChart);
    barChart.setOption(this.barOption);
    this.$once("hook:beforeDestroy", () => barChart.dispose());

    const lineChart = echarts.init(this.$refs.lineChart);
    lineChart.setOption(this.lineOption);
    this.$once("hook:beforeDestroy", () => lineChart.dispose());
  },
  methods: {
    goBack() {
      this.$router.back();
    },
  },
};
</script>

<style scoped>
.chart-container {
  height: 300px;
  margin: 10px 0;
}

/* 整体内容区域（控制上下分割线） */
.content-wrapper {
  border-top: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 0;
}

/* 上下部分的间隔 */
.top-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

/* 左右分割线（仅左侧区域添加右边框） */
.section-border {
  border-right: 1px solid #e4e7ed;
}

.update-info {
  padding: 20px;
  background: #f8f8f8;
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chart-title {
  font-weight: bold;
  margin-bottom: 10px;
  padding: 0 10px;
}
</style>
