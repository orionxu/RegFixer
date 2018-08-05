package edu.wisc.regfixer.enumerate;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.parser.RegexNode;

public class Enumerants {
  private final RegexNode original;
  private final Corpus corpus;
  private final Diagnostic diag;
  private Set<String> history;
  private Queue<Enumerant> queue;

  public Enumerants (RegexNode original, Corpus corpus, Diagnostic diag) {
    this.original = original;
    this.corpus = corpus;
    this.diag = diag;
    //this.init();
    this.first();
  }

  public Enumerant poll () {
    if (this.queue.isEmpty()) {
      return null;
    }

    Enumerant enumerant = this.queue.remove();
    
    // we need to check for the initial set of templates, otherwise only the expanded ones are checked
    // for expanded templates, they are checked twice for EmptySetTest
    // although there are some redundancy, let's implement this way for now
    boolean pass = corpus.passesEmptySetTest(enumerant);
    if (!pass)
    	return this.poll();
    
    for (Enumerant expansion : enumerant.expand()) {
      if (false == this.history.contains(expansion.toString())) {
        diag.timing().startTiming("timeEmptySetTest");
        boolean passesTests = corpus.passesEmptySetTest(expansion);
        diag.timing().stopTimingAndAdd("timeEmptySetTest");
        if(passesTests) {
          this.history.add(expansion.toString());
          this.queue.add(expansion);
          expansion.setParent(enumerant);
        }
      }
    }
    return enumerant;
    /*switch (enumerant.getLatestExpansion()) {
      case SyntheticUnion:
      case Freeze:
        // In these expansion cases, the template is garunteed to not produce a
        // better solution than its parent (but is kept in the queue for
        // search completeness reasons) so return the next possible template.
        return this.poll();
      default:
        return enumerant;
    }*/
  }

  /*private void init () {
    this.history = new HashSet<>();
    this.queue = new PriorityQueue<>();

    for (Enumerant expansion : Slicer.slice(this.original)) {
      this.diag.registry().bumpInt("totalDotStarTests");
      
      this.diag.timing().startTiming("timeDotStarTest");
      boolean passesDotStarTest = this.corpus.passesDotStarTest(expansion);
      this.diag.timing().stopTimingAndAdd("timeDotStarTest");

      if (passesDotStarTest) {
        this.history.add(expansion.toString());
        this.queue.add(expansion);
      } else {
        this.diag.registry().bumpInt("totalDotStarTestsRejects");
      }
    }
  }*/
  
  private void first () {
    this.history = new HashSet<>();
    this.queue = new PriorityQueue<>();

    for (Enumerant expansion : Expander.add(this.original)) {

      this.history.add(expansion.toString());
      this.queue.add(expansion);
    }
  }
  
  public Enumerant next () {
	  if (this.queue.isEmpty()) {
	      return null;
	    }
	
	    Enumerant enumerant = this.queue.remove();
	    // we need to check for the initial set of templates, otherwise only the expanded ones are checked
	    // for expanded templates, they are checked twice for EmptySetTest
	    // although there are some redundancy, let's implement this way for now
	    boolean pass = corpus.passesEmptySetTest(enumerant);
	   
	    // adding original holes
	    for (Enumerant addition : Expander.addOriginal(enumerant)) {
	      if (false == this.history.contains(addition.toString())) {
	          this.history.add(addition.toString());
	          this.queue.add(addition);
	          addition.setParent(enumerant);
	      }
	    }
	    
	    // reduce
	    for (Enumerant reduction : Expander.reduce(enumerant)) {
	      if (false == this.history.contains(reduction.toString())) {
	          this.history.add(reduction.toString());
	          this.queue.add(reduction);
	          reduction.setParent(enumerant);
	      }
	    }
	    
	    // expand
	    for (Enumerant expansion : Expander.expand(enumerant)) {
	      if (false == this.history.contains(expansion.toString())) {
	          this.history.add(expansion.toString());
	          this.queue.add(expansion);
	          expansion.setParent(enumerant);
	      }
	    }
	    
	    if (!pass)
	    	return this.next();
	    return enumerant;
	    /*switch (enumerant.getLatestExpansion()) {
	      case SyntheticUnion:
	      case Freeze:
	        // In these expansion cases, the template is garunteed to not produce a
	        // better solution than its parent (but is kept in the queue for
	        // search completeness reasons) so return the next possible template.
	        return this.poll();
	      default:
	        return enumerant;
	    }*/
  }
}
