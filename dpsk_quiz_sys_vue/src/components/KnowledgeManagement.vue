<template>
  <div class="knowledge-management">
    <!-- 顶部导航栏 -->
    <nav class="navbar">
      <div class="nav-content">
        <div class="nav-left">
          <div class="logo">
            <span class="logo-text">知识库管理</span>
          </div>
        </div>
        <div class="nav-right">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <span class="user-trigger">
              <el-icon><User /></el-icon>
              <span>{{ username || "用户" }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="action-left">
          <h2>文档管理</h2>
          <p class="subtitle">管理催收准则等文件，智能解析文档内容为知识块</p>
        </div>
        <div class="action-right">
          <el-upload
            class="upload-component"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept=".pdf,.doc,.docx,.txt"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              上传文档
            </el-button>
          </el-upload>
        </div>
      </div>

      <!-- 文档列表 -->
      <div class="document-list">
        <el-table
          :data="documents"
          v-loading="loading"
          style="width: 100%"
          @row-click="handleRowClick"
        >
          <el-table-column prop="documentName" label="文档名称" min-width="200">
            <template #default="scope">
              <div class="file-info">
                <el-icon class="file-icon"><Document /></el-icon>
                <span>{{ scope.row.documentName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="fileSize" label="文件大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="知识块数量" width="120">
            <template #default="scope">
              <el-tag type="info">{{ scope.row.chunkCount || 0 }} 个</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="uploadTime" label="上传时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.uploadTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="processStatus" label="处理状态" width="120">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row.processStatus)"
                :loading="scope.row.processStatus === 'processing'"
              >
                {{ getStatusText(scope.row.processStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click.stop="viewChunks(scope.row)"
                :disabled="scope.row.processStatus !== 'success'"
              >
                查看知识块
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click.stop="deleteDocument(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>



    <!-- 知识块查看对话框 -->
    <el-dialog
      v-model="showChunksDialog"
      :title="`${currentDocument?.documentName} - 知识块列表`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="chunks-content">
        <div class="chunks-header">
          <el-input
            v-model="chunkSearchKeyword"
            placeholder="搜索知识块内容..."
            style="width: 300px; margin-bottom: 16px"
            @input="searchChunks"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <div class="chunk-stats">共 {{ filteredChunks.length }} 个知识块</div>
        </div>

        <div class="chunks-list" v-loading="chunksLoading">
          <div
            v-for="(chunk, index) in paginatedChunks"
            :key="chunk.id"
            class="chunk-item"
          >
            <div class="chunk-header">
              <div class="chunk-title">
                <el-icon><Document /></el-icon>
                <span>{{ chunk.chapterName || `知识块 ${index + 1}` }}</span>
              </div>
              <div class="chunk-meta">
                <el-tag size="small"
                  >{{ chunk.content?.length || 0 }} 字符</el-tag
                >
              </div>
            </div>
            <div class="chunk-content">
              {{ chunk.content || "暂无内容" }}
            </div>
            <div class="chunk-keywords" v-if="chunk.keywords">
              <span class="keywords-label">关键词：</span>
              <el-tag
                v-for="keyword in chunk.keywords.split(',')"
                :key="keyword"
                size="small"
                type="info"
                style="margin-right: 4px"
              >
                {{ keyword.trim() }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 知识块分页 -->
        <div class="chunks-pagination">
          <el-pagination
            v-model:current-page="chunkCurrentPage"
            :page-size="chunkPageSize"
            :total="filteredChunks.length"
            layout="prev, pager, next"
            @current-change="handleChunkPageChange"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox, ElLoading } from "element-plus";
import { API_BASE_URL } from "../config/api";
import {
  getToken,
  requireAuth,
  authFetch,
  handleApiResponse,
} from "../utils/auth";
import {
  User,
  ArrowDown,
  Upload,
  Document,
  UploadFilled,
  Search,
} from "@element-plus/icons-vue";

export default {
  name: "KnowledgeManagement",
  components: {
    User,
    ArrowDown,
    Upload,
    Document,
    UploadFilled,
    Search,
  },
  setup() {
    const router = useRouter();

    // Cookie工具函数
    const getCookie = (name) => {
      const cookieArray = document.cookie.split("; ");
      const cookie = cookieArray.find((row) => row.startsWith(name + "="));
      return cookie ? cookie.split("=")[1] : null;
    };

    // 响应式数据
    const username = ref("");
    const loading = ref(false);
    const uploading = ref(false);
    const chunksLoading = ref(false);
    const showChunksDialog = ref(false);
    const selectedFile = ref(null);

    // 文档列表相关
    const documents = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const total = ref(0);

    // 知识块相关
    const currentDocument = ref(null);
    const chunks = ref([]);
    const chunkSearchKeyword = ref("");
    const chunkCurrentPage = ref(1);
    const chunkPageSize = ref(5);

    // 计算属性
    const filteredChunks = computed(() => {
      if (!chunkSearchKeyword.value) {
        return chunks.value;
      }
      return chunks.value.filter(
        (chunk) =>
          chunk.content
            ?.toLowerCase()
            .includes(chunkSearchKeyword.value.toLowerCase()) ||
          chunk.chapterName
            ?.toLowerCase()
            .includes(chunkSearchKeyword.value.toLowerCase())
      );
    });

    const paginatedChunks = computed(() => {
      const start = (chunkCurrentPage.value - 1) * chunkPageSize.value;
      const end = start + chunkPageSize.value;
      return filteredChunks.value.slice(start, end);
    });

    // 方法
    const checkAuth = () => {
      return requireAuth(router);
    };

    const loadDocuments = async () => {
      if (!checkAuth()) return;

      loading.value = true;
      try {
        const result = await authFetch(
          `${API_BASE_URL}/kb/documents?page=${currentPage.value}&size=${
            pageSize.value
          }`,
          {
            method: "GET",
          },
          router
        );

        console.log("文档列表响应:", result);

        if (result.code === 200 && result.data) {
          documents.value = result.data.content || [];
          total.value = result.data.totalElements || 0;

          // 获取每个文档的知识块数量
          for (let doc of documents.value) {
            await loadChunkCount(doc);
          }
        } else {
          console.error("获取文档列表失败:", result.message);
          documents.value = [];
        }
      } catch (error) {
        console.error("加载文档列表失败:", error);
        ElMessage.error("加载文档列表失败");
        documents.value = [];
      } finally {
        loading.value = false;
      }
    };

    const loadChunkCount = async (document) => {
      try {
        const result = await authFetch(
          `${API_BASE_URL}/kb/chunks/count?documentId=${document.documentId}`,
          {
            method: "GET",
          },
          router
        );

        console.log("知识块数量响应:", result);

        if (result.code === 200) {
          document.chunkCount = result.data || 0;
        } else {
          console.error("获取知识块数量失败:", result.message);
          document.chunkCount = 0;
        }
      } catch (error) {
        console.error("获取知识块数量失败:", error);
        document.chunkCount = 0;
      }
    };

    const handleFileChange = async (uploadFile) => {
      try {
        await validateFile(uploadFile.raw);
        const loading = ElLoading.service({
          lock: true,
          text: "正在上传文件到知识库...",
          background: "rgba(0, 0, 0, 0.7)",
        });

        try {
          const token = getCookie("token");
          const userId = getUserIdFromToken(token);

          if (!userId) {
            ElMessage.error("获取用户信息失败，请重新登录");
            return;
          }

          const formData = new FormData();
          formData.append("file", uploadFile.raw);
          formData.append("documentName", uploadFile.raw.name);
          formData.append("uploadUserId", userId);
          formData.append("description", "通过知识库管理界面上传");

          const response = await fetch("/api/kb/documents/upload", {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
            credentials: "include",
            body: formData,
          });

          const result = await response.json();

          if (response.ok && result.code === 200) {
            ElMessage.success("文件上传成功！正在解析文档内容...");

            // 调用文档处理接口
            const processResponse = await fetch(
              `/api/kb/documents/${result.data.documentId}/process`,
              {
                method: "POST",
                headers: {
                  Authorization: `Bearer ${token}`,
                },
                credentials: "include",
              }
            );

            const processResult = await processResponse.json();

            if (processResponse.ok && processResult.code === 200) {
              ElMessage.success("文档已成功解析并存储到知识库！");
              console.log("文档处理结果:", processResult.data);
            } else {
              ElMessage.warning(
                "文档上传成功，但解析失败：" + (processResult.message || "未知错误")
              );
            }

            // 刷新文档列表
            await loadDocuments();
          } else {
            throw new Error(result.message || "上传失败");
          }
        } finally {
          loading.close();
        }
      } catch (error) {
        ElMessage.error(error.message);
      }
    };

    const beforeUpload = (file) => {
      return validateFile(file).catch((error) => {
        ElMessage.error(error.message);
        return false;
      });
    };

    const validateFile = (file) => {
      return new Promise((resolve, reject) => {
        const extension = file.name.split(".").pop().toLowerCase();
        const supportedExtensions = ["pdf", "doc", "docx", "txt"];

        if (!supportedExtensions.includes(extension)) {
          reject(
            new Error(
              `不支持的文件格式: ${extension}，请上传 PDF、Word、TXT 格式的文件`
            )
          );
          return;
        }

        const isLt50M = file.size / 1024 / 1024 <= 50;
        if (!isLt50M) {
          reject(new Error("文件大小不能超过 50MB!"));
          return;
        }

        resolve();
      });
    };

    // 解析JWT token获取用户ID
    const getUserIdFromToken = (token) => {
      try {
        // JWT token由三部分组成，用.分隔，第二部分是payload
        const payload = token.split(".")[1];
        // Base64解码
        const decodedPayload = JSON.parse(atob(payload));
        // JWT中用户ID存储在aud字段中
        return decodedPayload.aud ? decodedPayload.aud[0] : null;
      } catch (error) {
        console.error("解析token失败:", error);
        return null;
      }
    };



    const processDocument = async (documentId) => {
      try {
        const token = getCookie("token");
        const response = await fetch(
          `${API_BASE_URL}/kb/documents/${documentId}/process`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );

        if (response.ok) {
          ElMessage.success("文档处理完成");
        } else {
          ElMessage.warning("文档上传成功，但处理可能需要一些时间");
        }
      } catch (error) {
        console.error("处理文档失败:", error);
        ElMessage.warning("文档上传成功，但处理可能需要一些时间");
      }
    };

    const deleteDocument = async (document) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除文档 "${document.documentName}" 吗？此操作将同时删除相关的知识块。`,
          "确认删除",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );

        const result = await authFetch(
          `${API_BASE_URL}/kb/documents/${document.documentId}`,
          {
            method: "DELETE",
          },
          router
        );

        ElMessage.success("删除成功");
        await loadDocuments();
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除文档失败:", error);
          ElMessage.error("删除文档失败");
        }
      }
    };

    const viewChunks = async (document) => {
      currentDocument.value = document;
      showChunksDialog.value = true;
      chunkCurrentPage.value = 1;
      chunkSearchKeyword.value = "";

      await loadChunks(document.documentId);
    };

    const loadChunks = async (documentId) => {
      chunksLoading.value = true;
      try {
        const result = await authFetch(
          `${API_BASE_URL}/kb/chunks/document/${documentId}`,
          {
            method: "GET",
          },
          router
        );

        console.log("知识块API返回数据:", result);
        // 确保chunks.value始终是数组
        if (Array.isArray(result)) {
          chunks.value = result;
        } else if (result && Array.isArray(result.data)) {
          chunks.value = result.data;
        } else if (result && Array.isArray(result.content)) {
          chunks.value = result.content;
        } else {
          console.warn("API返回的数据格式不正确:", result);
          chunks.value = [];
        }
      } catch (error) {
        console.error("加载知识块失败:", error);
        ElMessage.error("加载知识块失败");
        chunks.value = [];
      } finally {
        chunksLoading.value = false;
      }
    };

    const searchChunks = () => {
      chunkCurrentPage.value = 1;
    };

    const handleRowClick = (row) => {
      if (row.processStatus === "success") {
        viewChunks(row);
      }
    };

    const handleSizeChange = (val) => {
      pageSize.value = val;
      currentPage.value = 1;
      loadDocuments();
    };

    const handleCurrentChange = (val) => {
      currentPage.value = val;
      loadDocuments();
    };

    const handleChunkPageChange = (val) => {
      chunkCurrentPage.value = val;
    };

    const handleUserCommand = (command) => {
      if (command === "logout") {
        document.cookie =
          "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.cookie =
          "username=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        ElMessage.success("已退出登录");
        router.push("/login");
      }
    };

    // 工具函数
    const formatFileSize = (bytes) => {
      if (!bytes) return "0 B";
      const k = 1024;
      const sizes = ["B", "KB", "MB", "GB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
    };

    const formatDate = (dateString) => {
      if (!dateString) return "-";
      const date = new Date(dateString);
      return date.toLocaleString("zh-CN");
    };

    const getStatusType = (status) => {
      const statusMap = {
        uploaded: "info",
        processing: "warning",
        success: "success",
        failed: "danger",
      };
      return statusMap[status] || "info";
    };

    const getStatusText = (status) => {
      const statusMap = {
        uploaded: "已上传",
        processing: "处理中",
        success: "处理成功",
        failed: "处理失败",
      };
      return statusMap[status] || "未知";
    };

    // 生命周期
    onMounted(() => {
      username.value = getCookie("username") || "用户";
      loadDocuments();
    });

    return {
      // 响应式数据
      username,
      loading,
      uploading,
      chunksLoading,
      showChunksDialog,
      selectedFile,
      documents,
      currentPage,
      pageSize,
      total,
      currentDocument,
      chunks,
      chunkSearchKeyword,
      chunkCurrentPage,
      chunkPageSize,
      filteredChunks,
      paginatedChunks,

      // 方法
      handleFileChange,
      beforeUpload,
      deleteDocument,
      viewChunks,
      searchChunks,
      handleRowClick,
      handleSizeChange,
      handleCurrentChange,
      handleChunkPageChange,
      handleUserCommand,
      formatFileSize,
      formatDate,
      getStatusType,
      getStatusText,
    };
  },
};
</script>

<style scoped>
.knowledge-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 导航栏样式 */
.navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
}

.nav-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin-left: 8px;
}

.nav-right {
  display: flex;
  align-items: center;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #2c3e50;
}

.user-trigger:hover {
  background: rgba(103, 126, 234, 0.1);
}

/* 主内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.action-left h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

/* 文档列表样式 */
.document-list {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #3498db;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 上传区域样式 */
.upload-area {
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 知识块相关样式 */
.chunks-content {
  max-height: 600px;
}

.chunks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chunk-stats {
  color: #7f8c8d;
  font-size: 14px;
}

.chunks-list {
  max-height: 400px;
  overflow-y: auto;
}

.chunk-item {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafbfc;
}

.chunk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chunk-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #2c3e50;
}

.chunk-content {
  color: #34495e;
  line-height: 1.6;
  margin-bottom: 8px;
  max-height: 100px;
  overflow-y: auto;
  font-size: 14px;
}

.chunk-keywords {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.keywords-label {
  color: #7f8c8d;
  font-size: 12px;
}

.chunks-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e1e8ed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }

  .action-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .nav-content {
    padding: 0 16px;
  }
}
</style>
