package edu.oregonstate.edu;

import org.json.simple.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: michaelhilton
 * Date: 10/24/13
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReplayTest {

    Replay r;

    @Before
    public void setUp() throws Exception {
        r = new Replay();
        r.setDir("");
        if(!Files.exists(Paths.get("./testIO.txt"))){
            Charset charset = Charset.forName("US-ASCII");
            String s = "SampleFile";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./testIO.txt"), charset)) {
                writer.write(s, 0, s.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }

        if(!Files.exists(Paths.get("./same1.txt"))){
            Charset charset = Charset.forName("US-ASCII");
            String s = "This is a sample file used for comparison.";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./same1.txt"), charset)) {
                writer.write(s, 0, s.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }
        if(!Files.exists(Paths.get("./same2.txt"))){
            Charset charset = Charset.forName("US-ASCII");
            String s = "This is a sample file used for comparison.";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./same2.txt"), charset)) {
                writer.write(s, 0, s.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }
        if(!Files.exists(Paths.get("./different1.txt"))){
            Charset charset = Charset.forName("US-ASCII");
            String s = "This is a different sample file used for comparison, with some diffs.";
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./different1.txt"), charset)) {
                writer.write(s, 0, s.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }

    }

    @Test
    public void testAddChar() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "x", 0);
        assertEquals("xabcd",afterString);
    }

    @Test
    public void testAddCharEnd() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "x", 4);
        assertEquals("abcdx",afterString);
    }

    @Test
    public void testAddCharMiddle() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "x", 2);
        assertEquals("abxcd",afterString);
    }

    @Test
    public void testAddStringStart() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "xxx", 0);
        assertEquals("xxxabcd",afterString);
    }

    @Test
    public void testAddStringEnd() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "xxx", 2);
        assertEquals("abxxxcd",afterString);
    }

    @Test
    public void testAddStringMiddle() throws Exception {
        String initialString = "abcd";
        String afterString = r.insertString(initialString, "xxx", 4);
        assertEquals("abcdxxx",afterString);
    }

    @Test
    public void testRemoveStringStart() throws Exception {
        String initialString = "xxxabcd";
        String afterString = r.removeSubString(initialString, 0, 3);
        assertEquals("abcd",afterString);
    }
    @Test
    public void testRemoveStringMiddle() throws Exception {
        String initialString = "abxxcd";
        String afterString = r.removeSubString(initialString, 2, 2);
        assertEquals("abcd",afterString);
    }
    @Test
    public void testRemoveStringEnd() throws Exception {
        String initialString = "abcdxxx";
        String afterString = r.removeSubString(initialString, 4, 7);
        assertEquals("abcd",afterString);
    }

    @Test
    public void testRemoveCharStart() throws Exception {
        String initialString = "xabcd";
        String afterString = r.removeSubString(initialString, 0, 1);
        assertEquals("abcd",afterString);
    }
    @Test
    public void testRemoveCharMiddle() throws Exception {
        String initialString = "abxcd";
        String afterString = r.removeSubString(initialString, 2, 1);
        assertEquals("abcd",afterString);
    }
    @Test
    public void testRemoveCharEnd() throws Exception {
        String initialString = "abcdx";
        String afterString = r.removeSubString(initialString, 4, 1);
        assertEquals("abcd",afterString);
    }

    @Test
    public void testReplaceCharEnd() throws Exception {
        String initialString = "abcdx";
        String afterString = r.replaceSubString(initialString, "y", 4, 1);
        assertEquals("abcdy",afterString);
    }

    @Test
    public void testReplaceCharStart() throws Exception {
        String initialString = "xabcd";
        String afterString = r.replaceSubString(initialString, "y", 0, 1);
        assertEquals("yabcd",afterString);
    }

    @Test
    public void testReplaceCharMiddle() throws Exception {
        String initialString = "abxcd";
        String afterString = r.replaceSubString(initialString, "y", 2, 1);
        assertEquals("abycd",afterString);
    }

    @Test
    public void testReplaceStringEnd() throws Exception {
        String initialString = "abcdx";
        String afterString = r.replaceSubString(initialString, "yyyy", 4, 1);
        assertEquals("abcdyyyy",afterString);
    }

    @Test
    public void testReplaceStringStart() throws Exception {
        String initialString = "xxxabcd";
        String afterString = r.replaceSubString(initialString, "yyyy", 0, 3);
        assertEquals("yyyyabcd",afterString);
    }

    @Test
    public void testOpenFile() throws Exception {
        Path file = Paths.get("./testIO.txt");
        List<String> allLines = Files.readAllLines(file, Charset.defaultCharset());
        assertEquals("SampleFile",allLines.get(0));
    }

    @Test
    public void testReplaceStringMiddle() throws Exception {
        String initialString = "abxxxxxcd";
        String afterString = r.replaceSubString(initialString, "y", 2, 5);
        assertEquals("abycd",afterString);
    }

    @Test
    public void testSameFiles() throws Exception {
        Boolean isSame = r.areFilesIdentical("./same1.txt","./same2.txt");
        assertEquals((boolean)isSame,true);
    }
    @Test
    public void testDifferentFiles() throws Exception {
        Boolean isSame = r.areFilesIdentical("./same1.txt","./different1.txt");
        assertEquals((boolean)isSame,false);
    }

    @Test
    public void testJSONParse() throws Exception {
        String jsonString = "{\"IDE\":\"eclipse\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"eventType\":\"fileOpen\"}";
        JSONObject jObj = r.parseJSONString(jsonString);
        assertEquals(jObj.get("IDE"),"eclipse");
    }

    @Test
    public void testDispatchFileOpenEvent() throws Exception {
        JSONObject jObj = r.parseJSONString("{\"IDE\":\"eclipse\",\"entityAddress\":\"sampleProject\\/src\\/sampleProject\\/testClass.java\",\"eventType\":\"fileOpen\"}");
        String dest = r.dispatchJSON(jObj);
        assertEquals(dest,"fileOpen");
    }
    @Test
    public void testDispatchFileCloseEvent() throws Exception {
        JSONObject jObj = r.parseJSONString("{\"IDE\":\"eclipse\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"eventType\":\"fileClose\"}");
        String dest = r.dispatchJSON(jObj);
        assertEquals(dest,"fileClose");
    }

    @Test
    public void testDispatchTextChangeEvent() throws Exception {
        JSONObject jObj = r.parseJSONString("{\"text\":\"t\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":2}");
        String dest = r.dispatchJSON(jObj);
        assertEquals(dest,"textChange");
    }

    @Test
    public void testFileOpen() throws Exception {
        String jsonString = "{\"IDE\":\"eclipse\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"eventType\":\"fileOpen\"}";
        JSONObject jObj = r.parseJSONString(jsonString);
        String fileName = r.getFileNameFromJSON(jObj);
        System.out.println("FILENAME:" + fileName);
        r.openFile(fileName);
        assertEquals(r.isFileOpen(fileName),true);
    }

    @Test
    public void testCloseFileJSON() throws Exception {
        r.openFile("sampleProject/src/sampleProject/testClassOpen.java");
        String jsonString = "{\"IDE\":\"eclipse\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClassOpen.java\",\"eventType\":\"fileClose\"}";
        JSONObject jObj = r.parseJSONString(jsonString);
        Boolean isFileOpen = r.isFileOpen(jObj.get("entityAddress").toString());
        assertEquals((boolean)isFileOpen,false);

    }

    @Test
    public void testIsFileOpen() throws Exception {
        String jsonString = "{\"IDE\":\"eclipse\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClassOpen.java\",\"eventType\":\"fileOpen\"}";
        JSONObject jObj = r.parseJSONString(jsonString);
        Boolean isFileOpen = r.isFileOpen(jObj.get("entityAddress").toString());
        assertEquals((boolean)isFileOpen,false);

    }

    @Test
    public void testAddOpenFiles() throws Exception {
        String fileName = "\\/sampleProject\\/src\\/sampleProject\\/testClassOpen.java";
        String contents = "Initial File Contents";
        //r.addOpenFile(fileName,contents);

    }

    @Test
    public void testGetFileNameFromJSON() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":49}";
        //System.out.println("JSONStr"+JSONStr.toString());
        JSONObject jObj = r.parseJSONString(JSONStr);
        String fileName = r.getFileNameFromJSON(jObj);
        assertEquals(fileName,"sampleProject/src/sampleProject/testClass.java");
    }

    @Test
    public void testGetFileContentsString() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/testClass.java\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":49}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        String fileContents = r.getFileContentsString(r.getFileNameFromJSON(jObj));
        assertEquals(fileContents,"SAMPLE PRE DATA");
    }

    @Ignore
    public void testReadFile() throws Exception {
        String fileName = "/sampleProject/src/sampleProject/test.txt";
        if (fileName.charAt(0) == '/') {
            fileName = fileName.substring(1);
        }
        //System.out.println("fileName: "+ fileName);
        String fileContents;
        //System.out.println(Files.exists(Paths.get(fileName))) ;
        //Files.createFile(Paths.get("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt") );
        byte[] encoded = Files.readAllBytes(Paths.get("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt"));
        fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
        //System.out.println("FileContents: "+ fileContents);
    }

    @Test
    public void testOpenFileCreation() throws Exception {
        if(Files.exists(Paths.get("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt"))) {
        Files.delete(Paths.get("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt"));
        }
        r.readFile("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt") ;
        assertEquals(true,Files.exists(Paths.get("sampleProject/src/sampleProject/TEST_FILE_CREATION_JAVA_7.txt")));
    }

    @Test
    public void testGetTextFromObj() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        String text = r.getTextFromJSON(jObj);
        assertEquals("a",text);
    }

    @Test
    public void testGetOffsetFromJSON() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        int offset = r.getOffsetFromJSON(jObj);
        assertEquals(4,offset);
    }

    //TEST add change to File
    @Ignore
    public void testFileEdit() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":49}";
        //System.out.println("JSONStr"+JSONStr.toString());
        JSONObject jObj = r.parseJSONString(JSONStr);
        String fileContents = r.getFileContentsString(r.getFileNameFromJSON(jObj));
        System.out.println("Filecontents:"+fileContents);
    }

    @Ignore
    public void testFileChange() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        String fileContents = r.getFileContentsString(r.getFileNameFromJSON(jObj));
        System.out.println("Filecontents:"+fileContents);
    }

    @Ignore
    public void testName() throws Exception {
        String fileName = "sampleProject/src/sampleProject/test.txt";
        r.openFile(fileName);
        //r.editText(fileName);

        //System.out.println()
    }

    @Test
    public void testTextChangeAdd() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        r.textChange(jObj);
        assertEquals(r.getFileContentsString(r.getFileNameFromJSON(jObj)),"TESTaESTESTEST");
    }

    @Test
    public void testTextChangeDelete() throws Exception {
        String JSONStr = "{\"text\":\"\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":3,\"eventType\":\"textChange\",\"offset\":0}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        r.textChange(jObj);
        assertEquals(r.getFileContentsString(r.getFileNameFromJSON(jObj)),"TESTESTEST");
    }

    @Test
    public void testCloseFile() throws Exception {
        String JSONStr = "{\"text\":\"a\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"user\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        String fileName = r.getFileNameFromJSON(jObj);
        r.openFile(fileName);
        r.closeFile(fileName);
        assertEquals(r.isFileOpen(fileName), false);
    }

    @Test
    public void testWriteToFile() throws Exception {
        r.writeContentsToFile("text.txt", "THIS IS A TEST FILE");
        assertEquals(r.getFileContentsString("text.txt"),"THIS IS A TEST FILE");
    }

    @Test
    public void testRefreshEvent() throws Exception {
        String JSONStr = "{\"text\":\"NEW REFRESH TEXT\",\"entityAddress\":\"\\/sampleProject\\/src\\/sampleProject\\/test.txt\",\"changeOrigin\":\"refresh\",\"IDE\":\"eclipse\",\"len\":0,\"eventType\":\"textChange\",\"offset\":4}";
        JSONObject jObj = r.parseJSONString(JSONStr);
        r.textChange(jObj);
        assertEquals(r.getFileContentsString(r.getFileNameFromJSON(jObj)),"NEW REFRESH TEXT");
    }
}
