<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">VIP完整功能测试</text>
    </view>
    
    <view class="test-content">
      <!-- VIP状态显示 -->
      <view class="status-section">
        <text class="section-title">当前VIP状态</text>
        <view class="status-card" v-if="vipStatus">
          <view class="status-item">
            <text class="label">用户角色：</text>
            <text class="value" :class="getRoleClass(vipStatus.userRole)">
              {{ vipStatus.userRoleName }}
            </text>
          </view>
          <view class="status-item">
            <text class="label">VIP状态：</text>
            <text class="value" :class="vipStatus.isVip ? 'active' : 'inactive'">
              {{ vipStatus.isVip ? '已开通' : '未开通' }}
            </text>
          </view>
          <view class="status-item" v-if="vipStatus.vipExpireTime">
            <text class="label">过期时间：</text>
            <text class="value">{{ formatDate(vipStatus.vipExpireTime) }}</text>
          </view>
        </view>
        <button class="test-button" @click="checkVipStatus">刷新VIP状态</button>
      </view>
      
      <!-- 测试按钮区域 -->
      <view class="test-section">
        <text class="section-title">功能测试</text>
        <button class="test-button primary" @click="testCompleteFlow">🎯 测试完整VIP购买流程</button>
        <button class="test-button secondary" @click="testVipRecharge">💎 测试VIP充值页面</button>
        <button class="test-button" @click="testCreateOrder">📝 测试创建订单</button>
        <button class="test-button" @click="testPayment">💳 测试支付流程</button>
      </view>
      
      <!-- 测试结果显示 -->
      <view class="result-section" v-if="testResults.length > 0">
        <text class="section-title">测试结果</text>
        <view class="result-list">
          <view 
            v-for="(result, index) in testResults" 
            :key="index"
            class="result-item"
            :class="result.success ? 'success' : 'error'"
          >
            <text class="result-title">{{ result.title }}</text>
            <text class="result-message">{{ result.message }}</text>
            <text class="result-time">{{ result.time }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { 
  getUserVipStatus, 
  getVipPackages, 
  createVipOrder, 
  processVipPayment 
} from '@/api/vip.js'

export default {
  data() {
    return {
      vipStatus: null,
      testResults: [],
      currentOrder: null
    }
  },
  
  onLoad() {
    this.checkVipStatus()
  },
  
  methods: {
    // 检查VIP状态
    async checkVipStatus() {
      try {
        const response = await getUserVipStatus()
        if (response.code === 200) {
          this.vipStatus = response.data
          this.addTestResult('VIP状态查询', '成功获取VIP状态', true)
        } else {
          this.addTestResult('VIP状态查询', response.message || '查询失败', false)
        }
      } catch (error) {
        console.error('查询VIP状态失败:', error)
        this.addTestResult('VIP状态查询', '网络错误', false)
      }
    },
    
    // 测试完整VIP购买流程
    async testCompleteFlow() {
      this.addTestResult('完整流程测试', '开始测试完整VIP购买流程...', true)
      
      try {
        // 1. 获取套餐列表
        const packagesResponse = await getVipPackages()
        if (packagesResponse.code !== 200) {
          throw new Error('获取套餐列表失败')
        }
        this.addTestResult('获取套餐', '成功获取套餐列表', true)
        
        // 2. 创建订单（选择季度VIP）
        const orderResponse = await createVipOrder({ vipType: 2 })
        if (orderResponse.code !== 200) {
          throw new Error('创建订单失败')
        }
        this.currentOrder = orderResponse.data
        this.addTestResult('创建订单', `订单创建成功，订单号：${this.currentOrder.orderNo}`, true)
        
        // 3. 处理支付（使用微信支付）
        const payResponse = await processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: 1
        })
        if (payResponse.code !== 200) {
          throw new Error('支付处理失败')
        }
        this.addTestResult('支付处理', `支付成功，交易号：${payResponse.data.transactionId}`, true)
        
        // 4. 刷新VIP状态
        await this.checkVipStatus()
        
        this.addTestResult('完整流程', '✅ VIP购买流程测试完成！请检查数据库记录', true)
        
        // 显示成功提示
        uni.showModal({
          title: '测试完成',
          content: '完整VIP购买流程测试成功！\n\n请检查数据库：\n1. tb_vip_order 表的订单记录\n2. tb_user 表的用户角色更新\n3. tb_wallet_transaction 表的交易记录',
          showCancel: false
        })
        
      } catch (error) {
        console.error('完整流程测试失败:', error)
        this.addTestResult('完整流程', `测试失败：${error.message}`, false)
      }
    },
    
    // 测试VIP充值页面
    testVipRecharge() {
      uni.navigateTo({
        url: '/pages/vip/recharge'
      })
    },
    
    // 测试创建订单
    async testCreateOrder() {
      try {
        const response = await createVipOrder({ vipType: 1 })
        if (response.code === 200) {
          this.currentOrder = response.data
          this.addTestResult('创建订单', `订单创建成功，金额：¥${response.data.payAmount}`, true)
        } else {
          this.addTestResult('创建订单', response.message || '创建失败', false)
        }
      } catch (error) {
        console.error('创建订单失败:', error)
        this.addTestResult('创建订单', '网络错误', false)
      }
    },
    
    // 测试支付流程
    async testPayment() {
      if (!this.currentOrder) {
        this.addTestResult('支付测试', '请先创建订单', false)
        return
      }
      
      try {
        const response = await processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: 2 // 支付宝
        })
        if (response.code === 200) {
          this.addTestResult('支付测试', `支付成功，交易号：${response.data.transactionId}`, true)
          // 刷新VIP状态
          await this.checkVipStatus()
        } else {
          this.addTestResult('支付测试', response.message || '支付失败', false)
        }
      } catch (error) {
        console.error('支付测试失败:', error)
        this.addTestResult('支付测试', '网络错误', false)
      }
    },
    
    // 添加测试结果
    addTestResult(title, message, success) {
      this.testResults.unshift({
        title,
        message,
        success,
        time: new Date().toLocaleTimeString()
      })
      
      // 限制结果数量
      if (this.testResults.length > 10) {
        this.testResults.pop()
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '无'
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },

    // 获取角色样式类
    getRoleClass(userRole) {
      switch (userRole) {
        case 2: return 'vip'
        case 3: return 'matchmaker'
        case 4: return 'vip-matchmaker'
        default: return 'normal'
      }
    }
  }
}
</script>

<style scoped>
.test-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20rpx;
}

.test-header {
  text-align: center;
  padding: 40rpx 0;
}

.test-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
}

.test-content {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.status-section, .test-section, .result-section {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.status-card {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
  border-bottom: 1rpx solid #eee;
}

.status-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 24rpx;
  color: #666;
}

.value {
  font-size: 24rpx;
  font-weight: bold;
}

.value.vip {
  color: #FFD700;
}

.value.matchmaker {
  color: #ff6b6b;
}

.value.vip-matchmaker {
  background: linear-gradient(45deg, #FFD700, #ff6b6b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.value.normal {
  color: #999;
}

.value.active {
  color: #67C23A;
}

.value.inactive {
  color: #F56C6C;
}

.test-button {
  width: 100%;
  height: 80rpx;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
}

.test-button.primary {
  background: linear-gradient(45deg, #FFD700, #FFA500);
}

.test-button.secondary {
  background: linear-gradient(45deg, #FF6B9A, #FF8FA3);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.result-item {
  padding: 16rpx;
  border-radius: 8rpx;
  border-left: 4rpx solid;
}

.result-item.success {
  background: #f0f9ff;
  border-left-color: #67C23A;
}

.result-item.error {
  background: #fef2f2;
  border-left-color: #F56C6C;
}

.result-title {
  font-size: 24rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.result-message {
  font-size: 22rpx;
  color: #666;
  display: block;
  margin-bottom: 8rpx;
}

.result-time {
  font-size: 20rpx;
  color: #999;
  display: block;
}
</style>
