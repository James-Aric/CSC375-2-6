import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Runner {
    static volatile PlayerDatabase players = new PlayerDatabase();
    static final int totalNum = 4;
    static volatile ConcurrentHashMap<Long, Player> standardMap = new ConcurrentHashMap<>(20);
    public static void main(String[]args) throws InterruptedException{
        Player temp = null;
        for(int i = 0; i < 10; i++){
            temp = new Player("ID: " + i);
            players.put(temp);
        }

        for(int i = 0; i < 10; i++){
            temp = new Player("ID: " + i);
            standardMap.put((long)temp.getUser().hashCode(), temp);
        }

/*        System.out.println("Successfully added initial players.");
        ExecutorService es = Executors.newFixedThreadPool(totalNum);

        for(int i = 0; i < totalNum; i++){
            System.out.println(i);
            es.submit(new PlayerRunnable(i, players));
        }
        Thread.sleep(10000);
        es.shutdown();*/

        ExecutorService esStandard = Executors.newFixedThreadPool(totalNum);

        for(int i = 0; i < totalNum; i++){
            esStandard.submit(new StandardStructureRunner(standardMap, i));
        }

        //Thread.sleep(10000);
        //esStandard.shutdown();
    }
}
