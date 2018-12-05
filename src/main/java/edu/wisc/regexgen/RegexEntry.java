package edu.wisc.regexgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sat4j.specs.TimeoutException;

import automata.seqt.SymbolicEditTransducer;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import edu.wisc.regfixer.automata.Automaton;
import edu.wisc.regfixer.parser.RegexNode;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.Pair;

public class RegexEntry implements Comparable<RegexEntry>{

	private String exp;
	private RegexNode tree;
	private ArrayList<String> posNotMatch;
	private ArrayList<Integer> posED;
	private ArrayList<String> negMatch;
	private ArrayList<Integer> negED;
	private double acceptRate;
	private double rejectRate;
	private double cost;

	public RegexEntry(String r, RegexNode t) {
		exp = r;
		tree = t;
		posNotMatch = new ArrayList<String>();
		negMatch = new ArrayList<String>();
		acceptRate = -1.0;
		rejectRate = -1.0;
		cost = Double.MAX_VALUE;
	}

	public String getExpression() {
		return this.exp;
	}

	public RegexNode getTree() {
		return this.tree;
	}

	public void calculatePerformance(double totalPos, double totalNeg) {
		acceptRate = 100 - posNotMatch.size() * 100 / totalPos;
		rejectRate = 100 - negMatch.size() * 100 / totalNeg;
	}

	public double getAcceptRate() {
		return acceptRate;
	}

	public double getRejectRate() {
		return rejectRate;
	}

	public void failPos(String p) {
		this.posNotMatch.add(p);
	}

	public void failNeg(String n) {
		this.negMatch.add(n);
	}

	public void calculateEditDistance() throws TimeoutException {
		BooleanAlgebra<CharPred, Character> ba = new UnaryCharIntervalSolver();
		this.posED = new ArrayList<Integer>();
		this.negED = new ArrayList<Integer>();
		Automaton aut = null;
		try {
			aut = new Automaton(this.tree);
		} catch (TimeoutException e) {
			System.err.println("Timeout creating automaton. ");
		}

		SFA<CharPred, Character> sfaPos = null;
		try {
			sfaPos = aut.getSFA().removeEpsilonMoves(new UnaryCharIntervalSolver());
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}
		for (String p : this.posNotMatch) {
			SFA<CharPred, Character> strSFA = makeStrSFA(p);
			SymbolicEditTransducer<CharPred, Character> st = SymbolicEditTransducer.<CharPred, Character>editComposition(strSFA, sfaPos, ba);
			Pair<Integer, List<Character>> result = st.shortestPath(ba);
			posED.add(result.first);

		}

		SFA<CharPred, Character> sfaNeg = null;
		try {
			sfaNeg = aut.getSFA().complement(new UnaryCharIntervalSolver()).removeEpsilonMoves(new UnaryCharIntervalSolver());
		} catch (TimeoutException e) {
			System.err.println("Error: Failed taking complement of SFA. ");
		}

		for (String n : this.negMatch) {
			SFA<CharPred, Character> strSFA = makeStrSFA(n);
			SymbolicEditTransducer<CharPred, Character> st = SymbolicEditTransducer.<CharPred, Character>editComposition(strSFA, sfaNeg, ba);
			Pair<Integer, List<Character>> result = st.shortestPath(ba);
			negED.add(result.first);
		}
	}
	
	public void cost() {
		double avgPos = 0.0;
		if (posED.size() > 0) {
			for (Integer i: posED) {
				avgPos += i;
			}
			avgPos /= posED.size();
		}
		this.cost = avgPos - (100 - rejectRate);
	}

	@Override
	public String toString() {
		String result = "";
		result += this.exp + "    ";
		result += String.format("%.2f  %.2f    ", this.acceptRate, this.rejectRate);
		result += posED;
		result += negED;
		result += "  Cost: " + this.cost;
		return result;
	}

	@Override
	public int compareTo(RegexEntry o) {
		if (this.cost < o.cost) {
			return -1;
		} else {
			return 1;
		}
	}
	
	private static SFA<CharPred, Character> makeStrSFA(String s) throws TimeoutException {
		UnaryCharIntervalSolver ba = new UnaryCharIntervalSolver();
		int l = s.length();
		int initialState = 0;
		Collection<SFAMove<CharPred, Character>> transitions = new ArrayList<SFAMove<CharPred, Character>>();
		for (int i = 0; i < l; i++) {
			transitions.add(new SFAInputMove<CharPred, Character>(i, i + 1, ba.MkAtom(s.charAt(i))));
		}
		Collection<Integer> finalStates = new ArrayList<Integer>();
		finalStates.add(l);
		return SFA.<CharPred, Character>MkSFA(transitions, initialState, finalStates, ba);
	}

}
