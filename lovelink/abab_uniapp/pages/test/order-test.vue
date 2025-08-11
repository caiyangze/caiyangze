<template>
  <view class="test-page">
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">订单功能测试</view>
      <view class="nav-right"></view>
    </view>

    <view class="test-container">
      <view class="test-section">
        <view class="section-title">测试功能</view>
        
        <button class="test-btn" @click="testGetMyOrders">
          测试获取我的订单列表
        </button>
        
        <button class="test-btn" @click="testGetOrderDetail">
          测试获取订单详情（订单ID: 1）
        </button>
        
        <button class="test-btn" @click="testCancelOrder">
          测试取消订单（订单ID: 1）
        </button>
        
        <button class="test-btn" @click="testPayOrder">
          测试支付订单（订单ID: 1）
        </button>
        
        <button class="test-btn" @click="goToOrderList">
          跳转到订单列表页面
        </button>
        
        <button class="test-btn" @click="goToOrderDetail">
          跳转到订单详情页面
        </button>
      </view>

      <view class="result-section" v-if="testResult">
        <view class="section-title">测试结果</view>
        <view class="result-content">
          <pre>{{ testResult }}</pre>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { 
  getMyMatchmakerOrders, 
  getMatchmakerOrderDetail, 
  cancelMatchmakerOrder, 
  payMatchmakerOrder 
} from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: ''
    }
  },
  
  methods: {
    // 测试获取我的订单列表
    async testGetMyOrders() {
      try {
        this.testResult = '正在获取订单列表...'
        const result = await getMyMatchmakerOrders({
          pageNum: 1,
          pageSize: 10
        })
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 测试获取订单详情
    async testGetOrderDetail() {
      try {
        this.testResult = '正在获取订单详情...'
        const result = await getMatchmakerOrderDetail(1)
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 测试取消订单
    async testCancelOrder() {
      try {
        this.testResult = '正在取消订单...'
        const result = await cancelMatchmakerOrder(1)
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 测试支付订单
    async testPayOrder() {
      try {
        this.testResult = '正在支付订单...'
        const result = await payMatchmakerOrder(1, 3) // 使用虚拟币支付
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 跳转到订单列表页面
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
      })
    },
    
    // 跳转到订单详情页面
    goToOrderDetail() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-detail?orderId=1'
      })
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.test-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
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

.test-container {
  padding: 30rpx;
}

.test-section, .result-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-content {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #333;
  max-height: 600rpx;
  overflow-y: auto;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
