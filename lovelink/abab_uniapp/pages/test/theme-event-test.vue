<template>
  <view class="test-page" :style="{ background: pageBg }">
    <view class="content">
      <view class="title">主题事件测试页面</view>
      
      <view class="info-section">
        <view class="info-item">
          <text class="label">当前主题ID:</text>
          <text class="value">{{ currentThemeId }}</text>
        </view>
        <view class="info-item">
          <text class="label">当前背景:</text>
          <text class="value">{{ currentBackground }}</text>
        </view>
        <view class="info-item">
          <text class="label">页面背景:</text>
          <text class="value">{{ pageBg }}</text>
        </view>
        <view class="info-item">
          <text class="label">事件接收次数:</text>
          <text class="value">{{ eventCount }}</text>
        </view>
      </view>
      
      <view class="log-section">
        <view class="log-title">事件日志:</view>
        <scroll-view class="log-content" scroll-y>
          <view v-for="(log, index) in logs" :key="index" class="log-item">
            {{ log }}
          </view>
        </scroll-view>
      </view>
      
      <view class="button-section">
        <button @click="testThemeSwitch" class="test-btn">测试主题切换</button>
        <button @click="clearLogs" class="clear-btn">清空日志</button>
        <button @click="goBack" class="back-btn">返回</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { currentThemeId, currentBackground, switchTheme, getAllThemes } from '@/utils/simple-theme.js'

const pageBg = ref('linear-gradient(135deg, #667eea 0%, #764ba2 100%)')
const eventCount = ref(0)
const logs = ref([])

// 添加日志
function addLog(message) {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.unshift(`[${timestamp}] ${message}`)
  if (logs.value.length > 50) {
    logs.value = logs.value.slice(0, 50)
  }
}

// 主题事件处理函数
const handleThemeChange = (data) => {
  eventCount.value++
  addLog(`收到 themeChanged 事件: ${JSON.stringify(data)}`)
  if (data && data.background) {
    pageBg.value = data.background
    addLog(`更新页面背景: ${data.background}`)
  }
}

const handleGlobalThemeUpdate = (data) => {
  eventCount.value++
  addLog(`收到 globalThemeUpdate 事件: ${JSON.stringify(data)}`)
  if (data && data.background) {
    pageBg.value = data.background
    addLog(`更新页面背景: ${data.background}`)
  }
}

const handleSimpleThemeChanged = (data) => {
  eventCount.value++
  addLog(`收到 simpleThemeChanged 事件: ${JSON.stringify(data)}`)
  if (data && data.background) {
    pageBg.value = data.background
    addLog(`更新页面背景: ${data.background}`)
  }
}

const handleForceThemeUpdate = (data) => {
  eventCount.value++
  addLog(`收到 forceThemeUpdate 事件: ${JSON.stringify(data)}`)
  if (data && data.background) {
    pageBg.value = data.background
    addLog(`更新页面背景: ${data.background}`)
  }
}

// 测试主题切换
function testThemeSwitch() {
  const themes = getAllThemes()
  const currentIndex = themes.findIndex(t => t.id === currentThemeId.value)
  const nextIndex = (currentIndex + 1) % themes.length
  const nextTheme = themes[nextIndex]
  
  addLog(`开始切换主题: ${currentThemeId.value} -> ${nextTheme.id}`)
  switchTheme(nextTheme.id)
}

// 清空日志
function clearLogs() {
  logs.value = []
  eventCount.value = 0
  addLog('日志已清空')
}

// 返回
function goBack() {
  uni.navigateBack()
}

onMounted(() => {
  addLog('页面挂载，开始监听主题事件')
  
  // 初始化背景
  pageBg.value = currentBackground.value
  addLog(`初始化背景: ${currentBackground.value}`)
  
  // 监听主题事件
  uni.$on('themeChanged', handleThemeChange)
  uni.$on('globalThemeUpdate', handleGlobalThemeUpdate)
  uni.$on('simpleThemeChanged', handleSimpleThemeChanged)
  uni.$on('forceThemeUpdate', handleForceThemeUpdate)
  
  addLog('主题事件监听已注册')
})

onUnmounted(() => {
  addLog('页面卸载，移除主题事件监听')
  
  // 移除事件监听
  uni.$off('themeChanged', handleThemeChange)
  uni.$off('globalThemeUpdate', handleGlobalThemeUpdate)
  uni.$off('simpleThemeChanged', handleSimpleThemeChanged)
  uni.$off('forceThemeUpdate', handleForceThemeUpdate)
})
</script>

<style lang="scss" scoped>
.test-page {
  min-height: 100vh;
  padding: 40rpx;
}

.content {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20rpx;
  padding: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 40rpx;
  color: #333;
}

.info-section {
  margin-bottom: 40rpx;
}

.info-item {
  display: flex;
  margin-bottom: 20rpx;
  
  .label {
    width: 200rpx;
    font-weight: bold;
    color: #666;
  }
  
  .value {
    flex: 1;
    color: #333;
    word-break: break-all;
  }
}

.log-section {
  margin-bottom: 40rpx;
}

.log-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #333;
}

.log-content {
  height: 400rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  padding: 20rpx;
}

.log-item {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
  word-break: break-all;
}

.button-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.test-btn, .clear-btn, .back-btn {
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.test-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.clear-btn {
  background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%);
  color: #333;
}

.back-btn {
  background: linear-gradient(135deg, #ddd 0%, #bbb 100%);
  color: #333;
}
</style>