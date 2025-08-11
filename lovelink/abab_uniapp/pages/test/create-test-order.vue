<template>
  <view class="test-page">
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">创建测试订单</view>
      <view class="nav-right"></view>
    </view>

    <view class="test-container">
      <view class="test-section">
        <view class="section-title">创建测试订单</view>
        <view class="section-desc">用于测试订单列表和支付功能</view>
        
        <button class="test-btn" @click="createTestOrder(1)">
          创建预备红娘订单（¥99）
        </button>
        
        <button class="test-btn" @click="createTestOrder(2)">
          创建正式红娘订单（¥199）
        </button>
        
        <button class="test-btn" @click="createTestOrder(3)">
          创建金牌红娘订单（¥299）
        </button>
        
        <button class="test-btn" @click="goToOrderList">
          查看订单列表
        </button>
      </view>

      <view class="result-section" v-if="testResult">
        <view class="section-title">操作结果</view>
        <view class="result-content">
          <pre>{{ testResult }}</pre>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      testResult: ''
    }
  },
  
  methods: {
    // 创建测试订单
    async createTestOrder(level) {
      try {
        this.testResult = '正在创建测试订单...'
        
        // 模拟创建订单的数据
        const orderData = {
          orderNo: 'MM' + Date.now(),
          serviceType: 1,
          serviceTypeText: '单次牵线',
          serviceDesc: '牵线服务 - 专业红娘为您匹配心仪对象',
          amount: level === 1 ? 99 : level === 2 ? 199 : 299,
          payAmount: level === 1 ? 99 : level === 2 ? 199 : 299,
          orderStatus: 0,
          orderStatusText: '待支付',
          matchmakerNickname: level === 1 ? '预备红娘小王' : level === 2 ? '正式红娘小李' : '金牌红娘小张',
          createdAt: new Date().toISOString()
        }
        
        // 存储到本地（模拟订单数据）
        let orders = uni.getStorageSync('testOrders') || []
        orders.push(orderData)
        uni.setStorageSync('testOrders', orders)
        
        this.testResult = '测试订单创建成功！\n' + JSON.stringify(orderData, null, 2)
        
        uni.showToast({
          title: '测试订单创建成功',
          icon: 'success'
        })
        
      } catch (error) {
        this.testResult = '创建失败: ' + error.message
        uni.showToast({
          title: '创建失败',
          icon: 'error'
        })
      }
    },
    
    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
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
  margin-bottom: 15rpx;
}

.section-desc {
  font-size: 26rpx;
  color: #666;
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
