package org.example.zookeeper.barrier;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

@Slf4j
public class SyncPrimitive implements Watcher {

    protected ZooKeeper zooKeeper;
    protected final Object mutex = new Object();
    protected String root;

    public SyncPrimitive(String address, String root) {
        this.root = root;
        try {
            log.info("Starting ZK");
            zooKeeper = new ZooKeeper(address, 3000, this);
            log.info("Finished starting ZK : {}", zooKeeper);
        }catch (IOException e){
            log.error("error staring ZK : ", e);
            zooKeeper = null;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        log.info("received event: {}", event.getState());
        synchronized (mutex){
            mutex.notifyAll();
        }
    }
}
