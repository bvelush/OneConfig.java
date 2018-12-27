package com.oneconfig.utils.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

public class CryptTest {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String MASTERKEYSTORE = "MasterKey.p12";
    private static final String MASTERCERTNAME = "masterkey";
    private static final String MASTERPUBKEYSTORE = "MasterKeyPub.p12";

    private static final String MASTERKEYSTOREPWD = ""; // I would like the master cert pwd to be ALWAYS empty. Reason is that the cert security must
                                                        // be ensured not with the password, but with the secure distribution of the masterkey file
    private static final String MASTERCERTPWD = "";

    @Test
    public void testPrivareEncryptPublicDecrypt() throws Exception {
        String content = (new Date()).toString();

        // encrypting content
        byte[] contentBits = content.getBytes();
        KeyStore priKs = Crypt.loadKeyStore(MASTERKEYSTORE, MASTERKEYSTOREPWD);
        Key privKey = Crypt.getPrivateKey(priKs, MASTERCERTNAME, MASTERCERTPWD);
        byte[] cipherBits = Crypt.rsaSimpleEncrypt(contentBits, privKey);

        // decrypting content
        KeyStore pubKs = Crypt.loadKeyStore(MASTERPUBKEYSTORE, MASTERKEYSTOREPWD);
        Key pubKey = Crypt.getPublicKey(pubKs, MASTERCERTNAME, MASTERCERTPWD);
        byte[] plainBits = Crypt.rsaSimpleDecrypt(cipherBits, pubKey);

        assertEquals(content, new String(plainBits));
    }

    @Test
    public void testPublicEncryptPrivateDecrypt() throws Exception { // usually used to sign the hash, but could be used for many purposes
        String content = (new Date()).toString();

        // encrypting content
        byte[] contentBits = content.getBytes();
        KeyStore pubKs = Crypt.loadKeyStore(MASTERPUBKEYSTORE, MASTERKEYSTOREPWD);
        Key pubKey = Crypt.getPublicKey(pubKs, MASTERCERTNAME, MASTERCERTPWD);

        byte[] cipherBits = Crypt.rsaSimpleEncrypt(contentBits, pubKey);

        // decrypting content
        KeyStore priKs = Crypt.loadKeyStore(MASTERKEYSTORE, MASTERKEYSTOREPWD);
        Key privKey = Crypt.getPrivateKey(priKs, MASTERCERTNAME, MASTERCERTPWD);
        byte[] plainBits = Crypt.rsaSimpleDecrypt(cipherBits, privKey);

        assertEquals(content, new String(plainBits));
    }

    @Test
    public void testKeyWrapUnwrap() throws Exception {
        KeyStore priKs = Crypt.loadKeyStore(MASTERKEYSTORE, MASTERKEYSTOREPWD);
        Key privKey = Crypt.getPrivateKey(priKs, MASTERCERTNAME, MASTERCERTPWD);

        KeyStore pubKs = Crypt.loadKeyStore(MASTERPUBKEYSTORE, MASTERKEYSTOREPWD);
        Key pubKey = Crypt.getPublicKey(pubKs, MASTERCERTNAME, MASTERCERTPWD);

        byte[] aeskey = Crypt.getAESRandomKey();
        byte[] rsakeyen = Crypt.rsaWrapKey(aeskey, privKey);
        byte[] rsakeydec = Crypt.rsaUnwrapKey(rsakeyen, pubKey);
        assertTrue(Arrays.equals(aeskey, rsakeydec));
    }

    @Test
    public void testRsaAesEncrDecr() {
        int testDataSize = 1024 * 1024 * 20; // 20 MB

        KeyStore priKs = Crypt.loadKeyStore(MASTERKEYSTORE, MASTERKEYSTOREPWD);
        Key privKey = Crypt.getPrivateKey(priKs, MASTERCERTNAME, MASTERCERTPWD);

        byte[] content = new byte[testDataSize];
        new Random().nextBytes(content); // fill the test content with random bytes
        byte[] encryptedContent = Crypt.rsaAesEncrypt(content, privKey);
        // example encrypting the file: aa.bin -> aa.enc

        // byte[] content = IOUtils.toByteArray(ResourceLoader.getResourceAsStream("aa.bin", CryptTest.class));
        // FileUtils.writeByteArrayToFile(new File("d:\\aa.enc"), Crypt.rsaAesEncrypt(content, privKey));

        KeyStore pubKs = Crypt.loadKeyStore(MASTERPUBKEYSTORE, MASTERKEYSTOREPWD);
        Key pubKey = Crypt.getPublicKey(pubKs, MASTERCERTNAME, MASTERCERTPWD);

        byte[] decryptedContent = Crypt.rsaAesDecrypt(encryptedContent, pubKey);
        assertTrue(Arrays.equals(content, decryptedContent));

        // example decrypting the file: aa.enc-> aa.bin
        // byte[] content = Crypt.rsaAesDecrypt(Files.readAllBytes(Paths.get("d:\\aa.enc")), pubKey);
        // FileUtils.writeByteArrayToFile(new File("d:\\aa.bin"), content);
    }

}
