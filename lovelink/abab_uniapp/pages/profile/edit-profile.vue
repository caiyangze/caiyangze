<template>
	<view class="edit-profile-page">
		<!-- 背景层 -->
		<view class="bg-layer">
			<view class="bg-gradient"></view>
			<view class="overlay-gradient"></view>
		</view>
		
		<!-- 内容区 -->
		<view class="content-container">
			<!-- 顶部按钮 -->
			<view class="top-buttons">
				<view class="back-btn" @tap.stop="goBack">
					<text class="back-arrow">←</text>
				</view>
				<view class="save-btn-container" @tap.stop="handleSubmit" :class="{ disabled: isLoading }">
					<text class="save-btn" v-if="!isLoading">{{ isEdit ? '保存' : '提交' }}</text>
					<view v-else class="loading-icon"></view>
				</view>
			</view>

			<!-- 顶部标题 -->
			<view class="header">
				<view class="page-title">编辑资料</view>
				<view class="slogan">完善资料，提高匹配度</view>
			</view>
			
			<!-- 表单区域 -->
			<view class="profile-form-card">
				<scroll-view scroll-y class="form-scroll-view">
					<view class="form-container">
						<!-- 基本信息 -->
						<view class="form-section">
							<view class="section-title">基本信息</view>

							<view class="input-group">
								<text class="input-label">真实姓名</text>
								<input class="input-field" type="text" v-model="profileForm.realName" placeholder="请输入真实姓名" maxlength="20" />
							</view>

							<view class="input-group">
								<text class="input-label">年龄</text>
								<input class="input-field" type="number" v-model="profileForm.age" placeholder="请输入年龄" maxlength="3" />
							</view>

							<view class="input-group">
								<text class="input-label">身高(cm)</text>
								<input class="input-field" type="number" v-model="profileForm.height" placeholder="请输入身高" maxlength="3" />
							</view>

							<view class="input-group">
								<text class="input-label">体重(kg)</text>
								<input class="input-field" type="number" v-model="profileForm.weight" placeholder="请输入体重" maxlength="3" />
							</view>
						</view>
						
						<!-- 教育工作 -->
						<view class="form-section">
							<view class="section-title">教育工作</view>
							
							<view class="select-group">
								<text class="select-label">学历</text>
								<picker class="picker" @change="onEducationChange" :value="educationIndex" :range="educationOptions">
									<view class="picker-value">{{ educationOptions[educationIndex] || '请选择学历' }}</view>
								</picker>
							</view>
							
							<view class="input-group">
								<text class="input-label">学历证明</text>
								<view class="upload-field" @tap="chooseEducationCert">
									<text v-if="!profileForm.educationCert">上传证明</text>
									<text v-else class="uploaded">已上传</text>
									<text class="upload-icon">+</text>
								</view>
							</view>
							
							<view class="input-group">
								<text class="input-label">公司</text>
								<input class="input-field" type="text" v-model="profileForm.company" placeholder="请输入公司名称" maxlength="50" />
							</view>
							
							<view class="input-group">
								<text class="input-label">职位</text>
								<input class="input-field" type="text" v-model="profileForm.position" placeholder="请输入职位" maxlength="30" />
							</view>
							
							<view class="select-group">
								<text class="select-label">收入水平</text>
								<picker class="picker" @change="onIncomeChange" :value="incomeIndex" :range="incomeOptions">
									<view class="picker-value">{{ incomeOptions[incomeIndex] || '请选择收入水平' }}</view>
								</picker>
							</view>
						</view>
						
						<!-- 地区信息 -->
						<view class="form-section">
							<view class="section-title">地区信息</view>
							
							<view class="input-group">
								<text class="input-label">工作城市</text>
								<input class="input-field" type="text" v-model="profileForm.workCity" placeholder="请输入工作城市" maxlength="30" />
							</view>
							
							<view class="input-group">
								<text class="input-label">家乡</text>
								<input class="input-field" type="text" v-model="profileForm.hometown" placeholder="请输入家乡" maxlength="50" />
							</view>
						</view>
						
						<!-- 婚恋状况 -->
						<view class="form-section">
							<view class="section-title">婚恋状况</view>
							
							<view class="select-group">
								<text class="select-label">婚姻状况</text>
								<picker class="picker" @change="onMaritalStatusChange" :value="maritalStatusIndex" :range="maritalStatusOptions">
									<view class="picker-value">{{ maritalStatusOptions[maritalStatusIndex] || '请选择婚姻状况' }}</view>
								</picker>
							</view>
							
							<view class="select-group">
								<text class="select-label">有无子女</text>
								<picker class="picker" @change="onHasChildrenChange" :value="hasChildrenIndex" :range="hasChildrenOptions">
									<view class="picker-value">{{ hasChildrenOptions[hasChildrenIndex] || '请选择有无子女' }}</view>
								</picker>
							</view>
						</view>
						
						<!-- 资产情况 -->
						<view class="form-section">
							<view class="section-title">资产情况</view>
							
							<view class="select-group">
								<text class="select-label">住房情况</text>
								<picker class="picker" @change="onHouseStatusChange" :value="houseStatusIndex" :range="houseStatusOptions">
									<view class="picker-value">{{ houseStatusOptions[houseStatusIndex] || '请选择住房情况' }}</view>
								</picker>
							</view>
							
							<view class="select-group">
								<text class="select-label">车辆情况</text>
								<picker class="picker" @change="onCarStatusChange" :value="carStatusIndex" :range="carStatusOptions">
									<view class="picker-value">{{ carStatusOptions[carStatusIndex] || '请选择车辆情况' }}</view>
								</picker>
							</view>
						</view>
						
						<!-- 个人介绍 -->
						<view class="form-section">
							<view class="section-title">个人介绍</view>
							
							<view class="textarea-group">
								<textarea class="textarea-field" v-model="profileForm.selfIntroduction" placeholder="请简单介绍自己" maxlength="500" />
								<view class="textarea-counter">{{ profileForm.selfIntroduction?.length || 0 }}/500</view>
							</view>
						</view>
						
						<!-- 兴趣爱好 -->
						<view class="form-section">
							<view class="section-title">兴趣爱好</view>
							
							<view class="textarea-group">
								<textarea class="textarea-field" v-model="profileForm.hobby" placeholder="请填写您的兴趣爱好，用逗号分隔" maxlength="200" />
								<view class="textarea-counter">{{ profileForm.hobby?.length || 0 }}/200</view>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import http from '@/api/http';
import profileApi from '@/api/profile';

// 表单数据
const profileForm = reactive({
	realName: '',
	age: '',
	height: '',
	weight: '',
	education: 0,
	educationCert: '',
	company: '',
	position: '',
	incomeLevel: 0,
	workCity: '',
	hometown: '',
	maritalStatus: 0,
	hasChildren: 0,
	houseStatus: 0,
	carStatus: 0,
	selfIntroduction: '',
	hobby: ''
});

// 是否为编辑模式
const isEdit = ref(false);
// 加载状态
const isLoading = ref(false);

// 选项数据
const educationOptions = ['请选择', '高中及以下', '大专', '本科', '硕士', '博士及以上'];
const educationIndex = ref(0);

const incomeOptions = ['请选择', '5千以下', '5千-1万', '1万-2万', '2万-5万', '5万以上'];
const incomeIndex = ref(0);

const maritalStatusOptions = ['请选择', '未婚', '离异', '丧偶'];
const maritalStatusIndex = ref(0);

const hasChildrenOptions = ['请选择', '无子女', '有子女不同住', '有子女同住'];
const hasChildrenIndex = ref(0);

const houseStatusOptions = ['请选择', '无房', '有房有贷款', '有房无贷款', '与父母同住'];
const houseStatusIndex = ref(0);

const carStatusOptions = ['请选择', '无车', '有车有贷款', '有车无贷款'];
const carStatusIndex = ref(0);

// 页面加载时获取用户资料
onMounted(async () => {
	// 固定使用用户ID 21 获取详细信息
	await fetchProfileData(21);
});

// 获取用户资料数据
async function fetchProfileData(id) {
	try {
		isLoading.value = true;
		const result = await profileApi.getProfile(id);

		console.log('获取到的用户资料:', result);

		// 检查是否获取到了profile数据
		if (result && result.data && result.data.profile) {
			// 获取到了数据，设置为编辑模式
			isEdit.value = true;

			// 填充表单数据
			Object.assign(profileForm, result.data.profile);

			// 设置选择器索引
			setPickerIndexes();

			console.log('数据回显成功，进入编辑模式');
		} else {
			// 没有获取到数据，设置为新增模式
			isEdit.value = false;
			console.log('未获取到资料数据，进入新增模式');
		}
	} catch (error) {
		console.error('获取资料失败:', error);
		// 获取失败也设置为新增模式
		isEdit.value = false;
		console.log('获取资料失败，进入新增模式');
	} finally {
		isLoading.value = false;
	}
}

// 设置选择器索引
function setPickerIndexes() {
	educationIndex.value = profileForm.education || 0;
	incomeIndex.value = profileForm.incomeLevel || 0;
	maritalStatusIndex.value = profileForm.maritalStatus || 0;
	hasChildrenIndex.value = profileForm.hasChildren || 0;
	houseStatusIndex.value = profileForm.houseStatus || 0;
	carStatusIndex.value = profileForm.carStatus || 0;
}

// 选择器变更处理函数
function onEducationChange(e) {
	educationIndex.value = e.detail.value;
	profileForm.education = parseInt(e.detail.value);
}

function onIncomeChange(e) {
	incomeIndex.value = e.detail.value;
	profileForm.incomeLevel = parseInt(e.detail.value);
}

function onMaritalStatusChange(e) {
	maritalStatusIndex.value = e.detail.value;
	profileForm.maritalStatus = parseInt(e.detail.value);
}

function onHasChildrenChange(e) {
	hasChildrenIndex.value = e.detail.value;
	profileForm.hasChildren = parseInt(e.detail.value);
}

function onHouseStatusChange(e) {
	houseStatusIndex.value = e.detail.value;
	profileForm.houseStatus = parseInt(e.detail.value);
}

function onCarStatusChange(e) {
	carStatusIndex.value = e.detail.value;
	profileForm.carStatus = parseInt(e.detail.value);
}

// 选择学历证明
function chooseEducationCert() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const tempFilePath = res.tempFilePaths[0];
			console.log('选择的证明图片路径:', tempFilePath);
			
			// 上传证明图片
			uploadCertificate(tempFilePath);
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

// 上传证明图片
function uploadCertificate(filePath) {
	uni.showLoading({
		title: '上传中...'
	});
	
	profileApi.uploadCertificate(filePath)
		.then(result => {
			uni.hideLoading();
			
			console.log('服务器返回的URL:', result);
			
			if (!result || result.indexOf('http') === -1) {
				console.error('服务器返回的URL格式不正确:', result);
				uni.showToast({
					title: '上传失败，请重试',
					icon: 'none'
				});
				return;
			}
			
			// 设置证明URL
			profileForm.educationCert = result;
			console.log('设置证明URL成功:', profileForm.educationCert);
			
			uni.showToast({
				title: '上传成功',
				icon: 'success'
			});
		})
		.catch(err => {
			uni.hideLoading();
			console.error('上传失败:', err);
			uni.showToast({
				title: '上传失败',
				icon: 'none'
			});
		});
}

// 提交表单
async function handleSubmit() {
	// 表单验证
	if (!validateForm()) {
		return;
	}
	
	isLoading.value = true;
	
	try {
		let result;
		
		// 根据是否为编辑模式选择API
		if (isEdit.value) {
			result = await profileApi.updateProfile(profileForm);
		} else {
			result = await profileApi.createProfile(profileForm);
		}
		
		console.log('提交结果:', result);
		
		uni.showToast({
			title: isEdit.value ? '修改成功' : '提交成功',
			icon: 'success',
			duration: 2000
		});
		
		// 延迟返回上一页
		setTimeout(() => {
			uni.navigateBack();
		}, 2000);
	} catch (error) {
		console.error('提交失败:', error);
		uni.showToast({
			title: '提交失败，请重试',
			icon: 'none'
		});
	} finally {
		isLoading.value = false;
	}
}

// 表单验证
function validateForm() {
	// 必填项验证
	if (!profileForm.realName) {
		showError('请输入真实姓名');
		return false;
	}

	// 其他必填项验证
	if (!profileForm.age) {
		showError('请输入年龄');
		return false;
	}

	if (!profileForm.height) {
		showError('请输入身高');
		return false;
	}

	if (!profileForm.weight) {
		showError('请输入体重');
		return false;
	}

	if (profileForm.education === 0) {
		showError('请选择学历');
		return false;
	}

	if (profileForm.incomeLevel === 0) {
		showError('请选择收入水平');
		return false;
	}

	if (!profileForm.workCity) {
		showError('请输入工作城市');
		return false;
	}

	if (profileForm.maritalStatus === 0) {
		showError('请选择婚姻状况');
		return false;
	}

	if (profileForm.hasChildren === 0) {
		showError('请选择有无子女');
		return false;
	}

	if (profileForm.houseStatus === 0) {
		showError('请选择住房情况');
		return false;
	}

	if (profileForm.carStatus === 0) {
		showError('请选择车辆情况');
		return false;
	}

	if (!profileForm.selfIntroduction) {
		showError('请填写个人介绍');
		return false;
	}

	return true;
}

// 显示错误提示
function showError(message) {
	uni.showToast({
		title: message,
		icon: 'none'
	});
}

// 返回上一页
function goBack() {
	console.log('点击返回按钮');

	// 获取页面栈信息
	const pages = getCurrentPages();
	console.log('当前页面栈长度:', pages.length);

	if (pages.length > 1) {
		// 如果页面栈中有上一页，则返回上一页
		uni.navigateBack({
			delta: 1,
			success: function () {
				console.log('返回上一页成功');
			},
			fail: function (err) {
				console.error('返回上一页失败:', err);
				// 返回失败，跳转到我的页面
				goToMinePage();
			}
		});
	} else {
		// 如果没有上一页（比如直接访问或刷新），跳转到我的页面
		console.log('没有上一页，跳转到我的页面');
		goToMinePage();
	}
}

// 跳转到我的页面
function goToMinePage() {
	uni.reLaunch({
		url: '/pages/mine/mine',
		success: function () {
			console.log('跳转到我的页面成功');
		},
		fail: function (err) {
			console.error('跳转到我的页面失败:', err);
			// 最后尝试跳转到首页
			uni.reLaunch({
				url: '/pages/index/index'
			});
		}
	});
}

// 获取提交按钮文本
function getSubmitButtonText() {
  // 根据是否为编辑模式来决定按钮文本
  return isEdit.value ? '保存修改' : '提交资料';
}

// 跳转到个人主页
function goToProfile() {
  console.log('点击了退出按钮，准备跳转到我的页面');

  // 跳转到我的页面（使用 reLaunch 替代 switchTab）
  uni.reLaunch({
    url: '/pages/mine/mine',
    success: function () {
      console.log('跳转成功');
    },
    fail: function (err) {
      console.error('跳转失败:', err);
      uni.showToast({
        title: '跳转失败',
        icon: 'none'
      });
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

.edit-profile-page {
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
		
		// 顶部标题
		.header {
			margin-top: 120rpx;
			margin-bottom: 40rpx;
			display: flex;
			flex-direction: column;
			align-items: center;

			.page-title {
				font-size: 40rpx;
				font-weight: bold;
				color: #fff;
				text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
			}

			.slogan {
				margin-top: 20rpx;
				font-size: 28rpx;
				color: rgba(255, 255, 255, 0.8);
				letter-spacing: 2rpx;
			}
		}

		// 顶部按钮样式
		.top-buttons {
			position: absolute;
			top: 40rpx;
			left: 0;
			right: 0;
			z-index: 10;
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0 40rpx;

			.back-btn {
				display: flex;
				align-items: center;
				justify-content: center;
				width: 80rpx;
				height: 80rpx;
				cursor: pointer;

				.back-arrow {
					font-size: 48rpx;
					color: #fff;
					font-weight: bold;
					text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
				}

				&:active {
					opacity: 0.7;
					transform: scale(0.95);
				}
			}

			.save-btn-container {
				display: flex;
				align-items: center;
				justify-content: center;
				cursor: pointer;

				.save-btn {
					font-size: 30rpx;
					color: #fff;
					font-weight: bold;
					text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
					padding: 12rpx 24rpx;
					background: rgba(255, 255, 255, 0.25);
					border-radius: 40rpx;
					backdrop-filter: blur(15rpx);
					border: 2rpx solid rgba(255, 255, 255, 0.3);
				}

				.loading-icon {
					width: 36rpx;
					height: 36rpx;
					border: 4rpx solid rgba(255, 255, 255, 0.3);
					border-top: 4rpx solid #fff;
					border-radius: 50%;
					animation: spin 1s linear infinite;
				}

				&:active:not(.disabled) {
					opacity: 0.8;
					transform: scale(0.95);
				}

				&.disabled {
					opacity: 0.6;
					cursor: not-allowed;
				}

				@keyframes spin {
					0% { transform: rotate(0deg); }
					100% { transform: rotate(360deg); }
				}
			}
		}

		// 表单卡片
		.profile-form-card {
			width: 100%;
			background-color: rgba(255, 255, 255, 0.95);
			border-radius: 24rpx;
			box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.15),
						0 0 20rpx rgba(255, 255, 255, 0.1);
			padding: 30rpx 20rpx;
			margin-bottom: 40rpx;
			position: relative;
			overflow: hidden;
			display: flex;
			flex-direction: column;
			flex: 1;
			
			&::before {
				content: '';
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 8rpx;
				background: linear-gradient(to right, #12C2E9, #C471ED, #F64F59);
			}
			
			.form-scroll-view {
				flex: 1;
				height: calc(100vh - 250rpx);
				max-height: calc(100vh - 250rpx);
			}
			
			.form-container {
				padding: 10rpx;
				
				.form-section {
					margin-bottom: 40rpx;
					
					.section-title {
						font-size: 32rpx;
						font-weight: bold;
						color: #333;
						margin-bottom: 20rpx;
						position: relative;
						padding-left: 20rpx;
						
						&::before {
							content: '';
							position: absolute;
							left: 0;
							top: 50%;
							transform: translateY(-50%);
							width: 6rpx;
							height: 28rpx;
							background: linear-gradient(to bottom, #12C2E9, #C471ED);
							border-radius: 3rpx;
						}
					}
					
					.input-group {
						display: flex;
						align-items: center;
						padding: 20rpx 0;
						border-bottom: 2rpx solid #f0f0f0;
						
						.input-label {
							width: 180rpx;
							font-size: 28rpx;
							color: #333;
						}
						
						.input-field {
							flex: 1;
							height: 60rpx;
							font-size: 28rpx;
							color: #333;
						}
						
						.upload-field {
							flex: 1;
							height: 60rpx;
							display: flex;
							align-items: center;
							justify-content: space-between;
							font-size: 28rpx;
							color: #999;
							
							.uploaded {
								color: #12C2E9;
							}
							
							.upload-icon {
								width: 40rpx;
								height: 40rpx;
								line-height: 36rpx;
								text-align: center;
								border-radius: 50%;
								border: 2rpx solid #999;
								color: #999;
							}
						}
					}
					
					.select-group {
						display: flex;
						align-items: center;
						padding: 20rpx 0;
						border-bottom: 2rpx solid #f0f0f0;
						
						.select-label {
							width: 180rpx;
							font-size: 28rpx;
							color: #333;
						}
						
						.picker {
							flex: 1;
							
							.picker-value {
								font-size: 28rpx;
								color: #333;
							}
						}
					}
					
					.textarea-group {
						padding: 20rpx 0;
						position: relative;
						
						.textarea-field {
							width: 100%;
							height: 200rpx;
							font-size: 28rpx;
							color: #333;
							background-color: #f8f8f8;
							border-radius: 12rpx;
							padding: 20rpx;
							box-sizing: border-box;
						}
						
						.textarea-counter {
							position: absolute;
							right: 10rpx;
							bottom: 10rpx;
							font-size: 24rpx;
							color: #999;
						}
					}
				}
			}
			
			.submit-btn {
				width: 90%;
				height: 90rpx;
				background: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
				background-size: 300% 300%;
				color: #fff;
				font-size: 32rpx;
				font-weight: bold;
				border-radius: 45rpx;
				margin: 20rpx auto;
				display: flex;
				align-items: center;
				justify-content: center;
				box-shadow: 0 10rpx 20rpx rgba(196, 113, 237, 0.4);
				animation: gradientAnimation 15s ease infinite;
				
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
}
</style> 