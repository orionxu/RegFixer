package edu.wisc;
import static org.junit.Assert.assertEquals;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import edu.wisc.regfixer.automata.Automaton;
import edu.wisc.regfixer.parser.Main;
import theory.BooleanAlgebra;
import theory.characters.CharPred;
import theory.intervals.UnaryCharIntervalSolver;
import transducer.SymbolicTransducer;
@Ignore
public class TransducerTest {
	@Test
	public void test1() throws Exception {
		SFA<CharPred, Character> strSFA = makeStrSFA("expect_add_dash.txt");
		SFA<CharPred, Character> regexSFA = new Automaton(Main.parse("test_(\\w)+\\.txt")).getSFA();
		SymbolicTransducer<CharPred, Character> st = SymbolicTransducer.<CharPred, Character>editComposition(strSFA, regexSFA, new UnaryCharIntervalSolver());
		assertEquals(st.shortestPath(), 4);
	}
	
	@Test
	public void test2() throws Exception {
		SFA<CharPred, Character> strSFA = makeStrSFA("test_specialize_word.txt");
		SFA<CharPred, Character> regexSFA = new Automaton(Main.parse("test_(\\w)+\\.txt")).getSFA();
		SymbolicTransducer<CharPred, Character> st = SymbolicTransducer.<CharPred, Character>editComposition(strSFA, regexSFA, new UnaryCharIntervalSolver());
		assertEquals(st.shortestPath(), 0);
	}
	
	@Test
	public void test3() throws Exception {
		SFA<CharPred, Character> strSFA = makeStrSFA("123-456-789");
		SFA<CharPred, Character> regexSFA = new Automaton(Main.parse("\\d{3}-\\d{2}-\\d{4}")).getSFA();
		SymbolicTransducer<CharPred, Character> st = SymbolicTransducer.<CharPred, Character>editComposition(strSFA, regexSFA, new UnaryCharIntervalSolver());
		assertEquals(st.shortestPath(), 2);
	}
	
	@Test
	public void test4() throws Exception {
		SFA<CharPred, Character> strSFA = makeStrSFA("13:12:06");
		SFA<CharPred, Character> regexSFA = new Automaton(Main.parse("(([0-1]?[0-9])|([2][0-3])):([0-5]?[0-9])(:([0-5]?[0-9]))?")).getSFA();
		SymbolicTransducer<CharPred, Character> st = SymbolicTransducer.<CharPred, Character>editComposition(strSFA, regexSFA, new UnaryCharIntervalSolver());
		assertEquals(st.shortestPath(), 0);
	}
	
	public static SFA<CharPred, Character> makeStrSFA(String s) throws TimeoutException {
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
