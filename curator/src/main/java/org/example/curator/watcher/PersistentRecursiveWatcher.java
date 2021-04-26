package org.example.curator.watcher;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.watch.PersistentWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public class PersistentRecursiveWatcher {

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        PersistentWatcher persistentWatcher = new PersistentWatcher(client, "/watcher_base", true);
        persistentWatcher.start();

        persistentWatcher.getListenable()
                .addListener(new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        log.info("event : {}", event);
                    }
                });
        persistentWatcher.getResetListenable()
                .addListener(new Runnable() {
                    @Override
                    public void run() {
                        log.info("running : {}", persistentWatcher);
                    }
                });

        TimeUnit.SECONDS.sleep(90);
        persistentWatcher.close();

        client.close();
    }
}
