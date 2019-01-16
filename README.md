# Installation instructions

## Prerequisites
OneConfig requires two keystores in p12 format:
- Secret owner keystore. This store has a public key of the encryption certificate, and is essentially a key to create a secrets file (JSS, JSON Secret Store). For test purposes, the example store is provided at ./oneconfig.core/src/test/resources/CertStores/SecOwnerKeyStore.p12
- Deployment keystore. This store has a private key of the encryption certficate, and should be securely deployed to every machine in a datacenter where the product using OneConfig, is deployed. For test purposes, test store is at ./oneconfig.core/src/test/resources/CertStores/DeploymentKeyStore.p12



## Configuration

To build solution, some configuration needs to be provided in order for unit tests pass.

For test purposes, we provide ./oneconfig.core/src/test/resources/TestStores/config.json (non-secret config store, a default config store) and a secret store at ./oneconfig/oneconfig.core/src/test/resources/TestStores/testsecrets.jss

Configuration is done through the INIT section of the config file ./oneconfig.core/src/test/resources/TestStores/config.json.

- storePath -- a path to the secret store file. Path could be provided in three different ways:

    - as an absolute path (/var/oneconfig/config.json, as an example)
    - as a path relative to the classPath (resources/TestStores/config.json, for example)
    - as a reference to the environment varable that has absolute path the file (env:ONECFG_STOREPATH, for example). In this case, the envvar must exist and have a valid absolute path to the secret store
- deployment keystore path. Same three options
- encryption cert name within the deployment keystore

For the test purposes, the references to Secret Store and to Deployment Keystore are provided as envvar references, so two environment variables have to be defined:

- ONECFG_DEPLKEYPATH -- to the absolute location of the deployment keystore (test deployment keystore is at ./oneconfig.core/src/test/resources/CertStores/DeploymentKeyStore.p12.
- ONECFG_STOREPATH -- to the absolute location of the secret store (test secret store is at ./oneconfig.core/src/test/resources/TestStores/testsecrets.jss)

OneConfig implements a Deployment Sensor, that is configured at the "sensors" section of the ./oneconfig.core/src/test/resources/config.json. This sensor reads the value of another environment variable ONECFG_DEPLOYMENT to define the DEV, TEST or PROD deployment. For test purposes, this envvar has to be set to TEST

## Generating new Secret Store
OneConfig comes with a simple version of the utility to manage key stores. This utility currently can only encrypt the json file with the provided SecOwnerKeyStore. usage:

java -jar jssutil\target\jssutil-1.0-SNAPSHOT-pkg.jar
this will give you a small help of the commandline options.

An example of usage:
java -jar jssutil\target\jssutil-1.0-SNAPSHOT-pkg.jar -o oneconfig.core\src\test\resources\TestStores\testsecrets11.jss -j jssutil\src\test\resources\testsecrets.json -k oneconfig.core\src\test\resources\CertStores\SecOwnerKeyStore.p12

This command creates a testsecrets11.jss file by encrypting testsecrets.json with the "deployment_enc" key provided in the SecOwnwerKeyStore.p12
