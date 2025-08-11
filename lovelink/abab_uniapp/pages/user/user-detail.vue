<template>
  <view class="user-detail-page" :style="{ background: computedBg }">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 主要内容 -->
    <view v-else class="main-wrapper">

      <!-- 自定义导航栏 -->
      <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="navbar-content">
          <view class="nav-left" @tap="goBack">
            <text class="nav-icon">←</text>
          </view>
          <view class="nav-center">
            <text class="nav-title">{{ (userInfo && userInfo.nickname) || '用户详情' }}</text>
          </view>
          <view class="nav-right" @tap="showMoreActions">
            <text class="nav-icon">⋯</text>
          </view>
        </view>
      </view>

      <!-- 滚动内容 -->
      <scroll-view
          scroll-y
          class="scroll-container"
          :style="{ paddingTop: statusBarHeight + 88 + 'px' }"
      >
        <!-- 用户资料区域 -->
        <view class="user-profile">
          <!-- 头像区域 -->
          <view class="avatar-container">
            <image
                :src="userInfo.avatarUrl || '/static/common/default-avatar.png'"
                mode="aspectFill"
                class="user-avatar"
                @error="handleImageError"
            />
            <view v-if="userInfo.isOnline" class="online-indicator"></view>
            <view class="badges">
              <view v-if="userInfo.isVip === 1" class="vip-badge">VIP</view>
              <view v-if="userInfo.userRole === 2" class="matchmaker-badge">红娘</view>
              <view v-if="userInfo.isVerified === 1" class="verified-badge">✓</view>
            </view>
          </view>

          <!-- 用户名 -->
          <view class="username-section">
            <text class="user-name">{{ userInfo.nickname || '用户' }}</text>
          </view>



          <!-- 性别和年龄信息 -->
          <view class="gender-age-section">
            <text class="gender-age-text">{{ getGenderAndAge() }}</text>
          </view>

          <!-- 统计数据 -->
          <view class="stats-row">
            <view class="stat-item">
              <text class="stat-count">{{ userInfo.countFollow || 0 }}</text>
              <text class="stat-label">关注</text>
            </view>
            <view class="stat-item">
              <text class="stat-count">{{ userInfo.fan || 0 }}</text>
              <text class="stat-label">粉丝</text>
            </view>
            <view class="stat-item">
              <text class="stat-count">{{ userInfo.countLike || 0 }}</text>
              <text class="stat-label">获赞</text>
            </view>
          </view>

          <!-- 个人简介 -->
          <view class="bio-section" v-if="userProfile.selfIntroduction">
            <text class="bio-text">{{ userProfile.selfIntroduction }}</text>
          </view>

          <!-- 关注按钮 -->
          <view class="follow-button-container">
            <view
              class="follow-button"
              :class="{ followed: userInfo.isFollowed }"
              @tap="toggleFollow"
            >
              <text class="follow-text">{{ userInfo.isFollowed ? '已关注' : '关注' }}</text>
            </view>
          </view>
        </view>

        <!-- 标签展示 -->
        <view class="tags-section" v-if="userTags.length > 0">
          <view class="tag-item" v-for="tag in userTags" :key="tag.id">
            <text class="tag-text">{{ tag.tagName }}</text>
          </view>
        </view>

        <!-- 标签页切换 -->
        <view class="tab-bar">
          <view
              class="tab-item"
              :class="{ active: currentTab === 'photos' }"
              @tap="switchTab('photos')"
          >
            <text class="tab-text">相册</text>
          </view>
          <view
              class="tab-item"
              :class="{ active: currentTab === 'moments' }"
              @tap="switchTab('moments')"
          >
            <text class="tab-text">动态</text>
          </view>
          <view
              class="tab-item"
              :class="{ active: currentTab === 'info' }"
              @tap="switchTab('info')"
          >
            <text class="tab-text">资料</text>
          </view>
        </view>

        <!-- 内容区域 -->
        <view class="content-container">
          <!-- 相册内容 -->
          <view v-if="currentTab === 'photos'" class="photos-content">
            <view v-if="userPhotos.length > 0" class="photo-grid">
              <view
                  class="photo-item"
                  v-for="(photo, index) in userPhotos"
                  :key="photo.photoId"
                  @tap="previewPhoto(index)"
              >
                <image
                    :src="photo.photoUrl"
                    mode="aspectFill"
                    class="photo-image"
                    @error="handlePhotoError(index)"
                />
              </view>
            </view>
            <view v-else class="empty-state">
              <text class="empty-icon">📷</text>
              <text class="empty-text">暂无照片</text>
            </view>
          </view>

          <!-- 动态内容 -->
          <view v-if="currentTab === 'moments'" class="moments-content">
            <view v-if="userMoments.length > 0" class="moment-list">
              <view class="moment-item" v-for="moment in userMoments" :key="moment.momentId">
                <view class="moment-header">
                  <view class="moment-info">
                    <text class="moment-time">{{ formatTime(moment.createdAt) }}</text>
                    <text v-if="moment.location" class="moment-location">📍 {{ moment.location }}</text>
                  </view>
                </view>

                <view class="moment-content">
                  <text class="moment-text">{{ moment.content }}</text>
                </view>

                <view v-if="moment.mediaList && moment.mediaList.length > 0" class="moment-media">
                  <view
                      class="media-grid"
                      :class="getMediaGridClass(moment.mediaList.length)"
                  >
                    <view
                        class="media-item"
                        v-for="(media, index) in moment.mediaList"
                        :key="media.mediaId"
                        @tap="previewMomentMedia(moment.mediaList, index)"
                    >
                      <image
                          :src="media.mediaUrl"
                          mode="aspectFill"
                          class="media-image"
                      />
                    </view>
                  </view>
                </view>

                <view class="moment-actions">
                  <view class="action-item" @tap="toggleMomentLike(moment)">
                    <text class="action-icon" :class="{ liked: moment.isLiked }">
                      {{ moment.isLiked ? '❤️' : '🤍' }}
                    </text>
                    <text class="action-count">{{ moment.likeCount || 0 }}</text>
                  </view>
                  <view class="action-item" @tap="viewMomentComments(moment)">
                    <text class="action-icon">💬</text>
                    <text class="action-count">{{ moment.commentCount || 0 }}</text>
                  </view>
                  <view class="action-item">
                    <text class="action-icon">👁</text>
                    <text class="action-count">{{ moment.viewCount || 0 }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view v-else class="empty-state">
              <text class="empty-icon">📝</text>
              <text class="empty-text">暂无动态</text>
            </view>
          </view>

          <!-- 资料内容 -->
          <view v-if="currentTab === 'info'" class="info-content">
            <!-- 个人介绍 -->
            <view class="info-section" v-if="userProfile.selfIntroduction">
              <view class="section-title">💭 个人介绍</view>
              <view class="intro-text">{{ userProfile.selfIntroduction }}</view>
            </view>

            <!-- 基本资料 -->
            <view class="info-section">
              <view class="section-title">📋 基本资料</view>
              <view class="info-grid">
                <view class="info-item" v-if="userProfile.height">
                  <text class="info-label">身高</text>
                  <text class="info-value">{{ userProfile.height }}cm</text>
                </view>
                <view class="info-item" v-if="userProfile.weight">
                  <text class="info-label">体重</text>
                  <text class="info-value">{{ userProfile.weight }}kg</text>
                </view>
                <view class="info-item" v-if="userProfile.education">
                  <text class="info-label">学历</text>
                  <text class="info-value">{{ getEducationText(userProfile.education) }}</text>
                </view>
                <view class="info-item" v-if="userProfile.company">
                  <text class="info-label">公司</text>
                  <text class="info-value">{{ userProfile.company }}</text>
                </view>
                <view class="info-item" v-if="userProfile.position">
                  <text class="info-label">职位</text>
                  <text class="info-value">{{ userProfile.position }}</text>
                </view>
                <view class="info-item" v-if="userProfile.incomeLevel">
                  <text class="info-label">收入</text>
                  <text class="info-value">{{ getIncomeLevelText(userProfile.incomeLevel) }}</text>
                </view>
                <view class="info-item" v-if="userProfile.maritalStatus">
                  <text class="info-label">婚姻状况</text>
                  <text class="info-value">{{ getMaritalStatusText(userProfile.maritalStatus) }}</text>
                </view>
                <view class="info-item" v-if="userProfile.hasChildren !== null">
                  <text class="info-label">子女</text>
                  <text class="info-value">{{ userProfile.hasChildren ? '有' : '无' }}</text>
                </view>
                <view class="info-item" v-if="userProfile.houseStatus">
                  <text class="info-label">房产</text>
                  <text class="info-value">{{ getHouseStatusText(userProfile.houseStatus) }}</text>
                </view>
                <view class="info-item" v-if="userProfile.carStatus">
                  <text class="info-label">车产</text>
                  <text class="info-value">{{ getCarStatusText(userProfile.carStatus) }}</text>
                </view>
                <view class="info-item" v-if="userProfile.hobby">
                  <text class="info-label">爱好</text>
                  <text class="info-value">{{ userProfile.hobby }}</text>
                </view>
              </view>
            </view>

            <!-- 认证信息 -->
            <view class="info-section" v-if="userInfo.isVerified === 1">
              <view class="section-title">✅ 认证信息</view>
              <view class="cert-item">
                <text class="cert-icon">🆔</text>
                <text class="cert-text">实名认证</text>
                <text class="cert-status verified">已认证</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 底部占位 -->
        <view class="bottom-placeholder"></view>
      </scroll-view>

      <!-- 底部操作按钮 -->
      <view class="bottom-actions">
        <view class="action-btn secondary" @tap="sendMessage">
          <text class="btn-icon">💬</text>
          <text class="btn-text">发消息</text>
        </view>
        <view class="action-btn primary" @tap="requestIntroduction">
          <text class="btn-icon">💕</text>
          <text class="btn-text">申请介绍</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import {
  getUserDetail,
  getUserPhotos,
  getUserMoments,
  toggleUserFollow,
  checkUserFollow
} from '@/api/user/detail'
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js'

// 使用全局主题混入
const { currentBackground } = useGlobalThemeMixin()

// 计算属性
const computedBg = computed(() => {
  return currentBackground.value
})

// 页面数据
const userInfo = ref({
  nickname: '',
  gender: 0,
  avatarUrl: '',
  birthDate: null,
  fan: 0,
  countFollow: 0,
  countLike: 0,
  isVip: 0,
  isVerified: 0,
  isLiked: false,
  isFollowed: false,
  isOnline: false
})
const userProfile = ref({
  age: null,
  workCity: '',
  height: null,
  weight: null,
  education: null,
  company: '',
  position: '',
  incomeLevel: null,
  maritalStatus: null,
  hasChildren: null,
  houseStatus: null,
  carStatus: null,
  selfIntroduction: '',
  hobby: ''
})
const userTags = ref([])
const userPhotos = ref([])
const userMoments = ref([])
const photoErrors = reactive({})
const statusBarHeight = ref(0)
const currentTab = ref('photos')
const loading = ref(false)

// 页面参数
const pageParams = ref({})

// 页面加载
onMounted(() => {
  // 获取系统状态栏高度
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight

  // 从 URL 中获取参数
  const urlParams = new URLSearchParams(window.location.search)
  let userId = urlParams.get('userId') || urlParams.get('id')

  // 如果 URL 参数中没有，尝试从路由参数中获取
  if (!userId) {
    // 尝试从当前路由中获取参数
    try {
      const currentRoute = getCurrentInstance()?.proxy?.$route
      if (currentRoute && currentRoute.query) {
        userId = currentRoute.query.userId || currentRoute.query.id
      }
    } catch (e) {
      console.log('无法获取路由参数:', e)
    }
  }

  // 如果还是没有，使用默认测试用户ID
  if (!userId) {
    userId = '1' // 使用默认用户ID进行测试
    console.warn('未获取到用户ID参数，使用默认用户ID:', userId)
  }

  pageParams.value = { userId }
  console.log('获取到的userId:', userId)

  if (userId) {
    getUserDetailData(userId)
  } else {
    console.error('未获取到用户ID参数')
    uni.showToast({
      title: '用户ID参数错误',
      icon: 'none'
    })
  }
})

// 获取用户详情
async function getUserDetailData(userId) {
  try {
    loading.value = true
    let targetUser = null

    // 使用相亲广场API获取用户详情（支持按用户ID筛选）
    const result = await getUserDetail(userId)
    console.log('getUserDetail 响应:', result)

    if (result.code === 200 && result.data && result.data.records) {
      // 查找指定用户ID的数据
      targetUser = result.data.records.find(user => user.userId == userId)

      if (!targetUser && result.data.records.length > 0) {
        // 如果没找到指定用户，但有数据，取第一个（可能是筛选问题）
        targetUser = result.data.records[0]
        console.warn('未找到指定用户ID的数据，使用第一个用户数据')
      }
    }

    if (targetUser) {
      console.log('获取到的用户数据:', targetUser)
      console.log('用户资料:', targetUser.userProfile)
      console.log('用户标签:', targetUser.userTags)
      console.log('用户点赞数 countLike:', targetUser.countLike)
      console.log('用户粉丝数 fan:', targetUser.fan)
      console.log('用户关注数 countFollow:', targetUser.countFollow)

      // 确保数据库中的统计数据不被覆盖
      userInfo.value = {
        ...targetUser,
        isLiked: targetUser.isLiked || false,
        isFollowed: targetUser.isFollowed || false,
        isOnline: targetUser.isOnline || false,
        // 确保统计数据使用数据库的值
        countLike: targetUser.countLike || 0,
        fan: targetUser.fan || 0,
        countFollow: targetUser.countFollow || 0
      }
      userProfile.value = targetUser.userProfile || {}
      userTags.value = targetUser.userTags || []

      // 加载用户相册、动态和关注状态
      await Promise.all([
        loadUserPhotos(userId),
        loadUserMoments(userId),
        loadFollowStatus(userId)
      ])
    } else {
      throw new Error('未找到用户数据')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    uni.showToast({ title: '加载失败，请重试', icon: 'none' })
    userInfo.value = {}
    userProfile.value = {}
    userPhotos.value = []
    userMoments.value = []
  } finally {
    loading.value = false
  }
}

// 加载用户相册
async function loadUserPhotos(userId) {
  try {
    const result = await getUserPhotos(userId, 1, 20) // 获取前20张照片
    console.log('用户相册响应:', result)

    if (result.code === 200) {
      // 处理分页数据结构
      if (result.data && result.data.records) {
        userPhotos.value = result.data.records || []
      } else if (Array.isArray(result.data)) {
        userPhotos.value = result.data
      } else {
        userPhotos.value = []
      }
      console.log('用户相册数据:', userPhotos.value)
    } else {
      console.warn('获取用户相册失败:', result.message)
      userPhotos.value = []
    }
  } catch (error) {
    console.error('获取用户相册失败:', error)
    userPhotos.value = []
  }
}

// 加载用户动态
async function loadUserMoments(userId) {
  try {
    const result = await getUserMoments(userId, 1, 20) // 获取前20条动态
    if (result.code === 200) {
      // 处理分页数据结构
      if (result.data && result.data.records) {
        userMoments.value = result.data.records || []
      } else {
        userMoments.value = result.data || []
      }
    }
  } catch (error) {
    console.error('获取用户动态失败:', error)
    userMoments.value = []
  }
}

// 加载关注状态
async function loadFollowStatus(targetUserId) {
  try {
    // 检查是否有token（是否已登录）
    const token = getToken()
    if (!token) {
      console.log('用户未登录，跳过关注状态检查')
      userInfo.value.isFollowed = false
      return
    }

    const result = await checkUserFollow(targetUserId)
    console.log('关注状态响应:', result)

    if (result.code === 200) {
      userInfo.value.isFollowed = result.data || false
      console.log('关注状态:', userInfo.value.isFollowed)
    } else if (result.code === 401) {
      console.log('用户未登录或token过期')
      userInfo.value.isFollowed = false
    }
  } catch (error) {
    console.error('获取关注状态失败:', error)
    userInfo.value.isFollowed = false
  }
}

// 计算年龄
function calculateAge(birthDate) {
  if (!birthDate) return '未知'
  try {
    const birth = new Date(birthDate)
    if (isNaN(birth.getTime())) return '未知'

    const today = new Date()
    let age = today.getFullYear() - birth.getFullYear()
    const monthDiff = today.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--
    }
    return age > 0 ? age : '未知'
  } catch (error) {
    console.error('计算年龄失败:', error)
    return '未知'
  }
}

// 获取显示年龄
function getDisplayAge() {
  try {
    if (userProfile.value && userProfile.value.age) {
      return userProfile.value.age
    }
    if (userInfo.value && userInfo.value.birthDate) {
      return calculateAge(userInfo.value.birthDate)
    }
    return '未知'
  } catch (error) {
    console.error('获取年龄失败:', error)
    return '未知'
  }
}

// 获取显示性别
function getDisplayGender() {
  try {
    if (userInfo.value && userInfo.value.gender !== undefined) {
      switch (userInfo.value.gender) {
        case 1:
          return '男'
        case 2:
          return '女'
        default:
          return ''
      }
    }
    return ''
  } catch (error) {
    console.error('获取性别失败:', error)
    return ''
  }
}

// 获取性别图标
function getGenderIcon() {
  try {
    if (userInfo.value && userInfo.value.gender !== undefined) {
      switch (userInfo.value.gender) {
        case 1:
          return '♂'
        case 2:
          return '♀'
        default:
          return ''
      }
    }
    return ''
  } catch (error) {
    console.error('获取性别图标失败:', error)
    return ''
  }
}

// 获取性别和年龄组合显示
function getGenderAndAge() {
  try {
    const gender = getDisplayGender()
    const genderIcon = getGenderIcon()
    const age = getDisplayAge()

    let result = ''

    // 添加性别图标和文字
    if (gender && genderIcon) {
      result += `${genderIcon}`
    }

    // 添加年龄
    if (age && age !== '未知') {
      if (result) {
        result += `${age}岁`
      } else {
        result = `${age}岁`
      }
    }

    return result || '信息未完善'
  } catch (error) {
    console.error('获取性别年龄失败:', error)
    return '信息未完善'
  }
}

// 切换标签页
function switchTab(tab) {
  currentTab.value = tab
}



// 关注/取消关注
async function toggleFollow() {
  try {
    const targetUserId = pageParams.value.userId || pageParams.value.id

    // 检查是否已登录
    const token = getToken()
    if (!token) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      // 可以跳转到登录页面
      // uni.navigateTo({ url: '/pages/login/login' })
      return
    }

    // 如果当前是已关注状态，显示取消关注确认提示
    if (userInfo.value.isFollowed) {
      uni.showModal({
        title: '取消关注',
        content: `确定要取消关注 ${userInfo.value.nickname || '该用户'} 吗？`,
        confirmText: '取消关注',
        cancelText: '我再想想',
        confirmColor: '#ff4757',
        success: async (res) => {
          if (res.confirm) {
            await performFollowAction(targetUserId)
          }
        }
      })
    } else {
      // 直接执行关注操作
      await performFollowAction(targetUserId)
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 执行关注/取消关注操作
async function performFollowAction(targetUserId) {
  try {
    const result = await toggleUserFollow(targetUserId)
    console.log('关注操作响应:', result)

    if (result.code === 200) {
      // 更新关注状态
      userInfo.value.isFollowed = !userInfo.value.isFollowed

      // 更新粉丝数量（本地计算）
      if (userInfo.value.isFollowed) {
        // 关注成功，粉丝数+1
        userInfo.value.fan = (userInfo.value.fan || 0) + 1
      } else {
        // 取消关注，粉丝数-1
        userInfo.value.fan = Math.max((userInfo.value.fan || 0) - 1, 0)
      }

      uni.showToast({
        title: result.message || (userInfo.value.isFollowed ? '关注成功' : '取消关注成功'),
        icon: 'success'
      })

      // 可选：重新获取用户详情以确保数据准确性（推荐开启）
      await refreshUserDetail(targetUserId)

    } else if (result.code === 401) {
      uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
      // 可以跳转到登录页面
      // uni.navigateTo({ url: '/pages/login/login' })
    } else {
      uni.showToast({ title: result.message || '操作失败', icon: 'none' })
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 刷新用户详情数据（可选，确保数据准确性）
async function refreshUserDetail(targetUserId) {
  try {
    console.log('刷新用户详情数据...')
    const result = await getUserDetail(targetUserId)

    if (result.code === 200 && result.data && result.data.records) {
      const targetUser = result.data.records.find(user => user.userId == targetUserId)

      if (targetUser) {
        // 只更新统计数据，保持当前的关注状态
        const currentFollowStatus = userInfo.value.isFollowed

        userInfo.value = {
          ...targetUser,
          isLiked: targetUser.isLiked || false,
          isFollowed: currentFollowStatus, // 保持当前关注状态
          isOnline: targetUser.isOnline || false,
          // 确保统计数据使用数据库的最新值
          countLike: targetUser.countLike || 0,
          fan: targetUser.fan || 0,
          countFollow: targetUser.countFollow || 0
        }

        console.log('用户详情数据刷新成功，最新粉丝数:', userInfo.value.fan)
        console.log('用户详情数据刷新成功，最新点赞数:', userInfo.value.countLike)
      }
    }
  } catch (error) {
    console.error('刷新用户详情失败:', error)
  }
}

// 返回上一页
function goBack() {
  uni.navigateBack()
}

// 显示更多操作
function showMoreActions() {
  uni.showActionSheet({
    itemList: ['举报', '拉黑'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 举报
        uni.showToast({ title: '举报功能开发中', icon: 'none' })
      } else if (res.tapIndex === 1) {
        // 拉黑
        uni.showToast({ title: '拉黑功能开发中', icon: 'none' })
      }
    }
  })
}

// 预览照片
function previewPhoto(index) {
  const urls = userPhotos.value.map(photo => photo.photoUrl)
  uni.previewImage({
    current: index,
    urls: urls
  })
}

// 处理照片加载错误
function handlePhotoError(index) {
  photoErrors[index] = true
}

// 处理头像加载错误
function handleImageError() {
  userInfo.value.avatarUrl = '/static/common/default-avatar.png'
}

// 格式化时间
function formatTime(timestamp) {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return Math.floor(diff / 86400000) + '天前'
}

// 获取媒体网格样式类
function getMediaGridClass(count) {
  if (count === 1) return 'single'
  if (count <= 4) return 'quad'
  return 'nine'
}

// 预览动态媒体
function previewMomentMedia(mediaList, index) {
  const urls = mediaList.map(media => media.mediaUrl)
  uni.previewImage({
    current: index,
    urls: urls
  })
}

// 切换动态点赞
async function toggleMomentLike(moment) {
  try {
    // 这里应该调用动态点赞API
    moment.isLiked = !moment.isLiked
    moment.likeCount = moment.isLiked ? (moment.likeCount || 0) + 1 : Math.max((moment.likeCount || 0) - 1, 0)
  } catch (error) {
    console.error('动态点赞失败:', error)
  }
}

// 查看动态评论
function viewMomentComments() {
  uni.showToast({ title: '评论功能开发中', icon: 'none' })
}

// 发送消息
async function sendMessage() {
  if (!userInfo.value.userId) {
    uni.showToast({
      title: '用户信息错误',
      icon: 'none'
    })
    return
  }

  try {
    // 导入聊天API
    const chatApi = await import('@/api/chat.js')

    // 检查是否可以聊天（是否互相关注）
    const result = await chatApi.canChat(userInfo.value.userId)

    if (result.code === 200) {
      if (result.data === true) {
        // 可以聊天，跳转到聊天页面
        uni.navigateTo({
          url: `/pages/message/chat?userId=${userInfo.value.userId}&name=${encodeURIComponent(userInfo.value.nickname)}&avatar=${encodeURIComponent(userInfo.value.avatarUrl || '/static/message/default-avatar.png')}`
        })
      } else {
        // 不能聊天，提示需要互相关注
        uni.showModal({
          title: '提示',
          content: '需要互相关注才能聊天哦～',
          showCancel: false,
          confirmText: '知道了'
        })
      }
    } else {
      uni.showToast({
        title: result.message || '检查聊天权限失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('检查聊天权限失败:', error)
    uni.showToast({
      title: '网络错误，请重试',
      icon: 'none'
    })
  }
}

// 申请介绍（牵线申请）
function requestIntroduction() {
  // 检查是否已登录
  const token = getToken()
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    // 可以跳转到登录页面
    // uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  // 检查是否是自己
  const targetUserId = pageParams.value.userId || pageParams.value.id
  const currentUserId = getCurrentUserId() // 需要实现获取当前用户ID的方法

  if (targetUserId == currentUserId) {
    uni.showToast({ title: '不能向自己发起牵线申请', icon: 'none' })
    return
  }

  // 跳转到牵线申请页面
  uni.navigateTo({
    url: `/pages/matchmaker/request?targetUserId=${targetUserId}`
  })
}

// 获取学历文本
function getEducationText(education) {
  const educationMap = {
    1: '高中及以下',
    2: '大专',
    3: '本科',
    4: '硕士',
    5: '博士'
  }
  return educationMap[education] || '未知'
}

// 获取收入水平文本
function getIncomeLevelText(level) {
  const levelMap = {
    1: '5K以下',
    2: '5K-10K',
    3: '10K-20K',
    4: '20K-50K',
    5: '50K以上'
  }
  return levelMap[level] || '未知'
}

// 获取婚姻状况文本
function getMaritalStatusText(status) {
  const statusMap = {
    1: '未婚',
    2: '离异',
    3: '丧偶'
  }
  return statusMap[status] || '未知'
}

// 获取房产状况文本
function getHouseStatusText(status) {
  const statusMap = {
    1: '租房',
    2: '有房贷款',
    3: '有房无贷款',
    4: '和家人同住'
  }
  return statusMap[status] || '未知'
}

// 获取车产状况文本
function getCarStatusText(status) {
  const statusMap = {
    1: '无车',
    2: '有车有贷款',
    3: '有车无贷款'
  }
  return statusMap[status] || '未知'
}

// 获取用户token
function getToken() {
  try {
    // 从本地存储获取token
    const token = uni.getStorageSync('token')
    if (token) {
      return token
    }

    // 如果没有token，说明用户未登录
    console.log('未找到用户token，用户可能未登录')
    return null
  } catch (error) {
    console.error('获取token失败:', error)
    return null
  }
}

// 获取当前用户ID
function getCurrentUserId() {
  try {
    const token = getToken()
    if (!token) {
      return null
    }

    // 如果是JWT token，尝试解析
    const parts = token.split('.')
    if (parts.length === 3) {
      const payload = JSON.parse(atob(parts[1]))
      return payload.userId
    }

    // 如果不是JWT格式，尝试从本地存储获取用户信息
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.userId) {
      return userInfo.userId
    }

    return null
  } catch (error) {
    console.error('获取当前用户ID失败:', error)
    return null
  }
}
</script>

<style lang="scss" scoped>
.user-detail-page {
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

.main-wrapper {
  width: 100vw;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  z-index: 2;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg,
    rgba(99, 102, 241, 0.05) 0%,
    rgba(139, 92, 246, 0.03) 25%,
    rgba(236, 72, 153, 0.02) 50%,
    rgba(59, 130, 246, 0.03) 75%,
    rgba(16, 185, 129, 0.05) 100%
  );
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 400rpx;
    height: 400rpx;
    background: radial-gradient(circle, rgba(99, 102, 241, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    animation: pulse 2s ease-in-out infinite;
  }

  .loading-spinner {
    width: 80rpx;
    height: 80rpx;
    border: 6rpx solid rgba(255, 255, 255, 0.3);
    border-top: 6rpx solid #6366f1;
    border-right: 6rpx solid #ec4899;
    border-radius: 50%;
    animation: spin 1.2s cubic-bezier(0.4, 0, 0.2, 1) infinite;
    margin-bottom: 32rpx;
    position: relative;
    z-index: 1;
    box-shadow: 0 8rpx 32rpx rgba(99, 102, 241, 0.2);

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      width: 40rpx;
      height: 40rpx;
      background: linear-gradient(135deg, #6366f1, #ec4899);
      border-radius: 50%;
      transform: translate(-50%, -50%);
      animation: innerSpin 0.8s linear infinite reverse;
    }
  }

  .loading-text {
    font-size: 32rpx;
    color: #64748b;
    font-weight: 600;
    position: relative;
    z-index: 1;
    text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    animation: fadeInOut 2s ease-in-out infinite;
  }

  @keyframes pulse {
    0%, 100% {
      transform: translate(-50%, -50%) scale(1);
      opacity: 0.3;
    }
    50% {
      transform: translate(-50%, -50%) scale(1.1);
      opacity: 0.1;
    }
  }

  @keyframes innerSpin {
    0% {
      transform: translate(-50%, -50%) rotate(0deg);
    }
    100% {
      transform: translate(-50%, -50%) rotate(360deg);
    }
  }

  @keyframes fadeInOut {
    0%, 100% {
      opacity: 0.6;
    }
    50% {
      opacity: 1;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 背景渐变样式 - 与首页完全一致



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

    .nav-left, .nav-right {
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

    .nav-center {
      flex: 1;
      text-align: center;
      padding: 0 20rpx;

      .nav-title {
        font-size: 34rpx;
        font-weight: 700;
        color: white;
        letter-spacing: 0.8rpx;
        max-width: 400rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
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

// 用户资料区域
.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 40rpx 30rpx;
  text-align: center;





  .avatar-container {
    position: relative;
    margin-bottom: 20rpx;

    .user-avatar {
      width: 180rpx;
      height: 180rpx;
      border-radius: 90rpx;
      border: 4rpx solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.3);
    }

    .online-indicator {
      position: absolute;
      bottom: 20rpx;
      right: 20rpx;
      width: 32rpx;
      height: 32rpx;
      background: #00ff7f;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 255, 255, 0.9);
      box-shadow: 0 2rpx 8rpx rgba(0, 255, 127, 0.4);
    }

    .badges {
      position: absolute;
      top: -10rpx;
      right: -10rpx;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .vip-badge, .verified-badge, .matchmaker-badge {
        padding: 6rpx 12rpx;
        border-radius: 12rpx;
        font-size: 20rpx;
        font-weight: 700;
        color: white;
        text-align: center;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
      }

      .vip-badge {
        background: linear-gradient(135deg, #FFD700, #FFA500);
      }

      .matchmaker-badge {
        background: linear-gradient(135deg, #ff6b6b, #ee5a52);
      }

      .verified-badge {
        background: linear-gradient(135deg, #10b981, #059669);
      }
    }
  }

  // 用户名区域
  .username-section {
    margin-bottom: 16rpx;

    .user-name {
      font-size: 44rpx;
      font-weight: 700;
      color: white;
      line-height: 1.2;
      text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
    }
  }



  // 性别和年龄区域
  .gender-age-section {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;
    padding: 8rpx 16rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 20rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    max-width: 200rpx;
    margin-left: auto;
    margin-right: auto;

    .gender-age-text {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
      text-align: center;
      line-height: 1.2;
    }
  }

  // 统计数据行
  .stats-row {
    display: flex;
    justify-content: center;
    gap: 60rpx;
    margin-bottom: 20rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .stat-count {
        font-size: 32rpx;
        font-weight: 700;
        color: white;
        line-height: 1.2;
        text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
      }

      .stat-label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
        font-weight: 500;
      }
    }
  }

  // 个人简介
  .bio-section {
    margin-bottom: 24rpx;
    max-width: 500rpx;

    .bio-text {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.9);
      line-height: 1.6;
      text-align: center;
      font-weight: 400;
      letter-spacing: 0.5rpx;
      text-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.3);
    }
  }

  // 关注按钮容器
  .follow-button-container {
    width: 100%;
    max-width: 400rpx;

    .follow-button {
      width: 100%;
      height: 80rpx;
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.9);
      border: 2rpx solid rgba(255, 255, 255, 0.3);
      transition: all 0.3s ease;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.2);

      &:active {
        transform: scale(0.95);
      }

      &.followed {
        background: rgba(16, 185, 129, 0.8);
        border-color: rgba(16, 185, 129, 0.6);
      }

      .follow-text {
        font-size: 32rpx;
        color: #333;
        font-weight: 600;
      }

      &.followed .follow-text {
        color: white;
      }
    }
  }
}

// 标签区域样式

.tags-section {
  padding: 16rpx 32rpx 24rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  justify-content: center;
  align-items: center;

  .tag-item {
    padding: 10rpx 20rpx;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 20rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateY(-2rpx);
    }

    .tag-text {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.95);
      font-weight: 500;
      text-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.2);
      line-height: 1.2;
    }
  }
}

.tab-bar {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  display: flex;
  border-radius: 20rpx 20rpx 0 0;
  margin: 16rpx 0 0 0;
  width: 100%;
  box-sizing: border-box;
  border: 1rpx solid rgba(255, 255, 255, 0.2);

  .tab-item {
    flex: 1;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transition: all 0.3s ease;

    &.active {
      .tab-text {
        color: white;
        font-weight: 700;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: rgba(196, 113, 237, 0.8);
        border-radius: 2rpx;
      }
    }

    .tab-text {
      font-size: 30rpx;
      color: rgba(255, 255, 255, 0.7);
      font-weight: 600;
      transition: all 0.3s ease;
    }
  }
}

.content-container {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  min-height: 400rpx;
  padding: 20rpx;
  width: 100%;
  box-sizing: border-box;
  margin: 0;
  border: 1rpx solid rgba(255, 255, 255, 0.1);


}

.photos-content {
  .photo-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24rpx;

    .photo-item {
      aspect-ratio: 1;
      border-radius: 16rpx;
      overflow: hidden;
      transition: all 0.3s ease;
      position: relative;
      border: 1rpx solid rgba(255, 255, 255, 0.3);

      &:active {
        transform: scale(0.98);
      }

      .photo-image {
        width: 100%;
        height: 100%;
      }
    }
  }
}

.moments-content {
  .moment-list {
    .moment-item {
      padding: 24rpx 0;
      border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);

      &:last-child {
        border-bottom: none;
      }

      .moment-header {
        margin-bottom: 16rpx;

        .moment-info {
          display: flex;
          align-items: center;
          gap: 16rpx;

          .moment-time {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.9);
          }

          .moment-location {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }

      .moment-content {
        margin-bottom: 16rpx;

        .moment-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.95);
          line-height: 1.6;
        }
      }

      .moment-media {
        margin-bottom: 16rpx;

        .media-grid {
          display: grid;
          gap: 8rpx;

          &.single {
            grid-template-columns: 1fr;
            max-width: 400rpx;
          }

          &.quad {
            grid-template-columns: repeat(2, 1fr);
          }

          &.nine {
            grid-template-columns: repeat(3, 1fr);
          }

          .media-item {
            aspect-ratio: 1;
            border-radius: 8rpx;
            overflow: hidden;

            .media-image {
              width: 100%;
              height: 100%;
            }
          }
        }
      }

      .moment-actions {
        display: flex;
        gap: 32rpx;

        .action-item {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .action-icon {
            font-size: 28rpx;

            &.liked {
              color: #ff69b4;
            }
          }

          .action-count {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }
    }
  }
}

.info-content {
  .info-section {
    margin-bottom: 48rpx;

    .section-title {
      font-size: 36rpx;
      font-weight: 700;
      color: rgba(255, 255, 255, 0.95);
      margin-bottom: 24rpx;
      position: relative;
      padding-left: 20rpx;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4rpx;
        height: 24rpx;
        background: rgba(196, 113, 237, 0.8);
        border-radius: 2rpx;
      }
    }

    .intro-text {
      font-size: 30rpx;
      color: rgba(255, 255, 255, 0.9);
      line-height: 1.6;
      padding: 24rpx;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 16rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.2);
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 32rpx;

      .info-item {
        display: flex;
        flex-direction: column;
        gap: 12rpx;
        padding: 20rpx;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 16rpx;
        border: 1rpx solid rgba(255, 255, 255, 0.2);

        .info-label {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.7);
          font-weight: 500;
        }

        .info-value {
          font-size: 30rpx;
          color: rgba(255, 255, 255, 0.9);
          font-weight: 600;
        }
      }
    }

    .cert-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 20rpx;
      background: rgba(255, 255, 255, 0.08);
      border-radius: 16rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.15);

      .cert-icon {
        font-size: 36rpx;
        color: rgba(255, 255, 255, 0.8);
      }

      .cert-text {
        flex: 1;
        font-size: 30rpx;
        color: rgba(255, 255, 255, 0.95);
        font-weight: 500;
      }

      .cert-status {
        font-size: 26rpx;
        font-weight: 600;

        &.verified {
          color: #10b981;
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

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 32rpx;
    opacity: 0.6;
  }

  .empty-text {
    font-size: 32rpx;
    color: rgba(255, 255, 255, 0.7);
    font-weight: 500;
    text-align: center;
    line-height: 1.5;
  }
}

.bottom-placeholder {
  height: 80rpx;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100vw;
  padding: 16rpx 24rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
  display: flex;
  gap: 16rpx;
  box-sizing: border-box;
  z-index: 999;

  .action-btn {
    height: 80rpx;
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.98);
    }

    &.secondary {
      flex: 1;
      background: rgba(255, 255, 255, 0.15);
      border: 1rpx solid rgba(255, 255, 255, 0.2);

      .btn-icon, .btn-text {
        color: rgba(255, 255, 255, 0.9);
      }
    }

    &.primary {
      flex: 2;
      background: rgba(196, 113, 237, 0.8);
      border: 1rpx solid rgba(196, 113, 237, 0.4);

      .btn-icon, .btn-text {
        color: white;
      }
    }

    .btn-icon {
      font-size: 32rpx;
    }

    .btn-text {
      font-size: 28rpx;
      font-weight: 600;
    }
  }
}


</style>