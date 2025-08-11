"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user_detail = require("../../api/user/detail.js");
const _sfc_main = {
  __name: "user-detail",
  setup(__props) {
    const userInfo = common_vendor.ref({
      nickname: "",
      gender: 0,
      avatarUrl: "",
      birthDate: null,
      fan: 0,
      countFollow: 0,
      countLike: 0,
      isVip: 0,
      isVerified: 0,
      isLiked: false,
      isFollowed: false,
      isOnline: false
    });
    const userProfile = common_vendor.ref({
      age: null,
      workCity: "",
      height: null,
      weight: null,
      education: null,
      company: "",
      position: "",
      incomeLevel: null,
      maritalStatus: null,
      hasChildren: null,
      houseStatus: null,
      carStatus: null,
      selfIntroduction: "",
      hobby: ""
    });
    const userTags = common_vendor.ref([]);
    const userPhotos = common_vendor.ref([]);
    const userMoments = common_vendor.ref([]);
    const photoErrors = common_vendor.reactive({});
    const statusBarHeight = common_vendor.ref(0);
    const currentTab = common_vendor.ref("photos");
    const loading = common_vendor.ref(false);
    const pageParams = common_vendor.ref({});
    common_vendor.onMounted(() => {
      var _a, _b;
      const sysInfo = common_vendor.index.getSystemInfoSync();
      statusBarHeight.value = sysInfo.statusBarHeight;
      const urlParams = new URLSearchParams(window.location.search);
      let userId = urlParams.get("userId") || urlParams.get("id");
      if (!userId) {
        try {
          const currentRoute = (_b = (_a = common_vendor.getCurrentInstance()) == null ? void 0 : _a.proxy) == null ? void 0 : _b.$route;
          if (currentRoute && currentRoute.query) {
            userId = currentRoute.query.userId || currentRoute.query.id;
          }
        } catch (e) {
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:373", "无法获取路由参数:", e);
        }
      }
      if (!userId) {
        userId = "1";
        common_vendor.index.__f__("warn", "at pages/user/user-detail.vue:380", "未获取到用户ID参数，使用默认用户ID:", userId);
      }
      pageParams.value = { userId };
      common_vendor.index.__f__("log", "at pages/user/user-detail.vue:384", "获取到的userId:", userId);
      if (userId) {
        getUserDetailData(userId);
      } else {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:389", "未获取到用户ID参数");
        common_vendor.index.showToast({
          title: "用户ID参数错误",
          icon: "none"
        });
      }
    });
    async function getUserDetailData(userId) {
      try {
        loading.value = true;
        let targetUser = null;
        const result = await api_user_detail.getUserDetail(userId);
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:405", "getUserDetail 响应:", result);
        if (result.code === 200 && result.data && result.data.records) {
          targetUser = result.data.records.find((user) => user.userId == userId);
          if (!targetUser && result.data.records.length > 0) {
            targetUser = result.data.records[0];
            common_vendor.index.__f__("warn", "at pages/user/user-detail.vue:414", "未找到指定用户ID的数据，使用第一个用户数据");
          }
        }
        if (targetUser) {
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:419", "获取到的用户数据:", targetUser);
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:420", "用户资料:", targetUser.userProfile);
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:421", "用户标签:", targetUser.userTags);
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:422", "用户点赞数 countLike:", targetUser.countLike);
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:423", "用户粉丝数 fan:", targetUser.fan);
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:424", "用户关注数 countFollow:", targetUser.countFollow);
          userInfo.value = {
            ...targetUser,
            isLiked: targetUser.isLiked || false,
            isFollowed: targetUser.isFollowed || false,
            isOnline: targetUser.isOnline || false,
            // 确保统计数据使用数据库的值
            countLike: targetUser.countLike || 0,
            fan: targetUser.fan || 0,
            countFollow: targetUser.countFollow || 0
          };
          userProfile.value = targetUser.userProfile || {};
          userTags.value = targetUser.userTags || [];
          await Promise.all([
            loadUserPhotos(userId),
            loadUserMoments(userId),
            loadFollowStatus(userId)
          ]);
        } else {
          throw new Error("未找到用户数据");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:450", "获取用户详情失败:", error);
        common_vendor.index.showToast({ title: "加载失败，请重试", icon: "none" });
        userInfo.value = {};
        userProfile.value = {};
        userPhotos.value = [];
        userMoments.value = [];
      } finally {
        loading.value = false;
      }
    }
    async function loadUserPhotos(userId) {
      try {
        const result = await api_user_detail.getUserPhotos(userId, 1, 20);
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:465", "用户相册响应:", result);
        if (result.code === 200) {
          if (result.data && result.data.records) {
            userPhotos.value = result.data.records || [];
          } else if (Array.isArray(result.data)) {
            userPhotos.value = result.data;
          } else {
            userPhotos.value = [];
          }
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:476", "用户相册数据:", userPhotos.value);
        } else {
          common_vendor.index.__f__("warn", "at pages/user/user-detail.vue:478", "获取用户相册失败:", result.message);
          userPhotos.value = [];
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:482", "获取用户相册失败:", error);
        userPhotos.value = [];
      }
    }
    async function loadUserMoments(userId) {
      try {
        const result = await api_user_detail.getUserMoments(userId, 1, 20);
        if (result.code === 200) {
          if (result.data && result.data.records) {
            userMoments.value = result.data.records || [];
          } else {
            userMoments.value = result.data || [];
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:500", "获取用户动态失败:", error);
        userMoments.value = [];
      }
    }
    async function loadFollowStatus(targetUserId) {
      try {
        const token = getToken();
        if (!token) {
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:511", "用户未登录，跳过关注状态检查");
          userInfo.value.isFollowed = false;
          return;
        }
        const result = await api_user_detail.checkUserFollow(targetUserId);
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:517", "关注状态响应:", result);
        if (result.code === 200) {
          userInfo.value.isFollowed = result.data || false;
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:521", "关注状态:", userInfo.value.isFollowed);
        } else if (result.code === 401) {
          common_vendor.index.__f__("log", "at pages/user/user-detail.vue:523", "用户未登录或token过期");
          userInfo.value.isFollowed = false;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:527", "获取关注状态失败:", error);
        userInfo.value.isFollowed = false;
      }
    }
    function calculateAge(birthDate) {
      if (!birthDate)
        return "未知";
      try {
        const birth = new Date(birthDate);
        if (isNaN(birth.getTime()))
          return "未知";
        const today = /* @__PURE__ */ new Date();
        let age = today.getFullYear() - birth.getFullYear();
        const monthDiff = today.getMonth() - birth.getMonth();
        if (monthDiff < 0 || monthDiff === 0 && today.getDate() < birth.getDate()) {
          age--;
        }
        return age > 0 ? age : "未知";
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:547", "计算年龄失败:", error);
        return "未知";
      }
    }
    function getDisplayAge() {
      try {
        if (userProfile.value && userProfile.value.age) {
          return userProfile.value.age;
        }
        if (userInfo.value && userInfo.value.birthDate) {
          return calculateAge(userInfo.value.birthDate);
        }
        return "未知";
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:563", "获取年龄失败:", error);
        return "未知";
      }
    }
    function getDisplayGender() {
      try {
        if (userInfo.value && userInfo.value.gender !== void 0) {
          switch (userInfo.value.gender) {
            case 1:
              return "男";
            case 2:
              return "女";
            default:
              return "";
          }
        }
        return "";
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:583", "获取性别失败:", error);
        return "";
      }
    }
    function getGenderIcon() {
      try {
        if (userInfo.value && userInfo.value.gender !== void 0) {
          switch (userInfo.value.gender) {
            case 1:
              return "♂";
            case 2:
              return "♀";
            default:
              return "";
          }
        }
        return "";
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:603", "获取性别图标失败:", error);
        return "";
      }
    }
    function getGenderAndAge() {
      try {
        const gender = getDisplayGender();
        const genderIcon = getGenderIcon();
        const age = getDisplayAge();
        let result = "";
        if (gender && genderIcon) {
          result += `${genderIcon}`;
        }
        if (age && age !== "未知") {
          if (result) {
            result += `${age}岁`;
          } else {
            result = `${age}岁`;
          }
        }
        return result || "信息未完善";
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:633", "获取性别年龄失败:", error);
        return "信息未完善";
      }
    }
    function switchTab(tab) {
      currentTab.value = tab;
    }
    async function toggleFollow() {
      try {
        const targetUserId = pageParams.value.userId || pageParams.value.id;
        const token = getToken();
        if (!token) {
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          return;
        }
        if (userInfo.value.isFollowed) {
          common_vendor.index.showModal({
            title: "取消关注",
            content: `确定要取消关注 ${userInfo.value.nickname || "该用户"} 吗？`,
            confirmText: "取消关注",
            cancelText: "我再想想",
            confirmColor: "#ff4757",
            success: async (res) => {
              if (res.confirm) {
                await performFollowAction(targetUserId);
              }
            }
          });
        } else {
          await performFollowAction(targetUserId);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:678", "关注操作失败:", error);
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      }
    }
    async function performFollowAction(targetUserId) {
      try {
        const result = await api_user_detail.toggleUserFollow(targetUserId);
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:687", "关注操作响应:", result);
        if (result.code === 200) {
          userInfo.value.isFollowed = !userInfo.value.isFollowed;
          if (userInfo.value.isFollowed) {
            userInfo.value.fan = (userInfo.value.fan || 0) + 1;
          } else {
            userInfo.value.fan = Math.max((userInfo.value.fan || 0) - 1, 0);
          }
          common_vendor.index.showToast({
            title: result.message || (userInfo.value.isFollowed ? "关注成功" : "取消关注成功"),
            icon: "success"
          });
          await refreshUserDetail(targetUserId);
        } else if (result.code === 401) {
          common_vendor.index.showToast({ title: "登录已过期，请重新登录", icon: "none" });
        } else {
          common_vendor.index.showToast({ title: result.message || "操作失败", icon: "none" });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:718", "关注操作失败:", error);
        common_vendor.index.showToast({ title: "操作失败", icon: "none" });
      }
    }
    async function refreshUserDetail(targetUserId) {
      try {
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:726", "刷新用户详情数据...");
        const result = await api_user_detail.getUserDetail(targetUserId);
        if (result.code === 200 && result.data && result.data.records) {
          const targetUser = result.data.records.find((user) => user.userId == targetUserId);
          if (targetUser) {
            const currentFollowStatus = userInfo.value.isFollowed;
            userInfo.value = {
              ...targetUser,
              isLiked: targetUser.isLiked || false,
              isFollowed: currentFollowStatus,
              // 保持当前关注状态
              isOnline: targetUser.isOnline || false,
              // 确保统计数据使用数据库的最新值
              countLike: targetUser.countLike || 0,
              fan: targetUser.fan || 0,
              countFollow: targetUser.countFollow || 0
            };
            common_vendor.index.__f__("log", "at pages/user/user-detail.vue:747", "用户详情数据刷新成功，最新粉丝数:", userInfo.value.fan);
            common_vendor.index.__f__("log", "at pages/user/user-detail.vue:748", "用户详情数据刷新成功，最新点赞数:", userInfo.value.countLike);
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:752", "刷新用户详情失败:", error);
      }
    }
    function goBack() {
      common_vendor.index.navigateBack();
    }
    function showMoreActions() {
      common_vendor.index.showActionSheet({
        itemList: ["举报", "拉黑"],
        success: (res) => {
          if (res.tapIndex === 0) {
            common_vendor.index.showToast({ title: "举报功能开发中", icon: "none" });
          } else if (res.tapIndex === 1) {
            common_vendor.index.showToast({ title: "拉黑功能开发中", icon: "none" });
          }
        }
      });
    }
    function previewPhoto(index) {
      const urls = userPhotos.value.map((photo) => photo.photoUrl);
      common_vendor.index.previewImage({
        current: index,
        urls
      });
    }
    function handlePhotoError(index) {
      photoErrors[index] = true;
    }
    function handleImageError() {
      userInfo.value.avatarUrl = "/static/common/default-avatar.png";
    }
    function formatTime(timestamp) {
      const date = new Date(timestamp);
      const now = /* @__PURE__ */ new Date();
      const diff = now - date;
      if (diff < 6e4)
        return "刚刚";
      if (diff < 36e5)
        return Math.floor(diff / 6e4) + "分钟前";
      if (diff < 864e5)
        return Math.floor(diff / 36e5) + "小时前";
      return Math.floor(diff / 864e5) + "天前";
    }
    function getMediaGridClass(count) {
      if (count === 1)
        return "single";
      if (count <= 4)
        return "quad";
      return "nine";
    }
    function previewMomentMedia(mediaList, index) {
      const urls = mediaList.map((media) => media.mediaUrl);
      common_vendor.index.previewImage({
        current: index,
        urls
      });
    }
    async function toggleMomentLike(moment) {
      try {
        moment.isLiked = !moment.isLiked;
        moment.likeCount = moment.isLiked ? (moment.likeCount || 0) + 1 : Math.max((moment.likeCount || 0) - 1, 0);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:831", "动态点赞失败:", error);
      }
    }
    function viewMomentComments() {
      common_vendor.index.showToast({ title: "评论功能开发中", icon: "none" });
    }
    async function sendMessage() {
      if (!userInfo.value.userId) {
        common_vendor.index.showToast({
          title: "用户信息错误",
          icon: "none"
        });
        return;
      }
      try {
        const chatApi = await "../../api/chat.js";
        const result = await chatApi.canChat(userInfo.value.userId);
        if (result.code === 200) {
          if (result.data === true) {
            common_vendor.index.navigateTo({
              url: `/pages/message/chat?userId=${userInfo.value.userId}&name=${encodeURIComponent(userInfo.value.nickname)}&avatar=${encodeURIComponent(userInfo.value.avatarUrl || "/static/message/default-avatar.png")}`
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
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:879", "检查聊天权限失败:", error);
        common_vendor.index.showToast({
          title: "网络错误，请重试",
          icon: "none"
        });
      }
    }
    function requestIntroduction() {
      common_vendor.index.showToast({ title: "申请介绍功能开发中", icon: "none" });
    }
    function getEducationText(education) {
      const educationMap = {
        1: "高中及以下",
        2: "大专",
        3: "本科",
        4: "硕士",
        5: "博士"
      };
      return educationMap[education] || "未知";
    }
    function getIncomeLevelText(level) {
      const levelMap = {
        1: "5K以下",
        2: "5K-10K",
        3: "10K-20K",
        4: "20K-50K",
        5: "50K以上"
      };
      return levelMap[level] || "未知";
    }
    function getMaritalStatusText(status) {
      const statusMap = {
        1: "未婚",
        2: "离异",
        3: "丧偶"
      };
      return statusMap[status] || "未知";
    }
    function getHouseStatusText(status) {
      const statusMap = {
        1: "租房",
        2: "有房贷款",
        3: "有房无贷款",
        4: "和家人同住"
      };
      return statusMap[status] || "未知";
    }
    function getCarStatusText(status) {
      const statusMap = {
        1: "无车",
        2: "有车有贷款",
        3: "有车无贷款"
      };
      return statusMap[status] || "未知";
    }
    function getToken() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (token) {
          return token;
        }
        common_vendor.index.__f__("log", "at pages/user/user-detail.vue:957", "未找到用户token，用户可能未登录");
        return null;
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/user/user-detail.vue:960", "获取token失败:", error);
        return null;
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: loading.value
      }, loading.value ? {} : common_vendor.e({
        b: common_vendor.o(goBack),
        c: common_vendor.t(userInfo.value && userInfo.value.nickname || "用户详情"),
        d: common_vendor.o(showMoreActions),
        e: statusBarHeight.value + "px",
        f: userInfo.value.avatarUrl || "/static/common/default-avatar.png",
        g: common_vendor.o(handleImageError),
        h: userInfo.value.isOnline
      }, userInfo.value.isOnline ? {} : {}, {
        i: userInfo.value.isVip === 1
      }, userInfo.value.isVip === 1 ? {} : {}, {
        j: userInfo.value.userRole === 2
      }, userInfo.value.userRole === 2 ? {} : {}, {
        k: userInfo.value.isVerified === 1
      }, userInfo.value.isVerified === 1 ? {} : {}, {
        l: common_vendor.t(userInfo.value.nickname || "用户"),
        m: common_vendor.t(getGenderAndAge()),
        n: common_vendor.t(userInfo.value.countFollow || 0),
        o: common_vendor.t(userInfo.value.fan || 0),
        p: common_vendor.t(userInfo.value.countLike || 0),
        q: userProfile.value.selfIntroduction
      }, userProfile.value.selfIntroduction ? {
        r: common_vendor.t(userProfile.value.selfIntroduction)
      } : {}, {
        s: common_vendor.t(userInfo.value.isFollowed ? "已关注" : "关注"),
        t: userInfo.value.isFollowed ? 1 : "",
        v: common_vendor.o(toggleFollow),
        w: userTags.value.length > 0
      }, userTags.value.length > 0 ? {
        x: common_vendor.f(userTags.value, (tag, k0, i0) => {
          return {
            a: common_vendor.t(tag.tagName),
            b: tag.id
          };
        })
      } : {}, {
        y: currentTab.value === "photos" ? 1 : "",
        z: common_vendor.o(($event) => switchTab("photos")),
        A: currentTab.value === "moments" ? 1 : "",
        B: common_vendor.o(($event) => switchTab("moments")),
        C: currentTab.value === "info" ? 1 : "",
        D: common_vendor.o(($event) => switchTab("info")),
        E: currentTab.value === "photos"
      }, currentTab.value === "photos" ? common_vendor.e({
        F: userPhotos.value.length > 0
      }, userPhotos.value.length > 0 ? {
        G: common_vendor.f(userPhotos.value, (photo, index, i0) => {
          return {
            a: photo.photoUrl,
            b: common_vendor.o(($event) => handlePhotoError(index), photo.photoId),
            c: photo.photoId,
            d: common_vendor.o(($event) => previewPhoto(index), photo.photoId)
          };
        })
      } : {}) : {}, {
        H: currentTab.value === "moments"
      }, currentTab.value === "moments" ? common_vendor.e({
        I: userMoments.value.length > 0
      }, userMoments.value.length > 0 ? {
        J: common_vendor.f(userMoments.value, (moment, k0, i0) => {
          return common_vendor.e({
            a: common_vendor.t(formatTime(moment.createdAt)),
            b: moment.location
          }, moment.location ? {
            c: common_vendor.t(moment.location)
          } : {}, {
            d: common_vendor.t(moment.content),
            e: moment.mediaList && moment.mediaList.length > 0
          }, moment.mediaList && moment.mediaList.length > 0 ? {
            f: common_vendor.f(moment.mediaList, (media, index, i1) => {
              return {
                a: media.mediaUrl,
                b: media.mediaId,
                c: common_vendor.o(($event) => previewMomentMedia(moment.mediaList, index), media.mediaId)
              };
            }),
            g: common_vendor.n(getMediaGridClass(moment.mediaList.length))
          } : {}, {
            h: common_vendor.t(moment.isLiked ? "❤️" : "🤍"),
            i: moment.isLiked ? 1 : "",
            j: common_vendor.t(moment.likeCount || 0),
            k: common_vendor.o(($event) => toggleMomentLike(moment), moment.momentId),
            l: common_vendor.t(moment.commentCount || 0),
            m: common_vendor.o(($event) => viewMomentComments(), moment.momentId),
            n: common_vendor.t(moment.viewCount || 0),
            o: moment.momentId
          });
        })
      } : {}) : {}, {
        K: currentTab.value === "info"
      }, currentTab.value === "info" ? common_vendor.e({
        L: userProfile.value.selfIntroduction
      }, userProfile.value.selfIntroduction ? {
        M: common_vendor.t(userProfile.value.selfIntroduction)
      } : {}, {
        N: userProfile.value.height
      }, userProfile.value.height ? {
        O: common_vendor.t(userProfile.value.height)
      } : {}, {
        P: userProfile.value.weight
      }, userProfile.value.weight ? {
        Q: common_vendor.t(userProfile.value.weight)
      } : {}, {
        R: userProfile.value.education
      }, userProfile.value.education ? {
        S: common_vendor.t(getEducationText(userProfile.value.education))
      } : {}, {
        T: userProfile.value.company
      }, userProfile.value.company ? {
        U: common_vendor.t(userProfile.value.company)
      } : {}, {
        V: userProfile.value.position
      }, userProfile.value.position ? {
        W: common_vendor.t(userProfile.value.position)
      } : {}, {
        X: userProfile.value.incomeLevel
      }, userProfile.value.incomeLevel ? {
        Y: common_vendor.t(getIncomeLevelText(userProfile.value.incomeLevel))
      } : {}, {
        Z: userProfile.value.maritalStatus
      }, userProfile.value.maritalStatus ? {
        aa: common_vendor.t(getMaritalStatusText(userProfile.value.maritalStatus))
      } : {}, {
        ab: userProfile.value.hasChildren !== null
      }, userProfile.value.hasChildren !== null ? {
        ac: common_vendor.t(userProfile.value.hasChildren ? "有" : "无")
      } : {}, {
        ad: userProfile.value.houseStatus
      }, userProfile.value.houseStatus ? {
        ae: common_vendor.t(getHouseStatusText(userProfile.value.houseStatus))
      } : {}, {
        af: userProfile.value.carStatus
      }, userProfile.value.carStatus ? {
        ag: common_vendor.t(getCarStatusText(userProfile.value.carStatus))
      } : {}, {
        ah: userProfile.value.hobby
      }, userProfile.value.hobby ? {
        ai: common_vendor.t(userProfile.value.hobby)
      } : {}, {
        aj: userInfo.value.isVerified === 1
      }, userInfo.value.isVerified === 1 ? {} : {}) : {}, {
        ak: statusBarHeight.value + 88 + "px",
        al: common_vendor.o(sendMessage),
        am: common_vendor.o(requestIntroduction)
      }));
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-a9655861"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/user/user-detail.js.map
