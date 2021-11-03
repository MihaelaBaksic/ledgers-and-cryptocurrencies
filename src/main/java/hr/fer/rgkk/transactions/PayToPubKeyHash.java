package hr.fer.rgkk.transactions;

import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;
import static org.bitcoinj.script.ScriptOpCodes.OP_DUP;
import static org.bitcoinj.script.ScriptOpCodes.OP_HASH160;
import static org.bitcoinj.script.ScriptOpCodes.OP_EQUALVERIFY;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public class PayToPubKeyHash extends ScriptTransaction {
	
	// Key used to create destination address of a transaction.
    private final ECKey key;

    public PayToPubKeyHash(WalletKit walletKit, NetworkParameters parameters) {
        super(walletKit, parameters);
        key = getWallet().freshReceiveKey();
    }

    @Override
    public Script createLockingScript() {
        // TODO: Create Locking script
        //throw new UnsupportedOperationException();
    	return new ScriptBuilder()  
    			.op(OP_DUP) // Duplicate top
    			.op(OP_HASH160) // Hash top
                .data(key.getPubKeyHash()) // Set your public key hash160 on top
                .op(OP_EQUALVERIFY) // Make sure that public keys on top of stack are equal
                .op(OP_CHECKSIG) // Make sure that digital signature is valid
                .build(); // Build "OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG" locking script
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedTransaction) {
        // TODO: Create Unlocking script
        //throw new UnsupportedOperationException();
    	TransactionSignature txSig = sign(unsignedTransaction, key); // Create key signature
        return new ScriptBuilder()                                   // Create new ScriptBuilder
                .data(txSig.encodeToBitcoin())                       // Add key signature to unlocking script
                .data(key.getPubKey())                         		 // Add public key to unlocking script
                .build();                                            // Build "<signature(key)> <public_key>" unlocking script
    }
}
