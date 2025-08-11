<template>
  <view class="test-page" :style="{ background: computedBg }">
    <!-- 背景层 -->
    <view class="bg-layer" :style="{ background: computedBg }">
      <view class="bg-gradient"></view>
      <view class="overlay-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <view class="header">
        <text class="title">统一主题测试</text>
        <text class="subtitle">测试所有页面主题同步</text>
      </view>
      
      <view class="theme-info">
        <text class="info-title">当前主题信息:</text>
        <text class="info-text">ID: {{ currentThemeId }}</text>
        <text class="info-text">名称: {{ currentTheme.name }}</text>
        <text class="info-text">主色: {{ currentTheme.primary }}</text>
        <text class="info-text">背景: {{ currentTheme.background }}</text>
      </view>
      
      <!-- 主题切换组件 -->
      <view class="theme-switcher-container">
        <theme-switcher />
      </view>
      
      <!-- 快速切换按钮 -->
      <view class="quick-switch">
        <text class="section-title">快速切换:</text>
        <view class="theme-buttons">
          <view 
            v-for="theme in availableThemes" 
            :key="theme.id"
            class="theme-btn"
            :class="{ active: currentThemeId === theme.id }"
            @tap="switchToTheme(theme.id)"
          >
            <text class="btn-text">{{ theme.name }}</text>
          </view>
        </view>
      </view>
      
      <!-- 页面导航测试 -->
      <view class="navigation-test">
        <text class="section-title">页面导航测试:</text>
        <view class="nav-buttons">
          <button class="nav-btn" @tap="goToIndex">首页</button>
          <button class="nav-btn" @tap="goToSquare">广场</button>
          <button class="nav-btn" @tap="goToMessage">消息</button>
          <button class="nav-btn" @tap="goToMine">我的</button>
        </view>
      </view>
    </view>
    
    <!-- 底部导航 -->
    <custom-tab-bar />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useTheme } from '@/mixins/theme-mixin.js'
import { currentBackground } from '@/utils/simple-theme.js'
import customTabBar from '@/components/custom-tab-bar.vue'
import themeSwitcher from '@/components/theme-switcher.vue'

// 计算属性确保响应式更新（完全参考mine页面的成功实现）
const computedBg = computed(() => {
  // 使用统一主题混入的背景
  return computedBackground.value || currentBackground.value;
});

// 使用统一主题混入（完全参考mine页面）
const {
  currentTheme,
  currentThemeId,
  availableThemes,
  computedBackground,
  changeTheme
} = useTheme()

// 切换主题
function switchToTheme(themeId) {
  console.log('快速切换到主题:', themeId)
  const success = changeTheme(themeId)
  if (success) {
    uni.showToast({
      title: `已切换到${currentTheme.value.name}`,
      icon: 'success'
    })
  } else {
    uni.showToast({
      title: '主题切换失败',
      icon: 'error'
    })
  }
}

// 页面导航
function goToIndex() {
  uni.reLaunch({ url: '/pages/index/index' })
}

function goToSquare() {
  uni.reLaunch({ url: '/pages/square/square' })
}

function goToMessage() {
  uni.reLaunch({ url: '/pages/message/message' })
}

function goToMine() {
  uni.reLaunch({ url: '/pages/mine/mine' })
}
</script>

<style lang="scss" scoped>
.test-page {
  min-height: 100vh;
  position: relative;
}

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  
  .bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(45deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  }
  
  .overlay-gradient {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 40%;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.1) 0%, transparent 100%);
  }
}

.content-container {
  padding: 40rpx 30rpx 200rpx;
  position: relative;
  z-index: 1;
}

.header {
  text-align: center;
  margin-bottom: 60rpx;
  
  .title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: #FFFFFF;
    margin-bottom: 20rpx;
  }
  
  .subtitle {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.theme-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  backdrop-filter: blur(10rpx);
  
  .info-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #FFFFFF;
    margin-bottom: 20rpx;
  }
  
  .info-text {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 10rpx;
    line-height: 1.5;
  }
}

.theme-switcher-container {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
}

.quick-switch {
  margin-bottom: 40rpx;
  
  .section-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #FFFFFF;
    margin-bottom: 20rpx;
  }
}

.theme-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.theme-btn {
  flex: 1;
  min-width: 200rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  
  &.active {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.05);
  }
  
  .btn-text {
    font-size: 26rpx;
    color: #FFFFFF;
    font-weight: 500;
  }
}

.navigation-test {
  .section-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #FFFFFF;
    margin-bottom: 20rpx;
  }
}

.nav-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.nav-btn {
  flex: 1;
  min-width: 200rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 40rpx;
  border: none;
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.95);
  }
}
</style>
