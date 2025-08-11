"use strict";
const common_vendor = require("../../common/vendor.js");
require("../../api/http.js");
const api_profile = require("../../api/profile.js");
const _sfc_main = {
  __name: "edit-profile",
  setup(__props) {
    const profileForm = common_vendor.reactive({
      realName: "",
      age: "",
      height: "",
      weight: "",
      education: 0,
      educationCert: "",
      company: "",
      position: "",
      incomeLevel: 0,
      workCity: "",
      hometown: "",
      maritalStatus: 0,
      hasChildren: 0,
      houseStatus: 0,
      carStatus: 0,
      selfIntroduction: "",
      hobby: ""
    });
    const isEdit = common_vendor.ref(false);
    const isLoading = common_vendor.ref(false);
    const educationOptions = ["请选择", "高中及以下", "大专", "本科", "硕士", "博士及以上"];
    const educationIndex = common_vendor.ref(0);
    const incomeOptions = ["请选择", "5千以下", "5千-1万", "1万-2万", "2万-5万", "5万以上"];
    const incomeIndex = common_vendor.ref(0);
    const maritalStatusOptions = ["请选择", "未婚", "离异", "丧偶"];
    const maritalStatusIndex = common_vendor.ref(0);
    const hasChildrenOptions = ["请选择", "无子女", "有子女不同住", "有子女同住"];
    const hasChildrenIndex = common_vendor.ref(0);
    const houseStatusOptions = ["请选择", "无房", "有房有贷款", "有房无贷款", "与父母同住"];
    const houseStatusIndex = common_vendor.ref(0);
    const carStatusOptions = ["请选择", "无车", "有车有贷款", "有车无贷款"];
    const carStatusIndex = common_vendor.ref(0);
    common_vendor.onMounted(async () => {
      await fetchProfileData(21);
    });
    async function fetchProfileData(id) {
      try {
        isLoading.value = true;
        const result = await api_profile.profileApi.getProfile(id);
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:236", "获取到的用户资料:", result);
        if (result && result.data && result.data.profile) {
          isEdit.value = true;
          Object.assign(profileForm, result.data.profile);
          setPickerIndexes();
          common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:249", "数据回显成功，进入编辑模式");
        } else {
          isEdit.value = false;
          common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:253", "未获取到资料数据，进入新增模式");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:256", "获取资料失败:", error);
        isEdit.value = false;
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:259", "获取资料失败，进入新增模式");
      } finally {
        isLoading.value = false;
      }
    }
    function setPickerIndexes() {
      educationIndex.value = profileForm.education || 0;
      incomeIndex.value = profileForm.incomeLevel || 0;
      maritalStatusIndex.value = profileForm.maritalStatus || 0;
      hasChildrenIndex.value = profileForm.hasChildren || 0;
      houseStatusIndex.value = profileForm.houseStatus || 0;
      carStatusIndex.value = profileForm.carStatus || 0;
    }
    function onEducationChange(e) {
      educationIndex.value = e.detail.value;
      profileForm.education = parseInt(e.detail.value);
    }
    function onIncomeChange(e) {
      incomeIndex.value = e.detail.value;
      profileForm.incomeLevel = parseInt(e.detail.value);
    }
    function onMaritalStatusChange(e) {
      maritalStatusIndex.value = e.detail.value;
      profileForm.maritalStatus = parseInt(e.detail.value);
    }
    function onHasChildrenChange(e) {
      hasChildrenIndex.value = e.detail.value;
      profileForm.hasChildren = parseInt(e.detail.value);
    }
    function onHouseStatusChange(e) {
      houseStatusIndex.value = e.detail.value;
      profileForm.houseStatus = parseInt(e.detail.value);
    }
    function onCarStatusChange(e) {
      carStatusIndex.value = e.detail.value;
      profileForm.carStatus = parseInt(e.detail.value);
    }
    function chooseEducationCert() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:314", "选择的证明图片路径:", tempFilePath);
          uploadCertificate(tempFilePath);
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:320", "选择图片失败:", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    }
    function uploadCertificate(filePath) {
      common_vendor.index.showLoading({
        title: "上传中..."
      });
      api_profile.profileApi.uploadCertificate(filePath).then((result) => {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:339", "服务器返回的URL:", result);
        if (!result || result.indexOf("http") === -1) {
          common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:342", "服务器返回的URL格式不正确:", result);
          common_vendor.index.showToast({
            title: "上传失败，请重试",
            icon: "none"
          });
          return;
        }
        profileForm.educationCert = result;
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:352", "设置证明URL成功:", profileForm.educationCert);
        common_vendor.index.showToast({
          title: "上传成功",
          icon: "success"
        });
      }).catch((err) => {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:361", "上传失败:", err);
        common_vendor.index.showToast({
          title: "上传失败",
          icon: "none"
        });
      });
    }
    async function handleSubmit() {
      if (!validateForm()) {
        return;
      }
      isLoading.value = true;
      try {
        let result;
        if (isEdit.value) {
          result = await api_profile.profileApi.updateProfile(profileForm);
        } else {
          result = await api_profile.profileApi.createProfile(profileForm);
        }
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:388", "提交结果:", result);
        common_vendor.index.showToast({
          title: isEdit.value ? "修改成功" : "提交成功",
          icon: "success",
          duration: 2e3
        });
        setTimeout(() => {
          common_vendor.index.navigateBack();
        }, 2e3);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:401", "提交失败:", error);
        common_vendor.index.showToast({
          title: "提交失败，请重试",
          icon: "none"
        });
      } finally {
        isLoading.value = false;
      }
    }
    function validateForm() {
      if (!profileForm.realName) {
        showError("请输入真实姓名");
        return false;
      }
      if (!profileForm.age) {
        showError("请输入年龄");
        return false;
      }
      if (!profileForm.height) {
        showError("请输入身高");
        return false;
      }
      if (!profileForm.weight) {
        showError("请输入体重");
        return false;
      }
      if (profileForm.education === 0) {
        showError("请选择学历");
        return false;
      }
      if (profileForm.incomeLevel === 0) {
        showError("请选择收入水平");
        return false;
      }
      if (!profileForm.workCity) {
        showError("请输入工作城市");
        return false;
      }
      if (profileForm.maritalStatus === 0) {
        showError("请选择婚姻状况");
        return false;
      }
      if (profileForm.hasChildren === 0) {
        showError("请选择有无子女");
        return false;
      }
      if (profileForm.houseStatus === 0) {
        showError("请选择住房情况");
        return false;
      }
      if (profileForm.carStatus === 0) {
        showError("请选择车辆情况");
        return false;
      }
      if (!profileForm.selfIntroduction) {
        showError("请填写个人介绍");
        return false;
      }
      return true;
    }
    function showError(message) {
      common_vendor.index.showToast({
        title: message,
        icon: "none"
      });
    }
    function goBack() {
      common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:488", "点击返回按钮");
      const pages = getCurrentPages();
      common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:492", "当前页面栈长度:", pages.length);
      if (pages.length > 1) {
        common_vendor.index.navigateBack({
          delta: 1,
          success: function() {
            common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:499", "返回上一页成功");
          },
          fail: function(err) {
            common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:502", "返回上一页失败:", err);
            goToMinePage();
          }
        });
      } else {
        common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:509", "没有上一页，跳转到我的页面");
        goToMinePage();
      }
    }
    function goToMinePage() {
      common_vendor.index.reLaunch({
        url: "/pages/mine/mine",
        success: function() {
          common_vendor.index.__f__("log", "at pages/profile/edit-profile.vue:519", "跳转到我的页面成功");
        },
        fail: function(err) {
          common_vendor.index.__f__("error", "at pages/profile/edit-profile.vue:522", "跳转到我的页面失败:", err);
          common_vendor.index.reLaunch({
            url: "/pages/index/index"
          });
        }
      });
    }
    return (_ctx, _cache) => {
      var _a, _b;
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: !isLoading.value
      }, !isLoading.value ? {
        c: common_vendor.t(isEdit.value ? "保存" : "提交")
      } : {}, {
        d: common_vendor.o(handleSubmit),
        e: isLoading.value ? 1 : "",
        f: profileForm.realName,
        g: common_vendor.o(($event) => profileForm.realName = $event.detail.value),
        h: profileForm.age,
        i: common_vendor.o(($event) => profileForm.age = $event.detail.value),
        j: profileForm.height,
        k: common_vendor.o(($event) => profileForm.height = $event.detail.value),
        l: profileForm.weight,
        m: common_vendor.o(($event) => profileForm.weight = $event.detail.value),
        n: common_vendor.t(educationOptions[educationIndex.value] || "请选择学历"),
        o: common_vendor.o(onEducationChange),
        p: educationIndex.value,
        q: educationOptions,
        r: !profileForm.educationCert
      }, !profileForm.educationCert ? {} : {}, {
        s: common_vendor.o(chooseEducationCert),
        t: profileForm.company,
        v: common_vendor.o(($event) => profileForm.company = $event.detail.value),
        w: profileForm.position,
        x: common_vendor.o(($event) => profileForm.position = $event.detail.value),
        y: common_vendor.t(incomeOptions[incomeIndex.value] || "请选择收入水平"),
        z: common_vendor.o(onIncomeChange),
        A: incomeIndex.value,
        B: incomeOptions,
        C: profileForm.workCity,
        D: common_vendor.o(($event) => profileForm.workCity = $event.detail.value),
        E: profileForm.hometown,
        F: common_vendor.o(($event) => profileForm.hometown = $event.detail.value),
        G: common_vendor.t(maritalStatusOptions[maritalStatusIndex.value] || "请选择婚姻状况"),
        H: common_vendor.o(onMaritalStatusChange),
        I: maritalStatusIndex.value,
        J: maritalStatusOptions,
        K: common_vendor.t(hasChildrenOptions[hasChildrenIndex.value] || "请选择有无子女"),
        L: common_vendor.o(onHasChildrenChange),
        M: hasChildrenIndex.value,
        N: hasChildrenOptions,
        O: common_vendor.t(houseStatusOptions[houseStatusIndex.value] || "请选择住房情况"),
        P: common_vendor.o(onHouseStatusChange),
        Q: houseStatusIndex.value,
        R: houseStatusOptions,
        S: common_vendor.t(carStatusOptions[carStatusIndex.value] || "请选择车辆情况"),
        T: common_vendor.o(onCarStatusChange),
        U: carStatusIndex.value,
        V: carStatusOptions,
        W: profileForm.selfIntroduction,
        X: common_vendor.o(($event) => profileForm.selfIntroduction = $event.detail.value),
        Y: common_vendor.t(((_a = profileForm.selfIntroduction) == null ? void 0 : _a.length) || 0),
        Z: profileForm.hobby,
        aa: common_vendor.o(($event) => profileForm.hobby = $event.detail.value),
        ab: common_vendor.t(((_b = profileForm.hobby) == null ? void 0 : _b.length) || 0)
      });
    };
  }
};
wx.createPage(_sfc_main);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/profile/edit-profile.js.map
