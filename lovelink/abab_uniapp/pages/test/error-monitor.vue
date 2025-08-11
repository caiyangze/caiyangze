<template>
  <view class="error-monitor-page">
    <view class="header">
      <text class="title">错误监控面板</text>
      <view class="header-actions">
        <button @tap="refreshLogs" class="action-btn">刷新</button>
        <button @tap="clearLogs" class="action-btn clear">清空</button>
      </view>
    </view>
    
    <view class="stats">
      <view class="stat-item">
        <text class="stat-label">总错误数</text>
        <text class="stat-value">{{ errorLogs.length }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-label">ScrollTop错误</text>
        <text class="stat-value">{{ scrollTopErrors }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-label">最近错误</text>
        <text class="stat-value">{{ lastErrorTime }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">快速测试</text>
      <view class="test-buttons">
        <button @tap="testScrollError" class="test-btn">触发滚动错误</button>
        <button @tap="testChatNavigation" class="test-btn">测试聊天跳转</button>
        <button @tap="testUserDetail" class="test-btn">测试用户详情</button>
      </view>
    </view>
    
    <view class="logs-section">
      <text class="section-title">错误日志 ({{ errorLogs.length }})</text>
      <scroll-view class="logs-container" scroll-y>
        <view 
          v-for="(log, index) in errorLogs" 
          :key="index" 
          class="log-item"
          :class="{ 
            'scroll-error': log.message.includes('scrollTop'),
            'critical': log.type === 'UNHANDLED_PROMISE' || log.type === 'GLOBAL_ERROR'
          }"
        >
          <view class="log-header">
            <text class="log-type">{{ log.type }}</text>
            <text class="log-time">{{ formatTime(log.timestamp) }}</text>
          </view>
          <text class="log-message">{{ log.message }}</text>
          <view v-if="log.context && Object.keys(log.context).length > 0" class="log-context">
            <text class="context-title">上下文:</text>
            <text class="context-content">{{ JSON.stringify(log.context, null, 2) }}</text>
          </view>
          <view v-if="showDetails[index]" class="log-stack">
            <text class="stack-title">堆栈信息:</text>
            <text class="stack-content">{{ log.stack }}</text>
          </view>
          <button @tap="toggleDetails(index)" class="toggle-btn">
            {{ showDetails[index] ? '隐藏详情' : '显示详情' }}
          </button>
        </view>
        
        <view v-if="errorLogs.length === 0" class="empty-logs">
          <text>暂无错误日志</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { getErrorLogs, clearErrorLogs as clearErrorLogsUtil } from '@/utils/error-handler';

// 响应式数据
const errorLogs = ref([]);
const showDetails = ref({});
const refreshTimer = ref(null);

// 计算属性
const scrollTopErrors = computed(() => {
  return errorLogs.value.filter(log => 
    log.message.includes('scrollTop') || log.type === 'SCROLL_ERROR'
  ).length;
});

const lastErrorTime = computed(() => {
  if (errorLogs.value.length === 0) return '无';
  const lastLog = errorLogs.value[errorLogs.value.length - 1];
  return formatTime(lastLog.timestamp);
});

// 格式化时间
function formatTime(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN');
}

// 刷新日志
function refreshLogs() {
  try {
    errorLogs.value = getErrorLogs();
    console.log('错误日志已刷新，共', errorLogs.value.length, '条');
  } catch (error) {
    console.error('刷新错误日志失败:', error);
  }
}

// 清空日志
function clearLogs() {
  try {
    clearErrorLogsUtil();
    errorLogs.value = [];
    showDetails.value = {};
    console.log('错误日志已清空');
  } catch (error) {
    console.error('清空错误日志失败:', error);
  }
}

// 切换详情显示
function toggleDetails(index) {
  showDetails.value[index] = !showDetails.value[index];
}

// 测试滚动错误
function testScrollError() {
  try {
    // 故意创建一个会导致scrollTop错误的情况
    const nullElement = null;
    nullElement.scrollTop = 100; // 这会触发错误
  } catch (error) {
    console.log('测试错误已触发:', error.message);
    setTimeout(refreshLogs, 100);
  }
}

// 测试聊天跳转
function testChatNavigation() {
  uni.navigateTo({
    url: '/pages/message/chat?userId=test&name=测试用户&avatar=/static/message/default-avatar.png',
    success: () => {
      console.log('聊天页面跳转成功');
    },
    fail: (error) => {
      console.error('聊天页面跳转失败:', error);
    }
  });
}

// 测试用户详情
function testUserDetail() {
  uni.navigateTo({
    url: '/pages/user/user-detail?userId=test',
    success: () => {
      console.log('用户详情页面跳转成功');
    },
    fail: (error) => {
      console.error('用户详情页面跳转失败:', error);
    }
  });
}

// 生命周期
onMounted(() => {
  refreshLogs();
  
  // 每5秒自动刷新一次
  refreshTimer.value = setInterval(refreshLogs, 5000);
  
  console.log('错误监控面板已启动');
});

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
  }
});
</script>

<style lang="scss" scoped>
.error-monitor-page {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
  
  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .header-actions {
    display: flex;
    gap: 10rpx;
    
    .action-btn {
      padding: 10rpx 20rpx;
      font-size: 24rpx;
      border-radius: 5rpx;
      background: #007aff;
      color: #fff;
      border: none;
      
      &.clear {
        background: #ff3b30;
      }
    }
  }
}

.stats {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
  
  .stat-item {
    flex: 1;
    background: #fff;
    padding: 20rpx;
    border-radius: 10rpx;
    text-align: center;
    
    .stat-label {
      display: block;
      font-size: 24rpx;
      color: #666;
      margin-bottom: 10rpx;
    }
    
    .stat-value {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
}

.test-section {
  background: #fff;
  padding: 20rpx;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
  
  .section-title {
    display: block;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 15rpx;
  }
  
  .test-buttons {
    display: flex;
    gap: 15rpx;
    flex-wrap: wrap;
    
    .test-btn {
      flex: 1;
      min-width: 200rpx;
      padding: 15rpx;
      font-size: 24rpx;
      border-radius: 5rpx;
      background: #34c759;
      color: #fff;
      border: none;
    }
  }
}

.logs-section {
  background: #fff;
  border-radius: 10rpx;
  padding: 20rpx;
  
  .section-title {
    display: block;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 15rpx;
  }
  
  .logs-container {
    max-height: 600rpx;
    
    .log-item {
      border: 1rpx solid #eee;
      border-radius: 8rpx;
      padding: 15rpx;
      margin-bottom: 15rpx;
      
      &.scroll-error {
        border-color: #ff9500;
        background: #fff8f0;
      }
      
      &.critical {
        border-color: #ff3b30;
        background: #fff0f0;
      }
      
      .log-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10rpx;
        
        .log-type {
          font-size: 22rpx;
          color: #007aff;
          font-weight: bold;
        }
        
        .log-time {
          font-size: 20rpx;
          color: #999;
        }
      }
      
      .log-message {
        display: block;
        font-size: 24rpx;
        color: #333;
        margin-bottom: 10rpx;
        word-break: break-all;
      }
      
      .log-context, .log-stack {
        margin-top: 10rpx;
        
        .context-title, .stack-title {
          display: block;
          font-size: 22rpx;
          color: #666;
          font-weight: bold;
          margin-bottom: 5rpx;
        }
        
        .context-content, .stack-content {
          display: block;
          font-size: 20rpx;
          color: #666;
          background: #f8f8f8;
          padding: 10rpx;
          border-radius: 5rpx;
          white-space: pre-wrap;
          word-break: break-all;
        }
      }
      
      .toggle-btn {
        margin-top: 10rpx;
        padding: 8rpx 15rpx;
        font-size: 22rpx;
        background: #f0f0f0;
        color: #333;
        border: none;
        border-radius: 5rpx;
      }
    }
    
    .empty-logs {
      text-align: center;
      padding: 40rpx;
      color: #999;
      font-size: 26rpx;
    }
  }
}
</style>
