<template>
	<view class="test-page">
		<view class="header">
			<text class="title">简单API测试</text>
		</view>
		
		<view class="test-section">
			<view class="section-title">1. 测试后端连接</view>
			<button @tap="testBackend" :disabled="loading">测试后端服务</button>
			<view class="result" v-if="backendResult">{{ backendResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">2. 测试用户服务</view>
			<button @tap="testUserService" :disabled="loading">测试用户服务</button>
			<view class="result" v-if="userResult">{{ userResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">3. 测试社交服务</view>
			<button @tap="testSocialService" :disabled="loading">测试社交服务</button>
			<view class="result" v-if="socialResult">{{ socialResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">4. 测试位置上传</view>
			<button @tap="testLocationUpload" :disabled="loading">上传测试位置</button>
			<view class="result" v-if="locationResult">{{ locationResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">5. 添加测试用户数据</view>
			<button @tap="addTestUsers" :disabled="loading">添加测试用户</button>
			<view class="result" v-if="testUsersResult">{{ testUsersResult }}</view>
		</view>

		<view class="test-section">
			<view class="section-title">6. 测试附近用户查询</view>
			<button @tap="testNearbyQuery" :disabled="loading">查询附近用户</button>
			<view class="result" v-if="nearbyResult">{{ nearbyResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">调试信息</view>
			<view class="debug-info">
				<text>当前环境: {{ currentEnv }}</text>
				<text>用户服务URL: {{ userServiceUrl }}</text>
				<text>社交服务URL: {{ socialServiceUrl }}</text>
				<text>Token: {{ token ? '已设置' : '未设置' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import http from '@/api/http';
import config from '@/api/config';

// 状态
const loading = ref(false);
const backendResult = ref('');
const userResult = ref('');
const socialResult = ref('');
const locationResult = ref('');
const testUsersResult = ref('');
const nearbyResult = ref('');

// 调试信息
const currentEnv = ref('');
const userServiceUrl = ref('');
const socialServiceUrl = ref('');
const token = ref('');

onMounted(() => {
	// 获取调试信息
	currentEnv.value = config.CURRENT_ENV;
	userServiceUrl.value = config.getBaseUrl();
	socialServiceUrl.value = config.getSocialUrl();
	token.value = uni.getStorageSync('token');
});

// 测试后端连接
async function testBackend() {
	try {
		loading.value = true;
		backendResult.value = '正在测试...';

		console.log('开始测试后端连接...');

		// 使用uni.request直接测试
		uni.request({
			url: 'http://localhost:9002/api/social/location/test',
			method: 'GET',
			success: (res) => {
				console.log('后端测试成功:', res);
				backendResult.value = `成功: ${JSON.stringify(res.data)}`;

				uni.showToast({
					title: '后端连接成功',
					icon: 'success'
				});

				loading.value = false;
			},
			fail: (error) => {
				console.error('后端测试失败:', error);
				backendResult.value = `失败: ${JSON.stringify(error)}`;

				uni.showToast({
					title: '后端连接失败',
					icon: 'none'
				});

				loading.value = false;
			}
		});
	} catch (error) {
		backendResult.value = `异常: ${error.message}`;
		console.error('后端测试异常:', error);
		loading.value = false;
	}
}

// 测试用户服务
async function testUserService() {
	try {
		loading.value = true;
		userResult.value = '正在测试...';
		
		const result = await http.request({
			url: '/api/user/info',
			method: 'GET',
			hideErrorToast: true
		});
		
		userResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('用户服务测试成功:', result);
	} catch (error) {
		userResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('用户服务测试失败:', error);
	} finally {
		loading.value = false;
	}
}

// 测试社交服务
async function testSocialService() {
	try {
		loading.value = true;
		socialResult.value = '正在测试...';
		
		const result = await http.request({
			url: '/api/social/location/test',
			method: 'GET',
			hideErrorToast: true
		});
		
		socialResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('社交服务测试成功:', result);
	} catch (error) {
		socialResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('社交服务测试失败:', error);
	} finally {
		loading.value = false;
	}
}

// 测试位置上传
async function testLocationUpload() {
	try {
		loading.value = true;
		locationResult.value = '正在测试...';
		
		const result = await http.request({
			url: '/api/social/location/update',
			method: 'POST',
			data: {
				longitude: 116.397428,
				latitude: 39.90923,
				locationName: '测试位置',
				address: '北京市测试地址'
			},
			hideErrorToast: true
		});
		
		locationResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('位置上传测试成功:', result);
	} catch (error) {
		locationResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('位置上传测试失败:', error);
	} finally {
		loading.value = false;
	}
}

// 添加测试用户数据
async function addTestUsers() {
	try {
		loading.value = true;
		testUsersResult.value = '正在添加测试用户...';

		const result = await http.request({
			url: '/api/social/location/test/addTestUsers',
			method: 'POST',
			hideErrorToast: true
		});

		testUsersResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('添加测试用户成功:', result);

		uni.showToast({
			title: '测试用户添加成功',
			icon: 'success'
		});
	} catch (error) {
		testUsersResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('添加测试用户失败:', error);
	} finally {
		loading.value = false;
	}
}

// 测试附近用户查询
async function testNearbyQuery() {
	try {
		loading.value = true;
		nearbyResult.value = '正在测试...';
		
		const result = await http.request({
			url: '/api/social/location/nearby',
			method: 'POST',
			data: {
				longitude: 116.397428,
				latitude: 39.90923,
				radius: 10,
				limit: 20,
				gender: 0
			},
			hideErrorToast: true
		});
		
		nearbyResult.value = `成功: 找到${result.data ? result.data.length : 0}个用户`;
		console.log('附近用户查询测试成功:', result);
	} catch (error) {
		nearbyResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('附近用户查询测试失败:', error);
	} finally {
		loading.value = false;
	}
}
</script>

<style lang="scss" scoped>
.test-page {
	padding: 20rpx;
	
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
		margin-bottom: 30rpx;
		padding: 20rpx;
		background: #f5f5f5;
		border-radius: 10rpx;
		
		.section-title {
			font-size: 28rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 15rpx;
		}
		
		button {
			width: 100%;
			margin-bottom: 15rpx;
			background: #007aff;
			color: white;
			border: none;
			border-radius: 8rpx;
			padding: 20rpx;
		}
		
		.result {
			background: #fff;
			padding: 15rpx;
			border-radius: 8rpx;
			font-size: 24rpx;
			color: #666;
			word-break: break-all;
		}
	}
	
	.debug-info {
		background: #fff;
		padding: 15rpx;
		border-radius: 8rpx;
		
		text {
			display: block;
			margin-bottom: 8rpx;
			font-size: 24rpx;
			color: #666;
		}
	}
}
</style>
