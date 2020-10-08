import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

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