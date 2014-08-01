package edu.oregonstate.edu.TDD_Classifier;

import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.oregonstate.cope.eclipse.astinference.ast.ASTInferencerFacade;
import edu.oregonstate.cope.eclipse.astinference.ast.inferencing.InferredAST;
import edu.oregonstate.edu.Replayer.OpenFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static edu.oregonstate.edu.TDD_Classifier.Event.parseJSONString;


/**
 * Created by mcdse_000 on 7/25/2014.
 */

/*
       TDD_Classifier (Whole project) to-do list:
       TODO: [PRIORITY] output green cycle AST and diff data
       TODO: clean unnecessary methods from "Classify"
       TODO: modularize any outdated methods from "Classify"
 */

public class Classify {

    public static final boolean LOGGING = false;
    private static final boolean CLASS_LOGGING = false;
    private final JSONArray classificationJSON;
    private String replayDir = "";
    private String replayFile = "";
    private OperationDeserializer operationDeserializer;
    private ASTInferencerFacade inferencer;
    public static final List<String> knownTextFiles = Arrays.asList(new String[]{"txt", "java", "xml", "mf", "c", "cpp", "c", "h"});
    protected List<OpenFile> allOpenFiles;
    public List<String> allProjectDirectories;

    /* The following are variables that need to be kept with persistence as the program runs*/
    private String prevTest = "None"; //keeps a persistent value of last test that can be referenced outside classification functions
    private String currentTest = "No tests"; //used to group tests run in succession
    private Integer beginCycle = 0; //keeps track of beginning of the current cycle
    private String lastPhase = "None";
    private String editedFile = "None";
    private List<Cycle> cycles = new ArrayList<>();
    private boolean takenSnapshot = false;
    protected  List<OpenFile> snapshot;
    private List<String> currentFileDiffs;

    public Classify(String fileName, String Dir){
        classificationJSON = new JSONArray();
        replayDir = Dir;
        replayFile = fileName;
        this.allOpenFiles = new ArrayList<OpenFile>();
        allProjectDirectories = new ArrayList<String>();
        this.snapshot = new ArrayList<OpenFile>();
        this.currentFileDiffs = new ArrayList<String>();
    }

    public void classifyFile() {
        List<String> replayFileContents = getFileContentsList(replayFile);
        initASTInferencerState(replayFile);
        int currentIndex = 0;

        // iterator loop
        Iterator<String> iterator = replayFileContents.iterator();
        while (iterator.hasNext()) {
            String currLine = iterator.next();
            currLine = currLine.replace("$@$", "");
            JSONObject curjObj = parseJSONString(currLine);
            if(curjObj!= null){
                classifyObject(curjObj, currentIndex);
                currentIndex++;
            }
        }
        classifyPhase(currentIndex);
        writeContentsToFile(replayDir + "/classificationJSON.json", classificationJSON.toString());
    }

    private void createNewObject(String startIndex, String endIndex, String CycleType) {
        JSONObject newObj = new JSONObject();
        newObj.put("id", startIndex + endIndex);
        newObj.put("CycleType", CycleType);
        newObj.put("CycleStart", startIndex);
        newObj.put("CycleEnd", endIndex);
        classificationJSON.add(newObj);

        if(CycleType.equals("red")){
            //add new cycle
            Cycle newCyc = new Cycle();
            newCyc.addPhase(newObj.toJSONString());
            cycles.add(newCyc);
        }else{
            //update existing cycle
            cycles.get(cycles.size() - 1).addPhase(newObj.toJSONString());
        }
        return;
    }

    private boolean classifyPhase(int currentIndex){
         /* following must be - 1 since the currentIndex is wrong (due to the above function
         needing to go 1 operation after the test group to know its out of a test group) */
        Integer idx = currentIndex - 1;
        boolean newTest = newTestCreated();
        if(prevTest.equals("None")){
            if(currentTest.equals("Failed")){
                if (CLASS_LOGGING) System.out.print("\nRED DETECTED***********************");
                classifyRed(idx);
                return true;
            }else{
                /*Classify none*/
                if (CLASS_LOGGING) System.out.print("\nNON-TDD DETECTED***********************");
                return true;
            }
        }else if(prevTest.equals("Passed")){
            if(currentTest.equals("Failed")) {
                if(newTest){
                    if (CLASS_LOGGING) System.out.print("\nRED DETECTED***********************");
                    classifyRed(idx);
                    return true;
                }else{
                    classifyBlue(idx);
                    return true;
                }
            }else{ // Current test Passed
                if(!newTest){
                    if (CLASS_LOGGING) System.out.print("\nBLUE DETECTED**********************");
                    classifyBlue(idx);
                    return true;
                }else{
                    if (CLASS_LOGGING) System.out.print("\nNON-TDD DETECTED***********************");
                    /*Classify none*/
                    return true;
                }
            }
        }else if(prevTest.equals("Failed")){
            if(currentTest.equals("Passed")){
                if(!newTest){
                    if (CLASS_LOGGING) System.out.print("\nGREEN DETECTED**********************");
                    classifyGreen(idx);
                    return true;
                }else{
                    if (CLASS_LOGGING) System.out.print("\nNON-TDD DETECTED***********************");
                    /*Classify none*/
                    return true;
                }
            }else{ // Current test Failed
                if(newTest){
                    if (CLASS_LOGGING) System.out.print("\nNON-TDD DETECTED***********************");
                    /*Classify none*/
                    return true;
                }
            }
        }
        return false; // **FALLTHROUGH case is prev failed, curr failed and no new test so indeterminate
    }

    private void classifyRed(Integer idx) {
        createNewObject(beginCycle.toString(), idx.toString(), "red");
        lastPhase = "red";
    }

    private void classifyGreen(Integer idx) {
        outputDiff();
        createNewObject(beginCycle.toString(), idx.toString(), "green");
        lastPhase = "green";
    }

    private void classifyBlue(Integer idx){
        if(!lastPhase.equals("blue")){
            createNewObject(beginCycle.toString(), idx.toString(), "blue");
        }else{
             /*Change the end index of the last phase (since it was blue) to the current index*/
            JSONObject oldObj = (JSONObject) classificationJSON.get(classificationJSON.size() - 1);
            oldObj.put("CycleEnd", idx);
            oldObj.put("id", oldObj.get("CycleStart") + idx.toString());
            classificationJSON.remove(classificationJSON.size() - 1);
            classificationJSON.add(oldObj);
        }
        lastPhase = "blue";
    }

    private void nextPhase(int currentIndex){
        beginCycle = currentIndex;
    }

    public void classifyObject(JSONObject jObj, int currentIndex) {
        String eventDispatched = "Unknown eventType";
        String event = jObj.get("eventType").toString();
        boolean canInferAST = canInferAST(jObj);
        UserOperation userOperation = null;
        InferredAST iASTObj = new InferredAST();

        /*
        if (canInferAST) {
            userOperation = operationDeserializer.buildUserOperation(jObj, event);
            if(userOperation != null){
                String fileContentsBeforeChange = getFileContentsString(getFileNameFromJSON(jObj));
                inferencer.beforeDocumentChanged(userOperation, fileContentsBeforeChange, getFileNameFromJSON(jObj),iASTObj);
                if(iASTObj.getInferredAST() != null){
                    System.out.println("FOUND AST");
                    JSONObject inferredAST = new JSONObject();
                    inferredAST.put("InferredAST",iASTObj.getInferredAST());
                    //TODO: save AST data
                }
            }
        }
        */
        switch (event) {
            case "fileOpen":
                eventDispatched = "fileOpen";
                openFile(getFileNameFromJSON(jObj));
                if (LOGGING) System.out.println("fileOpen");
                break;
            case "fileClose":
                eventDispatched = "fileClose";
                closeFile(getFileNameFromJSON(jObj));
                if (LOGGING) System.out.println("fileClose");
                break;
            case "textChange":
                eventDispatched = "textChange";
                String currText = textChange(jObj);
                if (LOGGING) System.out.println("textChange");
                break;
            case "testRun":
                eventDispatched = "testRun";
                // TODO: takeSnapshot is naive
                if(!takenSnapshot) {
                    if(snapshot.size() != 0) {
                        diff();
                    }
                    takeSnapshot();
                    takenSnapshot = true;
                }
                 /*in the case that a test was run, all tests run in succession must be combined
                 so that if a single test failed, the entire group counts as a failure
                  */
                if(jObj.get("testResult").equals("Failure") || jObj.get("testResult").equals("Error")){
                    currentTest = "Failed";
                }else if(!currentTest.equals("Failed")){
                    currentTest = "Passed";
                }
                if (LOGGING) System.out.println("testRun");
                break;
            case "FileInit":
                eventDispatched = "FileInit";
                if (LOGGING) System.out.println("FileInit");
                break;
            case "resourceAdded":
                eventDispatched = "resourceAdded";
                addResource(jObj);
                if (LOGGING) System.out.println("resourceAdded");
                break;
            case "resourceRemoved":
                eventDispatched = "resourceRemoved";
                removeResource(jObj);
                if (LOGGING) System.out.println("resourceRemoved");
                break;
            case "refresh":
                eventDispatched = "refresh";
                refresh(jObj);
                if (LOGGING) System.out.println("refresh");
                break;
            case "refactoringLaunch":
                eventDispatched = "refactoringLaunch";
                if (LOGGING) System.out.println("refactoringStart");
                break;
            case "refactoringEnd":
                eventDispatched = "refactoringEnd";
                if (LOGGING) System.out.println("refactoringEnd");
                break;
            case "normalLaunch":
                eventDispatched = "normalLaunch";
                if (LOGGING) System.out.println("NormalLaunch");
                break;
            case "launchEnd":
                eventDispatched = "launchEnd";
                if (LOGGING) System.out.println("launchEnd");
                break;
            case "snapshot":
                eventDispatched = "snapshot";
                if (LOGGING) System.out.println("snapshot");
                break;
            case "fileSave":
                eventDispatched = "fileSave";
                if (LOGGING) System.out.println("fileSave");
                break;
            case "copy":
                eventDispatched = "copy";
                if (LOGGING) System.out.println("copy");
                break;
            default:
                throw new RuntimeException("Unknown eventType");
        }

        if(!currentTest.equals("No tests") && !(event.equals("testRun") || event.equals("normalLaunch") || event.equals("launchEnd"))){
            if(classifyPhase(currentIndex)) {
                nextPhase(currentIndex);
            }
            prevTest = currentTest;
            currentTest = "No tests";
            takenSnapshot = false;
        }
        if(!event.equals("refresh") || !event.equals("fileSave") || !event.equals("snapshot")) {
            editedFile = fileEdited(jObj);
        }
        /*
        if (canInferAST) {
            inferencer.flushCurrentTextChanges(userOperation,iASTObj);
            inferencer.handleResourceOperation(userOperation);
        }
        */
    }

    private boolean newTestCreated(){
        for(int i = 0; i < currentFileDiffs.size(); i++){
            if(currentFileDiffs.get(i).contains("@Test")){
                return true;
            }
        }
        return false;
    }

    private void outputDiff(){
        System.out.print("\n***********************DIFF*******************************\n");
        for(int i = 0; i < currentFileDiffs.size(); i++) {
            System.out.print(currentFileDiffs.get(i));
        }
        System.out.print("\n*************************-----*****************************\n\n\n");
    }

    private void takeSnapshot(){
        for(int i = 0; i < allOpenFiles.size(); i++){
            snapshot.get(i).setContents(allOpenFiles.get(i).getContents());
        }
    }

    private void diff(){
        currentFileDiffs.clear();
        diff_match_patch newDiffConstructor = new diff_match_patch();
        for(int i = 0; i < allOpenFiles.size(); i++) {
            String output = "";
            String prev = snapshot.get(i).getContents();
            String curr = allOpenFiles.get(i).getContents();
            LinkedList<diff_match_patch.Diff> diff = newDiffConstructor.diff_main(prev, curr);
            newDiffConstructor.diff_cleanupSemantic(diff);
            Iterator<diff_match_patch.Diff> it = diff.iterator();
            while (it.hasNext()) {
                diff_match_patch.Diff d = it.next();
                if (d.operation.equals(diff_match_patch.Operation.DELETE) || d.operation.equals(diff_match_patch.Operation.EQUAL)) {
                    it.remove();
                }else{
                    output += d.toString();
                }
            }

            currentFileDiffs.add(output);
        }
    }

    private String fileEdited(JSONObject obj){
        switch (editedFile){
            case "None":
                return whichFile(obj);
            case "test_file":
                if(whichFile(obj).equals("production_file")){
                    return "both_files";
                }else{
                    return whichFile(obj);
                }
            case "production_file":
                if(whichFile(obj).equals("test_file")){
                    return "both_files";
                }else{
                    return whichFile(obj);
                }
            default:
                return "None";
        }
    }

    private String whichFile(JSONObject obj){
        if(!obj.containsKey("entityAddress")){
            return "None";
        }
        if(obj.get("entityAddress").toString().contains("test") || obj.get("entityAddress").toString().contains("Test")){
            return "test_file";
        }else{
            return "production_file";
        }
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


    private void initASTInferencerState(String fileName) {
        operationDeserializer = new OperationDeserializer(fileName);

        inferencer = ASTInferencerFacade.getInstance();
        inferencer.setRecordingDirectory(new File(replayDir));
    }


    public void cleanProjectDirs() {
        if (LOGGING) System.out.println("Cleaning project directories...");

        for (String projectName : allProjectDirectories) {
            if (LOGGING) System.out.println("projectName: " + projectName);
            try {
                final File project = new File(projectName);
                FileUtils.cleanDirectory(project);
                FileUtils.forceDelete(project);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean canInferAST(JSONObject jObj) {
        return jObj.get("entityAddress") != null;
    }

    private void removeResource(JSONObject jObj) {
        closeFile(jObj.get("entityAddress").toString());
    }

    private void refresh(JSONObject jObj) {
        //byte[] b = Base64.decodeBase64(jObj.get("text").toString()) ;
        //System.out.println(new String(b));
        //setFileContents(jObj.get("entityAddress").toString(),new String(b));

        String fileName = jObj.get("entityAddress").toString();
        if (!knownTextFiles.contains(FilenameUtils.getExtension(fileName))){
            byte[] b = Base64.decodeBase64(jObj.get("text").toString()) ;
            setFileContents(jObj.get("entityAddress").toString(),new String(b));
        }else{
            setFileContents(jObj.get("entityAddress").toString(),jObj.get("text").toString());
        }


    }

    private void addResource(JSONObject jObj) {
        String fileName = jObj.get("entityAddress").toString();

        if (fileName.charAt(0) == '/') {
            fileName = fileName.substring(1);
        }
        fileName = replayDir + "/" + fileName;

        if (fileName.endsWith(".project")) {
            allProjectDirectories.add(Paths.get(fileName).getParent().toString());
        }

        if (!knownTextFiles.contains(FilenameUtils.getExtension(fileName))){
            byte[] b = Base64.decodeBase64(jObj.get("text").toString());
            edu.oregonstate.edu.Replayer.OpenFile of = new edu.oregonstate.edu.Replayer.OpenFile(fileName,new String(b));
            edu.oregonstate.edu.Replayer.OpenFile snapof = new edu.oregonstate.edu.Replayer.OpenFile(fileName,new String(b));
            allOpenFiles.add(of);
            snapshot.add(snapof);
        }else{
            edu.oregonstate.edu.Replayer.OpenFile of = new edu.oregonstate.edu.Replayer.OpenFile(fileName,jObj.get("text").toString());
            edu.oregonstate.edu.Replayer.OpenFile snapof = new edu.oregonstate.edu.Replayer.OpenFile(fileName,jObj.get("text").toString());
            allOpenFiles.add(of);
            snapshot.add(snapof);
        }
    }


    protected String textChange(JSONObject jObj) {
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
        return currFileContents;

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

    private String getChangeOrigin(JSONObject jObj) {
        return jObj.get("changeOrigin").toString();
    }

    private int getLengthFromJSON(JSONObject jObj) {
        return Integer.parseInt(jObj.get("len").toString());
    }

    private void setFileContents(String fileName, String newFileContents) {
        for (int i = 0; i < allOpenFiles.size(); i++) {
            if(allOpenFiles.get(i).getFileName().contains(fileName)){
                edu.oregonstate.edu.Replayer.OpenFile of = allOpenFiles.get(i);
                of.setContents(newFileContents);
                allOpenFiles.set(i,of);
            }
        }
    }

    public void openFile(String fileName) {
        String fileContents = "";

        if(!isFileOpen(fileName)){
            fileContents = readFile(fileName);
            edu.oregonstate.edu.Replayer.OpenFile of = new edu.oregonstate.edu.Replayer.OpenFile(fileName,fileContents);
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
//            byte[] b = Base64.decodeBase64(jObj.get("text").toString()) ;
//            System.out.println(new String(b));
//            setFileContents(jObj.get("entityAddress").toString(),new String(b));

            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            byte[] decoded = Base64.decodeBase64(encoded);
            fileContents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
            //fileContents = new String(decoded);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return fileContents;
    }

    public boolean isFileOpen(String fileName) {
        boolean isFileOpen = false;
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
            //Path path = Paths.get(replayDir.concat(fileName));
            Path path = Paths.get(fileName);
            Path parentDir = path.getParent();
            if (!Files.exists(parentDir))
                Files.createDirectories(parentDir);
            Files.write(path, currFileContents.getBytes(), StandardOpenOption.CREATE);
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
     * @param destDirname the directory to unzip to
     * @throws java.io.IOException
     * method from : http://fahdshariff.blogspot.com/2011/08/java-7-working-with-zip-files.html
     */
    public static void unzip(String zipFilename, String destDirname)
            throws IOException{

        final Path destDir = Paths.get(destDirname);
        //if the destination doesn't exist, create it
        if(Files.notExists(destDir)){
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
