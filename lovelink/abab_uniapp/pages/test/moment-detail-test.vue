<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">动态详情页面测试</text>
    </view>
    
    <view class="test-buttons">
      <button @tap="testMomentDetail" class="test-btn">测试动态详情页面</button>
      <button @tap="testWithComment" class="test-btn">测试评论功能</button>
    </view>
    
    <view class="test-info">
      <text class="info-title">测试说明：</text>
      <text class="info-text">1. 点击"测试动态详情页面"查看基本布局</text>
      <text class="info-text">2. 点击"测试评论功能"查看评论输入体验</text>
      <text class="info-text">3. 检查样式是否与主页保持一致</text>
      <text class="info-text">4. 测试评论发布和回显功能</text>
    </view>
  </view>
</template>

<script setup>
// 测试动态详情页面
function testMomentDetail() {
  uni.navigateTo({
    url: '/pages/moment/detail?id=test123'
  })
}

// 测试评论功能
function testWithComment() {
  uni.navigateTo({
    url: '/pages/moment/detail?id=test123&action=comment'
  })
}
</script>

<style lang="scss" scoped>
.test-page {
  padding: 40rpx;
  background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
  min-height: 100vh;
}

.test-header {
  text-align: center;
  margin-bottom: 60rpx;
  
  .test-title {
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
  }
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  margin-bottom: 60rpx;
  
  .test-btn {
    background: rgba(255, 255, 255, 0.2);
    border: 1rpx solid rgba(255, 255, 255, 0.3);
    border-radius: 25rpx;
    padding: 30rpx;
    color: #fff;
    font-size: 32rpx;
    font-weight: 600;
    backdrop-filter: blur(10rpx);
    
    &:active {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(0.98);
    }
  }
}

.test-info {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 30rpx;
  backdrop-filter: blur(10rpx);
  
  .info-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 20rpx;
  }
  
  .info-text {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.6;
    margin-bottom: 10rpx;
  }
}
</style>
