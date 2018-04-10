package edu.wisc;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;
import edu.wisc.regfixer.enumerate.*;

public class CorpusTest {
	
	@Test
	public void simpleTest() throws BadRangeException {
		String tempCorpus = "(123) 456-7890\n" + 
				"(987) 653-2109\n" + 
				"1-800-678-0693\n" + 
				"468-013-5790\n" + 
				"258-925";
		Set<Range> tempPosSet = new HashSet<>();
		tempPosSet.add(new Range("(30:44)"));
		tempPosSet.add(new Range("(45:57)"));
		Set<Range> tempNegSet = new HashSet<>();
		tempNegSet.add(new Range("(58:65)"));
		tempNegSet.add(new Range("(0:14)"));
		tempNegSet.add(new Range("(15:29)"));
		Corpus c = new Corpus(tempCorpus, tempPosSet, tempNegSet);
		assertEquals(c.getTotalCharsInPositiveExamples(),26);
		Pattern p = Pattern.compile("[0-9\\-]+");
		Set<Range> tempExp = new HashSet<>();
		tempExp.add(new Range("(30:44)"));
		tempExp.add(new Range("(45:57)"));
		Set<Range> result = Corpus.inferNegativeRanges(p, tempCorpus, tempExp);
		assertEquals(result.toString(), "[(58:65)]");
	}
}
