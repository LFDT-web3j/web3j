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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.tx.ChainIdLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UniversalResolverTest {

    private Web3j web3j;
    private Web3jService web3jService;
    private EnsResolver ensResolver;

    @BeforeEach
    void setUp() {
        web3jService = mock(Web3jService.class);
        web3j = Web3j.build(web3jService);
        ensResolver = new EnsResolver(web3j);
    }

    /**
     * ENSv2 resolution-tests "universal-resolver" case: resolve() must drive
     * ur.integration-tests.eth to 0x2222222222222222222222222222222222222222 by calling the
     * Universal Resolver (not the legacy ENS Registry → resolver path).
     *
     * <p>The mock returns the UR's {@code (bytes, address)} tuple for any EthCall. The legacy path
     * would call {@code ENSRegistry.resolver()} first and decode this tuple as a plain address —
     * ending up at {@code 0x0000…0040}, not {@code 0x2222…2222}. So this test only passes when
     * {@code resolve()} actually calls the UR.
     *
     * <p>Spec: https://github.com/ensdomains/resolution-tests
     */
    @Test
    void resolveGoesThroughUniversalResolver() throws Exception {
        NetVersion netVersion = new NetVersion();
        netVersion.setResult(Long.toString(ChainIdLong.MAINNET));
        when(web3jService.send(any(Request.class), eq(NetVersion.class))).thenReturn(netVersion);

        // UniversalResolver.resolveWithGateways(bytes name, bytes data, string[] gateways)
        // return value ABI: (bytes resultBytes, address resolver)
        //   - resultBytes is the addr(bytes32) return: a 32-byte zero-padded address.
        //   - resolver is the resolver the UR picked for this name.
        String encodedResponse =
                "0x"
                        // offset to `resultBytes` relative to start of tuple = 0x40
                        + "0000000000000000000000000000000000000000000000000000000000000040"
                        // `resolver` address (any non-zero address works for this test)
                        + "000000000000000000000000eeeeeeee14d718c2b47d9923deab1335e144eeee"
                        // length of `resultBytes` = 32
                        + "0000000000000000000000000000000000000000000000000000000000000020"
                        // ABI-encoded addr(): the sentinel address 0x2222...2222
                        + "0000000000000000000000002222222222222222222222222222222222222222";
        EthCall ethCall = new EthCall();
        ethCall.setResult(encodedResponse);
        when(web3jService.send(any(Request.class), eq(EthCall.class))).thenReturn(ethCall);

        String resolved = ensResolver.resolve("ur.integration-tests.eth");

        assertEquals("0x2222222222222222222222222222222222222222", resolved);
    }
}
