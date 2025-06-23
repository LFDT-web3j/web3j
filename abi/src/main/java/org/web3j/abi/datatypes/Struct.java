//File for errors regarding Struct use in TR and TRT files 
package org.web3j.abi.datatypes;

import java.util.List;

/**
 * Base class for Solidity structs. Should be extended by generated struct wrappers.
 */
public abstract class Struct extends DynamicStruct {
    protected Struct(List<Type> values) {
        super(values.toArray(new Type[0]));
    }

    protected Struct(Type... values) {
        super(values);
    }
}
