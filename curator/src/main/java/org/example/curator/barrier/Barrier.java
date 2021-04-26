package org.example.curator.barrier;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Barrier {

    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
        CuratorFramework client = ClientUtils.getClient();
        DistributedBarrier barrier = new DistributedBarrier(client, "/barrier");

        CuratorFramework client2 = ClientUtils.getClient();
        DistributedBarrier barrier2 = new DistributedBarrier(client2, "/barrier");

        barrier.setBarrier();
        barrier2.setBarrier();

        execute(barrier);
        execute(barrier2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client.close();
        client2.close();
    }

    static void execute(final DistributedBarrier barrier){
        executorService.execute(() -> {
            try {
                log.info("waiting barrier : {}", barrier);
                barrier.waitOnBarrier(2, TimeUnit.SECONDS);
                log.info("barrier arrived : {}", barrier);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    log.info("remove barrier");
                    barrier.removeBarrier();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
