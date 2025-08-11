"use strict";
const common_vendor = require("../../common/vendor.js");
const api_moment = require("../../api/moment.js");
const _sfc_main = {
  __name: "publish",
  setup(__props) {
    const momentData = common_vendor.reactive({
      content: "",
      location: "",
      visibility: 1
      // 1-公开，2-仅关注者，3-仅自己
    });
    const selectedImages = common_vendor.ref([]);
    const publishing = common_vendor.ref(false);
    const canPublish = common_vendor.computed(() => {
      return momentData.content.trim().length > 0 && !publishing.value;
    });
    const visibilityText = common_vendor.computed(() => {
      const texts = { 1: "公开", 2: "仅关注者可见", 3: "仅自己可见" };
      return texts[momentData.visibility];
    });
    function chooseImage() {
      const remainCount = 9 - selectedImages.value.length;
      common_vendor.index.chooseImage({
        count: remainCount,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          res.tempFilePaths.forEach((path) => {
            selectedImages.value.push({
              url: path,
              path
            });
          });
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/moment/publish.vue:143", "选择图片失败:", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    }
    function removeImage(index) {
      selectedImages.value.splice(index, 1);
    }
    function previewImage(index) {
      const urls = selectedImages.value.map((img) => img.url);
      common_vendor.index.previewImage({
        current: index,
        urls
      });
    }
    function chooseLocation() {
      common_vendor.index.showModal({
        title: "添加位置",
        editable: true,
        placeholderText: "请输入位置信息",
        success: (res) => {
          if (res.confirm && res.content) {
            momentData.location = res.content;
          }
        }
      });
    }
    function chooseVisibility() {
      common_vendor.index.showActionSheet({
        itemList: ["公开", "仅关注者可见", "仅自己可见"],
        success: (res) => {
          momentData.visibility = res.tapIndex + 1;
        }
      });
    }
    async function publishMoment() {
      if (!canPublish.value) {
        common_vendor.index.showToast({
          title: "请输入动态内容",
          icon: "none"
        });
        return;
      }
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        common_vendor.index.showModal({
          title: "提示",
          content: "请先登录",
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
      publishing.value = true;
      try {
        let imageUrls = [];
        if (selectedImages.value.length > 0) {
          common_vendor.index.showLoading({
            title: "上传图片中..."
          });
          const uploadPromises = selectedImages.value.map(async (image) => {
            const result2 = await api_moment.default.uploadMomentImage(image.path);
            if (result2.code === 200) {
              return result2.data;
            } else {
              throw new Error(result2.message || "图片上传失败");
            }
          });
          imageUrls = await Promise.all(uploadPromises);
          common_vendor.index.hideLoading();
        }
        common_vendor.index.showLoading({
          title: "发布中..."
        });
        const token2 = common_vendor.index.getStorageSync("token");
        const result = await new Promise((resolve, reject) => {
          common_vendor.index.request({
            url: "http://localhost:9002/api/social/moment/create",
            method: "POST",
            header: {
              "Content-Type": "application/json",
              "token": token2
            },
            data: {
              content: momentData.content,
              location: momentData.location,
              visibility: momentData.visibility,
              imageUrls
            },
            success: (res) => {
              resolve(res.data);
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
        common_vendor.index.hideLoading();
        if (result.code === 200) {
          common_vendor.index.showToast({
            title: "发布成功",
            icon: "success"
          });
          setTimeout(() => {
            common_vendor.index.navigateBack();
          }, 1500);
        } else {
          throw new Error(result.message || "发布失败");
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/moment/publish.vue:289", "发布失败:", error);
        common_vendor.index.showToast({
          title: error.message || "发布失败，请重试",
          icon: "none"
        });
      } finally {
        publishing.value = false;
      }
    }
    function goBack() {
      if (momentData.content.trim() || selectedImages.value.length > 0) {
        common_vendor.index.showModal({
          title: "提示",
          content: "确定要放弃编辑吗？",
          success: (res) => {
            if (res.confirm) {
              common_vendor.index.navigateBack();
            }
          }
        });
      } else {
        common_vendor.index.navigateBack();
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: !canPublish.value ? 1 : "",
        c: common_vendor.o(publishMoment),
        d: momentData.content,
        e: common_vendor.o(($event) => momentData.content = $event.detail.value),
        f: common_vendor.f(selectedImages.value, (image, index, i0) => {
          return {
            a: image.url,
            b: common_vendor.o(($event) => previewImage(index), index),
            c: common_vendor.o(($event) => removeImage(index), index),
            d: index
          };
        }),
        g: selectedImages.value.length < 9
      }, selectedImages.value.length < 9 ? {
        h: common_vendor.o(chooseImage)
      } : {}, {
        i: common_vendor.t(momentData.location || "添加位置"),
        j: common_vendor.o(chooseLocation),
        k: common_vendor.t(visibilityText.value),
        l: common_vendor.o(chooseVisibility),
        m: publishing.value
      }, publishing.value ? {} : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d6c9749c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/moment/publish.js.map
