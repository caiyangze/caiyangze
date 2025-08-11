<template>
  <view class="test-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <view class="test-content">
        <text class="title">底部导航栏测试页面</text>
        <text class="desc">测试新的发布按钮是否正常工作</text>
        
        <view class="test-buttons">
          <button @tap="testPublish" class="test-btn">测试发布功能</button>
          <button @tap="checkLogin" class="test-btn">检查登录状态</button>
        </view>
        
        <view class="info-section">
          <text class="info-title">修改说明：</text>
          <text class="info-item">✅ 发布按钮已移至底部导航栏中间</text>
          <text class="info-item">✅ 圆形渐变设计，与主页风格一致</text>
          <text class="info-item">✅ 发布按钮位置调整，更靠下显示</text>
          <text class="info-item">✅ 点击动画效果</text>
          <text class="info-item">✅ 登录状态检查</text>
          <text class="info-item">🎨 纯色CSS图标替代Emoji</text>
          <text class="info-item">💰 广场和我的页面已有金币图标</text>
        </view>

        <view class="icon-preview">
          <text class="preview-title">图标预览：</text>
          <view class="icon-list">
            <view class="icon-demo">
              <view class="tab-icon home"></view>
              <text>首页</text>
            </view>
            <view class="icon-demo">
              <view class="tab-icon square"></view>
              <text>广场</text>
            </view>
            <view class="icon-demo">
              <view class="publish-btn-demo">
                <view class="inner-glow"></view>
                <text class="plus-icon">+</text>
              </view>
              <text>发布</text>
            </view>
            <view class="icon-demo">
              <view class="tab-icon message"></view>
              <text>消息</text>
            </view>
            <view class="icon-demo">
              <view class="tab-icon mine"></view>
              <text>我的</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 自定义TabBar -->
    <custom-tab-bar />
  </view>
</template>

<script setup>
import customTabBar from '@/components/custom-tab-bar.vue';

// 测试发布功能
function testPublish() {
  uni.showToast({
    title: '请点击底部发布按钮',
    icon: 'none',
    duration: 2000
  });
}

// 检查登录状态
function checkLogin() {
  const token = uni.getStorageSync('token');
  if (token) {
    uni.showToast({
      title: '已登录',
      icon: 'success'
    });
  } else {
    uni.showToast({
      title: '未登录',
      icon: 'none'
    });
  }
}
</script>

<style lang="scss" scoped>
.test-page {
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
  
  .content-container {
    flex: 1;
    padding: 40rpx 30rpx 110rpx 30rpx;
    z-index: 1;
    
    .test-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .title {
        font-size: 48rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 20rpx;
        text-align: center;
      }
      
      .desc {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.8);
        margin-bottom: 60rpx;
        text-align: center;
      }
      
      .test-buttons {
        display: flex;
        flex-direction: column;
        gap: 20rpx;
        margin-bottom: 60rpx;
        
        .test-btn {
          background: rgba(255, 255, 255, 0.2);
          color: #fff;
          border: 2rpx solid rgba(255, 255, 255, 0.3);
          border-radius: 50rpx;
          padding: 20rpx 40rpx;
          font-size: 28rpx;
          backdrop-filter: blur(10rpx);
          
          &:active {
            background: rgba(255, 255, 255, 0.3);
          }
        }
      }
      
      .info-section {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20rpx;
        padding: 30rpx;
        backdrop-filter: blur(10rpx);
        border: 1rpx solid rgba(255, 255, 255, 0.2);
        
        .info-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #fff;
          display: block;
          margin-bottom: 20rpx;
        }
        
        .info-item {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.9);
          display: block;
          margin-bottom: 10rpx;
          line-height: 1.5;
        }
      }

      .icon-preview {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20rpx;
        padding: 30rpx;
        backdrop-filter: blur(10rpx);
        border: 1rpx solid rgba(255, 255, 255, 0.2);
        margin-top: 30rpx;

        .preview-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #fff;
          display: block;
          margin-bottom: 20rpx;
        }

        .icon-list {
          display: flex;
          justify-content: space-around;
          align-items: center;

          .icon-demo {
            display: flex;
            flex-direction: column;
            align-items: center;

            text {
              font-size: 20rpx;
              color: rgba(255, 255, 255, 0.8);
              margin-top: 8rpx;
            }

            .publish-btn-demo {
              width: 60rpx;
              height: 60rpx;
              background: linear-gradient(45deg, #FF6B6B, #4ECDC4, #45B7D1, #F093FB, #F5576C);
              background-size: 400% 400%;
              animation: publishGradient 4s ease infinite;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              position: relative;
              overflow: hidden;
              box-shadow:
                0 8rpx 25rpx rgba(0, 0, 0, 0.3),
                0 4rpx 15rpx rgba(255, 107, 107, 0.4),
                inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
              border: 3rpx solid rgba(255, 255, 255, 0.4);

              .inner-glow {
                position: absolute;
                top: 10%;
                left: 10%;
                width: 80%;
                height: 80%;
                background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
                border-radius: 50%;
                animation: innerGlow 2s ease-in-out infinite alternate;
              }

              .plus-icon {
                color: #fff;
                font-size: 32rpx;
                font-weight: bold;
                text-shadow:
                  0 2rpx 4rpx rgba(0, 0, 0, 0.4),
                  0 0 10rpx rgba(255, 255, 255, 0.3);
                z-index: 2;
                position: relative;
              }
            }
          }
        }
      }
    }
  }

  // 复制导航栏图标样式用于预览
  .tab-icon {
    width: 48rpx;
    height: 48rpx;
    opacity: 1;
    position: relative;

    // 首页图标 - 现代化房子
    &.home {
      &::before {
        content: '';
        position: absolute;
        top: 55%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 28rpx;
        height: 24rpx;
        background: #fff;
        border-radius: 0 0 6rpx 6rpx;
        box-shadow: inset 0 -8rpx 0 rgba(255, 255, 255, 0.3);
      }

      &::after {
        content: '';
        position: absolute;
        top: 25%;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 0;
        border-left: 18rpx solid transparent;
        border-right: 18rpx solid transparent;
        border-bottom: 18rpx solid #fff;
        filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.2));
      }
    }

    // 广场图标 - 精美网格
    &.square {
      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 32rpx;
        height: 32rpx;
        background:
          radial-gradient(circle at 6rpx 6rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 16rpx 6rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 26rpx 6rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 6rpx 16rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 16rpx 16rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 26rpx 16rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 6rpx 26rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 16rpx 26rpx, #fff 2rpx, transparent 2rpx),
          radial-gradient(circle at 26rpx 26rpx, #fff 2rpx, transparent 2rpx);
        background-size: 32rpx 32rpx;
        border-radius: 6rpx;
        filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
      }

      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 36rpx;
        height: 36rpx;
        border: 2rpx solid rgba(255, 255, 255, 0.3);
        border-radius: 8rpx;
        animation: squarePulse 2s ease-in-out infinite;
      }
    }

    // 消息图标 - 对话框
    &.message {
      &::before {
        content: '';
        position: absolute;
        top: 30%;
        left: 50%;
        transform: translateX(-50%);
        width: 28rpx;
        height: 20rpx;
        border: 3rpx solid #fff;
        border-radius: 8rpx;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 25%;
        left: 60%;
        width: 0;
        height: 0;
        border-left: 6rpx solid transparent;
        border-right: 6rpx solid transparent;
        border-top: 8rpx solid #fff;
      }
    }

    // 我的图标 - 人像
    &.mine {
      &::before {
        content: '';
        position: absolute;
        top: 25%;
        left: 50%;
        transform: translateX(-50%);
        width: 16rpx;
        height: 16rpx;
        border: 3rpx solid #fff;
        border-radius: 50%;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 20%;
        left: 50%;
        transform: translateX(-50%);
        width: 28rpx;
        height: 18rpx;
        border: 3rpx solid #fff;
        border-top: none;
        border-radius: 0 0 14rpx 14rpx;
      }
    }
  }

  // 动画关键帧
  @keyframes publishGradient {
    0% {
      background-position: 0% 50%;
    }
    25% {
      background-position: 100% 0%;
    }
    50% {
      background-position: 100% 100%;
    }
    75% {
      background-position: 0% 100%;
    }
    100% {
      background-position: 0% 50%;
    }
  }

  @keyframes innerGlow {
    0% {
      opacity: 0.3;
      transform: scale(0.8);
    }
    100% {
      opacity: 0.6;
      transform: scale(1.2);
    }
  }

  @keyframes squarePulse {
    0%, 100% {
      opacity: 0.3;
      transform: translate(-50%, -50%) scale(1);
    }
    50% {
      opacity: 0.6;
      transform: translate(-50%, -50%) scale(1.1);
    }
  }
}
</style>
