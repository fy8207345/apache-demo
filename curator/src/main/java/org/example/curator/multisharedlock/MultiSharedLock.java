package org.example.curator.multisharedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.example.curator.util.ClientUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MultiSharedLock {

    public static void main(String[] args) {

        CuratorFramework client1 = ClientUtils.getClient();
        CuratorFramework client2 = ClientUtils.getClient();
        InterProcessMutex mutex1 = new InterProcessMutex(client1, "/lock1");
        InterProcessMutex mutex2 = new InterProcessMutex(client2, "/lock2");
        InterProcessMultiLock lock = new InterProcessMultiLock(Arrays.asList(mutex1, mutex2));

        try {
            boolean acquire = lock.acquire(2, TimeUnit.SECONDS);
            log.info("acquired : {} - {} - {} - {}", lock, acquire, mutex1.isOwnedByCurrentThread(), mutex2.isOwnedByCurrentThread());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(lock.isAcquiredInThisProcess()){
                try {
                    log.info("release : {}", lock);
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        client1.close();
        client2.close();
    }
}
