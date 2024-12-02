package com.xuanxuan.mianshiya.esdao;

import com.xuanxuan.mianshiya.model.dto.post.PostEsDTO;
import com.xuanxuan.mianshiya.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 题目 ES 操作
 *
 * @author <a href="https://github.com/lixuanxuan">程序员鱼皮</a>
 * @from <a href="https://xuanxuan.icu">编程导航知识星球</a>
 */

/**
 * 这个 DAO 类如果 ES 未启动会报错，后续需要更改
 * 如果 ES 未启动则不注册该类
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    List<QuestionEsDTO> findByUserId(Long userId);
}