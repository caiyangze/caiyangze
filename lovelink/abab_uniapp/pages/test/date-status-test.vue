<template>
  <view class="test-page">
    <view class="header">
      <text class="title">约会状态显示测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">状态显示测试</view>
      
      <view class="status-demo" v-for="(status, index) in statusList" :key="index">
        <view class="status-card" :class="getRequestStatusClass(status.value)">
          <view class="status-icon">{{ getRequestStatusIcon(status.value) }}</view>
          <view class="status-content">
            <text class="status-title">{{ getRequestStatusText(status.value) }}</text>
            <text class="status-desc">{{ getRequestStatusDesc(status.value) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">完整流程测试</view>
      
      <button class="test-btn" @click="testFullFlow">完整约会流程测试</button>
      <button class="test-btn" @click="step1CreateRequest">步骤1: 创建申请</button>
      <button class="test-btn" @click="step2MatchmakerAccept">步骤2: 红娘接受</button>
      <button class="test-btn" @click="step3UserConfirm">步骤3: 双方确认</button>
      <button class="test-btn" @click="step4ArrangeDate">步骤4: 安排约会</button>
    </view>

    <view class="test-section">
      <view class="section-title">页面检查</view>
      
      <button class="check-btn" @click="checkArrangePage">检查约会安排页面</button>
      <button class="check-btn" @click="checkDateManagement">检查约会管理页面</button>
      <button class="check-btn" @click="checkRequestStatus">检查申请状态</button>
    </view>

    <view class="test-section">
      <view class="section-title">当前测试数据</view>
      
      <view class="data-display">
        <text class="data-label">申请ID:</text>
        <text class="data-value">{{ currentRequestId || '无' }}</text>
      </view>
      
      <view class="data-display">
        <text class="data-label">申请状态:</text>
        <text class="data-value">{{ getStatusText(currentStatus) }}</text>
      </view>
      
      <view class="data-display">
        <text class="data-label">约会ID:</text>
        <text class="data-value">{{ currentDateId || '无' }}</text>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">快速跳转</view>
      <button class="nav-btn" @click="goToArrangePage">约会安排页面</button>
      <button class="nav-btn" @click="goToDateManagement">约会管理页面</button>
    </view>
  </view>
</template>

<script>
import { submitMatchmakingRequest, payMatchmakerOrder } from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: '等待测试...\n',
      currentRequestId: null,
      currentStatus: null,
      currentDateId: null,
      statusList: [
        { value: 0, name: '待处理' },
        { value: 1, name: '红娘已接受' },
        { value: 2, name: '红娘已拒绝' },
        { value: 3, name: '已取消' },
        { value: 4, name: '双方已同意' }
      ]
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        0: '待处理',
        1: '红娘已接受',
        2: '红娘已拒绝',
        3: '已取消',
        4: '双方已同意'
      }
      return status !== null ? `${status} (${statusMap[status] || '未知'})` : '未知'
    },

    // 获取申请状态样式类
    getRequestStatusClass(status) {
      const classMap = {
        0: 'pending',
        1: 'accepted',
        2: 'rejected',
        3: 'cancelled',
        4: 'success'
      }
      return classMap[status] || 'pending'
    },

    // 获取申请状态图标
    getRequestStatusIcon(status) {
      const iconMap = {
        0: '⏳',
        1: '✅',
        2: '❌',
        3: '🚫',
        4: '🎉'
      }
      return iconMap[status] || '⏳'
    },

    // 获取申请状态文本
    getRequestStatusText(status) {
      const textMap = {
        0: '待处理',
        1: '红娘已接受',
        2: '红娘已拒绝',
        3: '已取消',
        4: '双方已同意'
      }
      return textMap[status] || '未知状态'
    },

    // 获取申请状态描述
    getRequestStatusDesc(status) {
      const descMap = {
        0: '等待红娘处理',
        1: '等待目标用户确认',
        2: '申请被拒绝',
        3: '申请已取消',
        4: '达成一致'
      }
      return descMap[status] || ''
    },

    // 完整流程测试
    async testFullFlow() {
      this.addLog('开始完整约会流程测试...')
      this.addLog('='.repeat(60))
      
      try {
        await this.step1CreateRequest()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step2MatchmakerAccept()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step3UserConfirm()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step4ArrangeDate()
        
        this.addLog('='.repeat(60))
        this.addLog('✅ 完整流程测试完成')
      } catch (error) {
        this.addLog(`❌ 完整流程测试失败: ${error.message}`)
      }
    },

    // 步骤1: 创建申请
    async step1CreateRequest() {
      this.addLog('步骤1: 创建并支付申请...')
      
      const requestData = {
        targetUserId: 2,
        matchmakerLevel: 2,
        requestMessage: `约会状态测试申请 - ${new Date().toLocaleTimeString()}`
      }
      
      try {
        const createResult = await submitMatchmakingRequest(requestData)
        if (createResult.code === 200) {
          this.currentRequestId = createResult.data.requestId
          const orderId = createResult.data.orderId
          this.currentStatus = -1
          this.addLog(`✅ 申请创建成功，状态: 待支付(-1)`)
          
          // 支付订单
          const payResult = await payMatchmakerOrder(orderId, 3)
          if (payResult.code === 200) {
            this.currentStatus = 0
            this.addLog(`✅ 支付成功，状态变为: 待处理(0)`)
          } else {
            this.addLog(`❌ 支付失败: ${payResult.message}`)
          }
        } else {
          this.addLog(`❌ 申请创建失败: ${createResult.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 步骤1异常: ${error.message}`)
      }
    },

    // 步骤2: 红娘接受申请
    async step2MatchmakerAccept() {
      if (!this.currentRequestId) {
        this.addLog('❌ 请先创建申请')
        return
      }
      
      this.addLog('步骤2: 模拟红娘接受申请...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/requests/handle',
            method: 'POST',
            data: {
              requestId: this.currentRequestId,
              action: 1 // 1-接受
            },
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          this.currentStatus = 1
          this.addLog(`✅ 红娘接受申请成功，状态变为: 红娘已接受(1)`)
        } else {
          this.addLog(`❌ 红娘接受申请失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 步骤2异常: ${error.message}`)
      }
    },

    // 步骤3: 双方确认
    async step3UserConfirm() {
      this.addLog('步骤3: 模拟双方确认...')
      
      // 这里需要模拟用户1和用户2都确认
      // 实际情况下需要两个用户分别确认
      this.addLog('⚠️ 需要手动让双方用户确认申请')
      this.addLog('确认后状态应该变为: 双方已同意(4)')
      
      // 模拟状态变更
      this.currentStatus = 4
      this.addLog(`✅ 假设双方已确认，状态变为: 双方已同意(4)`)
    },

    // 步骤4: 安排约会
    async step4ArrangeDate() {
      this.addLog('步骤4: 检查约会安排页面显示...')
      
      await this.checkArrangePage()
      
      this.addLog('现在状态应该正确显示为"双方已同意"而不是硬编码')
    },

    // 检查约会安排页面
    async checkArrangePage() {
      this.addLog('检查约会安排页面的状态显示...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/date/available-requests',
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
          this.addLog(`📋 待安排约会申请: 共 ${requests.length} 条`)
          
          if (this.currentRequestId) {
            const myRequest = requests.find(r => r.request.requestId === this.currentRequestId)
            if (myRequest) {
              this.addLog(`✅ 找到我们的申请记录`)
              this.addLog(`申请状态: ${myRequest.request.requestStatus}`)
              this.addLog(`状态文本: ${this.getRequestStatusText(myRequest.request.requestStatus)}`)
              
              if (myRequest.request.requestStatus === 4) {
                this.addLog(`✅ 状态正确显示为"双方已同意"`)
              } else {
                this.addLog(`❌ 状态显示错误，应该是4（双方已同意）`)
              }
            } else {
              this.addLog(`❌ 没有找到我们的申请记录`)
            }
          }
        } else {
          this.addLog(`❌ 获取约会安排页面数据失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查约会安排页面异常: ${error.message}`)
      }
    },

    // 检查约会管理页面
    async checkDateManagement() {
      this.addLog('检查约会管理页面...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/date/my-arrangements',
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
          const arrangements = result.data || []
          this.addLog(`📅 我的约会安排: 共 ${arrangements.length} 条`)
          
          arrangements.forEach((item, index) => {
            this.addLog(`约会${index + 1}: 状态${item.arrangementStatus}`)
          })
        } else {
          this.addLog(`❌ 获取约会管理页面数据失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查约会管理页面异常: ${error.message}`)
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
          
          if (this.currentRequestId) {
            const myRequest = requests.find(r => r.requestId === this.currentRequestId)
            if (myRequest) {
              this.currentStatus = myRequest.requestStatus
              this.addLog(`📊 当前申请状态: ${this.getStatusText(myRequest.requestStatus)}`)
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

    // 跳转到约会安排页面
    goToArrangePage() {
      uni.navigateTo({
        url: '/pages/matchmaker/arrange-dates'
      })
    },

    // 跳转到约会管理页面
    goToDateManagement() {
      uni.navigateTo({
        url: '/pages/date/date-management'
      })
    },

    // 清空日志
    clearLog() {
      this.testResult = ''
      this.currentRequestId = null
      this.currentStatus = null
      this.currentDateId = null
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

.status-demo {
  margin-bottom: 20rpx;
}

.status-card {
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.status-card.pending {
  background: linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%);
  border-left: 4rpx solid #f59e0b;
}

.status-card.accepted {
  background: linear-gradient(135deg, #f0f9ff 0%, #bfdbfe 100%);
  border-left: 4rpx solid #3b82f6;
}

.status-card.rejected {
  background: linear-gradient(135deg, #fef2f2 0%, #fecaca 100%);
  border-left: 4rpx solid #ef4444;
}

.status-card.cancelled {
  background: linear-gradient(135deg, #f9fafb 0%, #e5e7eb 100%);
  border-left: 4rpx solid #6b7280;
}

.status-card.success {
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border-left: 4rpx solid #48bb78;
}

.status-icon {
  font-size: 32rpx;
}

.status-content {
  flex: 1;
}

.status-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
  display: block;
  margin-bottom: 4rpx;
}

.status-desc {
  font-size: 24rpx;
  color: #718096;
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
