<template>
  <view class="apply-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">申请成为红娘</text>
      <view class="nav-right"></view>
    </view>

    <!-- 全屏申请状态显示 -->
    <view class="fullscreen-status" v-if="showFullscreenStatus">
      <!-- 背景装饰 -->
      <view class="status-background">
        <view class="bg-circle circle-1"></view>
        <view class="bg-circle circle-2"></view>
        <view class="bg-circle circle-3"></view>
      </view>

      <!-- 状态内容 -->
      <view class="status-content">
        <!-- 状态图标 -->
        <view class="status-icon-container">
          <view class="status-icon" :class="getApplicationStatusClass()">
            <text v-if="applicationStatus.statusCode === 1" class="icon-text success">✓</text>
            <view v-else-if="applicationStatus.statusCode === 0" class="loading-icon">
              <view class="loading-spinner"></view>
            </view>
            <text v-else class="icon-text error">✗</text>
          </view>
        </view>

        <!-- 状态信息 -->
        <view class="status-info">
          <text class="status-title" :class="getApplicationStatusClass()">
            {{ getApplicationStatusTitle() }}
          </text>
          <text class="status-desc">
            {{ getApplicationStatusDesc() }}
          </text>
        </view>

        <!-- 操作按钮 -->
        <view class="status-actions">
          <button v-if="applicationStatus.statusCode === 1" class="action-btn success-btn" @click="goBackToHome">
            开始接单
          </button>
          <button v-else-if="applicationStatus.statusCode === 2" class="action-btn retry-btn" @click="retryApplication">
            重新申请
          </button>
          <button v-else class="action-btn waiting-btn" @click="goBackToHome">
            返回首页
          </button>
        </view>

        <!-- 额外信息 -->
        <view class="status-extra" v-if="applicationStatus.statusCode === 0">
          <text class="extra-text">预计审核时间：1-3个工作日</text>
          <text class="extra-text">我们会通过站内消息通知您审核结果</text>
        </view>

        <view class="status-extra" v-else-if="applicationStatus.statusCode === 1">
          <text class="extra-text">恭喜您成为平台认证红娘！</text>
          <text class="extra-text">现在可以开始为用户提供专业的婚恋服务</text>
        </view>

        <view class="status-extra" v-else-if="applicationStatus.statusCode === 2 && applicationStatus.rejectReason">
          <text class="extra-text">拒绝原因：{{ applicationStatus.rejectReason }}</text>
        </view>
      </view>
    </view>

    <!-- 费用提示 -->
    <view class="cost-banner">
      <view class="cost-icon">💰</view>
      <view class="cost-info">
        <text class="cost-title">申请费用：699虚拟币</text>
        <text class="cost-desc">人人都可申请，成为专业红娘，开启收益之路</text>
      </view>
    </view>

    <!-- 申请说明 -->
    <view class="info-card">
      <view class="info-header">
        <text class="info-icon">ℹ️</text>
        <text class="info-title">申请条件</text>
      </view>
      <view class="info-content">
        <view class="info-item" :class="{ completed: userStatus.hasPhone, clickable: !userStatus.hasPhone }" @click="handleConditionClick('phone')">
          <text class="condition-icon">{{ userStatus.hasPhone ? '✓' : '○' }}</text>
          <text class="condition-text">已绑定手机号</text>
          <text v-if="!userStatus.hasPhone" class="action-hint">点击绑定</text>
        </view>
        <view class="info-item" :class="{ completed: userStatus.hasGender, clickable: !userStatus.hasGender }" @click="handleConditionClick('gender')">
          <text class="condition-icon">{{ userStatus.hasGender ? '✓' : '○' }}</text>
          <text class="condition-text">已选择性别</text>
          <text v-if="!userStatus.hasGender" class="action-hint">点击设置</text>
        </view>
        <view class="info-item" :class="{ completed: userStatus.isVerified, clickable: !userStatus.isVerified }" @click="handleConditionClick('verify')">
          <text class="condition-icon">{{ userStatus.isVerified ? '✓' : '○' }}</text>
          <text class="condition-text">已完成实名认证</text>
          <text v-if="!userStatus.isVerified" class="action-hint">点击认证</text>
        </view>
        <view class="info-item" :class="{ completed: userStatus.hasEnoughCoins, clickable: !userStatus.hasEnoughCoins }" @click="handleConditionClick('coins')">
          <text class="condition-icon">{{ userStatus.hasEnoughCoins ? '✓' : '○' }}</text>
          <text class="condition-text">拥有699个虚拟币</text>
          <text v-if="!userStatus.hasEnoughCoins" class="action-hint">点击充值</text>
        </view>
      </view>
      <text class="info-note">满足以上条件即可申请，无需VIP身份</text>
    </view>

    <!-- 申请表单 -->
    <view class="form-container">
      <!-- 基本信息 -->
      <view class="form-section">
        <text class="section-title">基本信息</text>

        <view class="form-item">
          <text class="form-label">真实姓名 *</text>
          <input
            class="form-input"
            :class="{ 'readonly-input': verificationFields.realName }"
            v-model="formData.realName"
            :placeholder="verificationFields.realName ? '已从实名认证自动填充' : '请输入真实姓名'"
            :disabled="verificationFields.realName"
            maxlength="20"
          />
          <text v-if="verificationFields.realName" class="auto-fill-tip">✓ 已从实名认证自动填充</text>
        </view>

        <view class="form-item">
          <text class="form-label">身份证号 *</text>
          <input
            class="form-input"
            :class="{ 'readonly-input': verificationFields.idCardNo }"
            v-model="formData.idCardNo"
            :placeholder="verificationFields.idCardNo ? '已从实名认证自动填充' : '请输入身份证号'"
            :disabled="verificationFields.idCardNo"
            maxlength="18"
          />
          <text v-if="verificationFields.idCardNo" class="auto-fill-tip">✓ 已从实名认证自动填充</text>
        </view>
      </view>

      <!-- 身份证照片 -->
      <view class="form-section">
        <text class="section-title">身份证照片</text>

        <view class="upload-section">
          <view class="upload-item">
            <text class="upload-label">身份证正面 *</text>
            <view class="upload-box" :class="{ 'readonly-upload': verificationFields.idCardFront }" @click="!verificationFields.idCardFront && uploadIdCardFront()">
              <image v-if="formData.idCardFront" :src="formData.idCardFront" class="upload-image" />
              <view v-else class="upload-placeholder">
                <text class="upload-icon">📷</text>
                <text class="upload-text">点击上传</text>
              </view>
              <view v-if="verificationFields.idCardFront" class="auto-fill-overlay">
                <text class="auto-fill-text">✓ 已从实名认证自动填充</text>
              </view>
            </view>
          </view>

          <view class="upload-item">
            <text class="upload-label">身份证背面 *</text>
            <view class="upload-box" :class="{ 'readonly-upload': verificationFields.idCardBack }" @click="!verificationFields.idCardBack && uploadIdCardBack()">
              <image v-if="formData.idCardBack" :src="formData.idCardBack" class="upload-image" />
              <view v-else class="upload-placeholder">
                <text class="upload-icon">📷</text>
                <text class="upload-text">点击上传</text>
              </view>
              <view v-if="verificationFields.idCardBack" class="auto-fill-overlay">
                <text class="auto-fill-text">✓ 已从实名认证自动填充</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 服务信息 -->
      <view class="form-section">
        <text class="section-title">服务信息</text>

        <view class="form-item">
          <text class="form-label">期望服务区域 *</text>
          <input
            class="form-input"
            v-model="formData.serviceArea"
            placeholder="如：北京市朝阳区"
            maxlength="50"
          />
        </view>

        <view class="form-item">
          <text class="form-label">联系电话 *</text>
          <input
            class="form-input"
            v-model="formData.contactPhone"
            placeholder="请输入联系电话（可与注册手机号不同）"
            type="number"
            maxlength="11"
          />
        </view>
      </view>

      
      <!-- 个人介绍 -->
      <view class="form-section">
        <text class="section-title">个人介绍</text>
        
        <view class="form-item">
          <text class="form-label">个人简介 *</text>
          <textarea
            class="form-textarea"
            v-model="formData.introduction"
            placeholder="请介绍您的个人背景、专业能力等（0-500字）"
            maxlength="500"
          />
          <text class="char-count">{{ formData.introduction.length }}/500</text>
        </view>
        
        <view class="form-item">
          <text class="form-label">相关经验 *</text>
          <textarea
            class="form-textarea"
            v-model="formData.experience"
            placeholder="请描述您的相关工作经验、成功案例等（0-500字）"
            maxlength="500"
          />
          <text class="char-count">{{ formData.experience.length }}/500</text>
        </view>
      </view>
    </view>
    
    <!-- 提交按钮 -->
    <view class="submit-container">
      <button 
        class="submit-btn" 
        :class="{ disabled: !canSubmit || isSubmitting }"
        :disabled="!canSubmit || isSubmitting"
        @click="showConfirmDialog"
      >
        <text class="submit-text">
          {{ isSubmitting ? '提交中...' : '提交申请（699币）' }}
        </text>
      </button>
    </view>
    
    <!-- 确认弹窗 -->
    <view v-if="showConfirm" class="confirm-overlay" @click="hideConfirmDialog">
      <view class="confirm-modal" @click.stop>
        <view class="confirm-header">
          <text class="confirm-title">确认申请</text>
        </view>
        
        <view class="confirm-content">
          <text class="confirm-text">申请成为红娘需要消耗</text>
          <text class="confirm-amount">699个虚拟币</text>
          <text class="confirm-text">确认提交申请吗？</text>

          <view class="balance-info" v-if="currentBalance >= 0">
            <text class="balance-label">当前余额：</text>
            <text class="balance-value" :class="{ insufficient: currentBalance < getActualCost() }">
              {{ currentBalance }}币
            </text>
          </view>

          <!-- 优惠券选择 -->
          <voucher-selector
            ref="voucherSelector"
            :consume-amount="699"
            consume-type="matchmaker-apply"
            @voucher-selected="onVoucherSelected"
          />

          <!-- 优惠券抵扣信息 -->
          <view v-if="selectedVoucher" class="voucher-discount-info">
            <text class="discount-label">优惠券抵扣：</text>
            <text class="discount-amount">-{{ selectedVoucher.voucher.actualValue }}币</text>
          </view>

          <!-- 实际消费金额 -->
          <view class="actual-cost-info">
            <text class="actual-cost-label">实际消费：</text>
            <text class="actual-cost-amount">{{ getActualCost() }}币</text>
          </view>
        </view>
        
        <view class="confirm-buttons">
          <button class="confirm-btn cancel-btn" @click="hideConfirmDialog">取消</button>
          <button
            class="confirm-btn submit-btn"
            :class="{ disabled: currentBalance < getActualCost() }"
            :disabled="currentBalance < getActualCost()"
            @click="submitApplication"
          >
            {{ currentBalance < getActualCost() ? '余额不足' : '确认申请' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import http from '@/api/http'
import { getWalletInfo, consume } from '@/api/wallet'
import { getByUserInfo } from '@/api/user/auth'
import { applyMatchmaker, getUserVerification, checkApplicationStatus } from '@/api/matchmaker'
import VoucherSelector from '@/components/voucher-selector/voucher-selector.vue'

export default {
  components: {
    VoucherSelector
  },

  data() {
    return {
      formData: {
        realName: '',
        phone: '',
        idCardNo: '',
        idCardFront: '',
        idCardBack: '',
        serviceArea: '',
        contactPhone: '',
        introduction: '',
        experience: ''
      },
      showConfirm: false,
      isSubmitting: false,
      currentBalance: -1,
      selectedVoucher: null, // 选中的优惠券
      userStatus: {
        hasPhone: false,
        hasGender: false,
        isVerified: false,
        hasEnoughCoins: false
      },
      // 标记哪些字段来自实名认证（不可编辑）
      verificationFields: {
        realName: false,
        idCardNo: false,
        idCardFront: false,
        idCardBack: false
      },
      // 申请状态信息
      applicationStatus: {
        hasApplied: false,
        isMatchmaker: false,
        status: '',
        applicationId: null,
        statusCode: null,
        rejectReason: ''
      },
      // 控制是否显示全屏状态
      showFullscreenStatus: false
    }
  },
  
  computed: {
    canSubmit() {
      // 如果已经申请过且不是被拒绝状态，则不能提交
      if (this.applicationStatus.hasApplied && this.applicationStatus.statusCode !== 2) {
        return false
      }

      const { realName, idCardNo, idCardFront, idCardBack, serviceArea, contactPhone, introduction, experience } = this.formData

      // 表单完整性检查
      const formComplete = realName && idCardNo && idCardFront && idCardBack &&
                           serviceArea && contactPhone &&
                           introduction.length >= 0 && experience.length >= 0

      // 用户状态检查
      const statusComplete = this.userStatus.hasPhone && this.userStatus.hasGender &&
                            this.userStatus.isVerified && this.userStatus.hasEnoughCoins

      return formComplete && statusComplete
    },

    // 所有条件是否满足
    allConditionsMet() {
      return this.userStatus.hasPhone && this.userStatus.hasGender &&
             this.userStatus.isVerified && this.userStatus.hasEnoughCoins
    }
  },
  
  methods: {
    goBack() {
      uni.navigateBack()
    },

    
    // 显示确认弹窗
    async showConfirmDialog() {
      await this.loadUserBalance()
      this.showConfirm = true
    },
    
    // 隐藏确认弹窗
    hideConfirmDialog() {
      this.showConfirm = false
    },
    
    // 加载用户余额
    async loadUserBalance() {
      try {
        const response = await getWalletInfo()
        if (response.code === 200) {
          this.currentBalance = response.data.coinBalance || 0
        } else {
          this.currentBalance = 0
        }
      } catch (error) {
        console.error('获取余额失败:', error)
        this.currentBalance = 0
      }
    },
    
    // 提交申请
    async submitApplication() {
      if (this.currentBalance < 699) {
        uni.showModal({
          title: '余额不足',
          content: '您的虚拟币余额不足，需要699币才能申请成为红娘。是否前往充值？',
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
      
      this.isSubmitting = true
      this.showConfirm = false
      
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
        
        // 准备提交数据，将contactPhone映射到phone
        const submitData = {
          ...this.formData,
          phone: this.formData.contactPhone
        }

        // 如果选择了优惠券，添加优惠券信息
        if (this.selectedVoucher) {
          submitData.voucherOrderId = this.selectedVoucher.id
          submitData.voucherDiscountAmount = this.selectedVoucher.voucher.actualValue
          console.log('申请红娘使用优惠券，优惠券订单ID：', this.selectedVoucher.id, '抵扣金额：', this.selectedVoucher.voucher.actualValue)
        }

        console.log('申请红娘提交数据：', submitData)
        const response = await applyMatchmaker(submitData)
        
        if (response.code === 200) {
          // 更新申请状态并显示全屏状态页面
          this.applicationStatus = {
            hasApplied: true,
            isMatchmaker: false,
            status: '申请审核中',
            applicationId: response.data?.applicationId,
            statusCode: 0, // 0表示审核中
            rejectReason: ''
          }
          this.showFullscreenStatus = true
        } else {
          uni.showToast({
            title: response.message || '申请失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('申请失败:', error)
        uni.showToast({
          title: '申请失败，请重试',
          icon: 'none'
        })
      } finally {
        this.isSubmitting = false
      }
    },

    // 检查申请状态
    async checkApplicationStatus() {
      try {
        const response = await checkApplicationStatus()
        if (response.code === 200 && response.data) {
          const data = response.data
          this.applicationStatus = {
            hasApplied: data.hasApplied,
            isMatchmaker: data.isMatchmaker,
            status: data.status,
            applicationId: data.applicationId,
            statusCode: data.applicationStatus,
            rejectReason: data.rejectReason || ''
          }

          // 如果已经申请过，显示相应的提示
          if (data.hasApplied) {
            this.showApplicationStatusModal()
          }
        }
      } catch (error) {
        console.error('检查申请状态失败:', error)
      }
    },

    // 显示申请状态
    showApplicationStatusModal() {
      const { hasApplied, isMatchmaker } = this.applicationStatus

      if (isMatchmaker || hasApplied) {
        // 显示全屏状态页面
        this.showFullscreenStatus = true
      }
    },

    // 获取用户实名认证信息
    async loadUserVerification() {
      try {
        const response = await getUserVerification()
        if (response.code === 200 && response.data) {
          // 用户已实名认证，自动填充信息
          const verificationData = response.data
          this.formData.realName = verificationData.realName || ''
          this.formData.idCardNo = verificationData.idCardNo || ''
          this.formData.idCardFront = verificationData.idCardFront || ''
          this.formData.idCardBack = verificationData.idCardBack || ''

          // 标记这些字段来自实名认证，不可编辑
          this.verificationFields.realName = !!verificationData.realName
          this.verificationFields.idCardNo = !!verificationData.idCardNo
          this.verificationFields.idCardFront = !!verificationData.idCardFront
          this.verificationFields.idCardBack = !!verificationData.idCardBack

          console.log('已自动填充实名认证信息')
        }
      } catch (error) {
        console.error('获取实名认证信息失败:', error)
      }
    },

    // 检查用户状态
    async checkUserStatus() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          return
        }

        // 获取用户信息
        const userResponse = await getByUserInfo(token)

        if (userResponse.code === 200) {
          const userInfo = userResponse.data
          this.userStatus.hasPhone = !!userInfo.phone
          this.userStatus.hasGender = userInfo.gender > 0
          this.userStatus.isVerified = userInfo.isVerified === 1
        }

        // 获取余额信息
        await this.loadUserBalance()
        this.userStatus.hasEnoughCoins = this.currentBalance >= 699

      } catch (error) {
        console.error('检查用户状态失败:', error)
      }
    },

    // 处理条件点击
    handleConditionClick(type) {
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

      switch (type) {
        case 'phone':
          if (!this.userStatus.hasPhone) {
            uni.showModal({
              title: '绑定手机号',
              content: '需要绑定手机号才能申请红娘，是否前往绑定？',
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({
                    url: '/pages/profile/bind-phone'
                  })
                }
              }
            })
          }
          break

        case 'gender':
          if (!this.userStatus.hasGender) {
            uni.showModal({
              title: '选择性别',
              content: '需要选择性别才能申请红娘，是否前往设置？',
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({
                    url: '/pages/profile/edit-profile'
                  })
                }
              }
            })
          }
          break

        case 'verify':
          if (!this.userStatus.isVerified) {
            uni.showModal({
              title: '实名认证',
              content: '需要完成实名认证才能申请红娘，是否前往认证？',
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({
                    url: '/pages/profile/verify'
                  })
                }
              }
            })
          }
          break

        case 'coins':
          if (!this.userStatus.hasEnoughCoins) {
            uni.showModal({
              title: '余额不足',
              content: `当前余额：${this.currentBalance}币，需要699币才能申请红娘。是否前往充值？`,
              success: (res) => {
                if (res.confirm) {
                  uni.navigateTo({
                    url: '/pages/wallet/wallet'
                  })
                }
              }
            })
          }
          break
      }
    },

    // 上传身份证正面
    uploadIdCardFront() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ['camera'] : ['album']

          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: sourceType,
            success: (res) => {
              // 这里暂时使用本地路径，实际应该上传到服务器
              this.formData.idCardFront = res.tempFilePaths[0]
              uni.showToast({
                title: '上传成功',
                icon: 'success'
              })
            },
            fail: (err) => {
              console.error('上传失败:', err)
              uni.showToast({
                title: '上传失败，请重试',
                icon: 'none'
              })
            }
          })
        }
      })
    },

    // 上传身份证背面
    uploadIdCardBack() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ['camera'] : ['album']

          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: sourceType,
            success: (res) => {
              // 这里暂时使用本地路径，实际应该上传到服务器
              this.formData.idCardBack = res.tempFilePaths[0]
              uni.showToast({
                title: '上传成功',
                icon: 'success'
              })
            },
            fail: (err) => {
              console.error('上传失败:', err)
              uni.showToast({
                title: '上传失败，请重试',
                icon: 'none'
              })
            }
          })
        }
      })
    },

    // 优惠券选择回调
    onVoucherSelected(voucher) {
      this.selectedVoucher = voucher
    },

    // 计算实际消费金额
    getActualCost() {
      const originalCost = 699
      if (this.selectedVoucher && this.selectedVoucher.voucher) {
        const discount = this.selectedVoucher.voucher.actualValue
        return Math.max(0, originalCost - discount)
      }
      return originalCost
    },

    // 获取申请状态样式类
    getApplicationStatusClass() {
      if (!this.applicationStatus.statusCode && this.applicationStatus.statusCode !== 0) return ''
      switch (this.applicationStatus.statusCode) {
        case 1: return 'success'
        case 2: return 'failed'
        case 0: return 'pending'
        default: return ''
      }
    },

    // 获取申请状态标题
    getApplicationStatusTitle() {
      if (this.applicationStatus.isMatchmaker) {
        return '您已是红娘'
      }

      switch (this.applicationStatus.statusCode) {
        case 1: return '申请通过'
        case 2: return '申请被拒'
        case 0: return '申请审核中'
        default: return '申请状态'
      }
    },

    // 获取申请状态描述
    getApplicationStatusDesc() {
      if (this.applicationStatus.isMatchmaker) {
        return '您已经是平台认证的红娘，无需重复申请'
      }

      switch (this.applicationStatus.statusCode) {
        case 1: return '恭喜您通过红娘申请审核！'
        case 2: return this.applicationStatus.rejectReason || '很遗憾，您的申请未通过审核'
        case 0: return '您的红娘申请已提交成功，已扣除699个虚拟币，请等待审核'
        default: return ''
      }
    },

    // 返回首页
    goBackToHome() {
      console.log('点击返回首页按钮')

      // 由于首页不是tabBar页面，使用reLaunch跳转
      uni.reLaunch({
        url: '/pages/index/index',
        success: () => {
          console.log('成功跳转到首页')
        },
        fail: (err) => {
          console.error('跳转首页失败:', err)
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          })
        }
      })
    },

    // 重新申请
    retryApplication() {
      this.showFullscreenStatus = false
      this.applicationStatus.hasApplied = false
    }
  },

  // 页面加载时检查用户状态、申请状态和加载实名认证信息
  onLoad() {
    this.checkUserStatus()
    this.loadUserVerification()
    this.checkApplicationStatus()
  },

  // 页面显示时重新检查用户状态、申请状态和加载实名认证信息
  onShow() {
    this.checkUserStatus()
    this.loadUserVerification()
    this.checkApplicationStatus()
  }
}
</script>

<style lang="scss" scoped>
.apply-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  width: 100%;
  box-sizing: border-box;
  
  .nav-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
    background: white;
    border-bottom: 1rpx solid #eee;
    width: 100%;
    box-sizing: border-box;
    
    .nav-left {
      width: 80rpx;
      
      .nav-icon {
        font-size: 36rpx;
        color: #333;
      }
    }
    
    .nav-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }
    
    .nav-right {
      width: 80rpx;
    }
  }
  
  .cost-banner {
    display: flex;
    align-items: center;
    padding: 32rpx;
    margin: 20rpx 32rpx;
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
    border-radius: 20rpx;
    width: calc(100% - 64rpx);
    box-sizing: border-box;
    
    .cost-icon {
      font-size: 48rpx;
      margin-right: 24rpx;
    }
    
    .cost-info {
      flex: 1;
      
      .cost-title {
        display: block;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .cost-desc {
        font-size: 28rpx;
        color: #666;
      }
    }
  }

  .info-card {
    margin: 20rpx 32rpx;
    background: white;
    border-radius: 20rpx;
    padding: 32rpx;
    border-left: 8rpx solid #667eea;
    width: calc(100% - 64rpx);
    box-sizing: border-box;

    .info-header {
      display: flex;
      align-items: center;
      margin-bottom: 24rpx;

      .info-icon {
        font-size: 32rpx;
        margin-right: 16rpx;
      }

      .info-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
    }

    .info-content {
      margin-bottom: 20rpx;

      .info-item {
        display: flex;
        align-items: center;
        font-size: 28rpx;
        color: #666;
        line-height: 2;
        padding: 12rpx 20rpx;
        border-radius: 12rpx;
        margin-bottom: 8rpx;
        transition: all 0.3s ease;

        .condition-icon {
          font-size: 32rpx;
          margin-right: 16rpx;
          width: 40rpx;
          text-align: center;
          color: #ccc;
        }

        .condition-text {
          flex: 1;
          color: #666;
        }

        .action-hint {
          font-size: 24rpx;
          color: #667eea;
          padding: 4rpx 12rpx;
          background: rgba(102, 126, 234, 0.1);
          border-radius: 20rpx;
        }

        &.completed {
          .condition-icon {
            color: #52c41a;
          }

          .condition-text {
            color: #333;
          }
        }

        &.clickable {
          cursor: pointer;

          &:hover {
            background: #f8f9fa;
          }

          &:active {
            background: #e9ecef;
            transform: scale(0.98);
          }
        }
      }
    }

    .info-note {
      font-size: 24rpx;
      color: #999;
      font-style: italic;
      text-align: center;
      padding: 16rpx;
      background: #f8f9fa;
      border-radius: 12rpx;
    }
  }

  .form-container {
    padding: 0 32rpx;
    width: 100%;
    box-sizing: border-box;

    .form-section {
      background: white;
      border-radius: 20rpx;
      padding: 32rpx;
      margin-bottom: 20rpx;

      .section-title {
        display: block;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 32rpx;
      }

      .form-item {
        margin-bottom: 32rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .form-label {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 16rpx;
        }

        .form-input {
          width: 100%;
          height: 80rpx;
          padding: 0 24rpx;
          background: #f8f9fa;
          border-radius: 12rpx;
          font-size: 28rpx;
          color: #333;
          border: 2rpx solid transparent;

          &:focus {
            border-color: #667eea;
            background: white;
          }

          &.readonly-input {
            background: #e9ecef;
            color: #6c757d;
            cursor: not-allowed;
          }
        }

        .auto-fill-tip {
          display: block;
          font-size: 24rpx;
          color: #52c41a;
          margin-top: 8rpx;
        }

        .form-textarea {
          width: 100%;
          min-height: 200rpx;
          padding: 24rpx;
          background: #f8f9fa;
          border-radius: 12rpx;
          font-size: 28rpx;
          color: #333;
          border: 2rpx solid transparent;

          &:focus {
            border-color: #667eea;
            background: white;
          }
        }

        .char-count {
          display: block;
          text-align: right;
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
        }
      }
    }

    .upload-section {
      display: flex;
      gap: 32rpx;

      .upload-item {
        flex: 1;

        .upload-label {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 16rpx;
        }

        .upload-box {
          width: 100%;
          height: 200rpx;
          border-radius: 12rpx;
          overflow: hidden;
          border: 2rpx dashed #ddd;
          position: relative;

          .upload-image {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .upload-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: #f8f9fa;

            .upload-icon {
              font-size: 48rpx;
              margin-bottom: 16rpx;
            }

            .upload-text {
              font-size: 24rpx;
              color: #999;
            }
          }

          &.readonly-upload {
            border-color: #52c41a;
            cursor: not-allowed;
          }

          .auto-fill-overlay {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            background: rgba(82, 196, 26, 0.9);
            padding: 8rpx;
            display: flex;
            align-items: center;
            justify-content: center;

            .auto-fill-text {
              font-size: 20rpx;
              color: white;
              text-align: center;
            }
          }
        }
      }
    }
  }

  .submit-container {
    padding: 32rpx;
    width: 100%;
    box-sizing: border-box;

    .submit-btn {
      width: 100%;
      height: 88rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 44rpx;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

      .submit-text {
        font-size: 32rpx;
        font-weight: bold;
        color: white;
      }

      &.disabled {
        background: #bdc3c7;
        box-shadow: none;

        .submit-text {
          color: #7f8c8d;
        }
      }

      &:active:not(.disabled) {
        transform: translateY(2rpx);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      }
    }
  }

  .confirm-overlay {
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

    .confirm-modal {
      width: 600rpx;
      background: white;
      border-radius: 20rpx;
      overflow: hidden;

      .confirm-header {
        padding: 40rpx;
        text-align: center;
        border-bottom: 1rpx solid #eee;

        .confirm-title {
          font-size: 36rpx;
          font-weight: bold;
          color: #333;
        }
      }

      .confirm-content {
        padding: 40rpx;
        text-align: center;

        .confirm-text {
          font-size: 28rpx;
          color: #666;
          line-height: 1.5;
        }

        .confirm-amount {
          font-size: 36rpx;
          font-weight: bold;
          color: #ff4757;
          margin: 0 8rpx;
        }

        .balance-info {
          margin-top: 24rpx;
          padding: 16rpx;
          background: #f8f9fa;
          border-radius: 12rpx;

          .balance-label {
            font-size: 24rpx;
            color: #666;
          }

          .balance-value {
            font-size: 28rpx;
            font-weight: bold;
            color: #2ecc71;
            margin-left: 8rpx;

            &.insufficient {
              color: #e74c3c;
            }
          }
        }
      }

      .confirm-buttons {
        display: flex;
        border-top: 1rpx solid #eee;

        .confirm-btn {
          flex: 1;
          height: 88rpx;
          border: none;
          font-size: 28rpx;

          &.cancel-btn {
            background: #f5f5f5;
            color: #666;
            border-right: 1rpx solid #eee;
          }

          &.submit-btn {
            background: #667eea;
            color: white;

            &.disabled {
              background: #bdc3c7;
              color: #7f8c8d;
            }
          }
        }
      }
    }
  }

  // 全屏状态页面样式
  .fullscreen-status {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1000;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .status-background {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      overflow: hidden;

      .bg-circle {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.1);

        &.circle-1 {
          width: 300rpx;
          height: 300rpx;
          top: 10%;
          right: -100rpx;
          animation: float 6s ease-in-out infinite;
        }

        &.circle-2 {
          width: 200rpx;
          height: 200rpx;
          bottom: 20%;
          left: -50rpx;
          animation: float 8s ease-in-out infinite reverse;
        }

        &.circle-3 {
          width: 150rpx;
          height: 150rpx;
          top: 30%;
          left: 20%;
          animation: float 10s ease-in-out infinite;
        }
      }
    }

    .status-content {
      position: relative;
      z-index: 10;
      text-align: center;
      padding: 60rpx 40rpx;

      .status-icon-container {
        margin-bottom: 60rpx;

        .status-icon {
          width: 160rpx;
          height: 160rpx;
          border-radius: 50%;
          margin: 0 auto;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 255, 255, 0.2);
          backdrop-filter: blur(10rpx);
          border: 4rpx solid rgba(255, 255, 255, 0.3);

          .icon-text {
            font-size: 80rpx;
            color: white;
            font-weight: bold;

            &.success {
              animation: bounceIn 0.8s ease-out;
            }

            &.error {
              animation: bounceIn 0.8s ease-out;
            }
          }

          .loading-icon {
            width: 80rpx;
            height: 80rpx;

            .loading-spinner {
              width: 100%;
              height: 100%;
              border: 6rpx solid rgba(255, 255, 255, 0.3);
              border-top: 6rpx solid white;
              border-radius: 50%;
              animation: spin 1s linear infinite;
            }
          }

          &.success {
            background: rgba(82, 196, 26, 0.3);
            border-color: rgba(82, 196, 26, 0.5);
          }

          &.pending {
            background: rgba(250, 173, 20, 0.3);
            border-color: rgba(250, 173, 20, 0.5);
          }

          &.failed {
            background: rgba(255, 77, 79, 0.3);
            border-color: rgba(255, 77, 79, 0.5);
          }
        }
      }

      .status-info {
        margin-bottom: 80rpx;

        .status-title {
          font-size: 48rpx;
          font-weight: bold;
          color: white;
          margin-bottom: 24rpx;
          display: block;

          &.success {
            animation: fadeInUp 0.8s ease-out 0.3s both;
          }

          &.pending {
            animation: fadeInUp 0.8s ease-out 0.3s both;
          }

          &.failed {
            animation: fadeInUp 0.8s ease-out 0.3s both;
          }
        }

        .status-desc {
          font-size: 32rpx;
          color: rgba(255, 255, 255, 0.9);
          line-height: 1.5;
          animation: fadeInUp 0.8s ease-out 0.5s both;
        }
      }

      .status-actions {
        margin-bottom: 40rpx;

        .action-btn {
          width: 300rpx;
          height: 88rpx;
          border-radius: 44rpx;
          font-size: 32rpx;
          font-weight: bold;
          border: none;
          color: white;
          animation: fadeInUp 0.8s ease-out 0.7s both;

          &.success-btn {
            background: rgba(82, 196, 26, 0.8);
            backdrop-filter: blur(10rpx);
            border: 2rpx solid rgba(82, 196, 26, 0.5);
          }

          &.waiting-btn {
            background: rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(10rpx);
            border: 2rpx solid rgba(255, 255, 255, 0.3);
          }

          &.retry-btn {
            background: rgba(255, 77, 79, 0.8);
            backdrop-filter: blur(10rpx);
            border: 2rpx solid rgba(255, 77, 79, 0.5);
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }

      .status-extra {
        .extra-text {
          display: block;
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.7);
          line-height: 1.6;
          margin-bottom: 12rpx;
          animation: fadeInUp 0.8s ease-out 0.9s both;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

// 动画关键帧
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes bounceIn {
  0% {
    transform: scale(0.3);
    opacity: 0;
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
