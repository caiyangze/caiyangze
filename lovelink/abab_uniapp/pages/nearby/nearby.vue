<template>
	<view class="nearby-page">
		<!-- 背景层 -->
		<view class="bg-layer" :style="{ background: computedBg }">
			<view class="bg-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部工具栏 -->
			<view class="header">
				<view class="back-btn" @tap="goBack">
					<text class="back-icon">←</text>
				</view>
				<text class="title">附近的人</text>
				<view class="filter-btn" @tap="showFilterModal">
					<text class="filter-icon">⚙</text>
				</view>
			</view>
			
			<!-- 地图容器 -->
			<view class="map-container">
				<map
					v-if="!mapError"
					id="nearbyMap"
					class="map"
					:longitude="currentLocation.longitude"
					:latitude="currentLocation.latitude"
					:scale="15"
					:markers="mapMarkers"
					:circles="mapCircles"
					:show-location="true"
					:enable-3D="false"
					:enable-overlooking="false"
					:enable-satellite="false"
					:enable-traffic="false"
					:enable-poi="true"
					:enable-building="true"
					@markertap="onMarkerTap"
					@regionchange="onRegionChange"
					@error="onMapError"
					@tap="onMapTap"
				></map>

				<!-- 地图加载失败时的备用显示 -->
				<view v-if="mapError" class="map-fallback">
					<view class="fallback-content">
						<text class="fallback-icon">🗺️</text>
						<text class="fallback-title">地图暂时无法加载</text>
						<text class="fallback-desc">请使用下方列表查看附近用户</text>
						<view class="retry-btn" @tap="retryMap">
							<text>重试</text>
						</view>
					</view>
				</view>
				
				<!-- 地图控制按钮 -->
				<view class="map-controls">
					<view class="control-btn my-location-btn" @tap="backToMyLocation">
						<text class="control-icon">📍</text>
					</view>
					<view
						class="control-btn label-toggle-btn"
						:class="{ active: showUserLabels }"
						@tap="toggleUserLabels"
					>
						<text class="control-icon">🏷️</text>
					</view>
					<view class="control-btn map-view-btn" @tap="goToMapView">
						<text class="control-icon">🗺️</text>
					</view>
				</view>
			</view>
			
			<!-- 用户列表 -->
			<view class="user-list">
				<view class="list-header">
					<text class="list-title">附近用户 ({{ nearbyUsers.length }})</text>
					<view class="header-actions">
						<view class="view-toggle">
							<text
								class="toggle-btn"
								:class="{ active: viewMode === 'list' }"
								@tap="switchViewMode('list')"
							>列表</text>
							<text
								class="toggle-btn"
								:class="{ active: viewMode === 'map' }"
								@tap="switchViewMode('map')"
							>🗺️</text>
						</view>
					</view>
				</view>
				
				<scroll-view 
					class="user-scroll" 
					scroll-y="true"
					v-show="viewMode === 'list'"
					@scrolltolower="loadMoreUsers"
				>
					<view 
						v-for="(user, index) in nearbyUsers" 
						:key="user.userId" 
						class="user-item"
						@tap="viewUserDetail(user)"
					>
						<image
							class="user-avatar"
							:src="user.avatar || '/static/default-avatar.png'"
							mode="aspectFill"
						></image>
						<view class="user-info">
							<view class="user-name-row">
								<text class="user-name">{{ user.nickname || '匿名用户' }}</text>
								<text class="user-age" v-if="user.age">{{ user.age }}岁</text>
								<text class="vip-badge" v-if="user.isVip">VIP</text>
								<view class="online-status" v-if="user.isOnline" :class="{ online: user.isOnline }">
									<text class="status-dot"></text>
									<text class="status-text">在线</text>
								</view>
							</view>
							<view class="match-score" v-if="user.matchScore">
								<text class="match-label">匹配度</text>
								<view class="match-bar">
									<view class="match-fill" :style="{ width: user.matchScore + '%' }"></view>
								</view>
								<text class="match-value">{{ Math.round(user.matchScore) }}%</text>
							</view>
							<text class="user-intro">{{ user.selfIntroduction || '这个人很神秘，没有留下介绍' }}</text>
							<view class="user-tags" v-if="user.tags && user.tags.length > 0">
								<text 
									v-for="tag in user.tags.slice(0, 3)" 
									:key="tag" 
									class="tag"
								>{{ tag }}</text>
							</view>
							<view class="user-location">
								<text class="distance">{{ formatDistance(user.distance) }}</text>
								<text class="location-name" v-if="user.locationName">{{ user.locationName }}</text>
							</view>
						</view>
						<view class="user-actions">
							<view class="action-btn greeting" @tap.stop="sendGreetingToUser(user, index)">
								<text class="action-icon">👋</text>
							</view>
							<view class="action-btn like" @tap.stop="toggleLike(user, index)">
								<text class="action-icon" :class="{ liked: user.isLiked }">♥</text>
							</view>
							<view class="action-btn follow" @tap.stop="toggleFavorite(user, index)">
								<text class="action-icon" :class="{ followed: user.isFollowed }">⭐</text>
							</view>
						</view>
					</view>
					
					<!-- 加载状态 -->
					<view v-if="loading" class="loading-more">
						<view class="loading-icon"></view>
						<text class="loading-text">正在加载...</text>
					</view>
					
					<!-- 无更多数据 -->
					<view v-if="!hasMore && nearbyUsers.length > 0" class="no-more">
						<text>— 已经到底啦 —</text>
					</view>
					
					<!-- 空状态 -->
					<view v-if="nearbyUsers.length === 0 && !loading" class="empty-state">
						<text class="empty-icon">📍</text>
						<text class="empty-text">附近暂无用户</text>
						<text class="empty-tip">试试调整搜索范围或刷新页面</text>
					</view>
				</scroll-view>
			</view>
		</view>
		
		<!-- 筛选弹窗 -->
		<uni-popup ref="filterPopup" type="bottom">
			<view class="filter-modal">
				<view class="filter-header">
					<text class="cancel-btn" @tap="cancelFilter">取消</text>
					<text class="filter-title">筛选条件</text>
					<text class="confirm-btn" @tap="confirmFilter">确定</text>
				</view>
				<view class="filter-content">
					<!-- 搜索半径 -->
					<view class="filter-item">
						<text class="filter-label">搜索半径</text>
						<slider 
							class="radius-slider"
							:value="filterOptions.radius" 
							:min="1" 
							:max="50" 
							:step="1"
							@change="onRadiusChange"
						></slider>
						<text class="radius-value">{{ filterOptions.radius }}公里</text>
					</view>
					
					<!-- 性别筛选 -->
					<view class="filter-item">
						<text class="filter-label">性别</text>
						<view class="gender-options">
							<text 
								v-for="option in genderOptions" 
								:key="option.value"
								class="gender-option" 
								:class="{ active: filterOptions.gender === option.value }"
								@tap="selectGender(option.value)"
							>{{ option.label }}</text>
						</view>
					</view>
					
					<!-- 年龄范围 -->
					<view class="filter-item">
						<text class="filter-label">年龄范围</text>
						<view class="age-range">
							<input 
								class="age-input" 
								type="number" 
								:value="filterOptions.minAge" 
								placeholder="最小"
								@input="onMinAgeChange"
							>
							<text class="age-separator">-</text>
							<input 
								class="age-input" 
								type="number" 
								:value="filterOptions.maxAge" 
								placeholder="最大"
								@input="onMaxAgeChange"
							>
							<text class="age-unit">岁</text>
						</view>
					</view>
				</view>
			</view>
		</uni-popup>
		
		<!-- 用户详情弹窗 -->
		<uni-popup ref="userDetailPopup" type="center">
			<view class="user-detail-modal" v-if="selectedUser">
				<view class="detail-header">
					<image 
						class="detail-avatar" 
						:src="selectedUser.avatarUrl || '/static/default-avatar.png'" 
						mode="aspectFill"
					></image>
					<view class="detail-info">
						<text class="detail-name">{{ selectedUser.nickname || '匿名用户' }}</text>
						<text class="detail-age" v-if="selectedUser.age">{{ selectedUser.age }}岁</text>
						<text class="detail-distance">{{ formatDistance(selectedUser.distance) }}</text>
					</view>
					<view class="close-btn" @tap="closeUserDetail">
						<text class="close-icon">×</text>
					</view>
				</view>
				<view class="detail-content">
					<text class="detail-intro">{{ selectedUser.selfIntroduction || '这个人很神秘，没有留下介绍' }}</text>
					<view class="detail-tags" v-if="selectedUser.tags && selectedUser.tags.length > 0">
						<text 
							v-for="tag in selectedUser.tags" 
							:key="tag" 
							class="detail-tag"
						>{{ tag }}</text>
					</view>
				</view>
				<view class="detail-actions">
					<view class="detail-btn secondary" @tap="sendMessage(selectedUser)">
						<text class="btn-text">发消息</text>
					</view>
					<view class="detail-btn primary" @tap="viewFullProfile(selectedUser)">
						<text class="btn-text">查看资料</text>
					</view>
				</view>
			</view>
		</uni-popup>
	</view>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch, onUnmounted } from 'vue';
import { getNearbyUsers, getAllUsers, updateUserLocation, sendGreeting, favoriteUser, unfavoriteUser } from '@/api/location';
import { currentBackground } from '@/utils/simple-theme.js';

// 当前位置
const currentLocation = reactive({
	longitude: 115.489791, // 您的位置坐标
	latitude: 38.815954
});

// 附近用户列表
const nearbyUsers = ref([]);

// 地图标记
const mapMarkers = ref([]);

// 地图圆圈（显示搜索范围）
const mapCircles = ref([]);

// 选中的用户
const selectedUser = ref(null);

// 视图模式
const viewMode = ref('list');

// 地图显示控制
const showUserLabels = ref(false); // 默认不显示标签，减少混乱

// 加载状态
const loading = ref(false);
const hasMore = ref(true);

// 分页参数
const currentPage = ref(1);
const pageSize = ref(20);

// 地图错误状态
const mapError = ref(false);

// 定位状态
const centering = ref(false);

// 筛选选项
const filterOptions = reactive({
	radius: 10, // 搜索半径（公里）
	gender: 0, // 性别：0-不限，1-男，2-女
	minAge: 18,
	maxAge: 50,
	limit: 20
});

// 性别选项
const genderOptions = [
	{ label: '不限', value: 0 },
	{ label: '男', value: 1 },
	{ label: '女', value: 2 }
];

// 弹窗引用
const filterPopup = ref(null);
const userDetailPopup = ref(null);

// 主题背景（监听全局主题变化）
const pageBg = ref('linear-gradient(135deg, #667eea 0%, #764ba2 100%)');

// 监听主题变化（关键！完全复制其他页面的成功模式）
watch(currentBackground, (newBg) => {
  console.log('附近的人页面主题变化:', newBg);
  pageBg.value = newBg;
}, { immediate: true });

// 计算属性
const computedBg = computed(() => {
  return pageBg.value;
});

// 监听主题变化事件（保留作为备用）
const handleThemeChange = (data) => {
  console.log('附近的人页面接收到主题变化事件:', data);
  if (data && data.background) {
    pageBg.value = data.background;
  }
};

// 页面加载
onMounted(() => {
	// 初始化时同步当前主题
	pageBg.value = currentBackground.value;

	// 监听主题变化事件
	uni.$on('themeChanged', handleThemeChange);
	uni.$on('globalThemeUpdate', handleThemeChange);
	uni.$on('simpleThemeChanged', handleThemeChange);
	uni.$on('forceThemeUpdate', handleThemeChange);

	initPage();
});

// 页面卸载
onUnmounted(() => {
	// 移除事件监听
	uni.$off('themeChanged', handleThemeChange);
	uni.$off('globalThemeUpdate', handleThemeChange);
	uni.$off('simpleThemeChanged', handleThemeChange);
	uni.$off('forceThemeUpdate', handleThemeChange);
});

// 初始化页面
async function initPage() {
	console.log('开始初始化页面...');

	// 直接使用默认位置，不需要任何定位操作
	console.log('使用默认位置:', currentLocation);

	// 立即加载数据，让页面可用
	try {
		await loadNearbyUsers();
		updateMapCircles();
		console.log('页面初始化完成');
	} catch (error) {
		console.error('加载数据失败:', error);
	}
}













// 加载附近用户
async function loadNearbyUsers(refresh = false) {
	if (loading.value) return;

	try {
		loading.value = true;

		// 如果是刷新，重置分页
		if (refresh) {
			currentPage.value = 1;
			nearbyUsers.value = [];
			hasMore.value = true;
		}

		// 构建查询参数 - 使用用户列表接口，添加附近的人特殊标识
		const queryData = {
			pageNum: currentPage.value,
			pageSize: pageSize.value,
			source: 'nearby', // 标识来源为附近的人页面
			includeLocation: true, // 包含位置信息
			sortBy: 'distance', // 按距离排序
			nearbyFilter: true // 附近的人专用筛选
		};

		console.log('查询用户列表参数:', queryData);

		// 调用后端真实API获取用户数据
		let result;
		try {
			console.log('调用后端API获取真实用户数据');
			result = await getAllUsers(queryData);
			console.log('后端API返回结果:', result);
		} catch (error) {
			console.error('后端API调用失败，使用模拟数据:', error);
			result = await getMockNearbyUsers(queryData);
			console.log('模拟数据查询结果:', result);
		}

		if (result && result.code === 200) {
			// 处理返回的数据
			let users = [];
			console.log('原始返回数据:', result.data);

			if (result.data && typeof result.data === 'object' && result.data.records) {
				// 后端分页对象格式（list接口）
				users = result.data.records;
				console.log('从分页对象提取用户列表:', users);
			} else if (Array.isArray(result.data)) {
				// 直接数组格式（模拟数据）
				users = result.data;
				console.log('使用数组数据:', users);
			} else {
				console.warn('未知的数据格式:', result.data);
				users = [];
			}

			console.log('最终用户数据:', users);
			console.log('用户数量:', users.length);

			// 处理用户数据
			const processedUsers = users.map((user, index) => {
				console.log(`处理用户 ${index + 1}:`, user);

				// 使用用户已有的位置信息，或生成随机位置
				let longitude, latitude, distance;
				if (user.longitude && user.latitude) {
					// 使用已有位置信息
					longitude = user.longitude;
					latitude = user.latitude;
					distance = user.distance || calculateDistance(
						currentLocation.longitude,
						currentLocation.latitude,
						longitude,
						latitude
					);
				} else {
					// 生成分散的环形分布，避免重叠
					const baseRadius = 0.003; // 基础半径约300米
					const ringRadius = baseRadius * (1 + Math.floor(index / 6)); // 每6个用户一圈
					const usersInRing = Math.min(6, users.length - Math.floor(index / 6) * 6);
					const angleStep = (2 * Math.PI) / usersInRing; // 角度步长
					const angle = (index % 6) * angleStep + (Math.random() - 0.5) * 0.2; // 添加小幅随机偏移

					longitude = currentLocation.longitude + ringRadius * Math.cos(angle);
					latitude = currentLocation.latitude + ringRadius * Math.sin(angle);
					distance = calculateDistance(
						currentLocation.longitude,
						currentLocation.latitude,
						longitude,
						latitude
					);
				}

				// 获取年龄信息
				let age = user.age || 25; // 优先使用直接的age字段
				if (!age && user.userProfile && user.userProfile.age) {
					age = user.userProfile.age;
				} else if (!age && user.birthDate) {
					age = calculateAgeFromBirthDate(user.birthDate);
				}

				// 处理用户标签
				let tags = user.tags || ['新用户']; // 优先使用直接的tags字段
				if (!user.tags && user.userTags && Array.isArray(user.userTags)) {
					tags = user.userTags.map(tag => tag.tagName).filter(name => name);
					if (tags.length === 0) tags = ['新用户'];
				}

				// 获取自我介绍
				let signature = user.selfIntroduction || '这个人很懒，什么都没留下~';
				if (!signature && user.userProfile && user.userProfile.selfIntroduction) {
					signature = user.userProfile.selfIntroduction;
				}

				const processedUser = {
					id: user.userId,
					userId: user.userId,
					nickname: user.nickname || '用户' + user.userId,
					avatar: user.avatarUrl || '/static/images/default-avatar.png',
					age: age,
					gender: user.gender === 1 ? '男' : user.gender === 2 ? '女' : '未知',
					location: user.locationName || '未知位置',
					distance: Math.round(distance * 100) / 100, // 保留两位小数
					longitude: longitude,
					latitude: latitude,
					isOnline: user.isOnline !== undefined ? user.isOnline : Math.random() > 0.3,
					lastActiveTime: user.lastActiveTime || new Date(Date.now() - Math.random() * 3600000).toISOString(),
					signature: signature,
					photos: user.photos || [],
					tags: tags,
					verified: user.isVerified === 1 || user.verified,
					matchScore: user.matchScore || 50.0,
					isVip: user.isVip || 0
				};

				console.log(`处理后的用户 ${index + 1}:`, processedUser);
				return processedUser;
			});

			console.log('处理后的用户数据:', processedUsers);
			console.log('处理后的用户数量:', processedUsers.length);

			if (refresh) {
				nearbyUsers.value = processedUsers;
			} else {
				nearbyUsers.value = [...nearbyUsers.value, ...processedUsers];
			}

			// 更新分页信息
			currentPage.value++;
			hasMore.value = users.length >= pageSize.value;

			console.log('最终用户列表:', nearbyUsers.value);
			console.log('准备更新地图标记...');

			// 更新地图标记
			updateMapMarkers();

			console.log('用户列表加载完成:', nearbyUsers.value.length, '个用户');

			if (users.length === 0) {
				uni.showToast({
					title: '暂无更多用户',
					icon: 'none'
				});
			}
		} else {
			throw new Error(result?.message || '加载失败');
		}
	} catch (error) {
		console.error('加载附近用户失败:', error);
		uni.showToast({
			title: error.message || '加载失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
	}
}

// 计算两点之间的距离（公里）
function calculateDistance(lng1, lat1, lng2, lat2) {
	const R = 6371; // 地球半径（公里）
	const dLat = (lat2 - lat1) * Math.PI / 180;
	const dLng = (lng2 - lng1) * Math.PI / 180;
	const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
		Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
		Math.sin(dLng / 2) * Math.sin(dLng / 2);
	const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	return R * c;
}

// 根据生日计算年龄
function calculateAgeFromBirthDate(birthDate) {
	if (!birthDate) return null;
	const birth = new Date(birthDate);
	const today = new Date();
	let age = today.getFullYear() - birth.getFullYear();
	const monthDiff = today.getMonth() - birth.getMonth();
	if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
		age--;
	}
	return age;
}

// 模拟附近用户数据
async function getMockNearbyUsers(queryData) {
	// 模拟网络延迟
	await new Promise(resolve => setTimeout(resolve, 800));

	// 根据是否为附近的人页面返回不同的用户数据
	const isNearbyPage = queryData.source === 'nearby';

	const mockUsers = isNearbyPage ? [
		// 附近的人页面专用用户数据
		{
			userId: 101,
			nickname: '附近小美',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=nearby1&backgroundColor=ffb3ba',
			age: 24,
			gender: 2,
			distance: 0.3,
			longitude: queryData.longitude + 0.0008,
			latitude: queryData.latitude + 0.0008,
			selfIntroduction: '就在附近，喜欢咖啡和阅读',
			tags: ['咖啡', '阅读', '附近'],
			locationName: '附近咖啡厅',
			isOnline: true,
			lastActiveTime: new Date(),
			isVip: 1,
			isFollowed: false,
			isLiked: false,
			matchScore: 92.0,
			commonInterests: 4
		},
		{
			userId: 102,
			nickname: '附近运动达人',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=nearby2&backgroundColor=bae1ff',
			age: 27,
			gender: 1,
			distance: 0.8,
			longitude: queryData.longitude - 0.0015,
			latitude: queryData.latitude + 0.0015,
			selfIntroduction: '附近健身房常客，寻找运动伙伴',
			tags: ['健身', '附近', '运动'],
			locationName: '附近健身房',
			isOnline: true,
			lastActiveTime: new Date(Date.now() - 2 * 60 * 1000),
			isVip: 0,
			isFollowed: false,
			isLiked: false,
			matchScore: 88.7,
			commonInterests: 3
		},
		{
			userId: 103,
			nickname: '附近书店老板',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=nearby3&backgroundColor=ffffba',
			age: 29,
			gender: 2,
			distance: 1.2,
			longitude: queryData.longitude + 0.002,
			latitude: queryData.latitude - 0.0008,
			selfIntroduction: '在附近开了家小书店，爱书爱生活',
			tags: ['书店', '文艺', '附近'],
			locationName: '附近书店',
			isOnline: false,
			lastActiveTime: new Date(Date.now() - 30 * 60 * 1000),
			isVip: 1,
			isFollowed: false,
			isLiked: false,
			matchScore: 85.2,
			commonInterests: 2
		},
		{
			userId: 104,
			nickname: '附近咖啡师',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=nearby4&backgroundColor=c7ceea',
			age: 26,
			gender: 1,
			distance: 0.6,
			longitude: queryData.longitude - 0.0012,
			latitude: queryData.latitude - 0.0005,
			selfIntroduction: '附近咖啡店的咖啡师，热爱咖啡文化',
			tags: ['咖啡', '手冲', '附近'],
			locationName: '附近咖啡店',
			isOnline: true,
			lastActiveTime: new Date(Date.now() - 10 * 60 * 1000),
			isVip: 0,
			isFollowed: false,
			isLiked: false,
			matchScore: 79.8,
			commonInterests: 2
		}
	] : [
		// 其他页面的默认用户数据
		{
			userId: 1,
			nickname: '小美',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xiaomei&backgroundColor=b6e3f4',
			age: 25,
			gender: 2,
			distance: 0.5,
			longitude: queryData.longitude + 0.001,
			latitude: queryData.latitude + 0.001,
			selfIntroduction: '喜欢旅行和摄影，寻找有趣的灵魂',
			tags: ['旅行', '摄影', '文艺'],
			locationName: '长安区',
			isOnline: true,
			lastActiveTime: new Date(),
			isVip: 1,
			isFollowed: false,
			isLiked: false,
			matchScore: 85.5,
			commonInterests: 3
		},
		{
			userId: 2,
			nickname: '阳光男孩',
			avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=yangguang&backgroundColor=c0aede',
			age: 28,
			gender: 1,
			distance: 1.2,
			longitude: queryData.longitude - 0.002,
			latitude: queryData.latitude + 0.002,
			selfIntroduction: '热爱运动，喜欢交朋友，积极向上',
			tags: ['运动', '健身', '阳光'],
			locationName: '桥西区',
			isOnline: false,
			lastActiveTime: new Date(Date.now() - 5 * 60 * 1000),
			isVip: 0,
			isFollowed: false,
			isLiked: false,
			matchScore: 72.3,
			commonInterests: 2
		}
	];

	// 根据性别筛选
	let filteredUsers = mockUsers;
	if (queryData.gender) {
		filteredUsers = mockUsers.filter(user => user.gender === queryData.gender);
	}

	// 根据年龄筛选
	if (queryData.minAge || queryData.maxAge) {
		filteredUsers = filteredUsers.filter(user => {
			if (queryData.minAge && user.age < queryData.minAge) return false;
			if (queryData.maxAge && user.age > queryData.maxAge) return false;
			return true;
		});
	}

	// 根据距离筛选
	filteredUsers = filteredUsers.filter(user => user.distance <= queryData.radius);

	// 限制返回数量
	if (queryData.limit) {
		filteredUsers = filteredUsers.slice(0, queryData.limit);
	}

	return {
		code: 200,
		message: '查询成功',
		data: filteredUsers
	};
}

// 更新地图标记
function updateMapMarkers() {
	console.log('开始更新地图标记，用户数量:', nearbyUsers.value.length);
	console.log('用户数据详情:', nearbyUsers.value);

	const markers = nearbyUsers.value.map((user, index) => {
		console.log(`处理用户 ${index + 1}:`, user);
		// 为每个用户创建圆形头像标记
		const avatarUrl = generateAvatarUrl(user);
		const circleAvatarUrl = createCircleAvatar(avatarUrl, user.gender);
		console.log(`用户 ${user.nickname} 的头像URL:`, avatarUrl);

		return {
			id: user.userId,
			longitude: user.longitude,
			latitude: user.latitude,
			iconPath: circleAvatarUrl,
			width: 40,
			height: 40,
			anchor: {
				x: 0.5,
				y: 0.5  // 居中锚点，避免重叠
			},
			callout: {
				content: `${user.nickname || '匿名用户'}\n${user.age}岁 · ${formatDistance(user.distance)}`,
				color: '#ffffff',
				fontSize: 12,
				borderRadius: 6,
				bgColor: user.gender === '男' ? 'rgba(52, 152, 219, 0.95)' : 'rgba(231, 76, 60, 0.95)',
				padding: 8,
				display: 'BYCLICK',  // 改为点击显示，减少视觉混乱
				textAlign: 'center'
			},
			label: showUserLabels.value ? {
				content: user.nickname || '匿名用户',
				color: '#ffffff',
				fontSize: 12,
				bgColor: user.gender === '男' ? 'rgba(52, 152, 219, 0.9)' : 'rgba(231, 76, 60, 0.9)',
				borderRadius: 6,
				padding: 6,
				anchorX: 0.5,  // 水平居中
				anchorY: 0,    // 顶部对齐
				x: 0,          // 相对于标记点的偏移
				y: 35,         // 标记点下方35像素
				textAlign: 'center'
			} : undefined
		};
	});

	// 添加当前位置标记（水滴状图标）
	const currentLocationSvg = `<svg width="32" height="40" viewBox="0 0 32 40" xmlns="http://www.w3.org/2000/svg">
		<defs>
			<linearGradient id="dropGrad" x1="0%" y1="0%" x2="0%" y2="100%">
				<stop offset="0%" style="stop-color:#FF6B6B;stop-opacity:1" />
				<stop offset="100%" style="stop-color:#E53E3E;stop-opacity:1" />
			</linearGradient>
		</defs>
		<path d="M16 2 C8 10, 2 16, 2 24 C2 30.627, 8.373 36, 16 36 C23.627 36, 30 30.627, 30 24 C30 16, 24 10, 16 2 Z"
			  fill="url(#dropGrad)"
			  stroke="#fff"
			  stroke-width="2"/>
		<circle cx="16" cy="24" r="6" fill="#fff"/>
	</svg>`;

	markers.push({
		id: 'current',
		longitude: currentLocation.longitude,
		latitude: currentLocation.latitude,
		iconPath: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(currentLocationSvg),
		width: 32,
		height: 40,
		anchor: {
			x: 0.5,
			y: 1
		},
		callout: {
			content: '我的位置',
			color: '#ffffff',
			fontSize: 12,
			borderRadius: 6,
			bgColor: 'rgba(255, 59, 48, 0.9)',
			padding: 8,
			display: 'BYCLICK',
			textAlign: 'center'
		}
	});

	// 更新搜索范围圆圈
	updateMapCircles();

	console.log('生成的地图标记数组:', markers);
	console.log('标记详情:', markers.map(m => ({
		id: m.id,
		longitude: m.longitude,
		latitude: m.latitude,
		iconPath: m.iconPath ? m.iconPath.substring(0, 50) + '...' : 'null'
	})));

	mapMarkers.value = markers;
	console.log('地图标记更新完成，共', markers.length, '个标记');
	console.log('mapMarkers.value:', mapMarkers.value.length);
}

// 更新地图圆圈（搜索范围）
function updateMapCircles() {
	const circles = [{
		longitude: currentLocation.longitude,
		latitude: currentLocation.latitude,
		radius: filterOptions.radius * 1000, // 转换为米
		strokeWidth: 2,
		strokeColor: '#007AFF',
		fillColor: 'rgba(0, 122, 255, 0.1)'
	}];

	mapCircles.value = circles;
}

// 格式化距离
function formatDistance(distance) {
	if (!distance) return '';

	if (distance < 1) {
		return Math.round(distance * 1000) + 'm';
	} else {
		return distance.toFixed(1) + 'km';
	}
}

// 生成用户头像URL
function generateAvatarUrl(user) {
	// 如果用户有头像，直接使用
	if (user.avatar && user.avatar.startsWith('http')) {
		return user.avatar;
	}

	// 如果有avatarUrl字段
	if (user.avatarUrl && user.avatarUrl.startsWith('http')) {
		return user.avatarUrl;
	}

	// 否则生成一个基于用户ID的头像
	const seed = user.userId || user.nickname || 'default';
	const style = user.gender === '女' ? 'avataaars' : 'micah';
	const backgroundColor = user.gender === '女' ? 'ffdfbf,ffd5dc,c0aede' : 'b6e3f4,c0aede,d1d4f9';

	return `https://api.dicebear.com/7.x/${style}/svg?seed=${seed}&backgroundColor=${backgroundColor}&radius=50`;
}

// 创建圆形头像标记
function createCircleAvatar(avatarUrl, gender) {
	const borderColor = gender === '男' ? '#4A90E2' : '#FF6B6B';
	const size = 40;

	// 暂时先返回原始头像，确保头像能正常显示
	// 圆形效果可以后续通过其他方式实现
	return avatarUrl;
}

// 坐标系转换工具函数
// WGS84坐标系转GCJ02坐标系（火星坐标系）
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

// 返回上一页
function goBack() {
	uni.navigateBack();
}

// 跳转到地图视图页面
function goToMapView() {
	uni.navigateTo({
		url: '/pages/nearby/nearby'
	});
}

// 显示筛选弹窗
function showFilterModal() {
	filterPopup.value.open();
}

// 取消筛选
function cancelFilter() {
	filterPopup.value.close();
}

// 确认筛选
function confirmFilter() {
	filterPopup.value.close();
	// 更新搜索范围圆圈
	updateMapCircles();
	// 重新加载附近用户
	loadNearbyUsers(true);
}

// 切换视图模式
function switchViewMode(mode) {
	viewMode.value = mode;
}

// 切换用户标签显示
function toggleUserLabels() {
	showUserLabels.value = !showUserLabels.value;
	// 重新更新地图标记
	updateMapMarkers();
}

// 居中到当前位置
async function centerToCurrentLocation() {
	try {
		centering.value = true;
		uni.showToast({
			title: '正在定位...',
			icon: 'loading'
		});

		await getCurrentLocation();

		// 重新更新地图标记和圆圈
		updateMapMarkers();

		uni.showToast({
			title: '定位成功',
			icon: 'success'
		});
	} catch (error) {
		console.error('定位失败:', error);
		uni.showToast({
			title: '定位失败',
			icon: 'error'
		});
	} finally {
		centering.value = false;
	}
}





// 回到我的位置
function backToMyLocation() {
	try {
		// 获取地图实例
		const mapContext = uni.createMapContext('nearbyMap');

		// 移动地图到我的位置
		mapContext.moveToLocation({
			longitude: currentLocation.longitude,
			latitude: currentLocation.latitude,
			success: () => {
				uni.showToast({
					title: '已回到我的位置',
					icon: 'success',
					duration: 1500
				});
			},
			fail: () => {
				// 如果moveToLocation失败，使用includePoints
				mapContext.includePoints({
					points: [{
						longitude: currentLocation.longitude,
						latitude: currentLocation.latitude
					}],
					padding: [50, 50, 50, 50]
				});

				uni.showToast({
					title: '已回到我的位置',
					icon: 'success',
					duration: 1500
				});
			}
		});

		// 重新加载数据以确保显示最新信息
		loadNearbyUsers(true);
		updateMapMarkers();
		updateMapCircles();

	} catch (error) {
		console.error('回到位置失败:', error);
		uni.showToast({
			title: '定位失败',
			icon: 'none'
		});
	}
}











// 发送招呼
async function sendGreetingToUser(user, index) {
	try {
		uni.showModal({
			title: '打招呼',
			content: `向 ${user.nickname} 发送招呼？`,
			confirmText: '发送',
			cancelText: '取消',
			success: async (res) => {
				if (res.confirm) {
					try {
						// 先尝试真实API
						const result = await sendGreeting({
							toUserId: user.userId,
							message: '你好，很高兴认识你！'
						});

						if (result && result.code === 200) {
							uni.showToast({
								title: '招呼发送成功',
								icon: 'success'
							});
						} else {
							throw new Error(result?.message || '发送失败');
						}
					} catch (error) {
						console.warn('真实API调用失败，使用模拟功能:', error);
						// 降级到模拟功能
						uni.showToast({
							title: '招呼发送成功（模拟）',
							icon: 'success'
						});
					}
				}
			}
		});
	} catch (error) {
		console.error('发送招呼失败:', error);
		uni.showToast({
			title: '功能暂时不可用',
			icon: 'none'
		});
	}
}

// 切换收藏状态
async function toggleFavorite(user, index) {
	try {
		const isFavorited = user.isFollowed;
		const action = isFavorited ? unfavoriteUser : favoriteUser;
		const actionText = isFavorited ? '取消收藏' : '收藏';

		try {
			const result = await action({
				targetUserId: user.userId
			});

			if (result && result.code === 200) {
				// 更新本地状态
				nearbyUsers.value[index].isFollowed = !isFavorited;

				uni.showToast({
					title: actionText + '成功',
					icon: 'success'
				});
			} else {
				throw new Error(result?.message || actionText + '失败');
			}
		} catch (error) {
			console.warn('真实API调用失败，使用本地状态:', error);
			// 降级到本地状态切换
			nearbyUsers.value[index].isFollowed = !isFavorited;

			uni.showToast({
				title: actionText + '成功（本地）',
				icon: 'success'
			});
		}
	} catch (error) {
		console.error('切换收藏状态失败:', error);
		uni.showToast({
			title: '操作失败',
			icon: 'none'
		});
	}
}

// 切换点赞状态
function toggleLike(user, index) {
	// 本地切换点赞状态（这里可以后续接入真实的点赞API）
	nearbyUsers.value[index].isLiked = !user.isLiked;

	uni.showToast({
		title: user.isLiked ? '已点赞' : '取消点赞',
		icon: 'success'
	});
}

// 加载更多用户
function loadMoreUsers() {
	if (hasMore.value && !loading.value) {
		loadNearbyUsers();
	}
}

// 地图标记点击
function onMarkerTap(e) {
	const markerId = e.detail.markerId;
	if (markerId === 'current') return;

	const user = nearbyUsers.value.find(u => u.userId == markerId);
	if (user) {
		// 跳转到用户详情页面
		uni.navigateTo({
			url: `/pages/user/user-detail?userId=${user.userId}`
		});
	}
}

// 地图区域变化
function onRegionChange(e) {
	// 可以在这里处理地图区域变化事件
	console.log('地图区域变化:', e);

	// 如果用户手动移动地图，可以考虑更新搜索中心点
	if (e.type === 'end') {
		console.log('地图移动结束，新的中心点:', e.detail);
	}
}

// 地图点击事件
function onMapTap(e) {
	console.log('地图点击:', e.detail);
	// 可以在这里添加地图点击的交互逻辑
}

// 地图错误处理
function onMapError(e) {
	console.error('地图加载错误:', e);
	mapError.value = true;
	uni.showToast({
		title: '地图加载失败，请使用列表查看',
		icon: 'none'
	});
}

// 重试地图加载
function retryMap() {
	mapError.value = false;
	uni.showToast({
		title: '正在重新加载地图...',
		icon: 'loading'
	});
}

// 查看用户详情
function viewUserDetail(user) {
	// 跳转到用户详情页面
	uni.navigateTo({
		url: `/pages/user/user-detail?userId=${user.userId}`
	});
}

// 关闭用户详情
function closeUserDetail() {
	userDetailPopup.value.close();
	selectedUser.value = null;
}

// 查看完整资料
function viewFullProfile(user) {
	closeUserDetail();
	uni.navigateTo({
		url: `/pages/user/user-detail?id=${user.userId}`
	});
}

// 发送消息
function sendMessage(user) {
	closeUserDetail();
	uni.navigateTo({
		url: `/pages/message/chat?userId=${user.userId}&nickname=${user.nickname}`
	});
}



// 切换关注
function toggleFollow(user, index) {
	nearbyUsers.value[index].isFollowed = !nearbyUsers.value[index].isFollowed;
	uni.showToast({
		title: nearbyUsers.value[index].isFollowed ? '已关注' : '取消关注',
		icon: 'none'
	});
}

// 筛选相关方法
function onRadiusChange(e) {
	filterOptions.radius = e.detail.value;
}

function selectGender(value) {
	filterOptions.gender = value;
}

function onMinAgeChange(e) {
	filterOptions.minAge = parseInt(e.detail.value) || 18;
}

function onMaxAgeChange(e) {
	filterOptions.maxAge = parseInt(e.detail.value) || 50;
}


</script>

<style lang="scss" scoped>
.nearby-page {
	position: relative;
	width: 100%;
	height: 100vh;
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
		z-index: 1;
		
		// 顶部工具栏
		.header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 20rpx 30rpx;
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);
			
			.back-btn, .filter-btn {
				width: 60rpx;
				height: 60rpx;
				display: flex;
				align-items: center;
				justify-content: center;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 50%;
				
				.back-icon, .filter-icon {
					font-size: 32rpx;
					color: #fff;
				}
			}
			
			.title {
				font-size: 36rpx;
				font-weight: bold;
				color: #fff;
			}
		}
		
		// 地图容器
		.map-container {
			position: relative;
			height: calc(66.67vh - 120rpx); // 占屏幕2/3减去头部高度

			.map {
				width: 100%;
				height: 100%;
			}

			.map-fallback {
				width: 100%;
				height: 100%;
				display: flex;
				align-items: center;
				justify-content: center;
				background: rgba(255, 255, 255, 0.1);
				backdrop-filter: blur(10rpx);

				.fallback-content {
					text-align: center;

					.fallback-icon {
						font-size: 80rpx;
						display: block;
						margin-bottom: 20rpx;
					}

					.fallback-title {
						font-size: 32rpx;
						color: #fff;
						font-weight: bold;
						display: block;
						margin-bottom: 10rpx;
					}

					.fallback-desc {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.7);
						display: block;
						margin-bottom: 30rpx;
					}

					.retry-btn {
						padding: 15rpx 30rpx;
						background: rgba(255, 255, 255, 0.2);
						border-radius: 25rpx;
						display: inline-block;

						text {
							color: #fff;
							font-size: 26rpx;
						}
					}
				}
			}

			.map-controls {
				position: absolute;
				right: 20rpx;
				bottom: 20rpx;
				display: flex;
				flex-direction: column;
				gap: 15rpx;

				.control-btn {
					width: 80rpx;
					height: 80rpx;
					display: flex;
					flex-direction: column;
					align-items: center;
					justify-content: center;
					background: rgba(255, 255, 255, 0.95);
					border-radius: 15rpx;
					box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.15);
					transition: all 0.3s ease;

					&.my-location-btn {
						background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);

						.control-icon {
							color: #fff;
							font-size: 32rpx;
						}
					}

					&.label-toggle-btn {
						background: rgba(255, 255, 255, 0.95);

						.control-icon {
							color: #666;
							font-size: 28rpx;
						}

						&.active {
							background: linear-gradient(135deg, #FF6B6B 0%, #EE5A52 100%);

							.control-icon {
								color: #fff;
							}
						}
					}

					&.active {
						transform: scale(0.95);
					}

					&:active {
						transform: scale(0.9);
					}

					.control-icon {
						font-size: 24rpx;
						margin-bottom: 4rpx;
					}

					.control-text {
						font-size: 18rpx;
						color: #666;
						font-weight: 500;
					}
				}
			}
		}
		
		// 用户列表
		.user-list {
			height: 33.33vh; // 固定占屏幕1/3高度
			background: rgba(255, 255, 255, 0.1);
			backdrop-filter: blur(10rpx);
			border-radius: 20rpx 20rpx 0 0;
			margin-top: 20rpx;
			display: flex;
			flex-direction: column;
			
			.list-header {
				display: flex;
				align-items: center;
				justify-content: space-between;
				padding: 30rpx;
				border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);

				.list-title {
					font-size: 32rpx;
					font-weight: bold;
					color: #fff;
				}

				.header-actions {
					display: flex;
					align-items: center;
					gap: 20rpx;



					.view-toggle {
						display: flex;
						background: rgba(255, 255, 255, 0.1);
						border-radius: 20rpx;
						overflow: hidden;

						.toggle-btn {
							padding: 10rpx 20rpx;
							font-size: 24rpx;
							color: rgba(255, 255, 255, 0.7);
							transition: all 0.3s;

							&.active {
								background: rgba(255, 255, 255, 0.2);
								color: #fff;
							}
						}
					}
				}
			}
			
			.user-scroll {
				flex: 1;
				height: 0; // 重要：让flex生效
			}
			
			.user-item {
				display: flex;
				align-items: center;
				padding: 30rpx;
				border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
				
				.user-avatar {
					width: 100rpx;
					height: 100rpx;
					border-radius: 50%;
					margin-right: 20rpx;
				}
				
				.user-info {
					flex: 1;
					
					.user-name-row {
						display: flex;
						align-items: center;
						margin-bottom: 10rpx;
						
						.user-name {
							font-size: 32rpx;
							font-weight: bold;
							color: #fff;
							margin-right: 15rpx;
						}
						
						.user-age {
							font-size: 24rpx;
							color: rgba(255, 255, 255, 0.8);
							background: rgba(255, 255, 255, 0.1);
							padding: 4rpx 12rpx;
							border-radius: 20rpx;
							margin-right: 10rpx;
						}
						
						.vip-badge {
							font-size: 20rpx;
							color: #FFD700;
							background: rgba(255, 215, 0, 0.2);
							padding: 4rpx 8rpx;
							border-radius: 10rpx;
						}

						.online-status {
							display: flex;
							align-items: center;
							margin-left: 10rpx;

							&.online {
								.status-dot {
									background: #00D4AA;
								}
							}

							.status-dot {
								width: 12rpx;
								height: 12rpx;
								border-radius: 50%;
								background: #ccc;
								margin-right: 6rpx;
							}

							.status-text {
								font-size: 20rpx;
								color: rgba(255, 255, 255, 0.8);
							}
						}
					}

					.match-score {
						display: flex;
						align-items: center;
						margin-bottom: 10rpx;

						.match-label {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.6);
							margin-right: 10rpx;
						}

						.match-bar {
							flex: 1;
							height: 8rpx;
							background: rgba(255, 255, 255, 0.1);
							border-radius: 4rpx;
							overflow: hidden;
							margin-right: 10rpx;

							.match-fill {
								height: 100%;
								background: linear-gradient(90deg, #FF6B6B 0%, #FFD93D 50%, #6BCF7F 100%);
								border-radius: 4rpx;
								transition: width 0.3s ease;
							}
						}

						.match-value {
							font-size: 22rpx;
							color: #00D4AA;
							font-weight: bold;
							min-width: 60rpx;
							text-align: right;
						}
					}

					.user-intro {
						font-size: 26rpx;
						color: rgba(255, 255, 255, 0.7);
						margin-bottom: 10rpx;
						display: -webkit-box;
						-webkit-line-clamp: 2;
						-webkit-box-orient: vertical;
						overflow: hidden;
					}
					
					.user-tags {
						display: flex;
						flex-wrap: wrap;
						margin-bottom: 10rpx;
						
						.tag {
							font-size: 20rpx;
							color: rgba(255, 255, 255, 0.8);
							background: rgba(255, 255, 255, 0.1);
							padding: 4rpx 8rpx;
							border-radius: 8rpx;
							margin-right: 8rpx;
							margin-bottom: 8rpx;
						}
					}
					
					.user-location {
						display: flex;
						align-items: center;
						
						.distance {
							font-size: 24rpx;
							color: #00D4AA;
							font-weight: bold;
							margin-right: 15rpx;
						}
						
						.location-name {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.6);
						}
					}
				}
				
				.user-actions {
					display: flex;
					flex-direction: column;
					gap: 10rpx;
					
					.action-btn {
						width: 60rpx;
						height: 60rpx;
						display: flex;
						align-items: center;
						justify-content: center;
						border-radius: 50%;
						transition: all 0.3s ease;

						&.greeting {
							background: rgba(255, 193, 7, 0.2);

							.action-icon {
								font-size: 28rpx;
								color: #FFC107;
							}

							&:active {
								transform: scale(0.9);
								background: rgba(255, 193, 7, 0.3);
							}
						}

						&.like {
							background: rgba(255, 107, 107, 0.2);

							.action-icon {
								font-size: 32rpx;
								color: rgba(255, 107, 107, 0.7);

								&.liked {
									color: #FF6B6B;
									transform: scale(1.2);
									animation: heartbeat 0.6s ease-in-out;
								}
							}

							&:active {
								transform: scale(0.9);
							}
						}

						&.follow {
							background: rgba(0, 212, 170, 0.2);

							.action-icon {
								font-size: 32rpx;
								color: rgba(0, 212, 170, 0.7);

								&.followed {
									color: #FFD700;
									transform: scale(1.2);
								}
							}

							&:active {
								transform: scale(0.9);
							}
						}
					}
				}

				@keyframes heartbeat {
					0% { transform: scale(1); }
					50% { transform: scale(1.2); }
					100% { transform: scale(1); }
				}
			}
		}
	}
}

// 加载状态
.loading-more {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 30rpx;
	
	.loading-icon {
		width: 40rpx;
		height: 40rpx;
		border: 3rpx solid rgba(255, 255, 255, 0.3);
		border-top: 3rpx solid #fff;
		border-radius: 50%;
		animation: spin 1s linear infinite;
		margin-bottom: 10rpx;
	}
	
	.loading-text {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.7);
	}
}

.no-more {
	text-align: center;
	padding: 30rpx;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.7);
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 30rpx;
	
	.empty-icon {
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}
	
	.empty-text {
		font-size: 32rpx;
		color: rgba(255, 255, 255, 0.8);
		margin-bottom: 10rpx;
	}
	
	.empty-tip {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.6);
	}
}

// 筛选弹窗
.filter-modal {
	background: #fff;
	border-radius: 20rpx 20rpx 0 0;
	
	.filter-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.filter-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
		}
		
		.cancel-btn, .confirm-btn {
			font-size: 28rpx;
			padding: 10rpx;
		}
		
		.cancel-btn {
			color: #999;
		}
		
		.confirm-btn {
			color: #007AFF;
		}
	}
	
	.filter-content {
		padding: 30rpx;
		
		.filter-item {
			margin-bottom: 40rpx;
			
			.filter-label {
				display: block;
				font-size: 28rpx;
				color: #333;
				margin-bottom: 20rpx;
			}
			
			.radius-slider {
				width: 100%;
				margin-bottom: 10rpx;
			}
			
			.radius-value {
				font-size: 24rpx;
				color: #666;
			}
			
			.gender-options {
				display: flex;
				gap: 20rpx;
				
				.gender-option {
					flex: 1;
					text-align: center;
					padding: 20rpx;
					background: #f5f5f5;
					border-radius: 10rpx;
					font-size: 26rpx;
					color: #666;
					
					&.active {
						background: #007AFF;
						color: #fff;
					}
				}
			}
			
			.age-range {
				display: flex;
				align-items: center;
				gap: 15rpx;
				
				.age-input {
					flex: 1;
					padding: 20rpx;
					background: #f5f5f5;
					border-radius: 10rpx;
					text-align: center;
					font-size: 26rpx;
				}
				
				.age-separator {
					font-size: 26rpx;
					color: #666;
				}
				
				.age-unit {
					font-size: 24rpx;
					color: #666;
				}
			}
		}
	}
}

// 用户详情弹窗
.user-detail-modal {
	width: 600rpx;
	background: #fff;
	border-radius: 20rpx;
	overflow: hidden;
	
	.detail-header {
		position: relative;
		display: flex;
		align-items: center;
		padding: 30rpx;
		background: linear-gradient(45deg, #12C2E9, #C471ED);
		
		.detail-avatar {
			width: 100rpx;
			height: 100rpx;
			border-radius: 50%;
			margin-right: 20rpx;
		}
		
		.detail-info {
			flex: 1;
			
			.detail-name {
				display: block;
				font-size: 32rpx;
				font-weight: bold;
				color: #fff;
				margin-bottom: 8rpx;
			}
			
			.detail-age {
				font-size: 24rpx;
				color: rgba(255, 255, 255, 0.8);
				margin-right: 15rpx;
			}
			
			.detail-distance {
				font-size: 24rpx;
				color: #00D4AA;
				font-weight: bold;
			}
		}
		
		.close-btn {
			position: absolute;
			top: 20rpx;
			right: 20rpx;
			width: 50rpx;
			height: 50rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			background: rgba(255, 255, 255, 0.2);
			border-radius: 50%;
			
			.close-icon {
				font-size: 32rpx;
				color: #fff;
			}
		}
	}
	
	.detail-content {
		padding: 30rpx;
		
		.detail-intro {
			font-size: 28rpx;
			color: #333;
			line-height: 1.5;
			margin-bottom: 20rpx;
		}
		
		.detail-tags {
			display: flex;
			flex-wrap: wrap;
			
			.detail-tag {
				font-size: 22rpx;
				color: #007AFF;
				background: rgba(0, 122, 255, 0.1);
				padding: 8rpx 12rpx;
				border-radius: 10rpx;
				margin-right: 10rpx;
				margin-bottom: 10rpx;
			}
		}
	}
	
	.detail-actions {
		display: flex;
		padding: 30rpx;
		gap: 20rpx;
		
		.detail-btn {
			flex: 1;
			text-align: center;
			padding: 25rpx;
			border-radius: 15rpx;
			
			&.secondary {
				background: #f5f5f5;
				
				.btn-text {
					color: #666;
				}
			}
			
			&.primary {
				background: #007AFF;
				
				.btn-text {
					color: #fff;
				}
			}
			
			.btn-text {
				font-size: 28rpx;
				font-weight: bold;
			}
		}
	}
}

// 背景渐变
.bg-gradient {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
	background-size: 300% 300%;
	animation: gradientAnimation 15s ease infinite;
	
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
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}
</style>
