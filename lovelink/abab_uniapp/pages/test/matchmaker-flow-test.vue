<template>
  <view class="test-page">
    <view class="header">
      <text class="title">红娘端功能测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">红娘申请管理测试</view>
      
      <button class="test-btn" @click="testGetRequests">1. 获取申请列表</button>
      <button class="test-btn" @click="testAcceptRequest">2. 接受申请</button>
      <button class="test-btn" @click="testRejectRequest">3. 拒绝申请（退款）</button>
      <button class="test-btn" @click="testPendingCount">4. 获取待处理数量</button>
    </view>

    <view class="test-section">
      <view class="section-title">页面跳转测试</view>
      
      <button class="nav-btn" @click="goToManageRequests">申请管理页面</button>
      <button class="nav-btn" @click="goToArrangeDates">约会安排页面</button>
    </view>

    <view class="test-section">
      <view class="section-title">测试结果</view>
      <textarea class="result-area" v-model="testResult" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">模拟数据</view>
      <view class="form-group">
        <text class="form-label">申请ID:</text>
        <input class="form-input" v-model="testRequestId" placeholder="输入申请ID" />
      </view>
      <view class="form-group">
        <text class="form-label">拒绝原因:</text>
        <textarea class="form-textarea" v-model="rejectReason" placeholder="输入拒绝原因" />
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      testResult: '等待测试...\n',
      testRequestId: '1',
      rejectReason: '经过评估，双方匹配度不高，建议寻找更合适的对象。'
    }
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testResult += `[${timestamp}] ${message}\n`
    },

    // 测试获取申请列表
    async testGetRequests() {
      this.addLog('开始测试获取申请列表...')
      
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
        
        this.addLog(`申请列表结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          const requests = result.data.records || []
          this.addLog(`✅ 获取申请列表成功！共 ${requests.length} 个已付费申请`)
          
          requests.forEach((request, index) => {
            this.addLog(`申请${index + 1}: ID=${request.requestId} 状态=${request.requestStatus} 用户=${request.applicantUser?.nickname}`)
          })
        } else {
          this.addLog(`❌ 获取申请列表失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 获取申请列表异常: ${error.message}`)
      }
    },

    // 测试接受申请
    async testAcceptRequest() {
      if (!this.testRequestId) {
        this.addLog('❌ 请先输入申请ID')
        return
      }
      
      this.addLog(`开始测试接受申请 ${this.testRequestId}...`)
      
      try {
        const token = uni.getStorageSync('token') || ''
        const data = {
          requestId: parseInt(this.testRequestId),
          action: 1 // 1-接受
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/requests/handle',
            method: 'POST',
            data: data,
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        this.addLog(`接受申请结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 申请接受成功！`)
          this.addLog(`消息: ${result.message}`)
        } else {
          this.addLog(`❌ 申请接受失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 接受申请异常: ${error.message}`)
      }
    },

    // 测试拒绝申请（退款）
    async testRejectRequest() {
      if (!this.testRequestId) {
        this.addLog('❌ 请先输入申请ID')
        return
      }
      
      this.addLog(`开始测试拒绝申请 ${this.testRequestId}（将自动退款）...`)
      
      try {
        const token = uni.getStorageSync('token') || ''
        const data = {
          requestId: parseInt(this.testRequestId),
          action: 2, // 2-拒绝
          rejectReason: this.rejectReason
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/requests/handle',
            method: 'POST',
            data: data,
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        this.addLog(`拒绝申请结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 申请拒绝成功！`)
          this.addLog(`消息: ${result.message}`)
          if (result.message.includes('退还')) {
            this.addLog(`💰 退款已自动处理`)
          }
        } else {
          this.addLog(`❌ 申请拒绝失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 拒绝申请异常: ${error.message}`)
      }
    },

    // 测试获取待处理数量
    async testPendingCount() {
      this.addLog('开始测试获取待处理申请数量...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9004/matchmaker/requests/pending-count',
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        this.addLog(`待处理数量结果: ${JSON.stringify(result, null, 2)}`)
        
        if (result.code === 200) {
          this.addLog(`✅ 获取待处理数量成功！当前有 ${result.data} 个待处理申请`)
        } else {
          this.addLog(`❌ 获取待处理数量失败: ${result.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 获取待处理数量异常: ${error.message}`)
      }
    },

    // 跳转到申请管理页面
    goToManageRequests() {
      uni.navigateTo({
        url: '/pages/matchmaker/manage-requests'
      })
    },

    // 跳转到约会安排页面
    goToArrangeDates() {
      uni.navigateTo({
        url: '/pages/matchmaker/arrange-dates'
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

.form-textarea {
  width: 100%;
  height: 120rpx;
  border: 2rpx solid #e9ecef;
  border-radius: 8rpx;
  padding: 16rpx;
  font-size: 28rpx;
  resize: none;
}
</style>
