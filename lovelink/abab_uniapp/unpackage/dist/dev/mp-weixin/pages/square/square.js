"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
require("../../api/http.js");
const api_wallet = require("../../api/wallet.js");
const utils_provinces = require("../../utils/provinces.js");
const api_user_auth = require("../../api/user/auth.js");
if (!Array) {
  const _component_uni_popup = common_vendor.resolveComponent("uni-popup");
  _component_uni_popup();
}
if (!Math) {
  customTabBar();
}
const customTabBar = () => "../../components/custom-tab-bar.js";
const indicatorStyle = "height: 80rpx;";
const _sfc_main = {
  __name: "square",
  setup(__props) {
    const imageError = common_vendor.reactive({
      coin: false
    });
    const userCurrency = common_vendor.ref(0);
    const regionPopup = common_vendor.ref(null);
    const regions = utils_provinces.CHINA_PROVINCES.map((province) => utils_provinces.getProvinceShortName(province));
    const regionIndex = common_vendor.ref([0]);
    const selectedRegion = common_vendor.ref("北京");
    const tempRegion = common_vendor.ref("");
    const currentTabIndex = common_vendor.ref(0);
    const filterTabs = [
      { name: "推荐", type: "recommend" },
      { name: "附近", type: "nearby" },
      { name: "新人", type: "new" },
      { name: "活跃", type: "active" },
      { name: "动态广场", type: "moment" }
    ];
    const users = common_vendor.ref([]);
    const pagination = common_vendor.reactive({
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      hasMore: true
    });
    const momentList = common_vendor.ref([]);
    const momentRefreshing = common_vendor.ref(false);
    const momentPagination = common_vendor.reactive({
      pageNum: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      hasMore: true
    });
    function calculateAge(birthDate) {
      if (!birthDate)
        return null;
      const birth = new Date(birthDate);
      const today = /* @__PURE__ */ new Date();
      let age = today.getFullYear() - birth.getFullYear();
      const monthDiff = today.getMonth() - birth.getMonth();
      if (monthDiff < 0 || monthDiff === 0 && today.getDate() < birth.getDate()) {
        age--;
      }
      return age > 0 ? age : null;
    }
    function getUserAge(user) {
      if (user.userProfile && user.userProfile.age) {
        return user.userProfile.age;
      }
      if (user.birthDate) {
        const calculatedAge = calculateAge(user.birthDate);
        if (calculatedAge) {
          return calculatedAge;
        }
      }
      return "未知";
    }
    function getUserIntroduction(user) {
      if (user.userProfile && user.userProfile.selfIntroduction) {
        return user.userProfile.selfIntroduction;
      }
      return "这个人很神秘，没有留下介绍";
    }
    function getUserTags(user) {
      if (user.userTags && user.userTags.length > 0) {
        return user.userTags;
      }
      return [{ tagName: "暂无标签" }];
    }
    async function loadUsers(type = "recommend", refresh = false) {
      try {
        if (refresh) {
          pagination.pageNum = 1;
          pagination.hasMore = true;
        }
        if (!pagination.hasMore && !refresh) {
          return;
        }
        pagination.loading = true;
        const squareApi = await "../../api/square.js";
        const result = await squareApi.default.getUserList(pagination.pageNum, pagination.pageSize);
        if (result.code === 200) {
          pagination.total = result.data.total;
          pagination.hasMore = pagination.pageNum * pagination.pageSize < pagination.total;
          const newUsers = result.data.records.map((user) => ({
            id: user.userId,
            name: user.nickname || "匿名用户",
            age: getUserAge(user),
            selfIntroduction: getUserIntroduction(user),
            avatar: user.avatarUrl || "/static/default-avatar.png",
            tags: getUserTags(user),
            isLiked: false
          }));
          if (refresh) {
            users.value = newUsers;
          } else {
            users.value = users.value.concat(newUsers);
          }
          pagination.pageNum++;
          common_vendor.index.__f__("log", "at pages/square/square.vue:365", "用户列表获取成功:", users.value);
        } else {
          common_vendor.index.__f__("error", "at pages/square/square.vue:367", "用户列表获取失败:", result);
          if (pagination.pageNum === 1) {
            setDefaultUsers();
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:374", "用户列表获取异常:", error);
        if (pagination.pageNum === 1) {
          setDefaultUsers();
        }
      } finally {
        pagination.loading = false;
        if (refresh) {
          common_vendor.index.stopPullDownRefresh();
        }
      }
    }
    function setDefaultUsers() {
      users.value = [
        {
          id: 1,
          name: "小雨",
          age: 26,
          tags: ["旅行", "摄影", "美食"],
          intro: "喜欢旅行，热爱生活，期待遇见有趣的灵魂",
          avatar: "/static/square/avatar1.png",
          isLiked: false
        },
        {
          id: 2,
          name: "阳阳",
          age: 28,
          tags: ["工程师", "健身", "电影"],
          intro: "工作认真负责，生活热情洋溢，喜欢健身和看电影",
          avatar: "/static/square/avatar2.png",
          isLiked: false
        },
        {
          id: 3,
          name: "小米",
          age: 25,
          tags: ["教师", "阅读", "音乐"],
          intro: "温柔贤淑，喜欢阅读和音乐，希望遇到志同道合的人",
          avatar: "/static/square/avatar3.png",
          isLiked: false
        },
        {
          id: 4,
          name: "大壮",
          age: 30,
          tags: ["医生", "烹饪", "篮球"],
          intro: "稳重可靠，工作是医生，业余爱好烹饪和打篮球",
          avatar: "/static/square/avatar4.png",
          isLiked: false
        },
        {
          id: 5,
          name: "晓晓",
          age: 27,
          tags: ["设计师", "插画", "咖啡"],
          intro: "创意无限，热爱设计和插画，喜欢品尝各种咖啡",
          avatar: "/static/square/avatar5.png",
          isLiked: false
        },
        {
          id: 6,
          name: "明明",
          age: 29,
          tags: ["金融", "游泳", "钢琴"],
          intro: "从事金融行业，爱好游泳和弹钢琴，性格开朗",
          avatar: "/static/square/avatar6.png",
          isLiked: false
        }
      ];
    }
    function handleImageError(type) {
      imageError[type] = true;
    }
    function showRegionPicker() {
      tempRegion.value = selectedRegion.value;
      const index = regions.findIndex((item) => item === selectedRegion.value);
      if (index !== -1) {
        regionIndex.value = [index];
      }
      regionPopup.value.open();
    }
    function onRegionChange(e) {
      const index = e.detail.value[0];
      tempRegion.value = regions[index];
    }
    async function confirmRegionSelect() {
      selectedRegion.value = tempRegion.value;
      regionPopup.value.close();
      await updateUserProvinceInfo(selectedRegion.value);
    }
    function cancelRegionSelect() {
      regionPopup.value.close();
    }
    function switchTab(index) {
      currentTabIndex.value = index;
      if (filterTabs[index].type === "moment") {
        loadMomentList(true);
        return;
      }
      loadUsers(filterTabs[index].type, true);
    }
    common_vendor.onMounted(() => {
      loadUsers("recommend");
      getWalletData();
      loadUserInfo();
    });
    common_vendor.onShow(() => {
      getWalletData();
    });
    common_vendor.onPullDownRefresh(() => {
      if (filterTabs[currentTabIndex.value].type === "moment") {
        onMomentRefresh();
      } else {
        loadUsers(filterTabs[currentTabIndex.value].type, true);
      }
    });
    common_vendor.onReachBottom(() => {
      if (filterTabs[currentTabIndex.value].type === "moment") {
        if (!momentPagination.loading && momentPagination.hasMore) {
          loadMoreMoments();
        }
      } else {
        if (!pagination.loading && pagination.hasMore) {
          loadUsers(filterTabs[currentTabIndex.value].type);
        }
      }
    });
    function viewUserDetail(user) {
      common_vendor.index.navigateTo({
        url: `/pages/user/user-detail?id=${user.id}`
      });
    }
    function likeUser(user, index) {
      users.value[index].isLiked = !users.value[index].isLiked;
      if (users.value[index].isLiked) {
        common_vendor.index.showToast({
          title: "已喜欢",
          icon: "none"
        });
      }
    }
    function goSearch() {
      common_vendor.index.navigateTo({
        url: "/pages/search/search"
      });
    }
    async function getWalletData() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/square/square.vue:565", "未登录，无法获取钱包信息");
          return;
        }
        const response = await api_wallet.getWalletInfo();
        if (response.code === 200) {
          userCurrency.value = response.data.coinBalance || 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:574", "获取钱包信息失败:", error);
      }
    }
    function goToWallet() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        common_vendor.index.navigateTo({
          url: "/pages/login/login"
        });
        return;
      }
      common_vendor.index.navigateTo({
        url: "/pages/wallet/wallet"
      });
    }
    async function loadUserInfo() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/square/square.vue:602", "未登录，使用默认省份");
          return;
        }
        const result = await api_user_auth.getByUserInfo(token);
        if (result.code === 200 && result.data) {
          const userInfo = result.data;
          if (userInfo.lastLoginIp) {
            const provinceShortName = utils_provinces.getProvinceShortName(userInfo.lastLoginIp);
            selectedRegion.value = provinceShortName;
            common_vendor.index.__f__("log", "at pages/square/square.vue:613", "设置用户当前省份:", provinceShortName);
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:617", "获取用户信息失败:", error);
      }
    }
    async function updateUserProvinceInfo(province) {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const result = await api_user_auth.updateUserProvince(token, province);
        if (result.code === 200) {
          common_vendor.index.showToast({
            title: "省份更新成功",
            icon: "success"
          });
          common_vendor.index.__f__("log", "at pages/square/square.vue:639", "省份更新成功:", province);
        } else {
          common_vendor.index.showToast({
            title: result.message || "更新失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:647", "更新省份失败:", error);
        common_vendor.index.showToast({
          title: "更新失败，请重试",
          icon: "none"
        });
      }
    }
    async function loadMomentList(isRefresh = false) {
      if (momentPagination.loading)
        return;
      momentPagination.loading = true;
      try {
        const momentApi = await "../../api/moment.js";
        const page = isRefresh ? 1 : momentPagination.pageNum;
        const result = await momentApi.default.getPublicMoments(page, momentPagination.pageSize);
        if (result.code === 200) {
          const records = result.data.records || [];
          if (isRefresh) {
            momentList.value = records;
            momentPagination.pageNum = 1;
            momentPagination.hasMore = true;
          } else {
            momentList.value.push(...records);
          }
          if (records.length < momentPagination.pageSize) {
            momentPagination.hasMore = false;
          } else {
            momentPagination.pageNum++;
          }
        } else {
          throw new Error(result.message || "加载失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:691", "加载动态列表失败:", error);
        common_vendor.index.showToast({
          title: error.message || "加载失败",
          icon: "none"
        });
      } finally {
        momentPagination.loading = false;
        momentRefreshing.value = false;
      }
    }
    function onMomentRefresh() {
      momentRefreshing.value = true;
      loadMomentList(true);
    }
    function loadMoreMoments() {
      if (!momentPagination.hasMore) {
        return;
      }
      loadMomentList();
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
    async function likeMoment(moment) {
      try {
        const momentApi = await "../../api/moment.js";
        const result = await momentApi.default.likeMoment(moment.momentId);
        if (result.code === 200) {
          moment.isLiked = !moment.isLiked;
          moment.likeCount = moment.isLiked ? (moment.likeCount || 0) + 1 : Math.max((moment.likeCount || 0) - 1, 0);
        } else {
          throw new Error(result.message || "操作失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:750", "点赞失败:", error);
        common_vendor.index.showToast({
          title: error.message || "操作失败",
          icon: "none"
        });
      }
    }
    function commentMoment(moment) {
      common_vendor.index.navigateTo({
        url: `/pages/moment/detail?id=${moment.momentId}&action=comment`
      });
    }
    async function goToMomentDetail(moment) {
      try {
        const momentApi = await "../../api/moment.js";
        await momentApi.default.incrementViewCount(moment.momentId);
        moment.viewCount = (moment.viewCount || 0) + 1;
        common_vendor.index.navigateTo({
          url: `/pages/moment/detail?id=${moment.momentId}`
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/square/square.vue:780", "跳转详情页面失败:", error);
        common_vendor.index.navigateTo({
          url: `/pages/moment/detail?id=${moment.momentId}`
        });
      }
    }
    function showMomentMenu(moment) {
      common_vendor.index.showActionSheet({
        itemList: ["举报", "不感兴趣", "屏蔽用户"],
        success: function(res) {
          switch (res.tapIndex) {
            case 0:
              reportMoment();
              break;
            case 1:
              markNotInterested();
              break;
            case 2:
              blockUser();
              break;
          }
        }
      });
    }
    function reportMoment(moment) {
      common_vendor.index.showToast({
        title: "举报成功",
        icon: "success"
      });
    }
    function markNotInterested(moment) {
      common_vendor.index.showToast({
        title: "已标记",
        icon: "success"
      });
    }
    function blockUser(moment) {
      common_vendor.index.showToast({
        title: "已屏蔽",
        icon: "success"
      });
    }
    function getMediaGridClass(count) {
      if (count === 1)
        return "single";
      if (count === 2)
        return "double";
      if (count === 3)
        return "three";
      if (count === 4)
        return "four";
      if (count === 5)
        return "five";
      if (count === 6)
        return "six";
      if (count === 7)
        return "seven";
      if (count === 8)
        return "eight";
      return "nine";
    }
    function previewMomentMedia(mediaList, index) {
      const urls = mediaList.map((media) => media.mediaUrl);
      common_vendor.index.previewImage({
        urls,
        current: index
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(selectedRegion.value),
        b: common_vendor.o(showRegionPicker),
        c: common_vendor.o(goSearch),
        d: imageError.coin
      }, imageError.coin ? {} : {}, {
        e: common_assets._imports_0,
        f: common_vendor.o(($event) => handleImageError("coin")),
        g: common_vendor.t(userCurrency.value),
        h: common_vendor.o(goToWallet),
        i: common_vendor.f(filterTabs, (tab, index, i0) => {
          return {
            a: common_vendor.t(tab.name),
            b: index,
            c: currentTabIndex.value === index ? 1 : "",
            d: common_vendor.o(($event) => switchTab(index), index)
          };
        }),
        j: currentTabIndex.value !== 4
      }, currentTabIndex.value !== 4 ? common_vendor.e({
        k: common_vendor.f(users.value, (user, index, i0) => {
          return common_vendor.e({
            a: imageError[`user${index}`]
          }, imageError[`user${index}`] ? {} : {}, {
            b: user.avatar,
            c: common_vendor.o(($event) => handleImageError(`user${index}`), index),
            d: common_vendor.t(user.name),
            e: common_vendor.t(typeof user.age === "number" ? user.age + "岁" : user.age),
            f: common_vendor.f(user.tags, (tag, tagIndex, i1) => {
              return {
                a: common_vendor.t(tag.tagName || tag),
                b: tagIndex
              };
            }),
            g: common_vendor.t(user.selfIntroduction),
            h: user.isLiked ? 1 : "",
            i: common_vendor.o(($event) => likeUser(user, index), index),
            j: index,
            k: common_vendor.o(($event) => viewUserDetail(user), index)
          });
        }),
        l: pagination.loading
      }, pagination.loading ? {} : {}, {
        m: !pagination.hasMore && users.value.length > 0 && !pagination.loading
      }, !pagination.hasMore && users.value.length > 0 && !pagination.loading ? {} : {}, {
        n: users.value.length === 0 && !pagination.loading
      }, users.value.length === 0 && !pagination.loading ? {} : {}) : {}, {
        o: currentTabIndex.value === 4
      }, currentTabIndex.value === 4 ? common_vendor.e({
        p: common_vendor.f(momentList.value, (moment, k0, i0) => {
          return common_vendor.e({
            a: imageError[`avatar${moment.momentId}`]
          }, imageError[`avatar${moment.momentId}`] ? {} : {}, {
            b: moment.avatarUrl || "/static/default-avatar.png",
            c: common_vendor.o(($event) => handleImageError(`avatar${moment.momentId}`), moment.momentId),
            d: common_vendor.t(moment.nickname || "匿名用户"),
            e: common_vendor.t(formatTime(moment.createdAt)),
            f: common_vendor.o(($event) => showMomentMenu(), moment.momentId),
            g: moment.location
          }, moment.location ? {
            h: common_vendor.t(moment.location)
          } : {}, {
            i: moment.mediaList && moment.mediaList.length > 0
          }, moment.mediaList && moment.mediaList.length > 0 ? {
            j: common_vendor.f(moment.mediaList, (media, index, i1) => {
              return common_vendor.e({
                a: imageError[`media${media.mediaId}`]
              }, imageError[`media${media.mediaId}`] ? {} : {}, {
                b: media.mediaUrl,
                c: common_vendor.o(($event) => handleImageError(`media${media.mediaId}`), media.mediaId),
                d: media.mediaId,
                e: common_vendor.o(($event) => previewMomentMedia(moment.mediaList, index), media.mediaId)
              });
            }),
            k: common_vendor.n(getMediaGridClass(moment.mediaList.length))
          } : {}, {
            l: common_vendor.t(moment.content),
            m: moment.isLiked ? "/static/icons/heart-filled.png" : "/static/icons/heart.png",
            n: moment.isLiked ? 1 : "",
            o: common_vendor.t(moment.likeCount || 0),
            p: common_vendor.o(($event) => likeMoment(moment), moment.momentId),
            q: common_vendor.t(moment.commentCount || 0),
            r: common_vendor.o(($event) => commentMoment(moment), moment.momentId),
            s: common_vendor.t(moment.viewCount || 0),
            t: moment.momentId,
            v: common_vendor.o(($event) => goToMomentDetail(moment), moment.momentId)
          });
        }),
        q: common_assets._imports_1,
        r: common_assets._imports_0$1,
        s: common_assets._imports_1$1,
        t: momentPagination.loading
      }, momentPagination.loading ? {} : {}, {
        v: !momentPagination.hasMore && momentList.value.length > 0 && !momentPagination.loading
      }, !momentPagination.hasMore && momentList.value.length > 0 && !momentPagination.loading ? {} : {}, {
        w: momentList.value.length === 0 && !momentPagination.loading
      }, momentList.value.length === 0 && !momentPagination.loading ? {} : {}) : {}, {
        x: common_vendor.o(cancelRegionSelect),
        y: common_vendor.o(confirmRegionSelect),
        z: common_vendor.f(common_vendor.unref(regions), (item, index, i0) => {
          return {
            a: common_vendor.t(item),
            b: index
          };
        }),
        A: indicatorStyle,
        B: regionIndex.value,
        C: common_vendor.o(onRegionChange),
        D: common_vendor.sr(regionPopup, "6bc6c6b7-0", {
          "k": "regionPopup"
        }),
        E: common_vendor.p({
          type: "bottom"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-6bc6c6b7"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/square/square.js.map
