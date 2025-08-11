"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user_detail = require("../../api/user/detail.js");
const _sfc_main = {
  __name: "detail",
  setup(__props) {
    const momentDetail = common_vendor.ref(null);
    const commentList = common_vendor.ref([]);
    const commentText = common_vendor.ref("");
    const commentInputFocus = common_vendor.ref(false);
    const momentId = common_vendor.ref("");
    const systemInfo = common_vendor.ref({});
    const keyboardHeight = common_vendor.ref(0);
    const isFollowed = common_vendor.ref(false);
    const isCurrentUser = common_vendor.ref(false);
    const followLoading = common_vendor.ref(false);
    const contentHeight = common_vendor.computed(() => {
      const windowHeight = systemInfo.value.windowHeight || 667;
      const navbarHeight = 88;
      const commentInputHeight = 100;
      return windowHeight - navbarHeight - commentInputHeight;
    });
    common_vendor.onLoad((options) => {
      common_vendor.index.__f__("log", "at pages/moment/detail.vue:180", "页面加载参数:", options);
      momentId.value = options.id;
      if (!momentId.value) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:183", "缺少动态ID参数");
        common_vendor.index.showToast({
          title: "参数错误",
          icon: "error"
        });
        return;
      }
      if (options.action === "comment") {
        setTimeout(() => {
          commentInputFocus.value = true;
        }, 500);
      }
      initPage();
    });
    common_vendor.onMounted(() => {
      common_vendor.index.getSystemInfo({
        success: (res) => {
          systemInfo.value = res;
        }
      });
    });
    async function initPage() {
      await loadMomentDetail();
      await loadCommentList();
    }
    async function loadMomentDetail() {
      try {
        const momentApi = await "../../api/moment.js";
        const result = await momentApi.default.getMomentDetail(momentId.value);
        if (result.code === 200) {
          momentDetail.value = result.data;
          common_vendor.index.__f__("log", "at pages/moment/detail.vue:221", "动态详情数据:", result.data);
          checkCurrentUser();
          if (!isCurrentUser.value) {
            await checkFollowStatus();
          }
          await momentApi.default.incrementViewCount(momentId.value);
        } else {
          common_vendor.index.__f__("error", "at pages/moment/detail.vue:234", "获取动态详情失败:", result.message);
          common_vendor.index.showToast({
            title: result.message || "获取失败",
            icon: "error"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:241", "加载动态详情失败:", error);
        common_vendor.index.showToast({
          title: "网络错误",
          icon: "error"
        });
      }
    }
    async function loadCommentList() {
      var _a, _b;
      try {
        const momentApi = await "../../api/moment.js";
        const result = await momentApi.default.getCommentList(momentId.value);
        if (result.code === 200) {
          commentList.value = ((_a = result.data) == null ? void 0 : _a.records) || [];
          common_vendor.index.__f__("log", "at pages/moment/detail.vue:257", "评论列表数据:", result.data);
          common_vendor.index.__f__("log", "at pages/moment/detail.vue:258", "评论记录:", (_b = result.data) == null ? void 0 : _b.records);
        } else {
          common_vendor.index.__f__("error", "at pages/moment/detail.vue:260", "获取评论列表失败:", result.message);
          commentList.value = [];
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:264", "加载评论列表失败:", error);
        commentList.value = [];
      }
    }
    function goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        common_vendor.index.navigateBack();
      } else {
        common_vendor.index.reLaunch({
          url: "/pages/index/index"
        });
      }
    }
    async function likeMoment() {
      try {
        const momentApi = await "../../api/moment.js";
        const result = await momentApi.default.likeMoment(momentId.value);
        if (result.code === 200) {
          momentDetail.value.isLiked = !momentDetail.value.isLiked;
          momentDetail.value.likeCount = momentDetail.value.isLiked ? (momentDetail.value.likeCount || 0) + 1 : Math.max((momentDetail.value.likeCount || 0) - 1, 0);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:295", "点赞失败:", error);
        common_vendor.index.showToast({
          title: "操作失败",
          icon: "error"
        });
      }
    }
    function focusCommentInput() {
      commentInputFocus.value = true;
    }
    function onInputFocus() {
      commentInputFocus.value = true;
      if (common_vendor.index.onKeyboardHeightChange && typeof common_vendor.index.onKeyboardHeightChange === "function") {
        common_vendor.index.onKeyboardHeightChange((res) => {
          keyboardHeight.value = res.height;
        });
      } else {
        keyboardHeight.value = 300;
      }
    }
    function onInputBlur() {
      commentInputFocus.value = false;
      keyboardHeight.value = 0;
    }
    async function submitComment() {
      if (!commentText.value.trim()) {
        common_vendor.index.showToast({
          title: "请输入评论内容",
          icon: "none"
        });
        return;
      }
      try {
        const momentApi = await "../../api/moment.js";
        const result = await momentApi.default.addComment(momentId.value, commentText.value.trim());
        if (result.code === 200) {
          commentText.value = "";
          commentInputFocus.value = false;
          keyboardHeight.value = 0;
          await loadCommentList();
          if (momentDetail.value) {
            momentDetail.value.commentCount = (momentDetail.value.commentCount || 0) + 1;
          }
          common_vendor.index.showToast({
            title: "评论成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.pageScrollTo({
              selector: ".comment-section",
              duration: 300
            });
          }, 100);
        } else {
          common_vendor.index.showToast({
            title: result.message || "评论失败",
            icon: "error"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:373", "评论失败:", error);
        let errorMessage = "网络错误";
        if (error && error.data && error.data.message) {
          errorMessage = error.data.message;
        } else if (error && error.message) {
          errorMessage = error.message;
        } else if (typeof error === "string") {
          errorMessage = error;
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "error"
        });
      }
    }
    function onAvatarError(e) {
      common_vendor.index.__f__("log", "at pages/moment/detail.vue:393", "头像加载失败:", e);
      e.target.src = "/static/default-avatar.svg";
    }
    function onCommentAvatarError(e) {
      common_vendor.index.__f__("log", "at pages/moment/detail.vue:399", "评论头像加载失败:", e);
      e.target.src = "/static/default-avatar.svg";
    }
    function previewImages(index) {
      const urls = momentDetail.value.mediaList.map((media) => media.mediaUrl);
      common_vendor.index.previewImage({
        urls,
        current: index
      });
    }
    async function handleFollow() {
      if (followLoading.value || !momentDetail.value)
        return;
      try {
        followLoading.value = true;
        const result = await api_user_detail.toggleUserFollow(momentDetail.value.userId);
        if (result.code === 200) {
          isFollowed.value = !isFollowed.value;
          common_vendor.index.showToast({
            title: result.message || (isFollowed.value ? "已关注" : "取消关注"),
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "操作失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:433", "关注操作失败:", error);
        let errorMessage = "网络错误，请重试";
        if (error && error.data && error.data.message) {
          errorMessage = error.data.message;
        } else if (error && error.message) {
          errorMessage = error.message;
        } else if (typeof error === "string") {
          errorMessage = error;
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none"
        });
      } finally {
        followLoading.value = false;
      }
    }
    async function checkFollowStatus() {
      if (!momentDetail.value || isCurrentUser.value)
        return;
      try {
        const result = await api_user_detail.checkUserFollow(momentDetail.value.userId);
        if (result.code === 200) {
          isFollowed.value = result.data || false;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:463", "检查关注状态失败:", error);
      }
    }
    function checkCurrentUser() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token || !momentDetail.value) {
        isCurrentUser.value = false;
        return;
      }
      try {
        const currentUserId = common_vendor.index.getStorageSync("userId");
        isCurrentUser.value = currentUserId && currentUserId == momentDetail.value.userId;
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/detail.vue:480", "检查当前用户失败:", error);
        isCurrentUser.value = false;
      }
    }
    function formatTime(timestamp) {
      if (!timestamp)
        return "";
      const date = new Date(timestamp);
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      if (diff < 6e4)
        return "刚刚";
      if (diff < 36e5)
        return Math.floor(diff / 6e4) + "分钟前";
      if (diff < 864e5)
        return Math.floor(diff / 36e5) + "小时前";
      if (diff < 6048e5)
        return Math.floor(diff / 864e5) + "天前";
      return date.toLocaleDateString();
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: momentDetail.value
      }, momentDetail.value ? common_vendor.e({
        c: momentDetail.value.avatarUrl || "/static/default-avatar.svg",
        d: common_vendor.o(onAvatarError),
        e: common_vendor.t(momentDetail.value.nickname || "匿名用户"),
        f: common_vendor.t(formatTime(momentDetail.value.publishTime)),
        g: !isCurrentUser.value
      }, !isCurrentUser.value ? {
        h: common_vendor.t(isFollowed.value ? "已关注" : "关注"),
        i: common_vendor.o(handleFollow)
      } : {}, {
        j: momentDetail.value.content
      }, momentDetail.value.content ? {
        k: common_vendor.t(momentDetail.value.content)
      } : {}, {
        l: momentDetail.value.location
      }, momentDetail.value.location ? {
        m: common_vendor.t(momentDetail.value.location)
      } : {}, {
        n: momentDetail.value.mediaList && momentDetail.value.mediaList.length > 0
      }, momentDetail.value.mediaList && momentDetail.value.mediaList.length > 0 ? {
        o: common_vendor.f(momentDetail.value.mediaList, (media, index, i0) => {
          return common_vendor.e({
            a: !media.mediaUrl
          }, !media.mediaUrl ? {} : {}, {
            b: media.mediaId,
            c: media.mediaUrl,
            d: common_vendor.o(($event) => previewImages(index), media.mediaId)
          });
        })
      } : {}, {
        p: momentDetail.value.isLiked ? "/static/icons/heart-filled.png" : "/static/icons/heart.png",
        q: momentDetail.value.isLiked ? 1 : "",
        r: common_vendor.t(momentDetail.value.likeCount || 0),
        s: common_vendor.o(likeMoment),
        t: common_assets._imports_0$1,
        v: common_vendor.t(commentList.value.length),
        w: common_vendor.o(focusCommentInput),
        x: common_assets._imports_1$1,
        y: common_vendor.t(momentDetail.value.viewCount || 0)
      }) : {}, {
        z: common_vendor.t(commentList.value.length),
        A: commentList.value.length === 0
      }, commentList.value.length === 0 ? {} : {
        B: common_vendor.f(commentList.value, (comment, k0, i0) => {
          return {
            a: comment.avatarUrl || "/static/default-avatar.svg",
            b: common_vendor.o(onCommentAvatarError, comment.commentId),
            c: common_vendor.t(comment.nickname || "匿名用户"),
            d: common_vendor.t(formatTime(comment.createdAt)),
            e: common_vendor.t(comment.content),
            f: comment.commentId
          };
        })
      }, {
        C: contentHeight.value + "px",
        D: commentInputFocus.value,
        E: common_vendor.o(onInputBlur),
        F: common_vendor.o(onInputFocus),
        G: common_vendor.o(submitComment),
        H: commentText.value,
        I: common_vendor.o(($event) => commentText.value = $event.detail.value),
        J: commentText.value.trim() ? 1 : "",
        K: common_vendor.o(submitComment),
        L: keyboardHeight.value > 0 ? 1 : "",
        M: keyboardHeight.value + "px"
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-07aa6715"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/moment/detail.js.map
