package org.example.curator.leaderlatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.example.curator.util.ClientUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyLeaderLatch {
    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws Exception {
        CuratorFramework client1 = ClientUtils.getClient();
        CuratorFramework client2 = ClientUtils.getClient();
        LeaderLatch leaderLatch1 = new LeaderLatch(client1, "/leader-latch", "participant1");
        LeaderLatch leaderLatch2 = new LeaderLatch(client2, "/leader-latch", "participant2");
        LeaderLatchListener listener = new LeaderLatchListener() {
            @Override
            public void isLeader() {
                log.info("isLeader");
            }

            @Override
            public void notLeader() {
                log.info("notLeader");
            }
        };

        ConnectionStateListener listener1 = new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                log.info("state: {} - {}", client, newState);
            }
        };
        client1.getConnectionStateListenable().addListener(listener1);
        client2.getConnectionStateListenable().addListener(listener1);

        leaderLatch1.addListener(listener);
        leaderLatch2.addListener(listener);

        MyLeaderLatch myLeaderLatch = new MyLeaderLatch();
        myLeaderLatch.start(leaderLatch1);
        myLeaderLatch.start(leaderLatch2);

        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }
        client1.close();
        client2.close();
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
