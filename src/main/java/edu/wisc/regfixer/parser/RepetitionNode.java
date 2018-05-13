package edu.wisc.regfixer.parser;
import edu.wisc.regfixer.enumerate.UnknownBounds;

public class RepetitionNode implements RegexNode {
  private RegexNode child;
  private Bounds bounds;

  public RepetitionNode (RegexNode child, int min) {
    this.child = child;
    this.bounds = Bounds.atLeast(min);
  }

  public RepetitionNode (RegexNode child, int min, int max) {
    this.child = child;
    this.bounds = Bounds.between(min, max);
  }

  public RepetitionNode (RegexNode child, Bounds bounds) {
    this.child = child;
    this.bounds = bounds;
  }

  public RegexNode getChild () {
    return this.child;
  }

  public Bounds getBounds () {
    return this.bounds;
  }

  public int descendants () {
    return 1 + this.child.descendants();
  }

  public String toString () {
	// handle emptySet test
	if (UnknownBounds.emptyTest) {
		return String.format("(%s%s)%s", this.child, (char) 0x2202, 1);
	}
    return String.format("(%s)%s", this.child, this.bounds);
  }
}
