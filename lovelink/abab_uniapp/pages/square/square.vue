<template>
	<view class="square-page">
		<!-- 背景层 -->
		<view class="bg-layer" :style="{ background: currentBackground }">
			<!-- 移除固定的bg-gradient，让主题背景生效 -->
			<view class="overlay-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部Logo、地区选择和搜索 -->
			<view class="header">
				<view class="left-section">
					<!-- 地区选择下拉框 -->
					<view class="region-selector" @tap="showRegionPicker">
						<text class="region-text">{{ selectedRegion }}</text>
						<text class="region-arrow"></text>
					</view>
					
					<view class="search-box" @tap="goSearch">
						<text class="search-icon"></text>
						<text class="search-placeholder">搜索</text>
					</view>
				</view>

				<!-- 虚拟货币 -->
				<view class="currency-section" @tap="goToWallet">
					<image class="currency-icon" src="/static/index/coin.png" mode="aspectFit" @error="handleImageError('coin')">
						<view v-if="imageError.coin" class="coin-placeholder"></view>
					</image>
					<text class="currency-amount">{{ userCurrency }}</text>
				</view>

				<!-- 主题切换按钮 -->
				<view class="theme-switch-btn" @tap="quickSwitchTheme">
					<text class="theme-icon">🎨</text>
				</view>
			</view>
			
			<!-- 主要内容区 -->
			<view class="main-content">
				<!-- 广场筛选选项 -->
				<view class="filter-tabs">
					<view 
						v-for="(tab, index) in filterTabs" 
						:key="index" 
						class="filter-tab" 
						:class="{ active: currentTabIndex === index }"
						@tap="switchTab(index)"
					>
						<text>{{ tab.name }}</text>
					</view>
				</view>
				
				<!-- 用户列表 -->
				<view v-if="currentTabIndex !== 3" class="user-grid">
					<view class="user-card" v-for="(user, index) in users" :key="index" @tap="viewUserDetail(user)">
						<image class="user-avatar" :src="user.avatar" mode="aspectFill" @error="handleImageError(`user${index}`)">
							<view v-if="imageError[`user${index}`]" class="avatar-placeholder"></view>
						</image>
						<view class="user-info">
							<view class="user-name-row">
								<text class="user-name">{{ user.name }}</text>
								<text class="user-age">{{ typeof user.age === 'number' ? user.age + '岁' : user.age }}</text>
							</view>
							<view class="user-tags">
								<text class="user-tag" v-for="(tag, tagIndex) in user.tags" :key="tagIndex">{{ tag.tagName || tag }}</text>
							</view>
							<text class="user-intro">{{ user.selfIntroduction }}</text>
						</view>
						<view class="interaction-btn" @tap.stop="likeUser(user, index)">
							<text class="btn-icon" :class="{ 'liked': user.isLiked }">♥</text>
						</view>
					</view>

					<!-- 加载状态提示 -->
					<view v-if="pagination.loading" class="loading-more">
						<view class="loading-icon"></view>
						<text class="loading-text">正在加载更多...</text>
					</view>

					<!-- 无更多数据提示 -->
					<view v-if="!pagination.hasMore && users.length > 0 && !pagination.loading" class="no-more-data">
						<text>— 已经到底啦 —</text>
					</view>

					<!-- 无数据提示 -->
					<view v-if="users.length === 0 && !pagination.loading" class="empty-data">
						<text class="empty-text">暂无数据</text>
					</view>
				</view>

				<!-- 动态广场内容 -->
				<view v-if="currentTabIndex === 3" class="moment-grid">
					<!-- 调试信息 -->
					<view v-if="false" style="background: rgba(255,255,255,0.1); padding: 20rpx; margin-bottom: 20rpx;">
						<text style="color: white; font-size: 24rpx;">
							调试: 当前选项卡={{ currentTabIndex }}, 动态数量={{ momentList.length }}, 加载中={{ momentPagination.loading }}
						</text>
					</view>
					<view
						v-for="moment in momentList"
						:key="moment.momentId"
						class="moment-card"
						@tap="goToMomentDetail(moment)"
					>
						<!-- 用户信息 -->
						<view class="moment-header">
							<image :src="moment.avatarUrl || '/static/default-avatar.png'" class="user-avatar" mode="aspectFill" @error="handleImageError(`avatar${moment.momentId}`)">
								<view v-if="imageError[`avatar${moment.momentId}`]" class="avatar-placeholder"></view>
							</image>
							<view class="user-info">
								<text class="user-nickname">{{ moment.nickname || '匿名用户' }}</text>
								<text class="publish-time">{{ formatTime(moment.createdAt) }}</text>
							</view>
							<view class="moment-menu" @tap.stop="showMomentMenu(moment)">
								<image src="/static/icons/more.png" class="menu-icon"></image>
							</view>
						</view>

						<!-- 位置信息 -->
						<view v-if="moment.location" class="moment-location">
							<text class="location-icon">📍</text>
							<text class="location-text">{{ moment.location }}</text>
						</view>

						<!-- 动态图片网格 -->
						<view v-if="moment.mediaList && moment.mediaList.length > 0" class="moment-media">
							<view class="media-grid" :class="getMediaGridClass(moment.mediaList.length)">
								<view
									v-for="(media, index) in moment.mediaList"
									:key="media.mediaId"
									class="media-item"
									@tap.stop="previewMomentMedia(moment.mediaList, index)"
								>
									<image
										:src="media.mediaUrl"
										class="media-image"
										mode="aspectFit"
										@error="handleImageError(`media${media.mediaId}`)"
									>
										<view v-if="imageError[`media${media.mediaId}`]" class="image-placeholder"></view>
									</image>
								</view>
							</view>
						</view>

						<!-- 动态内容 -->
						<view class="moment-content">
							<text>{{ moment.content }}</text>
						</view>

						<!-- 底部操作区 -->
						<view class="moment-footer">
							<view class="moment-actions">
								<view class="action-item" @tap.stop="likeMoment(moment)">
									<image
										:src="moment.isLiked ? '/static/icons/heart-filled.png' : '/static/icons/heart.png'"
										class="action-icon"
										:class="{ liked: moment.isLiked }"
									></image>
									<text class="action-count">{{ moment.likeCount || 0 }}</text>
								</view>
								<view class="action-item" @tap.stop="commentMoment(moment)">
									<image src="/static/icons/comment.png" class="action-icon"></image>
									<text class="action-count">{{ moment.commentCount || 0 }}</text>
								</view>
								<view class="action-item">
									<image src="/static/icons/eye.png" class="action-icon"></image>
									<text class="action-count">{{ moment.viewCount || 0 }}</text>
								</view>
							</view>
						</view>
					</view>

					<!-- 动态加载状态 -->
					<view v-if="momentPagination.loading" class="loading-more">
						<view class="loading-icon"></view>
						<text class="loading-text">正在加载更多...</text>
					</view>

					<!-- 动态没有更多数据 -->
					<view v-if="!momentPagination.hasMore && momentList.length > 0 && !momentPagination.loading" class="no-more-data">
						<text>— 已经到底啦 —</text>
					</view>

					<!-- 动态空状态 -->
					<view v-if="momentList.length === 0 && !momentPagination.loading" class="empty-data">
						<text class="empty-text">暂无动态</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 地区选择弹窗 -->
		<uni-popup ref="regionPopup" type="bottom">
			<view class="region-popup">
				<view class="region-popup-header">
					<text class="cancel-btn" @tap="cancelRegionSelect">取消</text>
					<text class="title">选择地区</text>
					<text class="confirm-btn" @tap="confirmRegionSelect">确定</text>
				</view>
				<picker-view class="region-picker" :indicator-style="indicatorStyle" :value="regionIndex" @change="onRegionChange">
					<picker-view-column>
						<view class="picker-item" v-for="(item, index) in regions" :key="index">{{item}}</view>
					</picker-view-column>
				</picker-view>
			</view>
		</uni-popup>
		
		<!-- 悬浮地图按钮 -->
		<view class="floating-map-btn" @tap="goToNearbyMap">
			<text class="map-icon">🗺️</text>
		</view>

		<!-- 自定义TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app';
import customTabBar from '@/components/custom-tab-bar.vue';
import squareApi from '@/api/square.js';
import { getWalletInfo } from '@/api/wallet';
import { CHINA_PROVINCES, getProvinceShortName } from '@/utils/provinces';
import { getByUserInfo, updateUserProvince } from '@/api/user/auth';
import momentApi from '@/api/moment.js';
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js';

// 图片加载错误状态
const imageError = reactive({
	coin: false
});

// 虚拟货币
const userCurrency = ref(0);

// 使用全局主题混入
const { currentBackground } = useGlobalThemeMixin();

console.log('广场页初始化，使用全局主题系统');

// 监听页面显示
onShow(() => {
  console.log('广场页显示');
});

// 地区选择相关
const regionPopup = ref(null);
const regions = CHINA_PROVINCES.map(province => getProvinceShortName(province));
const regionIndex = ref([0]);
const selectedRegion = ref('北京');
const tempRegion = ref('');
const indicatorStyle = 'height: 80rpx;';

// 筛选选项卡
const currentTabIndex = ref(0);
const filterTabs = [
	{ name: '推荐', type: 'recommend' },
	{ name: '新人', type: 'new' },
	{ name: '活跃', type: 'active' },
	{ name: '动态广场', type: 'moment' }
];

// 用户列表数据
const users = ref([]);

// 分页参数
const pagination = reactive({
	pageNum: 1,
	pageSize: 10,
	total: 0,
	loading: false,
	hasMore: true
});

// 动态列表数据
const momentList = ref([]);
const momentRefreshing = ref(false);

// 动态分页参数
const momentPagination = reactive({
	pageNum: 1,
	pageSize: 10,
	total: 0,
	loading: false,
	hasMore: true
});

// 计算年龄的辅助函数
function calculateAge(birthDate) {
	if (!birthDate) return null;

	const birth = new Date(birthDate);
	const today = new Date();
	let age = today.getFullYear() - birth.getFullYear();
	const monthDiff = today.getMonth() - birth.getMonth();

	if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
		age--;
	}

	return age > 0 ? age : null;
}

// 安全获取用户年龄
function getUserAge(user) {
	// 优先使用userProfile中的age
	if (user.userProfile && user.userProfile.age) {
		return user.userProfile.age;
	}

	// 如果userProfile中没有age，尝试从birthDate计算
	if (user.birthDate) {
		const calculatedAge = calculateAge(user.birthDate);
		if (calculatedAge) {
			return calculatedAge;
		}
	}

	// 如果都没有，返回默认值
	return '未知';
}

// 安全获取用户自我介绍
function getUserIntroduction(user) {
	if (user.userProfile && user.userProfile.selfIntroduction) {
		return user.userProfile.selfIntroduction;
	}
	return '这个人很神秘，没有留下介绍';
}

// 安全获取用户标签
function getUserTags(user) {
	if (user.userTags && user.userTags.length > 0) {
		return user.userTags;
	}
	return [{ tagName: '暂无标签' }];
}

// 获取用户列表数据
async function loadUsers(type = 'recommend', refresh = false) {
	try {
		// 如果是刷新，重置页码
		if (refresh) {
			pagination.pageNum = 1;
			pagination.hasMore = true;
		}

		// 如果没有更多数据，直接返回
		if (!pagination.hasMore && !refresh) {
			return;
		}

		// 设置加载状态
		pagination.loading = true;

		// 导入相亲广场API
		const squareApi = await import('@/api/square.js');

		// 调用API获取用户列表
		const result = await squareApi.default.getUserList(pagination.pageNum, pagination.pageSize);

		if (result.code === 200) {
			// 更新分页信息
			pagination.total = result.data.total;
			pagination.hasMore = pagination.pageNum * pagination.pageSize < pagination.total;

			// 将接口返回的数据转换为前端需要的格式，安全处理可能为空的字段
			const newUsers = result.data.records.map(user => ({
				id: user.userId,
				name: user.nickname || '匿名用户',
				age: getUserAge(user),
				selfIntroduction: getUserIntroduction(user),
				avatar: user.avatarUrl || '/static/default-avatar.png',
				tags: getUserTags(user),
				isLiked: false
			}));

			// 如果是刷新，直接替换数据，否则追加数据
			if (refresh) {
				users.value = newUsers;
			} else {
				users.value = users.value.concat(newUsers);
			}

			// 页码加1，为下次加载做准备
			pagination.pageNum++;

			console.log('用户列表获取成功:', users.value);
		} else {
			console.error('用户列表获取失败:', result);
			// 如果是第一页且失败，设置默认数据
			if (pagination.pageNum === 1) {
				setDefaultUsers();
			}
		}
	} catch (error) {
		console.error('用户列表获取异常:', error);
		// 如果是第一页且异常，设置默认数据
		if (pagination.pageNum === 1) {
			setDefaultUsers();
		}
	} finally {
		// 无论成功失败，都结束加载状态
		pagination.loading = false;

		// 如果是下拉刷新，结束刷新状态
		if (refresh) {
			uni.stopPullDownRefresh();
		}
	}
}

// 设置默认用户数据
function setDefaultUsers() {
	users.value = [
		{
			id: 1,
			name: '小雨',
			age: 26,
			tags: ['旅行', '摄影', '美食'],
			intro: '喜欢旅行，热爱生活，期待遇见有趣的灵魂',
			avatar: '/static/square/avatar1.png',
			isLiked: false
		},
		{
			id: 2,
			name: '阳阳',
			age: 28,
			tags: ['工程师', '健身', '电影'],
			intro: '工作认真负责，生活热情洋溢，喜欢健身和看电影',
			avatar: '/static/square/avatar2.png',
			isLiked: false
		},
		{
			id: 3,
			name: '小米',
			age: 25,
			tags: ['教师', '阅读', '音乐'],
			intro: '温柔贤淑，喜欢阅读和音乐，希望遇到志同道合的人',
			avatar: '/static/square/avatar3.png',
			isLiked: false
		},
		{
			id: 4,
			name: '大壮',
			age: 30,
			tags: ['医生', '烹饪', '篮球'],
			intro: '稳重可靠，工作是医生，业余爱好烹饪和打篮球',
			avatar: '/static/square/avatar4.png',
			isLiked: false
		},
		{
			id: 5,
			name: '晓晓',
			age: 27,
			tags: ['设计师', '插画', '咖啡'],
			intro: '创意无限，热爱设计和插画，喜欢品尝各种咖啡',
			avatar: '/static/square/avatar5.png',
			isLiked: false
		},
		{
			id: 6,
			name: '明明',
			age: 29,
			tags: ['金融', '游泳', '钢琴'],
			intro: '从事金融行业，爱好游泳和弹钢琴，性格开朗',
			avatar: '/static/square/avatar6.png',
			isLiked: false
		}
	];
}

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
}

// 地区选择相关方法
function showRegionPicker() {
	tempRegion.value = selectedRegion.value;
	const index = regions.findIndex(item => item === selectedRegion.value);
	if (index !== -1) {
		regionIndex.value = [index];
	}
	regionPopup.value.open();
}

function onRegionChange(e) {
	const index = e.detail.value[0];
	tempRegion.value = regions[index];
}

async function confirmRegionSelect() {
	selectedRegion.value = tempRegion.value;
	regionPopup.value.close();

	// 更新用户省份信息
	await updateUserProvinceInfo(selectedRegion.value);
}

function cancelRegionSelect() {
	regionPopup.value.close();
}

// 切换筛选选项卡
function switchTab(index) {
	console.log('切换选项卡:', index, filterTabs[index]);
	currentTabIndex.value = index;

	// 如果是动态广场，加载动态数据
	if (filterTabs[index].type === 'moment') {
		console.log('切换到动态广场，开始加载动态数据');
		loadMomentList(true);
		return;
	}

	// 根据选项卡类型加载不同的数据
	console.log('加载用户数据:', filterTabs[index].type);
	loadUsers(filterTabs[index].type, true);
}

// 页面加载时获取数据
onMounted(() => {
	console.log('🏛️ 广场页挂载，当前主题:', currentBackground.value);

	// 初始加载推荐用户列表
	loadUsers('recommend');
	// 获取钱包数据
	getWalletData();
	// 获取用户信息并设置当前省份
	loadUserInfo();
});

onUnmounted(() => {
  console.log('=== 广场页卸载 ===');
});

// 页面显示时刷新钱包数据
onShow(() => {
	getWalletData();
});

// 下拉刷新
onPullDownRefresh(() => {
	// 根据当前选项卡刷新对应的数据
	if (filterTabs[currentTabIndex.value].type === 'moment') {
		onMomentRefresh();
	} else {
		loadUsers(filterTabs[currentTabIndex.value].type, true);
	}
});

// 触底加载更多
onReachBottom(() => {
	// 根据当前选项卡加载更多数据
	if (filterTabs[currentTabIndex.value].type === 'moment') {
		if (!momentPagination.loading && momentPagination.hasMore) {
			loadMoreMoments();
		}
	} else {
		if (!pagination.loading && pagination.hasMore) {
			loadUsers(filterTabs[currentTabIndex.value].type);
		}
	}
});

// 查看用户详情
function viewUserDetail(user) {
	uni.navigateTo({
		url: `/pages/user/user-detail?id=${user.id}`
	});
}

// 喜欢用户
function likeUser(user, index) {
	users.value[index].isLiked = !users.value[index].isLiked;
	if (users.value[index].isLiked) {
		uni.showToast({
			title: '已喜欢',
			icon: 'none'
		});
	}
}

// 搜索页面
function goSearch() {
	uni.navigateTo({
		url: '/pages/search/search'
	});
}

// 获取钱包信息
async function getWalletData() {
	try {
		const token = uni.getStorageSync("token");
		if (!token) {
			console.log('未登录，无法获取钱包信息');
			return;
		}

		const response = await getWalletInfo();
		if (response.code === 200) {
			userCurrency.value = response.data.coinBalance || 0;
		}
	} catch (error) {
		console.error('获取钱包信息失败:', error);
	}
}

// 快速切换主题
function quickSwitchTheme() {
	const { availableThemes, currentThemeId, switchTheme } = useGlobalThemeMixin();
	
	// 获取当前主题索引
	const themes = availableThemes.value;
	const currentIndex = themes.findIndex(t => t.id === currentThemeId.value);
	
	// 切换到下一个主题
	const nextIndex = (currentIndex + 1) % themes.length;
	const nextTheme = themes[nextIndex];
	
	console.log('🎨 快速切换主题:', currentThemeId.value, '->', nextTheme.id);
	
	const success = switchTheme(nextTheme.id);
	
	if (success) {
		uni.showToast({
			title: `已切换到${nextTheme.name}`,
			icon: 'success',
			duration: 1500
		});
	} else {
		uni.showToast({
			title: '主题切换失败',
			icon: 'error'
		});
	}
}

// 跳转到钱包页面
function goToWallet() {
	const token = uni.getStorageSync("token");
	if (!token) {
		uni.showToast({
			title: '请先登录',
			icon: 'none'
		});
		uni.navigateTo({
			url: '/pages/login/login'
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/wallet/wallet'
	});
}

// 跳转到附近的人地图页面
function goToNearbyMap() {
	uni.navigateTo({
		url: '/pages/nearby/nearby'
	});
}

// 获取用户信息并设置当前省份
async function loadUserInfo() {
	try {
		const token = uni.getStorageSync('token');
		if (!token) {
			console.log('未登录，使用默认省份');
			return;
		}

		const result = await getByUserInfo(token);
		if (result.code === 200 && result.data) {
			const userInfo = result.data;
			// 如果用户有省份信息，设置为当前选中的省份
			if (userInfo.lastLoginIp) {
				const provinceShortName = getProvinceShortName(userInfo.lastLoginIp);
				selectedRegion.value = provinceShortName;
				console.log('设置用户当前省份:', provinceShortName);
			}
		}
	} catch (error) {
		console.error('获取用户信息失败:', error);
	}
}

// 更新用户省份信息
async function updateUserProvinceInfo(province) {
	try {
		const token = uni.getStorageSync('token');
		if (!token) {
			uni.showToast({
				title: '请先登录',
				icon: 'none'
			});
			return;
		}

		const result = await updateUserProvince(token, province);
		if (result.code === 200) {
			uni.showToast({
				title: '省份更新成功',
				icon: 'success'
			});
			console.log('省份更新成功:', province);
		} else {
			uni.showToast({
				title: result.message || '更新失败',
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('更新省份失败:', error);
		uni.showToast({
			title: '更新失败，请重试',
			icon: 'none'
		});
	}
}

// 动态相关方法
// 加载动态列表
async function loadMomentList(isRefresh = false) {
	console.log('开始加载动态列表, isRefresh:', isRefresh);

	if (momentPagination.loading) {
		console.log('动态正在加载中，跳过');
		return;
	}

	momentPagination.loading = true;

	try {
		// 导入动态API
		const momentApi = await import('@/api/moment.js');

		const page = isRefresh ? 1 : momentPagination.pageNum;
		console.log('请求动态数据，页码:', page, '页大小:', momentPagination.pageSize);

		const result = await momentApi.default.getPublicMoments(page, momentPagination.pageSize);
		console.log('动态API响应:', result);

		if (result.code === 200) {
			const records = result.data.records || [];
			console.log('获取到动态数据:', records.length, '条');

			if (isRefresh) {
				momentList.value = records;
				momentPagination.pageNum = 1;
				momentPagination.hasMore = true;
			} else {
				momentList.value.push(...records);
			}

			// 检查是否还有更多数据
			if (records.length < momentPagination.pageSize) {
				momentPagination.hasMore = false;
			} else {
				momentPagination.pageNum++;
			}

			console.log('动态列表更新完成，总数:', momentList.value.length);
		} else {
			console.error('动态API返回错误:', result);
			throw new Error(result.message || '加载失败');
		}

	} catch (error) {
		console.error('加载动态列表失败:', error);

		// 如果是网络错误或API不可用，添加一些测试数据
		if (momentList.value.length === 0) {
			console.log('添加测试动态数据');
			momentList.value = [
				{
					momentId: 'test1',
					nickname: '测试用户1',
					avatarUrl: '/static/default-avatar.png',
					content: '这是一条测试动态，用于验证动态广场功能是否正常工作。',
					createdAt: new Date().toISOString(),
					location: '测试地点',
					likeCount: 5,
					commentCount: 2,
					viewCount: 10,
					isLiked: false,
					mediaList: []
				},
				{
					momentId: 'test2',
					nickname: '测试用户2',
					avatarUrl: '/static/default-avatar.png',
					content: '又一条测试动态，包含更多内容来测试显示效果。动态广场应该能够正常显示这些内容。',
					createdAt: new Date(Date.now() - 3600000).toISOString(),
					location: '另一个测试地点',
					likeCount: 8,
					commentCount: 3,
					viewCount: 15,
					isLiked: true,
					mediaList: []
				}
			];
		}

		uni.showToast({
			title: '使用测试数据',
			icon: 'none'
		});
	} finally {
		momentPagination.loading = false;
		momentRefreshing.value = false;
	}
}

// 动态下拉刷新
function onMomentRefresh() {
	momentRefreshing.value = true;
	loadMomentList(true);
}

// 动态加载更多
function loadMoreMoments() {
	if (!momentPagination.hasMore) {
		return;
	}
	loadMomentList();
}

// 格式化时间
function formatTime(timeStr) {
	const time = new Date(timeStr);
	const now = new Date();
	const diff = now - time;

	if (diff < 60000) return '刚刚';
	if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
	if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
	return Math.floor(diff / 86400000) + '天前';
}

// 查看动态详情
function viewMomentDetail(moment) {
	uni.navigateTo({
		url: `/pages/moment/detail?id=${moment.momentId}`
	});
}

// 点赞动态
async function likeMoment(moment) {
	try {
		// 导入动态API
		const momentApi = await import('@/api/moment.js');

		const result = await momentApi.default.likeMoment(moment.momentId);
		if (result.code === 200) {
			// 更新本地状态
			moment.isLiked = !moment.isLiked;
			moment.likeCount = moment.isLiked ? (moment.likeCount || 0) + 1 : Math.max((moment.likeCount || 0) - 1, 0);
		} else {
			throw new Error(result.message || '操作失败');
		}
	} catch (error) {
		console.error('点赞失败:', error);
		uni.showToast({
			title: error.message || '操作失败',
			icon: 'none'
		});
	}
}

// 评论动态
function commentMoment(moment) {
	uni.navigateTo({
		url: `/pages/moment/detail?id=${moment.momentId}&action=comment`
	});
}

// 跳转到动态详情页面
async function goToMomentDetail(moment) {
	try {
		// 增加浏览次数
		const momentApi = await import('@/api/moment.js');
		await momentApi.default.incrementViewCount(moment.momentId);

		// 更新本地浏览次数
		moment.viewCount = (moment.viewCount || 0) + 1;

		// 跳转到详情页面
		uni.navigateTo({
			url: `/pages/moment/detail?id=${moment.momentId}`
		});
	} catch (error) {
		console.error('跳转详情页面失败:', error);
		// 即使API调用失败，也允许跳转
		uni.navigateTo({
			url: `/pages/moment/detail?id=${moment.momentId}`
		});
	}
}

// 显示动态菜单
function showMomentMenu(moment) {
	uni.showActionSheet({
		itemList: ['举报', '不感兴趣', '屏蔽用户'],
		success: function (res) {
			switch (res.tapIndex) {
				case 0:
					// 举报
					reportMoment(moment);
					break;
				case 1:
					// 不感兴趣
					markNotInterested(moment);
					break;
				case 2:
					// 屏蔽用户
					blockUser(moment);
					break;
			}
		}
	});
}

// 举报动态
function reportMoment(moment) {
	uni.showToast({
		title: '举报成功',
		icon: 'success'
	});
}

// 标记不感兴趣
function markNotInterested(moment) {
	uni.showToast({
		title: '已标记',
		icon: 'success'
	});
}

// 屏蔽用户
function blockUser(moment) {
	uni.showToast({
		title: '已屏蔽',
		icon: 'success'
	});
}

// 显示更多选项
function showMoreOptions(moment) {
	uni.showActionSheet({
		itemList: ['删除'],
		success: (res) => {
			if (res.tapIndex === 0) {
				deleteMoment(moment);
			}
		}
	});
}

// 删除动态
async function deleteMoment(moment) {
	try {
		const result = await uni.showModal({
			title: '确认删除',
			content: '确定要删除这条动态吗？',
			confirmText: '删除',
			confirmColor: '#ff4757'
		});

		if (!result.confirm) return;

		// 导入动态API
		const momentApi = await import('@/api/moment.js');

		const deleteResult = await momentApi.default.deleteMoment(moment.momentId);
		if (deleteResult.code === 200) {
			// 从列表中移除
			const index = momentList.value.findIndex(item => item.momentId === moment.momentId);
			if (index !== -1) {
				momentList.value.splice(index, 1);
			}
			uni.showToast({
				title: '删除成功',
				icon: 'success'
			});
		} else {
			throw new Error(deleteResult.message || '删除失败');
		}
	} catch (error) {
		console.error('删除动态失败:', error);
		uni.showToast({
			title: error.message || '删除失败',
			icon: 'none'
		});
	}
}

// 获取媒体网格样式类
function getMediaGridClass(count) {
	if (count === 1) return 'single';
	if (count === 2) return 'double';
	if (count === 3) return 'three';
	if (count === 4) return 'four';
	if (count === 5) return 'five';
	if (count === 6) return 'six';
	if (count === 7) return 'seven';
	if (count === 8) return 'eight';
	return 'nine';
}

// 预览动态图片
function previewMomentMedia(mediaList, index) {
	const urls = mediaList.map(media => media.mediaUrl);
	uni.previewImage({
		urls: urls,
		current: index
	});
}
</script>

<style lang="scss" scoped>
.square-page {
	position: relative;
	width: 100%;
	min-height: 100vh;
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
		padding: 40rpx 30rpx 110rpx 30rpx; // 底部增加110rpx padding为自定义tabBar预留空间
		z-index: 1;
		
		// 顶部区域
		.header {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-bottom: 30rpx;
			
			.left-section {
				display: flex;
				align-items: center;
				flex: 1;
			}
			
			// 地区选择器
			.region-selector {
				display: flex;
				align-items: center;
				padding: 0 20rpx;
				height: 60rpx;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 30rpx;
				
				.region-text {
					font-size: 26rpx;
					color: #fff;
					margin-right: 6rpx;
				}
				
				.region-arrow {
					width: 16rpx;
					height: 16rpx;
					border-right: 2rpx solid #fff;
					border-bottom: 2rpx solid #fff;
					transform: rotate(45deg);
					margin-top: -6rpx;
				}
			}
			
			.search-box {
				flex: 1;
				height: 60rpx;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 30rpx;
				margin-left: 20rpx;
				display: flex;
				align-items: center;
				padding: 0 20rpx;
				max-width: 300rpx;
				
				.search-icon {
					width: 32rpx;
					height: 32rpx;
					background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>') no-repeat center;
					background-size: contain;
					margin-right: 10rpx;
				}
				
				.search-placeholder {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.8);
				}
			}

			// 虚拟货币
			.currency-section {
				display: flex;
				align-items: center;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 30rpx;
				padding: 0 20rpx;
				height: 60rpx;

				.currency-icon {
					width: 36rpx;
					height: 36rpx;
					margin-right: 8rpx;

					.coin-placeholder {
						width: 100%;
						height: 100%;
						background: rgba(255, 255, 255, 0.3);
						border-radius: 50%;
					}
				}

				.currency-amount {
					font-size: 26rpx;
					color: #fff;
					font-weight: bold;
				}
			}

			// 主题切换按钮
			.theme-switch-btn {
				display: flex;
				align-items: center;
				justify-content: center;
				width: 60rpx;
				height: 60rpx;
				background: rgba(255, 255, 255, 0.2);
				border-radius: 50%;
				margin-left: 20rpx;

				.theme-icon {
					font-size: 32rpx;
				}
			}
		}
		
		// 主内容区
		.main-content {
			flex: 1;
			
			// 筛选选项卡
			.filter-tabs {
				display: flex;
				background: rgba(255, 255, 255, 0.1);
				border-radius: 20rpx;
				margin-bottom: 30rpx;
				padding: 10rpx;
				
				.filter-tab {
					flex: 1;
					text-align: center;
					padding: 15rpx 0;
					border-radius: 15rpx;
					transition: all 0.3s;
					
					text {
						font-size: 28rpx;
						color: rgba(255, 255, 255, 0.8);
					}
					
					&.active {
						background: rgba(255, 255, 255, 0.2);
						
						text {
							color: #fff;
							font-weight: bold;
						}
					}
				}
			}
			
			// 用户网格
			.user-grid {
				display: flex;
				flex-direction: column;
				
				.loading-more {
					display: flex;
					flex-direction: column;
					align-items: center;
					justify-content: center;
					padding: 30rpx 0;
					
					.loading-icon {
						width: 40rpx;
						height: 40rpx;
						border: 4rpx solid rgba(255, 255, 255, 0.2);
						border-top: 4rpx solid #fff;
						border-radius: 50%;
						animation: spin 1s linear infinite;
						margin-bottom: 10rpx;
					}
					
					.loading-text {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.7);
					}
				}
				
				.no-more-data, .empty-data {
					display: flex;
					justify-content: center;
					padding: 30rpx 0;
					font-size: 24rpx;
					color: rgba(255, 255, 255, 0.7);
				}
				
				.empty-data {
					padding: 100rpx 0;
					
					.empty-text {
						font-size: 28rpx;
						color: rgba(255, 255, 255, 0.7);
					}
				}
				
				.user-card {
					display: flex;
					background: rgba(255, 255, 255, 0.15);
					border-radius: 20rpx;
					padding: 20rpx;
					margin-bottom: 20rpx;
					position: relative;
					
					.user-avatar {
						width: 140rpx;
						height: 140rpx;
						border-radius: 15rpx;
						background-color: rgba(255, 255, 255, 0.1);
						
						.avatar-placeholder {
							width: 100%;
							height: 100%;
							background: rgba(255, 255, 255, 0.1);
							border-radius: 15rpx;
						}
					}
					
					.user-info {
						flex: 1;
						margin-left: 20rpx;
						overflow: hidden;
						
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
								background: rgba(255, 255, 255, 0.2);
								padding: 4rpx 12rpx;
								border-radius: 20rpx;
							}
						}
						
						.user-tags {
							display: flex;
							flex-wrap: wrap;
							margin-bottom: 10rpx;
							
							.user-tag {
								font-size: 22rpx;
								color: rgba(255, 255, 255, 0.9);
								background: rgba(255, 255, 255, 0.15);
								padding: 4rpx 12rpx;
								border-radius: 10rpx;
								margin-right: 10rpx;
								margin-bottom: 10rpx;
							}
						}
						
						.user-intro {
							font-size: 24rpx;
							color: rgba(255, 255, 255, 0.7);
							display: -webkit-box;
							-webkit-line-clamp: 2;
							-webkit-box-orient: vertical;
							overflow: hidden;
						}
					}
					
					.interaction-btn {
						position: absolute;
						right: 20rpx;
						top: 20rpx;
						width: 60rpx;
						height: 60rpx;
						display: flex;
						align-items: center;
						justify-content: center;
						
						.btn-icon {
							font-size: 40rpx;
							color: rgba(255, 255, 255, 0.7);
							transition: all 0.3s;
							
							&.liked {
								color: #FF6B6B;
								transform: scale(1.2);
							}
						}
					}
				}
			}
		}
	}
}

// 背景渐变样式
.bg-gradient {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
	background-size: 300% 300%;
	animation: gradientAnimation 15s ease infinite;
	position: relative;
	z-index: 0;
	
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
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.05)" fill-opacity="1" d="M0,64L48,96C96,128,192,192,288,202.7C384,213,480,171,576,165.3C672,160,768,192,864,197.3C960,203,1056,181,1152,165.3C1248,149,1344,139,1392,133.3L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom;
		background-size: 100% 40%;
		opacity: 0.6;
	}
	
	&::after {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background: radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%),
					radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0) 60%);
	}
}

// 地区选择弹窗
.region-popup {
	background-color: #fff;
	border-radius: 20rpx 20rpx 0 0;
	overflow: hidden;
	
	.region-popup-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.title {
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
			color: #C471ED;
		}
	}
	
	.region-picker {
		height: 400rpx;
		
		.picker-item {
			line-height: 80rpx;
			text-align: center;
			font-size: 32rpx;
			color: #333;
		}
	}
}

// 动态广场样式
.moment-grid {
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	.moment-card {
		background: transparent;
		border-radius: 0;
		padding: 30rpx 20rpx;
		border: none;
		box-shadow: none;
		transition: all 0.3s ease;
		position: relative;

		&:active {
			background: rgba(255, 255, 255, 0.05);
		}

		// 添加底部分割线
		&:not(:last-child)::after {
			content: '';
			position: absolute;
			bottom: 0;
			left: 20rpx;
			right: 20rpx;
			height: 1rpx;
			background: rgba(255, 255, 255, 0.1);
		}

		.moment-header {
			display: flex;
			align-items: center;
			margin-bottom: 20rpx;
			position: relative;

			.user-avatar {
				width: 80rpx;
				height: 80rpx;
				border-radius: 50%;
				margin-right: 20rpx;
				border: none;
				box-shadow: none;

				.avatar-placeholder {
					width: 100%;
					height: 100%;
					background: #ccc;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;

					&::before {
						content: '👤';
						font-size: 32rpx;
						color: rgba(255, 255, 255, 0.8);
					}
				}
			}

			.user-info {
				flex: 1;

				.user-nickname {
					display: block;
					font-size: 32rpx;
					font-weight: 600;
					color: #fff;
					margin-bottom: 6rpx;
					text-shadow: none;
				}

				.publish-time {
					font-size: 26rpx;
					color: rgba(255, 255, 255, 0.7);
					text-shadow: none;
				}
			}

			.moment-menu {
				padding: 8rpx;

				.menu-icon {
					width: 24rpx;
					height: 24rpx;
					opacity: 0.6;
					transition: all 0.3s ease;

					&:active {
						opacity: 0.8;
					}
				}
			}
		}

		.moment-location {
			display: flex;
			align-items: center;
			margin-bottom: 16rpx;
			padding: 0;
			background: transparent;
			align-self: flex-start;

			.location-icon {
				font-size: 24rpx;
				margin-right: 8rpx;
				color: rgba(255, 255, 255, 0.6);
			}

			.location-text {
				font-size: 26rpx;
				color: rgba(255, 255, 255, 0.6);
			}
		}

		.moment-media {
			margin-bottom: 24rpx;

			.media-grid {
				display: grid;
				gap: 8rpx;
				border-radius: 20rpx;
				overflow: hidden;

				&.single {
					grid-template-columns: 1fr;
					max-width: 500rpx;

					.media-item {
						height: 400rpx;
						border-radius: 20rpx;
					}
				}

				&.double {
					grid-template-columns: 1fr 1fr;

					.media-item {
						height: 280rpx;
						border-radius: 16rpx;
					}
				}

				&.three {
					grid-template-columns: 1fr 1fr 1fr;

					.media-item {
						height: 200rpx;
						border-radius: 16rpx;
					}
				}

				&.four {
					grid-template-columns: 1fr 1fr;
					grid-template-rows: 1fr 1fr;

					.media-item {
						height: 200rpx;
						border-radius: 16rpx;
					}
				}

				&.five, &.six {
					grid-template-columns: 1fr 1fr 1fr;
					grid-template-rows: 1fr 1fr;

					.media-item {
						height: 160rpx;
						border-radius: 12rpx;
					}
				}

				&.seven, &.eight, &.nine {
					grid-template-columns: 1fr 1fr 1fr;
					grid-template-rows: 1fr 1fr 1fr;

					.media-item {
						height: 130rpx;
						border-radius: 12rpx;
					}
				}

				.media-item {
					position: relative;
					overflow: hidden;
					background: transparent;

					.media-image {
						width: 100%;
						height: 100%;
						object-fit: cover;
					}

					.image-placeholder {
						width: 100%;
						height: 100%;
						background: rgba(0, 0, 0, 0.2);
						display: flex;
						align-items: center;
						justify-content: center;

						&::before {
							content: '📷';
							font-size: 30rpx;
							color: rgba(255, 255, 255, 0.6);
						}
					}
				}
			}
		}

		.moment-content {
			font-size: 32rpx;
			line-height: 1.5;
			color: rgba(255, 255, 255, 0.95);
			margin-bottom: 24rpx;
			text-shadow: none;
		}

		.moment-footer {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-top: 16rpx;
			padding-top: 0;
			border-top: none;

			.moment-actions {
				display: flex;
				align-items: center;
				gap: 60rpx;

				.action-item {
					display: flex;
					align-items: center;
					gap: 8rpx;
					padding: 0;
					border-radius: 0;
					background: transparent;
					transition: all 0.3s ease;

					&:active {
						background: transparent;
						transform: scale(0.95);
					}

					.action-icon {
						width: 32rpx;
						height: 32rpx;
						transition: all 0.3s ease;

						&.liked {
							filter: none;
						}
					}

					.action-count {
						font-size: 28rpx;
						color: rgba(255, 255, 255, 0.8);
						min-width: 32rpx;
						text-align: left;
						font-weight: 400;
						text-shadow: none;
					}
				}
			}
		}
	}

	// 加载状态样式
	.loading-more {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 30rpx 0;

		.loading-icon {
			width: 40rpx;
			height: 40rpx;
			border: 3rpx solid rgba(255, 255, 255, 0.3);
			border-top: 3rpx solid #C471ED;
			border-radius: 50%;
			animation: spin 1s linear infinite;
			margin-bottom: 10rpx;
		}

		.loading-text {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.7);
		}
	}

	.no-more-data, .empty-data {
		display: flex;
		justify-content: center;
		padding: 30rpx 0;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.7);
	}

	.empty-data {
		padding: 100rpx 0;

		.empty-text {
			font-size: 28rpx;
			color: rgba(255, 255, 255, 0.7);
		}
	}
}

/* 悬浮地图按钮 */
.floating-map-btn {
	position: fixed;
	right: 30rpx;
	bottom: 200rpx; /* 在TabBar上方 */
	width: 100rpx;
	height: 100rpx;
	background: linear-gradient(135deg, #00D4AA 0%, #00B894 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(0, 212, 170, 0.4);
	z-index: 999;
	transition: all 0.3s ease;

	&:active {
		transform: scale(0.9);
	}

	.map-icon {
		font-size: 40rpx;
		color: #fff;
	}
}

/* 动画定义 */
@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>
