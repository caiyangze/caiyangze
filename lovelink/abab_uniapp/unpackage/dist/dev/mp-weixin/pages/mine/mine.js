"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user_auth = require("../../api/user/auth.js");
const api_wallet = require("../../api/wallet.js");
const api_moment = require("../../api/moment.js");
if (!Math) {
  customTabBar();
}
const customTabBar = () => "../../components/custom-tab-bar.js";
const _sfc_main = {
  __name: "mine",
  setup(__props) {
    const imageError = common_vendor.reactive({
      avatar: false,
      coin: false
    });
    const userCurrency = common_vendor.ref(0);
    const userInfo = common_vendor.reactive({
      id: "",
      nickname: "心动用户",
      avatar: "/static/mine/avatar.png",
      gender: 1,
      // 1: 男, 2: 女
      age: 28,
      followers: 0,
      following: 0,
      likes: 0,
      isVip: false,
      vipExpireDate: "2023-12-31",
      isVerified: 0
      // 是否实名认证 0:未认证 1:已认证
    });
    async function getUserInfo() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/mine/mine.vue:233", "未登录，无法获取用户信息");
          return;
        }
        const res = await api_user_auth.getByUserInfo(token);
        if (res && res.code === 200 && res.data) {
          userInfo.id = res.data.userId;
          userInfo.nickname = res.data.nickname || "心动用户";
          userInfo.avatar = res.data.avatarUrl || "/static/mine/avatar.png";
          userInfo.gender = res.data.gender || 1;
          userInfo.followers = res.data.fan || 0;
          userInfo.following = res.data.countFollow || 0;
          userInfo.likes = res.data.countLike || 0;
          userInfo.isVip = res.data.isVip === 1;
          userInfo.vipExpireDate = res.data.vipExpireTime ? new Date(res.data.vipExpireTime).toLocaleDateString() : "";
          userInfo.isVerified = res.data.isVerified;
          common_vendor.index.__f__("log", "at pages/mine/mine.vue:251", "用户信息获取成功:", userInfo);
        } else {
          common_vendor.index.__f__("error", "at pages/mine/mine.vue:253", "获取用户信息失败:", res);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/mine.vue:256", "获取用户信息异常:", error);
      }
    }
    async function getWalletData() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/mine/mine.vue:265", "未登录，无法获取钱包信息");
          return;
        }
        const response = await api_wallet.getWalletInfo();
        if (response.code === 200) {
          userCurrency.value = response.data.coinBalance || 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/mine.vue:274", "获取钱包信息失败:", error);
      }
    }
    async function getUserPosts() {
      var _a;
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/mine/mine.vue:283", "未登录，无法获取用户动态");
          return;
        }
        postsLoading.value = true;
        postsError.value = "";
        const result = await api_moment.default.getMyMoments(1, 3);
        if (result.code === 200) {
          const records = ((_a = result.data) == null ? void 0 : _a.records) || [];
          userPosts.value = records.map((moment) => ({
            id: moment.momentId,
            title: moment.content,
            image: moment.mediaList && moment.mediaList.length > 0 ? moment.mediaList[0].mediaUrl : "/static/mine/default-post.png",
            likes: moment.likeCount || 0,
            comments: moment.commentCount || 0,
            momentData: moment
            // 保存原始数据
          }));
          common_vendor.index.__f__("log", "at pages/mine/mine.vue:305", "用户动态获取成功:", userPosts.value);
        } else {
          postsError.value = result.message || "获取动态失败";
          common_vendor.index.__f__("error", "at pages/mine/mine.vue:308", "获取用户动态失败:", result);
        }
      } catch (error) {
        postsError.value = "获取动态失败";
        common_vendor.index.__f__("error", "at pages/mine/mine.vue:312", "获取用户动态异常:", error);
      } finally {
        postsLoading.value = false;
      }
    }
    common_vendor.onMounted(() => {
      getWalletData();
    });
    common_vendor.onShow(() => {
      getUserInfo();
      getWalletData();
      getUserPosts();
    });
    const menuItems = common_vendor.computed(() => [
      { name: "我的喜欢", icon: "icon-like", url: "/pages/mine/likes" },
      { name: "我的收藏", icon: "icon-favorite", url: "/pages/mine/favorites" },
      {
        name: userInfo.isVerified === 1 ? "认证管理" : "实名认证",
        icon: "icon-verify",
        action: "goToVerification"
      },
      { name: "申请红娘", icon: "icon-matchmaker", url: "/pages/matchmaker/apply" },
      { name: "每日签到", icon: "icon-sign", url: "/pages/sign/sign" },
      { name: "优惠券", icon: "icon-voucher", url: "/pages/voucher/voucher-list" },
      { name: "我的相册", icon: "icon-album", url: "/pages/mine/album" },
      { name: "我的活动", icon: "icon-activity", url: "/pages/mine/activities" },
      { name: "我的礼物", icon: "icon-gift", url: "/pages/mine/gifts" },
      { name: "客服中心", icon: "icon-service", url: "/pages/mine/service" }
    ]);
    const userPosts = common_vendor.ref([]);
    const postsLoading = common_vendor.ref(false);
    const postsError = common_vendor.ref("");
    function handleImageError(type) {
      imageError[type] = true;
    }
    function goFollowers() {
      common_vendor.index.navigateTo({
        url: "/pages/user/fans-list"
      });
    }
    function goFollowing() {
      common_vendor.index.navigateTo({
        url: "/pages/user/following-list"
      });
    }
    function goLikes() {
      common_vendor.index.navigateTo({
        url: "/pages/mine/likes-received"
      });
    }
    function goToEditProfile() {
      const userId = (userInfo == null ? void 0 : userInfo.id) || "";
      common_vendor.index.navigateTo({
        url: "/pages/profile/edit-profile" + (userId ? `?id=${userId}` : ""),
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/mine/mine.vue:389", "跳转失败:", err);
          common_vendor.index.showToast({
            title: "跳转失败，请稍后重试",
            icon: "none"
          });
        }
      });
    }
    function goVipCenter() {
      common_vendor.index.navigateTo({
        url: "/pages/vip/recharge"
      });
    }
    function handleMenuClick(item) {
      if (item.action) {
        switch (item.action) {
          case "goToVerification":
            goToVerification();
            break;
          default:
            common_vendor.index.__f__("warn", "at pages/mine/mine.vue:412", "未知的菜单action:", item.action);
        }
      } else if (item.url) {
        common_vendor.index.navigateTo({
          url: item.url
        });
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
    function goToVerification() {
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
        url: "/pages/profile/verify",
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/mine/mine.vue:458", "跳转实名认证页面失败:", err);
          common_vendor.index.showToast({
            title: "跳转失败，请稍后重试",
            icon: "none"
          });
        }
      });
    }
    function goAllPosts() {
      common_vendor.index.navigateTo({
        url: "/pages/moment/list?type=mine"
      });
    }
    function viewPost(post) {
      if (post.momentData) {
        common_vendor.index.navigateTo({
          url: `/pages/moment/detail?id=${post.momentData.momentId}`
        });
      } else {
        common_vendor.index.navigateTo({
          url: `/pages/mine/post-detail?id=${post.id}`
        });
      }
    }
    function createPost() {
      common_vendor.index.navigateTo({
        url: "/pages/moment/publish"
      });
    }
    function goSettings() {
      common_vendor.index.navigateTo({
        url: "/pages/mine/settings"
      });
    }
    function goFeedback() {
      common_vendor.index.navigateTo({
        url: "/pages/mine/feedback"
      });
    }
    function goAbout() {
      common_vendor.index.navigateTo({
        url: "/pages/mine/about"
      });
    }
    function logout() {
      common_vendor.index.showModal({
        title: "退出登录",
        content: "确定要退出当前账号吗？\n您的个人信息将被清除",
        confirmText: "退出",
        cancelText: "取消",
        confirmColor: "#FF6B8B",
        cancelColor: "#8E8E93",
        success: (res) => {
          if (res.confirm) {
            common_vendor.index.removeStorageSync("token");
            common_vendor.index.removeStorageSync("userInfo");
            userInfo.id = "";
            userInfo.nickname = "心动用户";
            userInfo.avatar = "/static/mine/avatar.png";
            userInfo.gender = 1;
            userInfo.followers = 0;
            userInfo.following = 0;
            userInfo.likes = 0;
            userInfo.isVip = false;
            userInfo.isVerified = 0;
            common_vendor.index.showToast({
              title: "已安全退出",
              icon: "success",
              duration: 1500,
              success: () => {
                setTimeout(() => {
                  common_vendor.index.reLaunch({
                    url: "/pages/login/login"
                  });
                }, 1e3);
              }
            });
          }
        }
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: imageError.coin
      }, imageError.coin ? {} : {}, {
        b: common_assets._imports_0,
        c: common_vendor.o(($event) => handleImageError("coin")),
        d: common_vendor.t(userCurrency.value),
        e: common_vendor.o(goToWallet),
        f: imageError.avatar
      }, imageError.avatar ? {
        g: common_vendor.t(userInfo.nickname.substring(0, 1))
      } : {}, {
        h: userInfo.avatar,
        i: common_vendor.o(($event) => handleImageError("avatar")),
        j: common_vendor.t(userInfo.nickname),
        k: common_vendor.t(userInfo.gender === 1 ? "♂" : "♀"),
        l: common_vendor.n(userInfo.gender === 1 ? "male" : "female"),
        m: common_vendor.t(userInfo.id),
        n: userInfo.isVerified === 1
      }, userInfo.isVerified === 1 ? {} : {}, {
        o: common_vendor.t(userInfo.followers),
        p: common_vendor.o(goFollowers),
        q: common_vendor.t(userInfo.following),
        r: common_vendor.o(goFollowing),
        s: common_vendor.t(userInfo.likes),
        t: common_vendor.o(goLikes),
        v: common_vendor.o(goToEditProfile),
        w: common_vendor.t(userInfo.isVip ? "VIP会员" : "开通VIP"),
        x: common_vendor.t(userInfo.isVip ? "会员有效期至 " + userInfo.vipExpireDate : "专属特权 · 1v1精准配对"),
        y: common_vendor.t(userInfo.isVip ? "续费" : "立即开通"),
        z: common_vendor.o(goVipCenter),
        A: common_vendor.f(menuItems.value.slice(0, 4), (item, index, i0) => {
          return {
            a: common_vendor.n(item.icon),
            b: common_vendor.t(item.name),
            c: index,
            d: common_vendor.o(($event) => handleMenuClick(item), index)
          };
        }),
        B: common_vendor.f(menuItems.value.slice(4, 8), (item, index, i0) => {
          return {
            a: common_vendor.n(item.icon),
            b: common_vendor.t(item.name),
            c: index + 4,
            d: common_vendor.o(($event) => handleMenuClick(item), index + 4)
          };
        }),
        C: menuItems.value.length > 8
      }, menuItems.value.length > 8 ? common_vendor.e({
        D: common_vendor.f(menuItems.value.slice(8, 12), (item, index, i0) => {
          return {
            a: common_vendor.n(item.icon),
            b: common_vendor.t(item.name),
            c: index + 8,
            d: common_vendor.o(($event) => handleMenuClick(item), index + 8)
          };
        }),
        E: menuItems.value.slice(8, 12).length < 4
      }, menuItems.value.slice(8, 12).length < 4 ? {
        F: common_vendor.f(4 - Math.min(4, menuItems.value.length - 8), (n, k0, i0) => {
          return {
            a: "placeholder1-" + n
          };
        })
      } : {}, {
        G: common_vendor.f(menuItems.value.slice(12, 16), (item, index, i0) => {
          return {
            a: common_vendor.n(item.icon),
            b: common_vendor.t(item.name),
            c: index + 12,
            d: common_vendor.o(($event) => handleMenuClick(item), index + 12)
          };
        }),
        H: menuItems.value.slice(12, 16).length < 4
      }, menuItems.value.slice(12, 16).length < 4 ? {
        I: common_vendor.f(4 - Math.min(4, Math.max(0, menuItems.value.length - 12)), (n, k0, i0) => {
          return {
            a: "placeholder2-" + n
          };
        })
      } : {}) : {}, {
        J: common_vendor.o(goAllPosts),
        K: postsLoading.value
      }, postsLoading.value ? {} : postsError.value ? {
        M: common_vendor.t(postsError.value),
        N: common_vendor.o(getUserPosts)
      } : userPosts.value.length > 0 ? {
        P: common_vendor.f(userPosts.value, (post, index, i0) => {
          return common_vendor.e({
            a: imageError[`post${index}`]
          }, imageError[`post${index}`] ? {} : {}, {
            b: post.image,
            c: common_vendor.o(($event) => handleImageError(`post${index}`), index),
            d: common_vendor.t(post.title),
            e: common_vendor.t(post.likes),
            f: common_vendor.t(post.comments),
            g: index,
            h: common_vendor.o(($event) => viewPost(post), index)
          });
        })
      } : {
        Q: common_vendor.o(createPost)
      }, {
        L: postsError.value,
        O: userPosts.value.length > 0,
        R: common_vendor.o(goSettings),
        S: common_vendor.o(goFeedback),
        T: common_vendor.o(goAbout),
        U: common_vendor.o(logout)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-7c2ebfa5"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/mine/mine.js.map
