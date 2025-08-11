"use strict";
const common_vendor = require("../../common/vendor.js");
const api_http = require("../../api/http.js");
const api_wallet = require("../../api/wallet.js");
const VoucherSelector = () => "../../components/voucher-selector/voucher-selector.js";
const _sfc_main = {
  components: {
    VoucherSelector
  },
  data() {
    return {
      selectedGender: null,
      // 选中的性别 1-男 2-女
      isMatching: false,
      // 是否正在匹配中
      showCostConfirm: false,
      // 是否显示费用确认弹窗
      dontShowAgain: false,
      // 不再提示选项
      showSettings: false,
      // 是否显示设置弹窗
      currentBalance: 0,
      // 当前虚拟币余额
      loadingBalance: false,
      // 是否正在加载余额
      selectedVoucher: null
      // 选中的优惠券
    };
  },
  methods: {
    /**
     * 返回上一页
     */
    goBack() {
      common_vendor.index.navigateBack();
    },
    /**
     * 选择性别
     */
    selectGender(gender) {
      this.selectedGender = gender;
    },
    /**
     * 开始匹配
     */
    async startMatch() {
      if (!this.selectedGender) {
        common_vendor.index.showToast({
          title: "请先选择性别",
          icon: "none"
        });
        return;
      }
      const dontShowCostConfirm = common_vendor.index.getStorageSync("dontShowCostConfirm") || false;
      common_vendor.index.__f__("log", "at pages/game/heart-match.vue:219", "检查不再提示设置:", dontShowCostConfirm);
      if (!dontShowCostConfirm) {
        this.showCostConfirmDialog();
        return;
      }
      common_vendor.index.__f__("log", "at pages/game/heart-match.vue:228", "跳过费用确认，直接匹配");
      this.executeMatch();
    },
    /**
     * 显示费用确认弹窗
     */
    async showCostConfirmDialog() {
      this.showCostConfirm = true;
      await this.loadUserBalance();
    },
    /**
     * 关闭费用确认弹窗
     */
    closeCostConfirm() {
      this.showCostConfirm = false;
      this.dontShowAgain = false;
    },
    /**
     * 优惠券选择回调
     */
    onVoucherSelected(voucher) {
      this.selectedVoucher = voucher;
    },
    /**
     * 计算实际消费金额
     */
    getActualCost() {
      const originalCost = 5;
      if (this.selectedVoucher && this.selectedVoucher.voucher) {
        const discount = this.selectedVoucher.voucher.actualValue;
        return Math.max(0, originalCost - discount);
      }
      return originalCost;
    },
    /**
     * 确认费用并开始匹配
     */
    confirmCost() {
      const actualCost = this.getActualCost();
      if (this.currentBalance < actualCost) {
        common_vendor.index.showModal({
          title: "余额不足",
          content: `您的虚拟币余额为${this.currentBalance}币，需要${actualCost}币才能进行心动速配。是否前往充值？`,
          confirmText: "去充值",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.navigateTo({
                url: "/pages/wallet/wallet"
              });
            }
          }
        });
        return;
      }
      if (this.dontShowAgain) {
        common_vendor.index.__f__("log", "at pages/game/heart-match.vue:293", "保存不再提示设置");
        common_vendor.index.setStorageSync("dontShowCostConfirm", true);
      }
      this.showCostConfirm = false;
      this.dontShowAgain = false;
      this.executeMatch();
    },
    /**
     * 切换"不再提示"选项
     */
    toggleDontShowAgain() {
      this.dontShowAgain = !this.dontShowAgain;
    },
    /**
     * 加载用户余额
     */
    async loadUserBalance() {
      try {
        this.loadingBalance = true;
        const response = await api_wallet.getWalletInfo();
        if (response.code === 200) {
          this.currentBalance = response.data.coinBalance || 0;
          common_vendor.index.__f__("log", "at pages/game/heart-match.vue:319", "当前虚拟币余额:", this.currentBalance);
        } else {
          common_vendor.index.__f__("error", "at pages/game/heart-match.vue:321", "获取余额失败:", response.message);
          this.currentBalance = 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/game/heart-match.vue:325", "获取余额异常:", error);
        this.currentBalance = 0;
      } finally {
        this.loadingBalance = false;
      }
    },
    /**
     * 打开设置弹窗
     */
    openSettings() {
      const dontShowCostConfirm = common_vendor.index.getStorageSync("dontShowCostConfirm") || false;
      common_vendor.index.__f__("log", "at pages/game/heart-match.vue:337", "当前设置状态:", dontShowCostConfirm);
      if (dontShowCostConfirm) {
        common_vendor.index.showModal({
          title: "💰 费用提示设置",
          content: "您已设置跳过费用确认提示。\n\n是否重新启用费用确认提示？",
          confirmText: "启用提示",
          cancelText: "保持现状",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.removeStorageSync("dontShowCostConfirm");
              common_vendor.index.__f__("log", "at pages/game/heart-match.vue:348", "已清除不再提示设置");
              common_vendor.index.showToast({
                title: "✅ 已启用费用提示",
                icon: "success",
                duration: 2e3
              });
            }
          }
        });
      } else {
        common_vendor.index.showModal({
          title: "💰 费用提示设置",
          content: "当前已启用费用确认提示。\n\n每次心动速配前都会询问您是否确认消费5个虚拟币。",
          confirmText: "知道了",
          showCancel: false
        });
      }
    },
    /**
     * 执行匹配逻辑
     */
    async executeMatch() {
      this.isMatching = true;
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          common_vendor.index.navigateTo({
            url: "/pages/login/login"
          });
          return;
        }
        const consumeData = {
          coinAmount: 5,
          transactionDesc: "心动速配",
          relatedId: "HEART_MATCH_" + Date.now()
        };
        if (this.selectedVoucher) {
          consumeData.voucherOrderId = this.selectedVoucher.id;
          consumeData.voucherDiscountAmount = this.selectedVoucher.voucher.actualValue;
          common_vendor.index.__f__("log", "at pages/game/heart-match.vue:397", "使用优惠券消费，优惠券订单ID：", this.selectedVoucher.id, "抵扣金额：", this.selectedVoucher.voucher.actualValue);
        }
        common_vendor.index.__f__("log", "at pages/game/heart-match.vue:400", "消费请求数据：", consumeData);
        const consumeResponse = await api_wallet.consume(consumeData);
        if (consumeResponse.code !== 200) {
          throw new Error(consumeResponse.message || "虚拟币扣减失败");
        }
        const response = await api_http.http.post("/user/heartMatch", {
          gender: this.selectedGender,
          skipWalletDeduction: true
          // 告诉后端跳过钱包扣减，因为前端已经处理了
        }, {
          headers: {
            "token": token
          }
        });
        if (response.code === 200) {
          const matchResult = response.data;
          common_vendor.index.navigateTo({
            url: `/pages/game/match-result?result=${encodeURIComponent(JSON.stringify(matchResult))}`
          });
        } else {
          const errorMessage = response.message || "匹配失败";
          if (errorMessage.includes("余额不足") || errorMessage.includes("虚拟币")) {
            common_vendor.index.showModal({
              title: "余额不足",
              content: "您的虚拟币余额不足，需要5个虚拟币才能进行心动速配。是否前往充值？",
              confirmText: "去充值",
              cancelText: "取消",
              success: (res) => {
                if (res.confirm) {
                  common_vendor.index.navigateTo({
                    url: "/pages/wallet/wallet"
                  });
                }
              }
            });
          } else {
            common_vendor.index.showToast({
              title: errorMessage,
              icon: "none"
            });
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/game/heart-match.vue:457", "匹配失败:", error);
        common_vendor.index.showToast({
          title: "匹配失败，请重试",
          icon: "none"
        });
      } finally {
        this.isMatching = false;
      }
    }
  }
};
if (!Array) {
  const _easycom_voucher_selector2 = common_vendor.resolveComponent("voucher-selector");
  _easycom_voucher_selector2();
}
const _easycom_voucher_selector = () => "../../components/voucher-selector/voucher-selector.js";
if (!Math) {
  _easycom_voucher_selector();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: common_vendor.o((...args) => $options.openSettings && $options.openSettings(...args)),
    c: $data.selectedGender === 1 ? 1 : "",
    d: common_vendor.o(($event) => $options.selectGender(1)),
    e: $data.selectedGender === 2 ? 1 : "",
    f: common_vendor.o(($event) => $options.selectGender(2)),
    g: !$data.isMatching
  }, !$data.isMatching ? {} : {}, {
    h: !$data.selectedGender ? 1 : "",
    i: !$data.selectedGender || $data.isMatching,
    j: common_vendor.o((...args) => $options.startMatch && $options.startMatch(...args)),
    k: $data.showCostConfirm
  }, $data.showCostConfirm ? common_vendor.e({
    l: common_vendor.o((...args) => $options.closeCostConfirm && $options.closeCostConfirm(...args)),
    m: common_vendor.t($data.loadingBalance ? "加载中..." : $data.currentBalance + "币"),
    n: $data.currentBalance < 5 ? 1 : "",
    o: common_vendor.sr("voucherSelector", "39127fa0-0"),
    p: common_vendor.o($options.onVoucherSelected),
    q: common_vendor.p({
      ["consume-amount"]: 5,
      ["consume-type"]: "heart-match"
    }),
    r: $data.selectedVoucher
  }, $data.selectedVoucher ? {
    s: common_vendor.t($data.selectedVoucher.voucher.actualValue)
  } : {}, {
    t: common_vendor.t($options.getActualCost()),
    v: common_vendor.t($data.loadingBalance ? "计算中..." : $data.currentBalance >= $options.getActualCost() ? $data.currentBalance - $options.getActualCost() + "币" : "余额不足"),
    w: $data.currentBalance < $options.getActualCost() ? 1 : "",
    x: $data.dontShowAgain
  }, $data.dontShowAgain ? {} : {}, {
    y: $data.dontShowAgain ? 1 : "",
    z: common_vendor.o((...args) => $options.toggleDontShowAgain && $options.toggleDontShowAgain(...args)),
    A: common_vendor.o((...args) => $options.closeCostConfirm && $options.closeCostConfirm(...args)),
    B: common_vendor.t($data.currentBalance < $options.getActualCost() ? "余额不足" : $data.loadingBalance ? "加载中..." : "开始匹配"),
    C: $data.currentBalance < $options.getActualCost() || $data.loadingBalance ? 1 : "",
    D: $data.currentBalance < $options.getActualCost() || $data.loadingBalance,
    E: common_vendor.o((...args) => $options.confirmCost && $options.confirmCost(...args)),
    F: common_vendor.o(() => {
    }),
    G: common_vendor.o((...args) => $options.closeCostConfirm && $options.closeCostConfirm(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-39127fa0"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/game/heart-match.js.map
