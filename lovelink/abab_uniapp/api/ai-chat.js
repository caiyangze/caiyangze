/**
 * AI红娘聊天API
 * 提供与AI红娘的智能对话功能
 */

import { getApiPath } from './config.js';
import http from './http.js';

/**
 * 与AI红娘进行流式对话
 * @param {Object} chatData 聊天数据
 * @param {Long} chatData.memoryId 对话记忆ID
 * @param {String} chatData.message 用户消息
 * @param {String} chatData.messageType 消息类型：text, voice, image
 * @param {Long} chatData.userId 用户ID
 * @param {Integer} chatData.userGender 用户性别：0-女，1-男
 * @param {Integer} chatData.userAge 用户年龄
 * @param {String} chatData.userLocation 用户所在地区
 * @returns {Promise} 流式响应Promise
 */
export function chatWithAi(chatData) {
  const url = 'http://localhost:8084/xiaozhi/chat';

  return new Promise((resolve, reject) => {
    // 创建XMLHttpRequest对象用于流式请求
    const xhr = new XMLHttpRequest();

    // 配置请求
    xhr.open('POST', url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    let responseText = '';
    let isCompleted = false;

    // 创建响应对象
    const responseHandler = {
      onData: null,
      onComplete: null
    };

    // 监听响应状态变化
    xhr.onreadystatechange = function() {
      if (xhr.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
        // 检查响应状态
        if (xhr.status !== 200) {
          console.error('HTTP错误:', xhr.status, xhr.statusText);
          reject(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
          return;
        }
      }

      if (xhr.readyState === XMLHttpRequest.LOADING || xhr.readyState === XMLHttpRequest.DONE) {
        // 获取新的响应文本
        const newText = xhr.responseText.substring(responseText.length);
        responseText = xhr.responseText;

        if (newText && responseHandler.onData) {
          responseHandler.onData(newText);
        }

        // 请求完成
        if (xhr.readyState === XMLHttpRequest.DONE && !isCompleted) {
          isCompleted = true;

          if (xhr.status === 200) {
            if (responseHandler.onComplete) {
              responseHandler.onComplete(responseText);
            }
            resolve(responseHandler);
          } else {
            reject(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
          }
        }
      }
    };

    // 监听错误
    xhr.onerror = function() {
      console.error('XHR网络错误');
      reject(new Error('网络请求失败'));
    };

    // 监听超时
    xhr.ontimeout = function() {
      console.error('XHR请求超时');
      reject(new Error('请求超时'));
    };

    // 设置超时时间（60秒）
    xhr.timeout = 60000;

    // 发送请求
    try {
      xhr.send(JSON.stringify(chatData));

      // 立即返回响应处理器
      resolve(responseHandler);

    } catch (error) {
      console.error('发送请求失败:', error);
      reject(error);
    }
  });
}

/**
 * 获取AI聊天历史记录
 * @param {Long} memoryId 对话记忆ID
 * @returns {Promise}
 */
export function getAiChatHistory(memoryId) {
  return http.get(getApiPath('/xiaozhi/history'), {
    memoryId: memoryId
  });
}

/**
 * 清除AI聊天记忆
 * @param {Long} memoryId 对话记忆ID
 * @returns {Promise}
 */
export function clearAiChatMemory(memoryId) {
  return http.delete(getApiPath('/xiaozhi/memory'), {
    memoryId: memoryId
  });
}

export default {
  chatWithAi,
  getAiChatHistory,
  clearAiChatMemory
};
