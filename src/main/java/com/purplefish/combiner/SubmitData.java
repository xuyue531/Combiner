package com.purplefish.combiner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuyue on 2016/12/28.
 */
public class SubmitData<K, V> {

    private long submitId;
    private Map<K, V> valueMap;


    public SubmitData(long submitId, K key, V value) {
        this.submitId = submitId;
        valueMap = new HashMap<K, V>();
        valueMap.put(key, value);
    }

    public SubmitData(long submitId, Map<K, V> values) {
        this.submitId = submitId;
        valueMap = new HashMap<K, V>(values);
    }

    public long getSubmitId() {
        return submitId;
    }

    public void setSubmitId(long submitId) {
        this.submitId = submitId;
    }

    public Map<K, V> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<K, V> valueMap) {
        this.valueMap = valueMap;
    }

    public int dataAmount(){
        return valueMap.size();
    }
}
