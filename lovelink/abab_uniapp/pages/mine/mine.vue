<template>
  <view class="mine-page" :style="{ background: currentBackground }">
    <!-- 背景层 -->
    <view
      class="bg-layer"
      :style="{
        background: currentBackground,
        position: 'fixed',
        top: '0',
        left: '0',
        width: '100%',
        height: '100%',
        zIndex: '-1'
      }"
    >
      <!-- 移除固定的bg-gradient，让主题背景生效 -->
      <view class="overlay-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 顶部区域 -->
      <view class="header">
        <view class="left-section">
          <text class="page-title">我的</text>
        </view>
        
        <!-- 虚拟货币 -->
        <view class="currency-section" @tap="goToWallet">
          <image class="currency-icon" src="/static/index/coin.png" mode="aspectFit" @error="handleImageError('coin')">
            <view v-if="imageError.coin" class="coin-placeholder"></view>
          </image>
          <text class="currency-amount">{{ userCurrency }}</text>
        </view>
      </view>
      
      <!-- 用户信息卡片 -->
      <view class="user-card">
        <view class="user-info">
          <image class="avatar" :src="userInfo.avatar" mode="aspectFill" @error="handleImageError('avatar')">
            <view v-if="imageError.avatar" class="avatar-placeholder">
              <text class="avatar-text">{{ userInfo.nickname.substring(0, 1) }}</text>
            </view>
          </image>
          <view class="user-detail">
            <view class="name-row">
              <text class="nickname">{{ userInfo.nickname }}</text>
              <view class="gender-tag" :class="userInfo.gender === 1 ? 'male' : 'female'">
                <text class="gender-icon">{{ userInfo.gender === 1 ? '♂' : '♀' }}</text>
              </view>
            </view>
            <view class="user-desc">
              <text class="user-id">ID: {{ userInfo.id }}</text>
              <text v-if="userInfo.isVerified === 1" class="user-verified verified">已实名</text>
              <text v-else class="user-verified unverified">未实名</text>
            </view>
          </view>
        </view>
        <view class="user-stats">
          <view class="stat-item" @tap="goFollowers">
            <text class="stat-num">{{ userInfo.followers }}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item" @tap="goFollowing">
            <text class="stat-num">{{ userInfo.following }}</text>
            <text class="stat-label">关注</text>
          </view>
          <view class="stat-item" @tap="goLikes">
            <text class="stat-num">{{ userInfo.likes }}</text>
            <text class="stat-label">获赞</text>
          </view>
        </view>
        <view class="edit-profile-btn" @tap="goToEditProfile">
          <text>编辑资料</text>
        </view>
      </view>
      
      <!-- 会员卡片 -->
      <view class="vip-card" @tap="goVipCenter">
        <view class="vip-info">
          <text class="vip-title">{{ userInfo.isVip ? 'VIP会员' : '开通VIP' }}</text>
          <text class="vip-desc">{{ userInfo.isVip ? '会员有效期至 ' + userInfo.vipExpireDate : '专属特权 · 1v1精准配对' }}</text>
        </view>
        <view class="vip-button">
          {{ userInfo.isVip ? '续费' : '立即开通' }}
        </view>
      </view>
      
      <!-- 功能菜单 -->
      <view class="menu-section">
        <!-- 可滑动的菜单容器 -->
        <swiper class="menu-swiper" :indicator-dots="true" :indicator-color="'rgba(255,255,255,0.3)'" :indicator-active-color="'#fff'" :autoplay="false" :circular="false">
          <!-- 第一页菜单 -->
          <swiper-item class="menu-page">
            <view class="menu-row">
              <view class="menu-item" v-for="(item, index) in menuItems.slice(0, 4)" :key="index" @tap="handleMenuClick(item)">
                <view class="menu-icon" :class="item.icon"></view>
                <text class="menu-name">{{ item.name }}</text>
              </view>
            </view>
            <view class="menu-row">
              <view class="menu-item" v-for="(item, index) in menuItems.slice(4, 8)" :key="index + 4" @tap="handleMenuClick(item)">
                <view class="menu-icon" :class="item.icon"></view>
                <text class="menu-name">{{ item.name }}</text>
              </view>
            </view>
          </swiper-item>

          <!-- 第二页菜单（如果有超过8个菜单项） -->
          <swiper-item class="menu-page" v-if="menuItems.length > 8">
            <view class="menu-row">
              <view class="menu-item" v-for="(item, index) in menuItems.slice(8, 12)" :key="index + 8" @tap="handleMenuClick(item)">
                <view class="menu-icon" :class="item.icon"></view>
                <text class="menu-name">{{ item.name }}</text>
              </view>
              <!-- 填充空白项，保持布局对齐 -->
              <view class="menu-item menu-placeholder" v-for="n in (4 - Math.min(4, menuItems.length - 8))" :key="'placeholder1-' + n" v-if="menuItems.slice(8, 12).length < 4">
              </view>
            </view>
            <view class="menu-row">
              <view class="menu-item" v-for="(item, index) in menuItems.slice(12, 16)" :key="index + 12" @tap="handleMenuClick(item)">
                <view class="menu-icon" :class="item.icon"></view>
                <text class="menu-name">{{ item.name }}</text>
              </view>
              <!-- 填充空白项，保持布局对齐 -->
              <view class="menu-item menu-placeholder" v-for="n in (4 - Math.min(4, Math.max(0, menuItems.length - 12)))" :key="'placeholder2-' + n" v-if="menuItems.slice(12, 16).length < 4">
              </view>
            </view>
          </swiper-item>
        </swiper>
      </view>
      
      <!-- 我的动态 -->
      <view class="section posts-section">
        <view class="section-header">
          <text class="section-title">我的动态</text>
          <text class="more-link" @tap="goAllPosts">查看全部</text>
        </view>
        <!-- 加载状态 -->
        <view class="loading-posts" v-if="postsLoading">
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 错误状态 -->
        <view class="error-posts" v-else-if="postsError">
          <text class="error-text">{{ postsError }}</text>
          <view class="retry-btn" @tap="getUserPosts">
            <text>重试</text>
          </view>
        </view>

        <!-- 动态列表 -->
        <view class="posts-list" v-else-if="userPosts.length > 0">
          <view class="post-item" v-for="(post, index) in userPosts" :key="index" @tap="viewPost(post)">
            <image class="post-image" :src="post.image" mode="aspectFill" @error="handleImageError(`post${index}`)">
              <view v-if="imageError[`post${index}`]" class="image-placeholder"></view>
            </image>
            <text class="post-title">{{ post.title }}</text>
            <view class="post-stats">
              <view class="stat">
                <text class="stat-icon like-icon"></text>
                <text class="stat-count">{{ post.likes }}</text>
              </view>
              <view class="stat">
                <text class="stat-icon comment-icon"></text>
                <text class="stat-count">{{ post.comments }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="empty-posts" v-else>
          <text class="empty-text">还没有发布动态，快去分享你的心情吧~</text>
          <view class="create-post-btn" @tap="createPost">
            <text>发布动态</text>
          </view>
        </view>
      </view>
      
      <!-- 设置区域 -->
      <view class="settings-section">
        <view class="setting-item" @tap="goSettings">
          <text class="setting-name">设置</text>
          <text class="setting-arrow"></text>
        </view>
        <view class="setting-item" @tap="goFeedback">
          <text class="setting-name">意见反馈</text>
          <text class="setting-arrow"></text>
        </view>
        <view class="setting-item" @tap="goAbout">
          <text class="setting-name">关于我们</text>
          <text class="setting-arrow"></text>
        </view>
        <view class="setting-item logout-item" @tap="logout">
          <text class="setting-name logout-text">退出登录</text>
          <text class="setting-arrow"></text>
        </view>
      </view>
    </view>
    
    <!-- 主题选择弹窗 -->
    <view v-if="showThemePopup" class="theme-popup-mask" @tap="closeThemeSelector">
      <view class="theme-popup" @tap.stop>
        <view class="theme-popup-header">
          <text class="popup-title">选择主题</text>
          <text class="close-btn" @tap="closeThemeSelector">×</text>
        </view>
        <view class="theme-list">
          <view
            v-for="theme in availableThemes"
            :key="theme.id"
            class="theme-item"
            :class="{ active: currentThemeId === theme.id }"
            @tap="selectTheme(theme.id)"
          >
            <view class="theme-preview" :style="{ background: theme.background }">
              <view class="theme-preview-content">
                <view class="preview-circle" :style="{ backgroundColor: theme.primary }"></view>
                <view class="preview-circle" :style="{ backgroundColor: theme.secondary }"></view>
                <view class="preview-circle" :style="{ backgroundColor: theme.primary }"></view>
              </view>
            </view>
            <text class="theme-name">{{ theme.name }}</text>
            <view v-if="currentThemeId === theme.id" class="theme-check">✓</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 自定义TabBar -->
    <custom-tab-bar />
  </view>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import customTabBar from '@/components/custom-tab-bar.vue';
import { onShow } from '@dcloudio/uni-app';
import { getByUserInfo } from '../../api/user/auth';
import { getWalletInfo } from '@/api/wallet';
import momentApi from '@/api/moment.js';
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js';

// 图片加载错误状态
const imageError = reactive({
  avatar: false,
  coin: false
});

// 虚拟货币
const userCurrency = ref(0);

// 用户信息
const userInfo = reactive({
  id: '',
  nickname: '心动用户',
  avatar: '/static/mine/avatar.png',
  gender: 1, // 1: 男, 2: 女
  age: 28,
  followers: 0,
  following: 0,
  likes: 0,
  isVip: false,
  vipExpireDate: '2023-12-31',
  isVerified: 0 // 是否实名认证 0:未认证 1:已认证
});

// 获取用户信息
async function getUserInfo() {
  try {
    const token = uni.getStorageSync("token");
    if (!token) {
      console.log('未登录，无法获取用户信息');
      return;
    }
    
    const res = await getByUserInfo(token);
    if (res && res.code === 200 && res.data) {
      // 更新用户数据
      userInfo.id = res.data.userId;
      userInfo.nickname = res.data.nickname || '心动用户';
      userInfo.avatar = res.data.avatarUrl || '/static/mine/avatar.png';
      userInfo.gender = res.data.gender || 1;
      userInfo.followers = res.data.fan || 0;
      userInfo.following = res.data.countFollow || 0;
      userInfo.likes = res.data.countLike || 0;
      userInfo.isVip = res.data.isVip === 1;
      userInfo.vipExpireDate = res.data.vipExpireTime ? new Date(res.data.vipExpireTime).toLocaleDateString() : '';
      userInfo.isVerified = res.data.isVerified;
      userInfo.userRole = res.data.userRole; // 添加用户角色

      console.log('用户信息更新完成:', {
        userId: userInfo.id,
        userRole: userInfo.userRole,
        nickname: userInfo.nickname
      });
      
      console.log('用户信息获取成功:', userInfo);
    } else {
      console.error('获取用户信息失败:', res);
    }
  } catch (error) {
    console.error('获取用户信息异常:', error);
  }
}

// 获取钱包信息
async function getWalletData() {
  try {
    const token = uni.getStorageSync("token");
    if (!token) {
      console.log('未登录，无法获取钱包信息');
      return;
    }

    const response = await getWalletInfo();
    if (response.code === 200) {
      userCurrency.value = response.data.coinBalance || 0;
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error);
  }
}

// 获取用户动态
async function getUserPosts() {
  try {
    const token = uni.getStorageSync("token");
    if (!token) {
      console.log('未登录，无法获取用户动态');
      return;
    }

    postsLoading.value = true;
    postsError.value = '';

    const result = await momentApi.getMyMoments(1, 3); // 只获取前3条动态用于预览

    if (result.code === 200) {
      const records = result.data?.records || [];

      // 转换数据格式以适配现有UI
      userPosts.value = records.map(moment => ({
        id: moment.momentId,
        title: moment.content,
        image: moment.mediaList && moment.mediaList.length > 0 ? moment.mediaList[0].mediaUrl : '/static/mine/default-post.png',
        likes: moment.likeCount || 0,
        comments: moment.commentCount || 0,
        momentData: moment // 保存原始数据
      }));

      console.log('用户动态获取成功:', userPosts.value);
    } else {
      postsError.value = result.message || '获取动态失败';
      console.error('获取用户动态失败:', result);
    }
  } catch (error) {
    postsError.value = '获取动态失败';
    console.error('获取用户动态异常:', error);
  } finally {
    postsLoading.value = false;
  }
}

// 使用全局主题混入
const { currentBackground, currentThemeId, availableThemes, switchTheme } = useGlobalThemeMixin();

// 主题相关状态
const showThemePopup = ref(false);

// 用户动态相关状态
const userPosts = ref([]);
const postsLoading = ref(false);
const postsError = ref('');

// 页面加载时获取用户信息
onMounted(() => {
  getWalletData();
  console.log('mine页面初始化完成，当前主题:', currentThemeId.value);
});

// 每次显示页面时都获取最新的用户信息，但避免与onMounted重复调用
onShow(() => {
  // 使用一个标志来避免首次加载时重复调用
  // if (userInfo.id) {
    getUserInfo();
    getWalletData();
    getUserPosts(); // 获取用户动态
  // }
});

// 页面卸载时清理
onUnmounted(() => {
  // 全局主题混入会自动处理事件监听的清理
  console.log('mine页面已卸载');
});

// 菜单项 - 使用计算属性来动态显示实名认证状态
// 菜单项 - 使用计算属性来动态显示实名认证状态和红娘功能
const menuItems = computed(() => {
  const baseItems = [
    { name: '我的喜欢', icon: 'icon-like', url: '/pages/mine/likes' },
    { name: '我的收藏', icon: 'icon-favorite', url: '/pages/mine/favorites' },
    {
      name: userInfo.isVerified === 1 ? '认证管理' : '实名认证',
      icon: 'icon-verify',
      action: 'goToVerification'
    }
  ];

  // 根据用户角色添加不同的菜单项
  console.log('用户角色调试:', userInfo.userRole, typeof userInfo.userRole);
  if (userInfo.userRole === 2) {
    // 红娘用户菜单
    console.log('显示红娘菜单');
    baseItems.push(
      { name: '申请管理', icon: 'icon-manage', url: '/pages/matchmaker/manage-requests' },
      { name: '约会安排', icon: 'icon-date', url: '/pages/matchmaker/arrange-dates' },
      { name: '我的申请', icon: 'icon-heart', url: '/pages/matchmaker/my-requests' },
      { name: '我的订单', icon: 'icon-order', url: '/pages/matchmaker/order-list' }
    );
  } else {
    // 普通用户菜单
    console.log('显示普通用户菜单');
    baseItems.push(
      { name: '申请红娘', icon: 'icon-matchmaker', url: '/pages/matchmaker/apply' },
      { name: '牵线申请', icon: 'icon-heart', url: '/pages/matchmaker/my-requests' },
      { name: '我的订单', icon: 'icon-order', url: '/pages/matchmaker/order-list' },
      { name: '牵线确认', icon: 'icon-confirm', url: '/pages/matchmaking/confirm' }
    );
  }

  // 添加通用菜单项
  baseItems.push(
    { name: '每日签到', icon: 'icon-sign', url: '/pages/sign/sign' },
    { name: '优惠券', icon: 'icon-voucher', url: '/pages/voucher/voucher-list' },
    { name: '我的相册', icon: 'icon-album', url: '/pages/mine/album' },
    { name: '主题切换', icon: 'icon-theme', action: 'showThemeSelector' },
    { name: '我的活动', icon: 'icon-activity', url: '/pages/mine/activities' },
    { name: '我的礼物', icon: 'icon-gift', url: '/pages/mine/gifts' },
    { name: '客服中心', icon: 'icon-service', url: '/pages/mine/service' }
  );

  return baseItems;
});

// 页面功能函数

// 处理图片加载错误
function handleImageError(type) {
  imageError[type] = true;
}

// 页面跳转函数
function goFollowers() {
  uni.navigateTo({
    url: '/pages/user/fans-list'
  });
}

function goFollowing() {
  uni.navigateTo({
    url: '/pages/user/following-list'
  });
}

function goLikes() {
  uni.navigateTo({
    url: '/pages/mine/likes-received'
  });
}

// 跳转到编辑资料页面
function goToEditProfile() {
  // 如果有用户ID，则传递给编辑资料页面
  const userId = userInfo?.id || '';
  
  uni.navigateTo({
    url: '/pages/profile/edit-profile' + (userId ? `?id=${userId}` : ''),
    fail: (err) => {
      console.error('跳转失败:', err);
      uni.showToast({
        title: '跳转失败，请稍后重试',
        icon: 'none'
      });
    }
  });
}

function goVipCenter() {
  uni.navigateTo({
    url: '/pages/vip/recharge'
  });
}

// 主题相关方法
function showThemeSelector() {
  showThemePopup.value = true;
}

function closeThemeSelector() {
  showThemePopup.value = false;
}





function selectTheme(themeId) {
  console.log('选择主题:', themeId);

  // 使用全局主题切换函数
  const success = switchTheme(themeId);

  if (success) {
    console.log('主题切换成功:', themeId);
    uni.showToast({
      title: `已切换到${themeId}主题`,
      icon: 'success'
    });
    
    // 关闭主题选择弹窗
    closeThemeSelector();
  } else {
    console.error('主题切换失败:', themeId);
    uni.showToast({
      title: '主题切换失败',
      icon: 'error'
    });
  }
}

function handleMenuClick(item) {
  // 如果有自定义action，执行对应的函数
  if (item.action) {
    switch (item.action) {
      case 'goToVerification':
        goToVerification();
        break;
      case 'showThemeSelector':
        showThemeSelector();
        break;
      case 'directTest':
        directTest();
        break;
      default:
        console.warn('未知的菜单action:', item.action);
    }
  } else if (item.url) {
    // 否则使用url跳转
    uni.navigateTo({
      url: item.url
    });
  }
}

// 跳转到钱包页面
function goToWallet() {
  const token = uni.getStorageSync("token");
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    uni.navigateTo({
      url: '/pages/login/login'
    });
    return;
  }

  uni.navigateTo({
    url: '/pages/wallet/wallet'
  });
}

// 跳转到实名认证页面
function goToVerification() {
  const token = uni.getStorageSync("token");
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    uni.navigateTo({
      url: '/pages/login/login'
    });
    return;
  }

  uni.navigateTo({
    url: '/pages/profile/verify',
    fail: (err) => {
      console.error('跳转实名认证页面失败:', err);
      uni.showToast({
        title: '跳转失败，请稍后重试',
        icon: 'none'
      });
    }
  });
}

function goAllPosts() {
  // 跳转到动态列表页面，并设置为只显示我的动态
  uni.navigateTo({
    url: '/pages/moment/list?type=mine'
  });
}

function viewPost(post) {
  // 如果有动态数据，跳转到动态详情页面
  if (post.momentData) {
    uni.navigateTo({
      url: `/pages/moment/detail?id=${post.momentData.momentId}`
    });
  } else {
    // 兼容旧数据格式
    uni.navigateTo({
      url: `/pages/mine/post-detail?id=${post.id}`
    });
  }
}

function createPost() {
  // 跳转到发布动态页面
  uni.navigateTo({
    url: '/pages/moment/publish'
  });
}

function goSettings() {
  uni.navigateTo({
    url: '/pages/mine/settings'
  });
}

function goFeedback() {
  uni.navigateTo({
    url: '/pages/mine/feedback'
  });
}

function goAbout() {
  uni.navigateTo({
    url: '/pages/mine/about'
  });
}

function logout() {
  // 使用自定义对话框
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？\n您的个人信息将被清除',
    confirmText: '退出',
    cancelText: '取消',
    confirmColor: '#FF6B8B',
    cancelColor: '#8E8E93',
    success: (res) => {
      if (res.confirm) {
        // 清除token
        uni.removeStorageSync('token');
        
        // 清除其他用户相关信息
        uni.removeStorageSync('userInfo');
        
        // 重置用户信息
        userInfo.id = '';
        userInfo.nickname = '心动用户';
        userInfo.avatar = '/static/mine/avatar.png';
        userInfo.gender = 1;
        userInfo.followers = 0;
        userInfo.following = 0;
        userInfo.likes = 0;
        userInfo.isVip = false;
        userInfo.isVerified = 0;
        
        // 显示退出成功提示
        uni.showToast({
          title: '已安全退出',
          icon: 'success',
          duration: 1500,
          success: () => {
            // 延迟跳转到登录页面
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login/login'
              });
            }, 1000);
          }
        });
      }
    }
  });
}
</script>

<style lang="scss" scoped>
.mine-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  &.theme-test {
    background: linear-gradient(135deg, #ff0000 0%, #00ff00 100%) !important;
  }

  &.theme-ocean {
    background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%) !important;
  }

  &.theme-sunset {
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%) !important;
  }
  
  // 背景层
  .bg-layer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;

    &.theme-test {
      background: linear-gradient(135deg, #ff0000 0%, #00ff00 100%) !important;
    }

    &.theme-ocean {
      background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%) !important;
    }

    &.theme-sunset {
      background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%) !important;
    }
  }
  
  // 内容容器
  .content-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 40rpx 30rpx 110rpx 30rpx; // 底部增加110rpx padding为自定义tabBar预留空间
    z-index: 1;
    
    // 顶部区域
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 30rpx;
      
      .left-section {
        display: flex;
        align-items: center;
        
        .page-title {
          font-size: 36rpx;
          font-weight: bold;
          color: #fff;
        }
      }
      
      // 虚拟货币
      .currency-section {
        display: flex;
        align-items: center;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 30rpx;
        padding: 0 20rpx;
        height: 60rpx;
        
        .currency-icon {
          width: 36rpx;
          height: 36rpx;
          margin-right: 8rpx;
          
          .coin-placeholder {
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.3);
            border-radius: 50%;
          }
        }
        
        .currency-amount {
          font-size: 26rpx;
          color: #fff;
          font-weight: bold;
        }
      }
    }
    
    // 用户信息卡片
    .user-card {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 30rpx;
      
      .user-info {
        display: flex;
        align-items: center;
        margin-bottom: 30rpx;
        
        .avatar {
          width: 120rpx;
          height: 120rpx;
          border-radius: 60rpx;
          margin-right: 20rpx;
          background-color: rgba(255, 255, 255, 0.1);
          position: relative;
          
          .avatar-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 60rpx;
            background: linear-gradient(45deg, #12C2E9, #C471ED);
            
            .avatar-text {
              font-size: 48rpx;
              font-weight: bold;
              color: #fff;
            }
          }
        }
        
        .user-detail {
          flex: 1;
          
          .name-row {
            display: flex;
            align-items: center;
            margin-bottom: 10rpx;
            
            .nickname {
              font-size: 34rpx;
              font-weight: bold;
              color: #fff;
              margin-right: 15rpx;
            }
            
            .gender-tag {
              width: 40rpx;
              height: 40rpx;
              border-radius: 20rpx;
              display: flex;
              align-items: center;
              justify-content: center;
              
              &.male {
                background: #007AFF;
              }
              
              &.female {
                background: #FF2D55;
              }
              
              .gender-icon {
                font-size: 24rpx;
                color: #fff;
                font-weight: bold;
              }
            }
          }
          
          .user-desc {
            display: flex;
            align-items: center;
            
            .user-id {
              font-size: 24rpx;
              color: rgba(255, 255, 255, 0.7);
              margin-right: 20rpx;
            }
            
            .user-verified {
              font-size: 24rpx;
              font-weight: 500;
              padding: 4rpx 12rpx;
              border-radius: 12rpx;
              margin-left: 15rpx;
              
              &.verified {
                background-color: rgba(46, 213, 115, 0.15);
                color: #2ED573;
                border: 1rpx solid rgba(46, 213, 115, 0.3);
              }
              
              &.unverified {
                background-color: rgba(255, 71, 87, 0.15);
                color: #FF4757;
                border: 1rpx solid rgba(255, 71, 87, 0.3);
              }
            }
          }
        }
      }
      
      .user-stats {
        display: flex;
        justify-content: space-around;
        margin-bottom: 30rpx;
        
        .stat-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .stat-num {
            font-size: 32rpx;
            font-weight: bold;
            color: #fff;
            margin-bottom: 5rpx;
          }
          
          .stat-label {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.7);
          }
        }
      }
      
      .edit-profile-btn {
        height: 70rpx;
        border-radius: 35rpx;
        background: rgba(255, 255, 255, 0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        
        text {
          font-size: 28rpx;
          color: #fff;
        }
      }
    }
    
    // 会员卡片
    .vip-card {
      background: linear-gradient(45deg, #FFD700, #FFA500);
      border-radius: 16rpx;
      padding: 24rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;

      .vip-info {
        .vip-title {
          font-size: 30rpx;
          font-weight: bold;
          color: #fff;
          display: block;
          margin-bottom: 8rpx;
          text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
        }

        .vip-desc {
          font-size: 22rpx;
          color: rgba(255, 255, 255, 0.9);
          display: block;
        }
      }

      .vip-button {
        background-color: #fff;
        color: #FFA500;
        font-size: 24rpx;
        font-weight: bold;
        padding: 8rpx 24rpx;
        border-radius: 24rpx;
        box-shadow: 0 3rpx 6rpx rgba(0, 0, 0, 0.1);
      }
    }
    
    // 功能菜单
    .menu-section {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      padding: 20rpx;
      margin-bottom: 30rpx;

      .menu-swiper {
        height: 360rpx; // 再次增加高度确保文字完全显示

        .menu-page {
          display: flex;
          flex-direction: column;
          height: 100%;
          padding: 3rpx 0; // 增加上下内边距
        }
      }

      .menu-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 35rpx; // 进一步增加行间距
        flex: 1;
        align-items: flex-start; // 改为顶部对齐，给文字更多空间

        &:last-child {
          margin-bottom: 0;
        }
        
        .menu-item {
          width: 22%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: flex-start; // 改为顶部对齐
          min-height: 130rpx; // 进一步增加最小高度
          padding: 15rpx 5rpx; // 增加内边距

          &.menu-placeholder {
            opacity: 0;
            pointer-events: none;
          }

          .menu-icon {
            width: 80rpx; // 保持图标尺寸
            height: 80rpx;
            border-radius: 40rpx;
            background: rgba(255, 255, 255, 0.3);
            margin-bottom: 15rpx; // 增加图标和文字间距
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 40rpx; // 保持图标字体大小
            color: #fff;
            flex-shrink: 0; // 防止图标被压缩
            
            &.icon-like::before { content: '♥'; }
            &.icon-favorite::before { content: '★'; }
            &.icon-verify::before { content: '🆔'; }
            &.icon-matchmaker::before { content: '💕'; }
            &.icon-heart::before { content: '💘'; }
            &.icon-manage::before { content: '📋'; }
            &.icon-date::before { content: '📅'; }
            &.icon-confirm::before { content: '✅'; }
            &.icon-order::before { content: '📦'; }
            &.icon-sign::before { content: '📝'; }
            &.icon-voucher::before { content: '🎫'; }
            &.icon-album::before { content: '🖼'; }
            &.icon-theme::before { content: '🎨'; }
            &.icon-activity::before { content: '🎭'; }
            &.icon-gift::before { content: '🎁'; }
            &.icon-service::before { content: '💬'; }
          }
          
          .menu-name {
            font-size: 26rpx; // 保持文字字体大小
            color: #fff;
            text-align: center;
            line-height: 1.4; // 增加行高确保文字显示完整
            white-space: nowrap; // 防止文字换行
            font-weight: 500; // 保持字体粗细
            flex-shrink: 0; // 防止文字被压缩
            margin-top: auto; // 确保文字在底部显示
          }
        }
      }
    }
    
    // 我的动态
    .posts-section {
      margin-bottom: 30rpx;
      
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
        
        .section-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #fff;
        }
        
        .more-link {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      .posts-list {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        
        .post-item {
          width: 48%;
          margin-bottom: 20rpx;
          
          .post-image {
            width: 100%;
            height: 240rpx;
            border-radius: 15rpx;
            background-color: rgba(255, 255, 255, 0.1);
            margin-bottom: 10rpx;
            
            .image-placeholder {
              width: 100%;
              height: 100%;
              background: rgba(255, 255, 255, 0.1);
              border-radius: 15rpx;
            }
          }
          
          .post-title {
            font-size: 26rpx;
            color: #fff;
            display: block;
            margin-bottom: 8rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .post-stats {
            display: flex;
            
            .stat {
              display: flex;
              align-items: center;
              margin-right: 20rpx;
              
              .stat-icon {
                width: 24rpx;
                height: 24rpx;
                margin-right: 5rpx;
                
                &.like-icon {
                  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>') no-repeat center;
                  background-size: contain;
                }
                
                &.comment-icon {
                  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path></svg>') no-repeat center;
                  background-size: contain;
                }
              }
              
              .stat-count {
                font-size: 22rpx;
                color: rgba(255, 255, 255, 0.7);
              }
            }
          }
        }
      }
      
      .loading-posts {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 40rpx 0;

        .loading-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.7);
        }
      }

      .error-posts {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 40rpx 0;

        .error-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.7);
          margin-bottom: 20rpx;
        }

        .retry-btn {
          background: rgba(255, 255, 255, 0.3);
          border-radius: 30rpx;
          padding: 10rpx 30rpx;

          text {
            font-size: 26rpx;
            color: #fff;
          }
        }
      }

      .empty-posts {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 40rpx 0;

        .empty-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.7);
          margin-bottom: 30rpx;
        }

        .create-post-btn {
          background: rgba(255, 255, 255, 0.3);
          border-radius: 30rpx;
          padding: 15rpx 40rpx;

          text {
            font-size: 28rpx;
            color: #fff;
          }
        }
      }
    }
    
    // 设置区域
    .settings-section {
      margin-top: 30rpx;
      
      .setting-item {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;
        padding: 30rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;
        
        .setting-name {
          font-size: 30rpx;
          color: #fff;
          
          &.logout-text {
            color: #FF4757;
            font-weight: bold;
          }
        }
        
        .setting-arrow {
          width: 16rpx;
          height: 16rpx;
          border-right: 2rpx solid #fff;
          border-top: 2rpx solid #fff;
          transform: rotate(45deg);
        }
        
        &.logout-item {
          background: linear-gradient(45deg, #FFA5B8, #FF6B8B);
          border: none;
          box-shadow: 0 4rpx 10rpx rgba(255, 107, 139, 0.2);
          transition: all 0.3s ease;
          margin-top: 20rpx;
          
          .setting-name {
            color: #FFFFFF;
            font-weight: 500;
            font-size: 30rpx;
          }
          
          .setting-arrow {
            border-right: 2rpx solid #FFFFFF;
            border-top: 2rpx solid #FFFFFF;
          }
          
          &:active {
            transform: scale(0.98);
            opacity: 0.9;
          }
        }
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
  position: relative;
  z-index: 0;
  
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
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.05)" fill-opacity="1" d="M0,64L48,96C96,128,192,192,288,202.7C384,213,480,171,576,165.3C672,160,768,192,864,197.3C960,203,1056,181,1152,165.3C1248,149,1344,139,1392,133.3L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom;
    background-size: 100% 40%;
    opacity: 0.6;
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%),
          radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%);
  }
}

/* 主题选择弹窗样式 */
.theme-popup-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
}

.theme-popup {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
  max-height: 80vh;
  width: 100%;
  animation: slideUp 0.3s ease;

  .theme-popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;

    .popup-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }

    .close-btn {
      font-size: 48rpx;
      color: #999;
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .theme-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 30rpx;

    .theme-item {
      position: relative;
      background: #fff;
      border-radius: 20rpx;
      padding: 20rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &.active {
        border: 4rpx solid #00D4AA;
        transform: scale(1.02);
      }

      .theme-preview {
        height: 120rpx;
        border-radius: 16rpx;
        margin-bottom: 20rpx;
        position: relative;
        overflow: hidden;

        .theme-preview-content {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          display: flex;
          gap: 10rpx;

          .preview-circle {
            width: 24rpx;
            height: 24rpx;
            border-radius: 50%;
            border: 2rpx solid rgba(255, 255, 255, 0.3);
          }
        }
      }

      .theme-name {
        font-size: 28rpx;
        color: #333;
        text-align: center;
        font-weight: 500;
      }

      .theme-check {
        position: absolute;
        top: 10rpx;
        right: 10rpx;
        width: 40rpx;
        height: 40rpx;
        background: #00D4AA;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 24rpx;
        font-weight: bold;
      }
    }
  }
}

/* 主题图标样式 */
.icon-theme {
  background: linear-gradient(45deg, #FF6B9D, #C44569);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  &::before {
    content: '🎨';
    background: none;
    -webkit-background-clip: unset;
    background-clip: unset;
    color: #fff;
  }
}

/* 测试图标样式 */
.icon-test {
  &::before {
    content: '🧪';
    color: #fff;
  }
}

/* 动画效果 */
@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>