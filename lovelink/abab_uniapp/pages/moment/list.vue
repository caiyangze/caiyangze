<template>
  <view class="moment-list-page">
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
          <text class="navbar-title">{{ listType === 'mine' ? '我的动态' : '动态广场' }}</text>
        </view>
        <view class="navbar-right" @tap="goToPublish">
          <text class="publish-btn">发布</text>
        </view>
      </view>

      <!-- 动态列表 -->
      <scroll-view 
        scroll-y 
        class="scroll-area"
        @scrolltolower="loadMore"
        refresher-enabled
        @refresherrefresh="onRefresh"
        :refresher-triggered="refreshing"
      >
        <view 
          v-for="moment in momentList" 
          :key="moment.momentId" 
          class="moment-item"
          @tap="viewMomentDetail(moment)"
        >
          <!-- 用户信息 -->
          <view class="user-info">
            <image :src="moment.avatarUrl || '/static/default-avatar.png'" class="avatar"></image>
            <view class="user-details">
              <text class="nickname">{{ moment.nickname || '匿名用户' }}</text>
              <text class="time">{{ formatTime(moment.createdAt) }}</text>
            </view>
            <view v-if="moment.isMine" class="more-btn" @tap.stop="showMoreOptions(moment)">
              <text>⋯</text>
            </view>
          </view>

          <!-- 动态内容 -->
          <view class="content">
            <text>{{ moment.content }}</text>
          </view>

          <!-- 位置信息 -->
          <view v-if="moment.location" class="location">
            <text class="location-icon">📍</text>
            <text class="location-text">{{ moment.location }}</text>
          </view>

          <!-- 媒体内容 -->
          <view v-if="moment.mediaList && moment.mediaList.length > 0" class="media-grid">
            <image 
              v-for="(media, index) in moment.mediaList" 
              :key="media.mediaId"
              :src="media.mediaUrl" 
              class="media-image"
              @tap.stop="previewImages(moment.mediaList, index)"
            ></image>
          </view>

          <!-- 互动区域 -->
          <view class="actions">
            <view class="action-item" @tap.stop="toggleLike(moment)">
              <text :class="['action-icon', moment.isLiked ? 'liked' : '']">❤️</text>
              <text class="action-text">{{ moment.likeCount || 0 }}</text>
            </view>
            <view class="action-item" @tap.stop="showComments(moment)">
              <text class="action-icon">💬</text>
              <text class="action-text">{{ moment.commentCount || 0 }}</text>
            </view>
            <view class="action-item">
              <text class="action-icon">👁️</text>
              <text class="action-text">{{ moment.viewCount || 0 }}</text>
            </view>
          </view>
        </view>

        <!-- 加载状态 -->
        <view v-if="loading" class="loading">
          <text>加载中...</text>
        </view>

        <view v-if="noMore && momentList.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>

        <view v-if="momentList.length === 0 && !loading" class="empty">
          <text>暂无动态</text>
        </view>
      </scroll-view>

      <!-- 发布按钮 -->
      <view class="fab" @tap="goToPublish">
        <text class="fab-icon">+</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import momentApi from '@/api/moment.js';

// 数据
const momentList = ref([]);
const loading = ref(false);
const refreshing = ref(false);
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = 10;
const listType = ref('public'); // 'public' 或 'mine'

// 页面加载时获取参数
onLoad((options) => {
  if (options.type === 'mine') {
    listType.value = 'mine';
  }
});

// 页面加载
onMounted(() => {
  loadMomentList();
});

// 加载动态列表
async function loadMomentList(isRefresh = false) {
  if (loading.value) return;

  loading.value = true;

  try {
    const page = isRefresh ? 1 : currentPage.value;

    // 根据类型调用不同的API
    let result;
    if (listType.value === 'mine') {
      result = await momentApi.getMyMoments(page, pageSize);
    } else {
      result = await momentApi.getPublicMoments(page, pageSize);
    }

    if (result.code === 200) {
      const records = result.data.records || [];

      if (isRefresh) {
        momentList.value = records;
        currentPage.value = 1;
        noMore.value = false;
      } else {
        momentList.value.push(...records);
      }

      // 检查是否还有更多数据
      if (records.length < pageSize) {
        noMore.value = true;
      } else {
        currentPage.value++;
      }
    } else {
      throw new Error(result.message || '加载失败');
    }
    
  } catch (error) {
    console.error('加载动态列表失败:', error);
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
    refreshing.value = false;
  }
}

// 下拉刷新
function onRefresh() {
  refreshing.value = true;
  loadMomentList(true);
}

// 加载更多
function loadMore() {
  if (!noMore.value) {
    loadMomentList();
  }
}

// 格式化时间
function formatTime(timeStr) {
  const time = new Date(timeStr);
  const now = new Date();
  const diff = now - time;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return Math.floor(diff / 86400000) + '天前';
}

// 预览图片
function previewImages(mediaList, currentIndex) {
  const urls = mediaList.map(media => media.mediaUrl);
  uni.previewImage({
    current: currentIndex,
    urls: urls
  });
}

// 跳转到发布页面
function goToPublish() {
  uni.navigateTo({
    url: '/pages/moment/publish'
  });
}

// 返回
function goBack() {
  uni.navigateBack();
}

// 查看动态详情
function viewMomentDetail(moment) {
  // TODO: 跳转到动态详情页
  console.log('查看动态详情:', moment);
}

// 显示更多选项
function showMoreOptions(moment) {
  uni.showActionSheet({
    itemList: ['删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        deleteMoment(moment);
      }
    }
  });
}

// 删除动态
async function deleteMoment(moment) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条动态吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await momentApi.deleteMoment(moment.momentId);
          if (result.code === 200) {
            // 从列表中移除
            const index = momentList.value.findIndex(m => m.momentId === moment.momentId);
            if (index > -1) {
              momentList.value.splice(index, 1);
            }
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            });
          } else {
            throw new Error(result.message || '删除失败');
          }
        } catch (error) {
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          });
        }
      }
    }
  });
}

// 点赞/取消点赞
function toggleLike(moment) {
  // TODO: 实现点赞功能
  console.log('点赞:', moment);
}

// 显示评论
function showComments(moment) {
  // TODO: 跳转到评论页面
  console.log('查看评论:', moment);
}
</script>

<style lang="scss" scoped>
.moment-list-page {
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
        }
      }
    }
    
    // 滚动区域
    .scroll-area {
      flex: 1;
      padding: 20rpx;
    }
    
    // 动态项
    .moment-item {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 20rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      backdrop-filter: blur(10rpx);
      
      .user-info {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        
        .avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 50%;
          margin-right: 20rpx;
        }
        
        .user-details {
          flex: 1;
          
          .nickname {
            display: block;
            font-size: 32rpx;
            font-weight: bold;
            color: #fff;
          }
          
          .time {
            display: block;
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.7);
            margin-top: 5rpx;
          }
        }
        
        .more-btn {
          padding: 10rpx;
          font-size: 32rpx;
          color: rgba(255, 255, 255, 0.7);
        }
      }
      
      .content {
        font-size: 32rpx;
        line-height: 1.5;
        color: #fff;
        margin-bottom: 20rpx;
      }
      
      .location {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;
        
        .location-icon {
          margin-right: 10rpx;
          font-size: 24rpx;
        }
        
        .location-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      .media-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 10rpx;
        margin-bottom: 20rpx;
        
        .media-image {
          width: 200rpx;
          height: 200rpx;
          border-radius: 10rpx;
        }
      }
      
      .actions {
        display: flex;
        justify-content: space-around;
        padding-top: 20rpx;
        border-top: 1rpx solid rgba(255, 255, 255, 0.2);
        
        .action-item {
          display: flex;
          align-items: center;
          padding: 10rpx 20rpx;
          
          .action-icon {
            margin-right: 10rpx;
            font-size: 32rpx;
            
            &.liked {
              color: #ff4757;
            }
          }
          
          .action-text {
            font-size: 28rpx;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }
    }
    
    // 状态提示
    .loading, .no-more, .empty {
      text-align: center;
      padding: 40rpx;
      color: rgba(255, 255, 255, 0.7);
      font-size: 28rpx;
    }
    
    // 悬浮按钮
    .fab {
      position: fixed;
      bottom: 100rpx;
      right: 50rpx;
      width: 100rpx;
      height: 100rpx;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(10rpx);
      border: 2rpx solid rgba(255, 255, 255, 0.2);
      
      .fab-icon {
        color: #fff;
        font-size: 48rpx;
        font-weight: bold;
      }
      
      &:active {
        transform: scale(0.95);
        background: rgba(255, 255, 255, 0.4);
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
</style>
