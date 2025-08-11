<template>
  <view class="verify-page">
    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <text class="nav-title">实名认证</text>
      <view class="nav-right"></view>
    </view>
    
    <!-- 全屏认证状态显示 -->
    <view class="fullscreen-status" v-if="verificationStatus && (verificationStatus.status === 0 || verificationStatus.status === 1)">
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
          <view class="status-icon" :class="getStatusClass()">
            <text v-if="verificationStatus.status === 1" class="icon-text success">✓</text>
            <view v-else class="loading-icon">
              <view class="loading-spinner"></view>
            </view>
          </view>
        </view>

        <!-- 状态信息 -->
        <view class="status-info">
          <text class="status-title" :class="getStatusClass()">
            {{ getStatusTitle() }}
          </text>
          <text class="status-desc">
            {{ getStatusDesc() }}
          </text>
        </view>

        <!-- 操作按钮 -->
        <view class="status-actions">
          <button v-if="verificationStatus.status === 1" class="action-btn success-btn" @click="goBack">
            完成
          </button>
          <button v-else class="action-btn waiting-btn" @click="goBack">
            返回
          </button>
        </view>

        <!-- 额外信息 -->
        <view class="status-extra" v-if="verificationStatus.status === 0">
          <text class="extra-text">预计审核时间：1-3个工作日</text>
          <text class="extra-text">我们会通过站内消息通知您审核结果</text>
        </view>

        <view class="status-extra" v-else-if="verificationStatus.status === 1">
          <text class="extra-text">恭喜您！现在可以享受更多平台功能</text>
        </view>
      </view>
    </view>

    <!-- 简单状态卡片（失败状态） -->
    <view class="status-card" v-if="verificationStatus && verificationStatus.status === 2">
      <view class="status-icon failed">
        <text>✗</text>
      </view>
      <view class="status-info">
        <text class="status-title">认证失败</text>
        <text class="status-desc">
          {{ verificationStatus.rejectReason || '请重新提交认证资料' }}
        </text>
      </view>
    </view>
    
    <!-- 认证表单 -->
    <view class="form-container" v-if="!verificationStatus || verificationStatus.status === 2">
      <!-- 认证说明 -->
      <view class="info-banner">
        <text class="info-text">📋 请准备以下材料完成实名认证：</text>
        <text class="info-item">• 本人身份证正反面照片</text>
        <text class="info-item">• 本人清晰正面免冠照片</text>
        <text class="info-note">认证资料仅用于身份验证，我们将严格保护您的隐私</text>
      </view>

      <view class="form-section">
        <text class="section-title">基本信息</text>
        
        <view class="form-item">
          <text class="form-label">真实姓名 *</text>
          <input 
            class="form-input" 
            v-model="formData.realName" 
            placeholder="请输入真实姓名"
            maxlength="20"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">身份证号 *</text>
          <input
            class="form-input"
            v-model="formData.idCardNo"
            placeholder="请输入身份证号"
            maxlength="18"
            :disabled="preVerifyPassed"
          />
        </view>

        <!-- 身份证预验证按钮 -->
        <view class="pre-verify-container" v-if="showPreVerifyBtn">
          <button
            class="pre-verify-btn"
            :class="{ disabled: !canPreVerify || isPreVerifying }"
            :disabled="!canPreVerify || isPreVerifying"
            @click="preVerifyIdCard"
          >
            <text class="pre-verify-text">
              {{ isPreVerifying ? '验证中...' : preVerifyPassed ? '✓ 验证通过' : '验证身份证信息' }}
            </text>
          </button>
          <text class="pre-verify-tip" v-if="!preVerifyPassed">
            请先验证身份证信息，验证通过后才能继续上传照片
          </text>
          <text class="pre-verify-success" v-if="preVerifyPassed">
            ✓ 身份证信息验证成功，请继续上传证件照片
          </text>
        </view>
      </view>
      
      <!-- 身份证照片 -->
      <view class="form-section" v-if="preVerifyPassed">
        <text class="section-title">身份证照片</text>

        <view class="upload-section">
          <view class="upload-item">
            <text class="upload-label">身份证正面 *</text>
            <view class="upload-box" @click="uploadIdCardFront">
              <image v-if="formData.idCardFront" :src="formData.idCardFront" class="upload-image" />
              <view v-else class="upload-placeholder">
                <text class="upload-icon">📷</text>
                <text class="upload-text">点击上传</text>
              </view>
            </view>
          </view>

          <view class="upload-item">
            <text class="upload-label">身份证背面 *</text>
            <view class="upload-box" @click="uploadIdCardBack">
              <image v-if="formData.idCardBack" :src="formData.idCardBack" class="upload-image" />
              <view v-else class="upload-placeholder">
                <text class="upload-icon">📷</text>
                <text class="upload-text">点击上传</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 人脸照片 -->
      <view class="form-section" v-if="preVerifyPassed">
        <text class="section-title">人脸照片</text>
        <text class="section-desc">请上传清晰的正面免冠照片，用于人脸识别验证</text>

        <view class="face-upload-container">
          <view class="face-upload-box" @click="uploadFacePhoto">
            <image v-if="formData.facePhoto" :src="formData.facePhoto" class="face-upload-image" />
            <view v-else class="face-upload-placeholder">
              <text class="face-upload-icon">🤳</text>
              <text class="face-upload-text">点击上传人脸照片</text>
              <text class="face-upload-hint">建议使用前置摄像头拍摄</text>
            </view>
          </view>


        </view>
      </view>
      
      <!-- 提交按钮 -->
      <view class="submit-container">
        <button 
          class="submit-btn" 
          :class="{ disabled: !canSubmit || isSubmitting }"
          :disabled="!canSubmit || isSubmitting"
          @click="submitVerification"
        >
          <text class="submit-text">
            {{ isSubmitting ? '提交中...' : '提交认证' }}
          </text>
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import http from '@/api/http'
import { uploadIdCardFront, uploadIdCardBack, uploadFacePhoto } from '@/api/user/auth.js'

export default {
  data() {
    return {
      formData: {
        realName: '',
        idCardNo: '',
        idCardFront: '',
        idCardBack: '',
        facePhoto: ''
      },
      verificationStatus: null,
      isSubmitting: false,
      isPreVerifying: false,
      preVerifyPassed: false,
      showPreVerifyBtn: false
    }
  },
  
  computed: {
    canSubmit() {
      const { realName, idCardNo, idCardFront, idCardBack, facePhoto } = this.formData
      return realName && idCardNo && idCardFront && idCardBack && facePhoto && this.preVerifyPassed
    },

    canPreVerify() {
      return this.formData.realName && this.formData.idCardNo && !this.preVerifyPassed
    }
  },

  watch: {
    'formData.realName'() {
      this.checkShowPreVerifyBtn()
    },
    'formData.idCardNo'() {
      this.checkShowPreVerifyBtn()
    }
  },
  
  methods: {
    goBack() {
      uni.navigateBack()
    },

    // 检查是否显示预验证按钮
    checkShowPreVerifyBtn() {
      this.showPreVerifyBtn = this.formData.realName && this.formData.idCardNo && !this.preVerifyPassed
    },

    // 身份证预验证
    async preVerifyIdCard() {
      if (!this.canPreVerify) {
        return
      }

      this.isPreVerifying = true

      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }

        const response = await http.post('/user/verification/preVerify', {
          realName: this.formData.realName,
          idCardNo: this.formData.idCardNo
        }, {
          headers: { 'token': token }
        })

        if (response.code === 200) {
          this.preVerifyPassed = true
          this.showPreVerifyBtn = false
          uni.showToast({
            title: '身份证验证成功',
            icon: 'success'
          })
        } else {
          uni.showModal({
            title: '身份证验证失败',
            content: response.message || '请检查姓名和身份证号是否正确',
            confirmText: '确定',
            showCancel: false
          })
        }
      } catch (error) {
        console.error('身份证预验证失败:', error)
        uni.showToast({
          title: '验证失败，请重试',
          icon: 'none'
        })
      } finally {
        this.isPreVerifying = false
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
            success: async (res) => {
              const filePath = res.tempFilePaths[0]

              // 显示上传中提示
              uni.showLoading({
                title: '上传中...'
              })

              try {
                // 上传到MinIO
                const uploadResult = await uploadIdCardFront(filePath)

                if (uploadResult.code === 200) {
                  this.formData.idCardFront = uploadResult.data
                  uni.showToast({
                    title: '上传成功',
                    icon: 'success'
                  })
                } else {
                  throw new Error(uploadResult.message || '上传失败')
                }
              } catch (error) {
                console.error('上传身份证正面失败:', error)
                uni.showToast({
                  title: '上传失败，请重试',
                  icon: 'none'
                })
              } finally {
                uni.hideLoading()
              }
            },
            fail: (err) => {
              console.error('选择图片失败:', err)
              uni.showToast({
                title: '选择图片失败，请重试',
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
            success: async (res) => {
              const filePath = res.tempFilePaths[0]

              // 显示上传中提示
              uni.showLoading({
                title: '上传中...'
              })

              try {
                // 上传到MinIO
                const uploadResult = await uploadIdCardBack(filePath)

                if (uploadResult.code === 200) {
                  this.formData.idCardBack = uploadResult.data
                  uni.showToast({
                    title: '上传成功',
                    icon: 'success'
                  })
                } else {
                  throw new Error(uploadResult.message || '上传失败')
                }
              } catch (error) {
                console.error('上传身份证背面失败:', error)
                uni.showToast({
                  title: '上传失败，请重试',
                  icon: 'none'
                })
              } finally {
                uni.hideLoading()
              }
            },
            fail: (err) => {
              console.error('选择图片失败:', err)
              uni.showToast({
                title: '选择图片失败，请重试',
                icon: 'none'
              })
            }
          })
        }
      })
    },

    // 上传人脸照片
    uploadFacePhoto() {
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
          if (res.tapIndex === 0) {
            // 拍照
            this.takePhoto()
          } else if (res.tapIndex === 1) {
            // 从相册选择
            this.chooseFromAlbum()
          }
        }
      })
    },

    // 上传人脸照片到MinIO
    async uploadFacePhotoToMinio(filePath) {
      uni.showLoading({
        title: '上传中...'
      })

      try {
        const uploadResult = await uploadFacePhoto(filePath)

        if (uploadResult.code === 200) {
          this.formData.facePhoto = uploadResult.data
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          throw new Error(uploadResult.message || '上传失败')
        }
      } catch (error) {
        console.error('上传人脸照片失败:', error)
        uni.showToast({
          title: '上传失败，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 拍照
    takePhoto() {
      // #ifdef APP-PLUS
      // App端使用plus.camera
      plus.camera.getCamera(1, (camera) => { // 1表示前置摄像头
        camera.captureImage(async (path) => {
          await this.uploadFacePhotoToMinio(path)
        }, (error) => {
          console.error('拍照失败:', error)
          uni.showToast({
            title: '拍照失败',
            icon: 'none'
          })
        }, {
          filename: '_doc/camera/',
          index: 1 // 前置摄像头
        })
      }, (error) => {
        console.error('获取摄像头失败:', error)
        // 如果获取前置摄像头失败，尝试后置摄像头
        this.fallbackTakePhoto()
      })
      // #endif

      // #ifdef H5 || MP-WEIXIN || MP-ALIPAY
      // H5和小程序端使用uni.chooseImage
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['camera'],
        success: async (res) => {
          const filePath = res.tempFilePaths[0]
          await this.uploadFacePhotoToMinio(filePath)
        },
        fail: (err) => {
          console.error('拍照失败:', err)
          uni.showToast({
            title: '拍照失败，请重试',
            icon: 'none'
          })
        }
      })
      // #endif
    },

    // 备用拍照方法（后置摄像头）
    fallbackTakePhoto() {
      // #ifdef APP-PLUS
      plus.camera.getCamera(0, (camera) => { // 0表示后置摄像头
        camera.captureImage(async (path) => {
          await this.uploadFacePhotoToMinio(path)
        }, (error) => {
          console.error('拍照失败:', error)
          uni.showToast({
            title: '拍照失败',
            icon: 'none'
          })
        }, {
          filename: '_doc/camera/'
        })
      }, (error) => {
        console.error('获取后置摄像头也失败:', error)
        // 最后的备用方案
        this.chooseFromAlbum()
      })
      // #endif
    },

    // 从相册选择
    chooseFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: async (res) => {
          const filePath = res.tempFilePaths[0]
          await this.uploadFacePhotoToMinio(filePath)
        },
        fail: (err) => {
          console.error('选择照片失败:', err)
          uni.showToast({
            title: '选择照片失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 提交认证
    async submitVerification() {
      if (!this.canSubmit) {
        return
      }
      
      this.isSubmitting = true
      
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }
        
        const response = await http.post('/user/verification/submit', this.formData, {
          headers: { 'token': token }
        })
        
        if (response.code === 200) {
          uni.showModal({
            title: '提交成功',
            content: '您的实名认证资料已提交，请等待审核。',
            confirmText: '确定',
            showCancel: false,
            success: () => {
              this.loadVerificationStatus()
            }
          })
        } else {
          uni.showToast({
            title: response.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('提交认证失败:', error)
        uni.showToast({
          title: '提交失败，请重试',
          icon: 'none'
        })
      } finally {
        this.isSubmitting = false
      }
    },
    
    // 加载认证状态
    async loadVerificationStatus() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          return
        }
        
        const response = await http.get('/user/verification/status', {
          headers: { 'token': token }
        })
        
        if (response.code === 200) {
          this.verificationStatus = response.data
        }
      } catch (error) {
        console.error('获取认证状态失败:', error)
      }
    },

    // 获取状态样式类
    getStatusClass() {
      if (!this.verificationStatus) return ''
      switch (this.verificationStatus.status) {
        case 1: return 'success'
        case 2: return 'failed'
        case 0: return 'pending'
        default: return ''
      }
    },

    // 获取状态标题
    getStatusTitle() {
      if (!this.verificationStatus) return ''
      switch (this.verificationStatus.status) {
        case 1: return '认证成功'
        case 2: return '认证失败'
        case 0: return '审核中'
        default: return ''
      }
    },

    // 获取状态描述
    getStatusDesc() {
      if (!this.verificationStatus) return ''
      switch (this.verificationStatus.status) {
        case 1: return '您已通过实名认证'
        case 2: return this.verificationStatus.rejectReason || '请重新提交认证资料'
        case 0: return '您的认证资料正在审核中，请耐心等待'
        default: return ''
      }
    },

    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 重新认证
    retryVerification() {
      this.verificationStatus = null
    }
  },
  
  onLoad() {
    this.loadVerificationStatus()
  }
}
</script>

<style lang="scss" scoped>
.verify-page {
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
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .nav-icon {
        font-size: 36rpx;
        color: #333;
      }
    }
    
    .nav-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .nav-right {
      width: 60rpx;
    }
  }
  
  .status-card {
    margin: 20rpx 32rpx;
    background: white;
    border-radius: 20rpx;
    padding: 32rpx;
    display: flex;
    align-items: center;
    width: calc(100% - 64rpx);
    box-sizing: border-box;
    
    .status-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40rpx;
      color: white;
      margin-right: 24rpx;
      
      &.success {
        background: #52c41a;
      }
      
      &.failed {
        background: #ff4d4f;
      }
      
      &.pending {
        background: #faad14;
      }
    }
    
    .status-info {
      flex: 1;
      
      .status-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        display: block;
        margin-bottom: 8rpx;
      }
      
      .status-desc {
        font-size: 28rpx;
        color: #666;
        display: block;
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
  
  .form-container {
    .info-banner {
      margin: 20rpx 32rpx;
      background: linear-gradient(135deg, #e3f2fd, #f3e5f5);
      border-radius: 16rpx;
      padding: 24rpx;
      border-left: 6rpx solid #2196f3;
      width: calc(100% - 64rpx);
      box-sizing: border-box;

      .info-text {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
        display: block;
        margin-bottom: 16rpx;
      }

      .info-item {
        font-size: 26rpx;
        color: #555;
        display: block;
        margin-bottom: 8rpx;
        padding-left: 16rpx;
      }

      .info-note {
        font-size: 22rpx;
        color: #666;
        display: block;
        margin-top: 12rpx;
        font-style: italic;
        text-align: center;
        padding: 12rpx;
        background: rgba(255, 255, 255, 0.6);
        border-radius: 8rpx;
      }
    }

    .form-section {
      margin: 20rpx 32rpx;
      background: white;
      border-radius: 20rpx;
      padding: 32rpx;
      width: calc(100% - 64rpx);
      box-sizing: border-box;
      
      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 32rpx;
        display: block;
      }

      .section-desc {
        font-size: 24rpx;
        color: #666;
        margin-bottom: 24rpx;
        display: block;
        line-height: 1.5;
      }
      
      .form-item {
        margin-bottom: 32rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .form-label {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 16rpx;
          display: block;
        }
        
        .form-input {
          width: 100%;
          height: 88rpx;
          background: #f8f9fa;
          border-radius: 12rpx;
          padding: 0 24rpx;
          font-size: 28rpx;
          color: #333;
          border: 2rpx solid transparent;

          &:focus {
            border-color: #667eea;
            background: white;
          }

          &:disabled {
            background: #e9ecef;
            color: #6c757d;
          }
        }
      }
    }

    // 身份证预验证样式
    .pre-verify-container {
      margin-top: 24rpx;
      padding: 24rpx;
      background: linear-gradient(135deg, #f8f9ff, #e8f4fd);
      border-radius: 16rpx;
      border: 1rpx solid #e3f2fd;

      .pre-verify-btn {
        width: 100%;
        height: 80rpx;
        background: linear-gradient(45deg, #667eea, #764ba2);
        border-radius: 40rpx;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;

        &.disabled {
          background: #ccc;
        }

        .pre-verify-text {
          font-size: 28rpx;
          color: white;
          font-weight: 500;
        }
      }

      .pre-verify-tip {
        font-size: 24rpx;
        color: #666;
        text-align: center;
        line-height: 1.4;
        display: block;
      }

      .pre-verify-success {
        font-size: 24rpx;
        color: #52c41a;
        text-align: center;
        line-height: 1.4;
        display: block;
        font-weight: 500;
      }
    }

    .upload-section {
      display: flex;
      gap: 24rpx;
      
      .upload-item {
        flex: 1;
        
        .upload-label {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 16rpx;
          display: block;
        }
        
        .upload-box {
          width: 100%;
          height: 200rpx;
          background: #f8f9fa;
          border-radius: 12rpx;
          border: 2rpx dashed #ddd;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;
          overflow: hidden;
          
          .upload-image {
            width: 100%;
            height: 100%;
          }
          
          .upload-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            
            .upload-icon {
              font-size: 48rpx;
              color: #ccc;
              margin-bottom: 8rpx;
            }
            
            .upload-text {
              font-size: 24rpx;
              color: #999;
            }
          }
        }
      }
    }

    // 人脸照片上传样式
    .face-upload-container {
      display: flex;
      flex-direction: column;
      align-items: center;

      .face-upload-box {
        width: 300rpx;
        height: 300rpx;
        background: #f8f9fa;
        border-radius: 50%;
        border: 2rpx dashed #ddd;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        overflow: hidden;

        .face-upload-image {
          width: 100%;
          height: 100%;
          border-radius: 50%;
        }

        .face-upload-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          text-align: center;

          .face-upload-icon {
            font-size: 60rpx;
            color: #ccc;
            margin-bottom: 12rpx;
          }

          .face-upload-text {
            font-size: 26rpx;
            color: #666;
            margin-bottom: 8rpx;
            font-weight: 500;
          }

          .face-upload-hint {
            font-size: 20rpx;
            color: #999;
            line-height: 1.3;
          }
        }

        &:active {
          transform: scale(0.98);
          transition: transform 0.1s ease;
        }
      }


    }
  }
  
  .submit-container {
    padding: 40rpx 32rpx;
    width: 100%;
    box-sizing: border-box;
    
    .submit-btn {
      width: 100%;
      height: 88rpx;
      background: linear-gradient(45deg, #667eea, #764ba2);
      border-radius: 44rpx;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.disabled {
        background: #ccc;
      }
      
      .submit-text {
        font-size: 32rpx;
        color: white;
        font-weight: bold;
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
