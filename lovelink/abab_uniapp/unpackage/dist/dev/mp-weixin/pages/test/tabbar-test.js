"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Math) {
  customTabBar();
}
const customTabBar = () => "../../components/custom-tab-bar.js";
const _sfc_main = {
  __name: "tabbar-test",
  setup(__props) {
    function testPublish() {
      common_vendor.index.showToast({
        title: "请点击底部发布按钮",
        icon: "none",
        duration: 2e3
      });
    }
    function checkLogin() {
      const token = common_vendor.index.getStorageSync("token");
      if (token) {
        common_vendor.index.showToast({
          title: "已登录",
          icon: "success"
        });
      } else {
        common_vendor.index.showToast({
          title: "未登录",
          icon: "none"
        });
      }
    }
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(testPublish),
        b: common_vendor.o(checkLogin)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d1f070a3"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/tabbar-test.js.map
