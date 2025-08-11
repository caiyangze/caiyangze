<template>
  <view class="reminders-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">约会提醒</text>
      <view class="nav-right">
        <text class="nav-action" @tap="markAllRead">全部已读</text>
      </view>
    </view>

    <!-- 提醒列表 -->
    <scroll-view class="reminders-list" scroll-y>
      <!-- 今日提醒 -->
      <view v-if="todayReminders.length > 0" class="reminder-section">
        <view class="section-header">
          <text class="section-title">今日提醒</text>
          <view class="section-badge">{{ todayReminders.length }}</view>
        </view>
        
        <view 
          v-for="reminder in todayReminders" 
          :key="reminder.id"
          class="reminder-item today"
          :class="{ unread: !reminder.isRead }"
          @tap="handleReminderTap(reminder)">
          
          <view class="reminder-icon">
            <text class="icon-text">{{ getReminderIcon(reminder.type) }}</text>
          </view>
          
          <view class="reminder-content">
            <view class="reminder-header">
              <text class="reminder-title">{{ reminder.title }}</text>
              <text class="reminder-time">{{ formatTime(reminder.time) }}</text>
            </view>
            <text class="reminder-message">{{ reminder.message }}</text>
            <view class="reminder-tags">
              <text class="tag" :class="getTagClass(reminder.type)">
                {{ getTypeText(reminder.type) }}
              </text>
              <text class="tag urgent" v-if="reminder.isUrgent">紧急</text>
            </view>
          </view>
          
          <view class="reminder-actions">
            <view class="action-btn primary" @tap.stop="quickAction(reminder)">
              {{ getActionText(reminder.type) }}
            </view>
          </view>
        </view>
      </view>

      <!-- 即将到来 -->
      <view v-if="upcomingReminders.length > 0" class="reminder-section">
        <view class="section-header">
          <text class="section-title">即将到来</text>
          <view class="section-badge">{{ upcomingReminders.length }}</view>
        </view>
        
        <view 
          v-for="reminder in upcomingReminders" 
          :key="reminder.id"
          class="reminder-item upcoming"
          :class="{ unread: !reminder.isRead }"
          @tap="handleReminderTap(reminder)">
          
          <view class="reminder-icon">
            <text class="icon-text">{{ getReminderIcon(reminder.type) }}</text>
          </view>
          
          <view class="reminder-content">
            <view class="reminder-header">
              <text class="reminder-title">{{ reminder.title }}</text>
              <text class="reminder-date">{{ formatDate(reminder.dateTime) }}</text>
            </view>
            <text class="reminder-message">{{ reminder.message }}</text>
            <view class="reminder-location" v-if="reminder.location">
              <text class="location-icon">📍</text>
              <text class="location-text">{{ reminder.location }}</text>
            </view>
          </view>
          
          <view class="reminder-status">
            <view class="countdown">{{ getCountdown(reminder.dateTime) }}</view>
          </view>
        </view>
      </view>

      <!-- 历史提醒 -->
      <view v-if="historyReminders.length > 0" class="reminder-section">
        <view class="section-header">
          <text class="section-title">历史提醒</text>
          <view class="section-badge">{{ historyReminders.length }}</view>
        </view>
        
        <view 
          v-for="reminder in historyReminders" 
          :key="reminder.id"
          class="reminder-item history"
          @tap="handleReminderTap(reminder)">
          
          <view class="reminder-icon">
            <text class="icon-text">{{ getReminderIcon(reminder.type) }}</text>
          </view>
          
          <view class="reminder-content">
            <view class="reminder-header">
              <text class="reminder-title">{{ reminder.title }}</text>
              <text class="reminder-date">{{ formatDate(reminder.dateTime) }}</text>
            </view>
            <text class="reminder-message">{{ reminder.message }}</text>
          </view>
          
          <view class="reminder-status">
            <text class="status-text">{{ getStatusText(reminder.status) }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="allReminders.length === 0" class="empty-state">
        <view class="empty-icon">🔔</view>
        <text class="empty-title">暂无提醒</text>
        <text class="empty-desc">您的约会提醒将在这里显示</text>
      </view>
    </scroll-view>

    <!-- 浮动操作按钮 -->
    <view class="fab" @tap="refreshReminders">
      <text class="fab-icon">🔄</text>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      loading: false,
      allReminders: [],
      reminderTypes: {
        DATE_REMINDER: '约会提醒',
        FEEDBACK_REMINDER: '反馈提醒',
        CONFIRMATION_REMINDER: '确认提醒',
        CANCELLATION: '取消通知'
      }
    }
  },

  computed: {
    // 今日提醒
    todayReminders() {
      const today = new Date().toDateString()
      return this.allReminders.filter(r => {
        const reminderDate = new Date(r.dateTime).toDateString()
        return reminderDate === today && r.type !== 'HISTORY'
      })
    },

    // 即将到来的提醒
    upcomingReminders() {
      const now = new Date()
      const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000)
      
      return this.allReminders.filter(r => {
        const reminderDate = new Date(r.dateTime)
        return reminderDate > tomorrow && r.type !== 'HISTORY'
      })
    },

    // 历史提醒
    historyReminders() {
      const now = new Date()
      return this.allReminders.filter(r => {
        const reminderDate = new Date(r.dateTime)
        return reminderDate < now || r.type === 'HISTORY'
      }).slice(0, 10) // 只显示最近10条
    }
  },

  onLoad() {
    this.loadReminders()
  },

  onShow() {
    this.loadReminders()
  },

  onPullDownRefresh() {
    this.loadReminders().finally(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    // 加载提醒数据
    async loadReminders() {
      this.loading = true
      try {
        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/reminders`,
          method: 'GET',
          header: {
            'token': uni.getStorageSync('token')
          }
        })

        if (response.data.code === 200) {
          this.allReminders = response.data.data || []
          console.log('加载到的提醒数据:', this.allReminders)
        } else {
          uni.showToast({
            title: response.data.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载提醒失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    // 处理提醒点击
    handleReminderTap(reminder) {
      // 标记为已读
      this.markAsRead(reminder.id)
      
      // 根据类型跳转
      switch (reminder.type) {
        case 'DATE_REMINDER':
          uni.navigateTo({
            url: `/pages/date/date-detail?arrangementId=${reminder.arrangementId}`
          })
          break
        case 'FEEDBACK_REMINDER':
          uni.navigateTo({
            url: `/pages/date/date-feedback?arrangementId=${reminder.arrangementId}`
          })
          break
        default:
          // 显示详情
          this.showReminderDetail(reminder)
      }
    },

    // 快速操作
    quickAction(reminder) {
      switch (reminder.type) {
        case 'DATE_REMINDER':
          // 查看约会详情
          uni.navigateTo({
            url: `/pages/date/date-detail?arrangementId=${reminder.arrangementId}`
          })
          break
        case 'FEEDBACK_REMINDER':
          // 立即反馈
          uni.navigateTo({
            url: `/pages/date/date-feedback?arrangementId=${reminder.arrangementId}`
          })
          break
        case 'CONFIRMATION_REMINDER':
          // 确认约会
          this.confirmDate(reminder.arrangementId)
          break
      }
    },

    // 标记为已读
    async markAsRead(reminderId) {
      try {
        await uni.request({
          url: `${config.getBaseUrl()}/user/date/reminders/${reminderId}/read`,
          method: 'POST',
          header: {
            'token': uni.getStorageSync('token')
          }
        })
        
        // 更新本地状态
        const reminder = this.allReminders.find(r => r.id === reminderId)
        if (reminder) {
          reminder.isRead = true
        }
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },

    // 全部标记为已读
    async markAllRead() {
      try {
        await uni.request({
          url: `${config.getBaseUrl()}/user/date/reminders/read-all`,
          method: 'POST',
          header: {
            'token': uni.getStorageSync('token')
          }
        })
        
        // 更新本地状态
        this.allReminders.forEach(r => r.isRead = true)
        
        uni.showToast({
          title: '已全部标记为已读',
          icon: 'success'
        })
      } catch (error) {
        console.error('标记全部已读失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    },

    // 刷新提醒
    refreshReminders() {
      this.loadReminders()
      uni.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    },

    // 获取提醒图标
    getReminderIcon(type) {
      const icons = {
        DATE_REMINDER: '⏰',
        FEEDBACK_REMINDER: '💬',
        CONFIRMATION_REMINDER: '✅',
        CANCELLATION: '❌'
      }
      return icons[type] || '🔔'
    },

    // 获取类型文本
    getTypeText(type) {
      return this.reminderTypes[type] || '通知'
    },

    // 获取标签样式类
    getTagClass(type) {
      const classes = {
        DATE_REMINDER: 'date',
        FEEDBACK_REMINDER: 'feedback',
        CONFIRMATION_REMINDER: 'confirm',
        CANCELLATION: 'cancel'
      }
      return classes[type] || 'default'
    },

    // 获取操作按钮文本
    getActionText(type) {
      const actions = {
        DATE_REMINDER: '查看',
        FEEDBACK_REMINDER: '反馈',
        CONFIRMATION_REMINDER: '确认',
        CANCELLATION: '查看'
      }
      return actions[type] || '查看'
    },

    // 获取倒计时
    getCountdown(dateTime) {
      const now = new Date()
      const target = new Date(dateTime)
      const diff = target - now
      
      if (diff <= 0) return '已过期'
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      
      if (days > 0) return `${days}天后`
      if (hours > 0) return `${hours}小时后`
      
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      return `${minutes}分钟后`
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        completed: '已完成',
        cancelled: '已取消',
        expired: '已过期'
      }
      return statusMap[status] || '已处理'
    },

    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const now = new Date()
      const diffTime = date - now
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays === 0) return '今天'
      if (diffDays === 1) return '明天'
      if (diffDays === -1) return '昨天'
      if (diffDays > 1 && diffDays < 7) return `${diffDays}天后`
      if (diffDays < -1 && diffDays > -7) return `${Math.abs(diffDays)}天前`
      
      return `${date.getMonth() + 1}月${date.getDate()}日`
    },

    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.reminders-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 20rpx 30rpx;
  padding-top: calc(20rpx + var(--status-bar-height, 0));
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  box-sizing: border-box;
}

.nav-left, .nav-right {
  width: 120rpx;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
}

.nav-action {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  text-align: right;
}

/* 提醒列表 */
.reminders-list {
  height: calc(100vh - 120rpx);
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

.reminder-section {
  margin-bottom: 40rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: white;
  margin-right: 15rpx;
}

.section-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  min-width: 40rpx;
  text-align: center;
}

/* 提醒项 */
.reminder-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
}

.reminder-item.unread {
  border-left: 6rpx solid #667eea;
  background: rgba(255, 255, 255, 1);
}

.reminder-item.today {
  border-left: 6rpx solid #f59e0b;
}

.reminder-item.upcoming {
  border-left: 6rpx solid #10b981;
}

.reminder-item.history {
  opacity: 0.7;
  border-left: 6rpx solid #6b7280;
}

.reminder-icon {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.icon-text {
  font-size: 32rpx;
  color: white;
}

.reminder-content {
  flex: 1;
  min-width: 0;
}

.reminder-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.reminder-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1f2937;
  flex: 1;
}

.reminder-time, .reminder-date {
  font-size: 24rpx;
  color: #6b7280;
}

.reminder-message {
  font-size: 26rpx;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 15rpx;
}

.reminder-location {
  display: flex;
  align-items: center;
  margin-bottom: 15rpx;
}

.location-icon {
  font-size: 20rpx;
  margin-right: 8rpx;
}

.location-text {
  font-size: 24rpx;
  color: #6b7280;
}

.reminder-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.tag {
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.tag.date {
  background: #fef3c7;
  color: #92400e;
}

.tag.feedback {
  background: #dbeafe;
  color: #1e40af;
}

.tag.confirm {
  background: #d1fae5;
  color: #065f46;
}

.tag.cancel {
  background: #fee2e2;
  color: #991b1b;
}

.tag.urgent {
  background: #fecaca;
  color: #dc2626;
}

.reminder-actions {
  margin-left: 20rpx;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  text-align: center;
  min-width: 80rpx;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.reminder-status {
  margin-left: 20rpx;
  text-align: center;
}

.countdown {
  font-size: 24rpx;
  font-weight: bold;
  color: #f59e0b;
  padding: 8rpx 16rpx;
  background: #fef3c7;
  border-radius: 12rpx;
}

.status-text {
  font-size: 24rpx;
  color: #6b7280;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 60rpx;
  text-align: center;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 15rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
}

/* 浮动操作按钮 */
.fab {
  position: fixed;
  bottom: 120rpx;
  right: 60rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
}

.fab-icon {
  font-size: 40rpx;
  color: white;
}
</style>
