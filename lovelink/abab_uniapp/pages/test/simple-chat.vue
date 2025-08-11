<template>
	<view class="simple-chat">
		<view class="header">
			<text class="title">简化版AI聊天测试</text>
		</view>
		
		<!-- 用户信息显示 -->
		<view class="user-info">
			<text class="info-title">当前用户信息：</text>
			<text class="info-item">用户ID: {{ userInfo.userId || '未设置' }}</text>
			<text class="info-item">用户名: {{ userInfo.username || '未设置' }}</text>
			<text class="info-item">头像: {{ userInfo.avatar || '未设置' }}</text>
			<text class="info-item">性别: {{ userInfo.gender === 0 ? '女' : userInfo.gender === 1 ? '男' : '未设置' }}</text>
		</view>
		
		<!-- 消息显示区域 -->
		<view class="messages">
			<view v-for="(msg, index) in messages" :key="index" class="message">
				<text class="message-sender">{{ msg.sender }}:</text>
				<text class="message-content">{{ msg.content }}</text>
			</view>
		</view>
		
		<!-- 简化的输入区域 -->
		<view class="input-area">
			<input 
				class="simple-input" 
				v-model="inputText" 
				placeholder="请输入消息..."
				@confirm="sendMessage"
			/>
			<button class="send-button" @tap="sendMessage">发送</button>
		</view>
		
		<!-- 功能测试按钮 -->
		<view class="test-buttons">
			<button @tap="testUserInfo">测试用户信息</button>
			<button @tap="testConnection">测试连接</button>
			<button @tap="quickTest">快速测试</button>
			<button @tap="clearMessages">清空消息</button>
		</view>
		
		<!-- 状态显示 -->
		<view class="status">
			<text class="status-text">状态: {{ status }}</text>
			<text class="loading-text" v-if="loading">正在发送...</text>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 响应式数据
const inputText = ref('');
const messages = ref([]);
const loading = ref(false);
const status = ref('就绪');

// 用户信息
const userInfo = ref({
	userId: null,
	username: null,
	avatar: null,
	gender: null
});

// 页面加载
onMounted(() => {
	loadUserInfo();
	addMessage('系统', '简化版AI聊天测试页面已加载');
});

// 加载用户信息
function loadUserInfo() {
	try {
		const userInfoStr = uni.getStorageSync('userInfo');
		if (userInfoStr) {
			const userData = JSON.parse(userInfoStr);
			userInfo.value = {
				userId: userData.userId,
				username: userData.username || userData.name,
				avatar: userData.avatarUrl || userData.avatar,
				gender: userData.gender
			};
			addMessage('系统', '用户信息加载成功');
		} else {
			// 设置默认用户信息
			userInfo.value = {
				userId: Date.now(),
				username: '测试用户',
				avatar: '/static/default-avatar.png',
				gender: 1
			};
			addMessage('系统', '使用默认用户信息');
		}
	} catch (error) {
		addMessage('系统', '用户信息加载失败: ' + error.message);
		userInfo.value.userId = Date.now();
	}
}

// 添加消息
function addMessage(sender, content) {
	messages.value.push({
		sender: sender,
		content: content,
		time: new Date()
	});
}

// 发送消息
async function sendMessage() {
	if (!inputText.value.trim()) {
		addMessage('系统', '请输入消息内容');
		return;
	}
	
	const message = inputText.value.trim();
	inputText.value = '';
	
	addMessage('用户', message);
	
	loading.value = true;
	status.value = '发送中...';
	
	try {
		// 构建请求数据
		const chatData = {
			memoryId: Date.now(),
			message: message,
			messageType: 'text',
			userId: userInfo.value.userId,
			userGender: userInfo.value.gender,
			userAge: 25,
			userLocation: '北京'
		};
		
		addMessage('系统', '发送请求: ' + JSON.stringify(chatData));
		
		// 发送请求（不使用凭证）
		const response = await fetch('http://localhost:8084/xiaozhi/chat', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(chatData),
			credentials: 'omit'  // 不发送凭证，避免跨域问题
		});
		
		if (response.ok) {
			const result = await response.text();
			addMessage('AI', result);
			status.value = '发送成功';
		} else {
			addMessage('系统', `请求失败: HTTP ${response.status}`);
			status.value = '发送失败';
		}
		
	} catch (error) {
		addMessage('系统', '发送失败: ' + error.message);
		status.value = '网络错误';
	} finally {
		loading.value = false;
	}
}

// 测试用户信息
function testUserInfo() {
	addMessage('系统', '当前用户信息: ' + JSON.stringify(userInfo.value));
}

// 测试连接
async function testConnection() {
	status.value = '测试连接中...';
	try {
		const response = await fetch('http://localhost:8084/xiaozhi/health', {
			method: 'GET',
			credentials: 'omit'
		});
		
		if (response.ok || response.status === 405) {
			addMessage('系统', '后端连接正常');
			status.value = '连接正常';
		} else {
			addMessage('系统', `连接异常: HTTP ${response.status}`);
			status.value = '连接异常';
		}
	} catch (error) {
		addMessage('系统', '连接失败: ' + error.message);
		status.value = '连接失败';
	}
}

// 快速测试
function quickTest() {
	inputText.value = '你好，我是测试用户';
	sendMessage();
}

// 清空消息
function clearMessages() {
	messages.value = [];
	addMessage('系统', '消息已清空');
}
</script>

<style lang="scss" scoped>
.simple-chat {
	padding: 20rpx;
	background: #f5f5f5;
	min-height: 100vh;
	
	.header {
		text-align: center;
		margin-bottom: 30rpx;
		
		.title {
			font-size: 36rpx;
			font-weight: bold;
			color: #333;
		}
	}
	
	.user-info {
		background: #fff;
		padding: 20rpx;
		border-radius: 10rpx;
		margin-bottom: 20rpx;
		
		.info-title {
			font-size: 28rpx;
			font-weight: bold;
			color: #333;
			display: block;
			margin-bottom: 10rpx;
		}
		
		.info-item {
			font-size: 24rpx;
			color: #666;
			display: block;
			margin-bottom: 5rpx;
		}
	}
	
	.messages {
		background: #fff;
		padding: 20rpx;
		border-radius: 10rpx;
		margin-bottom: 20rpx;
		max-height: 400rpx;
		overflow-y: auto;
		
		.message {
			margin-bottom: 15rpx;
			padding-bottom: 10rpx;
			border-bottom: 1rpx solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.message-sender {
				font-size: 24rpx;
				font-weight: bold;
				color: #007aff;
				margin-right: 10rpx;
			}
			
			.message-content {
				font-size: 26rpx;
				color: #333;
				line-height: 1.4;
			}
		}
	}
	
	.input-area {
		background: #fff;
		padding: 20rpx;
		border-radius: 10rpx;
		margin-bottom: 20rpx;
		display: flex;
		gap: 15rpx;
		
		.simple-input {
			flex: 1;
			padding: 15rpx;
			border: 2rpx solid #ddd;
			border-radius: 8rpx;
			font-size: 28rpx;
		}
		
		.send-button {
			padding: 15rpx 30rpx;
			background: #007aff;
			color: #fff;
			border: none;
			border-radius: 8rpx;
			font-size: 28rpx;
		}
	}
	
	.test-buttons {
		display: flex;
		flex-wrap: wrap;
		gap: 15rpx;
		margin-bottom: 20rpx;
		
		button {
			flex: 1;
			min-width: 200rpx;
			padding: 20rpx;
			background: #28a745;
			color: #fff;
			border: none;
			border-radius: 8rpx;
			font-size: 24rpx;
		}
	}
	
	.status {
		background: #fff;
		padding: 15rpx;
		border-radius: 8rpx;
		text-align: center;
		
		.status-text {
			font-size: 24rpx;
			color: #666;
			display: block;
		}
		
		.loading-text {
			font-size: 22rpx;
			color: #007aff;
			display: block;
			margin-top: 5rpx;
		}
	}
}
</style>
