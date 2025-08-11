<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">匹配结果页面测试</text>
    </view>
    
    <view class="test-buttons">
      <button class="test-btn" @click="testWithData">测试有数据</button>
      <button class="test-btn" @click="testWithoutData">测试无数据</button>
    </view>
    
    <view class="test-info">
      <text class="info-text">点击按钮测试匹配结果页面的布局效果</text>
      <text class="info-text">检查页面是否充满整个屏幕宽度</text>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    // 测试有数据的情况
    testWithData() {
      const testData = {
        userId: 'test123',
        nickname: 'cmy',
        avatarUrl: '/static/message/default-avatar.png',
        gender: 2,
        age: 25
      }
      
      uni.navigateTo({
        url: `/pages/game/match-result?result=${encodeURIComponent(JSON.stringify(testData))}`
      })
    },
    
    // 测试无数据的情况（使用默认测试数据）
    testWithoutData() {
      uni.navigateTo({
        url: '/pages/game/match-result'
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.test-header {
  margin-bottom: 60rpx;
}

.test-title {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
  text-align: center;
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  margin-bottom: 60rpx;
}

.test-btn {
  width: 400rpx;
  height: 90rpx;
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: none;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.test-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.info-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
}
</style>
