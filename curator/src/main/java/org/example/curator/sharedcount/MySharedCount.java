package org.example.curator.sharedcount;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.example.curator.util.ClientUtils;

import java.io.IOException;

@Slf4j
public class MySharedCount {
    public static void main(String[] args) {

        CuratorFramework client = ClientUtils.getClient();
        SharedCount sharedCount = new SharedCount(client, "/shared-count", 1);

        try {
            sharedCount.start();
            sharedCount.setCount(2);
            int count = sharedCount.getCount();
            log.info("value : {}", count);
            boolean b = sharedCount.trySetCount(sharedCount.getVersionedValue(), 4);
            log.info("trySetCount : {}", b);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                sharedCount.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
