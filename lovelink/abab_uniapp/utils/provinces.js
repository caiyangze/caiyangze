/**
 * 中国省份列表
 */
export const CHINA_PROVINCES = [
  '北京市',
  '天津市', 
  '河北省',
  '山西省',
  '内蒙古自治区',
  '辽宁省',
  '吉林省',
  '黑龙江省',
  '上海市',
  '江苏省',
  '浙江省',
  '安徽省',
  '福建省',
  '江西省',
  '山东省',
  '河南省',
  '湖北省',
  '湖南省',
  '广东省',
  '广西壮族自治区',
  '海南省',
  '重庆市',
  '四川省',
  '贵州省',
  '云南省',
  '西藏自治区',
  '陕西省',
  '甘肃省',
  '青海省',
  '宁夏回族自治区',
  '新疆维吾尔自治区',
  '台湾省',
  '香港特别行政区',
  '澳门特别行政区'
];

/**
 * 省份简称映射
 */
export const PROVINCE_SHORT_MAP = {
  '北京市': '北京',
  '天津市': '天津',
  '河北省': '河北',
  '山西省': '山西',
  '内蒙古自治区': '内蒙古',
  '辽宁省': '辽宁',
  '吉林省': '吉林',
  '黑龙江省': '黑龙江',
  '上海市': '上海',
  '江苏省': '江苏',
  '浙江省': '浙江',
  '安徽省': '安徽',
  '福建省': '福建',
  '江西省': '江西',
  '山东省': '山东',
  '河南省': '河南',
  '湖北省': '湖北',
  '湖南省': '湖南',
  '广东省': '广东',
  '广西壮族自治区': '广西',
  '海南省': '海南',
  '重庆市': '重庆',
  '四川省': '四川',
  '贵州省': '贵州',
  '云南省': '云南',
  '西藏自治区': '西藏',
  '陕西省': '陕西',
  '甘肃省': '甘肃',
  '青海省': '青海',
  '宁夏回族自治区': '宁夏',
  '新疆维吾尔自治区': '新疆',
  '台湾省': '台湾',
  '香港特别行政区': '香港',
  '澳门特别行政区': '澳门'
};

/**
 * 获取省份简称
 * @param {String} fullName 省份全称
 * @returns {String} 省份简称
 */
export function getProvinceShortName(fullName) {
  return PROVINCE_SHORT_MAP[fullName] || fullName;
}

/**
 * 根据简称获取省份全称
 * @param {String} shortName 省份简称
 * @returns {String} 省份全称
 */
export function getProvinceFullName(shortName) {
  for (const [fullName, short] of Object.entries(PROVINCE_SHORT_MAP)) {
    if (short === shortName) {
      return fullName;
    }
  }
  return shortName;
}
