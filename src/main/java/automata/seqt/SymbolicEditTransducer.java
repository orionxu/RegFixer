package automata.seqt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.sat4j.specs.TimeoutException;

import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import theory.BooleanAlgebra;
import utilities.Pair;

public class SymbolicEditTransducer<P, S> {

	public Integer initialState;
	public Collection<Integer> states;
	public Collection<Integer> finalStates;

	public Map<Integer, Collection<TransducerMove<P, S>>> movesFrom;
	public Map<Integer, Collection<TransducerMove<P, S>>> movesTo;

	public Integer transitionCount;

	public SymbolicEditTransducer() {
		this.initialState = 0;
		this.finalStates = new HashSet<Integer>();
		this.movesFrom = new HashMap<Integer, Collection<TransducerMove<P, S>>>();
		this.movesTo = new HashMap<Integer, Collection<TransducerMove<P, S>>>();
		this.states = new HashSet<Integer>();
		this.transitionCount = 0;
	}

	public SymbolicEditTransducer(Collection<TransducerMove<P, S>> moves, Integer initialState,
			Collection<Integer> finalStates) {
		this.initialState = initialState;
		this.finalStates = finalStates;
		this.states = new HashSet<Integer>();
		this.transitionCount = 0;
		this.movesFrom = new HashMap<Integer, Collection<TransducerMove<P, S>>>();
		this.movesTo = new HashMap<Integer, Collection<TransducerMove<P, S>>>();
		for (TransducerMove<P, S> m : moves) {
			addMove(m);
		}
	}

	public void addMove(TransducerMove<P, S> m) {
		if (!this.movesFrom.containsKey(m.from)) {
			this.movesFrom.put(m.from, new HashSet<TransducerMove<P, S>>());
		}
		this.movesFrom.get(m.from).add(m);
		if (!states.contains(m.from))
			states.add(m.from);
		if (!this.movesTo.containsKey(m.to)) {
			this.movesTo.put(m.to, new HashSet<TransducerMove<P, S>>());
		}
		this.movesTo.get(m.to).add(m);
		if (!states.contains(m.to))
			states.add(m.to);
		transitionCount++;
	}

	protected Collection<TransducerMove<P, S>> getInputMovesFrom(Collection<Integer> stateIDs) {
		Collection<TransducerMove<P, S>> transitions = new LinkedList<TransducerMove<P, S>>();
		for (Integer i : stateIDs) {
			transitions.addAll(movesFrom.get(i));
		}
		return transitions;
	}

	public static <P, S> SymbolicEditTransducer<P, S> editComposition(SFA<P, S> sfaStr, SFA<P, S> sfaAut,
			BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (sfaStr.isEmpty || sfaAut.isEmpty) {
			return new SymbolicEditTransducer<P, S>();
		}

		Collection<TransducerMove<P, S>> transitions = new ArrayList<>();
		Integer initialState = 0;
		Collection<Integer> finalStates = new ArrayList<>();

		HashMap<Pair<Integer, Integer>, Integer> reached = new HashMap<>();
		LinkedList<Pair<Integer, Integer>> toVisit = new LinkedList<>();

		Pair<Integer, Integer> initPair = new Pair<Integer, Integer>(sfaStr.initialState, sfaAut.initialState);
		reached.put(initPair, 0);
		toVisit.add(initPair);

		while (!toVisit.isEmpty()) {
			Pair<Integer, Integer> currentState = toVisit.removeFirst();
			int currentStateID = reached.get(currentState);
			if (sfaStr.isFinalState(currentState.first) && sfaAut.isFinalState(currentState.second)) {
				finalStates.add(currentStateID);
			}
			if (sfaStr.isFinalState(currentState.first)) {
				for (Move<P, S> im2 : sfaAut.getMovesFrom(currentState.second)) {
					if (im2.isEpsilonTransition()) {

						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(currentState.first, im2.to);
						int newStateID = getStateId(newState, reached, toVisit);
						EpsMove<P, S> eps = new EpsMove<P, S>(currentStateID, newStateID);
						transitions.add(eps);
					}
				}
				continue;
			}

			for (SFAInputMove<P, S> im1 : sfaStr.getInputMovesFrom(currentState.first)) {
				for (Move<P, S> im2 : sfaAut.getMovesFrom(currentState.second)) {
					if (im2.isEpsilonTransition()) {
						// Delete Move
						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(im1.to, im2.to);
						int newStateID = getStateId(newState, reached, toVisit);
						DeleteMove<P, S> dm = new DeleteMove<P, S>(currentStateID, newStateID, im1.guard);
						transitions.add(dm);
						newState = new Pair<Integer, Integer>(im1.from, im2.to);
						newStateID = getStateId(newState, reached, toVisit);
						EpsMove<P, S> eps = new EpsMove<P, S>(currentStateID, newStateID);
						transitions.add(eps);
					} else {
						SFAInputMove<P, S> im2i = (SFAInputMove<P, S>) im2;
						// No edit move
						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(im1.to, im2.to);
						EditMove<P, S> nem = new EditMove<P, S>(im1.guard, im2i.guard, true);
						if (nem.isSatisfiable(ba)) {
							int newStateID = getStateId(newState, reached, toVisit);
							nem.setDirection(currentStateID, newStateID);
							transitions.add(nem);
						}
						// Edit move
						EditMove<P, S> em = new EditMove<P, S>(im1.guard, im2i.guard, false);
						if (em.isSatisfiable(ba)) {
							int newStateID = getStateId(newState, reached, toVisit);
							em.setDirection(currentStateID, newStateID);
							transitions.add(em);
						}
						// Delete move
						newState = new Pair<Integer, Integer>(im1.to, im2.from);
						int newStateID = getStateId(newState, reached, toVisit);
						DeleteMove<P, S> dm = new DeleteMove<P, S>(currentStateID, newStateID, im1.guard);
						transitions.add(dm);
						// Add move
						newState = new Pair<Integer, Integer>(im1.from, im2.to);
						newStateID = getStateId(newState, reached, toVisit);
						AddMove<P, S> am = new AddMove<P, S>(currentStateID, newStateID, im2i.guard);
					}
				}
			}
		}
		return new SymbolicEditTransducer<P, S>(transitions, initialState, finalStates);
	}

	public Pair<Integer,List<S>> shortestPath(BooleanAlgebra<P, S> ba) throws TimeoutException {
		HashMap<Integer, VertexNode<S>> nodes = new HashMap<>();
		PriorityQueue<VertexNode<S>> q = new PriorityQueue<>();
		for (Integer i : states) {
			if (i == (int) initialState) {
				VertexNode<S> n = new VertexNode<>(i, 0, new LinkedList<>());
				nodes.put(initialState, n);
				q.add(n);
			} else {
				nodes.put(i, new VertexNode<S>(i, Integer.MAX_VALUE, null));
			}
		}
		while (!q.isEmpty()) {
			VertexNode<S> curr = q.poll();
			if (this.movesFrom.get(curr.stateID) == null)
				continue;
			for (TransducerMove<P, S> tm : this.movesFrom.get(curr.stateID)) {
				int alt = curr.weight + tm.weight;
				if (alt < nodes.get(tm.to).weight) {
					VertexNode<S> nodeTo = nodes.get(tm.to);
					nodeTo.weight = alt;
					List<S> newStr = new LinkedList<>(curr.str);
					if(tm instanceof EditMove) {
						EditMove<P,S> m = (EditMove<P,S>)tm;
						newStr.add(m.getWitness(ba).second);
					}else {
						if(tm instanceof AddMove) {
							AddMove<P,S> m = (AddMove<P,S>)tm;
							newStr.add(m.getWitness(ba));
						}else {
							//do nothing
						}
					}
					nodeTo.str = newStr;
					q.add(nodeTo);
				}
			}
		}
		int min = Integer.MAX_VALUE;
		List<S> minStr = null;
		for (Integer j : finalStates) {
			VertexNode<S> node = nodes.get(j);
			if (node.weight < min) {
				min = node.weight;
				minStr = node.str;
			}
		}
		return new Pair<>(min,minStr);
	}

	public static int getStateId(Pair<Integer, Integer> state, Map<Pair<Integer, Integer>, Integer> reached,
			LinkedList<Pair<Integer, Integer>> toVisit) {
		if (!reached.containsKey(state)) {
			int newId = reached.size();
			reached.put(state, newId);
			toVisit.add(state);
			return newId;
		} else
			return reached.get(state);
	}

	public boolean isFinalConfiguration(Collection<Integer> stateIDs) {
		stateIDs.retainAll(finalStates);
		return stateIDs.isEmpty();
	}

	public boolean isEmpty() {
		return states.isEmpty();
	}

	class VertexNode<S> implements Comparable<VertexNode<S>> {
		int stateID;
		int weight;
		List<S> str;

		public VertexNode(int id, int w, List<S> str) {
			stateID = id;
			weight = w;
			this.str=str;
		}

		@Override
		public int compareTo(VertexNode<S> other) {
			return this.weight - other.weight;
		}
	}

	static class VertexNodeL implements Comparable<VertexNodeL> {
		int level;
		int stateID;
		int weight;

		public VertexNodeL(int l, int id, int w) {
			level = l;
			stateID = id;
			weight = w;
		}

		@Override
		public int compareTo(VertexNodeL other) {
			if (level < other.level || (level == other.level && weight < other.weight))
				return -1;
			return 1;
		}
	}

	public static <P, S> SymbolicEditTransducer<P, S> linearSpaceED(SFA<P, S> sfaStr, SFA<P, S> sfaAut,
			BooleanAlgebra<P, S> ba) throws TimeoutException {
		if (sfaStr.isEmpty || sfaAut.isEmpty) {
			return new SymbolicEditTransducer<P, S>();
		}
		Pair<Integer, Integer> iCounter = new Pair<>(0, 1);
		HashMap<Pair<Integer, Integer>, VertexNodeL> nodes = new HashMap<>();
		PriorityQueue<VertexNodeL> q = new PriorityQueue<>();
		HashMap<Pair<Integer, Integer>, TransducerMove<P, S>> transitionsFrom = new HashMap<>();
		Collection<Pair<Integer, Integer>> finalStates = new ArrayList<>();
		// add l0 expand l1 compute l0
		LinkedList<Pair<Integer, Integer>> toVisit = new LinkedList<>();
		Pair<Integer, Integer> initPair = new Pair<Integer, Integer>(sfaStr.initialState, sfaAut.initialState);
		VertexNodeL initNode = new VertexNodeL(initPair.first, initPair.second, 0);
		toVisit.add(initPair);
		nodes.put(initPair, initNode);
		q.add(initNode);
		while (!toVisit.isEmpty()) {
			Pair<Integer, Integer> currentState = toVisit.removeFirst();
			if (sfaStr.isFinalState(currentState.first) && sfaAut.isFinalState(currentState.second)) {
				finalStates.add(currentState);
			}
			if (sfaStr.isFinalState(currentState.first)) {
				for (Move<P, S> im2 : sfaAut.getMovesFrom(currentState.second)) {
					if (im2.isEpsilonTransition()) {

						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(currentState.first, im2.to);
						//int newStateID = getStateId(newState, reached, toVisit);
						//EpsMove<P, S> eps = new EpsMove<P, S>(currentStateID, newStateID);
						//transitions.add(eps);
					}
				}
				continue;
			}

			for (SFAInputMove<P, S> im1 : sfaStr.getInputMovesFrom(currentState.first)) {
				for (Move<P, S> im2 : sfaAut.getMovesFrom(currentState.second)) {
					if (im2.isEpsilonTransition()) {
						// Delete Move
						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(im1.to, im2.to);
						//int newStateID = getStateId(newState, reached, toVisit);
						//DeleteMove<P, S> dm = new DeleteMove<P, S>(currentStateID, newStateID, im1.guard);
						//transitions.add(dm);
						newState = new Pair<Integer, Integer>(im1.from, im2.to);
						//newStateID = getStateId(newState, reached, toVisit);
						//EpsMove<P, S> eps = new EpsMove<P, S>(currentStateID, newStateID);
						//transitions.add(eps);
					} else {
						SFAInputMove<P, S> im2i = (SFAInputMove<P, S>) im2;
						// No edit move
						Pair<Integer, Integer> newState = new Pair<Integer, Integer>(im1.to, im2.to);
						EditMove<P, S> nem = new EditMove<P, S>(im1.guard, im2i.guard, true);
						if (nem.isSatisfiable(ba)) {
							//int newStateID = getStateId(newState, reached, toVisit);
							//nem.setDirection(currentStateID, newStateID);
							//transitions.add(nem);
						}
						// Edit move
						EditMove<P, S> em = new EditMove<P, S>(im1.guard, im2i.guard, false);
						if (em.isSatisfiable(ba)) {
							//int newStateID = getStateId(newState, reached, toVisit);
							//em.setDirection(currentStateID, newStateID);
							//transitions.add(em);
						}
						// Delete move
						newState = new Pair<Integer, Integer>(im1.to, im2.from);
						//int newStateID = getStateId(newState, reached, toVisit);
						//DeleteMove<P, S> dm = new DeleteMove<P, S>(currentStateID, newStateID, im1.guard);
						//transitions.add(dm);
						// Add move
						newState = new Pair<Integer, Integer>(im1.from, im2.to);
						//newStateID = getStateId(newState, reached, toVisit);
						//AddMove<P, S> am = new AddMove<P, S>(currentStateID, newStateID, im2i.guard);
					}
				}
			}
		}
		// loop

//		for (Integer i : states) {
//			if (i == (int) initialState) {
//				VertexNode n = new VertexNode(i, 0);
//				nodes.put(initialState, n);
//				q.add(n);
//			} else {
//				nodes.put(i, new VertexNode(i, Integer.MAX_VALUE));
//			}
//		}
		return null;
	}

}
