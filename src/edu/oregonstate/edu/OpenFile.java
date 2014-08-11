package edu.oregonstate.edu;

/**
 * Created with IntelliJ IDEA.
 * User: michaelhilton
 * Date: 10/31/13
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpenFile {
    private String fileName;
    private String Contents;

    public OpenFile(String fileName,String contents) {
        setFileName(fileName);
        setContents(contents);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContents() {
        return Contents;
    }

    public void setContents(String contents) {
        Contents = contents;
    }
}
