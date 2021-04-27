package org.example.curator.async;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.api.CreateOption;
import org.apache.curator.x.async.modeled.JacksonModelSerializer;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.apache.curator.x.async.modeled.ZPath;
import org.example.curator.util.ClientUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class Modeled {

    public static void main(String[] args) {
        CuratorFramework client = ClientUtils.getClient();
        AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);

        ZPath zPath = ZPath.parse("/my/static/path");
        ModelSpec<Foo> spec = ModelSpec.builder(zPath, JacksonModelSerializer.build(Foo.class)).withCreateOptions(
                Sets.newHashSet(CreateOption.createParentsIfNeeded, CreateOption.setDataIfExists)
        ).build();
        ModeledFramework<Foo> wrap = ModeledFramework.wrap(async, spec);
        Foo foo = new Foo();
        foo.name = "foo";
        CompletableFuture<Void> fooCompletableFuture = wrap.set(foo)
                .thenAccept(watchedEvent -> {
                    log.info("watch created path : {}", watchedEvent);
                })
                .thenCompose(new Function<Void, CompletionStage<Foo>>() {
                    @Override
                    public CompletionStage<Foo> apply(Void unused) {
                        return wrap.read();
                    }
                })
                .thenAccept(foo1 -> {
                    log.info("readed result : {}", foo1);
                })
                .toCompletableFuture();
        fooCompletableFuture.join();
    }

    public static class Foo{
        private String name = "test";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Foo{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
