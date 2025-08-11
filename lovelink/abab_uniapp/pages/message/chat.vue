<template>
	<view class="chat-page">
		<!-- 背景层 -->
		<view class="bg-layer" :style="{ background: computedBg }"></view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部导航 -->
			<view class="chat-header">
				<view class="header-left" @tap="goBack">
					<text class="back-icon">‹</text>
				</view>
				<view class="header-center">
					<image class="user-avatar" :src="chatUser.avatar" mode="aspectFill" @error="handleAvatarError">
						<view v-if="avatarError" class="avatar-placeholder"></view>
					</image>
					<view class="user-info">
						<text class="user-name">{{ chatUser.name }}</text>
						<text class="online-status" :class="{ online: isOnline }">
							{{ isOnline ? '在线' : '离线' }}
						</text>
					</view>
				</view>
				<view class="header-right">
					<text class="more-icon">⋯</text>
				</view>
			</view>
			
			<!-- 聊天消息列表 -->
			<scroll-view
				class="message-list"
				scroll-y
				:scroll-into-view="scrollToView"
				:scroll-top="scrollTop"
				@scrolltoupper="loadMoreMessages"
			>
				<!-- 加载更多提示 -->
				<view v-if="loadingMore" class="loading-more">
					<text>加载中...</text>
				</view>
				
				<!-- 消息列表 -->
				<view
					v-for="(message, index) in messageList"
					:key="message.messageId || index"
					:id="`message-${message.messageId || message.tempId || index}`"
					class="message-item"
					:class="{ 'message-self': message.isSelf }"
				>
					<!-- 时间分割线 -->
					<view v-if="shouldShowTime(message, index)" class="time-divider">
						<text>{{ formatMessageTime(message.createdAt) }}</text>
					</view>
					
					<!-- 消息内容 -->
					<view class="message-content">
						<!-- 对方消息布局 -->
						<template v-if="!message.isSelf">
							<!-- 对方头像 -->
							<image
								class="message-avatar"
								:src="chatUser.avatar || '/static/message/default-avatar.png'"
								mode="aspectFill"
								@error="onAvatarError"
							></image>

							<!-- 对方消息气泡 -->
							<view class="message-bubble">
								<!-- 文本消息 -->
								<text v-if="message.messageType === 1" class="message-text">
									{{ message.content }}
								</text>

								<!-- 图片消息 -->
								<image
									v-else-if="message.messageType === 2"
									class="message-image"
									:src="message.mediaUrl"
									mode="aspectFit"
									@tap="previewImage(message.mediaUrl)"
								></image>

								<!-- 其他类型消息 -->
								<text v-else class="message-text">
									{{ getMessageTypeText(message.messageType) }}
								</text>
							</view>
						</template>

						<!-- 自己消息布局 -->
						<template v-else>
							<!-- 消息状态（在气泡外面） -->
							<view class="message-status-outside">
								<text v-if="message.status === 'sending'" class="status-sending">发送中</text>
								<text v-else-if="message.status === 'sent'" class="status-sent">已发送</text>
								<text v-else-if="message.status === 'read'" class="status-read">已读</text>
								<text v-else-if="message.status === 'failed'" class="status-failed" @tap="resendMessage(message)">重发</text>
							</view>

							<!-- 自己消息气泡 -->
							<view class="message-bubble bubble-self">
								<!-- 文本消息 -->
								<text v-if="message.messageType === 1" class="message-text">
									{{ message.content }}
								</text>

								<!-- 图片消息 -->
								<image
									v-else-if="message.messageType === 2"
									class="message-image"
									:src="message.mediaUrl"
									mode="aspectFit"
									@tap="previewImage(message.mediaUrl)"
								></image>

								<!-- 其他类型消息 -->
								<text v-else class="message-text">
									{{ getMessageTypeText(message.messageType) }}
								</text>
							</view>

							<!-- 自己的头像 -->
							<view class="avatar-wrapper">
								<image
									class="message-avatar"
									:src="currentUser.avatar || '/static/message/default-avatar.png'"
									mode="aspectFill"
									@error="onSelfAvatarError"
									@load="onAvatarLoad"
								></image>
								<!-- 头像占位符（仅在头像加载失败时显示） -->
								<view v-if="showAvatarPlaceholder" class="avatar-placeholder">
									<text class="avatar-text">{{ currentUser.nickname ? currentUser.nickname.charAt(0) : '我' }}</text>
								</view>
							</view>
						</template>
					</view>
				</view>

				<!-- 底部锚点，用于滚动到底部 -->
				<view id="bottom-anchor" style="height: 1px;"></view>
			</scroll-view>
			
			<!-- 输入区域 -->
			<view class="input-area">
				<view class="input-container">
					<view class="input-tools">
						<text class="tool-icon" @tap="showEmojiPanel">😊</text>
						<text class="tool-icon" @tap="chooseImage">📷</text>
					</view>
					
					<input 
						class="message-input" 
						v-model="inputMessage" 
						placeholder="输入消息..."
						@focus="onInputFocus"
						@blur="onInputBlur"
						@input="onInputChange"
						@confirm="sendMessage"
					/>
					
					<view class="send-button" :class="{ active: inputMessage.trim() }" @tap="sendMessage">
						<text>发送</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getChatHistory, markMessageAsRead, sendMessage as sendChatMessage } from '@/api/chat';
import wsManager from '@/utils/websocket';
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js';

// 页面参数
const props = defineProps({
	conversationId: String,
	userId: String,
	name: String,
	avatar: String
});

// 聊天用户信息
const chatUser = reactive({
	id: props.userId,
	name: decodeURIComponent(props.name || ''),
	avatar: decodeURIComponent(props.avatar || '/static/message/default-avatar.png')
});

// 当前用户信息
const currentUser = reactive({
	id: null,
	avatar: '/static/message/default-avatar.png'
});

// 页面状态
const messageList = ref([]);
const inputMessage = ref('');
const scrollTop = ref(0);
const scrollToView = ref('');
const isOnline = ref(false);
const loadingMore = ref(false);
const avatarError = ref(false);
const showAvatarPlaceholder = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);
const hasMoreMessages = ref(true);

// 输入状态
const isInputFocused = ref(false);
const typingTimer = ref(null);

// 使用全局主题混入
const { currentBackground } = useGlobalThemeMixin();

// 计算属性
const computedBg = computed(() => {
  return currentBackground.value;
});

// 获取页面参数
onMounted(() => {

	const pages = getCurrentPages();
	const currentPage = pages[pages.length - 1];
	const options = currentPage.options;

	if (options.userId) {
		chatUser.id = options.userId;
		chatUser.name = decodeURIComponent(options.name || '');
		chatUser.avatar = decodeURIComponent(options.avatar || '/static/message/default-avatar.png');

		// 如果有会话ID且不是'new'，加载聊天记录
		if (options.conversationId && options.conversationId !== 'new') {
			loadChatHistory();
		}

		// 获取当前用户信息
		getCurrentUserInfo();

		// 初始化WebSocket
		initWebSocket();

		// 连接WebSocket
		connectWebSocket();

		// 页面加载完成后滚动到底部
		setTimeout(() => {
			scrollToBottom();
		}, 500);
	}
});

// 页面显示时的处理
onShow(() => {
	console.log('聊天页面显示');
	// 确保每次页面显示时都滚动到底部
	setTimeout(() => {
		scrollToBottom();
	}, 300);
});

// 获取当前用户信息
async function getCurrentUserInfo() {
	const token = uni.getStorageSync('token');

	console.log('=== 获取用户信息调试 ===');
	console.log('token:', token);

	if (!token) {
		console.warn('未找到token，使用默认用户信息');
		setDefaultUserInfo();
		return;
	}

	try {
		// 使用API通过token获取用户信息
		const { getByUserInfo } = await import('@/api/user/auth');
		const result = await getByUserInfo(token);

		console.log('API返回的用户信息:', result);

		if (result && result.code === 200 && result.data) {
			const userInfo = result.data;

			currentUser.id = userInfo.userId || userInfo.id;
			currentUser.nickname = userInfo.nickname || userInfo.name || userInfo.username || '我';

			// 处理头像
			let avatarUrl = userInfo.avatarUrl || userInfo.avatar;
			console.log('API返回的头像路径:', avatarUrl);

			if (!avatarUrl || avatarUrl === 'null' || avatarUrl === 'undefined' || avatarUrl.trim() === '') {
				currentUser.avatar = '/static/message/default-avatar.png';
				showAvatarPlaceholder.value = true;
				console.log('使用默认头像和占位符');
			} else {
				currentUser.avatar = avatarUrl;
				showAvatarPlaceholder.value = false;
				console.log('使用API获取的真实头像:', currentUser.avatar);
			}

			// 保存用户信息到本地存储
			uni.setStorageSync('userInfo', userInfo);
		} else {
			console.warn('API返回数据格式错误:', result);
			setDefaultUserInfo();
		}
	} catch (error) {
		console.error('获取用户信息失败:', error);
		setDefaultUserInfo();
	}

	console.log('=== 最终用户信息 ===');
	console.log('currentUser:', currentUser);
	console.log('currentUser.avatar:', currentUser.avatar);
}

// 设置默认用户信息
function setDefaultUserInfo() {
	currentUser.id = 1001;
	currentUser.avatar = '/static/message/default-avatar.png';
	currentUser.nickname = '我';
	showAvatarPlaceholder.value = true;
	console.log('已设置默认用户信息');
}

// 加载聊天记录
async function loadChatHistory(page = 1) {
	try {
		// 如果没有会话ID，跳过加载
		const pages = getCurrentPages();
		const currentPage = pages[pages.length - 1];
		const options = currentPage.options;
		const conversationId = options.conversationId;

		if (!conversationId || conversationId === 'new') {
			console.log('新会话，无历史记录');
			return;
		}

		if (page === 1) {
			messageList.value = [];
		} else {
			loadingMore.value = true;
		}

		const response = await getChatHistory(conversationId, page, pageSize.value);
		if (response.code === 200) {
			const messages = response.data.map(msg => ({
				...msg,
				isSelf: msg.senderId == currentUser.id,
				status: msg.senderId == currentUser.id ? 'sent' : 'received'
			}));

			if (page === 1) {
				messageList.value = messages.reverse();
				// 滚动到底部 - 多重保障
				nextTick(() => {
					scrollToBottom();
				});
				setTimeout(() => {
					scrollToBottom();
				}, 200);
				setTimeout(() => {
					scrollToBottom();
				}, 500);
			} else {
				messageList.value = [...messages.reverse(), ...messageList.value];
			}

			hasMoreMessages.value = messages.length === pageSize.value;
		}
	} catch (error) {
		console.error('加载聊天记录失败:', error);
		uni.showToast({
			title: '加载失败',
			icon: 'none'
		});
	} finally {
		loadingMore.value = false;
	}
}

// 加载更多消息
function loadMoreMessages() {
	if (loadingMore.value || !hasMoreMessages.value) return;
	
	currentPage.value++;
	loadChatHistory(currentPage.value);
}

// 初始化WebSocket
function initWebSocket() {
	// 监听新消息
	wsManager.onMessage('CHAT', (message) => {
		if (message.senderId == chatUser.id || message.receiverId == chatUser.id) {
			const newMessage = {
				...message.data,
				messageId: message.messageId,
				senderId: message.senderId,
				isSelf: message.senderId == currentUser.id,
				status: 'received',
				createdAt: new Date().toISOString()
			};
			
			messageList.value.push(newMessage);

			// 自动滚动到新消息 - 多重保障
			nextTick(() => {
				scrollToBottom();
			});

			// 延迟再次滚动，确保DOM完全更新
			setTimeout(() => {
				scrollToBottom();
			}, 100);

			// 再次延迟滚动，确保在所有渲染完成后
			setTimeout(() => {
				scrollToBottom();
			}, 300);
			
			// 如果不是自己发送的消息，标记为已读
			if (!newMessage.isSelf) {
				markAsRead(newMessage.messageId);
			}
		}
	});
	
	// 监听消息发送状态
	wsManager.onMessage('SENT', (message) => {
		console.log('收到消息发送确认:', message);
		const index = messageList.value.findIndex(msg => msg.tempId === message.tempId || msg.tempId === message.messageId);
		if (index > -1) {
			messageList.value[index].status = 'sent';
			messageList.value[index].messageId = message.messageId;
			console.log('消息状态更新为已发送');
		}
	});

	// 监听消息发送失败
	wsManager.onMessage('SEND_FAILED', (message) => {
		console.log('收到消息发送失败通知:', message);
		const index = messageList.value.findIndex(msg => msg.tempId === message.tempId);
		if (index > -1) {
			messageList.value[index].status = 'failed';
		}
	});

	// 监听用户在线状态
	wsManager.onMessage('USER_STATUS', (data) => {
		console.log('收到用户状态更新:', data);
		if (data.userId == chatUser.id) {
			isOnline.value = data.isOnline;
		}
	});

	// 请求对方在线状态
	setTimeout(() => {
		if (wsManager.isConnected) {
			wsManager.send({
				type: 'GET_USER_STATUS',
				data: { userId: chatUser.id }
			});
		}
	}, 1000);
}

// 连接WebSocket
async function connectWebSocket() {
	const token = uni.getStorageSync('token');
	if (!token) {
		console.error('没有token，无法连接WebSocket');
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		});
		return;
	}

	try {
		console.log('正在连接WebSocket...');
		await wsManager.connect(token);
		console.log('WebSocket连接成功');

		// 设置在线状态为true（连接成功表示可以通信）
		isOnline.value = true;

		// 请求对方在线状态
		setTimeout(() => {
			if (wsManager.isConnected) {
				wsManager.send({
					type: 'GET_USER_STATUS',
					data: { userId: chatUser.id }
				});
			}
		}, 1000);

	} catch (error) {
		console.error('WebSocket连接失败:', error);
		isOnline.value = false;
		uni.showToast({
			title: 'WebSocket连接失败',
			icon: 'none'
		});
	}
}

// 发送消息
function sendMessage() {
	const content = inputMessage.value.trim();
	if (!content) return;

	const tempId = Date.now();
	const newMessage = {
		tempId,
		content,
		messageType: 1,
		isSelf: true,
		status: 'sending',
		createdAt: new Date().toISOString(),
		senderId: currentUser.id,
		senderAvatar: currentUser.avatar
	};

	console.log('发送消息:', newMessage);
	console.log('当前用户头像:', currentUser.avatar);

	messageList.value.push(newMessage);
	inputMessage.value = '';

	// 自动滚动到新消息 - 多重保障
	nextTick(() => {
		scrollToBottom();
	});

	// 延迟再次滚动，确保DOM完全更新
	setTimeout(() => {
		scrollToBottom();
	}, 100);

	// 再次延迟滚动，确保在所有渲染完成后
	setTimeout(() => {
		scrollToBottom();
	}, 300);

	// 检查WebSocket连接状态
	if (!wsManager.isConnected) {
		console.warn('WebSocket未连接，尝试重新连接...');
		const token = uni.getStorageSync('token');
		if (token) {
			wsManager.connect(token).then(() => {
				console.log('重连成功，重新发送消息');
				sendMessageWithWebSocket();
			}).catch(error => {
				console.error('重连失败:', error);
				markMessageFailed();
			});
		} else {
			console.error('没有token，无法连接WebSocket');
			markMessageFailed();
		}
		return;
	}

	sendMessageWithWebSocket();

	// 发送WebSocket消息
	async function sendMessageWithWebSocket() {
		// 1. 先通过API保存消息到数据库
		try {
			const messageData = {
				receiverId: chatUser.id,
				content: content,
				messageType: 1
			};

			console.log('保存消息到数据库:', messageData);
			const response = await sendChatMessage(messageData);

			if (response.code === 200) {
				console.log('消息保存成功:', response.data);

				// 更新消息ID
				const index = messageList.value.findIndex(msg => msg.tempId === tempId);
				if (index > -1) {
					messageList.value[index].messageId = response.data.messageId;
					messageList.value[index].status = 'sent';
				}

				// 2. 通过WebSocket发送实时通知
				const wsSuccess = wsManager.sendChatMessage(chatUser.id, content, 1, tempId);
				if (wsSuccess) {
					console.log('WebSocket实时通知发送成功');
				} else {
					console.warn('WebSocket实时通知发送失败，但消息已保存');
				}

			} else {
				console.error('消息保存失败:', response);
				markMessageFailed();
			}

		} catch (error) {
			console.error('发送消息API调用失败:', error);

			// API失败时，尝试仅通过WebSocket发送
			console.log('尝试仅通过WebSocket发送...');
			const wsSuccess = wsManager.sendChatMessage(chatUser.id, content, 1, tempId);
			if (wsSuccess) {
				console.log('WebSocket发送成功，但未保存到数据库');

				// 设置超时，标记为已发送但提示可能未保存
				setTimeout(() => {
					const index = messageList.value.findIndex(msg => msg.tempId === tempId);
					if (index > -1 && messageList.value[index].status === 'sending') {
						messageList.value[index].status = 'sent';
						console.log('消息超时自动标记为已发送（可能未保存到数据库）');
					}
				}, 3000);

			} else {
				markMessageFailed();
			}
		}
	}

	// 标记消息发送失败
	function markMessageFailed() {
		console.error('WebSocket发送失败');
		const index = messageList.value.findIndex(msg => msg.tempId === tempId);
		if (index > -1) {
			messageList.value[index].status = 'failed';
		}

		uni.showToast({
			title: '发送失败，请重试',
			icon: 'none'
		});
	}
}

// 重发消息
function resendMessage(message) {
	console.log('重发消息:', message);
	message.status = 'sending';

	// 检查WebSocket连接状态
	if (!wsManager.isConnected) {
		console.warn('WebSocket未连接，尝试重新连接...');
		const token = uni.getStorageSync('token');
		if (token) {
			wsManager.connect(token).then(() => {
				console.log('重连成功，重新发送消息');
				doResendMessage(message);
			}).catch(error => {
				console.error('重连失败:', error);
				message.status = 'failed';
				uni.showToast({
					title: '重发失败，请检查网络',
					icon: 'none'
				});
			});
		} else {
			message.status = 'failed';
			uni.showToast({
				title: '重发失败，请重新登录',
				icon: 'none'
			});
		}
		return;
	}

	doResendMessage(message);
}

// 执行重发消息
function doResendMessage(message) {
	const success = wsManager.sendChatMessage(chatUser.id, message.content, message.messageType, message.tempId);
	if (success) {
		console.log('重发成功，等待服务器确认...');

		// 设置超时
		setTimeout(() => {
			if (message.status === 'sending') {
				message.status = 'sent';
				console.log('重发消息超时自动标记为已发送');
			}
		}, 5000);

	} else {
		message.status = 'failed';
		uni.showToast({
			title: '重发失败',
			icon: 'none'
		});
	}
}

// 头像加载成功处理
function onAvatarLoad(e) {
	console.log('头像加载成功:', e.target.src);
	showAvatarPlaceholder.value = false;
}

// 自己头像加载错误处理
function onSelfAvatarError(e) {
	console.log('自己头像加载失败:', e.target.src);
	console.log('错误详情:', e);
	showAvatarPlaceholder.value = true;
}

// 对方头像加载错误处理
function onAvatarError(e) {
	console.log('对方头像加载失败:', e.target.src);
	console.log('错误详情:', e);
	// 可以在这里设置默认头像
	e.target.src = '/static/message/default-avatar.png';
}

// 标记消息为已读
async function markAsRead(messageId) {
	try {
		const pages = getCurrentPages();
		const currentPage = pages[pages.length - 1];
		const options = currentPage.options;
		const conversationId = options.conversationId;

		if (conversationId && conversationId !== 'new') {
			await markMessageAsRead(conversationId, messageId);
		}
	} catch (error) {
		console.error('标记已读失败:', error);
	}
}

// 输入框事件
function onInputFocus() {
	isInputFocused.value = true;
}

function onInputBlur() {
	isInputFocused.value = false;
}

function onInputChange() {
	// 发送正在输入状态
	if (typingTimer.value) {
		clearTimeout(typingTimer.value);
	}
	
	wsManager.sendTyping(chatUser.id);
	
	typingTimer.value = setTimeout(() => {
		// 停止输入状态
	}, 1000);
}

// 选择图片
function chooseImage() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			// 这里应该上传图片并发送图片消息
			console.log('选择图片:', res);
		}
	});
}

// 预览图片
function previewImage(url) {
	uni.previewImage({
		urls: [url]
	});
}

// 显示表情面板
function showEmojiPanel() {
	// 实现表情面板
	console.log('显示表情面板');
}

// 工具函数
function shouldShowTime(message, index) {
	if (index === 0) return true;
	
	const prevMessage = messageList.value[index - 1];
	const currentTime = new Date(message.createdAt).getTime();
	const prevTime = new Date(prevMessage.createdAt).getTime();
	
	// 超过5分钟显示时间
	return currentTime - prevTime > 5 * 60 * 1000;
}

function formatMessageTime(timeStr) {
	const time = new Date(timeStr);
	const now = new Date();
	
	if (now.toDateString() === time.toDateString()) {
		return time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
	} else {
		return time.toLocaleString('zh-CN', { 
			month: '2-digit', 
			day: '2-digit', 
			hour: '2-digit', 
			minute: '2-digit' 
		});
	}
}

function getMessageTypeText(type) {
	const typeMap = {
		2: '[图片]',
		3: '[语音]',
		4: '[视频]',
		5: '[文件]',
		6: '[系统消息]'
	};
	return typeMap[type] || '[未知消息]';
}

function scrollToBottom() {
	console.log('执行滚动到底部');

	// 方法1: 使用 scrollToView
	scrollToView.value = 'bottom-anchor';

	// 方法2: 使用 scrollTop (备用方案)
	nextTick(() => {
		const query = uni.createSelectorQuery();
		query.select('.message-list').boundingClientRect();
		query.exec((res) => {
			if (res[0]) {
				scrollTop.value = res[0].height + 1000; // 确保滚动到最底部
			}
		});
	});

	// 清空 scrollToView 以便下次滚动生效
	setTimeout(() => {
		scrollToView.value = '';
	}, 100);
}

function handleAvatarError() {
	avatarError.value = true;
}

function goBack() {
	console.log('尝试返回上一页...');

	// 获取页面栈
	const pages = getCurrentPages();
	console.log('当前页面栈长度:', pages.length);

	// 如果页面栈长度大于1，说明有上一页可以返回
	if (pages.length > 1) {
		uni.navigateBack({
			success: () => {
				console.log('返回成功');
			},
			fail: (error) => {
				console.error('返回失败:', error);
				// 返回失败时跳转到消息列表页
				goToMessageList();
			}
		});
	} else {
		// 页面栈只有一页，直接跳转到消息列表页
		console.log('页面栈只有一页，直接跳转到消息列表');
		goToMessageList();
	}
}

// 跳转到消息列表页的统一方法
function goToMessageList() {
	uni.reLaunch({
		url: '/pages/message/message',
		success: () => {
			console.log('跳转到消息页面成功');
		},
		fail: (err) => {
			console.error('跳转到消息页面失败:', err);
			// 最后尝试跳转到首页
			uni.switchTab({
				url: '/pages/index/index'
			});
		}
	});
}

// 页面卸载
onUnmounted(() => {
	if (typingTimer.value) {
		clearTimeout(typingTimer.value);
	}
});
</script>

<style lang="scss" scoped>
.chat-page {
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
		z-index: -1;
	}

	// 内容容器
	.content-container {
		flex: 1;
		display: flex;
		flex-direction: column;
		z-index: 1;
		height: 100vh;

		// 聊天头部 - 固定在顶部
		.chat-header {
			position: fixed;
			top: 0;
			left: 0;
			right: 0;
			z-index: 100;
			display: flex;
			align-items: center;
			padding: 20rpx 30rpx;
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);
			// 添加安全区域适配
			padding-top: calc(20rpx + env(safe-area-inset-top));

			.header-left {
				width: 80rpx;

				.back-icon {
					font-size: 48rpx;
					color: #fff;
					font-weight: bold;
				}
			}

			.header-center {
				flex: 1;
				display: flex;
				align-items: center;

				.user-avatar {
					width: 80rpx;
					height: 80rpx;
					border-radius: 50%;
					margin-right: 20rpx;
					background-color: rgba(255, 255, 255, 0.1);

					.avatar-placeholder {
						width: 100%;
						height: 100%;
						background: rgba(255, 255, 255, 0.1);
						border-radius: 50%;
					}
				}

				.user-info {
					.user-name {
						display: block;
						font-size: 32rpx;
						font-weight: bold;
						color: #fff;
						margin-bottom: 5rpx;
					}

					.online-status {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.6);

						&.online {
							color: #4CAF50;
						}
					}
				}
			}

			.header-right {
				width: 80rpx;
				text-align: right;

				.more-icon {
					font-size: 32rpx;
					color: #fff;
				}
			}
		}

		// 消息列表 - 为固定的头部和输入框留出空间
		.message-list {
			flex: 1;
			padding: 20rpx;
			padding-bottom: 120rpx; // 增加更多底部内边距
			// 为固定的头部留出空间（头部高度约120rpx + 安全区域）
			margin-top: calc(120rpx + env(safe-area-inset-top));
			// 为固定的输入框留出空间（输入框高度约250rpx + 安全区域）
			margin-bottom: calc(250rpx + env(safe-area-inset-bottom));

			.loading-more {
				text-align: center;
				padding: 20rpx;

				text {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.6);
				}
			}

			.message-item {
				margin-bottom: 30rpx;

				.time-divider {
					text-align: center;
					margin-bottom: 20rpx;

					text {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.5);
						background: rgba(0, 0, 0, 0.2);
						padding: 8rpx 16rpx;
						border-radius: 12rpx;
					}
				}

				.message-content {
					display: flex;
					align-items: flex-end;

					.avatar-wrapper {
						position: relative;
						width: 80rpx;
						height: 80rpx;
						flex-shrink: 0;

						.message-avatar {
							width: 100%;
							height: 100%;
							border-radius: 50%;
							background-color: rgba(255, 255, 255, 0.1);
						}

						.avatar-placeholder {
							position: absolute;
							top: 0;
							left: 0;
							width: 100%;
							height: 100%;
							background: rgba(255, 255, 255, 0.2);
							backdrop-filter: blur(10rpx);
							border-radius: 50%;
							display: flex;
							align-items: center;
							justify-content: center;

							.avatar-text {
								color: white;
								font-size: 28rpx;
								font-weight: bold;
							}
						}
					}

					.message-avatar {
						width: 80rpx;
						height: 80rpx;
						border-radius: 50%;
						background-color: rgba(255, 255, 255, 0.1);
						flex-shrink: 0;
					}

					.message-bubble {
						max-width: 60%;
						padding: 20rpx;
						border-radius: 20rpx;
						background: rgba(255, 255, 255, 0.9);
						position: relative;
						margin: 0 20rpx;

						&.bubble-self {
							background: rgba(255, 255, 255, 0.2);
							backdrop-filter: blur(10rpx);

							.message-text {
								color: #fff;
							}
						}

						.message-text {
							font-size: 30rpx;
							line-height: 1.4;
							color: #333;
							word-wrap: break-word;
						}

						.message-image {
							max-width: 400rpx;
							max-height: 400rpx;
							border-radius: 10rpx;
						}
					}

					// 外部状态显示（仅自己的消息）
					.message-status-outside {
						display: flex;
						align-items: center;
						margin-right: 10rpx;

						text {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.6);
							white-space: nowrap;
						}

						.status-failed {
							color: #FF6B6B;
						}

						.status-sending {
							color: rgba(255, 255, 255, 0.6);
						}

						.status-sent {
							color: rgba(255, 255, 255, 0.6);
						}

						.status-read {
							color: #4CAF50;
						}
					}
				}

				// 自己的消息样式
				&.message-self {
					.message-content {
						justify-content: flex-end;

						.message-status-outside {
							order: 1;
							margin-right: 10rpx;
							margin-left: 0;
						}

						.message-bubble {
							order: 2;
							margin-right: 20rpx;
							margin-left: 0;
						}

						.avatar-wrapper {
							order: 3;
							margin-right: 0;
							margin-left: 0;
						}
					}
				}
			}
		}

		// 输入区域 - 固定在底部
		.input-area {
			position: fixed;
			bottom: 0;
			left: 0;
			right: 0;
			z-index: 100;
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);
			padding: 20rpx;
			// 添加安全区域适配
			padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

			.input-container {
				display: flex;
				align-items: center;
				background: rgba(255, 255, 255, 0.9);
				border-radius: 30rpx;
				padding: 10rpx 20rpx;

				.input-tools {
					display: flex;
					margin-right: 20rpx;

					.tool-icon {
						font-size: 32rpx;
						margin-right: 15rpx;
						color: #666;
					}
				}

				.message-input {
					flex: 1;
					font-size: 30rpx;
					color: #333;
					background: transparent;
					border: none;
					outline: none;
				}

				.send-button {
					background: #ccc;
					color: #fff;
					font-size: 26rpx;
					padding: 15rpx 25rpx;
					border-radius: 20rpx;
					margin-left: 20rpx;
					transition: all 0.3s;

					&.active {
						background: rgba(255, 255, 255, 0.3);
						backdrop-filter: blur(10rpx);
					}
				}
			}
		}
	}
}


</style>
