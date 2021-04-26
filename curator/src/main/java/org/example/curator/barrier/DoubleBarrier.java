package org.example.curator.barrier;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.example.curator.util.ClientUtils;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DoubleBarrier {

    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        DistributedDoubleBarrier distributedDoubleBarrier = new DistributedDoubleBarrier(client, "/double-barrier", 2);

        CuratorFramework client2 = ClientUtils.getClient();
        DistributedDoubleBarrier distributedDoubleBarrier2 = new DistributedDoubleBarrier(client2, "/double-barrier", 2);

        execute(distributedDoubleBarrier);
        execute(distributedDoubleBarrier2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client.close();
        client2.close();
    }

    static void execute(final DistributedDoubleBarrier barrier){
        executorService.execute(() -> {
            try {
                log.info("start enter : {}", barrier);
                TimeUnit.SECONDS.sleep(value());
                boolean enter = barrier.enter(7, TimeUnit.SECONDS);
                log.info("entered : {} , {}",barrier, enter);
                TimeUnit.SECONDS.sleep(value());
                log.info("start leave : {}",barrier);
                boolean leave = barrier.leave(7, TimeUnit.SECONDS);
                log.info("end leave : {} - {}", barrier, leave);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static Random random = new Random();
    static long value(){
        return random.nextInt(5) + 2;
    }
}
