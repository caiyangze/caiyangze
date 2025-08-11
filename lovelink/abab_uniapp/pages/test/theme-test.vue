<template>
  <view class="theme-test-page">
    <!-- 背景层 -->
    <view class="bg-layer" :style="{ background: currentBackground }">
    </view>
    
    <view class="content">
      <view class="header">
        <text class="title">主题测试页面</text>
        <text class="subtitle">当前主题: {{ simpleCurrentTheme.name || '未知' }}</text>
      </view>
      
      <view class="theme-info">
        <text class="info-title">主题信息:</text>
        <text class="info-text">ID: {{ simpleCurrentThemeId }}</text>
        <text class="info-text">名称: {{ simpleCurrentTheme.name }}</text>
        <text class="info-text">主色: {{ simpleCurrentTheme.primary }}</text>
        <text class="info-text">背景: {{ simpleCurrentTheme.background }}</text>
      </view>
      
      <view class="theme-buttons">
        <view 
          v-for="theme in availableThemes" 
          :key="theme.id"
          class="theme-btn"
          :class="{ active: simpleCurrentThemeId === theme.id }"
          @tap="testSwitchTheme(theme.id)"
        >
          <text class="btn-text">{{ theme.name }}</text>
        </view>
      </view>
      
      <view class="debug-info">
        <text class="debug-title">调试信息:</text>
        <text class="debug-text">响应式主题ID: {{ simpleCurrentThemeId }}</text>
        <text class="debug-text">当前主题: {{ simpleCurrentTheme.name }}</text>
        <text class="debug-text">本地存储: {{ localStorageTheme }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTheme } from '@/mixins/theme-mixin.js'
import { getAllThemes, switchTheme, currentThemeId, getCurrentTheme, currentBackground } from '@/utils/simple-theme.js'

// 使用主题混入
const { currentTheme } = useTheme()

// 主题相关数据
const availableThemes = ref(getAllThemes())
const simpleCurrentThemeId = currentThemeId
const simpleCurrentTheme = computed(() => getCurrentTheme())
const localStorageTheme = ref('')

// 测试切换主题
function testSwitchTheme(themeId) {
  console.log('测试切换主题:', themeId)
  switchTheme(themeId)
  
  // 更新本地存储显示
  setTimeout(() => {
    localStorageTheme.value = uni.getStorageSync('selectedTheme') || '无'
  }, 100)
}

onMounted(() => {
  localStorageTheme.value = uni.getStorageSync('selectedTheme') || '无'
  console.log('主题测试页面加载完成')
  console.log('当前主题:', simpleCurrentTheme.value)
  console.log('当前背景:', currentBackground.value)
})
</script>

<style lang="scss" scoped>
.theme-test-page {
  min-height: 100vh;
  position: relative;
}

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
}

.content {
  padding: 40rpx;
  color: white;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
  
  .title {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
  }
  
  .subtitle {
    display: block;
    font-size: 32rpx;
    opacity: 0.8;
  }
}

.theme-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  
  .info-title {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
  }
  
  .info-text {
    display: block;
    font-size: 28rpx;
    margin-bottom: 10rpx;
    opacity: 0.9;
  }
}

.theme-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 40rpx;
  
  .theme-btn {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 15rpx;
    padding: 20rpx;
    text-align: center;
    transition: all 0.3s ease;
    
    &.active {
      background: rgba(255, 255, 255, 0.4);
      transform: scale(1.05);
    }
    
    .btn-text {
      font-size: 28rpx;
      color: white;
    }
  }
}

.debug-info {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 20rpx;
  padding: 30rpx;
  
  .debug-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 20rpx;
    color: #FFD700;
  }
  
  .debug-text {
    display: block;
    font-size: 24rpx;
    margin-bottom: 10rpx;
    opacity: 0.8;
    font-family: monospace;
  }
}
</style>
