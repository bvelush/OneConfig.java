package com.oneconfig.core.stores;

import java.util.HashMap;
import java.util.Map;

import com.oneconfig.core.OneConfigException;

public class EncJsonStore extends JsonStore implements IStore {

    // private class StoreConfig {
    // public String certStorePath;
    // public String certStorePwd = "";
    // public String certName = "masterkey";
    // public String certPwd = "";
    // public String storePath;
    // public String storeEncoding = "UTF-8";

    // StoreConfig(Object configObject) throws Exception {
    // // TODO: parse configObject (probably JSON string) to get all params
    // storePath = "d:\\dev\\aa.bin";
    // certStorePath = "MasterKey.p12";
    // }
    // }

    @Override
    public void init(String name, Map<String, String> configObject) {
        System.out.println(String.format("init of EncJsonStore with name '%s'", name));

        try {
            // StoreConfig storeConfig = new StoreConfig(configObject);
            // System.out.println("=====================1");
            // ObjectMapper om = new ObjectMapper();
            // System.out.println("=====================2");
            // JsonNode root = om.readTree(decryptStore(storeConfig));
            Map<String, String> subconfigObject = new HashMap<String, String>();
            // TODO: read root from env pointer //configObject.put(Const.JSON_STORE_CONTENTSTR, root);
            super.init(name, subconfigObject);
        } catch (Exception ex) {
            throw new OneConfigException("Can't initialize RSA provider");
        }
    }

    // private String decryptStore(StoreConfig storeConfig) throws Exception {
    // KeyStore keystore = KeyStore.getInstance("PKCS12");
    // System.out.println("=====================3");

    // try {
    // keystore.load(ResourceLoader.getResourceAsStream(storeConfig.certStorePath, EncJsonStore.class),
    // storeConfig.certStorePwd.toCharArray());
    // } catch (Exception ex) {
    // System.out.println(ex.getMessage());
    // }

    // System.out.println("=====================4");

    // PrivateKey key = (PrivateKey) keystore.getKey(storeConfig.certName, storeConfig.certPwd.toCharArray());
    // System.out.println("=====================5");
    // FileInputStream fis = new FileInputStream(new File(storeConfig.storePath));
    // byte[] encryptedStore = IOUtils.toByteArray(fis);
    // String decryptedStore = new String(Crypt.rsaAesDecrypt(encryptedStore, key), storeConfig.storeEncoding);
    // return decryptedStore;
    // }
}
