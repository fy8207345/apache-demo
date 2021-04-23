package org.example.zookeeper.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.proto.GetDataRequest;
import org.example.zookeeper.barrier.SyncPrimitive;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ZookeeperQueue extends SyncPrimitive {

    private final String name;

    public ZookeeperQueue(String address, String root, String name ) {
        super(address, root);
        this.name = name;
        if(zooKeeper != null){
            try {
                Stat stat = zooKeeper.exists(root, false);
                if(stat == null){
                    zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }catch (KeeperException e){
                log.info("error creating queue : ", e);
            }catch (InterruptedException e){
                log.info("InterruptedException ", e);
            }
        }
    }

    boolean produce(int i) throws InterruptedException, KeeperException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byte[] value;
        byteBuffer.putInt(i);
        value = byteBuffer.array();
        zooKeeper.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        return true;
    }

    int consume() throws InterruptedException, KeeperException {
        Stat stat = null;
        while (true){
            synchronized (mutex){
                log.info("start getchildren");
                List<String> children = zooKeeper.getChildren(root, true);
                log.info("children : {}", children);
                if(children.size() == 0){
                    log.info("going to wait");
                    mutex.wait();
                }else {
                    int min = Integer.parseInt(children.get(0).substring(7));
                    for(String s : children){
                        int temp = Integer.parseInt(s.substring(7));
                        if(temp < min){
                            min = temp;
                        }
                    }
//                    log.info("got current sequence index : {}", min);
                    String fullpath = root + "/element" + String.format("%010d", min);
                    log.info("start getdata");
                    byte[] data = zooKeeper.getData(fullpath, false, stat);
                    log.info("get data and start delete data");
                    zooKeeper.delete(fullpath, 0);
                    log.info("deleted data");
                    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                    return byteBuffer.getInt();
                }
            }
        }
    }
}
