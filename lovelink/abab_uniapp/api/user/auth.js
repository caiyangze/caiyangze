/**
 * 用户认证相关API
 */
import http from '../http';

// API基础路径
const BASE_PATH = '/user';

/**
 * 发送注册验证码
 * @param {String} phone 手机号
 */
export const sendRegisterCode = (phone) => {
  return http.post(`${BASE_PATH}/sendCode`, { phone });
};

/**
 * 验证验证码
 * @param {String} phone 手机号
 * @param {String} code 验证码
 * @param {String} password 密码（可选）
 * @returns {Promise} 返回包含用户ID的Promise
 */
export const verifyCode = (phone, code, password) => {
  const data = { phone, code };
  if (password) {
    data.password = password;
  }
  return http.post(`${BASE_PATH}/verifyCode`, data);
};

/**
 * 用户登录
 * @param {String} phone 手机号
 * @param {String} password 密码
 */
export const login = (phone, password) => {
  return http.post(`${BASE_PATH}/login`, { phone, password });
};

/**
 * 验证码登录
 * @param {String} phone 手机号
 * @param {String} code 验证码
 */
export const loginByCode = (phone, code) => {
  return http.post(`${BASE_PATH}/loginByCode`, { phone, code });
};

/**
 * 发送登录验证码
 * @param {String} phone 手机号
 */
export const sendLoginCode = (phone) => {
  return http.post(`${BASE_PATH}/sendLoginCode`, { phone });
};


// 获取用户信息
export const getByUserInfo = (token) =>{
	return http.post(`${BASE_PATH}/userInfo`,{token})
}

// 更新用户省份
export const updateUserProvince = (token, province) => {
	return http.post(`${BASE_PATH}/updateProvince`, { token, province })
}

/**
 * 身份证预验证
 * @param {String} realName 真实姓名
 * @param {String} idCardNo 身份证号
 */
export const preVerifyIdCard = (realName, idCardNo) => {
  return http.post(`${BASE_PATH}/verification/preVerify`, { realName, idCardNo });
};

/**
 * 提交实名认证
 * @param {Object} data 认证数据
 */
export const submitVerification = (data) => {
  return http.post(`${BASE_PATH}/verification/submit`, data);
};

/**
 * 获取认证状态
 */
export const getVerificationStatus = () => {
  return http.get(`${BASE_PATH}/verification/status`);
};

/**
 * 上传身份证正面照片
 * @param {String} filePath 本地文件路径
 */
export const uploadIdCardFront = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.uploadFile({
      url: http.getBaseUrl() + '/user/verification/upload/idcard-front',
      filePath: filePath,
      name: 'file',
      header: {
        'token': token
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

/**
 * 上传身份证背面照片
 * @param {String} filePath 本地文件路径
 */
export const uploadIdCardBack = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.uploadFile({
      url: http.getBaseUrl() + '/user/verification/upload/idcard-back',
      filePath: filePath,
      name: 'file',
      header: {
        'token': token
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

/**
 * 上传人脸照片
 * @param {String} filePath 本地文件路径
 */
export const uploadFacePhoto = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.uploadFile({
      url: http.getBaseUrl() + '/user/verification/upload/face-photo',
      filePath: filePath,
      name: 'file',
      header: {
        'token': token
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

/**
 * 退出登录
 */
export const logout = () => {
  return http.post(`${BASE_PATH}/logout`);
};

export default {
  sendRegisterCode,
  verifyCode,
  login,
  loginByCode,
  sendLoginCode,
  logout,
  getByUserInfo,
  preVerifyIdCard,
  submitVerification,
  getVerificationStatus,
  uploadIdCardFront,
  uploadIdCardBack,
  uploadFacePhoto,
  updateUserProvince
};