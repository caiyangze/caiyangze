<template>
  <view class="test-page">
    <view class="header">
      <text class="title">牵线申请功能测试</text>
    </view>
    
    <view class="test-section">
      <view class="section-title">测试发起牵线申请</view>
      <view class="form-group">
        <text class="label">目标用户ID:</text>
        <input class="input" v-model="testForm.targetUserId" placeholder="请输入目标用户ID" type="number" />
      </view>
      <view class="form-group">
        <text class="label">申请留言:</text>
        <textarea 
          class="textarea" 
          v-model="testForm.requestMessage" 
          placeholder="请输入申请留言（至少10字）"
          maxlength="500"
        ></textarea>
      </view>
      <button class="test-btn" @click="testSubmitRequest">测试提交申请</button>
    </view>
    
    <view class="test-section">
      <view class="section-title">我的申请列表</view>
      <button class="test-btn" @click="testGetMyRequests">获取我的申请</button>
      <view class="request-list" v-if="myRequests.length > 0">
        <view class="request-item" v-for="request in myRequests" :key="request.requestId">
          <text class="request-info">ID: {{ request.requestId }}</text>
          <text class="request-info">目标用户: {{ request.targetUserId }}</text>
          <text class="request-info">状态: {{ formatStatus(request.requestStatus) }}</text>
          <text class="request-info">留言: {{ request.requestMessage }}</text>
          <button 
            class="cancel-btn" 
            v-if="request.requestStatus === 0"
            @click="testCancelRequest(request.requestId)"
          >
            取消申请
          </button>
        </view>
      </view>
    </view>
    
    <view class="test-section">
      <view class="section-title">接口测试</view>
      <button class="test-btn" @click="testAPI">测试牵线申请接口</button>
      <button class="test-btn" @click="testSimpleAPI">测试简单接口</button>
      <button class="test-btn" @click="testUserService">测试用户服务</button>
      <button class="test-btn" @click="testCreateOrder">测试创建订单</button>
    </view>

    <view class="test-section">
      <view class="section-title">快捷操作</view>
      <button class="test-btn" @click="goToRequestPage">跳转到申请页面</button>
      <button class="test-btn" @click="goToMyRequestsPage">跳转到我的申请页面</button>
    </view>
  </view>
</template>

<script>
import {
  submitMatchmakingRequest,
  getMyMatchmakingRequests,
  cancelMatchmakingRequest,
  formatRequestStatus,
  testMatchmakingAPI
} from '@/api/matchmaker'

export default {
  data() {
    return {
      testForm: {
        targetUserId: '',
        requestMessage: '你好，我对你很感兴趣，希望能够认识你。我是一个积极向上的人，喜欢运动和阅读，希望我们能有机会进一步了解。'
      },
      myRequests: []
    }
  },
  
  methods: {
    // 测试接口连通性
    async testAPI() {
      try {
        console.log('开始测试接口连通性...')
        const result = await testMatchmakingAPI()
        console.log('接口测试结果:', result)

        if (result && result.code === 200) {
          uni.showModal({
            title: '接口测试成功',
            content: result.message || result.data || '接口正常工作',
            showCancel: false
          })
        } else {
          uni.showModal({
            title: '接口测试失败',
            content: `错误码: ${result?.code || '未知'}\n错误信息: ${result?.message || '未知错误'}`,
            showCancel: false
          })
        }
      } catch (error) {
        console.error('接口测试失败:', error)

        let errorMsg = '未知错误'
        if (error.errMsg) {
          errorMsg = error.errMsg
        } else if (error.message) {
          errorMsg = error.message
        } else if (typeof error === 'string') {
          errorMsg = error
        }

        uni.showModal({
          title: '接口连接失败',
          content: `错误详情: ${errorMsg}\n\n请检查:\n1. 用户服务是否启动(9001端口)\n2. 网络连接是否正常\n3. 接口路径是否正确`,
          showCancel: false
        })
      }
    },

    // 测试简单接口
    async testSimpleAPI() {
      try {
        console.log('测试简单接口...')
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/test/hello',
            method: 'GET',
            success: (res) => {
              resolve(res.data)
            },
            fail: (err) => {
              reject(err)
            }
          })
        })

        console.log('简单接口测试结果:', result)
        uni.showModal({
          title: '简单接口测试',
          content: JSON.stringify(result),
          showCancel: false
        })
      } catch (error) {
        console.error('简单接口测试失败:', error)
        uni.showModal({
          title: '简单接口测试失败',
          content: error.errMsg || error.message || '未知错误',
          showCancel: false
        })
      }
    },

    // 测试用户服务
    async testUserService() {
      try {
        console.log('测试用户服务...')
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/test/matchmaking',
            method: 'GET',
            success: (res) => {
              resolve(res.data)
            },
            fail: (err) => {
              reject(err)
            }
          })
        })

        console.log('用户服务测试结果:', result)
        uni.showModal({
          title: '用户服务测试',
          content: JSON.stringify(result),
          showCancel: false
        })
      } catch (error) {
        console.error('用户服务测试失败:', error)
        uni.showModal({
          title: '用户服务测试失败',
          content: error.errMsg || error.message || '未知错误',
          showCancel: false
        })
      }
    },

    // 测试提交申请
    async testSubmitRequest() {
      if (!this.testForm.targetUserId) {
        uni.showToast({
          title: '请输入目标用户ID',
          icon: 'error'
        })
        return
      }
      
      if (!this.testForm.requestMessage || this.testForm.requestMessage.length < 10) {
        uni.showToast({
          title: '申请留言至少10字',
          icon: 'error'
        })
        return
      }
      
      try {
        const result = await submitMatchmakingRequest({
          targetUserId: parseInt(this.testForm.targetUserId),
          requestMessage: this.testForm.requestMessage
        })
        
        uni.showToast({
          title: result.message || '操作完成',
          icon: result.code === 200 ? 'success' : 'error'
        })
        
        if (result.code === 200) {
          // 清空表单
          this.testForm.targetUserId = ''
          this.testForm.requestMessage = '你好，我对你很感兴趣，希望能够认识你。我是一个积极向上的人，喜欢运动和阅读，希望我们能有机会进一步了解。'
          // 刷新列表
          this.testGetMyRequests()
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      }
    },
    
    // 测试获取我的申请
    async testGetMyRequests() {
      try {
        const result = await getMyMatchmakingRequests()
        
        if (result.code === 200) {
          this.myRequests = result.data || []
          uni.showToast({
            title: `获取到${this.myRequests.length}条申请`,
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: result.message || '获取失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('获取申请列表失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      }
    },
    
    // 测试取消申请
    async testCancelRequest(requestId) {
      try {
        const result = await cancelMatchmakingRequest(requestId)
        
        uni.showToast({
          title: result.message || '操作完成',
          icon: result.code === 200 ? 'success' : 'error'
        })
        
        if (result.code === 200) {
          // 刷新列表
          this.testGetMyRequests()
        }
      } catch (error) {
        console.error('取消申请失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      }
    },
    
    // 格式化状态
    formatStatus(status) {
      return formatRequestStatus(status)
    },

    // 测试创建订单
    async testCreateOrder() {
      try {
        console.log('开始测试创建订单...')

        const testData = {
          targetUserId: 1, // 测试目标用户ID
          matchmakerLevel: 2, // 正式红娘
          requestMessage: '这是一个测试申请，用于验证订单创建功能是否正常工作。'
        }

        const result = await submitMatchmakingRequest(testData)
        console.log('创建订单结果:', result)

        if (result && result.code === 200) {
          const orderData = result.data
          uni.showModal({
            title: '订单创建成功',
            content: `订单编号: ${orderData.orderNo}\n订单ID: ${orderData.orderId}\n服务费用: ¥${orderData.amount}\n红娘: ${orderData.matchmakerName || '系统分配'}\n\n是否跳转到订单详情页面？`,
            confirmText: '去查看',
            cancelText: '稍后',
            success: (res) => {
              if (res.confirm) {
                uni.navigateTo({
                  url: `/pages/matchmaker/order-detail?orderId=${orderData.orderId}`
                })
              }
            }
          })
        } else {
          uni.showModal({
            title: '订单创建失败',
            content: `错误码: ${result?.code || '未知'}\n错误信息: ${result?.message || '未知错误'}`,
            showCancel: false
          })
        }
      } catch (error) {
        console.error('创建订单失败:', error)

        let errorMsg = '未知错误'
        if (error.errMsg) {
          errorMsg = error.errMsg
        } else if (error.message) {
          errorMsg = error.message
        } else if (typeof error === 'string') {
          errorMsg = error
        }

        uni.showModal({
          title: '创建订单失败',
          content: `错误详情: ${errorMsg}\n\n请检查:\n1. 用户服务是否启动\n2. 数据库连接是否正常\n3. 目标用户是否存在`,
          showCancel: false
        })
      }
    },

    // 跳转到申请页面
    goToRequestPage() {
      const targetUserId = this.testForm.targetUserId || '2'
      uni.navigateTo({
        url: `/pages/matchmaker/request?targetUserId=${targetUserId}`
      })
    },
    
    // 跳转到我的申请页面
    goToMyRequestsPage() {
      uni.navigateTo({
        url: '/pages/matchmaker/my-requests'
      })
    }
  },
  
  onLoad() {
    // 页面加载时获取我的申请列表
    this.testGetMyRequests()
  }
}
</script>

<style scoped>
.test-page {
  padding: 20rpx;
  background: #f5f5f5;
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
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  border-left: 6rpx solid #667eea;
  padding-left: 20rpx;
}

.form-group {
  margin-bottom: 20rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.input, .textarea {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}

.textarea {
  min-height: 120rpx;
  resize: none;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.test-btn:last-child {
  margin-bottom: 0;
}

.request-list {
  margin-top: 20rpx;
}

.request-item {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 15rpx;
  border-left: 6rpx solid #667eea;
}

.request-info {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.cancel-btn {
  width: 150rpx;
  height: 60rpx;
  background: #ff4757;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 24rpx;
  margin-top: 10rpx;
}
</style>
