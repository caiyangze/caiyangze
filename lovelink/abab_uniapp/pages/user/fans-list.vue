<template>
  <view class="fans-list-container">
    <!-- 空状态 -->
    <view v-if="fansList.length === 0 && !loading" class="empty-state">
      <view class="empty-icon">👥</view>
      <text class="empty-text">暂无粉丝</text>
      <text class="empty-desc">分享精彩内容吸引更多粉丝</text>
    </view>

    <!-- 粉丝列表 -->
    <view v-else class="fans-list">
      <view
        v-for="(fan, index) in fansList"
        :key="fan.followId || index"
        class="fan-item"
        @click="goToUserDetail(fan.user)"
      >
        <!-- 用户头像 -->
        <view class="avatar-container">
          <image
            :src="fan.user?.avatarUrl || '/static/message/default-avatar.png'"
            class="user-avatar"
            mode="aspectFill"
          ></image>
        </view>

        <!-- 用户信息 -->
        <view class="user-info">
          <text class="user-name">{{ fan.user?.nickname || '用户' + fan.user?.userId }}</text>
          <text class="user-desc">{{ getGenderText(fan.user?.gender) }}</text>
        </view>

        <!-- 关注按钮 -->
        <view class="follow-btn-container">
          <button
            :class="getFollowButtonClass(fan.isFollow)"
            @click.stop="handleFollow(fan, index)"
            :disabled="fan.loading"
          >
            {{ getFollowButtonText(fan.isFollow) }}
          </button>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script>
import { getFansList, toggleUserFollow } from '@/api/user/follow.js'

export default {
  data() {
    return {
      fansList: [],
      loading: false
    }
  },

  onLoad() {
    this.loadFansList()
  },

  onPullDownRefresh() {
    this.loadFansList().finally(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    /**
     * 加载粉丝列表
     */
    async loadFansList() {
      try {
        this.loading = true
        const result = await getFansList()
        
        if (result.code === 200) {
          this.fansList = result.data || []
        } else {
          uni.showToast({
            title: result.message || '获取粉丝列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取粉丝列表失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    /**
     * 处理关注操作
     */
    async handleFollow(fan, index) {
      try {
        // 设置按钮加载状态
        this.$set(this.fansList[index], 'loading', true)
        
        const result = await toggleUserFollow(fan.user.userId)
        
        if (result.code === 200) {
          // 更新关注状态
          if (fan.isFollow === 0) {
            this.fansList[index].isFollow = 1 // 未关注 -> 已关注
          } else if (fan.isFollow === 1) {
            this.fansList[index].isFollow = 0 // 已关注 -> 未关注
          } else if (fan.isFollow === 2) {
            this.fansList[index].isFollow = 0 // 相互关注 -> 未关注
          }
          
          uni.showToast({
            title: result.message || '操作成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: result.message || '操作失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('关注操作失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      } finally {
        this.$set(this.fansList[index], 'loading', false)
      }
    },

    /**
     * 跳转到用户详情页
     */
    goToUserDetail(user) {
      if (!user || !user.userId) return
      
      uni.navigateTo({
        url: `/pages/user/user-detail?userId=${user.userId}`
      })
    },

    /**
     * 获取性别文本
     */
    getGenderText(gender) {
      switch (gender) {
        case 1: return '男'
        case 2: return '女'
        default: return '未知'
      }
    },



    /**
     * 获取关注按钮样式类
     */
    getFollowButtonClass(isFollow) {
      const baseClass = 'follow-btn'
      switch (isFollow) {
        case 0: return `${baseClass} follow-btn-unfollow`
        case 1: return `${baseClass} follow-btn-following`
        case 2: return `${baseClass} follow-btn-mutual`
        default: return `${baseClass} follow-btn-unfollow`
      }
    },

    /**
     * 获取关注按钮文本
     */
    getFollowButtonText(isFollow) {
      switch (isFollow) {
        case 0: return '关注'
        case 1: return '已关注'
        case 2: return '相互关注'
        default: return '关注'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.fans-list-container {
  width: 100%;
  min-height: 100vh;
  height: auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  box-sizing: border-box;
  position: relative;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;

  .empty-icon {
    font-size: 100rpx;
    margin-bottom: 24rpx;
    opacity: 0.7;
  }

  .empty-text {
    color: rgba(255, 255, 255, 0.9);
    font-size: 30rpx;
    font-weight: 500;
    margin-bottom: 12rpx;
  }

  .empty-desc {
    color: rgba(255, 255, 255, 0.7);
    font-size: 24rpx;
  }
}

// 粉丝列表
.fans-list {
  padding: 20rpx 24rpx 40rpx 24rpx;
  max-width: 750rpx;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
  min-height: 100vh;
}

// 粉丝项
.fan-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 20rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.15);
  }

  &:active {
    transform: scale(0.98);
  }

  &:last-child {
    margin-bottom: 0;
  }
}

// 头像容器
.avatar-container {
  margin-right: 24rpx;
  position: relative;

  .user-avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    border: 3rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  }
}

// 用户信息
.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0; // 防止文字溢出

  .user-name {
    font-size: 34rpx;
    font-weight: 600;
    color: #2c3e50;
    line-height: 1.3;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-desc {
    font-size: 26rpx;
    color: #7f8c8d;
    opacity: 0.8;
  }
}

// 关注按钮容器
.follow-btn-container {
  margin-left: 20rpx;
  flex-shrink: 0;
}

// 关注按钮
.follow-btn {
  padding: 16rpx 28rpx;
  border-radius: 25rpx;
  font-size: 26rpx;
  font-weight: 500;
  min-width: 140rpx;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &:active {
    transform: scale(0.95);
  }
}

.follow-btn-unfollow {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba(102, 126, 234, 0.4);
  }

  &:active {
    background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  }
}

.follow-btn-following {
  background: #ecf0f1;
  color: #7f8c8d;
  border: 2rpx solid #bdc3c7;

  &:hover {
    background: #d5dbdb;
    border-color: #95a5a6;
  }

  &:active {
    background: #bdc3c7;
  }
}

.follow-btn-mutual {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: #fff;

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba(46, 204, 113, 0.4);
  }

  &:active {
    background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  }
}

// 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;

  .loading-spinner {
    width: 50rpx;
    height: 50rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    border-top: 4rpx solid rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 20rpx;
  }

  .loading-text {
    color: rgba(255, 255, 255, 0.8);
    font-size: 26rpx;
    font-weight: 500;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
