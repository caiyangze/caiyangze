// 全局主题管理系统 - 简化版本
import { ref, reactive, watch } from 'vue'

// 主题配置
const THEMES = {
  default: {
    id: 'default',
    name: '梦幻粉紫',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    primary: '#FF6B9D',
    secondary: '#C44569'
  },
  ocean: {
    id: 'ocean',
    name: '深海蓝调',
    background: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)',
    primary: '#4A90E2',
    secondary: '#357ABD'
  },
  sunset: {
    id: 'sunset',
    name: '日落橙光',
    background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%)',
    primary: '#FF8C42',
    secondary: '#FF6B35'
  },
  forest: {
    id: 'forest',
    name: '森林绿意',
    background: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)',
    primary: '#27AE60',
    secondary: '#229954'
  },
  violet: {
    id: 'violet',
    name: '紫罗兰梦',
    background: 'linear-gradient(135deg, #8360c3 0%, #2ebf91 100%)',
    primary: '#8E44AD',
    secondary: '#9B59B6'
  },
  sakura: {
    id: 'sakura',
    name: '樱花粉嫩',
    background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    primary: '#FF69B4',
    secondary: '#FF1493'
  },
  dark: {
    id: 'dark',
    name: '暗夜模式',
    background: 'linear-gradient(135deg, #2C2C2C 0%, #1A1A1A 100%)',
    primary: '#BB86FC',
    secondary: '#3700B3'
  }
}

// 全局主题状态 - 使用 reactive 确保深度响应式
export const globalThemeState = reactive({
  currentThemeId: 'default',
  currentTheme: THEMES.default,
  isInitialized: false
})

// 获取所有主题
export function getAllThemes() {
  return Object.values(THEMES)
}

// 获取当前主题
export function getCurrentTheme() {
  return globalThemeState.currentTheme
}

// 获取当前背景
export function getCurrentBackground() {
  return globalThemeState.currentTheme.background
}

// 切换主题 - 核心函数
export function switchTheme(themeId) {
  console.log('🎨 开始切换主题:', themeId)
  
  if (!THEMES[themeId]) {
    console.error('❌ 主题不存在:', themeId)
    return false
  }

  const newTheme = THEMES[themeId]
  
  // 更新全局状态
  globalThemeState.currentThemeId = themeId
  globalThemeState.currentTheme = newTheme
  
  console.log('✅ 主题状态已更新:', {
    id: themeId,
    name: newTheme.name,
    background: newTheme.background
  })

  // 保存到本地存储
  try {
    uni.setStorageSync('globalTheme', themeId)
    console.log('💾 主题已保存到本地存储')
  } catch (error) {
    console.error('❌ 保存主题失败:', error)
  }

  // 通知所有页面更新
  notifyThemeChange(newTheme)
  
  return true
}

// 通知主题变化
function notifyThemeChange(theme) {
  console.log('📢 通知主题变化:', theme.name)
  
  // 发送全局事件
  uni.$emit('GLOBAL_THEME_CHANGED', {
    theme,
    timestamp: Date.now()
  })
  
  console.log('📡 主题变化事件已发送')
}

// 初始化主题系统
export function initGlobalTheme() {
  if (globalThemeState.isInitialized) {
    console.log('⚠️ 主题系统已初始化，跳过')
    return
  }

  console.log('🚀 初始化全局主题系统')
  
  try {
    // 从本地存储读取主题
    const savedThemeId = uni.getStorageSync('globalTheme')
    
    if (savedThemeId && THEMES[savedThemeId]) {
      console.log('📖 读取到保存的主题:', savedThemeId)
      globalThemeState.currentThemeId = savedThemeId
      globalThemeState.currentTheme = THEMES[savedThemeId]
    } else {
      console.log('🎯 使用默认主题')
      globalThemeState.currentThemeId = 'default'
      globalThemeState.currentTheme = THEMES.default
    }
    
    globalThemeState.isInitialized = true
    
    console.log('✅ 主题系统初始化完成:', {
      currentThemeId: globalThemeState.currentThemeId,
      themeName: globalThemeState.currentTheme.name
    })
    
  } catch (error) {
    console.error('❌ 主题系统初始化失败:', error)
    // 使用默认主题
    globalThemeState.currentThemeId = 'default'
    globalThemeState.currentTheme = THEMES.default
    globalThemeState.isInitialized = true
  }
}

// 创建主题混入 - 供页面使用
export function useGlobalTheme() {
  return {
    // 响应式数据
    currentTheme: globalThemeState.currentTheme,
    currentBackground: getCurrentBackground(),
    allThemes: getAllThemes(),
    
    // 方法
    switchTheme,
    getCurrentTheme,
    getCurrentBackground
  }
}

// 自动初始化
initGlobalTheme()