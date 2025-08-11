"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {
      testUserId: "1"
    };
  },
  methods: {
    // 跳转到用户详情页
    goToUserDetail() {
      if (!this.testUserId) {
        common_vendor.index.showToast({
          title: "请输入用户ID",
          icon: "none"
        });
        return;
      }
      common_vendor.index.__f__("log", "at pages/test/user-detail-test.vue:60", "跳转到用户详情页，userId:", this.testUserId);
      common_vendor.index.navigateTo({
        url: `/pages/user/user-detail?userId=${this.testUserId}`
      });
    },
    // 测试用户API
    async testUserAPI() {
      var _a;
      if (!this.testUserId) {
        common_vendor.index.showToast({
          title: "请输入用户ID",
          icon: "none"
        });
        return;
      }
      try {
        const userDetailAPI = await "../../api/user/detail.js";
        common_vendor.index.__f__("log", "at pages/test/user-detail-test.vue:80", "开始测试用户API，userId:", this.testUserId);
        try {
          const result = await userDetailAPI.getUserDetail(this.testUserId);
          common_vendor.index.__f__("log", "at pages/test/user-detail-test.vue:85", "getUserDetail 结果:", result);
          if (result.code === 200 && result.data && result.data.records) {
            const targetUser = result.data.records.find((user) => user.userId == this.testUserId);
            if (targetUser) {
              common_vendor.index.showModal({
                title: "用户详情获取成功",
                content: `用户: ${targetUser.nickname}
年龄: ${((_a = targetUser.userProfile) == null ? void 0 : _a.age) || "未知"}
性别: ${targetUser.gender === 1 ? "男" : targetUser.gender === 2 ? "女" : "未知"}`,
                showCancel: false
              });
            } else {
              common_vendor.index.showModal({
                title: "未找到用户",
                content: `在返回的 ${result.data.records.length} 条记录中未找到用户ID ${this.testUserId}`,
                showCancel: false
              });
            }
          } else {
            common_vendor.index.showModal({
              title: "API调用失败",
              content: `错误码: ${result.code}
错误信息: ${result.message || "未知错误"}`,
              showCancel: false
            });
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/test/user-detail-test.vue:110", "getUserDetail 错误:", error);
          common_vendor.index.showModal({
            title: "网络错误",
            content: error.message || "请求失败",
            showCancel: false
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/user-detail-test.vue:119", "测试API失败:", error);
        common_vendor.index.showToast({
          title: "测试失败: " + error.message,
          icon: "none"
        });
      }
    },
    // 模拟匹配结果跳转
    simulateMatchToDetail() {
      const mockResult = {
        userId: this.testUserId || "1",
        nickname: "测试用户",
        avatarUrl: "/static/message/default-avatar.png",
        gender: 2,
        age: 25
      };
      common_vendor.index.navigateTo({
        url: `/pages/game/match-result?result=${encodeURIComponent(JSON.stringify(mockResult))}`
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: $data.testUserId,
    b: common_vendor.o(($event) => $data.testUserId = $event.detail.value),
    c: common_vendor.o((...args) => $options.goToUserDetail && $options.goToUserDetail(...args)),
    d: common_vendor.o((...args) => $options.testUserAPI && $options.testUserAPI(...args)),
    e: common_vendor.o((...args) => $options.simulateMatchToDetail && $options.simulateMatchToDetail(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-76123399"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/user-detail-test.js.map
