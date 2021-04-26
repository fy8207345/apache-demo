package org.example.curator.sharedsemaphore;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.VersionedValue;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SharedSemaphore {

    static String path = "/semaphore";

    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client1 = ClientUtils.getClient();
        SharedCount sharedCount = new SharedCount(client1, "/shared-count", 1);
        VersionedValue<Integer> versionedValue = sharedCount.getVersionedValue();
        log.info("version: {} - {}", versionedValue.getVersion(), versionedValue.getValue());
        InterProcessSemaphoreV2 semaphore1 = new InterProcessSemaphoreV2(client1, path, sharedCount);

        CuratorFramework client2 = ClientUtils.getClient();
        InterProcessSemaphoreV2 semaphore2 = new InterProcessSemaphoreV2(client2, path, sharedCount);

        execute(semaphore1);
        execute(semaphore2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client1.close();
        client2.close();
    }

    static void execute(final InterProcessSemaphoreV2 semaphoreV2){
        executorService.execute(() -> {
            Lease lease = null;
            try {
                lease = semaphoreV2.acquire(1, TimeUnit.SECONDS);
                log.info("lock acquire : {} - {}", semaphoreV2, lease);
                if(lease != null){
                    log.info("start execution : {} - {}", semaphoreV2, lease);
                    TimeUnit.SECONDS.sleep(2);
                    log.info("end execution : {} - {}", semaphoreV2, lease);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(lease != null){
                    log.info("release : {}", semaphoreV2);
                    try {
                        semaphoreV2.returnLease(lease);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
