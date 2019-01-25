package edu.wisc.regexgen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import edu.wisc.regfixer.RegFixer;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.diagnostic.ReportStream;
import edu.wisc.regfixer.enumerate.Job;

public class FixerThread implements Callable<String> {
	private Job tJob;
	private String tName;

	public FixerThread(Job j, String threadName) {
		this.tJob = j;
		this.tName = threadName;
	}

	@Override
	public String call() {
		boolean status = true;
		FileOutputStream fs = null;
		String sol = "";
		final long tStart = System.currentTimeMillis();
		try {
			fs = new FileOutputStream(tName + ".log");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return "";
		}
		Diagnostic diag = new Diagnostic(new ReportStream(fs));
		int cut = 4000;
		try {
			System.out.println("Start fixing....");
			sol = RegFixer.fix(this.tJob, cut, diag);
		} catch (Exception e) {
			e.printStackTrace();
			status = false;
		}
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
				
			}
		}
		final long duration = System.currentTimeMillis() - tStart;

		if (status)
			System.out.println(
					"===============\nThread " + tName + " done successfully. \nRunning time: " + duration + "\nSolution is: " + sol);
		else
			System.out.println("===============\nThread " + tName + " failed. ");
		return sol;
	}


}