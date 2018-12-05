package automata.seqt;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;

public class DeleteMove<P, S> extends TransducerMove<P, S> {
	public P inGuard;

	public DeleteMove(Integer from, Integer to, P inGuard) {
		this.from = from;
		this.to = to;
		this.weight = 1;
		this.inGuard = inGuard;
	}

	public boolean isSatisfiable(BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.IsSatisfiable(inGuard);
	}

	public boolean hasModel(S input, S output, BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.HasModel(inGuard, input);
	}
	
	public S getWitness(BooleanAlgebra<P, S> ba) throws TimeoutException {
		return ba.generateWitness(inGuard);
	}

	public boolean isEpsilonTransition() {
		return false;
	}
	
	@Override
	public String toString() {
		return "[del "+from+"->"+to+"]";
	}
}
