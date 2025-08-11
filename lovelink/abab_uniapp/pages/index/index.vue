<template>
	<view class="index-page">
		<!-- 背景层 -->
		<view class="bg-layer" :style="{ background: currentBackground }">
			<view class="bg-gradient"></view>
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
				<!-- 轮播图 -->
				<swiper class="banner-swiper" circular autoplay interval="5000" duration="500" indicator-dots indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#fff">
					<swiper-item v-for="(banner, index) in banners" :key="index" @tap="handleBannerClick(banner)">
						<image class="banner-image" :src="banner.image" mode="aspectFill" @error="handleImageError(`banner${index}`)">
							<view v-if="imageError[`banner${index}`]" class="banner-placeholder"></view>
						</image>
						<view class="banner-title">{{ banner.title }}</view>
					</swiper-item>
				</swiper>
				
				<!-- 心动游戏大厅 -->
				<view class="section game-section">
					<view class="section-header">
						<text class="section-title">心动游戏大厅</text>
						<text class="more-link" @tap="goMoreGames">更多</text>
					</view>
					<scroll-view scroll-x class="game-list" :show-scrollbar="false">
						<view class="game-item" v-for="(game, index) in games" :key="index" @tap="startGame(game)">
							<image class="game-image" :src="game.image" mode="aspectFill" @error="handleImageError(`game${index}`)">
								<view v-if="imageError[`game${index}`]" class="image-placeholder"></view>
							</image>
							<text class="game-name">{{game.name}}</text>
							<text class="game-desc">{{game.desc}}</text>
						</view>
					</scroll-view>
				</view>
				
				<!-- 相亲广场 -->
				<view class="section match-section">
					<view class="section-header">
						<text class="section-title">相亲广场</text>
						<text class="more-link" @tap="goMoreMatches">更多</text>
					</view>
					<scroll-view scroll-x class="match-list" :show-scrollbar="false">
						<view class="match-item" v-for="(match, index) in matches" :key="index" @tap="viewMatch(match)">
							<image class="match-image" :src="match.avatar" mode="aspectFill" @error="handleImageError(`match${index}`)">
								<view v-if="imageError[`match${index}`]" class="avatar-placeholder"></view>
							</image>
							<view class="match-info">
								<text class="match-name">{{match.name}}</text>
								<text class="match-age">{{match.gender === 1 ? '♂' : '♀'}}</text>
							</view>
							<text class="match-intro">{{match.selfIntroduction}}</text>
						</view>
					</scroll-view>
				</view>
				
				<!-- 娱乐专区 -->
				<view class="section entertainment-section">
					<view class="section-header">
						<text class="section-title">娱乐专区</text>
						<text class="more-link" @tap="goMoreEntertainment">更多</text>
					</view>
					<view class="entertainment-grid">
						<view class="entertainment-item" v-for="(item, index) in entertainment" :key="index" @tap="goEntertainment(item)">
							<image class="entertainment-image" :src="item.image" mode="aspectFill" @error="handleImageError(`entertainment${index}`)">
								<view v-if="imageError[`entertainment${index}`]" class="image-placeholder"></view>
							</image>
							<text class="entertainment-name">{{item.name}}</text>
						</view>
					</view>
				</view>
				
				<!-- VIP专区 -->
				<view class="section vip-section">
					<view class="section-header">
						<text class="section-title">VIP专区</text>
						<text class="more-link" @tap="goVipCenter">查看特权</text>
					</view>
					<view class="vip-card" @tap="goVipCenter">
						<view class="vip-info">
							<text class="vip-title">{{ vipStatus.isVip ? 'VIP会员' : '开通VIP' }}</text>
							<text class="vip-desc">{{ vipStatus.isVip ? '会员有效期至 ' + formatVipExpireTime() : '专属特权 · 1v1精准配对' }}</text>
						</view>
						<view class="vip-button">{{ vipStatus.isVip ? '续费' : '立即开通' }}</view>
					</view>
				</view>

				<!-- 约会中心 -->
				<view class="section date-section">
					<view class="section-header">
						<text class="section-title">我的约会</text>
						<text class="more-link" @tap="goDateCenter">查看全部</text>
					</view>
					<view class="date-main-card" @tap="goDateCenter">
						<view class="date-main-content">
							<view class="date-main-icon">💕</view>
							<view class="date-main-info">
								<text class="date-main-title">约会中心</text>
								<text class="date-main-desc">管理约会安排 · 确认参加约会</text>
							</view>
							<view v-if="unreadDateCount > 0" class="date-main-badge">{{ unreadDateCount }}</view>
						</view>
					</view>
					<view class="date-quick-stats">
						<view class="quick-stat-item" @tap="goDateCenter">
							<text class="stat-number">{{ pendingDateCount }}</text>
							<text class="stat-label">待确认</text>
						</view>
						<view class="quick-stat-item" @tap="goDateCenter">
							<text class="stat-number">{{ confirmedDateCount }}</text>
							<text class="stat-label">已确认</text>
						</view>
						<view class="quick-stat-item" @tap="goDateCenter">
							<text class="stat-number">{{ totalDateCount }}</text>
							<text class="stat-label">总约会</text>
						</view>
					</view>

					<!-- 约会快捷功能 -->
					<view class="date-quick-actions">
						<view class="quick-action-item" @tap="goToReminders">
							<view class="action-icon">🔔</view>
							<text class="action-text">约会提醒</text>
						</view>
						<view class="quick-action-item" @tap="goDateManagement">
							<view class="action-icon">📅</view>
							<text class="action-text">约会管理</text>
						</view>
						<view class="quick-action-item" @tap="goDateCenter">
							<view class="action-icon">💕</view>
							<text class="action-text">约会中心</text>
						</view>
						<view class="quick-action-item" @tap="goToProfile">
							<view class="action-icon">👤</view>
							<text class="action-text">个人资料</text>
						</view>
					</view>


				</view>

				<!-- 红娘申请专区 -->
				<view class="section matchmaker-section">
					<view class="section-header">
						<text class="section-title">成为红娘</text>
						<text class="more-link" @tap="goMatchmakerApply">立即申请</text>
					</view>
					<view class="matchmaker-card" @tap="goMatchmakerApply">
						<view class="matchmaker-info">
							<text class="matchmaker-title">申请成为红娘</text>
							<text class="matchmaker-desc">人人都可申请 · 开启收益之路</text>
						</view>
						<view class="matchmaker-button">699币申请</view>
					</view>
				</view>

				<!-- 订单管理专区 -->
				<view class="section order-section">
					<view class="section-header">
						<text class="section-title">我的订单</text>
						<text class="more-link" @tap="goOrderList">查看全部</text>
					</view>
					<view class="order-quick-actions">
						<view class="order-action-item" @tap="goOrderList">
							<view class="order-action-icon">📋</view>
							<text class="order-action-text">全部订单</text>
						</view>
						<view class="order-action-item" @tap="goOrderListWithStatus(0)">
							<view class="order-action-icon">💳</view>
							<text class="order-action-text">待支付</text>
						</view>
						<view class="order-action-item" @tap="goOrderListWithStatus(1)">
							<view class="order-action-icon">✅</view>
							<text class="order-action-text">已支付</text>
						</view>
						<view class="order-action-item" @tap="goOrderListWithStatus(4)">
							<view class="order-action-icon">🎉</view>
							<text class="order-action-text">已完成</text>
						</view>
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
		
		<!-- 自定义TabBar -->
		<custom-tab-bar />
	</view>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import customTabBar from '@/components/custom-tab-bar.vue';
import { getWalletInfo } from '@/api/wallet';
import { CHINA_PROVINCES, getProvinceShortName } from '@/utils/provinces';
import config from '@/api/config';
import { getByUserInfo, updateUserProvince } from '@/api/user/auth';
import { getUserVipStatus } from '@/api/vip.js';
import { useGlobalThemeMixin } from '@/mixins/global-theme-mixin.js';

// 图片加载错误状态
const imageError = reactive({
	logo: false,
	coin: false
});

// 虚拟货币
const userCurrency = ref(0);

// VIP状态
const vipStatus = ref({
	isVip: false,
	userRole: 1,
	vipExpireTime: null
});

// 用户信息
const userInfo = ref({
	userId: null,
	nickname: '',
	avatar: ''
});

// 使用全局主题混入
const { currentBackground } = useGlobalThemeMixin();

console.log('首页初始化，使用全局主题系统');

// 页面生命周期
onMounted(() => {
  console.log('🏠 首页挂载，当前主题:', currentBackground.value);

	// 获取轮播图和推荐用户数据
	fetchBanners();
	fetchRecommendUsers();

	// 获取钱包数据和VIP状态
	getWalletData();
	loadUserInfo();
	getVipStatusData();

	// 监听省份更新事件
	uni.$on('refreshUserProvince', () => {
		console.log('收到省份更新事件，延迟5秒后刷新用户信息');
		setTimeout(() => {
			loadUserInfo();
		}, 5000); // 给后端异步处理留出更多时间

		// 再次延迟刷新，确保数据同步
		setTimeout(() => {
			console.log('二次刷新用户省份信息');
			loadUserInfo();
		}, 8000);
	});
});

onUnmounted(() => {
  console.log('=== 首页卸载 ===');

  // 移除事件监听
	uni.$off('refreshUserProvince');
});

// 监听页面显示
onShow(() => {
  console.log('首页显示');
});

// 地区选择相关
const regionPopup = ref(null);
const regions = CHINA_PROVINCES.map(province => getProvinceShortName(province));
const regionIndex = ref([0]);
const selectedRegion = ref('北京');
const tempRegion = ref('');
const indicatorStyle = 'height: 80rpx;';

// 约会相关数据
const unreadDateCount = ref(0);
const pendingDateCount = ref(0);
const confirmedDateCount = ref(0);
const totalDateCount = ref(0);

// 轮播图数据
const banners = ref([]);

// 获取轮播图数据
async function fetchBanners() {
	try {
		const result = await new Promise((resolve, reject) => {
			uni.request({
				url: 'http://localhost:9001/lunb/lunbList',
				method: 'POST',
				success: (res) => {
					resolve(res);
				},
				fail: (err) => {
					reject(err);
				}
			});
		});
		
		if (result.statusCode === 200 && result.data.code === 200) {
			// 将接口返回的数据转换为前端需要的格式
			banners.value = result.data.data.map(item => ({
				id: item.bannerId,
				title: item.bannerTitle,
				image: item.bannerImage,
				url: item.bannerUrl || '#',
				targetType: item.targetType,
				targetId: item.targetId
			}));
			console.log('轮播图数据获取成功:', banners.value);
		} else {
			console.error('轮播图数据获取失败:', result);
			// 设置默认数据，防止界面空白
			setDefaultBanners();
		}
	} catch (error) {
		console.error('轮播图数据获取异常:', error);
		// 设置默认数据，防止界面空白
		setDefaultBanners();
	}
}

// 设置默认轮播图数据
function setDefaultBanners() {
	banners.value = [
		{
			id: 1,
			title: '缘来如此·夏日派对',
			image: '/static/index/banner1.png',
			url: '/pages/activity/summer-party'
		},
		{
			id: 2,
			title: '新人专享礼包',
			image: '/static/index/banner2.png',
			url: '/pages/activity/new-user'
		},
		{
			id: 3,
			title: '周末线下联谊',
			image: '/static/index/banner3.png',
			url: '/pages/activity/offline-event'
		}
	];
}

// 页面加载完成后调用接口获取数据（已合并到主onMounted中）

// 模拟数据
const games = ref([
	{
		id: 1,
		name: '心动速配',
		desc: '趣味互动，快速匹配',
		image: '/static/index/game1.png'
	},
	{
		id: 2,
		name: '性格测试',
		desc: '了解自己，找到匹配',
		image: '/static/index/game2.png'
	},
	{
		id: 3,
		name: '恋爱话题',
		desc: '破冰神器，告别尬聊',
		image: '/static/index/game3.png'
	},
	{
		id: 4,
		name: '缘分测算',
		desc: '星座配对，姻缘分析',
		image: '/static/index/game4.png'
	}
]);

// 相亲广场用户数据
const matches = ref([]);

// 获取相亲广场推荐用户
async function fetchRecommendUsers() {
	try {
		// 导入相亲广场API
		const squareApi = await import('@/api/square.js');
		
		// 调用API获取推荐用户（首页只显示4个）
		const result = await squareApi.default.getRecommendUsers(4);
		
		if (result.code === 200) {
			// 将接口返回的数据转换为前端需要的格式，安全处理可能为空的字段
			matches.value = result.data.records.map(user => ({
				id: user.userId,
				name: user.nickname || '匿名用户',
				sex: user.gender,
				selfIntroduction: (user.userProfile && user.userProfile.selfIntroduction) || '这个人很神秘，没有留下介绍',
				avatar: user.avatarUrl || '/static/default-avatar.png'
			}));
			console.log('相亲广场推荐用户获取成功:', matches.value);
		} else {
			console.error('相亲广场推荐用户获取失败:', result);
			// 设置默认数据，防止界面空白
			setDefaultMatches();
		}
	} catch (error) {
		console.error('相亲广场推荐用户获取异常:', error);
		// 设置默认数据，防止界面空白
		// setDefaultMatches();
	}
}

// 点击更多，跳转到相亲广场页面
function goMoreMatches() {
	uni.navigateTo({
		url: '/pages/square/square'
	});
}

// 设置默认用户数据
function setDefaultMatches() {
	matches.value = [
		{
			id: 1,
			name: '小雨',
			age: 26,
			intro: '喜欢旅行，热爱生活',
			avatar: '/static/index/avatar1.png'
		},
		{
			id: 2,
			name: '阳阳',
			age: 28,
			intro: '工程师，爱好摄影',
			avatar: '/static/index/avatar2.png'
		},
		{
			id: 3,
			name: '小米',
			age: 25,
			intro: '教师，温柔贤淑',
			avatar: '/static/index/avatar3.png'
		},
		{
			id: 4,
			name: '大壮',
			age: 30,
			intro: '医生，稳重可靠',
			avatar: '/static/index/avatar4.png'
		}
	];
}

const entertainment = ref([
	{
		id: 1,
		name: '语音聊天',
		image: '/static/index/entertainment1.png'
	},
	{
		id: 2,
		name: '视频相亲',
		image: '/static/index/entertainment2.png'
	},
	{
		id: 3,
		name: '趣味问答',
		image: '/static/index/entertainment3.png'
	},
	{
		id: 4,
		name: '脱单攻略',
		image: '/static/index/entertainment4.png'
	}
]);

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
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

// 轮播图点击
function handleBannerClick(banner) {
	console.log('点击轮播图:', banner);
	
	if (!banner.url || banner.url === '#') {
		console.log('轮播图没有设置跳转链接');
		return;
	}
	
	// 根据targetType判断跳转类型
	if (banner.targetType === 1) {
		// 内部页面跳转
		uni.navigateTo({
			url: banner.url
		});
	} else if (banner.targetType === 2 && banner.targetId) {
		// 详情页跳转
		uni.navigateTo({
			url: `/pages/detail/detail?id=${banner.targetId}`
		});
	} else {
		// 默认跳转
		uni.navigateTo({
			url: banner.url
		});
	}
}

// 页面跳转函数
function goSearch() {
	uni.navigateTo({
		url: '/pages/search/search'
	});
}

function goMoreGames() {
	uni.navigateTo({
		url: '/pages/games/games-list'
	});
}

function startGame(game) {
	// 根据游戏ID跳转到不同页面
	if (game.id === 1) {
		// 心动速配
		uni.navigateTo({
			url: '/pages/game/heart-match'
		});
	} else {
		// 其他游戏暂时跳转到通用游戏详情页
		uni.showToast({
			title: '该游戏正在开发中',
			icon: 'none'
		});
	}
}

function goMoreMatchList() { // 将 goMoreMatches 重命名为 goMoreMatchList
	uni.navigateTo({
		url: '/pages/match/match-list'
	});
}

function viewMatch(match) {
	uni.navigateTo({
		url: `/pages/user/user-detail?id=${match.id}`
	});
}

function goMoreEntertainment() {
	uni.navigateTo({
		url: '/pages/entertainment/entertainment-list'
	});
}

function goEntertainment(item) {
	uni.navigateTo({
		url: `/pages/entertainment/entertainment-detail?id=${item.id}`
	});
}

function goVipCenter() {
	uni.navigateTo({
		url: '/pages/vip/vip-center'
	});
}

// 约会中心跳转方法
function goDateCenter() {
	// 检查登录状态
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看约会中心',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/date/date-center'
	});
}

// 跳转到约会提醒
function goToReminders() {
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看约会提醒',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/date/date-reminders'
	});
}



// 跳转到约会管理
function goDateManagement() {
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看约会管理',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/date/date-management'
	});
}



// 跳转到个人资料
function goToProfile() {
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看个人资料',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/user/profile'
	});
}



function goMatchmakerApply() {
	// 检查登录状态
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后再申请成为红娘',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/matchmaker/apply'
	});
}

// 跳转到订单列表
function goOrderList() {
	// 检查登录状态
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看订单',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: '/pages/matchmaker/order-list'
	});
}

// 跳转到指定状态的订单列表
function goOrderListWithStatus(status) {
	// 检查登录状态
	const token = uni.getStorageSync('token');
	if (!token) {
		uni.showModal({
			title: '提示',
			content: '请先登录后查看订单',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/login'
					});
				}
			}
		});
		return;
	}

	uni.navigateTo({
		url: `/pages/matchmaker/order-list?status=${status}`
	});
}

// 加载约会统计数据
async function loadUnreadDateCount() {
	try {
		const token = uni.getStorageSync('token');
		if (!token) {
			unreadDateCount.value = 0;
			pendingDateCount.value = 0;
			confirmedDateCount.value = 0;
			totalDateCount.value = 0;
			return;
		}

		// 调用约会统计API
		const response = await uni.request({
			url: `${config.getBaseUrl()}/user/date/statistics`,
			method: 'GET',
			header: {
				'token': token
			}
		});

		if (response.data.code === 200) {
			const stats = response.data.data;
			pendingDateCount.value = stats.pendingCount || 0;
			confirmedDateCount.value = stats.confirmedCount || 0;
			totalDateCount.value = stats.totalCount || 0;
			unreadDateCount.value = stats.pendingCount || 0; // 待确认的就是需要用户关注的
		}

	} catch (error) {
		console.error('加载约会统计数据失败:', error);
		unreadDateCount.value = 0;
		pendingDateCount.value = 0;
		confirmedDateCount.value = 0;
		totalDateCount.value = 0;
	}
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

// 获取VIP状态信息
async function getVipStatusData() {
	try {
		const token = uni.getStorageSync("token");
		if (!token) {
			console.log('未登录，无法获取VIP状态');
			return;
		}

		const response = await getUserVipStatus();
		if (response.code === 200) {
			vipStatus.value = {
				isVip: response.data.isVip === 1,
				userRole: response.data.userRole,
				vipExpireTime: response.data.vipExpireTime
			};
		}
	} catch (error) {
		console.error('获取VIP状态失败:', error);
	}
}

// 格式化VIP过期时间
function formatVipExpireTime() {
	if (!vipStatus.value.vipExpireTime) {
		return '';
	}
	const date = new Date(vipStatus.value.vipExpireTime);
	return date.toLocaleDateString();
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



// 页面加载时获取钱包数据和VIP状态（已合并到主onMounted中）

// 页面显示时刷新钱包数据和VIP状态
onShow(() => {
	getWalletData();
	getVipStatusData();
	loadUnreadDateCount();
	loadUserInfo();
});

// 监听省份更新事件（已合并到主onMounted中）

// 获取用户信息并设置当前省份
async function loadUserInfo() {
	try {
		const token = uni.getStorageSync('token');
		if (!token) {
			console.log('未登录，使用默认省份');
			return;
		}

		const result = await getByUserInfo(token);
		console.log('获取用户信息API响应:', result);

		if (result.code === 200 && result.data) {
			const userData = result.data;
			console.log('用户信息详情:', userData);
			console.log('lastLoginIp字段值:', userData.lastLoginIp);

			// 更新用户信息状态
			userInfo.value = {
				userId: userData.userId,
				nickname: userData.nickname || '',
				avatar: userData.avatar || ''
			};

			// 如果用户有省份信息，设置为当前选中的省份
			if (userData.lastLoginIp) {
				const provinceShortName = getProvinceShortName(userData.lastLoginIp);
				console.log('省份转换: {} -> {}', userData.lastLoginIp, provinceShortName);
				selectedRegion.value = provinceShortName;
				console.log('设置用户当前省份:', provinceShortName);
			} else {
				console.log('用户暂无省份信息，使用默认省份');
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
</script>

<style lang="scss" scoped>
.index-page {
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
		
		// 移除固定的bg-gradient，让主题背景生效
		.bg-gradient {
			display: none;
		}
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
				margin-right: 20rpx;
				display: flex;
				align-items: center;
				padding: 0 20rpx;
				
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
			
			// 轮播图
			.banner-swiper {
				width: 100%;
				height: 300rpx;
				margin-bottom: 40rpx;
				border-radius: 20rpx;
				overflow: hidden;
				position: relative;
				
				.banner-image {
					width: 100%;
					height: 100%;
					
					.banner-placeholder {
						width: 100%;
						height: 100%;
						background: rgba(255, 255, 255, 0.1);
					}
				}
				
				.banner-title {
					position: absolute;
					bottom: 0;
					left: 0;
					width: auto;
					padding: 10rpx 20rpx;
					background: rgba(0, 0, 0, 0.4);
					color: #fff;
					font-size: 28rpx;
					line-height: 40rpx;
					text-align: left;
					backdrop-filter: blur(5px);
					border-radius: 0 10rpx 0 0;
					font-weight: 500;
					letter-spacing: 1px;
				}
			}
			
			// 通用区块样式
			.section {
				margin-bottom: 40rpx;
				
				.section-header {
					display: flex;
					justify-content: space-between;
					align-items: center;
					margin-bottom: 20rpx;
					
					.section-title {
						font-size: 32rpx;
						font-weight: bold;
						color: #fff;
					}
					
					.more-link {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.8);
					}
				}
			}
			
			// 游戏区块
			.game-section {
				.game-list {
					white-space: nowrap;
					
					.game-item {
						display: inline-block;
						width: 200rpx;
						margin-right: 20rpx;
						
						.game-image {
							width: 200rpx;
							height: 200rpx;
							border-radius: 20rpx;
							background-color: rgba(255, 255, 255, 0.1);
							margin-bottom: 10rpx;
							
							.image-placeholder {
								width: 100%;
								height: 100%;
								background: rgba(255, 255, 255, 0.1);
								border-radius: 20rpx;
							}
						}
						
						.game-name {
							font-size: 28rpx;
							color: #fff;
							display: block;
						}
						
						.game-desc {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.7);
							display: block;
						}
					}
				}
			}
			
			// 相亲广场
			.match-section {
				.match-list {
					white-space: nowrap;
					
					.match-item {
						display: inline-block;
						width: 240rpx;
						margin-right: 20rpx;
						
						.match-image {
							width: 240rpx;
							height: 320rpx;
							border-radius: 20rpx;
							background-color: rgba(255, 255, 255, 0.1);
							margin-bottom: 10rpx;
							
							.avatar-placeholder {
								width: 100%;
								height: 100%;
								background: rgba(255, 255, 255, 0.1);
								border-radius: 20rpx;
							}
						}
						
						.match-info {
							display: flex;
							align-items: center;
							
							.match-name {
								font-size: 28rpx;
								color: #fff;
								margin-right: 10rpx;
							}
							
							.match-age {
								font-size: 24rpx;
								color: rgba(255, 255, 255, 0.7);
							}
						}
						
						.match-intro {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.7);
							white-space: normal;
							display: -webkit-box;
							-webkit-line-clamp: 1;
							-webkit-box-orient: vertical;
							overflow: hidden;
						}
					}
				}
			}
			
			// 娱乐专区
			.entertainment-section {
				.entertainment-grid {
					display: flex;
					flex-wrap: wrap;
					justify-content: space-between;
					
					.entertainment-item {
						width: 48%;
						margin-bottom: 20rpx;
						
						.entertainment-image {
							width: 100%;
							height: 180rpx;
							border-radius: 20rpx;
							background-color: rgba(255, 255, 255, 0.1);
							margin-bottom: 10rpx;
							
							.image-placeholder {
								width: 100%;
								height: 100%;
								background: rgba(255, 255, 255, 0.1);
								border-radius: 20rpx;
							}
						}
						
						.entertainment-name {
							font-size: 28rpx;
							color: #fff;
							text-align: center;
							display: block;
						}
					}
				}
			}
			
			// 约会管理专区
			.date-section {
				.date-main-card {
					background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
					border-radius: 16rpx;
					padding: 30rpx;
					margin-bottom: 20rpx;
					box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
				}

				.date-main-content {
					display: flex;
					align-items: center;
					position: relative;
				}

				.date-main-icon {
					font-size: 56rpx;
					margin-right: 20rpx;
				}

				.date-main-info {
					flex: 1;

					.date-main-title {
						font-size: 32rpx;
						font-weight: bold;
						color: #fff;
						display: block;
						margin-bottom: 8rpx;
						text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
					}

					.date-main-desc {
						font-size: 24rpx;
						color: rgba(255, 255, 255, 0.9);
						display: block;
					}
				}

				.date-main-badge {
					position: absolute;
					top: -10rpx;
					right: -10rpx;
					background: #ff4757;
					color: white;
					font-size: 22rpx;
					font-weight: bold;
					padding: 6rpx 12rpx;
					border-radius: 24rpx;
					min-width: 40rpx;
					text-align: center;
					box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.5);
					animation: pulse 2s infinite;
				}

				.date-quick-stats {
					display: flex;
					background: rgba(255, 255, 255, 0.9);
					border-radius: 12rpx;
					padding: 20rpx;
					backdrop-filter: blur(10rpx);
				}

				.quick-stat-item {
					flex: 1;
					display: flex;
					flex-direction: column;
					align-items: center;
					cursor: pointer;
					transition: all 0.3s ease;

					&:active {
						transform: scale(0.95);
					}

					.stat-number {
						font-size: 32rpx;
						font-weight: bold;
						color: #667eea;
						margin-bottom: 4rpx;
					}

					.stat-label {
						font-size: 22rpx;
						color: #718096;
					}
				}

				.date-quick-actions {
					display: flex;
					background: rgba(255, 255, 255, 0.9);
					border-radius: 12rpx;
					padding: 20rpx;
					margin-top: 16rpx;
					backdrop-filter: blur(10rpx);
					gap: 20rpx;
				}

				.quick-action-item {
					flex: 1;
					display: flex;
					flex-direction: column;
					align-items: center;
					padding: 16rpx 8rpx;
					border-radius: 12rpx;
					transition: all 0.3s ease;
					cursor: pointer;

					&:hover {
						background: rgba(102, 126, 234, 0.1);
					}

					&:active {
						transform: scale(0.95);
						background: rgba(102, 126, 234, 0.2);
					}

					.action-icon {
						font-size: 40rpx;
						margin-bottom: 8rpx;
						filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
					}

					.action-text {
						font-size: 20rpx;
						color: #4a5568;
						font-weight: 500;
						text-align: center;
					}
				}


			}

			@keyframes pulse {
				0% {
					transform: scale(1);
				}
				50% {
					transform: scale(1.1);
				}
				100% {
					transform: scale(1);
				}
			}

			// VIP专区
			.vip-section {
				.vip-card {
					background: linear-gradient(45deg, #FFD700, #FFA500);
					border-radius: 16rpx;
					padding: 24rpx;
					display: flex;
					justify-content: space-between;
					align-items: center;

					.vip-info {
						.vip-title {
							font-size: 30rpx;
							font-weight: bold;
							color: #fff;
							display: block;
							margin-bottom: 8rpx;
							text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
						}

						.vip-desc {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.9);
							display: block;
						}
					}

					.vip-button {
						background-color: #fff;
						color: #FFA500;
						font-size: 24rpx;
						font-weight: bold;
						padding: 8rpx 24rpx;
						border-radius: 24rpx;
						box-shadow: 0 3rpx 6rpx rgba(0, 0, 0, 0.1);
					}
				}
			}

			// 红娘申请专区
			.matchmaker-section {
				.matchmaker-card {
					background: linear-gradient(45deg, #667eea, #764ba2);
					border-radius: 16rpx;
					padding: 24rpx;
					display: flex;
					justify-content: space-between;
					align-items: center;

					.matchmaker-info {
						.matchmaker-title {
							font-size: 30rpx;
							font-weight: bold;
							color: #fff;
							display: block;
							margin-bottom: 8rpx;
							text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
						}

						.matchmaker-desc {
							font-size: 22rpx;
							color: rgba(255, 255, 255, 0.9);
							display: block;
						}
					}

					.matchmaker-button {
						background-color: #fff;
						color: #667eea;
						font-size: 24rpx;
						font-weight: bold;
						padding: 8rpx 24rpx;
						border-radius: 24rpx;
						box-shadow: 0 3rpx 6rpx rgba(0, 0, 0, 0.1);
					}
				}
			}

			// 订单管理专区
			.order-section {
				.order-quick-actions {
					display: flex;
					justify-content: space-between;
					gap: 20rpx;

					.order-action-item {
						flex: 1;
						background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
						border-radius: 16rpx;
						padding: 24rpx 16rpx;
						display: flex;
						flex-direction: column;
						align-items: center;
						justify-content: center;
						min-height: 120rpx;
						box-shadow: 0 4rpx 12rpx rgba(240, 147, 251, 0.3);
						transition: all 0.3s ease;

						&:active {
							transform: scale(0.95);
							box-shadow: 0 2rpx 8rpx rgba(240, 147, 251, 0.4);
						}

						.order-action-icon {
							font-size: 36rpx;
							margin-bottom: 8rpx;
							filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
						}

						.order-action-text {
							font-size: 24rpx;
							color: #fff;
							font-weight: bold;
							text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2);
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

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>
