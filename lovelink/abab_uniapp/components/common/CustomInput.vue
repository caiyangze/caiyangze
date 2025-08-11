<template>
	<view class="custom-input" :class="{'input-focus': isFocus, 'input-error': error}">
		<view class="input-icon" v-if="leftIcon">
			<text class="iconfont" :class="leftIcon"></text>
		</view>
		<input
			class="input-field"
			:value="value"
			:type="type"
			:password="password"
			:placeholder="placeholder"
			:disabled="disabled"
			:maxlength="maxlength"
			@input="handleInput"
			@focus="handleFocus"
			@blur="handleBlur"
		/>
		<view class="input-icon right-icon" v-if="rightIcon || clearable" @click="handleRightIconClick">
			<text class="iconfont icon-close" v-if="clearable && value"></text>
			<text class="iconfont" :class="rightIcon" v-else-if="rightIcon"></text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'CustomInput',
	props: {
		value: {
			type: [String, Number],
			default: ''
		},
		type: {
			type: String,
			default: 'text'
		},
		placeholder: {
			type: String,
			default: '请输入'
		},
		leftIcon: {
			type: String,
			default: ''
		},
		rightIcon: {
			type: String,
			default: ''
		},
		clearable: {
			type: Boolean,
			default: false
		},
		password: {
			type: Boolean,
			default: false
		},
		disabled: {
			type: Boolean,
			default: false
		},
		maxlength: {
			type: [String, Number],
			default: 140
		},
		error: {
			type: Boolean,
			default: false
		}
	},
	data() {
		return {
			isFocus: false
		}
	},
	methods: {
		handleInput(e) {
			this.$emit('input', e.detail.value);
		},
		handleFocus(e) {
			this.isFocus = true;
			this.$emit('focus', e);
		},
		handleBlur(e) {
			this.isFocus = false;
			this.$emit('blur', e);
		},
		handleRightIconClick() {
			if (this.clearable && this.value) {
				this.$emit('input', '');
			} else if (this.rightIcon) {
				this.$emit('right-icon-click');
			}
		}
	}
}
</script>

<style>
.custom-input {
	display: flex;
	align-items: center;
	height: 90rpx;
	border-bottom: 1rpx solid #EEEEEE;
	transition: all 0.3s;
}

.input-focus {
	border-color: #FF6B9A;
}

.input-error {
	border-color: #FF5151;
}

.input-icon {
	width: 60rpx;
	display: flex;
	justify-content: center;
	align-items: center;
	color: #999999;
}

.right-icon {
	color: #CCCCCC;
}

.input-field {
	flex: 1;
	height: 90rpx;
	font-size: 28rpx;
	color: #333333;
}
</style> 