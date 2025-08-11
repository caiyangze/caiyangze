// 统一主题混入 - 让所有页面都能响应主题变化
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import {
  getCurrentTheme,
  switchTheme,
  initTheme as initSimpleTheme,
  currentThemeId,
  currentBackground,
  getAllThemes
} from '@/utils/simple-theme.js'

export function useTheme() {
  // 直接使用全局响应式数据，不创建本地副本
  const currentTheme = computed(() => getCurrentTheme())

  // 监听主题变化事件
  const handleThemeChange = (theme) => {
    console.log('页面接收到主题变化:', theme.name)
    // 强制触发响应式更新
    currentBackground.value = theme.background
  }

  // 监听全局主题更新事件
  const handleGlobalThemeUpdate = (data) => {
    console.log('页面接收到全局主题更新:', data.themeId)
    // 强制触发响应式更新
    currentBackground.value = data.background
  }

  // 监听强制主题更新事件
  const handleForceThemeUpdate = (data) => {
    console.log('页面接收到强制主题更新:', data.themeId)
    // 强制触发响应式更新
    currentBackground.value = data.background
  }

  onMounted(() => {
    // 初始化主题
    initSimpleTheme()

    // 监听主题变化事件
    uni.$on('themeChanged', handleThemeChange)
    uni.$on('globalThemeUpdate', handleGlobalThemeUpdate)
    uni.$on('forceThemeUpdate', handleForceThemeUpdate)

    console.log('主题混入已初始化，当前主题:', currentTheme.value.name)
  })

  onUnmounted(() => {
    // 移除事件监听
    uni.$off('themeChanged', handleThemeChange)
    uni.$off('globalThemeUpdate', handleGlobalThemeUpdate)
    uni.$off('forceThemeUpdate', handleForceThemeUpdate)
  })

  // 获取主题样式对象（适配简化主题系统）
  const getThemeStyles = () => {
    const theme = currentTheme.value
    return {
      // 背景相关
      backgroundStyle: {
        background: theme.background
      },

      // 按钮相关
      primaryButtonStyle: {
        background: `linear-gradient(135deg, ${theme.primary} 0%, ${theme.secondary} 100%)`,
        color: '#FFFFFF'
      },

      // 文字相关
      primaryTextStyle: {
        color: '#FFFFFF'
      },
      secondaryTextStyle: {
        color: 'rgba(255, 255, 255, 0.8)'
      }
    }
  }

  // 获取主题CSS变量（适配简化主题系统）
  const getThemeCSSVars = () => {
    const theme = currentTheme.value
    return {
      '--theme-primary': theme.primary,
      '--theme-secondary': theme.secondary,
      '--theme-background': theme.background
    }
  }

  // 计算属性：当前背景 - 确保响应式更新
  const computedBackground = computed(() => {
    // 强制依赖currentBackground的值，确保响应式更新
    return currentBackground.value
  })

  // 切换主题的方法
  const changeTheme = (themeId) => {
    console.log('主题混入切换主题:', themeId)
    return switchTheme(themeId)
  }

  // 获取所有可用主题
  const availableThemes = computed(() => {
    return getAllThemes()
  })

  return {
    currentTheme,
    computedBackground,
    getThemeStyles,
    getThemeCSSVars,
    changeTheme,
    availableThemes,
    currentThemeId,
    currentBackground
  }
}
