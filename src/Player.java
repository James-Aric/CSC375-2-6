import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Player {
    private final String user;
    private int exp;
    private int level;
    private int nextLevelExp;
    private int gold;
    ReadWriteLock lock = new ReentrantReadWriteLock();

    private Random rand = new Random();

    Player next;

    public Player(String user){
        this.user = user;
        level = rand.nextInt(10);
        exp = 0;
        nextLevelExp = level * 100;
        gold = rand.nextInt(1000);
        next = null;
    }
    public Player(Player p){
        this.user = p.user;
        this.level = p.level;
        this.exp = p.exp;
        this.nextLevelExp = p.nextLevelExp;
        this.gold = p.gold;
        this.next = p.next;
    }

    public String getUser(){
        return user;
    }

    public int getExp(){
        return exp;
    }

    public int getLevel(){
        return level;
    }

    public int getNextLevelExp(){
        return nextLevelExp;
    }

    public int getGold(){
        return gold;
    }

    public boolean hasNext(){
        if(next == null){
            return false;
        }
        return true;
    }

    public void levelUp(){
        exp = exp - nextLevelExp;
        level++;
        nextLevelExp = level * 100;
        //System.out.println("Level up!");
    }

    public void expInc(int num){
        exp += num;
        if(exp >= nextLevelExp){
            levelUp();
        }
    }

    public void transaction(int num){
        gold += num;
    }

    public void setNext(Player next){
        this.next = next;
    }
}
