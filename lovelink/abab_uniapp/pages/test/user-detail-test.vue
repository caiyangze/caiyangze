<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">用户详情页面测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试跳转到用户详情页</text>
      <view class="input-group">
        <text class="input-label">用户ID:</text>
        <input
          class="test-input"
          v-model="testUserId"
          placeholder="请输入用户ID"
          type="number"
        />
      </view>
      <button class="test-btn" @click="goToUserDetail">跳转到用户详情页</button>
      <button class="test-btn" @click="testUserAPI">测试用户API</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">模拟匹配结果跳转</text>
      <button class="test-btn" @click="simulateMatchToDetail">模拟匹配结果→用户详情</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试说明</text>
      <view class="info-box">
        <text class="info-text">
          1. 点击"查看资料"按钮会跳转到用户详情页
          2. 在用户详情页点击"发消息"会检查是否互相关注
          3. 如果互相关注，跳转到聊天页面
          4. 如果未互相关注，显示提示信息
        </text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      testUserId: '1'
    }
  },

  methods: {
    // 跳转到用户详情页
    goToUserDetail() {
      if (!this.testUserId) {
        uni.showToast({
          title: '请输入用户ID',
          icon: 'none'
        })
        return
      }

      console.log('跳转到用户详情页，userId:', this.testUserId)
      uni.navigateTo({
        url: `/pages/user/user-detail?userId=${this.testUserId}`
      })
    },

    // 测试用户API
    async testUserAPI() {
      if (!this.testUserId) {
        uni.showToast({
          title: '请输入用户ID',
          icon: 'none'
        })
        return
      }

      try {
        // 导入API
        const userDetailAPI = await import('@/api/user/detail.js')

        console.log('开始测试用户API，userId:', this.testUserId)

        // 测试 getUserDetail（相亲广场API）
        try {
          const result = await userDetailAPI.getUserDetail(this.testUserId)
          console.log('getUserDetail 结果:', result)

          if (result.code === 200 && result.data && result.data.records) {
            const targetUser = result.data.records.find(user => user.userId == this.testUserId)
            if (targetUser) {
              uni.showModal({
                title: '用户详情获取成功',
                content: `用户: ${targetUser.nickname}\n年龄: ${targetUser.userProfile?.age || '未知'}\n性别: ${targetUser.gender === 1 ? '男' : targetUser.gender === 2 ? '女' : '未知'}`,
                showCancel: false
              })
            } else {
              uni.showModal({
                title: '未找到用户',
                content: `在返回的 ${result.data.records.length} 条记录中未找到用户ID ${this.testUserId}`,
                showCancel: false
              })
            }
          } else {
            uni.showModal({
              title: 'API调用失败',
              content: `错误码: ${result.code}\n错误信息: ${result.message || '未知错误'}`,
              showCancel: false
            })
          }
        } catch (error) {
          console.error('getUserDetail 错误:', error)
          uni.showModal({
            title: '网络错误',
            content: error.message || '请求失败',
            showCancel: false
          })
        }

      } catch (error) {
        console.error('测试API失败:', error)
        uni.showToast({
          title: '测试失败: ' + error.message,
          icon: 'none'
        })
      }
    },

    // 模拟匹配结果跳转
    simulateMatchToDetail() {
      const mockResult = {
        userId: this.testUserId || '1',
        nickname: '测试用户',
        avatarUrl: '/static/message/default-avatar.png',
        gender: 2,
        age: 25
      }

      // 先跳转到匹配结果页面
      uni.navigateTo({
        url: `/pages/game/match-result?result=${encodeURIComponent(JSON.stringify(mockResult))}`
      })
    }
  }
}
</script>

<style scoped>
.test-page {
  min-height: 100vh;
  padding: 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.test-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.test-title {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
}

.test-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
}

.input-group {
  margin-bottom: 30rpx;
}

.input-label {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
  display: block;
}

.test-input {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #ddd;
  border-radius: 10rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #ff6b9d, #c44569);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.info-box {
  background: #f8f9fa;
  border-radius: 10rpx;
  padding: 30rpx;
  border-left: 6rpx solid #007bff;
}

.info-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
}
</style>
