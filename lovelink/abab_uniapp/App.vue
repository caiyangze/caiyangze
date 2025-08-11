<script>
import { initGlobalTheme } from '@/utils/global-theme.js';

export default {
	onLaunch: function() {
		console.log('App Launch');
		// 初始化全局主题
		initGlobalTheme();
		console.log('统一主题系统已初始化');
	},
	onShow: function() {
		console.log('App Show');
		// 每次显示时重新初始化主题，确保主题状态正确
		initGlobalTheme();
	},
	onHide: function() {
		console.log('App Hide');
	}
};
</script>

<style>
/* 全局样式 */
@import './static/css/common.css';
@import './static/css/placeholder.css';

page {
	min-height: 100%;
	display: flex;
}

/* 主色调定义 */
:root {
	--primary-color: #C471ED; /* 更新为紫色系的主色调 */
	--primary-gradient: linear-gradient(45deg, #12C2E9, #C471ED, #F64F59);
	--secondary-color: #F3E7FF;
	--text-color: #333333;
	--light-text: #999999;
	--background-color: #F8F8F8;
	--white: #FFFFFF;
}

/* 通用样式 */
.container {
	width: 100%;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	box-sizing: border-box;
}

.flex-row {
	display: flex;
	flex-direction: row;
}

.flex-column {
	display: flex;
	flex-direction: column;
}

.align-center {
	align-items: center;
}

.justify-center {
	justify-content: center;
}

.text-center {
	text-align: center;
}

/* 按钮样式 */
.btn-primary {
	background-color: var(--primary-color);
	color: var(--white);
	border-radius: 50rpx;
	height: 90rpx;
	line-height: 90rpx;
	font-size: 32rpx;
	font-weight: bold;
	box-shadow: 0 10rpx 20rpx rgba(255, 107, 154, 0.3);
}

.btn-outline {
	background-color: transparent;
	color: var(--primary-color);
	border: 2rpx solid var(--primary-color);
	border-radius: 50rpx;
	height: 90rpx;
	line-height: 90rpx;
	font-size: 32rpx;
}

/* 表单样式 */
.form-item {
	background-color: var(--white);
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.input-box {
	height: 90rpx;
	border-bottom: 1rpx solid #EEEEEE;
	display: flex;
	align-items: center;
}

.input-box input {
	flex: 1;
	height: 90rpx;
	font-size: 28rpx;
	color: var(--text-color);
}

.input-placeholder {
	color: var(--light-text);
}
</style>