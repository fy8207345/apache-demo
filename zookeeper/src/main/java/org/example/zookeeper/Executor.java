package org.example.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

    String znode;
    DataMonitor dataMonitor;
    ZooKeeper zooKeeper;
    String fileName;
    String exec[];
    Process child;

    public Executor(String host, int timeout, String znode, String fileName, String[] exec) throws IOException {
        this.fileName = fileName;
        this.exec = exec;
        this.znode = znode;
        this.zooKeeper = new ZooKeeper(host, timeout, this);
        dataMonitor = new DataMonitor(zooKeeper, znode, null, this);
    }

    @Override
    public void exists(byte[] data) {
        if(data == null){
            if(child != null){
                System.out.println("killing process");
                child.destroy();
            }
            try {
                child.waitFor();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            child = null;
        } else {
          if(child != null){
              System.out.println("stopping child");
              child.destroy();
              try {
                  child.waitFor();
              }catch (InterruptedException e){
                  e.printStackTrace();
              }
          }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                fileOutputStream.write(data);
                fileOutputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            try {
                System.out.println("starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(System.out, child.getInputStream());
                new StreamWriter(System.err, child.getErrorStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    static class StreamWriter extends Thread{
        OutputStream os;
        InputStream is;

        public StreamWriter(OutputStream os, InputStream is) {
            this.os = os;
            this.is = is;
        }

        @Override
        public void run() {
            byte b[] = new byte[80];
            int rc;
            try {
                while ((rc = is.read()) > 0){
                    os.write(b, 0 ,rc);
                }
            }catch (IOException e){}
        }
    }

    @Override
    public void closing(int rc) {
        synchronized (this){
            notifyAll();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        dataMonitor.process(watchedEvent);
    }

    @Override
    public void run() {
        try {
            synchronized (this){
                while (!dataMonitor.dead()){
                    wait();
                }
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
