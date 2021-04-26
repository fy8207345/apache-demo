package org.example.curator.queue;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.example.curator.util.ClientUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MyQueue {

    static AtomicLong atomicLong = new AtomicLong(0L);
    static ExecutorService executorService = Executors.newFixedThreadPool(8);

    public static void main(String[] args) throws InterruptedException {
        CuratorFramework client = ClientUtils.getClient();
        QueueBuilder<String> queueBuilder = QueueBuilder
                .builder(client, new Consumer(), new QueueSerializer<String>() {
                    @Override
                    public byte[] serialize(String item) {
                        return item.getBytes(StandardCharsets.UTF_8);
                    }

                    @Override
                    public String deserialize(byte[] bytes) {
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                }, "/queue");

        queueBuilder.lockPath("/queue-backup");
        queueBuilder.maxItems(3);
        queueBuilder.executor(executorService);
        DistributedQueue<String> stringDistributedQueue = queueBuilder.buildQueue();
        try {
            stringDistributedQueue.start();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for(int i=0;i<10;i++){
            produce(stringDistributedQueue);
        }

        TimeUnit.SECONDS.sleep(10);
        executorService.shutdown();
        while(!executorService.isTerminated()){
            TimeUnit.SECONDS.sleep(1);
        }

        try {
            stringDistributedQueue.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void produce(final DistributedQueue<String> queue){
        executorService.execute(() -> {
            try {
                String str = "value" + atomicLong.getAndIncrement();
                boolean success = queue.put(str, 2, TimeUnit.SECONDS);
                log.info("produce : {} _ {}", str, success);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static class Consumer implements QueueConsumer<String> {

        @Override
        public void consumeMessage(String message) throws Exception {
            log.info("consume : {}", message);
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            log.info("stateChanged : {} - {}", client, newState);
        }
    }
}
