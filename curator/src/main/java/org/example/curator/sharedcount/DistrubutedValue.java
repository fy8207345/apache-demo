package org.example.curator.sharedcount;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.checkerframework.checker.units.qual.A;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DistrubutedValue {

    static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(100, 5);
        DistributedAtomicLong distributedAtomicLong = new DistributedAtomicLong(client, "/distributedAtomicLong", retryPolicy);
        try {
            distributedAtomicLong.initialize(0L);
            for(int i=0;i<5;i++){
                executorService.execute(() -> {
                    try {
                        AtomicValue<Long> increment = distributedAtomicLong.increment();
                        if(increment.succeeded()){
                            log.info("add success : {} - {}", increment.preValue(), increment.postValue());
                        }else{
                            log.info("add failed : {}", distributedAtomicLong.get().preValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }

        try {
            AtomicValue<Long> longAtomicValue = distributedAtomicLong.get();
            log.info("value : {}", longAtomicValue.postValue());
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.close();
    }
}
