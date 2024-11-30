package com.xuanxuan.mianshiya.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuanxuan.mianshiya.common.ErrorCode;
import com.xuanxuan.mianshiya.constant.CommonConstant;
import com.xuanxuan.mianshiya.exception.BusinessException;
import com.xuanxuan.mianshiya.exception.ThrowUtils;
import com.xuanxuan.mianshiya.mapper.QuestionBankQuestionMapper;
import com.xuanxuan.mianshiya.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.xuanxuan.mianshiya.model.entity.Question;
import com.xuanxuan.mianshiya.model.entity.QuestionBank;
import com.xuanxuan.mianshiya.model.entity.QuestionBankQuestion;
import com.xuanxuan.mianshiya.model.entity.User;
import com.xuanxuan.mianshiya.model.vo.QuestionBankQuestionVO;
import com.xuanxuan.mianshiya.model.vo.UserVO;
import com.xuanxuan.mianshiya.service.QuestionBankQuestionService;
import com.xuanxuan.mianshiya.service.QuestionBankService;
import com.xuanxuan.mianshiya.service.QuestionService;
import com.xuanxuan.mianshiya.service.UserService;
import com.xuanxuan.mianshiya.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 *
 * @author <a href="https://github.com/lixuanxuan">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
//        String title = questionBankQuestion.getTitle();
//        // 创建数据时，参数不能为空
//        if (add) {
//            // todo 补充校验规则
//            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
//        }
//        // 修改数据时，有参数则校验
//        // todo 补充校验规则
//        if (StringUtils.isNotBlank(title)) {
//            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
//        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // 1) 从对象中取值
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        int current = questionBankQuestionQueryRequest.getCurrent();
        int pageSize = questionBankQuestionQueryRequest.getPageSize();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();

        // 2) 补充需要的查询条件
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    /**
     * 批量向题库添加题目
     *
     * @param questionIds
     * @param bankId
     * @param loginUser
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestions2Bank(List<Long> questionIds, Long bankId, User loginUser) {
        // 1) 参数校验
        // 1.1) 校验参数非空
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds), ErrorCode.PARAMS_ERROR, "题目 id 不能为空");
        ThrowUtils.throwIf(bankId == null || bankId <= 0, ErrorCode.PARAMS_ERROR, "题库 id 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 1.2) 校验题目 id 合法
        List<Question> questionList = questionService.listByIds(questionIds);
        List<Long> validQuestionsList = questionList.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionsList), ErrorCode.PARAMS_ERROR, "题目不存在");

        // 1.3) 校验题库 id 合法
        QuestionBank questionBank = questionBankService.getById(bankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");


        // 2) 批量添加题目到题库
        for (Long questionId : validQuestionsList) {
            QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
            questionBankQuestion.setQuestionBankId(bankId);
            questionBankQuestion.setQuestionId(questionId);
            questionBankQuestion.setUserId(loginUser.getId());
            boolean success = this.save(questionBankQuestion);

            if (!success) throw new BusinessException(ErrorCode.OPERATION_ERROR, "添加题目到题库失败");
        }
    }

    /**
     * 批量向题库删除题目
     *
     * @param questionIds
     * @param bankId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIds, Long bankId) {
        // 1) 参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIds), ErrorCode.PARAMS_ERROR, "题目 id 不能为空");
        ThrowUtils.throwIf(bankId == null || bankId <= 0, ErrorCode.PARAMS_ERROR, "题库 id 不能为空");

        // 2) 批量添加题目到题库
        for (Long questionId : questionIds) {
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, bankId);
            boolean success = this.remove(lambdaQueryWrapper);
            if (!success) throw new BusinessException(ErrorCode.OPERATION_ERROR, "从题库删除题目失败");
        }
    }

}
