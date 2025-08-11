<template>
	<view class="user-info-page">
		<!-- 背景层 -->
		<view class="bg-layer">
			<view class="bg-gradient"></view>
			<view class="overlay-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部Logo -->
			<view class="header">
				<image class="logo" src="/static/login/logo.png" mode="aspectFit" @error="handleImageError('logo')">
					<view v-if="imageError.logo" class="logo-placeholder">
						<text class="logo-text">缘来如此</text>
					</view>
				</image>
				<view class="slogan">遇见美好，从心开始</view>
			</view>
			
			<!-- 用户信息表单 -->
			<view class="user-info-card">
				<view class="form-title">完善个人信息</view>
				
				<!-- 头像上传 -->
				<view class="avatar-upload">
					<view class="avatar-wrapper" @tap="chooseAvatar">
						<image v-if="userForm.avatar" class="avatar-preview" :src="userForm.avatar" mode="aspectFill"></image>
						<view v-else class="avatar-placeholder">
							<text class="upload-icon">+</text>
							<text class="upload-text">上传头像</text>
						</view>
					</view>
				</view>
				
				<view class="form-container">
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon user-icon"></text>
						</view>
						<input class="input-field" type="text" v-model="userForm.nickname" placeholder="请输入昵称" maxlength="20" />
					</view>
					
					<view class="gender-group">
						<text class="gender-title">性别</text>
						<view class="gender-options">
							<view class="gender-option" :class="{ active: userForm.gender === 1 }" @tap="selectGender(1)">
								<text class="gender-icon">♂</text>
								<text class="gender-text">男</text>
							</view>
							<view class="gender-option" :class="{ active: userForm.gender === 2 }" @tap="selectGender(2)">
								<text class="gender-icon">♀</text>
								<text class="gender-text">女</text>
							</view>
						</view>
					</view>
					
					<button class="submit-btn" :class="{ loading: isLoading }" @tap="handleRegister" :disabled="isLoading">
						<text v-if="!isLoading">完成注册</text>
						<view v-else class="btn-loading"></view>
					</button>
				</view>
			</view>
			
			<!-- 底部协议 -->
			<view class="agreement">
				<!-- 跳过按钮 -->
				<view class="skip-btn" @tap="skipToLogin">跳过，直接登录</view>
				
				<text class="agreement-text">注册即表示您已同意</text>
				<text class="agreement-link" @tap="goAgreement('user')">《用户协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @tap="goAgreement('privacy')">《隐私政策》</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import http from '@/api/http';

// 用户信息表单
const userForm = reactive({
	phone: '',
	password: '',
	nickname: '',
	gender: 0, // 0: 未选择, 1: 男, 2: 女
	userId: '', // 用户ID
	avatar: '', // 用于显示的头像
	avatarUrl: '' // 用于提交给后端的URL
});

// 图片加载错误状态
const imageError = reactive({
	logo: false
});

// 加载状态
const isLoading = ref(false);

// 页面加载时获取手机号参数
onMounted(() => {
	const pages = getCurrentPages();
	const currentPage = pages[pages.length - 1];
	const options = currentPage.options || {};
	
	console.log('页面参数:', options);
	
	if (options.phone) {
		userForm.phone = options.phone;
		console.log('设置手机号:', userForm.phone);
	} else {
		uni.showToast({
			title: '未获取到手机号，请重新注册',
			icon: 'none',
			complete: () => {
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/register/register'
					});
				}, 1500);
			}
		});
	}
	
	if (options.password) {
		userForm.password = decodeURIComponent(options.password);
		console.log('设置密码:', '已获取(不显示)');
	}
	
	if (options.userId) {
		userForm.userId = options.userId;
		console.log('设置用户ID:', userForm.userId);
	} else {
		console.warn('未获取到用户ID');
	}
});

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
}

// 选择性别
function selectGender(gender) {
	userForm.gender = gender;
}

// 选择头像
function chooseAvatar() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0];
			console.log('选择的本地图片路径(blob):', tempFilePath);
			
			// 显示本地预览，但不设置为最终提交值
			userForm.avatar = tempFilePath;
			
			// 上传图片
			uploadAvatar(tempFilePath);
		},
		fail: (err) => {
			console.error('选择图片失败:', err);
			uni.showToast({
				title: '选择图片失败',
				icon: 'none'
			});
		}
	});
}

// 上传头像到服务器
function uploadAvatar(filePath) {
	uni.showLoading({
		title: '上传中...'
	});
	
	uni.uploadFile({
		url: 'http://localhost:9001/upload/avatar',
		filePath: filePath,
		name: 'file',
		formData: {},
		success: (uploadRes) => {
			uni.hideLoading();
			
			// 服务器直接返回URL字符串
			const result = uploadRes.data;
			console.log('服务器返回的URL:', result);
			
			if (!result || result.indexOf('http') === -1) {
				console.error('服务器返回的URL格式不正确:', result);
				uni.showToast({
					title: '头像上传失败，请重试',
					icon: 'none'
				});
				return;
			}
			
			// 确保avatarUrl使用服务器返回的URL
			userForm.avatarUrl = result;
			// 更新显示的头像也使用服务器URL
			userForm.avatar = result;
			
			console.log('设置后的头像URL:', userForm.avatarUrl);
			console.log('设置后的显示头像:', userForm.avatar);
			
			uni.showToast({
				title: '上传成功',
				icon: 'success'
			});
		},
		fail: (err) => {
			uni.hideLoading();
			console.error('上传失败:', err);
			uni.showToast({
				title: '上传失败',
				icon: 'none'
			});
		}
	});
}

// 完成注册
async function handleRegister() {
	// 表单验证
	if (!userForm.nickname) {
		uni.showToast({
			title: '请输入昵称',
			icon: 'none'
		});
		return;
	}
	
	if (userForm.gender === 0) {
		uni.showToast({
			title: '请选择性别',
			icon: 'none'
		});
		return;
	}
	
	// 检查头像URL是否为blob URL
	console.log('提交前检查头像URL:', userForm.avatarUrl);
	if (!userForm.avatarUrl || userForm.avatarUrl.startsWith('blob:')) {
		console.error('检测到头像URL仍然是blob URL或为空:', userForm.avatarUrl);
		
		// 使用默认头像URL
		userForm.avatarUrl = "http://182.254.244.209:9000/lovelink/avatar/2025/07/14/a1f1457c03284f3b9980e616d7b1ad4f.jpg";
		console.log('已设置默认头像URL:', userForm.avatarUrl);
		
		uni.showToast({
			title: '使用默认头像继续',
			icon: 'none'
		});
	}
	
	// 提交注册
	isLoading.value = true;
	
	// 构建请求数据
	const submitData = {
		phone: userForm.phone,
		password: userForm.password,
		nickname: userForm.nickname,
		gender: userForm.gender,
		avatarUrl: userForm.avatarUrl
	};
	
	// 如果有用户ID则添加
	if (userForm.userId && userForm.userId.trim() !== '') {
		submitData.id = userForm.userId;
	}
	
	console.log('最终提交的注册数据:', submitData);
	
	try {
		const result = await http.post('/user/register', submitData);
		
		// 保存token
		if (result.data && result.data.token) {
			uni.setStorageSync('token', result.data.token);
		}
		
		// 注册成功提示
		uni.showToast({
			title: '注册成功',
			icon: 'success',
			duration: 1500
		});
		
		// 延迟跳转，确保提示显示完毕
		setTimeout(() => {
			console.log('准备跳转到首页...');
			// 跳转到首页
			uni.reLaunch({
				url: '/pages/index/index',
				success: () => {
					console.log('跳转首页成功');
				},
				fail: (err) => {
					console.error('跳转失败:', err);
					uni.showToast({
						title: '跳转失败，请稍后重试',
						icon: 'none'
					});
				}
			});
		}, 1500);
	} catch (error) {
		console.error('注册失败:', error);
		// 错误信息已由HTTP拦截器处理
	} finally {
		isLoading.value = false;
	}
}

// 查看协议
function goAgreement(type) {
	uni.showToast({
		title: `${type === 'user' ? '用户协议' : '隐私政策'}查看功能开发中`,
		icon: 'none'
	});
}

// 跳过信息完善，直接登录
function skipToLogin() {
	// 如果已经获取了手机号和密码，将其保存到本地缓存，方便下次自动填充
	if (userForm.phone && userForm.password) {
		uni.setStorageSync('lastLoginPhone', userForm.phone);
	}
	
	uni.showToast({
		title: '跳过信息完善',
		icon: 'none',
		duration: 1500,
		success: () => {
			// 跳转到登录页
			setTimeout(() => {
				uni.reLaunch({
					url: '/pages/login/login',
					fail: (err) => {
						console.error('跳转失败:', err);
						uni.showToast({
							title: '跳转失败，请稍后重试',
							icon: 'none'
						});
					}
				});
			}, 1500);
		}
	});
}
</script>

<style lang="scss">
page {
	height: 100%;
	width: 100%;
	overflow: hidden;
}

.user-info-page {
	position: relative;
	width: 100%;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	
	// 背景层样式
	.bg-layer {
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		z-index: 1;
		
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
		
		.overlay-gradient {
			position: absolute;
			top: 0;
			left: 0;
			width: 100%;
			height: 100%;
			background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.5));
			backdrop-filter: blur(2px);
			
			&::before {
				content: '';
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect fill="rgba(255,255,255,0.03)" width="50" height="50" x="0" y="0"></rect><rect fill="rgba(255,255,255,0.03)" width="50" height="50" x="50" y="50"></rect></svg>');
				background-size: 20px 20px;
				opacity: 0.5;
			}
		}
	}
	
	// 内容区样式
	.content-container {
		position: relative;
		z-index: 2;
		width: 100%;
		min-height: 100vh;
		padding: 40rpx;
		display: flex;
		flex-direction: column;
		box-sizing: border-box;
		
		// 顶部Logo
		.header {
			margin-top: 60rpx;
			margin-bottom: 60rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			
			.logo {
				width: 180rpx;
				height: 180rpx;
				border-radius: 50%;
				background-color: rgba(255, 255, 255, 0.9);
				padding: 10rpx;
				box-shadow: 0 10rpx 20rpx rgba(0, 0, 0, 0.15),
							0 0 30rpx rgba(255, 255, 255, 0.2);
				position: relative;
				
				&::after {
					content: '';
					position: absolute;
					top: -10rpx;
					left: -10rpx;
					right: -10rpx;
					bottom: -10rpx;
					border-radius: 50%;
					border: 2rpx solid rgba(255, 255, 255, 0.3);
					animation: pulse 2s infinite;
				}
				
				@keyframes pulse {
					0% {
						transform: scale(1);
						opacity: 0.8;
					}
					70% {
						transform: scale(1.1);
						opacity: 0;
					}
					100% {
						transform: scale(1);
						opacity: 0;
					}
				}
			}
			
			.logo-placeholder {
				width: 100%;
				height: 100%;
				border-radius: 50%;
				background: linear-gradient(135deg, #12C2E9, #C471ED, #F64F59);
				background-size: 300% 300%;
				animation: gradientAnimation 15s ease infinite;
				display: flex;
				justify-content: center;
				align-items: center;
				
				.logo-text {
					color: #fff;
					font-size: 32rpx;
					font-weight: bold;
					text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
				}
			}
			
			.slogan {
				margin-top: 20rpx;
				font-size: 32rpx;
				color: #fff;
				font-weight: 500;
				letter-spacing: 4rpx;
				text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
				position: relative;
				
				&::after {
					content: '';
					position: absolute;
					bottom: -10rpx;
					left: 50%;
					transform: translateX(-50%);
					width: 60rpx;
					height: 4rpx;
					background: linear-gradient(to right, transparent, #fff, transparent);
					border-radius: 2rpx;
				}
			}
		}
		
		// 用户信息卡片
		.user-info-card {
			width: 100%;
			background-color: rgba(255, 255, 255, 0.95);
			border-radius: 24rpx;
			box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.15),
						0 0 20rpx rgba(255, 255, 255, 0.1);
			padding: 40rpx 30rpx;
			margin-bottom: 40rpx;
			position: relative;
			overflow: hidden;
			
			&::before {
				content: '';
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 8rpx;
				background: linear-gradient(to right, #12C2E9, #C471ED, #F64F59);
			}
			
			// 表单标题
			.form-title {
				font-size: 36rpx;
				font-weight: bold;
				color: #333;
				margin-bottom: 40rpx;
				text-align: center;
				position: relative;
				
				&::after {
					content: '';
					position: absolute;
					bottom: -10rpx;
					left: 50%;
					transform: translateX(-50%);
					width: 40rpx;
					height: 4rpx;
					background: linear-gradient(to right, #12C2E9, #C471ED, #F64F59);
					border-radius: 4rpx;
				}
			}
			
			// 头像上传
			.avatar-upload {
				display: flex;
				justify-content: center;
				margin-bottom: 30rpx;
				
				.avatar-wrapper {
					width: 160rpx;
					height: 160rpx;
					border-radius: 50%;
					overflow: hidden;
					box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
					position: relative;
					
					.avatar-preview {
						width: 100%;
						height: 100%;
						object-fit: cover;
					}
					
					.avatar-placeholder {
						width: 100%;
						height: 100%;
						background: linear-gradient(135deg, #f5f7fa, #c3cfe2);
						display: flex;
						flex-direction: column;
						align-items: center;
						justify-content: center;
						
						.upload-icon {
							font-size: 48rpx;
							color: #999;
							font-weight: 300;
							margin-bottom: 4rpx;
						}
						
						.upload-text {
							font-size: 24rpx;
							color: #999;
						}
					}
					
					&::after {
						content: '';
						position: absolute;
						top: 0;
						left: 0;
						right: 0;
						bottom: 0;
						background: rgba(0, 0, 0, 0.05);
						opacity: 0;
						transition: opacity 0.3s;
					}
					
					&:active::after {
						opacity: 1;
					}
				}
			}
			
			// 表单容器
			.form-container {
				.input-group {
					display: flex;
					align-items: center;
					height: 100rpx;
					border-bottom: 2rpx solid #f0f0f0;
					margin-bottom: 30rpx;
					transition: all 0.3s ease;
					
					&:focus-within {
						border-bottom-color: #C471ED;
					}
					
					.input-prefix {
						width: 80rpx;
						display: flex;
						align-items: center;
						justify-content: center;
						
						.prefix-icon {
							color: #999;
							font-size: 28rpx;
						}
					}
					
					.input-field {
						flex: 1;
						height: 100%;
						font-size: 30rpx;
						color: #333;
					}
				}
				
				// 性别选择
				.gender-group {
					margin: 20rpx 0 40rpx;
					
					.gender-title {
						font-size: 30rpx;
						color: #333;
						margin-bottom: 20rpx;
						display: block;
					}
					
					.gender-options {
						display: flex;
						justify-content: space-around;
						
						.gender-option {
							width: 45%;
							height: 100rpx;
							border: 2rpx solid #f0f0f0;
							border-radius: 16rpx;
							display: flex;
							flex-direction: column;
							align-items: center;
							justify-content: center;
							transition: all 0.3s ease;
							
							&.active {
								border-color: #C471ED;
								background-color: rgba(196, 113, 237, 0.05);
								
								.gender-icon, .gender-text {
									color: #C471ED;
								}
							}
							
							.gender-icon {
								font-size: 36rpx;
								color: #999;
								margin-bottom: 6rpx;
							}
							
							.gender-text {
								font-size: 28rpx;
								color: #666;
							}
						}
					}
				}
				
				.submit-btn {
					width: 100%;
					height: 90rpx;
					background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
					background-size: 300% 300%;
					color: #fff;
					font-size: 32rpx;
					font-weight: bold;
					border-radius: 45rpx;
					margin-top: 40rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					box-shadow: 0 10rpx 20rpx rgba(196, 113, 237, 0.4);
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
					
					&:active {
						transform: scale(0.98);
						box-shadow: 0 5rpx 10rpx rgba(233, 64, 87, 0.3);
					}
					
					&.loading {
						opacity: 0.8;
					}
					
					.btn-loading {
						width: 36rpx;
						height: 36rpx;
						border: 4rpx solid rgba(255, 255, 255, 0.3);
						border-top: 4rpx solid #fff;
						border-radius: 50%;
						animation: spin 1s linear infinite;
					}
					
					@keyframes spin {
						0% { transform: rotate(0deg); }
						100% { transform: rotate(360deg); }
					}
				}
			}
		}
		
		// 底部协议
		.agreement {
			margin-top: auto;
			padding: 40rpx 0;
			text-align: center;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.8);
			display: flex;
			justify-content: center;
			flex-wrap: wrap;
			
			// 跳过按钮
			.skip-btn {
				width: 100%;
				text-align: center;
				color: #fff;
				font-size: 28rpx;
				padding: 20rpx 0;
				margin-bottom: 20rpx;
				position: relative;
				
				&::after {
					content: '';
					position: absolute;
					bottom: 16rpx;
					left: 50%;
					transform: translateX(-50%);
					width: 140rpx;
					height: 2rpx;
					background: linear-gradient(to right, transparent, #fff, transparent);
				}
				
				&:active {
					opacity: 0.8;
				}
			}
			
			.agreement-text {
				color: rgba(255, 255, 255, 0.8);
			}
			
			.agreement-link {
				color: #fff;
				text-decoration: underline;
				transition: all 0.3s ease;
				
				&:active {
					opacity: 0.8;
				}
			}
		}
	}
}

// 用户图标样式
.prefix-icon.user-icon::before {
	content: '👤';
	font-size: 24rpx;
}
</style> 