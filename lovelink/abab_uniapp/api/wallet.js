/**
 * 钱包相关API
 */
import http from './http';

// API基础路径
const BASE_PATH = '/wallet';

/**
 * 获取用户钱包信息
 * @returns {Promise} 返回包含钱包信息的Promise
 */
export const getWalletInfo = () => {
  return http.get(`${BASE_PATH}/info`);
};

/**
 * 获取钱包交易记录
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页大小
 * @param {Number} type 交易类型（可选）1-充值，2-消费，3-收入，4-提现，5-退款
 * @returns {Promise} 返回包含交易记录的Promise
 */
export const getTransactions = (pageNum = 1, pageSize = 20, type = null) => {
  const params = { pageNum, pageSize };
  if (type !== null) {
    params.type = type;
  }
  return http.get(`${BASE_PATH}/transactions`, params);
};

/**
 * 虚拟币充值
 * @param {Object} data 充值信息
 * @param {Number} data.coinAmount 充值虚拟币数量
 * @param {String} data.transactionDesc 交易描述
 * @param {String} data.relatedId 关联订单号
 * @param {Number} data.paymentMethod 支付方式 1-支付宝，2-微信，3-银行卡
 * @returns {Promise} 返回充值结果的Promise
 */
export const recharge = (data) => {
  return http.post(`${BASE_PATH}/recharge`, data);
};

/**
 * 虚拟币消费
 * @param {Object} data 消费信息
 * @param {Number} data.coinAmount 消费虚拟币数量
 * @param {String} data.transactionDesc 交易描述
 * @param {String} data.relatedId 关联订单号
 * @param {Number} data.voucherOrderId 优惠券订单ID（可选）
 * @param {Number} data.voucherDiscountAmount 优惠券抵扣金额（可选）
 * @returns {Promise} 返回消费结果的Promise
 */
export const consume = (data) => {
  return http.post(`${BASE_PATH}/consume`, data);
};

/**
 * 现金收入
 * @param {Object} data 收入信息
 * @param {Number} data.transactionAmount 收入金额
 * @param {String} data.transactionDesc 交易描述
 * @param {String} data.relatedId 关联订单号
 * @returns {Promise} 返回收入结果的Promise
 */
export const income = (data) => {
  return http.post(`${BASE_PATH}/income`, data);
};

/**
 * 现金提现申请
 * @param {Object} data 提现信息
 * @param {Number} data.transactionAmount 提现金额
 * @param {String} data.withdrawAccount 提现账户
 * @param {Number} data.withdrawAccountType 提现账户类型 1-支付宝，2-微信，3-银行卡
 * @param {String} data.withdrawAccountName 提现账户姓名
 * @param {String} data.transactionDesc 交易描述
 * @returns {Promise} 返回提现申请结果的Promise
 */
export const withdraw = (data) => {
  return http.post(`${BASE_PATH}/withdraw`, data);
};

/**
 * 获取钱包统计信息
 * @returns {Promise} 返回包含统计信息的Promise
 */
export const getStatistics = () => {
  return http.get(`${BASE_PATH}/statistics`);
};

/**
 * 检查钱包状态
 * @returns {Promise} 返回钱包状态检查结果的Promise
 */
export const checkWalletStatus = () => {
  return http.get(`${BASE_PATH}/status`);
};

/**
 * 获取充值套餐列表（模拟数据，实际应该从后端获取）
 * @returns {Array} 充值套餐列表
 */
export const getRechargePackages = () => {
  return [
    { id: 1, coinAmount: 100, price: 10, bonus: 0, desc: '100虚拟币' },
    { id: 2, coinAmount: 500, price: 50, bonus: 50, desc: '500虚拟币+50赠送' },
    { id: 3, coinAmount: 1000, price: 100, bonus: 150, desc: '1000虚拟币+150赠送' },
    { id: 4, coinAmount: 2000, price: 200, bonus: 400, desc: '2000虚拟币+400赠送' },
    { id: 5, coinAmount: 5000, price: 500, bonus: 1200, desc: '5000虚拟币+1200赠送' },
    { id: 6, coinAmount: 10000, price: 1000, bonus: 3000, desc: '10000虚拟币+3000赠送' }
  ];
};

/**
 * 获取交易类型描述
 * @param {Number} type 交易类型
 * @returns {String} 交易类型描述
 */
export const getTransactionTypeDesc = (type) => {
  const typeMap = {
    1: '充值',
    2: '消费',
    3: '收入',
    4: '提现',
    5: '退款'
  };
  return typeMap[type] || '未知';
};

/**
 * 获取支付方式描述
 * @param {Number} method 支付方式
 * @returns {String} 支付方式描述
 */
export const getPaymentMethodDesc = (method) => {
  const methodMap = {
    1: '支付宝',
    2: '微信支付',
    3: '银行卡'
  };
  return methodMap[method] || '未知';
};

/**
 * 格式化金额显示
 * @param {Number} amount 金额
 * @param {String} unit 单位
 * @returns {String} 格式化后的金额
 */
export const formatAmount = (amount, unit = '元') => {
  if (amount === null || amount === undefined) {
    return `0${unit}`;
  }
  
  if (unit === '币') {
    return `${amount}${unit}`;
  }
  
  return `${parseFloat(amount).toFixed(2)}${unit}`;
};

/**
 * 验证提现金额
 * @param {Number} amount 提现金额
 * @param {Number} balance 当前余额
 * @returns {Object} 验证结果
 */
export const validateWithdrawAmount = (amount, balance) => {
  if (!amount || amount <= 0) {
    return { valid: false, message: '请输入有效的提现金额' };
  }
  
  if (amount < 10) {
    return { valid: false, message: '提现金额不能少于10元' };
  }
  
  if (amount > balance) {
    return { valid: false, message: '提现金额不能超过当前余额' };
  }
  
  return { valid: true, message: '验证通过' };
};

/**
 * 验证充值数量
 * @param {Number} coinAmount 充值虚拟币数量
 * @returns {Object} 验证结果
 */
export const validateRechargeAmount = (coinAmount) => {
  if (!coinAmount || coinAmount <= 0) {
    return { valid: false, message: '请选择充值套餐' };
  }
  
  if (coinAmount < 10) {
    return { valid: false, message: '最少充值10虚拟币' };
  }
  
  return { valid: true, message: '验证通过' };
};

export default {
  getWalletInfo,
  getTransactions,
  recharge,
  consume,
  income,
  withdraw,
  getStatistics,
  checkWalletStatus,
  getRechargePackages,
  getTransactionTypeDesc,
  getPaymentMethodDesc,
  formatAmount,
  validateWithdrawAmount,
  validateRechargeAmount
};
