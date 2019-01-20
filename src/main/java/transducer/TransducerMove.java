package transducer;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;

public abstract class TransducerMove<P, S> {

	public Integer from;
	public Integer to;
	public int weight;

	public abstract boolean isSatisfiable(BooleanAlgebra<P, S> ba) throws TimeoutException;

	public abstract boolean isEpsilonTransition();

}
