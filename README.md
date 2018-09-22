# Howto reproduce

```
~ mkdir target
~ javac src/JDKSslReproducer.java -d target/
~ java -cp src:target JDKSslReproducer
```

This will produce something like this:

```
Exception in thread "main" java.lang.RuntimeException: Delegated task threw Exception/Error
	at sun.security.ssl.Handshaker.checkThrown(Handshaker.java:1527)
	at sun.security.ssl.SSLEngineImpl.checkTaskThrown(SSLEngineImpl.java:535)
	at sun.security.ssl.SSLEngineImpl.writeAppRecord(SSLEngineImpl.java:1214)
	at sun.security.ssl.SSLEngineImpl.wrap(SSLEngineImpl.java:1186)
	at javax.net.ssl.SSLEngine.wrap(SSLEngine.java:469)
	at JDKSslReproducer.handshake(JDKSslReproducer.java:76)
	at JDKSslReproducer.main(JDKSslReproducer.java:51)
Caused by: java.lang.NullPointerException
	at sun.net.util.IPAddressUtil.textToNumericFormatV4(IPAddressUtil.java:49)
	at sun.net.util.IPAddressUtil.isIPv4LiteralAddress(IPAddressUtil.java:241)
	at sun.security.util.HostnameChecker.isIpAddress(HostnameChecker.java:125)
	at sun.security.util.HostnameChecker.match(HostnameChecker.java:93)
	at sun.security.ssl.X509TrustManagerImpl.checkIdentity(X509TrustManagerImpl.java:455)
	at sun.security.ssl.AbstractTrustManagerWrapper.checkAdditionalTrust(SSLContextImpl.java:1068)
	at sun.security.ssl.AbstractTrustManagerWrapper.checkServerTrusted(SSLContextImpl.java:1007)
	at sun.security.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:1601)
	at sun.security.ssl.ClientHandshaker.processMessage(ClientHandshaker.java:216)
	at sun.security.ssl.Handshaker.processLoop(Handshaker.java:1052)
	at sun.security.ssl.Handshaker$1.run(Handshaker.java:992)
	at sun.security.ssl.Handshaker$1.run(Handshaker.java:989)
	at java.security.AccessController.doPrivileged(Native Method)
	at sun.security.ssl.Handshaker$DelegatedTask.run(Handshaker.java:1467)
	at JDKSslReproducer.runDelegatedTasks(JDKSslReproducer.java:131)
	at JDKSslReproducer.handshake(JDKSslReproducer.java:99)
	... 1 more
```
