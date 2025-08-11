<template>
  <scroll-view scroll-y class="wallet-page" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
    <view class="wallet-container">
    <!-- 钱包余额卡片 -->
    <view class="balance-card">
      <view class="balance-header">
        <text class="balance-title">我的钱包</text>
        <text class="balance-status" :class="walletStatus === 1 ? 'status-normal' : 'status-frozen'">
          {{ walletStatus === 1 ? '正常' : '冻结' }}
        </text>
      </view>
      
      <view class="balance-content">
        <view class="balance-item">
          <text class="balance-label">虚拟币余额</text>
          <text class="balance-value coin">{{ walletInfo.coinBalance || 0 }}币</text>
        </view>
        <view class="balance-item">
          <text class="balance-label">现金余额</text>
          <text class="balance-value cash">¥{{ formatAmount(walletInfo.cashBalance) }}</text>
        </view>
      </view>
      
      <view class="balance-actions">
        <button class="action-btn recharge-btn" @click="showRechargeModal">充值</button>
        <button class="action-btn withdraw-btn" @click="showWithdrawModal">提现</button>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-card">
      <view class="stats-title">统计信息</view>
      <view class="stats-grid">
        <view class="stats-item">
          <text class="stats-value">{{ walletInfo.totalRecharge || 0 }}</text>
          <text class="stats-label">累计充值</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">{{ walletInfo.totalConsume || 0 }}</text>
          <text class="stats-label">累计消费</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">¥{{ formatAmount(walletInfo.totalIncome) }}</text>
          <text class="stats-label">累计收入</text>
        </view>
        <view class="stats-item">
          <text class="stats-value">¥{{ formatAmount(walletInfo.totalWithdraw) }}</text>
          <text class="stats-label">累计提现</text>
        </view>
      </view>
    </view>

    <!-- 交易记录 -->
    <view class="transactions-card">
      <view class="transactions-header">
        <text class="transactions-title">交易记录</text>
        <picker @change="onTypeChange" :value="typeIndex" :range="typeOptions">
          <view class="filter-btn">{{ typeOptions[typeIndex] }}</view>
        </picker>
      </view>
      
      <view class="transactions-list">
        <view v-for="(item, index) in transactions" :key="index" class="transaction-item">
          <view class="transaction-info">
            <text class="transaction-desc">{{ item.transactionDesc }}</text>
            <text class="transaction-time">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="transaction-amount" :class="getAmountClass(item.transactionType)">
            {{ getAmountText(item) }}
          </view>
        </view>

        <view v-if="transactions.length === 0 && !loading" class="empty-state">
          <text class="empty-icon">📝</text>
          <text class="empty-text">暂无交易记录</text>
          <text class="empty-desc">您还没有任何交易记录</text>
          <button class="retry-btn" @click="refreshData">刷新</button>
        </view>

        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>

        <view v-if="hasMore && transactions.length > 0" class="load-more" @click="loadMoreTransactions">
          <text>点击加载更多</text>
        </view>
      </view>
    </view>

    <!-- 充值弹窗 -->
    <uni-popup ref="rechargePopup" type="bottom">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">虚拟币充值</text>
          <text class="popup-close" @click="hideRechargeModal">×</text>
        </view>
        
        <view class="recharge-packages">
          <view v-for="(pkg, index) in rechargePackages" :key="index" 
                class="package-item" 
                :class="selectedPackage === index ? 'selected' : ''"
                @click="selectPackage(index)">
            <text class="package-amount">{{ pkg.coinAmount }}币</text>
            <text class="package-price">¥{{ pkg.price }}</text>
            <text v-if="pkg.bonus > 0" class="package-bonus">+{{ pkg.bonus }}赠送</text>
          </view>
        </view>
        
        <view class="payment-methods">
          <text class="section-title">支付方式</text>
          <view class="payment-options">
            <view v-for="(method, index) in paymentMethods" :key="index"
                  class="payment-item"
                  :class="selectedPayment === index ? 'selected' : ''"
                  @click="selectPayment(index)">
              <text class="payment-name">{{ method.name }}</text>
            </view>
          </view>
        </view>
        
        <button class="confirm-btn" @click="confirmRecharge" :disabled="selectedPackage === -1">
          确认充值
        </button>
      </view>
    </uni-popup>

    <!-- 提现弹窗 -->
    <uni-popup ref="withdrawPopup" type="bottom">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">现金提现</text>
          <text class="popup-close" @click="hideWithdrawModal">×</text>
        </view>
        
        <view class="withdraw-form">
          <view class="form-item">
            <text class="form-label">提现金额</text>
            <input class="form-input" type="number" v-model="withdrawForm.amount" 
                   placeholder="最少10元" />
          </view>
          
          <view class="form-item">
            <text class="form-label">提现方式</text>
            <picker @change="onWithdrawTypeChange" :value="withdrawTypeIndex" :range="withdrawTypes">
              <view class="form-picker">{{ withdrawTypes[withdrawTypeIndex] }}</view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="form-label">账户信息</text>
            <input class="form-input" v-model="withdrawForm.account" 
                   placeholder="请输入账户号码" />
          </view>
          
          <view class="form-item">
            <text class="form-label">账户姓名</text>
            <input class="form-input" v-model="withdrawForm.name" 
                   placeholder="请输入账户姓名" />
          </view>
        </view>
        
        <button class="confirm-btn" @click="confirmWithdraw">
          申请提现
        </button>
      </view>
    </uni-popup>
    </view>
  </scroll-view>
</template>

<script>
import { getWalletInfo, getTransactions, recharge, withdraw, getRechargePackages, formatAmount } from '@/api/wallet';

export default {
  name: 'Wallet',
  data() {
    return {
      walletInfo: {},
      walletStatus: 1,
      transactions: [],
      loading: false,
      refreshing: false,
      currentPage: 1,
      pageSize: 20,
      hasMore: true,
      
      // 筛选选项
      typeIndex: 0,
      typeOptions: ['全部', '充值', '消费', '收入', '提现', '退款'],
      
      // 充值相关
      rechargePackages: [],
      selectedPackage: -1,
      selectedPayment: 0,
      paymentMethods: [
        { id: 1, name: '支付宝' },
        { id: 2, name: '微信支付' },
        { id: 3, name: '银行卡' }
      ],
      
      // 提现相关
      withdrawForm: {
        amount: '',
        account: '',
        name: ''
      },
      withdrawTypeIndex: 0,
      withdrawTypes: ['支付宝', '微信支付', '银行卡']
    };
  },
  
  onLoad() {
    this.initData();
  },
  
  onPullDownRefresh() {
    this.refreshData();
  },
  
  methods: {
    async initData() {
      await this.loadWalletInfo();
      await this.loadTransactions();
      this.loadRechargePackages();
    },
    
    async refreshData() {
      this.currentPage = 1;
      this.transactions = [];
      this.hasMore = true;
      await this.initData();
      uni.stopPullDownRefresh();
      this.refreshing = false;
    },

    async onRefresh() {
      this.refreshing = true;
      await this.refreshData();
    },
    
    async loadWalletInfo() {
      try {
        const res = await getWalletInfo();
        if (res.code === 200) {
          this.walletInfo = res.data;
          this.walletStatus = res.data.walletStatus;
        }
      } catch (error) {
        console.error('获取钱包信息失败:', error);
        uni.showToast({
          title: '获取钱包信息失败',
          icon: 'none'
        });
      }
    },
    
    async loadTransactions() {
      if (this.loading || !this.hasMore) return;

      this.loading = true;
      try {
        const type = this.typeIndex === 0 ? null : this.typeIndex;
        console.log('正在加载交易记录...', { page: this.currentPage, type });
        const res = await getTransactions(this.currentPage, this.pageSize, type);

        console.log('交易记录API响应:', res);
        if (res.code === 200) {
          const newTransactions = res.data.records || [];
          console.log('获取到交易记录:', newTransactions.length, '条');

          if (this.currentPage === 1) {
            this.transactions = newTransactions;
          } else {
            this.transactions.push(...newTransactions);
          }

          this.hasMore = newTransactions.length === this.pageSize;
          this.currentPage++;
        } else {
          console.warn('交易记录API返回错误:', res.message);
          uni.showToast({
            title: res.message || '获取交易记录失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('获取交易记录失败:', error);
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    loadMoreTransactions() {
      this.loadTransactions();
    },
    
    loadRechargePackages() {
      this.rechargePackages = getRechargePackages();
    },
    
    onTypeChange(e) {
      this.typeIndex = e.detail.value;
      this.currentPage = 1;
      this.transactions = [];
      this.hasMore = true;
      this.loadTransactions();
    },
    
    showRechargeModal() {
      this.$refs.rechargePopup.open();
    },
    
    hideRechargeModal() {
      this.$refs.rechargePopup.close();
      this.selectedPackage = -1;
    },
    
    showWithdrawModal() {
      if (this.walletInfo.cashBalance <= 0) {
        uni.showToast({
          title: '现金余额不足',
          icon: 'none'
        });
        return;
      }
      this.$refs.withdrawPopup.open();
    },
    
    hideWithdrawModal() {
      this.$refs.withdrawPopup.close();
      this.withdrawForm = { amount: '', account: '', name: '' };
    },
    
    selectPackage(index) {
      this.selectedPackage = index;
    },
    
    selectPayment(index) {
      this.selectedPayment = index;
    },
    
    async confirmRecharge() {
      if (this.selectedPackage === -1) {
        uni.showToast({
          title: '请选择充值套餐',
          icon: 'none'
        });
        return;
      }
      
      const pkg = this.rechargePackages[this.selectedPackage];
      const paymentMethod = this.paymentMethods[this.selectedPayment];
      
      try {
        const res = await recharge({
          coinAmount: pkg.coinAmount + (pkg.bonus || 0),
          transactionDesc: `充值${pkg.coinAmount}虚拟币`,
          paymentMethod: paymentMethod.id
        });
        
        if (res.code === 200) {
          uni.showToast({
            title: '充值成功',
            icon: 'success'
          });
          this.hideRechargeModal();
          this.refreshData();
        } else {
          uni.showToast({
            title: res.message || '充值失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('充值失败:', error);
        uni.showToast({
          title: '充值失败',
          icon: 'none'
        });
      }
    },
    
    onWithdrawTypeChange(e) {
      this.withdrawTypeIndex = e.detail.value;
    },
    
    async confirmWithdraw() {
      const { amount, account, name } = this.withdrawForm;
      
      if (!amount || amount < 10) {
        uni.showToast({
          title: '提现金额不能少于10元',
          icon: 'none'
        });
        return;
      }
      
      if (!account || !name) {
        uni.showToast({
          title: '请填写完整的账户信息',
          icon: 'none'
        });
        return;
      }
      
      if (parseFloat(amount) > parseFloat(this.walletInfo.cashBalance)) {
        uni.showToast({
          title: '提现金额不能超过余额',
          icon: 'none'
        });
        return;
      }
      
      try {
        const res = await withdraw({
          transactionAmount: parseFloat(amount),
          withdrawAccount: account,
          withdrawAccountType: this.withdrawTypeIndex + 1,
          withdrawAccountName: name,
          transactionDesc: '现金提现'
        });
        
        if (res.code === 200) {
          uni.showToast({
            title: '提现申请成功',
            icon: 'success'
          });
          this.hideWithdrawModal();
          this.refreshData();
        } else {
          uni.showToast({
            title: res.message || '提现申请失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('提现申请失败:', error);
        uni.showToast({
          title: '提现申请失败',
          icon: 'none'
        });
      }
    },
    
    formatAmount(amount) {
      return formatAmount(amount, '元').replace('元', '');
    },
    
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
    },
    
    getAmountClass(type) {
      return type === 1 || type === 3 ? 'amount-positive' : 'amount-negative';
    },
    
    getAmountText(item) {
      const isPositive = item.transactionType === 1 || item.transactionType === 3;
      const prefix = isPositive ? '+' : '-';
      
      if (item.coinAmount) {
        return `${prefix}${item.coinAmount}币`;
      } else if (item.transactionAmount) {
        return `${prefix}¥${this.formatAmount(item.transactionAmount)}`;
      }
      return '';
    }
  }
};
</script>

<style scoped>
.wallet-page {
  height: 100vh;
  background-color: #f5f5f5;
}

.wallet-container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.balance-card {
  background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  color: white;
  box-shadow: 0 10rpx 30rpx rgba(196, 113, 237, 0.3);
}

.balance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.balance-title {
  font-size: 36rpx;
  font-weight: bold;
}

.balance-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.status-normal {
  background-color: rgba(76, 175, 80, 0.3);
  border: 1rpx solid rgba(76, 175, 80, 0.5);
}

.status-frozen {
  background-color: rgba(244, 67, 54, 0.3);
  border: 1rpx solid rgba(244, 67, 54, 0.5);
}

.balance-content {
  margin-bottom: 40rpx;
}

.balance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.balance-label {
  font-size: 28rpx;
  opacity: 0.8;
}

.balance-value {
  font-size: 32rpx;
  font-weight: bold;
}

.balance-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
  border: none;
  color: white;
  text-align: center;
}

.recharge-btn {
  background-color: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
}

.withdraw-btn {
  background-color: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.4);
}

.stats-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.stats-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30rpx;
}

.stats-item {
  text-align: center;
}

.stats-value {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #C471ED;
  margin-bottom: 10rpx;
}

.stats-label {
  font-size: 24rpx;
  color: #666;
}

.transactions-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.transactions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.transactions-title {
  font-size: 32rpx;
  font-weight: bold;
}

.filter-btn {
  padding: 10rpx 20rpx;
  background-color: #F3E7FF;
  border-radius: 50rpx;
  font-size: 24rpx;
  color: #C471ED;
}

.transactions-list {
  /* 移除固定高度，让内容自然展开 */
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.transaction-info {
  flex: 1;
}

.transaction-desc {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.transaction-time {
  font-size: 24rpx;
  color: #999;
}

.transaction-amount {
  font-size: 28rpx;
  font-weight: bold;
}

.amount-positive {
  color: #4caf50;
}

.amount-negative {
  color: #f44336;
}

.empty-state, .loading-state {
  text-align: center;
  padding: 60rpx;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #999;
}

.retry-btn {
  margin-top: 30rpx;
  padding: 20rpx 40rpx;
  background-color: #C471ED;
  color: white;
  border: none;
  border-radius: 50rpx;
  font-size: 28rpx;
}

.load-more {
  text-align: center;
  padding: 30rpx;
  color: #C471ED;
  font-size: 28rpx;
  border-top: 1rpx solid #f0f0f0;
  margin-top: 20rpx;
}

.popup-content {
  background: white;
  border-radius: 20rpx 20rpx 0 0;
  padding: 40rpx;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.popup-title {
  font-size: 36rpx;
  font-weight: bold;
}

.popup-close {
  font-size: 48rpx;
  color: #999;
}

.recharge-packages {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.package-item {
  border: 2rpx solid #e0e0e0;
  border-radius: 10rpx;
  padding: 30rpx;
  text-align: center;
  position: relative;
}

.package-item.selected {
  border-color: #C471ED;
  background-color: rgba(196, 113, 237, 0.1);
}

.package-amount {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.package-price {
  display: block;
  font-size: 28rpx;
  color: #f44336;
  margin-bottom: 10rpx;
}

.package-bonus {
  display: block;
  font-size: 24rpx;
  color: #ff9800;
}

.payment-methods {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.payment-options {
  display: flex;
  gap: 20rpx;
}

.payment-item {
  flex: 1;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 10rpx;
  text-align: center;
}

.payment-item.selected {
  border-color: #C471ED;
  background-color: rgba(196, 113, 237, 0.1);
}

.withdraw-form {
  margin-bottom: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  margin-bottom: 10rpx;
}

.form-input, .form-picker {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 16rpx;
  font-size: 28rpx;
}

.form-input:focus, .form-picker:focus {
  border-color: #C471ED;
}

.confirm-btn {
  width: 100%;
  padding: 30rpx;
  background-color: #C471ED;
  color: white;
  border: none;
  border-radius: 50rpx;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 10rpx 20rpx rgba(196, 113, 237, 0.3);
}

.confirm-btn:disabled {
  background-color: #ccc;
  box-shadow: none;
}
</style>
