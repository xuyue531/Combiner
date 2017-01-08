package com.purplefish.combiner.utils;

/**
 * 一致性相关工具类
 * Created by xuyue on 2016/12/28.
 */
public class ConsistUtils {

    /**
     * 生成实时ID
     * 保证一定程度的唯一性，目前使用本地的微秒作为ID
     * @return
     */
    public static long generateRTId(){
        return System.nanoTime();
    }

}
