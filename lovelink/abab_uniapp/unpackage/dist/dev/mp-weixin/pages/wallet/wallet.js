"use strict";
const common_vendor = require("../../common/vendor.js");
const api_wallet = require("../../api/wallet.js");
const _sfc_main = {
  name: "Wallet",
  data() {
    return {
      walletInfo: {},
      walletStatus: 1,
      transactions: [],
      loading: false,
      refreshing: false,
      currentPage: 1,
      pageSize: 20,
      hasMore: true,
      // 筛选选项
      typeIndex: 0,
      typeOptions: ["全部", "充值", "消费", "收入", "提现", "退款"],
      // 充值相关
      rechargePackages: [],
      selectedPackage: -1,
      selectedPayment: 0,
      paymentMethods: [
        { id: 1, name: "支付宝" },
        { id: 2, name: "微信支付" },
        { id: 3, name: "银行卡" }
      ],
      // 提现相关
      withdrawForm: {
        amount: "",
        account: "",
        name: ""
      },
      withdrawTypeIndex: 0,
      withdrawTypes: ["支付宝", "微信支付", "银行卡"]
    };
  },
  onLoad() {
    this.initData();
  },
  onPullDownRefresh() {
    this.refreshData();
  },
  methods: {
    async initData() {
      await this.loadWalletInfo();
      await this.loadTransactions();
      this.loadRechargePackages();
    },
    async refreshData() {
      this.currentPage = 1;
      this.transactions = [];
      this.hasMore = true;
      await this.initData();
      common_vendor.index.stopPullDownRefresh();
      this.refreshing = false;
    },
    async onRefresh() {
      this.refreshing = true;
      await this.refreshData();
    },
    async loadWalletInfo() {
      try {
        const res = await api_wallet.getWalletInfo();
        if (res.code === 200) {
          this.walletInfo = res.data;
          this.walletStatus = res.data.walletStatus;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/wallet/wallet.vue:249", "获取钱包信息失败:", error);
        common_vendor.index.showToast({
          title: "获取钱包信息失败",
          icon: "none"
        });
      }
    },
    async loadTransactions() {
      if (this.loading || !this.hasMore)
        return;
      this.loading = true;
      try {
        const type = this.typeIndex === 0 ? null : this.typeIndex;
        common_vendor.index.__f__("log", "at pages/wallet/wallet.vue:263", "正在加载交易记录...", { page: this.currentPage, type });
        const res = await api_wallet.getTransactions(this.currentPage, this.pageSize, type);
        common_vendor.index.__f__("log", "at pages/wallet/wallet.vue:266", "交易记录API响应:", res);
        if (res.code === 200) {
          const newTransactions = res.data.records || [];
          common_vendor.index.__f__("log", "at pages/wallet/wallet.vue:269", "获取到交易记录:", newTransactions.length, "条");
          if (this.currentPage === 1) {
            this.transactions = newTransactions;
          } else {
            this.transactions.push(...newTransactions);
          }
          this.hasMore = newTransactions.length === this.pageSize;
          this.currentPage++;
        } else {
          common_vendor.index.__f__("warn", "at pages/wallet/wallet.vue:280", "交易记录API返回错误:", res.message);
          common_vendor.index.showToast({
            title: res.message || "获取交易记录失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/wallet/wallet.vue:287", "获取交易记录失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请稍后重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    loadMoreTransactions() {
      this.loadTransactions();
    },
    loadRechargePackages() {
      this.rechargePackages = api_wallet.getRechargePackages();
    },
    onTypeChange(e) {
      this.typeIndex = e.detail.value;
      this.currentPage = 1;
      this.transactions = [];
      this.hasMore = true;
      this.loadTransactions();
    },
    showRechargeModal() {
      this.$refs.rechargePopup.open();
    },
    hideRechargeModal() {
      this.$refs.rechargePopup.close();
      this.selectedPackage = -1;
    },
    showWithdrawModal() {
      if (this.walletInfo.cashBalance <= 0) {
        common_vendor.index.showToast({
          title: "现金余额不足",
          icon: "none"
        });
        return;
      }
      this.$refs.withdrawPopup.open();
    },
    hideWithdrawModal() {
      this.$refs.withdrawPopup.close();
      this.withdrawForm = { amount: "", account: "", name: "" };
    },
    selectPackage(index) {
      this.selectedPackage = index;
    },
    selectPayment(index) {
      this.selectedPayment = index;
    },
    async confirmRecharge() {
      if (this.selectedPackage === -1) {
        common_vendor.index.showToast({
          title: "请选择充值套餐",
          icon: "none"
        });
        return;
      }
      const pkg = this.rechargePackages[this.selectedPackage];
      const paymentMethod = this.paymentMethods[this.selectedPayment];
      try {
        const res = await api_wallet.recharge({
          coinAmount: pkg.coinAmount + (pkg.bonus || 0),
          transactionDesc: `充值${pkg.coinAmount}虚拟币`,
          paymentMethod: paymentMethod.id
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "充值成功",
            icon: "success"
          });
          this.hideRechargeModal();
          this.refreshData();
        } else {
          common_vendor.index.showToast({
            title: res.message || "充值失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/wallet/wallet.vue:379", "充值失败:", error);
        common_vendor.index.showToast({
          title: "充值失败",
          icon: "none"
        });
      }
    },
    onWithdrawTypeChange(e) {
      this.withdrawTypeIndex = e.detail.value;
    },
    async confirmWithdraw() {
      const { amount, account, name } = this.withdrawForm;
      if (!amount || amount < 10) {
        common_vendor.index.showToast({
          title: "提现金额不能少于10元",
          icon: "none"
        });
        return;
      }
      if (!account || !name) {
        common_vendor.index.showToast({
          title: "请填写完整的账户信息",
          icon: "none"
        });
        return;
      }
      if (parseFloat(amount) > parseFloat(this.walletInfo.cashBalance)) {
        common_vendor.index.showToast({
          title: "提现金额不能超过余额",
          icon: "none"
        });
        return;
      }
      try {
        const res = await api_wallet.withdraw({
          transactionAmount: parseFloat(amount),
          withdrawAccount: account,
          withdrawAccountType: this.withdrawTypeIndex + 1,
          withdrawAccountName: name,
          transactionDesc: "现金提现"
        });
        if (res.code === 200) {
          common_vendor.index.showToast({
            title: "提现申请成功",
            icon: "success"
          });
          this.hideWithdrawModal();
          this.refreshData();
        } else {
          common_vendor.index.showToast({
            title: res.message || "提现申请失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/wallet/wallet.vue:441", "提现申请失败:", error);
        common_vendor.index.showToast({
          title: "提现申请失败",
          icon: "none"
        });
      }
    },
    formatAmount(amount) {
      return api_wallet.formatAmount(amount, "元").replace("元", "");
    },
    formatTime(time) {
      if (!time)
        return "";
      const date = new Date(time);
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, "0")}`;
    },
    getAmountClass(type) {
      return type === 1 || type === 3 ? "amount-positive" : "amount-negative";
    },
    getAmountText(item) {
      const isPositive = item.transactionType === 1 || item.transactionType === 3;
      const prefix = isPositive ? "+" : "-";
      if (item.coinAmount) {
        return `${prefix}${item.coinAmount}币`;
      } else if (item.transactionAmount) {
        return `${prefix}¥${this.formatAmount(item.transactionAmount)}`;
      }
      return "";
    }
  }
};
if (!Array) {
  const _component_uni_popup = common_vendor.resolveComponent("uni-popup");
  _component_uni_popup();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($data.walletStatus === 1 ? "正常" : "冻结"),
    b: common_vendor.n($data.walletStatus === 1 ? "status-normal" : "status-frozen"),
    c: common_vendor.t($data.walletInfo.coinBalance || 0),
    d: common_vendor.t($options.formatAmount($data.walletInfo.cashBalance)),
    e: common_vendor.o((...args) => $options.showRechargeModal && $options.showRechargeModal(...args)),
    f: common_vendor.o((...args) => $options.showWithdrawModal && $options.showWithdrawModal(...args)),
    g: common_vendor.t($data.walletInfo.totalRecharge || 0),
    h: common_vendor.t($data.walletInfo.totalConsume || 0),
    i: common_vendor.t($options.formatAmount($data.walletInfo.totalIncome)),
    j: common_vendor.t($options.formatAmount($data.walletInfo.totalWithdraw)),
    k: common_vendor.t($data.typeOptions[$data.typeIndex]),
    l: common_vendor.o((...args) => $options.onTypeChange && $options.onTypeChange(...args)),
    m: $data.typeIndex,
    n: $data.typeOptions,
    o: common_vendor.f($data.transactions, (item, index, i0) => {
      return {
        a: common_vendor.t(item.transactionDesc),
        b: common_vendor.t($options.formatTime(item.createdAt)),
        c: common_vendor.t($options.getAmountText(item)),
        d: common_vendor.n($options.getAmountClass(item.transactionType)),
        e: index
      };
    }),
    p: $data.transactions.length === 0 && !$data.loading
  }, $data.transactions.length === 0 && !$data.loading ? {
    q: common_vendor.o((...args) => $options.refreshData && $options.refreshData(...args))
  } : {}, {
    r: $data.loading
  }, $data.loading ? {} : {}, {
    s: $data.hasMore && $data.transactions.length > 0
  }, $data.hasMore && $data.transactions.length > 0 ? {
    t: common_vendor.o((...args) => $options.loadMoreTransactions && $options.loadMoreTransactions(...args))
  } : {}, {
    v: common_vendor.o((...args) => $options.hideRechargeModal && $options.hideRechargeModal(...args)),
    w: common_vendor.f($data.rechargePackages, (pkg, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(pkg.coinAmount),
        b: common_vendor.t(pkg.price),
        c: pkg.bonus > 0
      }, pkg.bonus > 0 ? {
        d: common_vendor.t(pkg.bonus)
      } : {}, {
        e: index,
        f: common_vendor.n($data.selectedPackage === index ? "selected" : ""),
        g: common_vendor.o(($event) => $options.selectPackage(index), index)
      });
    }),
    x: common_vendor.f($data.paymentMethods, (method, index, i0) => {
      return {
        a: common_vendor.t(method.name),
        b: index,
        c: common_vendor.n($data.selectedPayment === index ? "selected" : ""),
        d: common_vendor.o(($event) => $options.selectPayment(index), index)
      };
    }),
    y: common_vendor.o((...args) => $options.confirmRecharge && $options.confirmRecharge(...args)),
    z: $data.selectedPackage === -1,
    A: common_vendor.sr("rechargePopup", "4c380209-0"),
    B: common_vendor.p({
      type: "bottom"
    }),
    C: common_vendor.o((...args) => $options.hideWithdrawModal && $options.hideWithdrawModal(...args)),
    D: $data.withdrawForm.amount,
    E: common_vendor.o(($event) => $data.withdrawForm.amount = $event.detail.value),
    F: common_vendor.t($data.withdrawTypes[$data.withdrawTypeIndex]),
    G: common_vendor.o((...args) => $options.onWithdrawTypeChange && $options.onWithdrawTypeChange(...args)),
    H: $data.withdrawTypeIndex,
    I: $data.withdrawTypes,
    J: $data.withdrawForm.account,
    K: common_vendor.o(($event) => $data.withdrawForm.account = $event.detail.value),
    L: $data.withdrawForm.name,
    M: common_vendor.o(($event) => $data.withdrawForm.name = $event.detail.value),
    N: common_vendor.o((...args) => $options.confirmWithdraw && $options.confirmWithdraw(...args)),
    O: common_vendor.sr("withdrawPopup", "4c380209-1"),
    P: common_vendor.p({
      type: "bottom"
    }),
    Q: common_vendor.o((...args) => $options.onRefresh && $options.onRefresh(...args)),
    R: $data.refreshing
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-4c380209"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/wallet/wallet.js.map
