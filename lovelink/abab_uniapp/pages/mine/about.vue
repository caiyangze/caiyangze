<template>
  <view class="about-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
      <view class="overlay-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 顶部导航 -->
      <view class="navbar">
        <view class="back-btn" @tap="goBack">
          <text class="back-icon"></text>
        </view>
        <text class="page-title">关于我们</text>
        <view class="placeholder"></view>
      </view>
      
      <!-- Logo和版本信息 -->
      <view class="app-info">
        <image class="app-logo" src="/static/logo.png" mode="aspectFit"></image>
        <text class="app-name">心动交友</text>
        <text class="app-version">版本 1.0.0</text>
      </view>
      
      <!-- 关于我们内容 -->
      <view class="about-content">
        <view class="about-section">
          <text class="section-title">应用介绍</text>
          <text class="section-text">心动交友是一款专为单身人士打造的交友软件，致力于为用户提供安全、优质、高效的交友平台。通过智能匹配、趣味互动、线下活动等多种方式，帮助用户找到心仪的对象。</text>
        </view>
        
        <view class="about-section">
          <text class="section-title">联系我们</text>
          <text class="section-text">客服电话：400-123-4567</text>
          <text class="section-text">客服邮箱：support@xindong.com</text>
          <text class="section-text">官方网站：www.xindong.com</text>
        </view>
        
        <view class="about-section">
          <text class="section-title">商务合作</text>
          <text class="section-text">合作电话：020-87654321</text>
          <text class="section-text">合作邮箱：business@xindong.com</text>
        </view>
      </view>
      
      <!-- 退出登录按钮 -->
      <view class="logout-btn" @tap="logout">
        <text>退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue';

// 返回上一页
function goBack() {
  uni.navigateBack();
}

// 退出登录
function logout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        // 清除token
        uni.removeStorageSync('token');
        
        // 清除其他用户相关信息
        uni.removeStorageSync('userInfo');
        
        // 跳转到登录页面
        uni.reLaunch({
          url: '/pages/login/login'
        });
      }
    }
  });
}

onMounted(() => {
  console.log('About page mounted');
});
</script>

<style lang="scss" scoped>
.about-page {
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
    
    .overlay-gradient {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.1);
    }
  }
  
  // 内容容器
  .content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 40rpx 30rpx;
    z-index: 1;
    
    // 顶部导航
    .navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 40rpx;
      
      .back-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .back-icon {
          width: 36rpx;
          height: 36rpx;
          border-left: 4rpx solid #fff;
          border-bottom: 4rpx solid #fff;
          transform: rotate(45deg);
        }
      }
      
      .page-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .placeholder {
        width: 60rpx;
      }
    }
    
    // Logo和版本信息
    .app-info {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 60rpx;
      
      .app-logo {
        width: 160rpx;
        height: 160rpx;
        border-radius: 40rpx;
        margin-bottom: 20rpx;
        background: rgba(255, 255, 255, 0.2);
      }
      
      .app-name {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 10rpx;
      }
      
      .app-version {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }
    
    // 关于内容
    .about-content {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 40rpx;
      
      .about-section {
        margin-bottom: 30rpx;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .section-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #fff;
          margin-bottom: 16rpx;
          display: block;
        }
        
        .section-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.9);
          line-height: 1.6;
          display: block;
          margin-bottom: 8rpx;
        }
      }
    }
    
    // 退出登录按钮
    .logout-btn {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      padding: 30rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: auto;
      
      text {
        font-size: 32rpx;
        font-weight: bold;
        color: #FF4757;
      }
    }
  }
}
</style> 