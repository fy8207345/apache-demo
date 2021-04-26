package org.example.curator.cache;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.example.curator.util.ClientUtils;

@Slf4j
public class Cache {

    public static void main(String[] args) {
        CuratorFramework client = ClientUtils.getClient();
        CuratorCache cache = CuratorCache.build(client, "/cache", CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE);
        cache.start();

        CuratorCacheListener build = CuratorCacheListener.builder()
                .forCreates(childData -> {
                    log.info("create : {}", childData);
                })
                .build();
        cache.listenable()
                .addListener(build);

        log.info("cache: {}", cache.size());
        cache.close();
        client.close();
    }
}
