<template>
  <view class="test-page">
    <view class="header">
      <text class="title">用户角色测试</text>
    </view>
    
    <view class="test-section">
      <view class="section-title">当前用户信息</view>
      <view class="user-info">
        <text class="info-item">用户ID: {{ userInfo.userId || '未获取' }}</text>
        <text class="info-item">昵称: {{ userInfo.nickname || '未获取' }}</text>
        <text class="info-item">用户角色: {{ userInfo.userRole || '未获取' }} ({{ getRoleName(userInfo.userRole) }})</text>
        <text class="info-item">是否VIP: {{ userInfo.isVip === 1 ? '是' : '否' }}</text>
      </view>
      <button class="test-btn" @click="loadUserInfo">刷新用户信息</button>
    </view>
    
    <view class="test-section">
      <view class="section-title">角色切换测试</view>
      <view class="role-buttons">
        <button class="role-btn normal" @click="setRole(1)">设为普通用户</button>
        <button class="role-btn matchmaker" @click="setRole(2)">设为红娘</button>
      </view>
      <text class="note">注意：这只是前端测试，不会真正修改数据库</text>
    </view>
    
    <view class="test-section">
      <view class="section-title">菜单项预览</view>
      <view class="menu-preview">
        <view class="menu-item" v-for="(item, index) in testMenuItems" :key="index">
          <text class="menu-icon">{{ getMenuIcon(item.icon) }}</text>
          <text class="menu-name">{{ item.name }}</text>
        </view>
      </view>
    </view>
    
    <view class="test-section">
      <view class="section-title">快捷操作</view>
      <button class="test-btn" @click="goToMine">跳转到我的页面</button>
      <button class="test-btn" @click="goToManageRequests">跳转到申请管理</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {},
      testMenuItems: []
    }
  },
  
  onLoad() {
    this.loadUserInfo()
  },
  
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const token = uni.getStorageSync('token') || ''
        if (!token) {
          uni.showToast({
            title: '请先登录',
            icon: 'error'
          })
          return
        }
        
        const result = await new Promise((resolve, reject) => {
          uni.request({
            url: 'http://localhost:9001/user/info',
            method: 'GET',
            header: {
              'Content-Type': 'application/json',
              'token': token
            },
            success: (res) => resolve(res.data),
            fail: (err) => reject(err)
          })
        })
        
        if (result.code === 200) {
          this.userInfo = result.data || {}
          this.updateMenuItems()
        } else {
          uni.showToast({
            title: result.message || '获取用户信息失败',
            icon: 'error'
          })
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'error'
        })
      }
    },
    
    // 设置角色（仅用于测试）
    setRole(role) {
      this.userInfo.userRole = role
      this.updateMenuItems()
      
      uni.showToast({
        title: `已设为${this.getRoleName(role)}（仅测试）`,
        icon: 'success'
      })
    },
    
    // 更新菜单项
    updateMenuItems() {
      const baseItems = [
        { name: '我的喜欢', icon: 'icon-like' },
        { name: '我的收藏', icon: 'icon-favorite' },
        { name: '实名认证', icon: 'icon-verify' }
      ]
      
      // 根据用户角色添加不同的菜单项
      if (this.userInfo.userRole === 2) {
        // 红娘用户菜单
        baseItems.push(
          { name: '申请管理', icon: 'icon-manage' },
          { name: '我的申请', icon: 'icon-heart' }
        )
      } else {
        // 普通用户菜单
        baseItems.push(
          { name: '申请红娘', icon: 'icon-matchmaker' },
          { name: '牵线申请', icon: 'icon-heart' }
        )
      }
      
      baseItems.push(
        { name: '每日签到', icon: 'icon-sign' },
        { name: '优惠券', icon: 'icon-voucher' }
      )
      
      this.testMenuItems = baseItems
    },
    
    // 获取角色名称
    getRoleName(role) {
      switch (role) {
        case 1: return '普通用户'
        case 2: return '红娘'
        default: return '未知角色'
      }
    },
    
    // 获取菜单图标
    getMenuIcon(iconClass) {
      const iconMap = {
        'icon-like': '♥',
        'icon-favorite': '★',
        'icon-verify': '🆔',
        'icon-matchmaker': '💕',
        'icon-heart': '💘',
        'icon-manage': '📋',
        'icon-sign': '📅',
        'icon-voucher': '🎫'
      }
      return iconMap[iconClass] || '📱'
    },
    
    // 跳转到我的页面
    goToMine() {
      uni.navigateTo({
        url: '/pages/mine/mine'
      })
    },
    
    // 跳转到申请管理
    goToManageRequests() {
      if (this.userInfo.userRole === 2) {
        uni.navigateTo({
          url: '/pages/matchmaker/manage-requests'
        })
      } else {
        uni.showToast({
          title: '只有红娘用户才能访问',
          icon: 'error'
        })
      }
    }
  }
}
</script>

<style scoped>
.test-page {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.test-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  border-left: 6rpx solid #667eea;
  padding-left: 20rpx;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.info-item {
  font-size: 26rpx;
  color: #666;
  padding: 8rpx 12rpx;
  background: #f8f9fa;
  border-radius: 6rpx;
  border-left: 4rpx solid #667eea;
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.test-btn:last-child {
  margin-bottom: 0;
}

.role-buttons {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.role-btn {
  flex: 1;
  height: 70rpx;
  border: none;
  border-radius: 35rpx;
  font-size: 26rpx;
  font-weight: bold;
}

.role-btn.normal {
  background: #3498db;
  color: white;
}

.role-btn.matchmaker {
  background: #e74c3c;
  color: white;
}

.note {
  font-size: 24rpx;
  color: #999;
  text-align: center;
  font-style: italic;
}

.menu-preview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx solid #e0e0e0;
}

.menu-icon {
  font-size: 40rpx;
  margin-bottom: 10rpx;
}

.menu-name {
  font-size: 24rpx;
  color: #666;
  text-align: center;
}
</style>
