<template>
  <view class="arrange-dates-container">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">约会安排</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <!-- 顶部介绍 -->
    <view class="top-intro">
      <view class="intro-subtitle">为成功牵线的用户安排约会</view>
    </view>

    <!-- 标签切换 -->
    <view class="tab-container">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'available' }"
        @click="switchTab('available')">
        待安排约会
        <view class="tab-badge" v-if="availableCount > 0">{{ availableCount }}</view>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'arranged' }"
        @click="switchTab('arranged')">
        已安排约会
      </view>
    </view>

    <!-- 待安排约会列表 -->
    <view v-if="currentTab === 'available'" class="content-container">
      <view v-if="isLoading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>
      
      <view v-else-if="availableRequests.length === 0" class="empty-container">
        <text class="empty-text">暂无待安排约会的申请</text>
      </view>
      
      <view v-else class="request-list">
        <view 
          v-for="item in availableRequests" 
          :key="item.request.requestId"
          class="request-card">
          
          <!-- 顶部装饰条 -->
          <view class="card-top-decoration"></view>

          <!-- 用户信息 -->
          <view class="users-section">
            <view class="user-profile">
              <view class="user-card">
                <view class="user-header">
                  <view class="role-badge applicant">申请者</view>
                </view>
                <view class="user-avatar-section" @click="viewUserProfile(item.applicantUser?.userId || item.request?.userId)">
                  <image class="user-avatar" :src="getUserAvatar(item.applicantUser)" mode="aspectFill"></image>
                </view>
                <view class="user-details">
                  <text class="user-name">{{ getUserName(item.applicantUser) }}</text>
                  <text class="user-desc">发起牵线申请</text>
                </view>
              </view>
            </view>

            <view class="connection-animation">
              <view class="connection-line"></view>
              <view class="heart-pulse">💕</view>
              <view class="connection-line"></view>
            </view>

            <view class="user-profile">
              <view class="user-card">
                <view class="user-header">
                  <view class="role-badge target">目标用户</view>
                </view>
                <view class="user-avatar-section" @click="viewUserProfile(item.targetUser?.userId || item.request?.targetUserId)">
                  <image class="user-avatar" :src="getUserAvatar(item.targetUser)" mode="aspectFill"></image>
                </view>
                <view class="user-details">
                  <text class="user-name">{{ getUserName(item.targetUser) }}</text>
                  <text class="user-desc">已同意申请</text>
                </view>
              </view>
            </view>
          </view>



          <!-- 操作区域 -->
          <view class="action-section">
            <button
              v-if="!item.hasDateArrangement"
              class="arrange-button"
              @click="showArrangeModal(item.request)">
              <view class="btn-icon">📅</view>
              <text class="btn-text">安排约会</text>
              <view class="btn-arrow">→</view>
            </button>
            <view v-else class="arranged-indicator">
              <view class="indicator-icon">✅</view>
              <text class="indicator-text">约会已安排</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 已安排约会列表 -->
    <view v-if="currentTab === 'arranged'" class="content-container">
      <view v-if="isLoading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>
      
      <view v-else-if="arrangedDates.length === 0" class="empty-container">
        <text class="empty-text">暂无已安排的约会</text>
      </view>
      
      <view v-else class="date-list">
        <view 
          v-for="item in arrangedDates" 
          :key="item.dateArrangement.arrangementId"
          class="date-card">
          
          <!-- 顶部装饰条 -->
          <view class="card-top-decoration arranged"></view>

          <!-- 约会标题 -->
          <view class="date-header-section">
            <view class="date-title-area">
              <view class="title-icon">💑</view>
              <text class="date-title">约会安排</text>
            </view>
            <view class="date-status-badge" :class="getDateStatusClass(item.dateArrangement.arrangementStatus)">
              {{ formatDateStatus(item.dateArrangement.arrangementStatus) }}
            </view>
          </view>

          <!-- 用户信息 -->
          <view class="couple-section">
            <view class="couple-user" @click="viewUserProfile(item.userA?.userId)">
              <image class="couple-avatar" :src="getUserAvatar(item.userA)" mode="aspectFill"></image>
              <text class="couple-name">{{ getUserName(item.userA) }}</text>
            </view>
            <view class="couple-connector">
              <view class="connector-line"></view>
              <text class="connector-text">约会</text>
              <view class="connector-line"></view>
            </view>
            <view class="couple-user" @click="viewUserProfile(item.userB?.userId)">
              <image class="couple-avatar" :src="getUserAvatar(item.userB)" mode="aspectFill"></image>
              <text class="couple-name">{{ getUserName(item.userB) }}</text>
            </view>
          </view>

          <!-- 约会详情卡片 -->
          <view class="date-info-cards">
            <view class="info-card-row">
              <view class="info-card">
                <view class="card-icon">🕐</view>
                <view class="card-content">
                  <text class="card-label">约会时间</text>
                  <text class="card-value">{{ formatDateTime(item.dateArrangement.dateTime) }}</text>
                </view>
              </view>
              <view class="info-card">
                <view class="card-icon">📍</view>
                <view class="card-content">
                  <text class="card-label">约会地点</text>
                  <text class="card-value">{{ item.dateArrangement.dateLocation }}</text>
                </view>
              </view>
            </view>

            <view class="info-card-row">
              <view class="info-card">
                <view class="card-icon">🎯</view>
                <view class="card-content">
                  <text class="card-label">约会类型</text>
                  <text class="card-value">{{ formatDateType(item.dateArrangement.dateType) }}</text>
                </view>
              </view>
              <view class="info-card" v-if="item.dateArrangement.datePlan">
                <view class="card-icon">📝</view>
                <view class="card-content">
                  <text class="card-label">约会计划</text>
                  <text class="card-value">{{ item.dateArrangement.datePlan }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 确认状态 -->
          <view class="confirm-section">
            <view class="confirm-title">
              <view class="title-icon">✅</view>
              <text class="title-text">确认状态</text>
            </view>
            <view class="confirm-list">
              <view class="confirm-item">
                <view class="confirm-user">
                  <image class="confirm-avatar" :src="getUserAvatar(item.userA)" mode="aspectFill"></image>
                  <text class="confirm-name">{{ getUserName(item.userA) }}</text>
                </view>
                <view class="confirm-status" :class="getConfirmClass(item.dateArrangement.userAConfirm)">
                  {{ formatConfirmStatus(item.dateArrangement.userAConfirm) }}
                </view>
              </view>
              <view class="confirm-item">
                <view class="confirm-user">
                  <image class="confirm-avatar" :src="getUserAvatar(item.userB)" mode="aspectFill"></image>
                  <text class="confirm-name">{{ getUserName(item.userB) }}</text>
                </view>
                <view class="confirm-status" :class="getConfirmClass(item.dateArrangement.userBConfirm)">
                  {{ formatConfirmStatus(item.dateArrangement.userBConfirm) }}
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 安排约会弹窗 -->
    <view v-if="showArrangeDialog" class="arrange-modal-overlay" @click="closeArrangeModal">
      <view class="arrange-modal-content" @click.stop>
        <!-- 弹窗头部 -->
        <view class="arrange-modal-header">
          <view class="header-decoration"></view>
          <view class="header-content">
            <view class="header-icon">💕</view>
            <text class="header-title">安排约会</text>
            <text class="header-subtitle">为这对有缘人安排美好的约会</text>
          </view>
          <view class="header-close" @click="closeArrangeModal">
            <text class="close-icon">×</text>
          </view>
        </view>

        <!-- 弹窗内容 -->
        <view class="arrange-modal-body">
          <!-- 约会时间 -->
          <view class="arrange-form-section">
            <view class="section-header">
              <view class="section-icon">🕐</view>
              <text class="section-title">约会时间</text>
            </view>
            <picker
              mode="multiSelector"
              :value="dateTimeIndex"
              :range="dateTimeRange"
              @change="onDateTimeChange">
              <view class="arrange-picker-field">
                <view class="picker-content">
                  <text class="picker-text" :class="{ placeholder: !selectedDateTime }">
                    {{ selectedDateTime || '请选择约会时间' }}
                  </text>
                  <view class="picker-arrow">▼</view>
                </view>
              </view>
            </picker>
          </view>

          <!-- 约会地点 -->
          <view class="arrange-form-section">
            <view class="section-header">
              <view class="section-icon">📍</view>
              <text class="section-title">约会地点</text>
            </view>
            <input
              class="direct-input"
              v-model="arrangeForm.dateLocation"
              placeholder="请输入约会地点，如：星巴克咖啡厅"
              maxlength="100"
              type="text"
              confirm-type="done"
              cursor-spacing="0"
              selection-start="-1"
              selection-end="-1" />
          </view>

          <!-- 约会类型 -->
          <view class="arrange-form-section">
            <view class="section-header">
              <view class="section-icon">🎯</view>
              <text class="section-title">约会类型</text>
            </view>
            <picker
              :value="arrangeForm.dateType - 1"
              :range="dateTypeOptions"
              @change="onDateTypeChange">
              <view class="arrange-picker-field">
                <view class="picker-content">
                  <text class="picker-text">
                    {{ dateTypeOptions[arrangeForm.dateType - 1] || '咖啡厅' }}
                  </text>
                  <view class="picker-arrow">▼</view>
                </view>
              </view>
            </picker>
          </view>

          <!-- 约会计划 -->
          <view class="arrange-form-section">
            <view class="section-header">
              <view class="section-icon">📝</view>
              <text class="section-title">约会计划</text>
            </view>
            <view class="arrange-textarea-field">
              <textarea
                class="arrange-textarea"
                v-model="arrangeForm.datePlan"
                placeholder="请描述约会的具体安排和建议，如：建议先在咖啡厅聊天，然后可以去附近的公园散步..."
                maxlength="500" />
              <view class="textarea-counter">{{ arrangeForm.datePlan.length }}/500</view>
            </view>
          </view>
        </view>

        <!-- 弹窗底部 -->
        <view class="arrange-modal-footer">
          <button class="arrange-cancel-btn" @click="closeArrangeModal">
            <text class="btn-text">取消</text>
          </button>
          <button class="arrange-confirm-btn" @click="confirmArrange">
            <view class="btn-icon">💕</view>
            <text class="btn-text">确认安排</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      currentTab: 'available',
      isLoading: false,
      availableRequests: [],
      arrangedDates: [],
      availableCount: 0,
      
      // 安排约会弹窗
      showArrangeDialog: false,
      currentRequest: null,
      arrangeForm: {
        requestId: null,
        dateTime: null,
        dateLocation: '',
        dateType: 1,
        datePlan: ''
      },
      
      // 时间选择器
      dateTimeIndex: [0, 0],
      dateTimeRange: [[], []],
      selectedDateTime: '',
      
      // 约会类型选项
      dateTypeOptions: ['咖啡厅', '餐厅', '电影院', '其他']
    }
  },

  onLoad() {
    this.initDateTimeRange()
    this.loadData()
  },

  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack({
        delta: 1
      })
    },

    // 切换标签
    switchTab(tab) {
      this.currentTab = tab
      this.loadData()
    },

    // 加载数据
    async loadData() {
      if (this.currentTab === 'available') {
        await this.loadAvailableRequests()
      } else {
        await this.loadArrangedDates()
      }
    },

    // 加载待安排约会的申请
    async loadAvailableRequests() {
      try {
        this.isLoading = true
        const token = uni.getStorageSync('token') || ''
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/date/available-requests',
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
          this.availableRequests = result.data.records || []
          this.availableCount = this.availableRequests.filter(item => !item.hasDateArrangement).length
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载待安排约会失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
      }
    },

    // 加载已安排约会
    async loadArrangedDates() {
      try {
        this.isLoading = true
        const token = uni.getStorageSync('token') || ''
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/date/list',
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
          this.arrangedDates = result.data.records || []
        } else {
          uni.showToast({
            title: result.message || '加载失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载已安排约会失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
      }
    },

    // 显示安排约会弹窗
    showArrangeModal(request) {
      this.currentRequest = request
      this.arrangeForm = {
        requestId: request.requestId,
        dateTime: null,
        dateLocation: '',
        dateType: 1,
        datePlan: ''
      }
      this.selectedDateTime = ''
      this.showArrangeDialog = true
    },

    // 关闭安排约会弹窗
    closeArrangeModal() {
      this.showArrangeDialog = false
      this.currentRequest = null
    },

    // 初始化时间选择器
    initDateTimeRange() {
      const dates = []
      const times = []
      
      // 生成未来30天的日期
      for (let i = 1; i <= 30; i++) {
        const date = new Date()
        date.setDate(date.getDate() + i)
        const month = (date.getMonth() + 1).toString().padStart(2, '0')
        const day = date.getDate().toString().padStart(2, '0')
        dates.push(`${month}-${day}`)
      }
      
      // 生成时间选项（9:00-21:00，每小时一个选项）
      for (let hour = 9; hour <= 21; hour++) {
        times.push(`${hour.toString().padStart(2, '0')}:00`)
      }
      
      this.dateTimeRange = [dates, times]
    },

    // 时间选择器变化
    onDateTimeChange(e) {
      this.dateTimeIndex = e.detail.value
      const dateIndex = e.detail.value[0]
      const timeIndex = e.detail.value[1]
      
      if (dateIndex >= 0 && timeIndex >= 0) {
        const selectedDate = this.dateTimeRange[0][dateIndex]
        const selectedTime = this.dateTimeRange[1][timeIndex]
        this.selectedDateTime = `${selectedDate} ${selectedTime}`
        
        // 构造完整的日期时间
        const year = new Date().getFullYear()
        const dateTimeStr = `${year}-${selectedDate} ${selectedTime}:00`
        this.arrangeForm.dateTime = new Date(dateTimeStr)
      }
    },

    // 约会类型选择器变化
    onDateTypeChange(e) {
      this.arrangeForm.dateType = parseInt(e.detail.value) + 1
    },

    // 确认安排约会
    async confirmArrange() {
      // 验证表单
      if (!this.arrangeForm.dateTime) {
        uni.showToast({
          title: '请选择约会时间',
          icon: 'none'
        })
        return
      }
      
      if (!this.arrangeForm.dateLocation.trim()) {
        uni.showToast({
          title: '请输入约会地点',
          icon: 'none'
        })
        return
      }

      try {
        const token = uni.getStorageSync('token') || ''
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/date/arrange',
            method: 'POST',
            data: {
              requestId: this.arrangeForm.requestId,
              dateTime: this.arrangeForm.dateTime.toISOString(),
              dateLocation: this.arrangeForm.dateLocation.trim(),
              dateType: this.arrangeForm.dateType,
              datePlan: this.arrangeForm.datePlan.trim()
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
          uni.showToast({
            title: '约会安排成功',
            icon: 'success'
          })
          
          this.closeArrangeModal()
          this.loadData()
        } else {
          uni.showToast({
            title: result.message || '安排失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('安排约会失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },

    // 获取用户头像
    getUserAvatar(user) {
      return user?.avatarUrl || '/static/default-avatar.png'
    },

    // 获取用户姓名
    getUserName(user) {
      return user?.nickname || user?.realName || `用户${user?.userId}`
    },

    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      return `${month}-${day} ${hour}:${minute}`
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    },

    // 格式化约会类型
    formatDateType(type) {
      const typeMap = {
        1: '咖啡厅',
        2: '餐厅', 
        3: '电影院',
        4: '其他'
      }
      return typeMap[type] || '未知'
    },

    // 格式化约会状态
    formatDateStatus(status) {
      const statusMap = {
        0: '待确认',
        1: '已确认',
        2: '已取消',
        3: '已完成'
      }
      return statusMap[status] || '未知'
    },

    // 获取约会状态样式类
    getDateStatusClass(status) {
      const classMap = {
        0: 'status-pending',
        1: 'status-confirmed',
        2: 'status-cancelled',
        3: 'status-completed'
      }
      return classMap[status] || ''
    },

    // 格式化确认状态
    formatConfirmStatus(status) {
      const statusMap = {
        0: '待确认',
        1: '已确认',
        2: '已拒绝'
      }
      return statusMap[status] || '未知'
    },

    // 获取确认状态样式类
    getConfirmClass(status) {
      const classMap = {
        0: 'confirm-pending',
        1: 'confirm-yes',
        2: 'confirm-no'
      }
      return classMap[status] || ''
    },

    // 查看用户详情
    viewUserProfile(userId) {
      if (!userId) {
        uni.showToast({
          title: '用户信息不完整',
          icon: 'error'
        })
        return
      }

      uni.navigateTo({
        url: `/pages/user/user-detail?userId=${userId}`
      })
    },



  }
}
</script>

<style scoped>
/* 确保页面占满全屏 */
page {
  width: 100%;
  height: 100%;
}

.arrange-dates-container {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 20rpx;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 导航栏样式 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  padding-top: 60rpx; /* 为状态栏留出空间 */
  background: rgba(255, 255, 255, 0.1);
}

.nav-left, .nav-right {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
}

/* 顶部介绍样式 */
.top-intro {
  padding: 20rpx 30rpx 30rpx;
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
}

.intro-subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}



.tab-container {
  display: flex;
  background: white;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 8rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  width: calc(100% - 40rpx);
  box-sizing: border-box;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
  position: relative;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.tab-badge {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background: #ff4757;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 8rpx;
  border-radius: 10rpx;
  min-width: 20rpx;
  height: 20rpx;
  line-height: 20rpx;
  text-align: center;
}

.content-container {
  width: 100%;
  padding: 0 20rpx;
  box-sizing: border-box;
}

.loading-container, .empty-container {
  text-align: center;
  padding: 100rpx 0;
}

.loading-text, .empty-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 28rpx;
}

.request-card, .date-card {
  width: 100%;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
}

.users-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.user-item {
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
}

.user-role {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.heart-icon {
  font-size: 40rpx;
  margin: 0 20rpx;
}

.vs-text {
  font-size: 24rpx;
  color: #999;
  margin: 0 20rpx;
}

.request-info {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.request-message {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  display: block;
  margin-bottom: 10rpx;
}

.request-time {
  font-size: 22rpx;
  color: #999;
}

.action-buttons {
  text-align: center;
}

.arrange-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 20rpx 40rpx;
  border-radius: 25rpx;
  font-size: 26rpx;
  font-weight: bold;
}

.arranged-status {
  padding: 20rpx;
}

.status-text {
  color: #27ae60;
  font-size: 26rpx;
  font-weight: bold;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.date-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.date-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.status-pending {
  background: #ffeaa7;
  color: #d63031;
}

.status-confirmed {
  background: #55efc4;
  color: #00b894;
}

.status-cancelled {
  background: #fab1a0;
  color: #e17055;
}

.status-completed {
  background: #a29bfe;
  color: #6c5ce7;
}

.date-details {
  margin: 20rpx 0;
}

.detail-item {
  display: flex;
  margin-bottom: 10rpx;
}

.detail-label {
  font-size: 24rpx;
  color: #666;
  width: 100rpx;
  flex-shrink: 0;
}

.detail-value {
  font-size: 24rpx;
  color: #333;
  flex: 1;
}

.confirm-status {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.confirm-item:last-child {
  margin-bottom: 0;
}

.confirm-label {
  font-size: 24rpx;
  color: #666;
}

.confirm-value {
  font-size: 24rpx;
  font-weight: bold;
}

.confirm-pending {
  color: #f39c12;
}

.confirm-yes {
  color: #27ae60;
}

.confirm-no {
  color: #e74c3c;
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
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16rpx;
  width: 90%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 30rpx;
  max-height: 60vh;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 26rpx;
  color: #333;
  margin-bottom: 10rpx;
  font-weight: bold;
}

.form-input, .form-textarea, .picker-input {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #333;
  box-sizing: border-box;
}

.form-textarea {
  height: 120rpx;
  resize: none;
}

.picker-input {
  background: #f8f9fa;
  color: #666;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  padding: 30rpx;
  border-top: 1rpx solid #eee;
  gap: 20rpx;
}

.cancel-btn, .confirm-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  font-weight: bold;
  border: none;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 新的美化样式 */
/* 顶部装饰条 */
.card-top-decoration {
  height: 8rpx;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  width: 100%;
}

.card-top-decoration.arranged {
  background: linear-gradient(90deg, #48bb78 0%, #38a169 100%);
}

/* 用户信息区域 */
.users-section {
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-profile {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  min-width: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-header {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 15rpx;
}

.role-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  color: white;
  font-weight: bold;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.role-badge.applicant {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.role-badge.target {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.user-avatar-section {
  margin-bottom: 15rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar-section:active {
  transform: scale(0.95);
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  border: 3rpx solid #f8f9fa;
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user-avatar-section:active .user-avatar {
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.user-details {
  text-align: center;
}

.user-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 4rpx;
  display: block;
}

.user-desc {
  font-size: 20rpx;
  color: #718096;
}

/* 连接动画 */
.connection-animation {
  display: flex;
  align-items: center;
  margin: 0 20rpx;
}

.connection-line {
  width: 40rpx;
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.heart-pulse {
  font-size: 36rpx;
  margin: 0 10rpx;
  animation: heartbeat 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}





/* 操作区域 */
.action-section {
  padding: 0 30rpx 30rpx;
}

.arrange-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 16rpx;
  padding: 20rpx;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 6rpx 20rpx rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.arrange-button:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.btn-icon {
  font-size: 28rpx;
}

.btn-text {
  font-size: 28rpx;
  font-weight: bold;
  color: white;
}

.btn-arrow {
  font-size: 24rpx;
  color: white;
  font-weight: bold;
}

.arranged-indicator {
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: 2rpx solid #48bb78;
}

.indicator-icon {
  font-size: 28rpx;
}

.indicator-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #22543d;
}

/* 已安排约会卡片样式 */
/* 约会标题区域 */
.date-header-section {
  padding: 30rpx 30rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.date-title-area {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.title-icon {
  font-size: 32rpx;
}

.date-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
}

.date-status-badge {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  color: white;
}

.date-status-badge.status-pending {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
}

.date-status-badge.status-confirmed {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.date-status-badge.status-cancelled {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
}

.date-status-badge.status-completed {
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
}

/* 情侣区域 */
.couple-section {
  padding: 0 30rpx 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.couple-user {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 10rpx;
  border-radius: 12rpx;
}

.couple-user:active {
  transform: scale(0.95);
  background: rgba(102, 126, 234, 0.05);
}

.couple-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  border: 3rpx solid #f8f9fa;
  margin-bottom: 10rpx;
  box-shadow: 0 3rpx 10rpx rgba(0, 0, 0, 0.1);
}

.couple-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
  text-align: center;
}

.couple-connector {
  display: flex;
  align-items: center;
  margin: 0 20rpx;
}

.connector-line {
  width: 30rpx;
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, #e2e8f0 50%, transparent 100%);
}

.connector-text {
  font-size: 22rpx;
  color: #718096;
  margin: 0 10rpx;
  font-weight: bold;
}

/* 约会信息卡片 */
.date-info-cards {
  padding: 0 30rpx 20rpx;
}

.info-card-row {
  display: flex;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.info-card-row:last-child {
  margin-bottom: 0;
}

.info-card {
  flex: 1;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 12rpx;
  padding: 20rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.card-icon {
  font-size: 28rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(102, 126, 234, 0.15);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-label {
  font-size: 22rpx;
  color: #718096;
  font-weight: 600;
  display: block;
  margin-bottom: 4rpx;
}

.card-value {
  font-size: 24rpx;
  color: #2d3748;
  font-weight: bold;
  line-height: 1.4;
  word-break: break-all;
}

/* 确认状态区域 */
.confirm-section {
  padding: 0 30rpx 30rpx;
}

.confirm-title {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.title-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
}

.confirm-list {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  border-radius: 16rpx;
  padding: 20rpx;
  border: 2rpx solid rgba(226, 232, 240, 0.5);
}

.confirm-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.5);
}

.confirm-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.confirm-user {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.confirm-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 25rpx;
  border: 2rpx solid #f8f9fa;
}

.confirm-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #2d3748;
}

.confirm-status {
  padding: 6rpx 12rpx;
  border-radius: 15rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.confirm-status.confirm-pending {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
  border: 1rpx solid rgba(245, 158, 11, 0.3);
}

.confirm-status.confirm-yes {
  background: rgba(72, 187, 120, 0.1);
  color: #38a169;
  border: 1rpx solid rgba(72, 187, 120, 0.3);
}

.confirm-status.confirm-no {
  background: rgba(239, 68, 68, 0.1);
  color: #e53e3e;
  border: 1rpx solid rgba(239, 68, 68, 0.3);
}

/* 安排约会弹窗样式 */
.arrange-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(4rpx);
  padding: 40rpx 20rpx;
  box-sizing: border-box;
}

.arrange-modal-content {
  width: 90%;
  max-width: 600rpx;
  background: white;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
  display: flex;
  flex-direction: column;
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

/* 弹窗头部 */
.arrange-modal-header {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx 30rpx;
  color: white;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8rpx;
  background: linear-gradient(90deg, #ff6b6b 0%, #feca57 50%, #48dbfb 100%);
}

.header-content {
  text-align: center;
}

.header-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.header-subtitle {
  font-size: 24rpx;
  opacity: 0.9;
}

.header-close {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  width: 60rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.header-close:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.9);
}

.close-icon {
  font-size: 32rpx;
  color: white;
  font-weight: bold;
}

/* 弹窗内容 */
.arrange-modal-body {
  padding: 30rpx;
}

.arrange-form-section {
  margin-bottom: 40rpx;
}

.arrange-form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.section-icon {
  font-size: 28rpx;
  width: 50rpx;
  height: 50rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 25rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
}

/* 选择器字段 */
.arrange-picker-field {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 16rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.arrange-picker-field:active {
  border-color: rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, #f0f4ff 0%, #e8f0ff 100%);
}

.picker-content {
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-text {
  font-size: 28rpx;
  color: #2d3748;
  flex: 1;
}

.picker-text.placeholder {
  color: #a0aec0;
}

.picker-arrow {
  font-size: 20rpx;
  color: #667eea;
  font-weight: bold;
}



/* 直接输入框 */
.direct-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #2d3748;
  background: #ffffff;
  border: 2rpx solid #e2e8f0;
  border-radius: 16rpx;
  box-sizing: border-box;
}

.direct-input::placeholder {
  color: #a0aec0;
}

/* 文本域字段 */
.arrange-textarea-field {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%);
  border-radius: 16rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  position: relative;
}

.arrange-textarea-field:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
}

.arrange-textarea {
  width: 100%;
  padding: 24rpx;
  font-size: 28rpx;
  color: #2d3748;
  background: transparent;
  border: none;
  outline: none;
  min-height: 200rpx;
  line-height: 1.6;
  resize: none;
}

.arrange-textarea::placeholder {
  color: #a0aec0;
}

.textarea-counter {
  position: absolute;
  bottom: 12rpx;
  right: 16rpx;
  font-size: 22rpx;
  color: #a0aec0;
  background: rgba(255, 255, 255, 0.8);
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
}

/* 弹窗底部 */
.arrange-modal-footer {
  padding: 30rpx;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  display: flex;
  gap: 20rpx;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
}

.arrange-cancel-btn {
  flex: 1;
  background: white;
  border: 2rpx solid #d1d5db;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: #374151 !important;
  font-weight: bold;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrange-cancel-btn .btn-text {
  color: #374151 !important;
  font-size: 28rpx;
  font-weight: bold;
}

.arrange-cancel-btn:active {
  background: #f7fafc;
  border-color: #cbd5e0;
  transform: scale(0.98);
}

.arrange-confirm-btn {
  flex: 2;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 28rpx;
  color: white;
  font-weight: bold;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
}

.arrange-confirm-btn:active {
  transform: scale(0.98);
  box-shadow: 0 6rpx 20rpx rgba(102, 126, 234, 0.3);
}

.btn-icon {
  font-size: 24rpx;
}

.btn-text {
  font-size: 28rpx;
  font-weight: bold;
}

/* 响应式适配 */
@media screen and (max-width: 750rpx) {
  .arrange-modal-content {
    width: 95%;
    max-height: 90vh;
  }

  .arrange-modal-header {
    padding: 30rpx 20rpx 20rpx;
  }

  .arrange-modal-body {
    padding: 20rpx;
  }

  .arrange-modal-footer {
    padding: 20rpx;
  }

  .arrange-form-section {
    margin-bottom: 30rpx;
  }

  .section-title {
    font-size: 26rpx;
  }

  .picker-text, .arrange-input, .arrange-textarea {
    font-size: 26rpx;
  }

  .header-title {
    font-size: 32rpx;
  }

  .header-subtitle {
    font-size: 22rpx;
  }
}
</style>
