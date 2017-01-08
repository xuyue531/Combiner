package com.purplefish.combiner;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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
    //该executor是否已经被Combiner释放，若已经释放，则不接受新数据的submit等更新操作（如remove）
    private volatile boolean isRelease = false;
    //该executor所接受的最大数据量
    private int capacity;
    //该executor已经执行完毕
    private volatile boolean isDone = false;
    //该executor执行结果
    private volatile boolean isSuccess = false;
    //用于实现与各个future之间的通知机制
    private ReentrantLock callLock = new ReentrantLock();


    /**
     * @param combineId
     * @param combiner
     */
    public DefaultCombineExecutor(T combineId, Combiner<T, K, V> combiner) {
        this.combineId = combineId;
        this.combiner = combiner;
        this.datas = new ConcurrentHashMap<Long, SubmitData<K, V>>();
        this.delayTime = combiner.getDelay(combineId);
        this.capacity = combiner.getMaxCapacity();
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
    public int amount() {
        int totalAmount = 0;
        for (SubmitData data : datas.values()){
            totalAmount += totalAmount + data.dataAmount();
        }
        return totalAmount;
    }

    @Override
    public int maxCapacity() {
        return capacity;
    }

    /**
     * 移除submit对应的数据项
     * 几个问题：
     *  1. 如果id本身就在datas中不存在，返回true还是false
     *  2. 如果移除的数据的id和传入的id不一致，返回true还是false
     *  3. 不同的失败原因是否需要在返回值中体现（将返回值以枚举或者int返回）
     * @param submitId
     * @return
     */
    @Override
    public boolean remove(long submitId) {
        if (isRelease){
            return false;
        }
        try {
            SubmitData data = datas.remove(submitId);
            if (data.getSubmitId() != submitId){
                return false;
            }
        }catch (NullPointerException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean isExist(long submitId) {
        return datas.containsKey(submitId)
                && (datas.get(submitId).getSubmitId() == submitId);
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean isSuccess() {
        if (isDone){
            return isSuccess;
        }else {
            return false;
        }
    }


    @Override
    public Boolean call() throws Exception {
        boolean isSuccess = false;
        while (combiner.release(combineId)){
            isSuccess = combiner.getRunner().run(combineId, datas.values());
            break;
        }
        isDone = true;
        this.isSuccess = isSuccess;
        return isSuccess;
    }
}
