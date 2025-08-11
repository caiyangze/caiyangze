"use strict";
const common_vendor = require("../../common/vendor.js");
const api_http = require("../http.js");
const BASE_PATH = "/user/photo";
const uploadPhoto = (filePath, photoDesc = "", isPublic = 1) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.uploadFile({
      url: api_http.http.getBaseUrl() + BASE_PATH + "/upload",
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
};
const getPhotoList = (pageNum = 1, pageSize = 20) => {
  return api_http.http.get(`${BASE_PATH}/list`, {
    pageNum,
    pageSize
  });
};
const deletePhoto = (photoId) => {
  return api_http.http.delete(`${BASE_PATH}/delete/${photoId}`);
};
const setAsAvatar = (photoId) => {
  return api_http.http.post(`${BASE_PATH}/setAvatar/${photoId}`);
};
const updatePhoto = (photoId, photoDesc, isPublic) => {
  const params = {};
  if (photoDesc !== void 0)
    params.photoDesc = photoDesc;
  if (isPublic !== void 0)
    params.isPublic = isPublic;
  return api_http.http.put(`${BASE_PATH}/update/${photoId}`, params);
};
exports.deletePhoto = deletePhoto;
exports.getPhotoList = getPhotoList;
exports.setAsAvatar = setAsAvatar;
exports.updatePhoto = updatePhoto;
exports.uploadPhoto = uploadPhoto;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/api/user/photo.js.map
