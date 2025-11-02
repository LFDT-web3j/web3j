/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.crypto;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.web3j.crypto.Hash.sha256;
import static org.web3j.crypto.SampleKeys.CREDENTIALS;
import static org.web3j.crypto.SampleKeys.KEY_PAIR;
import static org.web3j.crypto.SampleKeys.MNEMONIC;
import static org.web3j.crypto.SampleKeys.PASSWORD;
import static org.web3j.crypto.WalletUtils.isValidAddress;
import static org.web3j.crypto.WalletUtils.isValidPrivateKey;

class WalletUtilsTest {

    private File tempDir;

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @BeforeEach
    void setUp() throws Exception {
        tempDir = createTempDir();
    }

    @AfterEach
    void tearDown() throws Exception {
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        tempDir.delete();
    }

    @Test
    void testGenerateBip39Wallets() throws Exception {
        Bip39Wallet wallet = WalletUtils.generateBip39Wallet(PASSWORD, tempDir);
        byte[] seed = MnemonicUtils.generateSeed(wallet.getMnemonic(), PASSWORD);
        Credentials credentials = Credentials.create(ECKeyPair.create(sha256(seed)));

        assertEquals(credentials, WalletUtils.loadBip39Credentials(PASSWORD, wallet.getMnemonic()));
    }

    @Test
    void testGenerateBip39WalletFromMnemonic() throws Exception {
        Bip39Wallet wallet =
                WalletUtils.generateBip39WalletFromMnemonic(PASSWORD, MNEMONIC, tempDir);
        byte[] seed = MnemonicUtils.generateSeed(wallet.getMnemonic(), PASSWORD);
        Credentials credentials = Credentials.create(ECKeyPair.create(sha256(seed)));

        assertEquals(credentials, WalletUtils.loadBip39Credentials(PASSWORD, wallet.getMnemonic()));
    }

    @Test
    void testGenerateFullNewWalletFile() throws Exception {
        String fileName = WalletUtils.generateFullNewWalletFile(PASSWORD, tempDir);
        testGeneratedNewWalletFile(fileName);
    }

    @Test
    void testGenerateNewWalletFile() throws Exception {
        String fileName = WalletUtils.generateNewWalletFile(PASSWORD, tempDir);
        testGeneratedNewWalletFile(fileName);
    }

    @Test
    void testGenerateLightNewWalletFile() throws Exception {
        String fileName = WalletUtils.generateLightNewWalletFile(PASSWORD, tempDir);
        testGeneratedNewWalletFile(fileName);
    }

    private void testGeneratedNewWalletFile(String fileName) throws Exception {
        WalletUtils.loadCredentials(PASSWORD, new File(tempDir, fileName));
    }

    @Test
    void testGenerateFullWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(PASSWORD, KEY_PAIR, tempDir, true);
        testGenerateWalletFile(fileName);
    }

    @Test
    void testGenerateLightWalletFile() throws Exception {
        String fileName = WalletUtils.generateWalletFile(PASSWORD, KEY_PAIR, tempDir, false);
        testGenerateWalletFile(fileName);
    }

    private void testGenerateWalletFile(String fileName) throws Exception {
        Credentials credentials =
                WalletUtils.loadCredentials(PASSWORD, new File(tempDir, fileName));

        assertEquals(credentials, (CREDENTIALS));
    }

    @Test
    void testLoadCredentialsFromFile() throws Exception {
        Credentials credentials =
                WalletUtils.loadCredentials(
                        PASSWORD,
                        new File(
                                WalletUtilsTest.class
                                        .getResource(
                                                "/keyfiles/"
                                                        + "UTC--2016-11-03T05-55-06."
                                                        + "340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")
                                        .getFile()));

        assertEquals(credentials, (CREDENTIALS));
    }

    @Test
    void testLoadCredentialsFromInputStream() throws Exception {
        try (InputStream walletStream = WalletUtilsTest.class
                .getResourceAsStream(
                        "/keyfiles/"
                                + "UTC--2016-11-03T05-55-06."
                                + "340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")
        ) {
            Credentials credentials = WalletUtils.loadCredentials(PASSWORD, walletStream);
            assertEquals(credentials, (CREDENTIALS));
        }

    }

    @Test
    void testLoadCredentialsFromString() throws Exception {
        Credentials credentials =
                WalletUtils.loadCredentials(
                        PASSWORD,
                        WalletUtilsTest.class
                                .getResource(
                                        "/keyfiles/"
                                                + "UTC--2016-11-03T05-55-06."
                                                + "340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")
                                .getFile());

        assertEquals(credentials, (CREDENTIALS));
    }

    @Disabled // enable if users need to work with MyEtherWallet
    @Test
    void testLoadCredentialsMyEtherWallet() throws Exception {
        Credentials credentials =
                WalletUtils.loadCredentials(
                        PASSWORD,
                        new File(
                                WalletUtilsTest.class
                                        .getResource(
                                                "/keyfiles/"
                                                        + "UTC--2016-11-03T07-47-45."
                                                        + "988Z--4f9c1a1efaa7d81ba1cabf07f2c3a5ac5cf4f818")
                                        .getFile()));

        assertEquals(
                credentials,
                (Credentials.create(
                        "6ca4203d715e693279d6cd9742ad2fb7a3f6f4abe27a64da92e0a70ae5d859c9")));
    }

    @Test
    void testLoadJsonCredentials() throws Exception {
        Credentials credentials =
                WalletUtils.loadJsonCredentials(
                        PASSWORD,
                        convertStreamToString(
                                WalletUtilsTest.class.getResourceAsStream(
                                        "/keyfiles/"
                                                + "UTC--2016-11-03T05-55-06."
                                                + "340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")));

        assertEquals(credentials, (CREDENTIALS));
    }

    @Test
    void testGetDefaultKeyDirectory() {
        assertTrue(
                WalletUtils.getDefaultKeyDirectory("Mac OS X")
                        .endsWith(
                                String.format(
                                        "%sLibrary%sEthereum", File.separator, File.separator)));
        assertTrue(
                WalletUtils.getDefaultKeyDirectory("Windows")
                        .endsWith(String.format("%sEthereum", File.separator)));
        assertTrue(
                WalletUtils.getDefaultKeyDirectory("Linux")
                        .endsWith(String.format("%s.ethereum", File.separator)));
    }

    @Test
    void testGetTestnetKeyDirectory() {
        assertTrue(
                WalletUtils.getMainnetKeyDirectory()
                        .endsWith(String.format("%skeystore", File.separator)));
        assertTrue(
                WalletUtils.getTestnetKeyDirectory()
                        .endsWith(
                                String.format(
                                        "%stestnet%skeystore", File.separator, File.separator)));
        assertTrue(
                WalletUtils.getRinkebyKeyDirectory()
                        .endsWith(
                                String.format(
                                        "%srinkeby%skeystore", File.separator, File.separator)));
    }

    static File createTempDir() throws Exception {
        return Files.createTempDirectory(WalletUtilsTest.class.getSimpleName() + "-testkeys")
                .toFile();
    }

    @Test
    void testIsValidPrivateKey() {
        assertTrue(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING));
        assertTrue(isValidPrivateKey(Numeric.prependHexPrefix(SampleKeys.PRIVATE_KEY_STRING)));

        assertFalse(isValidPrivateKey(""));
        assertFalse(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING + "a"));
        assertFalse(isValidPrivateKey(SampleKeys.PRIVATE_KEY_STRING.substring(1)));
    }

    @Test
    void testIsValidAddress() {
        assertTrue(isValidAddress(SampleKeys.ADDRESS));
        assertTrue(isValidAddress(SampleKeys.ADDRESS_NO_PREFIX));

        assertFalse(isValidAddress(""));
        assertFalse(isValidAddress(SampleKeys.ADDRESS + 'a'));
        assertFalse(isValidAddress(SampleKeys.ADDRESS.substring(1)));
    }
}
