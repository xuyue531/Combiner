package com.purplefish.combiner;

import com.purplefish.combiner.api.CombineRunner;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Created by xuyue on 2017/1/8.
 */
public class CombinerTest {

    private ExecutorService pool = Executors.newFixedThreadPool(10);
    private Combiner<String, String, Integer> combiner;
    List<String> combineIds = new ArrayList<String>();

    @Before
    public void init() throws Exception {
        combiner = Combiner.combinerBuilder().runner(new CombineRunner<String, String, Integer>(){
            @Override
            public boolean run(String id, Collection<SubmitData<String, Integer>> submitDatas) {
                System.out.println("id is " + id + "; data's size is " + submitDatas.size());
                return true;
            }
        }).maxRate(1).build();
        combineIds.add("aaa");
        combineIds.add("bbb");
        combineIds.add("ccc");
        combineIds.add("ddd");
    }

    @Test
    public void test() throws InterruptedException {
        for (int i = 0; i < 20; ++i){
            final String combineId = combineIds.get(i%combineIds.size());
            final String id = Integer.toString(i);
            final Integer value = i;
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    CombineFuture future = combiner.submit(combineId, id, value);
                    try {
                        System.out.println("==============>" + future.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
//            Thread.currentThread().sleep(10);
        }
        try {
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}