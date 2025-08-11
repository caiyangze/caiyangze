"use strict";
const common_vendor = require("../common/vendor.js");
const _sfc_main = {
  __name: "custom-tab-bar",
  setup(__props) {
    const active = common_vendor.ref(0);
    const list = [
      {
        pagePath: "/pages/index/index",
        text: "首页",
        iconType: "home"
      },
      {
        pagePath: "/pages/square/square",
        text: "广场",
        iconType: "square"
      },
      // 中间位置预留给发布按钮
      {
        pagePath: "/pages/message/message",
        text: "消息",
        iconType: "message"
      },
      {
        pagePath: "/pages/mine/mine",
        text: "我的",
        iconType: "mine"
      }
    ];
    const leftItems = common_vendor.computed(() => list.slice(0, 2));
    const rightItems = common_vendor.computed(() => list.slice(2, 4));
    function setActiveTab() {
      try {
        const pages = getCurrentPages();
        if (pages && pages.length > 0) {
          const currentPage = pages[pages.length - 1];
          if (currentPage && currentPage.route) {
            const currentRoute = "/" + currentPage.route;
            const index = list.findIndex((item) => item.pagePath === currentRoute);
            common_vendor.index.__f__("log", "at components/custom-tab-bar.vue:97", "当前路由:", currentRoute, "找到索引:", index);
            if (index !== -1) {
              active.value = index;
              common_vendor.index.__f__("log", "at components/custom-tab-bar.vue:100", "设置active为:", index);
            }
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at components/custom-tab-bar.vue:105", "设置激活标签页失败:", error);
        active.value = 0;
      }
    }
    function switchTab(url) {
      try {
        const pages = getCurrentPages();
        if (pages && pages.length > 0) {
          const currentPage = pages[pages.length - 1];
          if (currentPage && currentPage.route) {
            const currentRoute = "/" + currentPage.route;
            if (currentRoute !== url) {
              common_vendor.index.reLaunch({
                url
              });
            }
            return;
          }
        }
        common_vendor.index.reLaunch({
          url
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at components/custom-tab-bar.vue:136", "切换标签页失败:", error);
        common_vendor.index.reLaunch({
          url
        });
      }
    }
    function goToPublish() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.showModal({
            title: "提示",
            content: "请先登录后再发布动态",
            success: (res) => {
              if (res.confirm) {
                common_vendor.index.navigateTo({
                  url: "/pages/login/login"
                });
              }
            }
          });
          return;
        }
        common_vendor.index.navigateTo({
          url: "/pages/moment/publish"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at components/custom-tab-bar.vue:169", "跳转发布页面失败:", error);
        common_vendor.index.showToast({
          title: "跳转失败",
          icon: "none"
        });
      }
    }
    common_vendor.onMounted(() => {
      setActiveTab();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(leftItems.value, (item, index, i0) => {
          return {
            a: common_vendor.n(item.iconType),
            b: common_vendor.n({
              active: active.value === index
            }),
            c: common_vendor.t(item.text),
            d: active.value === index ? 1 : "",
            e: index,
            f: common_vendor.o(($event) => switchTab(item.pagePath), index)
          };
        }),
        b: common_vendor.o(goToPublish),
        c: common_vendor.f(rightItems.value, (item, index, i0) => {
          return {
            a: common_vendor.n(item.iconType),
            b: common_vendor.n({
              active: active.value === index + 2
            }),
            c: common_vendor.t(item.text),
            d: active.value === index + 2 ? 1 : "",
            e: index + 2,
            f: common_vendor.o(($event) => switchTab(item.pagePath), index + 2)
          };
        })
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-8fac706f"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../.sourcemap/mp-weixin/components/custom-tab-bar.js.map
