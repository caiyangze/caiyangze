"use strict";
const common_vendor = require("../common/vendor.js");
const utils_request = require("../utils/request.js");
const AI_MATCHMAKER_BASE_URL = "/api/ai-matchmaker";
function getAiMatchmakerInfo() {
  return utils_request.request({
    url: `${AI_MATCHMAKER_BASE_URL}/info`,
    method: "GET"
  });
}
function chatWithAi(userId, message) {
  return utils_request.request({
    url: `${AI_MATCHMAKER_BASE_URL}/chat`,
    method: "POST",
    header: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    data: {
      userId,
      message
    }
  });
}
function speechToText(userId, audioFile) {
  return utils_request.request({
    url: `${AI_MATCHMAKER_BASE_URL}/speech-to-text`,
    method: "POST",
    header: {
      "Content-Type": "multipart/form-data"
    },
    data: {
      userId,
      audio: audioFile
    }
  });
}
function textToSpeech(userId, text, voiceType = "LONGXIAOCHUN") {
  return utils_request.request({
    url: `${AI_MATCHMAKER_BASE_URL}/text-to-speech`,
    method: "POST",
    header: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    data: {
      userId,
      text,
      voiceType
    }
  });
}
function analyzeImage(userId, imageFile, question = "请帮我分析这张图片") {
  return utils_request.request({
    url: `${AI_MATCHMAKER_BASE_URL}/analyze-image`,
    method: "POST",
    header: {
      "Content-Type": "multipart/form-data"
    },
    data: {
      userId,
      image: imageFile,
      question
    }
  });
}
function playAudioFromBase64(audioBase64) {
  return new Promise((resolve, reject) => {
    try {
      const fs = common_vendor.index.getFileSystemManager();
      const tempFilePath = `${common_vendor.index.env.USER_DATA_PATH}/temp_audio_${Date.now()}.wav`;
      const buffer = common_vendor.index.base64ToArrayBuffer(audioBase64);
      fs.writeFile({
        filePath: tempFilePath,
        data: buffer,
        success: () => {
          const audioContext = common_vendor.index.createInnerAudioContext();
          audioContext.src = tempFilePath;
          audioContext.onPlay(() => {
            common_vendor.index.__f__("log", "at api/ai-matchmaker.js:194", "音频开始播放");
          });
          audioContext.onEnded(() => {
            common_vendor.index.__f__("log", "at api/ai-matchmaker.js:198", "音频播放结束");
            audioContext.destroy();
            fs.unlink({
              filePath: tempFilePath,
              success: () => common_vendor.index.__f__("log", "at api/ai-matchmaker.js:203", "临时音频文件已清理"),
              fail: (error) => common_vendor.index.__f__("error", "at api/ai-matchmaker.js:204", "清理临时音频文件失败:", error)
            });
            resolve();
          });
          audioContext.onError((error) => {
            common_vendor.index.__f__("error", "at api/ai-matchmaker.js:210", "音频播放失败:", error);
            audioContext.destroy();
            reject(error);
          });
          audioContext.play();
        },
        fail: (error) => {
          common_vendor.index.__f__("error", "at api/ai-matchmaker.js:219", "写入临时音频文件失败:", error);
          reject(error);
        }
      });
    } catch (error) {
      common_vendor.index.__f__("error", "at api/ai-matchmaker.js:224", "播放音频失败:", error);
      reject(error);
    }
  });
}
class AudioRecorder {
  constructor() {
    this.recorderManager = common_vendor.index.getRecorderManager();
    this.isRecording = false;
    this.recordingPromise = null;
    this.options = {
      duration: 6e4,
      // 最长录音时间60秒
      sampleRate: 16e3,
      // 采样率
      numberOfChannels: 1,
      // 声道数
      encodeBitRate: 48e3,
      // 编码码率
      format: "wav",
      // 音频格式
      frameSize: 50
      // 指定帧大小，单位 KB
    };
    this.setupEventListeners();
  }
  setupEventListeners() {
    this.recorderManager.onStart(() => {
      common_vendor.index.__f__("log", "at api/ai-matchmaker.js:254", "录音开始");
      this.isRecording = true;
    });
    this.recorderManager.onStop((res) => {
      common_vendor.index.__f__("log", "at api/ai-matchmaker.js:259", "录音结束:", res);
      this.isRecording = false;
      if (this.recordingPromise) {
        this.recordingPromise.resolve(res);
        this.recordingPromise = null;
      }
    });
    this.recorderManager.onError((error) => {
      common_vendor.index.__f__("error", "at api/ai-matchmaker.js:268", "录音错误:", error);
      this.isRecording = false;
      if (this.recordingPromise) {
        this.recordingPromise.reject(error);
        this.recordingPromise = null;
      }
    });
  }
  /**
   * 开始录音
   */
  startRecording() {
    return new Promise((resolve, reject) => {
      if (this.isRecording) {
        reject(new Error("正在录音中"));
        return;
      }
      this.recordingPromise = { resolve, reject };
      common_vendor.index.authorize({
        scope: "scope.record",
        success: () => {
          this.recorderManager.start(this.options);
        },
        fail: () => {
          reject(new Error("录音权限被拒绝"));
        }
      });
    });
  }
  /**
   * 停止录音
   */
  stopRecording() {
    if (this.isRecording) {
      this.recorderManager.stop();
    }
  }
  /**
   * 取消录音
   */
  cancelRecording() {
    if (this.isRecording) {
      this.recorderManager.stop();
      if (this.recordingPromise) {
        this.recordingPromise.reject(new Error("录音被取消"));
        this.recordingPromise = null;
      }
    }
  }
}
function fileToBase64(filePath) {
  return new Promise((resolve, reject) => {
    const fs = common_vendor.index.getFileSystemManager();
    fs.readFile({
      filePath,
      encoding: "base64",
      success: (res) => {
        resolve(res.data);
      },
      fail: (error) => {
        reject(error);
      }
    });
  });
}
exports.AudioRecorder = AudioRecorder;
exports.analyzeImage = analyzeImage;
exports.chatWithAi = chatWithAi;
exports.fileToBase64 = fileToBase64;
exports.getAiMatchmakerInfo = getAiMatchmakerInfo;
exports.playAudioFromBase64 = playAudioFromBase64;
exports.speechToText = speechToText;
exports.textToSpeech = textToSpeech;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/ai-matchmaker.js.map
