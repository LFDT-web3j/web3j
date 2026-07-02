package example;

import example.contracts.ExampleToken;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.web3j.EVMTest;
import org.web3j.NodeType;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EVMTest(type = NodeType.BESU)
class Erc20EvmTest {

    @Test
    void deploysTransfersAndStreamsEvents(
            Web3j web3j,
            TransactionManager transactionManager,
            ContractGasProvider gasProvider)
            throws Exception {
        ExampleToken token =
                ExampleToken.deploy(
                                web3j,
                                transactionManager,
                                gasProvider,
                                BigInteger.valueOf(1_000_000L),
                                "Skill Token",
                                "SKILL")
                        .send();

        assertTrue(token.isValid());
        assertEquals("Skill Token", token.name().send());
        assertEquals("SKILL", token.symbol().send());
        assertEquals(BigInteger.valueOf(1_000_000L), token.totalSupply().send());

        CountDownLatch eventSeen = new CountDownLatch(1);
        Disposable transferSubscription =
                token.transferEventFlowable(
                                DefaultBlockParameterName.EARLIEST,
                                DefaultBlockParameterName.LATEST)
                        .subscribe(event -> eventSeen.countDown());

        try {
            String recipient = "0x00000000000000000000000000000000000000b0";
            TransactionReceipt receipt = token.transfer(recipient, BigInteger.valueOf(25L)).send();

            ExampleToken.TransferEventResponse transferEvent = token.getTransferEvents(receipt).get(0);
            assertEquals(recipient.toLowerCase(), transferEvent._to.toLowerCase());
            assertEquals(BigInteger.valueOf(25L), transferEvent._value);
            assertEquals(BigInteger.valueOf(25L), token.balanceOf(recipient).send());
            assertTrue(eventSeen.await(5, TimeUnit.SECONDS));
        } finally {
            transferSubscription.dispose();
        }
    }
}
