"use strict";
const common_vendor = require("../../common/vendor.js");
const api_voucher = require("../../api/voucher.js");
const _sfc_main = {
  name: "VoucherSelector",
  props: {
    // 消费金额，用于筛选可用优惠券
    consumeAmount: {
      type: Number,
      default: 0
    },
    // 消费类型，用于筛选适用的优惠券
    consumeType: {
      type: String,
      default: "all"
      // all, heart-match, matchmaker-apply, vip, recharge
    }
  },
  data() {
    return {
      showModal: false,
      availableVouchers: [],
      selectedVoucher: null,
      tempSelectedVoucher: null,
      loading: false
    };
  },
  computed: {
    availableCount() {
      return this.availableVouchers.length;
    }
  },
  mounted() {
    this.loadAvailableVouchers();
  },
  methods: {
    // 显示优惠券选择弹窗
    showVoucherModal() {
      this.tempSelectedVoucher = this.selectedVoucher;
      this.showModal = true;
      this.loadAvailableVouchers();
    },
    // 隐藏弹窗
    hideModal() {
      this.showModal = false;
      this.tempSelectedVoucher = null;
    },
    // 加载可用优惠券
    async loadAvailableVouchers() {
      if (this.loading)
        return;
      this.loading = true;
      try {
        const response = await api_voucher.getMyAvailableVouchers();
        if (response.code === 200) {
          this.availableVouchers = response.data.records.filter((voucher) => {
            return this.isVoucherAvailable(voucher);
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at components/voucher-selector/voucher-selector.vue:176", "加载优惠券失败:", error);
      } finally {
        this.loading = false;
      }
    },
    // 判断优惠券是否可用
    isVoucherAvailable(voucher) {
      if (voucher.status !== 2) {
        return false;
      }
      if (!voucher.voucher) {
        return false;
      }
      if (!voucher.voucher.actualValue || voucher.voucher.actualValue <= 0) {
        return false;
      }
      if (this.consumeAmount < voucher.voucher.actualValue) {
        return false;
      }
      return true;
    },
    // 选择优惠券
    selectVoucher(voucher) {
      this.tempSelectedVoucher = voucher;
    },
    // 确认选择
    confirmSelection() {
      this.selectedVoucher = this.tempSelectedVoucher;
      this.showModal = false;
      this.$emit("voucher-selected", this.selectedVoucher);
    },
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr)
        return "";
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    },
    // 前往优惠券中心
    goToVoucherCenter() {
      this.hideModal();
      common_vendor.index.navigateTo({
        url: "/pages/voucher/voucher-list"
      });
    },
    // 获取选中的优惠券
    getSelectedVoucher() {
      return this.selectedVoucher;
    },
    // 清除选中的优惠券
    clearSelection() {
      this.selectedVoucher = null;
      this.$emit("voucher-selected", null);
    },
    // 获取优惠券最小使用金额
    getMinAmount(voucher) {
      return voucher.voucher.actualValue || 0;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: !$data.selectedVoucher
  }, !$data.selectedVoucher ? {} : {
    b: common_vendor.t($data.selectedVoucher.voucher.actualValue)
  }, {
    c: $options.availableCount > 0
  }, $options.availableCount > 0 ? {
    d: common_vendor.t($options.availableCount)
  } : {}, {
    e: common_vendor.o((...args) => $options.showVoucherModal && $options.showVoucherModal(...args)),
    f: $data.showModal
  }, $data.showModal ? common_vendor.e({
    g: common_vendor.t($data.availableVouchers.length),
    h: common_vendor.o((...args) => $options.hideModal && $options.hideModal(...args)),
    i: !$data.tempSelectedVoucher ? 1 : "",
    j: !$data.tempSelectedVoucher ? 1 : "",
    k: common_vendor.o(($event) => $options.selectVoucher(null)),
    l: common_vendor.f($data.availableVouchers, (voucher, k0, i0) => {
      return {
        a: common_vendor.t(voucher.voucher.actualValue),
        b: common_vendor.t(voucher.voucher.voucherName || "虚拟币优惠券"),
        c: common_vendor.t($options.getMinAmount(voucher)),
        d: common_vendor.t($options.formatDate(voucher.voucher.endTime)),
        e: $data.tempSelectedVoucher && $data.tempSelectedVoucher.id === voucher.id ? 1 : "",
        f: voucher.id,
        g: $data.tempSelectedVoucher && $data.tempSelectedVoucher.id === voucher.id ? 1 : "",
        h: common_vendor.o(($event) => $options.selectVoucher(voucher), voucher.id)
      };
    }),
    m: $data.availableVouchers.length === 0 && !$data.loading
  }, $data.availableVouchers.length === 0 && !$data.loading ? {
    n: common_vendor.o((...args) => $options.goToVoucherCenter && $options.goToVoucherCenter(...args))
  } : {}, {
    o: $data.loading
  }, $data.loading ? {} : {}, {
    p: common_vendor.o((...args) => $options.hideModal && $options.hideModal(...args)),
    q: common_vendor.o((...args) => $options.confirmSelection && $options.confirmSelection(...args)),
    r: common_vendor.o(() => {
    }),
    s: common_vendor.o((...args) => $options.hideModal && $options.hideModal(...args))
  }) : {});
}
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-6fa635ff"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/voucher-selector/voucher-selector.js.map
