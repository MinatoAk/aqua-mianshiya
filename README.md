# aqua-mianshiya

## 优化点

- [ ] **1. ES 接口支持降级**

  需求: 如果 ES 挂了，那照样能够访问接口；

  实现思路: ES 如果查询报错 / ES 客户端未正确初始化，改为调用数据库；

- [x] ~~**2. 防止重复执行定时任务**~~

  实现思路: 通过自定义注解 + AOP 实现分布式锁；





## 心得

- 所有写入 MySQL, Redis, ES 的操作能够批量执行的全都批量执行，避免多次建立连接；
- 使用 `BitMap` 等数据类型需要先本地缓存起来查询到的 `BitSet`，避免多次请求 Redis，并且 `BitSet` 这种位图不需要遍历每一位，可以直接遍历下一个为 `1` 的位，提升性能；
- 批量操作数据库的方法需要打上 `@Transactional` 注解，用 Spring 事务进行管理，有任何一个数据库操作失败都会回滚；