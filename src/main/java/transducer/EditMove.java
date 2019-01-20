package transducer;

import org.sat4j.specs.TimeoutException;

import theory.BooleanAlgebra;
import utilities.Pair;

public class EditMove<P, S> extends TransducerMove<P, S> {
	P inGuard;
	P outGuard;
	boolean outputIsEqual;

	public EditMove(P inGuard, P outGuard, boolean outputIsEqual) {
		this.weight = (outputIsEqual) ? 0 : 1;
		this.inGuard = inGuard;
		this.outGuard = outGuard;
		this.outputIsEqual = outputIsEqual;
	}

	public EditMove(Integer from, Integer to, P inGuard, P outGuard, boolean outputIsEqual) {
		this.from = from;
		this.to = to;
		this.weight = (outputIsEqual) ? 0 : 1;
		this.inGuard = inGuard;
		this.outGuard = outGuard;
		this.outputIsEqual = outputIsEqual;
	}

	public void setDirection(Integer from, Integer to) {
		this.from = from;
		this.to = to;
	}

	public boolean isSatisfiable(BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (!ba.IsSatisfiable(inGuard) || !ba.IsSatisfiable(outGuard))
			return false;
		if (outputIsEqual) {
			P pred = ba.MkAnd(inGuard, outGuard);
			return ba.IsSatisfiable(pred);
		} else {
			if (!predSizeOne(inGuard, ba) || !predSizeOne(outGuard, ba))
				return true;
			return !ba.IsSatisfiable(ba.MkAnd(inGuard, outGuard));
		}
	}

	public Pair<S, S> getWitness(BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (outputIsEqual) {
			S wit = ba.generateWitness(ba.MkAnd(inGuard, outGuard));
			return new Pair<S, S>(wit, wit);
		} else {
			if (predSizeOne(inGuard, ba)) {
				S wit2 = ba.generateWitness(ba.MkAnd(ba.MkNot(inGuard), outGuard));
				return new Pair<S, S>(ba.generateWitness(inGuard), wit2);
			} else {
				S wit2 = ba.generateWitness(outGuard);
				P pred1 = ba.MkAnd(ba.MkNot(ba.MkAtom(wit2)), inGuard);
				S wit1 = ba.generateWitness(pred1);
				return new Pair<S, S>(wit1, wit2);
			}
		}
	}

	public boolean hasModel(S input, S output, BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (ba.HasModel(inGuard, input) && ba.HasModel(outGuard, output)) {
			P atomi = ba.MkAtom(input);
			P atomo = ba.MkAtom(output);
			if (outputIsEqual)
				return ba.IsSatisfiable(ba.MkAnd(atomi, atomo));
			else
				return !ba.IsSatisfiable(ba.MkAnd(atomi, atomo));
		}
		return false;
	}

	public boolean isEpsilonTransition() {
		return false;
	}

	private boolean predSizeOne(P pred, BooleanAlgebra<P, S> ba) throws TimeoutException {
		P witPred = ba.MkAtom(ba.generateWitness(pred));
		return !ba.IsSatisfiable(ba.MkAnd(ba.MkNot(witPred), pred));
	}

	@Override
	public String toString() {
		return "[edit " + from + "->" + to + "  " + weight + "]";
	}

}
