package automata.seqt;

import theory.BooleanAlgebra;

public class EpsMove<P, S> extends TransducerMove<P, S> {
	public EpsMove(int f, int t) {
		this.from = f;
		this.to = t;
		this.weight = 0;
	}

	public boolean isSatisfiable(BooleanAlgebra<P, S> ba) {
		return true;
	}

	public boolean isEpsilonTransition() {
		return true;
	}
	
	@Override
	public String toString() {
		return "[eps "+from+"->"+to+"]";
	}
}
