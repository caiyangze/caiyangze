<template>
	<view class="test-page">
		<view class="header">
			<text class="title">点击测试页面</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">基础点击测试</text>
			
			<view class="test-item">
				<button @tap="testBasicClick">基础按钮测试</button>
				<text class="result">{{ basicClickResult }}</text>
			</view>
			
			<view class="test-item">
				<view class="custom-btn" @tap="testCustomClick">
					<text>自定义按钮测试</text>
				</view>
				<text class="result">{{ customClickResult }}</text>
			</view>
			
			<view class="test-item">
				<view class="input-test">
					<input 
						class="test-input" 
						v-model="testInput" 
						placeholder="测试输入框"
						@focus="onInputFocus"
						@blur="onInputBlur"
					/>
					<view class="send-btn" @tap="testSendClick">
						<text>发送</text>
					</view>
				</view>
				<text class="result">{{ inputTestResult }}</text>
			</view>
		</view>
		
		<view class="test-section">
			<text class="section-title">功能按钮测试</text>
			
			<view class="function-buttons">
				<view class="function-btn" @tap="testFunction1">
					<text class="btn-icon">💑</text>
					<text class="btn-text">功能1</text>
				</view>
				<view class="function-btn" @tap="testFunction2">
					<text class="btn-icon">🌹</text>
					<text class="btn-text">功能2</text>
				</view>
				<view class="function-btn" @tap="testFunction3">
					<text class="btn-icon">🎨</text>
					<text class="btn-text">功能3</text>
				</view>
			</view>
			<text class="result">{{ functionTestResult }}</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">层级测试</text>
			
			<view class="layer-test">
				<view class="background-layer" @tap="testBackgroundClick">
					<text>背景层</text>
				</view>
				<view class="content-layer" @tap.stop="testContentClick">
					<text>内容层</text>
				</view>
			</view>
			<text class="result">{{ layerTestResult }}</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">事件冒泡测试</text>
			
			<view class="bubble-test" @tap="testParentClick">
				<text>父级容器</text>
				<view class="child-element" @tap.stop="testChildClick">
					<text>子级元素</text>
				</view>
			</view>
			<text class="result">{{ bubbleTestResult }}</text>
		</view>
		
		<view class="test-section">
			<text class="section-title">测试日志</text>
			<scroll-view class="log-container" scroll-y="true">
				<view v-for="(log, index) in logs" :key="index" class="log-item">
					<text class="log-time">{{ formatTime(log.time) }}</text>
					<text class="log-content">{{ log.message }}</text>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';

// 响应式数据
const basicClickResult = ref('');
const customClickResult = ref('');
const inputTestResult = ref('');
const functionTestResult = ref('');
const layerTestResult = ref('');
const bubbleTestResult = ref('');
const testInput = ref('');
const logs = ref([]);

// 添加日志
function addLog(message) {
	logs.value.unshift({
		time: new Date(),
		message: message
	});
	
	// 限制日志数量
	if (logs.value.length > 20) {
		logs.value = logs.value.slice(0, 20);
	}
	
	console.log('[点击测试]', message);
}

// 格式化时间
function formatTime(time) {
	const date = new Date(time);
	const hours = date.getHours().toString().padStart(2, '0');
	const minutes = date.getMinutes().toString().padStart(2, '0');
	const seconds = date.getSeconds().toString().padStart(2, '0');
	return `${hours}:${minutes}:${seconds}`;
}

// 测试函数
function testBasicClick() {
	basicClickResult.value = '基础按钮点击成功！';
	addLog('基础按钮点击测试成功');
}

function testCustomClick() {
	customClickResult.value = '自定义按钮点击成功！';
	addLog('自定义按钮点击测试成功');
}

function testSendClick() {
	inputTestResult.value = `发送按钮点击成功！输入内容：${testInput.value}`;
	addLog(`发送按钮点击测试成功，输入内容：${testInput.value}`);
}

function onInputFocus() {
	addLog('输入框获得焦点');
}

function onInputBlur() {
	addLog('输入框失去焦点');
}

function testFunction1() {
	functionTestResult.value = '功能按钮1点击成功！';
	addLog('功能按钮1点击测试成功');
}

function testFunction2() {
	functionTestResult.value = '功能按钮2点击成功！';
	addLog('功能按钮2点击测试成功');
}

function testFunction3() {
	functionTestResult.value = '功能按钮3点击成功！';
	addLog('功能按钮3点击测试成功');
}

function testBackgroundClick() {
	layerTestResult.value = '背景层点击成功！';
	addLog('背景层点击测试成功');
}

function testContentClick() {
	layerTestResult.value = '内容层点击成功！';
	addLog('内容层点击测试成功');
}

function testParentClick() {
	bubbleTestResult.value = '父级容器点击成功！';
	addLog('父级容器点击测试成功');
}

function testChildClick() {
	bubbleTestResult.value = '子级元素点击成功！';
	addLog('子级元素点击测试成功');
}
</script>

<style lang="scss" scoped>
.test-page {
	padding: 20rpx;
	background: #f5f5f5;
	min-height: 100vh;
	
	.header {
		text-align: center;
		margin-bottom: 40rpx;
		
		.title {
			font-size: 36rpx;
			font-weight: bold;
			color: #333;
		}
	}
	
	.test-section {
		background: #fff;
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		
		.section-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20rpx;
			display: block;
		}
		
		.test-item {
			margin-bottom: 20rpx;
			
			button {
				width: 100%;
				padding: 20rpx;
				background: #007aff;
				color: #fff;
				border: none;
				border-radius: 10rpx;
				font-size: 28rpx;
				margin-bottom: 10rpx;
			}
			
			.custom-btn {
				width: 100%;
				padding: 20rpx;
				background: #28a745;
				color: #fff;
				border-radius: 10rpx;
				text-align: center;
				font-size: 28rpx;
				margin-bottom: 10rpx;
				cursor: pointer;
			}
			
			.input-test {
				display: flex;
				gap: 15rpx;
				margin-bottom: 10rpx;
				
				.test-input {
					flex: 1;
					padding: 20rpx;
					border: 2rpx solid #ddd;
					border-radius: 10rpx;
					font-size: 28rpx;
				}
				
				.send-btn {
					padding: 20rpx 30rpx;
					background: #ff6b6b;
					color: #fff;
					border-radius: 10rpx;
					text-align: center;
					cursor: pointer;
				}
			}
		}
		
		.function-buttons {
			display: flex;
			gap: 20rpx;
			margin-bottom: 20rpx;
			
			.function-btn {
				flex: 1;
				background: linear-gradient(135deg, #667eea, #764ba2);
				border-radius: 20rpx;
				padding: 20rpx 10rpx;
				text-align: center;
				cursor: pointer;
				
				.btn-icon {
					font-size: 32rpx;
					display: block;
					margin-bottom: 8rpx;
				}
				
				.btn-text {
					font-size: 22rpx;
					color: #fff;
					display: block;
				}
			}
		}
		
		.layer-test {
			position: relative;
			height: 200rpx;
			margin-bottom: 20rpx;
			
			.background-layer {
				position: absolute;
				top: 0;
				left: 0;
				width: 100%;
				height: 100%;
				background: #ffc107;
				border-radius: 10rpx;
				display: flex;
				align-items: center;
				justify-content: center;
				font-size: 28rpx;
				color: #333;
			}
			
			.content-layer {
				position: absolute;
				top: 50rpx;
				left: 50rpx;
				width: 200rpx;
				height: 100rpx;
				background: #17a2b8;
				border-radius: 10rpx;
				display: flex;
				align-items: center;
				justify-content: center;
				font-size: 24rpx;
				color: #fff;
				z-index: 10;
			}
		}
		
		.bubble-test {
			background: #e9ecef;
			padding: 40rpx;
			border-radius: 10rpx;
			text-align: center;
			margin-bottom: 20rpx;
			
			.child-element {
				background: #6c757d;
				color: #fff;
				padding: 20rpx;
				border-radius: 8rpx;
				margin-top: 20rpx;
				display: inline-block;
			}
		}
		
		.result {
			font-size: 24rpx;
			color: #28a745;
			font-weight: bold;
			display: block;
		}
		
		.log-container {
			height: 300rpx;
			border: 2rpx solid #eee;
			border-radius: 10rpx;
			padding: 20rpx;
			
			.log-item {
				margin-bottom: 10rpx;
				padding-bottom: 10rpx;
				border-bottom: 1rpx solid #f0f0f0;
				
				&:last-child {
					border-bottom: none;
				}
				
				.log-time {
					font-size: 20rpx;
					color: #999;
					margin-right: 15rpx;
				}
				
				.log-content {
					font-size: 24rpx;
					color: #333;
				}
			}
		}
	}
}
</style>
