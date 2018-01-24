import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.lang.System;

public class TxHandler {

	UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

		HashSet<UTXO> used = new HashSet<UTXO>();

		double inputSum = 0.0;
		for (int i = 0; i < tx.getInputs().size(); i++) {
			Transaction.Input input = tx.getInputs().get(i);
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

			Transaction.Output prevOutput = this.utxoPool.getTxOutput(utxo);
			if (prevOutput == null) {
				return false;
			}
			if (!Crypto.verifySignature(
				prevOutput.address,
				tx.getRawDataToSign(i),
				input.signature
			)) {
				return false;
			}

			if (used.contains(utxo)) {
				return false;
			}
			used.add(utxo);

			inputSum += prevOutput.value;
		}

		double outputSum = 0.0;
		for (Transaction.Output output : tx.getOutputs()) {
			if (output.value <= 0.0) {
				return false;
			}
			outputSum += output.value;	
		}

		return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
		List<Transaction> accepted = new ArrayList<Transaction>();

		boolean anyAccepted = true;
		while (anyAccepted) {
			anyAccepted = false;
			for (Transaction txn : possibleTxs) {
				if (!this.isValidTx(txn)) {
					continue;
				}

				for (Transaction.Input in : txn.getInputs()) {
					this.utxoPool.removeUTXO(
						new UTXO(in.prevTxHash, in.outputIndex));

				}
				for (int i = 0; i < txn.getOutputs().size(); i++) {
					Transaction.Output out = txn.getOutputs().get(i);
					this.utxoPool.addUTXO(new UTXO(txn.getHash(), i), out);
				}
				anyAccepted = true;
				accepted.add(txn);
			}
		}

		Transaction[] result = new Transaction[accepted.size()];
		accepted.toArray(result);
		return result;
    }
}
