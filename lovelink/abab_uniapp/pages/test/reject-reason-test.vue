<template>
  <view class="test-page">
    <view class="header">
      <text class="title">拒绝原因功能测试</text>
    </view>

    <view class="test-section">
      <view class="section-title">测试步骤</view>
      
      <view class="step-card">
        <view class="step-title">步骤1: 获取约会安排</view>
        <button class="test-btn" @click="loadDateArrangements">获取我的约会安排</button>
        <view class="step-result">
          <text v-if="arrangements.length > 0">✅ 找到 {{arrangements.length}} 个约会安排</text>
          <text v-else>⏳ 暂无约会安排</text>
        </view>
      </view>

      <view class="step-card" v-if="arrangements.length > 0">
        <view class="step-title">步骤2: 选择要拒绝的约会</view>
        <view class="arrangement-list">
          <view 
            v-for="arrangement in arrangements" 
            :key="arrangement.arrangementId"
            class="arrangement-item"
            :class="{ selected: selectedArrangement?.arrangementId === arrangement.arrangementId }"
            @click="selectArrangement(arrangement)">
            <view class="arrangement-info">
              <text class="info-text">ID: {{arrangement.arrangementId}}</text>
              <text class="info-text">时间: {{formatDateTime(arrangement.dateTime)}}</text>
              <text class="info-text">地点: {{arrangement.dateLocation}}</text>
              <text class="info-text">状态: {{getStatusText(arrangement.arrangementStatus)}}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="step-card" v-if="selectedArrangement">
        <view class="step-title">步骤3: 选择拒绝原因</view>
        <view class="reason-options">
          <view 
            v-for="(reason, index) in rejectReasons" 
            :key="index"
            class="reason-option"
            :class="{ active: selectedReason === reason.value }"
            @click="selectReason(reason.value)">
            <view class="option-radio">
              <view v-if="selectedReason === reason.value" class="radio-checked">●</view>
            </view>
            <text class="option-text">{{ reason.label }}</text>
          </view>
        </view>
        
        <view v-if="selectedReason === 'other'" class="custom-reason">
          <textarea 
            v-model="customReason"
            placeholder="请输入其他原因..."
            class="reason-input"
            maxlength="200">
          </textarea>
        </view>
      </view>

      <view class="step-card" v-if="selectedArrangement && selectedReason">
        <view class="step-title">步骤4: 测试拒绝功能</view>
        <button class="test-btn reject-btn" @click="testRejectWithReason">发送拒绝请求</button>
        <view class="step-result">
          <text v-if="rejectResult">{{rejectResult}}</text>
        </view>
      </view>
    </view>

    <view class="test-section">
      <view class="section-title">测试日志</view>
      <textarea class="log-area" v-model="testLog" readonly></textarea>
      <button class="clear-btn" @click="clearLog">清空日志</button>
    </view>

    <view class="test-section">
      <view class="section-title">快速操作</view>
      <button class="nav-btn" @click="goToDateManagement">约会管理页面</button>
      <button class="nav-btn" @click="checkRejectReasons">查看拒绝原因记录</button>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      arrangements: [],
      selectedArrangement: null,
      selectedReason: '',
      customReason: '',
      rejectResult: '',
      testLog: '等待测试...\n',
      rejectReasons: [
        { label: '时间不合适', value: 'time_conflict' },
        { label: '地点不方便', value: 'location_issue' },
        { label: '约会类型不喜欢', value: 'type_dislike' },
        { label: '个人原因', value: 'personal_reason' },
        { label: '对对方不感兴趣', value: 'not_interested' },
        { label: '其他原因', value: 'other' }
      ]
    }
  },

  onLoad() {
    this.addLog('拒绝原因功能测试页面加载完成')
  },

  methods: {
    // 添加日志
    addLog(message) {
      const timestamp = new Date().toLocaleTimeString()
      this.testLog += `[${timestamp}] ${message}\n`
    },

    // 获取约会安排
    async loadDateArrangements() {
      this.addLog('开始获取约会安排...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/my-arrangements`,
          method: 'GET',
          header: {
            'Content-Type': 'application/json',
            'token': token
          }
        })

        if (response.data.code === 200) {
          this.arrangements = response.data.data || []
          this.addLog(`✅ 成功获取 ${this.arrangements.length} 个约会安排`)
          
          // 只显示待确认的约会
          const pendingArrangements = this.arrangements.filter(item => item.arrangementStatus === 0)
          this.addLog(`📋 其中 ${pendingArrangements.length} 个待确认约会可以测试拒绝功能`)
        } else {
          this.addLog(`❌ 获取约会安排失败: ${response.data.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 获取约会安排异常: ${error.message}`)
      }
    },

    // 选择约会安排
    selectArrangement(arrangement) {
      this.selectedArrangement = arrangement
      this.addLog(`📌 选择约会: ID=${arrangement.arrangementId}, 地点=${arrangement.dateLocation}`)
    },

    // 选择拒绝原因
    selectReason(value) {
      this.selectedReason = value
      const reasonObj = this.rejectReasons.find(r => r.value === value)
      this.addLog(`📝 选择拒绝原因: ${reasonObj ? reasonObj.label : value}`)
      
      if (value !== 'other') {
        this.customReason = ''
      }
    },

    // 测试拒绝功能
    async testRejectWithReason() {
      if (!this.selectedArrangement || !this.selectedReason) {
        this.addLog('❌ 请先选择约会和拒绝原因')
        return
      }

      this.addLog('🚫 开始测试拒绝功能...')
      
      try {
        // 构建拒绝原因
        let rejectReason = ''
        if (this.selectedReason === 'other') {
          rejectReason = this.customReason.trim()
        } else {
          const reasonObj = this.rejectReasons.find(r => r.value === this.selectedReason)
          rejectReason = reasonObj ? reasonObj.label : '未知原因'
        }

        this.addLog(`📤 发送拒绝请求，原因: "${rejectReason}"`)

        // 构建请求URL
        let url = `${config.getBaseUrl()}/user/date/confirm/${this.selectedArrangement.arrangementId}?confirm=2`
        if (rejectReason) {
          url += `&rejectReason=${encodeURIComponent(rejectReason)}`
        }

        this.addLog(`🔗 请求URL: ${url}`)

        const response = await uni.request({
          url: url,
          method: 'POST',
          header: {
            'token': uni.getStorageSync('token')
          }
        })

        this.addLog(`📥 服务器响应: ${JSON.stringify(response.data)}`)

        if (response.data.code === 200) {
          this.rejectResult = '✅ 拒绝成功！'
          this.addLog('✅ 拒绝请求成功')
          
          // 重新加载约会安排查看状态变化
          setTimeout(() => {
            this.loadDateArrangements()
          }, 1000)
        } else {
          this.rejectResult = `❌ 拒绝失败: ${response.data.message}`
          this.addLog(`❌ 拒绝请求失败: ${response.data.message}`)
        }
      } catch (error) {
        this.rejectResult = `❌ 请求异常: ${error.message}`
        this.addLog(`❌ 拒绝请求异常: ${error.message}`)
      }
    },

    // 查看拒绝原因记录
    async checkRejectReasons() {
      this.addLog('🔍 查看拒绝原因记录...')
      
      try {
        const token = uni.getStorageSync('token') || ''
        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/my-arrangements`,
          method: 'GET',
          header: {
            'Content-Type': 'application/json',
            'token': token
          }
        })

        if (response.data.code === 200) {
          const arrangements = response.data.data || []
          const rejectedArrangements = arrangements.filter(item => 
            item.arrangementStatus === 2 && item.cancelReason
          )
          
          this.addLog(`📊 找到 ${rejectedArrangements.length} 个已拒绝的约会`)
          
          rejectedArrangements.forEach((item, index) => {
            this.addLog(`${index + 1}. ID=${item.arrangementId}, 拒绝原因: "${item.cancelReason}"`)
          })
        } else {
          this.addLog(`❌ 查看记录失败: ${response.data.message}`)
        }
      } catch (error) {
        this.addLog(`❌ 查看记录异常: ${error.message}`)
      }
    },

    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '未知时间'
      const date = new Date(dateTimeStr)
      return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
    },

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 0: return '待确认'
        case 1: return '已确认'
        case 2: return '已取消'
        case 3: return '已完成'
        default: return '未知状态'
      }
    },

    // 跳转到约会管理页面
    goToDateManagement() {
      uni.navigateTo({
        url: '/pages/date/date-management'
      })
    },

    // 清空日志
    clearLog() {
      this.testLog = ''
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

.step-card {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  border-left: 4rpx solid #667eea;
}

.step-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2d3748;
  margin-bottom: 15rpx;
}

.step-result {
  margin-top: 15rpx;
  font-size: 24rpx;
  color: #718096;
}

.arrangement-list {
  margin-top: 15rpx;
}

.arrangement-item {
  background: white;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 10rpx;
  border: 2rpx solid #f0f0f0;
  transition: all 0.3s;
}

.arrangement-item.selected {
  border-color: #667eea;
  background: #f8f9ff;
}

.arrangement-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-text {
  font-size: 24rpx;
  color: #2d3748;
}

.reason-options {
  margin-top: 15rpx;
}

.reason-option {
  display: flex;
  align-items: center;
  padding: 15rpx;
  margin-bottom: 10rpx;
  border-radius: 12rpx;
  border: 2rpx solid #f0f0f0;
  transition: all 0.3s;
}

.reason-option.active {
  border-color: #667eea;
  background: #f8f9ff;
}

.option-radio {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15rpx;
}

.reason-option.active .option-radio {
  border-color: #667eea;
  background: #667eea;
}

.radio-checked {
  color: white;
  font-size: 16rpx;
}

.option-text {
  font-size: 24rpx;
  color: #333;
}

.custom-reason {
  margin-top: 15rpx;
}

.reason-input {
  width: 100%;
  min-height: 100rpx;
  padding: 15rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  resize: none;
}

.test-btn {
  width: 100%;
  height: 70rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 35rpx;
  font-size: 26rpx;
  margin-bottom: 15rpx;
}

.reject-btn {
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
}

.nav-btn {
  width: 100%;
  height: 70rpx;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  border: none;
  border-radius: 35rpx;
  font-size: 26rpx;
  margin-bottom: 15rpx;
}

.clear-btn {
  width: 100%;
  height: 60rpx;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 24rpx;
  margin-top: 15rpx;
}

.log-area {
  width: 100%;
  height: 300rpx;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 10rpx;
  padding: 15rpx;
  font-size: 22rpx;
  line-height: 1.5;
}
</style>
