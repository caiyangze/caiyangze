<template>
  <view class="publish-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 自定义导航栏 -->
      <view class="custom-navbar">
        <view class="navbar-left" @tap="goBack">
          <text class="back-icon">‹</text>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">发布动态</text>
        </view>
        <view class="navbar-right" @tap="publishMoment">
          <text class="publish-btn" :class="{ disabled: !canPublish }">发布</text>
        </view>
      </view>

      <!-- 主要内容区 -->
      <view class="main-content">
        <!-- 动态内容输入 -->
        <view class="content-section">
          <textarea 
            v-model="momentData.content" 
            placeholder="分享你的生活..." 
            class="content-input"
            maxlength="1000"
            :show-count="true"
            auto-height
          ></textarea>
        </view>

        <!-- 图片选择区域 -->
        <view class="media-section">
          <view class="media-grid">
            <view 
              v-for="(image, index) in selectedImages" 
              :key="index" 
              class="media-item"
            >
              <image :src="image.url" class="media-image" @tap="previewImage(index)"></image>
              <view class="delete-btn" @tap="removeImage(index)">×</view>
            </view>
            
            <view 
              v-if="selectedImages.length < 9" 
              class="add-media-btn" 
              @tap="chooseImage"
            >
              <text class="add-icon">+</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
        </view>

        <!-- 选项区域 -->
        <view class="options-section">
          <view class="option-item" @tap="chooseLocation">
            <view class="option-left">
              <text class="option-icon">📍</text>
              <text class="option-label">位置</text>
            </view>
            <view class="option-right">
              <text class="option-value">{{ momentData.location || '添加位置' }}</text>
              <text class="arrow-icon">›</text>
            </view>
          </view>
          
          <view class="option-item" @tap="chooseVisibility">
            <view class="option-left">
              <text class="option-icon">👁️</text>
              <text class="option-label">可见范围</text>
            </view>
            <view class="option-right">
              <text class="option-value">{{ visibilityText }}</text>
              <text class="arrow-icon">›</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载遮罩 -->
    <view v-if="publishing" class="loading-mask">
      <view class="loading-content">
        <view class="loading-spinner"></view>
        <text class="loading-text">发布中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import momentApi from '@/api/moment.js';

// 动态数据
const momentData = reactive({
  content: '',
  location: '',
  visibility: 1 // 1-公开，2-仅关注者，3-仅自己
});

// 选中的图片
const selectedImages = ref([]);

// 发布状态
const publishing = ref(false);

// 是否可以发布
const canPublish = computed(() => {
  return momentData.content.trim().length > 0 && !publishing.value;
});

// 可见性文本
const visibilityText = computed(() => {
  const texts = { 1: '公开', 2: '仅关注者可见', 3: '仅自己可见' };
  return texts[momentData.visibility];
});

// 选择图片
function chooseImage() {
  const remainCount = 9 - selectedImages.value.length;
  
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      res.tempFilePaths.forEach(path => {
        selectedImages.value.push({
          url: path,
          path: path
        });
      });
    },
    fail: (err) => {
      console.error('选择图片失败:', err);
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      });
    }
  });
}

// 移除图片
function removeImage(index) {
  selectedImages.value.splice(index, 1);
}

// 预览图片
function previewImage(index) {
  const urls = selectedImages.value.map(img => img.url);
  uni.previewImage({
    current: index,
    urls: urls
  });
}

// 选择位置
function chooseLocation() {
  // 简化位置选择，使用输入框代替地图选择
  uni.showModal({
    title: '添加位置',
    editable: true,
    placeholderText: '请输入位置信息',
    success: (res) => {
      if (res.confirm && res.content) {
        momentData.location = res.content;
      }
    }
  });
}

// 选择可见性
function chooseVisibility() {
  uni.showActionSheet({
    itemList: ['公开', '仅关注者可见', '仅自己可见'],
    success: (res) => {
      momentData.visibility = res.tapIndex + 1;
    }
  });
}

// 发布动态
async function publishMoment() {
  if (!canPublish.value) {
    uni.showToast({
      title: '请输入动态内容',
      icon: 'none'
    });
    return;
  }

  // 检查登录状态
  const token = uni.getStorageSync('token');
  if (!token) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({
            url: '/pages/login/login'
          });
        }
      }
    });
    return;
  }

  publishing.value = true;

  try {
    // 1. 先上传图片
    let imageUrls = [];
    if (selectedImages.value.length > 0) {
      uni.showLoading({
        title: '上传图片中...'
      });
      
      const uploadPromises = selectedImages.value.map(async (image) => {
        const result = await momentApi.uploadMomentImage(image.path);
        if (result.code === 200) {
          return result.data;
        } else {
          throw new Error(result.message || '图片上传失败');
        }
      });

      imageUrls = await Promise.all(uploadPromises);
      uni.hideLoading();
    }

    // 2. 发布动态
    uni.showLoading({
      title: '发布中...'
    });

    // 使用uni.request直接发送请求，避免FormData问题
    const token = uni.getStorageSync('token');
    const result = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:9002/api/social/moment/create',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'token': token
        },
        data: {
          content: momentData.content,
          location: momentData.location,
          visibility: momentData.visibility,
          imageUrls: imageUrls
        },
        success: (res) => {
          resolve(res.data);
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
    
    uni.hideLoading();

    if (result.code === 200) {
      uni.showToast({
        title: '发布成功',
        icon: 'success'
      });

      // 延迟返回，让用户看到成功提示
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } else {
      throw new Error(result.message || '发布失败');
    }

  } catch (error) {
    uni.hideLoading();
    console.error('发布失败:', error);
    uni.showToast({
      title: error.message || '发布失败，请重试',
      icon: 'none'
    });
  } finally {
    publishing.value = false;
  }
}

// 返回
function goBack() {
  if (momentData.content.trim() || selectedImages.value.length > 0) {
    uni.showModal({
      title: '提示',
      content: '确定要放弃编辑吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack();
        }
      }
    });
  } else {
    uni.navigateBack();
  }
}
</script>

<style lang="scss" scoped>
.publish-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  
  // 背景层
  .bg-layer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;
  }
  
  // 内容容器
  .content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    z-index: 1;
    
    // 自定义导航栏
    .custom-navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 88rpx;
      padding: 0 30rpx;
      padding-top: var(--status-bar-height);
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10rpx);
      
      .navbar-left {
        display: flex;
        align-items: center;
        
        .back-icon {
          font-size: 48rpx;
          color: #fff;
          font-weight: bold;
          margin-right: 8rpx;
        }
        
        .back-text {
          font-size: 32rpx;
          color: #fff;
        }
      }
      
      .navbar-center {
        .navbar-title {
          font-size: 36rpx;
          font-weight: bold;
          color: #fff;
        }
      }
      
      .navbar-right {
        .publish-btn {
          font-size: 32rpx;
          color: #fff;
          background: rgba(255, 255, 255, 0.2);
          padding: 12rpx 24rpx;
          border-radius: 30rpx;
          
          &.disabled {
            opacity: 0.5;
          }
        }
      }
    }
    
    // 主内容区
    .main-content {
      flex: 1;
      padding: 30rpx;
      
      // 内容输入区
      .content-section {
        margin-bottom: 40rpx;
        
        .content-input {
          width: 100%;
          min-height: 200rpx;
          font-size: 32rpx;
          line-height: 1.5;
          color: #fff;
          background: rgba(255, 255, 255, 0.1);
          border-radius: 20rpx;
          padding: 30rpx;
          border: none;
          outline: none;
          
          &::placeholder {
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }
      
      // 媒体区域
      .media-section {
        margin-bottom: 40rpx;
        
        .media-grid {
          display: flex;
          flex-wrap: wrap;
          gap: 20rpx;
        }
        
        .media-item {
          position: relative;
          width: 200rpx;
          height: 200rpx;
          
          .media-image {
            width: 100%;
            height: 100%;
            border-radius: 15rpx;
          }
          
          .delete-btn {
            position: absolute;
            top: -10rpx;
            right: -10rpx;
            width: 40rpx;
            height: 40rpx;
            background-color: rgba(255, 77, 87, 0.9);
            color: #fff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24rpx;
            font-weight: bold;
          }
        }
        
        .add-media-btn {
          width: 200rpx;
          height: 200rpx;
          border: 2rpx dashed rgba(255, 255, 255, 0.5);
          border-radius: 15rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          
          .add-icon {
            font-size: 60rpx;
            color: rgba(255, 255, 255, 0.6);
            margin-bottom: 10rpx;
          }
          
          .add-text {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }
      
      // 选项区域
      .options-section {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20rpx;
        overflow: hidden;
        
        .option-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 30rpx;
          border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
          
          &:last-child {
            border-bottom: none;
          }
          
          .option-left {
            display: flex;
            align-items: center;
            
            .option-icon {
              font-size: 32rpx;
              margin-right: 20rpx;
            }
            
            .option-label {
              font-size: 32rpx;
              color: #fff;
            }
          }
          
          .option-right {
            display: flex;
            align-items: center;
            
            .option-value {
              font-size: 28rpx;
              color: rgba(255, 255, 255, 0.8);
              margin-right: 10rpx;
            }
            
            .arrow-icon {
              font-size: 32rpx;
              color: rgba(255, 255, 255, 0.6);
              font-weight: bold;
            }
          }
        }
      }
    }
  }
  
  // 加载遮罩
  .loading-mask {
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
    
    .loading-content {
      background: rgba(255, 255, 255, 0.9);
      border-radius: 20rpx;
      padding: 40rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .loading-spinner {
        width: 60rpx;
        height: 60rpx;
        border: 4rpx solid #f3f3f3;
        border-top: 4rpx solid #C471ED;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 20rpx;
      }
      
      .loading-text {
        font-size: 28rpx;
        color: #333;
      }
    }
  }
}

// 背景渐变样式
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
  background-size: 300% 300%;
  animation: gradientAnimation 15s ease infinite;
  
  @keyframes gradientAnimation {
    0% {
      background-position: 0% 50%;
    }
    50% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0% 50%;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
