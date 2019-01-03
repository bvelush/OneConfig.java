package com.oneconfig.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Demo {
    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option input = new Option("j", "json", true, "JSON file to encrypt");
        input.setRequired(true);
        options.addOption(input);

        Option keyStorePath = new Option("k", "keystore", true, "Path to the keystore containing encryption cert");
        keyStorePath.setRequired(true);
        options.addOption(keyStorePath);

        Option certName = new Option("c", "cert", true, "Name of the encryption certificate");
        certName.setRequired(true);
        options.addOption(certName);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            formatter.printHelp("JSON Encrypter", options);
            return;
        }

        System.out.println("j: " + cmd.getOptionValue("json"));
        System.out.println("j: " + cmd.getOptionValue("keystore"));
        System.out.println("j: " + cmd.getOptionValue("cert"));

    }
}
