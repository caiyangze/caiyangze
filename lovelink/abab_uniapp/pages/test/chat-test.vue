<template>
	<view class="test-page">
		<view class="header">
			<text class="title">聊天功能测试</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">API测试</text>

			<button @tap="testMutualFriends" class="test-btn">测试获取好友列表</button>
			<button @tap="testCanChat" class="test-btn">测试canChat接口</button>
			<button @tap="testWebSocket" class="test-btn">测试WebSocket连接</button>
			<button @tap="testSendMessage" class="test-btn">测试发送消息</button>
		</view>

		<view class="test-section">
			<text class="section-title">canChat测试</text>
			<input
				class="test-input"
				v-model="targetUserId"
				placeholder="输入目标用户ID"
				type="number"
			/>
			<button @tap="testCanChatWithId" class="test-btn">测试指定用户</button>
		</view>
		
		<view class="result-section">
			<text class="section-title">测试结果</text>
			<scroll-view class="result-content" scroll-y>
				<text class="result-text">{{ testResult }}</text>
			</scroll-view>
		</view>
		
		<view class="navigation">
			<button @tap="goToMessage" class="nav-btn">进入消息页面</button>
			<button @tap="goToChat" class="nav-btn">进入聊天页面</button>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { getMutualFollowList, canChat } from '@/api/chat';
import wsManager from '@/utils/websocket';

const testResult = ref('等待测试...\n');
const targetUserId = ref('');

function addResult(text) {
	testResult.value += new Date().toLocaleTimeString() + ': ' + text + '\n';
}

// 测试获取好友列表
async function testMutualFriends() {
	try {
		addResult('开始测试获取好友列表...');
		const response = await getMutualFollowList();
		addResult('API响应: ' + JSON.stringify(response, null, 2));
	} catch (error) {
		addResult('测试失败: ' + error.message);
	}
}

// 测试canChat接口（使用默认用户ID）
async function testCanChat() {
	try {
		addResult('开始测试canChat接口（默认用户ID: 1）...');
		const response = await canChat(1);
		addResult('canChat响应: ' + JSON.stringify(response, null, 2));

		if (response.code === 200) {
			if (response.data === true) {
				addResult('✅ 可以与用户1聊天（互相关注）');
			} else {
				addResult('❌ 不能与用户1聊天（未互相关注）');
			}
		} else {
			addResult('❌ 检查失败: ' + response.message);
		}
	} catch (error) {
		addResult('测试失败: ' + error.message);
	}
}

// 测试指定用户ID的canChat
async function testCanChatWithId() {
	if (!targetUserId.value) {
		addResult('❌ 请输入目标用户ID');
		return;
	}

	try {
		addResult(`开始测试canChat接口（用户ID: ${targetUserId.value}）...`);
		const response = await canChat(targetUserId.value);
		addResult('canChat响应: ' + JSON.stringify(response, null, 2));

		if (response.code === 200) {
			if (response.data === true) {
				addResult(`✅ 可以与用户${targetUserId.value}聊天（互相关注）`);
			} else {
				addResult(`❌ 不能与用户${targetUserId.value}聊天（未互相关注）`);
			}
		} else {
			addResult('❌ 检查失败: ' + response.message);
		}
	} catch (error) {
		addResult('测试失败: ' + error.message);
	}
}

// 测试WebSocket连接
async function testWebSocket() {
	try {
		addResult('开始测试WebSocket连接...');
		const token = uni.getStorageSync('token');
		if (!token) {
			addResult('错误: 未找到token，请先登录');
			return;
		}
		
		await wsManager.connect(token);
		addResult('WebSocket连接成功');
		
		// 监听消息
		wsManager.onMessage('CONNECTED', (message) => {
			addResult('收到连接确认: ' + JSON.stringify(message));
		});
		
		wsManager.onMessage('HEARTBEAT', (message) => {
			addResult('收到心跳响应: ' + JSON.stringify(message));
		});
		
	} catch (error) {
		addResult('WebSocket连接失败: ' + error.message);
	}
}

// 测试发送消息
function testSendMessage() {
	try {
		addResult('开始测试发送消息...');
		
		if (!wsManager.getConnectionStatus().isConnected) {
			addResult('错误: WebSocket未连接');
			return;
		}
		
		// 发送测试消息（需要一个有效的接收者ID）
		const success = wsManager.sendChatMessage(1, '这是一条测试消息', 1);
		if (success) {
			addResult('消息发送成功');
		} else {
			addResult('消息发送失败');
		}
		
	} catch (error) {
		addResult('发送消息失败: ' + error.message);
	}
}

// 导航到消息页面
function goToMessage() {
	uni.navigateTo({
		url: '/pages/message/message'
	});
}

// 导航到聊天页面（测试用）
function goToChat() {
	uni.navigateTo({
		url: '/pages/message/chat?conversationId=new&userId=1&name=测试用户&avatar=/static/message/default-avatar.png'
	});
}
</script>

<style lang="scss" scoped>
.test-page {
	padding: 40rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	min-height: 100vh;
}

.header {
	text-align: center;
	margin-bottom: 40rpx;
	
	.title {
		font-size: 48rpx;
		font-weight: bold;
		color: #fff;
	}
}

.test-section, .result-section, .navigation {
	background: rgba(255, 255, 255, 0.9);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
	display: block;
}

.test-btn, .nav-btn {
	width: 100%;
	height: 80rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	border: none;
	border-radius: 40rpx;
	font-size: 28rpx;
	margin-bottom: 20rpx;
}

.test-input {
	width: 100%;
	height: 80rpx;
	border: 2rpx solid #ddd;
	border-radius: 10rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	margin-bottom: 20rpx;
	box-sizing: border-box;
}

.result-content {
	height: 400rpx;
	background: #f5f5f5;
	border-radius: 10rpx;
	padding: 20rpx;
}

.result-text {
	font-size: 24rpx;
	color: #333;
	line-height: 1.5;
	white-space: pre-wrap;
}
</style>
