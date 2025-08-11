"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_user_auth = require("../../api/user/auth.js");
const _sfc_main = {
  __name: "register",
  setup(__props) {
    const registerForm = common_vendor.reactive({
      phone: "",
      password: "",
      confirmPassword: "",
      code: ""
    });
    const imageError = common_vendor.reactive({
      bg: false,
      logo: false
    });
    const isLoading = common_vendor.ref(false);
    const sendingCode = common_vendor.ref(false);
    const isCounting = common_vendor.ref(false);
    const countdown = common_vendor.ref(60);
    let timer = null;
    function handleImageError(type) {
      imageError[type] = true;
    }
    async function getVerifyCode() {
      if (isCounting.value || sendingCode.value)
        return;
      if (!registerForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
        common_vendor.index.showToast({
          title: "手机号格式不正确",
          icon: "none"
        });
        return;
      }
      try {
        sendingCode.value = true;
        const result = await api_user_auth.sendRegisterCode(registerForm.phone);
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
        common_vendor.index.__f__("error", "at pages/register/register.vue:159", "发送验证码失败:", error);
      } finally {
        sendingCode.value = false;
      }
    }
    async function handleVerifyCode() {
      if (!registerForm.phone) {
        common_vendor.index.showToast({
          title: "请输入手机号",
          icon: "none"
        });
        return;
      }
      if (!registerForm.password) {
        common_vendor.index.showToast({
          title: "请设置密码",
          icon: "none"
        });
        return;
      }
      if (registerForm.password.length < 6) {
        common_vendor.index.showToast({
          title: "密码长度不能少于6位",
          icon: "none"
        });
        return;
      }
      if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/.test(registerForm.password)) {
        common_vendor.index.showToast({
          title: "密码必须包含英文和数字",
          icon: "none"
        });
        return;
      }
      if (registerForm.password !== registerForm.confirmPassword) {
        common_vendor.index.showToast({
          title: "两次输入的密码不一致",
          icon: "none"
        });
        return;
      }
      if (!registerForm.code) {
        common_vendor.index.showToast({
          title: "请输入验证码",
          icon: "none"
        });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
        common_vendor.index.showToast({
          title: "手机号格式不正确",
          icon: "none"
        });
        return;
      }
      isLoading.value = true;
      try {
        const result = await api_user_auth.verifyCode(registerForm.phone, registerForm.code, registerForm.password);
        common_vendor.index.__f__("log", "at pages/register/register.vue:228", "验证码验证完整响应:", result);
        let userId = "";
        if (result && result.data) {
          userId = result.data.id || result.data.userId || result.data.user_id || "";
          if (!userId && typeof result.data === "string" && result.data.trim() !== "") {
            userId = result.data;
          }
          if (!userId && Array.isArray(result.data) && result.data.length > 0) {
            const firstItem = result.data[0];
            if (firstItem && typeof firstItem === "object") {
              userId = firstItem.id || firstItem.userId || firstItem.user_id || "";
            }
          }
        }
        common_vendor.index.__f__("log", "at pages/register/register.vue:250", "提取的用户ID:", userId);
        common_vendor.index.navigateTo({
          url: `/pages/register/user-info?phone=${registerForm.phone}&password=${encodeURIComponent(registerForm.password)}&userId=${userId}`,
          success: () => {
            common_vendor.index.__f__("log", "at pages/register/register.vue:256", "成功跳转到用户信息页面");
          },
          fail: (err) => {
            common_vendor.index.__f__("error", "at pages/register/register.vue:259", "跳转失败:", err);
            common_vendor.index.showToast({
              title: "跳转失败，请稍后重试",
              icon: "none"
            });
          }
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/register/register.vue:267", "验证码验证失败:", error);
      } finally {
        isLoading.value = false;
      }
    }
    function goLogin() {
      common_vendor.index.reLaunch({
        url: "/pages/login/login",
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/register/register.vue:280", "跳转到登录页面失败:", err);
          common_vendor.index.showToast({
            title: "跳转失败，请稍后重试",
            icon: "none"
          });
        }
      });
    }
    function goAgreement(type) {
      common_vendor.index.showToast({
        title: `${type === "user" ? "用户协议" : "隐私政策"}查看功能开发中`,
        icon: "none"
      });
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
        d: registerForm.phone,
        e: common_vendor.o(($event) => registerForm.phone = $event.detail.value),
        f: registerForm.password,
        g: common_vendor.o(($event) => registerForm.password = $event.detail.value),
        h: registerForm.confirmPassword,
        i: common_vendor.o(($event) => registerForm.confirmPassword = $event.detail.value),
        j: registerForm.code,
        k: common_vendor.o(($event) => registerForm.code = $event.detail.value),
        l: common_vendor.t(isCounting.value ? `${countdown.value}s后重新获取` : "获取验证码"),
        m: isCounting.value ? 1 : "",
        n: common_vendor.o(getVerifyCode),
        o: !isLoading.value
      }, !isLoading.value ? {} : {}, {
        p: isLoading.value ? 1 : "",
        q: common_vendor.o(handleVerifyCode),
        r: isLoading.value,
        s: common_vendor.o(goLogin),
        t: common_vendor.o(($event) => goAgreement("user")),
        v: common_vendor.o(($event) => goAgreement("privacy"))
      });
    };
  }
};
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/register/register.js.map
