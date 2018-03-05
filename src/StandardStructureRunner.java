import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class StandardStructureRunner implements Runnable{
    ConcurrentHashMap<Long, Player> map = null;
    int id;

    public StandardStructureRunner(ConcurrentHashMap<Long, Player> map, int id){
        this.map = map;
        this.id = id;
    }

    Set<Long> keys;
    List<Long> keyArray;

    @Override
    public void run() {
        for(int i = 0; i < 20000; i++) {
            keys = map.keySet();
            keyArray = new ArrayList<>(map.keySet());
            int index = 0;
            Player readPlayer;
            //System.out.println("test 1");
            if (!map.isEmpty()) {
                readPlayer = map.get(keyArray.get(ThreadLocalRandom.current().nextInt(keyArray.size())));
                if (map.get(index) != null) {
                    //System.out.println("test 3");
                    readPlayer = map.get(index);
                }
                if (ThreadLocalRandom.current().nextDouble() < .05) {
                    System.out.println("test 4");
                    readPlayer.expInc(ThreadLocalRandom.current().nextInt(150));
                    readPlayer.transaction(ThreadLocalRandom.current().nextInt(10));
                }
                //System.out.println("test post 4");
                if (ThreadLocalRandom.current().nextDouble() < .001) {
                    System.out.println("test 5");
                    Player p = new Player("NEW THREAD PLAYER " + ThreadLocalRandom.current().nextInt(10000000) + "       ID: " + id);
                    map.put((long) p.getUser().hashCode(), p);
                }
            }
        }
        System.out.println("bleh");
    }
}
