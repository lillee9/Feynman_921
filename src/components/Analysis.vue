<template>
  <!-- ... 其他代码 ... -->

  <!-- 排行榜区域 -->
  <section class="rankings-section">
    <div class="rankings-grid">
      <!-- 简单难度排行榜 -->
      <div class="ranking-card">
        <div class="section-header with-refresh">
          <div class="section-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <h2 class="section-title">简单难度排行</h2>
          <p class="section-desc">前五名高分用户</p>
          <!-- 添加刷新按钮 -->
          <button class="refresh-button" @click="refreshRanking('easy')">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
        <!-- ... 排行榜列表代码 ... -->
      </div>

      <!-- 中等难度排行榜 -->
      <div class="ranking-card">
        <div class="section-header with-refresh">
          <div class="section-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <h2 class="section-title">中等难度排行</h2>
          <p class="section-desc">前五名高分用户</p>
          <!-- 添加刷新按钮 -->
          <button class="refresh-button" @click="refreshRanking('medium')">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
        <!-- ... 排行榜列表代码 ... -->
      </div>

      <!-- 困难难度排行榜 -->
      <div class="ranking-card">
        <div class="section-header with-refresh">
          <div class="section-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <h2 class="section-title">困难难度排行</h2>
          <p class="section-desc">前五名高分用户</p>
          <!-- 添加刷新按钮 -->
          <button class="refresh-button" @click="refreshRanking('hard')">
            <el-icon><Refresh /></el-icon>
          </button>
        </div>
        <!-- ... 排行榜列表代码 ... -->
      </div>
    </div>
  </section>

  <!-- ... 其他代码 ... -->
</template>

<script setup>
import { ref } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import {
  UserFilled,
  CaretBottom,
  Trophy,
  Histogram,
  Document,
  WarningFilled,
  InfoFilled,
  // 添加Refresh图标导入
  Refresh,
} from "@element-plus/icons-vue";

// ... 其他代码 ...

// 添加刷新排行榜函数
const refreshRanking = (difficulty) => {
  // 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: "刷新中...",
    background: "rgba(0, 0, 0, 0.7)",
    target: document.querySelector(
      `.ranking-card:nth-child(${
        difficulty === "easy" ? 1 : difficulty === "medium" ? 2 : 3
      })`
    ),
  });

  // 模拟API请求延迟
  setTimeout(() => {
    // 根据难度类型刷新对应排行榜数据
    switch (difficulty) {
      case "easy":
        // 打乱顺序模拟刷新效果
        easyRankings.value = [...easyRankings.value].sort(
          () => Math.random() - 0.5
        );
        break;
      case "medium":
        mediumRankings.value = [...mediumRankings.value].sort(
          () => Math.random() - 0.5
        );
        break;
      case "hard":
        hardRankings.value = [...hardRankings.value].sort(
          () => Math.random() - 0.5
        );
        break;
    }

    ElMessage.success(
      `${
        difficulty === "easy"
          ? "简单"
          : difficulty === "medium"
          ? "中等"
          : "困难"
      }难度排行榜已刷新`
    );
    loading.close();
  }, 800);
};

// ... 其他代码 ...
</script>

<style scoped>
/* ... 其他样式 ... */

/* 排行榜标题区域样式调整 */
.section-header.with-refresh {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
}

/* 刷新按钮样式 */
.refresh-button {
  position: absolute;
  top: 0;
  right: 0;
  background: transparent;
  border: none;
  color: #667eea;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.refresh-button:hover {
  background-color: rgba(102, 126, 234, 0.1);
  transform: rotate(90deg);
}

/* ... 其他样式 ... */
</style>
