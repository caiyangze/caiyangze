<template>
  <view class="order-detail-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">订单详情</view>
      <view class="nav-right"></view>
    </view>

    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 订单详情 -->
    <view v-else-if="orderDetail" class="detail-container">
      <!-- 订单状态 -->
      <view class="status-card">
        <view class="status-icon" :style="{ color: getStatusColor(orderDetail.orderStatus) }">
          {{ getStatusIcon(orderDetail.orderStatus) }}
        </view>
        <view class="status-info">
          <text class="status-text" :style="{ color: getStatusColor(orderDetail.orderStatus) }">
            {{ orderDetail.orderStatusText }}
          </text>
          <text class="status-desc">{{ getStatusDesc(orderDetail.orderStatus) }}</text>
        </view>
      </view>

      <!-- 服务信息 -->
      <view class="info-card">
        <view class="card-title">服务信息</view>
        <view class="info-item">
          <text class="info-label">服务类型</text>
          <text class="info-value">{{ orderDetail.serviceTypeText }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">服务描述</text>
          <text class="info-value">{{ orderDetail.serviceDesc || '暂无描述' }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.matchmakerNickname">
          <text class="info-label">指定红娘</text>
          <text class="info-value">{{ orderDetail.matchmakerNickname }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.serviceStartTime">
          <text class="info-label">服务开始</text>
          <text class="info-value">{{ formatDateTime(orderDetail.serviceStartTime) }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.serviceEndTime">
          <text class="info-label">服务结束</text>
          <text class="info-value">{{ formatDateTime(orderDetail.serviceEndTime) }}</text>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="info-card">
        <view class="card-title">订单信息</view>
        <view class="info-item">
          <text class="info-label">订单编号</text>
          <text class="info-value">{{ orderDetail.orderNo }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">创建时间</text>
          <text class="info-value">{{ formatDateTime(orderDetail.createdAt) }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">订单金额</text>
          <text class="info-value amount">￥{{ orderDetail.amount }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.discountAmount && orderDetail.discountAmount > 0">
          <text class="info-label">优惠金额</text>
          <text class="info-value discount">-￥{{ orderDetail.discountAmount }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">实付金额</text>
          <text class="info-value pay-amount">￥{{ orderDetail.payAmount }}</text>
        </view>
      </view>

      <!-- 支付信息 -->
      <view class="info-card" v-if="orderDetail.orderStatus >= 1">
        <view class="card-title">支付信息</view>
        <view class="info-item">
          <text class="info-label">支付方式</text>
          <text class="info-value">{{ orderDetail.payTypeText }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.payTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ formatDateTime(orderDetail.payTime) }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.transactionId">
          <text class="info-label">交易单号</text>
          <text class="info-value">{{ orderDetail.transactionId }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button
          v-if="orderDetail.orderStatus === 0"
          class="action-btn cancel-btn"
          @click="cancelOrder"
        >
          取消订单
        </button>
        <button
          v-if="orderDetail.orderStatus === 0"
          class="action-btn pay-btn"
          @click="payOrder"
        >
          {{ getPayButtonText() }}
        </button>
        <button
          v-if="orderDetail.orderStatus === 1"
          class="action-btn refund-btn"
          @click="requestRefund"
        >
          申请退款
        </button>
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <view class="error-icon">❌</view>
      <text class="error-text">订单不存在或已被删除</text>
      <button class="retry-btn" @click="loadOrderDetail">重新加载</button>
    </view>

    <!-- 支付方式选择弹窗 -->
    <view class="pay-modal" v-if="showPayModal" @click="closePayModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择支付方式</text>
          <text class="modal-close" @click="closePayModal">×</text>
        </view>
        
        <view class="pay-methods">
          <view class="pay-method" @click="confirmPay(3)">
            <view class="method-icon">💰</view>
            <view class="method-info">
              <text class="method-name">虚拟币支付</text>
              <text class="method-desc">使用账户虚拟币余额支付</text>
            </view>
            <view class="method-arrow">></view>
          </view>
          
          <view class="pay-method disabled">
            <view class="method-icon">💳</view>
            <view class="method-info">
              <text class="method-name">微信支付</text>
              <text class="method-desc">暂未开放</text>
            </view>
          </view>
          
          <view class="pay-method disabled">
            <view class="method-icon">🅰️</view>
            <view class="method-info">
              <text class="method-name">支付宝</text>
              <text class="method-desc">暂未开放</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import {
  getMatchmakerOrderDetail,
  cancelMatchmakerOrder,
  payMatchmakerOrder,
  getOrderStatusColor
} from '@/api/matchmaker'
import { pageAuthCheck } from '@/utils/auth-check'

export default {
  data() {
    return {
      orderId: null,
      orderDetail: null,
      isLoading: false,
      showPayModal: false,
      presetPayType: null // 预设的支付方式
    }
  },
  
  onLoad(options) {
    // 使用统一的登录检查
    if (!pageAuthCheck('订单详情页面')) {
      return
    }

    if (options.orderId) {
      this.orderId = options.orderId
      console.log('订单详情页面 - OrderId:', this.orderId)

      // 接收预设的支付方式
      if (options.payType) {
        this.presetPayType = parseInt(options.payType)
        console.log('接收到预设支付方式:', this.presetPayType)
      }
      this.loadOrderDetail()
    } else {
      uni.showToast({
        title: '订单ID不能为空',
        icon: 'error'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  
  methods: {
    // 加载订单详情
    async loadOrderDetail() {
      this.isLoading = true
      try {
        const result = await getMatchmakerOrderDetail(this.orderId)
        
        if (result.code === 200) {
          this.orderDetail = result.data
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载订单详情失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
      }
    },
    
    // 取消订单
    async cancelOrder() {
      const confirmResult = await this.showConfirmDialog('确定要取消这个订单吗？取消后无法恢复。')
      if (!confirmResult) return
      
      try {
        const result = await cancelMatchmakerOrder(this.orderId)
        
        if (result.code === 200) {
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          this.loadOrderDetail() // 重新加载订单详情
        } else {
          uni.showToast({
            title: result.message || '取消失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },
    
    // 支付订单
    payOrder() {
      // 如果有预设的支付方式，直接支付
      if (this.presetPayType) {
        console.log('使用预设支付方式直接支付:', this.presetPayType)
        this.confirmPay(this.presetPayType)
      } else {
        // 否则显示支付方式选择弹窗
        this.showPayModal = true
      }
    },
    
    // 确认支付
    async confirmPay(payType) {
      this.closePayModal()
      
      try {
        uni.showLoading({ title: '支付中...' })
        
        const result = await payMatchmakerOrder(this.orderId, payType)
        
        uni.hideLoading()
        
        if (result.code === 200) {
          uni.showToast({
            title: '支付成功',
            icon: 'success'
          })
          this.loadOrderDetail() // 重新加载订单详情
        } else {
          uni.showToast({
            title: result.message || '支付失败',
            icon: 'error'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('支付失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },
    
    // 申请退款
    async requestRefund() {
      const confirmResult = await this.showConfirmDialog('确定要申请退款吗？退款申请提交后将由客服处理。')
      if (!confirmResult) return
      
      uni.showToast({
        title: '退款功能开发中',
        icon: 'none'
      })
    },
    
    // 关闭支付弹窗
    closePayModal() {
      this.showPayModal = false
    },

    // 获取支付按钮文本
    getPayButtonText() {
      if (this.presetPayType) {
        const payTypeMap = {
          1: '微信支付',
          2: '支付宝支付',
          3: '虚拟币支付',
          4: '其他支付'
        }
        return `${payTypeMap[this.presetPayType] || '立即支付'}`
      }
      return '选择支付方式'
    },
    
    // 获取状态颜色
    getStatusColor(status) {
      return getOrderStatusColor(status)
    },
    
    // 获取状态图标
    getStatusIcon(status) {
      const iconMap = {
        0: '⏰', // 待支付
        1: '✅', // 已支付
        2: '❌', // 已取消
        3: '💰', // 已退款
        4: '🎉'  // 已完成
      }
      return iconMap[status] || '❓'
    },
    
    // 获取状态描述
    getStatusDesc(status) {
      const descMap = {
        0: '请尽快完成支付，超时订单将自动取消',
        1: '支付成功，红娘将尽快为您提供服务',
        2: '订单已取消，如有疑问请联系客服',
        3: '退款已处理，请注意查收',
        4: '服务已完成，感谢您的使用'
      }
      return descMap[status] || '未知状态'
    },
    
    // 格式化日期时间
    formatDateTime(dateStr) {
      if (!dateStr) return ''
      
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },
    
    // 显示确认对话框
    showConfirmDialog(content) {
      return new Promise((resolve) => {
        uni.showModal({
          title: '确认操作',
          content: content,
          confirmText: '确定',
          cancelText: '取消',
          success: (res) => {
            resolve(res.confirm)
          },
          fail: () => {
            resolve(false)
          }
        })
      })
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.nav-left, .nav-right {
  width: 80rpx;
}

.nav-icon {
  font-size: 36rpx;
  color: #333;
  font-weight: bold;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

/* 详情容器 */
.detail-container {
  padding: 30rpx;
}

/* 状态卡片 */
.status-card {
  background: white;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.status-icon {
  font-size: 60rpx;
  margin-right: 30rpx;
}

.status-info {
  flex: 1;
}

.status-text {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.status-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 25rpx;
  padding-bottom: 15rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f8f9fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
  width: 160rpx;
}

.info-value {
  font-size: 28rpx;
  color: #333;
  text-align: right;
  flex: 1;
  word-break: break-all;
}

.info-value.amount {
  color: #e74c3c;
  font-weight: bold;
}

.info-value.discount {
  color: #27ae60;
}

.info-value.pay-amount {
  color: #e74c3c;
  font-size: 32rpx;
  font-weight: bold;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
}

.pay-btn {
  background: #667eea;
  color: white;
}

.refund-btn {
  background: #e74c3c;
  color: white;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

/* 错误状态 */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.error-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.error-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 30rpx;
}

.retry-btn {
  background: #667eea;
  color: white;
  padding: 20rpx 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

/* 支付弹窗 */
.pay-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  border-radius: 24rpx;
  width: 85%;
  max-height: 70vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 2rpx solid #f8f9fa;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  font-weight: bold;
}

.pay-methods {
  padding: 20rpx;
}

.pay-method {
  display: flex;
  align-items: center;
  padding: 30rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
  transition: all 0.3s ease;
}

.pay-method:not(.disabled):active {
  background: #f8f9fa;
}

.pay-method.disabled {
  opacity: 0.5;
}

.method-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.method-info {
  flex: 1;
}

.method-name {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.method-desc {
  font-size: 24rpx;
  color: #666;
}

.method-arrow {
  font-size: 28rpx;
  color: #ccc;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
