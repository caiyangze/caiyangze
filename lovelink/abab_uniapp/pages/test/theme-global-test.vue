<template>
  <view class="test-page" :style="{ background: currentBackground }">
    <!-- 背景层 -->
    <view class="bg-layer" :style="{ background: currentBackground }">
      <view class="bg-gradient"></view>
      <view class="overlay-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <view class="test-content">
        <text class="title">全局主题测试页面</text>
        <text class="desc">测试主题在所有页面的同步效果</text>
        
        <view class="current-theme">
          <text class="theme-info">当前主题ID: {{ currentThemeId }}</text>
          <text class="theme-info">当前背景: {{ currentBackground }}</text>
        </view>
        
        <view class="test-buttons">
          <button 
            v-for="theme in availableThemes" 
            :key="theme.id"
            @tap="switchToTheme(theme.id)" 
            class="theme-btn"
            :class="{ active: currentThemeId === theme.id }"
          >
            {{ theme.name }}
          </button>
        </view>
        
        <view class="navigation-test">
          <text class="nav-title">页面导航测试：</text>
          <button @tap="goToIndex" class="nav-btn">首页</button>
          <button @tap="goToSquare" class="nav-btn">广场</button>
          <button @tap="goToMessage" class="nav-btn">消息</button>
          <button @tap="goToMine" class="nav-btn">我的</button>
        </view>
      </view>
    </view>
    
    <!-- 自定义TabBar -->
    <custom-tab-bar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { currentBackground, currentThemeId, changeTheme, getAllThemes } from '@/utils/simple-theme.js';
import customTabBar from '@/components/custom-tab-bar.vue';

// 可用主题
const availableThemes = ref(getAllThemes());

// 切换主题
function switchToTheme(themeId) {
  console.log('切换到主题:', themeId);
  const success = changeTheme(themeId);
  if (success) {
    uni.showToast({
      title: `已切换到${themeId}主题`,
      icon: 'success'
    });
  } else {
    uni.showToast({
      title: '主题切换失败',
      icon: 'error'
    });
  }
}

// 页面导航
function goToIndex() {
  uni.reLaunch({ url: '/pages/index/index' });
}

function goToSquare() {
  uni.reLaunch({ url: '/pages/square/square' });
}

function goToMessage() {
  uni.reLaunch({ url: '/pages/message/message' });
}

function goToMine() {
  uni.reLaunch({ url: '/pages/mine/mine' });
}
</script>

<style lang="scss" scoped>
.test-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// 背景层
.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.overlay-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.05) 100%);
}

// 内容容器
.content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40rpx 30rpx 110rpx 30rpx;
  z-index: 1;
}

.test-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 20rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.desc {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40rpx;
}

.current-theme {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 40rpx;
  backdrop-filter: blur(10rpx);
}

.theme-info {
  display: block;
  font-size: 24rpx;
  color: #fff;
  margin-bottom: 10rpx;
  word-break: break-all;
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 40rpx;
  width: 100%;
  max-width: 400rpx;
}

.theme-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 50rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  backdrop-filter: blur(10rpx);
  
  &.active {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.6);
    font-weight: bold;
  }
}

.navigation-test {
  width: 100%;
  max-width: 400rpx;
}

.nav-title {
  display: block;
  font-size: 32rpx;
  color: #fff;
  margin-bottom: 20rpx;
  font-weight: bold;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 40rpx;
  height: 70rpx;
  line-height: 70rpx;
  font-size: 26rpx;
  margin: 10rpx;
  backdrop-filter: blur(10rpx);
  display: inline-block;
  min-width: 120rpx;
}
</style>
