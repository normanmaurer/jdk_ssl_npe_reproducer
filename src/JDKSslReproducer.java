import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public final class JDKSslReproducer {

    public static void main(String[] args) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(JDKSslReproducer.class.getResourceAsStream("test.p12"), "test".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "test".toCharArray());
        SSLContext serverCtx = SSLContext.getInstance("TLS");
        serverCtx.init(kmf.getKeyManagers(), null, null);
        SSLEngine serverEngine = serverCtx.createSSLEngine("localhost", -1);
        serverEngine.setUseClientMode(false);

        SSLContext clientCtx = SSLContext.getInstance("TLS");
        clientCtx.init(null, new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                        // NOOP
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                        // NOOP
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        }, null);

        SSLEngine clientEngine = clientCtx.createSSLEngine();
        clientEngine.setUseClientMode(true);

        SSLParameters sslParameters = clientEngine.getSSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
        clientEngine.setSSLParameters(sslParameters);
        handshake(clientEngine, serverEngine);
    }

    private static void handshake(SSLEngine clientEngine, SSLEngine serverEngine) throws SSLException{
        ByteBuffer cTOs = ByteBuffer.allocate(clientEngine.getSession().getPacketBufferSize());
        ByteBuffer sTOc = ByteBuffer.allocate(serverEngine.getSession().getPacketBufferSize());

        ByteBuffer serverAppReadBuffer = ByteBuffer.allocate(
                serverEngine.getSession().getApplicationBufferSize());
        ByteBuffer clientAppReadBuffer = ByteBuffer.allocate(
                clientEngine.getSession().getApplicationBufferSize());

        clientEngine.beginHandshake();
        serverEngine.beginHandshake();

        ByteBuffer empty = ByteBuffer.allocate(0);

        SSLEngineResult clientResult;
        SSLEngineResult serverResult;

        boolean clientHandshakeFinished = false;
        boolean serverHandshakeFinished = false;

        do {
            if (!clientHandshakeFinished) {
                clientResult = clientEngine.wrap(empty, cTOs);
                runDelegatedTasks(clientResult, clientEngine);

                if (isHandshakeFinished(clientResult)) {
                    clientHandshakeFinished = true;
                }
            }

            if (!serverHandshakeFinished) {
                serverResult = serverEngine.wrap(empty, sTOc);
                runDelegatedTasks(serverResult, serverEngine);

                if (isHandshakeFinished(serverResult)) {
                    serverHandshakeFinished = true;
                }
            }

            cTOs.flip();
            sTOc.flip();

            if (!clientHandshakeFinished) {
                clientResult = clientEngine.unwrap(sTOc, clientAppReadBuffer);

                runDelegatedTasks(clientResult, clientEngine);

                if (isHandshakeFinished(clientResult)) {
                    clientHandshakeFinished = true;
                }
            }

            if (!serverHandshakeFinished) {
                serverResult = serverEngine.unwrap(cTOs, serverAppReadBuffer);
                runDelegatedTasks(serverResult, serverEngine);

                if (isHandshakeFinished(serverResult)) {
                    serverHandshakeFinished = true;
                }
            }

            sTOc.compact();
            cTOs.compact();
        } while (!clientHandshakeFinished || !serverHandshakeFinished);
    }

    private static boolean isHandshakeFinished(SSLEngineResult result) {
        return result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED;
    }

    private static void runDelegatedTasks(SSLEngineResult result, SSLEngine engine) {
        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            for (;;) {
                Runnable task = engine.getDelegatedTask();
                if (task == null) {
                    break;
                }
                task.run();
            }
        }
    }
}
