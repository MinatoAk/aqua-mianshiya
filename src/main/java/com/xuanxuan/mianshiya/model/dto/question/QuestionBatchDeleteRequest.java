package com.xuanxuan.mianshiya.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建题库题目关联请求
 *
 * @author <a href="https://github.com/lixuanxuan">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionBatchDeleteRequest implements Serializable {
    /**
     * 题目 id
     */
    private List<Long> questionIds;

    private static final long serialVersionUID = 1L;
}