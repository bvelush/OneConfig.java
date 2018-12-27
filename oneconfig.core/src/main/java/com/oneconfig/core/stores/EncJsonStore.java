package com.oneconfig.core.stores;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneconfig.core.OneConfigException;
import com.oneconfig.utils.common.ResourceLoader;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.KeyTransRecipientInformation;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncJsonStore extends JsonStore implements IStore {

    @Override
    public void init(String name, Object configObject) {
        System.out.println(String.format("init of EncJsonStore with name '%s'", name));

        try {
            String certStoreName = (String) configObject;

            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");

            char[] keystorePassword = "".toCharArray();
            char[] keyPassword = "".toCharArray();

            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(ResourceLoader.getResourceAsStream(certStoreName, EncJsonStore.class), keystorePassword);
            PrivateKey key = (PrivateKey) keystore.getKey("masterkey", keyPassword);

            ObjectMapper om = new ObjectMapper();
            byte[] encryptedPackage = IOUtils.toByteArray(ResourceLoader.getResourceAsStream("SecurePackage.bin", EncJsonStore.class));
            String decryptedPackage = decryptData(encryptedPackage, key);
            JsonNode root = om.readTree(decryptedPackage);
            super.init(name, root);
        } catch (Exception ex) {
            throw new OneConfigException("Can't initialize RSA provider");
        }
    }

    private static String decryptData(byte[] encryptedData, PrivateKey decryptionKey) throws CMSException {
        try {
            CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);

            Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
            KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
            JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(decryptionKey);

            return new String(recipientInfo.getContent(recipient));
        } catch (Exception ex) {
            throw new OneConfigException("DecryptionError", ex);
        }
    }

}
