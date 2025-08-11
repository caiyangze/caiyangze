<template>
  <view class="test-page">
    <view class="header">
      <text class="title">支付状态同步测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">完整流程测试</view>
      
      <button class="test-btn" @click="testFullFlow">1. 完整流程测试</button>
      <button class="test-btn" @click="testCreateRequest">2. 创建申请</button>
      <button class="test-btn" @click="testPayOrder">3. 支付订单</button>
      <button class="test-btn" @click="testCheckMatchmakerView">4. 检查红娘视图</button>
    </view>

    <view class="test-section">
      <view class="section-title">状态检查</view>
      
      <button class="check-btn" @click="checkOrderStatus">检查订单状态</button>
      <button class="check-btn" @click="checkRequestStatus">检查申请状态</button>
      <button class="check-btn" @click="checkMatchmakerRequests">检查红娘申请列表</button>
    </view>

    <view class="test-section">
      <view class="section-title">测试数据</view>
      
      <view class="form-group">
        <text class="form-label">目标用户ID:</text>
        <input class="form-input" v-model="targetUserId" placeholder="输入目标用户ID" />
      </view>
      
      <view class="form-group">
        <text class="form-label">红娘等级:</text>
        <picker @change="onLevelChange" :value="levelIndex" :range="levels">
          <view class="picker">{{ levels[levelIndex] }}</view>
        </picker>
      </view>
      
      <view class="data-display">
        <text class="data-label">当前订单ID:</text>
        <text class="data-value">{{ currentOrderId || '无' }}</text>
      </view>
      
      <view class="data-display">
        <text class="data-label">当前申请ID:</text>
        <text class="data-value">{{ currentRequestId || '无' }}</text>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">快速跳转</view>
      <button class="nav-btn" @click="goToOrderList">订单列表</button>
      <button class="nav-btn" @click="goToMatchmakerManage">红娘申请管理</button>
      <button class="nav-btn" @click="goToRequestPage">牵线申请页面</button>
    </view>
  </view>
</template>

<script>
import { submitMatchmakingRequest, payMatchmakerOrder } from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n',
      targetUserId: '2',
      levels: ['预备红娘', '正式红娘', '金牌红娘'],
      levelIndex: 1,
      currentOrderId: null,
      currentRequestId: null
    }
  },

  computed: {
    currentLevel() {
      return this.levelIndex + 1 // 1-预备，2-正式，3-金牌
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 等级选择
    onLevelChange(e) {
      this.levelIndex = e.detail.value
      this.addLog(`红娘等级切换为: ${this.levels[this.levelIndex]}`)
    },

    // 完整流程测试
    async testFullFlow() {
      this.addLog('开始完整流程测试...')
      this.addLog('='.repeat(50))
      
      try {
        // 1. 创建申请
        await this.testCreateRequest()
        
        if (!this.currentOrderId) {
          this.addLog('❌ 创建申请失败，无法继续')
          return
        }
        
        // 等待1秒
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 2. 支付订单
        await this.testPayOrder()
        
        // 等待1秒
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 3. 检查红娘视图
        await this.testCheckMatchmakerView()
        
        this.addLog('='.repeat(50))
        this.addLog('✅ 完整流程测试完成')
      } catch (error) {
        this.addLog(`❌ 完整流程测试失败: ${error.message}`)
      }
    },

    // 创建申请
    async testCreateRequest() {
      this.addLog('步骤1: 创建牵线申请...')
      
      const requestData = {
        targetUserId: parseInt(this.targetUserId),
        matchmakerLevel: this.currentLevel,
        requestMessage: `测试申请 - ${new Date().toLocaleTimeString()}`
      }
      
      try {
        const result = await submitMatchmakingRequest(requestData)
        this.addLog(`创建申请结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.currentOrderId = result.data.orderId
          this.currentRequestId = result.data.requestId
          this.addLog(`✅ 申请创建成功`)
          this.addLog(`订单ID: ${this.currentOrderId}`)
          this.addLog(`申请ID: ${this.currentRequestId}`)
          this.addLog(`申请状态: 待支付(-1)`)
        } else {
          this.addLog(`❌ 申请创建失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 创建申请异常: ${error.message}`)
      }
    },

    // 支付订单
    async testPayOrder() {
      if (!this.currentOrderId) {
        this.addLog('❌ 请先创建申请')
        return
      }
      
      this.addLog('步骤2: 支付订单...')
      
      try {
        const result = await payMatchmakerOrder(this.currentOrderId, 3) // 虚拟币支付
        this.addLog(`支付结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 支付成功`)
          this.addLog(`订单状态应该变为: 已支付(1)`)
          this.addLog(`申请状态应该变为: 待处理(0)`)
        } else {
          this.addLog(`❌ 支付失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 支付异常: ${error.message}`)
      }
    },

    // 检查红娘视图
    async testCheckMatchmakerView() {
      this.addLog('步骤3: 检查红娘申请管理视图...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/requests/list?pageNum=1&pageSize=10',
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        this.addLog(`红娘申请列表: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          const requests = result.data.records || []
          this.addLog(`✅ 获取红娘申请列表成功，共 ${requests.length} 条`)
          
          // 查找我们刚才创建的申请
          const myRequest = requests.find(r => r.requestId === this.currentRequestId)
          if (myRequest) {
            this.addLog(`✅ 找到我们的申请记录`)
            this.addLog(`申请状态: ${myRequest.requestStatus}`)
            this.addLog(`申请用户: ${myRequest.applicantUser?.nickname}`)
            
            if (myRequest.requestStatus === 0) {
              this.addLog(`✅ 申请状态正确（待处理），红娘可以看到`)
            } else {
              this.addLog(`❌ 申请状态错误（${myRequest.requestStatus}），红娘看不到`)
            }
          } else {
            this.addLog(`❌ 没有找到我们的申请记录，可能状态同步失败`)
          }
        } else {
          this.addLog(`❌ 获取红娘申请列表失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查红娘视图异常: ${error.message}`)
      }
    },

    // 检查订单状态
    async checkOrderStatus() {
      if (!this.currentOrderId) {
        this.addLog('❌ 没有当前订单ID')
        return
      }
      
      this.addLog('检查订单状态...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: `http://localhost:9001/user/matchmaker-order/detail/${this.currentOrderId}`,
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          this.addLog(`✅ 订单状态: ${result.data.orderStatus} (${result.data.orderStatusText})`)
          this.addLog(`支付时间: ${result.data.payTime || '未支付'}`)
        } else {
          this.addLog(`❌ 获取订单状态失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查订单状态异常: ${error.message}`)
      }
    },

    // 检查申请状态
    async checkRequestStatus() {
      this.addLog('检查申请状态...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking-request/my-requests',
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          const requests = result.data || []
          this.addLog(`✅ 我的申请列表，共 ${requests.length} 条`)
          
          if (this.currentRequestId) {
            const myRequest = requests.find(r => r.requestId === this.currentRequestId)
            if (myRequest) {
              this.addLog(`✅ 找到申请记录，状态: ${myRequest.requestStatus}`)
            } else {
              this.addLog(`❌ 没有找到申请记录`)
            }
          }
        } else {
          this.addLog(`❌ 获取申请状态失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查申请状态异常: ${error.message}`)
      }
    },

    // 检查红娘申请列表
    async checkMatchmakerRequests() {
      await this.testCheckMatchmakerView()
    },

    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
      })
    },

    // 跳转到红娘申请管理
    goToMatchmakerManage() {
      uni.navigateTo({
        url: '/pages/matchmaker/manage-requests'
      })
    },

    // 跳转到申请页面
    goToRequestPage() {
      uni.navigateTo({
        url: `/pages/matchmaker/request?targetUserId=${this.targetUserId}`
      })
    },

    // 清空日志
    clearLog() {
      this.testResult = ''
      this.currentOrderId = null
      this.currentRequestId = null
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

.test-btn, .check-btn, .nav-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.check-btn {
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

.data-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.data-display:last-child {
  border-bottom: none;
}

.data-label {
  font-size: 28rpx;
  color: #666;
}

.data-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}
</style>
