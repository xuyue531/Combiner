package com.purplefish.combiner;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 批量操作执行器
 * 具有一下功能：
 *  1. 批量操作的最终调用者
 *  2. 持续聚合数据
 *  3. 提供本次批量操作执行的信息查询接口
 * Created by xuyue on 2016/12/24.
 */
public interface CombineExecutor<T, K, V> extends Callable<Boolean> {

    public boolean submitData(SubmitData<K, V> data);

    public Collection<SubmitData<K, V>> allData();

    public T combineId();

    public long willWait();

    public void go();

    public void release();

    public boolean isReleased();

    public int amount();

    public int maxCapacity();

    public boolean remove(long submitId);

    public boolean isExist(long submitId);

    public boolean isDone();

    public boolean isSuccess();
}
