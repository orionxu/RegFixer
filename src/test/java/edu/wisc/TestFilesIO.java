package edu.wisc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.enumerate.Range;

public class TestFilesIO {

	public static Job constructJobFromFile(String filename) throws FileNotFoundException{
		File fp = new File("src/test/resources/"+filename);
		Scanner scnr = new Scanner(fp);
		String regExp = scnr.nextLine();
		scnr.nextLine();
		String example = scnr.nextLine();
		String corpus = "";
		Set<Range> pRange = new HashSet<>();
		Set<Range> nRange = new HashSet<>();
		while (!example.equals("---")) {
			pRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
			example = scnr.nextLine();
		}
		while (scnr.hasNextLine()) {
			example = scnr.nextLine();
			nRange.add(new Range(corpus.length(), corpus.length() + example.length()));
			corpus = corpus.concat(example);
		}
		scnr.close();
		return new Job(filename, regExp, corpus, pRange, nRange);
	}

}
