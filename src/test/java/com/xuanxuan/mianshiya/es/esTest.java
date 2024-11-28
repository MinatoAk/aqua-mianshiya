package com.xuanxuan.mianshiya.es;

import com.xuanxuan.mianshiya.esdao.QuestionEsDao;
import com.xuanxuan.mianshiya.model.dto.question.QuestionEsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class esTest {

    @Resource
    private QuestionEsDao questionEsDao;

    @Test
    public void test() {
        QuestionEsDTO questionEsDTO = new QuestionEsDTO();
        List<QuestionEsDTO> questionEsDTOList = new ArrayList<>();

        questionEsDao.save(questionEsDTO);
        questionEsDao.saveAll(questionEsDTOList);
    }
}
