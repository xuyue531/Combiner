package com.purplefish.combiner;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuyue on 2016/12/24.
 */
public class DefaultCombineExecutor<T, K, V> implements CombineExecutor<T, K, V> {

    //聚合Id
    private T combineId;
    //聚合数据，key为提交ID，一般为提交时间点
    private Map<Long, SubmitData<K, V>> datas;
    //批量操作实际执行者
    private Combiner<T, K, V> combiner;
    //创建时间，即聚合等待的起点
    private long goTime;
    //定时执行的future
    private ScheduledFuture<Boolean> scheduledFuture;
    //聚合等待时间
    private long delayTime;
    //该executor是否已经被Combiner释放，若已经释放，则不接受新数据的submit
    private volatile boolean isRelease = false;


    /**
     * @param combineId
     * @param combiner
     */
    public DefaultCombineExecutor(T combineId, Combiner<T, K, V> combiner) {
        this.combineId = combineId;
        this.combiner = combiner;
        this.datas = new ConcurrentHashMap<Long, SubmitData<K, V>>();
        this.delayTime = combiner.getDelay(combineId);
    }

    @Override
    public boolean submitData(SubmitData<K, V> data) {
        if (isRelease){
            return false;
        }
        datas.put(data.getSubmitId(), data);
        return true;
    }

    @Override
    public Collection<SubmitData<K, V>> allData() {
        return datas.values();
    }

    @Override
    public T combineId() {
        return this.combineId;
    }

    @Override
    public long willWait() {
        long currentTime = System.currentTimeMillis();
        return delayTime - (currentTime - goTime);
    }

    @Override
    public void go() {
        this.scheduledFuture =
                combiner.getScheduledService().schedule(this, delayTime, TimeUnit.MILLISECONDS);
        this.goTime = System.currentTimeMillis();
    }

    @Override
    public void release() {
        this.isRelease = true;
    }

    @Override
    public boolean isReleased() {
        return isRelease;
    }


    @Override
    public Boolean call() throws Exception {
        boolean isSuccess = false;
        while (combiner.release(combineId)){
            isSuccess = combiner.getRunner().run(combineId, datas.values());
            break;
        }
        return isSuccess;
    }
}
