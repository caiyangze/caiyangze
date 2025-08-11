<template>
  <view class="album-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
      <view class="overlay-gradient"></view>
    </view>

    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="nav-left" @tap="goBack">
          <text class="nav-icon">←</text>
        </view>
        <view class="nav-center">
          <text class="nav-title">我的相册</text>
        </view>
        <view class="nav-right">
          <view v-if="!isDragMode" class="nav-btn" @tap="toggleDragMode">
            <text class="nav-icon">⋮⋮</text>
          </view>
          <view v-if="!isDragMode" class="nav-btn" @tap="addPhotos">
            <text class="nav-icon">+</text>
          </view>
          <view v-if="isDragMode" class="nav-btn done-btn" @tap="finishDragMode">
            <text class="nav-text">完成</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view
        scroll-y
        class="scroll-container"
        :style="{ paddingTop: statusBarHeight + 88 + 'px' }"
        @scrolltolower="loadMore"
    >
      <!-- 加载状态 -->
      <view v-if="loading && photos.length === 0" class="loading-container">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>



      <!-- 相册网格 -->
      <view v-else-if="photos.length > 0" class="photo-grid">
        <view class="photo-list">
          <view
              v-for="(photo, index) in photos"
              :key="photo.photoId"
              class="photo-item"
              :class="{ 'is-dragging': draggingIndex === index }"
          >
            <view
                class="photo-container"
                @tap="handlePhotoTap(photo, index)"
                @touchstart="onTouchStart($event, index)"
                @touchmove="onTouchMove($event, index)"
                @touchend="onTouchEnd($event, index)"
                @longpress="onLongPress($event, index)"
            >
              <image
                  :src="photo.photoUrl"
                  mode="aspectFill"
                  class="photo-image"
              />

              <!-- 拖拽模式下的排序号 -->
              <view v-if="isDragMode" class="drag-order">
                <text class="drag-order-text">{{ index + 1 }}</text>
              </view>

              <!-- 头像标识 -->
              <view v-if="photo.isAvatar === 1" class="avatar-badge">
                <text class="avatar-text">头像</text>
              </view>

              <!-- 隐私快速切换按钮 -->
              <view class="privacy-toggle" @tap.stop="togglePrivacy(photo, index)">
                <text class="privacy-icon">{{ photo.isPublic === 1 ? '🌍' : '🔒' }}</text>
              </view>

              <!-- 更多操作按钮 -->
              <view class="more-actions" @tap.stop="showPhotoActions(photo, index)">
                <text class="more-icon">⋯</text>
              </view>

              <!-- 照片描述 -->
              <view v-if="photo.photoDesc" class="photo-desc-overlay">
                <text class="photo-desc-text">{{ photo.photoDesc }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <text class="empty-icon">📷</text>
        <text class="empty-text">还没有照片</text>
        <text class="empty-desc">点击右上角"+"添加照片</text>
        <view class="empty-button" @tap="addPhotos">
          <text class="button-text">添加照片</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore && photos.length > 0" class="load-more">
        <view v-if="loadingMore" class="loading-more">
          <view class="loading-spinner-small"></view>
          <text class="loading-text-small">加载中...</text>
        </view>
        <text v-else class="load-more-text">上拉加载更多</text>
      </view>

      <!-- 没有更多 -->
      <view v-if="!hasMore && photos.length > 0" class="no-more">
        <text class="no-more-text">没有更多照片了</text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 上传进度弹窗 -->
    <view v-if="uploading" class="upload-modal">
      <view class="upload-content">
        <view class="upload-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: uploadProgress + '%' }"></view>
          </view>
          <text class="progress-text">{{ uploadProgress }}%</text>
        </view>
        <text class="upload-text">正在上传照片...</text>
      </view>
    </view>

    <!-- 上传照片弹窗 -->
    <view v-if="showUploadModal" class="modern-modal" @tap="closeUploadModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <view class="header-icon">📸</view>
          <text class="modal-title">上传照片</text>
          <text class="photo-count">{{ uploadForm.tempFilePaths.length }}张</text>
        </view>

        <view class="modal-body">
          <view class="form-section">
            <view class="section-title">
              <text class="title-icon">✏️</text>
              <text class="title-text">添加描述</text>
            </view>
            <view class="textarea-container">
              <textarea
                v-model="uploadForm.photoDesc"
                class="modern-textarea"
                placeholder="分享这一刻的心情..."
                maxlength="200"
                auto-height
              />
              <text class="char-counter">{{ uploadForm.photoDesc.length }}/200</text>
            </view>
          </view>

          <view class="form-section">
            <view class="section-title">
              <text class="title-icon">🔐</text>
              <text class="title-text">隐私设置</text>
            </view>
            <view class="privacy-selector">
              <view
                class="privacy-card"
                :class="{ selected: uploadForm.isPublic === 1 }"
                @tap="uploadForm.isPublic = 1"
              >
                <view class="card-icon">🌍</view>
                <view class="card-content">
                  <text class="card-title">公开</text>
                  <text class="card-desc">所有人可见</text>
                </view>
                <view class="card-check" v-if="uploadForm.isPublic === 1">✓</view>
              </view>
              <view
                class="privacy-card"
                :class="{ selected: uploadForm.isPublic === 0 }"
                @tap="uploadForm.isPublic = 0"
              >
                <view class="card-icon">🔒</view>
                <view class="card-content">
                  <text class="card-title">私密</text>
                  <text class="card-desc">仅自己可见</text>
                </view>
                <view class="card-check" v-if="uploadForm.isPublic === 0">✓</view>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="modern-btn cancel" @tap="closeUploadModal">取消</button>
          <button class="modern-btn primary" @tap="confirmUpload">
            <text class="btn-text">上传</text>
            <text class="btn-icon">🚀</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 编辑照片弹窗 -->
    <view v-if="showEditModal" class="modern-modal" @tap="closeEditModal">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <view class="header-icon">✏️</view>
          <text class="modal-title">编辑照片</text>
        </view>

        <view class="modal-body">
          <view class="form-section">
            <view class="section-title">
              <text class="title-icon">📝</text>
              <text class="title-text">修改描述</text>
            </view>
            <view class="textarea-container">
              <textarea
                v-model="editForm.photoDesc"
                class="modern-textarea"
                placeholder="为这张照片添加描述..."
                maxlength="200"
                auto-height
              />
              <text class="char-counter">{{ editForm.photoDesc.length }}/200</text>
            </view>
          </view>

          <view class="form-section">
            <view class="section-title">
              <text class="title-icon">🔐</text>
              <text class="title-text">隐私设置</text>
            </view>
            <view class="privacy-selector">
              <view
                class="privacy-card"
                :class="{ selected: editForm.isPublic === 1 }"
                @tap="editForm.isPublic = 1"
              >
                <view class="card-icon">🌍</view>
                <view class="card-content">
                  <text class="card-title">公开</text>
                  <text class="card-desc">所有人可见</text>
                </view>
                <view class="card-check" v-if="editForm.isPublic === 1">✓</view>
              </view>
              <view
                class="privacy-card"
                :class="{ selected: editForm.isPublic === 0 }"
                @tap="editForm.isPublic = 0"
              >
                <view class="card-icon">🔒</view>
                <view class="card-content">
                  <text class="card-title">私密</text>
                  <text class="card-desc">仅自己可见</text>
                </view>
                <view class="card-check" v-if="editForm.isPublic === 0">✓</view>
              </view>
            </view>
          </view>
        </view>

        <view class="modal-footer">
          <button class="modern-btn cancel" @tap="closeEditModal">取消</button>
          <button class="modern-btn primary" @tap="confirmEdit">
            <text class="btn-text">保存</text>
            <text class="btn-icon">💾</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
// import { getPhotoList, uploadPhotos, deletePhoto, setAsAvatar, updatePhoto } from '../../api/user/photo'

// 临时API实现
const BASE_URL = 'http://localhost:9001'

const getPhotoList = async (pageNum = 1, pageSize = 20) => {
  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/list`,
      method: 'GET',
      data: { pageNum, pageSize },
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const updatePhotoSort = async (photoId, sortOrder) => {
  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/updateSort/${photoId}`,
      method: 'PUT',
      data: { sortOrder },
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const updatePhoto = async (photoId, photoDesc, isPublic) => {
  const token = uni.getStorageSync('token') || ''
  const params = {}
  if (photoDesc !== undefined) params.photoDesc = photoDesc
  if (isPublic !== undefined) params.isPublic = isPublic

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/update/${photoId}`,
      method: 'PUT',
      data: params,
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const uploadPhotos = async (filePaths, photoDesc = '', isPublic = 1) => {
  const token = uni.getStorageSync('token') || ''
  const uploadPromises = filePaths.map(filePath => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: `${BASE_URL}/user/photo/upload`,
        filePath: filePath,
        name: 'file',
        formData: {
          photoDesc: photoDesc,
          isPublic: isPublic
        },
        header: {
          'token': token
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            resolve(data)
          } catch (e) {
            reject(e)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  })
  return Promise.all(uploadPromises)
}

const deletePhoto = async (photoId) => {
  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/delete/${photoId}`,
      method: 'DELETE',
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const setAsAvatar = async (photoId) => {
  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/setAvatar/${photoId}`,
      method: 'POST',
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const cancelAvatar = async () => {
  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}/user/photo/cancelAvatar`,
      method: 'POST',
      header: {
        'token': token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

// 页面数据
const statusBarHeight = ref(0)
const photos = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 上传状态
const uploading = ref(false)
const uploadProgress = ref(0)

// 弹窗状态
const showUploadModal = ref(false)
const showEditModal = ref(false)

// 拖拽排序状态
const isDragMode = ref(false)
const draggingIndex = ref(-1)
const dragStartPosition = ref({ x: 0, y: 0 })
const originalPhotos = ref([])

// 长按和触摸状态
const longPressTimer = ref(null)
const touchStartTime = ref(0)
const touchStartPosition = ref({ x: 0, y: 0 })
const isLongPressing = ref(false)
const isDragging = ref(false)
const LONG_PRESS_DELAY = 1500 // 1.5秒长按延迟

// 拖拽相关状态
const dragOverIndex = ref(-1) // 当前悬停的目标位置
const photoElements = ref([]) // 存储照片元素的位置信息

// 上传表单
const uploadForm = reactive({
  photoDesc: '',
  isPublic: 1,
  tempFilePaths: []
})

// 编辑表单
const editForm = reactive({
  photoId: null,
  photoDesc: '',
  isPublic: 1,
  photoIndex: -1
})

// 页面初始化
onMounted(() => {
  // 获取系统状态栏高度
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight

  // 加载相册数据
  loadPhotos()
})

// 页面卸载时清理定时器
onUnmounted(() => {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
})

// 加载相册数据
async function loadPhotos(refresh = false) {
  try {
    if (refresh) {
      currentPage.value = 1
      hasMore.value = true
    }
    
    loading.value = refresh || photos.value.length === 0
    
    const result = await getPhotoList(currentPage.value, pageSize.value)
    
    if (result.code === 200) {
      const newPhotos = result.data.records || []

      if (refresh) {
        photos.value = newPhotos
      } else {
        photos.value = [...photos.value, ...newPhotos]
      }
      
      // 判断是否还有更多数据
      hasMore.value = newPhotos.length === pageSize.value
      
      if (hasMore.value) {
        currentPage.value++
      }
      
      console.log('相册数据加载成功:', photos.value)
    } else {
      uni.showToast({
        title: result.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载相册数据失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
function loadMore() {
  if (hasMore.value && !loadingMore.value) {
    loadingMore.value = true
    loadPhotos()
  }
}

// 返回上一页
function goBack() {
  uni.navigateBack()
}

// 切换拖拽模式
function toggleDragMode() {
  isDragMode.value = true
  originalPhotos.value = [...photos.value]
  uni.showToast({
    title: '长按1.5秒后拖动排序',
    icon: 'none',
    duration: 3000
  })
}

// 完成拖拽排序
async function finishDragMode() {
  try {
    uni.showLoading({ title: '保存排序...' })

    // 更新每张照片的排序
    const updatePromises = photos.value.map((photo, index) => {
      return updatePhotoSort(photo.photoId, index + 1)
    })

    await Promise.all(updatePromises)

    isDragMode.value = false
    draggingIndex.value = -1
    isDragging.value = false
    isLongPressing.value = false

    // 清除可能存在的定时器
    if (longPressTimer.value) {
      clearTimeout(longPressTimer.value)
      longPressTimer.value = null
    }

    uni.showToast({
      title: '排序已保存',
      icon: 'success'
    })
  } catch (error) {
    console.error('保存排序失败:', error)
    // 恢复原始顺序
    photos.value = [...originalPhotos.value]
    uni.showToast({
      title: '保存失败，已恢复',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 触摸开始
function onTouchStart(event, index) {
  if (!isDragMode.value) return

  touchStartTime.value = Date.now()
  touchStartPosition.value = {
    x: event.touches[0].clientX,
    y: event.touches[0].clientY
  }

  // 设置长按定时器
  longPressTimer.value = setTimeout(() => {
    isLongPressing.value = true
    isDragging.value = true
    draggingIndex.value = index

    // 触觉反馈
    uni.vibrateShort({
      type: 'heavy'
    })

    uni.showToast({
      title: '开始拖拽',
      icon: 'none',
      duration: 1000
    })
  }, LONG_PRESS_DELAY)
}

// 触摸移动
function onTouchMove(event, index) {
  if (!isDragMode.value) return

  const currentPosition = {
    x: event.touches[0].clientX,
    y: event.touches[0].clientY
  }

  // 计算移动距离
  const deltaX = currentPosition.x - touchStartPosition.value.x
  const deltaY = currentPosition.y - touchStartPosition.value.y
  const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY)

  // 如果移动距离超过阈值，取消长按
  if (distance > 20 && !isLongPressing.value) {
    clearTimeout(longPressTimer.value)
  }

  // 如果正在拖拽，处理拖拽逻辑
  if (isDragging.value && draggingIndex.value === index) {
    // 阻止默认滚动行为
    event.preventDefault()

    // 使用简化的拖拽逻辑：基于移动方向和距离
    const moveThreshold = 80 // 移动阈值（像素）

    if (Math.abs(deltaY) > moveThreshold) {
      const direction = deltaY > 0 ? 1 : -1
      const targetIndex = draggingIndex.value + direction

      if (targetIndex >= 0 && targetIndex < photos.value.length) {
        // 重新排列
        reorderPhotos(draggingIndex.value, targetIndex)
        draggingIndex.value = targetIndex

        // 重置起始位置，避免连续触发
        touchStartPosition.value = currentPosition

        // 触觉反馈
        uni.vibrateShort({
          type: 'light'
        })
      }
    }
  }
}

// 触摸结束
function onTouchEnd(event, index) {
  if (!isDragMode.value) return

  // 清除长按定时器
  clearTimeout(longPressTimer.value)

  const touchDuration = Date.now() - touchStartTime.value

  // 如果是短按且没有拖拽，则不做任何操作
  if (touchDuration < LONG_PRESS_DELAY && !isDragging.value) {
    return
  }

  // 结束拖拽
  if (isDragging.value) {
    onDragEnd()
  }

  // 重置状态
  isLongPressing.value = false
  isDragging.value = false
}

// 长按事件（备用）
function onLongPress(event, index) {
  if (!isDragMode.value) return

  // 这个方法作为备用，主要逻辑在 onTouchStart 的定时器中
  console.log('长按触发:', index)
}

// 处理拖拽移动的视觉反馈
function handleDragMove(position, index) {
  // 这里可以添加拖拽过程中的视觉效果
  console.log('拖拽移动:', position, index)
}

// 重新排列照片数组
function reorderPhotos(fromIndex, toIndex) {
  if (fromIndex === toIndex) return

  const newPhotos = [...photos.value]
  const draggedItem = newPhotos.splice(fromIndex, 1)[0]
  newPhotos.splice(toIndex, 0, draggedItem)

  photos.value = newPhotos
}

// 开始拖拽
function onDragStart(index) {
  if (isDragMode.value) {
    draggingIndex.value = index
  }
}

// 结束拖拽
function onDragEnd() {
  if (draggingIndex.value !== -1) {
    uni.showToast({
      title: '拖拽结束',
      icon: 'none',
      duration: 1000
    })
  }

  draggingIndex.value = -1
  isDragging.value = false
  isLongPressing.value = false
}

// 照片移动
function onPhotoMove(e) {
  // 这里可以添加拖拽过程中的逻辑
  console.log('照片移动:', e)
}

// 处理照片点击
function handlePhotoTap(photo, index) {
  if (isDragMode.value) {
    return // 拖拽模式下不响应点击
  }
  previewPhoto(index)
}

// 快速切换隐私
async function togglePrivacy(photo, index) {
  try {
    const newPrivacy = photo.isPublic === 1 ? 0 : 1
    const result = await updatePhoto(photo.photoId, photo.photoDesc, newPrivacy)

    if (result.code === 200) {
      photos.value[index].isPublic = newPrivacy
      uni.showToast({
        title: newPrivacy === 1 ? '已设为公开' : '已设为私密',
        icon: 'success',
        duration: 1500
      })
    } else {
      uni.showToast({
        title: '设置失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('切换隐私失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

// 添加照片
function addPhotos() {
  uni.showActionSheet({
    itemList: ['拍照', '从相册选择'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 拍照
        chooseImage('camera')
      } else if (res.tapIndex === 1) {
        // 从相册选择
        chooseImage('album')
      }
    }
  })
}

// 选择图片
function chooseImage(sourceType) {
  uni.chooseImage({
    count: 9, // 最多选择9张
    sizeType: ['compressed'], // 压缩图
    sourceType: [sourceType],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths
      if (tempFilePaths.length > 0) {
        // 保存临时文件路径并显示上传弹窗
        uploadForm.tempFilePaths = tempFilePaths
        uploadForm.photoDesc = ''
        uploadForm.isPublic = 1
        showUploadModal.value = true
      }
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

// 关闭上传弹窗
function closeUploadModal() {
  showUploadModal.value = false
  uploadForm.tempFilePaths = []
  uploadForm.photoDesc = ''
  uploadForm.isPublic = 1
}

// 确认上传
function confirmUpload() {
  if (uploadForm.tempFilePaths.length > 0) {
    showUploadModal.value = false
    uploadImages(uploadForm.tempFilePaths, uploadForm.photoDesc, uploadForm.isPublic)
  }
}

// 关闭编辑弹窗
function closeEditModal() {
  showEditModal.value = false
  editForm.photoId = null
  editForm.photoDesc = ''
  editForm.isPublic = 1
  editForm.photoIndex = -1
}

// 确认编辑
async function confirmEdit() {
  try {
    uni.showLoading({ title: '保存中...' })

    const result = await updatePhoto(editForm.photoId, editForm.photoDesc, editForm.isPublic)

    if (result.code === 200) {
      // 更新本地数据
      if (editForm.photoIndex >= 0) {
        photos.value[editForm.photoIndex].photoDesc = editForm.photoDesc
        photos.value[editForm.photoIndex].isPublic = editForm.isPublic
      }

      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      closeEditModal()
    } else {
      uni.showToast({
        title: result.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('保存照片信息失败:', error)
    uni.showToast({
      title: '保存失败，请重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 上传图片
async function uploadImages(filePaths, photoDesc = '', isPublic = 1) {
  try {
    uploading.value = true
    uploadProgress.value = 0
    
    const totalFiles = filePaths.length
    let completedFiles = 0
    
    // 逐个上传以显示进度
    for (let i = 0; i < filePaths.length; i++) {
      const filePath = filePaths[i]
      
      try {
        const result = await uploadPhotos([filePath], photoDesc, isPublic)
        
        if (result[0].code === 200) {
          // 上传成功，添加到相册列表
          photos.value.unshift(result[0].data)
          completedFiles++
        } else {
          console.error('上传失败:', result[0].message)
        }
      } catch (error) {
        console.error('上传图片失败:', error)
      }
      
      // 更新进度
      uploadProgress.value = Math.round((completedFiles / totalFiles) * 100)
    }
    
    if (completedFiles > 0) {
      uni.showToast({
        title: `成功上传${completedFiles}张照片`,
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: '上传失败，请重试',
        icon: 'none'
      })
    }
    
  } catch (error) {
    console.error('批量上传失败:', error)
    uni.showToast({
      title: '上传失败，请重试',
      icon: 'none'
    })
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 预览照片
function previewPhoto(index) {
  const urls = photos.value.map(photo => photo.photoUrl)
  uni.previewImage({
    current: index,
    urls: urls
  })
}

// 显示照片操作菜单
function showPhotoActions(photo, index) {
  const actions = []

  // 如果当前照片是头像，显示"取消头像"，否则显示"设为头像"
  if (photo.isAvatar === 1) {
    actions.push('取消头像')
  } else {
    actions.push('设为头像')
  }

  actions.push('编辑信息', '删除照片')

  uni.showActionSheet({
    itemList: actions,
    success: (res) => {
      if (res.tapIndex === 0) {
        if (photo.isAvatar === 1) {
          // 取消头像
          cancelPhotoAvatar(photo)
        } else {
          // 设为头像
          setPhotoAsAvatar(photo)
        }
      } else if (res.tapIndex === 1) {
        // 编辑信息
        editPhotoInfo(photo, index)
      } else if (res.tapIndex === 2) {
        // 删除照片
        confirmDeletePhoto(photo, index)
      }
    }
  })
}

// 设置为头像
async function setPhotoAsAvatar(photo) {
  try {
    uni.showLoading({ title: '设置中...' })

    const result = await setAsAvatar(photo.photoId)

    if (result.code === 200) {
      // 更新本地数据
      photos.value.forEach(p => {
        p.isAvatar = p.photoId === photo.photoId ? 1 : 0
      })

      uni.showToast({
        title: '设置头像成功',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: result.message || '设置失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('设置头像失败:', error)
    uni.showToast({
      title: '设置失败，请重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 取消头像
async function cancelPhotoAvatar(photo) {
  try {
    uni.showLoading({ title: '取消中...' })

    const result = await cancelAvatar()

    if (result.code === 200) {
      // 更新本地数据
      photos.value.forEach(p => {
        p.isAvatar = 0
      })

      uni.showToast({
        title: '取消头像成功',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: result.message || '取消失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('取消头像失败:', error)
    uni.showToast({
      title: '取消失败，请重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 编辑照片信息
function editPhotoInfo(photo, index) {
  editForm.photoId = photo.photoId
  editForm.photoDesc = photo.photoDesc || ''
  editForm.isPublic = photo.isPublic
  editForm.photoIndex = index
  showEditModal.value = true
}

// 确认删除照片
function confirmDeletePhoto(photo, index) {
  uni.showModal({
    title: '删除照片',
    content: '确定要删除这张照片吗？删除后无法恢复。',
    confirmText: '删除',
    cancelText: '取消',
    confirmColor: '#ff4757',
    success: (res) => {
      if (res.confirm) {
        deletePhotoById(photo, index)
      }
    }
  })
}

// 删除照片
async function deletePhotoById(photo, index) {
  try {
    uni.showLoading({ title: '删除中...' })
    
    const result = await deletePhoto(photo.photoId)
    
    if (result.code === 200) {
      // 从列表中移除
      photos.value.splice(index, 1)
      
      uni.showToast({
        title: '删除成功',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: result.message || '删除失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('删除照片失败:', error)
    uni.showToast({
      title: '删除失败，请重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 处理照片加载错误
function handlePhotoError(index, event) {
  console.error('照片加载失败:', {
    index,
    photo: photos.value[index],
    url: getProxyImageUrl(photos.value[index]),
    event
  })
  if (photos.value[index]) {
    photos.value[index].loadError = true
  }
}

// 处理照片加载成功
function handlePhotoLoad(index, event) {
  console.log('照片加载成功:', {
    index,
    photo: photos.value[index],
    url: getProxyImageUrl(photos.value[index]),
    event
  })
  if (photos.value[index]) {
    photos.value[index].loaded = true
    photos.value[index].loadError = false
  }
}

// 获取代理图片URL
function getProxyImageUrl(photo) {
  if (!photo) return ''

  // 临时测试：先尝试使用原始URL看看能否显示
  if (photo.photoUrl) {
    console.log('测试使用原始URL:', photo.photoUrl)
    return photo.photoUrl
  }

  // 如果没有原始URL，使用代理接口
  const baseUrl = 'http://localhost:9001'
  const proxyUrl = `${baseUrl}/user/photo/proxy/${photo.photoId}`
  console.log('使用代理URL:', proxyUrl)
  return proxyUrl
}
</script>

<style lang="scss" scoped>
.album-page {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
  background-size: 300% 300%;
  animation: gradientAnimation 15s ease infinite;
  position: relative;
  overflow-x: hidden;

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

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1;

  .bg-gradient {
    width: 100%;
    height: 100%;
    background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
    background-size: 300% 300%;
    animation: gradientAnimation 15s ease infinite;
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

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  z-index: 1000;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);

  .navbar-content {
    height: 96rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32rpx;
    width: 100%;
    box-sizing: border-box;

    .nav-left {
      width: 68rpx;
      height: 68rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 34rpx;
      background: rgba(255, 255, 255, 0.15);
      border: 1rpx solid rgba(255, 255, 255, 0.3);
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        background: rgba(255, 255, 255, 0.25);
      }

      .nav-icon {
        font-size: 36rpx;
        color: rgba(255, 255, 255, 0.95);
        font-weight: 600;
      }
    }

    .nav-right {
      display: flex;
      gap: 16rpx;

      .nav-btn {
        width: 68rpx;
        height: 68rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 34rpx;
        background: rgba(255, 255, 255, 0.15);
        border: 1rpx solid rgba(255, 255, 255, 0.3);
        transition: all 0.3s ease;

        &:active {
          transform: scale(0.95);
          background: rgba(255, 255, 255, 0.25);
        }

        &.done-btn {
          width: auto;
          padding: 0 24rpx;
          background: rgba(255, 255, 255, 0.9);

          .nav-text {
            font-size: 28rpx;
            color: #333;
            font-weight: 600;
          }
        }

        .nav-icon {
          font-size: 36rpx;
          color: rgba(255, 255, 255, 0.95);
          font-weight: 600;
        }
      }
    }

    .nav-center {
      flex: 1;
      text-align: center;
      padding: 0 20rpx;

      .nav-title {
        font-size: 34rpx;
        font-weight: 700;
        color: white;
        letter-spacing: 0.8rpx;
      }
    }
  }
}

.scroll-container {
  width: 100vw;
  height: 100vh;
  background: transparent;
  box-sizing: border-box;
  position: relative;
  z-index: 2;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400rpx;

  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    border-top: 4rpx solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 24rpx;
  }

  .loading-text {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.photo-grid {
  padding: 24rpx 16rpx;

  .photo-list {
    width: 100%;
    min-height: 600rpx;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 12rpx;
  }

  .photo-item {
    width: calc(33.333% - 8rpx);
    height: 200rpx;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    border-radius: 20rpx;

    &.is-dragging {
      transform: scale(1.05);
      z-index: 999;
      box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.3);
      opacity: 0.9;

      .photo-image {
        border: 4rpx solid #007AFF;
      }
    }

    .photo-container {
      width: 100%;
      height: 100%;
      position: relative;
      border-radius: 20rpx;
      overflow: hidden;
      background: transparent;
      border: 2rpx solid rgba(255, 255, 255, 0.2);
      box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);

      .photo-image {
        width: 100%;
        height: 100%;
        border-radius: 18rpx;
        display: block;
        opacity: 1;
        z-index: 1;
        object-fit: cover;
      }

      .drag-order {
        position: absolute;
        top: 8rpx;
        left: 8rpx;
        width: 48rpx;
        height: 48rpx;
        background: rgba(0, 0, 0, 0.8);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10;

        .drag-order-text {
          font-size: 24rpx;
          color: white;
          font-weight: 600;
        }
      }

      .avatar-badge {
        position: absolute;
        top: 8rpx;
        left: 8rpx;
        background: rgba(0, 0, 0, 0.7);
        backdrop-filter: blur(10px);
        padding: 4rpx 12rpx;
        border-radius: 16rpx;
        border: 2rpx solid rgba(255, 255, 255, 0.3);
        z-index: 15;

        .avatar-text {
          font-size: 20rpx;
          color: white;
          font-weight: 500;
        }
      }

      .privacy-toggle {
        position: absolute;
        top: 8rpx;
        right: 8rpx;
        width: 48rpx;
        height: 48rpx;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(10px);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        z-index: 15;

        &:active {
          transform: scale(0.9);
        }

        .privacy-icon {
          font-size: 24rpx;
        }
      }

      .more-actions {
        position: absolute;
        bottom: 8rpx;
        right: 8rpx;
        width: 48rpx;
        height: 48rpx;
        background: rgba(0, 0, 0, 0.6);
        backdrop-filter: blur(10px);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        z-index: 15;

        &:active {
          transform: scale(0.9);
        }

        .more-icon {
          font-size: 28rpx;
          color: white;
          font-weight: 600;
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
  text-align: center;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 32rpx;
    opacity: 0.6;
  }

  .empty-text {
    font-size: 32rpx;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 600;
    margin-bottom: 16rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 40rpx;
    line-height: 1.5;
  }

  .empty-button {
    background: rgba(255, 255, 255, 0.2);
    border: 1rpx solid rgba(255, 255, 255, 0.3);
    border-radius: 40rpx;
    padding: 20rpx 40rpx;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.95);
      background: rgba(255, 255, 255, 0.3);
    }

    .button-text {
      font-size: 28rpx;
      color: white;
      font-weight: 600;
    }
  }
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 20rpx;

  .loading-more {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .loading-spinner-small {
      width: 32rpx;
      height: 32rpx;
      border: 2rpx solid rgba(255, 255, 255, 0.3);
      border-top: 2rpx solid white;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    .loading-text-small {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }

  .load-more-text {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.6);
  }
}

.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 20rpx;

  .no-more-text {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.6);
  }
}

.bottom-placeholder {
  height: 80rpx;
}

.upload-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;

  .upload-content {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20rpx;
    padding: 60rpx 40rpx;
    width: 500rpx;
    text-align: center;
    backdrop-filter: blur(20px);

    .upload-progress {
      margin-bottom: 32rpx;

      .progress-bar {
        width: 100%;
        height: 8rpx;
        background: rgba(0, 0, 0, 0.1);
        border-radius: 4rpx;
        overflow: hidden;
        margin-bottom: 16rpx;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #12C2E9, #C471ED);
          border-radius: 4rpx;
          transition: width 0.3s ease;
        }
      }

      .progress-text {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
      }
    }

    .upload-text {
      font-size: 30rpx;
      color: #666;
      font-weight: 500;
    }
  }
}

/* 照片描述样式 */
.photo-desc-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 24rpx 16rpx 16rpx;
  border-radius: 0 0 18rpx 18rpx;
  z-index: 10;

  .photo-desc-text {
    font-size: 22rpx;
    color: white;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
  }
}

/* 现代化弹窗样式 */
.modern-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.3s ease;

  .modal-content {
    width: 680rpx;
    max-height: 80vh;
    background: white;
    border-radius: 32rpx;
    overflow: hidden;
    box-shadow: 0 32rpx 64rpx rgba(0, 0, 0, 0.2);
    animation: slideUp 0.3s ease;

    .modal-header {
      background: linear-gradient(135deg, #667eea, #764ba2);
      padding: 40rpx 32rpx;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .header-icon {
        font-size: 48rpx;
        margin-right: 16rpx;
      }

      .modal-title {
        font-size: 36rpx;
        font-weight: 700;
        color: white;
        flex: 1;
      }

      .photo-count {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
        background: rgba(255, 255, 255, 0.2);
        padding: 8rpx 16rpx;
        border-radius: 20rpx;
      }
    }

    .modal-body {
      padding: 32rpx;
      max-height: 60vh;
      overflow-y: auto;

      .form-section {
        margin-bottom: 40rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          display: flex;
          align-items: center;
          margin-bottom: 20rpx;

          .title-icon {
            font-size: 32rpx;
            margin-right: 12rpx;
          }

          .title-text {
            font-size: 30rpx;
            font-weight: 600;
            color: #333;
          }
        }

        .textarea-container {
          position: relative;

          .modern-textarea {
            width: 100%;
            min-height: 160rpx;
            padding: 24rpx;
            border: 2rpx solid #f0f0f0;
            border-radius: 20rpx;
            font-size: 28rpx;
            color: #333;
            background: #fafafa;
            box-sizing: border-box;
            transition: all 0.3s ease;

            &:focus {
              border-color: #667eea;
              background: white;
              box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1);
            }
          }

          .char-counter {
            position: absolute;
            bottom: 12rpx;
            right: 20rpx;
            font-size: 24rpx;
            color: #999;
            background: rgba(255, 255, 255, 0.9);
            padding: 4rpx 8rpx;
            border-radius: 8rpx;
          }
        }

        .privacy-selector {
          display: flex;
          gap: 16rpx;

          .privacy-card {
            flex: 1;
            padding: 24rpx;
            border: 2rpx solid #f0f0f0;
            border-radius: 20rpx;
            background: #fafafa;
            transition: all 0.3s ease;
            position: relative;

            &.selected {
              border-color: #667eea;
              background: rgba(102, 126, 234, 0.05);
              transform: translateY(-4rpx);
              box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.2);
            }

            .card-icon {
              font-size: 40rpx;
              text-align: center;
              margin-bottom: 12rpx;
            }

            .card-content {
              text-align: center;

              .card-title {
                display: block;
                font-size: 28rpx;
                font-weight: 600;
                color: #333;
                margin-bottom: 8rpx;
              }

              .card-desc {
                font-size: 24rpx;
                color: #666;
              }
            }

            .card-check {
              position: absolute;
              top: 12rpx;
              right: 12rpx;
              width: 32rpx;
              height: 32rpx;
              background: #667eea;
              color: white;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 20rpx;
              font-weight: 600;
            }
          }
        }
      }
    }

    .modal-footer {
      padding: 32rpx;
      background: #fafafa;
      display: flex;
      gap: 20rpx;

      .modern-btn {
        flex: 1;
        height: 96rpx;
        border-radius: 24rpx;
        font-size: 30rpx;
        font-weight: 600;
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;

        &:active {
          transform: scale(0.98);
        }

        &.cancel {
          background: #f5f5f5;
          color: #666;

          &:active {
            background: #e8e8e8;
          }
        }

        &.primary {
          background: linear-gradient(135deg, #667eea, #764ba2);
          color: white;
          box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

          .btn-text {
            margin-right: 8rpx;
          }

          .btn-icon {
            font-size: 24rpx;
          }

          &:active {
            box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.4);
          }
        }
      }
    }
  }
}

/* 弹窗动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.debug-status {
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 20rpx;
  margin: 20rpx;
  border-radius: 10rpx;
  font-size: 24rpx;

  text {
    display: block;
    margin-bottom: 10rpx;
    word-break: break-all;
  }
}

.temp-debug {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 0, 0, 0.8);
  color: white;
  padding: 10rpx;
  font-size: 20rpx;

  text {
    word-break: break-all;
  }
}

/* 编辑弹窗样式 - 复用现代化弹窗样式 */
</style>
