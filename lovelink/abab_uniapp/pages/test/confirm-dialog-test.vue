<template>
  <view class="test-page">
    <view class="header">
      <text class="title">确认弹窗样式测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">弹窗预览</view>
      <button class="test-btn" @click="showConfirm">显示确认弹窗</button>
      <button class="test-btn" @click="showConfirmWithMatchmaker">显示指定红娘弹窗</button>
      <button class="test-btn" @click="showConfirmSmart">显示智能分配弹窗</button>
    </view>

    <!-- 自定义确认弹窗 -->
    <view class="custom-confirm-modal" v-if="showCustomConfirmModal" @click="cancelSubmit">
      <view class="confirm-content" @click.stop>
        <view class="confirm-header">
          <view class="confirm-icon">💕</view>
          <text class="confirm-title">确认提交申请</text>
        </view>

        <view class="confirm-body">
          <view class="target-info">
            <text class="target-label">申请对象：</text>
            <text class="target-name">{{ testData.targetName }}</text>
          </view>

          <view class="service-info">
            <view class="service-item" v-if="testData.type === 'manual'">
              <text class="service-label">指定红娘：</text>
              <text class="service-value">{{ testData.matchmakerInfo }}</text>
            </view>
            <view class="service-item" v-else-if="testData.type === 'smart'">
              <text class="service-label">智能分配：</text>
              <text class="service-value">{{ testData.levelText }}</text>
            </view>
            <view class="service-item" v-else>
              <text class="service-label">智能分配：</text>
              <text class="service-value">系统自动选择最优红娘</text>
            </view>

            <view class="service-item">
              <text class="service-label">服务费用：</text>
              <text class="service-price">¥{{ testData.price }}</text>
            </view>
          </view>

          <view class="confirm-tips">
            <view class="tip-item">
              <text class="tip-icon">✨</text>
              <text class="tip-text">提交后将创建服务订单</text>
            </view>
            <view class="tip-item">
              <text class="tip-icon">💳</text>
              <text class="tip-text">请完成支付后我们将安排专业红娘服务</text>
            </view>
            <view class="tip-item">
              <text class="tip-icon">🎯</text>
              <text class="tip-text">红娘将在24小时内与您联系</text>
            </view>
          </view>
        </view>

        <view class="confirm-actions">
          <button class="cancel-btn" @click="cancelSubmit">取消</button>
          <button class="submit-btn" @click="confirmSubmit">确定提交</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      showCustomConfirmModal: false,
      testData: {
        targetName: '测试用户',
        type: 'auto',
        matchmakerInfo: '',
        levelText: '',
        price: 199
      }
    }
  },

  methods: {
    showConfirm() {
      this.testData = {
        targetName: '小美',
        type: 'auto',
        matchmakerInfo: '',
        levelText: '',
        price: '待定'
      }
      this.showCustomConfirmModal = true
    },

    showConfirmWithMatchmaker() {
      this.testData = {
        targetName: '小丽',
        type: 'manual',
        matchmakerInfo: '金牌红娘 平平淡淡',
        levelText: '',
        price: 299
      }
      this.showCustomConfirmModal = true
    },

    showConfirmSmart() {
      this.testData = {
        targetName: '小王',
        type: 'smart',
        matchmakerInfo: '',
        levelText: '正式红娘',
        price: 199
      }
      this.showCustomConfirmModal = true
    },

    confirmSubmit() {
      this.showCustomConfirmModal = false
      uni.showToast({
        title: '确认提交成功',
        icon: 'success'
      })
    },

    cancelSubmit() {
      this.showCustomConfirmModal = false
      uni.showToast({
        title: '已取消',
        icon: 'none'
      })
    }
  }
}
</script>

<style scoped>
.test-page {
  padding: 20rpx;
  background-color: #f5f5f5;
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
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

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

/* 自定义确认弹窗样式 */
.custom-confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(10rpx);
}

.confirm-content {
  background: white;
  border-radius: 32rpx;
  margin: 40rpx;
  max-width: 600rpx;
  width: 100%;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: confirmSlideIn 0.3s ease-out;
}

@keyframes confirmSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50rpx) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.confirm-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx 30rpx;
  text-align: center;
  color: white;
}

.confirm-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.confirm-title {
  font-size: 36rpx;
  font-weight: bold;
}

.confirm-body {
  padding: 40rpx 30rpx;
}

.target-info {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 20rpx;
  color: white;
}

.target-label {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.target-name {
  font-size: 32rpx;
  font-weight: bold;
}

.service-info {
  margin-bottom: 30rpx;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.service-item:last-child {
  border-bottom: none;
}

.service-label {
  font-size: 28rpx;
  color: #666;
}

.service-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.service-price {
  font-size: 32rpx;
  color: #e74c3c;
  font-weight: bold;
}

.confirm-tips {
  background: #f8f9fa;
  border-radius: 20rpx;
  padding: 24rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  font-size: 24rpx;
  margin-right: 12rpx;
  width: 32rpx;
}

.tip-text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
}

.confirm-actions {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn, .submit-btn {
  flex: 1;
  height: 100rpx;
  border: none;
  font-size: 32rpx;
  font-weight: bold;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
  border-radius: 0 0 0 32rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 0 0 32rpx 0;
}

.cancel-btn:active {
  background: #e9ecef;
}

.submit-btn:active {
  opacity: 0.8;
}
</style>
