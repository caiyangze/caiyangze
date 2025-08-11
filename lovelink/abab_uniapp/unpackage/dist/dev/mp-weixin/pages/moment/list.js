"use strict";
const common_vendor = require("../../common/vendor.js");
const api_moment = require("../../api/moment.js");
const pageSize = 10;
const _sfc_main = {
  __name: "list",
  setup(__props) {
    const momentList = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const refreshing = common_vendor.ref(false);
    const noMore = common_vendor.ref(false);
    const currentPage = common_vendor.ref(1);
    const listType = common_vendor.ref("public");
    common_vendor.onLoad((options) => {
      if (options.type === "mine") {
        listType.value = "mine";
      }
    });
    common_vendor.onMounted(() => {
      loadMomentList();
    });
    async function loadMomentList(isRefresh = false) {
      if (loading.value)
        return;
      loading.value = true;
      try {
        const page = isRefresh ? 1 : currentPage.value;
        let result;
        if (listType.value === "mine") {
          result = await api_moment.default.getMyMoments(page, pageSize);
        } else {
          result = await api_moment.default.getPublicMoments(page, pageSize);
        }
        if (result.code === 200) {
          const records = result.data.records || [];
          if (isRefresh) {
            momentList.value = records;
            currentPage.value = 1;
            noMore.value = false;
          } else {
            momentList.value.push(...records);
          }
          if (records.length < pageSize) {
            noMore.value = true;
          } else {
            currentPage.value++;
          }
        } else {
          throw new Error(result.message || "加载失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/moment/list.vue:177", "加载动态列表失败:", error);
        common_vendor.index.showToast({
          title: error.message || "加载失败",
          icon: "none"
        });
      } finally {
        loading.value = false;
        refreshing.value = false;
      }
    }
    function onRefresh() {
      refreshing.value = true;
      loadMomentList(true);
    }
    function loadMore() {
      if (!noMore.value) {
        loadMomentList();
      }
    }
    function formatTime(timeStr) {
      const time = new Date(timeStr);
      const now = /* @__PURE__ */ new Date();
      const diff = now - time;
      if (diff < 6e4)
        return "刚刚";
      if (diff < 36e5)
        return Math.floor(diff / 6e4) + "分钟前";
      if (diff < 864e5)
        return Math.floor(diff / 36e5) + "小时前";
      return Math.floor(diff / 864e5) + "天前";
    }
    function previewImages(mediaList, currentIndex) {
      const urls = mediaList.map((media) => media.mediaUrl);
      common_vendor.index.previewImage({
        current: currentIndex,
        urls
      });
    }
    function goToPublish() {
      common_vendor.index.navigateTo({
        url: "/pages/moment/publish"
      });
    }
    function goBack() {
      common_vendor.index.navigateBack();
    }
    function viewMomentDetail(moment) {
      common_vendor.index.__f__("log", "at pages/moment/list.vue:237", "查看动态详情:", moment);
    }
    function showMoreOptions(moment) {
      common_vendor.index.showActionSheet({
        itemList: ["删除"],
        success: (res) => {
          if (res.tapIndex === 0) {
            deleteMoment(moment);
          }
        }
      });
    }
    async function deleteMoment(moment) {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这条动态吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await api_moment.default.deleteMoment(moment.momentId);
              if (result.code === 200) {
                const index = momentList.value.findIndex((m) => m.momentId === moment.momentId);
                if (index > -1) {
                  momentList.value.splice(index, 1);
                }
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
              } else {
                throw new Error(result.message || "删除失败");
              }
            } catch (error) {
              common_vendor.index.showToast({
                title: error.message || "删除失败",
                icon: "none"
              });
            }
          }
        }
      });
    }
    function toggleLike(moment) {
      common_vendor.index.__f__("log", "at pages/moment/list.vue:288", "点赞:", moment);
    }
    function showComments(moment) {
      common_vendor.index.__f__("log", "at pages/moment/list.vue:294", "查看评论:", moment);
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: common_vendor.t(listType.value === "mine" ? "我的动态" : "动态广场"),
        c: common_vendor.o(goToPublish),
        d: common_vendor.f(momentList.value, (moment, k0, i0) => {
          return common_vendor.e({
            a: moment.avatarUrl || "/static/default-avatar.png",
            b: common_vendor.t(moment.nickname || "匿名用户"),
            c: common_vendor.t(formatTime(moment.createdAt)),
            d: moment.isMine
          }, moment.isMine ? {
            e: common_vendor.o(($event) => showMoreOptions(moment), moment.momentId)
          } : {}, {
            f: common_vendor.t(moment.content),
            g: moment.location
          }, moment.location ? {
            h: common_vendor.t(moment.location)
          } : {}, {
            i: moment.mediaList && moment.mediaList.length > 0
          }, moment.mediaList && moment.mediaList.length > 0 ? {
            j: common_vendor.f(moment.mediaList, (media, index, i1) => {
              return {
                a: media.mediaId,
                b: media.mediaUrl,
                c: common_vendor.o(($event) => previewImages(moment.mediaList, index), media.mediaId)
              };
            })
          } : {}, {
            k: common_vendor.n(moment.isLiked ? "liked" : ""),
            l: common_vendor.t(moment.likeCount || 0),
            m: common_vendor.o(($event) => toggleLike(moment), moment.momentId),
            n: common_vendor.t(moment.commentCount || 0),
            o: common_vendor.o(($event) => showComments(moment), moment.momentId),
            p: common_vendor.t(moment.viewCount || 0),
            q: moment.momentId,
            r: common_vendor.o(($event) => viewMomentDetail(moment), moment.momentId)
          });
        }),
        e: loading.value
      }, loading.value ? {} : {}, {
        f: noMore.value && momentList.value.length > 0
      }, noMore.value && momentList.value.length > 0 ? {} : {}, {
        g: momentList.value.length === 0 && !loading.value
      }, momentList.value.length === 0 && !loading.value ? {} : {}, {
        h: common_vendor.o(loadMore),
        i: common_vendor.o(onRefresh),
        j: refreshing.value,
        k: common_vendor.o(goToPublish)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-02091260"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/moment/list.js.map
