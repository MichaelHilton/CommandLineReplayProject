package testRunner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.oregonstate.cope.eclipse.ASTInference.tests.ASTDeserializationTest;
import edu.oregonstate.edu.Main;

public class TestASTInference {
	
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
	public void testASTInference() throws Exception {
		String eventSourceFile = testData.resolve("add-change-delete").toAbsolutePath().toString();
		String replayDir = output.toAbsolutePath().toString();
		
		Main.main(new String[]{eventSourceFile, "NONE", replayDir});
		
		File[] eventFiles = output.resolve("eventFiles").toFile().listFiles();
		assertEquals(1, eventFiles.length);
		
		String fileContents = new String(Files.readAllBytes(eventFiles[0].toPath()));
		List<UserOperation> userOperations = new OperationDeserializer(null).getUserOperations(fileContents);
		
		new ASTDeserializationTest().assertASTDeserializationForBasicFile(userOperations);
	}
}
