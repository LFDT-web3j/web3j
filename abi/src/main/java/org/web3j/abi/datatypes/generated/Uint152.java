package org.web3j.abi.datatypes.generated;

import java.math.BigInteger;
import org.web3j.abi.datatypes.Uint;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.web3j.codegen.AbiTypesGenerator in the 
 * <a href="https://github.com/hyperledger/web3j/tree/main/codegen">codegen module</a> to update.
 */
public class Uint152 extends Uint {
    public static final Uint152 DEFAULT = new Uint152(BigInteger.ZERO);

    public Uint152(BigInteger value) {
        super(152, value);
    }

    public Uint152(long value) {
        this(BigInteger.valueOf(value));
    }
}
