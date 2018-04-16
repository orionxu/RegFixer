package edu.wisc;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.wisc.regfixer.enumerate.*;
import edu.wisc.regfixer.parser.*;

public class SlicerTest {

	@Test
	public void testDateExample() throws FileNotFoundException {
		//   \d{1,2}\/\d{1,2}\/\d{4}
		Job testJob = TestFilesIO.constructJobFromFile("test_date_true.txt");
		RegexNode n = testJob.getTree();
		List<Enumerant> enumerantsResult = Slicer.slice(n);
		int concatSize = ((ConcatNode) n).getChildren().size();
		int count_between = 0;
		for (Enumerant e : enumerantsResult) {
			RegexNode etree = e.getTree();
			if (etree instanceof ConcatNode && ((ConcatNode)etree).getChildren().size() == concatSize + 1)
			if (((ConcatNode) e.getTree()).getChildren().size() == concatSize + 1) {
				count_between++;
			}
		}
		
		assertEquals(count_between, concatSize + 1);
		assertEquals(enumerantsResult.size(), 27);
	}
	
	@Test
	public void testCommaFormat() throws FileNotFoundException {
		//   (\d|,)*\d+
		Job testJob = TestFilesIO.constructJobFromFile("test_commaformat_TO.txt");
		RegexNode n = testJob.getTree();
		List<Enumerant> sliceResult = Slicer.slice(n);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertTrue(h.contains("((■\\d|,)){■}(\\d)+"));
		assertTrue(h.contains("((\\d|,)){■}(\\d)+"));
		assertTrue(h.contains("((\\d|,))*■"));
		assertTrue(h.contains("((\\d|,))*(■){■}"));
		assertTrue(h.contains("((\\d|,))*■(\\d)+"));
		assertTrue(h.contains("■"));
	}

	@Test
	public void testConcat() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		CharLiteralNode n2 = new CharLiteralNode('b');
		CharLiteralNode n3 = new CharLiteralNode('c');
		CharLiteralNode n4 = new CharLiteralNode('d');
		List<RegexNode> l = new LinkedList<>();
		l.add(n1);
		l.add(n2);
		l.add(n3);
		l.add(n4);
		ConcatNode n5 = new ConcatNode(l);
		List<Enumerant> sliceResult = Slicer.slice(n5);
		assertEquals(sliceResult.size(), 15);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertTrue(h.contains("■d"));//■
		assertTrue(h.contains("ab■cd"));
		assertTrue(h.contains("ab■d"));
		assertTrue(h.contains("abc■"));
		assertTrue(h.contains("a■"));
		assertTrue(h.contains("■cd"));
		assertTrue(h.contains("■"));
		assertTrue(h.contains("ab■"));
		assertTrue(h.contains("abcd■"));
	}

	@Test
	public void testUnion() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		CharLiteralNode n2 = new CharLiteralNode('b');
		UnionNode n3 = new UnionNode(n1, n2);
		List<Enumerant> sliceResult = Slicer.slice(n3);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(■|b)"));
		assertTrue(h.contains("(a|■)"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testRepetition() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		RepetitionNode n2 = new RepetitionNode(n1, 3, 20);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testOptional() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		OptionalNode n2 = new OptionalNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testStar() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		StarNode n2 = new StarNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testPlus() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		PlusNode n2 = new PlusNode(n1);
		List<Enumerant> sliceResult = Slicer.slice(n2);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 3);
		assertTrue(h.contains("(a){■}"));
		assertTrue(h.contains("(■){■}"));
		assertTrue(h.contains("■"));
	}
	
	@Test
	public void testChar() {
		CharLiteralNode n1 = new CharLiteralNode('a');
		List<Enumerant> sliceResult = Slicer.slice(n1);
		List<String> h = sliceResult.stream().map(Enumerant::toString)
				.collect(Collectors.toList());
		assertEquals(h.size(), 1);
		assertTrue(h.contains("■"));
	}
}
