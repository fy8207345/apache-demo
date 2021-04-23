package org.example.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Client {
    public static void main(String[] args){
        try {
            Executor executor = new Executor("localhost:12181", 3000, "/s", "e:/zk.txt", new String[]{"echo", "hello"});
            executor.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
