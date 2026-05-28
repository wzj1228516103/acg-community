<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <h3>消息列表</h3>
        </div>
        <div class="room-list">
          <div
            v-for="room in rooms"
            :key="room.id"
            class="room-item"
            :class="{ active: currentRoom?.id === room.id }"
            @click="selectRoom(room)"
          >
            <el-avatar :size="44" :src="room.targetAvatar">
              {{ room.targetNickname?.[0] || 'U' }}
            </el-avatar>
            <div class="room-info">
              <div class="room-top">
                <span class="room-name">{{ room.targetNickname || '用户' }}</span>
                <span class="room-time">{{ formatTime(room.lastMessageTime) }}</span>
              </div>
              <span class="room-last-msg">{{ room.lastMessage || '暂无消息' }}</span>
            </div>
          </div>
          <el-empty v-if="rooms.length === 0" description="暂无会话" :image-size="80" />
        </div>
      </div>

      <div class="chat-main">
        <template v-if="currentRoom">
          <div class="chat-header">
            <span class="chat-title">{{ currentRoom.targetNickname || '用户' }}</span>
          </div>

          <div ref="messageListRef" class="message-list">
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ self: msg.senderId === userStore.user?.id }">
              <el-avatar v-if="msg.senderId !== userStore.user?.id" :size="36" :src="currentRoom.targetAvatar">
                {{ currentRoom.targetNickname?.[0] || 'U' }}
              </el-avatar>
              <div class="message-bubble">
                {{ msg.content }}
              </div>
              <el-avatar v-if="msg.senderId === userStore.user?.id" :size="36" :src="userStore.user?.avatarUrl">
                {{ userStore.user?.nickname?.[0] || 'U' }}
              </el-avatar>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无消息，发送第一条消息吧" :image-size="60" />
          </div>

          <div class="chat-input">
            <el-input
              v-model="messageContent"
              placeholder="输入消息..."
              @keyup.enter="handleSend"
            />
            <el-button type="primary" round :loading="sending" :disabled="!messageContent.trim()" @click="handleSend">
              发送
            </el-button>
          </div>
        </template>
        <div v-else class="no-chat">
          <el-icon :size="64" class="no-chat-icon"><ChatDotSquare /></el-icon>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotSquare } from '@element-plus/icons-vue'
import { getRoomsApi, getMessagesApi, sendMessageApi } from '@/api/chat'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const rooms = ref([])
const currentRoom = ref(null)
const messages = ref([])
const messageContent = ref('')
const loadingRooms = ref(true)
const loadingMessages = ref(false)
const sending = ref(false)
const messageListRef = ref()

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isSameDay = date.toDateString() === now.toDateString()
  if (isSameDay) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

async function selectRoom(room) {
  currentRoom.value = room
  loadingMessages.value = true
  try {
    const res = await getMessagesApi(room.id)
    messages.value = res.data || []
    scrollToBottom()
  } catch {
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

async function handleSend() {
  if (!messageContent.value.trim() || !currentRoom.value) return
  sending.value = true
  try {
    await sendMessageApi(currentRoom.value.id, messageContent.value)
    messages.value.push({
      id: Date.now(),
      roomId: currentRoom.value.id,
      senderId: userStore.user?.id,
      content: messageContent.value,
    })
    messageContent.value = ''
    scrollToBottom()
  } catch {
    // handled by interceptor
  } finally {
    sending.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getRoomsApi()
    rooms.value = res.data || []
  } catch {
    rooms.value = []
  } finally {
    loadingRooms.value = false
  }
})
</script>

<style lang="scss" scoped>
.chat-page {
  height: calc(100vh - 64px);
  background: #f8fafc;
  overflow: hidden;
}

.chat-container {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  background: white;
  border-radius: 0;
  overflow: hidden;
}

.chat-sidebar {
  width: 320px;
  border-right: 1px solid #f3f4f6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  @media (max-width: 768px) {
    width: 100%;
  }

  .sidebar-header {
    padding: 20px;
    border-bottom: 1px solid #f3f4f6;

    h3 {
      font-size: 18px;
      font-weight: 600;
      background: linear-gradient(135deg, #ec4899, #a855f7);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .room-list {
    flex: 1;
    overflow-y: auto;
  }

  .room-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #fdf4ff;
    }

    &.active {
      background: linear-gradient(135deg, rgba(236, 72, 153, 0.08), rgba(168, 85, 247, 0.08));
    }

    .room-info {
      flex: 1;
      min-width: 0;

      .room-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .room-name {
          font-size: 14px;
          font-weight: 600;
          color: #1f2937;
        }

        .room-time {
          font-size: 12px;
          color: #9ca3af;
          flex-shrink: 0;
        }
      }

      .room-last-msg {
        font-size: 13px;
        color: #9ca3af;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;

  .chat-header {
    padding: 16px 24px;
    border-bottom: 1px solid #f3f4f6;

    .chat-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .message-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;

    &.self {
      flex-direction: row-reverse;

      .message-bubble {
        background: linear-gradient(135deg, #ec4899, #a855f7);
        color: white;
      }
    }

    .message-bubble {
      max-width: 60%;
      padding: 10px 16px;
      border-radius: 16px;
      background: #f3f4f6;
      color: #1f2937;
      font-size: 14px;
      line-height: 1.5;
      word-break: break-word;
    }
  }

  .no-chat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #d1d5db;

    .no-chat-icon {
      margin-bottom: 16px;
    }

    p {
      font-size: 16px;
    }
  }

  .chat-input {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid #f3f4f6;

    :deep(.el-input__wrapper) {
      border-radius: 24px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    }
  }
}
</style>
