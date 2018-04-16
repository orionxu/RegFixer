package edu.wisc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import edu.wisc.regfixer.enumerate.BadRangeException;
import edu.wisc.regfixer.enumerate.Range;

public class RangeTest {
	@Test
	public void testBasic() throws BadRangeException {
		Range A = new Range(4,6);
		Range B = new Range("(4:6)");
		Range C = new Range(7,20);
		Range D = new Range("(7:20)");
		assertTrue(A.equals(B));
		assertTrue(C.equals(D));
		assertTrue(A.endsBefore(D));
		assertFalse(A.endsAfter(C));
		assertFalse(A.equals(D));
		assertTrue(A.compareTo(C)<0);
	}
}
