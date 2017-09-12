package br.com.olivum.recorder.note;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by allann on 8/9/17.
 */

public class Note {
    private static final String TAG = "Note";

    public final static String FOLDER_NAME = "olivum_notes";
    public final static String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;

    private String text = "";
    private String fileName = "";
    private int type = 0;
    private String date = "";

    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_PHOTO = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_UNSPECIFIED = 4;

    public Note(String filename) {
        this.fileName = filename;

        this.type = getTypeFromFileName(fileName);

        this.date = getDateFromFileName(fileName);
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

    public static int getTypeFromFileName(String name) {
        if (Pattern.compile("^audio_note").matcher(name).find()) {
            return TYPE_AUDIO;
        }
        else if (Pattern.compile("^photo_note").matcher(name).find()) {
            return TYPE_PHOTO;
        }
        else if (Pattern.compile("^video_note").matcher(name).find()) {
            return TYPE_VIDEO;
        }
        else {
            return TYPE_UNSPECIFIED;
        }
    }

    public static String getDateFromFileName(String fileName) {
        Pattern pattern = Pattern.compile("_note_([0-9]{4})([0-9]{2})([0-9]{2})_([0-9]{2})([0-9]{2})([0-9]{2})_([0-9]{3})");

        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(3) + "/" + matcher.group(2) + "/" + matcher.group(1) + " " +
                    matcher.group(4) + ":" + matcher.group(5) + ":" + matcher.group(6);
        }

        return "";
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static boolean deleteFile(String fileName) {
        String filePath = DIR_PATH + File.separator + fileName;

        Log.d(TAG, "Note.deleteFile(): " + filePath);

        File file = new File(filePath);

        return file.delete();
    }
}
