<template>
	<button 
		:class="['custom-btn', type, {'btn-block': block}, {'btn-disabled': disabled}]" 
		:style="customStyle"
		:disabled="disabled"
		@click="handleClick"
	>
		<view class="btn-content">
			<text v-if="loading" class="iconfont icon-loading loading-icon"></text>
			<slot></slot>
		</view>
	</button>
</template>

<script>
export default {
	name: 'CustomButton',
	props: {
		// 按钮类型：primary, outline, text
		type: {
			type: String,
			default: 'primary'
		},
		// 是否为块级按钮
		block: {
			type: Boolean,
			default: false
		},
		// 是否禁用
		disabled: {
			type: Boolean,
			default: false
		},
		// 是否显示加载状态
		loading: {
			type: Boolean,
			default: false
		},
		// 自定义样式
		customStyle: {
			type: Object,
			default: () => ({})
		}
	},
	methods: {
		handleClick(e) {
			if (!this.disabled && !this.loading) {
				this.$emit('click', e);
			}
		}
	}
}
</script>

<style>
.custom-btn {
	height: 90rpx;
	line-height: 90rpx;
	font-size: 32rpx;
	border-radius: 45rpx;
	padding: 0 60rpx;
	position: relative;
	overflow: hidden;
	display: inline-flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s;
}

.btn-content {
	display: flex;
	align-items: center;
	justify-content: center;
}

.primary {
	background-color: #FF6B9A;
	color: #FFFFFF;
	box-shadow: 0 10rpx 20rpx rgba(255, 107, 154, 0.3);
}

.outline {
	background-color: transparent;
	color: #FF6B9A;
	border: 2rpx solid #FF6B9A;
}

.text {
	background-color: transparent;
	color: #FF6B9A;
	padding: 0;
	height: auto;
	line-height: 1.5;
}

.btn-block {
	display: flex;
	width: 100%;
}

.btn-disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.loading-icon {
	margin-right: 10rpx;
	animation: rotate 1s linear infinite;
}

@keyframes rotate {
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(360deg);
	}
}
</style> 