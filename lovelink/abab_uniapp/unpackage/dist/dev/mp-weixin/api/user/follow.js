"use strict";
const api_http = require("../http.js");
const getFansList = () => {
  return api_http.http.post("/user/fensi");
};
const getFollowingList = () => {
  return api_http.http.post("/user/guanzhu");
};
exports.getFansList = getFansList;
exports.getFollowingList = getFollowingList;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/api/user/follow.js.map
