# Installation instructions

## Configure BouncyCastle as crypto provider in Java (required)

* open file $JAVA_HOME/jre/lib/security/java.security
* add the following line:   security.provider.N=org.bouncycastle.jce.provider.BouncyCastleProvider
* put BouncyCastle file 'bcpkix-jdk15on-1.60' to $JAVA_HOME/jre/lib/ext. On windows, you can find it in your user profie, %USERPROFILE%\\.m2\repository\org\bouncycastle\bcpkix-jdk15on\1.60
