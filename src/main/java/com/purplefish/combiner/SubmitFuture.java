package com.purplefish.combiner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xuyue on 2016/12/28.
 */
public class SubmitFuture implements CombineFuture {

    private CombineExecutor executor;

    public SubmitFuture(CombineExecutor executor) {
        this.executor = executor;
    }

    @Override
    public int amount() {
        return 0;
    }

    @Override
    public long willWait() {
        return 0;
    }

    @Override
    public int combineRate() {
        return 0;
    }

    @Override
    public int maxCapacity() {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
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
