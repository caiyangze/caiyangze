<template>
  <view class="sign-page">
    <!-- 背景层 -->
    <view class="bg-layer">
      <view class="bg-gradient"></view>
    </view>
    
    <!-- 内容区 -->
    <view class="content-container">
      <!-- 导航栏 -->
      <view class="nav-bar">
        <view class="nav-left" @click="goBack">
          <text class="nav-icon">←</text>
        </view>
        <text class="nav-title">每日签到</text>
        <view class="nav-right"></view>
      </view>
      
      <!-- 签到统计卡片 -->
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-number">{{ signCount }}</text>
          <text class="stats-label">连续签到</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-number">{{ todayDate }}</text>
          <text class="stats-label">今日日期</text>
        </view>
      </view>
      
      <!-- 签到按钮 -->
      <view class="sign-button-container">
        <button 
          class="sign-button"
          :class="{ 'signed': hasSignedToday, 'loading': signing }"
          @click="handleSign"
          :disabled="hasSignedToday || signing"
        >
          <view class="button-content">
            <text class="button-icon">{{ hasSignedToday ? '✓' : '📅' }}</text>
            <text class="button-text">
              {{ signing ? '签到中...' : (hasSignedToday ? '今日已签到' : '立即签到') }}
            </text>
          </view>
        </button>
      </view>
      
      <!-- 签到奖励说明 -->
      <view class="reward-section">
        <text class="section-title">签到奖励</text>
        <view class="reward-list">
          <view class="reward-item">
            <text class="reward-icon">🪙</text>
            <text class="reward-text">每日签到获得5金币</text>
          </view>
          <view class="reward-item">
            <text class="reward-icon">🎁</text>
            <text class="reward-text">连续7天额外获得优惠券</text>
          </view>
          <view class="reward-item">
            <text class="reward-icon">💎</text>
            <text class="reward-text">连续30天获得VIP体验</text>
          </view>
        </view>
      </view>
      
      <!-- 签到记录 -->
      <view class="history-section">
        <text class="section-title">本月签到记录</text>
        <view class="calendar-container">
          <view class="calendar-header">
            <text class="month-text">{{ currentMonth }}</text>
          </view>
          <!-- 星期标题 -->
          <view class="week-header">
            <view class="week-item" v-for="week in weekDays" :key="week">
              <text class="week-text">{{ week }}</text>
            </view>
          </view>
          <!-- 日历网格 -->
          <view class="calendar-grid">
            <view
              v-for="day in calendarDays"
              :key="day.date"
              class="calendar-day"
              :class="{
                'signed': day.signed,
                'today': day.isToday,
                'disabled': !day.inCurrentMonth
              }"
            >
              <text class="day-number">{{ day.day }}</text>
              <view v-if="day.signed && day.inCurrentMonth" class="sign-mark">✓</view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { userSign, getSignCount, checkTodaySign, getMonthSignRecord } from '@/api/sign.js'

export default {
  data() {
    return {
      signCount: 0,
      hasSignedToday: false,
      signing: false,
      currentMonth: '',
      todayDate: '',
      calendarDays: [],
      weekDays: ['日', '一', '二', '三', '四', '五', '六']
    }
  },
  
  onLoad() {
    // 清理旧的本地存储签到记录
    this.clearLocalSignStorage()
    this.initPage()
  },
  
  onShow() {
    this.loadSignData()
  },
  
  methods: {
    // 初始化页面
    initPage() {
      const now = new Date()
      this.currentMonth = `${now.getFullYear()}年${now.getMonth() + 1}月`
      this.todayDate = `${now.getMonth() + 1}/${now.getDate()}`
      this.generateCalendar()
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 加载签到数据
    async loadSignData() {
      try {
        // 获取连续签到天数
        const countResult = await getSignCount()
        if (countResult.code === 200) {
          this.signCount = countResult.data || 0
        }

        // 检查今天是否已签到
        await this.checkTodaySignStatus()

        // 加载本月签到记录
        await this.loadMonthSignRecord()
      } catch (error) {
        console.error('获取签到数据失败:', error)
      }
    },

    // 检查今天签到状态
    async checkTodaySignStatus() {
      try {
        const result = await checkTodaySign()
        if (result.code === 200) {
          this.hasSignedToday = result.data === true
        }
      } catch (error) {
        console.error('检查今天签到状态失败:', error)
        // 降级到本地存储检查
        const today = new Date()
        const todayKey = `sign_${today.getFullYear()}_${today.getMonth() + 1}_${today.getDate()}`
        this.hasSignedToday = uni.getStorageSync(todayKey) === 'true'
      }
    },

    // 加载本月签到记录
    async loadMonthSignRecord() {
      try {
        const result = await getMonthSignRecord()
        if (result.code === 200) {
          const signRecord = result.data || 0
          this.updateCalendarWithSignRecord(signRecord)
        } else {
          // 如果获取失败，清空日历签到状态
          this.clearCalendarSignStatus()
        }
      } catch (error) {
        console.error('获取本月签到记录失败:', error)
        // 获取失败时清空签到状态
        this.clearCalendarSignStatus()
      }
    },
    
    // 处理签到
    async handleSign() {
      if (this.hasSignedToday || this.signing) return
      
      this.signing = true
      
      try {
        const result = await userSign()
        
        if (result.code === 200) {
          // 签到成功
          this.hasSignedToday = true
          this.signCount += 1

          // 更新日历显示 - 重新从服务器获取数据
          await this.loadMonthSignRecord()

          uni.showToast({
            title: result.message || '签到成功，获得5个金币！',
            icon: 'success',
            duration: 3000
          })
        } else {
          uni.showToast({
            title: result.message || '签到失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('签到失败:', error)
        let errorMessage = '签到失败'
        if (error && error.message) {
          errorMessage = error.message
        }
        
        uni.showToast({
          title: errorMessage,
          icon: 'none'
        })
      } finally {
        this.signing = false
      }
    },
    
    // 生成日历
    generateCalendar() {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth()
      const today = now.getDate()

      // 获取当月第一天和最后一天
      const firstDay = new Date(year, month, 1)
      const lastDay = new Date(year, month + 1, 0)

      // 获取当月第一天是星期几（0=星期日，1=星期一...）
      const firstDayWeek = firstDay.getDay()

      const days = []

      // 添加上个月的日期（填充空白）
      if (firstDayWeek > 0) {
        const prevMonth = new Date(year, month, 0) // 上个月最后一天
        const prevMonthLastDate = prevMonth.getDate()

        for (let i = firstDayWeek - 1; i >= 0; i--) {
          const day = prevMonthLastDate - i
          days.push({
            date: new Date(year, month - 1, day).getTime(),
            day: day,
            signed: false,
            isToday: false,
            inCurrentMonth: false
          })
        }
      }

      // 添加当月的日期
      for (let day = 1; day <= lastDay.getDate(); day++) {
        const date = new Date(year, month, day)

        days.push({
          date: date.getTime(),
          day: day,
          signed: false, // 默认未签到，等待服务器数据更新
          isToday: day === today,
          inCurrentMonth: true
        })
      }

      // 添加下个月的日期（填充到6行，42个格子）
      const totalCells = 42
      const remainingCells = totalCells - days.length

      for (let day = 1; day <= remainingCells; day++) {
        days.push({
          date: new Date(year, month + 1, day).getTime(),
          day: day,
          signed: false,
          isToday: false,
          inCurrentMonth: false
        })
      }

      this.calendarDays = days
    },
    


    // 根据BitMap记录更新日历
    updateCalendarWithSignRecord(signRecord) {
      if (!signRecord) {
        this.clearCalendarSignStatus()
        return
      }

      const today = new Date().getDate()

      // 从BitMap中解析每一天的签到状态
      for (let day = 1; day <= today; day++) {
        // 检查第day位是否为1（注意BitMap是从0开始的）
        const hasSigned = (signRecord & (1 << (day - 1))) !== 0

        // 更新日历中对应的天
        this.calendarDays.forEach(calendarDay => {
          if (calendarDay.day === day && calendarDay.inCurrentMonth) {
            calendarDay.signed = hasSigned
          }
        })
      }
    },

    // 清空日历签到状态
    clearCalendarSignStatus() {
      this.calendarDays.forEach(day => {
        if (day.inCurrentMonth) {
          day.signed = false
        }
      })
    },

    // 清理本地存储的签到记录
    clearLocalSignStorage() {
      try {
        // 获取所有存储的key
        const storageInfo = uni.getStorageInfoSync()
        const keys = storageInfo.keys || []

        // 删除所有以 'sign_' 开头的key
        keys.forEach(key => {
          if (key.startsWith('sign_')) {
            uni.removeStorageSync(key)
          }
        })

        console.log('已清理本地签到存储记录')
      } catch (error) {
        console.error('清理本地存储失败:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.sign-page {
  min-height: 100vh;
  position: relative;
  
  .bg-layer {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;
    
    .bg-gradient {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
  }
  
  .content-container {
    padding: 40rpx 30rpx;
    padding-top: calc(var(--status-bar-height) + 40rpx);
    
    // 导航栏
    .nav-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 40rpx;
      
      .nav-left {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .nav-icon {
          font-size: 36rpx;
          color: #fff;
          font-weight: bold;
        }
      }
      
      .nav-title {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }
      
      .nav-right {
        width: 80rpx;
      }
    }
    
    // 统计卡片
    .stats-card {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;
      padding: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 40rpx;
      
      .stats-item {
        flex: 1;
        text-align: center;
        
        .stats-number {
          display: block;
          font-size: 48rpx;
          font-weight: bold;
          color: #fff;
          margin-bottom: 10rpx;
        }
        
        .stats-label {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }
      
      .stats-divider {
        width: 2rpx;
        height: 80rpx;
        background: rgba(255, 255, 255, 0.3);
        margin: 0 40rpx;
      }
    }
    
    // 签到按钮
    .sign-button-container {
      margin-bottom: 40rpx;
      
      .sign-button {
        width: 100%;
        height: 120rpx;
        border-radius: 60rpx;
        background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
        border: none;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 8rpx 30rpx rgba(255, 107, 107, 0.3);
        
        &.signed {
          background: linear-gradient(135deg, #52c41a, #73d13d);
          box-shadow: 0 8rpx 30rpx rgba(82, 196, 26, 0.3);
        }
        
        &.loading {
          opacity: 0.7;
        }
        
        .button-content {
          display: flex;
          align-items: center;
          
          .button-icon {
            font-size: 32rpx;
            margin-right: 15rpx;
          }
          
          .button-text {
            font-size: 32rpx;
            font-weight: bold;
            color: #fff;
          }
        }
      }
    }
    
    // 奖励说明
    .reward-section {
      margin-bottom: 40rpx;
      
      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 20rpx;
        display: block;
      }
      
      .reward-list {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20rpx;
        padding: 30rpx;
        
        .reward-item {
          display: flex;
          align-items: center;
          margin-bottom: 20rpx;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .reward-icon {
            font-size: 32rpx;
            margin-right: 20rpx;
          }
          
          .reward-text {
            font-size: 28rpx;
            color: rgba(255, 255, 255, 0.9);
          }
        }
      }
    }
    
    // 签到记录
    .history-section {
      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 20rpx;
        display: block;
      }
      
      .calendar-container {
        background: rgba(255, 255, 255, 0.1);
        border-radius: 20rpx;
        padding: 30rpx;
        
        .calendar-header {
          text-align: center;
          margin-bottom: 20rpx;

          .month-text {
            font-size: 28rpx;
            font-weight: bold;
            color: #fff;
          }
        }

        .week-header {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 10rpx;
          margin-bottom: 20rpx;

          .week-item {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 60rpx;

            .week-text {
              font-size: 24rpx;
              color: rgba(255, 255, 255, 0.8);
              font-weight: bold;
            }
          }
        }

        .calendar-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 10rpx;

          .calendar-day {
            aspect-ratio: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            border-radius: 10rpx;
            position: relative;
            min-height: 80rpx;

            &.today {
              background: rgba(255, 255, 255, 0.3);
              border: 2rpx solid #fff;
            }

            &.signed {
              background: rgba(82, 196, 26, 0.4);

              .day-number {
                color: #52c41a;
                font-weight: bold;
              }
            }

            &.disabled {
              opacity: 0.3;

              .day-number {
                color: rgba(255, 255, 255, 0.4);
              }
            }

            .day-number {
              font-size: 26rpx;
              color: #fff;
              font-weight: normal;
            }

            .sign-mark {
              position: absolute;
              top: 8rpx;
              right: 8rpx;
              font-size: 20rpx;
              color: #52c41a;
              font-weight: bold;
            }
          }
        }
      }
    }
  }
}
</style>
