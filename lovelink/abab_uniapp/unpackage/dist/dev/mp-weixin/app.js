"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
const api_http = require("./api/http.js");
const utils_errorHandler = require("./utils/error-handler.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/square/square.js";
  "./pages/message/message.js";
  "./pages/mine/mine.js";
  "./pages/wallet/wallet.js";
  "./pages/login/login.js";
  "./pages/register/register.js";
  "./pages/register/user-info.js";
  "./pages/profile/edit-profile.js";
  "./pages/message/chat.js";
  "./pages/test/chat-test.js";
  "./pages/test/user-detail-test.js";
  "./pages/test/scroll-test.js";
  "./pages/test/navigation-test.js";
  "./pages/test/scroll-error-test.js";
  "./pages/test/tabbar-test.js";
  "./pages/user/user-detail.js";
  "./pages/moment/publish.js";
  "./pages/moment/list.js";
  "./pages/moment/detail.js";
  "./pages/user/fans-list.js";
  "./pages/user/following-list.js";
  "./pages/voucher/voucher-list.js";
  "./pages/sign/sign.js";
  "./pages/game/heart-match.js";
  "./pages/game/match-result.js";
  "./pages/vip/recharge.js";
  "./pages/vip/success.js";
  "./pages/vip/order-detail.js";
  "./pages/test/vip-test.js";
  "./pages/test/vip-complete-test.js";
  "./pages/matchmaker/apply.js";
  "./pages/profile/verify.js";
  "./pages/mine/album.js";
  "./pages/test/album-test.js";
}
const _sfc_main = {
  onLaunch: function() {
    common_vendor.index.__f__("log", "at App.vue:4", "App Launch");
  },
  onShow: function() {
    common_vendor.index.__f__("log", "at App.vue:8", "App Show");
  },
  onHide: function() {
    common_vendor.index.__f__("log", "at App.vue:11", "App Hide");
  }
};
const uniPopup = () => "./components/common/uni-popup.js";
const customTabBar = () => "./components/custom-tab-bar.js";
function createApp() {
  const app = common_vendor.createSSRApp(_sfc_main);
  try {
    utils_errorHandler.initGlobalErrorHandler();
    if (true) {
      utils_errorHandler.monitorScrollTopSetting();
    }
    app.config.errorHandler = utils_errorHandler.createVueErrorHandler();
  } catch (error) {
    common_vendor.index.__f__("warn", "at main.js:23", "初始化错误处理器失败:", error);
  }
  app.component("uni-popup", uniPopup);
  app.component("custom-tab-bar", customTabBar);
  app.config.globalProperties.$baseUrl = "http://localhost:9001";
  app.config.globalProperties.$http = api_http.http;
  return {
    app
  };
}
createApp().app.mount("#app");
exports.createApp = createApp;
//# sourceMappingURL=../.sourcemap/mp-weixin/app.js.map
// app.js 中定义
App({
  globalData: {
    API_BASE_URL: 'https://wxe1200795a6ad4318'
  }
});
// 页面中通过 getApp() 获取
const app = getApp();
console.log(app.globalData.API_BASE_URL);
