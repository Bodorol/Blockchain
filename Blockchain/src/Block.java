import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
