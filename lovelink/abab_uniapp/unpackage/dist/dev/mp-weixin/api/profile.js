"use strict";
const common_vendor = require("../common/vendor.js");
const api_http = require("./http.js");
const profileApi = {
  /**
   * 创建用户资料
   * @param {Object} data - 用户资料数据
   * @returns {Promise} - 请求Promise
   */
  createProfile(data) {
    return api_http.http.post("/profile/create", data);
  },
  /**
   * 更新用户资料
   * @param {Object} data - 用户资料数据
   * @returns {Promise} - 请求Promise
   */
  updateProfile(data) {
    return api_http.http.put("/profile/update", data);
  },
  /**
   * 获取用户资料
   * @param {String|Number} id - 用户ID
   * @returns {Promise} - 请求Promise
   */
  getProfile(id) {
    return api_http.http.get(`/profile/detail/${id}`);
  },
  /**
   * 获取当前登录用户的资料
   * @returns {Promise} - 请求Promise
   */
  getMyProfile() {
    return api_http.http.get("/profile/my");
  },
  /**
   * 上传证书图片
   * @param {String} filePath - 本地文件路径
   * @returns {Promise} - 请求Promise
   */
  uploadCertificate(filePath) {
    return new Promise((resolve, reject) => {
      common_vendor.index.uploadFile({
        url: api_http.http.baseUrl + "/upload/certificate",
        filePath,
        name: "file",
        header: {
          "token": common_vendor.index.getStorageSync("token") || ""
        },
        success: (res) => {
          try {
            const data = res.data;
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
  }
};
exports.profileApi = profileApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/profile.js.map
