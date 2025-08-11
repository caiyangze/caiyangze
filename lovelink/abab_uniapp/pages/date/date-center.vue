<template>
  <view class="date-center-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会中心</text>
      </view>
      <view class="nav-right" @tap="goToReminders">
        <text class="nav-icon">🔔</text>
      </view>
    </view>

    <!-- 统计面板 -->
    <view class="stats-panel">
      <view class="stat-card" :class="{ active: currentFilter === 'pending' }" @tap="switchFilter('pending')">
        <text class="stat-number">{{ pendingCount }}</text>
        <text class="stat-label">待确认</text>
      </view>
      <view class="stat-card" :class="{ active: currentFilter === 'confirmed' }" @tap="switchFilter('confirmed')">
        <text class="stat-number">{{ confirmedCount }}</text>
        <text class="stat-label">已确认</text>
      </view>
      <view class="stat-card" :class="{ active: currentFilter === 'all' }" @tap="switchFilter('all')">
        <text class="stat-number">{{ totalCount }}</text>
        <text class="stat-label">全部</text>
      </view>
    </view>

    <!-- 约会列表 -->
    <scroll-view scroll-y class="date-list" v-if="filteredDates.length > 0">
      <view 
        v-for="arrangement in filteredDates" 
        :key="arrangement.arrangementId"
        class="date-card"
        @tap="viewDateDetail(arrangement)">
        
        <view class="date-header">
          <view class="date-status" :class="getStatusClass(arrangement.arrangementStatus)">
            <text class="status-icon">{{ getStatusIcon(arrangement.arrangementStatus) }}</text>
            <text class="status-text">{{ getStatusText(arrangement.arrangementStatus) }}</text>
          </view>
          <text class="date-time">{{ formatTime(arrangement.createdAt) }}</text>
        </view>
        
        <view class="date-content">
          <view class="date-info">
            <view class="info-row">
              <text class="info-label">约会时间</text>
              <text class="info-value">{{ formatDateTime(arrangement.dateTime) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">约会地点</text>
              <text class="info-value">{{ arrangement.dateLocation }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">约会类型</text>
              <text class="info-value">{{ getDateTypeName(arrangement.dateType) }}</text>
            </view>
          </view>
          
          <!-- 操作按钮 -->
          <view class="date-actions">
            <!-- 待确认状态 -->
            <view v-if="arrangement.arrangementStatus === 0" class="action-buttons">
              <button class="action-btn reject-btn" @tap.stop="showRejectModal(arrangement)">
                拒绝
              </button>
              <button class="action-btn accept-btn" @tap.stop="confirmDate(arrangement, 1)">
                确认
              </button>
            </view>

            <!-- 已确认状态 -->
            <view v-else-if="arrangement.arrangementStatus === 1" class="action-buttons">
              <button class="action-btn" @tap.stop="startChat(arrangement)">
                开始聊天
              </button>
            </view>

            <!-- 已完成状态 -->
            <view v-else-if="arrangement.arrangementStatus === 3" class="action-buttons">
              <button class="action-btn" @tap.stop="giveFeedback(arrangement)">
                评价约会
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon">{{ getEmptyIcon() }}</view>
      <text class="empty-text">{{ getEmptyText() }}</text>
      <button v-if="currentFilter === 'all'" class="empty-btn" @tap="goToMatch">
        去寻找缘分
      </button>
    </view>
  </view>

  <!-- 拒绝原因选择模态框 -->
  <view v-if="showRejectReasonModal" class="modal-overlay" @tap="hideRejectModal">
    <view class="modal-content" @tap.stop>
      <view class="modal-header">
        <text class="modal-title">选择拒绝原因</text>
        <view class="modal-close" @tap="hideRejectModal">
          <text class="close-icon">✕</text>
        </view>
      </view>

      <view class="modal-body">
        <view class="reason-grid">
          <view
            v-for="(reason, index) in rejectReasons"
            :key="index"
            class="reason-card"
            :class="{ active: selectedRejectReason === reason.value }"
            @tap="selectRejectReason(reason.value)">
            <view class="reason-icon">{{ reason.icon }}</view>
            <text class="reason-text">{{ reason.label }}</text>
            <view class="reason-check" v-if="selectedRejectReason === reason.value">
              <text class="check-icon">✓</text>
            </view>
          </view>
        </view>

        <view v-if="selectedRejectReason === 'other'" class="custom-reason">
          <view class="input-label">请详细说明原因：</view>
          <textarea
            v-model="customRejectReason"
            placeholder="请输入具体的拒绝原因..."
            class="reason-textarea"
            maxlength="200">
          </textarea>
          <view class="char-count">{{ customRejectReason.length }}/200</view>
        </view>
      </view>

      <view class="modal-footer">
        <button class="modal-btn cancel-btn" @tap="hideRejectModal">取消</button>
        <button
          class="modal-btn confirm-btn"
          :class="{ disabled: !canConfirmReject }"
          :disabled="!canConfirmReject"
          @tap="confirmReject">
          确认拒绝
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      currentFilter: 'all', // 'all', 'pending', 'confirmed'
      allDates: [],
      currentUserId: null,
      pendingCount: 0,
      confirmedCount: 0,
      totalCount: 0,
      dateTypeNames: {
        1: '咖啡厅',
        2: '餐厅',
        3: '电影院',
        4: '其他'
      },

      // 拒绝原因相关
      showRejectReasonModal: false,
      currentRejectArrangement: null,
      selectedRejectReason: '',
      customRejectReason: '',
      rejectReasons: [
        { label: '时间不合适', value: 'time_conflict', icon: '⏰' },
        { label: '地点不方便', value: 'location_issue', icon: '📍' },
        { label: '约会类型不喜欢', value: 'type_dislike', icon: '💔' },
        { label: '个人原因', value: 'personal_reason', icon: '👤' },
        { label: '对对方不感兴趣', value: 'not_interested', icon: '😐' },
        { label: '其他原因', value: 'other', icon: '✏️' }
      ]
    }
  },

  computed: {
    // 是否可以确认拒绝
    canConfirmReject() {
      if (!this.selectedRejectReason) return false
      if (this.selectedRejectReason === 'other') {
        return this.customRejectReason.trim().length > 0
      }
      return true
    },

    filteredDates() {
      switch (this.currentFilter) {
        case 'pending':
          return this.allDates.filter(date => date.arrangementStatus === 0)
        case 'confirmed':
          return this.allDates.filter(date => date.arrangementStatus === 1)
        default:
          return this.allDates
      }
    }
  },

  async onLoad() {
    await this.getCurrentUserId()
    this.loadDateArrangements()
    this.loadDateStatistics()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadDateArrangements()
    this.loadDateStatistics()
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 跳转到约会提醒页面
    goToReminders() {
      uni.navigateTo({
        url: '/pages/date/date-reminders'
      })
    },

    // 获取当前用户ID
    async getCurrentUserId() {
      // 强制清除可能错误的缓存
      uni.removeStorageSync('userId')
      uni.removeStorageSync('currentUserId')
      uni.removeStorageSync('user_id')

      // 强制从API重新获取
      this.currentUserId = null

      // 如果还是空，通过API获取用户信息
      if (!this.currentUserId) {
        const token = uni.getStorageSync('token')
        if (token) {
          try {
            // 调用后端API获取用户信息
            const response = await uni.request({
              url: `${config.getBaseUrl()}/user/userInfo`,
              method: 'POST',
              data: { token },
              header: {
                'token': token
              }
            })

            if (response.data.code === 200 && response.data.data) {
              this.currentUserId = response.data.data.userId

              // 保存到本地存储以便下次使用
              if (this.currentUserId) {
                uni.setStorageSync('userId', this.currentUserId)
              }
            }
          } catch (e) {
            console.error('获取用户信息失败:', e)
          }
        }
      }

      console.log('约会中心 - 获取用户ID结果:', {
        currentUserId: this.currentUserId,
        token: uni.getStorageSync('token') ? '存在' : '不存在'
      })
    },

    // 切换筛选器
    switchFilter(filter) {
      this.currentFilter = filter
    },

    // 加载约会安排
    async loadDateArrangements() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) return

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/my-arrangements`,
          method: 'GET',
          header: {
            'token': token
          }
        })

        if (response.data.code === 200) {
          this.allDates = response.data.data || []
          this.allDates.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        }
      } catch (error) {
        console.error('加载约会安排失败:', error)
      }
    },

    // 加载统计数据
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

    // 确认约会
    async confirmDate(arrangement, confirm) {
      // 检查是否已经确认过
      if (this.hasConfirmed(arrangement)) {
        console.log('用户已确认过，阻止重复确认')
        uni.showToast({
          title: '您已经确认过此约会',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({ title: '处理中...' })

        const token = uni.getStorageSync('token')
        const url = `${config.getBaseUrl()}/user/date/confirm/${arrangement.arrangementId}?confirm=${confirm}`

        console.log('请求详情:', {
          url: url,
          token: token,
          arrangementId: arrangement.arrangementId,
          confirm: confirm,
          currentUserId: this.currentUserId
        })

        const response = await uni.request({
          url: url,
          method: 'POST',
          header: {
            'token': token
          }
        })
        
        console.log('服务器响应:', response.data)

        if (response.data.code === 200) {
          uni.showToast({
            title: confirm === 1 ? '确认成功' : '已拒绝',
            icon: 'success'
          })

          // 刷新数据
          this.loadDateArrangements()
          this.loadDateStatistics()
        } else {
          console.error('服务器错误:', response.data)
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

    // 查看约会详情
    viewDateDetail(arrangement) {
      uni.navigateTo({
        url: `/pages/date/date-detail?arrangementId=${arrangement.arrangementId}`
      })
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

    // 去匹配页面
    goToMatch() {
      uni.switchTab({
        url: '/pages/match/match'
      })
    },

    // 判断是否可以确认
    canConfirm(arrangement) {
      if (!this.currentUserId) return false

      const isUserA = arrangement.userId === this.currentUserId
      const isUserB = arrangement.targetUserId === this.currentUserId

      if (isUserA && arrangement.userAConfirm === 0) return true
      if (isUserB && arrangement.userBConfirm === 0) return true

      return false
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 86400000) { // 1天内
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
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已取消'
        case 3: return '已完成'
        default: return '未知'
      }
    },

    // 获取空状态图标
    getEmptyIcon() {
      switch (this.currentFilter) {
        case 'pending': return '⏳'
        case 'confirmed': return '✅'
        default: return '📭'
      }
    },

    // 获取空状态文本
    getEmptyText() {
      switch (this.currentFilter) {
        case 'pending': return '暂无待确认的约会'
        case 'confirmed': return '暂无已确认的约会'
        default: return '暂无约会安排'
      }
    },

    // 检查当前用户是否已确认
    hasConfirmed(arrangement) {
      if (!this.currentUserId) return false

      const isUserA = arrangement.userId === this.currentUserId
      const myConfirmStatus = isUserA ? arrangement.userAConfirm : arrangement.userBConfirm

      console.log('约会中心 - hasConfirmed检查:', {
        arrangementId: arrangement.arrangementId,
        currentUserId: this.currentUserId,
        arrangementUserId: arrangement.userId,
        isUserA: isUserA,
        userAConfirm: arrangement.userAConfirm,
        userBConfirm: arrangement.userBConfirm,
        myConfirmStatus: myConfirmStatus,
        hasConfirmed: myConfirmStatus !== 0
      })

      return myConfirmStatus !== 0 // 0表示未确认，1确认，2拒绝
    },

    // 获取我的确认状态文本
    getMyConfirmText(arrangement) {
      if (!this.currentUserId) return '未知'

      const isUserA = arrangement.userId === this.currentUserId
      const myConfirmStatus = isUserA ? arrangement.userAConfirm : arrangement.userBConfirm

      switch (myConfirmStatus) {
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已拒绝'
        default: return '未知'
      }
    },

    // 获取对方的确认状态文本
    getOtherConfirmText(arrangement) {
      if (!this.currentUserId) return '未知'

      const isUserA = arrangement.userId === this.currentUserId
      const otherConfirmStatus = isUserA ? arrangement.userBConfirm : arrangement.userAConfirm

      switch (otherConfirmStatus) {
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已拒绝'
        default: return '未知'
      }
    },

    // 获取我的状态样式类（基于我的确认状态，不是整体约会状态）
    getMyStatusClass(arrangement) {
      if (!this.currentUserId) return 'pending'

      const isUserA = arrangement.userId === this.currentUserId
      const myConfirmStatus = isUserA ? arrangement.userAConfirm : arrangement.userBConfirm

      switch (myConfirmStatus) {
        case 0: return 'pending'    // 我待确认
        case 1: return 'confirmed'  // 我已确认
        case 2: return 'cancelled'  // 我已拒绝
        default: return 'pending'
      }
    },

    // 获取我的状态图标（基于我的确认状态）
    getMyStatusIcon(arrangement) {
      if (!this.currentUserId) return '⏳'

      const isUserA = arrangement.userId === this.currentUserId
      const myConfirmStatus = isUserA ? arrangement.userAConfirm : arrangement.userBConfirm

      switch (myConfirmStatus) {
        case 0: return '⏳'  // 我待确认
        case 1: return '✅'  // 我已确认
        case 2: return '❌'  // 我已拒绝
        default: return '⏳'
      }
    },

    // 获取我的状态文本（基于我的确认状态）
    getMyStatusText(arrangement) {
      if (!this.currentUserId) return '待确认'

      const isUserA = arrangement.userId === this.currentUserId
      const myConfirmStatus = isUserA ? arrangement.userAConfirm : arrangement.userBConfirm
      const otherConfirmStatus = isUserA ? arrangement.userBConfirm : arrangement.userAConfirm

      switch (myConfirmStatus) {
        case 0: return '待确认'
        case 1:
          // 我已确认，检查对方状态
          if (otherConfirmStatus === 1) {
            return '约会确定' // 双方都确认
          } else if (otherConfirmStatus === 2) {
            return '对方拒绝' // 对方拒绝了
          } else {
            return '已确认' // 我确认了，等待对方
          }
        case 2: return '已拒绝'
        default: return '待确认'
      }
    },

    // 显示拒绝原因选择模态框
    showRejectModal(arrangement) {
      this.currentRejectArrangement = arrangement
      this.selectedRejectReason = ''
      this.customRejectReason = ''
      this.showRejectReasonModal = true
    },

    // 隐藏拒绝原因模态框
    hideRejectModal() {
      this.showRejectReasonModal = false
      this.currentRejectArrangement = null
      this.selectedRejectReason = ''
      this.customRejectReason = ''
    },

    // 选择拒绝原因
    selectRejectReason(value) {
      this.selectedRejectReason = value
      if (value !== 'other') {
        this.customRejectReason = ''
      }
    },

    // 确认拒绝约会
    async confirmReject() {
      if (!this.canConfirmReject || !this.currentRejectArrangement) return

      try {
        uni.showLoading({ title: '处理中...' })

        // 构建拒绝原因
        let rejectReason = ''
        if (this.selectedRejectReason === 'other') {
          rejectReason = this.customRejectReason.trim()
        } else {
          const reasonObj = this.rejectReasons.find(r => r.value === this.selectedRejectReason)
          rejectReason = reasonObj ? reasonObj.label : '未知原因'
        }

        // 构建请求URL
        let url = `${config.getBaseUrl()}/user/date/confirm/${this.currentRejectArrangement.arrangementId}?confirm=2`
        if (rejectReason) {
          url += `&rejectReason=${encodeURIComponent(rejectReason)}`
        }

        const response = await uni.request({
          url: url,
          method: 'POST',
          header: {
            'token': uni.getStorageSync('token')
          }
        })

        if (response.data.code === 200) {
          uni.showToast({
            title: '已拒绝约会',
            icon: 'success'
          })

          // 隐藏模态框
          this.hideRejectModal()

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
        console.error('拒绝约会失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        uni.hideLoading()
      }
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.date-center-container {
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

/* 统计面板 */
.stats-panel {
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
}

.stat-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  backdrop-filter: blur(10rpx);
  transition: all 0.3s ease;
}

.stat-card.active {
  background: white;
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.stat-number {
  font-size: 48rpx;
  font-weight: bold;
  color: #667eea;
  display: block;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #718096;
}

/* 约会列表 */
.date-list {
  height: calc(100vh - 280rpx);
  padding: 0 20rpx 20rpx;
}

.date-card {
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.date-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.date-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.date-status.pending {
  background: rgba(255, 152, 0, 0.1);
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

.date-time {
  font-size: 24rpx;
  color: #a0aec0;
}

.date-content {
  padding: 30rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 26rpx;
  color: #718096;
}

.info-value {
  font-size: 26rpx;
  color: #2d3748;
  font-weight: 500;
}

.date-actions {
  margin-top: 30rpx;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
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

.action-btn.accept-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
}

.action-btn.reject-btn {
  background: #f7fafc;
  color: #e53e3e;
  border: 2rpx solid #fed7d7;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
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

/* 拒绝原因模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4rpx);
}

.modal-content {
  background: white;
  border-radius: 24rpx;
  width: 90%;
  max-width: 640rpx;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(100rpx) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 40rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #2d3748;
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f7fafc;
  transition: all 0.3s;
}

.modal-close:active {
  background: #e2e8f0;
  transform: scale(0.95);
}

.close-icon {
  font-size: 32rpx;
  color: #718096;
}

.modal-body {
  padding: 30rpx 40rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.reason-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.reason-card {
  position: relative;
  background: #f8f9fa;
  border: 3rpx solid transparent;
  border-radius: 20rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.reason-card:active {
  transform: scale(0.98);
}

.reason-card.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
}

.reason-icon {
  font-size: 48rpx;
  margin-bottom: 15rpx;
  display: block;
}

.reason-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #2d3748;
  display: block;
}

.reason-card.active .reason-text {
  color: white;
}

.reason-check {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.custom-reason {
  margin-top: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 20rpx;
}

.input-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 20rpx;
}

.reason-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  resize: none;
  background: white;
  transition: border-color 0.3s;
}

.reason-textarea:focus {
  border-color: #667eea;
  outline: none;
}

.char-count {
  font-size: 24rpx;
  color: #718096;
  text-align: right;
  margin-top: 15rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 40rpx 40rpx;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s;
}

.cancel-btn {
  background: #f7fafc;
  color: #718096;
}

.cancel-btn:active {
  background: #e2e8f0;
  transform: scale(0.98);
}

.confirm-btn {
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
  color: white;
  box-shadow: 0 8rpx 20rpx rgba(229, 62, 62, 0.3);
}

.confirm-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(229, 62, 62, 0.4);
}

.confirm-btn.disabled {
  background: #cbd5e0;
  color: #a0aec0;
  box-shadow: none;
  cursor: not-allowed;
}
</style>
