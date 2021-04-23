package org.example.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.example.curator.util.ClientUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyLeaderLatch {
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws Exception {
        LeaderLatch leaderLatch1 = new LeaderLatch(ClientUtils.getClient(), "/leader-latch", "participant1");
        LeaderLatch leaderLatch2 = new LeaderLatch(ClientUtils.getClient(), "/leader-latch", "participant2");

        MyLeaderLatch myLeaderLatch = new MyLeaderLatch();
        myLeaderLatch.start(leaderLatch1);
        myLeaderLatch.start(leaderLatch2);
    }

    private void start(final LeaderLatch leaderLatch){
        executorService.execute(() -> {
            try {
                leaderLatch.start();
                log.info("acquiring : {}", leaderLatch);
                boolean acquired = leaderLatch.await(3, TimeUnit.SECONDS);
                log.info("acquired : {}, {}", leaderLatch, acquired);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    leaderLatch.close();
                    log.info("lock released : {}", leaderLatch);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
