<template>
	<view class="register-page">
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
			
			<!-- 注册表单 -->
			<view class="register-card">
				<view class="form-title">手机号注册</view>
				
				<view class="form-container">
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon phone-icon">+86</text>
						</view>
						<input class="input-field" type="number" v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11" />
					</view>
					
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon lock-icon"></text>
						</view>
						<input class="input-field" type="password" v-model="registerForm.password" placeholder="请设置密码" password />
					</view>
					
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon lock-icon"></text>
						</view>
						<input class="input-field" type="password" v-model="registerForm.confirmPassword" placeholder="请确认密码" password />
					</view>
					
					<view class="input-group">
						<view class="input-prefix">
							<text class="prefix-icon code-icon"></text>
						</view>
						<input class="input-field" type="number" v-model="registerForm.code" placeholder="请输入验证码" maxlength="6" />
						<view class="input-suffix">
							<text class="code-btn" :class="{ disabled: isCounting }" @tap="getVerifyCode">
								{{ isCounting ? `${countdown}s后重新获取` : '获取验证码' }}
							</text>
						</view>
					</view>
					
					<button class="submit-btn" :class="{ loading: isLoading }" @tap="handleVerifyCode" :disabled="isLoading">
						<text v-if="!isLoading">下一步</text>
						<view v-else class="btn-loading"></view>
					</button>
				</view>
				
				<!-- 登录入口 -->
				<view class="login-link">
					已有账号？<text class="link-text" @tap="goLogin">立即登录</text>
				</view>
			</view>
			
			<!-- 底部协议 -->
			<view class="agreement">
				<text class="agreement-text">注册即表示您已同意</text>
				<text class="agreement-link" @tap="goAgreement('user')">《用户协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @tap="goAgreement('privacy')">《隐私政策》</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue';
import { sendRegisterCode, verifyCode } from '@/api/user/auth';
import { useTheme } from '@/mixins/theme-mixin.js';
import { currentBackground } from '@/utils/simple-theme.js';

// 使用主题混入
const { currentTheme, getThemeStyles } = useTheme();

// 注册表单
const registerForm = reactive({
	phone: '',
	password: '',
	confirmPassword: '',
	code: ''
});

// 图片加载错误状态
const imageError = reactive({
	bg: false,
	logo: false
});

// 加载状态
const isLoading = ref(false);
const sendingCode = ref(false);

// 验证码倒计时
const isCounting = ref(false);
const countdown = ref(60);
let timer = null;

// 处理图片加载错误
function handleImageError(type) {
	imageError[type] = true;
}

// 获取验证码
async function getVerifyCode() {
	if (isCounting.value || sendingCode.value) return;
	
	if (!registerForm.phone) {
		uni.showToast({
			title: '请输入手机号',
			icon: 'none'
		});
		return;
	}
	
	// 验证手机号格式
	if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
		uni.showToast({
			title: '手机号格式不正确',
			icon: 'none'
		});
		return;
	}
	
	// 发送验证码
	try {
		sendingCode.value = true;
		const result = await sendRegisterCode(registerForm.phone);
		
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

// 验证码验证
async function handleVerifyCode() {
	if (!registerForm.phone) {
		uni.showToast({
			title: '请输入手机号',
			icon: 'none'
		});
		return;
	}
	if (!registerForm.password) {
		uni.showToast({
			title: '请设置密码',
			icon: 'none'
		});
		return;
	}
	if (registerForm.password.length < 6) {
		uni.showToast({
			title: '密码长度不能少于6位',
			icon: 'none'
		});
		return;
	}
	// 验证密码必须包含英文和数字
	if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/.test(registerForm.password)) {
		uni.showToast({
			title: '密码必须包含英文和数字',
			icon: 'none'
		});
		return;
	}
	if (registerForm.password !== registerForm.confirmPassword) {
		uni.showToast({
			title: '两次输入的密码不一致',
			icon: 'none'
		});
		return;
	}
	if (!registerForm.code) {
		uni.showToast({
			title: '请输入验证码',
			icon: 'none'
		});
		return;
	}
	
	// 验证手机号格式
	if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
		uni.showToast({
			title: '手机号格式不正确',
			icon: 'none'
		});
		return;
	}
	
	// 调用验证接口
	isLoading.value = true;
	
	try {
		// 验证验证码
		const result = await verifyCode(registerForm.phone, registerForm.code, registerForm.password);
		
		console.log('验证码验证完整响应:', result);
		
		// 从响应中获取用户ID，考虑多种可能的数据结构
		let userId = '';
		if (result && result.data) {
			// 尝试多种可能的路径获取用户ID
			userId = result.data.id || result.data.userId || result.data.user_id || '';
			
			// 如果data本身就是ID
			if (!userId && typeof result.data === 'string' && result.data.trim() !== '') {
				userId = result.data;
			}
			
			// 如果data是一个对象数组，可能需要取第一个元素
			if (!userId && Array.isArray(result.data) && result.data.length > 0) {
				const firstItem = result.data[0];
				if (firstItem && typeof firstItem === 'object') {
					userId = firstItem.id || firstItem.userId || firstItem.user_id || '';
				}
			}
		}
		
		console.log('提取的用户ID:', userId);
		
		// 验证成功后跳转到用户信息注册页面
		uni.navigateTo({
			url: `/pages/register/user-info?phone=${registerForm.phone}&password=${encodeURIComponent(registerForm.password)}&userId=${userId}`,
			success: () => {
				console.log('成功跳转到用户信息页面');
			},
			fail: (err) => {
				console.error('跳转失败:', err);
				uni.showToast({
					title: '跳转失败，请稍后重试',
					icon: 'none'
				});
			}
		});
	} catch (error) {
		console.error('验证码验证失败:', error);
		// 错误信息已由HTTP拦截器处理
	} finally {
		isLoading.value = false;
	}
}

// 前往登录页面
function goLogin() {
	// 直接跳转到登录页面
	uni.reLaunch({
		url: '/pages/login/login',
		fail: (err) => {
			console.error('跳转到登录页面失败:', err);
			uni.showToast({
				title: '跳转失败，请稍后重试',
				icon: 'none'
			});
		}
	});
}

// 查看协议
function goAgreement(type) {
	uni.showToast({
		title: `${type === 'user' ? '用户协议' : '隐私政策'}查看功能开发中`,
		icon: 'none'
	});
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

.register-page {
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
		
		// 注册卡片
		.register-card {
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
			
			// 登录链接
			.login-link {
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

.prefix-icon {
	color: #999;
	font-size: 28rpx;
	
	&.phone-icon {
		// 已有样式
	}
	
	&.lock-icon {
		&::before {
			content: '🔒';
			font-size: 24rpx;
		}
	}
	
	&.code-icon {
		&::before {
			content: '🔑';
			font-size: 24rpx;
		}
	}
}
</style> 