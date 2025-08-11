<template>
	<view class="simple-location">
		<view class="header">
			<text class="title">简单定位测试</text>
		</view>
		
		<view class="content">
			<!-- 测试按钮 -->
			<view class="test-buttons">
				<button @click="testLocation" :disabled="testing" class="test-btn">
					{{ testing ? '测试中...' : '开始定位' }}
				</button>
				
				<button @click="checkPermission" class="test-btn">
					检查权限
				</button>
				
				<button @click="showHelp" class="test-btn">
					帮助
				</button>
			</view>
			
			<!-- 结果显示 -->
			<view class="result-section" v-if="result || error">
				<view class="result-title">测试结果：</view>
				
				<view v-if="result" class="result-success">
					<text class="result-label">✅ 定位成功</text>
					<text>经度：{{ result.longitude }}</text>
					<text>纬度：{{ result.latitude }}</text>
					<text>精度：{{ result.accuracy }}米</text>
					<text>时间：{{ result.timestamp }}</text>
				</view>
				
				<view v-if="error" class="result-error">
					<text class="result-label">❌ 定位失败</text>
					<text>{{ error }}</text>
				</view>
			</view>
			
			<!-- 权限状态 -->
			<view class="permission-section" v-if="permissionStatus">
				<view class="result-title">权限状态：</view>
				<text class="permission-text" :class="permissionStatus">
					{{ permissionStatusText }}
				</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const testing = ref(false);
const result = ref(null);
const error = ref('');
const permissionStatus = ref('');
const permissionStatusText = ref('');

// 测试定位
async function testLocation() {
	if (testing.value) return;
	
	testing.value = true;
	result.value = null;
	error.value = '';
	
	try {
		uni.showLoading({ title: '正在定位...' });
		
		const position = await getLocation();
		
		result.value = {
			longitude: position.longitude.toFixed(6),
			latitude: position.latitude.toFixed(6),
			accuracy: position.accuracy ? Math.round(position.accuracy) : '未知',
			timestamp: new Date().toLocaleString()
		};
		
		uni.hideLoading();
		uni.showToast({
			title: '定位成功',
			icon: 'success'
		});
		
	} catch (err) {
		error.value = err.message;
		uni.hideLoading();
		uni.showToast({
			title: '定位失败',
			icon: 'error'
		});
	} finally {
		testing.value = false;
	}
}

// 获取位置（超简化版本）
function getLocation() {
	return new Promise((resolve, reject) => {
		console.log('开始定位测试...');

		// #ifdef H5
		if (!navigator.geolocation) {
			reject(new Error('浏览器不支持定位'));
			return;
		}

		// 使用最简单的配置
		const options = {
			enableHighAccuracy: false,  // 不要求高精度
			timeout: 3000,              // 3秒超时
			maximumAge: 60000           // 1分钟缓存
		};

		navigator.geolocation.getCurrentPosition(
			(position) => {
				console.log('定位成功:', position);
				const { longitude, latitude, accuracy } = position.coords;
				resolve({
					longitude: longitude,
					latitude: latitude,
					accuracy: accuracy || 0
				});
			},
			(error) => {
				console.error('定位失败:', error);
				let msg = '定位失败';
				switch (error.code) {
					case 1:
						msg = '用户拒绝了定位请求';
						break;
					case 2:
						msg = '位置信息不可用';
						break;
					case 3:
						msg = '定位请求超时（3秒）';
						break;
					default:
						msg = `定位失败: ${error.message}`;
				}
				reject(new Error(msg));
			},
			options
		);
		// #endif

		// #ifndef H5
		uni.getLocation({
			type: 'wgs84',
			success: (res) => resolve({
				longitude: res.longitude,
				latitude: res.latitude,
				accuracy: res.accuracy || 0
			}),
			fail: () => reject(new Error('设备定位失败'))
		});
		// #endif
	});
}

// 检查权限
async function checkPermission() {
	// #ifdef H5
	if (!navigator.geolocation) {
		permissionStatus.value = 'unsupported';
		permissionStatusText.value = '浏览器不支持定位';
		return;
	}

	if (navigator.permissions) {
		try {
			const result = await navigator.permissions.query({name: 'geolocation'});
			permissionStatus.value = result.state;
			
			switch (result.state) {
				case 'granted':
					permissionStatusText.value = '✅ 已授权';
					break;
				case 'denied':
					permissionStatusText.value = '❌ 已拒绝';
					break;
				case 'prompt':
					permissionStatusText.value = '❓ 待询问';
					break;
			}
		} catch (error) {
			permissionStatus.value = 'error';
			permissionStatusText.value = '检查失败';
		}
	} else {
		permissionStatus.value = 'unknown';
		permissionStatusText.value = '无法检查权限状态';
	}
	// #endif
}

// 显示帮助
function showHelp() {
	uni.showModal({
		title: '定位帮助',
		content: '如果定位失败：\n\n1. 点击地址栏左侧位置图标\n2. 选择"允许"位置访问\n3. 刷新页面重试\n\n4. 确保GPS已开启\n5. 移动到室外或窗边',
		confirmText: '知道了',
		showCancel: false
	});
}
</script>

<style scoped>
.simple-location {
	padding: 20px;
	background: #f5f5f5;
	min-height: 100vh;
}

.header {
	text-align: center;
	margin-bottom: 30px;
}

.title {
	font-size: 24px;
	font-weight: bold;
	color: #333;
}

.test-buttons {
	display: flex;
	flex-direction: column;
	gap: 15px;
	margin-bottom: 30px;
}

.test-btn {
	background: #007AFF;
	color: white;
	border: none;
	border-radius: 8px;
	padding: 15px;
	font-size: 16px;
}

.test-btn:disabled {
	background: #ccc;
}

.result-section, .permission-section {
	background: white;
	border-radius: 8px;
	padding: 20px;
	margin-bottom: 20px;
}

.result-title {
	font-size: 18px;
	font-weight: bold;
	margin-bottom: 15px;
	color: #333;
}

.result-success {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.result-success text {
	color: #333;
}

.result-error {
	color: #ff3333;
}

.result-label {
	font-weight: bold;
	margin-bottom: 10px;
}

.permission-text {
	font-size: 16px;
	font-weight: bold;
}

.permission-text.granted {
	color: #00C851;
}

.permission-text.denied {
	color: #ff3333;
}

.permission-text.prompt {
	color: #ffbb33;
}
</style>
