package edu.wisc.regfixer.synthesize;

import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;

import edu.wisc.regfixer.automata.Route;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.enumerate.Enumerant;
import edu.wisc.regfixer.enumerate.Grafter;
import edu.wisc.regfixer.enumerate.Unknown;
import edu.wisc.regfixer.enumerate.UnknownChar;
import edu.wisc.regfixer.enumerate.UnknownId;
import edu.wisc.regfixer.parser.Bounds;
import edu.wisc.regfixer.parser.CharClass;
import edu.wisc.regfixer.parser.CharClassSetNode;
import edu.wisc.regfixer.parser.CharLiteralNode;
import edu.wisc.regfixer.parser.CharRangeNode;
import edu.wisc.regfixer.parser.RegexNode;

public class Synthesis {
  private RegexNode tree;
  private int totalCharLiterals;

  public Synthesis (Enumerant enumerant, List<Set<Route>> positives, List<Set<Route>> negatives) throws SynthesisFailure {
    this(enumerant, positives, negatives, new Diagnostic());
  }

  public Synthesis (Enumerant enumerant, List<Set<Route>> positives, List<Set<Route>> negatives, Diagnostic diag) throws SynthesisFailure {
    Formula formula = new Formula(positives, negatives, diag);
    formula.solve();

    Map<UnknownId, CharClass> charSolutions = formula.getCharSolutions();
    Map<UnknownId, Bounds> boundsSolutions = formula.getBoundsSolutions();

    if ((charSolutions.size() + boundsSolutions.size()) < enumerant.getIds().size()) {
      throw new SynthesisFailure("no solution for some unknowns");
    }

    RegexNode whole = enumerant.getTree();

    int totalCharLiterals = 0;
    for (Entry<UnknownId, CharClass> solution : charSolutions.entrySet()) {
      CharClass cc = solution.getValue();
      whole = Grafter.graft(whole, solution.getKey(), cc);

      // Count single character literal classes included in the solution.
      if (cc instanceof CharLiteralNode) {
        totalCharLiterals++;
      } else if (cc instanceof CharClassSetNode) {
        for (CharRangeNode cr : ((CharClassSetNode)cc).getSubClasses()) {
          if (cr.isSingle() && cr.getLeftChild() instanceof CharLiteralNode) {
            totalCharLiterals++;
          }
        }
      }
    }

    for (Entry<UnknownId, Bounds> solution : boundsSolutions.entrySet()) {
      whole = Grafter.graft(whole, solution.getKey(), solution.getValue());
    }

    this.tree = whole;
    this.totalCharLiterals = totalCharLiterals;
  }

  public RegexNode getTree () {
    return this.tree;
  }

  public int getFitness () {
    return this.totalCharLiterals;
  }

  public Pattern toPattern (boolean withAnchors) {
    if (withAnchors) {
      return Pattern.compile(String.format("^%s$", this.tree));
    }

    return this.toPattern();
  }

  public Pattern toPattern () {
    return Pattern.compile(this.tree.toString());
  }

  @Override
  public String toString () {
    return this.tree.toString();
  }
}
