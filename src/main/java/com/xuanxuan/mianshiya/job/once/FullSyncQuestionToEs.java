package com.xuanxuan.mianshiya.job.once;

import cn.hutool.core.collection.CollUtil;
import com.xuanxuan.mianshiya.annotation.DistributedLock;
import com.xuanxuan.mianshiya.common.ErrorCode;
import com.xuanxuan.mianshiya.constant.RedisConstant;
import com.xuanxuan.mianshiya.esdao.QuestionEsDao;
import com.xuanxuan.mianshiya.exception.BusinessException;
import com.xuanxuan.mianshiya.manager.ESManager;
import com.xuanxuan.mianshiya.model.dto.question.QuestionEsDTO;
import com.xuanxuan.mianshiya.model.entity.Question;
import com.xuanxuan.mianshiya.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

// todo 取消注释开启任务
// @Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    @Resource
    private ESManager esManager;

    @Override
    @DistributedLock(key = RedisConstant.FULL_SYNC_QUESTION_TO_ES)
    public void run(String... args) {
        // 0) 如果 ES 为连接则不同步
        if (!esManager.checkElasticsearch()) {
            log.error("ElasticSearch 尚未连接!");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ES 尚未连接，无法增量同步");
        }

        // 1) 全量获取题目（数据量不大的情况下使用）
        List<Question> questionList = questionService.list();
        if (CollUtil.isEmpty(questionList)) {
            return;
        }

        // 2) 转为 ES 实体类
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());

        // 3) 分页批量插入到 ES
        final int pageSize = 500;
        int total = questionEsDTOList.size();
        log.info("FullSyncQuestionToEs start, total {}", total);

        for (int i = 0; i < total; i += pageSize) {
            // 注意同步的数据下标不能超过总数据量
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        log.info("FullSyncQuestionToEs end, total {}", total);
    }
}
