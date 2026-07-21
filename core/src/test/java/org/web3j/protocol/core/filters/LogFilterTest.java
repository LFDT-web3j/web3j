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
package org.web3j.protocol.core.filters;

import org.junit.jupiter.api.Test;

import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

class LogFilterTest extends FilterTester {

    @Test
    void testLogFilter() throws Exception {

        EthLog ethLog =
                objectMapper.readValue(
                        "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":[{\"address\":\"0x2a98c5f40bfa3dee83431103c535f6fae9a8ad38\",\"blockHash\":\"0xd263df878c66b8a08c8509a8f33d6758bc3a1ee3c5ab3c9a765ea981ae9d72e3\",\"blockNumber\":\"0x2865a\",\"data\":\"0x45544855534400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"logIndex\":\"0x0\",\"removed\":false,\"topics\":[\"0x5a690ecd0cb15c1c1fd6b6f8a32df0d4f56cb41a54fea7e94020f013595de796\",\"0x0000000000000000000000000000000000000000000000000000000000000002\",\"0x0000000000000000000000003f37a1c95bbc0aa6bf62e99b30b147e68dee7b43\",\"0x0000000000000000000000000000000000000000000000000000000000000000\"],\"transactionHash\":\"0x9d8a4410204140a8fa7f191b0b1d9526822a87d212ade0b6a3cbd20a6e2ed2e9\",\"transactionIndex\":\"0x0\"},{\"address\":\"0x2a98c5f40bfa3dee83431103c535f6fae9a8ad38\",\"blockHash\":\"0xd263df878c66b8a08c8509a8f33d6758bc3a1ee3c5ab3c9a765ea981ae9d72e3\",\"blockNumber\":\"0x2865a\",\"data\":\"0x0000000000000000000000000000000000000000000000006c93a67534ce4000\",\"logIndex\":\"0x1\",\"removed\":false,\"topics\":[\"0xa9c6cbc4bd352a6940479f6d802a1001550581858b310d7f68f7bea51218cda6\",\"0x4554485553440000000000000000000000000000000000000000000000000000\"],\"transactionHash\":\"0x9d8a4410204140a8fa7f191b0b1d9526822a87d212ade0b6a3cbd20a6e2ed2e9\",\"transactionIndex\":\"0x0\"},{\"address\":\"0x3f37a1c95bbc0aa6bf62e99b30b147e68dee7b43\",\"blockHash\":\"0xd263df878c66b8a08c8509a8f33d6758bc3a1ee3c5ab3c9a765ea981ae9d72e3\",\"blockNumber\":\"0x2865a\",\"data\":\"0x0000000000000000000000000000000000000000000000006c93a67534ce4000\",\"logIndex\":\"0x2\",\"removed\":false,\"topics\":[\"0xa609f6bd4ad0b4f419ddad4ac9f0d02c2b9295c5e6891469055cf73c2b568fff\",\"0x0000000000000000000000003f37a1c95bbc0aa6bf62e99b30b147e68dee7b43\"],\"transactionHash\":\"0x9d8a4410204140a8fa7f191b0b1d9526822a87d212ade0b6a3cbd20a6e2ed2e9\",\"transactionIndex\":\"0x0\"},{\"address\":\"0x870283380c7da544d2b16d3434709874e3ed77cb\",\"blockHash\":\"0xd263df878c66b8a08c8509a8f33d6758bc3a1ee3c5ab3c9a765ea981ae9d72e3\",\"blockNumber\":\"0x2865a\",\"data\":\"0x000000000000000000000000000000000000000000000000000000005853a9f4000000000000000000000000000000000000000000000000000000000000004e\",\"logIndex\":\"0x3\",\"removed\":false,\"topics\":[\"0x9de614b5c45179d41912c87ca7f7af67525f50c10b6b9cea79dc2c12b8d693a3\"],\"transactionHash\":\"0xccf367f7c845de706ccf985a187ea168f9df241d4878780f996aa08a303ad910\",\"transactionIndex\":\"0x1\"},{\"address\":\"0x870283380c7da544d2b16d3434709874e3ed77cb\",\"blockHash\":\"0xd263df878c66b8a08c8509a8f33d6758bc3a1ee3c5ab3c9a765ea981ae9d72e3\",\"blockNumber\":\"0x2865a\",\"data\":\"0x000000000000000000000000000000000000000000000000000000005853aa04000000000000000000000000000000000000000000000000000000000000004e\",\"logIndex\":\"0x4\",\"removed\":false,\"topics\":[\"0x9de614b5c45179d41912c87ca7f7af67525f50c10b6b9cea79dc2c12b8d693a3\"],\"transactionHash\":\"0x83d38a70d55279be43cfb6da97256e438b6f72337be1a0919b349244fe633daa\",\"transactionIndex\":\"0x2\"}]}",
                        EthLog.class);

        runTest(ethLog, web3j.ethLogFlowable(new EthFilter().addSingleTopic("test")));
    }

    @Test
    void testEthFilterSerialization() throws Exception {
        EthFilter filter = new EthFilter();
        filter.addSingleTopic("0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b");
        String json = objectMapper.writeValueAsString(filter);
        org.junit.jupiter.api.Assertions.assertTrue(
                json.contains("0x000000000000000000000000a94f5374fce5edbc8e2a8697c15331677e6ebf0b"));
    }
}
