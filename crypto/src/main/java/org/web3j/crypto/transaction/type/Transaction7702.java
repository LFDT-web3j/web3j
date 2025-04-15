package org.web3j.crypto.transaction.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.web3j.crypto.AccessListObject;
import org.web3j.crypto.AuthorizationTuple;
import org.web3j.crypto.Sign;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * Example EIP-7702 transaction class that extends EIP-1559–style transactions,
 * adding an authorizationList field.
 *
 * The EIP-7702 spec calls for:
 *   [chain_id, nonce, max_priority_fee_per_gas, max_fee_per_gas, gas_limit,
 *    destination, value, data, access_list, authorization_list,
 *    signature_y_parity, signature_r, signature_s]
 */
public class Transaction7702 extends Transaction1559 implements ITransaction {

    private final List<AuthorizationTuple> authorizationList;

    /**
     * Minimal constructor (unsigned transaction).
     */
    protected Transaction7702(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            List<AccessListObject> accessList,
            List<AuthorizationTuple> authorizationList
    ) {
        // Call the Transaction1559 constructor (which includes chainId, nonce, gasLimit, etc.)
        super(chainId, nonce, gasLimit, to, value, data, maxPriorityFeePerGas, maxFeePerGas);

        this.authorizationList = authorizationList;
    }

    @Override
    public List<RlpType> asRlpValues(final Sign.SignatureData signatureData) {
        // EIP-7702 fields in RLP order:
        // chain_id, nonce, max_priority_fee_per_gas, max_fee_per_gas,
        // gas_limit, to, value, data, access_list, authorization_list,
        // y_parity, r, s
        List<RlpType> values = new ArrayList<>();

        // 0. chainId
        values.add(RlpString.create(getChainId()));

        // 1. nonce
        values.add(RlpString.create(getNonce()));

        // 2. max_priority_fee_per_gas
        values.add(RlpString.create(getMaxPriorityFeePerGas()));

        // 3. max_fee_per_gas
        values.add(RlpString.create(getMaxFeePerGas()));

        // 4. gas_limit
        values.add(RlpString.create(getGasLimit()));

        // 5. to
        final String to = getTo();
        if (to != null && !to.isEmpty()) {
            values.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            values.add(RlpString.create(""));
        }

        // 6. value
        values.add(RlpString.create(getValue()));

        // 7. data
        byte[] dataBytes = Numeric.hexStringToByteArray(getData());
        values.add(RlpString.create(dataBytes));

        // 8. access_list
        // In Transaction1559 you might have a method getAccessList()
        List<RlpType> accessListRlp = convertAccessListToRlp(getAccessList());
        values.add(new RlpList(accessListRlp));

        // 9. authorization_list
        // convert each AuthorizationTuple to RLP
        List<RlpType> authorizationRlp = convertAuthorizationListToRlp(authorizationList);
        values.add(new RlpList(authorizationRlp));

        // If we have a signature, add y_parity, r, s:
        if (signatureData != null) {
            // EIP-7702 uses y_parity in place of a normal "v"
            // For EVM compatibility, we can store it similarly to how 1559 does:
            int recId = Sign.getRecId(signatureData, getChainId());
            values.add(RlpString.create(recId));  // y_parity

            // r
            values.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getR())
                    )
            );

            // s
            values.add(
                    RlpString.create(
                            org.web3j.utils.Bytes.trimLeadingZeroes(signatureData.getS())
                    )
            );
        }

        // Finally, wrap the entire list in an RlpList if your encoder expects that
        List<RlpType> wrapped = new ArrayList<>();
        wrapped.add(new RlpList(values));
        return wrapped;
    }

    /**
     * Helper to convert the EIP-2930/EIP-1559 access list to RLP.
     */
    private List<RlpType> convertAccessListToRlp(List<AccessListObject> accessList) {
        List<RlpType> result = new ArrayList<>();
        if (accessList != null) {
            for (AccessListObject entry : accessList) {
                // Each item is [address, [storageKeys...]]
                List<RlpType> entryRlp = new ArrayList<>();
                byte[] addressBytes = Numeric.hexStringToByteArray(entry.getAddress());
                entryRlp.add(RlpString.create(addressBytes));

                // storageKeys
                List<RlpType> storageKeys = new ArrayList<>();
                for (String sk : entry.getStorageKeys()) {
                    storageKeys.add(RlpString.create(Numeric.hexStringToByteArray(sk)));
                }
                entryRlp.add(new RlpList(storageKeys));

                result.add(new RlpList(entryRlp));
            }
        }
        return result;
    }

    /**
     * Convert EIP-7702's authorization_list to RLP: each tuple is
     * [authChainId, address, authNonce, y_parity, r, s].
     */
    private List<RlpType> convertAuthorizationListToRlp(List<AuthorizationTuple> authList) {
        List<RlpType> result = new ArrayList<>();
        if (authList == null) {
            return result;
        }
        for (AuthorizationTuple at : authList) {
            List<RlpType> tuple = new ArrayList<>();
            tuple.add(RlpString.create(at.getChainId()));
            tuple.add(RlpString.create(Numeric.hexStringToByteArray(at.getAddress())));
            tuple.add(RlpString.create(at.getNonce()));
            tuple.add(RlpString.create(at.getYParity()));
            tuple.add(RlpString.create(at.getR()));
            tuple.add(RlpString.create(at.getS()));
            result.add(new RlpList(tuple));
        }
        return result;
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EIP7702;  // Make sure your enum has EIP7702
    }

    /**
     * Provide a static factory method to create an unsigned EIP-7702 transaction.
     */
    public static Transaction7702 createTransaction(
            long chainId,
            BigInteger nonce,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas,
            BigInteger gasLimit,
            String to,
            BigInteger value,
            String data,
            List<AccessListObject> accessList,
            List<AuthorizationTuple> authorizationList
    ) {
        return new Transaction7702(
                chainId,
                nonce,
                maxPriorityFeePerGas,
                maxFeePerGas,
                gasLimit,
                to,
                value,
                data,
                accessList,
                authorizationList
        );
    }

    public List<AuthorizationTuple> getAuthorizationList() {
        return authorizationList;
    }
}