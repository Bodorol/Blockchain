import java.security.MessageDigest;
import java.util.*;



class Blockchain {
    private List<Block> blocks = new ArrayList<>();

    public void createBlock(int zeros) {
        if (blocks.size() == 0) {
            blocks.add(new Block(zeros));
        } else {
            Block lastBlock = blocks.get(blocks.size() - 1);
            blocks.add(new Block(lastBlock.getId() + 1, lastBlock.getHash(), zeros));
        }
    }

    public void createMultipleBlocks(int amount, int zeros) {
        for (int i = 0; i < amount; i++) {
            createBlock(zeros);
        }
    }

    public void print() {
        blocks.forEach(block -> System.out.println(block + "Block was generating for " + block.getTimeGenerating() + " seconds\n"));
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Blockchain chain = new Blockchain();
        int zeros = -1;
        do {
            System.out.println("Enter how many zeros the hash must start with: ");
            zeros = input.nextInt();
        } while (zeros < 0 || zeros > 64);
        chain.createMultipleBlocks(6, zeros);
        chain.print();
    }
}


class Block {
    private long id;
    private long timestamp = new Date().getTime();
    private int magicNumber = 0;
    private String previousHash;
    private String hash;
    private long timeGenerating;

    public Block(int zeros) {
        this(1, "0", zeros);
    }

    public Block(long id, String previousHash, int zeros) {
        Random random = new Random();
        this.id = id;
        this.previousHash = previousHash;
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
        return zero.equals(hash.substring(0, zeros));
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public long getTimeGenerating() {
        return timeGenerating;
    }

    public String toString() {
        return String.format("Block:\n" +
                "Id: %d\n" +
                "Timestamp: %d\n" +
                "Magic number: %d\n" +
                "Hash of the previous block:\n" +
                "%s\n" +
                "Hash of the block:\n" +
                "%s\n", id, timestamp, magicNumber, previousHash, hash);
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