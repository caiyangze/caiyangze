<template>
  <view class="request-page">
    <!-- 导航栏 -->
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">发起牵线申请</view>
      <view class="nav-right"></view>
    </view>

    <!-- 目标用户信息 -->
    <view class="target-user-card" v-if="targetUser">
      <view class="user-avatar">
        <image :src="targetUser.avatarUrl || '/static/default-avatar.png'" mode="aspectFill"></image>
      </view>
      <view class="user-info">
        <view class="user-name">{{ targetUser.nickname }}</view>
        <view class="user-details">
          <text class="age">{{ targetUser.age || '未知' }}岁</text>
          <text class="location" v-if="targetUser.location">{{ targetUser.location }}</text>
        </view>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="form-container">
      <!-- 选择红娘 -->
      <view class="form-section">
        <view class="section-title">选择红娘</view>
        <view class="section-desc">选择分配方式，让系统智能分配或手动指定红娘</view>

        <!-- 分配方式选择 -->
        <view class="assign-selection">
          <view class="assign-options">
            <view
              class="assign-option"
              :class="{ active: assignType === 'smart' }"
              @click="selectAssignType('smart')"
            >
              <view class="assign-icon">🧠</view>
              <view class="assign-content">
                <text class="assign-name">智能分配</text>
                <text class="assign-desc">系统根据专业度和工作负载自动分配最优红娘</text>
              </view>
            </view>

            <view
              class="assign-option"
              :class="{ active: assignType === 'manual' }"
              @click="selectAssignType('manual')"
            >
              <view class="assign-icon">👆</view>
              <view class="assign-content">
                <text class="assign-name">指定红娘</text>
                <text class="assign-desc" v-if="!selectedMatchmaker">从所有红娘中选择您心仪的红娘</text>
                <text class="assign-desc selected" v-else>已选择：{{ selectedMatchmaker.nickname }}（{{ selectedMatchmaker.matchmakerLevelText }}）</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 专业红娘服务说明 -->
        <view class="service-info" v-if="assignType === 'smart'">
          <view class="service-title">专业红娘服务</view>
          <view class="service-desc">我们的专业红娘将为您提供一对一的牵线服务</view>
          <view class="service-features">
            <view class="feature-item">
              <text class="feature-icon">👥</text>
              <text class="feature-text">专业匹配分析</text>
            </view>
            <view class="feature-item">
              <text class="feature-icon">💬</text>
              <text class="feature-text">双方沟通协调</text>
            </view>
            <view class="feature-item">
              <text class="feature-icon">📅</text>
              <text class="feature-text">约会安排指导</text>
            </view>
            <view class="feature-item">
              <text class="feature-icon">💝</text>
              <text class="feature-text">贴心服务保障</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 支付方式选择 -->
      <view class="payment-section">
        <view class="payment-title">选择支付方式</view>
        <view class="payment-options">
          <view
            class="payment-option"
            :class="{ active: paymentType === 'virtual_coin' }"
            @click="selectPaymentType('virtual_coin')">
            <view class="payment-icon">💰</view>
            <view class="payment-info">
              <text class="payment-name">虚拟币支付</text>
              <text class="payment-amount">699虚拟币</text>
              <text class="payment-desc" v-if="userBalance !== null">
                余额：{{ userBalance }}币
                <text class="insufficient-tip" v-if="userBalance < 699">（余额不足）</text>
              </text>
            </view>
            <view class="payment-check" v-if="paymentType === 'virtual_coin'">✓</view>
          </view>

          <view
            class="payment-option"
            :class="{ active: paymentType === 'cash' }"
            @click="selectPaymentType('cash')">
            <view class="payment-icon">💳</view>
            <view class="payment-info">
              <text class="payment-name">现金支付</text>
              <text class="payment-amount">¥69.9</text>
              <text class="payment-desc">支持微信、支付宝</text>
            </view>
            <view class="payment-check" v-if="paymentType === 'cash'">✓</view>
          </view>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title">申请留言</view>
        <view class="section-desc">请简单介绍一下自己，说明为什么希望认识对方</view>
        <textarea
          class="message-input"
          v-model="formData.requestMessage"
          placeholder="请输入申请留言，让红娘更好地了解您的想法..."
          maxlength="500"
          :show-count="true"
        ></textarea>
        <view class="input-tip">建议至少填写10个字，让申请更有诚意</view>
      </view>

      <!-- 温馨提示 -->
      <view class="tips-section">
        <view class="tips-title">温馨提示</view>
        <view class="tips-content">
          <view class="tip-item">• 申请提交后，系统会为您分配专业红娘</view>
          <view class="tip-item">• 红娘会根据双方情况安排合适的见面方式</view>
          <view class="tip-item">• 请保持手机畅通，红娘会及时与您联系</view>
          <view class="tip-item">• 虚拟币支付：如目标用户拒绝，退还70%费用</view>
          <view class="tip-item">• 现金支付：需完成支付后红娘开始服务</view>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-container">
      <button
        class="submit-btn"
        :class="{ 'disabled': !canSubmit || (paymentType === 'virtual_coin' && userBalance < 699) }"
        @click="submitRequest"
        :disabled="!canSubmit || isSubmitting || (paymentType === 'virtual_coin' && userBalance < 699)"
      >
        {{ getSubmitButtonText() }}
      </button>
    </view>

    <!-- 红娘选择弹窗 -->
    <view class="matchmaker-modal" v-if="showMatchmakerModal" @click="closeMatchmakerModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">选择红娘</text>
          <text class="modal-close" @click="closeMatchmakerModal">×</text>
        </view>

        <view class="modal-body">

          <!-- 加载状态 -->
          <view v-if="loadingMatchmakers" class="loading-container">
            <view class="loading-spinner"></view>
            <text class="loading-text">加载红娘列表中...</text>
          </view>

          <!-- 红娘列表 -->
          <view v-else-if="matchmakerList.length > 0" class="matchmaker-list">
            <view
              v-for="matchmaker in matchmakerList"
              :key="matchmaker.matchmakerId"
              class="matchmaker-item"
              :class="{ recommended: matchmaker.recommended }"
              @click="selectMatchmaker(matchmaker)"
            >
              <!-- 推荐标签 -->
              <view class="recommend-badge" v-if="matchmaker.recommended">推荐</view>

              <view class="matchmaker-info">
                <view class="matchmaker-header">
                  <text class="matchmaker-name">{{ matchmaker.nickname }}</text>
                  <view class="matchmaker-level">
                    <text class="level-text">专业红娘</text>
                  </view>
                </view>

                <view class="matchmaker-details">
                  <view class="detail-item">
                    <text class="detail-label">服务区域：</text>
                    <text class="detail-value">{{ matchmaker.serviceArea || '全国' }}</text>
                  </view>
                  <view class="detail-item">
                    <text class="detail-label">从业年限：</text>
                    <text class="detail-value">{{ matchmaker.serviceYears || 0 }}年</text>
                  </view>
                  <view class="detail-item">
                    <text class="detail-label">成功案例：</text>
                    <text class="detail-value">{{ matchmaker.successCount || 0 }}次</text>
                  </view>
                  <view class="detail-item">
                    <text class="detail-label">当前处理：</text>
                    <text class="detail-value">{{ matchmaker.currentRequests || 0 }}个申请</text>
                  </view>
                </view>

                <view class="matchmaker-intro" v-if="matchmaker.introduction">
                  <text class="intro-text">{{ matchmaker.introduction }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view v-else class="empty-matchmakers">
            <view class="empty-icon">👩‍💼</view>
            <text class="empty-text">暂无可用红娘</text>
            <text class="empty-desc">请稍后再试或选择系统自动分配</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 自定义确认弹窗 -->
    <view class="custom-confirm-modal" v-if="showCustomConfirmModal" @click="cancelSubmit">
      <view class="confirm-content" @click.stop>
        <view class="confirm-header">
          <view class="confirm-icon">💕</view>
          <text class="confirm-title">确认提交申请</text>
        </view>

        <view class="confirm-body">
          <view class="target-info">
            <text class="target-label">申请对象：</text>
            <text class="target-name">{{ targetUser?.nickname || '该用户' }}</text>
          </view>

          <view class="service-info">
            <view class="service-item" v-if="assignType === 'manual' && selectedMatchmaker">
              <text class="service-label">指定红娘：</text>
              <text class="service-value">专业红娘 {{ selectedMatchmaker.nickname }}</text>
            </view>
            <view class="service-item" v-else>
              <text class="service-label">智能分配：</text>
              <text class="service-value">系统自动选择专业红娘</text>
            </view>

            <view class="service-item">
              <text class="service-label">服务费用：</text>
              <text class="service-price">
                {{ paymentType === 'virtual_coin' ? serviceCost.virtual_coin + '虚拟币' : '¥' + serviceCost.cash }}
              </text>
            </view>
          </view>

          <view class="confirm-tips">
            <view class="tip-item">
              <text class="tip-icon">✨</text>
              <text class="tip-text">提交后将创建服务订单</text>
            </view>
            <view class="tip-item">
              <text class="tip-icon">💳</text>
              <text class="tip-text">请完成支付后我们将安排专业红娘服务</text>
            </view>
            <view class="tip-item">
              <text class="tip-icon">🎯</text>
              <text class="tip-text">红娘将在24小时内与您联系</text>
            </view>
          </view>
        </view>

        <view class="confirm-actions">
          <button class="cancel-btn" @click="cancelSubmit">取消</button>
          <button class="submit-btn" @click="confirmSubmit">确定提交</button>
        </view>
      </view>
    </view>

    <!-- 加载遮罩 -->
    <view class="loading-mask" v-if="isLoading">
      <view class="loading-content">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import {
  submitMatchmakingRequest,
  validateMatchmakingRequestForm,
  getAvailableMatchmakers,
  getMatchmakerLevelText,
  getMatchmakerLevelPrice
} from '@/api/matchmaker'
import { getUserDetail } from '@/api/user/detail'
import { getWalletInfo } from '@/api/wallet'

export default {
  data() {
    return {
      targetUser: null,
      formData: {
        targetUserId: null,
        matchmakerId: null, // 选择的红娘ID
        matchmakerLevel: null, // 选择的红娘等级
        requestMessage: '你好，我对你很感兴趣，希望能够认识你。我是一个积极向上的人，喜欢运动和阅读，希望我们能有机会进一步了解。'
      },
      isLoading: false,
      isSubmitting: false,
      showMatchmakerModal: false,
      loadingMatchmakers: false,
      matchmakerList: [],
      selectedMatchmaker: null,
      selectedLevel: null, // 选择的红娘等级筛选
      assignType: 'smart', // 分配方式：smart-智能分配，manual-指定红娘
      showCustomConfirmModal: false, // 显示自定义确认弹窗
      confirmResolve: null, // 确认对话框的resolve函数
      userBalance: null, // 用户虚拟币余额
      paymentType: 'cash', // 支付方式：virtual_coin-虚拟币，cash-现金
      serviceCost: {
        virtual_coin: 699, // 虚拟币价格
        cash: 69.9 // 现金价格（元）
      }
    }
  },
  
  computed: {
    canSubmit() {
      const messageValid = this.formData.requestMessage.trim().length >= 10 &&
                           this.formData.requestMessage.trim().length <= 500

      if (this.paymentType === 'virtual_coin') {
        return messageValid && this.userBalance !== null && this.userBalance >= this.serviceCost.virtual_coin
      } else {
        return messageValid
      }
    }
  },
  
  onLoad(options) {
    if (options.targetUserId) {
      this.formData.targetUserId = parseInt(options.targetUserId)
      this.loadTargetUserInfo()
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
    // 加载目标用户信息
    async loadTargetUserInfo() {
      this.isLoading = true
      try {
        // 调用相亲广场API获取用户详情
        const result = await getUserDetail(this.formData.targetUserId)
        console.log('获取用户详情结果:', result)

        if (result.code === 200 && result.data && result.data.records) {
          // 查找指定用户ID的数据
          let targetUser = result.data.records.find(user => user.userId == this.formData.targetUserId)

          if (!targetUser && result.data.records.length > 0) {
            // 如果没找到指定用户，取第一个
            targetUser = result.data.records[0]
          }

          if (targetUser) {
            // 从userProfile中获取年龄和地区信息
            const profile = targetUser.userProfile || {}
            const age = profile.age || '未知'
            const location = profile.workCity || profile.hometown || '未知'

            this.targetUser = {
              userId: targetUser.userId,
              nickname: targetUser.nickname || '用户' + targetUser.userId,
              avatarUrl: targetUser.avatarUrl || '/static/default-avatar.png',
              age: age,
              location: location
            }
          } else {
            // 如果没有找到用户，使用默认信息
            this.targetUser = {
              userId: this.formData.targetUserId,
              nickname: '用户' + this.formData.targetUserId,
              avatarUrl: '/static/default-avatar.png',
              age: '未知',
              location: '未知'
            }
          }
        } else {
          throw new Error('获取用户信息失败')
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        // 使用默认信息
        this.targetUser = {
          userId: this.formData.targetUserId,
          nickname: '用户' + this.formData.targetUserId,
          avatarUrl: '/static/default-avatar.png',
          age: '未知',
          location: '未知'
        }
        uni.showToast({
          title: '加载用户信息失败，使用默认信息',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
      }
    },

    // 选择支付方式
    selectPaymentType(type) {
      this.paymentType = type

      // 如果选择虚拟币支付，加载用户余额
      if (type === 'virtual_coin' && this.userBalance === null) {
        this.loadUserBalance()
      }
    },

    // 加载用户虚拟币余额
    async loadUserBalance() {
      try {
        const result = await getWalletInfo()

        if (result.code === 200) {
          this.userBalance = result.data.coinBalance || 0
        } else {
          console.error('获取余额失败:', result.message)
          this.userBalance = 0
        }
      } catch (error) {
        console.error('加载用户余额失败:', error)
        this.userBalance = 0
      }
    },

    // 获取提交按钮文本
    getSubmitButtonText() {
      if (this.isSubmitting) {
        return '提交申请中...'
      }

      if (this.paymentType === 'virtual_coin') {
        if (this.userBalance === null) {
          return '加载余额中...'
        } else if (this.userBalance < this.serviceCost.virtual_coin) {
          return '虚拟币不足'
        } else {
          return `提交申请（${this.serviceCost.virtual_coin}虚拟币）`
        }
      } else {
        return `提交申请（¥${this.serviceCost.cash}）`
      }
    },

    // 提交申请
    async submitRequest() {
      if (!this.canSubmit || this.isSubmitting) {
        return
      }

      // 验证表单
      const validation = validateMatchmakingRequestForm(this.formData)
      if (!validation.valid) {
        uni.showToast({
          title: validation.message,
          icon: 'error'
        })
        return
      }

      // 虚拟币支付需要额外检查余额
      if (this.paymentType === 'virtual_coin') {
        if (this.userBalance < this.serviceCost.virtual_coin) {
          uni.showModal({
            title: '余额不足',
            content: `当前余额：${this.userBalance}币\n需要费用：${this.serviceCost.virtual_coin}币\n\n请先充值虚拟币`,
            confirmText: '去充值',
            cancelText: '取消',
            success: (res) => {
              if (res.confirm) {
                uni.navigateTo({
                  url: '/pages/wallet/recharge'
                })
              }
            }
          })
          return
        }
      }

      // 确认提交
      const confirmResult = await this.showConfirmDialog()
      if (!confirmResult) {
        return
      }

      this.isSubmitting = true

      try {
        // 添加支付方式到请求数据
        const requestData = {
          ...this.formData,
          paymentType: this.paymentType,
          amount: this.paymentType === 'virtual_coin' ? this.serviceCost.virtual_coin : this.serviceCost.cash,
          matchmakerLevel: 1 // 统一使用等级1
        }

        const result = await submitMatchmakingRequest(requestData)

        if (result.code === 200) {
          const orderData = result.data

          // 统一跳转到支付页面，让用户确认支付
          uni.showModal({
            title: '申请提交成功',
            content: `申请编号：${orderData.orderNo}\n服务费用：${this.paymentType === 'virtual_coin' ? this.serviceCost.virtual_coin + '虚拟币' : '¥' + orderData.amount}\n专业红娘：${orderData.matchmakerName || '系统智能分配'}\n\n请完成支付，我们将立即为您安排专业红娘服务`,
            confirmText: '立即支付',
            cancelText: '稍后支付',
            success: (res) => {
              if (res.confirm) {
                // 跳转到订单详情页面进行支付，传递预选的支付方式
                const payType = this.paymentType === 'virtual_coin' ? 3 : 1 // 3-虚拟币，1-微信支付
                uni.redirectTo({
                  url: `/pages/matchmaker/order-detail?orderId=${orderData.orderId}&payType=${payType}`
                })
              } else {
                // 跳转到我的申请列表
                uni.redirectTo({
                  url: '/pages/matchmaker/request-list'
                })
              }
            }
          })
        } else {
          uni.showToast({
            title: result.message || '申请提交失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.isSubmitting = false
      }
    },
    
    // 显示确认对话框
    showConfirmDialog() {
      return new Promise((resolve) => {
        this.showCustomConfirmModal = true
        this.confirmResolve = resolve
      })
    },

    // 确认提交
    confirmSubmit() {
      this.showCustomConfirmModal = false
      if (this.confirmResolve) {
        this.confirmResolve(true)
      }
    },

    // 取消提交
    cancelSubmit() {
      this.showCustomConfirmModal = false
      if (this.confirmResolve) {
        this.confirmResolve(false)
      }
    },



    // 选择分配方式
    selectAssignType(type) {
      this.assignType = type

      if (type === 'smart') {
        // 智能分配：清除指定红娘
        this.formData.matchmakerId = null
        this.selectedMatchmaker = null
      } else if (type === 'manual') {
        // 指定红娘：显示红娘列表
        this.showMatchmakerList()
      }
    },

    // 显示红娘列表
    async showMatchmakerList() {
      this.showMatchmakerModal = true
      // 加载所有专业红娘
      await this.loadMatchmakerList()
    },

    // 加载红娘列表
    async loadMatchmakerList() {
      this.loadingMatchmakers = true
      try {
        // 获取所有可用的专业红娘（等级1）
        const result = await getAvailableMatchmakers(1)

        if (result.code === 200) {
          this.matchmakerList = result.data || []
        } else {
          uni.showToast({
            title: result.message || '获取红娘列表失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载红娘列表失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'error'
        })
      } finally {
        this.loadingMatchmakers = false
      }
    },



    // 选择红娘
    selectMatchmaker(matchmaker) {
      this.formData.matchmakerId = matchmaker.matchmakerId
      this.formData.matchmakerLevel = matchmaker.matchmakerLevel // 设置对应的等级
      this.selectedMatchmaker = matchmaker
      this.closeMatchmakerModal()

      uni.showToast({
        title: `已选择${matchmaker.matchmakerLevelText}：${matchmaker.nickname}`,
        icon: 'success'
      })
    },

    // 关闭红娘选择弹窗
    closeMatchmakerModal() {
      this.showMatchmakerModal = false
    },

    // 跳转到订单列表
    goToOrderList() {
      uni.navigateTo({
        url: '/pages/matchmaker/order-list'
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
.request-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 100rpx;
}

/* 导航栏 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
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

/* 目标用户卡片 */
.target-user-card {
  margin: 30rpx;
  padding: 40rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  overflow: hidden;
  margin-right: 30rpx;
}

.user-avatar image {
  width: 100%;
  height: 100%;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.user-details {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.age, .location {
  font-size: 26rpx;
  color: #666;
  padding: 8rpx 16rpx;
  background: #f5f5f5;
  border-radius: 20rpx;
}

/* 表单容器 */
.form-container {
  margin: 0 30rpx;
}

.form-section {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.section-desc {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 30rpx;
}

.message-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
}

.input-tip {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 温馨提示 */
.tips-section {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.tips-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.tips-content {
  line-height: 1.6;
}

.tip-item {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

/* 支付方式选择 */
.payment-section {
  background: white;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.payment-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 25rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 3rpx solid transparent;
  transition: all 0.3s ease;
  position: relative;
}

.payment-option.active {
  background: #e8f4fd;
  border-color: #667eea;
}

.payment-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.payment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.payment-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 5rpx;
}

.payment-amount {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 5rpx;
}

.payment-desc {
  font-size: 22rpx;
  color: #666;
}

.insufficient-tip {
  color: #ff4757;
  font-weight: 600;
}

.payment-check {
  font-size: 32rpx;
  color: #667eea;
  font-weight: bold;
}

/* 提交按钮 */
.submit-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: white;
  box-shadow: 0 -10rpx 30rpx rgba(0, 0, 0, 0.1);
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn.disabled {
  background: #ccc;
  color: #999;
}

/* 加载遮罩 */
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  background: white;
  padding: 60rpx;
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #666;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 服务信息展示 */
.service-info {
  margin-top: 30rpx;
  padding: 25rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
}

.service-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.service-desc {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.service-features {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.feature-item {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 24rpx;
  margin-right: 8rpx;
}

.feature-text {
  font-size: 22rpx;
  color: #333;
  font-weight: 500;
}

/* 分配方式选择 */
.assign-selection {
  margin-top: 30rpx;
}

.assign-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.assign-options {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.assign-option {
  display: flex;
  align-items: center;
  padding: 25rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 3rpx solid transparent;
  transition: all 0.3s ease;
}

.assign-option.active {
  background: #e3f2fd;
  border-color: #667eea;
}

.assign-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.assign-content {
  flex: 1;
}

.assign-name {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 6rpx;
}

.assign-desc {
  font-size: 24rpx;
  color: #666;
}

.assign-desc.selected {
  color: #667eea;
  font-weight: bold;
}

/* 红娘选择弹窗 */
.matchmaker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  border-radius: 24rpx;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 2rpx solid #f8f9fa;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 40rpx;
  color: #999;
  font-weight: bold;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
}

/* 红娘列表 */
.matchmaker-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.matchmaker-item {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  border: 2rpx solid #e0e0e0;
  position: relative;
  transition: all 0.3s ease;
}

.matchmaker-item:active {
  transform: scale(0.98);
  border-color: #667eea;
}

.matchmaker-item.recommended {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
}

.recommend-badge {
  position: absolute;
  top: -5rpx;
  right: 20rpx;
  background: #ff6b6b;
  color: white;
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
  font-weight: bold;
}

.matchmaker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.matchmaker-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.matchmaker-level {
  background: #667eea;
  color: white;
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
}

.level-text {
  font-weight: bold;
}

.matchmaker-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15rpx;
  margin-bottom: 20rpx;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-label {
  font-size: 24rpx;
  color: #666;
  margin-right: 8rpx;
}

.detail-value {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;
}

.matchmaker-intro {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #667eea;
}

.intro-text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 空状态 */
.empty-matchmakers {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  line-height: 1.5;
}



/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
}

/* 自定义确认弹窗 */
.custom-confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(10rpx);
}

.confirm-content {
  background: white;
  border-radius: 32rpx;
  margin: 40rpx;
  max-width: 600rpx;
  width: 100%;
  overflow: hidden;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.3);
  animation: confirmSlideIn 0.3s ease-out;
}

@keyframes confirmSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50rpx) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.confirm-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx 30rpx;
  text-align: center;
  color: white;
}

.confirm-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.confirm-title {
  font-size: 36rpx;
  font-weight: bold;
}

.confirm-body {
  padding: 40rpx 30rpx;
}

.target-info {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 20rpx;
  color: white;
}

.target-label {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.target-name {
  font-size: 32rpx;
  font-weight: bold;
}

.service-info {
  margin-bottom: 30rpx;
}

.service-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.service-item:last-child {
  border-bottom: none;
}

.service-label {
  font-size: 28rpx;
  color: #666;
}

.service-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.service-price {
  font-size: 32rpx;
  color: #e74c3c;
  font-weight: bold;
}

.confirm-tips {
  background: #f8f9fa;
  border-radius: 20rpx;
  padding: 24rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.tip-item:last-child {
  margin-bottom: 0;
}

.tip-icon {
  font-size: 24rpx;
  margin-right: 12rpx;
  width: 32rpx;
}

.tip-text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
}

.confirm-actions {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn, .submit-btn {
  flex: 1;
  height: 100rpx;
  border: none;
  font-size: 32rpx;
  font-weight: bold;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
  border-radius: 0 0 0 32rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 0 0 32rpx 0;
}

.cancel-btn:active {
  background: #e9ecef;
}

.submit-btn:active {
  opacity: 0.8;
}
</style>
