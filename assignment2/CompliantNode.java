import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

	private Set<Transaction> all;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
		this.all = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
		// NOP
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.all.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
		return this.all;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
		for (Candidate c : candidates) {
			this.all.add(c.tx);
		}
    }
}
