package org.example.zookeeper.barrier;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

@Slf4j
public class Barrier extends SyncPrimitive{

    private final int size;
    private final String name;

    public Barrier(String address, String root, int size, String name) {
        super(address, root);
        this.size = size;
        this.name = name;
        if(zooKeeper != null){
            try {
                Stat stat = zooKeeper.exists(root, false);
                if(stat == null){
                    zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            }catch (KeeperException e){
                log.error("keeper exception : " , e);
            }catch (InterruptedException e){
                log.error("InterruptedException : " , e);
            }
        }
    }


    public boolean enter() throws InterruptedException, KeeperException {
        zooKeeper.create(root + "/" + name, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        while (true){
            synchronized (mutex){
                List<String> children = zooKeeper.getChildren(root, true);
                if(children.size() < size){
                    mutex.wait();
                }else{
                    return true;
                }
            }
        }
    }

    public boolean leave() throws InterruptedException, KeeperException {
        zooKeeper.delete(root + "/" + name, 0);
        while (true){
            synchronized (mutex){
                List<String> children = zooKeeper.getChildren(root, true);
                if(children.size() > 0){
                    mutex.wait();
                }else{
                    return true;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Barrier{" +
                "name='" + name + '\'' +
                '}';
    }
}
