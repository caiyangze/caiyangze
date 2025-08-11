<template>
	<view class="test-page">
		<view class="header">
			<text class="title">位置服务测试</text>
		</view>
		
		<view class="test-section">
			<view class="section-title">API连接测试</view>
			<button @tap="testApi" :disabled="loading">测试API连接</button>
			<view class="result" v-if="apiResult">{{ apiResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">地理位置测试</view>
			<button @tap="testLocation" :disabled="loading">获取地理位置</button>
			<view class="result" v-if="locationResult">
				<text>经度: {{ locationResult.longitude }}</text>
				<text>纬度: {{ locationResult.latitude }}</text>
			</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">上传位置测试</view>
			<button @tap="testUploadLocation" :disabled="loading || !locationResult">上传位置</button>
			<view class="result" v-if="uploadResult">{{ uploadResult }}</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">查询附近用户测试</view>
			<button @tap="testNearbyUsers" :disabled="loading || !locationResult">查询附近用户</button>
			<view class="result" v-if="nearbyResult">
				<text>找到 {{ nearbyResult.length }} 个附近用户</text>
				<view v-for="user in nearbyResult" :key="user.userId" class="user-item">
					<text>用户ID: {{ user.userId }}</text>
					<text>距离: {{ user.distance }}km</text>
				</view>
			</view>
		</view>
		
		<view class="test-section">
			<view class="section-title">地图测试</view>
			<view class="map-container">
				<map
					id="testMap"
					class="map"
					:longitude="currentLocation.longitude"
					:latitude="currentLocation.latitude"
					:scale="13"
					:markers="mapMarkers"
					:show-location="true"
				></map>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { getUserGeolocation, updateUserLocation, getNearbyUsers } from '@/api/location';
import http from '@/api/http';

// 状态
const loading = ref(false);
const apiResult = ref('');
const locationResult = ref(null);
const uploadResult = ref('');
const nearbyResult = ref([]);

// 当前位置
const currentLocation = reactive({
	longitude: 116.397428,
	latitude: 39.90923
});

// 地图标记
const mapMarkers = ref([]);

// 测试API连接
async function testApi() {
	try {
		loading.value = true;
		apiResult.value = '正在测试...';

		console.log('开始测试API连接...');

		const result = await http.request({
			url: '/api/social/location/test',
			method: 'GET',
			hideErrorToast: true
		});

		apiResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('API测试结果:', result);

		uni.showToast({
			title: 'API连接成功',
			icon: 'success'
		});
	} catch (error) {
		apiResult.value = `失败: ${error.message || JSON.stringify(error)}`;
		console.error('API测试失败:', error);

		uni.showToast({
			title: 'API连接失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
}

// 测试地理位置获取
async function testLocation() {
	try {
		loading.value = true;
		locationResult.value = null;

		console.log('开始获取地理位置...');

		// 使用uni.getLocation直接获取位置
		uni.getLocation({
			type: 'gcj02',
			success: (res) => {
				console.log('位置获取成功:', res);

				const position = {
					longitude: res.longitude,
					latitude: res.latitude,
					accuracy: res.accuracy,
					altitude: res.altitude,
					speed: res.speed
				};

				locationResult.value = position;
				currentLocation.longitude = position.longitude;
				currentLocation.latitude = position.latitude;

				// 更新地图标记
				mapMarkers.value = [{
					id: 'current',
					longitude: position.longitude,
					latitude: position.latitude,
					iconPath: '/static/default-avatar.png',
					width: 30,
					height: 30,
					callout: {
						content: '我的位置',
						color: '#ffffff',
						fontSize: 12,
						borderRadius: 4,
						bgColor: '#FF3B30',
						padding: 8
					}
				}];

				uni.showToast({
					title: '位置获取成功',
					icon: 'success'
				});

				loading.value = false;
			},
			fail: (error) => {
				console.error('位置获取失败:', error);
				locationResult.value = null;

				uni.showToast({
					title: '位置获取失败',
					icon: 'none'
				});

				loading.value = false;
			}
		});
	} catch (error) {
		locationResult.value = null;
		console.error('位置获取异常:', error);
		uni.showToast({
			title: '位置获取异常',
			icon: 'none'
		});
		loading.value = false;
	}
}

// 测试上传位置
async function testUploadLocation() {
	if (!locationResult.value) {
		uni.showToast({
			title: '请先获取位置',
			icon: 'none'
		});
		return;
	}
	
	try {
		loading.value = true;
		uploadResult.value = '正在上传...';
		
		const result = await updateUserLocation({
			longitude: locationResult.value.longitude,
			latitude: locationResult.value.latitude,
			locationName: '测试位置',
			address: '测试地址'
		});
		
		uploadResult.value = `成功: ${JSON.stringify(result)}`;
		console.log('位置上传成功:', result);
	} catch (error) {
		uploadResult.value = `失败: ${error.message}`;
		console.error('位置上传失败:', error);
	} finally {
		loading.value = false;
	}
}

// 测试查询附近用户
async function testNearbyUsers() {
	if (!locationResult.value) {
		uni.showToast({
			title: '请先获取位置',
			icon: 'none'
		});
		return;
	}
	
	try {
		loading.value = true;
		nearbyResult.value = [];
		
		const result = await getNearbyUsers({
			longitude: locationResult.value.longitude,
			latitude: locationResult.value.latitude,
			radius: 10,
			limit: 20,
			gender: 0
		});
		
		nearbyResult.value = result.data || [];
		console.log('附近用户查询成功:', result);
	} catch (error) {
		nearbyResult.value = [];
		console.error('附近用户查询失败:', error);
		uni.showToast({
			title: error.message,
			icon: 'none'
		});
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
		margin-bottom: 40rpx;
		padding: 20rpx;
		background: #f5f5f5;
		border-radius: 10rpx;
		
		.section-title {
			font-size: 28rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20rpx;
		}
		
		button {
			width: 100%;
			margin-bottom: 20rpx;
		}
		
		.result {
			background: #fff;
			padding: 20rpx;
			border-radius: 8rpx;
			font-size: 24rpx;
			color: #666;
			
			text {
				display: block;
				margin-bottom: 10rpx;
			}
			
			.user-item {
				border-bottom: 1rpx solid #eee;
				padding: 10rpx 0;
				
				&:last-child {
					border-bottom: none;
				}
			}
		}
	}
	
	.map-container {
		height: 400rpx;
		border-radius: 10rpx;
		overflow: hidden;
		
		.map {
			width: 100%;
			height: 100%;
		}
	}
}
</style>
