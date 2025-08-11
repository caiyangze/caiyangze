// 简化的主题管理系统
import { ref, nextTick } from 'vue'

// 主题配置
export const themes = {
  // 默认主题（当前的粉紫色主题）
  default: {
    name: '梦幻粉紫',
    id: 'default',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    primary: '#FF6B9D',
    secondary: '#C44569'
  },
  
  // 深蓝主题
  ocean: {
    name: '深海蓝调',
    id: 'ocean',
    background: 'linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)',
    primary: '#4A90E2',
    secondary: '#357ABD'
  },
  
  // 暖橙主题
  sunset: {
    name: '日落橙光',
    id: 'sunset',
    background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%)',
    primary: '#FF8C42',
    secondary: '#FF6B35'
  },
  
  // 森林绿主题
  forest: {
    name: '森林绿意',
    id: 'forest',
    background: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)',
    primary: '#27AE60',
    secondary: '#229954'
  },
  
  // 紫罗兰主题
  violet: {
    name: '紫罗兰梦',
    id: 'violet',
    background: 'linear-gradient(135deg, #8360c3 0%, #2ebf91 100%)',
    primary: '#8E44AD',
    secondary: '#9B59B6'
  },
  
  // 樱花粉主题
  sakura: {
    name: '樱花粉嫩',
    id: 'sakura',
    background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    primary: '#FF69B4',
    secondary: '#FF1493'
  },
  
  // 暗夜主题
  dark: {
    name: '暗夜模式',
    id: 'dark',
    background: 'linear-gradient(135deg, #2C2C2C 0%, #1A1A1A 100%)',
    primary: '#BB86FC',
    secondary: '#3700B3'
  }
}

// 当前主题ID（确保初始化）
export const currentThemeId = ref('default')

// 当前主题背景（响应式，确保初始化）
export const currentBackground = ref('linear-gradient(135deg, #667eea 0%, #764ba2 100%)')

// 确保响应式变量已正确初始化
if (!currentThemeId.value) {
  currentThemeId.value = 'default'
}

if (!currentBackground.value) {
  currentBackground.value = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 获取当前主题
export function getCurrentTheme() {
  return themes[currentThemeId.value] || themes.default
}

// 获取所有主题
export function getAllThemes() {
  return Object.values(themes)
}

// 切换主题
export function switchTheme(themeId) {
  console.log('简化主题切换:', themeId)

  if (themes[themeId]) {
    currentThemeId.value = themeId

    // 更新全局背景
    currentBackground.value = themes[themeId].background
    console.log('全局背景已更新:', currentBackground.value)

    // 保存到本地存储
    try {
      const timestamp = Date.now()
      uni.setStorageSync('selectedTheme', themeId)
      uni.setStorageSync('currentBackground', themes[themeId].background)
      uni.setStorageSync('themeChangeTime', timestamp)

      // 验证存储是否成功
      const savedTheme = uni.getStorageSync('selectedTheme')
      const savedBackground = uni.getStorageSync('currentBackground')
      const savedTime = uni.getStorageSync('themeChangeTime')

      console.log('主题已保存:', themeId, themes[themeId].background)
      console.log('存储验证:', {
        savedTheme,
        savedBackground,
        savedTime,
        timestamp
      })
    } catch (error) {
      console.error('保存主题失败:', error)
    }

    // 强制触发响应式更新 - 确保所有页面都能立即响应
    const themeData = themes[themeId]
    
    // 使用 nextTick 确保DOM更新后再发送事件
    nextTick(() => {
      try {
        console.log('=== 开始发送主题变化事件 ===');
        console.log('主题数据:', themeData);
        console.log('当前页面数量:', getCurrentPages().length);
        
        // 发送主题变化事件 - 多次发送确保可靠性
        for (let i = 0; i < 5; i++) {
          setTimeout(() => {
            console.log(`=== 发送第${i+1}次事件 ===`);
            
            uni.$emit('themeChanged', themeData)
            console.log('已发送 themeChanged 事件');
            
            uni.$emit('globalThemeUpdate', {
              themeId,
              background: themeData.background,
              theme: themeData,
              timestamp: Date.now()
            })
            console.log('已发送 globalThemeUpdate 事件');
            
            uni.$emit('simpleThemeChanged', {
              themeId,
              theme: themeData,
              background: themeData.background,
              timestamp: Date.now()
            })
            console.log('已发送 simpleThemeChanged 事件');
            
            // 额外发送强制更新事件
            uni.$emit('forceThemeUpdate', {
              themeId,
              background: themeData.background,
              theme: themeData,
              force: true,
              timestamp: Date.now()
            })
            console.log('已发送 forceThemeUpdate 事件');
            
            console.log(`=== 第${i+1}次事件发送完成 ===`);
          }, i * 100) // 每100ms发送一次，确保可靠性
        }
      } catch (error) {
        console.error('发送主题切换事件失败:', error)
      }
    })

    console.log('主题切换完成:', themes[themeId].name)
    return true
  } else {
    console.error('主题不存在:', themeId)
    return false
  }
}

// 初始化主题
export function initTheme() {
  try {
    const savedTheme = uni.getStorageSync('selectedTheme')
    console.log('初始化主题，本地存储:', savedTheme)

    if (savedTheme && themes[savedTheme]) {
      currentThemeId.value = savedTheme
      currentBackground.value = themes[savedTheme].background
      console.log('应用保存的主题:', savedTheme)
    } else {
      currentThemeId.value = 'default'
      currentBackground.value = themes.default.background
      console.log('应用默认主题')
    }

    console.log('主题初始化完成，背景:', currentBackground.value)
  } catch (error) {
    console.error('主题初始化失败:', error)
    currentThemeId.value = 'default'
    currentBackground.value = themes.default.background
  }
}

// 获取主题样式
export function getThemeStyle() {
  const theme = getCurrentTheme()
  return {
    background: theme.background,
    '--theme-primary': theme.primary,
    '--theme-secondary': theme.secondary
  }
}

// 导出changeTheme作为switchTheme的别名
export const changeTheme = switchTheme
