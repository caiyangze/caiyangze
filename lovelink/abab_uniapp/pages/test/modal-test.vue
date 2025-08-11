<template>
  <view class="test-page">
    <view class="header">
      <text class="title">模态框测试</text>
    </view>

    <view class="test-section">
      <text class="debug-info">showModal = {{showModal}}</text>
      <button class="test-btn" @tap="openModal">打开模态框</button>
      <button class="test-btn" @tap="closeModal">关闭模态框</button>
    </view>

    <!-- 测试模态框 -->
    <view v-if="showModal" class="modal-overlay" @tap="closeModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">测试模态框</text>
          <view class="modal-close" @tap="closeModal">✕</view>
        </view>
        
        <view class="modal-body">
          <text>这是一个测试模态框</text>
          <text>如果您能看到这个，说明模态框功能正常</text>
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn" @tap="closeModal">关闭</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      showModal: false
    }
  },

  methods: {
    openModal() {
      console.log('openModal 被调用')
      this.showModal = true
      console.log('showModal 设置为:', this.showModal)
    },

    closeModal() {
      console.log('closeModal 被调用')
      this.showModal = false
      console.log('showModal 设置为:', this.showModal)
    }
  }
}
</script>

<style scoped>
.test-page {
  padding: 40rpx;
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
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.debug-info {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 20rpx;
  display: block;
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

/* 模态框样式 */
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
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.modal-footer {
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.modal-btn {
  width: 100%;
  height: 80rpx;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
