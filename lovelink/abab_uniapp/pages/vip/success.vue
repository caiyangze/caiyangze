<template>
  <view class="success-page">
    <!-- 背景装饰 -->
    <view class="bg-decoration">
      <view class="floating-crown crown-1">👑</view>
      <view class="floating-crown crown-2">👑</view>
      <view class="floating-crown crown-3">👑</view>
      <view class="floating-star star-1">⭐</view>
      <view class="floating-star star-2">⭐</view>
      <view class="floating-star star-3">⭐</view>
    </view>
    
    <!-- 成功内容 -->
    <view class="success-content">
      <!-- 成功图标 -->
      <view class="success-icon">
        <text class="check-mark">✓</text>
      </view>
      
      <!-- 成功标题 -->
      <text class="success-title">VIP开通成功！</text>
      <text class="success-subtitle">恭喜您成为尊贵的VIP会员</text>
      
      <!-- VIP信息卡片 -->
      <view class="vip-info-card">
        <view class="vip-header">
          <text class="vip-badge">VIP</text>
          <text class="vip-type">{{ getVipTypeName() }}</text>
        </view>
        
        <view class="vip-details">
          <view class="detail-item">
            <text class="detail-label">订单号：</text>
            <text class="detail-value">{{ orderData.orderNo }}</text>
          </view>
          
          <view class="detail-item">
            <text class="detail-label">支付方式：</text>
            <text class="detail-value">{{ orderData.payMethodName }}</text>
          </view>
          
          <view class="detail-item">
            <text class="detail-label">支付金额：</text>
            <text class="detail-value amount">¥{{ orderData.payAmount }}</text>
          </view>
          
          <view class="detail-item">
            <text class="detail-label">有效期至：</text>
            <text class="detail-value">{{ formatDate(orderData.vipEndTime) }}</text>
          </view>
        </view>
      </view>
      
      <!-- VIP特权展示 -->
      <view class="privileges-section">
        <text class="privileges-title">您的专属特权</text>
        <view class="privileges-list">
          <view class="privilege-item" v-for="(privilege, index) in vipPrivileges" :key="index">
            <text class="privilege-icon">{{ privilege.icon }}</text>
            <text class="privilege-text">{{ privilege.text }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <button class="action-button secondary" @click="viewOrder">查看订单</button>
      <button class="action-button primary" @click="startExperience">立即体验</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      orderData: {},
      vipPrivileges: [
        { icon: '👁️', text: '无限制查看用户资料' },
        { icon: '💖', text: '每日超级喜欢次数增加' },
        { icon: '⭐', text: '专属VIP标识显示' },
        { icon: '🎯', text: '优先匹配推荐' },
        { icon: '👀', text: '查看谁喜欢了我' },
        { icon: '🔄', text: '无限制撤回操作' }
      ]
    }
  },
  
  onLoad(options) {
    // 获取订单数据
    if (options.orderData) {
      try {
        this.orderData = JSON.parse(decodeURIComponent(options.orderData))
        console.log('订单数据:', this.orderData)
      } catch (error) {
        console.error('解析订单数据失败:', error)
        // 如果解析失败，使用默认数据
        this.orderData = {
          orderNo: 'VIP' + Date.now(),
          payMethodName: '微信支付',
          payAmount: '19.90',
          vipEndTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
        }
      }
    }
  },
  
  methods: {
    // 获取VIP类型名称
    getVipTypeName() {
      // 根据结束时间推算VIP类型
      if (this.orderData.vipEndTime) {
        const endTime = new Date(this.orderData.vipEndTime)
        const startTime = new Date(this.orderData.vipStartTime || Date.now())
        const diffMonths = Math.round((endTime - startTime) / (30 * 24 * 60 * 60 * 1000))
        
        if (diffMonths <= 1) return '月度VIP'
        if (diffMonths <= 3) return '季度VIP'
        return '年度VIP'
      }
      return 'VIP会员'
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '永久有效'
      
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      
      return `${year}-${month}-${day}`
    },
    
    // 查看订单详情
    viewOrder() {
      if (this.orderData.orderId) {
        uni.navigateTo({
          url: `/pages/vip/order-detail?orderId=${this.orderData.orderId}`
        })
      } else {
        uni.showToast({
          title: '订单信息不完整',
          icon: 'none'
        })
      }
    },
    
    // 开始体验VIP功能
    startExperience() {
      // 返回首页或者跳转到主要功能页面
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style scoped>
.success-page {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 40rpx 16rpx 120rpx;
  box-sizing: border-box;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-crown, .floating-star {
  position: absolute;
  font-size: 40rpx;
  opacity: 0.3;
  animation: float 6s ease-in-out infinite;
}

.crown-1 {
  top: 15%;
  left: 10%;
  animation-delay: 0s;
}

.crown-2 {
  top: 25%;
  right: 15%;
  animation-delay: 2s;
}

.crown-3 {
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
}

.star-1 {
  top: 40%;
  right: 25%;
  animation-delay: 1s;
}

.star-2 {
  bottom: 20%;
  right: 10%;
  animation-delay: 3s;
}

.star-3 {
  top: 60%;
  left: 15%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20rpx) rotate(10deg);
  }
}

/* 成功内容 */
.success-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
}

.success-icon {
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(45deg, #4CAF50, #45a049);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
  box-shadow: 0 8rpx 25rpx rgba(76, 175, 80, 0.4);
  animation: bounce 0.6s ease-out;
}

@keyframes bounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

.check-mark {
  color: white;
  font-size: 50rpx;
  font-weight: bold;
}

.success-title {
  font-size: 40rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 12rpx;
  display: block;
}

.success-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40rpx;
  display: block;
}

/* VIP信息卡片 */
.vip-info-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  padding: 30rpx;
  margin: 30rpx auto;
  width: 85%;
  max-width: 600rpx;
  box-sizing: border-box;
  box-shadow: 0 8rpx 25rpx rgba(0, 0, 0, 0.1);
}

.vip-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.vip-badge {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: white;
  padding: 8rpx 18rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: bold;
  margin-right: 12rpx;
}

.vip-type {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.vip-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  min-height: 60rpx;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 30rpx;
  color: #666;
  flex: 1;
}

.detail-value {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  flex: 1;
  text-align: right;
}

.detail-value.amount {
  color: #FF6B6B;
  font-weight: bold;
  font-size: 32rpx;
}

/* VIP特权展示 */
.privileges-section {
  width: 100%;
  box-sizing: border-box;
}

.privileges-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 30rpx;
  display: block;
  text-align: center;
}

.privileges-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  width: 85%;
  max-width: 600rpx;
  margin: 0 auto;
  box-sizing: border-box;
}

.privilege-item {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8rpx;
  min-height: 60rpx;
}

.privilege-icon {
  font-size: 28rpx;
  margin-right: 16rpx;
  width: 36rpx;
  text-align: center;
  flex-shrink: 0;
}

.privilege-text {
  font-size: 24rpx;
  color: #333;
  flex: 1;
  line-height: 1.4;
}

/* 底部按钮 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  padding: 20rpx;
  display: flex;
  gap: 16rpx;
  box-sizing: border-box;
  width: 100vw;
}

.action-button {
  flex: 1;
  height: 88rpx;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-button.secondary {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border: 2rpx solid #667eea;
}

.action-button.primary {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: white;
  box-shadow: 0 8rpx 25rpx rgba(255, 215, 0, 0.4);
}

.action-button:active {
  transform: translateY(2rpx);
}

/* 响应式设计 */
@media screen and (max-width: 375px) {
  .success-page {
    padding: 30rpx 12rpx 120rpx;
  }

  .vip-info-card {
    padding: 24rpx;
    margin: 24rpx 0;
  }

  .privileges-list {
    padding: 24rpx;
    gap: 20rpx;
  }

  .privilege-item {
    padding: 12rpx;
    min-height: 70rpx;
  }

  .privilege-text {
    font-size: 22rpx;
  }
}

@media screen and (min-width: 768px) {
  .success-page {
    padding: 60rpx 40rpx 120rpx;
  }

  .success-content {
    max-width: 600rpx;
    margin: 0 auto;
  }

  .vip-info-card {
    padding: 40rpx;
    margin: 40rpx 0;
  }

  .privileges-list {
    grid-template-columns: 1fr 1fr 1fr;
    padding: 40rpx;
    gap: 30rpx;
  }

  .detail-label, .detail-value {
    font-size: 32rpx;
  }

  .privilege-text {
    font-size: 26rpx;
  }
}

/* 单行显示优化 */
@media screen and (max-width: 320px) {
  .privileges-list {
    grid-template-columns: 1fr;
  }

  .privilege-item {
    justify-content: flex-start;
  }
}

.action-button.primary:active {
  box-shadow: 0 4rpx 15rpx rgba(255, 215, 0, 0.3);
}
</style>
