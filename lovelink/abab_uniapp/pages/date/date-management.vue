<template>
  <view class="date-management-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会管理</text>
      </view>
      <view class="nav-right" @tap="showQuickActions">
        <text class="nav-icon">⚡</text>
      </view>
    </view>

    <!-- 标签页 -->
    <view class="tabs">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @tap="switchTab(index)">
        <text class="tab-text">{{ tab.name }}</text>
        <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view scroll-y class="content-area">
      <!-- 待确认约会 -->
      <view v-if="currentTab === 0" class="tab-content">
        <view v-if="pendingDates.length > 0" class="date-list">
          <view 
            v-for="date in pendingDates" 
            :key="date.arrangementId"
            class="date-item pending">
            <view class="date-header">
              <view class="date-status">⏳ 待确认</view>
              <text class="date-time">{{ formatTime(date.createdAt) }}</text>
            </view>
            
            <view class="date-content">
              <view class="date-info">
                <view class="info-row">
                  <text class="info-label">约会时间：</text>
                  <text class="info-value">{{ formatDateTime(date.dateTime) }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">约会地点：</text>
                  <text class="info-value">{{ date.dateLocation }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">约会类型：</text>
                  <text class="info-value">{{ getDateTypeName(date.dateType) }}</text>
                </view>
              </view>
              
              <view class="date-actions" v-if="!hasUserConfirmed(date)">
                <button class="action-btn reject-btn" @tap="directReject(date)">
                  拒绝
                </button>
                <button class="action-btn accept-btn" @tap="confirmDate(date.arrangementId, 1)">
                  确认参加
                </button>
              </view>

              <!-- 已确认状态显示 -->
              <view class="confirmed-status" v-else>
                <view class="status-indicator" :class="getUserConfirmClass(date)">
                  <view class="status-icon">{{ getUserConfirmIcon(date) }}</view>
                  <text class="status-text">{{ getUserConfirmText(date) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <view class="empty-icon">⏳</view>
          <text class="empty-text">暂无待确认的约会</text>
        </view>
      </view>

      <!-- 已确认约会 -->
      <view v-if="currentTab === 1" class="tab-content">
        <view v-if="confirmedDates.length > 0" class="date-list">
          <view 
            v-for="date in confirmedDates" 
            :key="date.arrangementId"
            class="date-item confirmed">
            <view class="date-header">
              <view class="date-status confirmed">✅ 已确认</view>
              <text class="date-time">{{ formatTime(date.updatedAt) }}</text>
            </view>
            
            <view class="date-content">
              <view class="date-info">
                <view class="info-row">
                  <text class="info-label">约会时间：</text>
                  <text class="info-value">{{ formatDateTime(date.dateTime) }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">约会地点：</text>
                  <text class="info-value">{{ date.dateLocation }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">约会类型：</text>
                  <text class="info-value">{{ getDateTypeName(date.dateType) }}</text>
                </view>
              </view>
              
              <view class="date-actions">
                <button class="action-btn" @tap="viewDateDetail(date)">
                  查看详情
                </button>
                <button class="action-btn feedback-btn" @tap="goToFeedback(date.arrangementId)">
                  提交反馈
                </button>
                <button class="action-btn primary" @tap="startChat(date)">
                  开始聊天
                </button>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <view class="empty-icon">✅</view>
          <text class="empty-text">暂无已确认的约会</text>
        </view>
      </view>

      <!-- 历史约会 -->
      <view v-if="currentTab === 2" class="tab-content">
        <view v-if="historyDates.length > 0" class="date-list">
          <view 
            v-for="date in historyDates" 
            :key="date.arrangementId"
            class="date-item history">
            <view class="date-header">
              <view class="date-status" :class="getStatusClass(date.arrangementStatus)">
                {{ getStatusText(date.arrangementStatus) }}
              </view>
              <text class="date-time">{{ formatTime(date.updatedAt) }}</text>
            </view>
            
            <view class="date-content">
              <view class="date-info">
                <view class="info-row">
                  <text class="info-label">约会时间：</text>
                  <text class="info-value">{{ formatDateTime(date.dateTime) }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">约会地点：</text>
                  <text class="info-value">{{ date.dateLocation }}</text>
                </view>
              </view>
              
              <view class="date-actions">
                <button class="action-btn" @tap="viewDateDetail(date)">
                  查看详情
                </button>
                <button v-if="date.arrangementStatus === 3" class="action-btn primary" @tap="giveFeedback(date)">
                  评价约会
                </button>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty-state">
          <view class="empty-icon">📅</view>
          <text class="empty-text">暂无历史约会记录</text>
        </view>
      </view>
    </scroll-view>

    <!-- 调试信息 -->
    <view class="debug-info-fixed">
      showRejectReasonModal: {{ showRejectReasonModal }}
    </view>


  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: '待确认', count: 0 },
        { name: '已确认', count: 0 },
        { name: '历史记录', count: 0 }
      ],
      pendingDates: [],
      confirmedDates: [],
      historyDates: [],
      dateTypeNames: {
        1: '咖啡厅',
        2: '餐厅',
        3: '电影院',
        4: '其他'
      },

      // 拒绝原因相关
      showRejectReasonModal: false,
      currentRejectDate: null,
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
    // 是否可以确认拒绝
    canConfirmReject() {
      if (!this.selectedRejectReason) return false
      if (this.selectedRejectReason === 'other') {
        return this.customRejectReason.trim().length > 0
      }
      return true
    }
  },

  onLoad() {
    this.loadDateArrangements()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadDateArrangements()
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 显示快捷操作
    showQuickActions() {
      uni.showActionSheet({
        itemList: ['约会提醒', '约会中心'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              uni.navigateTo({
                url: '/pages/date/date-reminders'
              })
              break
            case 1:
              uni.navigateTo({
                url: '/pages/date/date-center'
              })
              break
          }
        }
      })
    },

    // 切换标签页
    switchTab(index) {
      this.currentTab = index
    },

    // 加载约会安排数据
    async loadDateArrangements() {
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
          const arrangements = response.data.data || []
          const currentUserId = uni.getStorageSync('userId')

          // 分类约会数据 - 根据当前用户的确认状态和整体状态
          this.pendingDates = arrangements.filter(item => {
            if (item.arrangementStatus !== 0) return false // 只有待确认状态的约会

            // 判断当前用户是否已确认
            const isUserA = item.userId == currentUserId
            const isUserB = item.targetUserId == currentUserId

            if (isUserA) {
              return item.userAConfirm === 0 // 用户A未确认
            } else if (isUserB) {
              return item.userBConfirm === 0 // 用户B未确认
            }
            return false
          })

          this.confirmedDates = arrangements.filter(item => item.arrangementStatus === 1)
          this.historyDates = arrangements.filter(item => item.arrangementStatus >= 2)

          // 更新标签页计数
          this.tabs[0].count = this.pendingDates.length
          this.tabs[1].count = this.confirmedDates.length
          this.tabs[2].count = this.historyDates.length
          
        } else {
          uni.showToast({
            title: response.data.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载约会安排失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 确认约会
    async confirmDate(arrangementId, confirm) {
      try {
        uni.showLoading({ title: '处理中...' })
        
        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/confirm/${arrangementId}?confirm=${confirm}`,
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

    // 查看约会详情
    viewDateDetail(date) {
      uni.navigateTo({
        url: `/pages/date/date-detail?arrangementId=${date.arrangementId}`
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

    // 给出约会反馈
    giveFeedback(date) {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${date.arrangementId}`
      })
    },

    // 跳转到反馈页面
    goToFeedback(arrangementId) {
      uni.navigateTo({
        url: `/pages/date/date-feedback?arrangementId=${arrangementId}`
      })
    },

    // 格式化时间
    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) {
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

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 0: return '⏳ 待确认'
        case 1: return '✅ 已确认'
        case 2: return '❌ 已取消'
        case 3: return '🎉 已完成'
        default: return '未知状态'
      }
    },

    // 判断当前用户是否已确认
    hasUserConfirmed(date) {
      const currentUserId = uni.getStorageSync('userId')
      const isUserA = String(date.userId) === String(currentUserId)
      const isUserB = String(date.targetUserId) === String(currentUserId)

      if (isUserA && date.userAConfirm !== 0) return true
      if (isUserB && date.userBConfirm !== 0) return true

      return false
    },

    // 获取用户确认状态的样式类
    getUserConfirmClass(date) {
      const currentUserId = uni.getStorageSync('userId')
      const isUserA = String(date.userId) === String(currentUserId)
      const isUserB = String(date.targetUserId) === String(currentUserId)

      let confirmStatus = 0
      if (isUserA) {
        confirmStatus = date.userAConfirm
      } else if (isUserB) {
        confirmStatus = date.userBConfirm
      }

      switch (confirmStatus) {
        case 1: return 'confirmed'
        case 2: return 'rejected'
        default: return 'pending'
      }
    },

    // 获取用户确认状态的图标
    getUserConfirmIcon(date) {
      const currentUserId = uni.getStorageSync('userId')
      const isUserA = String(date.userId) === String(currentUserId)
      const isUserB = String(date.targetUserId) === String(currentUserId)

      let confirmStatus = 0
      if (isUserA) {
        confirmStatus = date.userAConfirm
      } else if (isUserB) {
        confirmStatus = date.userBConfirm
      }

      switch (confirmStatus) {
        case 1: return '✅'
        case 2: return '❌'
        default: return '⏳'
      }
    },

    // 获取用户确认状态的文本
    getUserConfirmText(date) {
      const currentUserId = uni.getStorageSync('userId')
      const isUserA = String(date.userId) === String(currentUserId)
      const isUserB = String(date.targetUserId) === String(currentUserId)

      let confirmStatus = 0
      if (isUserA) {
        confirmStatus = date.userAConfirm
      } else if (isUserB) {
        confirmStatus = date.userBConfirm
      }

      switch (confirmStatus) {
        case 1: return '您已确认'
        case 2: return '您已拒绝'
        default: return '待确认'
      }
    },

    // 直接拒绝（使用ActionSheet选择原因）
    directReject(date) {
      const reasons = [
        '时间不合适',
        '地点不方便',
        '约会类型不喜欢',
        '个人原因',
        '对对方不感兴趣',
        '其他原因'
      ]

      uni.showActionSheet({
        itemList: reasons,
        success: (res) => {
          const selectedReason = reasons[res.tapIndex]

          if (selectedReason === '其他原因') {
            // 如果选择其他原因，弹出输入框
            uni.showModal({
              title: '拒绝原因',
              content: '请输入拒绝原因',
              editable: true,
              placeholderText: '请输入具体原因...',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  const customReason = modalRes.content || '其他原因'
                  this.executeReject(date, customReason)
                }
              }
            })
          } else {
            // 直接使用选择的原因
            this.executeReject(date, selectedReason)
          }
        }
      })
    },

    // 执行拒绝操作
    async executeReject(date, rejectReason) {
      try {
        uni.showLoading({ title: '处理中...' })

        // 构建请求URL
        let url = `${config.getBaseUrl()}/user/date/confirm/${date.arrangementId}?confirm=2`
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

          // 刷新数据
          this.loadDateArrangements()
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
    },

    // 隐藏拒绝原因模态框
    hideRejectModal() {
      this.showRejectReasonModal = false
      this.currentRejectDate = null
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
      if (!this.canConfirmReject || !this.currentRejectDate) return

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
        let url = `${config.getBaseUrl()}/user/date/confirm/${this.currentRejectDate.arrangementId}?confirm=2`
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
.date-management-container {
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

/* 标签页 */
.tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
}

.tab-item {
  flex: 1;
  padding: 30rpx 20rpx;
  text-align: center;
  position: relative;
  border-bottom: 4rpx solid transparent;
}

.tab-item.active {
  border-bottom-color: white;
}

.tab-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.7);
}

.tab-item.active .tab-text {
  color: white;
  font-weight: bold;
}

.tab-badge {
  position: absolute;
  top: 15rpx;
  right: 20rpx;
  background: #ff4757;
  color: white;
  font-size: 20rpx;
  font-weight: bold;
  padding: 4rpx 8rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

/* 内容区域 */
.content-area {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}

/* 约会列表 */
.date-list {
  padding-bottom: 20rpx;
}

.date-item {
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
  font-size: 24rpx;
  font-weight: bold;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
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

.date-time {
  font-size: 24rpx;
  color: #a0aec0;
}

.date-content {
  padding: 30rpx;
}

.date-info {
  margin-bottom: 30rpx;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-size: 26rpx;
  color: #718096;
  width: 140rpx;
  flex-shrink: 0;
}

.info-value {
  font-size: 26rpx;
  color: #2d3748;
  flex: 1;
}

.date-actions {
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

.action-btn.feedback-btn {
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

/* 确认状态显示样式 */
.confirmed-status {
  padding: 20rpx;
  text-align: center;
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
}

.status-indicator.confirmed {
  background: #d4edda;
  color: #155724;
  border: 1rpx solid #c3e6cb;
}

.status-indicator.rejected {
  background: #f8d7da;
  color: #721c24;
  border: 1rpx solid #f5c6cb;
}

.status-indicator.pending {
  background: #fff3cd;
  color: #856404;
  border: 1rpx solid #ffeaa7;
}

.status-icon {
  font-size: 20rpx;
}

.status-text {
  font-size: 24rpx;
}
</style>
