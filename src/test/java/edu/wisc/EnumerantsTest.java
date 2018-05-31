package edu.wisc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.enumerate.*;

public class EnumerantsTest {

	private static Job testCase = null;
	private static ArrayList<Enumerant> enuList = null;
	private static Enumerant enuCase1 = null;
	private static Enumerant enuCase2 = null;
	private static Enumerant enuCase3 = null;

	/*@BeforeClass
	public static void preparation() throws FileNotFoundException {
		testCase = TestFilesIO.constructJobFromFile("test_date_true.txt");
		enuList = new ArrayList<>();
	}

	@Test
	public void testPollAndExpand() throws TimeoutException, IOException {
		// \d{1,2}\/\d{1,2}\/\d{4} (\d)
		Enumerants enumerants = new Enumerants(testCase.getTree(), testCase.getCorpus(), new Diagnostic());
		Enumerant enu = null;
		int cutoff = 0;
		while ((enu = enumerants.poll()) != null) {
			if (cutoff++ > 100) {
				break;
			}
			if (enu.toString().equals("(\\d){1,2}■(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 1);

			} else if (enu.toString().equals("(■■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 2);

			} else if (enu.toString().equals("(\\d){1,2}■(\\d){4}")) {
				assertEquals(enu.getCost(), 4);
			} else if (enu.toString().equals("((((■■)▓)(▓){■})▓)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 6);

			} else if (enu.toString().equals("(\\d){1,2}\\/(\\d){1,2}■\\/(\\d){4}")) {
				assertEquals(enu.getCost(), 1);
			} else if (enu.toString().equals("■(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}")) {
				enuCase1 = enu;
			}
			enuList.add(enu);
		}
		List<Enumerant> expands1 = enuCase1.expand();
		List<String> expandsDemo1 = expands1.stream().map(Enumerant::toString).collect(Collectors.toList());
		assertTrue(expandsDemo1.contains("(■|■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
		assertTrue(expandsDemo1.contains("(■■)(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
		assertTrue(expandsDemo1.contains("(■){■}(\\d){1,2}\\/(\\d){1,2}\\/(\\d){4}"));
	}*/
}
