<template>
  <view class="my-requests-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">我的牵线申请</view>
      <view class="nav-right"></view>
    </view>

    <!-- 申请列表 -->
    <view class="requests-container" v-if="!isLoading">
      <view class="request-item" v-for="request in requestList" :key="request.requestId">
        <view class="request-header">
          <view class="target-user">
            <image class="user-avatar" :src="request.targetUser?.avatarUrl || '/static/default-avatar.png'" mode="aspectFill"></image>
            <view class="user-info">
              <text class="user-name">{{ request.targetUser?.nickname || '用户' + request.targetUserId }}</text>
              <text class="request-time">{{ formatTime(request.createdAt) }}</text>
            </view>
          </view>
          <view class="status-badge" :style="{ backgroundColor: getStatusColor(request.requestStatus) }">
            {{ formatStatus(request.requestStatus) }}
          </view>
        </view>
        
        <view class="request-message">
          <text class="message-label">申请留言：</text>
          <text class="message-content">{{ request.requestMessage }}</text>
        </view>

        <!-- 红娘信息 -->
        <view class="matchmaker-info" v-if="request.matchmakerId">
          <view class="matchmaker-header">
            <image class="matchmaker-avatar" :src="request.matchmakerInfo?.avatarUrl || '/static/default-avatar.png'" mode="aspectFill"></image>
            <view class="matchmaker-details">
              <text class="matchmaker-name">{{ request.matchmakerInfo?.nickname || '红娘' + request.matchmakerId }}</text>
              <text class="matchmaker-desc">专业红娘，为您提供贴心服务</text>
            </view>
            <button class="view-profile-btn" @click="viewMatchmakerProfile(request.matchmakerId)">查看详情</button>
          </view>
        </view>
        
        <view class="request-actions" v-if="request.requestStatus === 0">
          <button class="cancel-btn" @click="cancelRequest(request.requestId)">取消申请</button>
        </view>
        
        <view class="reject-reason" v-if="request.requestStatus === 2 && request.rejectReason">
          <text class="reason-label">拒绝原因：</text>
          <text class="reason-content">{{ request.rejectReason }}</text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="requestList.length === 0">
        <image class="empty-icon" src="/static/icons/empty.png" mode="aspectFit"></image>
        <text class="empty-text">暂无牵线申请记录</text>
        <text class="empty-desc">快去发现心仪的人，发起牵线申请吧！</text>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script>
import { getMyMatchmakingRequests, cancelMatchmakingRequest, formatRequestStatus, getRequestStatusColor } from '@/api/matchmaker'
import { getUserDetail } from '@/api/user/detail'

export default {
  data() {
    return {
      requestList: [],
      isLoading: false
    }
  },
  
  onLoad() {
    this.loadRequestList()
  },
  
  onPullDownRefresh() {
    this.loadRequestList().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  
  methods: {
    // 加载申请列表
    async loadRequestList() {
      this.isLoading = true
      
      try {
        const result = await getMyMatchmakingRequests()
        
        if (result.code === 200) {
          this.requestList = result.data || []
          // 加载目标用户和红娘的详细信息
          await this.loadTargetUsersInfo()
          await this.loadMatchmakersInfo()
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
    
    // 加载目标用户信息
    async loadTargetUsersInfo() {
      // 为每个申请记录获取目标用户信息
      for (let request of this.requestList) {
        try {
          const result = await getUserDetail(request.targetUserId)

          if (result.code === 200 && result.data && result.data.records) {
            let targetUser = result.data.records.find(user => user.userId == request.targetUserId)

            if (!targetUser && result.data.records.length > 0) {
              targetUser = result.data.records[0]
            }

            if (targetUser) {
              // 从userProfile中获取年龄和地区信息
              const profile = targetUser.userProfile || {}
              const age = profile.age || '未知'
              const location = profile.workCity || profile.hometown || '未知'

              request.targetUser = {
                userId: targetUser.userId,
                nickname: targetUser.nickname || '用户' + targetUser.userId,
                avatarUrl: targetUser.avatarUrl || '/static/default-avatar.png',
                age: age,
                location: location
              }
            } else {
              // 使用默认信息
              request.targetUser = {
                userId: request.targetUserId,
                nickname: '用户' + request.targetUserId,
                avatarUrl: '/static/default-avatar.png'
              }
            }
          } else {
            // 使用默认信息
            request.targetUser = {
              userId: request.targetUserId,
              nickname: '用户' + request.targetUserId,
              avatarUrl: '/static/default-avatar.png'
            }
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 使用默认信息
          request.targetUser = {
            userId: request.targetUserId,
            nickname: '用户' + request.targetUserId,
            avatarUrl: '/static/default-avatar.png'
          }
        }
      }
    },
    
    // 取消申请
    async cancelRequest(requestId) {
      const confirmResult = await this.showConfirmDialog('确定要取消这个牵线申请吗？')
      if (!confirmResult) {
        return
      }
      
      try {
        const result = await cancelMatchmakingRequest(requestId)
        
        if (result.code === 200) {
          uni.showToast({
            title: '申请已取消',
            icon: 'success'
          })
          
          // 更新列表中的状态
          const request = this.requestList.find(r => r.requestId === requestId)
          if (request) {
            request.requestStatus = 2
            request.rejectReason = '用户主动取消'
          }
        } else {
          uni.showToast({
            title: result.message || '取消失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('取消申请失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      }
    },
    
    // 显示确认对话框
    showConfirmDialog(content) {
      return new Promise((resolve) => {
        uni.showModal({
          title: '确认操作',
          content: content,
          confirmText: '确定',
          cancelText: '取消',
          success: (res) => {
            resolve(res.confirm)
          },
          fail: () => {
            resolve(false)
          }
        })
      })
    },

    // 加载红娘信息
    async loadMatchmakersInfo() {
      for (let request of this.requestList) {
        if (request.matchmakerId && !request.matchmakerInfo) {
          try {
            const matchmakerData = await this.getMatchmakerUserId(request.matchmakerId)
            if (matchmakerData) {
              // 获取红娘的用户信息
              const userInfo = await this.getUserInfo(matchmakerData)
              request.matchmakerInfo = userInfo
            }
          } catch (error) {
            console.error('加载红娘信息失败:', error)
          }
        }
      }
      // 触发页面更新
      this.$forceUpdate()
    },

    // 获取用户信息（使用相亲广场API）
    async getUserInfo(userId) {
      try {
        const token = uni.getStorageSync('token') || ''

        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: `http://localhost:9001/loveSquare/list`,
            method: 'POST',
            data: {
              pageNum: 1,
              pageSize: 1,
              userId: userId // 按用户ID筛选
            },
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })

        if (result.code === 200 && result.data && result.data.records && result.data.records.length > 0) {
          return result.data.records[0] // 返回第一个用户信息
        }
        return null
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return null
      }
    },

    // 获取红娘头像
    getMatchmakerAvatar(matchmakerId) {
      return '/static/matchmaker-avatar.png' // 可以根据matchmakerId获取真实头像
    },

    // 查看红娘详情
    viewMatchmakerProfile(matchmakerId) {
      // 需要先获取红娘的用户ID，然后跳转到个人主页
      this.getMatchmakerUserId(matchmakerId).then(userId => {
        if (userId) {
          uni.navigateTo({
            url: `/pages/user/user-detail?userId=${userId}`
          })
        } else {
          uni.showToast({
            title: '无法查看红娘信息',
            icon: 'error'
          })
        }
      })
    },

    // 获取红娘的用户ID
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

        if (result.code === 200 && result.data) {
          return result.data.userId
        }
        return null
      } catch (error) {
        console.error('获取红娘用户ID失败:', error)
        return null
      }
    },

    // 格式化状态
    formatStatus(status) {
      return formatRequestStatus(status)
    },
    
    // 获取状态颜色
    getStatusColor(status) {
      return getRequestStatusColor(status)
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
.my-requests-page {
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

/* 申请列表 */
.requests-container {
  padding: 20rpx;
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
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.target-user {
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

.user-info {
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

.request-message {
  margin-bottom: 20rpx;
  line-height: 1.5;
}

.message-label {
  font-size: 26rpx;
  color: #666;
}

.message-content {
  font-size: 26rpx;
  color: #333;
}

.request-actions {
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 12rpx 24rpx;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 20rpx;
  font-size: 24rpx;
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 红娘信息 */
.matchmaker-info {
  background: #fff8e1;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  border-left: 4rpx solid #ffc107;
}

.matchmaker-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.matchmaker-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
}

.matchmaker-details {
  flex: 1;
}

.matchmaker-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.matchmaker-desc {
  font-size: 22rpx;
  color: #666;
}

.view-profile-btn {
  padding: 12rpx 20rpx;
  background: #ffc107;
  color: #333;
  border: none;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}
</style>
