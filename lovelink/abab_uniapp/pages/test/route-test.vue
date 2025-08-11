<template>
  <view class="container">
    <view class="header">
      <text class="title">路由测试页面</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试约会相关页面</text>
      
      <button class="test-btn" @tap="testDateCenter">测试约会中心</button>
      <button class="test-btn" @tap="testDateReminders">测试约会提醒</button>
      <button class="test-btn" @tap="testDateManagement">测试约会管理</button>
      <button class="test-btn" @tap="testMatchmakerApply">测试红娘申请</button>
      <button class="test-btn" @tap="testMatchmakingConfirm">测试牵线确认</button>
    </view>
    
    <view class="result-section">
      <text class="section-title">测试结果</text>
      <view class="result-list">
        <view v-for="(result, index) in testResults" :key="index" class="result-item">
          <text :class="['result-text', result.success ? 'success' : 'error']">
            {{ result.message }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      testResults: []
    }
  },
  
  methods: {
    addResult(message, success = true) {
      this.testResults.push({
        message: `${new Date().toLocaleTimeString()}: ${message}`,
        success
      })
    },
    
    testDateCenter() {
      try {
        uni.navigateTo({
          url: '/pages/date/date-center',
          success: () => {
            this.addResult('约会中心页面跳转成功', true)
          },
          fail: (err) => {
            this.addResult(`约会中心页面跳转失败: ${err.errMsg}`, false)
          }
        })
      } catch (error) {
        this.addResult(`约会中心页面跳转异常: ${error.message}`, false)
      }
    },
    
    testDateReminders() {
      try {
        uni.navigateTo({
          url: '/pages/date/date-reminders',
          success: () => {
            this.addResult('约会提醒页面跳转成功', true)
          },
          fail: (err) => {
            this.addResult(`约会提醒页面跳转失败: ${err.errMsg}`, false)
          }
        })
      } catch (error) {
        this.addResult(`约会提醒页面跳转异常: ${error.message}`, false)
      }
    },
    
    testDateManagement() {
      try {
        uni.navigateTo({
          url: '/pages/date/date-management',
          success: () => {
            this.addResult('约会管理页面跳转成功', true)
          },
          fail: (err) => {
            this.addResult(`约会管理页面跳转失败: ${err.errMsg}`, false)
          }
        })
      } catch (error) {
        this.addResult(`约会管理页面跳转异常: ${error.message}`, false)
      }
    },
    
    testMatchmakerApply() {
      try {
        uni.navigateTo({
          url: '/pages/matchmaker/apply',
          success: () => {
            this.addResult('红娘申请页面跳转成功', true)
          },
          fail: (err) => {
            this.addResult(`红娘申请页面跳转失败: ${err.errMsg}`, false)
          }
        })
      } catch (error) {
        this.addResult(`红娘申请页面跳转异常: ${error.message}`, false)
      }
    },
    
    testMatchmakingConfirm() {
      try {
        uni.navigateTo({
          url: '/pages/matchmaking/confirm',
          success: () => {
            this.addResult('牵线确认页面跳转成功', true)
          },
          fail: (err) => {
            this.addResult(`牵线确认页面跳转失败: ${err.errMsg}`, false)
          }
        })
      } catch (error) {
        this.addResult(`牵线确认页面跳转异常: ${error.message}`, false)
      }
    }
  }
}
</script>

<style scoped>
.container {
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

.test-section, .result-section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background-color: #007aff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.test-btn:last-child {
  margin-bottom: 0;
}

.result-list {
  max-height: 400rpx;
  overflow-y: auto;
}

.result-item {
  padding: 10rpx 0;
  border-bottom: 1rpx solid #eee;
}

.result-item:last-child {
  border-bottom: none;
}

.result-text {
  font-size: 24rpx;
  line-height: 1.4;
}

.result-text.success {
  color: #52c41a;
}

.result-text.error {
  color: #ff4d4f;
}
</style>
