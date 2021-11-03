package hr.fer.rgkk.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import static org.bitcoinj.script.ScriptOpCodes.*;

import java.math.BigInteger;

public class LogicalEquivalenceTransaction extends ScriptTransaction {
	
    public LogicalEquivalenceTransaction(WalletKit walletKit, NetworkParameters parameters) {
        super(walletKit, parameters);
    }

    @Override
    public Script createLockingScript() {
        // TODO: Create Locking script
    	return new ScriptBuilder()  
    			.op(OP_2DUP) // Duplicate top
    			// Provjeri jel y € {0,1}
    			.number(0)
    			.number(2)
    			.op(OP_WITHIN)
    			.op(OP_VERIFY)
    			// Provjeri jel x € {0,1}
    			.number(0)
    			.number(2)
    			.op(OP_WITHIN)
    			.op(OP_VERIFY)
    			// Provjeri y==x
    			.op(OP_EQUAL)
                .build();
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedScript) {
        long x = 1;
        long y = 1;
        return new ScriptBuilder()
                .number(x)
                .number(y)
                .build();
    }
}
