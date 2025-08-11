<template>
  <view class="theme-switcher">
    <!-- 主题切换按钮 -->
    <view class="theme-toggle-btn" @tap="showThemeSelector">
      <text class="theme-icon">🎨</text>
      <text class="theme-text">主题</text>
    </view>

    <!-- 主题选择弹窗 -->
    <view v-if="showThemePopup" class="theme-popup-mask" @tap="closeThemeSelector">
      <view class="theme-popup" @tap.stop>
        <view class="theme-popup-header">
          <text class="popup-title">选择主题</text>
          <text class="close-btn" @tap="closeThemeSelector">×</text>
        </view>
        <view class="theme-list">
          <view
            v-for="theme in availableThemes"
            :key="theme.id"
            class="theme-item"
            :class="{ active: currentThemeId === theme.id }"
            @tap="selectTheme(theme.id)"
          >
            <view class="theme-preview" :style="{ background: theme.background }">
              <view class="theme-preview-content">
                <view class="preview-circle" :style="{ backgroundColor: theme.primary }"></view>
                <view class="preview-circle" :style="{ backgroundColor: theme.secondary }"></view>
                <view class="preview-circle" :style="{ backgroundColor: theme.primary }"></view>
              </view>
            </view>
            <text class="theme-name">{{ theme.name }}</text>
            <view v-if="currentThemeId === theme.id" class="theme-check">✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js'

// 使用全局主题混入
const { availableThemes, currentThemeId, switchTheme } = useGlobalThemeMixin()

// 弹窗状态
const showThemePopup = ref(false)

// 显示主题选择器
function showThemeSelector() {
  showThemePopup.value = true
}

// 关闭主题选择器
function closeThemeSelector() {
  showThemePopup.value = false
}

// 选择主题
function selectTheme(themeId) {
  console.log('🎨 用户选择主题:', themeId)
  
  const success = switchTheme(themeId)
  
  if (success) {
    const themeName = availableThemes.value.find(t => t.id === themeId)?.name || '未知主题'
    
    uni.showToast({
      title: `已切换到${themeName}`,
      icon: 'success',
      duration: 1500
    })

    // 延迟关闭弹窗，让用户看到切换效果
    setTimeout(() => {
      closeThemeSelector()
    }, 800)
  } else {
    uni.showToast({
      title: '主题切换失败',
      icon: 'error'
    })
  }
}
</script>

<style lang="scss" scoped>
.theme-switcher {
  position: relative;
}

.theme-toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 30rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50rpx;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  
  .theme-icon {
    font-size: 32rpx;
    margin-right: 10rpx;
  }
  
  .theme-text {
    font-size: 28rpx;
    color: #FFFFFF;
    font-weight: 500;
  }
}

.theme-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.theme-popup {
  width: 600rpx;
  max-height: 80vh;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30rpx;
  backdrop-filter: blur(20rpx);
  overflow: hidden;
}

.theme-popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 30rpx 20rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
  
  .popup-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .close-btn {
    font-size: 48rpx;
    color: #999;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.theme-list {
  padding: 20rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.theme-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  margin-bottom: 20rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.5);
  position: relative;
  transition: all 0.3s ease;
  
  &.active {
    background: rgba(255, 255, 255, 0.8);
    transform: scale(1.02);
  }
}

.theme-preview {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.theme-preview-content {
  display: flex;
  gap: 4rpx;
}

.preview-circle {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.theme-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.theme-check {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 40rpx;
  height: 40rpx;
  background: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}
</style>
