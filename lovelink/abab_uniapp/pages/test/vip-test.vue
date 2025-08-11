<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">VIP功能测试</text>
    </view>
    
    <view class="test-content">
      <view class="test-section">
        <text class="section-title">VIP充值测试</text>
        <button class="test-button primary" @click="testVipRecharge">🎯 测试VIP充值页面</button>
        <button class="test-button secondary" @click="testVipSuccess">✅ 测试VIP成功页面</button>
      </view>
      
      <view class="test-section">
        <text class="section-title">API测试</text>
        <button class="test-button" @click="testGetPackages">测试获取VIP套餐</button>
        <button class="test-button" @click="testGetPayMethods">测试获取支付方式</button>
        <button class="test-button" @click="testCreateOrder">测试创建订单</button>
        <button class="test-button" @click="testPayment">测试支付流程</button>
      </view>
      
      <view class="test-section">
        <text class="section-title">测试结果</text>
        <view class="result-area">
          <text class="result-text">{{ testResult }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import http from '@/api/http.js'

export default {
  data() {
    return {
      testResult: '点击按钮开始测试...',
      testOrderId: null
    }
  },
  
  methods: {
    // 测试VIP充值页面
    testVipRecharge() {
      uni.navigateTo({
        url: '/pages/vip/recharge'
      })
    },
    
    // 测试VIP成功页面
    testVipSuccess() {
      const testData = {
        orderId: 12345,
        orderNo: 'VIP' + Date.now(),
        payMethodName: '微信支付',
        payAmount: '19.90',
        vipStartTime: new Date().toISOString(),
        vipEndTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
      }
      
      uni.navigateTo({
        url: `/pages/vip/success?orderData=${encodeURIComponent(JSON.stringify(testData))}`
      })
    },
    
    // 测试获取VIP套餐
    async testGetPackages() {
      try {
        this.testResult = '正在获取VIP套餐...'
        const response = await http.get('/VIP/packages')
        this.testResult = `获取VIP套餐成功:\n${JSON.stringify(response, null, 2)}`
      } catch (error) {
        this.testResult = `获取VIP套餐失败: ${error.message}`
      }
    },
    
    // 测试获取支付方式
    async testGetPayMethods() {
      try {
        this.testResult = '正在获取支付方式...'
        const response = await http.get('/VIP/payMethods')
        this.testResult = `获取支付方式成功:\n${JSON.stringify(response, null, 2)}`
      } catch (error) {
        this.testResult = `获取支付方式失败: ${error.message}`
      }
    },
    
    // 测试创建订单
    async testCreateOrder() {
      try {
        this.testResult = '正在创建订单...'
        const response = await http.post('/VIP/createOrder', {
          vipType: 1 // 月度VIP
        })
        
        if (response.code === 200) {
          this.testOrderId = response.data.orderId
          this.testResult = `创建订单成功:\n订单ID: ${this.testOrderId}\n${JSON.stringify(response, null, 2)}`
        } else {
          this.testResult = `创建订单失败: ${response.message}`
        }
      } catch (error) {
        this.testResult = `创建订单失败: ${error.message}`
      }
    },
    
    // 测试支付流程
    async testPayment() {
      if (!this.testOrderId) {
        this.testResult = '请先创建订单'
        return
      }
      
      try {
        this.testResult = '正在处理支付...'
        const response = await http.post('/VIP/pay', {
          orderId: this.testOrderId,
          payType: 1 // 微信支付
        })
        
        if (response.code === 200) {
          this.testResult = `支付成功:\n${JSON.stringify(response, null, 2)}`
        } else {
          this.testResult = `支付失败: ${response.message}`
        }
      } catch (error) {
        this.testResult = `支付失败: ${error.message}`
      }
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

.test-content {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.test-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.test-button {
  width: 100%;
  height: 80rpx;
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.test-button.primary {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  box-shadow: 0 4rpx 15rpx rgba(255, 215, 0, 0.3);
}

.test-button.secondary {
  background: linear-gradient(45deg, #667eea, #764ba2);
}

.test-button:active {
  transform: translateY(2rpx);
  opacity: 0.8;
}

.test-button:last-child {
  margin-bottom: 0;
}

.result-area {
  background: #f5f5f5;
  border-radius: 10rpx;
  padding: 20rpx;
  min-height: 200rpx;
  max-height: 400rpx;
  overflow-y: auto;
}

.result-text {
  font-size: 24rpx;
  color: #333;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
