"use strict";
const common_vendor = require("../../common/vendor.js");
const api_sign = require("../../api/sign.js");
const _sfc_main = {
  data() {
    return {
      signCount: 0,
      hasSignedToday: false,
      signing: false,
      currentMonth: "",
      todayDate: "",
      calendarDays: [],
      weekDays: ["日", "一", "二", "三", "四", "五", "六"]
    };
  },
  onLoad() {
    this.clearLocalSignStorage();
    this.initPage();
  },
  onShow() {
    this.loadSignData();
  },
  methods: {
    // 初始化页面
    initPage() {
      const now = /* @__PURE__ */ new Date();
      this.currentMonth = `${now.getFullYear()}年${now.getMonth() + 1}月`;
      this.todayDate = `${now.getMonth() + 1}/${now.getDate()}`;
      this.generateCalendar();
    },
    // 返回上一页
    goBack() {
      common_vendor.index.navigateBack();
    },
    // 加载签到数据
    async loadSignData() {
      try {
        const countResult = await api_sign.getSignCount();
        if (countResult.code === 200) {
          this.signCount = countResult.data || 0;
        }
        await this.checkTodaySignStatus();
        await this.loadMonthSignRecord();
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/sign/sign.vue:158", "获取签到数据失败:", error);
      }
    },
    // 检查今天签到状态
    async checkTodaySignStatus() {
      try {
        const result = await api_sign.checkTodaySign();
        if (result.code === 200) {
          this.hasSignedToday = result.data === true;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/sign/sign.vue:170", "检查今天签到状态失败:", error);
        const today = /* @__PURE__ */ new Date();
        const todayKey = `sign_${today.getFullYear()}_${today.getMonth() + 1}_${today.getDate()}`;
        this.hasSignedToday = common_vendor.index.getStorageSync(todayKey) === "true";
      }
    },
    // 加载本月签到记录
    async loadMonthSignRecord() {
      try {
        const result = await api_sign.getMonthSignRecord();
        if (result.code === 200) {
          const signRecord = result.data || 0;
          this.updateCalendarWithSignRecord(signRecord);
        } else {
          this.clearCalendarSignStatus();
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/sign/sign.vue:190", "获取本月签到记录失败:", error);
        this.clearCalendarSignStatus();
      }
    },
    // 处理签到
    async handleSign() {
      if (this.hasSignedToday || this.signing)
        return;
      this.signing = true;
      try {
        const result = await api_sign.userSign();
        if (result.code === 200) {
          this.hasSignedToday = true;
          this.signCount += 1;
          await this.loadMonthSignRecord();
          common_vendor.index.showToast({
            title: result.message || "签到成功，获得5个金币！",
            icon: "success",
            duration: 3e3
          });
        } else {
          common_vendor.index.showToast({
            title: result.message || "签到失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/sign/sign.vue:225", "签到失败:", error);
        let errorMessage = "签到失败";
        if (error && error.message) {
          errorMessage = error.message;
        }
        common_vendor.index.showToast({
          title: errorMessage,
          icon: "none"
        });
      } finally {
        this.signing = false;
      }
    },
    // 生成日历
    generateCalendar() {
      const now = /* @__PURE__ */ new Date();
      const year = now.getFullYear();
      const month = now.getMonth();
      const today = now.getDate();
      const firstDay = new Date(year, month, 1);
      const lastDay = new Date(year, month + 1, 0);
      const firstDayWeek = firstDay.getDay();
      const days = [];
      if (firstDayWeek > 0) {
        const prevMonth = new Date(year, month, 0);
        const prevMonthLastDate = prevMonth.getDate();
        for (let i = firstDayWeek - 1; i >= 0; i--) {
          const day = prevMonthLastDate - i;
          days.push({
            date: new Date(year, month - 1, day).getTime(),
            day,
            signed: false,
            isToday: false,
            inCurrentMonth: false
          });
        }
      }
      for (let day = 1; day <= lastDay.getDate(); day++) {
        const date = new Date(year, month, day);
        days.push({
          date: date.getTime(),
          day,
          signed: false,
          // 默认未签到，等待服务器数据更新
          isToday: day === today,
          inCurrentMonth: true
        });
      }
      const totalCells = 42;
      const remainingCells = totalCells - days.length;
      for (let day = 1; day <= remainingCells; day++) {
        days.push({
          date: new Date(year, month + 1, day).getTime(),
          day,
          signed: false,
          isToday: false,
          inCurrentMonth: false
        });
      }
      this.calendarDays = days;
    },
    // 根据BitMap记录更新日历
    updateCalendarWithSignRecord(signRecord) {
      if (!signRecord) {
        this.clearCalendarSignStatus();
        return;
      }
      const today = (/* @__PURE__ */ new Date()).getDate();
      for (let day = 1; day <= today; day++) {
        const hasSigned = (signRecord & 1 << day - 1) !== 0;
        this.calendarDays.forEach((calendarDay) => {
          if (calendarDay.day === day && calendarDay.inCurrentMonth) {
            calendarDay.signed = hasSigned;
          }
        });
      }
    },
    // 清空日历签到状态
    clearCalendarSignStatus() {
      this.calendarDays.forEach((day) => {
        if (day.inCurrentMonth) {
          day.signed = false;
        }
      });
    },
    // 清理本地存储的签到记录
    clearLocalSignStorage() {
      try {
        const storageInfo = common_vendor.index.getStorageInfoSync();
        const keys = storageInfo.keys || [];
        keys.forEach((key) => {
          if (key.startsWith("sign_")) {
            common_vendor.index.removeStorageSync(key);
          }
        });
        common_vendor.index.__f__("log", "at pages/sign/sign.vue:351", "已清理本地签到存储记录");
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/sign/sign.vue:353", "清理本地存储失败:", error);
      }
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goBack && $options.goBack(...args)),
    b: common_vendor.t($data.signCount),
    c: common_vendor.t($data.todayDate),
    d: common_vendor.t($data.hasSignedToday ? "✓" : "📅"),
    e: common_vendor.t($data.signing ? "签到中..." : $data.hasSignedToday ? "今日已签到" : "立即签到"),
    f: $data.hasSignedToday ? 1 : "",
    g: $data.signing ? 1 : "",
    h: common_vendor.o((...args) => $options.handleSign && $options.handleSign(...args)),
    i: $data.hasSignedToday || $data.signing,
    j: common_vendor.t($data.currentMonth),
    k: common_vendor.f($data.weekDays, (week, k0, i0) => {
      return {
        a: common_vendor.t(week),
        b: week
      };
    }),
    l: common_vendor.f($data.calendarDays, (day, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(day.day),
        b: day.signed && day.inCurrentMonth
      }, day.signed && day.inCurrentMonth ? {} : {}, {
        c: day.date,
        d: day.signed ? 1 : "",
        e: day.isToday ? 1 : "",
        f: !day.inCurrentMonth ? 1 : ""
      });
    })
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-bf476203"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/sign/sign.js.map
