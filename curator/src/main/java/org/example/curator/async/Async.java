package org.example.curator.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Async {

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        AsyncCuratorFramework wrap = AsyncCuratorFramework.wrap(client);
        CompletableFuture<Void> voidCompletableFuture = wrap.checkExists()
                .forPath("/async")
                .thenAccept(stat -> {
                    log.info("exists : {}", stat);
                }).toCompletableFuture();
        voidCompletableFuture.join();
    }
}
