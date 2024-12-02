package com.xuanxuan.mianshiya.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.xuanxuan.mianshiya.annotation.DistributedLock;
import com.xuanxuan.mianshiya.common.ErrorCode;
import com.xuanxuan.mianshiya.constant.RedisConstant;
import com.xuanxuan.mianshiya.esdao.QuestionEsDao;
import com.xuanxuan.mianshiya.exception.BusinessException;
import com.xuanxuan.mianshiya.manager.ESManager;
import com.xuanxuan.mianshiya.mapper.QuestionMapper;
import com.xuanxuan.mianshiya.model.dto.question.QuestionEsDTO;
import com.xuanxuan.mianshiya.model.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步题目到 es
 *
 * @author <a href="https://github.com/lixuanxuan">程序员鱼皮</a>
 * @from <a href="https://xuanxuan.icu">编程导航知识星球</a>
 */
// todo 取消注释开启任务
@Component
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;

    @Resource
    private ESManager esManager;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    @DistributedLock(key = RedisConstant.INC_SYNC_QUESTION_TO_ES)
    public void run() {
        // 0) 如果 ES 为连接则不同步
        if (!esManager.checkElasticsearch()) {
            log.error("ElasticSearch 尚未连接!");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ES 尚未连接，无法增量同步");
        }

        // 1) 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Question> questionList = questionMapper.listUpdatedQuestion(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(questionList)) {
            log.info("no inc question");
            return;
        }

        // 2) 实体类转换为 ESDTO
        List<QuestionEsDTO> questionEsDTOList = questionList.stream()
                .map(QuestionEsDTO::objToDto)
                .collect(Collectors.toList());

        // 3) 分页批量保存到 ES
        final int pageSize = 500;
        int total = questionEsDTOList.size();
        log.info("IncSyncQuestionToEs start, total {}", total);

        for (int i = 0; i < total; i += pageSize) {
            // 注意同步的数据下标不能超过总数据量
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOList.subList(i, end));
        }
        log.info("IncSyncQuestionToEs end, total {}", total);
    }
}
