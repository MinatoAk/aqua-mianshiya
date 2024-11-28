package com.xuanxuan.mianshiya.mapper;

import com.xuanxuan.mianshiya.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author MinatoAqukinn
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2024-11-24 12:57:31
* @Entity com.xuanxuan.mianshiya.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查出来包括逻辑删除的记录
     *
     * @param minUpdateTime
     * @return
     */
    @Select("SELECT * FROM question WHERE updatetime >= #{minUpdateTime}")
    List<Question> listUpdatedQuestion(Date minUpdateTime);
}




