import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestAll {

	public static final int timeOutSeconds = 10;

	public static void main(String[] args) throws IOException, InterruptedException {
		FileOutputStream fp = new FileOutputStream("testAllResult.log");
		PrintWriter pw = new PrintWriter(fp);
		System.out.println("----executing: mvn install----");
		Process proc = Runtime.getRuntime().exec("mvn -T 1C install");
		proc.waitFor();
		System.out.println("----getting test files----");
		List<String> testfiles = Files.walk(Paths.get("./tests/benchmark_explicit/"))
				.filter(p -> p.toString().endsWith(".txt")).map(p -> p.toString()).map(p -> p.substring(2, p.length()))
				.collect(Collectors.toList());
		System.out.println("----start testing----");
		for (String fname : testfiles) {
			System.out.println(">>>>current file: " + fname);
			System.out.println();
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
			}
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
		}
		pw.close();
		fp.close();
	}

}
