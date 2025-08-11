<template>
  <view class="scroll-error-test-page">
    <view class="header">
      <text class="title">滚动错误修复测试</text>
    </view>
    
    <view class="test-info">
      <text class="info-text">此页面用于测试滚动相关错误的修复效果</text>
      <text class="info-text">特别是 "Cannot set properties of null (setting 'scrollTop')" 错误</text>
    </view>
    
    <view class="test-buttons">
      <button @tap="testNormalScroll" class="test-btn">正常滚动测试</button>
      <button @tap="testRapidScroll" class="test-btn">快速滚动测试</button>
      <button @tap="testScrollAfterDestroy" class="test-btn">销毁后滚动测试</button>
      <button @tap="testNavigateToChat" class="test-btn">跳转聊天页面测试</button>
      <button @tap="clearLogs" class="test-btn clear-btn">清空日志</button>
    </view>
    
    <scroll-view 
      class="message-container"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-into-view="scrollToView"
    >
      <view 
        v-for="(message, index) in messages" 
        :key="index"
        class="message-item"
        :id="`message-${index}`"
      >
        <text>{{ message }}</text>
      </view>
      
      <!-- 底部锚点 -->
      <view id="bottom-anchor" style="height: 1px;"></view>
    </scroll-view>
    
    <view class="log-container">
      <text class="log-title">测试日志:</text>
      <scroll-view class="log-content" scroll-y>
        <text 
          v-for="(log, index) in logs" 
          :key="index" 
          class="log-item"
          :class="{ error: log.includes('错误') || log.includes('失败') }"
        >
          {{ log }}
        </text>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { createSafeScrollHandler, createSafeScrollRefs } from '@/utils/scroll';

// 响应式数据
const messages = ref([]);
const logs = ref([]);
const isDestroyed = ref(false);

// 创建安全的滚动引用
const scrollRefs = createSafeScrollRefs(0, '');
const scrollTop = scrollRefs.scrollTop;
const scrollToView = scrollRefs.scrollToView;

// 创建安全滚动处理器
const scrollHandler = createSafeScrollHandler(scrollToView, scrollTop);

// 添加日志
function addLog(message) {
  const timestamp = new Date().toLocaleTimeString();
  logs.value.push(`[${timestamp}] ${message}`);
  
  // 限制日志数量
  if (logs.value.length > 50) {
    logs.value.shift();
  }
}

// 添加消息
function addMessage(content) {
  messages.value.push(content);
  addLog(`添加消息: ${content}`);
}

// 正常滚动测试
function testNormalScroll() {
  addLog('开始正常滚动测试');
  
  try {
    addMessage(`正常滚动测试消息 - ${new Date().toLocaleTimeString()}`);
    
    setTimeout(() => {
      try {
        scrollHandler.scrollToBottom('bottom-anchor');
        addLog('正常滚动测试成功');
      } catch (error) {
        addLog(`正常滚动测试错误: ${error.message}`);
      }
    }, 100);
  } catch (error) {
    addLog(`正常滚动测试异常: ${error.message}`);
  }
}

// 快速滚动测试
function testRapidScroll() {
  addLog('开始快速滚动测试');
  
  try {
    // 快速添加多条消息并滚动
    for (let i = 0; i < 5; i++) {
      addMessage(`快速滚动测试消息 ${i + 1}`);
      
      setTimeout(() => {
        try {
          scrollHandler.scrollToBottom('bottom-anchor');
          addLog(`快速滚动 ${i + 1} 成功`);
        } catch (error) {
          addLog(`快速滚动 ${i + 1} 错误: ${error.message}`);
        }
      }, i * 50);
    }
  } catch (error) {
    addLog(`快速滚动测试异常: ${error.message}`);
  }
}

// 销毁后滚动测试
function testScrollAfterDestroy() {
  addLog('开始销毁后滚动测试');
  
  try {
    // 模拟页面销毁
    isDestroyed.value = true;
    addLog('模拟页面销毁状态');
    
    // 尝试滚动
    setTimeout(() => {
      try {
        scrollHandler.scrollToBottom('bottom-anchor');
        addLog('销毁后滚动测试完成（应该被跳过）');
      } catch (error) {
        addLog(`销毁后滚动测试错误: ${error.message}`);
      }
    }, 100);
    
    // 恢复正常状态
    setTimeout(() => {
      isDestroyed.value = false;
      addLog('恢复正常状态');
    }, 1000);
  } catch (error) {
    addLog(`销毁后滚动测试异常: ${error.message}`);
  }
}

// 跳转聊天页面测试
function testNavigateToChat() {
  addLog('开始跳转聊天页面测试');
  
  try {
    // 模拟用户详情数据
    const testUser = {
      userId: '123',
      nickname: '测试用户',
      avatarUrl: '/static/message/default-avatar.png'
    };
    
    uni.navigateTo({
      url: `/pages/message/chat?userId=${testUser.userId}&name=${encodeURIComponent(testUser.nickname)}&avatar=${encodeURIComponent(testUser.avatarUrl)}`,
      success: () => {
        addLog('跳转聊天页面成功');
      },
      fail: (error) => {
        addLog(`跳转聊天页面失败: ${JSON.stringify(error)}`);
      }
    });
  } catch (error) {
    addLog(`跳转聊天页面异常: ${error.message}`);
  }
}

// 清空日志
function clearLogs() {
  logs.value = [];
  addLog('日志已清空');
}

// 初始化
onMounted(() => {
  addLog('滚动错误修复测试页面已加载');
  
  // 添加一些初始消息
  for (let i = 1; i <= 10; i++) {
    messages.value.push(`初始测试消息 ${i}`);
  }
  
  addLog(`添加了 ${messages.value.length} 条初始消息`);
});

onUnmounted(() => {
  isDestroyed.value = true;
  addLog('测试页面已销毁');
});
</script>

<style lang="scss" scoped>
.scroll-error-test-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 20rpx;
  background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
}

.header {
  text-align: center;
  padding: 20rpx 0;
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }
}

.test-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  
  .info-text {
    display: block;
    color: rgba(255, 255, 255, 0.9);
    font-size: 24rpx;
    line-height: 1.5;
    margin-bottom: 10rpx;
  }
}

.test-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 20rpx;
  
  .test-btn {
    flex: 1;
    min-width: 200rpx;
    height: 70rpx;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    border: none;
    border-radius: 10rpx;
    font-size: 24rpx;
    
    &.clear-btn {
      background: rgba(255, 107, 107, 0.8);
    }
  }
}

.message-container {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  max-height: 300rpx;
  
  .message-item {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 10rpx;
    padding: 15rpx;
    margin-bottom: 10rpx;
    
    text {
      color: #fff;
      font-size: 26rpx;
    }
  }
}

.log-container {
  height: 300rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10rpx;
  padding: 20rpx;
  
  .log-title {
    color: #fff;
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
  }
  
  .log-content {
    height: 240rpx;
    
    .log-item {
      display: block;
      color: rgba(255, 255, 255, 0.8);
      font-size: 22rpx;
      line-height: 1.4;
      margin-bottom: 5rpx;
      
      &.error {
        color: #ff6b6b;
      }
    }
  }
}
</style>
