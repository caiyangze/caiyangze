<template>
  <view class="date-history-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会记录</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <!-- 筛选器 -->
    <view class="filter-bar">
      <view 
        v-for="(filter, index) in filters" 
        :key="index"
        class="filter-item"
        :class="{ active: currentFilter === index }"
        @tap="switchFilter(index)">
        <text class="filter-text">{{ filter.name }}</text>
      </view>
    </view>

    <!-- 约会列表 -->
    <scroll-view scroll-y class="history-list" v-if="filteredDates.length > 0">
      <view 
        v-for="date in filteredDates" 
        :key="date.arrangementId"
        class="history-item"
        @tap="viewDateDetail(date)">
        
        <view class="history-header">
          <view class="date-avatar">
            <text class="avatar-text">{{ getDateTypeIcon(date.dateType) }}</text>
          </view>
          <view class="date-info">
            <text class="date-title">{{ getDateTypeName(date.dateType) }}约会</text>
            <text class="date-location">{{ date.dateLocation }}</text>
          </view>
          <view class="date-status" :class="getStatusClass(date.arrangementStatus)">
            <text class="status-text">{{ getStatusText(date.arrangementStatus) }}</text>
          </view>
        </view>
        
        <view class="history-content">
          <view class="content-row">
            <text class="content-label">约会时间：</text>
            <text class="content-value">{{ formatDateTime(date.dateTime) }}</text>
          </view>
          <view v-if="date.datePlan" class="content-row">
            <text class="content-label">约会计划：</text>
            <text class="content-value plan-text">{{ date.datePlan }}</text>
          </view>
          <view class="content-row">
            <text class="content-label">创建时间：</text>
            <text class="content-value">{{ formatTime(date.createdAt) }}</text>
          </view>
        </view>
        
        <view class="history-actions">
          <button class="action-btn" @tap.stop="viewDateDetail(date)">
            查看详情
          </button>
          <button 
            v-if="date.arrangementStatus === 3 && !date.hasFeedback" 
            class="action-btn primary" 
            @tap.stop="giveFeedback(date)">
            评价约会
          </button>
          <button 
            v-if="date.arrangementStatus === 1" 
            class="action-btn success" 
            @tap.stop="startChat(date)">
            开始聊天
          </button>
        </view>
      </view>
    </scroll-view>
    
    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon">📅</view>
      <text class="empty-text">{{ getEmptyText() }}</text>
      <button v-if="currentFilter === 0" class="empty-btn" @tap="goToMatch">
        去寻找缘分
      </button>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      currentFilter: 0,
      filters: [
        { name: '全部', status: null },
        { name: '已确认', status: 1 },
        { name: '已完成', status: 3 },
        { name: '已取消', status: 2 }
      ],
      allDates: [],
      dateTypeNames: {
        1: '咖啡厅',
        2: '餐厅',
        3: '电影院',
        4: '其他'
      },
      dateTypeIcons: {
        1: '☕',
        2: '🍽️',
        3: '🎬',
        4: '💕'
      }
    }
  },

  computed: {
    filteredDates() {
      if (this.currentFilter === 0) {
        return this.allDates
      }
      const filterStatus = this.filters[this.currentFilter].status
      return this.allDates.filter(date => date.arrangementStatus === filterStatus)
    }
  },

  onLoad() {
    this.loadDateHistory()
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 切换筛选器
    switchFilter(index) {
      this.currentFilter = index
    },

    // 加载约会历史
    async loadDateHistory() {
      try {
        uni.showLoading({ title: '加载中...' })
        
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
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
          this.allDates = response.data.data || []
          // 按时间倒序排列
          this.allDates.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        } else {
          uni.showToast({
            title: response.data.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载约会历史失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 查看约会详情
    viewDateDetail(date) {
      uni.navigateTo({
        url: `/pages/date/date-detail?arrangementId=${date.arrangementId}`
      })
    },

    // 给出约会反馈
    giveFeedback(date) {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${date.arrangementId}`
      })
    },

    // 开始聊天
    startChat(date) {
      const currentUserId = uni.getStorageSync('userId')
      const chatUserId = date.userId === currentUserId ? date.targetUserId : date.userId
      
      uni.navigateTo({
        url: `/pages/message/chat?userId=${chatUserId}`
      })
    },

    // 去匹配页面
    goToMatch() {
      uni.switchTab({
        url: '/pages/match/match'
      })
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
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

    // 获取约会类型图标
    getDateTypeIcon(type) {
      return this.dateTypeIcons[type] || '💕'
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

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已取消'
        case 3: return '已完成'
        default: return '未知'
      }
    },

    // 获取空状态文本
    getEmptyText() {
      switch (this.currentFilter) {
        case 0: return '暂无约会记录'
        case 1: return '暂无已确认的约会'
        case 2: return '暂无已完成的约会'
        case 3: return '暂无已取消的约会'
        default: return '暂无记录'
      }
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.date-history-container {
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

/* 筛选器 */
.filter-bar {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  padding: 20rpx;
  gap: 20rpx;
}

.filter-item {
  flex: 1;
  padding: 16rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  text-align: center;
  transition: all 0.3s ease;
}

.filter-item.active {
  background: white;
}

.filter-text {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.filter-item.active .filter-text {
  color: #667eea;
  font-weight: bold;
}

/* 历史列表 */
.history-list {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}

.history-item {
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.history-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.date-avatar {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.avatar-text {
  font-size: 36rpx;
}

.date-info {
  flex: 1;
}

.date-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2d3748;
  display: block;
  margin-bottom: 8rpx;
}

.date-location {
  font-size: 24rpx;
  color: #718096;
}

.date-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.date-status.pending {
  background: rgba(255, 193, 7, 0.1);
  color: #ff9800;
}

.date-status.confirmed {
  background: rgba(76, 175, 80, 0.1);
  color: #4caf50;
}

.date-status.cancelled {
  background: rgba(244, 67, 54, 0.1);
  color: #f44336;
}

.date-status.completed {
  background: rgba(156, 39, 176, 0.1);
  color: #9c27b0;
}

.history-content {
  padding: 30rpx;
}

.content-row {
  display: flex;
  margin-bottom: 12rpx;
}

.content-row:last-child {
  margin-bottom: 0;
}

.content-label {
  font-size: 26rpx;
  color: #718096;
  width: 140rpx;
  flex-shrink: 0;
}

.content-value {
  font-size: 26rpx;
  color: #2d3748;
  flex: 1;
}

.plan-text {
  line-height: 1.5;
}

.history-actions {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
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

.action-btn.success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
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
  margin-bottom: 40rpx;
}

.empty-btn {
  background: white;
  color: #667eea;
  font-size: 28rpx;
  font-weight: bold;
  padding: 24rpx 48rpx;
  border-radius: 24rpx;
  border: none;
}
</style>
