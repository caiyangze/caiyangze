<template>
  <view class="test-page">
    <view class="header">
      <text class="title">订单重复问题测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">重复问题测试</view>
      
      <button class="test-btn" @click="testOrderListLoad">测试订单列表加载</button>
      <button class="test-btn" @click="testMultipleClicks">测试多次点击</button>
      <button class="test-btn" @click="testStatusFilter">测试状态筛选</button>
      <button class="test-btn" @click="testPageRefresh">测试页面刷新</button>
    </view>

    <view class="test-section">
      <view class="section-title">模拟操作</view>
      
      <button class="action-btn" @click="simulateOnLoad">模拟 onLoad</button>
      <button class="action-btn" @click="simulateOnShow">模拟 onShow</button>
      <button class="action-btn" @click="simulateFilterChange">模拟筛选切换</button>
      <button class="action-btn" @click="simulatePullRefresh">模拟下拉刷新</button>
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
import { getMyMatchmakerOrders } from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n',
      requestCount: 0,
      orderList: [],
      isLoading: false
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 测试订单列表加载
    async testOrderListLoad() {
      this.addLog('开始测试订单列表加载...')
      this.requestCount = 0
      this.orderList = []
      
      try {
        // 模拟连续多次调用
        for (let i = 0; i < 3; i++) {
          this.addLog(`第 ${i + 1} 次调用 API...`)
          const result = await this.mockLoadOrderList()
          this.addLog(`第 ${i + 1} 次调用结果: ${result.length} 个订单`)
        }
        
        this.addLog(`✅ 测试完成，最终列表长度: ${this.orderList.length}`)
        this.addLog(`总 API 调用次数: ${this.requestCount}`)
      } catch (error) {
        this.addLog(`❌ 测试失败: ${error.message}`)
      }
    },

    // 模拟加载订单列表
    async mockLoadOrderList(isRefresh = false) {
      if (this.isLoading) {
        this.addLog('⚠️ 正在加载中，跳过重复请求')
        return []
      }

      this.isLoading = true
      this.requestCount++
      
      try {
        const result = await getMyMatchmakerOrders({
          pageNum: 1,
          pageSize: 10
        })
        
        if (result.code === 200) {
          const newOrders = result.data.records || []
          
          if (isRefresh) {
            this.orderList = newOrders
            this.addLog(`🔄 刷新模式: 替换为 ${newOrders.length} 个订单`)
          } else {
            // 去重处理
            const existingOrderIds = new Set(this.orderList.map(order => order.orderId))
            const uniqueNewOrders = newOrders.filter(order => !existingOrderIds.has(order.orderId))
            
            this.orderList = [...this.orderList, ...uniqueNewOrders]
            this.addLog(`➕ 追加模式: 新增 ${uniqueNewOrders.length} 个订单（去重后）`)
          }
          
          return newOrders
        } else {
          this.addLog(`❌ API 返回错误: ${result.message}`)
          return []
        }
      } catch (error) {
        this.addLog(`❌ API 调用异常: ${error.message}`)
        return []
      } finally {
        this.isLoading = false
      }
    },

    // 测试多次点击
    async testMultipleClicks() {
      this.addLog('开始测试多次快速点击...')
      
      // 模拟用户快速点击多次
      const promises = []
      for (let i = 0; i < 5; i++) {
        promises.push(this.mockLoadOrderList())
      }
      
      try {
        const results = await Promise.all(promises)
        this.addLog(`✅ 多次点击测试完成`)
        this.addLog(`实际执行的请求数: ${this.requestCount}`)
        this.addLog(`最终列表长度: ${this.orderList.length}`)
      } catch (error) {
        this.addLog(`❌ 多次点击测试失败: ${error.message}`)
      }
    },

    // 测试状态筛选
    async testStatusFilter() {
      this.addLog('开始测试状态筛选...')
      this.orderList = []
      this.requestCount = 0
      
      const statuses = [null, 0, 1, 4, null]
      
      for (const status of statuses) {
        this.addLog(`切换到状态: ${status === null ? '全部' : status}`)
        await this.mockLoadOrderList(true) // 使用刷新模式
        this.addLog(`当前列表长度: ${this.orderList.length}`)
      }
      
      this.addLog(`✅ 状态筛选测试完成`)
    },

    // 测试页面刷新
    async testPageRefresh() {
      this.addLog('开始测试页面刷新...')
      
      // 先加载一些数据
      await this.mockLoadOrderList()
      this.addLog(`初始加载后列表长度: ${this.orderList.length}`)
      
      // 模拟页面刷新
      await this.mockLoadOrderList(true)
      this.addLog(`刷新后列表长度: ${this.orderList.length}`)
      
      this.addLog(`✅ 页面刷新测试完成`)
    },

    // 模拟 onLoad
    simulateOnLoad() {
      this.addLog('模拟 onLoad 事件...')
      this.orderList = []
      this.mockLoadOrderList()
    },

    // 模拟 onShow
    simulateOnShow() {
      this.addLog('模拟 onShow 事件...')
      if (this.orderList.length > 0) {
        this.mockLoadOrderList(true)
      } else {
        this.addLog('列表为空，跳过 onShow 刷新')
      }
    },

    // 模拟筛选切换
    simulateFilterChange() {
      this.addLog('模拟筛选状态切换...')
      this.mockLoadOrderList(true)
    },

    // 模拟下拉刷新
    simulatePullRefresh() {
      this.addLog('模拟下拉刷新...')
      this.mockLoadOrderList(true)
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
      this.requestCount = 0
      this.orderList = []
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
</style>
