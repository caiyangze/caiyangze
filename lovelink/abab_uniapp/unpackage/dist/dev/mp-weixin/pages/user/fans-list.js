"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user_follow = require("../../api/user/follow.js");
const api_user_detail = require("../../api/user/detail.js");
const _sfc_main = {
  data() {
    return {
      fansList: [],
      loading: false
    };
  },
  onLoad() {
    this.loadFansList();
  },
  onPullDownRefresh() {
    this.loadFansList().finally(() => {
      common_vendor.index.stopPullDownRefresh();
    });
  },
  methods: {
    /**
     * 加载粉丝列表
     */
    async loadFansList() {
      try {
        this.loading = true;
        const result = await api_user_follow.getFansList();
        if (result.code === 200) {
          this.fansList = result.data || [];
        } else {
          common_vendor.index.showToast({
            title: result.message || "获取粉丝列表失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/fans-list.vue:93", "获取粉丝列表失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        this.loading = false;
      }
    },
    /**
     * 处理关注操作
     */
    async handleFollow(fan, index) {
      try {
        this.$set(this.fansList[index], "loading", true);
        const result = await api_user_detail.toggleUserFollow(fan.user.userId);
        if (result.code === 200) {
          if (fan.isFollow === 0) {
            this.fansList[index].isFollow = 1;
          } else if (fan.isFollow === 1) {
            this.fansList[index].isFollow = 0;
          } else if (fan.isFollow === 2) {
            this.fansList[index].isFollow = 0;
          }
          common_vendor.index.showToast({
            title: result.message || "操作成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "操作失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/fans-list.vue:134", "关注操作失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        this.$set(this.fansList[index], "loading", false);
      }
    },
    /**
     * 跳转到用户详情页
     */
    goToUserDetail(user) {
      if (!user || !user.userId)
        return;
      common_vendor.index.navigateTo({
        url: `/pages/user/user-detail?userId=${user.userId}`
      });
    },
    /**
     * 获取性别文本
     */
    getGenderText(gender) {
      switch (gender) {
        case 1:
          return "男";
        case 2:
          return "女";
        default:
          return "未知";
      }
    },
    /**
     * 获取关注按钮样式类
     */
    getFollowButtonClass(isFollow) {
      const baseClass = "follow-btn";
      switch (isFollow) {
        case 0:
          return `${baseClass} follow-btn-unfollow`;
        case 1:
          return `${baseClass} follow-btn-following`;
        case 2:
          return `${baseClass} follow-btn-mutual`;
        default:
          return `${baseClass} follow-btn-unfollow`;
      }
    },
    /**
     * 获取关注按钮文本
     */
    getFollowButtonText(isFollow) {
      switch (isFollow) {
        case 0:
          return "关注";
        case 1:
          return "已关注";
        case 2:
          return "相互关注";
        default:
          return "关注";
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $data.fansList.length === 0 && !$data.loading
  }, $data.fansList.length === 0 && !$data.loading ? {} : {
    b: common_vendor.f($data.fansList, (fan, index, i0) => {
      var _a, _b, _c, _d;
      return {
        a: ((_a = fan.user) == null ? void 0 : _a.avatarUrl) || "/static/message/default-avatar.png",
        b: common_vendor.t(((_b = fan.user) == null ? void 0 : _b.nickname) || "用户" + ((_c = fan.user) == null ? void 0 : _c.userId)),
        c: common_vendor.t($options.getGenderText((_d = fan.user) == null ? void 0 : _d.gender)),
        d: common_vendor.t($options.getFollowButtonText(fan.isFollow)),
        e: common_vendor.n($options.getFollowButtonClass(fan.isFollow)),
        f: common_vendor.o(($event) => $options.handleFollow(fan, index), fan.followId || index),
        g: fan.loading,
        h: fan.followId || index,
        i: common_vendor.o(($event) => $options.goToUserDetail(fan.user), fan.followId || index)
      };
    })
  }, {
    c: $data.loading
  }, $data.loading ? {} : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-3b99b7d3"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/fans-list.js.map
