package edu.oregonstate.edu;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: michaelhilton
 * Date: 10/24/13
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Replay {

    protected List<OpenFile> allOpenFiles;
    String replayDir = "";

    public Replay() {
        this.allOpenFiles = new ArrayList<OpenFile>();  ;
    }

    String insertString(String s, String c, int i) {
        StringBuilder str = new StringBuilder(s);
        str.insert(i, c);
        return str.toString();
    }

    public String removeSubString(String initialString, int start, int length) {
        StringBuilder sb = new StringBuilder(initialString);
        sb.delete(start, start + length);
        return sb.toString();
    }

    public String replaceSubString(String initialString, String insertedString, int start, int length) {
        StringBuilder sb = new StringBuilder(initialString);
        sb.replace(start, start + length, insertedString);
        return sb.toString();
    }

    protected List<String> getFileContentsList(String filename) {
        Path filePath = Paths.get(filename);
        List<String> allLines = null;
        try {
            allLines = Files.readAllLines(filePath, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return allLines;
    }

    public Boolean areFilesIdentical(String fileName1, String fileName2) {
        List<String> file1 = getFileContentsList(fileName1);
        List<String> file2 = getFileContentsList(fileName2);
        if (file1.equals(file2)) {
            return true;
        }
        return false;
    }

    public void replayFile(String fileName) {
        if(replayDir.length()>0){
            fileName = replayDir + "/"+ fileName;
        }
        List<String> replayFileContents = getFileContentsList(fileName);
        // iterator loop
        //System.out.println("#1 iterator");
        Iterator<String> iterator = replayFileContents.iterator();
        while (iterator.hasNext()) {
            String currLine = iterator.next();
            currLine = currLine.replace("$@$", "");
            JSONObject curjObj = parseJSONString(currLine);
            if(curjObj!= null){
                System.out.println(curjObj.toString());
                dispatchJSON(curjObj);

            }

        }

    }

    public JSONObject parseJSONString(String jsonString) {
        Object obj = JSONValue.parse(jsonString);
        JSONObject jObj = (JSONObject) obj;
        return jObj;
    }


    public String dispatchJSON(JSONObject jObj) {
        String eventDispatched = "Unknown eventType";
        switch (jObj.get("eventType").toString()) {
            case "fileOpen":
                openFile(getFileNameFromJSON(jObj));
                eventDispatched = "fileOpen";
                break;
            case "fileClose":
                eventDispatched = "fileClose";
                closeFile(getFileNameFromJSON(jObj));
                break;
            case "textChange":
                eventDispatched = "textChange";
                textChange(jObj);
                break;
            case "testRun":
                System.out.println("testRun");
                break;
            case "FileInit":
                System.out.println("testRun");
                break;
            default:
                throw new RuntimeException("Unknown eventType");

        }

        return eventDispatched;  //To change body of created methods use File | Settings | File Templates.
    }

    protected void textChange(JSONObject jObj) {
        String fileName = getFileNameFromJSON(jObj);
        openFile(fileName);
        String changeOrigin = getChangeOrigin(jObj);
        String currFileContents;
        if(changeOrigin.equals("refresh")){
            currFileContents = getTextFromJSON(jObj);
        }else{
            currFileContents = getFileContentsString(fileName);
            int textReplaceLength = getLengthFromJSON(jObj);
            if(textReplaceLength > 0){
                currFileContents = removeSubString(currFileContents,getOffsetFromJSON(jObj),textReplaceLength);
            }
            currFileContents = insertString(currFileContents,getTextFromJSON(jObj),getOffsetFromJSON(jObj));
        }

        setFileContents(fileName,currFileContents) ;

    }

    private String getChangeOrigin(JSONObject jObj) {
        return jObj.get("changeOrigin").toString();
    }

    private int getLengthFromJSON(JSONObject jObj) {
        return Integer.parseInt(jObj.get("len").toString());
    }

    private void setFileContents(String fileName, String newFileContents) {
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(fileName.equals(allOpenFiles.get(i).getFileName())){
                OpenFile of = allOpenFiles.get(i);
                of.setContents(newFileContents);
                allOpenFiles.set(i,of);
            }
        }
    }

    public void openFile(String fileName) {
        String fileContents = "";
        if(!isFileOpen(fileName)){
            fileContents = readFile(fileName);
            OpenFile of = new OpenFile(fileName,fileContents);
            allOpenFiles.add(of);
        }
    }


    public String readFile(String fileName) {
        String fileContents = "";
        File f = new File(Paths.get(fileName).toUri()) ;
        if(!f.exists()){
            try {
                if(!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }

                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return fileContents;
    }

    public boolean isFileOpen(String fileName) {
        boolean isFileOpen = false;
        //System.out.println("Size"+allOpenFiles.size());
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(fileName.equals(allOpenFiles.get(i).getFileName())){
                isFileOpen = true;
            }
        }
        return isFileOpen;
    }

    protected String getFileNameFromJSON(JSONObject jObj) {
        String fileName = jObj.get("entityAddress").toString();
        if (fileName.charAt(0) == '/') {
            fileName = fileName.substring(1);
        }
        if(replayDir.length()>0){
            fileName = replayDir + "/" + fileName;
        }
        return fileName;
    }

    public String getFileContentsString(String fileName) {
        if(!isFileOpen(fileName)){
            openFile(fileName);
        }
        String contents = getOpenFileContents(fileName);
        return contents;  //To change body of created methods use File | Settings | File Templates.
    }

    private String getOpenFileContents(String fileName) {
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(fileName.equals(allOpenFiles.get(i).getFileName())){
                return allOpenFiles.get(i).getContents();
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }


    public String getTextFromJSON(JSONObject jObj) {
        return jObj.get("text").toString();
    }

    public int getOffsetFromJSON(JSONObject jObj) {
        return Integer.parseInt(jObj.get("offset").toString());
    }

    public void closeFile(String fileName) {
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(fileName.equals(allOpenFiles.get(i).getFileName())){
                writeContentsToFile(fileName, allOpenFiles.get(i).getContents());
                allOpenFiles.remove(i);
                break;
            }
        }
        //To change body of created methods use File | Settings | File Templates.
    }

    protected void writeContentsToFile(String fileName, String currFileContents) {
        try {
            Files.write(Paths.get(fileName), currFileContents.getBytes(),StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void closeAllFiles() {
        for (int i = 0; i < allOpenFiles.size(); i++) {
            writeContentsToFile(allOpenFiles.get(i).getFileName(), allOpenFiles.get(i).getContents());
        }
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(isFileOpen(allOpenFiles.get(i).getFileName())){
                openFile(allOpenFiles.get(i).getFileName());
                closeFile(allOpenFiles.get(i).getFileName());
            }

        }
    }

    /**
     * Returns a zip file system
     * @param zipFilename to construct the file system from
     * @param create true if the zip file should be created
     * @return a zip file system
     * @throws java.io.IOException
     *
     * method from : http://fahdshariff.blogspot.com/2011/08/java-7-working-with-zip-files.html
     */
    private static FileSystem createZipFileSystem(String zipFilename,
                                                  boolean create)
            throws IOException {
        // convert the filename to a URI
        final Path path = Paths.get(zipFilename);
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * Unzips the specified zip file to the specified destination directory.
     * Replaces any files in the destination, if they already exist.
     * @param zipFilename the name of the zip file to extract
     * @param destFilename the directory to unzip to
     * @throws java.io.IOException
     * method from : http://fahdshariff.blogspot.com/2011/08/java-7-working-with-zip-files.html
     */
    public static void unzip(String zipFilename, String destDirname)
            throws IOException{

        final Path destDir = Paths.get(destDirname);
        //if the destination doesn't exist, create it
        if(Files.notExists(destDir)){
            //System.out.println(destDir + " does not exist. Creating...");
            Files.createDirectories(destDir);
        }

        try (FileSystem zipFileSystem = createZipFileSystem(zipFilename, false)){
            final Path root = zipFileSystem.getPath("/");

            //walk the zip file tree and copy files to the destination
            Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                    final Path destFile = Paths.get(destDir.toString(),
                            file.toString());
                    //System.out.printf("Extracting file %s to %s\n", file, destFile);
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destDir.toString(),
                            dir.toString());
                    if(Files.notExists(dirToCreate)){
                        //System.out.printf("Creating directory %s\n", dirToCreate);
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public void unzipInitialState(String zipName) {
        if(replayDir.length()>0){
            zipName = replayDir + "/" + zipName;
        }
        if(Files.exists(Paths.get(zipName))){
        try {
            unzip(zipName,replayDir) ;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        }
    }

    public void setDir(String dir) {
        replayDir =  dir;
    }
}