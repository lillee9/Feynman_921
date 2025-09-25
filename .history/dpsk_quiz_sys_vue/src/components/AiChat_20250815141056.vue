<template>
  <el-container
    style="
      height: 100vh;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
        sans-serif;
    "
  >
    <!-- 顶部导航栏 - Apple风格 -->
    <el-header
      style="
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(20px);
        border-bottom: 1px solid rgba(0, 0, 0, 0.1);
        color: #1d1d1f;
        display: flex;
        align-items: center;
        height: 56px;
        padding: 0 20px;
      "
    >
      <el-button
        type="text"
        @click="goBack"
        style="
          color: #007aff;
          margin-right: 16px;
          padding: 8px;
          border-radius: 8px;
          transition: all 0.2s;
        "
        :hover-style="{ backgroundColor: 'rgba(0, 122, 255, 0.1)' }"
      >
        <el-icon :size="18">
          <ArrowLeft />
        </el-icon>
      </el-button>
      <div style="font-size: 17px; font-weight: 600; letter-spacing: -0.4px">
        智能问答
      </div>
    </el-header>

    <!-- 主体区域 -->
    <el-container style="height: calc(100vh - 56px); position: relative">
      <!-- 侧边栏切换按钮 - Apple风格 -->
      <div
        @click="toggleSidebar"
        :style="{
          position: 'absolute',
          left: sidebarCollapsed ? '20px' : `calc(${sidebarWidth} + 8px)`,
          top: '20px',
          width: '32px',
          height: '32px',
          background: 'rgba(255, 255, 255, 0.9)',
          backdropFilter: 'blur(20px)',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          boxShadow: '0 4px 16px rgba(0, 0, 0, 0.12)',
          zIndex: 10,
          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          border: '1px solid rgba(0, 0, 0, 0.04)',
        }"
      >
        <el-icon :size="14" style="color: #666">
          <component :is="sidebarCollapsed ? 'ArrowRight' : 'ArrowLeft'" />
        </el-icon>
      </div>

      <!-- 左侧边栏 - Apple风格 -->
      <el-aside
        :width="sidebarWidth"
        :style="{
          background: 'rgba(255, 255, 255, 0.9)',
          backdropFilter: 'blur(20px)',
          borderRight: '1px solid rgba(0, 0, 0, 0.08)',
          transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
          position: 'relative',
          overflow: 'hidden',
        }"
      >
        <div
          style="
            padding: 20px;
            height: 100%;
            display: flex;
            flex-direction: column;
          "
        >
          <div
            style="
              margin-bottom: 20px;
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <span
              style="
                font-weight: 600;
                color: #1d1d1f;
                font-size: 15px;
                letter-spacing: -0.2px;
              "
              >历史会话</span
            >
            <el-button
              type="primary"
              size="small"
              @click="createNewConversation"
              style="
                background: #007aff;
                border: none;
                border-radius: 16px;
                padding: 6px 12px;
                font-size: 13px;
                font-weight: 500;
                box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
                transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
              "
            >
              新建
            </el-button>
          </div>
          <el-scrollbar style="flex: 1">
            <div style="space-y: 4px">
              <div
                v-for="conversation in conversations"
                :key="conversation.conversationId"
                @click="handleSelectConversation(conversation)"
                :style="{
                  padding: '12px 16px',
                  borderRadius: '12px',
                  cursor: 'pointer',
                  transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                  marginBottom: '4px',
                  position: 'relative',
                  background:
                    nowConversation === conversation.conversationId
                      ? 'rgba(0, 122, 255, 0.1)'
                      : 'transparent',
                }"
                @mouseenter="
                  $event.target.style.background =
                    nowConversation === conversation.conversationId
                      ? 'rgba(0, 122, 255, 0.15)'
                      : 'rgba(0, 0, 0, 0.04)'
                "
                @mouseleave="
                  $event.target.style.background =
                    nowConversation === conversation.conversationId
                      ? 'rgba(0, 122, 255, 0.1)'
                      : 'transparent'
                "
              >
                <div
                  style="
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                  "
                >
                  <span
                    :style="{
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      fontSize: '14px',
                      fontWeight: '500',
                      color:
                        nowConversation === conversation.conversationId
                          ? '#007aff'
                          : '#1d1d1f',
                      letterSpacing: '-0.2px',
                    }"
                    :title="conversation.title || '新对话'"
                  >
                    {{ conversation.title || "新对话" }}
                  </span>
                  <div class="conversation-actions">
                    <el-button
                      type="text"
                      icon="Edit"
                      @click.stop="renameConversation(conversation)"
                      style="color: #666; padding: 4px; border-radius: 6px"
                    />
                    <el-button
                      type="text"
                      icon="Delete"
                      @click.stop="
                        deleteConversation(conversation.conversationId)
                      "
                      style="color: #ff3b30; padding: 4px; border-radius: 6px"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
      </el-aside>

      <!-- 右侧聊天区域 -->
      <el-main
        style="
          padding: 20px;
          display: flex;
          justify-content: center;
          height: 100%;
          overflow: hidden;
        "
      >
        <div
          style="
            width: 100%;
            max-width: 800px;
            height: 100%;
            display: flex;
            flex-direction: column;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: 20px;
            border: 1px solid rgba(0, 0, 0, 0.06);
            overflow: hidden;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
          "
        >
          <!-- 空状态提示 -->
          <div
            v-if="!hasInitialized"
            style="
              flex: 1;
              display: flex;
              align-items: center;
              justify-content: center;
              color: #666;
            "
          >
            <el-icon
              class="is-loading"
              style="margin-right: 8px; color: #007aff"
              ><Loading
            /></el-icon>
            <span style="font-size: 15px; font-weight: 500"
              >正在加载对话...</span
            >
          </div>

          <!-- 没有对话时的提示 -->
          <div
            v-else-if="!activeConversation"
            style="
              flex: 1;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              color: #8e8e93;
            "
          >
            <div style="margin-bottom: 20px; font-size: 17px; font-weight: 500">
              💼 欢迎使用贷后催收培训助手！我可以帮您提升催收技能 📈
            </div>
            <el-button
              type="primary"
              @click="createNewConversation"
              style="
                background: #007aff;
                border: none;
                border-radius: 20px;
                padding: 12px 24px;
                font-size: 15px;
                font-weight: 500;
                box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
              "
            >
              开始新对话
            </el-button>
          </div>

          <!-- 正常聊天界面 -->
          <div
            v-else
            class="chat-container"
            style="height: 100%; display: flex; flex-direction: column"
          >
            <!-- 聊天消息区域 -->
            <el-scrollbar style="flex: 1; padding: 20px">
              <div class="messages-container">
                <div
                  class="message"
                  v-for="chat in chats"
                  :key="chat.id"
                  :class="chat.role"
                >
                  <!-- 头像 -->
                  <div class="avatar" v-if="chat.role !== 'user'">
                    <img
                      :src="roleInfo[chat.role].avatar || ''"
                      alt=""
                      v-if="roleInfo[chat.role].avatar"
                    />
                    <div class="default-avatar" v-else>
                      {{ roleInfo[chat.role].name.charAt(0) }}
                    </div>
                  </div>
                  <div
                    class="content"
                    :class="{ loading: chat.status === 'loading' }"
                  >
                    {{ chat.content }}
                    <div class="loading-dots" v-if="chat.status === 'loading'">
                      <span>.</span><span>.</span><span>.</span>
                    </div>
                  </div>
                </div>
                <!-- 底部占位 -->
                <div ref="messagesEnd" style="height: 20px"></div>
              </div>
            </el-scrollbar>

            <!-- 输入区域 - Apple风格 -->
            <div class="input-container">
              <div style="display: flex; align-items: flex-end; gap: 12px">
                <el-input
                  v-model="inputMessage"
                  placeholder="输入消息..."
                  :rows="1"
                  type="textarea"
                  resize="none"
                  @keyup.enter.exact="handleSend"
                  style="flex: 1"
                />
                <el-button
                  type="primary"
                  @click="handleSend"
                  :style="{
                    background: inputMessage.trim() ? '#007aff' : '#c7c7cc',
                    border: 'none',
                    borderRadius: '50%',
                    width: '44px',
                    height: '44px',
                    padding: '0',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                    boxShadow: inputMessage.trim()
                      ? '0 2px 8px rgba(0, 122, 255, 0.3)'
                      : 'none',
                  }"
                >
                  <el-icon :size="18">
                    <Top />
                  </el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>

  <!-- 重命名对话框 - Apple风格 -->
  <el-dialog
    v-model="renameDialogVisible"
    title="重命名对话"
    width="400px"
    :style="{
      borderRadius: '16px',
      overflow: 'hidden',
    }"
  >
    <el-input
      v-model="renameInput"
      placeholder="请输入新名称"
      style="margin-bottom: 20px"
    />
    <template #footer>
      <div style="display: flex; gap: 12px; justify-content: flex-end">
        <el-button
          @click="renameDialogVisible = false"
          style="
            background: transparent;
            border: 1px solid #d1d1d6;
            color: #007aff;
            border-radius: 12px;
            padding: 10px 20px;
            font-weight: 500;
          "
        >
          取消
        </el-button>
        <el-button
          type="primary"
          @click="confirmRename"
          style="
            background: #007aff;
            border: none;
            border-radius: 12px;
            padding: 10px 20px;
            font-weight: 500;
            box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
          "
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, onMounted, watch, computed } from "vue";
import { useUserStore } from "../stores/user";
import { ElMessage, ElMessageBox } from "element-plus";
import logo from "../assets/deepseek.png";
import axios from "axios";

const userStore = useUserStore();

// 侧边栏状态
const sidebarCollapsed = ref(false);
const sidebarWidth = computed(() => (sidebarCollapsed.value ? "0px" : "280px"));

// 聊天消息
const chats = ref([]);
const inputMessage = ref("");
const messagesEnd = ref(null);

// 对话相关状态
const conversations = ref([]);
const activeConversation = ref("");
const hasInitialized = ref(false);
const nowConversation = ref("");
const isGenerating = ref(false);

// 重命名相关状态
const renameDialogVisible = ref(false);
const renameInput = ref("");
const currentRenameConversation = ref(null);

// 角色信息
const roleInfo = {
  user: {
    name: "User",
    avatar: "",
  },
  assistant: {
    name: "DeepSeek",
    avatar: logo,
  },
  system: {
    name: "System",
    avatar:
      "https://lf3-static.bytednsdoc.com/obj/eden-cn/ptlz_zlp/ljhwZthlaukjlkulzlp/other/logo.png",
  },
};

let count = 0;

// 滚动到底部函数
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesEnd.value) {
      messagesEnd.value.scrollIntoView({ behavior: "smooth" });
    }
  });
};

// 监听消息变化
watch(() => chats.value.length, scrollToBottom);
watch(() => chats.value[chats.value.length - 1]?.content, scrollToBottom);

// 组件挂载时加载最近对话
onMounted(() => {
  loadRecentConversations();
  setTimeout(scrollToBottom, 100);
});

// 加载最近对话
const loadRecentConversations = async () => {
  console.log("加载最近对话");
  try {
    const response = await fetch(
      `/api/conversation/getAll?userId=${userStore.id}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error("获取对话列表失败");
    }

    const res = await response.json();
    conversations.value = res.data;

    // 如果有对话列表，选择更新时间最近的对话
    if (conversations.value.length > 0) {
      // 按照 updatedAt 字段降序排序
      conversations.value.sort(
        (a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)
      );

      // 选择排序后的第一个对话
      nowConversation.value = conversations.value[0].conversationId;
      activeConversation.value = conversations.value[0].conversationId;
      loadConversationMessages(activeConversation.value);
    } else {
      activeConversation.value = "";
    }

    hasInitialized.value = true;
  } catch (error) {
    console.error("加载对话列表出错:", error);
    ElMessage.error("加载对话列表失败");
    hasInitialized.value = true;
  }
};

// 侧边栏操作
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value;
};

// 处理对话点击
const handleSelectConversation = (conversation) => {
  if (isGenerating.value) {
    ElMessage.warning("正在生成对话，请勿切换");
    return;
  }
  chats.value = [];
  if (conversation.conversationId === -1) {
    chats.value = [];
    chats.value.push({
      role: "assistant",
      id: "temporary-id",
      createAt: Date.now(),
      content:
        "你好！我是你的贷后催收培训小助手。我可以帮你解答催收相关的问题，提供话术指导，分析案例，或者进行催收技能培训。有什么我可以帮助你的吗？",
    });
    nowConversation.value = -1;
    return;
  }
  nowConversation.value = conversation.conversationId;
  activeConversation.value = conversation.conversationId;
  loadConversationMessages(conversation.conversationId);
};

const createNewConversation = () => {
  if (!hasInitialized.value) return;

  if (isGenerating.value) {
    ElMessage.warning("对话正在生成");
    return;
  }
  if (activeConversation.value === "new") {
    return;
  }

  nowConversation.value = -1;
  activeConversation.value = "new";
  const newConversation = {
    conversationId: -1,
    title: "新对话",
  };
  conversations.value.unshift(newConversation);

  chats.value = [];
  chats.value.push({
    role: "assistant",
    id: "temporary-id",
    createAt: Date.now(),
    content:
      "你好！我是你的贷后催收培训小助手。我可以帮你解答催收相关的问题，提供话术指导，分析案例，或者进行催收技能培训。有什么我可以帮助你的吗？",
  });
};

const deleteConversation = async (conversationId) => {
  try {
    await ElMessageBox.confirm("确定要删除此对话吗?", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    if (isGenerating.value) {
      ElMessage.warning("对话正在生成");
      return;
    }

    if (nowConversation.value === -1) {
      ElMessage.info("对话尚未建立");
      return;
    }
    // api调用
    const response = await axios.delete(`/api/conversation/${conversationId}`);
    console.log(response);

    if (response.data.code === 200) {
      // 假设成功返回的code是200
      ElMessage.success("删除成功");
      // 这里可以添加删除成功后的其他操作，比如刷新对话列表
      loadRecentConversations();
    } else {
      ElMessage.error(response.data.message || "删除失败");
    }
  } catch (error) {
    if (error !== "cancel") {
      // 用户点击了取消按钮，不需要显示错误
      ElMessage.error("删除失败");
    }
  }
};

const renameConversation = (conversation) => {
  if (isGenerating.value) {
    ElMessage.warning("对话正在生成");
    return;
  }
  if (nowConversation.value === -1) {
    ElMessage.info("对话尚未建立");
    return;
  }

  currentRenameConversation.value = conversation;
  renameInput.value = conversation.title || "";
  renameDialogVisible.value = true;
};

const confirmRename = async () => {
  currentRenameConversation.value.title = renameInput.value;
  console.log("currentRenameConversation");
  console.log(currentRenameConversation.value);

  const response = await fetch(`/api/conversation/edit`, {
    method: "POST",
    body: JSON.stringify(currentRenameConversation.value),
    headers: {
      "Content-Type": "application/json",
    },
  });
  //const res = await response.json();
  if (response.ok) {
    ElMessage.success("重命名成功");
  } else {
    throw new Error("网络错误，请重试");
  }
  loadRecentConversations();
  renameDialogVisible.value = false;
};

const loadConversationMessages = async (conversationId) => {
  console.log("加载对话中的消息记录 #3");

  chats.value = [];

  try {
    const response = await fetch(
      `/api/message/getAll?conversationId=${conversationId}`
    );
    const result = await response.json();

    if (result.code === 200) {
      chats.value = result.data.map((message) => ({
        id: message.messageId,
        role: message.senderType === "USER" ? "user" : "assistant",
        content: message.content,
        status: "done",
        sentAt: message.sentAt,
      }));
      console.log(chats.value);
    } else {
      console.error("获取消息失败:", result.message);
      chats.value = [
        {
          id: 0,
          role: "assistant",
          content: "获取对话消息失败，请稍后重试",
          status: "done",
        },
      ];
    }
  } catch (error) {
    console.error("请求出错:", error);
    chats.value = [
      {
        id: 0,
        role: "assistant",
        content: "网络错误，无法加载消息",
        status: "done",
      },
    ];
  }
};

// 发送消息处理
const handleSend = () => {
  if (!inputMessage.value.trim()) {
    return;
  }

  sendMessage(inputMessage.value);
  inputMessage.value = "";
};

const sendMessage = async (message) => {
  if (!message) {
    return;
  }
  isGenerating.value = true;
  //新对话中发送消息
  if (nowConversation.value === -1) {
    console.log("新对话中发送消息");
    var conversation = {
      userId: userStore.id,
      title: message.slice(0, 15),
    };
    const response = await fetch(`/api/conversation/add`, {
      method: "POST",
      body: JSON.stringify(conversation),
      headers: {
        "Content-Type": "application/json",
      },
    });
    const res = await response.json();
    if (response.ok) {
      console.log(conversations.value);
      nowConversation.value = res.data.conversationId;
      activeConversation.value = res.data.conversationId;
      conversations.value[0].title = res.data.title.slice(0, 15);
      conversations.value[0].conversationId = res.data.conversationId;
      chats.value = [];
    } else {
      loadRecentConversations();
      throw new Error("网络错误，请重试");
    }
  }

  chats.value.push({
    role: "user",
    id: "temporary-id",
    createAt: Date.now(),
    content: message || "",
  });

  chats.value.push({
    role: "assistant",
    id: "temporary-id",
    createAt: Date.now(),
    content: "",
    status: "loading",
  });

  scrollToBottom();
  console.log(chats.value);
  const messages = chats.value
    .filter((c) => c.role === "user" || c.role === "assistant")
    .map((c) => ({
      role: c.role,
      content: c.content,
    }));
  console.log("传给后端的chats: ");
  console.log(chats.value);
  const params = new URLSearchParams();
  params.append("messages", JSON.stringify(messages));
  params.append("conversationId", Number(activeConversation.value));

  const url = `/api/stream?${params.toString()}`;
  const eventSource = new EventSource(url, {
    withCredentials: true,
  });

  eventSource.onmessage = (event) => {
    let response = event.data;

    if (response === "end") {
      const lastMessage = chats.value[chats.value.length - 1];
      if (lastMessage) {
        lastMessage.status = "complete";
      }
      isGenerating.value = false;
      close();
      scrollToBottom();
      return;
    }

    try {
      response = JSON.parse(response).content;
      const lastMessage = chats.value[chats.value.length - 1];
      if (lastMessage) {
        lastMessage.status = "incomplete";
        lastMessage.content += response;
      }
      scrollToBottom();
    } catch (error) {
      console.error("Error parsing response:", error);
    }
  };

  eventSource.onerror = () => {
    const lastMessage = chats.value[chats.value.length - 1];
    if (lastMessage) {
      lastMessage.status = "error";
    }
    close();
    scrollToBottom();
  };

  const close = () => {
    eventSource.close();
  };
};

const goBack = () => {
  window.history.back();
};
</script>

<style scoped>
/* Apple风格样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.messages-container {
  flex: 1;
  overflow-y: hidden;
  padding-bottom: 16px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  font-size: 14px;
}

.content {
  padding: 12px 16px;
  border-radius: 18px;
  max-width: 70%;
  word-break: break-word;
  white-space: pre-wrap;
  font-size: 15px;
  line-height: 1.4;
  letter-spacing: -0.2px;
}

.user .content {
  background: #007aff;
  color: white;
  border-radius: 18px 18px 4px 18px;
  margin-left: auto;
  box-shadow: 0 2px 12px rgba(0, 122, 255, 0.25);
}

.user {
  justify-content: flex-end;
}

.assistant .content {
  background: rgba(0, 0, 0, 0.04);
  color: #1d1d1f;
  border-radius: 18px 18px 18px 4px;
  backdrop-filter: blur(10px);
}

.system .content {
  background: rgba(0, 0, 0, 0.02);
  color: #8e8e93;
  border-radius: 18px;
}

.loading-dots span {
  animation: loading 1.4s infinite;
  display: inline-block;
  color: #8e8e93;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes loading {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 1;
  }
}

.input-container {
  padding: 20px;
  background: rgba(248, 248, 248, 0.8);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.input-container :deep(.el-textarea__inner) {
  border-radius: 20px;
  padding: 12px 16px;
  min-height: 44px !important;
  resize: none;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  font-size: 15px;
  line-height: 1.4;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-container :deep(.el-textarea__inner:focus) {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.conversation-actions {
  opacity: 0;
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  gap: 4px;
}

.conversation-actions:hover {
  opacity: 1;
}

/* 滚动条样式 - Apple风格 */
:deep(::-webkit-scrollbar) {
  width: 4px;
}

:deep(::-webkit-scrollbar-track) {
  background: transparent;
}

:deep(::-webkit-scrollbar-thumb) {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

:deep(::-webkit-scrollbar-thumb:hover) {
  background: rgba(0, 0, 0, 0.3);
}

/* 对话项悬停效果 */
.conversation-item:hover .conversation-actions {
  opacity: 1;
}

/* Element Plus 组件覆写 */
:deep(.el-button) {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(20px);
}

:deep(.el-dialog__header) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

:deep(.el-dialog__body) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
}

:deep(.el-input__inner) {
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-input__inner:focus) {
  border-color: #007aff;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}
</style>
