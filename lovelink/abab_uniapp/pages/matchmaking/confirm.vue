<template>
  <view class="confirm-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">牵线确认</view>
      <view class="nav-right">
        <view class="badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'pending' }"
        @click="switchTab('pending')">
        最新申请
        <view class="tab-badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'history' }"
        @click="switchTab('history')">
        历史记录
      </view>
    </view>

    <!-- 申请列表 -->
    <view class="requests-container">
      <!-- 加载状态 -->
      <view v-if="isLoading" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 申请列表 -->
      <view v-else-if="requestList.length > 0">
        <!-- 优美的牵线确认卡片 -->
        <view
          v-for="request in requestList"
          :key="request.request.requestId"
          class="beautiful-confirm-card">

          <!-- 顶部装饰 -->
          <view class="card-decoration">
            <view class="decoration-line"></view>
            <view class="heart-decoration">💕</view>
            <view class="decoration-line"></view>
          </view>

          <!-- 申请者信息 -->
          <view class="applicant-section">
            <view class="applicant-card">
              <view class="avatar-wrapper">
                <image class="applicant-avatar" :src="getApplicantAvatarUrl(request)" mode="aspectFill"></image>
                <view class="online-indicator"></view>
              </view>
              <view class="applicant-info">
                <text class="applicant-name">{{ getApplicantName(request) }}</text>
                <text class="apply-time">{{ formatTime(request.request.createdAt) }} 申请牵线</text>
              </view>
              <view class="applicant-actions">
                <button class="view-profile-btn" @click="viewApplicantProfile(request.request.userId)">
                  <text class="btn-icon">👤</text>
                  <text class="btn-text">查看详情</text>
                </button>
              </view>
            </view>
          </view>

          <!-- 红娘推荐 -->
          <view class="matchmaker-section">
            <view class="section-header">
              <text class="section-icon">👩‍💼</text>
              <text class="section-title">红娘推荐</text>
            </view>
            <view class="matchmaker-card">
              <image class="matchmaker-avatar" :src="getMatchmakerAvatarUrl(request)" mode="aspectFill"></image>
              <view class="matchmaker-info">
                <text class="matchmaker-name">{{ getMatchmakerName(request) }}</text>
                <text class="matchmaker-desc">专业红娘 · 贴心服务</text>
              </view>
              <view class="matchmaker-actions">
                <button class="view-matchmaker-btn" @click="viewMatchmakerProfile(request)" v-if="request.request.matchmakerId">
                  <text class="btn-icon">👩‍💼</text>
                  <text class="btn-text">查看红娘</text>
                </button>
              </view>
            </view>
          </view>

          <!-- 申请留言 -->
          <view class="message-section">
            <view class="section-header">
              <text class="section-icon">💬</text>
              <text class="section-title">TA想对你说</text>
            </view>
            <view class="message-bubble">
              <text class="message-text">{{ request.request.requestMessage || '你好，我对你很感兴趣，希望能够认识你。' }}</text>
              <view class="bubble-tail"></view>
            </view>
          </view>

          <!-- 操作区域 -->
          <view class="action-area" v-if="request.request.requestStatus === 1">
            <view class="action-title">
              <text class="title-text">你的决定</text>
              <text class="title-desc">选择是否接受这次牵线</text>
            </view>
            <view class="action-buttons">
              <button class="action-btn accept" @click="handleConfirm(request.request.requestId, 1)">
                <view class="btn-content">
                  <text class="btn-icon">💖</text>
                  <text class="btn-text">接受牵线</text>
                  <text class="btn-desc">开始美好缘分</text>
                </view>
              </button>
              <button class="action-btn decline" @click="showRejectDialog(request.request.requestId)">
                <view class="btn-content">
                  <text class="btn-icon">🤝</text>
                  <text class="btn-text">暂不考虑</text>
                  <text class="btn-desc">礼貌拒绝</text>
                </view>
              </button>
            </view>
          </view>

          <!-- 结果状态 -->
          <view class="result-section" v-else>
            <view class="result-card" v-if="request.request.requestStatus === 4">
              <text class="result-icon">🎉</text>
              <text class="result-title">牵线成功！</text>
              <text class="result-desc">双方都同意了，红娘将为你们安排约会</text>
            </view>
            <view class="result-card declined" v-else-if="request.request.requestStatus === 3">
              <text class="result-icon">💔</text>
              <text class="result-title">已拒绝</text>
              <text class="result-desc" v-if="request.request.rejectReason">{{ request.request.rejectReason }}</text>
              <text class="result-desc" v-else>你已拒绝了这次牵线</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <view class="empty-icon">💌</view>
        <text class="empty-text" v-if="currentTab === 'pending'">暂无待确认的牵线申请</text>
        <text class="empty-text" v-else>暂无历史记录</text>
        <text class="empty-desc" v-if="currentTab === 'pending'">当有人通过红娘申请牵线时，会显示在这里</text>
      </view>
    </view>

    <!-- 拒绝原因弹窗 -->
    <view class="modal-overlay" v-if="showRejectModal" @click="closeRejectModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">拒绝牵线申请</text>
          <text class="modal-close" @click="closeRejectModal">×</text>
        </view>
        <view class="modal-body">
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
export default {
  data() {
    return {
      currentTab: 'pending', // pending: 待确认, history: 历史记录
      requestList: [],
      pendingCount: 0,
      isLoading: false,
      showRejectModal: false,
      rejectReason: '',
      currentRejectRequestId: null
    }
  },

  onLoad() {
    this.loadPendingCount()
    this.loadRequestList()
  },

  onShow() {
    // 每次显示页面时刷新数据
    this.loadPendingCount()
    this.loadRequestList()
  },

  methods: {
    // 加载待确认申请数量
    async loadPendingCount() {
      try {
        const token = uni.getStorageSync('token') || ''
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking/confirm/count',
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          this.pendingCount = result.data || 0
        }
      } catch (error) {
        console.error('加载待确认数量失败:', error)
      }
    },

    // 加载申请列表
    async loadRequestList() {
      try {
        this.isLoading = true
        const token = uni.getStorageSync('token') || ''
        
        let url = ''
        if (this.currentTab === 'pending') {
          url = 'http://localhost:9001/user/matchmaking/confirm/pending'
        } else {
          url = 'http://localhost:9001/user/matchmaking/confirm/history'
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: url,
            method: 'GET',
            data: {
              pageNum: 1,
              pageSize: 20
            },
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          this.requestList = result.data.records || []
          console.log('DEBUG: 接收到的申请列表数据:', this.requestList)
          // 打印第一个申请的详细信息
          if (this.requestList.length > 0) {
            console.log('DEBUG: 第一个申请的详细信息:', this.requestList[0])
          }
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

    // 切换标签
    switchTab(tab) {
      this.currentTab = tab
      this.loadRequestList()
    },

    // 处理确认
    async handleConfirm(requestId, action) {
      try {
        const token = uni.getStorageSync('token') || ''
        const data = {
          requestId: requestId,
          action: action
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking/confirm/handle',
            method: 'POST',
            data: data,
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          uni.showToast({
            title: result.message || '处理成功',
            icon: 'success'
          })
          
          // 刷新列表和计数
          this.loadPendingCount()
          this.loadRequestList()
        } else {
          uni.showToast({
            title: result.message || '处理失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('处理确认失败:', error)
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
        const token = uni.getStorageSync('token') || ''
        const data = {
          requestId: this.currentRejectRequestId,
          action: 2,
          rejectReason: this.rejectReason.trim()
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking/confirm/handle',
            method: 'POST',
            data: data,
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          uni.showToast({
            title: '已拒绝该申请',
            icon: 'success'
          })
          
          this.closeRejectModal()
          this.loadPendingCount()
          this.loadRequestList()
        } else {
          uni.showToast({
            title: result.message || '操作失败',
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

    // 获取申请者头像URL
    getApplicantAvatarUrl(request) {
      if (request.applicantUser && request.applicantUser.avatarUrl) {
        return request.applicantUser.avatarUrl
      }
      return '/static/default-avatar.png'
    },

    // 获取申请者姓名（优先显示昵称）
    getApplicantName(request) {
      if (request.applicantUser) {
        // 优先显示昵称，如果没有昵称则显示用户ID
        return request.applicantUser.nickname || `用户${request.applicantUser.userId}`
      }
      return `用户${request.request.userId}`
    },

    // 获取红娘头像URL
    getMatchmakerAvatarUrl(request) {
      if (request.matchmakerUser && request.matchmakerUser.avatarUrl) {
        return request.matchmakerUser.avatarUrl
      }
      return '/static/matchmaker-default.png'
    },

    // 获取红娘姓名（优先显示昵称）
    getMatchmakerName(request) {
      if (request.matchmakerUser) {
        // 优先显示用户昵称，如果没有昵称则显示红娘真实姓名，最后显示ID
        return request.matchmakerUser.nickname ||
               (request.matchmaker && request.matchmaker.realName) ||
               `红娘${request.matchmakerUser.userId}`
      }
      if (request.matchmaker && request.matchmaker.realName) {
        return request.matchmaker.realName
      }
      return `红娘${request.request.matchmakerId}`
    },

    // 查看申请者详情
    viewApplicantProfile(userId) {
      if (!userId) {
        uni.showToast({
          title: '用户信息不存在',
          icon: 'none'
        })
        return
      }

      // 跳转到用户详情页面
      uni.navigateTo({
        url: `/pages/user/user-detail?userId=${userId}`
      })
    },

    // 查看红娘详情
    viewMatchmakerProfile(request) {
      // 优先使用已获取的红娘用户信息
      if (request.matchmakerUser && request.matchmakerUser.userId) {
        // 直接跳转到红娘对应的用户详情页面
        uni.navigateTo({
          url: `/pages/user/user-detail?userId=${request.matchmakerUser.userId}`
        })
        return
      }

      // 如果没有红娘用户信息，尝试通过API获取
      if (request.request.matchmakerId) {
        this.getMatchmakerUserId(request.request.matchmakerId)
      } else {
        uni.showToast({
          title: '红娘信息不存在',
          icon: 'none'
        })
      }
    },

    // 获取红娘对应的用户ID并跳转（备用方法）
    async getMatchmakerUserId(matchmakerId) {
      try {
        const token = uni.getStorageSync('token') || ''

        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: `http://localhost:9001/user/matchmaker/${matchmakerId}`,
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })

        if (result.code === 200 && result.data && result.data.userId) {
          // 跳转到红娘对应的用户详情页面
          uni.navigateTo({
            url: `/pages/user/user-detail?userId=${result.data.userId}`
          })
        } else {
          uni.showToast({
            title: '无法获取红娘信息',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取红娘信息失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },

    // 获取头像URL (保留原方法以兼容)
    getAvatarUrl(userId) {
      return '/static/default-avatar.png' // 这里可以根据userId获取真实头像
    },

    // 格式化状态
    formatStatus(status) {
      const statusMap = {
        0: '待红娘处理',
        1: '待您确认',
        2: '红娘已拒绝',
        3: '您已拒绝',
        4: '双方同意',
        5: '已完成'
      }
      return statusMap[status] || '未知状态'
    },

    // 获取状态颜色
    getStatusColor(status) {
      const colorMap = {
        0: '#f39c12',
        1: '#3498db',
        2: '#e74c3c',
        3: '#e67e22',
        4: '#27ae60',
        5: '#9b59b6'
      }
      return colorMap[status] || '#95a5a6'
    },

    // 获取状态渐变
    getStatusGradient(status) {
      const gradientMap = {
        1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', // 待确认 - 紫色渐变
        3: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', // 已拒绝 - 粉色渐变
        4: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'  // 已同意 - 绿色渐变
      }
      return gradientMap[status] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
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

    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.confirm-page {
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

.badge {
  position: absolute;
  top: -10rpx;
  right: 10rpx;
  background: #e74c3c;
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
  transition: all 0.3s ease;
  position: relative;
}

.tab-item.active {
  background: #667eea;
  color: white;
}

.tab-badge {
  position: absolute;
  top: 5rpx;
  right: 10rpx;
  background: #e74c3c;
  color: white;
  font-size: 18rpx;
  padding: 2rpx 6rpx;
  border-radius: 20rpx;
  min-width: 24rpx;
  text-align: center;
}

/* 申请列表 */
.requests-container {
  padding: 0 20rpx;
  width: 100%;
  box-sizing: border-box;
}

.request-item {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
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

.request-content {
  margin-bottom: 20rpx;
}

.matchmaker-info {
  margin-bottom: 15rpx;
}

.matchmaker-label {
  font-size: 26rpx;
  color: #666;
}

.matchmaker-name {
  font-size: 26rpx;
  color: #667eea;
  font-weight: bold;
}

.request-message {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #667eea;
}

.message-label {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
  display: block;
}

.message-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
}

/* 操作按钮 */
.request-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
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

/* 拒绝原因 */
.reject-reason {
  background: #fff5f5;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #e74c3c;
}

.reason-label {
  font-size: 24rpx;
  color: #e74c3c;
  margin-bottom: 10rpx;
  display: block;
}

.reason-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
}

/* 成功提示 */
.success-tip {
  background: #f0fff4;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #27ae60;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.tip-icon {
  font-size: 32rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #27ae60;
  font-weight: bold;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  width: 100%;
  box-sizing: border-box;
}

.empty-icon {
  font-size: 120rpx;
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

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
  width: 100%;
  box-sizing: border-box;
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

/* 弹窗样式 */
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

.modal-body {
  padding: 30rpx;
}

.reject-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 2rpx solid #f8f9fa;
}

.modal-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
  font-weight: bold;
}

.cancel-btn {
  background: #f8f9fa;
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

/* 优美的确认卡片样式 */
.beautiful-confirm-card {
  background: white;
  border-radius: 24rpx;
  margin-bottom: 30rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  position: relative;
}

/* 顶部装饰 */
.card-decoration {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.decoration-line {
  flex: 1;
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, #dee2e6 50%, transparent 100%);
}

.heart-decoration {
  font-size: 28rpx;
  margin: 0 20rpx;
  animation: heartbeat 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 申请者信息 */
.applicant-section {
  padding: 30rpx;
}

.applicant-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: white;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.avatar-wrapper {
  position: relative;
}

.applicant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  border: 4rpx solid #f8f9fa;
}

.online-indicator {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 20rpx;
  height: 20rpx;
  background: #48bb78;
  border-radius: 50%;
  border: 3rpx solid white;
}

.applicant-info {
  flex: 1;
}

.applicant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 8rpx;
  display: block;
}

.apply-time {
  font-size: 24rpx;
  color: #718096;
}

.applicant-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.view-profile-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.view-profile-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.4);
}

.view-profile-btn .btn-icon {
  font-size: 20rpx;
}

.view-profile-btn .btn-text {
  font-size: 24rpx;
  font-weight: 500;
}

/* 区域标题 */
.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 0 30rpx;
}

.section-icon {
  font-size: 28rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
}

/* 红娘推荐 */
.matchmaker-section {
  padding-bottom: 30rpx;
  border-bottom: 1rpx solid #e2e8f0;
}

.matchmaker-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 0 30rpx;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  margin: 0 30rpx;
  border-radius: 16rpx;
  padding: 20rpx;
}

.matchmaker-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
}

.matchmaker-info {
  flex: 1;
}

.matchmaker-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 6rpx;
  display: block;
}

.matchmaker-desc {
  font-size: 22rpx;
  color: #718096;
}

.matchmaker-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.view-matchmaker-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 16rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  border-radius: 18rpx;
  font-size: 22rpx;
  border: none;
  box-shadow: 0 3rpx 10rpx rgba(255, 107, 107, 0.3);
  transition: all 0.3s ease;
}

.view-matchmaker-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 6rpx rgba(255, 107, 107, 0.4);
}

.view-matchmaker-btn .btn-icon {
  font-size: 18rpx;
}

.view-matchmaker-btn .btn-text {
  font-size: 22rpx;
  font-weight: 500;
}

.badge-text {
  font-size: 20rpx;
  color: white;
  font-weight: bold;
}

/* 申请留言 */
.message-section {
  padding: 30rpx;
  border-bottom: 1rpx solid #e2e8f0;
}

.message-bubble {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 20rpx;
  border-radius: 16rpx;
  border-left: 4rpx solid #667eea;
  position: relative;
  margin: 0 20rpx;
}

.message-bubble::before {
  content: '';
  position: absolute;
  top: 20rpx;
  left: -8rpx;
  width: 0;
  height: 0;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
  border-right: 8rpx solid #f7fafc;
}

.message-text {
  font-size: 26rpx;
  color: #4a5568;
  line-height: 1.6;
}

/* 操作区域 */
.action-area {
  padding: 30rpx;
}

.action-title {
  text-align: center;
  margin-bottom: 30rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 8rpx;
  display: block;
}

.title-desc {
  font-size: 24rpx;
  color: #718096;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  border-radius: 20rpx;
  border: none;
  padding: 0;
  overflow: hidden;
  position: relative;
}

.action-btn.accept {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  box-shadow: 0 8rpx 25rpx rgba(72, 187, 120, 0.4);
}

.action-btn.decline {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  box-shadow: 0 8rpx 25rpx rgba(245, 101, 101, 0.4);
}

.btn-content {
  padding: 25rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.btn-icon {
  font-size: 36rpx;
}

.btn-text {
  font-size: 28rpx;
  font-weight: bold;
  color: white;
}

.btn-desc {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 结果状态 */
.result-section {
  padding: 30rpx;
}

.result-card {
  text-align: center;
  padding: 40rpx 20rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border: 2rpx solid #48bb78;
}

.result-card.declined {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  border: 2rpx solid #f56565;
}

.result-icon {
  font-size: 48rpx;
  margin-bottom: 15rpx;
  display: block;
}

.result-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 10rpx;
  display: block;
}

.result-desc {
  font-size: 26rpx;
  color: #718096;
  line-height: 1.5;
}
</style>
