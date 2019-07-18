/*
 * Copyright 2019 Web3 Labs LTD.
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
package org.web3j.protocol.eea.crypto;

import java.util.List;
import java.util.stream.Collectors;

import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

public class PrivateTransactionDecoder {

    public static RawPrivateTransaction decode(final String hexTransaction) {
        final byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        final RlpList rlpList = RlpDecoder.decode(transaction);
        final RlpList values = (RlpList) rlpList.getValues().get(0);

        final RawTransaction rawTransaction = TransactionDecoder.decode(hexTransaction);

        if (values.getValues().size() == 8) {
            return new RawPrivateTransaction(rawTransaction,
                    extractString(values, 6),  extractString(values, 7));
        } else if (values.getValues().size() == 9) {
            final String privateFrom = extractString(values, 6);
            final String restriction = extractString(values, 8);
            if (values.getValues().get(7) instanceof RlpList) {
                return new RawPrivateTransaction(
                        rawTransaction,
                        privateFrom,
                        extractStringList(values, 7),
                        restriction);
            } else {
                return new RawPrivateTransaction(
                        rawTransaction,
                        privateFrom,
                        extractString(values, 7),
                        restriction);
            }

        } else if (values.getValues().size() == 11) {
            return new SignedRawPrivateTransaction(
                    (SignedRawTransaction) rawTransaction,
                    extractString(values, 9),
                    extractString(values, 10));
        } else {
            final String privateFrom = extractString(values, 9);
            final String restriction = extractString(values, 11);
            if (values.getValues().get(10) instanceof RlpList) {
                return new SignedRawPrivateTransaction(
                        (SignedRawTransaction) rawTransaction,
                        privateFrom,
                        extractStringList(values, 10),
                        restriction);
            } else {
                return new SignedRawPrivateTransaction(
                        (SignedRawTransaction) rawTransaction,
                        privateFrom,
                        extractString(values, 10),
                        restriction);
            }
        }
    }

    private static String extractString(final RlpList values, int i) {
        return new String(((RlpString) values.getValues().get(i)).getBytes());
    }

    private static List<String> extractStringList(final RlpList values, int i) {
        return ((RlpList) values.getValues().get(i))
                .getValues().stream()
                        .map(rlp -> new String(((RlpString) rlp).getBytes()))
                        .collect(Collectors.toList());
    }
}
