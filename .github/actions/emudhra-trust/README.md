# CI containment for the eMudhra certificate incident

Following [Oracle guidance](https://ops.java/emudhra), this action removes the emSign ECC Root CA - G3 and AAA Certificate Services roots from an isolated copy of the selected JDK truststore. It identifies roots by SHA-256, preserves other anchors, and never changes the installed JDK. Removing these roots may reject legitimate services that still use them.

The action validates fixture checksums, then checks that the effective JSSE trust manager rejects all twelve compromised TLS chains. Missing, malformed or expired fixtures fail the gate. Four hostname-verified TLS handshakes check legitimate dependency/publication hosts. No credentials or HTTP requests are sent. JAVA_TOOL_OPTIONS selects the isolated store for subsequent JVMs; a Gradle init script verifies that each Gradle daemon uses it and excludes both roots.

The public certificate fixtures come from [this pinned incident-research commit](https://github.com/galadran/we-have-a-problem.com/tree/954b49d8a5735beb08889687c1d9d549e7c37ebe/chains). They cover DV/OV/EV, G3B/G3C and both issuer paths. No private keys or third-party code are included. SHA256SUMS records their exact bytes. Refresh the fixtures before their validity expires, preserving all twelve paths and provenance.

Build, integration, snapshot, release, Javadocs and CodeQL jobs use the action before Gradle. Changes to this action trigger the non-publishing release rehearsal. PR and manual rehearsal events cannot enter the push-only publishing steps.

This protects default Java TLS trust within these jobs. It does not update Node actions, Git/curl, Python, OS/container trust, consumer JDKs, custom trust managers or JVMs launched with overriding trust options. Those need independent verification. It does not change the Web3j library's API or consumer trust policy.
