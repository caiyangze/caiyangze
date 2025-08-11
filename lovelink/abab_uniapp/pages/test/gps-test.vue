<template>
	<view class="gps-test-page">
		<view class="header">
			<text class="title">GPS定位测试</text>
		</view>
		
		<view class="content">
			<view class="test-section">
				<button @click="testGPS" :disabled="testing" class="test-btn">
					{{ testing ? '正在定位...' : '开始GPS定位测试' }}
				</button>
			</view>
			
			<view class="result-section" v-if="result">
				<view class="result-title">定位结果：</view>
				<view class="result-item">
					<text class="label">经度：</text>
					<text class="value">{{ result.longitude }}</text>
				</view>
				<view class="result-item">
					<text class="label">纬度：</text>
					<text class="value">{{ result.latitude }}</text>
				</view>
				<view class="result-item">
					<text class="label">精度：</text>
					<text class="value">{{ result.accuracy }}米</text>
				</view>
				<view class="result-item">
					<text class="label">时间：</text>
					<text class="value">{{ result.timestamp }}</text>
				</view>
			</view>
			
			<view class="error-section" v-if="error">
				<view class="error-title">错误信息：</view>
				<text class="error-text">{{ error }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

const testing = ref(false);
const result = ref(null);
const error = ref('');

// GPS定位测试
async function testGPS() {
	if (testing.value) return;

	testing.value = true;
	result.value = null;
	error.value = '';

	try {
		uni.showLoading({ title: '正在定位...' });

		// 尝试简单的定位
		const position = await getSimpleLocation();

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

// 简单定位
function getSimpleLocation() {
	return new Promise((resolve, reject) => {
		// #ifdef H5
		if (!navigator.geolocation) {
			reject(new Error('浏览器不支持定位'));
			return;
		}

		navigator.geolocation.getCurrentPosition(
			(position) => {
				const { longitude, latitude, accuracy } = position.coords;
				const gcj02Coords = wgs84ToGcj02(longitude, latitude);
				resolve({
					longitude: gcj02Coords.longitude,
					latitude: gcj02Coords.latitude,
					accuracy: accuracy
				});
			},
			(error) => {
				let msg = '定位失败';
				if (error.code === 1) msg = '用户拒绝了定位请求';
				else if (error.code === 2) msg = '位置信息不可用';
				else if (error.code === 3) msg = '定位超时';
				reject(new Error(msg));
			},
			{
				enableHighAccuracy: false,
				timeout: 10000,
				maximumAge: 60000
			}
		);
		// #endif

		// #ifndef H5
		uni.getLocation({
			type: 'gcj02',
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



// WGS84转GCJ02坐标系
function wgs84ToGcj02(lng, lat) {
	const PI = 3.1415926535897932384626;
	const a = 6378245.0;
	const ee = 0.00669342162296594323;

	function transformLat(lng, lat) {
		let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
		ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	function transformLng(lng, lat) {
		let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
		ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
		return ret;
	}

	function outOfChina(lng, lat) {
		return (lng < 72.004 || lng > 137.8347) || ((lat < 0.8293 || lat > 55.8271));
	}

	if (outOfChina(lng, lat)) {
		return { longitude: lng, latitude: lat };
	}

	let dlat = transformLat(lng - 105.0, lat - 35.0);
	let dlng = transformLng(lng - 105.0, lat - 35.0);
	const radlat = lat / 180.0 * PI;
	let magic = Math.sin(radlat);
	magic = 1 - ee * magic * magic;
	const sqrtmagic = Math.sqrt(magic);
	dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * PI);
	dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * PI);
	const mglat = lat + dlat;
	const mglng = lng + dlng;

	return { longitude: mglng, latitude: mglat };
}
</script>

<style lang="scss" scoped>
.gps-test-page {
	padding: 40rpx;
	background: #f5f5f5;
	min-height: 100vh;
}

.header {
	text-align: center;
	margin-bottom: 60rpx;
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}
}

.content {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
}

.test-section {
	text-align: center;
	margin-bottom: 40rpx;
	
	.test-btn {
		background: #007AFF;
		color: #fff;
		border: none;
		border-radius: 10rpx;
		padding: 20rpx 40rpx;
		font-size: 32rpx;
		
		&[disabled] {
			background: #ccc;
		}
	}
}

.result-section {
	margin-bottom: 40rpx;
	
	.result-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.result-item {
		display: flex;
		margin-bottom: 15rpx;
		
		.label {
			width: 120rpx;
			color: #666;
			font-size: 28rpx;
		}
		
		.value {
			flex: 1;
			color: #333;
			font-size: 28rpx;
			font-family: monospace;
		}
	}
}

.error-section {
	.error-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #ff3b30;
		margin-bottom: 20rpx;
	}
	
	.error-text {
		color: #ff3b30;
		font-size: 28rpx;
		line-height: 1.5;
	}
}
</style>
