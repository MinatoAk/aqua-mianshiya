# aqua-mianshiya

## 优化点

- [ ] **1. ES 接口支持降级**

  需求: 如果 ES 挂了，那照样能够访问接口；

  实现思路: ES 如果查询报错 / ES 客户端未正确初始化，改为调用数据库；

- [ ] **2. 防止重复执行定时任务**

  实现思路: 通过自定义注解实现分布式锁；