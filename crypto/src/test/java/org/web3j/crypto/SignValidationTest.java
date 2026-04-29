/*
 * Copyright 2026 Web3 Labs Ltd.
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

import java.math.BigInteger;
import java.security.SignatureException;

import org.junit.jupiter.api.Test;

import org.web3j.utils.Numeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SignValidationTest {

    private static final byte[] TEST_MESSAGE = "A test message".getBytes();

    @Test
    public void testValidSignature() throws SignatureException {
        Sign.SignatureData signatureData =
                Sign.signPrefixedMessage(TEST_MESSAGE, SampleKeys.KEY_PAIR);
        BigInteger recovered = Sign.signedPrefixedMessageToKey(TEST_MESSAGE, signatureData);
        assertEquals(SampleKeys.PUBLIC_KEY, recovered);
    }

    @Test
    public void testMalleableSignatureRejected() {
        Sign.SignatureData signatureData =
                Sign.signPrefixedMessage(TEST_MESSAGE, SampleKeys.KEY_PAIR);

        BigInteger sBI = new BigInteger(1, signatureData.getS());
        BigInteger n = Sign.CURVE_PARAMS.getN();
        BigInteger malleableS = n.subtract(sBI);
        byte v = (byte) (signatureData.getV()[0] == 27 ? 28 : 27);

        Sign.SignatureData malleable =
                new Sign.SignatureData(
                        v, signatureData.getR(), Numeric.toBytesPadded(malleableS, 32));

        assertThrows(
                SignatureException.class,
                () -> Sign.signedPrefixedMessageToKey(TEST_MESSAGE, malleable));
    }

    @Test
    public void testHighSValueRejected() {
        // s value in the upper half of curve order — must be rejected
        byte[] r =
                Numeric.hexStringToByteArray(
                        "1234567812345678123456781234567812345678123456781234567812345678");
        byte[] s =
                Numeric.hexStringToByteArray(
                        "8765432187654321876543218765432187654321876543218765432187654321");
        byte[] v = new byte[] {27};

        Sign.SignatureData sig = new Sign.SignatureData(v, r, s);

        assertThrows(
                SignatureException.class,
                () -> Sign.signedPrefixedMessageToKey(TEST_MESSAGE, sig));
    }
}
