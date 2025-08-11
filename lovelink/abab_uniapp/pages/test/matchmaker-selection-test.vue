<template>
  <view class="test-page">
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">红娘选择功能测试</view>
      <view class="nav-right"></view>
    </view>

    <view class="test-container">
      <view class="test-section">
        <view class="section-title">测试功能</view>
        
        <button class="test-btn" @click="testAvailableMatchmakers">
          测试获取可用红娘列表（全部）
        </button>

        <button class="test-btn" @click="testAvailableMatchmakersByLevel(3)">
          测试获取金牌红娘列表
        </button>

        <button class="test-btn" @click="testAvailableMatchmakersByLevel(2)">
          测试获取正式红娘列表
        </button>

        <button class="test-btn" @click="testAvailableMatchmakersByLevel(1)">
          测试获取预备红娘列表
        </button>
        
        <button class="test-btn" @click="testSubmitWithMatchmaker">
          测试提交申请（指定红娘）
        </button>

        <button class="test-btn" @click="testSubmitSmartAssign">
          测试提交申请（智能分配金牌红娘）
        </button>
        
        <button class="test-btn" @click="goToRequestPage">
          跳转到申请页面测试
        </button>
      </view>

      <view class="result-section" v-if="testResult">
        <view class="section-title">测试结果</view>
        <view class="result-content">
          <pre>{{ testResult }}</pre>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getAvailableMatchmakers, submitMatchmakingRequest } from '@/api/matchmaker'

export default {
  data() {
    return {
      testResult: ''
    }
  },
  
  methods: {
    // 测试获取可用红娘列表
    async testAvailableMatchmakers() {
      try {
        this.testResult = '正在获取红娘列表（全部）...'
        const result = await getAvailableMatchmakers()
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },

    // 测试按等级获取红娘列表
    async testAvailableMatchmakersByLevel(level) {
      try {
        const levelText = level === 3 ? '金牌' : level === 2 ? '正式' : '预备'
        this.testResult = `正在获取${levelText}红娘列表...`
        const result = await getAvailableMatchmakers(level)
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 测试提交申请（指定红娘）
    async testSubmitWithMatchmaker() {
      try {
        this.testResult = '正在提交申请（指定红娘）...'
        const data = {
          targetUserId: 1,
          matchmakerId: 1,
          requestMessage: '这是一个测试申请，指定了红娘ID为1'
        }
        const result = await submitMatchmakingRequest(data)
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 测试提交申请（智能分配）
    async testSubmitSmartAssign() {
      try {
        this.testResult = '正在提交申请（智能分配金牌红娘）...'
        const data = {
          targetUserId: 1,
          matchmakerLevel: 3, // 金牌红娘
          requestMessage: '这是一个测试申请，智能分配金牌红娘'
        }
        const result = await submitMatchmakingRequest(data)
        this.testResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.testResult = '错误: ' + JSON.stringify(error, null, 2)
      }
    },
    
    // 跳转到申请页面
    goToRequestPage() {
      uni.navigateTo({
        url: '/pages/matchmaker/request?targetUserId=1'
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
