package org.example.curator.sharedreentrantlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.RevocationListener;
import org.apache.curator.framework.recipes.locks.Revoker;
import org.example.curator.util.ClientUtils;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 可回收的可重入锁
 */
@Slf4j
public class RevocableInterProcessMutex {

    public static final String PATH = "/mutex";
    static ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
        CuratorFramework client1 = ClientUtils.getClient();
        InterProcessMutex mutex1 = new InterProcessMutex(client1, PATH);

        CuratorFramework client2 = ClientUtils.getClient();
        InterProcessMutex mutex2 = new InterProcessMutex(client2, PATH);

        RevocationListener<InterProcessMutex> listener = new RevocationListener<InterProcessMutex>() {
            @Override
            public void revocationRequested(InterProcessMutex forLock) {
                log.info("revoke request : {}", forLock);
                if(forLock.isOwnedByCurrentThread()){
                    try {
                        forLock.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mutex1.makeRevocable(listener, executorService);
        mutex2.makeRevocable(listener, executorService);

        acquire(mutex1, client1);
        acquire(mutex2, client2);

        TimeUnit.SECONDS.sleep(20);
        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client1.close();
        client2.close();
    }

    private static void acquire(final InterProcessMutex mutex, final CuratorFramework client){
        executorService.execute(() -> {
            boolean acquire = false;
            try {
                acquire = mutex.acquire(3, TimeUnit.SECONDS);
                log.info("acquired: {} - {}", mutex.getParticipantNodes(), acquire);
                if(acquire){
                    revoke(mutex, client);
                }
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(mutex.isOwnedByCurrentThread()){
                    try {
                        log.info("release: {}", mutex);
                        mutex.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static void revoke(final InterProcessMutex mutex, final CuratorFramework client){
        executorService.execute(() -> {
            try {
                mutex.getParticipantNodes().stream().findFirst().ifPresent(s -> {
                    try {
                        Revoker.attemptRevoke(client, s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
