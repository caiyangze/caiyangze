"use strict";
const common_vendor = require("../../common/vendor.js");
const api_http = require("../../api/http.js");
const _sfc_main = {
  data() {
    return {
      testResult: "点击按钮开始测试...",
      testOrderId: null
    };
  },
  methods: {
    // 测试VIP充值页面
    testVipRecharge() {
      common_vendor.index.navigateTo({
        url: "/pages/vip/recharge"
      });
    },
    // 测试VIP成功页面
    testVipSuccess() {
      const testData = {
        orderId: 12345,
        orderNo: "VIP" + Date.now(),
        payMethodName: "微信支付",
        payAmount: "19.90",
        vipStartTime: (/* @__PURE__ */ new Date()).toISOString(),
        vipEndTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1e3).toISOString()
      };
      common_vendor.index.navigateTo({
        url: `/pages/vip/success?orderData=${encodeURIComponent(JSON.stringify(testData))}`
      });
    },
    // 测试获取VIP套餐
    async testGetPackages() {
      try {
        this.testResult = "正在获取VIP套餐...";
        const response = await api_http.http.get("/VIP/packages");
        this.testResult = `获取VIP套餐成功:
${JSON.stringify(response, null, 2)}`;
      } catch (error) {
        this.testResult = `获取VIP套餐失败: ${error.message}`;
      }
    },
    // 测试获取支付方式
    async testGetPayMethods() {
      try {
        this.testResult = "正在获取支付方式...";
        const response = await api_http.http.get("/VIP/payMethods");
        this.testResult = `获取支付方式成功:
${JSON.stringify(response, null, 2)}`;
      } catch (error) {
        this.testResult = `获取支付方式失败: ${error.message}`;
      }
    },
    // 测试创建订单
    async testCreateOrder() {
      try {
        this.testResult = "正在创建订单...";
        const response = await api_http.http.post("/VIP/createOrder", {
          vipType: 1
          // 月度VIP
        });
        if (response.code === 200) {
          this.testOrderId = response.data.orderId;
          this.testResult = `创建订单成功:
订单ID: ${this.testOrderId}
${JSON.stringify(response, null, 2)}`;
        } else {
          this.testResult = `创建订单失败: ${response.message}`;
        }
      } catch (error) {
        this.testResult = `创建订单失败: ${error.message}`;
      }
    },
    // 测试支付流程
    async testPayment() {
      if (!this.testOrderId) {
        this.testResult = "请先创建订单";
        return;
      }
      try {
        this.testResult = "正在处理支付...";
        const response = await api_http.http.post("/VIP/pay", {
          orderId: this.testOrderId,
          payType: 1
          // 微信支付
        });
        if (response.code === 200) {
          this.testResult = `支付成功:
${JSON.stringify(response, null, 2)}`;
        } else {
          this.testResult = `支付失败: ${response.message}`;
        }
      } catch (error) {
        this.testResult = `支付失败: ${error.message}`;
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.testVipRecharge && $options.testVipRecharge(...args)),
    b: common_vendor.o((...args) => $options.testVipSuccess && $options.testVipSuccess(...args)),
    c: common_vendor.o((...args) => $options.testGetPackages && $options.testGetPackages(...args)),
    d: common_vendor.o((...args) => $options.testGetPayMethods && $options.testGetPayMethods(...args)),
    e: common_vendor.o((...args) => $options.testCreateOrder && $options.testCreateOrder(...args)),
    f: common_vendor.o((...args) => $options.testPayment && $options.testPayment(...args)),
    g: common_vendor.t($data.testResult)
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-b416ba08"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/vip-test.js.map
