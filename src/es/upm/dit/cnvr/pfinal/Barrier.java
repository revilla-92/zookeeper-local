package es.upm.dit.cnvr.pfinal;

import java.io.IOException;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher;

public class Barrier implements Watcher {

	static ZooKeeper zk = null;

	private int size;
	private String name = "process_";
	String root;
	int nWatchers;

	Integer mutexBarrier = -1;


	/**
	 * Barrier constructor
	 *
	 * @param address
	 * @param root
	 * @param size
	 */
	public Barrier(String address, String root, int size) {

		if(zk == null){
			try {
				System.out.println("Starting ZK:");
				zk = new ZooKeeper(address, 3000, this);
				System.out.println("Finished starting ZK: " + zk);
			} catch (IOException e) {
				System.out.println(e.toString());
				zk = null;
			}
		}
		//else mutex = new Integer(-1);

		this.root = root;
		this.size = size;

		// Create barrier node
		if (zk != null) {
			try {
				Stat s = zk.exists(root, false);
				if (s == null) {
					zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} catch (KeeperException e) {
				System.out.println("Keeper exception when instantiating queue: " + e.toString());
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception");
			}
		}
	}


	public void process(WatchedEvent event) {
		nWatchers++;
		System.out.println(">>> Process: " + event.toString() + ", " + nWatchers);
		System.out.println("Process: " + event.getType());
		synchronized (mutexBarrier) {
			mutexBarrier.notify();
		}
	}


	/**
	 * Join barrier
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	boolean enter() throws KeeperException, InterruptedException{
		name = zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		zk.exists(name, this);

		while (true) {
			List<String> list = zk.getChildren(root, true);

			if (list.size() < size) {
				synchronized (mutexBarrier) {
					mutexBarrier.wait();
				}
			} else {
				return true;
			}
		}
	}

	
	/**
	 * Wait until all reach barrier
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	boolean leave() throws KeeperException, InterruptedException{
		zk.delete(name, 0);
		while (true) {
			List<String> list = zk.getChildren(root, true);
			if (list.size() > 0) {
				synchronized (mutexBarrier) {
					mutexBarrier.wait();
				}
			} else {
				return true;
			}
		}
	}

}