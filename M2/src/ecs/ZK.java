package ecs;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZK {

    private static ZooKeeper zk;
    private static final String LOCAL_HOST = "localhost";
    //public static final String ZK_HOST = getCurrentHost();
    private static final int ZK_TIMEOUT = 2000;
    public static final String ZK_PORT = "2181";

    //public int ZK_PORT = 2181;
    //public static final String ZK_SERVER_PATH = "/server";
    public final CountDownLatch connectedSignal = new CountDownLatch(1);
    public static String ZK_HOST;

    public ZK(String zk_host){
        ZK_HOST = zk_host;
    }



    public ZooKeeper connect() throws IOException, IllegalStateException {
        zk = new ZooKeeper(ZK_HOST+":"+ZK_PORT, ZK_TIMEOUT, watchedEvent -> {
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        try {
            connectedSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }

    public void close() throws InterruptedException {
        zk.close();
    }

    public void create(String path, byte[] data) throws KeeperException, InterruptedException {
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public static byte[] read(String path) throws KeeperException, InterruptedException {
        return zk.getData(path, false, zk.exists(path, true));
    }

    public static byte[] readNullStat(String path) throws KeeperException, InterruptedException {
        return zk.getData(path, false, null);
    }

    public static void update(String path, byte[] data) throws KeeperException, InterruptedException {

        zk.setData(path, data, zk.exists(path, true).getVersion());
        List<String> children = zk.getChildren(path, false);
        for (String child : children) delete(path + "/" + child);
    }

    public static void delete(String path) throws KeeperException, InterruptedException {
        zk.delete(path, zk.exists(path, true).getVersion());
    }

    public static void deleteNoWatch(String path) throws KeeperException, InterruptedException {
        zk.delete(path, zk.exists(path, false).getVersion());
    }

    // ACL: access control list
    // authentication method
    // this returns {<scheme>, <who can access>}
    /*
    crdwa = 31
    Create
    Read
    Delete
    Write
    Admin
     */
    public static List<ACL> getacl(String path) throws KeeperException, InterruptedException {
        return zk.getACL(path, zk.exists(path, true));
    }

}
