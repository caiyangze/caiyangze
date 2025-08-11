package com.zhentao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhentao.pojo.TbDateFeedback;

import java.util.Map;

/**
 * 约会反馈业务服务接口
 */
public interface DateFeedbackService {

    /**
     * 提交约会反馈
     * @param feedback 反馈信息
     * @return 是否成功
     */
    boolean submitFeedback(TbDateFeedback feedback);

    /**
     * 根据约会ID和用户ID获取反馈
     * @param arrangementId 约会安排ID
     * @param userId 用户ID
     * @return 反馈信息
     */
    TbDateFeedback getFeedbackByArrangementAndUser(Long arrangementId, Long userId);

    /**
     * 检查用户是否已提交反馈
     * @param arrangementId 约会安排ID
     * @param userId 用户ID
     * @return 是否已提交
     */
    boolean hasFeedbackSubmitted(Long arrangementId, Long userId);

    /**
     * 获取约会的所有反馈信息
     * @param arrangementId 约会安排ID
     * @return 反馈统计信息
     */
    Map<String, Object> getAllFeedbackByArrangement(Long arrangementId);

    /**
     * 检查约会是否可以标记为完成
     * @param arrangementId 约会安排ID
     * @return 是否可以完成
     */
    boolean canMarkAsCompleted(Long arrangementId);
}
