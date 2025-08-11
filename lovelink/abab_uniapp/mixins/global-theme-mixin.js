// 全局主题混入 - 供所有页面使用
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { globalThemeState, switchTheme, getAllThemes } from '@/utils/global-theme.js'

export function useGlobalThemeMixin() {
  // 当前背景 - 直接从全局状态计算
  const currentBackground = computed(() => {
    return globalThemeState.currentTheme.background
  })
  
  // 当前主题ID
  const currentThemeId = computed(() => {
    return globalThemeState.currentThemeId
  })
  
  // 所有可用主题
  const availableThemes = computed(() => {
    return getAllThemes()
  })
  
  // 主题变化处理函数
  const handleGlobalThemeChange = (data) => {
    console.log('📱 页面接收到主题变化:', data.theme.name)
    // 由于使用了响应式状态，这里不需要手动更新
    // Vue 会自动重新渲染使用了 currentBackground 的组件
  }
  
  // 页面挂载时监听主题变化
  onMounted(() => {
    console.log('🔗 页面开始监听全局主题变化')
    uni.$on('GLOBAL_THEME_CHANGED', handleGlobalThemeChange)
  })
  
  // 页面卸载时移除监听
  onUnmounted(() => {
    console.log('🔌 页面停止监听全局主题变化')
    uni.$off('GLOBAL_THEME_CHANGED', handleGlobalThemeChange)
  })
  
  return {
    // 响应式数据
    currentBackground,
    currentThemeId,
    availableThemes,
    
    // 方法
    switchTheme,
    
    // 事件处理
    handleGlobalThemeChange
  }
}