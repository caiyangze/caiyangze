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
        <text class="title">简单主题测试</text>
        <text class="subtitle">测试主题切换功能</text>
      </view>
      
      <view class="theme-info">
        <text class="info-title">当前主题信息:</text>
        <text class="info-text">ID: {{ currentThemeId }}</text>
        <text class="info-text">背景: {{ currentBackground }}</text>
      </view>
      
      <!-- 主题切换按钮 -->
      <view class="theme-buttons">
        <button class="theme-btn" @click="switchToTheme('default')">默认</button>
        <button class="theme-btn" @click="switchToTheme('ocean')">海洋</button>
        <button class="theme-btn" @click="switchToTheme('sunset')">日落</button>
        <button class="theme-btn" @click="switchToTheme('forest')">森林</button>
        <button class="theme-btn" @click="switchToTheme('violet')">紫罗兰</button>
        <button class="theme-btn" @click="switchToTheme('sakura')">樱花</button>
        <button class="theme-btn" @click="switchToTheme('dark')">暗黑</button>
      </view>
      
      <!-- 页面导航测试 -->
      <view class="navigation-test">
        <text class="nav-title">页面导航测试</text>
        <button class="nav-btn" @click="goToPage('/pages/test/theme-test')">主题测试页</button>
        <button class="nav-btn" @click="goToPage('/pages/test/theme-global-test')">全局主题测试页</button>
        <button class="nav-btn" @click="goToPage('/pages/test/unified-theme-test')">统一主题测试页</button>
        <button class="nav-btn" @click="goToPage('/pages/test/theme-event-test')">主题事件测试页</button>
      </view>
    </view>
    
    <!-- 底部导航 -->
    <custom-tab-bar />
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import customTabBar from '@/components/custom-tab-bar.vue'
import { getAllThemes, switchTheme, currentThemeId, getCurrentTheme as getSimpleTheme, initTheme as initSimpleTheme, currentBackground } from '@/utils/simple-theme.js'

// 主题相关（简化版本，直接复制底部导航栏的成功模式）
const pageBg = ref('linear-gradient(135deg, #667eea 0%, #764ba2 100%)');

// 监听主题变化（关键！完全复制底部导航栏的成功模式）
watch(currentBackground, (newBg) => {
  console.log('测试页主题变化:', newBg);
  pageBg.value = newBg;
}, { immediate: true });

// 计算属性确保响应式更新（简化版本）
const computedBg = computed(() => {
  return pageBg.value;
});

// 页面初始化
onMounted(() => {
  // 初始化主题（简化版本）
  initSimpleTheme();
  console.log('简单测试页初始化完成，当前背景:', currentBackground.value);
});

// 切换主题
function switchToTheme(themeId) {
  console.log('简单测试页切换到主题:', themeId)
  const success = switchTheme(themeId)
  if (success) {
    uni.showToast({
      title: `已切换到${themeId}主题`,
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
    margin-bottom: 20rpx;
    color: #FFFFFF;
  }
  
  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #FFFFFF;
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
    margin-bottom: 20rpx;
    color: #FFFFFF;
  }
  
  .info-text {
    display: block;
    font-size: 26rpx;
    margin-bottom: 10rpx;
    line-height: 1.5;
    color: rgba(255, 255, 255, 0.8);
  }
}

.theme-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 40rpx;
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
  color: #FFFFFF;
  
  &:active {
    transform: scale(0.95);
  }
  
  .btn-text {
    font-size: 26rpx;
    font-weight: 500;
  }
}

.navigation-test {
  .nav-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
    color: #FFFFFF;
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
  font-size: 28rpx;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
  color: #FFFFFF;
  
  &:active {
    transform: scale(0.95);
  }
}
</style>
