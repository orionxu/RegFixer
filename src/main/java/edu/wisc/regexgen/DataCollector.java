package edu.wisc.regexgen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.sat4j.specs.TimeoutException;

public class DataCollector {

	private ArrayList<RegexEntry> regexSet;
	private ArrayList<String> positive;
	private ArrayList<String> negative;

	public DataCollector(ArrayList<RegexEntry> regex, ArrayList<String> positive,
			ArrayList<String> negative) {
		this.regexSet = regex;
		this.positive = positive;
		this.negative = negative;
	}

	public void run() throws TimeoutException {


		int posNum = positive.size();
		int negNum = negative.size();
		for (int i = 0; i < regexSet.size(); i++) {
			RegexEntry e = regexSet.get(i);
			for (String p : positive) {
				if (!Pattern.matches(e.getExpression(), p)) {
					e.failPos(p);
				}
			}

			for (String n : negative) {
				if (Pattern.matches(e.getExpression(), n)) {
					e.failNeg(n);
				}
			}
			
			e.calculatePerformance(posNum, negNum);
			e.calculateEditDistance();
			e.cost();
		}

	}

}
