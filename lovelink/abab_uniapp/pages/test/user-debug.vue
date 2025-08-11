<template>
  <view class="debug-page">
    <view class="header">
      <text class="title">用户信息调试</text>
    </view>
    
    <view class="test-section">
      <view class="section-title">获取用户详情</view>
      <view class="form-group">
        <text class="label">用户ID:</text>
        <input class="input" v-model="testUserId" placeholder="请输入用户ID" type="number" />
      </view>
      <button class="test-btn" @click="testGetUserDetail">获取用户详情</button>
      
      <view class="result-section" v-if="userDetailResult">
        <view class="result-title">API返回结果:</view>
        <textarea class="result-text" :value="JSON.stringify(userDetailResult, null, 2)" readonly></textarea>
      </view>
      
      <view class="parsed-section" v-if="parsedUser">
        <view class="result-title">解析后的用户信息:</view>
        <view class="user-info">
          <text class="info-item">用户ID: {{ parsedUser.userId }}</text>
          <text class="info-item">昵称: {{ parsedUser.nickname }}</text>
          <text class="info-item">头像: {{ parsedUser.avatarUrl }}</text>
          <text class="info-item">年龄: {{ parsedUser.age }}</text>
          <text class="info-item">地区: {{ parsedUser.location }}</text>
          <text class="info-item">性别: {{ parsedUser.gender }}</text>
        </view>
      </view>
    </view>
    
    <view class="test-section">
      <view class="section-title">快捷测试</view>
      <button class="test-btn" @click="testUserId = '1'; testGetUserDetail()">测试用户1</button>
      <button class="test-btn" @click="testUserId = '2'; testGetUserDetail()">测试用户2</button>
      <button class="test-btn" @click="testUserId = '3'; testGetUserDetail()">测试用户3</button>
    </view>
  </view>
</template>

<script>
import { getUserDetail } from '@/api/user/detail'

export default {
  data() {
    return {
      testUserId: '',
      userDetailResult: null,
      parsedUser: null
    }
  },
  
  methods: {
    async testGetUserDetail() {
      if (!this.testUserId) {
        uni.showToast({
          title: '请输入用户ID',
          icon: 'error'
        })
        return
      }
      
      try {
        uni.showLoading({
          title: '获取中...'
        })
        
        const result = await getUserDetail(this.testUserId)
        this.userDetailResult = result
        
        console.log('用户详情API结果:', result)
        
        if (result.code === 200 && result.data && result.data.records) {
          let targetUser = result.data.records.find(user => user.userId == this.testUserId)
          
          if (!targetUser && result.data.records.length > 0) {
            targetUser = result.data.records[0]
          }
          
          if (targetUser) {
            const profile = targetUser.userProfile || {}
            const age = profile.age || '未知'
            const location = profile.workCity || profile.hometown || '未知'

            this.parsedUser = {
              userId: targetUser.userId,
              nickname: targetUser.nickname || '用户' + targetUser.userId,
              avatarUrl: targetUser.avatarUrl || '/static/default-avatar.png',
              age: age,
              location: location,
              gender: targetUser.gender === 1 ? '男' : targetUser.gender === 2 ? '女' : '未知'
            }
          } else {
            this.parsedUser = null
            uni.showToast({
              title: '未找到用户信息',
              icon: 'error'
            })
          }
        } else {
          this.parsedUser = null
          uni.showToast({
            title: result.message || '获取失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('获取用户详情失败:', error)
        this.userDetailResult = { error: error.message }
        this.parsedUser = null
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
.debug-page {
  padding: 20rpx;
  background: #f5f5f5;
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

.test-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  border-left: 6rpx solid #667eea;
  padding-left: 20rpx;
}

.form-group {
  margin-bottom: 20rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.input {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.test-btn:last-child {
  margin-bottom: 0;
}

.result-section, .parsed-section {
  margin-top: 30rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.result-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.result-text {
  width: 100%;
  min-height: 200rpx;
  padding: 15rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-family: monospace;
  background: white;
  box-sizing: border-box;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.info-item {
  font-size: 24rpx;
  color: #666;
  padding: 8rpx 12rpx;
  background: white;
  border-radius: 6rpx;
  border-left: 4rpx solid #667eea;
}
</style>
