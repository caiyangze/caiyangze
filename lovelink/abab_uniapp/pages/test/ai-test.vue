<template>
	<view class="test-page">
		<view class="header">
			<text class="title">AI红娘测试页面</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">连接测试</text>
			<view class="test-item">
				<button @tap="testConnection" :disabled="testing">
					{{ testing ? '测试中...' : '测试后端连接' }}
				</button>
			</view>
			<view class="result" v-if="connectionResult">
				<text :class="connectionResult.success ? 'success' : 'error'">
					{{ connectionResult.message }}
				</text>
			</view>
		</view>
		
		<view class="test-section">
			<text class="section-title">AI聊天测试</text>
			<view class="test-item">
				<input 
					class="test-input" 
					v-model="testMessage" 
					placeholder="输入测试消息"
				/>
				<button @tap="testAiChat" :disabled="chatTesting">
					{{ chatTesting ? '发送中...' : '发送消息' }}
				</button>
			</view>
			<view class="chat-result" v-if="chatResult">
				<text class="result-title">AI回复：</text>
				<text class="result-content">{{ chatResult }}</text>
			</view>
		</view>
		
		<view class="test-section">
			<text class="section-title">用户信息</text>
			<view class="user-info">
				<text>用户ID: {{ userInfo.userId }}</text>
				<text>性别: {{ userInfo.gender === 0 ? '女' : userInfo.gender === 1 ? '男' : '未设置' }}</text>
				<text>年龄: {{ userInfo.age || '未设置' }}</text>
				<text>地区: {{ userInfo.location || '未设置' }}</text>
			</view>
		</view>
		
		<view class="test-section">
			<text class="section-title">快速测试</text>
			<view class="quick-tests">
				<button @tap="quickTest('你好')">测试问候</button>
				<button @tap="quickTest('请推荐用户')">测试推荐</button>
				<button @tap="quickTest('生成图片：浪漫约会')">测试文生图</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { chatWithAi } from '@/api/ai-chat.js';

// 响应式数据
const testing = ref(false);
const chatTesting = ref(false);
const connectionResult = ref(null);
const chatResult = ref('');
const testMessage = ref('你好，我想找个对象');

// 用户信息
const userInfo = ref({
	userId: null,
	gender: null,
	age: null,
	location: null
});

// 页面加载
onMounted(() => {
	initUserInfo();
});

// 初始化用户信息
function initUserInfo() {
	try {
		const userInfoStr = uni.getStorageSync('userInfo');
		if (userInfoStr) {
			const userData = JSON.parse(userInfoStr);
			userInfo.value = {
				userId: userData.userId || Date.now(),
				gender: userData.gender,
				age: userData.age,
				location: userData.location || userData.city
			};
		} else {
			userInfo.value.userId = Date.now();
		}
	} catch (error) {
		console.error('用户信息加载失败:', error);
		userInfo.value.userId = Date.now();
	}
}

// 测试后端连接
async function testConnection() {
	testing.value = true;
	connectionResult.value = null;
	
	try {
		// 简单的连接测试
		const response = await fetch('http://localhost:8084/xiaozhi/chat', {
			method: 'OPTIONS',
			headers: {
				'Content-Type': 'application/json'
			}
		});
		
		if (response.ok || response.status === 405) {
			connectionResult.value = {
				success: true,
				message: '后端连接正常 ✓'
			};
		} else {
			connectionResult.value = {
				success: false,
				message: `连接失败: HTTP ${response.status}`
			};
		}
	} catch (error) {
		connectionResult.value = {
			success: false,
			message: '连接失败: ' + error.message
		};
	} finally {
		testing.value = false;
	}
}

// 测试AI聊天
async function testAiChat() {
	if (!testMessage.value.trim()) {
		uni.showToast({
			title: '请输入测试消息',
			icon: 'none'
		});
		return;
	}
	
	chatTesting.value = true;
	chatResult.value = '';
	
	try {
		const chatData = {
			memoryId: Date.now(),
			message: testMessage.value,
			messageType: 'text',
			userId: userInfo.value.userId,
			userGender: userInfo.value.gender,
			userAge: userInfo.value.age,
			userLocation: userInfo.value.location
		};
		
		const response = await chatWithAi(chatData);
		
		// 设置流式数据回调
		response.onData = (chunk) => {
			chatResult.value += chunk;
		};
		
		// 设置完成回调
		response.onComplete = (fullResponse) => {
			chatTesting.value = false;
		};
		
	} catch (error) {
		chatResult.value = '测试失败: ' + error.message;
		chatTesting.value = false;
		
		uni.showToast({
			title: '聊天测试失败',
			icon: 'none'
		});
	}
}

// 快速测试
function quickTest(message) {
	testMessage.value = message;
	testAiChat();
}
</script>

<style lang="scss" scoped>
.test-page {
	padding: 20rpx;
	background: #f5f5f5;
	min-height: 100vh;
	
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
		background: #fff;
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		
		.section-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20rpx;
			display: block;
		}
		
		.test-item {
			margin-bottom: 20rpx;
			
			.test-input {
				width: 100%;
				padding: 20rpx;
				border: 2rpx solid #ddd;
				border-radius: 10rpx;
				margin-bottom: 20rpx;
				font-size: 28rpx;
			}
			
			button {
				width: 100%;
				padding: 20rpx;
				background: #007aff;
				color: #fff;
				border: none;
				border-radius: 10rpx;
				font-size: 28rpx;
				
				&:disabled {
					background: #ccc;
				}
			}
		}
		
		.result {
			padding: 20rpx;
			border-radius: 10rpx;
			
			.success {
				color: #4cd964;
				background: #f0f9f0;
				padding: 10rpx;
				border-radius: 5rpx;
			}
			
			.error {
				color: #ff3b30;
				background: #fff0f0;
				padding: 10rpx;
				border-radius: 5rpx;
			}
		}
		
		.chat-result {
			.result-title {
				font-weight: bold;
				color: #333;
				display: block;
				margin-bottom: 10rpx;
			}
			
			.result-content {
				color: #666;
				line-height: 1.5;
				background: #f8f8f8;
				padding: 20rpx;
				border-radius: 10rpx;
				display: block;
			}
		}
		
		.user-info {
			text {
				display: block;
				margin-bottom: 10rpx;
				color: #666;
				font-size: 26rpx;
			}
		}
		
		.quick-tests {
			display: flex;
			flex-direction: column;
			gap: 15rpx;
			
			button {
				padding: 15rpx;
				background: #28a745;
				color: #fff;
				border: none;
				border-radius: 8rpx;
				font-size: 26rpx;
			}
		}
	}
}
</style>
