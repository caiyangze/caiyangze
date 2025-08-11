<template>
  <view class="order-detail-page" style="width: 100%; max-width: none;">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="header-left">
        <view class="back-button" @click="goBack">
          <text class="back-icon">←</text>
        </view>
      </view>
      <text class="page-title">VIP订单详情</text>
      <view class="header-right"></view>
    </view>
    
    <!-- 订单状态 -->
    <view class="status-section">
      <view class="status-icon" :class="getStatusClass()">
        <text class="status-emoji">{{ getStatusIcon() }}</text>
      </view>
      <text class="status-text">{{ getStatusText() }}</text>
      <text class="status-desc">{{ getStatusDesc() }}</text>
    </view>
    
    <!-- 订单信息 -->
    <view class="order-info-section">
      <view class="section-title">
        <text class="title-text">订单信息</text>
      </view>
      
      <view class="info-card" style="width: 96%; margin: 0 auto;">
        <view class="info-item">
          <text class="info-label">订单号</text>
          <text class="info-value">{{ orderDetail.orderNo || '加载中...' }}</text>
        </view>
        
        <view class="info-item">
          <text class="info-label">VIP类型</text>
          <text class="info-value">{{ orderDetail.vipTypeName || '加载中...' }}</text>
        </view>
        
        <view class="info-item">
          <text class="info-label">订单金额</text>
          <text class="info-value">¥{{ orderDetail.amount || '0.00' }}</text>
        </view>
        
        <view class="info-item">
          <text class="info-label">实付金额</text>
          <text class="info-value amount">¥{{ orderDetail.payAmount || '0.00' }}</text>
        </view>
        
        <view class="info-item" v-if="orderDetail.discountAmount > 0">
          <text class="info-label">优惠金额</text>
          <text class="info-value discount">-¥{{ orderDetail.discountAmount || '0.00' }}</text>
        </view>
        
        <view class="info-item" v-if="orderDetail.payTypeName">
          <text class="info-label">支付方式</text>
          <text class="info-value">{{ orderDetail.payTypeName }}</text>
        </view>
        
        <view class="info-item" v-if="orderDetail.payTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ formatDate(orderDetail.payTime) }}</text>
        </view>
        
        <view class="info-item" v-if="orderDetail.transactionId">
          <text class="info-label">交易号</text>
          <text class="info-value transaction-id">{{ orderDetail.transactionId }}</text>
        </view>
      </view>
    </view>
    
    <!-- VIP有效期 -->
    <view class="validity-section" v-if="orderDetail.startTime && orderDetail.endTime">
      <view class="section-title">
        <text class="title-text">VIP有效期</text>
      </view>
      
      <view class="validity-card" style="width: 96%; margin: 0 auto;">
        <view class="validity-item">
          <text class="validity-label">开始时间</text>
          <text class="validity-value">{{ formatDate(orderDetail.startTime) }}</text>
        </view>
        
        <view class="validity-item">
          <text class="validity-label">结束时间</text>
          <text class="validity-value">{{ formatDate(orderDetail.endTime) }}</text>
        </view>
        
        <view class="validity-duration">
          <text class="duration-text">有效期：{{ getValidityDuration() }}</text>
        </view>
      </view>
    </view>
    
    <!-- 底部按钮 -->
    <view class="bottom-actions" v-if="orderDetail.orderStatus === 1">
      <button class="action-button primary" @click="goToVipCenter">查看VIP特权</button>
    </view>
  </view>
</template>

<script>
import { getVipOrderDetail } from '@/api/vip.js'

export default {
  data() {
    return {
      orderId: null,
      orderDetail: {},
      loading: true
    }
  },
  
  onLoad(options) {
    if (options.orderId) {
      this.orderId = options.orderId
      this.loadOrderDetail()
    } else {
      uni.showToast({
        title: '订单ID不能为空',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  
  methods: {
    // 加载订单详情
    async loadOrderDetail() {
      try {
        this.loading = true
        const response = await getVipOrderDetail(this.orderId)
        
        if (response.code === 200) {
          this.orderDetail = response.data
        } else {
          uni.showToast({
            title: response.message || '获取订单详情失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取订单详情失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 获取状态样式类
    getStatusClass() {
      switch (this.orderDetail.orderStatus) {
        case 0: return 'status-pending'
        case 1: return 'status-success'
        case 2: return 'status-cancelled'
        case 3: return 'status-refunded'
        default: return 'status-unknown'
      }
    },
    
    // 获取状态图标
    getStatusIcon() {
      switch (this.orderDetail.orderStatus) {
        case 0: return '⏳'
        case 1: return '✅'
        case 2: return '❌'
        case 3: return '↩️'
        default: return '❓'
      }
    },
    
    // 获取状态文本
    getStatusText() {
      switch (this.orderDetail.orderStatus) {
        case 0: return '待支付'
        case 1: return '支付成功'
        case 2: return '已取消'
        case 3: return '已退款'
        default: return '未知状态'
      }
    },
    
    // 获取状态描述
    getStatusDesc() {
      switch (this.orderDetail.orderStatus) {
        case 0: return '请尽快完成支付'
        case 1: return 'VIP会员已开通成功'
        case 2: return '订单已取消'
        case 3: return '订单已退款'
        default: return ''
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    // 获取有效期时长
    getValidityDuration() {
      if (!this.orderDetail.startTime || !this.orderDetail.endTime) return ''
      
      const start = new Date(this.orderDetail.startTime)
      const end = new Date(this.orderDetail.endTime)
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays >= 365) {
        return `${Math.floor(diffDays / 365)}年`
      } else if (diffDays >= 30) {
        return `${Math.floor(diffDays / 30)}个月`
      } else {
        return `${diffDays}天`
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack({
        delta: 1
      })
    },

    // 跳转到VIP中心
    goToVipCenter() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20rpx 0;
  width: 100% !important;
  max-width: none !important;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 0 20rpx;
  position: relative;
}

.header-left, .header-right {
  flex: 1;
}

.back-button {
  width: 60rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.back-button:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.back-icon {
  font-size: 32rpx;
  color: white;
  font-weight: bold;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  text-align: center;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

/* 订单状态 */
.status-section {
  text-align: center;
  padding: 40rpx 0;
}

.status-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20rpx;
}

.status-icon.status-pending {
  background: linear-gradient(45deg, #FFA726, #FF9800);
}

.status-icon.status-success {
  background: linear-gradient(45deg, #66BB6A, #4CAF50);
}

.status-icon.status-cancelled {
  background: linear-gradient(45deg, #EF5350, #F44336);
}

.status-icon.status-refunded {
  background: linear-gradient(45deg, #42A5F5, #2196F3);
}

.status-emoji {
  font-size: 40rpx;
}

.status-text {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 8rpx;
}

.status-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 信息卡片 */
.order-info-section, .validity-section {
  margin-bottom: 30rpx;
  padding: 0 10rpx;
}

.section-title {
  margin-bottom: 20rpx;
}

.title-text {
  font-size: 28rpx;
  font-weight: bold;
  color: white;
}

.info-card, .validity-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  padding: 30rpx;
  width: 100% !important;
  margin: 0;
  box-sizing: border-box;
}

.info-item, .validity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child, .validity-item:last-child {
  border-bottom: none;
}

.info-label, .validity-label {
  font-size: 28rpx;
  color: #666;
  flex: 1;
}

.info-value, .validity-value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  flex: 1;
  text-align: right;
}

.info-value.amount {
  color: #FF6B6B;
  font-weight: bold;
}

.info-value.discount {
  color: #4CAF50;
  font-weight: bold;
}

.info-value.transaction-id {
  font-size: 24rpx;
  font-family: monospace;
}

.validity-duration {
  text-align: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.duration-text {
  font-size: 26rpx;
  color: #666;
  font-weight: bold;
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
  box-sizing: border-box;
}

.action-button {
  width: 100%;
  height: 80rpx;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-button.primary {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: white;
  box-shadow: 0 8rpx 25rpx rgba(255, 215, 0, 0.4);
}

.action-button:active {
  transform: translateY(2rpx);
}
</style>
