package com.xuanxuan.mianshiya.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuanxuan.mianshiya.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xuanxuan.mianshiya.model.entity.QuestionBankQuestion;
import com.xuanxuan.mianshiya.model.entity.User;
import com.xuanxuan.mianshiya.model.vo.QuestionBankQuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题库题目关联服务
 *
 * @author <a href="https://github.com/lixuanxuan">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest);
    
    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request);

    /**
     * 批量向题库添加题目
     *
     * @param questionIds
     * @param bankId
     * @param loginUser
     */
    void batchAddQuestions2Bank(List<Long> questionIds, Long bankId, User loginUser);

    /**
     * 批量从题库移除题目
     *
     * @param questionIds
     * @param bankId
     */
    void batchRemoveQuestionsFromBank(List<Long> questionIds, Long bankId);
}
