"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user_auth = require("../../api/user/auth.js");
const _sfc_main = {
  __name: "login",
  setup(__props) {
    const loginType = common_vendor.ref("password");
    const passwordForm = common_vendor.reactive({
      phone: "",
      password: ""
    });
    const codeForm = common_vendor.reactive({
      phone: "",
      code: ""
    });
    const imageError = common_vendor.reactive({
      bg: false,
      logo: false,
      wechat: false,
      qq: false,
      weibo: false
    });
    const isLoading = common_vendor.ref(false);
    const sendingCode = common_vendor.ref(false);
    const isCounting = common_vendor.ref(false);
    const countdown = common_vendor.ref(60);
    let timer = null;
    common_vendor.onMounted(() => {
      const token = common_vendor.index.getStorageSync("token");
      if (token) {
        common_vendor.index.__f__("log", "at pages/login/login.vue:171", "检测到已登录状态，自动跳转到首页");
        common_vendor.index.reLaunch({
          url: "/pages/index/index",
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/login/login.vue:175", "自动跳转失败:", err);
          }
        });
        return;
      }
      const lastLoginPhone = common_vendor.index.getStorageSync("lastLoginPhone");
      if (lastLoginPhone) {
        passwordForm.phone = lastLoginPhone;
        codeForm.phone = lastLoginPhone;
        common_vendor.index.__f__("log", "at pages/login/login.vue:186", "自动填充上次登录手机号:", lastLoginPhone);
      }
    });
    function switchLoginType(type) {
      loginType.value = type;
    }
    function handleImageError(type) {
      imageError[type] = true;
    }
    async function handlePasswordLogin() {
      if (!passwordForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!passwordForm.password) {
        common_vendor.index.showToast({
          title: "请输入密码",
          icon: "none"
        });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(passwordForm.phone)) {
        common_vendor.index.showToast({
          title: "手机号格式不正确",
          icon: "none"
        });
        return;
      }
      isLoading.value = true;
      try {
        const result = await api_user_auth.login(passwordForm.phone, passwordForm.password);
        if (result.data) {
          common_vendor.index.setStorageSync("token", result.data);
        }
        common_vendor.index.showToast({
          title: "登录成功",
          icon: "success",
          duration: 1500
        });
        setTimeout(() => {
          common_vendor.index.__f__("log", "at pages/login/login.vue:248", "准备跳转到首页...");
          common_vendor.index.reLaunch({
            url: "/pages/index/index",
            success: () => {
              common_vendor.index.__f__("log", "at pages/login/login.vue:253", "跳转首页成功");
              setTimeout(() => {
                triggerProvinceUpdate();
              }, 2e3);
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/login/login.vue:260", "跳转失败:", err);
              common_vendor.index.showToast({
                title: "跳转失败，请稍后重试",
                icon: "none"
              });
            }
          });
        }, 1500);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:269", "登录失败:", error);
      } finally {
        isLoading.value = false;
      }
    }
    async function handleCodeLogin() {
      if (!codeForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!codeForm.code) {
        common_vendor.index.showToast({
          title: "请输入验证码",
          icon: "none"
        });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(codeForm.phone)) {
        common_vendor.index.showToast({
          title: "手机号格式不正确",
          icon: "none"
        });
        return;
      }
      isLoading.value = true;
      try {
        const result = await api_user_auth.loginByCode(codeForm.phone, codeForm.code);
        common_vendor.index.setStorageSync("token", result.data);
        common_vendor.index.__f__("log", "at pages/login/login.vue:310", "用户信息已保存到本地", result.data);
        common_vendor.index.showToast({
          title: "登录成功",
          icon: "success",
          duration: 1500
        });
        setTimeout(() => {
          common_vendor.index.__f__("log", "at pages/login/login.vue:321", "准备跳转到首页...");
          common_vendor.index.reLaunch({
            url: "/pages/index/index",
            success: () => {
              common_vendor.index.__f__("log", "at pages/login/login.vue:326", "跳转首页成功");
              setTimeout(() => {
                triggerProvinceUpdate();
              }, 2e3);
            },
            fail: (err) => {
              common_vendor.index.__f__("error", "at pages/login/login.vue:333", "跳转失败:", err);
              common_vendor.index.showToast({
                title: "跳转失败，请稍后重试",
                icon: "none"
              });
            }
          });
        }, 1500);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:342", "登录失败:", error);
      } finally {
        isLoading.value = false;
      }
    }
    async function getVerifyCode() {
      if (isCounting.value || sendingCode.value)
        return;
      if (!codeForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(codeForm.phone)) {
        common_vendor.index.showToast({
          title: "手机号格式不正确",
          icon: "none"
        });
        return;
      }
      try {
        sendingCode.value = true;
        const result = await api_user_auth.sendLoginCode(codeForm.phone);
        isCounting.value = true;
        countdown.value = 60;
        timer = setInterval(() => {
          countdown.value--;
          if (countdown.value <= 0) {
            clearInterval(timer);
            isCounting.value = false;
          }
        }, 1e3);
        common_vendor.index.showToast({
          title: result.message || "验证码发送成功",
          icon: "success"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:394", "发送验证码失败:", error);
      } finally {
        sendingCode.value = false;
      }
    }
    function forgetPassword() {
      common_vendor.index.showToast({
        title: "忘记密码功能开发中",
        icon: "none"
      });
    }
    function goRegister() {
      common_vendor.index.reLaunch({
        url: "/pages/register/register",
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/login/login.vue:414", "跳转到注册页面失败:", err);
          common_vendor.index.showToast({
            title: "跳转失败，请稍后重试",
            icon: "none"
          });
        }
      });
    }
    function thirdLogin(type) {
      common_vendor.index.showToast({
        title: `${type}登录功能开发中`,
        icon: "none"
      });
    }
    function goAgreement(type) {
      common_vendor.index.showToast({
        title: `${type === "user" ? "用户协议" : "隐私政策"}查看功能开发中`,
        icon: "none"
      });
    }
    function triggerProvinceUpdate() {
      try {
        common_vendor.index.$emit("refreshUserProvince");
        common_vendor.index.__f__("log", "at pages/login/login.vue:444", "已触发省份信息更新事件");
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/login/login.vue:446", "触发省份更新失败:", error);
      }
    }
    common_vendor.onUnmounted(() => {
      if (timer) {
        clearInterval(timer);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: imageError.logo
      }, imageError.logo ? {} : {}, {
        b: common_assets._imports_0$3,
        c: common_vendor.o(($event) => handleImageError("logo")),
        d: loginType.value === "password" ? 1 : "",
        e: common_vendor.o(($event) => switchLoginType("password")),
        f: loginType.value === "code" ? 1 : "",
        g: common_vendor.o(($event) => switchLoginType("code")),
        h: loginType.value === "password"
      }, loginType.value === "password" ? common_vendor.e({
        i: passwordForm.phone,
        j: common_vendor.o(($event) => passwordForm.phone = $event.detail.value),
        k: passwordForm.password,
        l: common_vendor.o(($event) => passwordForm.password = $event.detail.value),
        m: common_vendor.o(forgetPassword),
        n: !isLoading.value
      }, !isLoading.value ? {} : {}, {
        o: isLoading.value ? 1 : "",
        p: common_vendor.o(handlePasswordLogin),
        q: isLoading.value
      }) : common_vendor.e({
        r: codeForm.phone,
        s: common_vendor.o(($event) => codeForm.phone = $event.detail.value),
        t: codeForm.code,
        v: common_vendor.o(($event) => codeForm.code = $event.detail.value),
        w: common_vendor.t(isCounting.value ? `${countdown.value}s后重新获取` : "获取验证码"),
        x: isCounting.value ? 1 : "",
        y: common_vendor.o(getVerifyCode),
        z: !isLoading.value
      }, !isLoading.value ? {} : {}, {
        A: isLoading.value ? 1 : "",
        B: common_vendor.o(handleCodeLogin),
        C: isLoading.value
      }), {
        D: common_vendor.o(goRegister),
        E: imageError.wechat
      }, imageError.wechat ? {} : {}, {
        F: common_assets._imports_1$2,
        G: common_vendor.o(($event) => handleImageError("wechat")),
        H: common_vendor.o(($event) => thirdLogin("wechat")),
        I: imageError.qq
      }, imageError.qq ? {} : {}, {
        J: common_assets._imports_2,
        K: common_vendor.o(($event) => handleImageError("qq")),
        L: common_vendor.o(($event) => thirdLogin("qq")),
        M: imageError.weibo
      }, imageError.weibo ? {} : {}, {
        N: common_assets._imports_3,
        O: common_vendor.o(($event) => handleImageError("weibo")),
        P: common_vendor.o(($event) => thirdLogin("weibo")),
        Q: common_vendor.o(($event) => goAgreement("user")),
        R: common_vendor.o(($event) => goAgreement("privacy"))
      });
    };
  }
};
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/login/login.js.map
