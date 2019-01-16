RootStorePrivate.p12 -- a store with the private key of the root trust cert. It is not directly used in OneConfig, but is used in the process of creation of the signing cert.
RootStore.p12 -- a store with the public key of the root trust cert. Is used by OneConfig to validate the signing certificate
SecOwnrtKeyStore.p12 -- contains the private key for "signcert" that is used for signing, and public key for "cryptcert" used for encryption
