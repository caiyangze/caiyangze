// 主题配置管理
import { reactive, watch } from 'vue'

// 主题配置
export const themes = {
  // 默认主题（当前的粉紫色主题）
  default: {
    name: '梦幻粉紫',
    id: 'default',
    colors: {
      primary: '#FF6B9D',
      secondary: '#C44569',
      accent: '#F8B500',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      cardBg: 'rgba(255, 255, 255, 0.1)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.8)',
      textTertiary: 'rgba(255, 255, 255, 0.6)',
      border: 'rgba(255, 255, 255, 0.2)',
      shadow: 'rgba(0, 0, 0, 0.1)',
      success: '#00D4AA',
      warning: '#F8B500',
      error: '#FF6B6B',
      info: '#4ECDC4'
    }
  },
  
  // 深蓝主题
  ocean: {
    name: '深海蓝调',
    id: 'ocean',
    colors: {
      primary: '#4A90E2',
      secondary: '#357ABD',
      accent: '#50C878',
      background: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)',
      cardBg: 'rgba(255, 255, 255, 0.1)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.8)',
      textTertiary: 'rgba(255, 255, 255, 0.6)',
      border: 'rgba(255, 255, 255, 0.2)',
      shadow: 'rgba(0, 0, 0, 0.2)',
      success: '#50C878',
      warning: '#FFB347',
      error: '#FF6B6B',
      info: '#87CEEB'
    }
  },
  
  // 暖橙主题
  sunset: {
    name: '日落橙光',
    id: 'sunset',
    colors: {
      primary: '#FF8C42',
      secondary: '#FF6B35',
      accent: '#FFD23F',
      background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%)',
      cardBg: 'rgba(255, 255, 255, 0.15)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.9)',
      textTertiary: 'rgba(255, 255, 255, 0.7)',
      border: 'rgba(255, 255, 255, 0.3)',
      shadow: 'rgba(255, 140, 66, 0.2)',
      success: '#32CD32',
      warning: '#FFD700',
      error: '#FF4757',
      info: '#74B9FF'
    }
  },
  
  // 森林绿主题
  forest: {
    name: '森林绿意',
    id: 'forest',
    colors: {
      primary: '#27AE60',
      secondary: '#229954',
      accent: '#F39C12',
      background: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)',
      cardBg: 'rgba(255, 255, 255, 0.1)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.8)',
      textTertiary: 'rgba(255, 255, 255, 0.6)',
      border: 'rgba(255, 255, 255, 0.2)',
      shadow: 'rgba(0, 0, 0, 0.15)',
      success: '#2ECC71',
      warning: '#F39C12',
      error: '#E74C3C',
      info: '#3498DB'
    }
  },
  
  // 紫罗兰主题
  violet: {
    name: '紫罗兰梦',
    id: 'violet',
    colors: {
      primary: '#8E44AD',
      secondary: '#9B59B6',
      accent: '#E67E22',
      background: 'linear-gradient(135deg, #8360c3 0%, #2ebf91 100%)',
      cardBg: 'rgba(255, 255, 255, 0.1)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.8)',
      textTertiary: 'rgba(255, 255, 255, 0.6)',
      border: 'rgba(255, 255, 255, 0.2)',
      shadow: 'rgba(142, 68, 173, 0.2)',
      success: '#1ABC9C',
      warning: '#F39C12',
      error: '#E74C3C',
      info: '#3498DB'
    }
  },
  
  // 樱花粉主题
  sakura: {
    name: '樱花粉嫩',
    id: 'sakura',
    colors: {
      primary: '#FF69B4',
      secondary: '#FF1493',
      accent: '#FFB6C1',
      background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
      cardBg: 'rgba(255, 255, 255, 0.2)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.9)',
      textTertiary: 'rgba(255, 255, 255, 0.7)',
      border: 'rgba(255, 255, 255, 0.3)',
      shadow: 'rgba(255, 105, 180, 0.2)',
      success: '#98FB98',
      warning: '#FFD700',
      error: '#FF6347',
      info: '#87CEFA'
    }
  },
  
  // 暗夜主题
  dark: {
    name: '暗夜模式',
    id: 'dark',
    colors: {
      primary: '#BB86FC',
      secondary: '#3700B3',
      accent: '#03DAC6',
      background: 'linear-gradient(135deg, #2C2C2C 0%, #1A1A1A 100%)',
      cardBg: 'rgba(255, 255, 255, 0.05)',
      textPrimary: '#FFFFFF',
      textSecondary: 'rgba(255, 255, 255, 0.7)',
      textTertiary: 'rgba(255, 255, 255, 0.5)',
      border: 'rgba(255, 255, 255, 0.1)',
      shadow: 'rgba(0, 0, 0, 0.3)',
      success: '#4CAF50',
      warning: '#FF9800',
      error: '#F44336',
      info: '#2196F3'
    }
  }
}

// 当前主题状态 - 使用深度响应式
export const currentTheme = reactive({
  theme: { ...themes.default },
  themeId: 'default'
})

// 获取当前主题
export function getCurrentTheme() {
  return currentTheme.theme
}

// 切换主题
export function switchTheme(themeId) {
  console.log('开始切换主题到:', themeId)

  if (themes[themeId]) {
    const oldTheme = currentTheme.theme
    // 深度复制主题对象以确保响应式更新
    currentTheme.theme = { ...themes[themeId], colors: { ...themes[themeId].colors } }
    currentTheme.themeId = themeId

    console.log('主题对象已更新:', {
      oldTheme: oldTheme.name,
      newTheme: themes[themeId].name,
      newBackground: themes[themeId].colors.background
    })

    // 保存到本地存储
    try {
      uni.setStorageSync('selectedTheme', themeId)
      console.log('主题已保存到本地存储')
    } catch (error) {
      console.error('保存主题到本地存储失败:', error)
    }

    // 应用主题到页面
    applyThemeToPage()

    console.log('主题已切换到:', themes[themeId].name)
  } else {
    console.error('主题不存在:', themeId)
  }
}

// 初始化主题
export function initTheme() {
  try {
    // 从本地存储获取保存的主题
    const savedTheme = uni.getStorageSync('selectedTheme')
    console.log('从本地存储获取的主题:', savedTheme)

    if (savedTheme && themes[savedTheme]) {
      console.log('应用保存的主题:', savedTheme)
      switchTheme(savedTheme)
    } else {
      console.log('应用默认主题')
      // 默认主题
      switchTheme('default')
    }
  } catch (error) {
    console.error('主题初始化失败:', error)
    switchTheme('default')
  }
}

// 应用主题到页面
export function applyThemeToPage() {
  const theme = getCurrentTheme()
  
  // 设置CSS变量到根元素
  const cssVars = {
    '--theme-primary': theme.colors.primary,
    '--theme-secondary': theme.colors.secondary,
    '--theme-accent': theme.colors.accent,
    '--theme-background': theme.colors.background,
    '--theme-card-bg': theme.colors.cardBg,
    '--theme-text-primary': theme.colors.textPrimary,
    '--theme-text-secondary': theme.colors.textSecondary,
    '--theme-text-tertiary': theme.colors.textTertiary,
    '--theme-border': theme.colors.border,
    '--theme-shadow': theme.colors.shadow,
    '--theme-success': theme.colors.success,
    '--theme-warning': theme.colors.warning,
    '--theme-error': theme.colors.error,
    '--theme-info': theme.colors.info
  }
  
  // 在小程序中，我们需要通过其他方式应用主题
  // 这里我们触发一个全局事件，让各个页面监听并更新
  uni.$emit('themeChanged', theme)
}

// 获取所有主题列表
export function getAllThemes() {
  return Object.values(themes)
}

// 监听主题变化
watch(() => currentTheme.themeId, (newThemeId) => {
  console.log('主题变化监听:', newThemeId)
})
