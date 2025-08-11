<template>
  <view class="date-detail-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会详情</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <view class="loading-icon">⏳</view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 约会详情内容 -->
    <scroll-view v-else-if="dateDetail" scroll-y class="detail-content">
      <!-- 状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="getStatusClass(dateDetail.arrangementStatus)">
            {{ getStatusIcon(dateDetail.arrangementStatus) }}
          </view>
          <view class="status-info">
            <text class="status-title">{{ getStatusText(dateDetail.arrangementStatus) }}</text>
            <text class="status-desc">{{ getStatusDesc(dateDetail.arrangementStatus) }}</text>
          </view>
        </view>
      </view>

      <!-- 约会信息卡片 -->
      <view class="info-card">
        <view class="card-header">
          <text class="card-title">约会信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <view class="info-icon">🕐</view>
            <view class="info-content">
              <text class="info-label">约会时间</text>
              <text class="info-value">{{ formatDateTime(dateDetail.dateTime) }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">📍</view>
            <view class="info-content">
              <text class="info-label">约会地点</text>
              <text class="info-value">{{ dateDetail.dateLocation }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">{{ getDateTypeIcon(dateDetail.dateType) }}</view>
            <view class="info-content">
              <text class="info-label">约会类型</text>
              <text class="info-value">{{ getDateTypeName(dateDetail.dateType) }}</text>
            </view>
          </view>
          <view v-if="dateDetail.datePlan" class="info-item">
            <view class="info-icon">📝</view>
            <view class="info-content">
              <text class="info-label">约会计划</text>
              <text class="info-value plan-text">{{ dateDetail.datePlan }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 确认状态卡片 -->
      <view class="confirm-card">
        <view class="card-header">
          <text class="card-title">确认状态</text>
        </view>
        <view class="confirm-list">
          <view class="confirm-item">
            <view class="confirm-user">
              <text class="user-label">{{ getUserLabel(true) }}</text>
            </view>
            <view class="confirm-status" :class="getConfirmClass(getMyConfirmStatus())">
              <text class="confirm-text">{{ getConfirmText(getMyConfirmStatus()) }}</text>
            </view>
          </view>
          <view class="confirm-item">
            <view class="confirm-user">
              <text class="user-label">{{ getUserLabel(false) }}</text>
            </view>
            <view class="confirm-status" :class="getConfirmClass(getOtherConfirmStatus())">
              <text class="confirm-text">{{ getConfirmText(getOtherConfirmStatus()) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 取消原因（如果有） -->
      <view v-if="dateDetail.cancelReason" class="cancel-card">
        <view class="card-header">
          <text class="card-title">取消原因</text>
        </view>
        <view class="cancel-content">
          <text class="cancel-reason">{{ dateDetail.cancelReason }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <view class="error-icon">😔</view>
      <text class="error-text">约会详情加载失败</text>
      <button class="retry-btn" @tap="loadDateDetail">重试</button>
    </view>

    <!-- 底部操作栏 -->
    <view v-if="dateDetail && showActions" class="action-bar">
      <!-- 待确认状态的操作 -->
      <view v-if="dateDetail.arrangementStatus === 0 && canConfirm" class="action-buttons">
        <button class="action-btn reject-btn" @tap="showRejectModal">
          拒绝约会
        </button>
        <button class="action-btn accept-btn" @tap="confirmDate(1)">
          确认参加
        </button>
      </view>
      
      <!-- 已确认状态的操作 -->
      <view v-else-if="dateDetail.arrangementStatus === 1" class="action-buttons">
        <button class="action-btn" @tap="startChat">
          开始聊天
        </button>
        <button class="action-btn primary" @tap="viewLocation">
          查看位置
        </button>
      </view>
      
      <!-- 已确认状态的操作 -->
      <view v-if="dateDetail.arrangementStatus === 1" class="action-buttons">
        <button class="action-btn primary" @tap="giveFeedback">
          提交反馈
        </button>
      </view>

      <!-- 已完成状态的操作 -->
      <view v-else-if="dateDetail.arrangementStatus === 3" class="action-buttons">
        <button class="action-btn" @tap="startChat">
          继续聊天
        </button>
        <button class="action-btn secondary" @tap="viewFeedback">
          查看反馈
        </button>
      </view>
    </view>

    <!-- 拒绝原因选择模态框 -->
    <view v-if="showRejectReasonModal" class="modal-overlay" @tap="hideRejectModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">拒绝约会</text>
          <view class="modal-close" @tap="hideRejectModal">✕</view>
        </view>

        <view class="modal-body">
          <view class="reason-section">
            <text class="section-title">请选择拒绝原因：</text>

            <view class="reason-options">
              <view
                v-for="(reason, index) in rejectReasons"
                :key="index"
                class="reason-option"
                :class="{ active: selectedRejectReason === reason.value }"
                @tap="selectRejectReason(reason.value)">
                <view class="option-radio">
                  <view v-if="selectedRejectReason === reason.value" class="radio-checked">●</view>
                </view>
                <text class="option-text">{{ reason.label }}</text>
              </view>
            </view>

            <view v-if="selectedRejectReason === 'other'" class="custom-reason">
              <textarea
                v-model="customRejectReason"
                placeholder="请输入其他原因..."
                class="reason-input"
                maxlength="200">
              </textarea>
              <text class="char-count">{{ customRejectReason.length }}/200</text>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="modal-btn cancel-btn" @tap="hideRejectModal">取消</button>
          <button class="modal-btn confirm-btn" @tap="confirmReject" :disabled="!canConfirmReject">确认拒绝</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      arrangementId: null,
      dateDetail: null,
      loading: true,
      currentUserId: null,
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
      },

      // 拒绝原因相关
      showRejectReasonModal: false,
      selectedRejectReason: '',
      customRejectReason: '',
      rejectReasons: [
        { label: '时间不合适', value: 'time_conflict' },
        { label: '地点不方便', value: 'location_issue' },
        { label: '约会类型不喜欢', value: 'type_dislike' },
        { label: '个人原因', value: 'personal_reason' },
        { label: '对对方不感兴趣', value: 'not_interested' },
        { label: '其他原因', value: 'other' }
      ]
    }
  },

  computed: {
    // 是否显示操作按钮
    showActions() {
      return this.dateDetail && [0, 1, 3].includes(this.dateDetail.arrangementStatus)
    },

    // 当前用户是否可以确认
    canConfirm() {
      if (!this.dateDetail || !this.currentUserId) return false
      
      const isUserA = this.dateDetail.userId === this.currentUserId
      const isUserB = this.dateDetail.targetUserId === this.currentUserId
      
      if (isUserA && this.dateDetail.userAConfirm === 0) return true
      if (isUserB && this.dateDetail.userBConfirm === 0) return true
      
      return false
    },

    // 是否可以确认拒绝
    canConfirmReject() {
      if (!this.selectedRejectReason) return false
      if (this.selectedRejectReason === 'other') {
        return this.customRejectReason.trim().length > 0
      }
      return true
    }
  },

  async onLoad(options) {
    this.arrangementId = options.arrangementId
    await this.getCurrentUserId()

    if (this.arrangementId) {
      this.loadDateDetail()
    } else {
      this.loading = false
    }
  },

  onShow() {
    // 页面显示时重新加载数据，确保数据是最新的
    console.log('约会详情页面onShow触发，arrangementId:', this.arrangementId)
    if (this.arrangementId) {
      this.loadDateDetail()
    }
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
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

            console.log('用户信息API响应:', response.data)

            if (response.data.code === 200 && response.data.data) {
              // TbUser对象的主键是userId
              this.currentUserId = response.data.data.userId

              console.log('从API获取的用户ID:', {
                userId: response.data.data.userId,
                userIdType: typeof response.data.data.userId,
                fullUserData: response.data.data
              })

              // 保存到本地存储以便下次使用
              if (this.currentUserId) {
                uni.setStorageSync('userId', this.currentUserId)
              }
            } else {
              console.error('获取用户信息失败:', response.data)
            }
          } catch (e) {
            console.error('获取用户信息失败:', e)
          }
        }
      }

      console.log('获取用户ID结果:', {
        currentUserId: this.currentUserId,
        token: uni.getStorageSync('token') ? '存在' : '不存在'
      })
    },

    // 加载约会详情
    async loadDateDetail() {
      try {
        this.loading = true
        
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/arrangement/${this.arrangementId}`,
          method: 'GET',
          header: {
            'token': token
          }
        })

        if (response.data.code === 200) {
          this.dateDetail = response.data.data
          console.log('约会详情数据:', {
            arrangementId: this.dateDetail.arrangementId,
            userId: this.dateDetail.userId,
            targetUserId: this.dateDetail.targetUserId,
            userAConfirm: this.dateDetail.userAConfirm,
            userBConfirm: this.dateDetail.userBConfirm,
            currentUserId: this.currentUserId
          })
        } else {
          uni.showToast({
            title: response.data.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载约会详情失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    // 确认约会
    async confirmDate(confirm) {
      try {
        uni.showLoading({ title: '处理中...' })
        
        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/confirm/${this.arrangementId}?confirm=${confirm}`,
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
          
          // 刷新详情
          this.loadDateDetail()
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

    // 开始聊天
    startChat() {
      const chatUserId = this.dateDetail.userId === this.currentUserId 
        ? this.dateDetail.targetUserId 
        : this.dateDetail.userId
      
      uni.navigateTo({
        url: `/pages/message/chat?userId=${chatUserId}`
      })
    },

    // 查看位置
    viewLocation() {
      // 这里可以集成地图功能
      uni.showModal({
        title: '约会地点',
        content: this.dateDetail.dateLocation,
        showCancel: false
      })
    },

    // 给出反馈
    giveFeedback() {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${this.arrangementId}`
      })
    },

    // 查看反馈
    viewFeedback() {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${this.arrangementId}`
      })
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
    },

    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      const date = new Date(dateTimeStr)
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()
      const hour = date.getHours()
      const minute = date.getMinutes().toString().padStart(2, '0')
      const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
      
      return `${year}年${month}月${day}日 ${weekDay} ${hour}:${minute}`
    },

    // 获取约会类型名称
    getDateTypeName(type) {
      return this.dateTypeNames[type] || '其他'
    },

    // 获取用户标签
    getUserLabel(isMe) {
      if (isMe) {
        return '我'
      } else {
        return '对方'
      }
    },

    // 获取我的确认状态
    getMyConfirmStatus() {
      if (!this.currentUserId || !this.dateDetail) {
        console.log('getMyConfirmStatus - 缺少数据:', {
          currentUserId: this.currentUserId,
          dateDetail: this.dateDetail
        })
        return 0
      }

      const isUserA = this.dateDetail.userId === this.currentUserId
      const myStatus = isUserA ? this.dateDetail.userAConfirm : this.dateDetail.userBConfirm

      console.log('getMyConfirmStatus - 计算结果:', {
        currentUserId: this.currentUserId,
        currentUserIdType: typeof this.currentUserId,
        dateDetailUserId: this.dateDetail.userId,
        dateDetailUserIdType: typeof this.dateDetail.userId,
        isUserA: isUserA,
        userAConfirm: this.dateDetail.userAConfirm,
        userBConfirm: this.dateDetail.userBConfirm,
        myStatus: myStatus,
        comparison: `${this.currentUserId} === ${this.dateDetail.userId} = ${this.currentUserId === this.dateDetail.userId}`
      })

      return myStatus
    },

    // 获取对方的确认状态
    getOtherConfirmStatus() {
      if (!this.currentUserId || !this.dateDetail) return 0

      const isUserA = this.dateDetail.userId === this.currentUserId
      return isUserA ? this.dateDetail.userBConfirm : this.dateDetail.userAConfirm
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
        default: return '未知状态'
      }
    },

    // 获取状态描述
    getStatusDesc(status) {
      switch (status) {
        case 0: return '等待双方确认参加约会'
        case 1: return '双方已确认，约会正式确定'
        case 2: return '约会已取消'
        case 3: return '约会已完成，可以给出评价'
        default: return ''
      }
    },

    // 获取确认状态样式类
    getConfirmClass(confirm) {
      switch (confirm) {
        case 0: return 'pending'
        case 1: return 'confirmed'
        case 2: return 'rejected'
        default: return ''
      }
    },

    // 获取确认状态文本
    getConfirmText(confirm) {
      switch (confirm) {
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已拒绝'
        default: return '未知'
      }
    },

    // 显示拒绝原因选择模态框
    showRejectModal() {
      this.selectedRejectReason = ''
      this.customRejectReason = ''
      this.showRejectReasonModal = true
    },

    // 隐藏拒绝原因模态框
    hideRejectModal() {
      this.showRejectReasonModal = false
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
      if (!this.canConfirmReject) return

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
        let url = `${config.getBaseUrl()}/user/date/confirm/${this.arrangementId}?confirm=2`
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

          // 刷新详情
          this.loadDateDetail()
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
.date-detail-container {
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

/* 内容区域 */
.detail-content {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}

/* 卡片通用样式 */
.status-card, .info-card, .confirm-card, .cancel-card {
  background: white;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 30rpx;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2d3748;
}

/* 状态卡片 */
.status-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-right: 20rpx;
}

.status-icon.pending {
  background: rgba(255, 193, 7, 0.1);
}

.status-icon.confirmed {
  background: rgba(76, 175, 80, 0.1);
}

.status-icon.cancelled {
  background: rgba(244, 67, 54, 0.1);
}

.status-icon.completed {
  background: rgba(156, 39, 176, 0.1);
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
  display: block;
  margin-bottom: 8rpx;
}

.status-desc {
  font-size: 24rpx;
  color: #718096;
}

/* 信息列表 */
.info-list, .confirm-list {
  padding: 30rpx;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-icon {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
}

.info-label {
  font-size: 24rpx;
  color: #718096;
  display: block;
  margin-bottom: 8rpx;
}

.info-value {
  font-size: 28rpx;
  color: #2d3748;
  line-height: 1.5;
}

.plan-text {
  line-height: 1.6;
}

/* 确认状态 */
.confirm-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.confirm-item:last-child {
  margin-bottom: 0;
}

.user-label {
  font-size: 28rpx;
  color: #2d3748;
  font-weight: bold;
}

.confirm-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.confirm-status.pending {
  background: rgba(255, 193, 7, 0.1);
  color: #ff9800;
}

.confirm-status.confirmed {
  background: rgba(76, 175, 80, 0.1);
  color: #4caf50;
}

.confirm-status.rejected {
  background: rgba(244, 67, 54, 0.1);
  color: #f44336;
}



/* 取消原因 */
.cancel-content {
  padding: 30rpx;
}

.cancel-reason {
  font-size: 28rpx;
  color: #e53e3e;
  line-height: 1.6;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 20rpx;
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

.action-btn.secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
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

/* 状态页面 */
.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-icon, .error-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.loading-text, .error-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40rpx;
}

.retry-btn {
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  border-radius: 20rpx;
  width: 90%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #999;
  border-radius: 50%;
  background: #f5f5f5;
}

.modal-body {
  padding: 30rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.reason-section {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.reason-options {
  margin-bottom: 20rpx;
}

.reason-option {
  display: flex;
  align-items: center;
  padding: 20rpx;
  margin-bottom: 10rpx;
  border-radius: 12rpx;
  border: 2rpx solid #f0f0f0;
  transition: all 0.3s;
}

.reason-option.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.option-radio {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.reason-option.active .option-radio {
  border-color: #667eea;
  background: #667eea;
}

.radio-checked {
  color: white;
  font-size: 20rpx;
}

.option-text {
  font-size: 26rpx;
  color: #333;
}

.custom-reason {
  margin-top: 20rpx;
}

.reason-input {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  font-size: 26rpx;
  line-height: 1.5;
  resize: none;
}

.char-count {
  font-size: 22rpx;
  color: #999;
  text-align: right;
  margin-top: 10rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
  color: white;
}

.confirm-btn:disabled {
  background: #ccc;
  color: #999;
}
</style>
