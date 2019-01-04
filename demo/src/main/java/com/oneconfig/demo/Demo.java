package com.oneconfig.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;

import com.oneconfig.utils.crypt.Crypt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

public class Demo {
    public static final String CERTNAME = "masterkey";
    public static final String CERTPWD = "";

    public static void main(String[] args) {
        CommandLine cmd = parseCmdOptions(args);
        if (cmd != null) {

            try {
                String jsonPath = cmd.getOptionValue("json");
                String keystorePath = cmd.getOptionValue("keystore");
                String outPath = cmd.getOptionValue("out");

                System.out.println(String.format("Reading the JSON file '%s'...", jsonPath));
                Path path = Paths.get(cmd.getOptionValue("json"));
                byte[] data = Files.readAllBytes(path);

                System.out.println(String.format("Opening the key store at '%s'...", keystorePath));
                KeyStore keystore = Crypt.loadKeyStoreSysPath(keystorePath, "");

                System.out.println(String.format("Reading the key '%s'...", CERTNAME));
                PrivateKey key = Crypt.getPrivateKey(keystore, CERTNAME, CERTPWD);

                System.out.println("Encrypting the file...");
                byte[] encryptedData = Crypt.rsaAesEncrypt(data, key);

                System.out.println(String.format("Writing encrypted file to '%s'", outPath));
                FileOutputStream outputFile = new FileOutputStream(new File(outPath));
                IOUtils.write(encryptedData, outputFile);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }

        }
    }

    // returns null in case of command line arguments mistake
    private static CommandLine parseCmdOptions(String[] args) {
        Options options = new Options();

        Option input = new Option("j", "json", true, "JSON file to encrypt");
        input.setRequired(true);
        options.addOption(input);

        Option keyStorePath = new Option("k", "keystore", true, "Path to the keystore containing encryption cert");
        keyStorePath.setRequired(true);
        options.addOption(keyStorePath);

        Option certName = new Option("o", "out", true, "Name of the encrypted file");
        certName.setRequired(true);
        options.addOption(certName);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            formatter.printHelp("JSON Encrypter", options);
            return null;
        }
    }
}
