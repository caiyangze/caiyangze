"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_http = require("../../api/http.js");
const _sfc_main = {
  __name: "user-info",
  setup(__props) {
    const userForm = common_vendor.reactive({
      phone: "",
      password: "",
      nickname: "",
      gender: 0,
      // 0: 未选择, 1: 男, 2: 女
      userId: "",
      // 用户ID
      avatar: "",
      // 用于显示的头像
      avatarUrl: ""
      // 用于提交给后端的URL
    });
    const imageError = common_vendor.reactive({
      logo: false
    });
    const isLoading = common_vendor.ref(false);
    common_vendor.onMounted(() => {
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const options = currentPage.options || {};
      common_vendor.index.__f__("log", "at pages/register/user-info.vue:108", "页面参数:", options);
      if (options.phone) {
        userForm.phone = options.phone;
        common_vendor.index.__f__("log", "at pages/register/user-info.vue:112", "设置手机号:", userForm.phone);
      } else {
        common_vendor.index.showToast({
          title: "未获取到手机号，请重新注册",
          icon: "none",
          complete: () => {
            setTimeout(() => {
              common_vendor.index.reLaunch({
                url: "/pages/register/register"
              });
            }, 1500);
          }
        });
      }
      if (options.password) {
        userForm.password = decodeURIComponent(options.password);
        common_vendor.index.__f__("log", "at pages/register/user-info.vue:129", "设置密码:", "已获取(不显示)");
      }
      if (options.userId) {
        userForm.userId = options.userId;
        common_vendor.index.__f__("log", "at pages/register/user-info.vue:134", "设置用户ID:", userForm.userId);
      } else {
        common_vendor.index.__f__("warn", "at pages/register/user-info.vue:136", "未获取到用户ID");
      }
    });
    function handleImageError(type) {
      imageError[type] = true;
    }
    function selectGender(gender) {
      userForm.gender = gender;
    }
    function chooseAvatar() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          common_vendor.index.__f__("log", "at pages/register/user-info.vue:158", "选择的本地图片路径(blob):", tempFilePath);
          userForm.avatar = tempFilePath;
          uploadAvatar(tempFilePath);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/register/user-info.vue:167", "选择图片失败:", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    }
    function uploadAvatar(filePath) {
      common_vendor.index.showLoading({
        title: "上传中..."
      });
      common_vendor.index.uploadFile({
        url: "http://localhost:9001/upload/avatar",
        filePath,
        name: "file",
        formData: {},
        success: (uploadRes) => {
          common_vendor.index.hideLoading();
          const result = uploadRes.data;
          common_vendor.index.__f__("log", "at pages/register/user-info.vue:192", "服务器返回的URL:", result);
          if (!result || result.indexOf("http") === -1) {
            common_vendor.index.__f__("error", "at pages/register/user-info.vue:195", "服务器返回的URL格式不正确:", result);
            common_vendor.index.showToast({
              title: "头像上传失败，请重试",
              icon: "none"
            });
            return;
          }
          userForm.avatarUrl = result;
          userForm.avatar = result;
          common_vendor.index.__f__("log", "at pages/register/user-info.vue:208", "设置后的头像URL:", userForm.avatarUrl);
          common_vendor.index.__f__("log", "at pages/register/user-info.vue:209", "设置后的显示头像:", userForm.avatar);
          common_vendor.index.showToast({
            title: "上传成功",
            icon: "success"
          });
        },
        fail: (err) => {
          common_vendor.index.hideLoading();
          common_vendor.index.__f__("error", "at pages/register/user-info.vue:218", "上传失败:", err);
          common_vendor.index.showToast({
            title: "上传失败",
            icon: "none"
          });
        }
      });
    }
    async function handleRegister() {
      if (!userForm.nickname) {
        common_vendor.index.showToast({
          title: "请输入昵称",
          icon: "none"
        });
        return;
      }
      if (userForm.gender === 0) {
        common_vendor.index.showToast({
          title: "请选择性别",
          icon: "none"
        });
        return;
      }
      common_vendor.index.__f__("log", "at pages/register/user-info.vue:247", "提交前检查头像URL:", userForm.avatarUrl);
      if (!userForm.avatarUrl || userForm.avatarUrl.startsWith("blob:")) {
        common_vendor.index.__f__("error", "at pages/register/user-info.vue:249", "检测到头像URL仍然是blob URL或为空:", userForm.avatarUrl);
        userForm.avatarUrl = "http://182.254.244.209:9000/lovelink/avatar/2025/07/14/a1f1457c03284f3b9980e616d7b1ad4f.jpg";
        common_vendor.index.__f__("log", "at pages/register/user-info.vue:253", "已设置默认头像URL:", userForm.avatarUrl);
        common_vendor.index.showToast({
          title: "使用默认头像继续",
          icon: "none"
        });
      }
      isLoading.value = true;
      const submitData = {
        phone: userForm.phone,
        password: userForm.password,
        nickname: userForm.nickname,
        gender: userForm.gender,
        avatarUrl: userForm.avatarUrl
      };
      if (userForm.userId && userForm.userId.trim() !== "") {
        submitData.id = userForm.userId;
      }
      common_vendor.index.__f__("log", "at pages/register/user-info.vue:278", "最终提交的注册数据:", submitData);
      try {
        const result = await api_http.http.post("/user/register", submitData);
        if (result.data && result.data.token) {
          common_vendor.index.setStorageSync("token", result.data.token);
        }
        common_vendor.index.showToast({
          title: "注册成功",
          icon: "success",
          duration: 1500
        });
        setTimeout(() => {
          common_vendor.index.__f__("log", "at pages/register/user-info.vue:297", "准备跳转到首页...");
          common_vendor.index.reLaunch({
            url: "/pages/index/index",
            success: () => {
              common_vendor.index.__f__("log", "at pages/register/user-info.vue:302", "跳转首页成功");
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/register/user-info.vue:305", "跳转失败:", err);
              common_vendor.index.showToast({
                title: "跳转失败，请稍后重试",
                icon: "none"
              });
            }
          });
        }, 1500);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/register/user-info.vue:314", "注册失败:", error);
      } finally {
        isLoading.value = false;
      }
    }
    function goAgreement(type) {
      common_vendor.index.showToast({
        title: `${type === "user" ? "用户协议" : "隐私政策"}查看功能开发中`,
        icon: "none"
      });
    }
    function skipToLogin() {
      if (userForm.phone && userForm.password) {
        common_vendor.index.setStorageSync("lastLoginPhone", userForm.phone);
      }
      common_vendor.index.showToast({
        title: "跳过信息完善",
        icon: "none",
        duration: 1500,
        success: () => {
          setTimeout(() => {
            common_vendor.index.reLaunch({
              url: "/pages/login/login",
              fail: (err) => {
                common_vendor.index.__f__("error", "at pages/register/user-info.vue:346", "跳转失败:", err);
                common_vendor.index.showToast({
                  title: "跳转失败，请稍后重试",
                  icon: "none"
                });
              }
            });
          }, 1500);
        }
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: imageError.logo
      }, imageError.logo ? {} : {}, {
        b: common_assets._imports_0$3,
        c: common_vendor.o(($event) => handleImageError("logo")),
        d: userForm.avatar
      }, userForm.avatar ? {
        e: userForm.avatar
      } : {}, {
        f: common_vendor.o(chooseAvatar),
        g: userForm.nickname,
        h: common_vendor.o(($event) => userForm.nickname = $event.detail.value),
        i: userForm.gender === 1 ? 1 : "",
        j: common_vendor.o(($event) => selectGender(1)),
        k: userForm.gender === 2 ? 1 : "",
        l: common_vendor.o(($event) => selectGender(2)),
        m: !isLoading.value
      }, !isLoading.value ? {} : {}, {
        n: isLoading.value ? 1 : "",
        o: common_vendor.o(handleRegister),
        p: isLoading.value,
        q: common_vendor.o(skipToLogin),
        r: common_vendor.o(($event) => goAgreement("user")),
        s: common_vendor.o(($event) => goAgreement("privacy"))
      });
    };
  }
};
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/register/user-info.js.map
