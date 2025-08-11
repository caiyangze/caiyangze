import http from './http';

// 用户资料相关API
const profileApi = {
  /**
   * 创建用户资料
   * @param {Object} data - 用户资料数据
   * @returns {Promise} - 请求Promise
   */
  createProfile(data) {
    return http.post('/profile/create', data);
  },

  /**
   * 更新用户资料
   * @param {Object} data - 用户资料数据
   * @returns {Promise} - 请求Promise
   */
  updateProfile(data) {
    return http.put('/profile/update', data);
  },

  /**
   * 获取用户资料
   * @param {String|Number} id - 用户ID
   * @returns {Promise} - 请求Promise
   */
  getProfile(id) {
    return http.get(`/profile/detail/${id}`);
  },

  /**
   * 获取当前登录用户的资料
   * @returns {Promise} - 请求Promise
   */
  getMyProfile() {
    return http.get('/profile/my');
  },
  
  /**
   * 上传证书图片
   * @param {String} filePath - 本地文件路径
   * @returns {Promise} - 请求Promise
   */
  uploadCertificate(filePath) {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: http.baseUrl + '/upload/certificate',
        filePath: filePath,
        name: 'file',
        header: {
          'token': uni.getStorageSync('token') || ''
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

export default profileApi;