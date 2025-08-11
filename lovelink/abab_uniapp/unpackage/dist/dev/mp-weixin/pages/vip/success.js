"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      orderData: {},
      vipPrivileges: [
        { icon: "👁️", text: "无限制查看用户资料" },
        { icon: "💖", text: "每日超级喜欢次数增加" },
        { icon: "⭐", text: "专属VIP标识显示" },
        { icon: "🎯", text: "优先匹配推荐" },
        { icon: "👀", text: "查看谁喜欢了我" },
        { icon: "🔄", text: "无限制撤回操作" }
      ]
    };
  },
  onLoad(options) {
    if (options.orderData) {
      try {
        this.orderData = JSON.parse(decodeURIComponent(options.orderData));
        common_vendor.index.__f__("log", "at pages/vip/success.vue:95", "订单数据:", this.orderData);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/vip/success.vue:97", "解析订单数据失败:", error);
        this.orderData = {
          orderNo: "VIP" + Date.now(),
          payMethodName: "微信支付",
          payAmount: "19.90",
          vipEndTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1e3).toISOString()
        };
      }
    }
  },
  methods: {
    // 获取VIP类型名称
    getVipTypeName() {
      if (this.orderData.vipEndTime) {
        const endTime = new Date(this.orderData.vipEndTime);
        const startTime = new Date(this.orderData.vipStartTime || Date.now());
        const diffMonths = Math.round((endTime - startTime) / (30 * 24 * 60 * 60 * 1e3));
        if (diffMonths <= 1)
          return "月度VIP";
        if (diffMonths <= 3)
          return "季度VIP";
        return "年度VIP";
      }
      return "VIP会员";
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString)
        return "永久有效";
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    // 查看订单详情
    viewOrder() {
      if (this.orderData.orderId) {
        common_vendor.index.navigateTo({
          url: `/pages/vip/order-detail?orderId=${this.orderData.orderId}`
        });
      } else {
        common_vendor.index.showToast({
          title: "订单信息不完整",
          icon: "none"
        });
      }
    },
    // 开始体验VIP功能
    startExperience() {
      common_vendor.index.reLaunch({
        url: "/pages/index/index"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.t($options.getVipTypeName()),
    b: common_vendor.t($data.orderData.orderNo),
    c: common_vendor.t($data.orderData.payMethodName),
    d: common_vendor.t($data.orderData.payAmount),
    e: common_vendor.t($options.formatDate($data.orderData.vipEndTime)),
    f: common_vendor.f($data.vipPrivileges, (privilege, index, i0) => {
      return {
        a: common_vendor.t(privilege.icon),
        b: common_vendor.t(privilege.text),
        c: index
      };
    }),
    g: common_vendor.o((...args) => $options.viewOrder && $options.viewOrder(...args)),
    h: common_vendor.o((...args) => $options.startExperience && $options.startExperience(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-7437d51e"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/vip/success.js.map
