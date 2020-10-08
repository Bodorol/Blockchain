import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Blockchain chain = Blockchain.getBlockchain();
        List<User> miners = new ArrayList<>();
        User sender = new User("sender", chain);
        User receiver = new User("receiver", chain);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        sender.sendCoins(receiver, 50);
        for (int i = 0; i < 10; i++) {
            User miner = new User("miner" + (i + 1), chain);
            miners.add(miner);
            executor.submit(miner);
        }
        miners.get(1).sendCoins(miners.get(0), 20);
        receiver.sendCoins(sender, 50);
        miners.get(0).sendCoins(miners.get(1), 20);
        Thread.sleep(5);
        miners.get(5).sendCoins(miners.get(3), 10);
        miners.get(8).sendCoins(miners.get(6), 20);
        Thread.sleep(5);
        miners.get(6).sendCoins(miners.get(4), 15);
        miners.get(3).sendCoins(miners.get(2), 10);
        Thread.sleep(6000);
        executor.shutdownNow();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        chain.print();
    }
}
