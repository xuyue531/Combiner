package com.purplefish.combiner.api;

import com.purplefish.combiner.SubmitData;

import java.util.Collection;

/**
 * 最终的合并执行类
 * 需要用户提供实现，用户要确保该实现要线程安全
 * Created by xuyue on 2016/12/28.
 */
public interface CombineRunner<T, K, V> {

    public boolean run(T id, Collection<SubmitData<K, V>> submitDatas);

}
