<template>
	<view class="test-page">
		<view class="header">
			<text class="title">导航测试</text>
		</view>
		
		<view class="test-section">
			<button @tap="testChatNavigation" class="test-btn">测试聊天页面导航</button>
			<button @tap="testMessageNavigation" class="test-btn">测试消息页面导航</button>
		</view>
		
		<view class="result-section">
			<text class="section-title">测试结果</text>
			<text class="result-text">{{ testResult }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const testResult = ref('等待测试...');

// 测试聊天页面导航
function testChatNavigation() {
	try {
		testResult.value = '正在测试聊天页面导航...';
		
		uni.navigateTo({
			url: '/pages/message/chat?conversationId=new&userId=1&name=测试用户&avatar=/static/message/default-avatar.png',
			success: () => {
				testResult.value = '聊天页面导航成功！';
			},
			fail: (error) => {
				testResult.value = '聊天页面导航失败：' + JSON.stringify(error);
			}
		});
	} catch (error) {
		testResult.value = '导航异常：' + error.message;
	}
}

// 测试消息页面导航
function testMessageNavigation() {
	try {
		testResult.value = '正在测试消息页面导航...';
		
		uni.navigateTo({
			url: '/pages/message/message',
			success: () => {
				testResult.value = '消息页面导航成功！';
			},
			fail: (error) => {
				testResult.value = '消息页面导航失败：' + JSON.stringify(error);
			}
		});
	} catch (error) {
		testResult.value = '导航异常：' + error.message;
	}
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

.test-section, .result-section {
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

.test-btn {
	width: 100%;
	height: 80rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	border: none;
	border-radius: 40rpx;
	font-size: 28rpx;
	margin-bottom: 20rpx;
}

.result-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.5;
}
</style>
