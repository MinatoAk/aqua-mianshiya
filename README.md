# aqua-mianshiya

## 1 拓展优化点

- [ ] **1. ES 接口支持降级**

  需求: 如果 ES 挂了，那照样能够访问接口；

  实现思路: ES 如果查询报错 / ES 客户端未正确初始化，改为调用数据库；

- [x] ~~**2. 防止重复执行定时任务 **~~

  实现思路: 通过自定义注解 + AOP 实现分布式锁；

<br>

<br>

## 2 Coding 小心得

- 使用 `BitMap` 等数据类型需要先本地缓存起来查询到的 `BitSet`，避免多次请求 Redis，并且 `BitSet` 这种位图不需要遍历每一位，可以直接遍历下一个为 `1` 的位，提升性能；
- 所有写入 MySQL, Redis, ES 的操作能够批量执行的全都批量执行，避免多次建立连接；
- 批量操作数据库的方法需要打上 `@Transactional` 注解，用 Spring 事务进行管理，有任何一个数据库操作失败都会回滚，另外注意调用事务修饰的方法需要保证**使用当前实现类的代理对象调用**，而不是 `this` 调用，保证 Spring 事务生效；

<br>

### 2.1 重要: 批处理操作通用优化注意点

> 参考 `QuestionBankQuestionServiceImpl` 中的 `batchAddQuestions2Bank` 方法，是一个很好的实践示例；

- 健壮性: 处理异常情况及非法输入

  - **参数校验**: 非空校验，合法校验，包括逻辑上是否合法，比如要添加的题目中是否包括已添加过的题目；
  - **异常处理**: 更加细粒度的异常处理，比如数据库唯一键处理，数据库事务问题导致操作失败的处理等；

- 稳定性

  - **避免长事务**: 数据分批处理，每 1000 条一个事务处理，避免最后有操作失败，导致事务回滚，造成较大性能损耗；

    > 要注意使用事务修饰的方法，调用的时候不能用 `this`，因为是未被代理的对象，应该用代理对象调用；
    >
    > 要在启动类添加下面的注释，开启自动代理:
    >
    > `@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)`

  - **重试**: 消除偶发问题，可以学习使用 Guava Retrying 库实现，自行实现要注意有重试最大次数；

  - **增量恢复**: 记录某一批任务执行的情况，当出现失败后，从上一次执行失败的地方重新开始执行，而不是整批执行；

- 性能优化

  - **批处理**: 每一条数据都调用一次 `save` 性能损耗太严重，改用 MyBatis-Plus 提供的 `saveBatch`，避免频繁的和数据库交互，减少 I/O 操作，降低和数据库建立连接和提交的频率，像 Redis 提供的 `pipeline` 也是同理；

  - **SQL 优化 [! 究极常用 !]**: 禁用 `SELECT *` 只查询需要查询的字段，并且从内存角度考虑，只保存查出来的字段，而不是存整个对象；

    ```java
    // SQL 优化前
    List<Question> questionList = questionService.listByIds(questionIds);
    List<Long> validQuestionsList = questionList.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());
    
    // SQL 优化后
    LambdaQueryWrapper<Question> questionLambdaQueryWrapper = Wrappers.lambdaQuery(Question.class)
        .select(Question::getId)
        .in(Question::getId, questionIds);
    List<Long> validQuestionsList = questionService.listObjs(questionLambdaQueryWrapper, obj -> (Long)obj);
    ```

  - **并发编程 [! 究极常用 !]**: 通过 `CompletableFuture` 配合自定义线程池提交任务，并行执行；

    ```java
    // 保存所有批次的 CompletableFuture
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    
    for (int i = 0; i < totalSize; i += batchSize ) {
        // 一些业务代码 //
        
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 执行任务具体逻辑
            questionBankQuestionService.batchAddQuestions2BankInner(batchQuestionBankQuestion);
        }, customExecutor).exceptionally(ex -> {
            log.error("向题库添加题目失败，错误信息: {}", ex.getMessage());
            return null;
        });
        futures.add(future);
    }
    
    // 等待所有任务都执行完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // 关闭线程池
    customExecutor.shutdown();
    ```

  - **异步化**: 提交到 MQ，后台异步处理任务，记得更新任务状态；
  
  - **数据库连接池优化**: 可以**复用**现有的数据库连接，而不是每次请求时都重新创建和销毁；
  
    可以使用 Druid 管理数据库连接池，提供丰富的监控和管理，包括 SQL 分析，性能监控，慢日志查询等，连接池大小需要根据实际监控和测试分析；
  
- 数据库一致性

  - **事务管理**: `@Transaction` 但要注意保证事务生效；
  - **并发管理**: 乐观锁或悲观锁处理；

- 可观测性

  - **日志记录**
  - **监控**
  - **返回值优化**: 不只是返回 `void` 而是封装一个返回类，包括错误原因，执行总数据数，成功数据数，失败数据数等；

