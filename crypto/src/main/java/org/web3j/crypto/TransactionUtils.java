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

import org.web3j.utils.Numeric;

/** Transaction utility functions. */
public class TransactionUtils {
    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;

    public static byte[] generateTransactionHash(
            RawTransaction rawTransaction, Credentials credentials) {
        Sign.SignatureData signatureData =
                TransactionEncoder.signMessageToSignatureData(rawTransaction, credentials);
        return Hash.sha3(TransactionEncoder.encode(rawTransaction, signatureData));
    }

    /**
     * Utility method to provide the transaction hash for a given transaction.
     *
     * @param rawTransaction we wish to send
     * @param chainId of the intended chain
     * @param credentials of the sender
     * @return encoded transaction hash
     */
    public static byte[] generateTransactionHash(
            RawTransaction rawTransaction, byte chainId, Credentials credentials) {
        Sign.SignatureData signatureData =
                TransactionEncoder.signMessageToSignatureData(
                        rawTransaction, (long) chainId, credentials);
        return Hash.sha3(TransactionEncoder.encode(rawTransaction, signatureData));
    }

    /**
     * Utility method to provide the transaction hash for a given transaction.
     *
     * @param rawTransaction we wish to send
     * @param credentials of the sender
     * @return transaction hash as a hex encoded string
     */
    public static String generateTransactionHashHexEncoded(
            RawTransaction rawTransaction, Credentials credentials) {
        return Numeric.toHexString(generateTransactionHash(rawTransaction, credentials));
    }

    /**
     * Utility method to provide the transaction hash for a given transaction.
     *
     * @param rawTransaction we wish to send
     * @param chainId of the intended chain
     * @param credentials of the sender
     * @return transaction hash as a hex encoded string
     */
    public static String generateTransactionHashHexEncoded(
            RawTransaction rawTransaction, byte chainId, Credentials credentials) {
        return Numeric.toHexString(generateTransactionHash(rawTransaction, chainId, credentials));
    }

    /**
     * Utility method to derive chain id from v parameter
     *
     * @param v recovery identifier
     * @return Chain id
     */
    public static Long deriveChainId(long v) {
        if (v == LOWER_REAL_V || v == (LOWER_REAL_V + 1)) {
            return null;
        }
        return (v - CHAIN_ID_INC) / 2;
    }
}
