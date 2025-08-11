/**
 * 钱包相关工具函数
 */

/**
 * 格式化金额显示
 * @param {Number|String} amount 金额
 * @param {String} unit 单位，默认为'元'
 * @param {Number} decimals 小数位数，默认为2
 * @returns {String} 格式化后的金额字符串
 */
export function formatAmount(amount, unit = '元', decimals = 2) {
  if (amount === null || amount === undefined || amount === '') {
    return `0${unit}`;
  }
  
  const num = parseFloat(amount);
  if (isNaN(num)) {
    return `0${unit}`;
  }
  
  if (unit === '币') {
    return `${Math.floor(num)}${unit}`;
  }
  
  return `${num.toFixed(decimals)}${unit}`;
}

/**
 * 格式化虚拟币数量
 * @param {Number} coinAmount 虚拟币数量
 * @returns {String} 格式化后的虚拟币字符串
 */
export function formatCoin(coinAmount) {
  return formatAmount(coinAmount, '币', 0);
}

/**
 * 格式化现金金额
 * @param {Number} cashAmount 现金金额
 * @returns {String} 格式化后的现金字符串
 */
export function formatCash(cashAmount) {
  return formatAmount(cashAmount, '元', 2);
}

/**
 * 验证充值金额
 * @param {Number} coinAmount 充值虚拟币数量
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export function validateRechargeAmount(coinAmount) {
  if (!coinAmount || coinAmount <= 0) {
    return { valid: false, message: '请选择充值套餐' };
  }
  
  if (coinAmount < 10) {
    return { valid: false, message: '最少充值10虚拟币' };
  }
  
  if (coinAmount > 100000) {
    return { valid: false, message: '单次充值不能超过100000虚拟币' };
  }
  
  return { valid: true, message: '验证通过' };
}

/**
 * 验证提现金额
 * @param {Number} amount 提现金额
 * @param {Number} balance 当前余额
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export function validateWithdrawAmount(amount, balance) {
  if (!amount || amount <= 0) {
    return { valid: false, message: '请输入有效的提现金额' };
  }
  
  if (amount < 10) {
    return { valid: false, message: '提现金额不能少于10元' };
  }
  
  if (amount > balance) {
    return { valid: false, message: '提现金额不能超过当前余额' };
  }
  
  if (amount > 50000) {
    return { valid: false, message: '单次提现不能超过50000元' };
  }
  
  return { valid: true, message: '验证通过' };
}

/**
 * 验证消费金额
 * @param {Number} coinAmount 消费虚拟币数量
 * @param {Number} balance 当前虚拟币余额
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export function validateConsumeAmount(coinAmount, balance) {
  if (!coinAmount || coinAmount <= 0) {
    return { valid: false, message: '请输入有效的消费数量' };
  }
  
  if (coinAmount > balance) {
    return { valid: false, message: '虚拟币余额不足' };
  }
  
  return { valid: true, message: '验证通过' };
}

/**
 * 获取交易类型描述
 * @param {Number} type 交易类型
 * @returns {String} 交易类型描述
 */
export function getTransactionTypeDesc(type) {
  const typeMap = {
    1: '充值',
    2: '消费',
    3: '收入',
    4: '提现',
    5: '退款'
  };
  return typeMap[type] || '未知';
}

/**
 * 获取交易类型图标
 * @param {Number} type 交易类型
 * @returns {String} 图标名称
 */
export function getTransactionTypeIcon(type) {
  const iconMap = {
    1: 'plus-circle',     // 充值
    2: 'minus-circle',    // 消费
    3: 'arrow-up-circle', // 收入
    4: 'arrow-down-circle', // 提现
    5: 'refresh-circle'   // 退款
  };
  return iconMap[type] || 'help-circle';
}

/**
 * 获取支付方式描述
 * @param {Number} method 支付方式
 * @returns {String} 支付方式描述
 */
export function getPaymentMethodDesc(method) {
  const methodMap = {
    1: '支付宝',
    2: '微信支付',
    3: '银行卡'
  };
  return methodMap[method] || '未知';
}

/**
 * 获取钱包状态描述
 * @param {Number} status 钱包状态
 * @returns {String} 状态描述
 */
export function getWalletStatusDesc(status) {
  const statusMap = {
    0: '冻结',
    1: '正常'
  };
  return statusMap[status] || '未知';
}

/**
 * 格式化时间显示
 * @param {String|Date} time 时间
 * @param {String} format 格式，默认为'MM-DD HH:mm'
 * @returns {String} 格式化后的时间字符串
 */
export function formatTime(time, format = 'MM-DD HH:mm') {
  if (!time) return '';
  
  const date = new Date(time);
  if (isNaN(date.getTime())) return '';
  
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 计算充值优惠
 * @param {Number} coinAmount 充值虚拟币数量
 * @returns {Object} 优惠信息 {bonus: number, discount: number}
 */
export function calculateRechargeBonus(coinAmount) {
  let bonus = 0;
  let discount = 0;
  
  if (coinAmount >= 10000) {
    bonus = Math.floor(coinAmount * 0.3); // 30%赠送
    discount = 0.3;
  } else if (coinAmount >= 5000) {
    bonus = Math.floor(coinAmount * 0.24); // 24%赠送
    discount = 0.24;
  } else if (coinAmount >= 2000) {
    bonus = Math.floor(coinAmount * 0.2); // 20%赠送
    discount = 0.2;
  } else if (coinAmount >= 1000) {
    bonus = Math.floor(coinAmount * 0.15); // 15%赠送
    discount = 0.15;
  } else if (coinAmount >= 500) {
    bonus = Math.floor(coinAmount * 0.1); // 10%赠送
    discount = 0.1;
  }
  
  return { bonus, discount };
}

/**
 * 生成交易订单号
 * @param {String} prefix 前缀，默认为'TXN'
 * @returns {String} 订单号
 */
export function generateOrderId(prefix = 'TXN') {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
  return `${prefix}${timestamp}${random}`;
}

/**
 * 检查钱包是否可用
 * @param {Object} walletInfo 钱包信息
 * @returns {Object} 检查结果 {available: boolean, message: string}
 */
export function checkWalletAvailable(walletInfo) {
  if (!walletInfo) {
    return { available: false, message: '钱包信息不存在' };
  }
  
  if (walletInfo.walletStatus === 0) {
    return { available: false, message: '钱包已被冻结' };
  }
  
  return { available: true, message: '钱包可用' };
}

/**
 * 获取充值套餐列表
 * @returns {Array} 充值套餐列表
 */
export function getRechargePackages() {
  return [
    { 
      id: 1, 
      coinAmount: 100, 
      price: 10, 
      bonus: 0, 
      desc: '100虚拟币',
      popular: false
    },
    { 
      id: 2, 
      coinAmount: 500, 
      price: 50, 
      bonus: 50, 
      desc: '500虚拟币+50赠送',
      popular: false
    },
    { 
      id: 3, 
      coinAmount: 1000, 
      price: 100, 
      bonus: 150, 
      desc: '1000虚拟币+150赠送',
      popular: true
    },
    { 
      id: 4, 
      coinAmount: 2000, 
      price: 200, 
      bonus: 400, 
      desc: '2000虚拟币+400赠送',
      popular: false
    },
    { 
      id: 5, 
      coinAmount: 5000, 
      price: 500, 
      bonus: 1200, 
      desc: '5000虚拟币+1200赠送',
      popular: false
    },
    { 
      id: 6, 
      coinAmount: 10000, 
      price: 1000, 
      bonus: 3000, 
      desc: '10000虚拟币+3000赠送',
      popular: false
    }
  ];
}

/**
 * 获取提现手续费
 * @param {Number} amount 提现金额
 * @returns {Number} 手续费
 */
export function getWithdrawFee(amount) {
  if (amount <= 0) return 0;
  
  // 提现手续费规则：
  // 100元以下：免费
  // 100-1000元：2元
  // 1000元以上：0.2%，最高20元
  
  if (amount < 100) {
    return 0;
  } else if (amount <= 1000) {
    return 2;
  } else {
    const fee = amount * 0.002;
    return Math.min(fee, 20);
  }
}

export default {
  formatAmount,
  formatCoin,
  formatCash,
  validateRechargeAmount,
  validateWithdrawAmount,
  validateConsumeAmount,
  getTransactionTypeDesc,
  getTransactionTypeIcon,
  getPaymentMethodDesc,
  getWalletStatusDesc,
  formatTime,
  calculateRechargeBonus,
  generateOrderId,
  checkWalletAvailable,
  getRechargePackages,
  getWithdrawFee
};
