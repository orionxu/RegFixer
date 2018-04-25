import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestAll {
	private static class TestCase {
		Set<String> rejects;
		Set<String> accepts;
		public TestCase(String fileName) throws FileNotFoundException {
			this.rejects = new HashSet<>();
			this.accepts = new HashSet<>();
			File fp = new File(fileName);
			Scanner scnr = new Scanner(fp);
			scnr.nextLine();
			scnr.nextLine();
			String example = scnr.nextLine();
			while (!example.equals("---")) {
				accepts.add(example);
				example = scnr.nextLine();
			}
			while (scnr.hasNextLine()) {
				example = scnr.nextLine();
				rejects.add(example);
			}
			scnr.close();
		}
		
		public boolean resultValid(String regex) {
			regex = "^" + regex + "$";
			boolean valid = true;
			for (String s:accepts) {
				if (!Pattern.matches(regex, s)) {
					valid = false;
				}
			}
			for (String s:rejects) {
				if (Pattern.matches(regex, s)) {
					valid = false;
				}
			}
			return valid;
		}
	}

	public static final int timeOutSeconds = 10;

	public static void main(String[] args) throws IOException, InterruptedException {
		FileOutputStream fp = new FileOutputStream("testAllResult.log");
		PrintWriter pw = new PrintWriter(fp);
		System.out.println("----executing: mvn install----");
		Process proc = Runtime.getRuntime().exec("mvn -T 1C -offline install");
		proc.waitFor();
		System.out.println("----getting test files----");
		List<String> testfiles = Files.walk(Paths.get("./tests/benchmark_explicit/"))
				.filter(p -> p.toString().endsWith(".txt")).map(p -> p.toString()).map(p -> p.substring(2, p.length()))
				.collect(Collectors.toList());
		System.out.println("----start testing----");
		for (String fname : testfiles) {
			System.out.println(">>>>current file: " + fname);
			System.out.println();
			TestCase t = new TestCase("./" + fname);
			Process testproc = Runtime.getRuntime()
					.exec("java -jar target/regfixer.jar fix --limit 4000 --file " + fname);
			long now = System.currentTimeMillis();
			long timeoutInMillis = 1000L * timeOutSeconds;
			long finish = now + timeoutInMillis;
			while (testproc.isAlive() && (System.currentTimeMillis() < finish)) {
				Thread.sleep(50);
			}
			if (testproc.isAlive()) {
				System.err.println("Error: File " + fname + " timeout out (10s). \n");
				testproc.destroy();
				continue;
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(testproc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(testproc.getErrorStream()));
			String s = null;
			boolean extractRegex = false;
			while ((s = stdInput.readLine()) != null) {
				pw.println(s);
				if (!extractRegex && s.contains("Finds the following solutions")) {
					extractRegex = true;
				} else if (s.contains("Computed in")) {
					extractRegex = false;
				} else if (extractRegex && s.length() > 0) {
					Scanner lineScanner = new Scanner(s);
					int fit = lineScanner.nextInt();
					String regexFound = lineScanner.nextLine().trim();
					boolean regexValid = t.resultValid(regexFound);
					System.out.println("\tResult Regex:  " + regexFound);
					System.out.println("\tFitness: " + fit + "\t" + "Valid? " + regexValid);
					System.out.println();
				} 
			}
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
		}
		pw.close();
		fp.close();
	}

}
