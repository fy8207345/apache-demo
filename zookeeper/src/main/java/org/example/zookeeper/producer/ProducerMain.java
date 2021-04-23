package org.example.zookeeper.producer;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProducerMain {

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    ZookeeperQueue producer = new ZookeeperQueue("localhost:12181,localhost:12182,localhost:12183", "/producer-consumer", "producer");
    ZookeeperQueue consumer = new ZookeeperQueue("localhost:12181,localhost:12182,localhost:12183", "/producer-consumer", "consumer");

    public static void main(String[] args) throws InterruptedException {
        ProducerMain producerMain = new ProducerMain();
        producerMain.produce(producerMain.producer);
        producerMain.consume(producerMain.consumer);

        TimeUnit.HOURS.sleep(1);
    }

    private void produce(final ZookeeperQueue queue){
        executorService.submit(() -> {
            int i = 0;
            while (true){
                try {
                    boolean produce = queue.produce(++i);
                    if(produce){
                        log.info("produced: {}", i);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                TimeUnit.SECONDS.sleep(3);
            }
        });
    }

    private void consume(final ZookeeperQueue queue){
        executorService.submit(() -> {
            while (true){
                try {
                    int value = queue.consume();
                    log.info("consumed value: {}", value);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
