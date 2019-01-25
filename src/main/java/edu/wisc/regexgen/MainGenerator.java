package edu.wisc.regexgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.sat4j.specs.TimeoutException;

import edu.wisc.regfixer.enumerate.Benchmark;
import edu.wisc.regfixer.enumerate.Job;
import edu.wisc.regfixer.parser.RegexNode;

public class MainGenerator {

	private static boolean info = true;
	public static ReentrantLock flagLock = new ReentrantLock();

	public static void main(String[] args) throws TimeoutException {

		final long startTime = System.currentTimeMillis();
		BufferedReader reader = null;
		ArrayList<RegexEntry> regexSet = new ArrayList<>();
		ArrayList<String> positive = new ArrayList<>();
		ArrayList<String> negative = new ArrayList<>();

		try {
			File file = new File("../regex_small.re");
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				try {
					RegexNode root = edu.wisc.regfixer.parser.Main.parse(line);
					regexSet.add(new RegexEntry(line, root));
				} catch (Exception e) {
					System.err.println("Error: Could not parse regex. ");
				}
			}
		} catch (IOException e) {
			System.err.println("Error: Could not read dataset file. ");
		}

		final long endReadingData = System.currentTimeMillis();
		if (info) {
			System.out.println("========Finished reading dataset========");
			System.out.println("Time used: " + (endReadingData - startTime) + "ms");
		}

		String fileName = args[0];
		File fp = null;
		try {
			fp = new File("../tests/benchmark_explicit/" + fileName);
			Scanner scnr = new Scanner(fp);
			scnr.nextLine();
			scnr.nextLine();
			String example = scnr.nextLine();
			while (!example.equals("---")) {
				positive.add(example);
				example = scnr.nextLine();
			}
			while (scnr.hasNextLine()) {
				example = scnr.nextLine();
				negative.add(example);
			}
			scnr.close();
		} catch (FileNotFoundException e) {
			System.err.println("Error: Could not read user's examples. ");
		}
		final long endUE = System.currentTimeMillis();
		if (info) {
			System.out.println("========Finished reading user examples========");
			System.out.println("Time used: " + (endUE - endReadingData) + "ms");
			System.out.println("========Start collecting performance data========");
		}
		final long sdc = System.currentTimeMillis();
		DataCollector dc = new DataCollector(regexSet, positive, negative);
		dc.run();
		final long edc = System.currentTimeMillis();
		System.out.println("Time used: " + (edc - sdc) + "ms");

		// TODO sort
		Collections.sort(regexSet);
		final long es = System.currentTimeMillis();
		System.out.println("Time for sorting: " + (es - edc));
		for (int i = 0; i < 5; i++) {
			System.out.println(regexSet.get(i));
		}

		// reassemble format
		String part2 = "+++\n";
		for (String s : positive) {
			part2 += s + "\n";
		}
		part2 += "---\n";
		for (int i = 0; i < negative.size(); i++) {
			part2 += negative.get(i);
			if (i != negative.size() - 1)
				part2 += "\n";
		}

		System.out.println("========Start fixing========");

		ExecutorService pool = Executors.newFixedThreadPool(5);
		Set<Future<String>> tSet = new HashSet<Future<String>>();
		Set<String> results = new HashSet<String>();
		for (int i = 0; i < 5; i++) {
			Job tempJ = Benchmark.readFromStr(part2, regexSet.get(i).getTree());
			Callable<String> callable = new FixerThread(tempJ, "thread" + i);
			Future<String> future = pool.submit(callable);
			tSet.add(future);
		}

		for (Future<String> future : tSet) {
			try {
				String res = future.get(30, TimeUnit.SECONDS);
				if (res.length() > 0) {
					results.add(res);
				}
			} catch (InterruptedException | ExecutionException | java.util.concurrent.TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
