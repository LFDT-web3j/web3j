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
package org.web3j.ens;

import org.web3j.tx.ChainIdLong;

/**
 * Universal Resolver contract addresses per chain.
 *
 * <p>The Universal Resolver is the entry point for ENSv2-ready resolution — it handles registry
 * lookup, ENSIP-10 wildcard traversal, and CCIP-Read trampoline in a single call.
 */
public class UniversalResolverContracts {

    /** ENSv2 Universal Resolver on Ethereum mainnet. */
    public static final String MAINNET = "0xeeeeeeee14d718c2b47d9923deab1335e144eeee";

    /** ENSv2 Universal Resolver on Sepolia (deterministic CREATE2 deployment, same as mainnet). */
    public static final String SEPOLIA = "0xeeeeeeee14d718c2b47d9923deab1335e144eeee";

    private UniversalResolverContracts() {}

    public static String resolveUniversalResolverContract(String chainId) {
        final Long chainIdLong = Long.parseLong(chainId);
        if (chainIdLong.equals(ChainIdLong.MAINNET)) {
            return MAINNET;
        } else if (chainIdLong.equals(ChainIdLong.SEPOLIA)) {
            return SEPOLIA;
        } else {
            throw new EnsResolutionException(
                    "Unable to resolve Universal Resolver contract for chain id: " + chainId);
        }
    }
}
