"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "uni-popup",
  props: {
    // 开启动画
    animation: {
      type: Boolean,
      default: true
    },
    // 弹出层类型，可选值，top: 顶部弹出层；bottom：底部弹出层；center：全屏弹出层
    type: {
      type: String,
      default: "center"
    },
    // maskClick
    maskClick: {
      type: Boolean,
      default: true
    }
  },
  setup(__props, { expose: __expose }) {
    const props = __props;
    common_vendor.ref(false);
    const showPopup = common_vendor.ref(false);
    const showTrans = common_vendor.ref(false);
    const popupWidth = common_vendor.ref(0);
    const popupHeight = common_vendor.ref(0);
    const maskClass = common_vendor.computed(() => {
      return showTrans.value ? "uni-popup__mask--show" : "";
    });
    const typeClass = common_vendor.computed(() => {
      return props.type === "center" ? "uni-popup__wrapper--center" : props.type === "bottom" ? "uni-popup__wrapper--bottom" : props.type === "top" ? "uni-popup__wrapper--top" : "";
    });
    const popupClass = common_vendor.computed(() => {
      return showPopup.value ? "uni-popup--show" : "";
    });
    const popupStyle = common_vendor.computed(() => {
      return props.type !== "center" ? {} : {
        width: popupWidth.value ? `${popupWidth.value}px` : "",
        height: popupHeight.value ? `${popupHeight.value}px` : "",
        backgroundColor: "transparent"
      };
    });
    function open() {
      showPopup.value = true;
      showTrans.value = true;
    }
    function close() {
      showTrans.value = false;
      setTimeout(() => {
        showPopup.value = false;
      }, 300);
    }
    function onTap() {
      if (!props.maskClick)
        return;
      close();
    }
    function clear() {
    }
    __expose({
      open,
      close
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.n(maskClass.value),
        b: common_vendor.o(onTap),
        c: common_vendor.n(typeClass.value),
        d: common_vendor.s(popupStyle.value),
        e: common_vendor.n(popupClass.value),
        f: common_vendor.o(clear)
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-c501667a"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/common/uni-popup.js.map
