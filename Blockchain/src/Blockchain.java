import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class Blockchain {
    private volatile static Blockchain blockchain = new Blockchain();
    private volatile List<Block> blocks = new ArrayList<>();
    private volatile Set<Long> ids = new HashSet<>();
    private volatile List<Message> messagesToAdd = new ArrayList<>();
    private volatile int numZeros = 0;

    private Blockchain() {}

    public static Blockchain getBlockchain() {
        return blockchain;
    }

    public void addBlock(Block block) {
        if (!ids.contains(block.getId())) {
            blocks.add(block);
            ids.add(block.getId());
            adjustNumZeros(blocks.get(blocks.size() - 1));
            rewardMiner(block.getMiner());
            messagesToAdd.removeAll(block.getMessages());
        }
    }

    public void rewardMiner(User user) {
        user.receiveCoins(100);
    }

    public void submitMessage(Message message) {
        if (messagesToAdd.isEmpty() || messagesToAdd.get(messagesToAdd.size() - 1).getId() < message.getId()) {
            messagesToAdd.add(message);
        }
    }

    public List<Message> getMessagesToAdd() {
        return new ArrayList<>(messagesToAdd);
    }

    public long getCurrentId() {
        return blocks.get(blocks.size() - 1).getId();
    }

    public String getLastHash() {
        return blocks.get(blocks.size() - 1).getHash();
    }

    public int getBlockchainSize() {
        return blocks.size();
    }

    public int getNumZeros() {
        return numZeros;
    }

    public void adjustNumZeros(Block block) {
        long time = block.getTimeGenerating();
        if (time <= 10) {
            numZeros++;
            block.setChangeInZeros(numZeros);
        } else if (time >= 60) {
            numZeros--;
            block.setChangeInZeros(-numZeros);
        } else {
            block.setChangeInZeros(0);
        }
    }

    public void print() {
        blocks.forEach(block -> System.out.println(block + "Block was generating for " + block.getTimeGenerating() + " seconds\n"
        + block.getChange() + "\n"));
    }
}