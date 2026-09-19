/*
 * Copyright 2024 Web3 Labs Ltd.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.web3j.protocol.core.Response;

/** txpool_contentFrom. */
public final class TxPoolContentFrom extends Response<TxPoolContentFrom.TxPoolContentFromResult> {
    public static class TxPoolContentFromResult {

        private Map<BigInteger, Transaction> pending;
        private Map<BigInteger, Transaction> queued;

        public TxPoolContentFromResult() {}

        public TxPoolContentFromResult(
                Map<BigInteger, Transaction> pending,
                Map<BigInteger, Transaction> queued) {
            this.pending = immutableCopy(pending, Function.identity());
            this.queued = immutableCopy(queued, Function.identity());
        }

        public Map<BigInteger, Transaction> getPending() {
            return pending;
        }

        public Map<BigInteger, Transaction> getQueued() {
            return queued;
        }

        public List<Transaction> getPendingTransactions() {
            return pending == null ? Collections.emptyList() : new ArrayList<>(pending.values());
        }

        public List<Transaction> getQueuedTransactions() {
            return queued == null ? Collections.emptyList() : new ArrayList<>(queued.values());
        }

        private static <K, V> Map<K, V> immutableCopy(Map<K, V> map, Function<V, V> valueMapper) {
            if (map == null) {
                return Collections.emptyMap();
            }
            Map<K, V> result = new HashMap<>();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                result.put(key, valueMapper.apply(value));
            }
            return Collections.unmodifiableMap(result);
        }
    }
}
