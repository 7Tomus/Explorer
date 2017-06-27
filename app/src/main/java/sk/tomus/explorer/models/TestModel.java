package sk.tomus.explorer.models;

/**
 * Created by Tomus on 16-Jun-17.
 */

public class TestModel {

    private String fileName;
    private int fileSize;
    private String fileExtension;

    public TestModel(String fileName, String fileExtension, int fileSize){
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
