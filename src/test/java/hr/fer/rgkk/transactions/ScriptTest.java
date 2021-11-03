package hr.fer.rgkk.transactions;

import hr.fer.rgkk.transactions.CoinToss.CoinTossChoice;
import hr.fer.rgkk.transactions.CoinToss.WinningPlayer;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ScriptTest {

    private WalletKit walletKit;
    private NetworkParameters networkParameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTest.class);

    public ScriptTest() {
        String walletName = "wallet";
        this.networkParameters = RegTestParams.get();
        this.walletKit = new WalletKit(networkParameters, new File(walletName), "password");
    }

    @Test
    public void printAddress() {
        LOGGER.info("Importing key");
        LOGGER.info("Your address is {}", walletKit.getWallet().currentReceiveAddress());
        LOGGER.info("Your balance is {}", walletKit.getWallet().getBalance());
        walletKit.close();
    }

    private void testTransaction(ScriptTransaction scriptTransaction) throws InsufficientMoneyException {
        Script lockingScript = scriptTransaction.createLockingScript();
        Transaction transaction = scriptTransaction.createOutgoingTransaction(lockingScript, Coin.CENT);
        transaction.getOutputs().stream()
                .filter(to -> to.getScriptPubKey().equals(lockingScript))
                .findAny()
                .ifPresent(relevantOutput -> {
                    Transaction unlockingTransaction = scriptTransaction.createUnsignedUnlockingTransaction(relevantOutput, scriptTransaction.getReceiveAddress());
                    Script unlockingScript = scriptTransaction.createUnlockingScript(unlockingTransaction);
                    scriptTransaction.testScript(lockingScript, unlockingScript, unlockingTransaction);
                    unlockingTransaction.getInput(0).setScriptSig(unlockingScript);
                    scriptTransaction.sendTransaction(transaction);
                    scriptTransaction.sendTransaction(unlockingTransaction);
                });
    }

    //////////////////////
    // PayToPubKey test //
    //////////////////////

    @Test
    public void testPayToPubKey() {
        try (ScriptTransaction payToPubKey = new PayToPubKey(walletKit, networkParameters)) {
            testTransaction(payToPubKey);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    //////////////////////////
    // PayToPubKeyHash test //
    //////////////////////////

    @Test
    public void testPayToPubKeyHash() {
        try (ScriptTransaction payToPubKeyHash = new PayToPubKeyHash(walletKit, networkParameters)) {
            testTransaction(payToPubKeyHash);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
    //////////////////////////////
    // Logical equivalence test //
    //////////////////////////////

    @Test
    public void testLogicalEquivalence() {
        try (LogicalEquivalenceTransaction logEq = new LogicalEquivalenceTransaction(walletKit, networkParameters)) {
            testTransaction(logEq);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    /////////////////////
    // Coin toss tests //
    /////////////////////

    @Test
    public void testTailPlayerWinsWithTwoZeros() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ZERO,
                CoinTossChoice.ZERO,
                WinningPlayer.TAIL
        )) {
            testTransaction(coinToss);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testHeadPlayerLoosesWithTwoZeros() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ZERO,
                CoinTossChoice.ZERO,
                WinningPlayer.HEAD
        )) {
            testTransaction(coinToss);
            Assert.fail("Head player should loose.");
        } catch (Exception ignore) { }
    }

    @Test
    public void testTailPlayerWinsWithTwoOnes() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ONE,
                CoinTossChoice.ONE,
                WinningPlayer.TAIL
        )) {
            testTransaction(coinToss);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testHeadPlayerLoosesWithTwoOnes() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ONE,
                CoinTossChoice.ONE,
                WinningPlayer.HEAD
        )) {
            testTransaction(coinToss);
            Assert.fail("Head player should loose.");
        } catch (Exception ignore) { }
    }

    @Test
    public void testTailPlayerLoosesWithZeroAndOne() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ZERO,
                CoinTossChoice.ONE,
                WinningPlayer.TAIL
        )) {
            testTransaction(coinToss);
            Assert.fail("Tail player should loose");
        } catch (Exception ignore) { }
    }

    @Test
    public void testHeadPlayerWinsWithZeroAndOne() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ZERO,
                CoinTossChoice.ONE,
                WinningPlayer.HEAD
        )) {
            testTransaction(coinToss);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTailPlayerLoosesWithOneAndZero() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ONE,
                CoinTossChoice.ZERO,
                WinningPlayer.TAIL
        )) {
            testTransaction(coinToss);
            Assert.fail("Tail player should loose");
        } catch (Exception ignore) { }
    }

    @Test
    public void testHeadPlayerWinsWithOneAndZero() {
        try (ScriptTransaction coinToss = CoinToss.of(
                walletKit,
                networkParameters,
                CoinTossChoice.ONE,
                CoinTossChoice.ZERO,
                WinningPlayer.HEAD
        )) {
            testTransaction(coinToss);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
