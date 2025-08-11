<template>
	<view class="ai-chat-page">
		<!-- 背景层 -->
		<view class="bg-layer">
			<view class="bg-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部导航 -->
			<view class="header">
				<view class="nav-left" @tap="goBack">
					<text class="back-icon">‹</text>
				</view>
				<view class="nav-center">
					<view class="ai-info">
						<image class="ai-avatar" src="/static/ai/22.png" mode="aspectFill"></image>
						<view class="ai-details">
							<text class="ai-name">AI红娘小智</text>
							<text class="ai-status">在线 · 智能助手</text>
						</view>
					</view>
				</view>
				<view class="nav-right">
					<view class="user-info-mini" @tap="showUserInfo">
						<image class="user-mini-avatar" :src="userAvatar" mode="aspectFill"></image>
						<text class="user-mini-name">{{ userInfo.nickname || '用户' }}</text>
					</view>
				</view>
			</view>
			
			<!-- 聊天消息区域 -->
			<scroll-view 
				class="chat-messages" 
				scroll-y="true" 
				:scroll-top="scrollTop"
				:scroll-with-animation="true"
			>
				<!-- 欢迎消息 -->
				<view v-if="messages.length === 0" class="welcome-message">
					<view class="welcome-card">
						<image class="welcome-avatar" src="/static/ai/22.png" mode="aspectFill"></image>
						<view class="welcome-content">
							<text class="welcome-title">你好！我是AI红娘小智 💕</text>
							<text class="welcome-desc">我可以为您提供：</text>
							<view class="service-list">
								<text class="service-item">💑 智能推荐心仪对象</text>
								<text class="service-item">🌹 专业红娘服务推荐</text>
								<text class="service-item">🎨 个性化头像生成</text>
								<text class="service-item">🗣️ 语音聊天互动</text>
							</view>
							<text class="welcome-tip">请告诉我您的需求，我来为您提供专业的婚恋建议！</text>
						</view>
					</view>
				</view>
				
				<!-- 聊天消息列表 -->
				<view v-for="(message, index) in messages" :key="index" class="message-item">
					<!-- 用户消息 -->
					<view v-if="message.type === 'user'" class="message-wrapper user-message">
						<view class="message-content">
							<text class="message-text">{{ message.content }}</text>
							<text class="message-time">{{ formatTime(message.timestamp) }}</text>
						</view>
						<image class="message-avatar" :src="userAvatar" mode="aspectFill"></image>
					</view>
					
					<!-- AI消息 -->
					<view v-else class="message-wrapper ai-message">
						<image class="message-avatar" src="/static/ai/22.png" mode="aspectFill"></image>
						<view class="message-content">
							<view class="ai-typing" v-if="message.isTyping">
								<view class="typing-dots">
									<view class="dot"></view>
									<view class="dot"></view>
									<view class="dot"></view>
								</view>
							</view>
							<text v-else class="message-text">{{ message.content }}</text>
							<text v-if="!message.isTyping" class="message-time">{{ formatTime(message.timestamp) }}</text>
						</view>
					</view>
				</view>
				
				<!-- 加载更多 -->
				<view v-if="loading" class="loading-more">
					<text class="loading-text">AI正在思考中...</text>
				</view>
			</scroll-view>
			
			<!-- 输入区域 -->
			<view class="input-area">
				<!-- 功能按钮区 -->
				<view class="function-buttons" v-if="showFunctions">
					<button class="function-btn" @tap="requestUserRecommendation">
						<text class="btn-icon">💑</text>
						<text class="btn-text">推荐用户</text>
					</button>
					<button class="function-btn" @tap="requestMatchmakerRecommendation">
						<text class="btn-icon">🌹</text>
						<text class="btn-text">推荐红娘</text>
					</button>
					<button class="function-btn" @tap="requestImageGeneration">
						<text class="btn-icon">🎨</text>
						<text class="btn-text">生成图片</text>
					</button>
				</view>

				<!-- 输入框区域 -->
				<view class="input-container">
					<button class="function-toggle" @tap="toggleFunctions">
						{{ showFunctions ? '−' : '+' }}
					</button>
					<input
						class="message-input"
						v-model="inputMessage"
						placeholder="请输入您的问题..."
						:disabled="loading"
						@input="handleInput"
						@focus="onInputFocus"
						@blur="onInputBlur"
						@confirm="sendMessage"
						confirm-type="send"
					/>
					<button class="send-btn" :class="{ active: inputMessage.trim() }" @tap="sendMessage">
						→
					</button>
				</view>
			</view>
		</view>
		
		<!-- 菜单弹窗 -->
		<view v-if="showMenuModal" class="menu-modal" @tap="hideMenu">
			<view class="menu-content" @tap.stop>
				<view class="menu-item" @tap="clearChat">
					<text class="menu-icon">🗑️</text>
					<text class="menu-text">清空聊天</text>
				</view>
				<view class="menu-item" @tap="hideMenu">
					<text class="menu-icon">❌</text>
					<text class="menu-text">取消</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue';
import { chatWithAi } from '@/api/ai-chat.js';

// 响应式数据
const messages = ref([]);
const inputMessage = ref('');
const loading = ref(false);
const scrollTop = ref(0);
const showFunctions = ref(false);
const showMenuModal = ref(false);

// 用户信息
const userInfo = ref({
	userId: null,
	nickname: null,
	gender: null,
	age: null,
	location: null,
	avatar: '/static/default-avatar.png',
	userRole: null  // 1-普通用户，2-红娘
});

// 对话记忆ID
const memoryId = ref(Date.now());

// 计算属性
const userAvatar = computed(() => {
	return userInfo.value.avatar || '/static/default-avatar.png';
});

// 页面生命周期
onMounted(() => {
	initUserInfo();
	initChat();
});

// 初始化用户信息
function initUserInfo() {
	try {
		const userInfoStr = uni.getStorageSync('userInfo');
		if (userInfoStr) {
			const userData = JSON.parse(userInfoStr);
			userInfo.value = {
				userId: userData.userId || Date.now(),
				nickname: userData.nickname || userData.name || '用户' + (userData.userId || Date.now()).toString().slice(-4),
				gender: userData.gender,
				age: userData.age,
				location: userData.location || userData.city,
				avatar: userData.avatarUrl || userData.avatar || '/static/default-avatar.png',
				userRole: userData.userRole || 1  // 默认为普通用户
			};
		} else {
			// 如果没有存储的用户信息，创建默认用户信息
			const defaultUserId = Date.now();
			userInfo.value = {
				userId: defaultUserId,
				nickname: '用户' + defaultUserId.toString().slice(-4),
				gender: 1,  // 默认男性
				age: 25,    // 默认年龄
				location: '北京',  // 默认地区
				avatar: '/static/default-avatar.png',
				userRole: 1  // 默认普通用户
			};

			// 保存默认用户信息到本地存储
			uni.setStorageSync('userInfo', JSON.stringify(userInfo.value));
		}
	} catch (error) {
		console.error('初始化用户信息失败:', error);
		// 创建兜底用户信息
		const fallbackUserId = Date.now();
		userInfo.value = {
			userId: fallbackUserId,
			nickname: '用户' + fallbackUserId.toString().slice(-4),
			gender: 1,
			age: 25,
			location: '北京',
			avatar: '/static/default-avatar.png',
			userRole: 1
		};
	}
}

// 初始化聊天
function initChat() {
	memoryId.value = Date.now();
}

// 发送消息
async function sendMessage() {
	if (!inputMessage.value.trim() || loading.value) {
		return;
	}
	
	const userMessage = inputMessage.value.trim();
	inputMessage.value = '';
	
	// 添加用户消息到列表
	messages.value.push({
		type: 'user',
		content: userMessage,
		timestamp: new Date()
	});
	
	// 添加AI正在输入的消息
	const aiMessageIndex = messages.value.length;
	messages.value.push({
		type: 'ai',
		content: '',
		timestamp: new Date(),
		isTyping: true
	});
	
	// 滚动到底部
	scrollToBottom();
	
	try {
		loading.value = true;
		
		// 构建聊天数据
		const chatData = {
			memoryId: memoryId.value,
			message: userMessage,
			messageType: 'text',
			userId: userInfo.value.userId,
			userGender: userInfo.value.gender,
			userRole: userInfo.value.userRole,  // 添加用户角色信息
			userAge: userInfo.value.age,
			userLocation: userInfo.value.location
		};

		// 发送流式请求
		const response = await chatWithAi(chatData);
		
		// 设置流式数据回调
		response.onData = (chunk) => {
			// 移除打字状态
			if (messages.value[aiMessageIndex].isTyping) {
				messages.value[aiMessageIndex].isTyping = false;
			}
			
			// 追加AI回复内容
			messages.value[aiMessageIndex].content += chunk;
			
			// 滚动到底部
			nextTick(() => {
				scrollToBottom();
			});
		};
		
		// 设置完成回调
		response.onComplete = (fullResponse) => {
			loading.value = false;
			
			// 确保消息状态正确
			if (messages.value[aiMessageIndex].isTyping) {
				messages.value[aiMessageIndex].isTyping = false;
				messages.value[aiMessageIndex].content = fullResponse;
			}
			
			// 最终滚动到底部
			nextTick(() => {
				scrollToBottom();
			});
		};
		
	} catch (error) {
		console.error('发送消息失败:', error);
		loading.value = false;
		
		// 移除打字状态并显示错误消息
		messages.value[aiMessageIndex].isTyping = false;
		messages.value[aiMessageIndex].content = '抱歉，我暂时无法回复您的消息，请稍后重试。';
		
		uni.showToast({
			title: '发送失败: ' + (error.message || '网络错误'),
			icon: 'none'
		});
	}
}

// 滚动到底部
function scrollToBottom() {
	nextTick(() => {
		const query = uni.createSelectorQuery();
		query.select('.chat-messages').boundingClientRect();
		query.exec((res) => {
			if (res[0]) {
				scrollTop.value = res[0].scrollHeight;
			}
		});
	});
}

// 格式化时间
function formatTime(time) {
	const date = new Date(time);
	const hours = date.getHours().toString().padStart(2, '0');
	const minutes = date.getMinutes().toString().padStart(2, '0');
	return `${hours}:${minutes}`;
}

// 返回上一页
function goBack() {
	uni.navigateBack({
		delta: 1
	});
}

// 显示菜单
function showMenu() {
	showMenuModal.value = true;
}

// 隐藏菜单
function hideMenu() {
	showMenuModal.value = false;
}

// 切换功能按钮显示
function toggleFunctions() {
	showFunctions.value = !showFunctions.value;
}

// 请求用户推荐
function requestUserRecommendation() {
	const message = "请为我推荐一些合适的异性用户";
	inputMessage.value = message;
	sendMessage();
	showFunctions.value = false;
}

// 请求红娘推荐
function requestMatchmakerRecommendation() {
	const message = "请为我推荐一些专业的红娘";
	inputMessage.value = message;
	sendMessage();
	showFunctions.value = false;
}

// 请求图片生成
function requestImageGeneration() {
	uni.showModal({
		title: '图片生成',
		content: '请描述您想要生成的图片',
		editable: true,
		placeholderText: '例如：温馨浪漫的约会场景',
		success: (res) => {
			if (res.confirm && res.content) {
				const message = `请为我生成一张图片：${res.content}`;
				inputMessage.value = message;
				sendMessage();
			}
		}
	});
	showFunctions.value = false;
}

// 清空聊天
function clearChat() {
	uni.showModal({
		title: '确认清空',
		content: '确定要清空所有聊天记录吗？',
		success: (res) => {
			if (res.confirm) {
				messages.value = [];
				memoryId.value = Date.now(); // 重新生成对话ID
				uni.showToast({
					title: '聊天记录已清空',
					icon: 'success'
				});
			}
		}
	});
	hideMenu();
}

// 处理输入事件
function handleInput(e) {
	inputMessage.value = e.detail.value;
}

// 输入框获得焦点
function onInputFocus() {
	showFunctions.value = false;
}

// 输入框失去焦点
function onInputBlur() {
}



// 显示用户信息
function showUserInfo() {
	uni.showModal({
		title: '当前用户信息',
		content: `昵称：${userInfo.value.nickname}\n性别：${userInfo.value.gender === 0 ? '女' : '男'}\n年龄：${userInfo.value.age}\n地区：${userInfo.value.location}\n角色：${userInfo.value.userRole === 2 ? '红娘' : '普通用户'}`,
		showCancel: false,
		confirmText: '确定'
	});
}
</script>

<style lang="scss" scoped>
.ai-chat-page {
	position: relative;
	width: 100%;
	height: 100vh;
	display: flex;
	flex-direction: column;

	// 背景层
	.bg-layer {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		z-index: -10;
		pointer-events: none;

		.bg-gradient {
			width: 100%;
			height: 100%;
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		}
	}

	// 内容容器
	.content-container {
		flex: 1;
		display: flex;
		flex-direction: column;
		height: 100vh;

		// 顶部导航
		.header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 20rpx 30rpx;
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);

			.nav-left {
				width: 80rpx;
				height: 80rpx;
				display: flex;
				align-items: center;
				justify-content: center;

				.back-icon {
					font-size: 40rpx;
					color: #fff;
					font-weight: bold;
				}
			}

			.nav-right {
				width: auto;
				height: 80rpx;
				display: flex;
				align-items: center;
				justify-content: center;

				.user-info-mini {
					display: flex;
					align-items: center;
					gap: 8rpx;
					padding: 8rpx 12rpx;
					background: rgba(255, 255, 255, 0.1);
					border-radius: 20rpx;
					cursor: pointer;

					.user-mini-avatar {
						width: 32rpx;
						height: 32rpx;
						border-radius: 50%;
						border: 2rpx solid rgba(255, 255, 255, 0.3);
					}

					.user-mini-name {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.9);
						max-width: 80rpx;
						overflow: hidden;
						text-overflow: ellipsis;
						white-space: nowrap;
					}

					&:active {
						background: rgba(255, 255, 255, 0.2);
					}
				}

				.menu-icon {
					font-size: 40rpx;
					color: #fff;
					font-weight: bold;
				}
			}

			.nav-center {
				flex: 1;
				display: flex;
				justify-content: center;

				.ai-info {
					display: flex;
					align-items: center;

					.ai-avatar {
						width: 80rpx;
						height: 80rpx;
						border-radius: 50%;
						margin-right: 20rpx;
						border: 3rpx solid rgba(255, 255, 255, 0.5);
					}

					.ai-details {
						display: flex;
						flex-direction: column;

						.ai-name {
							font-size: 32rpx;
							font-weight: bold;
							color: #fff;
							margin-bottom: 5rpx;
						}

						.ai-status {
							font-size: 24rpx;
							color: rgba(255, 255, 255, 0.8);
						}
					}
				}
			}
		}

		// 聊天消息区域
		.chat-messages {
			flex: 1;
			padding: 20rpx;

			// 欢迎消息
			.welcome-message {
				display: flex;
				justify-content: center;
				margin-top: 100rpx;

				.welcome-card {
					background: rgba(255, 255, 255, 0.95);
					border-radius: 30rpx;
					padding: 40rpx;
					max-width: 600rpx;
					text-align: center;
					box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);

					.welcome-avatar {
						width: 120rpx;
						height: 120rpx;
						border-radius: 50%;
						margin-bottom: 30rpx;
					}

					.welcome-content {
						.welcome-title {
							font-size: 36rpx;
							font-weight: bold;
							color: #333;
							display: block;
							margin-bottom: 20rpx;
						}

						.welcome-desc {
							font-size: 28rpx;
							color: #666;
							display: block;
							margin-bottom: 30rpx;
						}

						.service-list {
							margin-bottom: 30rpx;

							.service-item {
								display: block;
								font-size: 26rpx;
								color: #555;
								margin-bottom: 15rpx;
								text-align: left;
							}
						}

						.welcome-tip {
							font-size: 24rpx;
							color: #888;
							line-height: 1.5;
							display: block;
						}
					}
				}
			}

			// 消息项
			.message-item {
				margin-bottom: 30rpx;

				.message-wrapper {
					display: flex;
					align-items: flex-end;

					&.user-message {
						justify-content: flex-end;

						.message-content {
							background: linear-gradient(135deg, #667eea, #764ba2);
							color: #fff;
							margin-right: 20rpx;
							order: 1;
						}

						.message-avatar {
							order: 2;
						}
					}

					&.ai-message {
						justify-content: flex-start;

						.message-content {
							background: rgba(255, 255, 255, 0.95);
							color: #333;
							margin-left: 20rpx;
							order: 2;
						}

						.message-avatar {
							order: 1;
						}
					}

					.message-avatar {
						width: 80rpx;
						height: 80rpx;
						border-radius: 50%;
						border: 2rpx solid rgba(255, 255, 255, 0.5);
					}

					.message-content {
						max-width: 500rpx;
						padding: 20rpx 25rpx;
						border-radius: 25rpx;
						position: relative;

						.message-text {
							font-size: 28rpx;
							line-height: 1.5;
							word-wrap: break-word;
							display: block;
						}

						.message-time {
							font-size: 20rpx;
							opacity: 0.7;
							margin-top: 10rpx;
							display: block;
						}

						// AI打字动画
						.ai-typing {
							display: flex;
							align-items: center;
							height: 40rpx;

							.typing-dots {
								display: flex;
								gap: 8rpx;

								.dot {
									width: 12rpx;
									height: 12rpx;
									border-radius: 50%;
									background: #999;
									animation: typing 1.4s infinite ease-in-out;

									&:nth-child(1) { animation-delay: -0.32s; }
									&:nth-child(2) { animation-delay: -0.16s; }
									&:nth-child(3) { animation-delay: 0s; }
								}
							}
						}
					}
				}
			}

			// 加载更多
			.loading-more {
				text-align: center;
				padding: 20rpx;

				.loading-text {
					font-size: 24rpx;
					color: rgba(255, 255, 255, 0.7);
				}
			}
		}

		// 输入区域
		.input-area {
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);

			// 功能按钮区
			.function-buttons {
				display: flex;
				padding: 20rpx;
				gap: 20rpx;
				border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);

				.function-btn {
					flex: 1;
					background: rgba(255, 255, 255, 0.2);
					border: none;
					border-radius: 20rpx;
					padding: 20rpx 10rpx;
					text-align: center;
					color: rgba(255, 255, 255, 0.9);
					font-size: 22rpx;

					.btn-icon {
						font-size: 32rpx;
						display: block;
						margin-bottom: 8rpx;
					}

					.btn-text {
						font-size: 22rpx;
						color: rgba(255, 255, 255, 0.9);
						display: block;
					}
				}
			}

			// 输入框区域
			.input-container {
				display: flex;
				align-items: center;
				padding: 20rpx;
				position: relative;

				.message-input {
					flex: 1;
					height: 60rpx;
					background: #fff;
					border: 2rpx solid #ddd;
					border-radius: 30rpx;
					padding: 0 80rpx; /* 左右留出按钮空间 */
					font-size: 28rpx;
					color: #333;

					&:focus {
						border-color: #007aff;
						background: #fff;
					}

					&:disabled {
						opacity: 0.6;
					}
				}

				.function-toggle {
					position: absolute;
					left: 35rpx; /* 在输入框左侧中间位置 */
					top: 50%;
					transform: translateY(-50%);
					width: 60rpx;
					height: 60rpx;
					background: rgba(255, 255, 255, 0.2);
					border: none;
					border-radius: 50%;
					font-size: 32rpx;
					color: #fff;
					font-weight: bold;
					z-index: 10;
				}

				.send-btn {
					position: absolute;
					right: 35rpx; /* 在输入框右侧中间位置 */
					top: 50%;
					transform: translateY(-50%);
					width: 60rpx;
					height: 60rpx;
					background: rgba(255, 255, 255, 0.3);
					border: none;
					border-radius: 50%;
					font-size: 28rpx;
					color: #fff;
					font-weight: bold;
					z-index: 10;

					&.active {
						background: linear-gradient(135deg, #667eea, #764ba2);
					}
				}
			}
		}
	}

	// 菜单弹窗
	.menu-modal {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: rgba(0, 0, 0, 0.5);
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 1000;

		.menu-content {
			background: #fff;
			border-radius: 20rpx;
			padding: 40rpx;
			min-width: 400rpx;

			.menu-item {
				display: flex;
				align-items: center;
				padding: 25rpx 0;
				border-bottom: 1rpx solid #f0f0f0;

				&:last-child {
					border-bottom: none;
				}

				.menu-icon {
					font-size: 32rpx;
					margin-right: 20rpx;
				}

				.menu-text {
					font-size: 28rpx;
					color: #333;
				}
			}
		}
	}
}

// 动画
@keyframes typing {
	0%, 80%, 100% {
		transform: scale(0);
		opacity: 0.5;
	}
	40% {
		transform: scale(1);
		opacity: 1;
	}
}
</style>
