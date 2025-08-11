"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user_follow = require("../../api/user/follow.js");
const api_user_detail = require("../../api/user/detail.js");
const _sfc_main = {
  data() {
    return {
      followingList: [],
      loading: false
    };
  },
  onLoad() {
    this.loadFollowingList();
  },
  onPullDownRefresh() {
    this.loadFollowingList().finally(() => {
      common_vendor.index.stopPullDownRefresh();
    });
  },
  methods: {
    /**
     * 加载关注列表
     */
    async loadFollowingList() {
      try {
        this.loading = true;
        const result = await api_user_follow.getFollowingList();
        if (result.code === 200) {
          this.followingList = result.data || [];
        } else {
          common_vendor.index.showToast({
            title: result.message || "获取关注列表失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/following-list.vue:93", "获取关注列表失败:", error);
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
    async handleFollow(following, index) {
      try {
        this.$set(this.followingList[index], "loading", true);
        const result = await api_user_detail.toggleUserFollow(following.user.userId);
        if (result.code === 200) {
          if (following.isFollow === 1) {
            this.followingList[index].isFollow = 0;
          } else if (following.isFollow === 2) {
            this.followingList[index].isFollow = 0;
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
        common_vendor.index.__f__("error", "at pages/user/following-list.vue:132", "关注操作失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      } finally {
        this.$set(this.followingList[index], "loading", false);
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
    a: $data.followingList.length === 0 && !$data.loading
  }, $data.followingList.length === 0 && !$data.loading ? {} : {
    b: common_vendor.f($data.followingList, (following, index, i0) => {
      var _a, _b, _c, _d;
      return {
        a: ((_a = following.user) == null ? void 0 : _a.avatarUrl) || "/static/message/default-avatar.png",
        b: common_vendor.t(((_b = following.user) == null ? void 0 : _b.nickname) || "用户" + ((_c = following.user) == null ? void 0 : _c.userId)),
        c: common_vendor.t($options.getGenderText((_d = following.user) == null ? void 0 : _d.gender)),
        d: common_vendor.t($options.getFollowButtonText(following.isFollow)),
        e: common_vendor.n($options.getFollowButtonClass(following.isFollow)),
        f: common_vendor.o(($event) => $options.handleFollow(following, index), following.followId || index),
        g: following.loading,
        h: following.followId || index,
        i: common_vendor.o(($event) => $options.goToUserDetail(following.user), following.followId || index)
      };
    })
  }, {
    c: $data.loading
  }, $data.loading ? {} : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-602739a9"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/following-list.js.map
