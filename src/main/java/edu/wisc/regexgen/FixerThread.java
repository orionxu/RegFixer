package edu.wisc.regexgen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import edu.wisc.regfixer.RegFixer;
import edu.wisc.regfixer.diagnostic.Diagnostic;
import edu.wisc.regfixer.diagnostic.ReportStream;
import edu.wisc.regfixer.enumerate.Job;

public class FixerThread implements Runnable {
	private Job tJob;
	private String tName;

	public FixerThread(Job j, String threadName) {
		this.tJob = j;
		this.tName = threadName;
	}

	@Override
	public void run() {
		FileOutputStream fs = null;
		try {
			fs = new FileOutputStream(tName + ".log");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}
		Diagnostic diag = new Diagnostic(new ReportStream(fs));
		int cut = 4000;
		try {
			RegFixer.fix(this.tJob, cut, diag);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}