import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class Blockchain {
    private volatile static Blockchain blockchain = new Blockchain();
    private volatile List<Block> blocks = new ArrayList<>();
    private volatile Set<Long> ids = new HashSet<>();
    private volatile List<String> messagesToAdd = new ArrayList<>();
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
            messagesToAdd.removeAll(block.getMessages());
        }
    }

    public void submitMessage(String message) {
        messagesToAdd.add(message);
    }

    public List<String> getMessagesToAdd() {
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

    public static void main(String[] args) throws InterruptedException {
        Blockchain chain = Blockchain.getBlockchain();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executor.submit(new Miner(chain));
        }
        chain.submitMessage("Tom: Hey, I'm first!");
        Thread.sleep(10);
        chain.submitMessage("Tom: Hey, I'm second also!");
        Thread.sleep(30);
        chain.submitMessage("Sarah: It's not fair!");
        chain.submitMessage("Sarah: You always will be first because it is your blockchain!");
        chain.submitMessage("Sarah: Anyway, thank you for this amazing chat.");
        Thread.sleep(30);
        chain.submitMessage("Tom: You're welcome :)");
        chain.submitMessage("Nick: Hey Tom, nice chat");
        Thread.sleep(6000);
        executor.shutdownNow();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        chain.print();
    }
}


class Block {
    private long id;
    private long timestamp = new Date().getTime();
    private int magicNumber = 0;
    private String previousHash;
    private String hash;
    private List<String> messages;
    private long timeGenerating;
    private long minerId;
    private int changeInZeros;

    public Block(long minerId, int zeros) {
        this(1, "0", minerId, zeros, Collections.emptyList());
    }

    public Block(long id, String previousHash, long minerId, int zeros, List<String> messages) {
        Random random = new Random();
        this.id = id;
        this.previousHash = previousHash;
        this.messages = messages;
        hash = StringUtil.applySha256(toString());
        while (!checkIfValid(zeros)) {
            magicNumber = random.nextInt();
            hash = StringUtil.applySha256(toString());
        }
        timeGenerating = (new Date().getTime() - timestamp)/1000;
        this.minerId = minerId;
    }

    public boolean checkIfValid(int zeros) {
        String zero = "";
        for (int i = 0; i < zeros; i++) {
            zero += "0";
        }
        return zero.equals(hash.substring(0, zeros));
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public List<String> getMessages() {
        return messages;
    }

    public long getTimeGenerating() {
        return timeGenerating;
    }

    public String getChange() {
        return changeInZeros == 0 ? "N stays the same" :
                changeInZeros < 0 ? "N was decreased to " + -changeInZeros : "N was increased to " + changeInZeros + "\n";
    }

    public void setChangeInZeros(int changeInZeros) {
        this.changeInZeros = changeInZeros;
    }

    public String toString() {
        return String.format("Block:\n" +
                "Created by miner #" + this.minerId + "\n" +
                "Id: %d\n" +
                "Timestamp: %d\n" +
                "Magic number: %d\n" +
                "Hash of the previous block:\n" +
                "%s\n" +
                "Hash of the block:\n" +
                "%s\n" +
                "Block data:\n" +
                "%s\n",
                id, timestamp, magicNumber, previousHash, hash, messages.isEmpty() ? "no messages" : String.join("\n", messages));
    }

}

class Miner implements Runnable {
    private static long numInstances = 0;
    private long id;
    private Blockchain blockchain;

    public Miner(Blockchain blockchain) {
        id = ++numInstances;
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        while (blockchain.getBlockchainSize() < 5) {
            if (blockchain.getBlockchainSize() == 0) {
                blockchain.addBlock(new Block(id, blockchain.getNumZeros()));
            } else {
                blockchain.addBlock(new Block(blockchain.getCurrentId() + 1,
                    blockchain.getLastHash(), id, blockchain.getNumZeros(), blockchain.getMessagesToAdd()));
            }
        }
    }
}

class Message {

}


class StringUtil {
    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}