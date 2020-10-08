import java.security.*;

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