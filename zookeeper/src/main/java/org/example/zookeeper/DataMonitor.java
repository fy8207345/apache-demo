package org.example.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

public class DataMonitor implements AsyncCallback.StatCallback, Watcher {

    private ZooKeeper zooKeeper;
    private String znode;
    private Watcher watcher;
    boolean dead;
    DataMonitorListener listener;
    byte[] prevData;

    public DataMonitor(ZooKeeper zooKeeper, String znode, Watcher watcher, DataMonitorListener listener) {
        this.zooKeeper = zooKeeper;
        this.znode = znode;
        this.watcher = watcher;
        this.listener = listener;
        zooKeeper.exists(znode, true, this, null);
    }

    @Override
    public void processResult(int rc, String s, Object o, Stat stat) {
        boolean exists = false;
        if(rc == KeeperException.Code.OK.intValue()){
            exists = true;
        }else if(rc == KeeperException.Code.NONODE.intValue()){
            exists = false;
        }else if(rc == KeeperException.Code.SESSIONEXPIRED.intValue() || rc == KeeperException.Code.NOAUTH.intValue()){
            dead = true;
            listener.closing(rc);
        }else if(rc == KeeperException.Code.CONNECTIONLOSS.intValue()){
            return;
        }else{
            zooKeeper.exists(znode, true, this, null);
            return;
        }
        byte[] b = null;
        if(exists){
            try {
                b = zooKeeper.getData(znode, false, null);
            }catch (KeeperException e){
                e.printStackTrace();
            }catch (InterruptedException e){
                return;
            }
        }
        if(b == null && b != prevData || (b != null && !Arrays.equals(prevData, b))){
            listener.exists(b);
            prevData = b;
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String path = watchedEvent.getPath();
        if(watchedEvent.getType() == Event.EventType.None){
            switch (watchedEvent.getState()){
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.intValue());
                    break;
            }
        }else {
            if(path != null && path.equals(znode)){
                zooKeeper.exists(znode, true, this, null);
            }
        }
        if(watcher != null){
            watcher.process(watchedEvent);
        }
    }

    public boolean dead(){
        return dead;
    }

    public interface DataMonitorListener{
        /**
         * The existence status of the node has changed.
         * @param data node data
         */
        void exists(byte[] data);

        /**
         *
         * @param rc  the ZooKeeper reason code
         */
        void closing(int rc);
    }
}
