"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("../../common/vendor.js");
const api_http = require("../http.js");
const BASE_PATH = "/user";
const sendRegisterCode = (phone) => {
  return api_http.http.post(`${BASE_PATH}/sendCode`, { phone });
};
const verifyCode = (phone, code, password) => {
  const data = { phone, code };
  if (password) {
    data.password = password;
  }
  return api_http.http.post(`${BASE_PATH}/verifyCode`, data);
};
const login = (phone, password) => {
  return api_http.http.post(`${BASE_PATH}/login`, { phone, password });
};
const loginByCode = (phone, code) => {
  return api_http.http.post(`${BASE_PATH}/loginByCode`, { phone, code });
};
const sendLoginCode = (phone) => {
  return api_http.http.post(`${BASE_PATH}/sendLoginCode`, { phone });
};
const getByUserInfo = (token) => {
  return api_http.http.post(`${BASE_PATH}/userInfo`, { token });
};
const updateUserProvince = (token, province) => {
  return api_http.http.post(`${BASE_PATH}/updateProvince`, { token, province });
};
const uploadIdCardFront = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.uploadFile({
      url: api_http.http.getBaseUrl() + "/user/verification/upload/idcard-front",
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
};
const uploadIdCardBack = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.uploadFile({
      url: api_http.http.getBaseUrl() + "/user/verification/upload/idcard-back",
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
};
const uploadFacePhoto = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.uploadFile({
      url: api_http.http.getBaseUrl() + "/user/verification/upload/face-photo",
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
};
exports.getByUserInfo = getByUserInfo;
exports.login = login;
exports.loginByCode = loginByCode;
exports.sendLoginCode = sendLoginCode;
exports.sendRegisterCode = sendRegisterCode;
exports.updateUserProvince = updateUserProvince;
exports.uploadFacePhoto = uploadFacePhoto;
exports.uploadIdCardBack = uploadIdCardBack;
exports.uploadIdCardFront = uploadIdCardFront;
exports.verifyCode = verifyCode;
//# sourceMappingURL=../../../.sourcemap/mp-weixin/api/user/auth.js.map
