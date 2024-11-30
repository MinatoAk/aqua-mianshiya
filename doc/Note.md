# Learning Note

## 1 后端基础能力

### 1.1 根据题库查询该题库对应的题目

1. 本质就是查询题目，但是根据 `bankId` 查询题目，所以可以直接在 `questionQueryRequest` 添加 `questionBankId` 字段；

2. 封装通用的查询题目方法到 `QuestionService`:

   ```java
   public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
       long current = questionQueryRequest.getCurrent();
       long size = questionQueryRequest.getPageSize();
   
       // 1) 题目表的查询条件
       QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
   
       // 2) 根据题库 id 查询该题库下的所有题目，先根据关联表查所有题目 ids，再搭到 queryWrapper
       Long questionBankId = questionQueryRequest.getQuestionBankId();
       if (questionBankId != null) {
           List<QuestionBankQuestion> questions = questionBankQuestionService.list(
               Wrappers.lambdaQuery(QuestionBankQuestion.class)
               .select(QuestionBankQuestion::getQuestionId)
               .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
           );
   
           if (CollUtil.isNotEmpty(questions)) {
               Set<Long> questionIds = questions.stream()
                   .map(QuestionBankQuestion::getQuestionId)
                   .collect(Collectors.toSet());
               queryWrapper.in("id", questionIds);
           }
       }
   
       // 3) 查询数据库
       Page<Question> questionPage = this.page(new Page<>(current, size), queryWrapper);
   
       return questionPage;
   }
   ```

3. 技巧:

   1) 只查询需要的字段，也就是 `select questionId` 即可；

   2) 参数校验是否为空，为空就不用进一步处理；

   3) 直接复用原来的接口，向 `queryWrapper` 添加查询条件；



### 1.2 查询题库详情，根据需要决定是否查题库中题目

1. 决定要不要查题目就直接传一个 `boolean` 就可以，所以要另外封装一个 `DTO` 传递参数，一般来说都是一个接口对应于一个 `DTO` 传参，养成这种习惯；

2. 另外要返回给前端的 `VO` 需要添加分页的题目列表字段 <font color = "">**[如何动态修改返回给前端的封装信息]**</font>；

3. 实现对应接口，这边查询题目直接复用刚刚实现的 `questionService` 中的方法即可:

   ```java
   @GetMapping("/get/vo")
   public BaseResponse<QuestionBankVO> getQuestionBankVOById(QuestionBankQueryRequest questionBankQueryRequest, HttpServletRequest request) {
       ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
       Long id = questionBankQueryRequest.getId();
       boolean needQueryQuestionList = questionBankQueryRequest.isNeedQueryQuestionList();
       ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
   
       // 查询数据库
       QuestionBank questionBank = questionBankService.getById(id);
       ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR);
   
       QuestionBankVO questionBankVO = questionBankService.getQuestionBankVO(questionBank, request);
   
       // 如果需要查询题库列表
       if (needQueryQuestionList) {
           QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
           questionQueryRequest.setQuestionBankId(id);
           Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
           questionBankVO.setQuestionPage(questionPage);
       }
   
       // 获取封装类
       return ResultUtils.success(questionBankVO);
   }
   ```








## 2 业务需求开发

### 2.1 用户签到统计

**技术**: Redis `BitMap`

**需求**: 

​	1) 添加签到记录；

​	2) 查询签到记录；



### 2.2 分词题目搜索

**技术**: ES

**需求**: 

​	1) 全量及增量同步数据库数据到 ES；

​	2) 根据题目，内容，答案可以灵活检索到相关题目；



### 2.3 批量向题库添加/移除题目及批量删除题目

**迭代**:

1) 第一版简单地批量查询与删除，要记得使用 `@Transactional`，这是 Spring 帮助我们管理数据库事务，方法中任何一个数据库操作失败，Spring 都会通过代理回滚操作；

```java
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
```











## 3 业务自行优化

### 3.1 定时任务唯一执行

**技术**: 自定义注解 + AOP

**需求**: 

​	1) ES 数据同步分布式环境下只需要一台机器执行，使用分布式锁保证唯一性；

​	2) 通过自定义注解 + AOP 快捷为方法添加分布式锁；