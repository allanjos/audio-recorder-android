package br.com.olivum.recorder.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.olivum.recorder.application.App;
import br.com.olivum.recorder.note.Note;

/**
 * Created by allann on 8/6/17.
 */

public class AudioNote {
    private static final String TAG = "AudioNote";
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO; // AudioFormat.CHANNEL_IN_MONO
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static AudioRecord recorder = null;
    private static Thread recordingThread = null;
    private static Thread playerThread = null;
    private static String filePrefix = "audio_note_";
    private static String lastRecordedFilePath = "";

    private static int bufferElementsToRecord = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    private static int bytesPerElement = 2; // 2 bytes in 16bit format

    private static Boolean recording = false;
    private static Boolean playing = false;

    private static boolean playingStopRequest = false;
    private static boolean recordingStopRequest = false;

    private static List<AudioListener> listeners = new ArrayList<>();

    private static Handler handler = null;

    private static AudioTrack audioTrack = null;

    private enum AudioState {
        RECORDER_START,
        RECORDER_STOP,
        PLAYER_START,
        PLAYER_STOP
    }

    public static String getAudioNotesDirPath() {
        return Note.DIR_PATH;
    }

    public static Boolean createDirectory() {
        Log.d(TAG, "createDirectory()");

        String path = AudioNote.getAudioNotesDirPath();

        Log.d(TAG, "Path: " + path);

        File directory = new File(path);

        if (directory.exists()) {
            if (!directory.isDirectory()) {
                Log.e(TAG, "Path is not a directory: " + path);

                return false;
            }
            else {
                Log.d(TAG, "Directory already exists: " + path);

                return true;
            }
        }

        return directory.mkdir();
    }

    public static void init() {
        if (handler == null) {
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    Log.d(TAG, "Handler.handleMessage()");

                    Log.d(TAG, "msg: " + msg.obj);

                    int state = Integer.parseInt(msg.obj.toString());

                    if (state == AudioState.RECORDER_START.ordinal()) {
                        Log.d(TAG, "AudioState.RECORDER_START");

                        notifyOnRecordStart();
                    }
                    else if (state == AudioState.RECORDER_STOP.ordinal()) {
                        Log.d(TAG, "AudioState.RECORDER_STOP");

                        notifyOnRecordStop();
                    }
                    else if (state == AudioState.PLAYER_START.ordinal()) {
                        Log.d(TAG, "AudioState.PLAYER_START");

                        notifyOnPlayStart();
                    }
                    else if (state == AudioState.PLAYER_STOP.ordinal()) {
                        Log.d(TAG, "AudioState.PLAYER_STOP");

                        notifyOnPlayStop();
                    }
                    else {
                        Log.d(TAG, "Unknow message");
                    }
                }
            };
        }
    }

    public static Boolean isRecording() {
        return recording;
    }

    public static Boolean isPlaying() {
        return playing;
    }

    public static void startRecord(Context context) {
        Log.d(TAG, "startRecord()");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault());

        Date now = new Date();

        String strDate = sdf.format(now);

        final String filePath = getAudioNotesDirPath() + File.separator + filePrefix + strDate + ".pcm";

        startRecord(context, filePath);
    }

    public static void startRecord(Context context, final String filePath) {
        init();

        if (!createDirectory()) {
            Log.d(TAG, "Cannot create directory" + getAudioNotesDirPath());

            return;
        }

        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                       RECORDER_SAMPLERATE,
                                       RECORDER_CHANNELS,
                                       RECORDER_AUDIO_ENCODING,
                                       bufferElementsToRecord * bytesPerElement);
        }
        catch (Exception e) {
            Log.e(TAG, "AudioRecorder exception : " + e.getMessage());

            return;
        }

        Toast.makeText(App.getInstance().getApplicationContext(), "Recording " + filePath, Toast.LENGTH_SHORT).show();

        recorder.startRecording();

        recording = true;

        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile(filePath);
            }
        }, "AudioRecorder Thread");

        notifyOnRecordStart();

        recordingThread.start();
    }

    private static void writeAudioDataToFile(String filePath) {
        Log.d(TAG, "writeAudioDataToFile()");

        short audioData[] = new short[bufferElementsToRecord];

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(filePath);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        File file = new File(filePath);

        lastRecordedFilePath = file.getName();

        while (!recordingStopRequest) {
            // Gets the data from microphone

            recorder.read(audioData, 0, bufferElementsToRecord);

            //Log.d(TAG, "Short writing to file " + audioData.toString());

            try {
                // Convert audio data to byte

                byte byteData[] = short2byte(audioData);

                // Write to buffer

                outputStream.write(byteData, 0, bufferElementsToRecord * bytesPerElement);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Message msg = new Message();

        recorder.stop();

        recorder.release();

        recorder = null;

        recording = false;

        recordingStopRequest = false;

        msg.obj = String.valueOf(AudioState.RECORDER_STOP.ordinal());

        handler.sendMessage(msg);
    }

    // Convert short to byte
    private static byte[] short2byte(short[] audioData) {
        int audioDataCount = audioData.length;

        byte[] bytes = new byte[audioDataCount * 2];

        for (int i = 0; i < audioDataCount; i++) {
            bytes[i * 2] = (byte) (audioData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (audioData[i] >> 8);

            audioData[i] = 0;
        }

        return bytes;
    }

    public static void stopRecord() {
        Log.d(TAG, "stopRecord()");

        if (!recording) {
            Log.d(TAG, "Audio recorder already stopped.");

            return;
        }

        recordingStopRequest = true;

        // Stop the recording activity

        if (null == recorder) {
            return;
        }

        try {
            recordingThread.join();
        }
        catch (InterruptedException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }

        recordingThread = null;
    }

    @SuppressWarnings("deprecation")
    public static void playAudioFile() {
        Log.d(TAG, "playAudioFile()");

        if (lastRecordedFilePath.equals("")) {
            Log.e(TAG, "There is no recorded audio file.");

            return;
        }

        playAudioFile(lastRecordedFilePath);
    }

    public static void playAudioFile(final String fileName) {
        Log.d(TAG, "playAudioFile()");

        if (isPlaying()) {
            Log.d(TAG, "Audio player is already playing");

            return;
        }

        init();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault());

        Date now = new Date();

        String strDate = sdf.format(now);

        final String filePath = getAudioNotesDirPath() + File.separator + fileName;

        playing = true;

        int intSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                    RECORDER_SAMPLERATE,
                                    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                                    RECORDER_AUDIO_ENCODING,
                                    intSize,
                                    AudioTrack.MODE_STREAM);

        playerThread = new Thread(new Runnable() {
            public void run() {
                playFileThreadFunction(filePath);
            }
        }, "AudioRecorder Thread");

        notifyOnPlayStart();

        playerThread.start();
    }

    public static void stopPlayer() {
        Log.d(TAG, "stopPlayer()");

        if (!playing) {
            Log.d(TAG, "Audio player already stopped.");

            return;
        }

        playingStopRequest = true;

        // stops the recording activity
        if (null == recorder) {
            return;
        }

        try {
            playerThread.join();
        }
        catch (InterruptedException e) {
            Log.d(TAG, "Exception: " + e.getMessage());
        }

        playerThread = null;
    }

    private static void playFileThreadFunction(String filePath) {
        Log.d(TAG, "playFileThreadFunction()");

        int count = 1024; // 512 kb

        byte[] byteData;

        Log.d(TAG, "Audio file path to play: " + filePath);

        File file = new File(filePath);

        byteData = new byte[count];

        FileInputStream in = null;

        try {
            in = new FileInputStream(file);

            int bytesRead = 0, ret = 0;

            int size = (int) file.length();

            audioTrack.play();

            while (bytesRead < size) {
                if (playingStopRequest) {
                    break;
                }

                ret = in.read(byteData, 0, count);

                if (ret != -1) { // Write the byte array to the track
                    audioTrack.write(byteData, 0, ret);

                    bytesRead += ret;
                }
                else {
                    break;
                }
            }

            in.close();
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException: " + e.getMessage());
        }
        catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage());
        }

        audioTrack.stop();

        audioTrack.release();

        playingStopRequest = false;

        playing = false;

        Message msg = new Message();

        msg.obj = String.valueOf(AudioState.PLAYER_STOP.ordinal());

        handler.sendMessage(msg);
    }

    public static void registerListener(AudioListener listener) {
        listeners.add(listener);
    }

    private static void notifyOnPlayStart() {
        Log.d(TAG, "notifyOnPlayStart()");

        for(AudioListener listener : listeners){
            listener.onPlayStart();
        }
    }

    private static void notifyOnPlayStop() {
        Log.d(TAG, "notifyOnPlayStop()");

        for(AudioListener listener : listeners){
            listener.onPlayStop();
        }
    }

    private static void notifyOnRecordStart() {
        Log.d(TAG, "notifyOnRecordStart()");

        for(AudioListener listener : listeners){
            listener.onRecordStart();
        }
    }

    private static void notifyOnRecordStop() {
        Log.d(TAG, "notifyOnRecordStop()");

        for(AudioListener listener : listeners){
            listener.onRecordStop();
        }
    }
}