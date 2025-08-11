"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      matchResult: {
        userId: null,
        nickname: "",
        avatarUrl: "",
        gender: 1,
        age: 0
      },
      heartRate: 0
    };
  },
  onLoad(options) {
    if (options.result) {
      try {
        this.matchResult = JSON.parse(decodeURIComponent(options.result));
        common_vendor.index.__f__("log", "at pages/game/match-result.vue:109", "匹配结果:", this.matchResult);
        this.animateHeartRate();
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/game/match-result.vue:114", "解析匹配结果失败:", error);
        common_vendor.index.showToast({
          title: "数据解析失败",
          icon: "none"
        });
        this.goBack();
      }
    } else {
      common_vendor.index.__f__("log", "at pages/game/match-result.vue:123", "使用测试数据");
      this.matchResult = {
        userId: "test123",
        nickname: "阿巴阿巴",
        avatarUrl: "/static/message/default-avatar.png",
        gender: 2,
        age: 25
      };
      this.animateHeartRate();
    }
  },
  methods: {
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 动画显示心动指数
    animateHeartRate() {
      const targetRate = Math.floor(Math.random() * 26) + 70;
      let currentRate = 0;
      const increment = targetRate / 30;
      const timer = setInterval(() => {
        currentRate += increment;
        if (currentRate >= targetRate) {
          currentRate = targetRate;
          clearInterval(timer);
        }
        this.heartRate = Math.floor(currentRate);
      }, 50);
    },
    // 查看用户资料
    viewProfile() {
      if (this.matchResult.userId) {
        common_vendor.index.navigateTo({
          url: `/pages/user/user-detail?userId=${this.matchResult.userId}`
        });
      }
    },
    // 发送消息
    async sendMessage() {
      if (!this.matchResult.userId) {
        common_vendor.index.showToast({
          title: "用户信息错误",
          icon: "none"
        });
        return;
      }
      try {
        const chatApi = await "../../api/chat.js";
        const result = await chatApi.canChat(this.matchResult.userId);
        if (result.code === 200) {
          if (result.data === true) {
            common_vendor.index.navigateTo({
              url: `/pages/message/chat?userId=${this.matchResult.userId}&name=${encodeURIComponent(this.matchResult.nickname)}&avatar=${encodeURIComponent(this.matchResult.avatarUrl || "/static/message/default-avatar.png")}`
            });
          } else {
            common_vendor.index.showModal({
              title: "提示",
              content: "需要互相关注才能聊天哦～",
              showCancel: false,
              confirmText: "知道了"
            });
          }
        } else {
          common_vendor.index.showToast({
            title: result.message || "检查聊天权限失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/game/match-result.vue:207", "检查聊天权限失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      }
    },
    // 再次匹配
    rematch() {
      common_vendor.index.navigateBack({
        delta: 1
      });
    },
    // 获取显示年龄
    getDisplayAge() {
      if (this.matchResult.age && this.matchResult.age > 0) {
        return this.matchResult.age;
      }
      return "未知";
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: $data.matchResult.avatarUrl || "/static/message/default-avatar.png",
    c: common_vendor.t($data.matchResult.nickname),
    d: common_vendor.t($options.getDisplayAge()),
    e: common_vendor.t($data.matchResult.gender === 1 ? "♂" : "♀"),
    f: $data.heartRate + "%",
    g: common_vendor.t($data.heartRate),
    h: common_vendor.o((...args) => $options.viewProfile && $options.viewProfile(...args)),
    i: common_vendor.o((...args) => $options.sendMessage && $options.sendMessage(...args)),
    j: common_vendor.o((...args) => $options.rematch && $options.rematch(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-c4cdc761"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/game/match-result.js.map
