"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("../common/vendor.js");
const api_http = require("./http.js");
const SOCIAL_BASE_URL = "http://localhost:9002";
const momentApi = {
  /**
   * 发布动态
   * @param {Object} data - 动态数据
   * @param {Array} mediaFiles - 媒体文件数组
   */
  createMoment(data, mediaFiles = []) {
    const formData = new FormData();
    formData.append("content", data.content);
    if (data.location)
      formData.append("location", data.location);
    if (data.visibility)
      formData.append("visibility", data.visibility);
    mediaFiles.forEach((file, index) => {
      formData.append("mediaFiles", file);
    });
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/create`,
      method: "POST",
      data: formData,
      header: {
        "Content-Type": "multipart/form-data"
      }
    });
  },
  /**
   * 上传动态图片（使用MinIO）
   * @param {String} filePath - 本地文件路径
   */
  uploadMomentImage(filePath) {
    return new Promise((resolve, reject) => {
      const token = common_vendor.index.getStorageSync("token") || "";
      common_vendor.index.uploadFile({
        url: SOCIAL_BASE_URL + "/api/social/file/upload/moment",
        filePath,
        name: "file",
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
  },
  /**
   * 批量上传动态图片
   * @param {Array} filePaths - 本地文件路径数组
   */
  uploadMomentImages: async function(filePaths) {
    const uploadPromises = filePaths.map((filePath) => this.uploadMomentImage(filePath));
    return Promise.all(uploadPromises);
  },
  /**
   * 查询公开动态列表
   * @param {Number} pageNum - 页码
   * @param {Number} pageSize - 页大小
   */
  getPublicMoments(pageNum = 1, pageSize = 10) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/public?pageNum=${pageNum}&pageSize=${pageSize}`,
      method: "GET"
    });
  },
  /**
   * 查询我的动态列表
   * @param {Number} pageNum - 页码
   * @param {Number} pageSize - 页大小
   */
  getMyMoments(pageNum = 1, pageSize = 10) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/mine?pageNum=${pageNum}&pageSize=${pageSize}`,
      method: "GET"
    });
  },
  /**
   * 查询动态详情
   * @param {Number} momentId - 动态ID
   */
  getMomentDetail(momentId) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/detail/${momentId}`,
      method: "GET"
    });
  },
  /**
   * 更新动态
   * @param {Object} data - 更新数据
   */
  updateMoment(data) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/update`,
      method: "PUT",
      data
    });
  },
  /**
   * 删除动态
   * @param {Number} momentId - 动态ID
   */
  deleteMoment(momentId) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/delete/${momentId}`,
      method: "DELETE"
    });
  },
  /**
   * 更新动态可见性
   * @param {Number} momentId - 动态ID
   * @param {Number} visibility - 可见性
   */
  updateMomentVisibility(momentId, visibility) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/visibility/${momentId}?visibility=${visibility}`,
      method: "PUT"
    });
  },
  /**
   * 点赞/取消点赞动态
   * @param {Number} momentId - 动态ID
   */
  likeMoment(momentId) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/like/${momentId}`,
      method: "POST"
    });
  },
  /**
   * 获取动态评论列表
   * @param {Number} momentId - 动态ID
   * @param {Number} pageNum - 页码
   * @param {Number} pageSize - 页大小
   */
  getMomentComments(momentId, pageNum = 1, pageSize = 10) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/comments/${momentId}?pageNum=${pageNum}&pageSize=${pageSize}`,
      method: "GET"
    });
  },
  /**
   * 添加动态评论
   * @param {Number} momentId - 动态ID
   * @param {String} content - 评论内容
   */
  addMomentComment(momentId, content) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/comment`,
      method: "POST",
      data: {
        momentId,
        content
      }
    });
  },
  /**
   * 增加动态浏览次数
   * @param {Number} momentId - 动态ID
   */
  incrementViewCount(momentId) {
    return api_http.http.request({
      url: `${SOCIAL_BASE_URL}/api/social/moment/view/${momentId}`,
      method: "POST"
    });
  },
  /**
   * 获取评论列表（简化方法名）
   * @param {Number} momentId - 动态ID
   * @param {Number} pageNum - 页码
   * @param {Number} pageSize - 页大小
   */
  getCommentList(momentId, pageNum = 1, pageSize = 20) {
    return this.getMomentComments(momentId, pageNum, pageSize);
  },
  /**
   * 添加评论（简化方法名）
   * @param {Number} momentId - 动态ID
   * @param {String} content - 评论内容
   */
  addComment(momentId, content) {
    return this.addMomentComment(momentId, content);
  }
};
exports.default = momentApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/moment.js.map
