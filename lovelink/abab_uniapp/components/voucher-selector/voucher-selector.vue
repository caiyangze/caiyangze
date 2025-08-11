<template>
  <view class="voucher-selector">
    <!-- 优惠券选择区域 -->
    <view class="voucher-section" @click="showVoucherModal">
      <view class="voucher-info">
        <text class="voucher-icon">🎫</text>
        <view class="voucher-text">
          <text class="voucher-label">优惠券</text>
          <text class="voucher-desc" v-if="!selectedVoucher">选择可用优惠券</text>
          <text class="voucher-desc selected" v-else>已选择 {{ selectedVoucher.voucher.actualValue }}币优惠券</text>
        </view>
      </view>
      <view class="voucher-arrow">
        <text class="arrow-icon">></text>
        <text class="voucher-count" v-if="availableCount > 0">({{ availableCount }}张可用)</text>
      </view>
    </view>

    <!-- 优惠券弹窗 -->
    <view v-if="showModal" class="modal-overlay" @click="hideModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <view class="header-left">
            <text class="modal-title">选择优惠券</text>
            <text class="voucher-count">{{ availableVouchers.length }}张可用</text>
          </view>
          <view class="modal-close" @click="hideModal">
            <text class="close-icon">✕</text>
          </view>
        </view>

        <scroll-view scroll-y class="voucher-list" :show-scrollbar="false">
          <!-- 不使用优惠券选项 -->
          <view
            class="voucher-card no-voucher-card"
            :class="{ selected: !tempSelectedVoucher }"
            @click="selectVoucher(null)"
          >
            <view class="card-content">
              <view class="no-voucher-icon">🚫</view>
              <view class="no-voucher-info">
                <text class="no-voucher-title">不使用优惠券</text>
                <text class="no-voucher-desc">按原价支付</text>
              </view>
              <view class="select-indicator" :class="{ active: !tempSelectedVoucher }">
                <text class="check-icon">✓</text>
              </view>
            </view>
          </view>

          <!-- 可用优惠券列表 -->
          <view
            v-for="voucher in availableVouchers"
            :key="voucher.id"
            class="voucher-card"
            :class="{ selected: tempSelectedVoucher && tempSelectedVoucher.id === voucher.id }"
            @click="selectVoucher(voucher)"
          >
            <view class="card-content">
              <view class="voucher-left-section">
                <view class="voucher-amount">
                  <text class="amount-number">{{ voucher.voucher.actualValue }}</text>
                  <text class="amount-unit">币</text>
                </view>
                <view class="voucher-tag">优惠券</view>
              </view>

              <view class="voucher-divider"></view>

              <view class="voucher-right-section">
                <view class="voucher-info">
                  <text class="voucher-title">{{ voucher.voucher.voucherName || '虚拟币优惠券' }}</text>
                  <text class="voucher-desc">消费满{{ getMinAmount(voucher) }}币可用</text>
                  <view class="voucher-expire-info">
                    <text class="expire-label">有效期至</text>
                    <text class="expire-date">{{ formatDate(voucher.voucher.endTime) }}</text>
                  </view>
                </view>

                <view class="select-indicator" :class="{ active: tempSelectedVoucher && tempSelectedVoucher.id === voucher.id }">
                  <text class="check-icon">✓</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 无可用优惠券 -->
          <view v-if="availableVouchers.length === 0 && !loading" class="empty-state">
            <view class="empty-icon">🎫</view>
            <text class="empty-title">暂无可用优惠券</text>
            <text class="empty-desc">当前消费金额无可用优惠券</text>
            <button class="go-voucher-btn" @click="goToVoucherCenter">
              <text>去优惠券中心</text>
            </button>
          </view>

          <!-- 加载状态 -->
          <view v-if="loading" class="loading-state">
            <view class="loading-spinner"></view>
            <text class="loading-text">加载中...</text>
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="hideModal">取消</button>
          <button class="confirm-btn" @click="confirmSelection">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getMyAvailableVouchers } from '@/api/voucher'

export default {
  name: 'VoucherSelector',
  props: {
    // 消费金额，用于筛选可用优惠券
    consumeAmount: {
      type: Number,
      default: 0
    },
    // 消费类型，用于筛选适用的优惠券
    consumeType: {
      type: String,
      default: 'all' // all, heart-match, matchmaker-apply, vip, recharge
    }
  },
  data() {
    return {
      showModal: false,
      availableVouchers: [],
      selectedVoucher: null,
      tempSelectedVoucher: null,
      loading: false
    }
  },
  computed: {
    availableCount() {
      return this.availableVouchers.length
    }
  },
  mounted() {
    this.loadAvailableVouchers()
  },
  methods: {
    // 显示优惠券选择弹窗
    showVoucherModal() {
      this.tempSelectedVoucher = this.selectedVoucher
      this.showModal = true
      this.loadAvailableVouchers()
    },

    // 隐藏弹窗
    hideModal() {
      this.showModal = false
      this.tempSelectedVoucher = null
    },

    // 加载可用优惠券
    async loadAvailableVouchers() {
      if (this.loading) return

      this.loading = true
      try {
        const response = await getMyAvailableVouchers()

        if (response.code === 200) {
          // 筛选可用的优惠券
          this.availableVouchers = response.data.records.filter(voucher => {
            return this.isVoucherAvailable(voucher)
          })
        }
      } catch (error) {
        console.error('加载优惠券失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 判断优惠券是否可用
    isVoucherAvailable(voucher) {
      // 检查是否未使用（status=2表示已支付未使用）
      if (voucher.status !== 2) {
        return false
      }

      // 检查优惠券信息是否存在
      if (!voucher.voucher) {
        return false
      }

      // 检查优惠券抵扣金额是否大于0
      if (!voucher.voucher.actualValue || voucher.voucher.actualValue <= 0) {
        return false
      }

      // 检查消费金额是否大于等于优惠券抵扣金额（消费金额大于等于优惠券金额时才能使用）
      if (this.consumeAmount < voucher.voucher.actualValue) {
        // 如果消费金额小于优惠券金额，不能使用优惠券
        return false
      }

      return true
    },

    // 选择优惠券
    selectVoucher(voucher) {
      this.tempSelectedVoucher = voucher
    },

    // 确认选择
    confirmSelection() {
      this.selectedVoucher = this.tempSelectedVoucher
      this.showModal = false
      
      // 触发选择事件
      this.$emit('voucher-selected', this.selectedVoucher)
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },

    // 前往优惠券中心
    goToVoucherCenter() {
      this.hideModal()
      uni.navigateTo({
        url: '/pages/voucher/voucher-list'
      })
    },

    // 获取选中的优惠券
    getSelectedVoucher() {
      return this.selectedVoucher
    },

    // 清除选中的优惠券
    clearSelection() {
      this.selectedVoucher = null
      this.$emit('voucher-selected', null)
    },

    // 获取优惠券最小使用金额
    getMinAmount(voucher) {
      // 根据优惠券抵扣金额计算最小使用金额
      // 消费金额大于等于优惠券金额即可使用
      return voucher.voucher.actualValue || 0
    }
  }
}
</script>

<style lang="scss" scoped>
.voucher-selector {
  .voucher-section {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx;
    background: white;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    border: 2rpx solid #f0f0f0;

    .voucher-info {
      display: flex;
      align-items: center;

      .voucher-icon {
        font-size: 32rpx;
        margin-right: 16rpx;
      }

      .voucher-text {
        .voucher-label {
          font-size: 28rpx;
          color: #333;
          display: block;
          margin-bottom: 4rpx;
        }

        .voucher-desc {
          font-size: 24rpx;
          color: #999;

          &.selected {
            color: #ff6b35;
          }
        }
      }
    }

    .voucher-arrow {
      display: flex;
      align-items: center;

      .arrow-icon {
        font-size: 24rpx;
        color: #999;
        margin-right: 8rpx;
      }

      .voucher-count {
        font-size: 20rpx;
        color: #ff6b35;
      }
    }
  }

  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(4rpx);

    .modal-content {
      width: 92%;
      max-width: 680rpx;
      max-height: 85vh;
      background: white;
      border-radius: 24rpx;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);

      .modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 40rpx 32rpx 24rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;

        .header-left {
          .modal-title {
            font-size: 36rpx;
            font-weight: 600;
            margin-bottom: 8rpx;
          }

          .voucher-count {
            font-size: 24rpx;
            opacity: 0.9;
          }
        }

        .modal-close {
          width: 60rpx;
          height: 60rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.2);

          .close-icon {
            font-size: 32rpx;
            color: white;
          }
        }
      }

      .voucher-list {
        flex: 1;
        padding: 24rpx 0;
        max-height: 60vh;

        .voucher-card {
          margin: 0 24rpx 24rpx;
          border-radius: 16rpx;
          overflow: hidden;
          box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
          transition: all 0.3s ease;

          &.selected {
            transform: scale(1.02);
            box-shadow: 0 8rpx 30rpx rgba(102, 126, 234, 0.3);
            border: 3rpx solid #667eea;
          }

          &.no-voucher-card {
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
            border: 2rpx dashed #dee2e6;

            &.selected {
              border-color: #667eea;
              background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            }
          }

          .card-content {
            display: flex;
            align-items: center;
            padding: 32rpx;
            background: white;
            position: relative;

            .no-voucher-icon {
              font-size: 48rpx;
              margin-right: 24rpx;
              opacity: 0.6;
            }

            .no-voucher-info {
              flex: 1;

              .no-voucher-title {
                font-size: 32rpx;
                color: #495057;
                font-weight: 600;
                display: block;
                margin-bottom: 8rpx;
              }

              .no-voucher-desc {
                font-size: 24rpx;
                color: #6c757d;
              }
            }

            .voucher-left-section {
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              width: 160rpx;
              background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
              color: white;
              border-radius: 12rpx;
              padding: 24rpx 16rpx;
              margin-right: 24rpx;
              position: relative;

              .voucher-amount {
                display: flex;
                align-items: baseline;
                margin-bottom: 8rpx;

                .amount-number {
                  font-size: 48rpx;
                  font-weight: 700;
                  line-height: 1;
                }

                .amount-unit {
                  font-size: 24rpx;
                  margin-left: 4rpx;
                  opacity: 0.9;
                }
              }

              .voucher-tag {
                font-size: 20rpx;
                background: rgba(255, 255, 255, 0.2);
                padding: 4rpx 12rpx;
                border-radius: 20rpx;
                font-weight: 500;
              }
            }

            .voucher-divider {
              width: 2rpx;
              height: 80rpx;
              background: linear-gradient(to bottom, transparent, #e9ecef, transparent);
              margin-right: 24rpx;
            }

            .voucher-right-section {
              flex: 1;
              display: flex;
              align-items: center;
              justify-content: space-between;

              .voucher-info {
                flex: 1;

                .voucher-title {
                  font-size: 32rpx;
                  color: #212529;
                  font-weight: 600;
                  display: block;
                  margin-bottom: 12rpx;
                }

                .voucher-desc {
                  font-size: 24rpx;
                  color: #6c757d;
                  margin-bottom: 16rpx;
                }

                .voucher-expire-info {
                  display: flex;
                  align-items: center;

                  .expire-label {
                    font-size: 22rpx;
                    color: #adb5bd;
                    margin-right: 8rpx;
                  }

                  .expire-date {
                    font-size: 22rpx;
                    color: #fd7e14;
                    font-weight: 500;
                  }
                }
              }
            }

            .select-indicator {
              width: 48rpx;
              height: 48rpx;
              border-radius: 50%;
              border: 3rpx solid #dee2e6;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-left: 16rpx;
              transition: all 0.3s ease;

              &.active {
                background: #667eea;
                border-color: #667eea;

                .check-icon {
                  color: white;
                  font-size: 24rpx;
                  font-weight: 700;
                }
              }

              .check-icon {
                color: transparent;
                font-size: 24rpx;
                font-weight: 700;
              }
            }
          }
        }

        .empty-state {
          padding: 80rpx 40rpx;
          text-align: center;

          .empty-icon {
            font-size: 80rpx;
            display: block;
            margin-bottom: 24rpx;
          }

          .empty-title {
            font-size: 36rpx;
            color: #495057;
            font-weight: 600;
            display: block;
            margin-bottom: 16rpx;
          }

          .empty-desc {
            font-size: 28rpx;
            color: #6c757d;
            display: block;
            margin-bottom: 48rpx;
            line-height: 1.5;
          }

          .go-voucher-btn {
            width: 240rpx;
            height: 72rpx;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 36rpx;
            font-size: 28rpx;
            font-weight: 600;
            box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);

            text {
              color: white;
            }
          }
        }

        .loading-state {
          padding: 100rpx 40rpx;
          text-align: center;
          display: flex;
          flex-direction: column;
          align-items: center;

          .loading-spinner {
            width: 60rpx;
            height: 60rpx;
            border: 4rpx solid #f3f3f3;
            border-top: 4rpx solid #667eea;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-bottom: 24rpx;
          }

          .loading-text {
            font-size: 28rpx;
            color: #6c757d;
          }
        }
      }

      .modal-footer {
        padding: 32rpx;
        background: #f8f9fa;
        display: flex;
        gap: 24rpx;

        .cancel-btn {
          flex: 1;
          height: 88rpx;
          background: white;
          color: #6c757d;
          border: 2rpx solid #dee2e6;
          border-radius: 16rpx;
          font-size: 32rpx;
          font-weight: 500;
        }

        .confirm-btn {
          flex: 2;
          height: 88rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border: none;
          border-radius: 16rpx;
          font-size: 32rpx;
          font-weight: 600;
          box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
        }
      }
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
