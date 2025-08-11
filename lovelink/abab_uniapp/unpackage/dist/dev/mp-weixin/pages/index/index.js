"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_wallet = require("../../api/wallet.js");
const utils_provinces = require("../../utils/provinces.js");
const api_user_auth = require("../../api/user/auth.js");
const api_vip = require("../../api/vip.js");
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
  __name: "index",
  setup(__props) {
    const imageError = common_vendor.reactive({
      logo: false,
      coin: false
    });
    const userCurrency = common_vendor.ref(0);
    const vipStatus = common_vendor.ref({
      isVip: false,
      userRole: 1,
      vipExpireTime: null
    });
    const regionPopup = common_vendor.ref(null);
    const regions = utils_provinces.CHINA_PROVINCES.map((province) => utils_provinces.getProvinceShortName(province));
    const regionIndex = common_vendor.ref([0]);
    const selectedRegion = common_vendor.ref("北京");
    const tempRegion = common_vendor.ref("");
    const banners = common_vendor.ref([]);
    async function fetchBanners() {
      try {
        const result = await new Promise((resolve, reject) => {
          common_vendor.index.request({
            url: "http://localhost:9001/lunb/lunbList",
            method: "POST",
            success: (res) => {
              resolve(res);
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
        if (result.statusCode === 200 && result.data.code === 200) {
          banners.value = result.data.data.map((item) => ({
            id: item.bannerId,
            title: item.bannerTitle,
            image: item.bannerImage,
            url: item.bannerUrl || "#",
            targetType: item.targetType,
            targetId: item.targetId
          }));
          common_vendor.index.__f__("log", "at pages/index/index.vue:215", "轮播图数据获取成功:", banners.value);
        } else {
          common_vendor.index.__f__("error", "at pages/index/index.vue:217", "轮播图数据获取失败:", result);
          setDefaultBanners();
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:222", "轮播图数据获取异常:", error);
        setDefaultBanners();
      }
    }
    function setDefaultBanners() {
      banners.value = [
        {
          id: 1,
          title: "缘来如此·夏日派对",
          image: "/static/index/banner1.png",
          url: "/pages/activity/summer-party"
        },
        {
          id: 2,
          title: "新人专享礼包",
          image: "/static/index/banner2.png",
          url: "/pages/activity/new-user"
        },
        {
          id: 3,
          title: "周末线下联谊",
          image: "/static/index/banner3.png",
          url: "/pages/activity/offline-event"
        }
      ];
    }
    common_vendor.onMounted(() => {
      fetchBanners();
      fetchRecommendUsers();
    });
    const games = common_vendor.ref([
      {
        id: 1,
        name: "心动速配",
        desc: "趣味互动，快速匹配",
        image: "/static/index/game1.png"
      },
      {
        id: 2,
        name: "性格测试",
        desc: "了解自己，找到匹配",
        image: "/static/index/game2.png"
      },
      {
        id: 3,
        name: "恋爱话题",
        desc: "破冰神器，告别尬聊",
        image: "/static/index/game3.png"
      },
      {
        id: 4,
        name: "缘分测算",
        desc: "星座配对，姻缘分析",
        image: "/static/index/game4.png"
      }
    ]);
    const matches = common_vendor.ref([]);
    async function fetchRecommendUsers() {
      try {
        const squareApi = await "../../api/square.js";
        const result = await squareApi.default.getRecommendUsers(4);
        if (result.code === 200) {
          matches.value = result.data.records.map((user) => ({
            id: user.userId,
            name: user.nickname || "匿名用户",
            sex: user.gender,
            selfIntroduction: user.userProfile && user.userProfile.selfIntroduction || "这个人很神秘，没有留下介绍",
            avatar: user.avatarUrl || "/static/default-avatar.png"
          }));
          common_vendor.index.__f__("log", "at pages/index/index.vue:307", "相亲广场推荐用户获取成功:", matches.value);
        } else {
          common_vendor.index.__f__("error", "at pages/index/index.vue:309", "相亲广场推荐用户获取失败:", result);
          setDefaultMatches();
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:314", "相亲广场推荐用户获取异常:", error);
      }
    }
    function goMoreMatches() {
      common_vendor.index.navigateTo({
        url: "/pages/square/square"
      });
    }
    function setDefaultMatches() {
      matches.value = [
        {
          id: 1,
          name: "小雨",
          age: 26,
          intro: "喜欢旅行，热爱生活",
          avatar: "/static/index/avatar1.png"
        },
        {
          id: 2,
          name: "阳阳",
          age: 28,
          intro: "工程师，爱好摄影",
          avatar: "/static/index/avatar2.png"
        },
        {
          id: 3,
          name: "小米",
          age: 25,
          intro: "教师，温柔贤淑",
          avatar: "/static/index/avatar3.png"
        },
        {
          id: 4,
          name: "大壮",
          age: 30,
          intro: "医生，稳重可靠",
          avatar: "/static/index/avatar4.png"
        }
      ];
    }
    const entertainment = common_vendor.ref([
      {
        id: 1,
        name: "语音聊天",
        image: "/static/index/entertainment1.png"
      },
      {
        id: 2,
        name: "视频相亲",
        image: "/static/index/entertainment2.png"
      },
      {
        id: 3,
        name: "趣味问答",
        image: "/static/index/entertainment3.png"
      },
      {
        id: 4,
        name: "脱单攻略",
        image: "/static/index/entertainment4.png"
      }
    ]);
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
    function handleBannerClick(banner) {
      common_vendor.index.__f__("log", "at pages/index/index.vue:418", "点击轮播图:", banner);
      if (!banner.url || banner.url === "#") {
        common_vendor.index.__f__("log", "at pages/index/index.vue:421", "轮播图没有设置跳转链接");
        return;
      }
      if (banner.targetType === 1) {
        common_vendor.index.navigateTo({
          url: banner.url
        });
      } else if (banner.targetType === 2 && banner.targetId) {
        common_vendor.index.navigateTo({
          url: `/pages/detail/detail?id=${banner.targetId}`
        });
      } else {
        common_vendor.index.navigateTo({
          url: banner.url
        });
      }
    }
    function goSearch() {
      common_vendor.index.navigateTo({
        url: "/pages/search/search"
      });
    }
    function goMoreGames() {
      common_vendor.index.navigateTo({
        url: "/pages/games/games-list"
      });
    }
    function startGame(game) {
      if (game.id === 1) {
        common_vendor.index.navigateTo({
          url: "/pages/game/heart-match"
        });
      } else {
        common_vendor.index.showToast({
          title: "该游戏正在开发中",
          icon: "none"
        });
      }
    }
    function viewMatch(match) {
      common_vendor.index.navigateTo({
        url: `/pages/user/user-detail?id=${match.id}`
      });
    }
    function goMoreEntertainment() {
      common_vendor.index.navigateTo({
        url: "/pages/entertainment/entertainment-list"
      });
    }
    function goEntertainment(item) {
      common_vendor.index.navigateTo({
        url: `/pages/entertainment/entertainment-detail?id=${item.id}`
      });
    }
    function goVipCenter() {
      common_vendor.index.navigateTo({
        url: "/pages/vip/vip-center"
      });
    }
    function goMatchmakerApply() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        common_vendor.index.showModal({
          title: "提示",
          content: "请先登录后再申请成为红娘",
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
        url: "/pages/matchmaker/apply"
      });
    }
    async function getWalletData() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/index/index.vue:531", "未登录，无法获取钱包信息");
          return;
        }
        const response = await api_wallet.getWalletInfo();
        if (response.code === 200) {
          userCurrency.value = response.data.coinBalance || 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:540", "获取钱包信息失败:", error);
      }
    }
    async function getVipStatusData() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/index/index.vue:549", "未登录，无法获取VIP状态");
          return;
        }
        const response = await api_vip.getUserVipStatus();
        if (response.code === 200) {
          vipStatus.value = {
            isVip: response.data.isVip === 1,
            userRole: response.data.userRole,
            vipExpireTime: response.data.vipExpireTime
          };
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:562", "获取VIP状态失败:", error);
      }
    }
    function formatVipExpireTime() {
      if (!vipStatus.value.vipExpireTime) {
        return "";
      }
      const date = new Date(vipStatus.value.vipExpireTime);
      return date.toLocaleDateString();
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
    common_vendor.onMounted(() => {
      getWalletData();
      loadUserInfo();
      getVipStatusData();
    });
    common_vendor.onShow(() => {
      getWalletData();
      getVipStatusData();
    });
    common_vendor.onMounted(() => {
      common_vendor.index.$on("refreshUserProvince", () => {
        common_vendor.index.__f__("log", "at pages/index/index.vue:613", "收到省份更新事件，延迟5秒后刷新用户信息");
        setTimeout(() => {
          loadUserInfo();
        }, 5e3);
        setTimeout(() => {
          common_vendor.index.__f__("log", "at pages/index/index.vue:620", "二次刷新用户省份信息");
          loadUserInfo();
        }, 8e3);
      });
    });
    common_vendor.onUnmounted(() => {
      common_vendor.index.$off("refreshUserProvince");
    });
    async function loadUserInfo() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.__f__("log", "at pages/index/index.vue:636", "未登录，使用默认省份");
          return;
        }
        const result = await api_user_auth.getByUserInfo(token);
        common_vendor.index.__f__("log", "at pages/index/index.vue:641", "获取用户信息API响应:", result);
        if (result.code === 200 && result.data) {
          const userInfo = result.data;
          common_vendor.index.__f__("log", "at pages/index/index.vue:645", "用户信息详情:", userInfo);
          common_vendor.index.__f__("log", "at pages/index/index.vue:646", "lastLoginIp字段值:", userInfo.lastLoginIp);
          if (userInfo.lastLoginIp) {
            const provinceShortName = utils_provinces.getProvinceShortName(userInfo.lastLoginIp);
            common_vendor.index.__f__("log", "at pages/index/index.vue:651", "省份转换: {} -> {}", userInfo.lastLoginIp, provinceShortName);
            selectedRegion.value = provinceShortName;
            common_vendor.index.__f__("log", "at pages/index/index.vue:653", "设置用户当前省份:", provinceShortName);
          } else {
            common_vendor.index.__f__("log", "at pages/index/index.vue:655", "用户暂无省份信息，使用默认省份");
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:659", "获取用户信息失败:", error);
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
          common_vendor.index.__f__("log", "at pages/index/index.vue:681", "省份更新成功:", province);
        } else {
          common_vendor.index.showToast({
            title: result.message || "更新失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/index/index.vue:689", "更新省份失败:", error);
        common_vendor.index.showToast({
          title: "更新失败，请重试",
          icon: "none"
        });
      }
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
        i: common_vendor.f(banners.value, (banner, index, i0) => {
          return common_vendor.e({
            a: imageError[`banner${index}`]
          }, imageError[`banner${index}`] ? {} : {}, {
            b: banner.image,
            c: common_vendor.o(($event) => handleImageError(`banner${index}`), index),
            d: common_vendor.t(banner.title),
            e: index,
            f: common_vendor.o(($event) => handleBannerClick(banner), index)
          });
        }),
        j: common_vendor.o(goMoreGames),
        k: common_vendor.f(games.value, (game, index, i0) => {
          return common_vendor.e({
            a: imageError[`game${index}`]
          }, imageError[`game${index}`] ? {} : {}, {
            b: game.image,
            c: common_vendor.o(($event) => handleImageError(`game${index}`), index),
            d: common_vendor.t(game.name),
            e: common_vendor.t(game.desc),
            f: index,
            g: common_vendor.o(($event) => startGame(game), index)
          });
        }),
        l: common_vendor.o(goMoreMatches),
        m: common_vendor.f(matches.value, (match, index, i0) => {
          return common_vendor.e({
            a: imageError[`match${index}`]
          }, imageError[`match${index}`] ? {} : {}, {
            b: match.avatar,
            c: common_vendor.o(($event) => handleImageError(`match${index}`), index),
            d: common_vendor.t(match.name),
            e: common_vendor.t(match.gender === 1 ? "♂" : "♀"),
            f: common_vendor.t(match.selfIntroduction),
            g: index,
            h: common_vendor.o(($event) => viewMatch(match), index)
          });
        }),
        n: common_vendor.o(goMoreEntertainment),
        o: common_vendor.f(entertainment.value, (item, index, i0) => {
          return common_vendor.e({
            a: imageError[`entertainment${index}`]
          }, imageError[`entertainment${index}`] ? {} : {}, {
            b: item.image,
            c: common_vendor.o(($event) => handleImageError(`entertainment${index}`), index),
            d: common_vendor.t(item.name),
            e: index,
            f: common_vendor.o(($event) => goEntertainment(item), index)
          });
        }),
        p: common_vendor.o(goVipCenter),
        q: common_vendor.t(vipStatus.value.isVip ? "VIP会员" : "开通VIP"),
        r: common_vendor.t(vipStatus.value.isVip ? "会员有效期至 " + formatVipExpireTime() : "专属特权 · 1v1精准配对"),
        s: common_vendor.t(vipStatus.value.isVip ? "续费" : "立即开通"),
        t: common_vendor.o(goVipCenter),
        v: common_vendor.o(goMatchmakerApply),
        w: common_vendor.o(goMatchmakerApply),
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
        D: common_vendor.sr(regionPopup, "1cf27b2a-0", {
          "k": "regionPopup"
        }),
        E: common_vendor.p({
          type: "bottom"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
