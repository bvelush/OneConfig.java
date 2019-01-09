package com.oneconfig.core.stores;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import com.oneconfig.core.OneConfigException;
import com.oneconfig.utils.common.ResourceLoader;
import com.oneconfig.utils.crypt.Crypt;

import org.apache.commons.io.IOUtils;

/**
 * EncJsonStore init expects the following keys in the configObject map:
 *
 * -- keystorePath -- path to the certificate store that has the decription private key
 *
 * -- cryptcert -- name of the decription cert in the store
 *
 * -- storePath -- path to the encrypted file containing secrets
 */
public class EncJsonStore extends JsonStore implements IStore {
    public static final String PWD = "";
    public static final String KEYSTOREPATH = "keystorePath";
    public static final String CRYPTCERT = "cryptcert";
    public static final String STOREPATH = "storePath";

    @Override
    public void init(String name, Map<String, String> configObject) {
        try {
            Map<String, String> subconfigObject = new HashMap<String, String>();
            String decryptedStore = decryptStore(configObject);
            subconfigObject.put(JsonStore.JSON_STORE_CONTENTSTR, decryptedStore);
            super.init(name, subconfigObject);
        } catch (Exception ex) {
            throw new OneConfigException(String.format("Can't initialize store '%s'", name), ex);
        }
    }

    private String decryptStore(Map<String, String> configObject) throws Exception {
        KeyStore keystore = Crypt.loadKeyStore(configObject.get(KEYSTOREPATH), PWD);
        PrivateKey key = Crypt.getPrivateKey(keystore, configObject.get(CRYPTCERT), PWD);
        byte[] encryptedStore = IOUtils.toByteArray(ResourceLoader.getResourceAsStream(configObject.get(STOREPATH)));
        String decryptedStore = new String(Crypt.rsaAesDecrypt(encryptedStore, key), "UTF-8");
        return decryptedStore;
    }
}
