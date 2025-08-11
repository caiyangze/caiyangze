<template>
  <view class="match-result-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
      <view class="floating-hearts">
        <text class="heart heart-1">💕</text>
        <text class="heart heart-2">💖</text>
        <text class="heart heart-3">💝</text>
        <text class="heart heart-4">💗</text>
        <text class="heart heart-5">💘</text>
      </view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-left" @click="goBack">
          <text class="nav-icon">←</text>
        </view>
        <text class="nav-title">匹配结果</text>
        <view class="nav-right"></view>
      </view>
      
      <!-- 主要内容 -->
      <view class="main-content">
        <!-- 成功标题 -->
        <view class="success-section">
          <view class="success-icon">✨</view>
          <text class="success-title">匹配成功！</text>
          <text class="success-subtitle">为你找到了心动的TA</text>
        </view>
        
        <!-- 用户卡片 -->
        <view class="user-card">
          <view class="user-avatar-container">
            <image 
              class="user-avatar" 
              :src="matchResult.avatarUrl || '/static/message/default-avatar.png'"
              mode="aspectFill"
            />
            <view class="avatar-border"></view>
          </view>
          
          <view class="user-info">
            <text class="user-name">{{ matchResult.nickname }}</text>
            <view class="user-details">
              <text class="user-age">{{ getDisplayAge() }}岁</text>
              <text class="user-gender">{{ matchResult.gender === 1 ? '♂' : '♀' }}</text>
            </view>
          </view>
          
          <!-- 心动指数 -->
          <view class="heart-rate">
            <text class="heart-rate-label">心动指数</text>
            <view class="heart-rate-bar">
              <view class="heart-rate-fill" :style="{ width: heartRate + '%' }"></view>
            </view>
            <text class="heart-rate-value">{{ heartRate }}%</text>
          </view>
        </view>
        
        <!-- 操作按钮 -->
        <view class="action-buttons">
          <view class="action-btn secondary" @click="viewProfile">
            <text class="btn-icon">👤</text>
            <text class="btn-text">查看资料</text>
          </view>
          
          <view class="action-btn primary" @click="sendMessage">
            <text class="btn-icon">💬</text>
            <text class="btn-text">发送消息</text>
          </view>
        </view>
        
        <!-- 再次匹配按钮 -->
        <view class="rematch-section">
          <view class="rematch-btn" @click="rematch">
            <text class="rematch-text">再次匹配</text>
            <text class="rematch-icon">🔄</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      matchResult: {
        userId: null,
        nickname: '',
        avatarUrl: '',
        gender: 1,
        age: 0
      },
      heartRate: 0
    }
  },
  
  onLoad(options) {
    // 获取匹配结果数据
    if (options.result) {
      try {
        this.matchResult = JSON.parse(decodeURIComponent(options.result))
        console.log('匹配结果:', this.matchResult)

        // 动画显示心动指数
        this.animateHeartRate()
      } catch (error) {
        console.error('解析匹配结果失败:', error)
        uni.showToast({
          title: '数据解析失败',
          icon: 'none'
        })
        this.goBack()
      }
    } else {
      // 如果没有传递结果，使用测试数据
      console.log('使用测试数据')
      this.matchResult = {
        userId: 'test123',
        nickname: '阿巴阿巴',
        avatarUrl: '/static/message/default-avatar.png',
        gender: 2,
        age: 25
      }
      this.animateHeartRate()
    }
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 动画显示心动指数
    animateHeartRate() {
      // 生成随机心动指数 (70-95之间)
      const targetRate = Math.floor(Math.random() * 26) + 70
      
      let currentRate = 0
      const increment = targetRate / 30 // 30步完成动画
      
      const timer = setInterval(() => {
        currentRate += increment
        if (currentRate >= targetRate) {
          currentRate = targetRate
          clearInterval(timer)
        }
        this.heartRate = Math.floor(currentRate)
      }, 50)
    },
    
    // 查看用户资料
    viewProfile() {
      if (this.matchResult.userId) {
        uni.navigateTo({
          url: `/pages/user/user-detail?userId=${this.matchResult.userId}`
        })
      }
    },
    
    // 发送消息
    async sendMessage() {
      if (!this.matchResult.userId) {
        uni.showToast({
          title: '用户信息错误',
          icon: 'none'
        })
        return
      }

      try {
        // 导入聊天API
        const chatApi = await import('@/api/chat.js')

        // 检查是否可以聊天（是否互相关注）
        const result = await chatApi.canChat(this.matchResult.userId)

        if (result.code === 200) {
          if (result.data === true) {
            // 可以聊天，跳转到聊天页面
            uni.navigateTo({
              url: `/pages/message/chat?userId=${this.matchResult.userId}&name=${encodeURIComponent(this.matchResult.nickname)}&avatar=${encodeURIComponent(this.matchResult.avatarUrl || '/static/message/default-avatar.png')}`
            })
          } else {
            // 不能聊天，提示需要互相关注
            uni.showModal({
              title: '提示',
              content: '需要互相关注才能聊天哦～',
              showCancel: false,
              confirmText: '知道了'
            })
          }
        } else {
          uni.showToast({
            title: result.message || '检查聊天权限失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('检查聊天权限失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      }
    },
    
    // 再次匹配
    rematch() {
      uni.navigateBack({
        delta: 1
      })
    },

    // 获取显示年龄
    getDisplayAge() {
      if (this.matchResult.age && this.matchResult.age > 0) {
        return this.matchResult.age
      }
      return '未知'
    }
  }
}
</script>

<style scoped>
.match-result-page {
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

/* 背景层 */
.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
}

.bg-gradient {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 50%, #f8b500 100%);
}

/* 浮动爱心动画 */
.floating-hearts {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.heart {
  position: absolute;
  font-size: 24rpx;
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.heart-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.heart-2 {
  top: 40%;
  right: 15%;
  animation-delay: 1s;
}

.heart-3 {
  top: 60%;
  left: 20%;
  animation-delay: 2s;
}

.heart-4 {
  top: 30%;
  left: 50%;
  animation-delay: 3s;
}

.heart-5 {
  top: 70%;
  right: 25%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
}

/* 内容容器 */
.content-container {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  width: 100%;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(15px);
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: calc(var(--status-bar-height, 44px) + 20rpx);
  background: rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.nav-left {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
}

.nav-right {
  width: 80rpx;
}

/* 主要内容 */
.main-content {
  padding: 40rpx 0 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  flex: 1;
  gap: 40rpx;
  overflow-y: auto;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
}

/* 成功区域 */
.success-section {
  text-align: center;
  flex-shrink: 0;
  margin-top: 60rpx;
  margin-bottom: 20rpx;
}

.success-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.success-title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 15rpx;
}

.success-subtitle {
  font-size: 30rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 用户卡片 */
.user-card {
  width: calc(100% - 40rpx);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30rpx;
  padding: 50rpx 40rpx;
  box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  margin: 0 20rpx;
  box-sizing: border-box;
}

.user-avatar-container {
  position: relative;
  margin-bottom: 30rpx;
}

.user-avatar {
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  border: 5rpx solid #ff6b9d;
}

.avatar-border {
  position: absolute;
  top: -10rpx;
  left: -10rpx;
  right: -10rpx;
  bottom: -10rpx;
  border: 4rpx solid rgba(255, 107, 157, 0.3);
  border-radius: 50%;
  animation: rotate 3s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.user-info {
  text-align: center;
  margin-bottom: 30rpx;
}

.user-name {
  font-size: 38rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.user-details {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.user-age {
  font-size: 28rpx;
  color: #666;
}

.user-gender {
  font-size: 32rpx;
  color: #ff6b9d;
  font-weight: bold;
}

/* 心动指数 */
.heart-rate {
  width: 100%;
  text-align: center;
}

.heart-rate-label {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.heart-rate-bar {
  width: 100%;
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.heart-rate-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff6b9d, #c44569);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

.heart-rate-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff6b9d;
}

/* 操作按钮 */
.action-buttons {
  width: calc(100% - 40rpx);
  display: flex;
  gap: 30rpx;
  margin-bottom: 30rpx;
  margin-left: 20rpx;
  margin-right: 20rpx;
  box-sizing: border-box;
}

.action-btn {
  flex: 1;
  height: 100rpx;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  font-weight: bold;
  transition: all 0.3s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #ff6b9d, #c44569);
  color: white;
  box-shadow: 0 10rpx 30rpx rgba(255, 107, 157, 0.3);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.9);
  color: #666;
  border: 2rpx solid #e0e0e0;
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-size: 28rpx;
}

/* 再次匹配 */
.rematch-section {
  width: calc(100% - 40rpx);
  margin: 0 20rpx 40rpx;
  box-sizing: border-box;
}

.rematch-btn {
  width: 100%;
  height: 90rpx;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  transition: all 0.3s ease;
  backdrop-filter: blur(10rpx);
}

.rematch-text {
  font-size: 28rpx;
  color: white;
}

.rematch-icon {
  font-size: 28rpx;
}

/* 响应式适配 */
@media screen and (max-height: 667px) {
  .main-content {
    padding: 30rpx 20rpx 40rpx;
    gap: 30rpx;
  }

  .success-section {
    margin-top: 40rpx;
    margin-bottom: 15rpx;
  }

  .success-icon {
    font-size: 100rpx;
    margin-bottom: 20rpx;
  }

  .user-card {
    padding: 40rpx 30rpx;
    margin: 0 10rpx;
  }

  .user-avatar {
    width: 160rpx;
    height: 160rpx;
  }
}

/* 安全区域适配 */
@supports (bottom: env(safe-area-inset-bottom)) {
  .main-content {
    padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  }
}

/* 大屏幕适配 */
@media screen and (min-width: 768px) {
  .main-content {
    max-width: 750rpx;
    margin: 0 auto;
  }

  .user-card {
    max-width: 500rpx;
    margin: 0 auto;
  }

  .action-buttons {
    max-width: 500rpx;
    margin: 0 auto 30rpx;
  }

  .rematch-section {
    max-width: 500rpx;
    margin: 0 auto 40rpx;
  }
}

/* 小屏幕适配 */
@media screen and (max-width: 375px) {
  .main-content {
    padding: 40rpx 10rpx 60rpx;
  }

  .user-card {
    width: calc(100% - 20rpx);
    padding: 40rpx 25rpx;
    margin: 0 10rpx;
  }

  .action-buttons {
    width: calc(100% - 20rpx);
    gap: 20rpx;
    margin-left: 10rpx;
    margin-right: 10rpx;
  }

  .rematch-section {
    width: calc(100% - 20rpx);
    margin-left: 10rpx;
    margin-right: 10rpx;
  }
}

/* 调试样式 - 可以临时启用来查看布局 */
/*
.match-result-page {
  border: 2px solid red !important;
}

.content-container {
  border: 2px solid blue !important;
}

.main-content {
  border: 2px solid green !important;
}

.user-card {
  border: 2px solid orange !important;
}
*/
</style>
