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
package org.web3j.protocol.core.methods.response;

import java.math.BigInteger;

import org.web3j.protocol.core.Response;
import org.web3j.utils.Numeric;

/**
 * eth_blobBaseFee.
 *
 * <p>Returns the node-computed base fee per blob gas (in wei), correct under EIP-7691 (Pectra) and
 * EIP-7918 (Fusaka) and any future blob fork, since the node applies the active fork rules.
 */
public class EthBlobBaseFee extends Response<String> {
    public BigInteger getBaseFeePerBlobGas() {
        return Numeric.decodeQuantity(getResult());
    }
}
