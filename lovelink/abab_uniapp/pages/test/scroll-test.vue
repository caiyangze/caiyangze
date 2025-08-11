<template>
  <view class="scroll-test-page">
    <view class="header">
      <text class="title">滚动功能测试</text>
    </view>
    
    <view class="test-buttons">
      <button @tap="testScrollToBottom" class="test-btn">测试滚动到底部</button>
      <button @tap="testScrollToTop" class="test-btn">测试滚动到顶部</button>
      <button @tap="addMessage" class="test-btn">添加消息</button>
      <button @tap="clearMessages" class="test-btn">清空消息</button>
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
    
    <view class="status">
      <text>消息数量: {{ messages.length }}</text>
      <text>scrollTop: {{ scrollTop }}</text>
      <text>scrollToView: {{ scrollToView }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { createSafeScrollHandler, createSafeScrollRefs } from '@/utils/scroll';

// 响应式数据
const messages = ref([]);

// 创建安全的滚动引用
const scrollRefs = createSafeScrollRefs(0, '');
const scrollTop = scrollRefs.scrollTop;
const scrollToView = scrollRefs.scrollToView;

// 创建安全滚动处理器
const scrollHandler = createSafeScrollHandler(scrollToView, scrollTop);

// 添加消息
function addMessage() {
  const messageCount = messages.value.length + 1;
  messages.value.push(`这是第 ${messageCount} 条测试消息 - ${new Date().toLocaleTimeString()}`);
  
  // 自动滚动到底部
  setTimeout(() => {
    scrollHandler.scrollToBottom('bottom-anchor');
  }, 100);
}

// 测试滚动到底部
function testScrollToBottom() {
  scrollHandler.scrollToBottom('bottom-anchor');
}

// 测试滚动到顶部
function testScrollToTop() {
  scrollHandler.scrollToTop();
}

// 清空消息
function clearMessages() {
  messages.value = [];
}

// 初始化一些测试消息
for (let i = 1; i <= 20; i++) {
  messages.value.push(`初始测试消息 ${i}`);
}
</script>

<style lang="scss" scoped>
.scroll-test-page {
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

.test-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 20rpx;
  
  .test-btn {
    flex: 1;
    min-width: 200rpx;
    height: 80rpx;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    border: none;
    border-radius: 10rpx;
    font-size: 28rpx;
    
    &:active {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.message-container {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.message-item {
  background: rgba(255, 255, 255, 0.2);
  padding: 20rpx;
  margin-bottom: 10rpx;
  border-radius: 10rpx;
  
  text {
    color: #fff;
    font-size: 28rpx;
  }
}

.status {
  background: rgba(255, 255, 255, 0.1);
  padding: 20rpx;
  border-radius: 10rpx;
  
  text {
    display: block;
    color: #fff;
    font-size: 24rpx;
    margin-bottom: 5rpx;
  }
}
</style>
