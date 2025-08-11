import App from './App'
import { createSSRApp } from 'vue'
import http from './api/http'
import uniPopup from './components/common/uni-popup.vue'
import customTabBar from './components/custom-tab-bar.vue'
import { initGlobalErrorHandler, createVueErrorHandler, monitorScrollTopSetting } from './utils/error-handler'

export function createApp() {
  const app = createSSRApp(App)

  // 初始化全局错误处理器
  try {
    initGlobalErrorHandler()

    // 在开发环境中启用DOM监控
    if (process.env.NODE_ENV === 'development') {
      monitorScrollTopSetting()
    }

    // 设置Vue错误处理器
    app.config.errorHandler = createVueErrorHandler()
  } catch (error) {
    console.warn('初始化错误处理器失败:', error)
  }

  // 注册全局组件
  app.component('uni-popup', uniPopup)
  app.component('custom-tab-bar', customTabBar)

  // 全局配置
  app.config.globalProperties.$baseUrl = 'http://localhost:9001'
  app.config.globalProperties.$http = http

  return {
    app
  }
}