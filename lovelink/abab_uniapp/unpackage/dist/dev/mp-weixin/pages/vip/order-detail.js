"use strict";
const common_vendor = require("../../common/vendor.js");
const api_vip = require("../../api/vip.js");
const _sfc_main = {
  data() {
    return {
      orderId: null,
      orderDetail: {},
      loading: true
    };
  },
  onLoad(options) {
    if (options.orderId) {
      this.orderId = options.orderId;
      this.loadOrderDetail();
    } else {
      common_vendor.index.showToast({
        title: "订单ID不能为空",
        icon: "none"
      });
      setTimeout(() => {
        common_vendor.index.navigateBack();
      }, 1500);
    }
  },
  methods: {
    // 加载订单详情
    async loadOrderDetail() {
      try {
        this.loading = true;
        const response = await api_vip.getVipOrderDetail(this.orderId);
        if (response.code === 200) {
          this.orderDetail = response.data;
        } else {
          common_vendor.index.showToast({
            title: response.message || "获取订单详情失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/vip/order-detail.vue:145", "获取订单详情失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    // 获取状态样式类
    getStatusClass() {
      switch (this.orderDetail.orderStatus) {
        case 0:
          return "status-pending";
        case 1:
          return "status-success";
        case 2:
          return "status-cancelled";
        case 3:
          return "status-refunded";
        default:
          return "status-unknown";
      }
    },
    // 获取状态图标
    getStatusIcon() {
      switch (this.orderDetail.orderStatus) {
        case 0:
          return "⏳";
        case 1:
          return "✅";
        case 2:
          return "❌";
        case 3:
          return "↩️";
        default:
          return "❓";
      }
    },
    // 获取状态文本
    getStatusText() {
      switch (this.orderDetail.orderStatus) {
        case 0:
          return "待支付";
        case 1:
          return "支付成功";
        case 2:
          return "已取消";
        case 3:
          return "已退款";
        default:
          return "未知状态";
      }
    },
    // 获取状态描述
    getStatusDesc() {
      switch (this.orderDetail.orderStatus) {
        case 0:
          return "请尽快完成支付";
        case 1:
          return "VIP会员已开通成功";
        case 2:
          return "订单已取消";
        case 3:
          return "订单已退款";
        default:
          return "";
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString)
        return "";
      const date = new Date(dateString);
      return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
      });
    },
    // 获取有效期时长
    getValidityDuration() {
      if (!this.orderDetail.startTime || !this.orderDetail.endTime)
        return "";
      const start = new Date(this.orderDetail.startTime);
      const end = new Date(this.orderDetail.endTime);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1e3 * 60 * 60 * 24));
      if (diffDays >= 365) {
        return `${Math.floor(diffDays / 365)}年`;
      } else if (diffDays >= 30) {
        return `${Math.floor(diffDays / 30)}个月`;
      } else {
        return `${diffDays}天`;
      }
    },
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack({
        delta: 1
      });
    },
    // 跳转到VIP中心
    goToVipCenter() {
      common_vendor.index.switchTab({
        url: "/pages/index/index"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: common_vendor.t($options.getStatusIcon()),
    c: common_vendor.n($options.getStatusClass()),
    d: common_vendor.t($options.getStatusText()),
    e: common_vendor.t($options.getStatusDesc()),
    f: common_vendor.t($data.orderDetail.orderNo || "加载中..."),
    g: common_vendor.t($data.orderDetail.vipTypeName || "加载中..."),
    h: common_vendor.t($data.orderDetail.amount || "0.00"),
    i: common_vendor.t($data.orderDetail.payAmount || "0.00"),
    j: $data.orderDetail.discountAmount > 0
  }, $data.orderDetail.discountAmount > 0 ? {
    k: common_vendor.t($data.orderDetail.discountAmount || "0.00")
  } : {}, {
    l: $data.orderDetail.payTypeName
  }, $data.orderDetail.payTypeName ? {
    m: common_vendor.t($data.orderDetail.payTypeName)
  } : {}, {
    n: $data.orderDetail.payTime
  }, $data.orderDetail.payTime ? {
    o: common_vendor.t($options.formatDate($data.orderDetail.payTime))
  } : {}, {
    p: $data.orderDetail.transactionId
  }, $data.orderDetail.transactionId ? {
    q: common_vendor.t($data.orderDetail.transactionId)
  } : {}, {
    r: $data.orderDetail.startTime && $data.orderDetail.endTime
  }, $data.orderDetail.startTime && $data.orderDetail.endTime ? {
    s: common_vendor.t($options.formatDate($data.orderDetail.startTime)),
    t: common_vendor.t($options.formatDate($data.orderDetail.endTime)),
    v: common_vendor.t($options.getValidityDuration())
  } : {}, {
    w: $data.orderDetail.orderStatus === 1
  }, $data.orderDetail.orderStatus === 1 ? {
    x: common_vendor.o((...args) => $options.goToVipCenter && $options.goToVipCenter(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-8ee6528b"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/vip/order-detail.js.map
