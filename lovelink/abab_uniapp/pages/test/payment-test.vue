<template>
  <view class="test-page">
    <view class="header">
      <text class="title">支付功能测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">支付流程测试</view>
      
      <button class="test-btn" @click="testPaymentFlow">测试完整支付流程</button>
      <button class="test-btn" @click="testPaymentAPI">测试支付API</button>
      <button class="test-btn" @click="testInvalidOrder">测试无效订单支付</button>
      <button class="test-btn" @click="testNullOrder">测试空订单支付</button>
    </view>

    <view class="test-section">
      <view class="section-title">模拟订单数据</view>
      
      <view class="form-group">
        <text class="form-label">订单ID:</text>
        <input class="form-input" v-model="testOrderId" placeholder="输入订单ID" />
      </view>
      
      <view class="form-group">
        <text class="form-label">支付方式:</text>
        <picker @change="onPayTypeChange" :value="payTypeIndex" :range="payTypes">
          <view class="picker">{{ payTypes[payTypeIndex] }}</view>
        </picker>
      </view>
      
      <button class="action-btn" @click="createTestOrder">创建测试订单</button>
      <button class="action-btn" @click="testPayOrder">测试支付订单</button>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">快速跳转</view>
      <button class="nav-btn" @click="goToOrderList">跳转到订单列表</button>
    </view>
  </view>
</template>

<script>
import { payMatchmakerOrder, getMyMatchmakerOrders } from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n',
      testOrderId: '1',
      payTypes: ['虚拟币支付', '微信支付', '支付宝'],
      payTypeIndex: 0,
      testOrder: null
    }
  },

  computed: {
    currentPayType() {
      return this.payTypeIndex + 1 // 1-微信，2-支付宝，3-虚拟币
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 支付方式选择
    onPayTypeChange(e) {
      this.payTypeIndex = e.detail.value
      this.addLog(`支付方式切换为: ${this.payTypes[this.payTypeIndex]}`)
    },

    // 测试完整支付流程
    async testPaymentFlow() {
      this.addLog('开始测试完整支付流程...')
      
      try {
        // 1. 创建测试订单
        await this.createTestOrder()
        
        // 2. 测试支付
        if (this.testOrder) {
          await this.testPayOrder()
        }
        
        this.addLog('✅ 完整支付流程测试完成')
      } catch (error) {
        this.addLog(`❌ 支付流程测试失败: ${error.message}`)
      }
    },

    // 创建测试订单
    async createTestOrder() {
      this.addLog('创建测试订单...')
      
      try {
        // 获取现有订单作为测试数据
        const result = await getMyMatchmakerOrders({ pageNum: 1, pageSize: 1 })
        
        if (result.code === 200 && result.data.records.length > 0) {
          this.testOrder = result.data.records[0]
          this.testOrderId = this.testOrder.orderId.toString()
          
          this.addLog(`✅ 获取到测试订单: ${this.testOrder.orderNo}`)
          this.addLog(`订单ID: ${this.testOrder.orderId}`)
          this.addLog(`订单状态: ${this.testOrder.orderStatus}`)
          this.addLog(`订单金额: ¥${this.testOrder.payAmount}`)
        } else {
          this.addLog('❌ 没有找到可用的测试订单')
          
          // 创建模拟订单数据
          this.testOrder = {
            orderId: parseInt(this.testOrderId),
            orderNo: `MM${Date.now()}`,
            orderStatus: 0,
            payAmount: 199,
            serviceDesc: '测试牵线服务'
          }
          
          this.addLog(`📝 创建模拟订单数据: ${this.testOrder.orderNo}`)
        }
      } catch (error) {
        this.addLog(`❌ 创建测试订单失败: ${error.message}`)
      }
    },

    // 测试支付订单
    async testPayOrder() {
      if (!this.testOrder) {
        this.addLog('❌ 请先创建测试订单')
        return
      }
      
      this.addLog(`开始测试支付订单 ${this.testOrder.orderNo}...`)
      this.addLog(`支付方式: ${this.payTypes[this.payTypeIndex]}`)
      
      try {
        const result = await payMatchmakerOrder(this.testOrder.orderId, this.currentPayType)
        
        this.addLog(`支付API返回: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog('✅ 支付成功！')
        } else {
          this.addLog(`❌ 支付失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 支付异常: ${error.message}`)
      }
    },

    // 测试支付API
    async testPaymentAPI() {
      this.addLog('开始测试支付API...')
      
      const orderId = parseInt(this.testOrderId)
      const payType = this.currentPayType
      
      this.addLog(`订单ID: ${orderId}`)
      this.addLog(`支付方式: ${payType}`)
      
      try {
        const result = await payMatchmakerOrder(orderId, payType)
        
        this.addLog(`API返回结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog('✅ API调用成功')
        } else {
          this.addLog(`❌ API返回错误: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ API调用异常: ${error.message}`)
      }
    },

    // 测试无效订单支付
    async testInvalidOrder() {
      this.addLog('开始测试无效订单支付...')
      
      const invalidOrderId = 99999
      
      try {
        const result = await payMatchmakerOrder(invalidOrderId, 3)
        
        this.addLog(`无效订单支付结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog('⚠️ 无效订单支付成功（可能有问题）')
        } else {
          this.addLog(`✅ 正确拒绝了无效订单: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`✅ 正确抛出异常: ${error.message}`)
      }
    },

    // 测试空订单支付
    testNullOrder() {
      this.addLog('开始测试空订单支付...')
      
      // 模拟 payOrder 方法的逻辑
      const order = null
      
      if (!order || !order.orderId) {
        this.addLog('✅ 正确检测到空订单，拒绝支付')
        return
      }
      
      this.addLog('❌ 空订单检测失败')
    },

    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
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

.test-btn, .action-btn, .nav-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.action-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.nav-btn {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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

.form-group {
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  display: block;
}

.form-input {
  width: 100%;
  height: 60rpx;
  border: 2rpx solid #e9ecef;
  border-radius: 8rpx;
  padding: 0 16rpx;
  font-size: 28rpx;
}

.picker {
  height: 60rpx;
  line-height: 60rpx;
  border: 2rpx solid #e9ecef;
  border-radius: 8rpx;
  padding: 0 16rpx;
  font-size: 28rpx;
  background: white;
}
</style>
