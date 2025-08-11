<template>
  <view class="vip-recharge-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">开通VIP</text>
      <view class="nav-right"></view>
    </view>
    
    <!-- VIP特权介绍 -->
    <view class="vip-intro">
      <view class="vip-logo">
        <text class="vip-text">VIP</text>
        <text class="crown-icon">👑</text>
      </view>
      <text class="vip-slogan">解锁更多特权，遇见更好的TA</text>
    </view>
    
    <!-- VIP套餐选择 -->
    <view class="package-section">
      <text class="section-title">💎 选择套餐</text>
      <view class="package-grid">
        <view
          v-for="(pkg, index) in vipPackages"
          :key="pkg.vipType"
          class="package-card"
          :class="[
            'package-' + (index + 1),
            { 'selected': selectedPackage === pkg.vipType },
            { 'recommended': index === 1 }
          ]"
          @click="selectPackage(pkg)"
        >
          <view class="package-badge" v-if="pkg.discount">
            <text class="badge-text">{{ pkg.discount }}</text>
          </view>

          <view class="package-icon">
            <text class="icon-text">{{ getPackageIcon(index) }}</text>
          </view>

          <view class="package-title">
            <text class="package-name">{{ pkg.name }}</text>
            <text class="package-duration">{{ pkg.duration }}</text>
          </view>

          <view class="package-pricing">
            <text class="current-price">¥{{ pkg.currentPrice }}</text>
            <text class="original-price" v-if="pkg.originalPrice !== pkg.currentPrice">¥{{ pkg.originalPrice }}</text>
          </view>

          <view class="package-features">
            <text
              v-for="(feature, fIndex) in pkg.features.slice(0, 2)"
              :key="fIndex"
              class="feature-item"
            >
              {{ feature }}
            </text>
            <text v-if="pkg.features.length > 2" class="more-features">+{{ pkg.features.length - 2 }}项特权</text>
          </view>

          <view class="select-indicator" v-if="selectedPackage === pkg.vipType">
            <text class="check-icon">✓</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 支付方式选择 -->
    <view class="payment-section" v-if="selectedPackage">
      <text class="section-title">💳 支付方式</text>
      <view class="payment-grid">
        <view
          v-for="method in payMethods"
          :key="method.payType"
          class="payment-card"
          :class="{ 'selected': selectedPayMethod === method.payType }"
          @click="selectPayMethod(method.payType)"
        >
          <view class="payment-icon">
            <text class="pay-icon">{{ getPayIcon(method.payType) }}</text>
          </view>
          <text class="payment-name">{{ method.name }}</text>
          <view class="payment-check" v-if="selectedPayMethod === method.payType">
            <text class="check-icon">✓</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 立即开通按钮 -->
    <view class="bottom-section" v-if="selectedPackage && selectedPayMethod">
      <view class="price-summary">
        <text class="total-text">总计：</text>
        <text class="total-price">¥{{ getCurrentPrice() }}</text>
      </view>
      <button class="pay-button" @click="processPayment" :disabled="isProcessing">
        {{ isProcessing ? '处理中...' : '立即开通VIP' }}
      </button>
    </view>
  </view>
</template>

<script>
import { getVipPackages, getPayMethods, createVipOrder, processVipPayment } from '@/api/vip.js'

export default {
  data() {
    return {
      vipPackages: [],
      payMethods: [],
      selectedPackage: null,
      selectedPayMethod: null,
      isProcessing: false,
      currentOrder: null
    }
  },
  
  onLoad() {
    this.loadVipPackages()
    this.loadPayMethods()
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 加载VIP套餐
    async loadVipPackages() {
      try {
        const response = await getVipPackages()
        if (response.code === 200) {
          this.vipPackages = response.data
          console.log('VIP套餐加载成功:', this.vipPackages)
        } else {
          uni.showToast({
            title: response.message || '加载套餐失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载VIP套餐失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    },

    // 加载支付方式
    async loadPayMethods() {
      try {
        const response = await getPayMethods()
        if (response.code === 200) {
          this.payMethods = response.data
          console.log('支付方式加载成功:', this.payMethods)
        } else {
          uni.showToast({
            title: response.message || '加载支付方式失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载支付方式失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    },
    
    // 选择套餐
    selectPackage(pkg) {
      this.selectedPackage = pkg.vipType
      console.log('选择套餐:', pkg)
    },
    
    // 选择支付方式
    selectPayMethod(payType) {
      this.selectedPayMethod = payType
      console.log('选择支付方式:', payType)
    },
    
    // 获取当前选中套餐的价格
    getCurrentPrice() {
      const pkg = this.vipPackages.find(p => p.vipType === this.selectedPackage)
      return pkg ? pkg.currentPrice : '0.00'
    },
    
    // 获取支付图标
    getPayIcon(payType) {
      switch (payType) {
        case 1: return '💬' // 微信
        case 2: return '💰' // 支付宝
        case 3: return '🍎' // Apple Pay
        case 4: return '💳' // 其他
        default: return '💳'
      }
    },

    // 获取套餐图标
    getPackageIcon(index) {
      const icons = ['🌟', '💎', '👑']
      return icons[index] || '⭐'
    },
    
    // 处理支付
    async processPayment() {
      if (!this.selectedPackage || !this.selectedPayMethod) {
        uni.showToast({
          title: '请选择套餐和支付方式',
          icon: 'none'
        })
        return
      }
      
      this.isProcessing = true
      
      try {
        // 1. 创建订单
        const orderResponse = await createVipOrder({
          vipType: this.selectedPackage
        })

        if (orderResponse.code !== 200) {
          throw new Error(orderResponse.message || '创建订单失败')
        }

        this.currentOrder = orderResponse.data
        console.log('订单创建成功:', this.currentOrder)

        // 2. 处理支付
        const payResponse = await processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: this.selectedPayMethod
        })

        if (payResponse.code !== 200) {
          throw new Error(payResponse.message || '支付失败')
        }
        
        console.log('支付成功:', payResponse.data)
        
        // 支付成功，显示成功页面
        uni.showToast({
          title: '开通成功！',
          icon: 'success',
          duration: 2000
        })
        
        // 延迟跳转到成功页面
        setTimeout(() => {
          uni.redirectTo({
            url: `/pages/vip/success?orderData=${encodeURIComponent(JSON.stringify(payResponse.data))}`
          })
        }, 2000)
        
      } catch (error) {
        console.error('支付处理失败:', error)
        uni.showToast({
          title: error.message || '支付失败',
          icon: 'none'
        })
      } finally {
        this.isProcessing = false
      }
    }
  }
}
</script>

<style scoped>
.vip-recharge-page {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 100rpx;
  box-sizing: border-box;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 20rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  box-sizing: border-box;
}

.nav-left, .nav-right {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon {
  font-size: 36rpx;
  color: white;
  font-weight: bold;
}

.nav-title {
  font-size: 36rpx;
  font-weight: bold;
  color: white;
}

/* VIP介绍 */
.vip-intro {
  text-align: center;
  width: 100%;
  padding: 30rpx 20rpx 20rpx;
  box-sizing: border-box;
}

.vip-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}

.vip-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #FFD700;
  text-shadow: 2rpx 2rpx 4rpx rgba(0, 0, 0, 0.3);
  margin-right: 12rpx;
}

.crown-icon {
  font-size: 40rpx;
}

.vip-slogan {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 套餐选择 */
.package-section, .payment-section {
  width: 100%;
  margin: 24rpx 0;
  padding: 0 20rpx;
  box-sizing: border-box;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: white;
  margin-bottom: 20rpx;
  display: block;
  text-align: center;
}

.package-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  justify-content: space-between;
}

.package-card {
  width: calc(50% - 8rpx);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16rpx;
  padding: 20rpx;
  position: relative;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  box-sizing: border-box;
  min-height: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

/* 不同套餐的颜色主题 */
.package-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.package-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.package-3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.package-card.selected {
  transform: scale(1.05);
  box-shadow: 0 8rpx 25rpx rgba(255, 215, 0, 0.4);
  border-color: #FFD700;
}

.package-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background: linear-gradient(45deg, #FF6B6B, #FF8E8E);
  color: white;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 16rpx;
  font-weight: bold;
  z-index: 2;
}

.package-icon {
  margin-bottom: 12rpx;
}

.icon-text {
  font-size: 40rpx;
}

.package-title {
  margin-bottom: 12rpx;
}

.package-name {
  font-size: 24rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 4rpx;
}

.package-duration {
  font-size: 18rpx;
  opacity: 0.8;
  display: block;
}

.package-pricing {
  margin-bottom: 12rpx;
}

.current-price {
  font-size: 26rpx;
  font-weight: bold;
  display: block;
}

.original-price {
  font-size: 18rpx;
  opacity: 0.7;
  text-decoration: line-through;
  display: block;
  margin-top: 2rpx;
}

.package-features {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  font-size: 16rpx;
  opacity: 0.9;
}

.feature-item {
  line-height: 1.2;
}

.more-features {
  font-weight: bold;
  margin-top: 4rpx;
}

.select-indicator {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 24rpx;
  height: 24rpx;
  background: #FFD700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.select-indicator .check-icon {
  color: white;
  font-size: 16rpx;
  font-weight: bold;
}

/* 推荐标签 */
.package-card.recommended::before {
  content: '推荐';
  position: absolute;
  top: -8rpx;
  left: 50%;
  transform: translateX(-50%);
  background: #FFD700;
  color: #333;
  padding: 4rpx 16rpx;
  border-radius: 12rpx;
  font-size: 16rpx;
  font-weight: bold;
  z-index: 2;
}

/* 支付方式 */
.payment-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  justify-content: space-between;
}

.payment-card {
  width: calc(50% - 6rpx);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12rpx;
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
  position: relative;
  min-height: 80rpx;
  justify-content: center;
}

.payment-card.selected {
  border-color: #FFD700;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
  transform: scale(1.02);
  box-shadow: 0 4rpx 15rpx rgba(255, 215, 0, 0.3);
}

.payment-icon {
  margin-bottom: 8rpx;
}

.pay-icon {
  font-size: 28rpx;
}

.payment-name {
  font-size: 20rpx;
  font-weight: bold;
}

.payment-check {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 20rpx;
  height: 20rpx;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.payment-check .check-icon {
  color: #FFD700;
  font-size: 14rpx;
  font-weight: bold;
}

/* 底部支付按钮 */
.bottom-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  padding: 16rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.price-summary {
  text-align: center;
  margin-bottom: 12rpx;
}

.total-text {
  font-size: 24rpx;
  color: #666;
}

.total-price {
  font-size: 30rpx;
  font-weight: bold;
  color: #FF6B6B;
  margin-left: 8rpx;
}

.pay-button {
  width: 100%;
  height: 72rpx;
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: white;
  border: none;
  border-radius: 36rpx;
  font-size: 26rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(255, 215, 0, 0.3);
  transition: all 0.3s ease;
}

.pay-button:disabled {
  opacity: 0.6;
  box-shadow: none;
}

.pay-button:active:not(:disabled) {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 15rpx rgba(255, 215, 0, 0.3);
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.package-card.recommended {
  animation: pulse 2s infinite;
}

.package-card:hover {
  transform: translateY(-4rpx);
}

.payment-card:hover {
  transform: translateY(-2rpx);
}

/* 响应式优化 */
@media screen and (max-width: 375px) {
  .package-grid {
    gap: 12rpx;
  }

  .package-card {
    width: calc(50% - 6rpx);
    min-height: 180rpx;
    padding: 16rpx;
  }

  .payment-grid {
    gap: 10rpx;
  }

  .payment-card {
    width: calc(50% - 5rpx);
    min-height: 70rpx;
    padding: 12rpx;
  }
}

@media screen and (min-width: 768px) {
  .package-section, .payment-section {
    max-width: 600rpx;
    margin-left: auto;
    margin-right: auto;
  }

  .vip-intro {
    max-width: 600rpx;
    margin: 0 auto;
  }

  .package-grid {
    gap: 20rpx;
  }

  .package-card {
    width: calc(33.333% - 14rpx);
  }

  .payment-grid {
    justify-content: center;
    gap: 16rpx;
  }

  .payment-card {
    width: 140rpx;
  }
}
</style>
