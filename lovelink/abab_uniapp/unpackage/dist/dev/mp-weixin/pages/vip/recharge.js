"use strict";
const common_vendor = require("../../common/vendor.js");
const api_vip = require("../../api/vip.js");
const _sfc_main = {
  data() {
    return {
      vipPackages: [],
      payMethods: [],
      selectedPackage: null,
      selectedPayMethod: null,
      isProcessing: false,
      currentOrder: null
    };
  },
  onLoad() {
    this.loadVipPackages();
    this.loadPayMethods();
  },
  methods: {
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 加载VIP套餐
    async loadVipPackages() {
      try {
        const response = await api_vip.getVipPackages();
        if (response.code === 200) {
          this.vipPackages = response.data;
          common_vendor.index.__f__("log", "at pages/vip/recharge.vue:139", "VIP套餐加载成功:", this.vipPackages);
        } else {
          common_vendor.index.showToast({
            title: response.message || "加载套餐失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/vip/recharge.vue:147", "加载VIP套餐失败:", error);
        common_vendor.index.showToast({
          title: "网络错误",
          icon: "none"
        });
      }
    },
    // 加载支付方式
    async loadPayMethods() {
      try {
        const response = await api_vip.getPayMethods();
        if (response.code === 200) {
          this.payMethods = response.data;
          common_vendor.index.__f__("log", "at pages/vip/recharge.vue:161", "支付方式加载成功:", this.payMethods);
        } else {
          common_vendor.index.showToast({
            title: response.message || "加载支付方式失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/vip/recharge.vue:169", "加载支付方式失败:", error);
        common_vendor.index.showToast({
          title: "网络错误",
          icon: "none"
        });
      }
    },
    // 选择套餐
    selectPackage(pkg) {
      this.selectedPackage = pkg.vipType;
      common_vendor.index.__f__("log", "at pages/vip/recharge.vue:180", "选择套餐:", pkg);
    },
    // 选择支付方式
    selectPayMethod(payType) {
      this.selectedPayMethod = payType;
      common_vendor.index.__f__("log", "at pages/vip/recharge.vue:186", "选择支付方式:", payType);
    },
    // 获取当前选中套餐的价格
    getCurrentPrice() {
      const pkg = this.vipPackages.find((p) => p.vipType === this.selectedPackage);
      return pkg ? pkg.currentPrice : "0.00";
    },
    // 获取支付图标
    getPayIcon(payType) {
      switch (payType) {
        case 1:
          return "💬";
        case 2:
          return "💰";
        case 3:
          return "🍎";
        case 4:
          return "💳";
        default:
          return "💳";
      }
    },
    // 获取套餐图标
    getPackageIcon(index) {
      const icons = ["🌟", "💎", "👑"];
      return icons[index] || "⭐";
    },
    // 处理支付
    async processPayment() {
      if (!this.selectedPackage || !this.selectedPayMethod) {
        common_vendor.index.showToast({
          title: "请选择套餐和支付方式",
          icon: "none"
        });
        return;
      }
      this.isProcessing = true;
      try {
        const orderResponse = await api_vip.createVipOrder({
          vipType: this.selectedPackage
        });
        if (orderResponse.code !== 200) {
          throw new Error(orderResponse.message || "创建订单失败");
        }
        this.currentOrder = orderResponse.data;
        common_vendor.index.__f__("log", "at pages/vip/recharge.vue:235", "订单创建成功:", this.currentOrder);
        const payResponse = await api_vip.processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: this.selectedPayMethod
        });
        if (payResponse.code !== 200) {
          throw new Error(payResponse.message || "支付失败");
        }
        common_vendor.index.__f__("log", "at pages/vip/recharge.vue:247", "支付成功:", payResponse.data);
        common_vendor.index.showToast({
          title: "开通成功！",
          icon: "success",
          duration: 2e3
        });
        setTimeout(() => {
          common_vendor.index.redirectTo({
            url: `/pages/vip/success?orderData=${encodeURIComponent(JSON.stringify(payResponse.data))}`
          });
        }, 2e3);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/vip/recharge.vue:264", "支付处理失败:", error);
        common_vendor.index.showToast({
          title: error.message || "支付失败",
          icon: "none"
        });
      } finally {
        this.isProcessing = false;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: common_vendor.f($data.vipPackages, (pkg, index, i0) => {
      return common_vendor.e({
        a: pkg.discount
      }, pkg.discount ? {
        b: common_vendor.t(pkg.discount)
      } : {}, {
        c: common_vendor.t($options.getPackageIcon(index)),
        d: common_vendor.t(pkg.name),
        e: common_vendor.t(pkg.duration),
        f: common_vendor.t(pkg.currentPrice),
        g: pkg.originalPrice !== pkg.currentPrice
      }, pkg.originalPrice !== pkg.currentPrice ? {
        h: common_vendor.t(pkg.originalPrice)
      } : {}, {
        i: common_vendor.f(pkg.features.slice(0, 2), (feature, fIndex, i1) => {
          return {
            a: common_vendor.t(feature),
            b: fIndex
          };
        }),
        j: pkg.features.length > 2
      }, pkg.features.length > 2 ? {
        k: common_vendor.t(pkg.features.length - 2)
      } : {}, {
        l: $data.selectedPackage === pkg.vipType
      }, $data.selectedPackage === pkg.vipType ? {} : {}, {
        m: pkg.vipType,
        n: common_vendor.n("package-" + (index + 1)),
        o: common_vendor.n({
          "selected": $data.selectedPackage === pkg.vipType
        }),
        p: common_vendor.n({
          "recommended": index === 1
        }),
        q: common_vendor.o(($event) => $options.selectPackage(pkg), pkg.vipType)
      });
    }),
    c: $data.selectedPackage
  }, $data.selectedPackage ? {
    d: common_vendor.f($data.payMethods, (method, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($options.getPayIcon(method.payType)),
        b: common_vendor.t(method.name),
        c: $data.selectedPayMethod === method.payType
      }, $data.selectedPayMethod === method.payType ? {} : {}, {
        d: method.payType,
        e: $data.selectedPayMethod === method.payType ? 1 : "",
        f: common_vendor.o(($event) => $options.selectPayMethod(method.payType), method.payType)
      });
    })
  } : {}, {
    e: $data.selectedPackage && $data.selectedPayMethod
  }, $data.selectedPackage && $data.selectedPayMethod ? {
    f: common_vendor.t($options.getCurrentPrice()),
    g: common_vendor.t($data.isProcessing ? "处理中..." : "立即开通VIP"),
    h: common_vendor.o((...args) => $options.processPayment && $options.processPayment(...args)),
    i: $data.isProcessing
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-fc401ca1"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/vip/recharge.js.map
