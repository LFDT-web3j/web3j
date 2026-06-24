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
package org.web3j.abi.datatypes;

import java.util.List;

/** Base class for Solidity structs. Should be extended by generated struct wrappers. */
public abstract class Struct extends DynamicStruct {
    protected Struct(List<Type> values) {
        super(values.toArray(new Type[0]));
    }

    protected Struct(Type... values) {
        super(values);
    }
}
