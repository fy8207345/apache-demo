package org.example.curator.leaderelection;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.CancelLeadershipException;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyLeaderElection {

    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client1 = ClientUtils.getClient();
        CuratorFramework client2 = ClientUtils.getClient();
        LeaderSelectorListenerAdapter selectorListener = new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                log.info("takeLeadership: {}", client);
                TimeUnit.SECONDS.sleep(2);
                log.info("release leadership : {}", client);
            }
        };
        LeaderSelector leaderSelector1 = new LeaderSelector(client1, "/leader-election", selectorListener);
        LeaderSelector leaderSelector2 = new LeaderSelector(client2, "/leader-election", selectorListener);
        leaderSelector1.start();
        leaderSelector2.start();

        TimeUnit.SECONDS.sleep(5);
    }
}
