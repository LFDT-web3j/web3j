/*
 * Copyright 2022 Web3 Labs Ltd.
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
package org.web3j.service;

import org.web3j.crypto.HSMPass;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.tx.ChainId;

import static org.web3j.crypto.TransactionEncoder.createEip155SignatureData;
import static org.web3j.crypto.TransactionEncoder.encode;

/** Service to sign transaction with HSM (hardware security module). */
public class TxHSMSignService<T extends HSMPass> implements TxSignService {

    private final T hsmPass;
    private final HSMRequestProcessor<T> hsmRequestProcessor;

    public TxHSMSignService(HSMRequestProcessor<T> hsmRequestProcessor, T hsmPass) {
        this.hsmPass = hsmPass;
        this.hsmRequestProcessor = hsmRequestProcessor;
    }

    @Override
    public byte[] sign(RawTransaction rawTransaction, long chainId) {
        byte[] finalBytes;
        byte[] encodedTransaction;
        Sign.SignatureData signatureData;
        // Legacy tx is tx before Eip1559, should have chainId as an additional parameter.
        // After Eip1559 chainId is a part of tx.
        boolean isLegacy =
                chainId > ChainId.NONE && rawTransaction.getType().equals(TransactionType.LEGACY);

        if (isLegacy) {
            encodedTransaction = encode(rawTransaction, chainId);
        } else {
            encodedTransaction = encode(rawTransaction);
        }

        byte[] messageHash = Hash.sha3(encodedTransaction);

        signatureData = hsmRequestProcessor.callHSM(messageHash, hsmPass);
        if (!isComplete(signatureData)) {
            throw new IllegalStateException("HSM returned no signature");
        }

        if (isLegacy) {
            signatureData = createEip155SignatureData(signatureData, chainId);
        }

        finalBytes = encode(rawTransaction, signatureData);

        return finalBytes;
    }

    @Override
    public String getAddress() {
        return hsmPass.getAddress();
    }

    private static boolean isComplete(Sign.SignatureData signatureData) {
        return signatureData != null
                && isNotEmpty(signatureData.getV())
                && isNotEmpty(signatureData.getR())
                && isNotEmpty(signatureData.getS());
    }

    private static boolean isNotEmpty(byte[] value) {
        return value != null && value.length > 0;
    }
}
