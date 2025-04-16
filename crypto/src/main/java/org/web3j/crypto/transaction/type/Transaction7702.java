/*
 * Copyright 2025 Web3 Labs Ltd.
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
package org.web3j.crypto.transaction.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.crypto.AccessListObject;
import org.web3j.crypto.AuthorizationTuple;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

public class Transaction7702 extends Transaction1559 implements ITransaction {

    private final List<AuthorizationTuple> authorizationList;

    protected Transaction7702(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            List<AccessListObject> accessList,
            List<AuthorizationTuple> authorizationList) {
        super(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);
        this.authorizationList = authorizationList;
    }

    @Override
    public List<RlpType> asRlpValues(final Sign.SignatureData signatureData) {
        // EIP-7702 fields in RLP order:
        // chain_id, nonce, max_priority_fee_per_gas, max_fee_per_gas,
        // gas_limit, to, value, data, access_list, authorization_list,
        // y_parity, r, s
        List<RlpType> values = new ArrayList<>();
        values.add(RlpString.create(getChainId()));
        values.add(RlpString.create(getNonce()));
        values.add(RlpString.create(getMaxPriorityFeePerGas()));
        values.add(RlpString.create(getMaxFeePerGas()));
        values.add(RlpString.create(getGasLimit()));
        final String to = getTo();
        if (to != null && !to.isEmpty()) {
            values.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            values.add(RlpString.create(""));
        }
        values.add(RlpString.create(getValue()));
        byte[] dataBytes = Numeric.hexStringToByteArray(getData());
        values.add(RlpString.create(dataBytes));
        List<RlpType> accessListRlp = rlpAccessListRlp();
        values.add(new RlpList(accessListRlp));
        List<RlpType> authorizationRlp = convertAuthorizationListToRlp(authorizationList);
        values.add(new RlpList(authorizationRlp));
        if (signatureData != null) {
            int recId = Sign.getRecId(signatureData, getChainId());
            values.add(RlpString.create(recId)); // y_parity
            values.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getR())));
            values.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getS())));
        }

        return values;
    }

    private List<RlpType> convertAuthorizationListToRlp(List<AuthorizationTuple> authList) {
        List<RlpType> result = new ArrayList<>();
        if (authList == null) {
            return result;
        }
        for (AuthorizationTuple at : authList) {
            List<RlpType> tuple = new ArrayList<>();
            tuple.add(RlpString.create(at.getChainId()));
            tuple.add(RlpString.create(Numeric.hexStringToByteArray(at.getAddress())));
            tuple.add(RlpString.create(at.getNonce()));
            tuple.add(RlpString.create(at.getYParity()));
            tuple.add(RlpString.create(at.getR()));
            tuple.add(RlpString.create(at.getS()));
            result.add(new RlpList(tuple));
        }
        return result;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EIP7702;
    }

    public static Transaction7702 createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            List<AccessListObject> accessList,
            List<AuthorizationTuple> authorizationList) {
        return new Transaction7702(
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                accessList,
                authorizationList);
    }

    public List<AuthorizationTuple> getAuthorizationList() {
        return authorizationList;
    }
}
