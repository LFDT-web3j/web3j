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

import tools.jackson.databind.annotation.JsonDeserialize;

import org.web3j.protocol.core.Response;

public class LineaGetProof extends Response<EthGetProof.Proof> {

    @Override
    @JsonDeserialize(using = EthGetProof.ResponseDeserializer.class)
    public void setResult(EthGetProof.Proof result) {
        super.setResult(result);
    }

    /**
     * linea get proof result.
     *
     * @return proof result
     */
    public EthGetProof.Proof getProof() {
        return getResult();
    }
}
