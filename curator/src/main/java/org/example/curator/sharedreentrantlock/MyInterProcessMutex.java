package org.example.curator.sharedreentrantlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 可重入锁
 */
@Slf4j
public class MyInterProcessMutex {

    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client1 = ClientUtils.getClient();
        InterProcessMutex mutex1 = new InterProcessMutex(client1, "/mutex");

        CuratorFramework client2 = ClientUtils.getClient();
        InterProcessMutex mutex2 = new InterProcessMutex(client2, "/mutex");
        acquire(mutex1);
        acquire(mutex2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client1.close();
        client2.close();
    }

    private static void acquire(final InterProcessMutex mutex){
        executorService.execute(() -> {
            boolean acquire = false;
            try {
                acquire = mutex.acquire(3, TimeUnit.SECONDS);
                log.info("acquired: {} - {}", mutex, acquire);
                TimeUnit.SECONDS.sleep(4);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(acquire){
                    try {
                        mutex.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
