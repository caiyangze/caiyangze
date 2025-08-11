package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.pojo.TbDateFeedback;
import com.zhentao.pojo.TbDateArrangement;
import com.zhentao.service.DateFeedbackService;
import com.zhentao.service.TbDateArrangementService;
import com.zhentao.service.TbDateFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 约会反馈业务服务实现
 */
@Service
public class DateFeedbackServiceImpl implements DateFeedbackService {

    @Autowired
    private TbDateArrangementService dateArrangementService;

    @Autowired
    private TbDateFeedbackService tbDateFeedbackService;

    @Override
    @Transactional
    public boolean submitFeedback(TbDateFeedback feedback) {
        try {
            // 1. 检查是否已提交过反馈
            if (hasFeedbackSubmitted(feedback.getArrangementId(), feedback.getUserId())) {
                throw new RuntimeException("您已经提交过反馈了");
            }

            // 2. 验证约会安排是否存在且状态正确
            TbDateArrangement arrangement = dateArrangementService.getById(feedback.getArrangementId());
            if (arrangement == null) {
                throw new RuntimeException("约会安排不存在");
            }

            // 只有已确认或已完成的约会才能提交反馈
            if (arrangement.getArrangementStatus() != 1 && arrangement.getArrangementStatus() != 3) {
                throw new RuntimeException("约会状态不正确，无法提交反馈");
            }

            // 3. 验证用户是否参与了这次约会
            if (!arrangement.getUserId().equals(feedback.getUserId()) && 
                !arrangement.getTargetUserId().equals(feedback.getUserId())) {
                throw new RuntimeException("您没有参与这次约会");
            }

            // 4. 保存反馈
            boolean saved = tbDateFeedbackService.save(feedback);
            if (!saved) {
                throw new RuntimeException("保存反馈失败");
            }

            // 5. 检查是否双方都已提交反馈，如果是则更新约会状态为已完成
            if (canMarkAsCompleted(feedback.getArrangementId())) {
                arrangement.setArrangementStatus(3); // 已完成
                dateArrangementService.updateById(arrangement);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("提交反馈失败：" + e.getMessage());
        }
    }

    @Override
    public TbDateFeedback getFeedbackByArrangementAndUser(Long arrangementId, Long userId) {
        QueryWrapper<TbDateFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("arrangement_id", arrangementId)
                   .eq("user_id", userId);
        return tbDateFeedbackService.getOne(queryWrapper);
    }

    @Override
    public boolean hasFeedbackSubmitted(Long arrangementId, Long userId) {
        QueryWrapper<TbDateFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("arrangement_id", arrangementId)
                   .eq("user_id", userId);
        return tbDateFeedbackService.count(queryWrapper) > 0;
    }

    @Override
    public Map<String, Object> getAllFeedbackByArrangement(Long arrangementId) {
        // 获取约会安排信息
        TbDateArrangement arrangement = dateArrangementService.getById(arrangementId);
        if (arrangement == null) {
            throw new RuntimeException("约会安排不存在");
        }

        // 获取所有反馈
        QueryWrapper<TbDateFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("arrangement_id", arrangementId);
        List<TbDateFeedback> feedbacks = tbDateFeedbackService.list(queryWrapper);

        // 分别获取双方的反馈
        TbDateFeedback userAFeedback = null;
        TbDateFeedback userBFeedback = null;

        for (TbDateFeedback feedback : feedbacks) {
            if (feedback.getUserId().equals(arrangement.getUserId())) {
                userAFeedback = feedback;
            } else if (feedback.getUserId().equals(arrangement.getTargetUserId())) {
                userBFeedback = feedback;
            }
        }

        // 计算平均分
        double avgSatisfactionScore = 0;
        double avgMatchmakerScore = 0;
        int satisfactionCount = 0;
        int matchmakerCount = 0;

        if (userAFeedback != null) {
            avgSatisfactionScore += userAFeedback.getSatisfactionScore();
            satisfactionCount++;
            if (userAFeedback.getMatchmakerScore() != null) {
                avgMatchmakerScore += userAFeedback.getMatchmakerScore();
                matchmakerCount++;
            }
        }

        if (userBFeedback != null) {
            avgSatisfactionScore += userBFeedback.getSatisfactionScore();
            satisfactionCount++;
            if (userBFeedback.getMatchmakerScore() != null) {
                avgMatchmakerScore += userBFeedback.getMatchmakerScore();
                matchmakerCount++;
            }
        }

        if (satisfactionCount > 0) {
            avgSatisfactionScore = avgSatisfactionScore / satisfactionCount;
        }
        if (matchmakerCount > 0) {
            avgMatchmakerScore = avgMatchmakerScore / matchmakerCount;
        }

        // 统计愿意再次约会的人数
        int willingNextCount = 0;
        if (userAFeedback != null && userAFeedback.getIsWillingNext() == 1) {
            willingNextCount++;
        }
        if (userBFeedback != null && userBFeedback.getIsWillingNext() == 1) {
            willingNextCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("arrangement", arrangement);
        result.put("userAFeedback", userAFeedback);
        result.put("userBFeedback", userBFeedback);
        result.put("avgSatisfactionScore", Math.round(avgSatisfactionScore * 10) / 10.0);
        result.put("avgMatchmakerScore", Math.round(avgMatchmakerScore * 10) / 10.0);
        result.put("willingNextCount", willingNextCount);
        result.put("totalFeedbacks", feedbacks.size());
        result.put("bothSubmitted", feedbacks.size() >= 2);

        return result;
    }

    @Override
    public boolean canMarkAsCompleted(Long arrangementId) {
        // 获取约会安排信息
        TbDateArrangement arrangement = dateArrangementService.getById(arrangementId);
        if (arrangement == null) {
            return false;
        }

        // 检查双方是否都已提交反馈
        boolean userASubmitted = hasFeedbackSubmitted(arrangementId, arrangement.getUserId());
        boolean userBSubmitted = hasFeedbackSubmitted(arrangementId, arrangement.getTargetUserId());

        return userASubmitted && userBSubmitted;
    }
}
