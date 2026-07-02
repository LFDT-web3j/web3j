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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Live mainnet integration test for the Universal Resolver path.
 *
 * <p>Gated on {@code WEB3J_MAINNET_RPC} so CI stays green without a shared RPC URL. To run:
 *
 * <pre>
 * WEB3J_MAINNET_RPC=https://ethereum-rpc.publicnode.com \
 *     ./gradlew :integration-tests:test --tests org.web3j.ens.UniversalResolverIT
 * </pre>
 *
 * <p>Covers the first case from the ENSv2 resolution-tests suite
 * (https://github.com/ensdomains/resolution-tests): {@code ur.integration-tests.eth} must resolve
 * to {@code 0x2222222222222222222222222222222222222222} via the Universal Resolver.
 */
@EnabledIfEnvironmentVariable(named = "WEB3J_MAINNET_RPC", matches = ".+")
public class UniversalResolverIT {

    @Test
    void resolvesUrIntegrationTestsEth() {
        String rpc = System.getenv("WEB3J_MAINNET_RPC");
        Web3j web3j = Web3j.build(new HttpService(rpc));
        EnsResolver ens = new EnsResolver(web3j);

        String resolved = ens.resolve("ur.integration-tests.eth");

        assertEquals("0x2222222222222222222222222222222222222222", resolved.toLowerCase());
    }
}
