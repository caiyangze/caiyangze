<template>
  <view class="custom-tab-bar">
    <view class="tab-bar-bg" :style="{ background: tabBarBg }"></view>
    <view class="tab-bar-content">
      <!-- 左侧两个导航项 -->
      <view
        v-for="(item, index) in leftItems"
        :key="index"
        class="tab-item"
        @tap="switchTab(item.pagePath)"
      >
        <view
          class="tab-icon"
          :class="[item.iconType, { active: active === index }]"
        ></view>
        <text
          class="tab-text"
          :class="{ active: active === index }"
        >{{ item.text }}</text>
      </view>

      <!-- 中间发布按钮 -->
      <view class="tab-item publish-item" @tap="goToPublish">
        <view class="publish-btn">
          <text class="publish-icon">+</text>
        </view>
        <text class="tab-text publish-text">发布</text>
      </view>

      <!-- 右侧两个导航项 -->
      <view
        v-for="(item, index) in rightItems"
        :key="index + 2"
        class="tab-item"
        @tap="switchTab(item.pagePath)"
      >
        <view
          class="tab-icon"
          :class="[item.iconType, { active: active === (index + 2) }]"
        ></view>
        <text
          class="tab-text"
          :class="{ active: active === (index + 2) }"
        >{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js';

// 当前激活的索引
const active = ref(0);

// 使用全局主题混入
const { currentBackground, currentThemeId } = useGlobalThemeMixin();

// 主题相关
const tabBarBg = ref('linear-gradient(45deg, #12C2E9, #C471ED, #F64F59)');

// 监听主题变化
watch(currentBackground, (newBg) => {
  console.log('底部导航栏主题变化:', newBg);
  tabBarBg.value = newBg;
}, { immediate: true });

// 完整的标签页配置
const list = [
  {
    pagePath: '/pages/index/index',
    text: '首页',
    iconType: 'home'
  },
  {
    pagePath: '/pages/square/square',
    text: '广场',
    iconType: 'square'
  },
  // 中间位置预留给发布按钮
  {
    pagePath: '/pages/message/message',
    text: '消息',
    iconType: 'message'
  },
  {
    pagePath: '/pages/mine/mine',
    text: '我的',
    iconType: 'mine'
  }
];

// 左侧导航项（首页、广场）
const leftItems = computed(() => list.slice(0, 2));

// 右侧导航项（消息、我的）
const rightItems = computed(() => list.slice(2, 4));

// 检测当前页面并设置激活状态
function setActiveTab() {
  try {
    const pages = getCurrentPages();
    if (pages && pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      if (currentPage && currentPage.route) {
        const currentRoute = '/' + currentPage.route;

        const index = list.findIndex(item => item.pagePath === currentRoute);
        console.log('当前路由:', currentRoute, '找到索引:', index);
        if (index !== -1) {
          active.value = index;
          console.log('设置active为:', index);
        }
      }
    }
  } catch (error) {
    console.error('设置激活标签页失败:', error);
    // 默认设置为首页
    active.value = 0;
  }
}

// 切换标签页
function switchTab(url) {
  try {
    // 获取当前页面路径
    const pages = getCurrentPages();
    if (pages && pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      if (currentPage && currentPage.route) {
        const currentRoute = '/' + currentPage.route;

        // 如果不是当前页面，则跳转
        if (currentRoute !== url) {
          uni.reLaunch({
            url: url
          });
        }
        return;
      }
    }

    // 如果无法获取当前页面信息，直接跳转
    uni.reLaunch({
      url: url
    });
  } catch (error) {
    console.error('切换标签页失败:', error);
    // 发生错误时仍然尝试跳转
    uni.reLaunch({
      url: url
    });
  }
}

// 跳转到发布动态页面
function goToPublish() {
  try {
    // 检查登录状态
    const token = uni.getStorageSync('token');
    if (!token) {
      uni.showModal({
        title: '提示',
        content: '请先登录后再发布动态',
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

    // 跳转到发布动态页面
    uni.navigateTo({
      url: '/pages/moment/publish'
    });
  } catch (error) {
    console.error('跳转发布页面失败:', error);
    uni.showToast({
      title: '跳转失败',
      icon: 'none'
    });
  }
}

// 初始化时设置当前激活的标签
onMounted(() => {
  setActiveTab();
});
</script>

<style lang="scss" scoped>
.custom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100rpx;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom); // 适配安全区域
  
  .tab-bar-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: calc(100% + env(safe-area-inset-bottom)); // 背景覆盖到安全区域
    background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
    background-size: 300% 300%;
    animation: gradientAnimation 15s ease infinite;
    opacity: 0.95;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
    
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
  
  .tab-bar-content {
    position: relative;
    z-index: 2;
    display: flex;
    height: 100%;
    align-items: flex-end;
    padding-bottom: 15rpx;

    .tab-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .tab-icon {
        width: 48rpx;
        height: 48rpx;
        margin-bottom: 4rpx;
        opacity: 0.7;
        position: relative;

        &.active {
          opacity: 1;
        }

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

          // 添加门的装饰
          &:hover::before {
            box-shadow: inset 0 -8rpx 0 rgba(255, 255, 255, 0.3),
                        inset 8rpx 0 0 rgba(255, 255, 255, 0.1);
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

        // 消息图标 - 现代对话框
        &.message {
          &::before {
            content: '';
            position: absolute;
            top: 32%;
            left: 50%;
            transform: translateX(-50%);
            width: 30rpx;
            height: 22rpx;
            background: #fff;
            border-radius: 12rpx;
            box-shadow: inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
                        0 2rpx 8rpx rgba(0, 0, 0, 0.1);
          }

          &::after {
            content: '';
            position: absolute;
            bottom: 22%;
            left: 58%;
            width: 0;
            height: 0;
            border-left: 8rpx solid transparent;
            border-right: 4rpx solid transparent;
            border-top: 10rpx solid #fff;
            filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
          }

          // 添加消息点装饰
          &:hover::before {
            box-shadow: inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
                        inset 6rpx 0 0 rgba(255, 255, 255, 0.1),
                        inset 12rpx 0 0 rgba(255, 255, 255, 0.1),
                        0 2rpx 8rpx rgba(0, 0, 0, 0.1);
          }
        }

        // 我的图标 - 精致人像
        &.mine {
          &::before {
            content: '';
            position: absolute;
            top: 28%;
            left: 50%;
            transform: translateX(-50%);
            width: 18rpx;
            height: 18rpx;
            background: #fff;
            border-radius: 50%;
            box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1),
                        inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
          }

          &::after {
            content: '';
            position: absolute;
            bottom: 18%;
            left: 50%;
            transform: translateX(-50%);
            width: 32rpx;
            height: 20rpx;
            background: #fff;
            border-radius: 16rpx 16rpx 0 0;
            box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1),
                        inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
          }

          // 添加肩膀装饰
          &:hover::after {
            box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1),
                        inset 0 2rpx 0 rgba(255, 255, 255, 0.3),
                        inset 8rpx 0 0 rgba(255, 255, 255, 0.1),
                        inset -8rpx 0 0 rgba(255, 255, 255, 0.1);
          }
        }
      }

      .tab-text {
        font-size: 20rpx;
        color: #fff;
        opacity: 0.7;

        &.active {
          opacity: 1;
          font-weight: bold;
        }
      }

      // 发布按钮特殊样式
      &.publish-item {
        position: relative;

        .publish-btn {
          width: 100rpx;
          height: 100rpx;
          background: linear-gradient(45deg, #FF6B6B, #4ECDC4, #45B7D1, #F093FB, #F5576C);
          background-size: 400% 400%;
          animation: publishGradient 4s ease infinite;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 8rpx;
          box-shadow:
            0 8rpx 25rpx rgba(0, 0, 0, 0.3),
            0 4rpx 15rpx rgba(255, 107, 107, 0.4),
            inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
          border: 3rpx solid rgba(255, 255, 255, 0.4);
          backdrop-filter: blur(15rpx);
          transform: translateY(-10rpx);
          position: relative;
          overflow: hidden;

          // 添加内部光晕效果
          &::before {
            content: '';
            position: absolute;
            top: 10%;
            left: 10%;
            width: 80%;
            height: 80%;
            background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
            border-radius: 50%;
            animation: innerGlow 2s ease-in-out infinite alternate;
          }

          .publish-icon {
            font-size: 48rpx;
            color: #fff;
            font-weight: bold;
            text-shadow:
              0 2rpx 4rpx rgba(0, 0, 0, 0.4),
              0 0 10rpx rgba(255, 255, 255, 0.3);
            z-index: 2;
            position: relative;
          }

          &:active {
            transform: translateY(-8rpx) scale(0.95);
            box-shadow:
              0 6rpx 20rpx rgba(0, 0, 0, 0.4),
              0 2rpx 10rpx rgba(255, 107, 107, 0.5),
              inset 0 2rpx 0 rgba(255, 255, 255, 0.4);
          }

          // 悬停效果
          &:hover {
            transform: translateY(-12rpx) scale(1.05);
            box-shadow:
              0 12rpx 30rpx rgba(0, 0, 0, 0.4),
              0 6rpx 20rpx rgba(255, 107, 107, 0.5),
              inset 0 2rpx 0 rgba(255, 255, 255, 0.4);
          }
        }

        .publish-text {
          font-size: 20rpx;
          color: #fff;
          opacity: 0.9;
          font-weight: bold;
        }
      }
    }
  }

  // 发布按钮渐变动画
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

  // 内部光晕动画
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

  // 广场图标脉冲动画
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

  // 图标悬停效果
  .tab-item:hover .tab-icon {
    transform: translateY(-2rpx);
    transition: all 0.3s ease;

    &.home::after {
      filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.3));
    }

    &.square::before {
      filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.2));
    }

    &.message::before {
      transform: translateX(-50%) scale(1.05);
    }

    &.mine::before {
      transform: translateX(-50%) scale(1.1);
    }
  }
}
</style>