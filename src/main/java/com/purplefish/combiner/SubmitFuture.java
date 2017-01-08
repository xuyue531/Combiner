package com.purplefish.combiner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xuyue on 2016/12/28.
 */
public class SubmitFuture implements CombineFuture {

    private CombineExecutor executor;
    private long submitId;

    public SubmitFuture(CombineExecutor executor, long submitId) {
        this.executor = executor;
        this.submitId = submitId;
    }

    @Override
    public int amount() {
        return executor.amount();
    }

    @Override
    public long willWait() {
        return executor.willWait();
    }

    @Override
    public int maxCapacity() {
        return executor.maxCapacity();
    }

    /**
     * 删除submintId对应的数据
     * @param mayInterruptIfRunning
     * @return
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return executor.remove(submitId);
    }

    @Override
    public boolean isCancelled() {
        return executor.isExist(submitId);
    }

    @Override
    public boolean isDone() {
        return executor.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
