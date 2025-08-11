<template>
  <view class="manage-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">牵线申请管理</view>
      <view class="nav-right">
        <view class="pending-badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
      </view>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-tabs">
      <view 
        class="tab-item" 
        :class="{ 'active': currentStatus === null }"
        @click="switchStatus(null)"
      >
        全部
      </view>
      <view 
        class="tab-item" 
        :class="{ 'active': currentStatus === 0 }"
        @click="switchStatus(0)"
      >
        待处理
        <view class="tab-badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
      </view>
      <view 
        class="tab-item" 
        :class="{ 'active': currentStatus === 1 }"
        @click="switchStatus(1)"
      >
        已接受
      </view>
      <view 
        class="tab-item" 
        :class="{ 'active': currentStatus === 2 }"
        @click="switchStatus(2)"
      >
        已拒绝
      </view>
    </view>

    <!-- 申请列表 -->
    <view class="requests-container" v-if="!isLoading">
      <view class="request-item" v-for="request in requestList" :key="request.requestId">
        <!-- 优美的申请卡片 -->
        <view class="beautiful-card">
          <!-- 顶部装饰条 -->
          <view class="card-top" :style="{ background: getStatusGradient(request.requestStatus) }"></view>

          <!-- 用户信息区域 -->
          <view class="user-section">
            <view class="user-profile">
              <view class="avatar-container">
                <image class="user-avatar" :src="request.applicantUser?.avatarUrl || '/static/default-avatar.png'" mode="aspectFill"></image>
                <view class="online-dot"></view>
              </view>
              <view class="user-details">
                <text class="user-name">{{ request.applicantUser?.nickname || '测试用户1' }}</text>
                <view class="user-meta">
                  <text class="time-text">{{ formatTime(request.createdAt) }}</text>
                  <view class="status-pill" :style="{ background: getStatusColor(request.requestStatus) }">
                    {{ formatStatus(request.requestStatus) }}
                  </view>
                  <view class="paid-badge">
                    <text class="paid-text">✅ 已付费</text>
                  </view>
                </view>
              </view>
            </view>
            <button class="profile-btn" @click="viewUserProfile(request.userId)">
              <view class="btn-content">
                <text class="btn-text">查看</text>
                <text class="btn-arrow">→</text>
              </view>
            </button>
          </view>

          <!-- 连接线 -->
          <view class="connection-line">
            <view class="line"></view>
            <view class="heart-icon">💕</view>
            <view class="line"></view>
          </view>

          <!-- 目标用户 -->
          <view class="target-section">
            <view class="target-info">
              <text class="target-label">想要认识</text>
              <text class="target-name">{{ request.targetUser?.nickname || '测试用户2' }}</text>
            </view>
            <button class="target-profile-btn" @click="viewUserProfile(request.targetUserId)">
              <view class="btn-content">
                <text class="btn-text">查看</text>
                <text class="btn-arrow">→</text>
              </view>
            </button>
          </view>

          <!-- 申请详情 -->
          <view class="request-details">
            <!-- 红娘工作提示 -->
            <view class="work-guide">
              <view class="guide-header">
                <text class="guide-icon">👩‍💼</text>
                <text class="guide-label">红娘工作指引</text>
              </view>
              <view class="guide-content">
                <text class="guide-text">请根据双方用户资料评估匹配度，决定是否接受此牵线申请</text>
              </view>
            </view>

            <!-- 申请信息列表 -->
            <view class="info-list">
              <view class="info-item">
                <view class="item-icon">⏰</view>
                <view class="item-content">
                  <text class="item-label">申请时间</text>
                  <text class="item-value">{{ formatTime(request.createdAt) }}</text>
                </view>
              </view>

              <view class="info-item">
                <view class="item-icon">💕</view>
                <view class="item-content">
                  <text class="item-label">服务类型</text>
                  <text class="item-value">专业牵线服务</text>
                </view>
              </view>

              <view class="info-item">
                <view class="item-icon">💰</view>
                <view class="item-content">
                  <text class="item-label">服务费用</text>
                  <text class="item-value paid">¥199（用户已支付）</text>
                </view>
              </view>

              <view class="info-item">
                <view class="item-icon">📊</view>
                <view class="item-content">
                  <text class="item-label">工作建议</text>
                  <text class="item-value">点击查看按钮了解双方详细资料</text>
                </view>
              </view>

              <view class="info-item" v-if="request.requestStatus === 4">
                <view class="item-icon">🎉</view>
                <view class="item-content">
                  <text class="item-label">当前状态</text>
                  <text class="item-value success">双方已同意，可安排约会</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="action-section" v-if="request.requestStatus === 0">
            <button class="action-btn accept" @click="handleRequest(request.requestId, 1)">
              <text class="action-icon">✓</text>
              <text class="action-text">接受牵线</text>
            </button>
            <button class="action-btn reject" @click="showRejectDialog(request.requestId)">
              <text class="action-icon">✕</text>
              <text class="action-text">暂不考虑</text>
            </button>
          </view>

          <!-- 其他状态显示 -->
          <view class="status-section" v-else>
            <text class="status-description">{{ getStatusDescription(request.requestStatus) }}</text>
          </view>
        </view>
        
        <view class="reject-reason" v-if="request.requestStatus === 2 && request.rejectReason">
          <text class="reason-label">拒绝原因：</text>
          <text class="reason-content">{{ request.rejectReason }}</text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="requestList.length === 0">
        <image class="empty-icon" src="/static/icons/empty.png" mode="aspectFit"></image>
        <text class="empty-text">暂无申请记录</text>
        <text class="empty-desc">当有用户发起牵线申请时，会显示在这里</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 拒绝原因弹窗 -->
    <view class="modal-overlay" v-if="showRejectModal" @click="closeRejectModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">拒绝申请</text>
          <text class="modal-close" @click="closeRejectModal">×</text>
        </view>
        <view class="modal-body">
          <view class="refund-notice">
            <view class="notice-icon">⚠️</view>
            <view class="notice-content">
              <text class="notice-title">重要提醒</text>
              <text class="notice-text">拒绝申请后，用户支付的服务费用将自动退还到其账户中</text>
            </view>
          </view>

          <textarea
            class="reject-input"
            v-model="rejectReason"
            placeholder="请输入拒绝原因（选填）"
            maxlength="200"
          ></textarea>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeRejectModal">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmReject">确认拒绝</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { handleMatchmakingRequest, getMatchmakerRequests, getPendingRequestCount } from '@/api/matchmaker-manage'

export default {
  data() {
    return {
      requestList: [],
      currentStatus: null, // 当前筛选状态
      pendingCount: 0, // 待处理申请数量
      isLoading: false,
      showRejectModal: false,
      rejectReason: '',
      currentRejectRequestId: null
    }
  },
  
  onLoad() {
    // 检查登录状态
    const token = uni.getStorageSync('token') || ''
    console.log('红娘管理页面 - Token检查:', {
      hasToken: !!token,
      tokenLength: token.length,
      tokenPreview: token ? token.substring(0, 20) + '...' : 'null'
    })

    if (!token) {
      uni.showModal({
        title: '需要登录',
        content: '请先登录红娘账号',
        showCancel: false,
        confirmText: '去登录',
        success: () => {
          uni.redirectTo({
            url: '/pages/login/login'
          })
        }
      })
      return
    }

    this.loadPendingCount()
    this.loadRequestList()
  },
  
  onPullDownRefresh() {
    this.loadRequestList().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  
  methods: {
    // 加载待处理申请数量
    async loadPendingCount() {
      try {
        const result = await getPendingRequestCount()
        
        if (result.code === 200) {
          this.pendingCount = result.data || 0
        }
      } catch (error) {
        console.error('加载待处理数量失败:', error)
      }
    },
    
    // 加载申请列表
    async loadRequestList() {
      this.isLoading = true
      
      try {
        const result = await getMatchmakerRequests(this.currentStatus)
        
        if (result.code === 200) {
          this.requestList = result.data.records || []
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载申请列表失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
      }
    },
    
    // 切换状态筛选
    switchStatus(status) {
      this.currentStatus = status
      this.loadRequestList()
    },
    
    // 处理申请
    async handleRequest(requestId, action) {
      try {
        const result = await handleMatchmakingRequest(requestId, action)
        
        if (result.code === 200) {
          uni.showToast({
            title: result.message || '处理成功',
            icon: 'success'
          })
          
          // 刷新列表和待处理数量
          this.loadRequestList()
          this.loadPendingCount()
        } else {
          uni.showToast({
            title: result.message || '处理失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('处理申请失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },
    
    // 显示拒绝对话框
    showRejectDialog(requestId) {
      this.currentRejectRequestId = requestId
      this.rejectReason = ''
      this.showRejectModal = true
    },
    
    // 关闭拒绝对话框
    closeRejectModal() {
      this.showRejectModal = false
      this.currentRejectRequestId = null
      this.rejectReason = ''
    },
    
    // 确认拒绝
    async confirmReject() {
      try {
        const result = await handleMatchmakingRequest(
          this.currentRejectRequestId,
          2,
          this.rejectReason || '红娘拒绝了此申请'
        )
        
        if (result.code === 200) {
          uni.showToast({
            title: '已拒绝申请',
            icon: 'success'
          })
          
          this.closeRejectModal()
          this.loadRequestList()
          this.loadPendingCount()
        } else {
          uni.showToast({
            title: result.message || '拒绝失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('拒绝申请失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },

    // 查看用户详情
    viewUserProfile(userId) {
      uni.navigateTo({
        url: `/pages/user/user-detail?userId=${userId}`
      })
    },

    // 格式化状态
    formatStatus(status) {
      const statusMap = {
        0: '待处理',
        1: '待用户确认',
        2: '已拒绝',
        3: '用户已拒绝',
        4: '双方同意',
        5: '已完成'
      }
      return statusMap[status] || '未知状态'
    },
    
    // 获取状态颜色
    getStatusColor(status) {
      const colorMap = {
        0: '#f39c12', // 橙色 - 待处理
        1: '#3498db', // 蓝色 - 待用户确认
        2: '#e74c3c', // 红色 - 已拒绝
        3: '#e67e22', // 深橙色 - 用户已拒绝
        4: '#27ae60', // 绿色 - 双方同意
        5: '#9b59b6'  // 紫色 - 已完成
      }
      return colorMap[status] || '#95a5a6'
    },

    // 获取状态描述
    getStatusDescription(status) {
      const descMap = {
        1: '已接受申请，等待目标用户确认',
        2: '申请已被拒绝',
        3: '目标用户已拒绝',
        4: '双方都同意，可以安排约会',
        5: '牵线服务已完成'
      }
      return descMap[status] || ''
    },

    // 获取状态渐变色
    getStatusGradient(status) {
      const gradientMap = {
        0: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 待处理 - 紫色渐变
        1: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', // 待确认 - 蓝色渐变
        2: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', // 已拒绝 - 粉色渐变
        3: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)', // 用户拒绝 - 橙色渐变
        4: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)', // 双方同意 - 绿色渐变
        5: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)'  // 已完成 - 彩虹渐变
      }
      return gradientMap[status] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      
      const time = new Date(timeStr)
      const now = new Date()
      const diff = now - time
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) { // 1天内
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return time.toLocaleDateString()
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.manage-page {
  min-height: 100vh;
  background: #f5f5f5;
  width: 100%;
  box-sizing: border-box;
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
  width: 100%;
  box-sizing: border-box;
}

.nav-left, .nav-right {
  width: 80rpx;
  position: relative;
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

.pending-badge {
  position: absolute;
  top: -10rpx;
  right: 0;
  background: #ff4757;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 8rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  background: white;
  padding: 20rpx;
  margin-bottom: 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 12rpx;
  margin: 0 10rpx;
  position: relative;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tab-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  background: #ff4757;
  color: white;
  font-size: 18rpx;
  padding: 2rpx 6rpx;
  border-radius: 10rpx;
  min-width: 24rpx;
  text-align: center;
}

/* 申请列表样式与之前的my-requests.vue类似 */
.requests-container {
  padding: 0 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.request-item {
  background: white;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 15rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.06);
}

.request-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  margin-right: 20rpx;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.request-time {
  font-size: 24rpx;
  color: #999;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.target-info, .request-message {
  margin-bottom: 20rpx;
  line-height: 1.5;
}

.target-label, .message-label {
  font-size: 26rpx;
  color: #666;
}

.target-name, .message-content {
  font-size: 26rpx;
  color: #333;
}

.request-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  border: none;
  border-radius: 35rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.accept-btn {
  background: #27ae60;
  color: white;
}

.reject-btn {
  background: #e74c3c;
  color: white;
}

.reject-reason {
  padding: 20rpx;
  background: #fff5f5;
  border-radius: 12rpx;
  border-left: 6rpx solid #ff4757;
}

.reason-label {
  font-size: 24rpx;
  color: #ff4757;
  font-weight: bold;
}

.reason-content {
  font-size: 24rpx;
  color: #666;
}

/* 空状态和加载状态样式与之前相同 */
.empty-state, .loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.empty-icon {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 40rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  line-height: 1.5;
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

/* 拒绝弹窗 */
.modal-overlay {
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
  border-radius: 20rpx;
  width: 600rpx;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 2rpx solid #f0f0f0;
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

.modal-body {
  padding: 30rpx;
}

.reject-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  padding: 30rpx;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: #e74c3c;
  color: white;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 优美的卡片设计 */
.beautiful-card {
  position: relative;
  background: white;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
}

.card-top {
  height: 8rpx;
  width: 100%;
}

.user-section {
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar-container {
  position: relative;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  border: 4rpx solid #f8f9fa;
}

.online-dot {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 20rpx;
  height: 20rpx;
  background: #48bb78;
  border-radius: 50%;
  border: 3rpx solid white;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.time-text {
  font-size: 24rpx;
  color: #718096;
}

.status-pill {
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  color: white;
  font-weight: bold;
}

.profile-btn {
  background: rgba(102, 126, 234, 0.1);
  border: 2rpx solid rgba(102, 126, 234, 0.2);
  border-radius: 25rpx;
  padding: 12rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10rpx);
}

.profile-btn:active {
  background: rgba(102, 126, 234, 0.2);
  transform: scale(0.95);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.btn-text {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
}

.btn-arrow {
  font-size: 20rpx;
  color: #667eea;
  font-weight: bold;
  transition: transform 0.3s ease;
}

.profile-btn:active .btn-arrow {
  transform: translateX(4rpx);
}

/* 连接线设计 */
.connection-line {
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  margin: 20rpx 0;
}

.line {
  flex: 1;
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.heart-icon {
  font-size: 32rpx;
  margin: 0 20rpx;
  animation: heartbeat 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 目标用户区域 */
.target-section {
  padding: 0 30rpx 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.target-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.target-label {
  font-size: 24rpx;
  color: #718096;
}

.target-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
}

.target-profile-btn {
  background: rgba(79, 172, 254, 0.1);
  border: 2rpx solid rgba(79, 172, 254, 0.2);
  border-radius: 20rpx;
  padding: 10rpx 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(10rpx);
}

.target-profile-btn:active {
  background: rgba(79, 172, 254, 0.2);
  transform: scale(0.95);
}

.target-profile-btn .btn-text {
  font-size: 22rpx;
  color: #4facfe;
  font-weight: 600;
}

.target-profile-btn .btn-arrow {
  font-size: 18rpx;
  color: #4facfe;
  font-weight: bold;
}

.target-profile-btn:active .btn-arrow {
  transform: translateX(3rpx);
}

/* 消息区域 */
.message-section {
  padding: 0 30rpx 30rpx;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.message-icon {
  font-size: 24rpx;
}

.message-label {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
}

.message-bubble {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 20rpx;
  border-radius: 16rpx;
  border-left: 4rpx solid #667eea;
  position: relative;
}

.message-bubble::before {
  content: '';
  position: absolute;
  top: 15rpx;
  left: -8rpx;
  width: 0;
  height: 0;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
  border-right: 8rpx solid #f7fafc;
}

.message-content {
  font-size: 26rpx;
  color: #4a5568;
  line-height: 1.6;
}

/* 操作按钮区域 */
.action-section {
  padding: 0 30rpx 30rpx;
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 16rpx;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  font-weight: bold;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: left 0.5s;
}

.action-btn:active::before {
  left: 100%;
}

.action-btn.accept {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  box-shadow: 0 6rpx 20rpx rgba(72, 187, 120, 0.4);
}

.action-btn.reject {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  color: white;
  box-shadow: 0 6rpx 20rpx rgba(245, 101, 101, 0.4);
}

.action-icon {
  font-size: 32rpx;
  font-weight: bold;
}

.action-text {
  font-size: 26rpx;
  font-weight: bold;
}

/* 状态显示区域 */
.status-section {
  padding: 30rpx;
  text-align: center;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-top: 1rpx solid #e2e8f0;
}

.status-description {
  font-size: 26rpx;
  color: #718096;
  font-weight: 500;
}

/* 申请详情区域 */
.request-details {
  padding: 0 30rpx 20rpx;
}

/* 工作指引区域 */
.work-guide {
  margin-bottom: 20rpx;
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.guide-icon {
  font-size: 24rpx;
}

.guide-label {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
}

.guide-content {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  padding: 20rpx;
  border-radius: 16rpx;
  border-left: 4rpx solid #f56565;
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(245, 101, 101, 0.1);
}

.guide-content::before {
  content: '';
  position: absolute;
  top: 15rpx;
  left: -8rpx;
  width: 0;
  height: 0;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
  border-right: 8rpx solid #fff5f5;
}

.guide-text {
  font-size: 26rpx;
  color: #c53030;
  line-height: 1.6;
  font-weight: 500;
}

/* 信息列表 */
.info-list {
  margin-top: 20rpx;
}

.info-item {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 12rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.05);
}

.info-item:active {
  transform: scale(0.98);
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
}

.info-item:last-child {
  margin-bottom: 0;
}

.item-icon {
  font-size: 36rpx;
  width: 60rpx;
  height: 60rpx;
  background: rgba(102, 126, 234, 0.15);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.item-label {
  font-size: 26rpx;
  color: #718096;
  font-weight: 600;
}

.item-value {
  font-size: 28rpx;
  color: #2d3748;
  font-weight: bold;
  line-height: 1.4;
  word-break: break-all;
}

.item-value.success {
  color: #48bb78;
}

.item-value.paid {
  color: #e53e3e;
  font-weight: bold;
}

/* 已付费标识 */
.paid-badge {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  margin-left: 12rpx;
}

.paid-text {
  font-size: 20rpx;
  font-weight: bold;
}

/* 退款提醒样式 */
.refund-notice {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border: 2rpx solid #fc8181;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: flex-start;
}

.notice-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
  margin-top: 4rpx;
}

.notice-content {
  flex: 1;
}

.notice-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #c53030;
  display: block;
  margin-bottom: 8rpx;
}

.notice-text {
  font-size: 24rpx;
  color: #742a2a;
  line-height: 1.4;
}

/* 消息气泡优化 */
.message-bubble {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 20rpx;
  border-radius: 16rpx;
  border-left: 4rpx solid #667eea;
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.message-bubble::before {
  content: '';
  position: absolute;
  top: 15rpx;
  left: -8rpx;
  width: 0;
  height: 0;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
  border-right: 8rpx solid #f7fafc;
}

.message-content {
  font-size: 26rpx;
  color: #4a5568;
  line-height: 1.6;
  word-break: break-all;
}

/* 整体卡片动画 */
.request-item {
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.request-item:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.15);
}
</style>
