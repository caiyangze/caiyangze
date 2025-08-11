<template>
  <view class="voucher-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">优惠券中心</text>
      <view class="nav-right"></view>
    </view>

    <!-- 标签页 -->
    <view class="tab-bar">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <view v-if="currentTab === index" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 优惠券列表 -->
    <scroll-view 
      scroll-y 
      class="voucher-list" 
      @scrolltolower="loadMore"
      refresher-enabled 
      @refresherrefresh="onRefresh" 
      :refresher-triggered="refreshing"
    >
      <view class="voucher-container">
        <view 
          v-for="(voucher, index) in voucherList" 
          :key="voucher.id"
          class="voucher-card"
          :class="getVoucherClass(voucher)"
        >
          <!-- 优惠券主体 -->
          <view class="voucher-main">
            <view class="voucher-left">
              <view class="voucher-value">
                <text class="value-number">{{ voucher.actualValue }}</text>
                <text class="value-unit">币</text>
              </view>
              <text class="voucher-type">{{ getVoucherTypeText(voucher.type) }}</text>
            </view>
            
            <view class="voucher-divider"></view>
            
            <view class="voucher-right">
              <view class="voucher-info">
                <text class="voucher-title">{{ voucher.title }}</text>
                <text class="voucher-subtitle">{{ voucher.subTitle }}</text>
                
                <!-- 秒杀券信息 -->
                <view v-if="voucher.seckilVoucher && currentTab === 0" class="seckill-info">
                  <text class="stock-text">剩余: {{ voucher.seckilVoucher.stock }}张</text>
                  <text class="time-text">{{ getTimeText(voucher.seckilVoucher) }}</text>
                </view>

                <!-- 我的券信息 -->
                <view v-if="currentTab === 1" class="my-voucher-info">
                  <text v-if="voucher.payTime" class="info-text">获取时间: {{ formatTime(voucher.payTime) }}</text>
                  <text v-if="voucher.useTime" class="info-text">使用时间: {{ formatTime(voucher.useTime) }}</text>
                  <text v-if="voucher.orderId" class="info-text">订单号: {{ voucher.orderId }}</text>
                </view>
              </view>
              
              <button 
                class="voucher-btn"
                :class="getBtnClass(voucher)"
                @click="handleVoucherAction(voucher)"
                :disabled="isVoucherDisabled(voucher)"
              >
                {{ getBtnText(voucher) }}
              </button>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="voucherList.length === 0 && !loading" class="empty-state">
          <text class="empty-icon">🎫</text>
          <text class="empty-text">暂无优惠券</text>
          <text class="empty-desc">{{ currentTab === 0 ? '暂时没有可领取的优惠券' : '您还没有优惠券' }}</text>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore && voucherList.length > 0" class="load-more">
          <text>上拉加载更多</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getVoucherList, seckillVoucher, getMyVoucherList } from '@/api/voucher.js'

export default {
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: '可领取', key: 'available' },
        { name: '我的券', key: 'my' }
      ],
      voucherList: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      pageNum: 1,
      pageSize: 10
    }
  },
  
  onLoad() {
    this.loadVoucherList()
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 切换标签页
    switchTab(index) {
      this.currentTab = index
      this.resetList()
      this.loadVoucherList()
    },
    
    // 重置列表
    resetList() {
      this.voucherList = []
      this.pageNum = 1
      this.hasMore = true
    },
    
    // 加载优惠券列表
    async loadVoucherList() {
      if (this.loading) return
      
      this.loading = true
      try {
        let result
        if (this.currentTab === 0) {
          // 可领取的优惠券
          result = await getVoucherList({
            pageNum: this.pageNum,
            pageSize: this.pageSize
          })
        } else {
          // 我的优惠券
          result = await getMyVoucherList({
            pageNum: this.pageNum,
            pageSize: this.pageSize
          })
        }
        
        if (result.code === 200) {
          const data = result.data
          let records = data.records || data || []

          // 如果是我的券，需要处理数据结构
          if (this.currentTab === 1) {
            // 我的券返回的是TbVoucherOrder对象，需要提取voucher信息并添加订单状态
            records = records.map(order => {
              const voucher = order.voucher || {}
              return {
                ...voucher,
                // 添加订单相关信息
                orderId: order.id, // 保持原始ID（可能是字符串或数字）
                orderStatus: order.status, // 订单状态：1未支付，2已支付，3已核销，4已取消，5退款中，6已退款
                payTime: order.payTime,
                useTime: order.useTime,
                createTime: order.createTime,
                // 根据订单状态判断优惠券状态
                voucherStatus: this.getVoucherStatusFromOrder(order.status)
              }
            })
          }

          if (this.pageNum === 1) {
            this.voucherList = records
          } else {
            this.voucherList.push(...records)
          }

          // 判断是否还有更多数据
          if (data.records) {
            this.hasMore = data.current < data.pages
          } else {
            this.hasMore = records.length >= this.pageSize
          }
        }
      } catch (error) {
        console.error('加载优惠券列表失败:', error)
        // 显示具体的错误信息
        let errorMessage = '加载失败'
        if (error && error.message) {
          errorMessage = error.message
        } else if (error && typeof error === 'string') {
          errorMessage = error
        } else if (error && error.data && error.data.message) {
          errorMessage = error.data.message
        }

        uni.showToast({
          title: errorMessage,
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },
    
    // 下拉刷新
    onRefresh() {
      this.refreshing = true
      this.resetList()
      this.loadVoucherList()
    },
    
    // 加载更多
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.pageNum++
        this.loadVoucherList()
      }
    },
    
    // 处理优惠券操作
    async handleVoucherAction(voucher) {
      if (this.currentTab === 0) {
        // 领取/秒杀优惠券
        await this.seckillVoucher(voucher)
      }
    },
    
    // 秒杀优惠券
    async seckillVoucher(voucher) {
      try {
        uni.showLoading({ title: '领取中...' })
        
        const result = await seckillVoucher(voucher.id)
        
        if (result.code === 200) {
          uni.showToast({
            title: '领取成功',
            icon: 'success'
          })
          // 刷新列表
          this.onRefresh()
        } else {
          uni.showToast({
            title: result.message || '领取失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('领取优惠券失败:', error)
        // 显示具体的错误信息
        let errorMessage = '领取失败'
        if (error && error.message) {
          errorMessage = error.message
        } else if (error && typeof error === 'string') {
          errorMessage = error
        } else if (error && error.data && error.data.message) {
          errorMessage = error.data.message
        }

        uni.showToast({
          title: errorMessage,
          icon: 'none',
          duration: 3000 // 延长显示时间，让用户能看清楚
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 获取优惠券样式类
    getVoucherClass(voucher) {
      const classes = []
      
      if (voucher.type === 1) {
        classes.push('seckill-voucher')
      }
      
      if (this.isVoucherExpired(voucher)) {
        classes.push('expired')
      }
      
      if (this.isVoucherUsed(voucher)) {
        classes.push('used')
      }
      
      return classes.join(' ')
    },
    
    // 获取优惠券类型文本
    getVoucherTypeText(type) {
      const typeMap = {
        0: '普通券',
        1: '秒杀券',
        2: '代金券'
      }
      return typeMap[type] || '优惠券'
    },
    
    // 获取时间文本
    getTimeText(seckilVoucher) {
      if (!seckilVoucher) return ''
      
      const now = new Date()
      const beginTime = new Date(seckilVoucher.beginTime)
      const endTime = new Date(seckilVoucher.endTime)
      
      if (now < beginTime) {
        return `${this.formatTime(beginTime)} 开始`
      } else if (now > endTime) {
        return '已结束'
      } else {
        return `${this.formatTime(endTime)} 结束`
      }
    },
    
    // 获取按钮样式类
    getBtnClass(voucher) {
      if (this.currentTab === 1) {
        // 我的优惠券 - 根据状态设置样式
        if (voucher.voucherStatus) {
          switch (voucher.voucherStatus) {
            case 'unused':
              return 'available' // 可使用
            case 'used':
            case 'cancelled':
            case 'refunded':
              return 'disabled' // 已使用/已取消/已退款
            case 'unpaid':
              return 'warning' // 未支付
            case 'refunding':
              return 'warning' // 退款中
            default:
              return 'disabled'
          }
        }
      }

      // 可领取的优惠券
      if (this.isVoucherDisabled(voucher)) {
        return 'disabled'
      }

      if (voucher.type === 1) {
        return 'seckill'
      }

      return 'normal'
    },
    
    // 获取按钮文本
    getBtnText(voucher) {
      if (this.currentTab === 1) {
        // 我的优惠券 - 根据订单状态显示
        if (voucher.voucherStatus) {
          switch (voucher.voucherStatus) {
            case 'unpaid':
              return '未支付'
            case 'unused':
              return '未使用'
            case 'used':
              return '已使用'
            case 'cancelled':
              return '已取消'
            case 'refunding':
              return '退款中'
            case 'refunded':
              return '已退款'
            default:
              return '未知状态'
          }
        }
        // 兼容旧逻辑
        if (this.isVoucherUsed(voucher)) {
          return '已使用'
        } else if (this.isVoucherExpired(voucher)) {
          return '已过期'
        } else {
          return '未使用'
        }
      } else {
        // 可领取的优惠券
        if (this.isVoucherExpired(voucher)) {
          return '已过期'
        } else if (voucher.seckilVoucher && voucher.seckilVoucher.stock <= 0) {
          return '已抢完'
        } else {
          return voucher.type === 1 ? '立即抢购' : '立即领取'
        }
      }
    },
    
    // 判断优惠券是否禁用
    isVoucherDisabled(voucher) {
      if (this.currentTab === 1) {
        return true // 我的优惠券不可操作
      }
      
      return this.isVoucherExpired(voucher) || 
             (voucher.seckilVoucher && voucher.seckilVoucher.stock <= 0)
    },
    
    // 判断优惠券是否过期
    isVoucherExpired(voucher) {
      if (!voucher.seckilVoucher) return false
      
      const now = new Date()
      const endTime = new Date(voucher.seckilVoucher.endTime)
      return now > endTime
    },
    
    // 判断优惠券是否已使用
    isVoucherUsed(voucher) {
      // 对于我的券，根据voucherStatus判断
      if (this.currentTab === 1 && voucher.voucherStatus) {
        return voucher.voucherStatus === 'used'
      }
      // 对于可领取的券，根据原有逻辑判断
      return voucher.status === 3
    },

    // 根据订单状态获取优惠券状态
    getVoucherStatusFromOrder(orderStatus) {
      // 订单状态：1未支付，2已支付，3已核销，4已取消，5退款中，6已退款
      switch (orderStatus) {
        case 1:
          return 'unpaid' // 未支付
        case 2:
          return 'unused' // 未使用（已支付）
        case 3:
          return 'used' // 已使用（已核销）
        case 4:
          return 'cancelled' // 已取消
        case 5:
          return 'refunding' // 退款中
        case 6:
          return 'refunded' // 已退款
        default:
          return 'unknown'
      }
    },
    
    // 格式化时间
    formatTime(time) {
      const date = new Date(time)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style lang="scss" scoped>
.voucher-page {
  width: 100%;
  max-width: 750rpx;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  // 导航栏
  .nav-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 30rpx;
    padding-top: calc(var(--status-bar-height) + 20rpx);

    .nav-left {
      width: 80rpx;
      height: 80rpx;
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
    }
  }

  // 标签页
  .tab-bar {
    display: flex;
    background: rgba(255, 255, 255, 0.1);
    margin: 0 30rpx 30rpx;
    border-radius: 50rpx;
    padding: 10rpx;

    .tab-item {
      flex: 1;
      text-align: center;
      padding: 20rpx 0;
      border-radius: 40rpx;
      position: relative;

      &.active {
        background: rgba(255, 255, 255, 0.2);

        .tab-text {
          color: #fff;
          font-weight: bold;
        }
      }

      .tab-text {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.7);
        transition: all 0.3s ease;
      }
    }
  }

  // 优惠券列表
  .voucher-list {
    flex: 1;
    height: calc(100vh - 300rpx);

    .voucher-container {
      padding: 0 30rpx 30rpx;

      .voucher-card {
        background: #fff;
        border-radius: 20rpx;
        margin-bottom: 30rpx;
        overflow: hidden;
        box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);

        &.seckill-voucher {
          border: 2rpx solid #ff6b6b;

          .voucher-left {
            background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
          }
        }

        &.expired {
          opacity: 0.6;

          .voucher-main::after {
            content: '已过期';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%) rotate(-15deg);
            background: rgba(255, 0, 0, 0.8);
            color: #fff;
            padding: 10rpx 30rpx;
            border-radius: 10rpx;
            font-size: 24rpx;
            font-weight: bold;
          }
        }

        .voucher-main {
          display: flex;
          position: relative;

          .voucher-left {
            width: 200rpx;
            background: linear-gradient(135deg, #667eea, #764ba2);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 40rpx 20rpx;
            position: relative;

            &::after {
              content: '';
              position: absolute;
              right: -10rpx;
              top: 50%;
              transform: translateY(-50%);
              width: 20rpx;
              height: 20rpx;
              background: #f5f5f5;
              border-radius: 50%;
            }

            .voucher-value {
              display: flex;
              align-items: baseline;
              margin-bottom: 10rpx;

              .value-number {
                font-size: 48rpx;
                font-weight: bold;
                color: #fff;
              }

              .value-unit {
                font-size: 24rpx;
                color: #fff;
                margin-left: 5rpx;
              }
            }

            .voucher-type {
              font-size: 22rpx;
              color: rgba(255, 255, 255, 0.8);
            }
          }

          .voucher-divider {
            width: 2rpx;
            background: repeating-linear-gradient(
              to bottom,
              transparent 0,
              transparent 10rpx,
              #ddd 10rpx,
              #ddd 20rpx
            );
          }

          .voucher-right {
            flex: 1;
            padding: 30rpx;
            display: flex;
            justify-content: space-between;
            align-items: center;

            .voucher-info {
              flex: 1;

              .voucher-title {
                font-size: 32rpx;
                font-weight: bold;
                color: #333;
                margin-bottom: 10rpx;
                display: block;
              }

              .voucher-subtitle {
                font-size: 24rpx;
                color: #666;
                margin-bottom: 15rpx;
                display: block;
              }

              .seckill-info {
                .stock-text {
                  font-size: 22rpx;
                  color: #ff6b6b;
                  margin-right: 20rpx;
                }

                .time-text {
                  font-size: 22rpx;
                  color: #999;
                }
              }

              .my-voucher-info {
                .info-text {
                  font-size: 22rpx;
                  color: #666;
                  display: block;
                  margin-bottom: 5rpx;

                  &:last-child {
                    margin-bottom: 0;
                  }
                }
              }
            }

            .voucher-btn {
              padding: 15rpx 30rpx;
              border-radius: 30rpx;
              font-size: 24rpx;
              border: none;

              &.normal {
                background: linear-gradient(135deg, #667eea, #764ba2);
                color: #fff;
              }

              &.seckill {
                background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
                color: #fff;
              }

              &.available {
                background: linear-gradient(135deg, #52c41a, #73d13d);
                color: #fff;
              }

              &.warning {
                background: linear-gradient(135deg, #faad14, #ffc53d);
                color: #fff;
              }

              &.disabled {
                background: #ddd;
                color: #999;
              }
            }
          }
        }
      }

      // 空状态
      .empty-state {
        text-align: center;
        padding: 100rpx 0;

        .empty-icon {
          font-size: 120rpx;
          margin-bottom: 30rpx;
          display: block;
        }

        .empty-text {
          font-size: 32rpx;
          color: #fff;
          margin-bottom: 15rpx;
          display: block;
        }

        .empty-desc {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.7);
          display: block;
        }
      }

      // 加载状态
      .loading-state {
        text-align: center;
        padding: 40rpx 0;
        color: #fff;
        font-size: 28rpx;
      }

      // 加载更多
      .load-more {
        text-align: center;
        padding: 30rpx 0;
        color: rgba(255, 255, 255, 0.7);
        font-size: 24rpx;
      }
    }
  }
}
</style>
