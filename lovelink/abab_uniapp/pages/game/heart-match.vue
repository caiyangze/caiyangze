<template>
  <view class="heart-match-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-left" @click="goBack">
          <text class="nav-icon">←</text>
        </view>
        <text class="nav-title">心动速配</text>
        <view class="nav-right" @click="openSettings">
          <text class="nav-icon">⚙️</text>
        </view>
      </view>
      
      <!-- 主要内容 -->
      <view class="main-content">
        <!-- 标题区域 -->
        <view class="title-section">
          <text class="main-title">💕 心动速配 💕</text>
          <text class="sub-title">选择你想要匹配的性别</text>
        </view>
        
        <!-- 性别选择区域 -->
        <view class="gender-selection">
          <view
            class="gender-card"
            :class="{ active: selectedGender === 1 }"
            @click="selectGender(1)"
          >
            <view class="gender-icon">👨</view>
            <text class="gender-text">男生</text>
            <text class="gender-desc">寻找帅气男生</text>
          </view>

          <view
            class="gender-card"
            :class="{ active: selectedGender === 0 }"
            @click="selectGender(0)"
          >
            <view class="gender-icon">👩</view>
            <text class="gender-text">女生</text>
            <text class="gender-desc">寻找美丽女生</text>
          </view>
        </view>
        
        <!-- 开始匹配按钮 -->
        <view class="match-button-container">
          <button
            class="match-button"
            :class="{ disabled: selectedGender === null || selectedGender === undefined }"
            :disabled="selectedGender === null || selectedGender === undefined || isMatching"
            @click="startMatch"
          >
            <text v-if="!isMatching">开始匹配</text>
            <text v-else>匹配中...</text>
          </button>
        </view>
        
        <!-- 费用提示 -->
        <view class="cost-info">
          <text class="cost-text">💰 每次心动速配消耗 5 个虚拟币</text>
        </view>

        <!-- 说明文字 -->
        <view class="description">
          <text class="desc-text">系统将为你随机匹配一位符合条件的用户</text>
          <text class="desc-text">每次匹配都是全新的缘分邂逅</text>
        </view>
      </view>
    </view>

    <!-- 费用确认弹窗 -->
    <view v-if="showCostConfirm" class="cost-confirm-overlay" @click="closeCostConfirm">
      <view class="cost-confirm-modal" @click.stop>
        <!-- 关闭按钮 -->
        <view class="modal-close" @click="closeCostConfirm">
          <text class="close-icon">×</text>
        </view>

        <!-- 图标和标题 -->
        <view class="modal-icon">
          <text class="heart-icon">💝</text>
        </view>

        <view class="modal-title">心动速配</view>

        <!-- 费用信息 -->
        <view class="cost-info-box">
          <view class="balance-info">
            <text class="balance-label">当前余额：</text>
            <text class="balance-amount" :class="{ insufficient: currentBalance < 5 }">
              {{ loadingBalance ? '加载中...' : currentBalance + '币' }}
            </text>
          </view>

          <view class="cost-row">
            <text class="cost-label">消耗虚拟币：</text>
            <text class="cost-amount">5币</text>
          </view>

          <!-- 优惠券选择 -->
          <voucher-selector
            ref="voucherSelector"
            :consume-amount="5"
            consume-type="heart-match"
            @voucher-selected="onVoucherSelected"
          />

          <!-- 优惠券抵扣信息 -->
          <view v-if="selectedVoucher" class="voucher-discount-row">
            <text class="discount-label">优惠券抵扣：</text>
            <text class="discount-amount">-{{ selectedVoucher.voucher.actualValue }}币</text>
          </view>

          <view class="after-balance-row">
            <text class="after-label">实际消费：</text>
            <text class="after-amount actual-cost">{{ getActualCost() }}币</text>
          </view>

          <view class="after-balance-row">
            <text class="after-label">扣减后余额：</text>
            <text class="after-amount" :class="{ insufficient: currentBalance < getActualCost() }">
              {{ loadingBalance ? '计算中...' : (currentBalance >= getActualCost() ? (currentBalance - getActualCost()) + '币' : '余额不足') }}
            </text>
          </view>

          <text class="cost-desc">开启一次浪漫邂逅</text>
        </view>

        <!-- 不再提示选项 -->
        <view class="checkbox-container">
          <view class="checkbox-option" @click="toggleDontShowAgain">
            <view class="custom-checkbox" :class="{ checked: dontShowAgain }">
              <text v-if="dontShowAgain" class="checkbox-icon">✓</text>
            </view>
            <text class="checkbox-text">记住我的选择，不再提示</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button class="action-btn cancel-btn" @click="closeCostConfirm">
            <text class="btn-text">取消</text>
          </button>
          <button
            class="action-btn confirm-btn"
            :class="{ disabled: currentBalance < getActualCost() || loadingBalance }"
            :disabled="currentBalance < getActualCost() || loadingBalance"
            @click="confirmCost"
          >
            <text class="btn-text">
              {{ currentBalance < getActualCost() ? '余额不足' : (loadingBalance ? '加载中...' : '开始匹配') }}
            </text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import http from '@/api/http'
import { getWalletInfo, consume } from '@/api/wallet'
import VoucherSelector from '@/components/voucher-selector/voucher-selector.vue'

export default {
  components: {
    VoucherSelector
  },

  data() {
    return {
      selectedGender: null, // 选中的性别 0-女 1-男
      isMatching: false, // 是否正在匹配中
      showCostConfirm: false, // 是否显示费用确认弹窗
      dontShowAgain: false, // 不再提示选项
      showSettings: false, // 是否显示设置弹窗
      currentBalance: 0, // 当前虚拟币余额
      loadingBalance: false, // 是否正在加载余额
      selectedVoucher: null // 选中的优惠券
    }
  },
  
  methods: {
    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack()
    },
    
    /**
     * 选择性别
     */
    selectGender(gender) {
      this.selectedGender = gender
    },
    
    /**
     * 开始匹配
     */
    async startMatch() {
      if (this.selectedGender === null || this.selectedGender === undefined) {
        uni.showToast({
          title: '请先选择性别',
          icon: 'none'
        })
        return
      }

      // 检查是否需要显示费用确认
      const dontShowCostConfirm = uni.getStorageSync('dontShowCostConfirm') || false
      console.log('检查不再提示设置:', dontShowCostConfirm)

      if (!dontShowCostConfirm) {
        // 显示费用确认弹窗
        this.showCostConfirmDialog()
        return
      }

      // 直接执行匹配
      console.log('跳过费用确认，直接匹配')
      this.executeMatch()
    },

    /**
     * 显示费用确认弹窗
     */
    async showCostConfirmDialog() {
      this.showCostConfirm = true
      await this.loadUserBalance()
    },

    /**
     * 关闭费用确认弹窗
     */
    closeCostConfirm() {
      this.showCostConfirm = false
      this.dontShowAgain = false // 重置状态
    },

    /**
     * 优惠券选择回调
     */
    onVoucherSelected(voucher) {
      this.selectedVoucher = voucher
    },

    /**
     * 计算实际消费金额
     */
    getActualCost() {
      const originalCost = 5
      if (this.selectedVoucher && this.selectedVoucher.voucher) {
        const discount = this.selectedVoucher.voucher.actualValue
        return Math.max(0, originalCost - discount)
      }
      return originalCost
    },

    /**
     * 确认费用并开始匹配
     */
    confirmCost() {
      const actualCost = this.getActualCost()

      // 检查余额是否足够
      if (this.currentBalance < actualCost) {
        uni.showModal({
          title: '余额不足',
          content: `您的虚拟币余额为${this.currentBalance}币，需要${actualCost}币才能进行心动速配。是否前往充值？`,
          confirmText: '去充值',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({
                url: '/pages/wallet/wallet'
              })
            }
          }
        })
        return
      }

      // 检查是否选择了不再提示
      if (this.dontShowAgain) {
        console.log('保存不再提示设置')
        uni.setStorageSync('dontShowCostConfirm', true)
      }

      this.showCostConfirm = false
      this.dontShowAgain = false // 重置状态
      this.executeMatch()
    },

    /**
     * 切换"不再提示"选项
     */
    toggleDontShowAgain() {
      this.dontShowAgain = !this.dontShowAgain
    },

    /**
     * 加载用户余额
     */
    async loadUserBalance() {
      try {
        this.loadingBalance = true
        const response = await getWalletInfo()

        if (response.code === 200) {
          this.currentBalance = response.data.coinBalance || 0
          console.log('当前虚拟币余额:', this.currentBalance)
        } else {
          console.error('获取余额失败:', response.message)
          this.currentBalance = 0
        }
      } catch (error) {
        console.error('获取余额异常:', error)
        this.currentBalance = 0
      } finally {
        this.loadingBalance = false
      }
    },

    /**
     * 打开设置弹窗
     */
    openSettings() {
      const dontShowCostConfirm = uni.getStorageSync('dontShowCostConfirm') || false
      console.log('当前设置状态:', dontShowCostConfirm)

      if (dontShowCostConfirm) {
        uni.showModal({
          title: '💰 费用提示设置',
          content: '您已设置跳过费用确认提示。\n\n是否重新启用费用确认提示？',
          confirmText: '启用提示',
          cancelText: '保持现状',
          success: (res) => {
            if (res.confirm) {
              uni.removeStorageSync('dontShowCostConfirm')
              console.log('已清除不再提示设置')
              uni.showToast({
                title: '✅ 已启用费用提示',
                icon: 'success',
                duration: 2000
              })
            }
          }
        })
      } else {
        uni.showModal({
          title: '💰 费用提示设置',
          content: '当前已启用费用确认提示。\n\n每次心动速配前都会询问您是否确认消费5个虚拟币。',
          confirmText: '知道了',
          showCancel: false
        })
      }
    },

    /**
     * 执行匹配逻辑
     */
    async executeMatch() {
      this.isMatching = true

      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          uni.navigateTo({
            url: '/pages/login/login'
          })
          return
        }

        // 先进行虚拟币消费（支持优惠券）
        const consumeData = {
          coinAmount: 5,
          transactionDesc: '心动速配',
          relatedId: 'HEART_MATCH_' + Date.now()
        }

        // 如果选择了优惠券，添加优惠券信息
        if (this.selectedVoucher) {
          consumeData.voucherOrderId = this.selectedVoucher.id
          consumeData.voucherDiscountAmount = this.selectedVoucher.voucher.actualValue
          console.log('使用优惠券消费，优惠券订单ID：', this.selectedVoucher.id, '抵扣金额：', this.selectedVoucher.voucher.actualValue)
        }

        console.log('消费请求数据：', consumeData)
        const consumeResponse = await consume(consumeData)
        if (consumeResponse.code !== 200) {
          throw new Error(consumeResponse.message || '虚拟币扣减失败')
        }

        // 虚拟币扣减成功后，进行匹配
        const response = await http.post('/user/heartMatch', {
          gender: this.selectedGender,
          skipWalletDeduction: true // 告诉后端跳过钱包扣减，因为前端已经处理了
        }, {
          headers: {
            'token': token
          }
        })
        
        if (response.code === 200) {
          // 匹配成功，跳转到结果页面
          const matchResult = response.data

          // 选择A：简洁提示（如果需要的话，取消注释下面的代码）
          // uni.showToast({
          //   title: '💝 匹配成功',
          //   icon: 'none',
          //   duration: 1000
          // })

          uni.navigateTo({
            url: `/pages/game/match-result?result=${encodeURIComponent(JSON.stringify(matchResult))}`
          })
        } else {
          // 处理不同的错误情况
          const errorMessage = response.message || '匹配失败'

          // 如果是余额不足，提供充值选项
          if (errorMessage.includes('余额不足') || errorMessage.includes('虚拟币')) {
            uni.showModal({
              title: '余额不足',
              content: '您的虚拟币余额不足，需要5个虚拟币才能进行心动速配。是否前往充值？',
              confirmText: '去充值',
              cancelText: '取消',
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({
                    url: '/pages/wallet/wallet'
                  })
                }
              }
            })
          } else {
            uni.showToast({
              title: errorMessage,
              icon: 'none'
            })
          }
        }
      } catch (error) {
        console.error('匹配失败:', error)
        uni.showToast({
          title: '匹配失败，请重试',
          icon: 'none'
        })
      } finally {
        this.isMatching = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.heart-match-page {
  min-height: 100vh;
  position: relative;
  
  // 背景层
  .bg-layer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    
    .bg-gradient {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
  }
  
  // 内容容器
  .content-container {
    position: relative;
    z-index: 1;
    min-height: 100vh;
    padding: 0 40rpx;
    
    // 导航栏
    .nav-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 88rpx;
      padding-top: var(--status-bar-height);
      
      .nav-left {
        width: 80rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .nav-icon {
          font-size: 36rpx;
          color: #fff;
          font-weight: bold;
        }
      }
      
      .nav-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .nav-right {
        width: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        .nav-icon {
          font-size: 32rpx;
          color: #fff;
          opacity: 0.8;
          transition: opacity 0.3s;

          &:active {
            opacity: 1;
          }
        }
      }
    }
    
    // 主要内容
    .main-content {
      display: flex;
      flex-direction: column;
      justify-content: center;
      min-height: calc(100vh - 88rpx - var(--status-bar-height));
      padding: 40rpx 0;

      // 标题区域
      .title-section {
        text-align: center;
        margin-bottom: 120rpx;

        .main-title {
          display: block;
          font-size: 52rpx;
          font-weight: bold;
          color: #fff;
          margin-bottom: 30rpx;
        }

        .sub-title {
          display: block;
          font-size: 30rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }

      // 性别选择区域
      .gender-selection {
        display: flex;
        justify-content: center;
        margin-bottom: 120rpx;
        gap: 40rpx;
        padding: 0 20rpx;
        
        .gender-card {
          width: 280rpx;
          height: 320rpx;
          background: rgba(255, 255, 255, 0.15);
          border-radius: 30rpx;
          padding: 50rpx 30rpx;
          text-align: center;
          border: 3rpx solid transparent;
          transition: all 0.3s ease;
          backdrop-filter: blur(15rpx);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);

          &.active {
            background: rgba(255, 255, 255, 0.25);
            border-color: rgba(255, 255, 255, 0.8);
            transform: scale(1.08);
            box-shadow: 0 15rpx 40rpx rgba(0, 0, 0, 0.2);
          }

          .gender-icon {
            font-size: 100rpx;
            margin-bottom: 30rpx;
            line-height: 1;
          }

          .gender-text {
            display: block;
            font-size: 36rpx;
            font-weight: bold;
            color: #fff;
            margin-bottom: 15rpx;
          }

          .gender-desc {
            display: block;
            font-size: 26rpx;
            color: rgba(255, 255, 255, 0.8);
            line-height: 1.3;
          }
        }
      }
      
      // 匹配按钮容器
      .match-button-container {
        margin-bottom: 80rpx;
        padding: 0 20rpx;

        .match-button {
          width: 100%;
          height: 120rpx;
          background: linear-gradient(45deg, #ff6b6b, #ee5a24);
          border-radius: 60rpx;
          border: none;
          color: #fff;
          font-size: 36rpx;
          font-weight: bold;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 15rpx 40rpx rgba(255, 107, 107, 0.4);
          transition: all 0.3s ease;

          &.disabled {
            background: rgba(255, 255, 255, 0.3);
            color: rgba(255, 255, 255, 0.6);
            box-shadow: none;
          }

          &:not(.disabled):active {
            transform: scale(0.95);
          }
        }
      }

      // 费用提示
      .cost-info {
        text-align: center;
        margin: 40rpx 0 20rpx;
        padding: 20rpx 30rpx;
        background: rgba(255, 255, 255, 0.15);
        border-radius: 25rpx;
        backdrop-filter: blur(10rpx);
        border: 1rpx solid rgba(255, 255, 255, 0.2);

        .cost-text {
          color: #FFE066;
          font-size: 28rpx;
          font-weight: 500;
          text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
        }
      }

      // 说明文字
      .description {
        text-align: center;
        padding: 0 40rpx;

        .desc-text {
          display: block;
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.8);
          line-height: 45rpx;
          margin-bottom: 15rpx;
        }
      }
    }
  }

  // 费用确认弹窗
  .cost-confirm-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    backdrop-filter: blur(10rpx);

    .cost-confirm-modal {
      width: 640rpx;
      background: white;
      border-radius: 32rpx;
      position: relative;
      box-shadow: 0 32rpx 64rpx rgba(0, 0, 0, 0.2);
      animation: modalSlideIn 0.3s ease-out;

      // 关闭按钮
      .modal-close {
        position: absolute;
        top: 24rpx;
        right: 24rpx;
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.05);

        .close-icon {
          font-size: 36rpx;
          color: #999;
          line-height: 1;
        }
      }

      // 图标
      .modal-icon {
        text-align: center;
        padding: 60rpx 0 20rpx;

        .heart-icon {
          font-size: 80rpx;
          line-height: 1;
        }
      }

      // 标题
      .modal-title {
        text-align: center;
        font-size: 40rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 40rpx;
      }

      // 费用信息框
      .cost-info-box {
        margin: 0 40rpx 40rpx;
        padding: 32rpx;
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
        border-radius: 24rpx;
        text-align: center;

        .balance-info {
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 20rpx;
          padding: 16rpx;
          background: rgba(255, 255, 255, 0.3);
          border-radius: 16rpx;

          .balance-label {
            font-size: 28rpx;
            color: #666;
          }

          .balance-amount {
            font-size: 32rpx;
            font-weight: bold;
            color: #2ecc71;
            margin-left: 16rpx;

            &.insufficient {
              color: #e74c3c;
            }
          }
        }

        .cost-row {
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 16rpx;

          .cost-label {
            font-size: 32rpx;
            color: #666;
          }

          .cost-amount {
            font-size: 40rpx;
            font-weight: bold;
            color: #ff4757;
            margin-left: 16rpx;
          }
        }

        .after-balance-row {
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 20rpx;
          padding: 12rpx;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 12rpx;

          .after-label {
            font-size: 28rpx;
            color: #666;
          }

          .after-amount {
            font-size: 30rpx;
            font-weight: bold;
            color: #3498db;
            margin-left: 16rpx;

            &.insufficient {
              color: #e74c3c;
            }
          }
        }

        .cost-desc {
          font-size: 28rpx;
          color: #888;
          margin-top: 16rpx;
        }
      }

      // 复选框容器
      .checkbox-container {
        padding: 0 40rpx 40rpx;

        .checkbox-option {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20rpx;
          border-radius: 16rpx;
          background: rgba(102, 126, 234, 0.05);

          .custom-checkbox {
            width: 40rpx;
            height: 40rpx;
            border: 3rpx solid #ddd;
            border-radius: 8rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 20rpx;
            transition: all 0.3s;

            &.checked {
              background: #667eea;
              border-color: #667eea;

              .checkbox-icon {
                color: white;
                font-size: 24rpx;
                font-weight: bold;
              }
            }
          }

          .checkbox-text {
            font-size: 28rpx;
            color: #666;
          }
        }
      }

      // 操作按钮
      .action-buttons {
        display: flex;
        padding: 0 40rpx 40rpx;
        gap: 24rpx;

        .action-btn {
          flex: 1;
          height: 88rpx;
          border-radius: 44rpx;
          border: none;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32rpx;
          font-weight: 500;
          transition: all 0.3s;

          .btn-text {
            line-height: 1;
          }

          &.cancel-btn {
            background: #f8f9fa;
            color: #666;

            &:active {
              background: #e9ecef;
            }
          }

          &.confirm-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

            &:active:not(.disabled) {
              transform: translateY(2rpx);
              box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
            }

            &.disabled {
              background: #bdc3c7;
              color: #7f8c8d;
              box-shadow: none;
              cursor: not-allowed;
            }
          }
        }
      }
    }
  }

  // 弹窗动画
  @keyframes modalSlideIn {
    from {
      opacity: 0;
      transform: translateY(-100rpx) scale(0.9);
    }
    to {
      opacity: 1;
      transform: translateY(0) scale(1);
    }
  }
}
</style>
