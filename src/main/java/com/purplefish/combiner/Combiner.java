package com.purplefish.combiner;

import com.purplefish.combiner.api.CombineRunner;
import com.purplefish.combiner.common.RollingNumber;
import com.purplefish.combiner.common.RollingNumberEvent;
import com.purplefish.combiner.utils.ConsistUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 批量执行类，通过在一定时间内聚合数据，将多个请求合并成一个批量请求
 * 基本特性：
 *  1. 数据聚合
 *  2. 批量操作执行
 *  3. 实时QPS探测，动态调整聚合等待时间
 *  4. 参数安全检查
 *  5. 轻量，无任何第三方依赖
 * @param <T> 数据聚合维度
 * @param <K>
 * @param <V>
 *
 * TODO
 *      1. 增加配置文件读取功能，线程池大小等参数需要用户可配置
 *      2. 基于自身数据量的批量执行策略
 * Created by xuyue on 2016/12/24.
 */
public class Combiner<T, K, V> {

    //批量操作执行器，不同的数据聚合ID拥有独立的执行器
    private Map<T, CombineExecutor> combineExecutors;
    //所有批量操作的定时执行线程池
    private ScheduledExecutorService scheduledService;
    //Combiner自身状态的定时扫描
    private ScheduledExecutorService CombinerService;
    //最终的批量操作类，需要用户提供实现，被CombineExecutor调用
    private CombineRunner<T, K, V> runner;

    private float rate = 10;
    private int expTimePerExe = 100;
    private int effective = 10;
    private int maxCapacity = Integer.MAX_VALUE;
    private int cyclicalTaskPeriod = 10;
    //批量执行等待时间，如何清除过期的项，定期重建？
    private Map<T, RollingNumber> delayTime = new ConcurrentHashMap<T, RollingNumber>();
    //新增executor的时候，防止重复创建，需要加锁
    private ReentrantLock execLock = new ReentrantLock();


    /**
     * 不允许自己创建Combiner
     * 强制使用Builder模式创建，以检查参数设置是否安全
     */
    private Combiner() {
        combineExecutors = new ConcurrentHashMap<T, CombineExecutor>();
        scheduledService = Executors.newScheduledThreadPool(5);
        CombinerService = Executors.newScheduledThreadPool(1);
        CombinerService.scheduleAtFixedRate(new CyclicalTask(), cyclicalTaskPeriod, cyclicalTaskPeriod, TimeUnit.MINUTES);
    }

    /**
     * 创建builder
     * @return
     */
    public static CombinerBuilder combinerBuilder(){
        return new CombinerBuilder();
    }

    /**
     * 批量操作的最终执行者
     * @param runner
     * @return
     */
    public Combiner runner(CombineRunner runner){
        this.runner = runner;
        return this;
    }

    public CombineRunner<T, K, V> getRunner() {
        return runner;
    }

    /**
     * 批量执行的最大速率
     * @param rate 1秒内能执行的批量操作数，可以为小数
     * @return
     */
    public Combiner maxRate(float rate){
        this.rate = rate;
        return this;
    }

    /**
     * 单个批量数据集的最大数据量，超过这个阀值立刻执行批量操作
     * 单位为数据条数
     * @param capacity
     * @return
     */
    public Combiner maxCapacity(int capacity){
        this.maxCapacity = capacity;
        return this;
    }

    public int getMaxCapacity(){
        return maxCapacity;
    }

    /**
     * 释放批量ID对应的执行器
     * 此处无并发，所以做一下简单的校验就可以
     * @param combineId
     * @return
     */
    public boolean release(T combineId){
        if (!combineExecutors.containsKey(combineId)){
            return true;
        }
        CombineExecutor executor = combineExecutors.get(combineId);
        executor.release();
        if (executor.isReleased()){
            combineExecutors.remove(combineId);
            return true;
        }else {
            return false;
        }
    }

    public ScheduledExecutorService getScheduledService() {
        return scheduledService;
    }

    /**
     * 获取数据聚合等待时间
     * 每个聚合ID使用单独的动态等待时间
     * @param combine
     * @return
     */
    public long getDelay(T combine){
        if (!delayTime.containsKey(combine)){
            return 100;
        }else {
            return dynamicDelayTime(delayTime.get(combine).getRollingSum(RollingNumberEvent.SUBMIT));
        }
    }

    /**
     * 根据实时的Qps计算需要delay的时间
     * @param qps
     * @return
     */
    private long dynamicDelayTime(long qps) {
        if (qps < rate){
            return 0;
        }
        int millsPerRequest = (int) (1000/qps);
        int v = (int) ((rate * effective) / qps);
        if (v == 0){
            return expTimePerExe - millsPerRequest;
        }
        return (expTimePerExe - millsPerRequest) >> (v - 1);
    }

    public CombineFuture submit(T combine, K key, V val){
        long submitId = ConsistUtils.generateRTId();
        return submit(submitId, combine, key, val);
    }

    public CombineFuture submit(Long submitId, T combine, K key, V val){
        SubmitData<K, V> data = new SubmitData<K, V>(submitId, key, val);
        return submit(combine, data);
    }

    public CombineFuture submit(Long submitId, T combine, Map<K, V> values){
        SubmitData<K, V> data = new SubmitData<K, V>(submitId, values);
        return submit(combine, data);
    }

    /**
     * 提交数据
     * @param combine
     * @param data
     * @return
     */
    public CombineFuture submit(T combine, SubmitData<K, V> data){
        /**
         * 统计该combine下提交数据的qps
         */
        RollingNumber counter = delayTime.get(combine);
        if (counter == null){
            counter = new RollingNumber(1000, 10);
            delayTime.put(combine, counter);
        }
        counter.increment(RollingNumberEvent.SUBMIT);

        /**
         * do submit
         */
        boolean isSubmitSuccess = false;
        CombineExecutor<T, K, V> executor = null;
        while (!isSubmitSuccess){
            executor = combineExecutors.get(combine);
            if (executor == null){
                //如果不存在批量执行者，则创建
                execLock.lock();
                try {
                    executor = combineExecutors.get(combine);
                    if (executor == null){
                        executor = new DefaultCombineExecutor<T, K, V>(combine, this);
                        combineExecutors.put(combine, executor);
                        executor.go();
                    }
                }finally {
                    execLock.unlock();
                }
            }
            isSubmitSuccess = executor.submitData(data);
        }

        /**
         * create future
         */
        CombineFuture future;
        if (isSubmitSuccess && executor != null){
            future = new SubmitFuture<K, V>(executor, data);
        }else {
            //优化
            future = null;
        }
        return future;
    }


    /**
     * 定时对Combiner本身的一些状态做检查和清理
     */
    class CyclicalTask implements Runnable{

        @Override
        public void run() {
            if (delayTime.size() > 10000){
                delayTime = new ConcurrentHashMap<T, RollingNumber>();
            }
        }
    }


    static class CombinerBuilder<T, K, V> {

        private Combiner<T, K, V> combiner = new Combiner<T, K, V>();

        public CombinerBuilder() {

        }

        public CombinerBuilder maxRate(float rate){
            this.combiner.maxRate(rate);
            this.combiner.expTimePerExe = (int) (1000/rate);
            return this;
        }

        public CombinerBuilder maxCapacity(int capacity){
            this.combiner.maxCapacity(capacity);
            return this;
        }

        public CombinerBuilder runner(CombineRunner runner){
            this.combiner.runner(runner);
            return this;
        }

        public Combiner<T, K, V> build() throws Exception {
            if (this.combiner.runner == null){
                throw new Exception();
            }
            //参数限制，避免内存泄漏，策略再议
            if (this.combiner.rate > 1000
                    && this.combiner.maxCapacity == Integer.MAX_VALUE){
                throw new Exception();
            }
            return combiner;
        }

    }


}
