<template>
	<view class="message-page">
		<!-- 背景层 -->
		<view class="bg-layer">
			<view class="bg-gradient"></view>
			<view class="overlay-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部标题和搜索 -->
			<view class="header">
				<text class="page-title">消息</text>
				<view class="search-box" @tap="goSearch">
					<text class="search-icon"></text>
					<text class="search-placeholder">搜索</text>
				</view>
			</view>
			
			<!-- 消息分类选项卡 -->
			<view class="message-tabs">
				<view 
					v-for="(tab, index) in messageTabs" 
					:key="index" 
					class="message-tab" 
					:class="{ active: currentTabIndex === index }"
					@tap="switchTab(index)"
				>
					<text>{{ tab.name }}</text>
				</view>
			</view>
			
			<!-- AI红娘入口 -->
			<view class="ai-matchmaker-entry" @tap="openAiMatchmaker">
				<view class="ai-avatar-container">
					<image
						class="ai-avatar"
						src="/static/ai/22.png"
						mode="aspectFill"
					></image>
					<view class="ai-indicator"></view>
				</view>
				<view class="ai-content">
					<view class="ai-top">
						<text class="ai-name">AI红娘小智</text>
						<view class="ai-tag">智能助手</view>
					</view>
					<text class="ai-description">专业红娘服务，为您推荐心仪对象</text>
				</view>
				<view class="ai-arrow">›</view>
			</view>

			<!-- 消息列表 -->
			<view class="message-list">
				<view class="message-item" v-for="(message, index) in messages" :key="index" @tap="openChat(message)">
					<view class="avatar-container">
						<image
							class="user-avatar"
							:src="imageError[`message${index}`] ? '/static/message/default-avatar.png' : (message.avatar || '/static/message/default-avatar.png')"
							mode="aspectFill"
							@error="handleImageError(`message${index}`)"
						></image>
						<view v-if="imageError[`message${index}`]" class="avatar-placeholder">
							<text class="avatar-text">{{ message.name ? message.name.charAt(0) : '?' }}</text>
						</view>
						<!-- 在线状态指示器 -->
						<view v-if="message.isFriend" class="online-indicator" :class="{ online: message.isOnline }"></view>
						<!-- 未读消息徽章 -->
						<view v-if="message.unreadCount > 0" class="unread-badge">{{ message.unreadCount > 99 ? '99+' : message.unreadCount }}</view>
					</view>

					<view class="message-content">
						<view class="message-top">
							<text class="user-name">{{ message.name }}</text>
							<view class="message-right">
								<!-- 在线状态文字 -->
								<text v-if="message.isFriend" class="online-status" :class="{ online: message.isOnline }">
									{{ message.isOnline ? '在线' : getOfflineTime(message.lastSeen) }}
								</text>
								<text class="message-time">{{ message.time }}</text>
							</view>
						</view>
						<text class="message-preview">{{ message.lastMessage }}</text>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-if="messages.length === 0" class="empty-state">
				<image class="empty-icon" src="/static/message/empty.png" mode="aspectFit"></image>
				<text class="empty-text">暂无消息</text>
				<view class="action-button" @tap="goExplore">去广场看看</view>
			</view>
		</view>
		
		<!-- 自定义TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue';
import customTabBar from '@/components/custom-tab-bar.vue';
import { getConversationList, getMutualFollowList, getOnlineStatus, getBatchOnlineStatus, getUnreadCounts } from '@/api/chat';
import wsManager from '@/utils/websocket';
import { checkTokenStatus, diagnoseLoginStatus } from '@/utils/token-debug';

// 图片加载错误状态
const imageError = reactive({});

// 消息选项卡
const currentTabIndex = ref(0);
const messageTabs = [
	{ name: '全部', type: 'all' },
	{ name: '未读', type: 'unread' },
	{ name: '系统', type: 'system' }
];

// 消息列表数据
const messages = ref([]);
const loading = ref(false);

// 在线状态数据
const onlineUsers = ref(new Set());
const unreadCounts = ref(new Map());

// 定时器
let refreshTimer = null;

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
}

// 切换消息选项卡
function switchTab(index) {
	currentTabIndex.value = index;
	// 这里可以根据选项卡类型筛选不同的消息
	// filterMessages(messageTabs[index].type);
}

// 加载互相关注的好友列表
async function loadMutualFriends() {
	try {
		loading.value = true;

		// 先清空现有数据
		messages.value = [];

		// 调试：检查登录状态
		console.log('=== 开始加载好友列表 ===');
		const tokenStatus = checkTokenStatus();
		if (!tokenStatus.hasToken) {
			console.error('❌ 没有token，无法加载好友列表');
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			});
			return;
		}

		const response = await getMutualFollowList();
		console.log('好友列表API响应:', response);

		if (response.code === 200) {
			if (response.data && response.data.length > 0) {
				console.log(`找到 ${response.data.length} 个好友`);
				messages.value = response.data.map(friend => ({
					id: null, // 暂时没有会话ID
					userId: friend.userId,
					name: friend.nickname || friend.name || `用户${friend.userId}`,
					avatar: friend.avatarUrl || friend.avatar || '/static/message/default-avatar.png',
					lastMessage: '点击开始聊天',
					time: '',
					unreadCount: 0, // 统一使用unreadCount
					isFriend: true, // 标记为好友
					isOnline: false, // 在线状态，默认离线
					lastSeen: friend.lastSeen || null // 最后在线时间
				}));

				// 加载好友在线状态和未读消息
				await loadFriendsOnlineStatus();
				await loadUnreadCounts();
			} else {
				console.log('没有好友数据，清空列表');
				messages.value = [];
			}
		} else {
			console.error('好友列表API返回错误:', response);
			messages.value = [];
			uni.showToast({
				title: response.message || '获取好友列表失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('加载好友列表失败:', error);

		// 清空好友列表
		messages.value = [];

		// 如果是401错误，可能是token过期
		if (error.code === 400 && error.message && error.message.includes('用户未登录')) {
			console.log('检测到用户未登录错误，开始诊断...');
			await diagnoseLoginStatus();

			uni.showModal({
				title: '登录状态异常',
				content: '检测到登录状态异常，请重新登录',
				showCancel: false,
				success: () => {
					uni.reLaunch({
						url: '/pages/login/login'
					});
				}
			});
		} else {
			// 不再显示测试数据，只显示错误提示
			console.log('好友列表加载失败，错误信息:', error);
			uni.showToast({
				title: '加载失败: ' + (error.message || '网络错误'),
				icon: 'none'
			});
		}
	} finally {
		loading.value = false;
	}
}

// 加载会话列表（仅用于更新好友的会话信息）
async function loadConversations() {
	try {
		// 检查是否有好友，没有好友就不需要加载会话
		const friends = messages.value.filter(m => m.isFriend);
		if (friends.length === 0) {
			console.log('没有好友，跳过会话加载');
			return;
		}

		loading.value = true;
		const response = await getConversationList();
		if (response.code === 200) {
			const conversations = response.data.map(conversation => ({
				id: conversation.conversationId,
				userId: conversation.otherUserId,
				name: conversation.otherUserNickname,
				avatar: conversation.otherUserAvatar || '/static/message/default-avatar.png',
				lastMessage: conversation.lastMessageContent || '暂无消息',
				time: formatTime(conversation.lastMessageTime),
				unreadCount: conversation.unreadCount || 0,
				isFriend: false // 标记为会话
			}));

			// 只更新现有好友的会话信息，不添加非好友的会话
			messages.value.forEach(friend => {
				if (friend.isFriend) {
					const conversation = conversations.find(c => c.userId === friend.userId);
					if (conversation) {
						friend.id = conversation.id;
						friend.lastMessage = conversation.lastMessage;
						friend.time = conversation.time;
						friend.unreadCount = conversation.unreadCount;
					}
				}
			});

			console.log('会话信息已更新到好友列表，不添加非好友会话');
		}
	} catch (error) {
		console.error('加载会话列表失败:', error);
	} finally {
		loading.value = false;
	}
}

// 格式化时间
function formatTime(timeStr) {
	if (!timeStr) return '';

	const time = new Date(timeStr);
	const now = new Date();
	const diff = now.getTime() - time.getTime();

	// 今天
	if (diff < 24 * 60 * 60 * 1000 && now.getDate() === time.getDate()) {
		return time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
	}
	// 昨天
	else if (diff < 48 * 60 * 60 * 1000) {
		return '昨天';
	}
	// 本周
	else if (diff < 7 * 24 * 60 * 60 * 1000) {
		const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return days[time.getDay()];
	}
	// 更早
	else {
		return time.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
	}
}



// 加载好友在线状态
async function loadFriendsOnlineStatus() {
	try {
		const friendIds = messages.value.filter(m => m.isFriend).map(m => m.userId);
		if (friendIds.length === 0) {
			console.log('没有好友，跳过在线状态加载');
			return;
		}

		console.log('批量加载好友在线状态:', friendIds);
		console.log('发送请求参数:', { userIds: friendIds });

		// 使用批量接口获取在线状态
		const response = await getBatchOnlineStatus(friendIds);
		console.log('在线状态API响应:', response);

		if (response && response.code === 200) {
			const onlineStatusMap = response.data || {};
			console.log('在线状态数据:', onlineStatusMap);

			// 更新好友在线状态
			messages.value.forEach(friend => {
				if (friend.isFriend) {
					const status = onlineStatusMap[friend.userId];
					if (status) {
						friend.isOnline = status.isOnline || false;
						friend.lastSeen = status.lastSeen || null;
					} else {
						// 如果没有状态数据，默认为离线
						friend.isOnline = false;
						friend.lastSeen = null;
					}
				}
			});

			console.log('在线状态加载完成，更新后的好友列表:',
				messages.value.filter(m => m.isFriend).map(m => ({
					name: m.name,
					isOnline: m.isOnline,
					lastSeen: m.lastSeen
				}))
			);
		} else {
			console.error('在线状态API返回错误:', response);
			// 降级到单个请求
			await loadFriendsOnlineStatusFallback();
		}
	} catch (error) {
		console.error('加载好友在线状态失败:', error);
		// 降级到单个请求
		await loadFriendsOnlineStatusFallback();
	}
}

// 降级方案：单个请求获取在线状态
async function loadFriendsOnlineStatusFallback() {
	const friendIds = messages.value.filter(m => m.isFriend).map(m => m.userId);

	for (const friendId of friendIds) {
		try {
			const response = await getOnlineStatus(friendId);
			if (response.code === 200) {
				const friend = messages.value.find(m => m.userId === friendId);
				if (friend) {
					friend.isOnline = response.data.isOnline;
					friend.lastSeen = response.data.lastSeen;
				}
			}
		} catch (error) {
			console.error(`获取用户${friendId}在线状态失败:`, error);
		}
	}
}

// 加载未读消息数量
async function loadUnreadCounts() {
	try {
		const friendIds = messages.value.filter(m => m.isFriend).map(m => m.userId);
		if (friendIds.length === 0) {
			console.log('没有好友，跳过未读消息数量加载');
			return;
		}

		console.log('加载未读消息数量:', friendIds);
		console.log('发送请求参数:', { userIds: friendIds });

		const response = await getUnreadCounts(friendIds);
		console.log('未读消息API响应:', response);

		if (response && response.code === 200) {
			const unreadCountsMap = response.data || {};
			console.log('未读消息数据:', unreadCountsMap);

			// 更新未读消息数量
			messages.value.forEach(friend => {
				if (friend.isFriend) {
					const unreadCount = unreadCountsMap[friend.userId];
					friend.unreadCount = typeof unreadCount === 'number' ? unreadCount : 0;
				}
			});

			console.log('未读消息数量加载完成，更新后的好友列表:',
				messages.value.filter(m => m.isFriend).map(m => ({
					name: m.name,
					unreadCount: m.unreadCount
				}))
			);
		} else {
			console.error('未读消息API返回错误:', response);
		}
	} catch (error) {
		console.error('加载未读消息数量失败:', error);
	}
}

// 获取离线时间显示
function getOfflineTime(lastSeen) {
	if (!lastSeen) return '离线';

	const now = new Date();
	const lastSeenDate = new Date(lastSeen);
	const diffMs = now - lastSeenDate;
	const diffMinutes = Math.floor(diffMs / (1000 * 60));
	const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
	const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

	if (diffMinutes < 1) return '刚刚离线';
	if (diffMinutes < 60) return `${diffMinutes}分钟前`;
	if (diffHours < 24) return `${diffHours}小时前`;
	if (diffDays < 7) return `${diffDays}天前`;
	return '离线';
}

// 打开聊天页面
function openChat(message) {
	try {
		// 如果是好友但没有会话ID，需要先创建会话或直接进入聊天
		const conversationId = message.id || 'new';

		// 确保参数安全
		const userId = message.userId || '';
		const name = message.name || '未知用户';
		const avatar = message.avatar || '/static/message/default-avatar.png';

		console.log('准备导航到聊天页面:', { conversationId, userId, name, avatar });

		uni.navigateTo({
			url: `/pages/message/chat?conversationId=${conversationId}&userId=${userId}&name=${encodeURIComponent(name)}&avatar=${encodeURIComponent(avatar)}`,
			success: () => {
				console.log('导航成功');
				// 将未读消息置为0
				const index = messages.value.findIndex(item => item.userId === message.userId);
				if (index !== -1) {
					messages.value[index].unreadCount = 0;
				}
			},
			fail: (error) => {
				console.error('导航失败:', error);
				uni.showToast({
					title: '打开聊天页面失败',
					icon: 'none'
				});
			}
		});
	} catch (error) {
		console.error('openChat error:', error);
		uni.showToast({
			title: '操作失败',
			icon: 'none'
		});
	}
}



// 搜索页面
function goSearch() {
	uni.navigateTo({
		url: '/pages/search/search'
	});
}

// 去广场页面
function goExplore() {
	uni.reLaunch({
		url: '/pages/square/square'
	});
}

// 打开AI红娘页面
function openAiMatchmaker() {
	uni.navigateTo({
		url: '/pages/ai-chat/ai-chat',
		success: () => {
			console.log('导航到AI红娘页面成功');
		},
		fail: (error) => {
			console.error('导航到AI红娘页面失败:', error);
			uni.showToast({
				title: '打开AI红娘失败',
				icon: 'none'
			});
		}
	});
}

// 初始化WebSocket连接
async function initWebSocket() {
	try {
		const token = uni.getStorageSync('token');
		if (token) {
			await wsManager.connect(token);

			// 监听新消息
			wsManager.onMessage('CHAT', (message) => {
				console.log('收到新消息:', message);
				// 更新会话列表
				loadConversations();
			});

			// 监听消息状态更新
			wsManager.onMessage('SENT', (message) => {
				console.log('消息发送成功:', message);
			});
		}
	} catch (error) {
		console.error('WebSocket连接失败:', error);
	}
}

// 页面生命周期
onMounted(() => {
	// 先加载好友列表，再加载会话列表
	loadMutualFriends().then(() => {
		loadConversations();
	});
	initWebSocket();
	initWebSocketListeners();

	// 启动定时刷新（每30秒刷新一次在线状态和未读消息）
	refreshTimer = setInterval(() => {
		if (messages.value.length > 0) {
			loadFriendsOnlineStatus();
			loadUnreadCounts();
		}
	}, 30000);
});

// 初始化WebSocket监听器
function initWebSocketListeners() {
	// 监听用户在线状态变化
	wsManager.onMessage('USER_STATUS', (data) => {
		console.log('收到用户状态变化:', data);
		const friend = messages.value.find(m => m.userId === data.userId);
		if (friend) {
			friend.isOnline = data.isOnline;
			friend.lastSeen = data.lastSeen || new Date().toISOString();
			console.log(`用户${data.userId}状态更新为:`, data.isOnline ? '在线' : '离线');
		}
	});

	// 监听用户上线
	wsManager.onMessage('USER_ONLINE', (data) => {
		console.log('用户上线:', data);
		const friend = messages.value.find(m => m.userId === data.userId);
		if (friend) {
			friend.isOnline = true;
			friend.lastSeen = new Date().toISOString();
		}
	});

	// 监听用户下线
	wsManager.onMessage('USER_OFFLINE', (data) => {
		console.log('用户下线:', data);
		const friend = messages.value.find(m => m.userId === data.userId);
		if (friend) {
			friend.isOnline = false;
			friend.lastSeen = data.lastSeen || new Date().toISOString();
		}
	});

	// 监听新消息
	wsManager.onMessage('NEW_MESSAGE', (data) => {
		console.log('收到新消息:', data);
		updateMessageWithNewMessage(data);
	});

	// 监听未读消息数量变化
	wsManager.onMessage('UNREAD_COUNT_CHANGED', (data) => {
		console.log('未读消息数量变化:', data);
		updateUnreadCount(data.userId, data.unreadCount);
	});
}

// 更新消息列表中的新消息
function updateMessageWithNewMessage(messageData) {
	const friend = messages.value.find(m => m.userId === messageData.senderId);
	if (friend) {
		// 更新最后一条消息
		friend.lastMessage = messageData.content || '新消息';
		friend.time = formatTime(new Date(messageData.timestamp));
		// 增加未读数量
		friend.unreadCount = (friend.unreadCount || 0) + 1;

		// 将该好友移到列表顶部
		const index = messages.value.findIndex(m => m.userId === messageData.senderId);
		if (index > 0) {
			const friendData = messages.value.splice(index, 1)[0];
			messages.value.unshift(friendData);
		}

		console.log(`收到来自用户${messageData.senderId}的新消息，未读数量: ${friend.unreadCount}`);
	}
}

// 更新未读消息数量
function updateUnreadCount(userId, unreadCount) {
	const friend = messages.value.find(m => m.userId === userId);
	if (friend) {
		friend.unreadCount = unreadCount;
	}
}

onUnmounted(() => {
	wsManager.disconnect();
	// 清除定时器
	if (refreshTimer) {
		clearInterval(refreshTimer);
		refreshTimer = null;
	}
});
</script>

<style lang="scss" scoped>
.message-page {
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
		padding: 40rpx 30rpx 110rpx 30rpx; // 底部增加110rpx padding为自定义tabBar预留空间
		z-index: 1;
		
		// 顶部区域
		.header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 30rpx;

			.page-title {
				font-size: 40rpx;
				font-weight: bold;
				color: #fff;
			}

			.search-box {
				width: 200rpx;
				height: 60rpx;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 30rpx;
				display: flex;
				align-items: center;
				padding: 0 20rpx;

				.search-icon {
					width: 32rpx;
					height: 32rpx;
					background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>') no-repeat center;
					background-size: contain;
					margin-right: 10rpx;
				}

				.search-placeholder {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.8);
				}
			}
		}
		
		// AI红娘入口
		.ai-matchmaker-entry {
			display: flex;
			align-items: center;
			padding: 25rpx;
			background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.15) 100%);
			border-radius: 25rpx;
			margin-bottom: 30rpx;
			border: 2rpx solid rgba(255, 255, 255, 0.3);
			box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);

			.ai-avatar-container {
				position: relative;
				margin-right: 20rpx;

				.ai-avatar {
					width: 110rpx;
					height: 110rpx;
					border-radius: 50%;
					border: 4rpx solid rgba(255, 255, 255, 0.5);
				}

				.ai-indicator {
					position: absolute;
					bottom: 5rpx;
					right: 5rpx;
					width: 28rpx;
					height: 28rpx;
					border-radius: 50%;
					background: linear-gradient(45deg, #4CAF50, #8BC34A);
					border: 4rpx solid rgba(255, 255, 255, 0.9);
					animation: pulse 2s infinite;
				}
			}

			.ai-content {
				flex: 1;

				.ai-top {
					display: flex;
					align-items: center;
					margin-bottom: 8rpx;

					.ai-name {
						font-size: 34rpx;
						font-weight: bold;
						color: #fff;
						margin-right: 15rpx;
					}

					.ai-tag {
						background: linear-gradient(45deg, #FF6B6B, #FF8E8E);
						color: #fff;
						font-size: 20rpx;
						padding: 4rpx 12rpx;
						border-radius: 12rpx;
						font-weight: 500;
					}
				}

				.ai-description {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.9);
					line-height: 1.4;
				}
			}

			.ai-arrow {
				font-size: 40rpx;
				color: rgba(255, 255, 255, 0.7);
				font-weight: bold;
			}
		}

		// 消息选项卡
		.message-tabs {
			display: flex;
			background: rgba(255, 255, 255, 0.1);
			border-radius: 20rpx;
			margin-bottom: 30rpx;
			padding: 10rpx;
			
			.message-tab {
				flex: 1;
				text-align: center;
				padding: 15rpx 0;
				border-radius: 15rpx;
				transition: all 0.3s;
				
				text {
					font-size: 28rpx;
					color: rgba(255, 255, 255, 0.8);
				}
				
				&.active {
					background: rgba(255, 255, 255, 0.2);
					
					text {
						color: #fff;
						font-weight: bold;
					}
				}
			}
		}
		
		// 消息列表
		.message-list {
			flex: 1;
			
			.message-item {
				display: flex;
				align-items: center;
				padding: 20rpx;
				background: rgba(255, 255, 255, 0.15);
				border-radius: 20rpx;
				margin-bottom: 20rpx;
				
				.avatar-container {
					position: relative;

					.user-avatar {
						width: 100rpx;
						height: 100rpx;
						border-radius: 50%;
						background-color: rgba(255, 255, 255, 0.1);

						.avatar-placeholder {
							position: absolute;
							top: 0;
							left: 0;
							width: 100%;
							height: 100%;
							background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
							border-radius: 50%;
							display: flex;
							align-items: center;
							justify-content: center;

							.avatar-text {
								color: white;
								font-size: 36rpx;
								font-weight: bold;
							}
						}
					}

					.online-indicator {
						position: absolute;
						bottom: 2rpx;
						right: 2rpx;
						width: 24rpx;
						height: 24rpx;
						border-radius: 50%;
						background: #ccc;
						border: 4rpx solid rgba(255, 255, 255, 0.9);

						&.online {
							background: #4CAF50;
						}
					}

					.unread-badge {
						position: absolute;
						top: 0;
						right: 0;
						min-width: 36rpx;
						height: 36rpx;
						background: #FF6B6B;
						border-radius: 18rpx;
						color: #fff;
						font-size: 20rpx;
						display: flex;
						align-items: center;
						justify-content: center;
						padding: 0 6rpx;
						border: 2rpx solid #fff;
						z-index: 2;
					}
				}
				
				.message-content {
					flex: 1;
					margin-left: 20rpx;
					overflow: hidden;
					
					.message-top {
						display: flex;
						justify-content: space-between;
						align-items: center;
						margin-bottom: 10rpx;

						.user-name {
							font-size: 32rpx;
							font-weight: bold;
							color: #fff;
						}

						.message-right {
							display: flex;
							flex-direction: column;
							align-items: flex-end;

							.online-status {
								font-size: 20rpx;
								color: rgba(255, 255, 255, 0.5);
								margin-bottom: 4rpx;

								&.online {
									color: #4CAF50;
								}
							}

							.message-time {
								font-size: 24rpx;
								color: rgba(255, 255, 255, 0.6);
							}
						}
					}
					
					.message-preview {
						font-size: 26rpx;
						color: rgba(255, 255, 255, 0.8);
						white-space: nowrap;
						overflow: hidden;
						text-overflow: ellipsis;
					}
				}
			}
		}
		
		// 空状态
		.empty-state {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			
			.empty-icon {
				width: 200rpx;
				height: 200rpx;
				margin-bottom: 30rpx;
				opacity: 0.7;
			}
			
			.empty-text {
				font-size: 30rpx;
				color: rgba(255, 255, 255, 0.7);
				margin-bottom: 40rpx;
			}
			
			.action-button {
				background: rgba(255, 255, 255, 0.2);
				color: #fff;
				font-size: 28rpx;
				padding: 15rpx 40rpx;
				border-radius: 30rpx;
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
	position: relative;
	z-index: 0;
	
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
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.05)" fill-opacity="1" d="M0,64L48,96C96,128,192,192,288,202.7C384,213,480,171,576,165.3C672,160,768,192,864,197.3C960,203,1056,181,1152,165.3C1248,149,1344,139,1392,133.3L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom;
		background-size: 100% 40%;
		opacity: 0.6;
	}
	
	&::after {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%),
					radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%);
	}
}

// AI红娘动画
@keyframes pulse {
	0% {
		transform: scale(1);
		opacity: 1;
	}
	50% {
		transform: scale(1.1);
		opacity: 0.8;
	}
	100% {
		transform: scale(1);
		opacity: 1;
	}
}
</style> 