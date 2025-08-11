<template>
  <view class="date-notifications-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会通知</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <!-- 快速统计 -->
    <view class="quick-stats">
      <view class="stat-item">
        <text class="stat-number">{{ pendingCount }}</text>
        <text class="stat-label">待确认</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ confirmedCount }}</text>
        <text class="stat-label">已确认</text>
      </view>
      <view class="stat-item">
        <text class="stat-number">{{ totalCount }}</text>
        <text class="stat-label">总约会</text>
      </view>
    </view>

    <!-- 约会列表 -->
    <scroll-view scroll-y class="notifications-list" v-if="dateArrangements.length > 0">
      <view
        v-for="arrangement in dateArrangements"
        :key="arrangement.arrangementId"
        class="notification-card">

        <view class="notification-header">
          <view class="notification-icon">
            {{ getStatusIcon(arrangement.arrangementStatus) }}
          </view>
          <view class="notification-info">
            <text class="notification-title">{{ getStatusText(arrangement.arrangementStatus) }}</text>
            <text class="notification-time">{{ formatTime(arrangement.createdAt) }}</text>
          </view>
        </view>

        <view class="notification-content">
          <text class="notification-message">{{ getStatusMessage(arrangement.arrangementStatus) }}</text>

          <!-- 约会详情 -->
          <view class="date-details">
            <view class="detail-item">
              <text class="detail-label">约会时间：</text>
              <text class="detail-value">{{ formatDateTime(arrangement.dateTime) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">约会地点：</text>
              <text class="detail-value">{{ arrangement.dateLocation }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">约会类型：</text>
              <text class="detail-value">{{ getDateTypeName(arrangement.dateType) }}</text>
            </view>
          </view>

          <!-- 确认按钮 -->
          <view v-if="arrangement.arrangementStatus === 0 && canConfirm(arrangement)" class="confirm-buttons">
            <button class="confirm-btn reject-btn" @tap="showRejectModal(arrangement)">
              拒绝
            </button>
            <button class="confirm-btn accept-btn" @tap="confirmArrangement(arrangement, 1)">
              确认参加
            </button>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon">📭</view>
      <text class="empty-text">暂无约会安排</text>
    </view>
  </view>
</template>

<script>
import wsManager from '@/utils/websocket'
import config from '@/api/config'

export default {
  data() {
    return {
      notifications: [],
      dateArrangements: [],
      currentUserId: null,
      pendingCount: 0,
      confirmedCount: 0,
      totalCount: 0,
      dateTypeNames: {
        1: '咖啡厅',
        2: '餐厅',
        3: '电影院',
        4: '其他'
      }
    }
  },

  onLoad() {
    this.currentUserId = uni.getStorageSync('userId')
    this.loadNotifications()
    this.loadDateArrangements()
    this.loadDateStatistics()
    this.initWebSocketListeners()
  },

  onUnload() {
    // 移除WebSocket监听器
    wsManager.offMessage('DATE_ARRANGEMENT')
    wsManager.offMessage('DATE_CONFIRMATION')
    wsManager.offMessage('DATE_FINALIZED')
    wsManager.offMessage('DATE_CANCELLED')
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 加载通知列表
    loadNotifications() {
      // 从本地存储加载通知
      const savedNotifications = uni.getStorageSync('dateNotifications') || []
      this.notifications = savedNotifications.sort((a, b) => b.timestamp - a.timestamp)
    },

    // 加载约会安排列表
    async loadDateArrangements() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          console.log('未登录，无法加载约会安排')
          return
        }

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/my-arrangements`,
          method: 'GET',
          header: {
            'token': token
          }
        })

        if (response.data.code === 200) {
          this.dateArrangements = response.data.data || []
          // 按创建时间倒序排列
          this.dateArrangements.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          console.log('加载约会安排成功:', this.dateArrangements.length)
        } else {
          console.error('加载约会安排失败:', response.data.message)
        }
      } catch (error) {
        console.error('加载约会安排失败:', error)
      }
    },

    // 初始化WebSocket监听器
    initWebSocketListeners() {
      // 监听约会安排通知
      wsManager.onMessage('DATE_ARRANGEMENT', (message) => {
        this.handleDateArrangementNotification(message)
      })

      // 监听约会确认结果通知
      wsManager.onMessage('DATE_CONFIRMATION', (message) => {
        this.handleDateConfirmationNotification(message)
      })

      // 监听约会最终确定通知
      wsManager.onMessage('DATE_FINALIZED', (message) => {
        this.handleDateFinalizedNotification(message)
      })

      // 监听约会取消通知
      wsManager.onMessage('DATE_CANCELLED', (message) => {
        this.handleDateCancelledNotification(message)
      })
    },

    // 处理约会安排通知
    handleDateArrangementNotification(message) {
      const notification = {
        id: Date.now(),
        type: 'DATE_ARRANGEMENT',
        timestamp: message.timestamp,
        data: message.data,
        read: false,
        confirmed: false
      }
      
      this.notifications.unshift(notification)
      this.saveNotifications()
      
      // 显示系统通知
      uni.showToast({
        title: '收到新的约会安排',
        icon: 'none'
      })
    },

    // 处理约会确认结果通知
    handleDateConfirmationNotification(message) {
      const notification = {
        id: Date.now(),
        type: 'DATE_CONFIRMATION',
        timestamp: message.timestamp,
        data: message.data,
        read: false
      }
      
      this.notifications.unshift(notification)
      this.saveNotifications()
    },

    // 处理约会最终确定通知
    handleDateFinalizedNotification(message) {
      const notification = {
        id: Date.now(),
        type: 'DATE_FINALIZED',
        timestamp: message.timestamp,
        data: message.data,
        read: false
      }
      
      this.notifications.unshift(notification)
      this.saveNotifications()
      
      // 显示庆祝通知
      uni.showToast({
        title: '约会确定成功！',
        icon: 'success'
      })
    },

    // 处理约会取消通知
    handleDateCancelledNotification(message) {
      const notification = {
        id: Date.now(),
        type: 'DATE_CANCELLED',
        timestamp: message.timestamp,
        data: message.data,
        read: false
      }
      
      this.notifications.unshift(notification)
      this.saveNotifications()
    },



    // 保存通知到本地存储
    saveNotifications() {
      uni.setStorageSync('dateNotifications', this.notifications)
    },

    // 加载约会统计数据
    async loadDateStatistics() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) return

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/statistics`,
          method: 'GET',
          header: {
            'token': token
          }
        })

        if (response.data.code === 200) {
          const stats = response.data.data
          this.pendingCount = stats.pendingCount || 0
          this.confirmedCount = stats.confirmedCount || 0
          this.totalCount = stats.totalCount || 0
        }
      } catch (error) {
        console.error('加载约会统计失败:', error)
      }
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) { // 1天内
        return `${Math.floor(diff / 3600000)}小时前`
      } else {
        return `${date.getMonth() + 1}月${date.getDate()}日`
      }
    },

    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      const date = new Date(dateTimeStr)
      return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
    },

    // 获取约会类型名称
    getDateTypeName(type) {
      return this.dateTypeNames[type] || '其他'
    },

    // 判断当前用户是否可以确认约会
    canConfirm(arrangement) {
      if (!this.currentUserId) return false

      const isUserA = arrangement.userId === this.currentUserId
      const isUserB = arrangement.targetUserId === this.currentUserId

      if (isUserA && arrangement.userAConfirm === 0) return true
      if (isUserB && arrangement.userBConfirm === 0) return true

      return false
    },

    // 确认约会安排
    async confirmArrangement(arrangement, confirm) {
      try {
        uni.showLoading({ title: '处理中...' })

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/confirm/${arrangement.arrangementId}?confirm=${confirm}`,
          method: 'POST',
          header: {
            'token': uni.getStorageSync('token')
          }
        })

        if (response.data.code === 200) {
          uni.showToast({
            title: confirm === 1 ? '确认成功' : '已拒绝',
            icon: 'success'
          })

          // 刷新数据
          this.loadDateArrangements()
          this.loadDateStatistics()
        } else {
          uni.showToast({
            title: response.data.message || '操作失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('确认约会失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 0: return 'pending'
        case 1: return 'confirmed'
        case 2: return 'cancelled'
        case 3: return 'completed'
        default: return ''
      }
    },

    // 获取状态图标
    getStatusIcon(status) {
      switch (status) {
        case 0: return '⏳'
        case 1: return '✅'
        case 2: return '❌'
        case 3: return '🎉'
        default: return '❓'
      }
    },

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 0: return '约会安排通知'
        case 1: return '约会已确认'
        case 2: return '约会已取消'
        case 3: return '约会已完成'
        default: return '约会通知'
      }
    },

    // 获取状态消息
    getStatusMessage(status) {
      switch (status) {
        case 0: return '红娘为您安排了一场约会，请确认是否参加'
        case 1: return '双方已确认参加，约会正式确定'
        case 2: return '约会已取消'
        case 3: return '约会已完成，可以给出评价反馈'
        default: return ''
      }
    },

    // 开始聊天
    startChat(arrangement) {
      const chatUserId = arrangement.userId === this.currentUserId
        ? arrangement.targetUserId
        : arrangement.userId

      uni.navigateTo({
        url: `/pages/message/chat?userId=${chatUserId}`
      })
    },

    // 给出反馈
    giveFeedback(arrangement) {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${arrangement.arrangementId}`
      })
    },

    // 查看约会详情
    viewDateDetails(dateInfo) {
      // 跳转到约会详情页面
      uni.navigateTo({
        url: `/pages/date/date-detail?arrangementId=${dateInfo.arrangementId}`
      })
    },

    // 开始聊天
    startChat(dateInfo) {
      // 确定聊天对象
      const currentUserId = uni.getStorageSync('userId')
      const chatUserId = dateInfo.userId === currentUserId ? dateInfo.targetUserId : dateInfo.userId
      
      uni.navigateTo({
        url: `/pages/message/chat?userId=${chatUserId}`
      })
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.date-notifications-container {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
}

.nav-left, .nav-right {
  width: 80rpx;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
}

/* 快速统计 */
.quick-stats {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 8rpx;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.stat-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 通知列表 */
.notifications-list {
  height: calc(100vh - 240rpx);
  padding: 0 20rpx 20rpx;
}

/* 通知卡片 */
.notification-card {
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.notification-card.pending {
  border-left: 8rpx solid #ff9800;
}

.notification-card.confirmed {
  border-left: 8rpx solid #4caf50;
}

.notification-card.cancelled {
  border-left: 8rpx solid #f44336;
}

.notification-card.completed {
  border-left: 8rpx solid #9c27b0;
}

/* 通知头部 */
.notification-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.notification-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon.pending {
  background: rgba(255, 152, 0, 0.1);
}

.notification-icon.confirmed {
  background: rgba(76, 175, 80, 0.1);
}

.notification-icon.cancelled {
  background: rgba(244, 67, 54, 0.1);
}

.notification-icon.completed {
  background: rgba(156, 39, 176, 0.1);
}

.notification-info {
  flex: 1;
}

.notification-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
  display: block;
  margin-bottom: 8rpx;
}

.notification-time {
  font-size: 24rpx;
  color: #a0aec0;
}

/* 通知内容 */
.notification-content {
  padding: 30rpx;
}

.notification-message {
  font-size: 28rpx;
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 20rpx;
}

/* 约会详情 */
.date-details {
  background: #f7fafc;
  border-radius: 12rpx;
  padding: 20rpx;
  margin: 20rpx 0;
}

.detail-item {
  display: flex;
  margin-bottom: 12rpx;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 26rpx;
  color: #718096;
  width: 140rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 26rpx;
  color: #2d3748;
  flex: 1;
}

/* 确认按钮 */
.confirm-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.confirm-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.accept-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.reject-btn {
  background: #f7fafc;
  color: #718096;
  border: 2rpx solid #e2e8f0;
}

/* 已确认状态 */
.confirmed-status {
  margin-top: 20rpx;
  text-align: center;
}

.status-text {
  font-size: 28rpx;
  font-weight: bold;
  padding: 16rpx 32rpx;
  border-radius: 20rpx;
}

.status-text.accepted {
  background: rgba(72, 187, 120, 0.1);
  color: #38a169;
}

.status-text.rejected {
  background: rgba(245, 101, 101, 0.1);
  color: #e53e3e;
}

.status-text.completed {
  background: rgba(156, 39, 176, 0.1);
  color: #9c27b0;
}

/* 操作按钮 */
.action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 24rpx;
  font-weight: bold;
  padding: 16rpx 32rpx;
  border-radius: 20rpx;
  border: none;
  margin-top: 16rpx;
}

/* 取消原因 */
.cancel-reason {
  font-size: 24rpx;
  color: #e53e3e;
  margin-top: 8rpx;
  display: block;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.action-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  background: #f7fafc;
  color: #718096;
  border: 2rpx solid #e2e8f0;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
}
</style>
