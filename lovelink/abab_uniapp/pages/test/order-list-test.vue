<template>
  <view class="test-page">
    <view class="header">
      <text class="title">订单列表页面测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">页面跳转测试</view>
      
      <button class="test-btn" @click="testOrderList">测试订单列表页面</button>
      <button class="test-btn" @click="testOrderListWithStatus">测试带状态的订单列表</button>
      <button class="test-btn" @click="testOrderListPending">测试待支付订单</button>
      <button class="test-btn" @click="testOrderListPaid">测试已支付订单</button>
    </view>

    <view class="test-section">
      <view class="section-title">API 测试</view>
      
      <button class="api-btn" @click="testGetOrders">测试获取订单API</button>
      <button class="api-btn" @click="testOrderStatusColor">测试状态颜色函数</button>
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
  getMyMatchmakerOrders, 
  getOrderStatusColor 
} from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n'
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 测试订单列表页面
    testOrderList() {
      this.addLog('跳转到订单列表页面...')
      uni.navigateTo({
        url: '/pages/matchmaker/order-list',
        success: () => {
          this.addLog('✅ 跳转成功')
        },
        fail: (err) => {
          this.addLog(`❌ 跳转失败: ${err.errMsg}`)
        }
      })
    },

    // 测试带状态的订单列表
    testOrderListWithStatus() {
      this.addLog('跳转到带状态筛选的订单列表页面...')
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?status=1',
        success: () => {
          this.addLog('✅ 跳转成功（已支付状态）')
        },
        fail: (err) => {
          this.addLog(`❌ 跳转失败: ${err.errMsg}`)
        }
      })
    },

    // 测试待支付订单
    testOrderListPending() {
      this.addLog('跳转到待支付订单列表...')
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?status=0',
        success: () => {
          this.addLog('✅ 跳转成功（待支付状态）')
        },
        fail: (err) => {
          this.addLog(`❌ 跳转失败: ${err.errMsg}`)
        }
      })
    },

    // 测试已支付订单
    testOrderListPaid() {
      this.addLog('跳转到已支付订单列表...')
      uni.navigateTo({
        url: '/pages/matchmaker/order-list?status=1',
        success: () => {
          this.addLog('✅ 跳转成功（已支付状态）')
        },
        fail: (err) => {
          this.addLog(`❌ 跳转失败: ${err.errMsg}`)
        }
      })
    },

    // 测试获取订单API
    async testGetOrders() {
      this.addLog('开始测试获取订单API...')
      
      try {
        const result = await getMyMatchmakerOrders({
          pageNum: 1,
          pageSize: 5
        })
        
        this.addLog(`API调用结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ API调用成功，获取到 ${result.data.records?.length || 0} 个订单`)
        } else {
          this.addLog(`❌ API调用失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ API调用异常: ${error.message}`)
      }
    },

    // 测试状态颜色函数
    testOrderStatusColor() {
      this.addLog('开始测试订单状态颜色函数...')
      
      const statuses = [
        { status: 0, name: '待支付' },
        { status: 1, name: '已支付' },
        { status: 2, name: '已取消' },
        { status: 3, name: '已退款' },
        { status: 4, name: '已完成' }
      ]
      
      statuses.forEach(item => {
        const color = getOrderStatusColor(item.status)
        this.addLog(`状态 ${item.status}(${item.name}): ${color}`)
      })
      
      this.addLog('✅ 状态颜色函数测试完成')
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

.test-btn, .api-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.api-btn {
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
