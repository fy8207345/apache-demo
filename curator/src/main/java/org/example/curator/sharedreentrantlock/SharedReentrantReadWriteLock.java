package org.example.curator.sharedreentrantlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 可重入，读写锁
 */
@Slf4j
public class SharedReentrantReadWriteLock {

    static String path = "/read-write-lock";

    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, path);
        execute(readWriteLock.readLock());
        execute(readWriteLock.readLock());
        execute(readWriteLock.readLock());
        execute(readWriteLock.readLock());

        execute(readWriteLock.writeLock());
        execute(readWriteLock.writeLock());
        execute(readWriteLock.writeLock());
        execute(readWriteLock.writeLock());

        TimeUnit.SECONDS.sleep(20);
        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client.close();
    }

    static void execute(final InterProcessMutex mutex){
        executorService.execute(() -> {
            try {
                boolean acquire = mutex.acquire(2, TimeUnit.SECONDS);
                log.info("lock acquire : {} - {}", mutex, acquire);
                if(acquire){
                    log.info("start execution : {} - {}", mutex, acquire);
                    TimeUnit.SECONDS.sleep(3);
                    log.info("end execution : {} - {}", mutex, acquire);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(mutex.isOwnedByCurrentThread()){
                    log.info("release : {}", mutex);
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
