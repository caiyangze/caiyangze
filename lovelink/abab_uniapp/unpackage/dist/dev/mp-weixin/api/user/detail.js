"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const api_http = require("../http.js");
const BASE_PATH = "/loveSquare";
const SOCIAL_BASE_URL = "http://localhost:9002";
const getUserDetail = (userId) => {
  return api_http.http.post(`${BASE_PATH}/list`, {
    pageNum: 1,
    pageSize: 100,
    userId
    // 如果后端支持按用户ID筛选
  });
};
const getUserDetailById = (userId) => {
  return api_http.http.get(`/user/${userId}/detail`);
};
const getUserPhotos = (userId, pageNum = 1, pageSize = 10) => {
  return api_http.http.post(`${BASE_PATH}/findUserPhoto`, {
    userId,
    pageNum,
    pageSize
  });
};
const getUserMoments = (userId, pageNum = 1, pageSize = 10) => {
  return api_http.http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`,
    method: "GET"
  });
};
const toggleUserLike = (userId) => {
  return api_http.http.post(`/user/${userId}/like`);
};
const toggleUserFollow = (targetUserId) => {
  return api_http.http.post(`/follow/addAndCancel?followedUserId=${targetUserId}`);
};
const checkUserFollow = (targetUserId) => {
  return api_http.http.post(`/follow/isFollow?followedUserId=${targetUserId}`);
};
const detail = {
  getUserDetail,
  getUserDetailById,
  getUserPhotos,
  getUserMoments,
  toggleUserLike,
  toggleUserFollow,
  checkUserFollow
};
exports.checkUserFollow = checkUserFollow;
exports.default = detail;
exports.getUserDetail = getUserDetail;
exports.getUserDetailById = getUserDetailById;
exports.getUserMoments = getUserMoments;
exports.getUserPhotos = getUserPhotos;
exports.toggleUserFollow = toggleUserFollow;
exports.toggleUserLike = toggleUserLike;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/api/user/detail.js.map
