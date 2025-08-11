<template>
	<view class="moment-detail-page" :style="{ background: computedBg }">
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 自定义导航栏 -->
			<view class="custom-navbar">
				<view class="navbar-left" @tap="goBack">
					<text class="back-icon">‹</text>
				</view>
				<view class="navbar-center">
					<text class="navbar-title">动态详情</text>
				</view>
				<view class="navbar-right"></view>
			</view>

			<!-- 滚动区域 -->
			<scroll-view
				scroll-y
				class="scroll-area"
				:style="{ height: contentHeight + 'px' }"
			>
				<!-- 动态详情卡片 -->
				<view v-if="momentDetail" class="moment-item">
					<!-- 用户信息和关注按钮 -->
					<view class="user-info">
						<image
							:src="momentDetail.avatarUrl || '/static/default-avatar.svg'"
							class="avatar"
							mode="aspectFill"
							@error="onAvatarError"
						></image>
						<view class="user-details">
							<text class="nickname">{{ momentDetail.nickname || '匿名用户' }}</text>
							<text class="time">{{ formatTime(momentDetail.publishTime) }}</text>
						</view>
						<!-- 关注按钮 -->
						<view class="follow-btn" @tap="handleFollow" v-if="!isCurrentUser">
							<text class="follow-text">{{ isFollowed ? '已关注' : '关注' }}</text>
						</view>
					</view>

					<!-- 动态内容 -->
					<view v-if="momentDetail.content" class="content">
						<text>{{ momentDetail.content }}</text>
					</view>

					<!-- 位置信息 -->
					<view v-if="momentDetail.location" class="location">
						<text class="location-icon">📍</text>
						<text class="location-text">{{ momentDetail.location }}</text>
					</view>

					<!-- 媒体内容 -->
					<view v-if="momentDetail.mediaList && momentDetail.mediaList.length > 0" class="media-grid">
						<image
							v-for="(media, index) in momentDetail.mediaList"
							:key="media.mediaId"
							:src="media.mediaUrl"
							class="media-image"
							mode="aspectFill"
							@tap="previewImages(index)"
						>
							<view v-if="!media.mediaUrl" class="image-placeholder"></view>
						</image>
					</view>

					<!-- 互动区域 -->
					<view class="actions">
						<view class="action-item" @tap="likeMoment">
							<image
								:src="momentDetail.isLiked ? '/static/icons/heart-filled.png' : '/static/icons/heart.png'"
								class="action-icon-img"
								:class="{ 'liked-heart': momentDetail.isLiked }"
								mode="aspectFit"
							></image>
							<text class="action-text">{{ momentDetail.likeCount || 0 }}</text>
						</view>
						<view class="action-item" @tap="focusCommentInput">
							<image
								src="/static/icons/comment.png"
								class="action-icon-img"
								mode="aspectFit"
							></image>
							<text class="action-text">{{ commentList.length }}</text>
						</view>
						<view class="action-item">
							<image
								src="/static/icons/eye.png"
								class="action-icon-img"
								mode="aspectFit"
							></image>
							<text class="action-text">{{ momentDetail.viewCount || 0 }}</text>
						</view>
					</view>
				</view>

				<!-- 评论列表 -->
				<view class="comment-section">
					<view class="comment-title">评论 ({{ commentList.length }})</view>
					<view v-if="commentList.length === 0" class="no-comments">
						<text>暂无评论，快来抢沙发吧~</text>
					</view>
					<view v-else class="comment-list">
						<view v-for="comment in commentList" :key="comment.commentId" class="comment-item">
							<image
								:src="comment.avatarUrl || '/static/default-avatar.svg'"
								class="comment-avatar"
								mode="aspectFill"
								@error="onCommentAvatarError"
							></image>
							<view class="comment-content">
								<view class="comment-header">
									<text class="comment-nickname">{{ comment.nickname || '匿名用户' }}</text>
									<text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
								</view>
								<text class="comment-text">{{ comment.content }}</text>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 底部评论输入框 -->
		<view class="comment-input-container" :class="{ 'keyboard-active': keyboardHeight > 0 }" :style="{ bottom: keyboardHeight + 'px' }">
			<view class="comment-input-wrapper">
				<view class="input-area">
					<input
						v-model="commentText"
						class="comment-input"
						placeholder="请输入评论内容..."
						:focus="commentInputFocus"
						@blur="onInputBlur"
						@focus="onInputFocus"
						@confirm="submitComment"
						confirm-type="send"
						maxlength="500"
					/>
				</view>
				<view class="send-button" :class="{ active: commentText.trim() }" @tap="submitComment">
					<text class="send-text">发送</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, onMounted, computed } from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import { toggleUserFollow, checkUserFollow } from '@/api/user/detail.js'
	import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js'

	// 使用全局主题混入
	const { currentBackground } = useGlobalThemeMixin()

	// 响应式数据
	const momentDetail = ref(null)
	const commentList = ref([])
	const commentText = ref('')
	const commentInputFocus = ref(false)
	const momentId = ref('')
	const systemInfo = ref({})
	const keyboardHeight = ref(0)
	const isFollowed = ref(false)
	const isCurrentUser = ref(false)
	const followLoading = ref(false)

	// 计算属性
	const contentHeight = computed(() => {
		const windowHeight = systemInfo.value.windowHeight || 667
		const navbarHeight = 88 // 自定义导航栏高度
		const commentInputHeight = 100 // 评论输入框高度
		return windowHeight - navbarHeight - commentInputHeight
	})
	
	const computedBg = computed(() => {
		return currentBackground.value
	})
	
	// 页面加载
	onLoad((options) => {
		console.log('页面加载参数:', options)
		momentId.value = options.id
		if (!momentId.value) {
			console.error('缺少动态ID参数')
			uni.showToast({
				title: '参数错误',
				icon: 'error'
			})
			return
		}
		if (options.action === 'comment') {
			// 如果是评论操作，自动聚焦输入框
			setTimeout(() => {
				commentInputFocus.value = true
			}, 500)
		}
		initPage()
	})
	
	onMounted(() => {
		// 获取系统信息
		uni.getSystemInfo({
			success: (res) => {
				systemInfo.value = res
			}
		})
	})
	
	// 初始化页面
	async function initPage() {
		await loadMomentDetail()
		await loadCommentList()
	}
	
	// 加载动态详情
	async function loadMomentDetail() {
		try {
			const momentApi = await import('@/api/moment.js')
			const result = await momentApi.default.getMomentDetail(momentId.value)
			if (result.code === 200) {
				momentDetail.value = result.data
				console.log('动态详情数据:', result.data)

				// 检查是否为当前用户
				checkCurrentUser()

				// 检查关注状态
				if (!isCurrentUser.value) {
					await checkFollowStatus()
				}

				// 增加浏览次数
				await momentApi.default.incrementViewCount(momentId.value)
			} else {
				console.error('获取动态详情失败:', result.message)
				uni.showToast({
					title: result.message || '获取失败',
					icon: 'error'
				})
			}
		} catch (error) {
			console.error('加载动态详情失败:', error)
			uni.showToast({
				title: '网络错误',
				icon: 'error'
			})
		}
	}
	
	// 加载评论列表
	async function loadCommentList() {
		try {
			const momentApi = await import('@/api/moment.js')
			const result = await momentApi.default.getCommentList(momentId.value)
			if (result.code === 200) {
				// 后端返回的是分页数据，需要取records字段
				commentList.value = result.data?.records || []
				console.log('评论列表数据:', result.data)
				console.log('评论记录:', result.data?.records)
			} else {
				console.error('获取评论列表失败:', result.message)
				commentList.value = []
			}
		} catch (error) {
			console.error('加载评论列表失败:', error)
			commentList.value = []
		}
	}
	
	// 返回上一页
	function goBack() {
		// 检查是否可以返回
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack()
		} else {
			// 如果没有上一页，跳转到首页
			uni.reLaunch({
				url: '/pages/index/index'
			})
		}
	}
	
	// 点赞动态
	async function likeMoment() {
		try {
			const momentApi = await import('@/api/moment.js')
			const result = await momentApi.default.likeMoment(momentId.value)
			if (result.code === 200) {
				momentDetail.value.isLiked = !momentDetail.value.isLiked
				momentDetail.value.likeCount = momentDetail.value.isLiked 
					? (momentDetail.value.likeCount || 0) + 1 
					: Math.max((momentDetail.value.likeCount || 0) - 1, 0)
			}
		} catch (error) {
			console.error('点赞失败:', error)
			uni.showToast({
				title: '操作失败',
				icon: 'error'
			})
		}
	}
	
	// 聚焦评论输入框
	function focusCommentInput() {
		commentInputFocus.value = true
	}

	// 输入框获得焦点
	function onInputFocus() {
		commentInputFocus.value = true
		// 监听键盘高度变化（兼容性处理）
		if (uni.onKeyboardHeightChange && typeof uni.onKeyboardHeightChange === 'function') {
			uni.onKeyboardHeightChange((res) => {
				keyboardHeight.value = res.height
			})
		} else {
			// 在不支持的平台上使用固定高度
			keyboardHeight.value = 300
		}
	}

	// 输入框失去焦点
	function onInputBlur() {
		commentInputFocus.value = false
		keyboardHeight.value = 0
	}

	// 提交评论
	async function submitComment() {
		if (!commentText.value.trim()) {
			uni.showToast({
				title: '请输入评论内容',
				icon: 'none'
			})
			return
		}

		try {
			const momentApi = await import('@/api/moment.js')
			const result = await momentApi.default.addComment(momentId.value, commentText.value.trim())
			if (result.code === 200) {
				commentText.value = ''
				commentInputFocus.value = false
				keyboardHeight.value = 0

				// 重新加载评论列表
				await loadCommentList()

				// 更新评论数量
				if (momentDetail.value) {
					momentDetail.value.commentCount = (momentDetail.value.commentCount || 0) + 1
				}

				uni.showToast({
					title: '评论成功',
					icon: 'success'
				})

				// 滚动到评论区域
				setTimeout(() => {
					uni.pageScrollTo({
						selector: '.comment-section',
						duration: 300
					})
				}, 100)
			} else {
				uni.showToast({
					title: result.message || '评论失败',
					icon: 'error'
				})
			}
		} catch (error) {
			console.error('评论失败:', error)
			// 尝试从错误对象中获取后端返回的错误信息
			let errorMessage = '网络错误'
			if (error && error.data && error.data.message) {
				errorMessage = error.data.message
			} else if (error && error.message) {
				errorMessage = error.message
			} else if (typeof error === 'string') {
				errorMessage = error
			}

			uni.showToast({
				title: errorMessage,
				icon: 'error'
			})
		}
	}

	// 头像加载错误处理
	function onAvatarError(e) {
		console.log('头像加载失败:', e)
		e.target.src = '/static/default-avatar.svg'
	}

	// 评论头像加载错误处理
	function onCommentAvatarError(e) {
		console.log('评论头像加载失败:', e)
		e.target.src = '/static/default-avatar.svg'
	}

	// 预览图片
	function previewImages(index) {
		const urls = momentDetail.value.mediaList.map(media => media.mediaUrl)
		uni.previewImage({
			urls: urls,
			current: index
		})
	}

	// 处理关注操作
	async function handleFollow() {
		if (followLoading.value || !momentDetail.value) return

		try {
			followLoading.value = true
			const result = await toggleUserFollow(momentDetail.value.userId)

			if (result.code === 200) {
				isFollowed.value = !isFollowed.value
				uni.showToast({
					title: result.message || (isFollowed.value ? '已关注' : '取消关注'),
					icon: 'success'
				})
			} else {
				uni.showToast({
					title: result.message || '操作失败',
					icon: 'none'
				})
			}
		} catch (error) {
			console.error('关注操作失败:', error)
			// 尝试从错误对象中获取后端返回的错误信息
			let errorMessage = '网络错误，请重试'
			if (error && error.data && error.data.message) {
				errorMessage = error.data.message
			} else if (error && error.message) {
				errorMessage = error.message
			} else if (typeof error === 'string') {
				errorMessage = error
			}

			uni.showToast({
				title: errorMessage,
				icon: 'none'
			})
		} finally {
			followLoading.value = false
		}
	}

	// 检查关注状态
	async function checkFollowStatus() {
		if (!momentDetail.value || isCurrentUser.value) return

		try {
			const result = await checkUserFollow(momentDetail.value.userId)
			if (result.code === 200) {
				isFollowed.value = result.data || false
			}
		} catch (error) {
			console.error('检查关注状态失败:', error)
		}
	}

	// 检查是否为当前用户
	function checkCurrentUser() {
		const token = uni.getStorageSync('token')
		if (!token || !momentDetail.value) {
			isCurrentUser.value = false
			return
		}

		try {
			// 这里可以从token中解析用户ID，或者从其他地方获取当前用户信息
			const currentUserId = uni.getStorageSync('userId')
			isCurrentUser.value = currentUserId && currentUserId == momentDetail.value.userId
		} catch (error) {
			console.error('检查当前用户失败:', error)
			isCurrentUser.value = false
		}
	}
	
	// 格式化时间
	function formatTime(timestamp) {
		if (!timestamp) return ''
		const date = new Date(timestamp)
		const now = new Date()
		const diff = now - date
		
		if (diff < 60000) return '刚刚'
		if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
		if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
		if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
		
		return date.toLocaleDateString()
	}
</script>

<style lang="scss" scoped>
.moment-detail-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;



  // 内容容器
  .content-container {
    flex: 1;
    display: flex;
    flex-direction: column;

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
        width: 80rpx;
      }
    }

    // 滚动区域
    .scroll-area {
      flex: 1;
      padding: 20rpx;
    }

    // 评论区域
    .comment-section {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 20rpx;
      padding: 30rpx;
      margin-top: 20rpx;
      backdrop-filter: blur(10rpx);

      .comment-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 20rpx;
      }

      .no-comments {
        text-align: center;
        padding: 60rpx 0;

        text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.7);
        }
      }

      .comment-list {
        .comment-item {
          display: flex;
          margin-bottom: 24rpx;

          &:last-child {
            margin-bottom: 0;
          }

          .comment-avatar {
            width: 60rpx;
            height: 60rpx;
            border-radius: 50%;
            margin-right: 15rpx;

            .avatar-placeholder {
              width: 100%;
              height: 100%;
              background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;

              &::before {
                content: '👤';
                font-size: 24rpx;
                color: rgba(255, 255, 255, 0.6);
              }
            }
          }

          .comment-content {
            flex: 1;

            .comment-header {
              display: flex;
              align-items: center;
              justify-content: space-between;
              margin-bottom: 8rpx;

              .comment-nickname {
                font-size: 28rpx;
                font-weight: bold;
                color: rgba(255, 255, 255, 0.9);
              }

              .comment-time {
                font-size: 24rpx;
                color: rgba(255, 255, 255, 0.6);
              }
            }

            .comment-text {
              font-size: 28rpx;
              line-height: 1.4;
              color: rgba(255, 255, 255, 0.8);
            }
          }
        }
      }
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

          .avatar-placeholder {
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;

            &::before {
              content: '👤';
              font-size: 32rpx;
              color: rgba(255, 255, 255, 0.6);
            }
          }
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

        .follow-btn {
          background: rgba(255, 255, 255, 0.4);
          border: 1rpx solid rgba(255, 255, 255, 0.6);
          border-radius: 20rpx;
          padding: 6rpx 16rpx;
          backdrop-filter: blur(10rpx);
          transition: all 0.3s ease;
          box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);

          &:active {
            transform: scale(0.95);
          }

          .follow-text {
            color: #fff;
            font-weight: 500;
          }
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

          .image-placeholder {
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 10rpx;

            &::before {
              content: '📷';
              font-size: 30rpx;
              color: rgba(255, 255, 255, 0.6);
            }
          }
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

          .action-icon-img {
            width: 32rpx;
            height: 32rpx;
            margin-right: 10rpx;
            filter: brightness(0) invert(1);
            opacity: 0.8;
            transition: all 0.3s ease;

            &.liked-heart {
              filter: none;
              opacity: 1;
            }
          }

          .action-text {
            font-size: 28rpx;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }
    }



  }

  // 底部评论输入框
  .comment-input-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(20rpx);
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);
    padding: 20rpx;
    z-index: 1000;
    transition: all 0.3s ease;

    &.keyboard-active {
      background: rgba(255, 255, 255, 0.2);
    }

    .comment-input-wrapper {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .input-area {
        flex: 1;
        background: rgba(255, 255, 255, 0.15);
        border: 1rpx solid rgba(255, 255, 255, 0.3);
        border-radius: 25rpx;
        backdrop-filter: blur(10rpx);
        transition: all 0.3s ease;
        height: 88rpx;
        display: flex;
        align-items: center;

        &:focus-within {
          background: rgba(255, 255, 255, 0.2);
          border-color: rgba(255, 255, 255, 0.5);
          box-shadow: 0 4rpx 12rpx rgba(255, 255, 255, 0.1);
        }

        .comment-input {
          width: 100%;
          background: transparent;
          border: none;
          padding: 0 20rpx;
          font-size: 28rpx;
          color: #fff;
          height: 60rpx;
          line-height: 60rpx;
          box-sizing: border-box;

          &::placeholder {
            color: rgba(255, 255, 255, 0.6);
          }
        }
      }

      .send-button {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 25rpx;
        padding: 15rpx 24rpx;
        transition: all 0.3s ease;
        min-width: 80rpx;
        text-align: center;
        display: flex;
        align-items: center;
        justify-content: center;

        &.active {
          background: rgba(102, 126, 234, 0.8);
          box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
        }

        &:active {
          transform: scale(0.95);
        }

        .send-text {
          font-size: 28rpx;
          color: #fff;
          font-weight: 600;
          line-height: 1;
        }
      }
    }
  }
}
</style>
