"use strict";
const common_vendor = require("../../common/vendor.js");
const api_voucher = require("../../api/voucher.js");
const _sfc_main = {
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: "可领取", key: "available" },
        { name: "我的券", key: "my" }
      ],
      voucherList: [],
      loading: false,
      refreshing: false,
      hasMore: true,
      pageNum: 1,
      pageSize: 10
    };
  },
  onLoad() {
    this.loadVoucherList();
  },
  methods: {
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 切换标签页
    switchTab(index) {
      this.currentTab = index;
      this.resetList();
      this.loadVoucherList();
    },
    // 重置列表
    resetList() {
      this.voucherList = [];
      this.pageNum = 1;
      this.hasMore = true;
    },
    // 加载优惠券列表
    async loadVoucherList() {
      if (this.loading)
        return;
      this.loading = true;
      try {
        let result;
        if (this.currentTab === 0) {
          result = await api_voucher.getVoucherList({
            pageNum: this.pageNum,
            pageSize: this.pageSize
          });
        } else {
          result = await api_voucher.getMyVoucherList({
            pageNum: this.pageNum,
            pageSize: this.pageSize
          });
        }
        if (result.code === 200) {
          const data = result.data;
          let records = data.records || data || [];
          if (this.currentTab === 1) {
            records = records.map((order) => {
              const voucher = order.voucher || {};
              return {
                ...voucher,
                // 添加订单相关信息
                orderId: order.id,
                // 保持原始ID（可能是字符串或数字）
                orderStatus: order.status,
                // 订单状态：1未支付，2已支付，3已核销，4已取消，5退款中，6已退款
                payTime: order.payTime,
                useTime: order.useTime,
                createTime: order.createTime,
                // 根据订单状态判断优惠券状态
                voucherStatus: this.getVoucherStatusFromOrder(order.status)
              };
            });
          }
          if (this.pageNum === 1) {
            this.voucherList = records;
          } else {
            this.voucherList.push(...records);
          }
          if (data.records) {
            this.hasMore = data.current < data.pages;
          } else {
            this.hasMore = records.length >= this.pageSize;
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/voucher/voucher-list.vue:208", "加载优惠券列表失败:", error);
        let errorMessage = "加载失败";
        if (error && error.message) {
          errorMessage = error.message;
        } else if (error && typeof error === "string") {
          errorMessage = error;
        } else if (error && error.data && error.data.message) {
          errorMessage = error.data.message;
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none"
        });
      } finally {
        this.loading = false;
        this.refreshing = false;
      }
    },
    // 下拉刷新
    onRefresh() {
      this.refreshing = true;
      this.resetList();
      this.loadVoucherList();
    },
    // 加载更多
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.pageNum++;
        this.loadVoucherList();
      }
    },
    // 处理优惠券操作
    async handleVoucherAction(voucher) {
      if (this.currentTab === 0) {
        await this.seckillVoucher(voucher);
      }
    },
    // 秒杀优惠券
    async seckillVoucher(voucher) {
      try {
        common_vendor.index.showLoading({ title: "领取中..." });
        const result = await api_voucher.seckillVoucher(voucher.id);
        if (result.code === 200) {
          common_vendor.index.showToast({
            title: "领取成功",
            icon: "success"
          });
          this.onRefresh();
        } else {
          common_vendor.index.showToast({
            title: result.message || "领取失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/voucher/voucher-list.vue:273", "领取优惠券失败:", error);
        let errorMessage = "领取失败";
        if (error && error.message) {
          errorMessage = error.message;
        } else if (error && typeof error === "string") {
          errorMessage = error;
        } else if (error && error.data && error.data.message) {
          errorMessage = error.data.message;
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none",
          duration: 3e3
          // 延长显示时间，让用户能看清楚
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    },
    // 获取优惠券样式类
    getVoucherClass(voucher) {
      const classes = [];
      if (voucher.type === 1) {
        classes.push("seckill-voucher");
      }
      if (this.isVoucherExpired(voucher)) {
        classes.push("expired");
      }
      if (this.isVoucherUsed(voucher)) {
        classes.push("used");
      }
      return classes.join(" ");
    },
    // 获取优惠券类型文本
    getVoucherTypeText(type) {
      const typeMap = {
        0: "普通券",
        1: "秒杀券",
        2: "代金券"
      };
      return typeMap[type] || "优惠券";
    },
    // 获取时间文本
    getTimeText(seckilVoucher) {
      if (!seckilVoucher)
        return "";
      const now = /* @__PURE__ */ new Date();
      const beginTime = new Date(seckilVoucher.beginTime);
      const endTime = new Date(seckilVoucher.endTime);
      if (now < beginTime) {
        return `${this.formatTime(beginTime)} 开始`;
      } else if (now > endTime) {
        return "已结束";
      } else {
        return `${this.formatTime(endTime)} 结束`;
      }
    },
    // 获取按钮样式类
    getBtnClass(voucher) {
      if (this.currentTab === 1) {
        if (voucher.voucherStatus) {
          switch (voucher.voucherStatus) {
            case "unused":
              return "available";
            case "used":
            case "cancelled":
            case "refunded":
              return "disabled";
            case "unpaid":
              return "warning";
            case "refunding":
              return "warning";
            default:
              return "disabled";
          }
        }
      }
      if (this.isVoucherDisabled(voucher)) {
        return "disabled";
      }
      if (voucher.type === 1) {
        return "seckill";
      }
      return "normal";
    },
    // 获取按钮文本
    getBtnText(voucher) {
      if (this.currentTab === 1) {
        if (voucher.voucherStatus) {
          switch (voucher.voucherStatus) {
            case "unpaid":
              return "未支付";
            case "unused":
              return "未使用";
            case "used":
              return "已使用";
            case "cancelled":
              return "已取消";
            case "refunding":
              return "退款中";
            case "refunded":
              return "已退款";
            default:
              return "未知状态";
          }
        }
        if (this.isVoucherUsed(voucher)) {
          return "已使用";
        } else if (this.isVoucherExpired(voucher)) {
          return "已过期";
        } else {
          return "未使用";
        }
      } else {
        if (this.isVoucherExpired(voucher)) {
          return "已过期";
        } else if (voucher.seckilVoucher && voucher.seckilVoucher.stock <= 0) {
          return "已抢完";
        } else {
          return voucher.type === 1 ? "立即抢购" : "立即领取";
        }
      }
    },
    // 判断优惠券是否禁用
    isVoucherDisabled(voucher) {
      if (this.currentTab === 1) {
        return true;
      }
      return this.isVoucherExpired(voucher) || voucher.seckilVoucher && voucher.seckilVoucher.stock <= 0;
    },
    // 判断优惠券是否过期
    isVoucherExpired(voucher) {
      if (!voucher.seckilVoucher)
        return false;
      const now = /* @__PURE__ */ new Date();
      const endTime = new Date(voucher.seckilVoucher.endTime);
      return now > endTime;
    },
    // 判断优惠券是否已使用
    isVoucherUsed(voucher) {
      if (this.currentTab === 1 && voucher.voucherStatus) {
        return voucher.voucherStatus === "used";
      }
      return voucher.status === 3;
    },
    // 根据订单状态获取优惠券状态
    getVoucherStatusFromOrder(orderStatus) {
      switch (orderStatus) {
        case 1:
          return "unpaid";
        case 2:
          return "unused";
        case 3:
          return "used";
        case 4:
          return "cancelled";
        case 5:
          return "refunding";
        case 6:
          return "refunded";
        default:
          return "unknown";
      }
    },
    // 格式化时间
    formatTime(time) {
      const date = new Date(time);
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return `${month}-${day} ${hours}:${minutes}`;
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: common_vendor.f($data.tabs, (tab, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(tab.name),
        b: $data.currentTab === index
      }, $data.currentTab === index ? {} : {}, {
        c: index,
        d: $data.currentTab === index ? 1 : "",
        e: common_vendor.o(($event) => $options.switchTab(index), index)
      });
    }),
    c: common_vendor.f($data.voucherList, (voucher, index, i0) => {
      return common_vendor.e({
        a: common_vendor.t(voucher.actualValue),
        b: common_vendor.t($options.getVoucherTypeText(voucher.type)),
        c: common_vendor.t(voucher.title),
        d: common_vendor.t(voucher.subTitle),
        e: voucher.seckilVoucher && $data.currentTab === 0
      }, voucher.seckilVoucher && $data.currentTab === 0 ? {
        f: common_vendor.t(voucher.seckilVoucher.stock),
        g: common_vendor.t($options.getTimeText(voucher.seckilVoucher))
      } : {}, $data.currentTab === 1 ? common_vendor.e({
        h: voucher.payTime
      }, voucher.payTime ? {
        i: common_vendor.t($options.formatTime(voucher.payTime))
      } : {}, {
        j: voucher.useTime
      }, voucher.useTime ? {
        k: common_vendor.t($options.formatTime(voucher.useTime))
      } : {}, {
        l: voucher.orderId
      }, voucher.orderId ? {
        m: common_vendor.t(voucher.orderId)
      } : {}) : {}, {
        n: common_vendor.t($options.getBtnText(voucher)),
        o: common_vendor.n($options.getBtnClass(voucher)),
        p: common_vendor.o(($event) => $options.handleVoucherAction(voucher), voucher.id),
        q: $options.isVoucherDisabled(voucher),
        r: voucher.id,
        s: common_vendor.n($options.getVoucherClass(voucher))
      });
    }),
    d: $data.currentTab === 1,
    e: $data.voucherList.length === 0 && !$data.loading
  }, $data.voucherList.length === 0 && !$data.loading ? {
    f: common_vendor.t($data.currentTab === 0 ? "暂时没有可领取的优惠券" : "您还没有优惠券")
  } : {}, {
    g: $data.loading
  }, $data.loading ? {} : {}, {
    h: $data.hasMore && $data.voucherList.length > 0
  }, $data.hasMore && $data.voucherList.length > 0 ? {} : {}, {
    i: common_vendor.o((...args) => $options.loadMore && $options.loadMore(...args)),
    j: common_vendor.o((...args) => $options.onRefresh && $options.onRefresh(...args)),
    k: $data.refreshing
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e625e266"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/voucher/voucher-list.js.map
