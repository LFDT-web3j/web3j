import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;

/** Incident containment for CI only. Does not change the installed JDK truststore. */
public class EmudhraTrust {
    private static final char[] PASSWORD = "changeit".toCharArray();
    private static final Set<String> ROOTS = Set.of(
            "86A1ECBA089C4A8D3BBE2734C612BA341D813E043CF9E8A862CD5C57A36BBE6B",
            "D7A7A0FB5D7E2731D771E9484EBCDEF71D5F0C3E0A2948782BC83EE0EA699EF4");

    private static String fingerprint(java.security.cert.Certificate certificate) throws Exception {
        return HexFormat.of().withUpperCase().formatHex(
                MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
    }

    private static void prepare(Path output) throws Exception {
        Path source = Path.of(System.getProperty("java.home"), "lib", "security", "cacerts");
        KeyStore original = KeyStore.getInstance(source.toFile(), PASSWORD);
        KeyStore isolated = KeyStore.getInstance("JKS");
        isolated.load(null, PASSWORD);
        int removed = 0;
        for (String alias : Collections.list(original.aliases())) {
            java.security.cert.Certificate certificate = original.getCertificate(alias);
            if (ROOTS.contains(fingerprint(certificate))) {
                System.out.println("Excluded root: " + alias);
                removed++;
            } else {
                if (!original.isCertificateEntry(alias)) {
                    throw new IllegalStateException("Unexpected non-certificate entry: " + alias);
                }
                isolated.setCertificateEntry(alias, certificate);
            }
        }
        if (isolated.size() == 0 || isolated.size() != original.size() - removed) {
            throw new IllegalStateException("Unexpected truststore contents");
        }
        try (var stream = Files.newOutputStream(output, StandardOpenOption.CREATE_NEW)) {
            isolated.store(stream, PASSWORD);
        }
        System.out.println("Prepared " + output + "; preserved " + isolated.size()
                + " trust anchors; removed " + removed);
    }

    private static void verify(Path fixtures) throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        X509TrustManager manager = (X509TrustManager) Arrays.stream(factory.getTrustManagers())
                .filter(t -> t instanceof X509TrustManager).findFirst().orElseThrow();
        for (X509Certificate anchor : manager.getAcceptedIssuers()) {
            if (ROOTS.contains(fingerprint(anchor))) {
                throw new IllegalStateException("Affected root still trusted: "
                        + anchor.getSubjectX500Principal());
            }
        }
        List<Path> files;
        try (var stream = Files.list(fixtures)) {
            files = stream.filter(p -> p.toString().endsWith(".pem")).sorted().toList();
        }
        if (files.size() != 12) throw new IllegalStateException("Expected all 12 TLS chain fixtures");
        for (Path file : files) {
            X509Certificate[] chain;
            try (var input = Files.newInputStream(file)) {
                chain = CertificateFactory.getInstance("X.509")
                        .generateCertificates(input).toArray(X509Certificate[]::new);
            }
            if (chain.length < 2) throw new IllegalStateException("Incomplete fixture: " + file);
            // Expired or malformed fixtures must fail the gate, never count as protection.
            for (int i = 0; i < chain.length; i++) {
                chain[i].checkValidity();
                if (i + 1 < chain.length) chain[i].verify(chain[i + 1].getPublicKey());
            }
            boolean rejected = false;
            try {
                manager.checkServerTrusted(chain, "UNKNOWN");
            } catch (CertificateException expected) {
                rejected = true;
            }
            if (!rejected) throw new IllegalStateException("Compromised chain accepted: " + file);
            System.out.println("Rejected " + file.getFileName());
        }
        System.out.println("PASS: both affected roots absent; all 12 compromised TLS chains rejected by "
                + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
    }

    private static void probe(String[] hosts) throws Exception {
        for (String host : hosts) {
            try (Socket tcp = new Socket()) {
                tcp.connect(new InetSocketAddress(host, 443), 15000);
                tcp.setSoTimeout(15000);
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                try (SSLSocket socket = (SSLSocket) factory.createSocket(tcp, host, 443, true)) {
                    SSLParameters parameters = socket.getSSLParameters();
                    parameters.setEndpointIdentificationAlgorithm("HTTPS");
                    socket.setSSLParameters(parameters);
                    socket.startHandshake();
                    System.out.println("PASS: hostname-verified TLS to " + host);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("prepare OUTPUT | verify FIXTURES | probe HOST...");
        switch (args[0]) {
            case "prepare" -> prepare(Path.of(args[1]));
            case "verify" -> verify(Path.of(args[1]));
            case "probe" -> probe(Arrays.copyOfRange(args, 1, args.length));
            default -> throw new IllegalArgumentException("Unknown mode");
        }
    }
}
