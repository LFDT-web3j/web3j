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

import org.junit.jupiter.api.Test;

import org.web3j.tx.ChainIdLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniversalResolverContractsTest {

    @Test
    void resolvesMainnet() {
        assertEquals(
                UniversalResolverContracts.MAINNET,
                UniversalResolverContracts.resolveUniversalResolverContract(
                        Long.toString(ChainIdLong.MAINNET)));
    }

    @Test
    void resolvesSepolia() {
        assertEquals(
                UniversalResolverContracts.SEPOLIA,
                UniversalResolverContracts.resolveUniversalResolverContract(
                        Long.toString(ChainIdLong.SEPOLIA)));
    }

    @Test
    void throwsForUnsupportedChain() {
        assertThrows(
                EnsResolutionException.class,
                () ->
                        UniversalResolverContracts.resolveUniversalResolverContract(
                                Long.toString(ChainIdLong.HOLESKY)));
    }
}
