import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class Blockchain {
    private List<Block> blocks = new ArrayList<>();

    public void createBlock() {
        if (blocks.size() == 0) {
            blocks.add(new Block());
        } else {
            Block lastBlock = blocks.get(blocks.size() - 1);
            blocks.add(new Block(lastBlock.getId() + 1, lastBlock.getHash()));
        }
    }

    public void createMultipleBlocks(int amount) {
        for (int i = 0; i < amount; i++) {
            createBlock();
        }
    }

    public void print() {
        blocks.forEach(System.out::println);
    }

    public static void main(String[] args) {
        Blockchain chain = new Blockchain();
        chain.createMultipleBlocks(10);
        chain.print();
    }
}


class Block {
    private long id;
    private long timestamp = new Date().getTime();
    private String previousHash;
    private String hash;

    public Block() {
        this(1, "0");
    }

    public Block(long id, String previousHash) {
        this.id = id;
        this.previousHash = previousHash;
        hash = StringUtil.applySha256(toString());
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String toString() {
        return String.format("Block:\n" +
                "Id: %d\n" +
                "Timestamp: %d\n" +
                "Hash of the previous block:\n" +
                "%s\n" +
                "Hash of the block:\n" +
                "%s\n", id, timestamp, previousHash, hash);
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
