package automata.seqt;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;

public class AddMove<P, S> extends TransducerMove<P, S> {
	public P outGuard;

	public AddMove(Integer from, Integer to, P outGuard) {
		this.from = from;
		this.to = to;
		this.weight = 1;
		this.outGuard = outGuard;
	}

	public boolean isSatisfiable(BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.IsSatisfiable(outGuard);
	}

	public S getWitness(BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.generateWitness(outGuard);
	}

	public boolean hasModel(S input, S output, BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.HasModel(outGuard, output);
	}

	public boolean isEpsilonTransition() {
		return true;
	}
	
	@Override
	public String toString() {
		return "[add "+from+"->"+to+"]";
	}
}
