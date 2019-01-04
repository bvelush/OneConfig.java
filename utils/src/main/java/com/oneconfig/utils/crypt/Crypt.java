package com.oneconfig.utils.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import com.oneconfig.utils.common.ResourceLoader;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Crypt {
    public static final String KEYSTORE_INSTANCE = "PKCS12";
    public static final String SIGN_ALG = "SHA256WithRSA";
    public static final String KEYSTOREFILE = "MasterKey.p12";
    public static final String KEYSTOREPWD = ""; // no password. It doesn't make sense. We protect the keystore file physically, not with password
    public static final String CERTNAME = "masterkey";
    public static final String CERTPWD = ""; // no password. We protect cert by physically protecting the key file

    public static final int AESKEYLENGTH = 256;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyStore loadKeyStore(String keystorePath, String keystorePwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_INSTANCE);
            keyStore.load(ResourceLoader.getResourceAsStream(KEYSTOREFILE, Crypt.class), KEYSTOREPWD.toCharArray());
            return keyStore;
        } catch (Exception ex) {
            throw new CryptException(String.format("Can't load keystore '%s'", keystorePath), ex);
        }
    }

    // TODO: get rid of loadKeyStore, and add to ResourceLoader the discoverPath call.
    public static KeyStore loadKeyStoreSysPath(String keystorePath, String keystorePwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_INSTANCE);
            keyStore.load(new FileInputStream(new File(keystorePath)), KEYSTOREPWD.toCharArray());
            return keyStore;
        } catch (Exception ex) {
            throw new CryptException(String.format("Can't load keystore '%s'", keystorePath), ex);
        }
    }

    public static PrivateKey getPrivateKey(KeyStore store, String certName, String certPwd) {
        PrivateKey result = null;
        try {
            Key key = store.getKey(certName, certPwd.toCharArray());
            Certificate cert = store.getCertificate(certName);
            PublicKey pubKey = cert.getPublicKey();
            KeyPair keyPair = new KeyPair(pubKey, (PrivateKey) key); // if the certificate does not have private key, the cast exception will occur
            return keyPair.getPrivate();
        } catch (Exception ex) {
            throw new CryptException(String.format("Can't get private key for certificate '%s'", certName), ex);
        }
    }

    public static PublicKey getPublicKey(KeyStore store, String certName, String certPwd) {
        try {
            Certificate cert = store.getCertificate(certName);
            return cert.getPublicKey();
        } catch (Exception ex) {
            throw new CryptException(String.format("Can't get public key for certificate '%s'", certName), ex);
        }
    }

    public static byte[] rsaSimpleEncrypt(byte[] content, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            SecureRandom random = new SecureRandom();
            cipher.init(Cipher.ENCRYPT_MODE, key, random);
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Encryption failed", ex);
        }
    }

    public static byte[] rsaSimpleDecrypt(byte[] content, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Decryption failed", ex);
        }
    }

    public static byte[] aesEncrypt(byte[] content, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            SecureRandom random = new SecureRandom();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), random);
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Encryption failed", ex);
        }
    }

    public static byte[] aesDecrypt(byte[] content, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Decryption failed", ex);
        }
    }

    public static byte[] rsaWrapKey(byte[] content, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA-512AndMGF1Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key, new OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, PSource.PSpecified.DEFAULT));
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Encryption failed", ex);
        }
    }

    public static byte[] rsaUnwrapKey(byte[] content, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA-512AndMGF1Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key, new OAEPParameterSpec("SHA-512", "MGF1", MGF1ParameterSpec.SHA512, PSource.PSpecified.DEFAULT));
            return cipher.doFinal(content);
        } catch (Exception ex) {
            throw new CryptException("Decryption failed", ex);
        }
    }

    public static byte[] getAESRandomKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom rnd = new SecureRandom();
            keygen.init(AESKEYLENGTH, rnd);
            return keygen.generateKey().getEncoded();
        } catch (Exception ex) {
            throw new CryptException("Can't generate the AES key", ex);
        }
    }

    public static byte[] rsaAesEncrypt(byte[] content, Key rsaKey) {
        try {
            // encrypt content with random key
            byte[] aeskey = Crypt.getAESRandomKey();
            byte[] encContent = Crypt.aesEncrypt(content, aeskey);

            // wrap random key with RSA
            byte[] wrappedkey = Crypt.rsaWrapKey(aeskey, rsaKey);
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                try (DataOutputStream dos = new DataOutputStream(os)) {
                    dos.writeInt(wrappedkey.length); // save wrapped key length
                    dos.write(wrappedkey); // save wrapped key
                    dos.write(encContent); // save encrypted content
                    dos.flush();

                    return os.toByteArray();
                }
            }
        } catch (Exception ex) {
            throw new CryptException("Can't RSA-AES encrypt", ex);
        }
    }

    public static byte[] rsaAesDecrypt(byte[] encryptedBlob, Key rsaKey) {
        try {
            try (ByteArrayInputStream is = new ByteArrayInputStream(encryptedBlob)) {
                try (DataInputStream dis = new DataInputStream(is)) {
                    int wrappedKeyLength = dis.readInt(); // read wrapped key length
                    byte[] wrappedKey = new byte[wrappedKeyLength];
                    dis.read(wrappedKey, 0, wrappedKeyLength); // read wrapped key to a buffer

                    byte[] aeskey = Crypt.rsaUnwrapKey(wrappedKey, rsaKey); // unwrap aes key
                    byte[] encryptedContent = readBytes(dis);

                    return Crypt.aesDecrypt(encryptedContent, aeskey);
                }
            }
        } catch (Exception ex) {
            throw new CryptException("Can't RSA-AES decrypt", ex);
        }
    }

    private static byte[] readBytes(InputStream stream) throws IOException {
        if (stream == null) return new byte[] {};
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean error = false;
        try {
            int numRead = 0;
            while ((numRead = stream.read(buffer)) > -1) {
                output.write(buffer, 0, numRead);
            }
        } catch (IOException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } catch (RuntimeException e) {
            error = true; // this error should be thrown, even if there is an error closing stream
            throw e;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                if (!error) throw e;
            }
        }
        output.flush();
        return output.toByteArray();
    }
}
