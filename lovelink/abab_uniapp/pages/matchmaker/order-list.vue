<template>
  <view class="order-list-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">我的红娘订单</view>
      <view class="nav-right"></view>
    </view>

    <!-- 状态筛选 -->
    <view class="status-filter">
      <view
        class="filter-item"
        :class="{ active: selectedStatus === null }"
        @click="filterByStatus(null)"
      >
        全部
      </view>
      <view
        class="filter-item"
        :class="{ active: selectedStatus === 0 }"
        @click="filterByStatus(0)"
      >
        待支付
      </view>
      <view
        class="filter-item"
        :class="{ active: selectedStatus === 1 }"
        @click="filterByStatus(1)"
      >
        已支付
      </view>
      <view
        class="filter-item"
        :class="{ active: selectedStatus === 4 }"
        @click="filterByStatus(4)"
      >
        已完成
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-container">
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 订单列表 -->
      <view v-else-if="orderList.length > 0">
        <view 
          v-for="order in orderList" 
          :key="order.orderId"
          class="order-card"
          @click="viewOrderDetail(order.orderId)"
        >
          <!-- 订单头部 -->
          <view class="order-header">
            <view class="order-info">
              <text class="order-no">订单号：{{ order.orderNo }}</text>
              <text class="order-time">{{ formatTime(order.createdAt) }}</text>
            </view>
            <view class="order-status" :style="{ color: getStatusColor(order.orderStatus) }">
              {{ order.orderStatusText }}
            </view>
          </view>

          <!-- 服务信息 -->
          <view class="service-info">
            <view class="service-icon">👩‍💼</view>
            <view class="service-details">
              <text class="service-type">{{ order.serviceTypeText }}</text>
              <text class="service-desc">{{ order.serviceDesc }}</text>
              <text class="matchmaker-info" v-if="order.matchmakerNickname">
                红娘：{{ order.matchmakerNickname }}
              </text>
            </view>
            <view class="service-amount">
              <text class="amount-label">￥</text>
              <text class="amount-value">{{ order.payAmount }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="order-actions" v-if="order.orderStatus === 0 || order.orderStatus === 1">
            <button
              v-if="order.orderStatus === 0"
              class="action-btn cancel-btn"
              @click.stop="cancelOrder(order.orderId)"
            >
              取消订单
            </button>
            <button
              v-if="order.orderStatus === 0"
              class="action-btn pay-btn"
              @click.stop="payOrder(order)"
            >
              立即支付
            </button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <view class="empty-icon">📋</view>
        <text class="empty-text">暂无订单记录</text>
        <text class="empty-desc">您还没有红娘服务订单</text>
      </view>
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
  getMyMatchmakerOrders, 
  cancelMatchmakerOrder, 
  payMatchmakerOrder,
  getOrderStatusColor 
} from '@/api/matchmaker'

export default {
  data() {
    return {
      orderList: [],
      isLoading: false,
      selectedStatus: null,
      showPayModal: false,
      currentPayOrder: null,
      pageNum: 1,
      pageSize: 10,
      hasMore: true
    }
  },
  
  onLoad(options) {
    // 如果从URL参数中传入了状态，设置默认筛选状态
    if (options.status !== undefined) {
      this.selectedStatus = parseInt(options.status)
    }

    this.loadOrderList()
  },

  onShow() {
    // 页面显示时刷新列表，以便显示最新的订单
    // 只有在不是首次加载时才刷新（避免与onLoad重复）
    if (this.orderList.length > 0) {
      this.refreshOrderList()
    }
  },
  
  onReachBottom() {
    if (this.hasMore && !this.isLoading) {
      this.loadMore()
    }
  },
  
  onPullDownRefresh() {
    this.refreshOrderList()
  },
  
  methods: {
    // 加载订单列表
    async loadOrderList(isRefresh = false) {
      // 防止重复加载
      if (this.isLoading) {
        console.log('正在加载中，跳过重复请求')
        return
      }

      if (isRefresh) {
        this.pageNum = 1
        this.hasMore = true
        this.orderList = [] // 刷新时清空列表
      }

      this.isLoading = true
      console.log(`加载订单列表 - 页码: ${this.pageNum}, 刷新: ${isRefresh}, 状态筛选: ${this.selectedStatus}`)

      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }

        if (this.selectedStatus !== null) {
          params.orderStatus = this.selectedStatus
        }

        const result = await getMyMatchmakerOrders(params)
        console.log('API返回结果:', result)

        if (result.code === 200) {
          const newOrders = result.data.records || []
          console.log(`获取到 ${newOrders.length} 个订单`)

          // 去重处理：根据订单ID去重
          const existingOrderIds = new Set(this.orderList.map(order => order.orderId))
          const uniqueNewOrders = newOrders.filter(order => !existingOrderIds.has(order.orderId))

          console.log(`去重后剩余 ${uniqueNewOrders.length} 个新订单`)

          if (isRefresh) {
            this.orderList = newOrders
          } else {
            this.orderList = [...this.orderList, ...uniqueNewOrders]
          }

          this.hasMore = newOrders.length === this.pageSize
          console.log(`当前列表总数: ${this.orderList.length}, 还有更多: ${this.hasMore}`)
        } else {
          console.error('API返回错误:', result.message)
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载订单列表失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
        if (isRefresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    
    // 加载更多
    loadMore() {
      this.pageNum++
      this.loadOrderList()
    },
    
    // 刷新列表
    refreshOrderList() {
      this.loadOrderList(true)
    },
    
    // 按状态筛选
    filterByStatus(status) {
      console.log(`筛选状态变更: ${this.selectedStatus} -> ${status}`)
      this.selectedStatus = status
      this.pageNum = 1
      this.hasMore = true
      this.loadOrderList(true) // 使用刷新模式
    },
    
    // 查看订单详情
    viewOrderDetail(orderId) {
      uni.navigateTo({
        url: `/pages/matchmaker/order-detail?orderId=${orderId}`
      })
    },
    
    // 取消订单
    async cancelOrder(orderId) {
      const confirmResult = await this.showConfirmDialog('确定要取消这个订单吗？')
      if (!confirmResult) return
      
      try {
        const result = await cancelMatchmakerOrder(orderId)
        
        if (result.code === 200) {
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
          this.refreshOrderList()
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
    payOrder(order) {
      console.log('准备支付订单:', order)

      if (!order || !order.orderId) {
        console.error('订单信息无效:', order)
        uni.showToast({
          title: '订单信息错误',
          icon: 'error'
        })
        return
      }

      this.currentPayOrder = order
      this.showPayModal = true
    },
    
    // 确认支付
    async confirmPay(payType) {
      if (!this.currentPayOrder) {
        console.error('当前支付订单为空')
        uni.showToast({
          title: '支付信息错误，请重试',
          icon: 'error'
        })
        return
      }

      // 保存订单信息，避免在关闭弹窗后丢失
      const orderToPayId = this.currentPayOrder.orderId
      const orderToPayNo = this.currentPayOrder.orderNo

      // 关闭支付弹窗
      this.closePayModal()

      try {
        uni.showLoading({ title: '支付中...' })

        const result = await payMatchmakerOrder(orderToPayId, payType)

        uni.hideLoading()

        if (result.code === 200) {
          uni.showToast({
            title: '支付成功',
            icon: 'success'
          })
          this.refreshOrderList()
        } else {
          uni.showToast({
            title: result.message || '支付失败',
            icon: 'error'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error(`支付订单 ${orderToPayNo} 失败:`, error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },
    
    // 关闭支付弹窗
    closePayModal() {
      this.showPayModal = false
      this.currentPayOrder = null
    },
    
    // 获取状态颜色
    getStatusColor(status) {
      return getOrderStatusColor(status)
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      
      const time = new Date(timeStr)
      const now = new Date()
      const diff = now - time
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return time.toLocaleDateString()
      }
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
/* 基础样式重置 */
* {
  box-sizing: border-box;
}

.order-list-page {
  width: 100vw;
  min-height: 100vh;
  background: #f5f5f5;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

/* 导航栏 */
.navbar {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
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

/* 状态筛选 */
.status-filter {
  width: 100%;
  display: flex;
  background: white;
  padding: 20rpx 15rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
}

.filter-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 8rpx;
  font-size: 26rpx;
  color: #666;
  border-radius: 12rpx;
  margin: 0 6rpx;
  transition: all 0.3s ease;
  border: 2rpx solid transparent;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.filter-item.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
  font-weight: bold;
}

/* 订单容器 */
.order-container {
  width: 100%;
  padding: 0 30rpx;
  box-sizing: border-box;
}

/* 订单卡片 */
.order-card {
  width: 100%;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.order-info {
  flex: 1;
}

.order-no {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.order-time {
  font-size: 24rpx;
  color: #999;
}

.order-status {
  font-size: 26rpx;
  font-weight: bold;
}

/* 服务信息 */
.service-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.service-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.service-details {
  flex: 1;
}

.service-type {
  display: block;
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.service-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.matchmaker-info {
  display: block;
  font-size: 24rpx;
  color: #667eea;
}

.service-amount {
  text-align: right;
}

.amount-label {
  font-size: 24rpx;
  color: #e74c3c;
}

.amount-value {
  font-size: 32rpx;
  color: #e74c3c;
  font-weight: bold;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.action-btn {
  padding: 16rpx 32rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  border: none;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
}

.pay-btn {
  background: #667eea;
  color: white;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  text-align: center;
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
