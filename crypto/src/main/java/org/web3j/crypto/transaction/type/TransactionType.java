/*
 * Copyright 2021 Web3 Labs Ltd.
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

public enum TransactionType {
    LEGACY(null),
    EIP2930(((byte) 0x01)),
    EIP1559(((byte) 0x02)),
    EIP4844(((byte) 0x03)),
    EIP7702(((byte) 0x04));

    Byte type;

    TransactionType(final Byte type) {
        this.type = type;
    }

    public Byte getRlpType() {
        return type;
    }

    public boolean isLegacy() {
        return this.equals(TransactionType.LEGACY);
    }

    public boolean isEip1559() {
        return this.equals(TransactionType.EIP1559);
    }

    public boolean isEip2930() {
        return this.equals(TransactionType.EIP2930);
    }

    public boolean isEip4844() {
        return this.equals(TransactionType.EIP4844);
    }
}
