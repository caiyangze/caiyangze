"use strict";
const common_vendor = require("../../common/vendor.js");
const api_vip = require("../../api/vip.js");
const _sfc_main = {
  data() {
    return {
      vipStatus: null,
      testResults: [],
      currentOrder: null
    };
  },
  onLoad() {
    this.checkVipStatus();
  },
  methods: {
    // 检查VIP状态
    async checkVipStatus() {
      try {
        const response = await api_vip.getUserVipStatus();
        if (response.code === 200) {
          this.vipStatus = response.data;
          this.addTestResult("VIP状态查询", "成功获取VIP状态", true);
        } else {
          this.addTestResult("VIP状态查询", response.message || "查询失败", false);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/vip-complete-test.vue:94", "查询VIP状态失败:", error);
        this.addTestResult("VIP状态查询", "网络错误", false);
      }
    },
    // 测试完整VIP购买流程
    async testCompleteFlow() {
      this.addTestResult("完整流程测试", "开始测试完整VIP购买流程...", true);
      try {
        const packagesResponse = await api_vip.getVipPackages();
        if (packagesResponse.code !== 200) {
          throw new Error("获取套餐列表失败");
        }
        this.addTestResult("获取套餐", "成功获取套餐列表", true);
        const orderResponse = await api_vip.createVipOrder({ vipType: 2 });
        if (orderResponse.code !== 200) {
          throw new Error("创建订单失败");
        }
        this.currentOrder = orderResponse.data;
        this.addTestResult("创建订单", `订单创建成功，订单号：${this.currentOrder.orderNo}`, true);
        const payResponse = await api_vip.processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: 1
        });
        if (payResponse.code !== 200) {
          throw new Error("支付处理失败");
        }
        this.addTestResult("支付处理", `支付成功，交易号：${payResponse.data.transactionId}`, true);
        await this.checkVipStatus();
        this.addTestResult("完整流程", "✅ VIP购买流程测试完成！请检查数据库记录", true);
        common_vendor.index.showModal({
          title: "测试完成",
          content: "完整VIP购买流程测试成功！\n\n请检查数据库：\n1. tb_vip_order 表的订单记录\n2. tb_user 表的用户角色更新\n3. tb_wallet_transaction 表的交易记录",
          showCancel: false
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/vip-complete-test.vue:142", "完整流程测试失败:", error);
        this.addTestResult("完整流程", `测试失败：${error.message}`, false);
      }
    },
    // 测试VIP充值页面
    testVipRecharge() {
      common_vendor.index.navigateTo({
        url: "/pages/vip/recharge"
      });
    },
    // 测试创建订单
    async testCreateOrder() {
      try {
        const response = await api_vip.createVipOrder({ vipType: 1 });
        if (response.code === 200) {
          this.currentOrder = response.data;
          this.addTestResult("创建订单", `订单创建成功，金额：¥${response.data.payAmount}`, true);
        } else {
          this.addTestResult("创建订单", response.message || "创建失败", false);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/vip-complete-test.vue:165", "创建订单失败:", error);
        this.addTestResult("创建订单", "网络错误", false);
      }
    },
    // 测试支付流程
    async testPayment() {
      if (!this.currentOrder) {
        this.addTestResult("支付测试", "请先创建订单", false);
        return;
      }
      try {
        const response = await api_vip.processVipPayment({
          orderId: this.currentOrder.orderId,
          payType: 2
          // 支付宝
        });
        if (response.code === 200) {
          this.addTestResult("支付测试", `支付成功，交易号：${response.data.transactionId}`, true);
          await this.checkVipStatus();
        } else {
          this.addTestResult("支付测试", response.message || "支付失败", false);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/vip-complete-test.vue:190", "支付测试失败:", error);
        this.addTestResult("支付测试", "网络错误", false);
      }
    },
    // 添加测试结果
    addTestResult(title, message, success) {
      this.testResults.unshift({
        title,
        message,
        success,
        time: (/* @__PURE__ */ new Date()).toLocaleTimeString()
      });
      if (this.testResults.length > 10) {
        this.testResults.pop();
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString)
        return "无";
      const date = new Date(dateString);
      return date.toLocaleString("zh-CN");
    },
    // 获取角色样式类
    getRoleClass(userRole) {
      switch (userRole) {
        case 2:
          return "vip";
        case 3:
          return "matchmaker";
        case 4:
          return "vip-matchmaker";
        default:
          return "normal";
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.vipStatus
  }, $data.vipStatus ? common_vendor.e({
    b: common_vendor.t($data.vipStatus.userRoleName),
    c: common_vendor.n($options.getRoleClass($data.vipStatus.userRole)),
    d: common_vendor.t($data.vipStatus.isVip ? "已开通" : "未开通"),
    e: common_vendor.n($data.vipStatus.isVip ? "active" : "inactive"),
    f: $data.vipStatus.vipExpireTime
  }, $data.vipStatus.vipExpireTime ? {
    g: common_vendor.t($options.formatDate($data.vipStatus.vipExpireTime))
  } : {}) : {}, {
    h: common_vendor.o((...args) => $options.checkVipStatus && $options.checkVipStatus(...args)),
    i: common_vendor.o((...args) => $options.testCompleteFlow && $options.testCompleteFlow(...args)),
    j: common_vendor.o((...args) => $options.testVipRecharge && $options.testVipRecharge(...args)),
    k: common_vendor.o((...args) => $options.testCreateOrder && $options.testCreateOrder(...args)),
    l: common_vendor.o((...args) => $options.testPayment && $options.testPayment(...args)),
    m: $data.testResults.length > 0
  }, $data.testResults.length > 0 ? {
    n: common_vendor.f($data.testResults, (result, index, i0) => {
      return {
        a: common_vendor.t(result.title),
        b: common_vendor.t(result.message),
        c: common_vendor.t(result.time),
        d: index,
        e: common_vendor.n(result.success ? "success" : "error")
      };
    })
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-34437ccf"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/vip-complete-test.js.map
