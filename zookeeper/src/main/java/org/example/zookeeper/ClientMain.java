package org.example.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.server.util.ZxidUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientMain {

    public static void main(String[] args) {
        System.out.println(ZxidUtils.makeZxid(1, 0));
        System.setProperty(ZKClientConfig.ZOOKEEPER_CLIENT_CNXN_SOCKET, "org.apache.zookeeper.ClientCnxnSocketNetty");
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper("localhost:63180", 3000, new MyWatcher());
            zooKeeper.create("/client", "data".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            TimeUnit.HOURS.sleep(100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public static class MyWatcher implements Watcher{

        @Override
        public void process(WatchedEvent event) {
            log.info("process: {}", event);
        }
    }
}
