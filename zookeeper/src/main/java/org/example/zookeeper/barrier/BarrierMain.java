package org.example.zookeeper.barrier;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class BarrierMain {

    Barrier barrier1 = new Barrier("localhost:12181", "/barrier", 2, "barrier1");
    Barrier barrier2 = new Barrier("localhost:12182", "/barrier", 2, "barrier2");
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException, KeeperException {

        BarrierMain barrierMain = new BarrierMain();
        barrierMain.startEnter(barrierMain.barrier1);
        barrierMain.startEnter(barrierMain.barrier2);
        TimeUnit.SECONDS.sleep(5);
    }

    private void startEnter(final Barrier barrier){
        executorService.submit(() -> {
            try {
                boolean enter = barrier.enter();
                log.info("barrier entered : {} - {}", barrier, enter);
                if(enter){
                    afterEnter(barrier);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        });
    }

    private void afterEnter(final Barrier barrier){
        log.info("{} execute action!!!! ", barrier);
        leave(barrier);
    }

    private void leave(final Barrier barrier){
        executorService.submit(() -> {
            try {
                boolean leave = barrier.leave();
                log.info("barrier leaved : {} - {}", barrier, leave);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        });
    }
}
