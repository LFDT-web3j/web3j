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
package org.web3j.protocol.core;

import java.math.BigInteger;

public interface BlobFee {

    /** Blob base-fee update fraction prior to Pectra (Cancun, EIP-4844). */
    BigInteger BLOB_BASE_FEE_UPDATE_FRACTION_CANCUN = BigInteger.valueOf(3338477);

    /** Blob base-fee update fraction from Pectra onward (Prague, EIP-7691). */
    BigInteger BLOB_BASE_FEE_UPDATE_FRACTION_PRAGUE = BigInteger.valueOf(5007716);

    /**
     * Calculates the base fee per blob gas from the latest block's {@code excessBlobGas}, using the
     * post-Pectra (Prague, EIP-7691) update fraction — the value live on Ethereum mainnet. Existing
     * callers can keep using this no-arg method unchanged; it now defaults to the Prague fraction.
     *
     * <p>For a chain that has not yet activated Pectra, call {@link
     * #ethGetBaseFeePerBlobGas(BigInteger)} with {@link #BLOB_BASE_FEE_UPDATE_FRACTION_CANCUN}.
     *
     * @return baseFee per blob gas value.
     */
    BigInteger ethGetBaseFeePerBlobGas();

    /**
     * Calculates the base fee per blob gas from the latest block's {@code excessBlobGas}, using the
     * given blob base-fee update fraction (see {@link #BLOB_BASE_FEE_UPDATE_FRACTION_PRAGUE} /
     * {@link #BLOB_BASE_FEE_UPDATE_FRACTION_CANCUN}).
     *
     * @param blobBaseFeeUpdateFraction the EIP-4844/EIP-7691 update fraction for the target fork
     * @return baseFee per blob gas value.
     */
    BigInteger ethGetBaseFeePerBlobGas(BigInteger blobBaseFeeUpdateFraction);
}
