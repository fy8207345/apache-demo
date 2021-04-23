package org.example.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.example.curator.util.ClientUtils;

import java.nio.charset.StandardCharsets;

public class CuratorMain {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = ClientUtils.getClient();
        client.create()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/path", "string".getBytes(StandardCharsets.UTF_8));
    }
}
