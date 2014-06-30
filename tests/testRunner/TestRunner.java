package testRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.oregonstate.edu.Main;

public class TestRunner {
	
	private static final Path testData = Paths.get("testData");
	private static final Path output = Paths.get("testData/output");
	
	@Before
	public void setUP() throws IOException {
		if (Files.exists(output)) {
			FileUtils.deleteQuietly(output.toFile());
		}
		
		Files.createDirectory(output);
	}
	
	@Test
	public void runWithArgs() throws Exception {
		Main.main(new String[]{testData.resolve("add-change-delete").toAbsolutePath().toString(), "NONE", output.toAbsolutePath().toString()});
	}
}
