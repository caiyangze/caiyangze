<template>
  <view class="test-page">
    <view class="header">
      <text class="title">订单流程测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">完整流程测试</view>
      
      <button class="test-btn" @click="testCreateOrder">1. 创建订单</button>
      <button class="test-btn" @click="testViewOrders">2. 查看订单列表</button>
      <button class="test-btn" @click="testCancelOrder">3. 取消订单</button>
      <button class="test-btn" @click="testPayOrder">4. 支付订单</button>
    </view>

    <view class="test-section">
      <view class="section-title">页面跳转测试</view>
      
      <button class="nav-btn" @click="goToRequestPage">牵线申请页面</button>
      <button class="nav-btn" @click="goToOrderList">订单列表页面</button>
      <button class="nav-btn" @click="goToOrderListPending">待支付订单</button>
      <button class="nav-btn" @click="goToOrderListPaid">已支付订单</button>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>
  </view>
</template>

<script>
import { 
  submitMatchmakingRequest,
  getMyMatchmakerOrders,
  cancelMatchmakerOrder,
  payMatchmakerOrder
} from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n',
      lastOrderId: null
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 测试创建订单
    async testCreateOrder() {
      this.addLog('开始测试创建订单...')
      
      const testData = {
        targetUserId: 1,
        matchmakerLevel: 2,
        requestMessage: '这是一个测试申请，用于验证订单创建和取消功能。'
      }
      
      try {
        const result = await submitMatchmakingRequest(testData)
        this.addLog(`创建订单结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.lastOrderId = result.data.orderId
          this.addLog(`✅ 订单创建成功！订单ID: ${this.lastOrderId}`)
          this.addLog(`订单编号: ${result.data.orderNo}`)
          this.addLog(`服务费用: ¥${result.data.amount}`)
        } else {
          this.addLog(`❌ 订单创建失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 创建订单异常: ${error.message}`)
      }
    },

    // 测试查看订单列表
    async testViewOrders() {
      this.addLog('开始测试查看订单列表...')
      
      try {
        const result = await getMyMatchmakerOrders({ pageNum: 1, pageSize: 10 })
        this.addLog(`订单列表结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          const orders = result.data.records || []
          this.addLog(`✅ 获取订单列表成功！共 ${orders.length} 个订单`)
          
          orders.forEach((order, index) => {
            this.addLog(`订单${index + 1}: ${order.orderNo} - ${order.orderStatusText} - ¥${order.payAmount}`)
          })
        } else {
          this.addLog(`❌ 获取订单列表失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 获取订单列表异常: ${error.message}`)
      }
    },

    // 测试取消订单
    async testCancelOrder() {
      if (!this.lastOrderId) {
        this.addLog('❌ 请先创建订单')
        return
      }
      
      this.addLog(`开始测试取消订单 ${this.lastOrderId}...`)
      
      try {
        const result = await cancelMatchmakerOrder(this.lastOrderId)
        this.addLog(`取消订单结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 订单取消成功！`)
          this.addLog(`消息: ${result.message}`)
        } else {
          this.addLog(`❌ 订单取消失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 取消订单异常: ${error.message}`)
      }
    },

    // 测试支付订单
    async testPayOrder() {
      if (!this.lastOrderId) {
        this.addLog('❌ 请先创建订单')
        return
      }
      
      this.addLog(`开始测试支付订单 ${this.lastOrderId}...`)
      
      try {
        const result = await payMatchmakerOrder(this.lastOrderId, 3) // 虚拟币支付
        this.addLog(`支付订单结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 订单支付成功！`)
          this.addLog(`消息: ${result.message}`)
        } else {
          this.addLog(`❌ 订单支付失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 支付订单异常: ${error.message}`)
      }
    },

    // 跳转到申请页面
    goToRequestPage() {
      uni.navigateTo({
        url: '/pages/matchmaker/request?targetUserId=1'
      })
    },

    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
      })
    },

    // 跳转到待支付订单
    goToOrderListPending() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?status=0'
      })
    },

    // 跳转到已支付订单
    goToOrderListPaid() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?status=1'
      })
    },

    // 清空日志
    clearLog() {
      this.testResult = ''
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

.test-btn, .nav-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.nav-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.clear-btn {
  width: 100%;
  height: 60rpx;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 24rpx;
  margin-top: 20rpx;
}

.result-area {
  width: 100%;
  height: 400rpx;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 10rpx;
  padding: 20rpx;
  font-size: 24rpx;
  line-height: 1.5;
}
</style>
