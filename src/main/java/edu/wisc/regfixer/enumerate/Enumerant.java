package edu.wisc.regfixer.enumerate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeSet;

import edu.wisc.regexgen.MainGenerator;
import edu.wisc.regfixer.automata.Automaton;
import edu.wisc.regfixer.automata.Route;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.global.Global;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.ConcatNode;
import edu.wisc.regfixer.parser.OptionalNode;
import edu.wisc.regfixer.parser.PlusNode;
import edu.wisc.regfixer.parser.RegexNode;
import edu.wisc.regfixer.parser.RepetitionNode;
import edu.wisc.regfixer.parser.StarNode;
import edu.wisc.regfixer.parser.UnionNode;
import edu.wisc.regfixer.parser.Storage;
import edu.wisc.regfixer.synthesize.Synthesis;
import edu.wisc.regfixer.synthesize.SynthesisFailure;
import org.sat4j.specs.TimeoutException;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class Enumerant implements Comparable<Enumerant> {
  public final static int UNION_COST    = 1;
  public final static int OPTIONAL_COST = 1;
  public final static int STAR_COST     = 3;
  public final static int PLUS_COST     = 2;
  public final static int CONCAT_COST   = 1;
  public final static int REPEAT_COST   = 1;
  public final static int FREEZE_COST   = 1;
  
  public static boolean emptyTest;
  public boolean passDot;
  public boolean passEmpty;
  public long order;

  private final RegexNode tree;
  private final Set<UnknownId> ids;
  private final int cost;
  private final Expansion latest;
  private Enumerant parent;

  @FunctionalInterface
  public static interface ExpansionFunction {
    Enumerant apply(UnknownChar unknown) throws ForbiddenExpansionException;
  }

  @FunctionalInterface
  public static interface MultExpansionFunction {
    Enumerant apply(Collection<UnknownChar> unknowns) throws ForbiddenExpansionException;
  }

  public Enumerant (RegexNode tree, UnknownId id, int cost, Expansion latest) {
    this(tree, Arrays.asList(id), cost, latest);
  }

  public Enumerant (RegexNode tree, Collection<UnknownId> ids, int cost, Expansion latest) {
    this.tree = tree;
    this.ids = new HashSet<>(ids);
    this.cost = cost;
    this.latest = latest;
    this.parent = null;
    this.passDot = false;
  }

  public RegexNode getTree () {
    return this.tree;
  }
  
  public Enumerant getParent() {
	  return this.parent;
  }
  
  public void setParent(Enumerant p) {
	  this.parent = p;
  }

  public Set<UnknownId> getIds () {
    return this.ids;
  }

  public boolean hasUnknownId (UnknownId id) {
    return this.ids.contains(id);
  }

  public int getCost () {
    return this.cost;
  }

  public Expansion getLatestExpansion () {
    return this.latest;
  }

  	public Pattern toPattern(UnknownChar.FillType type) {
		// Set temporary values for unknowns.
		MainGenerator.flagLock.lock();
		UnknownChar.setFill(type);

		if (type == UnknownChar.FillType.EmptySet) {
			UnknownBounds.setFill(Bounds.exactly(1));
			Enumerant.emptyTest = true;
		} else {
			UnknownBounds.setFill();
		}
		String regex = String.format("^%s$", this.tree);

		// Clear the temporary values.
		UnknownChar.clearFill();
		UnknownBounds.clearFill();
		Enumerant.emptyTest = false;
		MainGenerator.flagLock.unlock();


		return Pattern.compile(regex);
	}

  public List<Enumerant> expand () {
    List<Enumerant> expansions = new LinkedList<>();

    // Create a sorted list of UnknownChar's from youngest -> oldest.
    TreeSet<UnknownChar> unknowns = new TreeSet<UnknownChar>(this.ids
      .stream()
      .filter(id -> id.getUnknown() instanceof UnknownChar)
      .map(id -> (UnknownChar)id.getUnknown())
      .collect(Collectors.toSet()));

    // 1. Identify oldest unfrozen unknown
    // 2. Apply all valid expansions to that unknown and push those templates to the stack
    // 3. Freeze that unknown and push that template to the stack
    UnknownChar oldest = null;
    for (UnknownChar unknown : unknowns) {
      if (oldest == null) {
        oldest = unknown;
      } else if (unknown.isFrozen() == false && unknown.getAge() < oldest.getAge()) {
        oldest = unknown;
      }
    }

    if (oldest != null) {
      // Perform expansion converting unknown char -> union, quantifier, and concat.
      this.addExpansion(expansions, oldest, this::expandWithUnion);
      if (oldest.canInsertQuantifierNodes()) {
        this.addExpansion(expansions, oldest, this::expandWithUnknownQuantifier);
      }
      this.addExpansion(expansions, oldest, this::expandWithConcat);
      //this.addExpansion(expansions, oldest, this::expandWithFrozen);
    }

    return expansions;
  }

  private void addExpansion (List<Enumerant> expansions, UnknownChar unknown, ExpansionFunction expander) {
    Enumerant expansion = null;
    try {
      expansion = expander.apply(unknown);
    } catch (ForbiddenExpansionException ex) {
      return;
    }

    if (expansion != null) {
      expansions.add(expansion);
    }
  }

  private void addExpansion (List<Enumerant> expansions, Collection<UnknownChar> unknowns, MultExpansionFunction expander) {
    Enumerant expansion = null;
    try {
      expansion = expander.apply(unknowns);
    } catch (ForbiddenExpansionException ex) {
      return;
    }

    if (expansion != null) {
      expansions.add(expansion);
    }
  }

  private Enumerant expandWithUnion (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create both unknown chars to be added to the regex tree.
    UnknownChar un1 = new UnknownChar(unknown.getHistory(), Expansion.SyntheticUnion);
    UnknownChar un2 = new UnknownChar(unknown.getHistory(), Expansion.SyntheticUnion);

    // Create union node to added in place of the given 'unknown'.
    RegexNode scion = new UnionNode(un1, un2, true);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(un1.getId());
    ids.add(un2.getId());

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.UNION_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.SyntheticUnion);
  }

  private Enumerant expandWithUnknownQuantifier (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create an unknown char to be added to the regex tree.
    UnknownChar child = new UnknownChar(unknown.getHistory(), Expansion.Repeat);
    UnknownBounds bounds = new UnknownBounds();

    // Create unknown node to add in place of the given 'unknown'.
    RegexNode scion = new RepetitionNode(child, bounds);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graftWithUnknownAncestors(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(child.getId());
    ids.add(bounds.getId());
    ids.addAll(Grafter.addedBounds);

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.REPEAT_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Repeat);
  }

  private Enumerant expandWithConcat (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create both unknown chars to be added to the regex tree.
    UnknownChar un1 = new UnknownChar(unknown.getHistory(), Expansion.Concat);
    UnknownChar un2 = new UnknownChar(unknown.getHistory(), Expansion.Concat);

    // Create concatenation node to added in place of the given 'unknown'.
    RegexNode scion = new ConcatNode(un1, un2);

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), scion);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(un1.getId());
    ids.add(un2.getId());

    // Add cost of the expansion.
    int cost = this.getCost() + Enumerant.CONCAT_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Concat);
  }

  private Enumerant expandWithFrozen (UnknownChar unknown) throws ForbiddenExpansionException {
    // Create frozen unknown to added in place of the given 'unknown'.
    UnknownChar frozen = new UnknownChar(unknown.getHistory(), Expansion.Freeze);
    frozen.freeze();

    // Graft scion onto the root regex tree.
    RegexNode root = Grafter.graft(this.tree, unknown.getId(), frozen);

    // Build set of IDs custom to the new enumerant.
    Set<UnknownId> ids = new HashSet<>();
    ids.addAll(this.getIds());
    ids.remove(unknown.getId());
    ids.add(frozen.getId());

    // Add cost of expansion.
    int cost = this.getCost() + Enumerant.FREEZE_COST;

    // Build components into new enumerant.
    return new Enumerant(root, ids, cost, Expansion.Freeze);
  }

  public Synthesis synthesize (Set<String> p, Set<String> n) throws SynthesisFailure {
    return this.synthesize(p, n, new Diagnostic());
  }

  public Synthesis synthesize (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
    Automaton automaton = null;

    try {
      automaton = new Automaton(this.tree);
      automaton.IniMoveTo();
    } catch (TimeoutException ex) {
      String fmt = "timed-out building automaton for `%s`";
      throw new SynthesisFailure(String.format(fmt, this.tree));
    }

    List<Set<Route>> positiveRuns = new LinkedList<>();
    List<Set<Route>> negativeRuns = new LinkedList<>();

    //System.out.println("automaton: " + automaton);
    //System.out.println("unknownToEntryState " + automaton.unknownToEntryState);
    //System.out.println("unknownToExitStates " + automaton.unknownToExitStates);

    try {
      for (String source : p) {
    	  //System.out.println("positive example: " + source);
    	//if(emptySetMatching(source))
    		//continue;
    	Set<Route> positiveRun = automaton.trace(source);
    	if (positiveRun.size() == 0) {
    		//System.out.println("get p size() 0 !!!!!!!!!!!!!!!!!!!!!");
    		return null;
    	}
    	boolean ignore = false;
    	for (Route route : positiveRun) {
    		if (route.hasNoRealExits() && route.hasNoSpans())
    			ignore = true;
    	}
    	if (ignore)
    		continue;
        positiveRuns.add(positiveRun);
      }

      for (String source : n) {
    	  //System.out.println("negative example: " + source);
    	  Set<Route> negativeRun = automaton.trace(source);
    	  //System.out.println("negativeRun:\n" + negativeRun);
    	  if (negativeRun.size() == 0) {
    		  continue;
    	  }
    	  //System.err.println("source is " + source);
    	  for (Route route : negativeRun) {
    		  //System.err.println("negative route: " + route);
      		if (route.hasNoRealExits() && route.hasNoSpans())
      			return null;
      	}
        negativeRuns.add(negativeRun);
      }
    } catch (TimeoutException ex) {
      String fmt = "timed-out computing runs for `%s`";
      throw new SynthesisFailure(String.format(fmt, this.tree));
    }

    /*for (int i=0; i < positiveRuns.size(); i++) {
    	System.out.println("positive run" + Integer.toString(i) + positiveRuns.get(i));
    }
    
    for (int i=0; i < negativeRuns.size(); i++) {
    	System.out.println("negative run" + Integer.toString(i) + negativeRuns.get(i));
    }*/
    
    int totalRuns = positiveRuns.size() + negativeRuns.size();
    if (diag.getInt("maximumRoutes") < totalRuns) {
      diag.registry().setInt("maximumRoutes", totalRuns);
    }

    Global.root = this.tree;
    return new Synthesis(this, positiveRuns, negativeRuns, diag);
  }

  public Synthesis synthesizePair (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
	  /*if (Global.findMaxSat) {
		  return synthesizePairMax(p, n, diag);
	  }*/
	  Storage.reset();
	  this.tree.toBinary();
	  int holes = this.tree.collectUnknown();
	  
	  Storage.ctx = new Context();
	  Set<Character> chars = new HashSet<>();
	  for (String s : p) {
		  chars.addAll(this.CollectChars(s));
	  }
	  Storage.allChars = new char[chars.size()];
	  int ctr = 0;
	  for (char c : chars) {
		  Storage.allChars[ctr++] = c;
	  }
	  
	  int charNum = chars.size();
	  if (Storage.unknownCharCounter > -1) {
	      Storage.charPreds = new BoolExpr[Storage.unknownCharCounter + 1][charNum];
	  }
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  for (int j = 0; j < Storage.allChars.length; j++) {
			  Storage.charPreds[i][j] = Storage.ctx.mkBoolConst("char" + Integer.toString(i) + "_" + Character.toString(
					  Storage.allChars[j]));
		  }
	  }
	  
	  if (Storage.unknownBoundCounter > -1)
	      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  Storage.boundPreds[i] = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
	  }
	  
	  this.tree.setNullable();
	  this.tree.setLen();
	  
	  BoolExpr expr = Storage.ctx.mkBoolConst("final");
	  int ini = 0;
	  for (String s : p) {
		  if (s.length() < 1)
			  continue;
		  if (ini == 0) {
		      expr = Storage.ctx.mkAnd(this.getConstraintForOne(s, tree));
		      ini++;
		  } else {
		      expr = Storage.ctx.mkAnd(expr, this.getConstraintForOne(s, tree));
		  }
	  }
	  ini = 0;
	  for (String s : n) {
		  if (s.length() < 1)
			  continue;
		  expr = Storage.ctx.mkAnd(expr, Storage.ctx.mkNot(this.getConstraintForOne(s, tree)));
	  }
	  
	  //Optimize opt = Storage.ctx.mkOptimize();
	  Model model;
	  
	  Optimize opt = Storage.ctx.mkOptimize();
	  if (Global.findMaxSat) {
		  Storage.costArray = new ArrayList<>();
		  if (Storage.unknownBoundCounter > -1)
		      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
		  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
			  Storage.boundPreds[i] = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
		  }
		  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
			  this.setOptBound(opt, i);
		  }
		  IntExpr[] costArray = Storage.costArray.toArray(new IntExpr[Storage.costArray.size()]);
		  opt.Assert(expr);
	      opt.MkMinimize(Storage.ctx.mkAdd(costArray));
	  } else {
		  opt.Assert(expr);
	  }
	  
	  diag.timing().startTiming("timeSATSolver");
	  Status status = opt.Check();
	  long duration = diag.timing().stopTimingAndAdd("timeSATSolver");
	  diag.stat.add(tree.toString(), tree.descendants(), holes, duration);
	  if (status != Status.SATISFIABLE) {
	      throw new SynthesisFailure("unsatisfiable SAT formula");
	  } else {
	      model = opt.getModel();
	      Storage.model = model;
	      //System.err.println("model is " + model.toString());
	      System.out.println("tree is" + this.tree);
	      
	      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
	        	for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
	        	    System.out.println("char_" + Storage.allChars[cNum] + ": " + 
	        	    		model.evaluate(Storage.charPreds[count][cNum], false));
	        	}
	        }
	      
	      for (int count = 0; count < Storage.unknownBoundCounter + 1; count++) {
	            System.out.println(Storage.boundPreds[count]);
	            
	            IntExpr intExpr = Storage.boundPreds[count];
	            System.out.println("bound is " + intExpr + "_" + model.getConstInterp(intExpr));
	      }
	     
	      System.out.println("final tree is" + this.tree.finalString());
	      return new Synthesis();
	  }
	  
  }
  
  /*public Synthesis synthesizePairMax (Set<String> p, Set<String> n, Diagnostic diag) throws SynthesisFailure {
	  Storage.reset();
	  this.tree.toBinary();
	  int holes = this.tree.collectUnknown();
	  
	  Storage.ctx = new Context();
	  Set<Character> chars = new HashSet<>();
	  for (String s : p) {
		  chars.addAll(this.CollectChars(s));
	  }
	  Storage.allChars = new char[chars.size()];
	  int ctr = 0;
	  for (char c : chars) {
		  Storage.allChars[ctr++] = c;
	  }
	  
	  int charNum = chars.size();
	  Storage.costArray = new ArrayList<>();
	  if (Storage.unknownCharCounter > -1) {
	      Storage.charPreds = new BoolExpr[Storage.unknownCharCounter + 1][charNum];
	      Storage.maxCharPreds = new BoolExpr[Storage.unknownCharCounter + 1][4];
	  }
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  for (int j = 0; j < Storage.allChars.length; j++) {
			  Storage.charPreds[i][j] = Storage.ctx.mkBoolConst("char" + Integer.toString(i) + "_" + Character.toString(
					  Storage.allChars[j]));
		  }
		  Storage.maxCharPreds[i][0] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "d");
		  Storage.maxCharPreds[i][1] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "az");
		  Storage.maxCharPreds[i][2] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "AZ");
		  Storage.maxCharPreds[i][3] = Storage.ctx.mkBoolConst("max" + Integer.toString(i) + "w");
	  }
	  
	  if (Storage.unknownBoundCounter > -1)
	      Storage.boundPreds = new IntExpr[Storage.unknownBoundCounter + 1];
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  Storage.boundPreds[i] = Storage.ctx.mkIntConst("bound" + Integer.toString(i));
	  }
	  
	  this.tree.setNullable();
	  this.tree.setLen();
	  
	  BoolExpr expr = Storage.ctx.mkBoolConst("final");
	  int ini = 0;
	  for (String s : p) {
		  if (s.length() < 1)
			  continue;
		  if (ini == 0) {
		      expr = Storage.ctx.mkAnd(this.getConstraintForOne(s, tree));
		      ini++;
		  } else {
		      expr = Storage.ctx.mkAnd(expr, this.getConstraintForOne(s, tree));
		  }
	  }
	  ini = 0;
	  for (String s : n) {
		  if (s.length() < 1)
			  continue;
		  expr = Storage.ctx.mkAnd(expr, Storage.ctx.mkNot(this.getConstraintForOne(s, tree)));
	  }
	  
	  //Optimize opt = Storage.ctx.mkOptimize();
	  Model model;
	  
	  //Solver s = Storage.ctx.mkSolver();
	  //s.add(expr);
	  Optimize opt = Storage.ctx.mkOptimize();
	  //opt.Assert(expr);
	  for (int i = 0; i < Storage.unknownCharCounter + 1; i++) {
		  this.setOpt(opt, i);
	  }
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  this.setOptBound(opt, i);
	  }
	  IntExpr[] costArray = Storage.costArray.toArray(new IntExpr[Storage.costArray.size()]);
      opt.MkMinimize(Storage.ctx.mkAdd(costArray));
      opt.Assert(expr);
      
      System.err.println("check5");
	  diag.timing().startTiming("timeSATSolver");
	  Status status = opt.Check();
	  long duration = diag.timing().stopTimingAndAdd("timeSATSolver");
	  diag.stat.add(tree.toString(), tree.descendants(), holes, duration);
	  System.err.println("check6");
	  if (status != Status.SATISFIABLE) {
	      throw new SynthesisFailure("unsatisfiable SAT formula");
	  } else {
	      model = opt.getModel();
	      Storage.model = model;
	      //System.err.println("model is " + model.toString());
	      System.out.println("tree is" + this.tree);
	      
	      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
	        	for (int cNum = 0; cNum < Storage.allChars.length; cNum++) {
	        	    System.out.println("char_" + Storage.allChars[cNum] + ": " + 
	        	    		model.evaluate(Storage.charPreds[count][cNum], false));
	        	}
	        }
	      
	      for (int count = 0; count < Storage.unknownBoundCounter + 1; count++) {
	            System.out.println(Storage.boundPreds[count]);
	            
	            IntExpr intExpr = Storage.boundPreds[count];
	            System.out.println("bound is " + intExpr + "_" + model.getConstInterp(intExpr));
	      }
	      
	      for (int count = 0; count < Storage.unknownCharCounter + 1; count++) {
	    	  System.out.println("max_d" +
      	    		model.evaluate(Storage.maxCharPreds[count][0], false));
	    	  System.out.println("max_az" +
	      	    		model.evaluate(Storage.maxCharPreds[count][1], false));
	    	  System.out.println("max_AZ" +
	      	    		model.evaluate(Storage.maxCharPreds[count][2], false));
	    	  System.out.println("max_w" +
	      	    		model.evaluate(Storage.maxCharPreds[count][3], false));
	      }
	      
	      System.out.println("final tree is" + this.tree.finalString());
	      return new Synthesis();
	  }
	  
  }*/
  
  @Override
  public int compareTo (Enumerant other) {
    int weight = Integer.compare(this.getCost(), other.getCost());
    if (weight != 0)
    	return weight;
    return Long.compare(this.order, other.order);
  }

  @Override
  public String toString () {
    return this.tree.toString();
  }
  
  private boolean emptySetMatching(String s) {
	  Pattern p = toPattern(UnknownChar.FillType.EmptySet);
	  return p.matcher(s).matches();
  }
  
  private Set<Character> CollectChars(String s) {
	  Set<Character> res = new HashSet<>();
	  for (char c : s.toCharArray()) {
		  res.add(c);
	  }
	  return res;
  }
  
  private BoolExpr getConstraintForOne(String s, RegexNode tree) {
	  Storage.curExample = s.toCharArray();
	  int length = Storage.curExample.length;
	  this.tree.calUpto(length);
	  this.tree.setPairs();
	  return this.tree.getPairs()[0][length - 1];
  }
  
  /*private void setOpt(Optimize opt, int index) {
	  Context ctx = Storage.ctx;
	  int num_d = 0;
	  int num_az = 0;
	  int num_AZ = 0;
	  for (int i = 0; i < Storage.allChars.length; i++) {
		  char c = Storage.allChars[i];
		  if (c >= '0' && c <= '9') {
			  num_d++;
			  ctx.mkImplies(Storage.maxCharPreds[index][0], Storage.charPreds[index][i]);
		  } else if (c >= 'a' && c <= 'z') {
			  num_az++;
			  ctx.mkImplies(Storage.maxCharPreds[index][1], Storage.charPreds[index][i]);
		  } else if (c >= 'A' && c <= 'Z') {
			  num_AZ++;
			  ctx.mkImplies(Storage.maxCharPreds[index][2], Storage.charPreds[index][i]);
		  }
		  //Storage.ctx.mkImplies(Storage.charPreds[index][i], null);
		  
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + c);
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(2));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.charPreds[index][i], ifTrue, ifFalse));
		  Storage.costArray.add(weight);
	  }
	  
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][0]);
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][1]);
	  //ctx.mkImplies(Storage.maxCharPreds[index][3], Storage.maxCharPreds[index][2]);
	  
	  if (num_d > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_d");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_d*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][0], ifTrue, ifFalse));
		  Storage.costArray.add(weight);
	  }
	  
	  if (num_az > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_az");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_az*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][1], ifTrue, ifFalse));
		  Storage.costArray.add(weight);
	  }
	  
	  if (num_AZ > 0) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "num_AZ");
		  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(-(num_AZ*2)+3));
		  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
		  opt.Assert((BoolExpr)Storage.ctx.mkITE(Storage.maxCharPreds[index][2], ifTrue, ifFalse));
		  Storage.costArray.add(weight);
	  }
	  
	  BoolExpr d = Storage.maxCharPreds[index][0];
	  BoolExpr az = Storage.maxCharPreds[index][1];
	  BoolExpr AZ = Storage.maxCharPreds[index][2];
	  BoolExpr w = Storage.maxCharPreds[index][3];
	  
	  ctx.mkImplies(ctx.mkOr(ctx.mkAnd(d,az), ctx.mkAnd(d,AZ), ctx.mkAnd(az,AZ)), w);
  }*/
  
  private void setOptBound(Optimize opt, int index) {
	  Context ctx = Storage.ctx;
	  for (int i = 0; i < Storage.unknownBoundCounter + 1; i++) {
		  IntExpr weight = Storage.ctx.mkIntConst(Integer.toString(index) + "bound");
		  //if (i % 2 == 0) {
			  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
			  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(1));
			  opt.Assert((BoolExpr)Storage.ctx.mkITE(ctx.mkOr(ctx.mkEq(Storage.boundPreds[index],ctx.mkInt(0)),
					  ctx.mkEq(Storage.boundPreds[index],ctx.mkInt(1))),
					  ifTrue, ifFalse));
			  Storage.costArray.add(weight);
		  /*} else {
			  BoolExpr ifTrue = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(0));
			  BoolExpr ifFalse = Storage.ctx.mkEq(weight, Storage.ctx.mkInt(1));
			  opt.Assert((BoolExpr)Storage.ctx.mkITE(
					  ctx.mkEq(Storage.boundPreds[index],ctx.mkInt(1)),
					  ifTrue, ifFalse));
			  Storage.costArray.add(weight);
		  }*/
		  
	  }
  }
  
}
