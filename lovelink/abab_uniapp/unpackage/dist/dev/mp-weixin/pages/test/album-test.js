"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user_photo = require("../../api/user/photo.js");
const api_http = require("../../api/http.js");
const _sfc_main = {
  __name: "album-test",
  setup(__props) {
    const uploadResult = common_vendor.ref("");
    const listResult = common_vendor.ref("");
    const photos = common_vendor.ref([]);
    const baseUrl = common_vendor.ref("");
    const token = common_vendor.ref("");
    common_vendor.onMounted(() => {
      baseUrl.value = api_http.http.getBaseUrl();
      token.value = common_vendor.index.getStorageSync("token");
      testGetList();
    });
    function testUpload() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: async (res) => {
          try {
            uploadResult.value = "上传中...";
            const filePath = res.tempFilePaths[0];
            const result = await api_user_photo.uploadPhoto(filePath, "这是一张测试照片，包含描述信息", 1);
            if (result.code === 200) {
              uploadResult.value = "上传成功！";
              testGetList();
            } else {
              uploadResult.value = `上传失败：${result.message}`;
            }
          } catch (error) {
            uploadResult.value = `上传异常：${error.message || error}`;
            common_vendor.index.__f__("error", "at pages/test/album-test.vue:88", "上传失败:", error);
          }
        },
        fail: (err) => {
          uploadResult.value = `选择图片失败：${err.errMsg}`;
        }
      });
    }
    async function testGetList() {
      try {
        listResult.value = "加载中...";
        const result = await api_user_photo.getPhotoList(1, 20);
        if (result.code === 200) {
          photos.value = result.data.records || [];
          listResult.value = `获取成功，共${photos.value.length}张照片`;
        } else {
          listResult.value = `获取失败：${result.message}`;
        }
      } catch (error) {
        listResult.value = `获取异常：${error.message || error}`;
        common_vendor.index.__f__("error", "at pages/test/album-test.vue:111", "获取相册列表失败:", error);
      }
    }
    async function testSetAvatar(photoId) {
      try {
        const result = await api_user_photo.setAsAvatar(photoId);
        if (result.code === 200) {
          common_vendor.index.showToast({
            title: "设置头像成功",
            icon: "success"
          });
          testGetList();
        } else {
          common_vendor.index.showToast({
            title: result.message || "设置失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.showToast({
          title: "设置异常",
          icon: "none"
        });
        common_vendor.index.__f__("error", "at pages/test/album-test.vue:138", "设置头像失败:", error);
      }
    }
    async function testEdit(photoId) {
      try {
        const result = await api_user_photo.updatePhoto(photoId, "这是修改后的照片描述", 0);
        if (result.code === 200) {
          common_vendor.index.showToast({
            title: "编辑成功",
            icon: "success"
          });
          testGetList();
        } else {
          common_vendor.index.showToast({
            title: result.message || "编辑失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/test/album-test.vue:161", "编辑照片失败:", error);
        common_vendor.index.showToast({
          title: "编辑失败，请重试",
          icon: "none"
        });
      }
    }
    async function testDelete(photoId) {
      common_vendor.index.showModal({
        title: "确认删除",
        content: "确定要删除这张照片吗？",
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await api_user_photo.deletePhoto(photoId);
              if (result.code === 200) {
                common_vendor.index.showToast({
                  title: "删除成功",
                  icon: "success"
                });
                testGetList();
              } else {
                common_vendor.index.showToast({
                  title: result.message || "删除失败",
                  icon: "none"
                });
              }
            } catch (error) {
              common_vendor.index.showToast({
                title: "删除异常",
                icon: "none"
              });
              common_vendor.index.__f__("error", "at pages/test/album-test.vue:197", "删除照片失败:", error);
            }
          }
        }
      });
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(testUpload),
        b: uploadResult.value
      }, uploadResult.value ? {
        c: common_vendor.t(uploadResult.value)
      } : {}, {
        d: common_vendor.o(testGetList),
        e: listResult.value
      }, listResult.value ? {
        f: common_vendor.t(listResult.value)
      } : {}, {
        g: photos.value.length > 0
      }, photos.value.length > 0 ? {
        h: common_vendor.f(photos.value, (photo, k0, i0) => {
          return common_vendor.e({
            a: photo.photoUrl,
            b: common_vendor.t(photo.photoDesc || "无描述"),
            c: common_vendor.t(photo.isPublic ? "公开" : "私密"),
            d: photo.isAvatar
          }, photo.isAvatar ? {} : {}, {
            e: common_vendor.o(($event) => testSetAvatar(photo.photoId), photo.photoId),
            f: common_vendor.o(($event) => testEdit(photo.photoId), photo.photoId),
            g: common_vendor.o(($event) => testDelete(photo.photoId), photo.photoId),
            h: photo.photoId
          });
        })
      } : {}, {
        i: common_vendor.t(baseUrl.value),
        j: common_vendor.t(token.value ? token.value.substring(0, 20) + "..." : "未登录")
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-ea59386f"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/album-test.js.map
