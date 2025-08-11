<template>
  <view class="test-page">
    <view class="header">
      <text class="title">相册功能测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">1. 上传照片测试</text>
      <button class="test-btn" @tap="testUpload">选择并上传照片</button>
      <text class="result" v-if="uploadResult">{{ uploadResult }}</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">2. 获取相册列表测试</text>
      <button class="test-btn" @tap="testGetList">获取相册列表</button>
      <text class="result" v-if="listResult">{{ listResult }}</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">3. 相册照片展示</text>
      <view class="photo-grid" v-if="photos.length > 0">
        <view class="photo-item" v-for="photo in photos" :key="photo.photoId">
          <image :src="photo.photoUrl" mode="aspectFill" class="photo-image" />
          <view class="photo-info">
            <text class="photo-desc">{{ photo.photoDesc || '无描述' }}</text>
            <text class="photo-status">{{ photo.isPublic ? '公开' : '私密' }}</text>
            <text class="photo-avatar" v-if="photo.isAvatar">头像</text>
          </view>
          <view class="photo-actions">
            <button class="action-btn" @tap="testSetAvatar(photo.photoId)">设为头像</button>
            <button class="action-btn edit" @tap="testEdit(photo.photoId)">编辑</button>
            <button class="action-btn delete" @tap="testDelete(photo.photoId)">删除</button>
          </view>
        </view>
      </view>
    </view>
    
    <view class="test-section">
      <text class="section-title">4. API配置信息</text>
      <text class="config-info">Base URL: {{ baseUrl }}</text>
      <text class="config-info">Token: {{ token ? token.substring(0, 20) + '...' : '未登录' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { uploadPhoto, getPhotoList, deletePhoto, setAsAvatar, updatePhoto } from '../../api/user/photo'
import http from '../../api/http'

// 测试数据
const uploadResult = ref('')
const listResult = ref('')
const photos = ref([])
const baseUrl = ref('')
const token = ref('')

onMounted(() => {
  // 获取配置信息
  baseUrl.value = http.getBaseUrl()
  token.value = uni.getStorageSync('token')
  
  // 自动加载相册列表
  testGetList()
})

// 测试上传照片
function testUpload() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        uploadResult.value = '上传中...'
        const filePath = res.tempFilePaths[0]
        const result = await uploadPhoto(filePath, '这是一张测试照片，包含描述信息', 1)

        if (result.code === 200) {
          uploadResult.value = '上传成功！'
          // 重新加载相册列表
          testGetList()
        } else {
          uploadResult.value = `上传失败：${result.message}`
        }
      } catch (error) {
        uploadResult.value = `上传异常：${error.message || error}`
        console.error('上传失败:', error)
      }
    },
    fail: (err) => {
      uploadResult.value = `选择图片失败：${err.errMsg}`
    }
  })
}

// 测试获取相册列表
async function testGetList() {
  try {
    listResult.value = '加载中...'
    const result = await getPhotoList(1, 20)
    
    if (result.code === 200) {
      photos.value = result.data.records || []
      listResult.value = `获取成功，共${photos.value.length}张照片`
    } else {
      listResult.value = `获取失败：${result.message}`
    }
  } catch (error) {
    listResult.value = `获取异常：${error.message || error}`
    console.error('获取相册列表失败:', error)
  }
}

// 测试设置头像
async function testSetAvatar(photoId) {
  try {
    const result = await setAsAvatar(photoId)
    
    if (result.code === 200) {
      uni.showToast({
        title: '设置头像成功',
        icon: 'success'
      })
      // 重新加载相册列表
      testGetList()
    } else {
      uni.showToast({
        title: result.message || '设置失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '设置异常',
      icon: 'none'
    })
    console.error('设置头像失败:', error)
  }
}

// 测试编辑照片
async function testEdit(photoId) {
  try {
    const result = await updatePhoto(photoId, '这是修改后的照片描述', 0) // 设为私密

    if (result.code === 200) {
      uni.showToast({
        title: '编辑成功',
        icon: 'success'
      })
      // 重新加载相册列表
      testGetList()
    } else {
      uni.showToast({
        title: result.message || '编辑失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('编辑照片失败:', error)
    uni.showToast({
      title: '编辑失败，请重试',
      icon: 'none'
    })
  }
}

// 测试删除照片
async function testDelete(photoId) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这张照片吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await deletePhoto(photoId)
          
          if (result.code === 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
            // 重新加载相册列表
            testGetList()
          } else {
            uni.showToast({
              title: result.message || '删除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.showToast({
            title: '删除异常',
            icon: 'none'
          })
          console.error('删除照片失败:', error)
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.test-page {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
  
  .title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
}

.test-section {
  background: white;
  margin-bottom: 30rpx;
  padding: 30rpx;
  border-radius: 10rpx;
  
  .section-title {
    display: block;
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .test-btn {
    background: #007aff;
    color: white;
    border: none;
    padding: 20rpx 40rpx;
    border-radius: 10rpx;
    font-size: 28rpx;
    margin-bottom: 20rpx;
  }
  
  .result {
    display: block;
    font-size: 26rpx;
    color: #666;
    background: #f8f8f8;
    padding: 20rpx;
    border-radius: 8rpx;
    word-break: break-all;
  }
  
  .config-info {
    display: block;
    font-size: 24rpx;
    color: #666;
    margin-bottom: 10rpx;
    word-break: break-all;
  }
}

.photo-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20rpx;
}

.photo-item {
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  overflow: hidden;
  
  .photo-image {
    width: 100%;
    height: 300rpx;
  }
  
  .photo-info {
    padding: 20rpx;
    
    .photo-desc {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-bottom: 10rpx;
    }
    
    .photo-status {
      display: inline-block;
      font-size: 24rpx;
      color: #666;
      background: #f0f0f0;
      padding: 4rpx 12rpx;
      border-radius: 12rpx;
      margin-right: 10rpx;
    }
    
    .photo-avatar {
      display: inline-block;
      font-size: 24rpx;
      color: white;
      background: #ff9500;
      padding: 4rpx 12rpx;
      border-radius: 12rpx;
    }
  }
  
  .photo-actions {
    padding: 20rpx;
    display: flex;
    gap: 20rpx;
    
    .action-btn {
      flex: 1;
      background: #007aff;
      color: white;
      border: none;
      padding: 16rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      
      &.edit {
        background: #ff9500;
      }

      &.delete {
        background: #ff3b30;
      }
    }
  }
}
</style>
