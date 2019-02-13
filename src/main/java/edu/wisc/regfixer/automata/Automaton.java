package edu.wisc.regfixer.automata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import automata.Move;
import automata.sfa.SFA;
import automata.sfa.SFAInputMove;
import automata.sfa.SFAMove;
import edu.wisc.regfixer.enumerate.Layer;
import edu.wisc.regfixer.enumerate.UnknownBounds;
import edu.wisc.regfixer.enumerate.UnknownChar;
import edu.wisc.regfixer.enumerate.UnknownId;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharDotNode;
import edu.wisc.regfixer.parser.CharEscapedNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;
import org.sat4j.specs.TimeoutException;
import theory.characters.CharPred;
import theory.characters.StdCharPred;
import theory.intervals.UnaryCharIntervalSolver;
import utilities.Pair;

public class Automaton extends automata.Automaton {
  public static UnaryCharIntervalSolver solver = new UnaryCharIntervalSolver();
  public static final CharPred Num = StdCharPred.NUM;
  public static final CharPred NotNum = StdCharPred.SPACES;
  public static final CharPred Spaces = StdCharPred.WORD;
  public static final CharPred NotSpaces = solver.MkNot(StdCharPred.NUM);
  public static final CharPred Word = solver.MkNot(StdCharPred.SPACES);
  public static final CharPred NotWord = solver.MkNot(StdCharPred.WORD);

  private final SFA<CharPred, Character> sfa;
  public Map<UnknownId, Set<Integer>> unknownToExitStates = new HashMap<>();
  public Map<UnknownId, Integer> unknownToEntryState = new HashMap<>();
  public Map<Integer, Set<Integer>> moveTo = new HashMap<>();
  // states which have at least one non-epsilon transition
  public Set<Integer> coreStates = new HashSet<>();

  public Automaton (RegexNode tree) throws TimeoutException {
    Automaton aut = nodeToAutomaton(tree);
    this.sfa = aut.sfa;
    this.unknownToExitStates = aut.unknownToExitStates;
    this.unknownToEntryState = aut.unknownToEntryState;
  }

  public Automaton (CharPred predicate) throws TimeoutException {
    this.sfa = predicateToSFA(predicate);
  }

  private Automaton (SFA<CharPred, Character> sfa) {
    this.sfa = sfa;
  }

  /**
   * METHODS FOR EVALUATING THE AUTOMATON
   */

  private List<State> getEpsClosure (State frontier) {
    return getEpsClosureDest(Arrays.asList(frontier), null);
  }
  
  private List<State> getEpsClosure (List<State> frontier) {
    return getEpsClosureDest(frontier, null);
  }

  /*private List<State> getEpsClosure (List<State> frontier) {
    List<State> reached = new LinkedList<>(frontier);
    Set<Integer> seenStateIds = frontier.stream()
      .map(s -> s.getStateId())
      .collect(Collectors.toSet());
    LinkedList<State> toVisit = new LinkedList<>(frontier);

    while (toVisit.size() > 0) {
      State currState = toVisit.removeFirst();
      for (Move<CharPred, Character> move : getMovesFrom(currState.getStateId())) {
        if (move.isEpsilonTransition()) {
          State newState = new State(move.to, currState);
          reached.add(newState);

          if (false == seenStateIds.contains(newState.getStateId())) {
            toVisit.add(newState);
            seenStateIds.add(newState.getStateId());
          }
        }
      }
    }

    return reached;
  }*/

  private List<State> getEpsClosureDest (List<State> frontier, Set<Integer> dest) {
    List<State> res = new LinkedList<>();
    for (State s : frontier) {
    	List<State> curstates = getEpsClosureForOneState(s);
    	if (dest != null) {
	    	for (State curstate : curstates) {
	    		if (dest.contains(curstate.getStateId()))
	    			res.add(curstate);
	    	}
    	} else {
    		res.addAll(curstates);
    	}
    	/*int currId = s.getStateId();
    	for (int reach : moveTo.get(currId)) {
    		res.add(new State(reach, s));
    	}*/
    }
    return res;
  }
  
  /*private List<State> getEpsClosureWithDest (State frontier, Set<Integer> dest) {
    return getEpsClosureWithDest(Arrays.asList(frontier), dest);
  }
  
  private List<State> getEpsClosureWithDest (List<State> frontier, Set<Integer> dest) {
    List<State> res = new LinkedList<>();
    for (State s : frontier) {
    	int currId = s.getStateId();
    	for (int reach : moveTo.get(currId)) {
    		if (dest.contains(reach))
    			res.add(new State(reach, s));
    	}
    }
    return res;
  }
  
  private List<State> getEpsClosureWithDestForOne (State frontier, Set<Integer> dest) {
	  List<State> res = new LinkedList<>();
      int currId = frontier.getStateId();
      for (int reach : moveTo.get(currId)) {
    	  if (dest.contains(reach))
    	      res.add(new State(reach, frontier));
      }
	  return res;
  }*/
  
  private List<State> getEpsClosureForOneState (State frontier) {
    List<State> reached = new LinkedList<>();
    reached.add(frontier);
    Set<Integer> seenStateIds = new HashSet<>();
    seenStateIds.add(frontier.getStateId());
    LinkedList<State> toVisit = new LinkedList<>();
    toVisit.add(frontier);
    
    while (toVisit.size() > 0) {
      State currState = toVisit.removeFirst();
      for (Move<CharPred, Character> move : getMovesFrom(currState.getStateId())) {
        if (move.isEpsilonTransition()) {
          State newState = new State(move.to, currState);
          reached.add(newState);

          if (false == seenStateIds.contains(newState.getStateId())) {
            toVisit.add(newState);
            seenStateIds.add(newState.getStateId());
          }
        }
      }
    }

    return reached;
  }
  
  public void IniMoveTo() {
	  for (int curr : this.getStates()) {
		  moveTo.put(curr, getReachableId(curr));
	  }
  }
  
  private Set<Integer> getReachableId (int from) {
    Set<Integer> reached = new HashSet<>();
    reached.add(from);
    Set<Integer> seenStateIds = new HashSet<>();
    seenStateIds.add(from);
    LinkedList<Integer> toVisit = new LinkedList<>();
    toVisit.add(from);
    
    while (toVisit.size() > 0) {
      int currId = toVisit.removeFirst();
      for (Move<CharPred, Character> move : getMovesFrom(currId)) {
        if (move.isEpsilonTransition()) {
          int newId = move.to;
          reached.add(newId);

          if (false == seenStateIds.contains(newId)) {
            toVisit.add(newId);
            seenStateIds.add(newId);
          }
        }
      }
    }

    // remove non-final states that only have epsilon transitions
    Iterator<Integer> itr = reached.iterator();
    while(itr.hasNext()) {
    	int id = itr.next();
    	if (!isFinalState(id)) {
    		boolean flag = true;
    		for (Move<CharPred, Character> move : getMovesFrom(id)) {
    			if (!move.isEpsilonTransition()) {
    				flag = false;
    				break;
    			}
    		}
    		if (flag)
    			itr.remove();
    	}
    }
    
    return reached;
  }
  
  public void collectId () {
	  for (Map.Entry<Integer, Set<Integer>> entry : this.moveTo.entrySet()) {
		  this.coreStates.addAll(entry.getValue());
	  }
	  this.coreStates.remove(getFinalStates());
  }
  
  public List<State> getNextState (List<State> frontier, Character ch) throws TimeoutException {
    return getNextState(null, frontier, ch);
  }

  public List<State> getNextState (Set<Integer> filter, List<State> frontier, Character ch) throws TimeoutException {
    List<State> nextStates = new LinkedList<>();

    for (State state : frontier) {
      nextStates.addAll(getNextState(filter, state, ch));
    }

    return nextStates;
  }

  public List<State> getNextState (Set<Integer> filter, State parent, Character ch) throws TimeoutException {
    List<State> nextStates = new LinkedList<>();

    for (Move<CharPred, Character> move : getMovesFrom(parent.getStateId())) {
      if (move.isEpsilonTransition() == false) {
        if (move.hasModel(ch, Automaton.solver) && (filter == null || filter.contains(move.to))) {
          State newState = new State(move.to, parent, ch);

          // Check if the predicate relating to the automaton transition is
          // associated with a unknown ID and if so, associate the newly created
          // state with that unknown ID too.
          if (move instanceof SFAInputMove) {
            SFAInputMove moveCast = (SFAInputMove) move;

            if (moveCast.guard instanceof UnknownPred) {
              UnknownPred predCast = (UnknownPred) moveCast.guard;
              UnknownId holdId = predCast.getId();
              newState = new State(move.to, parent, ch, holdId);
            }
          }

          nextStates.add(newState);
        }
      }
    }

    return nextStates;
  }
  
  private Set<Integer> getNextState (int stateId, Character ch) throws TimeoutException {
	  Set<Integer> nextStates = new HashSet<>();
	
	    for (Move<CharPred, Character> move : getMovesFrom(stateId)) {
	      if (move.isEpsilonTransition() == false) {
	        if (move.hasModel(ch, Automaton.solver)) {
	
	          nextStates.add(move.to);
	        }
	      }
	    }
	
	    return nextStates;
  }

  private boolean isFinalConfiguration (List<State> states) {
    for (State state : states) {
      if (isFinalState(state.getStateId())) {
        return true;
      }
    }

    return false;
  }

  private Route traceFromState (State endState) {
    Map<UnknownId, Set<Character>> crosses = new HashMap<>();
    Map<UnknownId, Stack<Integer>> quantTally = new HashMap<>();
    Map<UnknownId, Boolean> outGuard = new HashMap<>();

    for (UnknownId id : this.unknownToEntryState.keySet()) {
      quantTally.put(id, new Stack<>());
      //quantTally.get(id).push(null);
    }

    State currState = endState;
    State prevState = null;
    while (currState != null) {
      // Compute which characters cross unknown character classes.
      if (currState.getValue() != null && currState.getId() != null) {
        char value = currState.getValue();
        UnknownId id = currState.getId();

        if (crosses.containsKey(id) == false) {
          crosses.put(id, new HashSet<>());
        }

        crosses.get(id).add(value);
      }

      for (UnknownId id : this.unknownToEntryState.keySet()) {
        Integer entryStateId = this.unknownToEntryState.get(id);
        if (!outGuard.containsKey(id) || outGuard.get(id) == false)
          continue;
        if (entryStateId == currState.getStateId()) {
          Integer old = quantTally.get(id).pop();

          //if (old == null) {
          if (old == 0) {
            old = 0;
          }

          quantTally.get(id).push(old + 1);
          outGuard.put(id, false);
        }
      }

      for (UnknownId id : this.unknownToExitStates.keySet()) {
        Set<Integer> exitStateIds = this.unknownToExitStates.get(id);
        if (exitStateIds.contains(currState.getStateId())) {
          outGuard.put(id, true);
          if (prevState != null && this.unknownToEntryState.get(id) != null) {
             if (this.unknownToEntryState.get(id) != prevState.getStateId()) {
               // This exit does NOT loop back to the start of this quantifier
               // so push a new counter onto the tally stack.
               //quantTally.get(id).push(null);
               quantTally.get(id).push(0);
             }
          } else if (prevState == null) {
        	  quantTally.get(id).push(0);
          }
        }
      }

      prevState = currState;
      currState = currState.getParent();
    }

    Map<UnknownId, Set<Integer>> exits = new HashMap<>();
    for (Map.Entry<UnknownId, Stack<Integer>> entry : quantTally.entrySet()) {
      // Convert from Stack<Integer> to Set<Integer> and remove any null values.
      exits.put(entry.getKey(), entry.getValue().stream()
        .filter(i -> i != null)
        .collect(Collectors.toSet()));
    }

    return new Route(crosses, exits);
  }

  public boolean accepts (String str) throws TimeoutException {
    List<Character> charList = new LinkedList<>();

    for (int i = 0; i < str.length(); i++) {
      charList.add(str.charAt(i));
    }

    return accepts(charList);
  }

  public boolean accepts (List<Character> chars) throws TimeoutException {
    List<State> frontier = getEpsClosure(new State(getInitialState()));

    for (Character ch : chars) {
      frontier = getNextState(frontier, ch);
      frontier = getEpsClosure(frontier);

      if (frontier.isEmpty()) {
        return false;
      }
    }

    return isFinalConfiguration(frontier);
  }

  private Set<Integer> getReverseEpsClosure (Stack<Set<Integer>> layers, Integer frontier) {
    return getReverseEpsClosure(layers, new HashSet<>(frontier));
  }

  private Set<Integer> getReverseEpsClosure (Stack<Set<Integer>> layers, Set<Integer> frontier) {
    Set<Integer> reached = new HashSet<>(frontier);
    Set<Integer> seen    = new HashSet<>(frontier);
    Set<Integer> toVisit = new HashSet<>(frontier);

    while (toVisit.size() > 0) {
      Integer curr = toVisit.iterator().next();
      toVisit.remove(curr);
      // layers.peek().add(curr);

      for (Move<CharPred, Character> move : getMovesTo(curr)) {
        if (move.isEpsilonTransition()) {
          Integer prev = move.from;
          reached.add(prev);

          if (false == seen.contains(prev)) {
            toVisit.add(prev);
            seen.add(prev);
          }
        }
      }
    }

    return reached;
  }

  public Set<Integer> getPrevState (Stack<Set<Integer>> layers, Set<Integer> frontier, Character ch) throws TimeoutException {
    Set<Integer> prevStates = new HashSet<>();

    for (Integer stateId : frontier) {
      prevStates.addAll(getPrevState(layers, stateId, ch));
    }

    return prevStates;
  }

  public Set<Integer> getPrevState (Stack<Set<Integer>> layers, Integer child, Character ch) throws TimeoutException {
    Set<Integer> prevStates = new HashSet<>();

    for (Move<CharPred, Character> move : getMovesTo(child)) {
      if (move.isEpsilonTransition() == false) {
        if (move.hasModel(ch, Automaton.solver)) {
          prevStates.add(move.from);
          layers.peek().add(move.to);
        }
      }
    }

    return prevStates;
  }

  private List<Set<Integer>> getStateFilter (String source) throws TimeoutException {
    Stack<Set<Integer>> layers = new Stack<>();
    // layers.push(new HashSet<>());
    Set<Integer> frontier = getReverseEpsClosure(layers, new HashSet<>(getFinalStates()));

    for (int i = source.length()-1; i >= 0; i--) {
      layers.push(new HashSet<>());
      frontier = getPrevState(layers, frontier, source.charAt(i));
      // layers.push(new HashSet<>());
      frontier = getReverseEpsClosure(layers, frontier);

      if (frontier.isEmpty()) {
        return new Stack<>();
      }
    }

    List<Set<Integer>> layerList = new LinkedList<>(layers);
    Collections.reverse(layerList);
    return layerList;
  }

  public Set<Route> trace (String source) throws TimeoutException {
	  boolean useElimination = true;
	  List<State> frontier;
	  List<Set<Integer>> valid = null;
	  if (useElimination) {
		  Layer[] net = this.buildTrans(source);
		  valid = new LinkedList<>();
		  for (int i = 0; i < source.length() + 1; i++) {
			  valid.add(net[i].reachFinal);
		  }
	      //frontier = getEpsClosureWithDest(new State(getInitialState()), valid.get(0));
	      frontier = getEpsClosureDest(Arrays.asList(new State(getInitialState())), valid.get(0));
	  } else {
		  frontier = getEpsClosureDest(Arrays.asList(new State(getInitialState())), null);
	  }
    for (int i = 0; i < source.length(); i++) {
      /*if (layers.size() <= i) {
        return new HashSet<>();
      }

      Set<Integer> filter = layers.get(i);
      frontier = getNextState(filter, frontier, source.charAt(i));*/
      frontier = getNextState(frontier, source.charAt(i));
      if (useElimination) {
    	  frontier = getEpsClosureDest(frontier, valid.get(i + 1));
      } else {
    	  frontier = getEpsClosureDest(frontier, null);
      }
      if (frontier.isEmpty()) {
        return new HashSet<>();
      }
    }

    return frontier.stream()
      .filter(s -> isFinalState(s.getStateId()))
      .map(s -> traceFromState(s))
      .collect(Collectors.toSet());
  }

  public List<Map<UnknownId, Set<Character>>> computeRuns (String source) throws TimeoutException {
    List<Map<UnknownId, Set<Character>>> accum = new LinkedList<>();

    for (Route route : this.trace(source)) {
      accum.add(route.getSpans());
    }

    return accum;
  }

  /**
   * METHODS FOR BUILDING THE AUTOMATON AND ITS PREDICATES
   */

  private static SFA<CharPred, Character> predicateToSFA (CharPred predicate) throws TimeoutException {
    Integer fromState = 0;
    Integer toState   = 1;
    SFAMove<CharPred, Character> move = new SFAInputMove(fromState, toState, predicate);
    List<SFAMove<CharPred, Character>> moves = new LinkedList<>();
    moves.add(move);
    return SFA.MkSFA(moves, fromState, Arrays.asList(toState), Automaton.solver);
  }

  public Collection<Integer> getStates () {
    return this.sfa.getStates();
  }

  public Collection<Integer> getFinalStates () {
    return this.sfa.getFinalStates();
  }

  public Integer getInitialState () {
    return this.sfa.getInitialState();
  }

  public Collection<Move<CharPred, Character>> getMovesTo (Integer state) {
    return this.sfa.getMovesTo(state);
  }

  public Collection<Move<CharPred, Character>> getMovesFrom (Integer state) {
    return this.sfa.getMovesFrom(state);
  }

  public static Automaton concatenate (Automaton first, Automaton second) throws TimeoutException {
    Automaton aut = new Automaton(SFA.concatenate(first.sfa, second.sfa, Automaton.solver));

    // Offset will be added to all watched states of the second Automaton
    // to ensure that the states of the first and second are disjointed.
    int offset = first.sfa.getMaxStateId() + 1;
    aut.unknownToExitStates.putAll(first.unknownToExitStates);
    for (Map.Entry<UnknownId, Set<Integer>> entry : second.unknownToExitStates.entrySet()) {
      aut.unknownToExitStates.put(entry.getKey(), new HashSet<>());
      for (Integer state : entry.getValue()) {
        aut.unknownToExitStates.get(entry.getKey()).add(state + offset);
      }
    }

    aut.unknownToEntryState.putAll(first.unknownToEntryState);
    for (Map.Entry<UnknownId, Integer> entry : second.unknownToEntryState.entrySet()) {
      aut.unknownToEntryState.put(entry.getKey(), entry.getValue() + offset);
    }

    return aut;
  }

  public static Automaton concatenate (List<Automaton> automata) throws TimeoutException {
    if (automata.size() == 0) {
      return getEmptyStringSFA();
    }

    Automaton unknown = null;

    for (Automaton next : automata) {
      if (unknown == null) {
        unknown = next;
      } else {
        unknown = concatenate(unknown, next);
      }
    }

    return unknown;
  }

  public static Automaton union (Automaton first, Automaton second) throws TimeoutException {
    Automaton aut = new Automaton(SFA.union(first.sfa, second.sfa, Automaton.solver));

    // Offset will be added to all watched states of the second Automaton
    // to ensure that the states of the first and second are disjointed.
    int offset = first.sfa.getMaxStateId() + 2;
    aut.unknownToExitStates.putAll(first.unknownToExitStates);
    for (Map.Entry<UnknownId, Set<Integer>> entry : second.unknownToExitStates.entrySet()) {
      aut.unknownToExitStates.put(entry.getKey(), new HashSet<>());
      for (Integer state : entry.getValue()) {
        aut.unknownToExitStates.get(entry.getKey()).add(state + offset);
      }
    }

    // Update entry state ID.
    for (Map.Entry<UnknownId, Integer> entry : first.unknownToEntryState.entrySet()) {
        aut.unknownToEntryState.put(entry.getKey(), entry.getValue());
      }
    for (Map.Entry<UnknownId, Integer> entry : second.unknownToEntryState.entrySet()) {
      //aut.unknownToEntryState.put(entry.getKey(), first.sfa.getMaxStateId() + offset + 1);
      aut.unknownToEntryState.put(entry.getKey(), entry.getValue() + offset);
    }

    return aut;
  }

  public static Automaton star (Automaton only) throws TimeoutException {
    Automaton aut = new Automaton(SFA.star(only.sfa, Automaton.solver));

    // Transfer watched states from the child Automaton to the new Automaton
    // without any need to rename states.
    aut.unknownToEntryState.putAll(only.unknownToEntryState);
    aut.unknownToExitStates.putAll(only.unknownToExitStates);

    return aut;
  }

  public static Automaton fromPredicate (Character ch) throws TimeoutException {
    return fromPredicate(new CharPred(ch));
  }

  public static Automaton fromPredicate (CharPred predicate) throws TimeoutException {
    return new Automaton(predicateToSFA(predicate));
  }

  public static Automaton fromPredicates (List<CharPred> predicates) throws TimeoutException {
    return fromPredicate(combinePredicates(predicates));
  }

  public static Automaton fromTruePredicate () throws TimeoutException {
    return fromPredicate(StdCharPred.TRUE);
  }

  public static Automaton fromUnknownPredicate (UnknownId id) throws TimeoutException {
    return fromPredicate(new UnknownPred(id));
  }

  public static Automaton fromInversePredicates (List<CharPred> predicates) throws TimeoutException {
    return fromPredicate(Automaton.solver.MkNot(combinePredicates(predicates)));
  }

  private static CharPred combinePredicates (List<CharPred> predicates) throws TimeoutException {
    CharPred unknown = null;

    for (CharPred next : predicates) {
      if (unknown == null) {
        unknown = next;
      } else {
        unknown = Automaton.solver.MkOr(unknown, next);
      }
    }

    return unknown;
  }

  public static Automaton empty () throws TimeoutException {
    return new Automaton(SFA.getEmptySFA(Automaton.solver));
  }

  /**
   * METHODS FOR CONVERTING FROM REGEX -> AUTOMATON
   */

  private static Automaton nodeToAutomaton (RegexNode node) throws TimeoutException {
         if (node instanceof ConcatNode)       return concatToAutomaton((ConcatNode) node);
    else if (node instanceof UnionNode)        return unionToAutomaton((UnionNode) node);
    else if (node instanceof RepetitionNode)   return repetitionToAutomaton((RepetitionNode) node);
    else if (node instanceof OptionalNode)     return optionalToAutomaton((OptionalNode) node);
    else if (node instanceof StarNode)         return starToAutomaton((StarNode) node);
    else if (node instanceof PlusNode)         return plusToAutomaton((PlusNode) node);
    else if (node instanceof UnknownChar)         return unknownToAutomaton((UnknownChar) node);
    else if (node instanceof CharClassSetNode) return charClassSetToAutomaton((CharClassSetNode) node);
    else if (node instanceof CharDotNode)      return charDotToAutomaton((CharDotNode) node);
    else if (node instanceof CharEscapedNode)  return charEscapedToAutomaton((CharEscapedNode) node);
    else if (node instanceof CharLiteralNode)  return charLiteralToAutomaton((CharLiteralNode) node);
    else {
      System.err.printf("Unknown AST class: %s\n", node.getClass().getName());
      throw new TimeoutException(); 
    }
  }

  private static Automaton concatToAutomaton (ConcatNode node) throws TimeoutException {
    List<Automaton> automata = new LinkedList<>();

    for (RegexNode child : node.getChildren()) {
      automata.add(nodeToAutomaton(child));
    }

    return concatenate(automata);
  }

  private static Automaton unionToAutomaton (UnionNode node) throws TimeoutException {
    Automaton left  = nodeToAutomaton(node.getLeftChild());
    Automaton right = nodeToAutomaton(node.getRightChild());
    return union(left, right);
  }

  private static Automaton repetitionToAutomaton (RepetitionNode node) throws TimeoutException {
    if (node.getBounds() instanceof UnknownBounds) {
      return repetitionWithUnknownBoundsToAutomaton(node);
    } else {
      return repetitionWithKnownBoundsToAutomaton(node);
    }
  }

  public static Automaton getEmptyStringSFA() throws TimeoutException {
    SFA<CharPred, Character> aut = new SFA<CharPred, Character>();
    aut.states = new HashSet<Integer>();
    aut.states.add(0);
    aut.finalStates = new HashSet<Integer>();
    aut.finalStates.add(0);
    aut.initialState = 0;
    aut.isDeterministic = true;
    aut.isEmpty = false;
    aut.isEpsilonFree = true;
    aut.maxStateId = 0;
    //aut.addTransition(new SFAInputMove<A, B>(0, 0, ba.True()), ba, true);
    return new Automaton(aut);
  }

  private static Automaton repetitionWithUnknownBoundsToAutomaton (RepetitionNode node) throws TimeoutException {
    Automaton sub = nodeToAutomaton(node.getChild());
    UnknownId unknown = ((UnknownBounds)node.getBounds()).getId();
    Integer entryState = sub.sfa.getInitialState();

    Automaton aut = star(sub);
    Set<Integer> exitStates = new HashSet<>(aut.sfa.getFinalStates());
    aut.unknownToExitStates.put(unknown, exitStates);
    aut.unknownToExitStates.putAll(sub.unknownToExitStates);
    aut.unknownToEntryState.put(unknown, entryState);

    return aut;
  }

  private static Automaton repetitionWithKnownBoundsToAutomaton (RepetitionNode node) throws TimeoutException {
    if (node.getBounds().hasMax() && node.getBounds().getMax() == 0) {
      return getEmptyStringSFA();
    }

    Automaton sub = nodeToAutomaton(node.getChild());
    Automaton min = getEmptyStringSFA();

    for (int i = 0; i < node.getBounds().getMin(); i++) {
      if (i == 0) {
        min = sub;
      } else {
        min = concatenate(min, sub);
      }
    }

    if (node.getBounds().hasMax() == false) {
      // min to infinite
      Automaton star = star(sub);
      return concatenate(min, star);
    } else if (node.getBounds().getMin() < node.getBounds().getMax()) {
      // min to max
      Automaton ithsfa = min;
      Automaton uptoith = min;

      for (int i = node.getBounds().getMin() + 1; i <= node.getBounds().getMax(); i++) {
        ithsfa = concatenate(ithsfa, sub);
        uptoith = union(uptoith, ithsfa);
      }

      return uptoith;
    } else {
      // just min becaue min = max
      return min;
    }
  }

  private static Automaton optionalToAutomaton (OptionalNode node) throws TimeoutException {
    return union(nodeToAutomaton(node.getChild()), getEmptyStringSFA());
  }

  private static Automaton starToAutomaton (StarNode node) throws TimeoutException {
    return star(nodeToAutomaton(node.getChild()));
  }

  private static Automaton plusToAutomaton (PlusNode node) throws TimeoutException {
    Automaton sub = nodeToAutomaton(node.getChild());
    return concatenate(sub, star(sub));
  }

  private static Automaton unknownToAutomaton (UnknownChar node) throws TimeoutException {
    return fromUnknownPredicate(node.getId());
  }

  private static Automaton charClassSetToAutomaton (CharClassSetNode node) throws TimeoutException {
    List<CharPred> predicates = new LinkedList<>();

    for (CharRangeNode charClass : node.getSubClasses()) {
      if (charClass.isSingle()) {
        char ch = charClass.getLeftChild().getChar();
        if (charClass.getLeftChild() instanceof CharEscapedNode) {
          predicates.add(predicateFromMetaChar(ch));
        } else {
          predicates.add(new CharPred(ch));
        }
      } else {
        char leftCh  = charClass.getLeftChild().getChar();
        char rightCh = charClass.getRightChild().getChar();
        predicates.add(new CharPred(leftCh, rightCh));
      }
    }

    if (node.isInverted()) {
      return fromInversePredicates(predicates);
    } else {
      return fromPredicates(predicates);
    }
  }

  private static Automaton charDotToAutomaton (CharDotNode node) throws TimeoutException {
    return fromTruePredicate();
  }

  private static Automaton charEscapedToAutomaton (CharEscapedNode node) throws TimeoutException {
    return fromPredicate(predicateFromMetaChar(node.getChar()));
  }

  private static Automaton charLiteralToAutomaton (CharLiteralNode node) throws TimeoutException {
    return fromPredicate(node.getChar());
  }

  private static CharPred predicateFromMetaChar (char ch) {
    switch (ch) {
      case 't': return new CharPred('\t');
      case 'n': return new CharPred('\n');
      case 'r': return new CharPred('\r');
      case 'f': return new CharPred('\f');
      case 'd': return Automaton.Num;
      case 'D': return Automaton.NotNum;
      case 's': return Automaton.Spaces;
      case 'S': return Automaton.NotSpaces;
      case 'w': return Automaton.Word;
      case 'W': return Automaton.NotWord;
      case 'v': throw new UnsupportedOperationException();
      case 'b': throw new UnsupportedOperationException();
      case 'B': throw new UnsupportedOperationException();
      default:  return new CharPred(ch);
    }
  }
  
  private Layer[] buildTrans(String s) throws TimeoutException {
	  this.collectId();
	  Set<Integer> states = this.coreStates;
	  int stateNum = states.size();
	  int len = s.length();
	  Layer[] net = new Layer[len + 1];
	  for (int i = 0; i < len + 1; i++) {
		  net[i] = new Layer(i, states);
	  }
	  // forward -- get activated states
	  int initial = this.getInitialState();
	  Set<Integer> initials = this.moveTo.get(initial);
	  for (int i : initials) {
		  net[0].activated.put(i, true);
	  }
	  for (int i = 0; i < len; i++) {
		  char curr = s.charAt(i);
		  Set<Integer> next = new HashSet<>();
		  for (Map.Entry<Integer, Boolean> entry : net[i].activated.entrySet()) {
			  if (entry.getValue()) {
				  Set<Integer> immediate = this.getNextState(entry.getKey(), curr);
				  Set<Integer> nextForOneState = new HashSet<>();
				  for (int j : immediate) {
					  nextForOneState.addAll(this.moveTo.get(j));
				  }
				  net[i].pathToNext.put(entry.getKey(), nextForOneState);
				  next.addAll(nextForOneState);
			  }
		  }
		  for (int j : next) {
			  net[i + 1].activated.put(j, true);
		  }
	  }
	  // backward -- get states that could reach Final
	  Set<Integer> finals = new HashSet<>(this.getFinalStates());
	  Set<Integer> reachFinal = new HashSet<>();
	  for (int i : this.coreStates) {
		  if (!Collections.disjoint(moveTo.get(i), finals)) {
			  reachFinal.add(i);
		  }
	  }
	  
	  net[len].reachFinal.addAll(reachFinal);
	  for (int i = len - 1; i >= 0; i--) {
		  for (int j : this.coreStates) {
			  if (!Collections.disjoint(net[i].pathToNext.get(j), net[i + 1].reachFinal)) {
				  net[i].reachFinal.add(j);
			  }
		  }
	  }
	  
	  return net;
  }
  
  public Set<EpsPath> getEpsPaths(Integer initState, Set<Integer> desiredStates){
	  Set<EpsPath> result = new HashSet<>();
	  EpsPath initPath = new EpsPath(initState);
	  Stack<EpsPath> pathStack = new Stack<EpsPath>();
	  pathStack.push(initPath);
	  while(!pathStack.isEmpty()) {
		  EpsPath currentPath = pathStack.pop();
		  Integer last = currentPath.getLast();
		  for (Move<CharPred, Character> move : getMovesFrom(last)) {
			  if(move.isEpsilonTransition()) {
				  EpsPath currentCopy = new EpsPath(currentPath);
				  int dest = move.to;
				  boolean dup = false;
				  for(Pair<Integer, Integer> p:currentCopy.getRep()) {
					  if((int)p.first==last && (int)p.second == dest) {
						  dup = true;
					  }
				  }
				  if(!dup) {
					  currentCopy.append(dest);
					  if(desiredStates.contains(dest)) {
						  result.add(currentCopy);
					  }
					  pathStack.push(currentCopy);
				  }
			  }
		  }
	  }
	  return result;
  }

  public SFA<CharPred, Character> getSFA() { 
    return this.sfa; 
  }
  
}
