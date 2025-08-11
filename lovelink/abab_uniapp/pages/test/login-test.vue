<template>
	<view class="test-page">
		<view class="header">
			<text class="title">登录状态测试</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">当前状态</text>
			
			<view class="status-item">
				<text class="label">Token:</text>
				<text class="value">{{ tokenStatus.token ? tokenStatus.token.substring(0, 30) + '...' : '无' }}</text>
			</view>
			
			<view class="status-item">
				<text class="label">用户信息:</text>
				<text class="value">{{ userInfo ? userInfo.nickname : '无' }}</text>
			</view>
			
			<view class="status-item">
				<text class="label">Token有效性:</text>
				<text class="value" :class="{ valid: tokenValidation.valid, invalid: !tokenValidation.valid }">
					{{ tokenValidation.reason }}
				</text>
			</view>
		</view>
		
		<view class="test-section">
			<text class="section-title">操作</text>
			
			<button @tap="checkStatus" class="test-btn">检查登录状态</button>
			<button @tap="testApi" class="test-btn">测试API调用</button>
			<button @tap="clearLogin" class="test-btn danger">清除登录数据</button>
			<button @tap="mockLogin" class="test-btn">模拟登录</button>
			<button @tap="fixLogin" class="test-btn primary">修复登录</button>
		</view>
		
		<view class="result-section">
			<text class="section-title">测试结果</text>
			<scroll-view class="result-content" scroll-y>
				<text class="result-text">{{ testResult }}</text>
			</scroll-view>
		</view>
		
		<view class="navigation">
			<button @tap="goToMessage" class="nav-btn">返回消息页面</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { 
	checkTokenStatus, 
	validateToken, 
	clearAllLoginData, 
	mockLogin as doMockLogin,
	fixLoginIssue,
	testApiRequest
} from '@/utils/login-fix';

const testResult = ref('等待测试...\n');
const tokenStatus = ref({});
const userInfo = ref(null);
const tokenValidation = ref({ valid: false, reason: '未检查' });

function addResult(text) {
	testResult.value += new Date().toLocaleTimeString() + ': ' + text + '\n';
}

// 检查登录状态
function checkStatus() {
	addResult('开始检查登录状态...');
	
	const token = uni.getStorageSync('token');
	const user = uni.getStorageSync('userInfo');
	
	tokenStatus.value = { token };
	userInfo.value = user;
	
	if (token) {
		tokenValidation.value = validateToken(token);
		addResult(`Token状态: ${tokenValidation.value.reason}`);
	} else {
		tokenValidation.value = { valid: false, reason: '无Token' };
		addResult('没有找到Token');
	}
	
	addResult(`用户信息: ${user ? user.nickname : '无'}`);
}

// 测试API调用
async function testApi() {
	addResult('开始测试API调用...');
	
	try {
		const result = await testApiRequest();
		if (result) {
			addResult('✅ API调用成功: ' + JSON.stringify(result, null, 2));
		} else {
			addResult('❌ API调用失败');
		}
	} catch (error) {
		addResult('❌ API调用异常: ' + error.message);
	}
}

// 清除登录数据
function clearLogin() {
	addResult('清除登录数据...');
	clearAllLoginData();
	checkStatus();
	addResult('✅ 登录数据已清除');
}

// 模拟登录
function mockLogin() {
	addResult('执行模拟登录...');
	const result = doMockLogin();
	checkStatus();
	addResult('✅ 模拟登录完成');
}

// 修复登录
async function fixLogin() {
	addResult('开始修复登录...');
	
	try {
		const result = await fixLoginIssue();
		if (result) {
			addResult('✅ 登录修复成功');
		} else {
			addResult('❌ 登录修复失败，已跳转到登录页');
		}
		checkStatus();
	} catch (error) {
		addResult('❌ 登录修复异常: ' + error.message);
	}
}

// 返回消息页面
function goToMessage() {
	uni.navigateBack({
		fail: () => {
			uni.reLaunch({
				url: '/pages/message/message'
			});
		}
	});
}

onMounted(() => {
	checkStatus();
});
</script>

<style lang="scss" scoped>
.test-page {
	padding: 40rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	min-height: 100vh;
	color: white;
}

.header {
	text-align: center;
	margin-bottom: 40rpx;
	
	.title {
		font-size: 48rpx;
		font-weight: bold;
	}
}

.test-section {
	background: rgba(255, 255, 255, 0.1);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		margin-bottom: 20rpx;
		display: block;
	}
	
	.status-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 15rpx;
		
		.label {
			font-weight: bold;
		}
		
		.value {
			flex: 1;
			text-align: right;
			margin-left: 20rpx;
			word-break: break-all;
			
			&.valid {
				color: #4CAF50;
			}
			
			&.invalid {
				color: #ff4757;
			}
		}
	}
}

.test-btn {
	width: 100%;
	margin-bottom: 20rpx;
	padding: 20rpx;
	border-radius: 15rpx;
	background: rgba(255, 255, 255, 0.2);
	color: white;
	border: none;
	font-size: 28rpx;
	
	&.primary {
		background: #4CAF50;
	}
	
	&.danger {
		background: #ff4757;
	}
	
	&:active {
		transform: scale(0.98);
	}
}

.result-section {
	background: rgba(0, 0, 0, 0.3);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		margin-bottom: 20rpx;
		display: block;
	}
	
	.result-content {
		height: 400rpx;
		
		.result-text {
			font-family: monospace;
			font-size: 24rpx;
			line-height: 1.5;
			white-space: pre-wrap;
		}
	}
}

.navigation {
	.nav-btn {
		width: 100%;
		padding: 25rpx;
		border-radius: 15rpx;
		background: rgba(255, 255, 255, 0.9);
		color: #333;
		border: none;
		font-size: 30rpx;
		font-weight: bold;
	}
}
</style>
