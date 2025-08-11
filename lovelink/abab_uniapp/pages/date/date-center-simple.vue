<template>
  <view class="container">
    <view class="header">
      <text class="title">约会中心</text>
    </view>
    
    <view class="content">
      <view class="section">
        <text class="section-title">我的约会</text>
        <view class="date-list">
          <view v-for="(date, index) in dateList" :key="index" class="date-item">
            <view class="date-info">
              <text class="date-title">{{ date.title }}</text>
              <text class="date-time">{{ date.time }}</text>
            </view>
            <view class="date-status" :class="date.status">
              {{ getStatusText(date.status) }}
            </view>
          </view>
        </view>
      </view>
      
      <view class="actions">
        <button class="action-btn" @tap="createDate">创建约会</button>
        <button class="action-btn secondary" @tap="viewHistory">查看历史</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      dateList: [
        {
          title: '咖啡约会',
          time: '2025-08-06 14:00',
          status: 'pending'
        },
        {
          title: '电影约会',
          time: '2025-08-07 19:00',
          status: 'confirmed'
        }
      ]
    }
  },
  
  methods: {
    getStatusText(status) {
      const statusMap = {
        pending: '待确认',
        confirmed: '已确认',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || '未知'
    },
    
    createDate() {
      uni.showToast({
        title: '创建约会功能开发中',
        icon: 'none'
      })
    },
    
    viewHistory() {
      uni.showToast({
        title: '查看历史功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.date-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
}

.date-item:last-child {
  border-bottom: none;
}

.date-info {
  flex: 1;
}

.date-title {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.date-time {
  font-size: 24rpx;
  color: #666;
}

.date-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #fff;
}

.date-status.pending {
  background-color: #ffa500;
}

.date-status.confirmed {
  background-color: #52c41a;
}

.date-status.completed {
  background-color: #1890ff;
}

.date-status.cancelled {
  background-color: #ff4d4f;
}

.actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.action-btn.secondary {
  background-color: #6c757d;
}
</style>
