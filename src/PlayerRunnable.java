import java.util.concurrent.ThreadLocalRandom;

public class PlayerRunnable implements Runnable{

    public final int id;
    volatile PlayerDatabase db;

    public PlayerRunnable(int id, PlayerDatabase db){
        this.id = id;
        this.db = db;
    }

    @Override
    public void run() {
        for(int i  = 0; i < 20000; i++) {
            System.out.println("test 1");
            System.out.println(db.playerID.size());
            int user = 0;
            if(db.playerID.size() != 0) {
                user = ThreadLocalRandom.current().nextInt(db.playerID.size());
            }
            System.out.println("test 2 post get");
            if(!db.playerID.isEmpty() && db.playerID.get(user) != null) {
                System.out.println("test 2 pre read");
                Player playerRead = readPlayer(db.playerID.get(user));
                System.out.println("test 2");

                if (playerRead != null) {
                    writePlayer(playerRead);
                }
            }
            System.out.println("test 3");
            newPlayer();
            System.out.println("test 4");
            checkResize();
            System.out.println("TEST BEFORE: " +id);
        }
    }

    public Player readPlayer(String user){
        //takes the absolute value of the hash % table length -1
        int i = Math.abs(user.hashCode() % (db.table.length));
        if(db.table[i] != null){
            if(db.table[i].lock.readLock().tryLock()){
                try {
                    return db.getPlayer(user, db.table[i]);
                }
                finally {
                    db.table[i].lock.readLock().unlock();
                }
            }
        }
        return null;
    }

    public void writePlayer(Player p){
        if(ThreadLocalRandom.current().nextDouble() < .05){
            if(p.lock.writeLock().tryLock()){
                try {
                    p.expInc(ThreadLocalRandom.current().nextInt(150));
                    p.transaction(ThreadLocalRandom.current().nextInt(10));
                }
                finally {
                    p.lock.writeLock().unlock();
                }
            }
        }
    }

    public void newPlayer(){
        if(ThreadLocalRandom.current().nextDouble() < .001){
            Player np = new Player("NEW THREAD PLAYER " + ThreadLocalRandom.current().nextInt(10000000) + "       ID: " + id);
            db.put(np);
            if(db.filled.get() >= db.table.length * .75  &&  !db.resizing){
                db.resizePrep();
            }
        }
    }

    public void checkResize(){
        if(db.resizing && !db.resizers[id]){
            int start = (db.resizeTable.length / db.totalNum) * id;
            int finish = (db.resizeTable.length / db.totalNum) * (id + 1);
            for(int i = start; i < finish; i++){
                if(db.resizeTable[i] != null) {
                    putLinked(db.resizeTable[i]);
                }
            }
            System.out.println("FINISH RESIZE:     asd" + id);
            db.resizers[id] = true;
        }
        if(db.resizing && db.resizers[id]){
            //System.out.println("FINISH RESIZE:     " + id);
            db.finishedResize();
            System.out.println("TEST AFTER FINISH RESIZE" + id);
        }
    }

    public void putLinked(Player p){
        p.lock.readLock().lock();
        try{
            db.put(new Player(p));
            if(p.next != null){
                putLinked(p.next);
            }
        }
        finally {
            p.lock.readLock().unlock();
        }
    }
}
