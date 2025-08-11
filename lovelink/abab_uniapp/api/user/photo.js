import http from '../http'

const BASE_PATH = '/user/photo'

/**
 * 上传相册照片
 * @param {String} filePath 本地文件路径
 * @param {String} photoDesc 照片描述（可选）
 * @param {Number} isPublic 是否公开：0-私密，1-公开（默认1）
 * @returns {Promise} 返回上传结果
 */
export const uploadPhoto = (filePath, photoDesc = '', isPublic = 1) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.uploadFile({
      url: http.getBaseUrl() + BASE_PATH + '/upload',
      filePath: filePath,
      name: 'file',
      formData: {
        photoDesc: photoDesc,
        isPublic: isPublic
      },
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
 * 批量上传相册照片
 * @param {Array} filePaths 本地文件路径数组
 * @param {String} photoDesc 照片描述（可选）
 * @param {Number} isPublic 是否公开：0-私密，1-公开（默认1）
 * @returns {Promise} 返回批量上传结果
 */
export const uploadPhotos = async (filePaths, photoDesc = '', isPublic = 1) => {
  const uploadPromises = filePaths.map(filePath => uploadPhoto(filePath, photoDesc, isPublic));
  return Promise.all(uploadPromises);
};

/**
 * 获取用户相册列表
 * @param {Number} pageNum 页码（默认1）
 * @param {Number} pageSize 每页数量（默认20）
 * @returns {Promise} 返回相册列表
 */
export const getPhotoList = (pageNum = 1, pageSize = 20) => {
  return http.get(`${BASE_PATH}/list`, {
    pageNum: pageNum,
    pageSize: pageSize
  });
};

/**
 * 删除相册照片
 * @param {Number} photoId 照片ID
 * @returns {Promise} 返回删除结果
 */
export const deletePhoto = (photoId) => {
  return http.delete(`${BASE_PATH}/delete/${photoId}`);
};

/**
 * 设置照片为头像
 * @param {Number} photoId 照片ID
 * @returns {Promise} 返回设置结果
 */
export const setAsAvatar = (photoId) => {
  return http.post(`${BASE_PATH}/setAvatar/${photoId}`);
};

/**
 * 取消头像
 * @returns {Promise} 返回取消结果
 */
export const cancelAvatar = () => {
  return http.post(`${BASE_PATH}/cancelAvatar`);
};

/**
 * 更新照片信息
 * @param {Number} photoId 照片ID
 * @param {String} photoDesc 照片描述（可选）
 * @param {Number} isPublic 是否公开（可选）
 * @returns {Promise} 返回更新结果
 */
export const updatePhoto = (photoId, photoDesc, isPublic) => {
  const params = {};
  if (photoDesc !== undefined) params.photoDesc = photoDesc;
  if (isPublic !== undefined) params.isPublic = isPublic;
  
  return http.put(`${BASE_PATH}/update/${photoId}`, params);
};
