package com.purplefish.combiner;

import java.util.concurrent.Future;

/**
 * 聚合任务的Future
 * 提供以下信息的查询：
 *  1. 当前executor聚合数据量；
 *  2. 聚合任务预计执行时间；
 *  3. 当前executor的执行速率；
 *  4. 当前executor最大接受的数据量
 *  5. 当前executor是否执行完毕
 * 提供以下操作：
 *  1. 取消本次数据的提交，有一定概率失败；
 *  2. 注册listener，定义一些对应的回调功能；
 *  3. 获取当前任务执行结果
 * Created by xuyue on 2016/12/24.
 */
public interface CombineFuture extends Future {

    /**
     * 当前聚合数据量
     * @return
     */
    public int amount();

    /**
     * 还需要等待执行的时长
     * @return
     */
    public long willWait();

    /**
     * 当前合并执行的速率
     * @return
     */
    public int combineRate();

    /**
     * 当前允许的最大数据量
     * @return
     */
    public int maxCapacity();

    //listener

}
