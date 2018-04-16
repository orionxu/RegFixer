package edu.wisc;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import edu.wisc.regfixer.enumerate.*;
import edu.wisc.regfixer.parser.*;

public class GrafterTest {
	@Test
	public void allInOne() throws ForbiddenExpansionException {
		CharLiteralNode cln1 = new CharLiteralNode('a');
		CharLiteralNode cln2 = new CharLiteralNode('b');
		CharLiteralNode cln3 = new CharLiteralNode('c');
		CharLiteralNode cln4 = new CharLiteralNode('d');
		UnknownChar uc1 = new UnknownChar(Expansion.Concat);
		UnknownChar uc2 = new UnknownChar(Expansion.Union);
		UnknownChar uc3 = new UnknownChar(Expansion.Concat);
		UnknownChar uc4 = new UnknownChar(Expansion.Concat);
		UnknownChar uc5 = new UnknownChar(Expansion.Concat);
		List<RegexNode> l1 = new ArrayList<>();
		l1.add(uc1);
		l1.add(cln1);
		l1.add(cln2);
		l1.add(cln3);
		ConcatNode c1 = new ConcatNode(l1);
		UnionNode u1 = new UnionNode(uc2, cln4);
		UnknownChar child = new UnknownChar(Expansion.Concat);
		UnknownBounds bounds = new UnknownBounds();
		
		UnionNode r1 = new UnionNode(uc4, uc5);
		ConcatNode r2 = new ConcatNode(uc4, uc5);
		RepetitionNode r3 = new RepetitionNode(child, bounds);
		RegexNode e1 = Grafter.graft(c1, uc1.getId(), r1);
		RegexNode e2 = Grafter.graft(c1, uc1.getId(), r2);
		RegexNode e3 = Grafter.graft(c1, uc1.getId(), r3);
		assertEquals(e1.toString(), "(■|■)abc");
		assertEquals(e2.toString(), "(■■)abc");
		assertEquals(e3.toString(), "(■){■}abc");
		
		RegexNode e4 = Grafter.graft(u1, uc2.getId(), r1);
		RegexNode e5 = Grafter.graft(u1, uc2.getId(), r2);
		RegexNode e6 = Grafter.graft(u1, uc2.getId(), r3);
		
		assertEquals(e4.toString(), "((■|■)|d)");
		assertEquals(e5.toString(), "(■■|d)");
		assertEquals(e6.toString(), "((■){■}|d)");
		
		RegexNode e7 = Grafter.graft(uc1, uc1.getId(), r1);
		RegexNode e8 = Grafter.graft(uc1, uc1.getId(), r2);
		RegexNode e9 = Grafter.graft(uc1, uc1.getId(), r3);
		
		assertEquals(e7.toString(), "(■|■)");
		assertEquals(e8.toString(), "■■");
		assertEquals(e9.toString(), "(■){■}");
	}
}
