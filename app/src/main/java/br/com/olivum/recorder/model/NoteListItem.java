package br.com.olivum.recorder.model;

/**
 * Created by allann on 8/9/17.
 */

public class NoteListItem {
    private String text = "";
    private String fileName = "";

    public NoteListItem(String filename) {
        this.fileName = filename;

        this.text = filename;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
