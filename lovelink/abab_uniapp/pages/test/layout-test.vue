<template>
  <view class="test-page">
    <view class="header">
      <text class="title">页面布局测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">宽度测试</view>
      
      <view class="width-test-item">
        <text class="test-label">100% 宽度测试:</text>
        <view class="width-bar width-100">100%</view>
      </view>
      
      <view class="width-test-item">
        <text class="test-label">100vw 宽度测试:</text>
        <view class="width-bar width-100vw">100vw</view>
      </view>
      
      <view class="width-test-item">
        <text class="test-label">固定宽度测试:</text>
        <view class="width-bar width-fixed">750rpx</view>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">页面跳转测试</view>
      
      <button class="test-btn" @click="goToOrderList">跳转到订单列表</button>
      <button class="test-btn" @click="goToOrderListEmpty">跳转到空订单列表</button>
      <button class="test-btn" @click="goToRequestPage">跳转到申请页面</button>
    </view>

    <view class="test-section">
      <view class="section-title">模拟订单卡片</view>
      
      <view class="mock-order-card">
        <view class="mock-order-header">
          <view class="mock-order-info">
            <text class="mock-order-no">订单号: MM17542886030255478</text>
            <text class="mock-order-time">2小时前</text>
          </view>
          <text class="mock-order-status">待支付</text>
        </view>
        
        <view class="mock-service-info">
          <view class="mock-service-icon">💕</view>
          <view class="mock-service-details">
            <text class="mock-service-type">单次牵线</text>
            <text class="mock-service-desc">希望红娘帮忙平平淡淡为您匹配测试用户2</text>
            <text class="mock-matchmaker">红娘: 平平淡淡</text>
          </view>
        </view>
        
        <view class="mock-order-footer">
          <view class="mock-price-info">
            <text class="mock-price">¥99</text>
          </view>
          <view class="mock-actions">
            <button class="mock-cancel-btn">取消订单</button>
            <button class="mock-pay-btn">立即支付</button>
          </view>
        </view>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">空状态测试</view>
      
      <view class="mock-empty-state">
        <view class="mock-empty-icon">📋</view>
        <text class="mock-empty-text">暂无订单记录</text>
        <text class="mock-empty-desc">您还没有红娘服务订单</text>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">屏幕信息</view>
      
      <view class="screen-info">
        <text class="info-item">屏幕宽度: {{ screenWidth }}px</text>
        <text class="info-item">屏幕高度: {{ screenHeight }}px</text>
        <text class="info-item">设备像素比: {{ pixelRatio }}</text>
        <text class="info-item">状态栏高度: {{ statusBarHeight }}px</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      screenWidth: 0,
      screenHeight: 0,
      pixelRatio: 0,
      statusBarHeight: 0
    }
  },

  onLoad() {
    this.getSystemInfo()
  },

  methods: {
    // 获取系统信息
    getSystemInfo() {
      uni.getSystemInfo({
        success: (res) => {
          this.screenWidth = res.screenWidth
          this.screenHeight = res.screenHeight
          this.pixelRatio = res.pixelRatio
          this.statusBarHeight = res.statusBarHeight
        }
      })
    },

    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
      })
    },

    // 跳转到空订单列表
    goToOrderListEmpty() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?empty=true'
      })
    },

    // 跳转到申请页面
    goToRequestPage() {
      uni.navigateTo({
        url: '/pages/matchmaker/request?targetUserId=2'
      })
    }
  }
}
</script>

<style scoped>
/* 基础样式重置 */
* {
  box-sizing: border-box;
}

.test-page {
  width: 100vw;
  min-height: 100vh;
  background: #f5f5f5;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.header {
  width: 100%;
  text-align: center;
  padding: 40rpx 0;
  background: white;
  margin-bottom: 20rpx;
  box-sizing: border-box;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.test-section {
  width: 100%;
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-sizing: border-box;
  margin-left: 20rpx;
  margin-right: 20rpx;
  width: calc(100% - 40rpx);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 宽度测试 */
.width-test-item {
  margin-bottom: 20rpx;
}

.test-label {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
}

.width-bar {
  height: 60rpx;
  line-height: 60rpx;
  text-align: center;
  color: white;
  font-weight: bold;
  border-radius: 8rpx;
  margin-bottom: 10rpx;
}

.width-100 {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.width-100vw {
  width: 100vw;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  margin-left: -30rpx;
}

.width-fixed {
  width: 750rpx;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

/* 按钮样式 */
.test-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

/* 模拟订单卡片 */
.mock-order-card {
  width: 100%;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
}

.mock-order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.mock-order-info {
  flex: 1;
}

.mock-order-no {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.mock-order-time {
  font-size: 24rpx;
  color: #999;
}

.mock-order-status {
  font-size: 26rpx;
  font-weight: bold;
  color: #f39c12;
}

.mock-service-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.mock-service-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.mock-service-details {
  flex: 1;
}

.mock-service-type {
  display: block;
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.mock-service-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.mock-matchmaker {
  font-size: 24rpx;
  color: #e74c3c;
}

.mock-order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mock-price {
  font-size: 36rpx;
  color: #e74c3c;
  font-weight: bold;
}

.mock-actions {
  display: flex;
  gap: 20rpx;
}

.mock-cancel-btn, .mock-pay-btn {
  padding: 16rpx 32rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  border: none;
}

.mock-cancel-btn {
  background: #f8f9fa;
  color: #666;
}

.mock-pay-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 模拟空状态 */
.mock-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.mock-empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.mock-empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.mock-empty-desc {
  font-size: 26rpx;
  color: #999;
  text-align: center;
}

/* 屏幕信息 */
.screen-info {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
}

.info-item {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}
</style>
