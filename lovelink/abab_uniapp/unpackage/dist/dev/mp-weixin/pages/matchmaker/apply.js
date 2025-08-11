"use strict";
const common_vendor = require("../../common/vendor.js");
require("../../api/http.js");
const api_wallet = require("../../api/wallet.js");
const api_user_auth = require("../../api/user/auth.js");
const api_matchmaker = require("../../api/matchmaker.js");
const VoucherSelector = () => "../../components/voucher-selector/voucher-selector.js";
const _sfc_main = {
  components: {
    VoucherSelector
  },
  data() {
    return {
      formData: {
        realName: "",
        phone: "",
        idCardNo: "",
        idCardFront: "",
        idCardBack: "",
        serviceArea: "",
        contactPhone: "",
        introduction: "",
        experience: ""
      },
      showConfirm: false,
      isSubmitting: false,
      currentBalance: -1,
      selectedVoucher: null,
      // 选中的优惠券
      userStatus: {
        hasPhone: false,
        hasGender: false,
        isVerified: false,
        hasEnoughCoins: false
      },
      // 标记哪些字段来自实名认证（不可编辑）
      verificationFields: {
        realName: false,
        idCardNo: false,
        idCardFront: false,
        idCardBack: false
      },
      // 申请状态信息
      applicationStatus: {
        hasApplied: false,
        isMatchmaker: false,
        status: "",
        applicationId: null,
        statusCode: null,
        rejectReason: ""
      },
      // 控制是否显示全屏状态
      showFullscreenStatus: false
    };
  },
  computed: {
    canSubmit() {
      if (this.applicationStatus.hasApplied && this.applicationStatus.statusCode !== 2) {
        return false;
      }
      const { realName, idCardNo, idCardFront, idCardBack, serviceArea, contactPhone, introduction, experience } = this.formData;
      const formComplete = realName && idCardNo && idCardFront && idCardBack && serviceArea && contactPhone && introduction.length >= 0 && experience.length >= 0;
      const statusComplete = this.userStatus.hasPhone && this.userStatus.hasGender && this.userStatus.isVerified && this.userStatus.hasEnoughCoins;
      return formComplete && statusComplete;
    },
    // 所有条件是否满足
    allConditionsMet() {
      return this.userStatus.hasPhone && this.userStatus.hasGender && this.userStatus.isVerified && this.userStatus.hasEnoughCoins;
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 显示确认弹窗
    async showConfirmDialog() {
      await this.loadUserBalance();
      this.showConfirm = true;
    },
    // 隐藏确认弹窗
    hideConfirmDialog() {
      this.showConfirm = false;
    },
    // 加载用户余额
    async loadUserBalance() {
      try {
        const response = await api_wallet.getWalletInfo();
        if (response.code === 200) {
          this.currentBalance = response.data.coinBalance || 0;
        } else {
          this.currentBalance = 0;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:418", "获取余额失败:", error);
        this.currentBalance = 0;
      }
    },
    // 提交申请
    async submitApplication() {
      var _a;
      if (this.currentBalance < 699) {
        common_vendor.index.showModal({
          title: "余额不足",
          content: "您的虚拟币余额不足，需要699币才能申请成为红娘。是否前往充值？",
          confirmText: "去充值",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.navigateTo({
                url: "/pages/wallet/wallet"
              });
            }
          }
        });
        return;
      }
      this.isSubmitting = true;
      this.showConfirm = false;
      try {
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
        const submitData = {
          ...this.formData,
          phone: this.formData.contactPhone
        };
        if (this.selectedVoucher) {
          submitData.voucherOrderId = this.selectedVoucher.id;
          submitData.voucherDiscountAmount = this.selectedVoucher.voucher.actualValue;
          common_vendor.index.__f__("log", "at pages/matchmaker/apply.vue:468", "申请红娘使用优惠券，优惠券订单ID：", this.selectedVoucher.id, "抵扣金额：", this.selectedVoucher.voucher.actualValue);
        }
        common_vendor.index.__f__("log", "at pages/matchmaker/apply.vue:471", "申请红娘提交数据：", submitData);
        const response = await api_matchmaker.applyMatchmaker(submitData);
        if (response.code === 200) {
          this.applicationStatus = {
            hasApplied: true,
            isMatchmaker: false,
            status: "申请审核中",
            applicationId: (_a = response.data) == null ? void 0 : _a.applicationId,
            statusCode: 0,
            // 0表示审核中
            rejectReason: ""
          };
          this.showFullscreenStatus = true;
        } else {
          common_vendor.index.showToast({
            title: response.message || "申请失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:492", "申请失败:", error);
        common_vendor.index.showToast({
          title: "申请失败，请重试",
          icon: "none"
        });
      } finally {
        this.isSubmitting = false;
      }
    },
    // 检查申请状态
    async checkApplicationStatus() {
      try {
        const response = await api_matchmaker.checkApplicationStatus();
        if (response.code === 200 && response.data) {
          const data = response.data;
          this.applicationStatus = {
            hasApplied: data.hasApplied,
            isMatchmaker: data.isMatchmaker,
            status: data.status,
            applicationId: data.applicationId,
            statusCode: data.applicationStatus,
            rejectReason: data.rejectReason || ""
          };
          if (data.hasApplied) {
            this.showApplicationStatusModal();
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:523", "检查申请状态失败:", error);
      }
    },
    // 显示申请状态
    showApplicationStatusModal() {
      const { hasApplied, isMatchmaker } = this.applicationStatus;
      if (isMatchmaker || hasApplied) {
        this.showFullscreenStatus = true;
      }
    },
    // 获取用户实名认证信息
    async loadUserVerification() {
      try {
        const response = await api_matchmaker.getUserVerification();
        if (response.code === 200 && response.data) {
          const verificationData = response.data;
          this.formData.realName = verificationData.realName || "";
          this.formData.idCardNo = verificationData.idCardNo || "";
          this.formData.idCardFront = verificationData.idCardFront || "";
          this.formData.idCardBack = verificationData.idCardBack || "";
          this.verificationFields.realName = !!verificationData.realName;
          this.verificationFields.idCardNo = !!verificationData.idCardNo;
          this.verificationFields.idCardFront = !!verificationData.idCardFront;
          this.verificationFields.idCardBack = !!verificationData.idCardBack;
          common_vendor.index.__f__("log", "at pages/matchmaker/apply.vue:555", "已自动填充实名认证信息");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:558", "获取实名认证信息失败:", error);
      }
    },
    // 检查用户状态
    async checkUserStatus() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          return;
        }
        const userResponse = await api_user_auth.getByUserInfo(token);
        if (userResponse.code === 200) {
          const userInfo = userResponse.data;
          this.userStatus.hasPhone = !!userInfo.phone;
          this.userStatus.hasGender = userInfo.gender > 0;
          this.userStatus.isVerified = userInfo.isVerified === 1;
        }
        await this.loadUserBalance();
        this.userStatus.hasEnoughCoins = this.currentBalance >= 699;
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:585", "检查用户状态失败:", error);
      }
    },
    // 处理条件点击
    handleConditionClick(type) {
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
      switch (type) {
        case "phone":
          if (!this.userStatus.hasPhone) {
            common_vendor.index.showModal({
              title: "绑定手机号",
              content: "需要绑定手机号才能申请红娘，是否前往绑定？",
              success: (res) => {
                if (res.confirm) {
                  common_vendor.index.navigateTo({
                    url: "/pages/profile/bind-phone"
                  });
                }
              }
            });
          }
          break;
        case "gender":
          if (!this.userStatus.hasGender) {
            common_vendor.index.showModal({
              title: "选择性别",
              content: "需要选择性别才能申请红娘，是否前往设置？",
              success: (res) => {
                if (res.confirm) {
                  common_vendor.index.navigateTo({
                    url: "/pages/profile/edit-profile"
                  });
                }
              }
            });
          }
          break;
        case "verify":
          if (!this.userStatus.isVerified) {
            common_vendor.index.showModal({
              title: "实名认证",
              content: "需要完成实名认证才能申请红娘，是否前往认证？",
              success: (res) => {
                if (res.confirm) {
                  common_vendor.index.navigateTo({
                    url: "/pages/profile/verify"
                  });
                }
              }
            });
          }
          break;
        case "coins":
          if (!this.userStatus.hasEnoughCoins) {
            common_vendor.index.showModal({
              title: "余额不足",
              content: `当前余额：${this.currentBalance}币，需要699币才能申请红娘。是否前往充值？`,
              success: (res) => {
                if (res.confirm) {
                  common_vendor.index.navigateTo({
                    url: "/pages/wallet/wallet"
                  });
                }
              }
            });
          }
          break;
      }
    },
    // 上传身份证正面
    uploadIdCardFront() {
      common_vendor.index.showActionSheet({
        itemList: ["拍照", "从相册选择"],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ["camera"] : ["album"];
          common_vendor.index.chooseImage({
            count: 1,
            sizeType: ["compressed"],
            sourceType,
            success: (res2) => {
              this.formData.idCardFront = res2.tempFilePaths[0];
              common_vendor.index.showToast({
                title: "上传成功",
                icon: "success"
              });
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:690", "上传失败:", err);
              common_vendor.index.showToast({
                title: "上传失败，请重试",
                icon: "none"
              });
            }
          });
        }
      });
    },
    // 上传身份证背面
    uploadIdCardBack() {
      common_vendor.index.showActionSheet({
        itemList: ["拍照", "从相册选择"],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ["camera"] : ["album"];
          common_vendor.index.chooseImage({
            count: 1,
            sizeType: ["compressed"],
            sourceType,
            success: (res2) => {
              this.formData.idCardBack = res2.tempFilePaths[0];
              common_vendor.index.showToast({
                title: "上传成功",
                icon: "success"
              });
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:721", "上传失败:", err);
              common_vendor.index.showToast({
                title: "上传失败，请重试",
                icon: "none"
              });
            }
          });
        }
      });
    },
    // 优惠券选择回调
    onVoucherSelected(voucher) {
      this.selectedVoucher = voucher;
    },
    // 计算实际消费金额
    getActualCost() {
      const originalCost = 699;
      if (this.selectedVoucher && this.selectedVoucher.voucher) {
        const discount = this.selectedVoucher.voucher.actualValue;
        return Math.max(0, originalCost - discount);
      }
      return originalCost;
    },
    // 获取申请状态样式类
    getApplicationStatusClass() {
      if (!this.applicationStatus.statusCode && this.applicationStatus.statusCode !== 0)
        return "";
      switch (this.applicationStatus.statusCode) {
        case 1:
          return "success";
        case 2:
          return "failed";
        case 0:
          return "pending";
        default:
          return "";
      }
    },
    // 获取申请状态标题
    getApplicationStatusTitle() {
      if (this.applicationStatus.isMatchmaker) {
        return "您已是红娘";
      }
      switch (this.applicationStatus.statusCode) {
        case 1:
          return "申请通过";
        case 2:
          return "申请被拒";
        case 0:
          return "申请审核中";
        default:
          return "申请状态";
      }
    },
    // 获取申请状态描述
    getApplicationStatusDesc() {
      if (this.applicationStatus.isMatchmaker) {
        return "您已经是平台认证的红娘，无需重复申请";
      }
      switch (this.applicationStatus.statusCode) {
        case 1:
          return "恭喜您通过红娘申请审核！";
        case 2:
          return this.applicationStatus.rejectReason || "很遗憾，您的申请未通过审核";
        case 0:
          return "您的红娘申请已提交成功，已扣除699个虚拟币，请等待审核";
        default:
          return "";
      }
    },
    // 返回首页
    goBackToHome() {
      common_vendor.index.__f__("log", "at pages/matchmaker/apply.vue:788", "点击返回首页按钮");
      common_vendor.index.reLaunch({
        url: "/pages/index/index",
        success: () => {
          common_vendor.index.__f__("log", "at pages/matchmaker/apply.vue:794", "成功跳转到首页");
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/matchmaker/apply.vue:797", "跳转首页失败:", err);
          common_vendor.index.showToast({
            title: "跳转失败，请重试",
            icon: "none"
          });
        }
      });
    },
    // 重新申请
    retryApplication() {
      this.showFullscreenStatus = false;
      this.applicationStatus.hasApplied = false;
    }
  },
  // 页面加载时检查用户状态、申请状态和加载实名认证信息
  onLoad() {
    this.checkUserStatus();
    this.loadUserVerification();
    this.checkApplicationStatus();
  },
  // 页面显示时重新检查用户状态、申请状态和加载实名认证信息
  onShow() {
    this.checkUserStatus();
    this.loadUserVerification();
    this.checkApplicationStatus();
  }
};
if (!Array) {
  const _easycom_voucher_selector2 = common_vendor.resolveComponent("voucher-selector");
  _easycom_voucher_selector2();
}
const _easycom_voucher_selector = () => "../../components/voucher-selector/voucher-selector.js";
if (!Math) {
  _easycom_voucher_selector();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: $data.showFullscreenStatus
  }, $data.showFullscreenStatus ? common_vendor.e({
    c: $data.applicationStatus.statusCode === 1
  }, $data.applicationStatus.statusCode === 1 ? {} : $data.applicationStatus.statusCode === 0 ? {} : {}, {
    d: $data.applicationStatus.statusCode === 0,
    e: common_vendor.n($options.getApplicationStatusClass()),
    f: common_vendor.t($options.getApplicationStatusTitle()),
    g: common_vendor.n($options.getApplicationStatusClass()),
    h: common_vendor.t($options.getApplicationStatusDesc()),
    i: $data.applicationStatus.statusCode === 1
  }, $data.applicationStatus.statusCode === 1 ? {
    j: common_vendor.o((...args) => $options.goBackToHome && $options.goBackToHome(...args))
  } : $data.applicationStatus.statusCode === 2 ? {
    l: common_vendor.o((...args) => $options.retryApplication && $options.retryApplication(...args))
  } : {
    m: common_vendor.o((...args) => $options.goBackToHome && $options.goBackToHome(...args))
  }, {
    k: $data.applicationStatus.statusCode === 2,
    n: $data.applicationStatus.statusCode === 0
  }, $data.applicationStatus.statusCode === 0 ? {} : $data.applicationStatus.statusCode === 1 ? {} : $data.applicationStatus.statusCode === 2 && $data.applicationStatus.rejectReason ? {
    q: common_vendor.t($data.applicationStatus.rejectReason)
  } : {}, {
    o: $data.applicationStatus.statusCode === 1,
    p: $data.applicationStatus.statusCode === 2 && $data.applicationStatus.rejectReason
  }) : {}, {
    r: common_vendor.t($data.userStatus.hasPhone ? "✓" : "○"),
    s: !$data.userStatus.hasPhone
  }, !$data.userStatus.hasPhone ? {} : {}, {
    t: $data.userStatus.hasPhone ? 1 : "",
    v: !$data.userStatus.hasPhone ? 1 : "",
    w: common_vendor.o(($event) => $options.handleConditionClick("phone")),
    x: common_vendor.t($data.userStatus.hasGender ? "✓" : "○"),
    y: !$data.userStatus.hasGender
  }, !$data.userStatus.hasGender ? {} : {}, {
    z: $data.userStatus.hasGender ? 1 : "",
    A: !$data.userStatus.hasGender ? 1 : "",
    B: common_vendor.o(($event) => $options.handleConditionClick("gender")),
    C: common_vendor.t($data.userStatus.isVerified ? "✓" : "○"),
    D: !$data.userStatus.isVerified
  }, !$data.userStatus.isVerified ? {} : {}, {
    E: $data.userStatus.isVerified ? 1 : "",
    F: !$data.userStatus.isVerified ? 1 : "",
    G: common_vendor.o(($event) => $options.handleConditionClick("verify")),
    H: common_vendor.t($data.userStatus.hasEnoughCoins ? "✓" : "○"),
    I: !$data.userStatus.hasEnoughCoins
  }, !$data.userStatus.hasEnoughCoins ? {} : {}, {
    J: $data.userStatus.hasEnoughCoins ? 1 : "",
    K: !$data.userStatus.hasEnoughCoins ? 1 : "",
    L: common_vendor.o(($event) => $options.handleConditionClick("coins")),
    M: $data.verificationFields.realName ? 1 : "",
    N: $data.verificationFields.realName ? "已从实名认证自动填充" : "请输入真实姓名",
    O: $data.verificationFields.realName,
    P: $data.formData.realName,
    Q: common_vendor.o(($event) => $data.formData.realName = $event.detail.value),
    R: $data.verificationFields.realName
  }, $data.verificationFields.realName ? {} : {}, {
    S: $data.verificationFields.idCardNo ? 1 : "",
    T: $data.verificationFields.idCardNo ? "已从实名认证自动填充" : "请输入身份证号",
    U: $data.verificationFields.idCardNo,
    V: $data.formData.idCardNo,
    W: common_vendor.o(($event) => $data.formData.idCardNo = $event.detail.value),
    X: $data.verificationFields.idCardNo
  }, $data.verificationFields.idCardNo ? {} : {}, {
    Y: $data.formData.idCardFront
  }, $data.formData.idCardFront ? {
    Z: $data.formData.idCardFront
  } : {}, {
    aa: $data.verificationFields.idCardFront
  }, $data.verificationFields.idCardFront ? {} : {}, {
    ab: $data.verificationFields.idCardFront ? 1 : "",
    ac: common_vendor.o(($event) => !$data.verificationFields.idCardFront && $options.uploadIdCardFront()),
    ad: $data.formData.idCardBack
  }, $data.formData.idCardBack ? {
    ae: $data.formData.idCardBack
  } : {}, {
    af: $data.verificationFields.idCardBack
  }, $data.verificationFields.idCardBack ? {} : {}, {
    ag: $data.verificationFields.idCardBack ? 1 : "",
    ah: common_vendor.o(($event) => !$data.verificationFields.idCardBack && $options.uploadIdCardBack()),
    ai: $data.formData.serviceArea,
    aj: common_vendor.o(($event) => $data.formData.serviceArea = $event.detail.value),
    ak: $data.formData.contactPhone,
    al: common_vendor.o(($event) => $data.formData.contactPhone = $event.detail.value),
    am: $data.formData.introduction,
    an: common_vendor.o(($event) => $data.formData.introduction = $event.detail.value),
    ao: common_vendor.t($data.formData.introduction.length),
    ap: $data.formData.experience,
    aq: common_vendor.o(($event) => $data.formData.experience = $event.detail.value),
    ar: common_vendor.t($data.formData.experience.length),
    as: common_vendor.t($data.isSubmitting ? "提交中..." : "提交申请（699币）"),
    at: !$options.canSubmit || $data.isSubmitting ? 1 : "",
    av: !$options.canSubmit || $data.isSubmitting,
    aw: common_vendor.o((...args) => $options.showConfirmDialog && $options.showConfirmDialog(...args)),
    ax: $data.showConfirm
  }, $data.showConfirm ? common_vendor.e({
    ay: $data.currentBalance >= 0
  }, $data.currentBalance >= 0 ? {
    az: common_vendor.t($data.currentBalance),
    aA: $data.currentBalance < $options.getActualCost() ? 1 : ""
  } : {}, {
    aB: common_vendor.sr("voucherSelector", "0a4d6000-0"),
    aC: common_vendor.o($options.onVoucherSelected),
    aD: common_vendor.p({
      ["consume-amount"]: 699,
      ["consume-type"]: "matchmaker-apply"
    }),
    aE: $data.selectedVoucher
  }, $data.selectedVoucher ? {
    aF: common_vendor.t($data.selectedVoucher.voucher.actualValue)
  } : {}, {
    aG: common_vendor.t($options.getActualCost()),
    aH: common_vendor.o((...args) => $options.hideConfirmDialog && $options.hideConfirmDialog(...args)),
    aI: common_vendor.t($data.currentBalance < $options.getActualCost() ? "余额不足" : "确认申请"),
    aJ: $data.currentBalance < $options.getActualCost() ? 1 : "",
    aK: $data.currentBalance < $options.getActualCost(),
    aL: common_vendor.o((...args) => $options.submitApplication && $options.submitApplication(...args)),
    aM: common_vendor.o(() => {
    }),
    aN: common_vendor.o((...args) => $options.hideConfirmDialog && $options.hideConfirmDialog(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0a4d6000"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/matchmaker/apply.js.map
