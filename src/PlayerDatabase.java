import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerDatabase {
    final static int totalNum = 4;
    volatile Player[] table = new Player[20];
    volatile Player[] resizeTable;
    volatile boolean resizing = false;
    volatile boolean[] resizers = new boolean[totalNum];
    AtomicInteger filled = new AtomicInteger();
    ReadWriteLock lock = new ReentrantReadWriteLock();

    volatile ArrayList<String> playerID = new ArrayList<>();

    public PlayerDatabase(){
        for(boolean b: resizers){
            b = false;
        }
    }

    public void put(Player p){
        int h = p.getUser().hashCode();
        //takes the absolute value of the hash % table length -1
        int i = Math.abs(h % (table.length));
        Player current = table[i];
        p.next = null;
        for(;;) {
            if (current == null) {
                this.table[i] = p;
                playerID.add(p.getUser());
                filled.getAndIncrement();
                break;
            }
            else if(current.getUser().equalsIgnoreCase(p.getUser())) {
                break;
            }
            else{
                current = returnParent(current);
                current.lock.writeLock().lock();
                try{
                    current.setNext(p);
                    filled.getAndIncrement();
                    playerID.add(p.getUser());
                    break;
                }
                finally {
                    current.lock.writeLock().unlock();
                }
            }
        }
    }

    public void resizePrep(){
        if(this.lock.writeLock().tryLock()){
            try{
                resizing = true;
                resizeTable = table;
                table = new Player[table.length * 2];
                playerID.clear();
                filled.set(0);
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    public void finishedResize(){
        int counter = 0;
        for(boolean b: resizers){
            if(b){
                counter++;
            }
        }
        if(counter >= totalNum){
            if(this.lock.writeLock().tryLock()){
                try {
                    for (int i = 0; i < totalNum; i++) {
                        this.resizers[i] = false;
                    }
                    this.resizing = false;
                }
                finally {
                    this.lock.writeLock().unlock();
                }
            }
        }
    }

    public Player returnParent(Player p){
        if(p.lock.readLock().tryLock()) {
            try {
                if (p.next == null) {
                    return p;
                } else {
                    return returnParent(p.next);
                }
            } finally {
                p.lock.readLock().unlock();
            }
        }
        return null;
    }

    public Player getPlayer(String id, Player p){
        p.lock.readLock().lock();
        try{
            if(p.getUser().equalsIgnoreCase(id)){
                //System.out.println("Found player");
                return p;
            }
            else if(p.next == null){
                return null;
            }
            else{
                //System.out.println("Find player test recursive");
                return getPlayer(p.next.getUser(), p.next);
            }
        }
        finally {
            //System.out.println("Player unlock");
            p.lock.readLock().unlock();
        }
    }
}
