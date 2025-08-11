"use strict";
const common_vendor = require("../../common/vendor.js");
const api_http = require("../../api/http.js");
const api_user_auth = require("../../api/user/auth.js");
const _sfc_main = {
  data() {
    return {
      formData: {
        realName: "",
        idCardNo: "",
        idCardFront: "",
        idCardBack: "",
        facePhoto: ""
      },
      verificationStatus: null,
      isSubmitting: false,
      isPreVerifying: false,
      preVerifyPassed: false,
      showPreVerifyBtn: false
    };
  },
  computed: {
    canSubmit() {
      const { realName, idCardNo, idCardFront, idCardBack, facePhoto } = this.formData;
      return realName && idCardNo && idCardFront && idCardBack && facePhoto && this.preVerifyPassed;
    },
    canPreVerify() {
      return this.formData.realName && this.formData.idCardNo && !this.preVerifyPassed;
    }
  },
  watch: {
    "formData.realName"() {
      this.checkShowPreVerifyBtn();
    },
    "formData.idCardNo"() {
      this.checkShowPreVerifyBtn();
    }
  },
  methods: {
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 检查是否显示预验证按钮
    checkShowPreVerifyBtn() {
      this.showPreVerifyBtn = this.formData.realName && this.formData.idCardNo && !this.preVerifyPassed;
    },
    // 身份证预验证
    async preVerifyIdCard() {
      if (!this.canPreVerify) {
        return;
      }
      this.isPreVerifying = true;
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const response = await api_http.http.post("/user/verification/preVerify", {
          realName: this.formData.realName,
          idCardNo: this.formData.idCardNo
        }, {
          headers: { "token": token }
        });
        if (response.code === 200) {
          this.preVerifyPassed = true;
          this.showPreVerifyBtn = false;
          common_vendor.index.showToast({
            title: "身份证验证成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showModal({
            title: "身份证验证失败",
            content: response.message || "请检查姓名和身份证号是否正确",
            confirmText: "确定",
            showCancel: false
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/verify.vue:291", "身份证预验证失败:", error);
        common_vendor.index.showToast({
          title: "验证失败，请重试",
          icon: "none"
        });
      } finally {
        this.isPreVerifying = false;
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
            success: async (res2) => {
              const filePath = res2.tempFilePaths[0];
              common_vendor.index.showLoading({
                title: "上传中..."
              });
              try {
                const uploadResult = await api_user_auth.uploadIdCardFront(filePath);
                if (uploadResult.code === 200) {
                  this.formData.idCardFront = uploadResult.data;
                  common_vendor.index.showToast({
                    title: "上传成功",
                    icon: "success"
                  });
                } else {
                  throw new Error(uploadResult.message || "上传失败");
                }
              } catch (error) {
                common_vendor.index.__f__("error", "at pages/profile/verify.vue:334", "上传身份证正面失败:", error);
                common_vendor.index.showToast({
                  title: "上传失败，请重试",
                  icon: "none"
                });
              } finally {
                common_vendor.index.hideLoading();
              }
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/profile/verify.vue:344", "选择图片失败:", err);
              common_vendor.index.showToast({
                title: "选择图片失败，请重试",
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
            success: async (res2) => {
              const filePath = res2.tempFilePaths[0];
              common_vendor.index.showLoading({
                title: "上传中..."
              });
              try {
                const uploadResult = await api_user_auth.uploadIdCardBack(filePath);
                if (uploadResult.code === 200) {
                  this.formData.idCardBack = uploadResult.data;
                  common_vendor.index.showToast({
                    title: "上传成功",
                    icon: "success"
                  });
                } else {
                  throw new Error(uploadResult.message || "上传失败");
                }
              } catch (error) {
                common_vendor.index.__f__("error", "at pages/profile/verify.vue:388", "上传身份证背面失败:", error);
                common_vendor.index.showToast({
                  title: "上传失败，请重试",
                  icon: "none"
                });
              } finally {
                common_vendor.index.hideLoading();
              }
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/profile/verify.vue:398", "选择图片失败:", err);
              common_vendor.index.showToast({
                title: "选择图片失败，请重试",
                icon: "none"
              });
            }
          });
        }
      });
    },
    // 上传人脸照片
    uploadFacePhoto() {
      common_vendor.index.showActionSheet({
        itemList: ["拍照", "从相册选择"],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.takePhoto();
          } else if (res.tapIndex === 1) {
            this.chooseFromAlbum();
          }
        }
      });
    },
    // 上传人脸照片到MinIO
    async uploadFacePhotoToMinio(filePath) {
      common_vendor.index.showLoading({
        title: "上传中..."
      });
      try {
        const uploadResult = await api_user_auth.uploadFacePhoto(filePath);
        if (uploadResult.code === 200) {
          this.formData.facePhoto = uploadResult.data;
          common_vendor.index.showToast({
            title: "上传成功",
            icon: "success"
          });
        } else {
          throw new Error(uploadResult.message || "上传失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/verify.vue:444", "上传人脸照片失败:", error);
        common_vendor.index.showToast({
          title: "上传失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    },
    // 拍照
    takePhoto() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["camera"],
        success: async (res) => {
          const filePath = res.tempFilePaths[0];
          await this.uploadFacePhotoToMinio(filePath);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/profile/verify.vue:489", "拍照失败:", err);
          common_vendor.index.showToast({
            title: "拍照失败，请重试",
            icon: "none"
          });
        }
      });
    },
    // 备用拍照方法（后置摄像头）
    fallbackTakePhoto() {
    },
    // 从相册选择
    chooseFromAlbum() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album"],
        success: async (res) => {
          const filePath = res.tempFilePaths[0];
          await this.uploadFacePhotoToMinio(filePath);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/profile/verify.vue:533", "选择照片失败:", err);
          common_vendor.index.showToast({
            title: "选择照片失败",
            icon: "none"
          });
        }
      });
    },
    // 提交认证
    async submitVerification() {
      if (!this.canSubmit) {
        return;
      }
      this.isSubmitting = true;
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const response = await api_http.http.post("/user/verification/submit", this.formData, {
          headers: { "token": token }
        });
        if (response.code === 200) {
          common_vendor.index.showModal({
            title: "提交成功",
            content: "您的实名认证资料已提交，请等待审核。",
            confirmText: "确定",
            showCancel: false,
            success: () => {
              this.loadVerificationStatus();
            }
          });
        } else {
          common_vendor.index.showToast({
            title: response.message || "提交失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/verify.vue:581", "提交认证失败:", error);
        common_vendor.index.showToast({
          title: "提交失败，请重试",
          icon: "none"
        });
      } finally {
        this.isSubmitting = false;
      }
    },
    // 加载认证状态
    async loadVerificationStatus() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          return;
        }
        const response = await api_http.http.get("/user/verification/status", {
          headers: { "token": token }
        });
        if (response.code === 200) {
          this.verificationStatus = response.data;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/verify.vue:607", "获取认证状态失败:", error);
      }
    },
    // 获取状态样式类
    getStatusClass() {
      if (!this.verificationStatus)
        return "";
      switch (this.verificationStatus.status) {
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
    // 获取状态标题
    getStatusTitle() {
      if (!this.verificationStatus)
        return "";
      switch (this.verificationStatus.status) {
        case 1:
          return "认证成功";
        case 2:
          return "认证失败";
        case 0:
          return "审核中";
        default:
          return "";
      }
    },
    // 获取状态描述
    getStatusDesc() {
      if (!this.verificationStatus)
        return "";
      switch (this.verificationStatus.status) {
        case 1:
          return "您已通过实名认证";
        case 2:
          return this.verificationStatus.rejectReason || "请重新提交认证资料";
        case 0:
          return "您的认证资料正在审核中，请耐心等待";
        default:
          return "";
      }
    },
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 重新认证
    retryVerification() {
      this.verificationStatus = null;
    }
  },
  onLoad() {
    this.loadVerificationStatus();
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: $data.verificationStatus && ($data.verificationStatus.status === 0 || $data.verificationStatus.status === 1)
  }, $data.verificationStatus && ($data.verificationStatus.status === 0 || $data.verificationStatus.status === 1) ? common_vendor.e({
    c: $data.verificationStatus.status === 1
  }, $data.verificationStatus.status === 1 ? {} : {}, {
    d: common_vendor.n($options.getStatusClass()),
    e: common_vendor.t($options.getStatusTitle()),
    f: common_vendor.n($options.getStatusClass()),
    g: common_vendor.t($options.getStatusDesc()),
    h: $data.verificationStatus.status === 1
  }, $data.verificationStatus.status === 1 ? {
    i: common_vendor.o((...args) => $options.goBack && $options.goBack(...args))
  } : {
    j: common_vendor.o((...args) => $options.goBack && $options.goBack(...args))
  }, {
    k: $data.verificationStatus.status === 0
  }, $data.verificationStatus.status === 0 ? {} : $data.verificationStatus.status === 1 ? {} : {}, {
    l: $data.verificationStatus.status === 1
  }) : {}, {
    m: $data.verificationStatus && $data.verificationStatus.status === 2
  }, $data.verificationStatus && $data.verificationStatus.status === 2 ? {
    n: common_vendor.t($data.verificationStatus.rejectReason || "请重新提交认证资料")
  } : {}, {
    o: !$data.verificationStatus || $data.verificationStatus.status === 2
  }, !$data.verificationStatus || $data.verificationStatus.status === 2 ? common_vendor.e({
    p: $data.formData.realName,
    q: common_vendor.o(($event) => $data.formData.realName = $event.detail.value),
    r: $data.preVerifyPassed,
    s: $data.formData.idCardNo,
    t: common_vendor.o(($event) => $data.formData.idCardNo = $event.detail.value),
    v: $data.showPreVerifyBtn
  }, $data.showPreVerifyBtn ? common_vendor.e({
    w: common_vendor.t($data.isPreVerifying ? "验证中..." : $data.preVerifyPassed ? "✓ 验证通过" : "验证身份证信息"),
    x: !$options.canPreVerify || $data.isPreVerifying ? 1 : "",
    y: !$options.canPreVerify || $data.isPreVerifying,
    z: common_vendor.o((...args) => $options.preVerifyIdCard && $options.preVerifyIdCard(...args)),
    A: !$data.preVerifyPassed
  }, !$data.preVerifyPassed ? {} : {}, {
    B: $data.preVerifyPassed
  }, $data.preVerifyPassed ? {} : {}) : {}, {
    C: $data.preVerifyPassed
  }, $data.preVerifyPassed ? common_vendor.e({
    D: $data.formData.idCardFront
  }, $data.formData.idCardFront ? {
    E: $data.formData.idCardFront
  } : {}, {
    F: common_vendor.o((...args) => $options.uploadIdCardFront && $options.uploadIdCardFront(...args)),
    G: $data.formData.idCardBack
  }, $data.formData.idCardBack ? {
    H: $data.formData.idCardBack
  } : {}, {
    I: common_vendor.o((...args) => $options.uploadIdCardBack && $options.uploadIdCardBack(...args))
  }) : {}, {
    J: $data.preVerifyPassed
  }, $data.preVerifyPassed ? common_vendor.e({
    K: $data.formData.facePhoto
  }, $data.formData.facePhoto ? {
    L: $data.formData.facePhoto
  } : {}, {
    M: common_vendor.o((...args) => $options.uploadFacePhoto && $options.uploadFacePhoto(...args))
  }) : {}, {
    N: common_vendor.t($data.isSubmitting ? "提交中..." : "提交认证"),
    O: !$options.canSubmit || $data.isSubmitting ? 1 : "",
    P: !$options.canSubmit || $data.isSubmitting,
    Q: common_vendor.o((...args) => $options.submitVerification && $options.submitVerification(...args))
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0afa9e63"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/verify.js.map
