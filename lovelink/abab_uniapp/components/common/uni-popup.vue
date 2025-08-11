<template>
  <view class="uni-popup" :class="[popupClass]" @touchmove.stop.prevent="clear">
    <view class="uni-popup__mask" :class="[maskClass]" @tap="onTap" />
    <view class="uni-popup__wrapper" :class="[typeClass]" :style="[popupStyle]">
      <view class="uni-popup__wrapper-box">
        <slot />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

const props = defineProps({
  // 开启动画
  animation: {
    type: Boolean,
    default: true
  },
  // 弹出层类型，可选值，top: 顶部弹出层；bottom：底部弹出层；center：全屏弹出层
  type: {
    type: String,
    default: 'center'
  },
  // maskClick
  maskClick: {
    type: Boolean,
    default: true
  }
});

const isDesktop = ref(false);
const showPopup = ref(false);
const showTrans = ref(false);

const popupWidth = ref(0);
const popupHeight = ref(0);

// 弹窗背景样式
const maskClass = computed(() => {
  return showTrans.value ? 'uni-popup__mask--show' : '';
});

// 弹窗样式
const typeClass = computed(() => {
  return props.type === 'center' ? 'uni-popup__wrapper--center' : 
         props.type === 'bottom' ? 'uni-popup__wrapper--bottom' : 
         props.type === 'top' ? 'uni-popup__wrapper--top' : '';
});

const popupClass = computed(() => {
  return showPopup.value ? 'uni-popup--show' : '';
});

const popupStyle = computed(() => {
  return props.type !== 'center' ? {} : {
    width: popupWidth.value ? `${popupWidth.value}px` : '',
    height: popupHeight.value ? `${popupHeight.value}px` : '',
    backgroundColor: 'transparent'
  };
});

// 打开弹窗
function open() {
  showPopup.value = true;
  showTrans.value = true;
}

// 关闭弹窗
function close() {
  showTrans.value = false;
  setTimeout(() => {
    showPopup.value = false;
  }, 300);
}

// 点击遮罩层
function onTap() {
  if (!props.maskClick) return;
  close();
}

// 清理
function clear() {}

// 暴露方法给父组件
defineExpose({
  open,
  close
});
</script>

<style lang="scss" scoped>
.uni-popup {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  overflow: hidden;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  
  &--show {
    opacity: 1;
    visibility: visible;
  }
  
  &__mask {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: rgba(0, 0, 0, 0.4);
    opacity: 0;
    transition: all 0.3s;
    
    &--show {
      opacity: 1;
    }
  }
  
  &__wrapper {
    position: absolute;
    transition: all 0.3s;
    
    &--center {
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%) scale(0.8);
      opacity: 0;
      
      .uni-popup--show & {
        transform: translate(-50%, -50%) scale(1);
        opacity: 1;
      }
    }
    
    &--top {
      top: 0;
      left: 0;
      right: 0;
      transform: translateY(-100%);
      
      .uni-popup--show & {
        transform: translateY(0);
      }
    }
    
    &--bottom {
      bottom: 0;
      left: 0;
      right: 0;
      transform: translateY(100%);
      
      .uni-popup--show & {
        transform: translateY(0);
      }
    }
    
    &-box {
      position: relative;
      box-sizing: border-box;
    }
  }
}
</style> 