"use strict";
const common_vendor = require("../../common/vendor.js");
const BASE_URL = "http://localhost:9001";
const LONG_PRESS_DELAY = 1500;
const _sfc_main = {
  __name: "album",
  setup(__props) {
    const getPhotoList = async (pageNum = 1, pageSize2 = 20) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/list`,
          method: "GET",
          data: { pageNum, pageSize: pageSize2 },
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const updatePhotoSort = async (photoId, sortOrder) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/updateSort/${photoId}`,
          method: "PUT",
          data: { sortOrder },
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const updatePhoto = async (photoId, photoDesc, isPublic) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      const params = {};
      if (photoDesc !== void 0)
        params.photoDesc = photoDesc;
      if (isPublic !== void 0)
        params.isPublic = isPublic;
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/update/${photoId}`,
          method: "PUT",
          data: params,
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const uploadPhotos = async (filePaths, photoDesc = "", isPublic = 1) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      const uploadPromises = filePaths.map((filePath) => {
        return new Promise((resolve, reject) => {
          common_vendor.index.uploadFile({
            url: `${BASE_URL}/user/photo/upload`,
            filePath,
            name: "file",
            formData: {
              photoDesc,
              isPublic
            },
            header: {
              "token": token
            },
            success: (res) => {
              try {
                const data = JSON.parse(res.data);
                resolve(data);
              } catch (e) {
                reject(e);
              }
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
      });
      return Promise.all(uploadPromises);
    };
    const deletePhoto = async (photoId) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/delete/${photoId}`,
          method: "DELETE",
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const setAsAvatar = async (photoId) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/setAvatar/${photoId}`,
          method: "POST",
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const cancelAvatar = async () => {
      const token = common_vendor.index.getStorageSync("token") || "";
      return new Promise((resolve, reject) => {
        common_vendor.index.request({
          url: `${BASE_URL}/user/photo/cancelAvatar`,
          method: "POST",
          header: {
            "token": token,
            "Content-Type": "application/json"
          },
          success: (res) => {
            resolve(res.data);
          },
          fail: (err) => {
            reject(err);
          }
        });
      });
    };
    const statusBarHeight = common_vendor.ref(0);
    const photos = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    const loadingMore = common_vendor.ref(false);
    const hasMore = common_vendor.ref(true);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(20);
    const uploading = common_vendor.ref(false);
    const uploadProgress = common_vendor.ref(0);
    const showUploadModal = common_vendor.ref(false);
    const showEditModal = common_vendor.ref(false);
    const isDragMode = common_vendor.ref(false);
    const draggingIndex = common_vendor.ref(-1);
    common_vendor.ref({ x: 0, y: 0 });
    const originalPhotos = common_vendor.ref([]);
    const longPressTimer = common_vendor.ref(null);
    const touchStartTime = common_vendor.ref(0);
    const touchStartPosition = common_vendor.ref({ x: 0, y: 0 });
    const isLongPressing = common_vendor.ref(false);
    const isDragging = common_vendor.ref(false);
    common_vendor.ref(-1);
    common_vendor.ref([]);
    const uploadForm = common_vendor.reactive({
      photoDesc: "",
      isPublic: 1,
      tempFilePaths: []
    });
    const editForm = common_vendor.reactive({
      photoId: null,
      photoDesc: "",
      isPublic: 1,
      photoIndex: -1
    });
    common_vendor.onMounted(() => {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      statusBarHeight.value = sysInfo.statusBarHeight;
      loadPhotos();
    });
    common_vendor.onUnmounted(() => {
      if (longPressTimer.value) {
        clearTimeout(longPressTimer.value);
        longPressTimer.value = null;
      }
    });
    async function loadPhotos(refresh = false) {
      try {
        if (refresh) {
          currentPage.value = 1;
          hasMore.value = true;
        }
        loading.value = refresh || photos.value.length === 0;
        const result = await getPhotoList(currentPage.value, pageSize.value);
        if (result.code === 200) {
          const newPhotos = result.data.records || [];
          if (refresh) {
            photos.value = newPhotos;
          } else {
            photos.value = [...photos.value, ...newPhotos];
          }
          hasMore.value = newPhotos.length === pageSize.value;
          if (hasMore.value) {
            currentPage.value++;
          }
          common_vendor.index.__f__("log", "at pages/mine/album.vue:550", "相册数据加载成功:", photos.value);
        } else {
          common_vendor.index.showToast({
            title: result.message || "加载失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:558", "加载相册数据失败:", error);
        common_vendor.index.showToast({
          title: "加载失败，请重试",
          icon: "none"
        });
      } finally {
        loading.value = false;
        loadingMore.value = false;
      }
    }
    function loadMore() {
      if (hasMore.value && !loadingMore.value) {
        loadingMore.value = true;
        loadPhotos();
      }
    }
    function goBack() {
      common_vendor.index.navigateBack();
    }
    function toggleDragMode() {
      isDragMode.value = true;
      originalPhotos.value = [...photos.value];
      common_vendor.index.showToast({
        title: "长按1.5秒后拖动排序",
        icon: "none",
        duration: 3e3
      });
    }
    async function finishDragMode() {
      try {
        common_vendor.index.showLoading({ title: "保存排序..." });
        const updatePromises = photos.value.map((photo, index) => {
          return updatePhotoSort(photo.photoId, index + 1);
        });
        await Promise.all(updatePromises);
        isDragMode.value = false;
        draggingIndex.value = -1;
        isDragging.value = false;
        isLongPressing.value = false;
        if (longPressTimer.value) {
          clearTimeout(longPressTimer.value);
          longPressTimer.value = null;
        }
        common_vendor.index.showToast({
          title: "排序已保存",
          icon: "success"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:621", "保存排序失败:", error);
        photos.value = [...originalPhotos.value];
        common_vendor.index.showToast({
          title: "保存失败，已恢复",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    function onTouchStart(event, index) {
      if (!isDragMode.value)
        return;
      touchStartTime.value = Date.now();
      touchStartPosition.value = {
        x: event.touches[0].clientX,
        y: event.touches[0].clientY
      };
      longPressTimer.value = setTimeout(() => {
        isLongPressing.value = true;
        isDragging.value = true;
        draggingIndex.value = index;
        common_vendor.index.vibrateShort({
          type: "heavy"
        });
        common_vendor.index.showToast({
          title: "开始拖拽",
          icon: "none",
          duration: 1e3
        });
      }, LONG_PRESS_DELAY);
    }
    function onTouchMove(event, index) {
      if (!isDragMode.value)
        return;
      const currentPosition = {
        x: event.touches[0].clientX,
        y: event.touches[0].clientY
      };
      const deltaX = currentPosition.x - touchStartPosition.value.x;
      const deltaY = currentPosition.y - touchStartPosition.value.y;
      const distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      if (distance > 20 && !isLongPressing.value) {
        clearTimeout(longPressTimer.value);
      }
      if (isDragging.value && draggingIndex.value === index) {
        event.preventDefault();
        const moveThreshold = 80;
        if (Math.abs(deltaY) > moveThreshold) {
          const direction = deltaY > 0 ? 1 : -1;
          const targetIndex = draggingIndex.value + direction;
          if (targetIndex >= 0 && targetIndex < photos.value.length) {
            reorderPhotos(draggingIndex.value, targetIndex);
            draggingIndex.value = targetIndex;
            touchStartPosition.value = currentPosition;
            common_vendor.index.vibrateShort({
              type: "light"
            });
          }
        }
      }
    }
    function onTouchEnd(event, index) {
      if (!isDragMode.value)
        return;
      clearTimeout(longPressTimer.value);
      const touchDuration = Date.now() - touchStartTime.value;
      if (touchDuration < LONG_PRESS_DELAY && !isDragging.value) {
        return;
      }
      if (isDragging.value) {
        onDragEnd();
      }
      isLongPressing.value = false;
      isDragging.value = false;
    }
    function onLongPress(event, index) {
      if (!isDragMode.value)
        return;
      common_vendor.index.__f__("log", "at pages/mine/album.vue:739", "长按触发:", index);
    }
    function reorderPhotos(fromIndex, toIndex) {
      if (fromIndex === toIndex)
        return;
      const newPhotos = [...photos.value];
      const draggedItem = newPhotos.splice(fromIndex, 1)[0];
      newPhotos.splice(toIndex, 0, draggedItem);
      photos.value = newPhotos;
    }
    function onDragEnd() {
      if (draggingIndex.value !== -1) {
        common_vendor.index.showToast({
          title: "拖拽结束",
          icon: "none",
          duration: 1e3
        });
      }
      draggingIndex.value = -1;
      isDragging.value = false;
      isLongPressing.value = false;
    }
    function handlePhotoTap(photo, index) {
      if (isDragMode.value) {
        return;
      }
      previewPhoto(index);
    }
    async function togglePrivacy(photo, index) {
      try {
        const newPrivacy = photo.isPublic === 1 ? 0 : 1;
        const result = await updatePhoto(photo.photoId, photo.photoDesc, newPrivacy);
        if (result.code === 200) {
          photos.value[index].isPublic = newPrivacy;
          common_vendor.index.showToast({
            title: newPrivacy === 1 ? "已设为公开" : "已设为私密",
            icon: "success",
            duration: 1500
          });
        } else {
          common_vendor.index.showToast({
            title: "设置失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:815", "切换隐私失败:", error);
        common_vendor.index.showToast({
          title: "操作失败",
          icon: "none"
        });
      }
    }
    function addPhotos() {
      common_vendor.index.showActionSheet({
        itemList: ["拍照", "从相册选择"],
        success: (res) => {
          if (res.tapIndex === 0) {
            chooseImage("camera");
          } else if (res.tapIndex === 1) {
            chooseImage("album");
          }
        }
      });
    }
    function chooseImage(sourceType) {
      common_vendor.index.chooseImage({
        count: 9,
        // 最多选择9张
        sizeType: ["compressed"],
        // 压缩图
        sourceType: [sourceType],
        success: (res) => {
          const tempFilePaths = res.tempFilePaths;
          if (tempFilePaths.length > 0) {
            uploadForm.tempFilePaths = tempFilePaths;
            uploadForm.photoDesc = "";
            uploadForm.isPublic = 1;
            showUploadModal.value = true;
          }
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/mine/album.vue:856", "选择图片失败:", err);
          common_vendor.index.showToast({
            title: "选择图片失败",
            icon: "none"
          });
        }
      });
    }
    function closeUploadModal() {
      showUploadModal.value = false;
      uploadForm.tempFilePaths = [];
      uploadForm.photoDesc = "";
      uploadForm.isPublic = 1;
    }
    function confirmUpload() {
      if (uploadForm.tempFilePaths.length > 0) {
        showUploadModal.value = false;
        uploadImages(uploadForm.tempFilePaths, uploadForm.photoDesc, uploadForm.isPublic);
      }
    }
    function closeEditModal() {
      showEditModal.value = false;
      editForm.photoId = null;
      editForm.photoDesc = "";
      editForm.isPublic = 1;
      editForm.photoIndex = -1;
    }
    async function confirmEdit() {
      try {
        common_vendor.index.showLoading({ title: "保存中..." });
        const result = await updatePhoto(editForm.photoId, editForm.photoDesc, editForm.isPublic);
        if (result.code === 200) {
          if (editForm.photoIndex >= 0) {
            photos.value[editForm.photoIndex].photoDesc = editForm.photoDesc;
            photos.value[editForm.photoIndex].isPublic = editForm.isPublic;
          }
          common_vendor.index.showToast({
            title: "保存成功",
            icon: "success"
          });
          closeEditModal();
        } else {
          common_vendor.index.showToast({
            title: result.message || "保存失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:917", "保存照片信息失败:", error);
        common_vendor.index.showToast({
          title: "保存失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    async function uploadImages(filePaths, photoDesc = "", isPublic = 1) {
      try {
        uploading.value = true;
        uploadProgress.value = 0;
        const totalFiles = filePaths.length;
        let completedFiles = 0;
        for (let i = 0; i < filePaths.length; i++) {
          const filePath = filePaths[i];
          try {
            const result = await uploadPhotos([filePath], photoDesc, isPublic);
            if (result[0].code === 200) {
              photos.value.unshift(result[0].data);
              completedFiles++;
            } else {
              common_vendor.index.__f__("error", "at pages/mine/album.vue:948", "上传失败:", result[0].message);
            }
          } catch (error) {
            common_vendor.index.__f__("error", "at pages/mine/album.vue:951", "上传图片失败:", error);
          }
          uploadProgress.value = Math.round(completedFiles / totalFiles * 100);
        }
        if (completedFiles > 0) {
          common_vendor.index.showToast({
            title: `成功上传${completedFiles}张照片`,
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: "上传失败，请重试",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:971", "批量上传失败:", error);
        common_vendor.index.showToast({
          title: "上传失败，请重试",
          icon: "none"
        });
      } finally {
        uploading.value = false;
        uploadProgress.value = 0;
      }
    }
    function previewPhoto(index) {
      const urls = photos.value.map((photo) => photo.photoUrl);
      common_vendor.index.previewImage({
        current: index,
        urls
      });
    }
    function showPhotoActions(photo, index) {
      const actions = [];
      if (photo.isAvatar === 1) {
        actions.push("取消头像");
      } else {
        actions.push("设为头像");
      }
      actions.push("编辑信息", "删除照片");
      common_vendor.index.showActionSheet({
        itemList: actions,
        success: (res) => {
          if (res.tapIndex === 0) {
            if (photo.isAvatar === 1) {
              cancelPhotoAvatar();
            } else {
              setPhotoAsAvatar(photo);
            }
          } else if (res.tapIndex === 1) {
            editPhotoInfo(photo, index);
          } else if (res.tapIndex === 2) {
            confirmDeletePhoto(photo, index);
          }
        }
      });
    }
    async function setPhotoAsAvatar(photo) {
      try {
        common_vendor.index.showLoading({ title: "设置中..." });
        const result = await setAsAvatar(photo.photoId);
        if (result.code === 200) {
          photos.value.forEach((p) => {
            p.isAvatar = p.photoId === photo.photoId ? 1 : 0;
          });
          common_vendor.index.showToast({
            title: "设置头像成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "设置失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:1050", "设置头像失败:", error);
        common_vendor.index.showToast({
          title: "设置失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    async function cancelPhotoAvatar(photo) {
      try {
        common_vendor.index.showLoading({ title: "取消中..." });
        const result = await cancelAvatar();
        if (result.code === 200) {
          photos.value.forEach((p) => {
            p.isAvatar = 0;
          });
          common_vendor.index.showToast({
            title: "取消头像成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "取消失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:1084", "取消头像失败:", error);
        common_vendor.index.showToast({
          title: "取消失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    function editPhotoInfo(photo, index) {
      editForm.photoId = photo.photoId;
      editForm.photoDesc = photo.photoDesc || "";
      editForm.isPublic = photo.isPublic;
      editForm.photoIndex = index;
      showEditModal.value = true;
    }
    function confirmDeletePhoto(photo, index) {
      common_vendor.index.showModal({
        title: "删除照片",
        content: "确定要删除这张照片吗？删除后无法恢复。",
        confirmText: "删除",
        cancelText: "取消",
        confirmColor: "#ff4757",
        success: (res) => {
          if (res.confirm) {
            deletePhotoById(photo, index);
          }
        }
      });
    }
    async function deletePhotoById(photo, index) {
      try {
        common_vendor.index.showLoading({ title: "删除中..." });
        const result = await deletePhoto(photo.photoId);
        if (result.code === 200) {
          photos.value.splice(index, 1);
          common_vendor.index.showToast({
            title: "删除成功",
            icon: "success"
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "删除失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/mine/album.vue:1141", "删除照片失败:", error);
        common_vendor.index.showToast({
          title: "删除失败，请重试",
          icon: "none"
        });
      } finally {
        common_vendor.index.hideLoading();
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: !isDragMode.value
      }, !isDragMode.value ? {
        c: common_vendor.o(toggleDragMode)
      } : {}, {
        d: !isDragMode.value
      }, !isDragMode.value ? {
        e: common_vendor.o(addPhotos)
      } : {}, {
        f: isDragMode.value
      }, isDragMode.value ? {
        g: common_vendor.o(finishDragMode)
      } : {}, {
        h: statusBarHeight.value + "px",
        i: loading.value && photos.value.length === 0
      }, loading.value && photos.value.length === 0 ? {} : photos.value.length > 0 ? {
        k: common_vendor.f(photos.value, (photo, index, i0) => {
          return common_vendor.e({
            a: photo.photoUrl
          }, isDragMode.value ? {
            b: common_vendor.t(index + 1)
          } : {}, {
            c: photo.isAvatar === 1
          }, photo.isAvatar === 1 ? {} : {}, {
            d: common_vendor.t(photo.isPublic === 1 ? "🌍" : "🔒"),
            e: common_vendor.o(($event) => togglePrivacy(photo, index), photo.photoId),
            f: common_vendor.o(($event) => showPhotoActions(photo, index), photo.photoId),
            g: photo.photoDesc
          }, photo.photoDesc ? {
            h: common_vendor.t(photo.photoDesc)
          } : {}, {
            i: common_vendor.o(($event) => handlePhotoTap(photo, index), photo.photoId),
            j: common_vendor.o(($event) => onTouchStart($event, index), photo.photoId),
            k: common_vendor.o(($event) => onTouchMove($event, index), photo.photoId),
            l: common_vendor.o(($event) => onTouchEnd(), photo.photoId),
            m: common_vendor.o(($event) => onLongPress($event, index), photo.photoId),
            n: photo.photoId,
            o: draggingIndex.value === index ? 1 : ""
          });
        }),
        l: isDragMode.value
      } : {
        m: common_vendor.o(addPhotos)
      }, {
        j: photos.value.length > 0,
        n: hasMore.value && photos.value.length > 0
      }, hasMore.value && photos.value.length > 0 ? common_vendor.e({
        o: loadingMore.value
      }, loadingMore.value ? {} : {}) : {}, {
        p: !hasMore.value && photos.value.length > 0
      }, !hasMore.value && photos.value.length > 0 ? {} : {}, {
        q: statusBarHeight.value + 88 + "px",
        r: common_vendor.o(loadMore),
        s: uploading.value
      }, uploading.value ? {
        t: uploadProgress.value + "%",
        v: common_vendor.t(uploadProgress.value)
      } : {}, {
        w: showUploadModal.value
      }, showUploadModal.value ? common_vendor.e({
        x: common_vendor.t(uploadForm.tempFilePaths.length),
        y: uploadForm.photoDesc,
        z: common_vendor.o(($event) => uploadForm.photoDesc = $event.detail.value),
        A: common_vendor.t(uploadForm.photoDesc.length),
        B: uploadForm.isPublic === 1
      }, uploadForm.isPublic === 1 ? {} : {}, {
        C: uploadForm.isPublic === 1 ? 1 : "",
        D: common_vendor.o(($event) => uploadForm.isPublic = 1),
        E: uploadForm.isPublic === 0
      }, uploadForm.isPublic === 0 ? {} : {}, {
        F: uploadForm.isPublic === 0 ? 1 : "",
        G: common_vendor.o(($event) => uploadForm.isPublic = 0),
        H: common_vendor.o(closeUploadModal),
        I: common_vendor.o(confirmUpload),
        J: common_vendor.o(() => {
        }),
        K: common_vendor.o(closeUploadModal)
      }) : {}, {
        L: showEditModal.value
      }, showEditModal.value ? common_vendor.e({
        M: editForm.photoDesc,
        N: common_vendor.o(($event) => editForm.photoDesc = $event.detail.value),
        O: common_vendor.t(editForm.photoDesc.length),
        P: editForm.isPublic === 1
      }, editForm.isPublic === 1 ? {} : {}, {
        Q: editForm.isPublic === 1 ? 1 : "",
        R: common_vendor.o(($event) => editForm.isPublic = 1),
        S: editForm.isPublic === 0
      }, editForm.isPublic === 0 ? {} : {}, {
        T: editForm.isPublic === 0 ? 1 : "",
        U: common_vendor.o(($event) => editForm.isPublic = 0),
        V: common_vendor.o(closeEditModal),
        W: common_vendor.o(confirmEdit),
        X: common_vendor.o(() => {
        }),
        Y: common_vendor.o(closeEditModal)
      }) : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e7901737"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/mine/album.js.map
