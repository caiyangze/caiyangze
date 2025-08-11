<template>
	<view class="login-page">
		<!-- 背景层 -->
		<view class="bg-layer" :style="{ background: currentBackground }">
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
			
			<!-- 登录表单 -->
			<view class="login-card">
				<view class="tab-header">
					<view class="tab-item" :class="{ active: loginType === 'password' }" @tap="switchLoginType('password')">
						密码登录
					</view>
					<view class="tab-item" :class="{ active: loginType === 'code' }" @tap="switchLoginType('code')">
						验证码登录
					</view>
				</view>
				
				<!-- 密码登录表单 -->
				<view class="form-container" v-if="loginType === 'password'">
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon phone-icon">+86</text>
						</view>
						<input class="input-field" type="number" v-model="passwordForm.phone" placeholder="请输入手机号" maxlength="11" />
					</view>
					
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon lock-icon"></text>
						</view>
						<input class="input-field" type="password" v-model="passwordForm.password" placeholder="请输入密码" password />
						<view class="input-suffix" @tap="forgetPassword">
							<text class="forget-pwd">忘记密码</text>
						</view>
					</view>
					
					<button class="submit-btn" :class="{ loading: isLoading }" @tap="handlePasswordLogin" :disabled="isLoading">
						<text v-if="!isLoading">登录</text>
						<view v-else class="btn-loading"></view>
					</button>
				</view>
				
				<!-- 验证码登录表单 -->
				<view class="form-container" v-else>
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon phone-icon">+86</text>
						</view>
						<input class="input-field" type="number" v-model="codeForm.phone" placeholder="请输入手机号" maxlength="11" />
					</view>
					
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon code-icon"></text>
						</view>
						<input class="input-field" type="number" v-model="codeForm.code" placeholder="请输入验证码" maxlength="6" />
						<view class="input-suffix">
							<text class="code-btn" :class="{ disabled: isCounting }" @tap="getVerifyCode">
								{{ isCounting ? `${countdown}s后重新获取` : '获取验证码' }}
							</text>
						</view>
					</view>
					
					<button class="submit-btn" :class="{ loading: isLoading }" @tap="handleCodeLogin" :disabled="isLoading">
						<text v-if="!isLoading">登录</text>
						<view v-else class="btn-loading"></view>
					</button>
				</view>
				
				<!-- 注册入口 -->
				<view class="register-link">
					还没有账号？<text class="link-text" @tap="goRegister">立即注册</text>
				</view>
			</view>
			
			<!-- 第三方登录 -->
			<view class="third-party-login">
				<view class="divider">
					<view class="line"></view>
					<text>社交账号登录</text>
					<view class="line"></view>
				</view>
				
				<view class="social-icons">
					<view class="social-btn wechat" @tap="thirdLogin('wechat')">
						<image src="/static/login/weix.svg" mode="aspectFit" @error="handleImageError('wechat')">
							<view v-if="imageError.wechat" class="icon-placeholder wechat-placeholder"></view>
						</image>
					</view>
					<view class="social-btn qq" @tap="thirdLogin('qq')">
						<image src="/static/login/Q.svg" mode="aspectFit" @error="handleImageError('qq')">
							<view v-if="imageError.qq" class="icon-placeholder qq-placeholder"></view>
						</image>
					</view>
					<view class="social-btn weibo" @tap="thirdLogin('weibo')">
						<image src="/static/login/weib.svg" mode="aspectFit" @error="handleImageError('weibo')">
							<view v-if="imageError.weibo" class="icon-placeholder weibo-placeholder"></view>
						</image>
					</view>
				</view>
			</view>
			
			<!-- 底部协议 -->
			<view class="agreement">
				<text class="agreement-text">登录即表示您已同意</text>
				<text class="agreement-link" @tap="goAgreement('user')">《用户协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @tap="goAgreement('privacy')">《隐私政策》</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref, onUnmounted, onMounted } from 'vue';
import { login, loginByCode, sendLoginCode } from '@/api/user/auth';
import { getUserGeolocation, updateUserLocation } from '@/api/location';
import { useTheme } from '@/mixins/theme-mixin.js';
import { currentBackground } from '@/utils/simple-theme.js';

// 使用主题混入
const { currentTheme, getThemeStyles } = useTheme();

// 登录类型（密码登录/验证码登录）
const loginType = ref('password');

// 密码登录表单
const passwordForm = reactive({
	phone: '',
	password: ''
});

// 验证码登录表单
const codeForm = reactive({
	phone: '',
	code: ''
});

// 图片加载错误状态
const imageError = reactive({
	bg: false,
	logo: false,
	wechat: false,
	qq: false,
	weibo: false
});

// 加载状态
const isLoading = ref(false);
const sendingCode = ref(false);

// 验证码倒计时
const isCounting = ref(false);
const countdown = ref(60);
let timer = null;

// 页面加载时自动填充上次使用的手机号
onMounted(() => {
	// 检查是否有存储的token
	const token = uni.getStorageSync('token');
	if (token) {
		// 如果有token，说明用户已登录，直接跳转到首页
		console.log('检测到已登录状态，自动跳转到首页');
		uni.reLaunch({
			url: '/pages/index/index',
			fail: (err) => {
				console.error('自动跳转失败:', err);
			}
		});
		return;
	}
	
	// 获取上次登录的手机号
	const lastLoginPhone = uni.getStorageSync('lastLoginPhone');
	if (lastLoginPhone) {
		passwordForm.phone = lastLoginPhone;
		codeForm.phone = lastLoginPhone;
		console.log('自动填充上次登录手机号:', lastLoginPhone);
	}
});

// 切换登录方式
function switchLoginType(type) {
	loginType.value = type;
}

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
}

// 密码登录
async function handlePasswordLogin() {
	if (!passwordForm.phone) {
		uni.showToast({
			title: '请输入手机号',
			icon: 'none'
		});
		return;
	}
	if (!passwordForm.password) {
		uni.showToast({
			title: '请输入密码',
			icon: 'none'
		});
		return;
	}
	
	// 验证手机号格式
	if (!/^1[3-9]\d{9}$/.test(passwordForm.phone)) {
		uni.showToast({
			title: '手机号格式不正确',
			icon: 'none'
		});
		return;
	}
	
	// 调用登录接口
	isLoading.value = true;
	
	try {
		// 调用登录API
		const result = await login(passwordForm.phone, passwordForm.password);
		
		// 保存token和用户信息
		if (result.data) {	
			// 保存用户信息到本地存储
			uni.setStorageSync('token', result.data);
		}
		
		// 先显示登录成功提示
		uni.showToast({
			title: '登录成功',
			icon: 'success',
			duration: 1500
		});
		
		// 延迟跳转，确保提示显示完毕
		setTimeout(() => {
			console.log('准备跳转到首页...');
			// 登录成功后直接跳转到首页
			uni.reLaunch({
				url: '/pages/index/index',
				success: () => {
					console.log('跳转首页成功');
					// 跳转成功后，延迟触发省份信息更新
					setTimeout(() => {
						triggerProvinceUpdate();
					}, 2000);
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
		console.error('登录失败:', error);
		// 错误信息已由HTTP拦截器处理
	} finally {
		isLoading.value = false;
	}
}

// 验证码登录
async function handleCodeLogin() {
	if (!codeForm.phone) {
		uni.showToast({
			title: '请输入手机号',
			icon: 'none'
		});
		return;
	}
	if (!codeForm.code) {
		uni.showToast({
			title: '请输入验证码',
			icon: 'none'
		});
		return;
	}
	
	// 验证手机号格式
	if (!/^1[3-9]\d{9}$/.test(codeForm.phone)) {
		uni.showToast({
			title: '手机号格式不正确',
			icon: 'none'
		});
		return;
	}
	
	// 调用登录接口
	isLoading.value = true;
	
	try {
		// 调用登录API
		const result = await loginByCode(codeForm.phone, codeForm.code);	
		// 保存用户信息到本地存储
		uni.setStorageSync('token', result.data);
		console.log('用户信息已保存到本地', result.data);
		
		// 先显示登录成功提示
		uni.showToast({
			title: '登录成功',
			icon: 'success',
			duration: 1500
		});
		
		// 延迟跳转，确保提示显示完毕
		setTimeout(() => {
			console.log('准备跳转到首页...');
			// 登录成功后直接跳转到首页
			uni.reLaunch({
				url: '/pages/index/index',
				success: () => {
					console.log('跳转首页成功');
					// 跳转成功后，延迟触发省份信息更新
					setTimeout(() => {
						triggerProvinceUpdate();
					}, 2000);
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
		console.error('登录失败:', error);
		// 错误信息已由HTTP拦截器处理
	} finally {
		isLoading.value = false;
	}
}

// 获取验证码
async function getVerifyCode() {
	if (isCounting.value || sendingCode.value) return;
	
	if (!codeForm.phone) {
		uni.showToast({
			title: '请输入手机号',
			icon: 'none'
		});
		return;
	}
	
	// 验证手机号格式
	if (!/^1[3-9]\d{9}$/.test(codeForm.phone)) {
		uni.showToast({
			title: '手机号格式不正确',
			icon: 'none'
		});
		return;
	}
	
	// 发送验证码
	try {
		sendingCode.value = true;
		const result = await sendLoginCode(codeForm.phone);
		
		// 开始倒计时
		isCounting.value = true;
		countdown.value = 60;
		
		// 倒计时
		timer = setInterval(() => {
			countdown.value--;
			if (countdown.value <= 0) {
				clearInterval(timer);
				isCounting.value = false;
			}
		}, 1000);
		
		// 提示发送成功
		uni.showToast({
			title: result.message || '验证码发送成功',
			icon: 'success'
		});
	} catch (error) {
		console.error('发送验证码失败:', error);
		// 错误信息已由HTTP拦截器处理
	} finally {
		sendingCode.value = false;
	}
}

// 忘记密码
function forgetPassword() {
	uni.showToast({
		title: '忘记密码功能开发中',
		icon: 'none'
	});
}

// 前往注册页面
function goRegister() {
	uni.reLaunch({
		url: '/pages/register/register',
		fail: (err) => {
			console.error('跳转到注册页面失败:', err);
			uni.showToast({
				title: '跳转失败，请稍后重试',
				icon: 'none'
			});
		}
	});
}

// 第三方登录
function thirdLogin(type) {
	uni.showToast({
		title: `${type}登录功能开发中`,
		icon: 'none'
	});
}

// 查看协议
function goAgreement(type) {
	uni.showToast({
		title: `${type === 'user' ? '用户协议' : '隐私政策'}查看功能开发中`,
		icon: 'none'
	});
}

// 触发省份信息更新（通过全局事件）
async function triggerProvinceUpdate() {
	try {
		// 通过全局事件通知首页更新省份信息
		uni.$emit('refreshUserProvince');
		console.log('已触发省份信息更新事件');

		// 获取用户地理位置并上传到服务器
		await updateUserLocationInfo();
	} catch (error) {
		console.error('触发省份更新失败:', error);
	}
}

// 获取并更新用户位置信息
async function updateUserLocationInfo() {
	try {
		console.log('开始获取用户位置信息...');

		// 检查是否有token
		const token = uni.getStorageSync('token');
		if (!token) {
			console.log('未登录，跳过位置更新');
			return;
		}

		// 获取用户地理位置
		const position = await getUserGeolocation({
			enableHighAccuracy: true,
			timeout: 15000,
			maximumAge: 300000
		});

		console.log('获取到用户位置:', position);

		// 构建位置数据
		const locationData = {
			longitude: position.longitude,
			latitude: position.latitude,
			locationName: '', // 可以后续通过逆地理编码获取
			address: ''
		};

		// 上传位置信息到服务器
		const result = await updateUserLocation(locationData);

		if (result.code === 200) {
			console.log('用户位置信息更新成功');
		} else {
			console.warn('位置信息更新失败:', result.message);
		}

	} catch (error) {
		console.warn('获取或更新位置信息失败:', error.message);
		// 位置获取失败不影响正常使用，只记录日志
	}
}

// 组件卸载时清除定时器
onUnmounted(() => {
	if (timer) {
		clearInterval(timer);
	}
});
</script>

<style lang="scss">
page {
	height: 100%;
	width: 100%;
	overflow: hidden;
}

.login-page {
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
		
		// 登录卡片
		.login-card {
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
			
			// 选项卡
			.tab-header {
				display: flex;
				margin-bottom: 40rpx;
				border-bottom: 2rpx solid #f0f0f0;
				
				.tab-item {
					flex: 1;
					text-align: center;
					font-size: 32rpx;
					color: #666;
					padding-bottom: 20rpx;
					position: relative;
					transition: all 0.3s ease;
					
					&.active {
						color: #C471ED;
						font-weight: bold;
						
						&::after {
							content: '';
							position: absolute;
							bottom: -2rpx;
							left: 50%;
							transform: translateX(-50%);
							width: 40%;
							height: 4rpx;
							background: linear-gradient(to right, #12C2E9, #C471ED, #F64F59);
							border-radius: 4rpx;
						}
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
					
					.input-suffix {
						padding-left: 20rpx;
						
						.forget-pwd {
							color: #999;
							font-size: 26rpx;
							transition: all 0.3s ease;
							
							&:active {
								color: #C471ED;
							}
						}
						
						.code-btn {
							color: #C471ED;
							font-size: 26rpx;
							padding: 10rpx 0;
							transition: all 0.3s ease;
							
							&.disabled {
								color: #999;
							}
							
							&:active:not(.disabled) {
								transform: scale(0.98);
								opacity: 0.8;
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
			
			// 注册链接
			.register-link {
				text-align: center;
				margin-top: 30rpx;
				font-size: 28rpx;
				color: #666;
				
				.link-text {
					color: #C471ED;
					font-weight: bold;
					transition: all 0.3s ease;
					
					&:active {
						opacity: 0.8;
					}
				}
			}
		}
		
		// 第三方登录
		.third-party-login {
			margin-top: 20rpx;
			
			.divider {
				display: flex;
				align-items: center;
				justify-content: center;
				margin-bottom: 30rpx;
				
				.line {
					width: 80rpx;
					height: 1rpx;
					background-color: rgba(255, 255, 255, 0.5);
				}
				
				text {
					padding: 0 20rpx;
					font-size: 24rpx;
					color: rgba(255, 255, 255, 0.8);
				}
			}
			
			.social-icons {
				display: flex;
				justify-content: center;
				
				.social-btn {
					width: 80rpx;
					height: 80rpx;
					border-radius: 50%;
					background-color: rgba(255, 255, 255, 0.95);
					margin: 0 20rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.15),
								0 0 20rpx rgba(255, 255, 255, 0.1);
					transition: all 0.3s ease;
					
					&:active {
						transform: scale(0.95);
						box-shadow: 0 2rpx 5rpx rgba(0, 0, 0, 0.15);
					}
					
					image {
						width: 50rpx;
						height: 50rpx;
					}
					
					.icon-placeholder {
						width: 50rpx;
						height: 50rpx;
						border-radius: 50%;
					}
					
					.wechat-placeholder {
						background-color: #07c160;
					}
					
					.qq-placeholder {
						background-color: #12b7f5;
					}
					
					.weibo-placeholder {
						background-color: #e6162d;
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

// 占位符样式
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
</style> 