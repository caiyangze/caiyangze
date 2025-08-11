"use strict";
const common_vendor = require("../common/vendor.js");
function checkTokenStatus() {
  const token = common_vendor.index.getStorageSync("token");
  const refreshToken = common_vendor.index.getStorageSync("refreshToken");
  const userInfo = common_vendor.index.getStorageSync("userInfo");
  common_vendor.index.__f__("log", "at utils/token-debug.js:13", "=== Token状态检查 ===");
  common_vendor.index.__f__("log", "at utils/token-debug.js:14", "Token:", token ? token.substring(0, 30) + "..." : "null");
  common_vendor.index.__f__("log", "at utils/token-debug.js:15", "RefreshToken:", refreshToken ? refreshToken.substring(0, 30) + "..." : "null");
  common_vendor.index.__f__("log", "at utils/token-debug.js:16", "UserInfo:", userInfo);
  common_vendor.index.__f__("log", "at utils/token-debug.js:17", "Token长度:", token ? token.length : 0);
  if (token) {
    try {
      const parts = token.split(".");
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        common_vendor.index.__f__("log", "at utils/token-debug.js:26", "JWT Payload:", payload);
        common_vendor.index.__f__("log", "at utils/token-debug.js:27", "Token过期时间:", new Date(payload.exp * 1e3));
        common_vendor.index.__f__("log", "at utils/token-debug.js:28", "当前时间:", /* @__PURE__ */ new Date());
        common_vendor.index.__f__("log", "at utils/token-debug.js:29", "Token是否过期:", payload.exp * 1e3 < Date.now());
      }
    } catch (error) {
      common_vendor.index.__f__("log", "at utils/token-debug.js:32", "Token不是JWT格式或解析失败:", error);
    }
  }
  return {
    hasToken: !!token,
    hasRefreshToken: !!refreshToken,
    hasUserInfo: !!userInfo,
    token,
    tokenLength: token ? token.length : 0
  };
}
const testApiRequest = async function() {
  common_vendor.index.__f__("log", "at utils/token-debug.js:69", "=== 测试API请求 ===");
  const status = checkTokenStatus();
  if (!status.hasToken) {
    common_vendor.index.__f__("error", "at utils/token-debug.js:73", "没有token，无法测试");
    return;
  }
  try {
    const response = await common_vendor.index.request({
      url: "http://localhost:9001/follow/mutual",
      method: "GET",
      header: {
        "Content-Type": "application/json",
        "token": status.token,
        "Authorization": `Bearer ${status.token}`
      }
    });
    common_vendor.index.__f__("log", "at utils/token-debug.js:89", "API测试响应:", response);
    if (response.statusCode === 200) {
      common_vendor.index.__f__("log", "at utils/token-debug.js:92", "✅ API请求成功");
      return response.data;
    } else {
      common_vendor.index.__f__("error", "at utils/token-debug.js:95", "❌ API请求失败:", response);
      return null;
    }
  } catch (error) {
    common_vendor.index.__f__("error", "at utils/token-debug.js:100", "❌ API请求异常:", error);
    return null;
  }
};
function checkNetworkStatus() {
  common_vendor.index.getNetworkType({
    success: (res) => {
      common_vendor.index.__f__("log", "at utils/token-debug.js:111", "=== 网络状态 ===");
      common_vendor.index.__f__("log", "at utils/token-debug.js:112", "网络类型:", res.networkType);
      common_vendor.index.__f__("log", "at utils/token-debug.js:113", "是否连接:", res.networkType !== "none");
    },
    fail: (error) => {
      common_vendor.index.__f__("error", "at utils/token-debug.js:116", "获取网络状态失败:", error);
    }
  });
}
const diagnoseLoginStatus = async function() {
  common_vendor.index.__f__("log", "at utils/token-debug.js:125", "🔍 开始登录状态诊断...");
  checkNetworkStatus();
  const tokenStatus = checkTokenStatus();
  if (tokenStatus.hasToken) {
    await testApiRequest();
  }
  common_vendor.index.__f__("log", "at utils/token-debug.js:139", "=== 诊断建议 ===");
  if (!tokenStatus.hasToken) {
    common_vendor.index.__f__("log", "at utils/token-debug.js:141", "❌ 没有token，请重新登录");
  } else if (!tokenStatus.hasUserInfo) {
    common_vendor.index.__f__("log", "at utils/token-debug.js:143", "⚠️ 有token但没有用户信息，可能需要重新获取用户信息");
  } else {
    common_vendor.index.__f__("log", "at utils/token-debug.js:145", "✅ 登录状态看起来正常，可能是服务器端问题");
  }
  return tokenStatus;
};
exports.checkTokenStatus = checkTokenStatus;
exports.diagnoseLoginStatus = diagnoseLoginStatus;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/token-debug.js.map
