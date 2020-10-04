import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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


class Block {
    private long id;
    private long timestamp = new Date().getTime();
    private int magicNumber = 0;
    private String previousHash;
    private String hash;
    private List<Message> messages;
    private long timeGenerating;
    private User miner;
    private long minerId;
    private int changeInZeros;

    public Block(User miner, int zeros) {
        this(1, "0", miner, zeros, Collections.emptyList());
    }

    public Block(long id, String previousHash, User miner, int zeros, List<Message> messages) {
        Random random = new Random();
        this.id = id;
        this.previousHash = previousHash;
        this.miner = miner;
        this.minerId = miner.getId();
        this.messages = messages;
        hash = StringUtil.applySha256(toString());
        while (!checkIfValid(zeros)) {
            magicNumber = random.nextInt();
            hash = StringUtil.applySha256(toString());
        }
        timeGenerating = (new Date().getTime() - timestamp)/1000;

    }

    public boolean checkIfValid(int zeros) {
        String zero = "";
        for (int i = 0; i < zeros; i++) {
            zero += "0";
        }
        return zero.equals(hash.substring(0, zeros))
                && messages.stream().allMatch(message -> message.verifySignature(message.getUser()));
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getTimeGenerating() {
        return timeGenerating;
    }

    public User getMiner() {
        return miner;
    }

    public String getChange() {
        return changeInZeros == 0 ? "N stays the same" :
                changeInZeros < 0 ? "N was decreased to " + -changeInZeros : "N was increased to " + changeInZeros + "\n";
    }

    public void setChangeInZeros(int changeInZeros) {
        this.changeInZeros = changeInZeros;
    }

    public String toString() {
        return String.format("Block:\n" + "" +
                "Created by: %s\n" +
                "%s was awarded 100 VC\n" +
                "Id: %d\n" +
                "Timestamp: %d\n" +
                "Magic number: %d\n" +
                "Hash of the previous block:\n" +
                "%s\n" +
                "Hash of the block:\n" +
                "%s\n" +
                "Block data:\n" +
                "%s\n",
                miner.getUsername(), miner.getUsername(), id, timestamp, magicNumber, previousHash, hash, messages.isEmpty() ? "no transactions" :
                        String.join("\n", messages.stream().map(Message::getText).collect(Collectors.toList())));
    }

}

class User implements Runnable {
    private static long instanceCount = 0;
    private long id;
    private String username;
    private volatile long coins = 100L;
    private Blockchain blockchain;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public User(String username, Blockchain blockchain) {
        id = ++instanceCount;
        this.username = username;
        this.blockchain = blockchain;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String text) {
        try {
            blockchain.submitMessage(new Message(this, text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCoins(User user, long amount) {
        if (coins >= amount) {
            coins -= amount;
            user.receiveCoins(amount);
            sendMessage(username + " sent " + amount + " VC to " + user.getUsername());
        }
    }

    public void receiveCoins(long coins) {
        this.coins += coins;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    @Override
    public void run() {
        while (blockchain.getBlockchainSize() < 6) {
            if (blockchain.getBlockchainSize() == 0) {
                blockchain.addBlock(new Block(this, blockchain.getNumZeros()));
            } else {
                blockchain.addBlock(new Block(blockchain.getCurrentId() + 1,
                        blockchain.getLastHash(), this, blockchain.getNumZeros(), blockchain.getMessagesToAdd()));
            }
        }
    }
}

class Message {
    private static long instanceCount = 0;
    private long id;
    private User user;
    private String text;
    private byte[] signature;

    public Message(User user, String text) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        id = ++instanceCount;
        this.user = user;
        this.text = text;
        signature = sign(user);
    }

    public byte[] sign(User user) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(user.getPrivateKey());
        rsa.update((text + "\nId: " + id).getBytes());
        return rsa.sign();
    }

    public boolean verifySignature(User user){
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(user.getPublicKey());
            sig.update((text + "\nId: " + id).getBytes());
            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public long getId() {
        return id;
    }
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