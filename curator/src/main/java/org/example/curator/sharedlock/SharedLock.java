package org.example.curator.sharedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 不可重入锁
 */
@Slf4j
public class SharedLock {
    static String path = "/shared-lock";
    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client1 = ClientUtils.getClient();
        InterProcessSemaphoreMutex mutex1 = new InterProcessSemaphoreMutex(client1, path);

        CuratorFramework client2 = ClientUtils.getClient();
        InterProcessSemaphoreMutex mutex2 = new InterProcessSemaphoreMutex(client2, path);

        execute(mutex1);
        execute(mutex2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client1.close();
        client2.close();
    }

    static void execute(final InterProcessSemaphoreMutex mutex){
        executorService.execute(() -> {
            try {
                boolean acquire = mutex.acquire(3, TimeUnit.SECONDS);
                log.info("acquired : {} - {}", mutex, acquire);
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(mutex.isAcquiredInThisProcess()){
                    try {
                        log.info("release : {}", mutex);
                        mutex.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
