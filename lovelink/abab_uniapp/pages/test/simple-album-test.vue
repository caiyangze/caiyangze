<template>
  <view class="test-container">
    <view class="header">
      <text class="title">简单相册测试</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">基础信息</text>
      <text class="info">Base URL: {{ baseUrl }}</text>
      <text class="info">Token: {{ token ? '已登录' : '未登录' }}</text>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试按钮</text>
      <button class="test-btn" @tap="testGetList">获取相册列表</button>
      <button class="test-btn" @tap="testUpload">上传照片</button>
    </view>
    
    <view class="test-section">
      <text class="section-title">测试结果</text>
      <text class="result">{{ result }}</text>
    </view>
    
    <view class="test-section" v-if="photos.length > 0">
      <text class="section-title">照片列表</text>
      <view class="photo-list">
        <view class="photo-item" v-for="photo in photos" :key="photo.photoId">
          <image :src="photo.photoUrl" class="photo-img" mode="aspectFill" />
          <text class="photo-desc">{{ photo.photoDesc || '无描述' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const baseUrl = ref('http://localhost:9001')
const token = ref('')
const result = ref('等待测试...')
const photos = ref([])

onMounted(() => {
  token.value = uni.getStorageSync('token') || ''
})

// 测试获取相册列表
function testGetList() {
  result.value = '正在获取相册列表...'
  
  uni.request({
    url: `${baseUrl.value}/user/photo/list`,
    method: 'GET',
    data: {
      pageNum: 1,
      pageSize: 10
    },
    header: {
      'token': token.value,
      'Content-Type': 'application/json'
    },
    success: (res) => {
      console.log('API响应:', res)
      if (res.data && res.data.code === 200) {
        photos.value = res.data.data.records || []
        result.value = `获取成功，共${photos.value.length}张照片`
      } else {
        result.value = `获取失败：${res.data?.message || '未知错误'}`
      }
    },
    fail: (err) => {
      console.error('请求失败:', err)
      result.value = `请求失败：${err.errMsg || JSON.stringify(err)}`
    }
  })
}

// 测试上传照片
function testUpload() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const filePath = res.tempFilePaths[0]
      result.value = '正在上传照片...'
      
      uni.uploadFile({
        url: `${baseUrl.value}/user/photo/upload`,
        filePath: filePath,
        name: 'file',
        formData: {
          photoDesc: '测试照片',
          isPublic: 1
        },
        header: {
          'token': token.value
        },
        success: (uploadRes) => {
          console.log('上传响应:', uploadRes)
          try {
            const data = JSON.parse(uploadRes.data)
            if (data.code === 200) {
              result.value = '上传成功！'
              // 重新获取列表
              testGetList()
            } else {
              result.value = `上传失败：${data.message}`
            }
          } catch (e) {
            result.value = `解析响应失败：${e.message}`
          }
        },
        fail: (err) => {
          console.error('上传失败:', err)
          result.value = `上传失败：${err.errMsg || JSON.stringify(err)}`
        }
      })
    },
    fail: (err) => {
      result.value = `选择图片失败：${err.errMsg}`
    }
  })
}
</script>

<style lang="scss" scoped>
.test-container {
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
  
  .info {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 10rpx;
  }
  
  .test-btn {
    background: #007aff;
    color: white;
    border: none;
    padding: 20rpx 40rpx;
    border-radius: 10rpx;
    font-size: 28rpx;
    margin-bottom: 20rpx;
    margin-right: 20rpx;
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
}

.photo-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.photo-item {
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  overflow: hidden;
  
  .photo-img {
    width: 100%;
    height: 200rpx;
  }
  
  .photo-desc {
    display: block;
    padding: 10rpx;
    font-size: 24rpx;
    color: #666;
  }
}
</style>
