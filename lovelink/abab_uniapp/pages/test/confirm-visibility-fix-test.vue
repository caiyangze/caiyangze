<template>
  <view class="test-page">
    <view class="header">
      <text class="title">牵线确认可见性修复测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">完整流程测试</view>
      
      <button class="test-btn" @click="testFullFlow">完整流程测试</button>
      <button class="test-btn" @click="step1CreateAndPay">步骤1: 创建并支付申请</button>
      <button class="test-btn" @click="step2CheckBeforeProcess">步骤2: 红娘处理前检查</button>
      <button class="test-btn" @click="step3MatchmakerAccept">步骤3: 红娘接受申请</button>
      <button class="test-btn" @click="step4CheckAfterProcess">步骤4: 红娘处理后检查</button>
    </view>

    <view class="test-section">
      <view class="section-title">状态检查</view>
      
      <button class="check-btn" @click="checkConfirmHistory">检查确认历史</button>
      <button class="check-btn" @click="checkPendingConfirm">检查待确认申请</button>
      <button class="check-btn" @click="checkMatchmakerView">检查红娘视图</button>
      <button class="check-btn" @click="checkRequestStatus">检查申请状态</button>
    </view>

    <view class="test-section">
      <view class="section-title">测试数据</view>
      
      <view class="data-display">
        <text class="data-label">当前申请ID:</text>
        <text class="data-value">{{ currentRequestId || '无' }}</text>
      </view>
      
      <view class="data-display">
        <text class="data-label">当前订单ID:</text>
        <text class="data-value">{{ currentOrderId || '无' }}</text>
      </view>
      
      <view class="data-display">
        <text class="data-label">申请状态:</text>
        <text class="data-value">{{ getStatusText(currentStatus) }}</text>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">预期行为说明</view>
      
      <view class="behavior-item">
        <text class="behavior-status">状态 -1 (待支付):</text>
        <text class="behavior-desc">用户2看不到，红娘看不到</text>
      </view>
      
      <view class="behavior-item">
        <text class="behavior-status">状态 0 (待处理):</text>
        <text class="behavior-desc">用户2看不到，红娘可以看到</text>
      </view>
      
      <view class="behavior-item">
        <text class="behavior-status">状态 1 (已接受):</text>
        <text class="behavior-desc">用户2可以看到，红娘可以看到</text>
      </view>
      
      <view class="behavior-item">
        <text class="behavior-status">状态 2 (已拒绝):</text>
        <text class="behavior-desc">用户2可以看到，红娘可以看到</text>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">快速跳转</view>
      <button class="nav-btn" @click="goToConfirmPage">牵线确认页面</button>
      <button class="nav-btn" @click="goToMatchmakerManage">红娘申请管理</button>
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
      currentOrderId: null,
      currentStatus: null
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
        '-1': '待支付',
        '0': '待处理',
        '1': '已接受',
        '2': '已拒绝'
      }
      return status !== null ? `${status} (${statusMap[status] || '未知'})` : '未知'
    },

    // 完整流程测试
    async testFullFlow() {
      this.addLog('开始完整流程测试...')
      this.addLog('='.repeat(60))
      
      try {
        await this.step1CreateAndPay()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step2CheckBeforeProcess()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step3MatchmakerAccept()
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        await this.step4CheckAfterProcess()
        
        this.addLog('='.repeat(60))
        this.addLog('✅ 完整流程测试完成')
      } catch (error) {
        this.addLog(`❌ 完整流程测试失败: ${error.message}`)
      }
    },

    // 步骤1: 创建并支付申请
    async step1CreateAndPay() {
      this.addLog('步骤1: 创建并支付申请...')
      
      // 创建申请
      const requestData = {
        targetUserId: 2,
        matchmakerLevel: 2,
        requestMessage: `可见性测试申请 - ${new Date().toLocaleTimeString()}`
      }
      
      try {
        const createResult = await submitMatchmakingRequest(requestData)
        if (createResult.code === 200) {
          this.currentRequestId = createResult.data.requestId
          this.currentOrderId = createResult.data.orderId
          this.currentStatus = -1
          this.addLog(`✅ 申请创建成功，状态: 待支付(-1)`)
          
          // 支付订单
          const payResult = await payMatchmakerOrder(this.currentOrderId, 3)
          if (payResult.code === 200) {
            this.currentStatus = 0
            this.addLog(`✅ 支付成功，状态应该变为: 待处理(0)`)
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

    // 步骤2: 红娘处理前检查
    async step2CheckBeforeProcess() {
      this.addLog('步骤2: 红娘处理前检查用户2的可见性...')
      
      await this.checkConfirmHistory()
      
      this.addLog('预期结果: 用户2应该看不到申请（状态0，待处理）')
    },

    // 步骤3: 红娘接受申请
    async step3MatchmakerAccept() {
      if (!this.currentRequestId) {
        this.addLog('❌ 请先创建申请')
        return
      }
      
      this.addLog('步骤3: 模拟红娘接受申请...')
      
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
          this.addLog(`✅ 红娘接受申请成功，状态变为: 已接受(1)`)
        } else {
          this.addLog(`❌ 红娘接受申请失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 步骤3异常: ${error.message}`)
      }
    },

    // 步骤4: 红娘处理后检查
    async step4CheckAfterProcess() {
      this.addLog('步骤4: 红娘处理后检查用户2的可见性...')
      
      await this.checkConfirmHistory()
      await this.checkPendingConfirm()
      
      this.addLog('预期结果: 用户2应该能看到申请（状态1，已接受）')
    },

    // 检查确认历史
    async checkConfirmHistory() {
      this.addLog('检查用户2的确认历史...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking/confirm/history',
            method: 'GET',
            data: {
              pageNum: 1,
              pageSize: 10
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
          const records = result.data.records || []
          this.addLog(`📋 确认历史: 共 ${records.length} 条记录`)
          
          if (this.currentRequestId) {
            const myRequest = records.find(r => r.requestId === this.currentRequestId)
            if (myRequest) {
              this.addLog(`✅ 找到我们的申请记录，状态: ${myRequest.requestStatus}`)
              
              if (this.currentStatus >= 1) {
                this.addLog(`✅ 正确：状态 >= 1，用户2可以看到`)
              } else {
                this.addLog(`❌ 错误：状态 < 1，用户2不应该看到`)
              }
            } else {
              if (this.currentStatus < 1) {
                this.addLog(`✅ 正确：状态 < 1，用户2看不到申请`)
              } else {
                this.addLog(`❌ 错误：状态 >= 1，用户2应该能看到申请`)
              }
            }
          }
        } else {
          this.addLog(`❌ 获取确认历史失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查确认历史异常: ${error.message}`)
      }
    },

    // 检查待确认申请
    async checkPendingConfirm() {
      this.addLog('检查用户2的待确认申请...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/matchmaking/confirm/pending',
            method: 'GET',
            data: {
              pageNum: 1,
              pageSize: 10
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
          const records = result.data.records || []
          this.addLog(`⏳ 待确认申请: 共 ${records.length} 条记录`)
          
          if (this.currentRequestId) {
            const myRequest = records.find(r => r.requestId === this.currentRequestId)
            if (myRequest) {
              this.addLog(`✅ 找到待确认申请，状态: ${myRequest.requestStatus}`)
            } else {
              if (this.currentStatus === 1) {
                this.addLog(`❌ 错误：状态为1（已接受），应该在待确认列表中`)
              } else {
                this.addLog(`✅ 正确：状态不是1，不在待确认列表中`)
              }
            }
          }
        } else {
          this.addLog(`❌ 获取待确认申请失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查待确认申请异常: ${error.message}`)
      }
    },

    // 检查红娘视图
    async checkMatchmakerView() {
      this.addLog('检查红娘申请管理视图...')
      
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
        
        if (result.code === 200) {
          const records = result.data.records || []
          this.addLog(`👩‍💼 红娘申请列表: 共 ${records.length} 条记录`)
          
          if (this.currentRequestId) {
            const myRequest = records.find(r => r.requestId === this.currentRequestId)
            if (myRequest) {
              this.addLog(`✅ 红娘可以看到申请，状态: ${myRequest.requestStatus}`)
            } else {
              this.addLog(`❌ 红娘看不到申请`)
            }
          }
        } else {
          this.addLog(`❌ 获取红娘申请列表失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 检查红娘视图异常: ${error.message}`)
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

    // 跳转到确认页面
    goToConfirmPage() {
      uni.navigateTo({
        url: '/pages/matchmaking/confirm'
      })
    },

    // 跳转到红娘申请管理
    goToMatchmakerManage() {
      uni.navigateTo({
        url: '/pages/matchmaker/manage-requests'
      })
    },

    // 清空日志
    clearLog() {
      this.testResult = ''
      this.currentRequestId = null
      this.currentOrderId = null
      this.currentStatus = null
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

.behavior-item {
  margin-bottom: 16rpx;
  padding: 16rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.behavior-status {
  display: block;
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.behavior-desc {
  font-size: 24rpx;
  color: #666;
}
</style>
