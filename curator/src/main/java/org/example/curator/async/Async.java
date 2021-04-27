package org.example.curator.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.api.WatchableAsyncCuratorFramework;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Async {

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
        WatchableAsyncCuratorFramework watched = async.watched();
        CompletableFuture<Void> voidCompletableFuture = watched.getData().forPath("/watch").event()
                .thenAccept(watchedEvent -> {
                    log.info("getData watched event : {}", watchedEvent);
                    addWatch(watched);
                }).toCompletableFuture();
        watched.checkExists().forPath("/watch")
                .event()
                .thenAccept(watchedEvent -> {
                    log.info("checkExists watched event : {}", watchedEvent);
                    addExistsWatch(watched);
                });
        TimeUnit.SECONDS.sleep(100);
    }

    static void addWatch(WatchableAsyncCuratorFramework watched){
        CompletableFuture<Void> voidCompletableFuture = watched.getData().forPath("/watch").event()
                .thenAccept(watchedEvent -> {
                    log.info("getData watched event : {}", watchedEvent);
                    addWatch(watched);
                }).toCompletableFuture();
    }

    static void addExistsWatch(WatchableAsyncCuratorFramework watched){
        CompletableFuture<Void> voidCompletableFuture = watched.getData().forPath("/watch").event()
                .thenAccept(watchedEvent -> {
                    log.info("checkExists watched event : {}", watchedEvent);
                    addExistsWatch(watched);
                }).toCompletableFuture();
    }
}
