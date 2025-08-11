<template>
  <view class="feedback-container">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-left" @tap="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">约会反馈</text>
      <view class="nav-right"></view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner">⏳</view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 反馈表单 -->
    <view v-else-if="!hasSubmitted" class="feedback-form">
      <!-- 约会信息卡片 -->
      <view class="date-info-card">
        <view class="card-header">
          <text class="card-title">约会信息</text>
          <view class="date-status" :class="getStatusClass(dateInfo.arrangementStatus)">
            {{ getStatusText(dateInfo.arrangementStatus) }}
          </view>
        </view>
        
        <view class="date-details">
          <view class="detail-row">
            <text class="detail-label">约会时间：</text>
            <text class="detail-value">{{ formatDateTime(dateInfo.dateTime) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">约会地点：</text>
            <text class="detail-value">{{ dateInfo.dateLocation }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">约会类型：</text>
            <text class="detail-value">{{ getDateTypeName(dateInfo.dateType) }}</text>
          </view>
        </view>
      </view>

      <!-- 满意度评分 -->
      <view class="feedback-section">
        <view class="section-header">
          <text class="section-title">约会满意度</text>
          <text class="section-subtitle">请为这次约会打分</text>
        </view>
        
        <view class="rating-container">
          <view class="rating-stars">
            <view 
              v-for="star in 5" 
              :key="star"
              class="star"
              :class="{ active: star <= feedback.satisfactionScore }"
              @tap="setSatisfactionScore(star)">
              ⭐
            </view>
          </view>
          <text class="rating-text">{{ getSatisfactionText(feedback.satisfactionScore) }}</text>
        </view>
      </view>

      <!-- 红娘服务评分 -->
      <view class="feedback-section">
        <view class="section-header">
          <text class="section-title">红娘服务评分</text>
          <text class="section-subtitle">请为红娘的服务打分</text>
        </view>
        
        <view class="rating-container">
          <view class="rating-stars">
            <view 
              v-for="star in 5" 
              :key="star"
              class="star"
              :class="{ active: star <= feedback.matchmakerScore }"
              @tap="setMatchmakerScore(star)">
              ⭐
            </view>
          </view>
          <text class="rating-text">{{ getMatchmakerText(feedback.matchmakerScore) }}</text>
        </view>
      </view>

      <!-- 反馈内容 -->
      <view class="feedback-section">
        <view class="section-header">
          <text class="section-title">详细反馈</text>
          <text class="section-subtitle">分享您的约会体验</text>
        </view>
        
        <textarea 
          v-model="feedback.feedbackContent"
          placeholder="请分享您对这次约会的感受和建议..."
          class="feedback-textarea"
          maxlength="500">
        </textarea>
        <view class="char-count">{{ feedback.feedbackContent.length }}/500</view>
      </view>

      <!-- 再次约会意愿 -->
      <view class="feedback-section">
        <view class="section-header">
          <text class="section-title">再次约会意愿</text>
          <text class="section-subtitle">您是否愿意与对方再次约会？</text>
        </view>
        
        <view class="willing-options">
          <view 
            class="willing-option"
            :class="{ active: feedback.isWillingNext === 1 }"
            @tap="setWillingNext(1)">
            <view class="option-icon">💕</view>
            <text class="option-text">愿意</text>
          </view>
          <view 
            class="willing-option"
            :class="{ active: feedback.isWillingNext === 0 }"
            @tap="setWillingNext(0)">
            <view class="option-icon">🤝</view>
            <text class="option-text">不愿意</text>
          </view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-container">
        <button 
          class="submit-btn"
          :class="{ disabled: !canSubmit }"
          :disabled="!canSubmit"
          @tap="submitFeedback">
          提交反馈
        </button>
      </view>
    </view>

    <!-- 已提交状态 -->
    <view v-else class="submitted-container">
      <view class="submitted-icon">✅</view>
      <text class="submitted-title">反馈已提交</text>
      <text class="submitted-desc">感谢您的反馈，我们会持续改进服务质量</text>
      
      <!-- 显示已提交的反馈 -->
      <view v-if="submittedFeedback" class="submitted-feedback">
        <view class="feedback-summary">
          <view class="summary-item">
            <text class="summary-label">满意度：</text>
            <view class="summary-stars">
              <text v-for="star in submittedFeedback.satisfactionScore" :key="star" class="star active">⭐</text>
            </view>
          </view>
          <view class="summary-item">
            <text class="summary-label">红娘服务：</text>
            <view class="summary-stars">
              <text v-for="star in submittedFeedback.matchmakerScore" :key="star" class="star active">⭐</text>
            </view>
          </view>
          <view class="summary-item">
            <text class="summary-label">再次约会：</text>
            <text class="summary-value">{{ submittedFeedback.isWillingNext ? '愿意' : '不愿意' }}</text>
          </view>
        </view>
      </view>

      <button class="back-btn" @tap="goBack">返回</button>
    </view>
  </view>
</template>

<script>
import config from '@/api/config'

export default {
  data() {
    return {
      arrangementId: null,
      loading: true,
      hasSubmitted: false,
      dateInfo: null,
      submittedFeedback: null,
      feedback: {
        arrangementId: null,
        satisfactionScore: 0,
        matchmakerScore: 0,
        feedbackContent: '',
        isWillingNext: null
      },
      dateTypeNames: {
        1: '咖啡厅',
        2: '餐厅',
        3: '电影院',
        4: '其他'
      }
    }
  },

  computed: {
    canSubmit() {
      return this.feedback.satisfactionScore > 0 && 
             this.feedback.matchmakerScore > 0 && 
             this.feedback.isWillingNext !== null
    }
  },

  onLoad(options) {
    if (options.arrangementId) {
      this.arrangementId = parseInt(options.arrangementId)
      this.feedback.arrangementId = this.arrangementId
      this.loadData()
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'error'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },

  methods: {
    // 加载数据
    async loadData() {
      try {
        // 检查反馈状态
        await this.checkFeedbackStatus()
        
        if (!this.hasSubmitted) {
          // 获取约会信息
          await this.loadDateInfo()
        } else {
          // 获取已提交的反馈
          await this.loadSubmittedFeedback()
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    // 检查反馈状态
    async checkFeedbackStatus() {
      const response = await uni.request({
        url: `${config.getBaseUrl()}/user/date/feedback/check/${this.arrangementId}`,
        method: 'GET',
        header: {
          'token': uni.getStorageSync('token')
        }
      })

      if (response.data.code === 200) {
        this.hasSubmitted = response.data.data.hasSubmitted
      } else {
        throw new Error(response.data.message || '检查反馈状态失败')
      }
    },

    // 加载约会信息
    async loadDateInfo() {
      const response = await uni.request({
        url: `${config.getBaseUrl()}/user/date/arrangement/${this.arrangementId}`,
        method: 'GET',
        header: {
          'token': uni.getStorageSync('token')
        }
      })

      if (response.data.code === 200) {
        this.dateInfo = response.data.data
      } else {
        throw new Error(response.data.message || '获取约会信息失败')
      }
    },

    // 加载已提交的反馈
    async loadSubmittedFeedback() {
      const response = await uni.request({
        url: `${config.getBaseUrl()}/user/date/feedback/${this.arrangementId}`,
        method: 'GET',
        header: {
          'token': uni.getStorageSync('token')
        }
      })

      if (response.data.code === 200) {
        this.submittedFeedback = response.data.data
      }
    },

    // 设置满意度评分
    setSatisfactionScore(score) {
      this.feedback.satisfactionScore = score
    },

    // 设置红娘评分
    setMatchmakerScore(score) {
      this.feedback.matchmakerScore = score
    },

    // 设置再次约会意愿
    setWillingNext(willing) {
      this.feedback.isWillingNext = willing
    },

    // 提交反馈
    async submitFeedback() {
      if (!this.canSubmit) return

      try {
        uni.showLoading({ title: '提交中...' })

        const response = await uni.request({
          url: `${config.getBaseUrl()}/user/date/feedback/submit`,
          method: 'POST',
          data: this.feedback,
          header: {
            'token': uni.getStorageSync('token'),
            'Content-Type': 'application/json'
          }
        })

        if (response.data.code === 200) {
          uni.showToast({
            title: '提交成功',
            icon: 'success'
          })

          // 更新状态
          this.hasSubmitted = true
          this.submittedFeedback = this.feedback

        } else {
          uni.showToast({
            title: response.data.message || '提交失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('提交反馈失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 获取满意度文本
    getSatisfactionText(score) {
      const texts = ['', '很不满意', '不满意', '一般', '满意', '非常满意']
      return texts[score] || ''
    },

    // 获取红娘服务文本
    getMatchmakerText(score) {
      const texts = ['', '很差', '较差', '一般', '良好', '优秀']
      return texts[score] || ''
    },

    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return ''
      const date = new Date(dateTimeStr)
      return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
    },

    // 获取约会类型名称
    getDateTypeName(type) {
      return this.dateTypeNames[type] || '未知'
    },

    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 0: return 'pending'
        case 1: return 'confirmed'
        case 2: return 'cancelled'
        case 3: return 'completed'
        default: return ''
      }
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

    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
/* 全局重置 */
* {
  box-sizing: border-box;
}

/* 页面容器 */
.feedback-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-sizing: border-box;
  overflow-x: hidden;
  position: relative;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 20rpx 30rpx;
  padding-top: calc(20rpx + var(--status-bar-height, 0));
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  box-sizing: border-box;
}

.nav-left, .nav-right {
  width: 80rpx;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-spinner {
  font-size: 80rpx;
  margin-bottom: 30rpx;
}

.loading-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 反馈表单 */
.feedback-form {
  width: 100%;
  padding: 20rpx 30rpx 30rpx;
  box-sizing: border-box;
  max-width: 750rpx;
  margin: 0 auto;
}

/* 约会信息卡片 */
.date-info-card {
  width: 100%;
  background: white;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
  backdrop-filter: blur(10rpx);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #2d3748;
}

.date-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.date-status.confirmed {
  background: #d4edda;
  color: #155724;
}

.date-status.completed {
  background: #cce5ff;
  color: #0066cc;
}

.date-details {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.detail-row {
  display: flex;
  align-items: center;
}

.detail-label {
  font-size: 26rpx;
  color: #718096;
  width: 160rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #2d3748;
  font-weight: 500;
}

/* 反馈区块 */
.feedback-section {
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.section-header {
  margin-bottom: 25rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #2d3748;
  display: block;
  margin-bottom: 8rpx;
}

.section-subtitle {
  font-size: 24rpx;
  color: #718096;
}

/* 评分组件 */
.rating-container {
  text-align: center;
}

.rating-stars {
  display: flex;
  justify-content: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.star {
  font-size: 72rpx;
  color: #e2e8f0;
  transition: all 0.3s ease;
  cursor: pointer;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

.star:hover {
  transform: scale(1.05);
}

.star.active {
  color: #ffd700;
  transform: scale(1.15);
  text-shadow: 0 4rpx 12rpx rgba(255, 215, 0, 0.4);
  filter: drop-shadow(0 4rpx 8rpx rgba(255, 215, 0, 0.3));
}

.rating-text {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 500;
}

/* 反馈文本框 */
.feedback-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e2e8f0;
  border-radius: 16rpx;
  font-size: 26rpx;
  line-height: 1.6;
  resize: none;
  background: #f8f9fa;
  box-sizing: border-box;
}

.char-count {
  font-size: 22rpx;
  color: #718096;
  text-align: right;
  margin-top: 10rpx;
}

/* 意愿选择 */
.willing-options {
  display: flex;
  gap: 20rpx;
  width: 100%;
}

.willing-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 20rpx;
  border: 3rpx solid #e2e8f0;
  border-radius: 20rpx;
  transition: all 0.3s;
  cursor: pointer;
  box-sizing: border-box;
  min-width: 0;
}

.willing-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.option-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.option-text {
  font-size: 26rpx;
  font-weight: 500;
}

/* 提交按钮 */
.submit-container {
  padding: 30rpx 0;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: bold;
  box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.submit-btn:active::before {
  left: 100%;
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.5);
}

.submit-btn.disabled {
  background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
  color: #718096;
  box-shadow: none;
  cursor: not-allowed;
}

/* 已提交状态 */
.submitted-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 30rpx;
  text-align: center;
}

.submitted-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.submitted-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 15rpx;
}

.submitted-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40rpx;
  line-height: 1.6;
}

.submitted-feedback {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 40rpx;
  width: 100%;
  backdrop-filter: blur(10rpx);
}

.feedback-summary {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.summary-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.summary-stars {
  display: flex;
  gap: 5rpx;
}

.summary-value {
  font-size: 26rpx;
  color: white;
  font-weight: 500;
}

.back-btn {
  width: 300rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
}
</style>
