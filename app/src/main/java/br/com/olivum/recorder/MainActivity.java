package br.com.olivum.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Boolean isRecording = false;
    //MediaRecorder recorder = new MediaRecorder();
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO; // AudioFormat.CHANNEL_IN_MONO
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;

    int bufferElementsToRecord = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int bytesPerElement = 2; // 2 bytes in 16bit format

    String fileName = "voice8K16bitstereo.pcm";
    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    Button buttonRecord;
    Button buttonPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);


        // EditText - file name

        final EditText editTextFileName = (EditText) findViewById(R.id.editTextFileName);

        editTextFileName.setText(fileName);

        // EditText - file path

        final EditText editTextFilePath = (EditText) findViewById(R.id.editTextFilePath);

        editTextFilePath.setText(filePath);

        // Button - Start to record

        buttonRecord = (Button) findViewById(R.id.buttonRecord);

        // Button - Play recorded file

        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        buttonPlay.setEnabled(false);

        // Update UI

        updateButton();

        // Events

        buttonRecord.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (!MainActivity.this.isRecording) {
                    filePath = editTextFilePath.getText().toString();
                    fileName = editTextFileName.getText().toString();

                    // Start record

                    //SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss", Locale.US);

                    //String filename = s.format(new Date());

                    //Toast.makeText(MainActivity.this, "Recording " + filename, Toast.LENGTH_SHORT).show();

                    startRecord(fileName);
                }
                else {
                    // Stop record

                    stopRecord();

                    buttonPlay.setEnabled(true);
                }

                MainActivity.this.updateButton();
            }
        });

        buttonPlay.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "voice8K16bitmono.pcm";

                buttonRecord.setEnabled(false);
                buttonPlay.setEnabled(false);

                try {
                  String fullPath = filePath + File.separator + fileName;

                  MainActivity.this.PlayAudioFile(fullPath);
                }
                catch (IOException e) {
                    Log.d("Recorder", e.toString());
                }

                buttonRecord.setEnabled(true);
                buttonPlay.setEnabled(true);
            }
        });
    }

    public void updateButton() {
        if (isRecording)
            buttonRecord.setText("Stop recording");
        else
            buttonRecord.setText("Start recording");
    }

    public void startRecord(String fileName) {
        Toast.makeText(MainActivity.this, "Recording " + fileName, Toast.LENGTH_SHORT).show();

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                   RECORDER_SAMPLERATE,
                                   RECORDER_CHANNELS,
                                   RECORDER_AUDIO_ENCODING,
                                   bufferElementsToRecord * bytesPerElement);

        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    // Convert short to byte
    private byte[] short2byte(short[] audioData) {
        int shortArrsize = audioData.length;

        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (audioData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (audioData[i] >> 8);

            audioData[i] = 0;
        }

        return bytes;
    }

    private void writeAudioDataToFile() {
        String fullPath = filePath + File.separator + fileName;

        short audioData[] = new short[bufferElementsToRecord];

        FileOutputStream outputStream = null;

        try {
          outputStream = new FileOutputStream(fullPath);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // Gets the data from microphone

            recorder.read(audioData, 0, bufferElementsToRecord);

            System.out.println("Short wirting to file" + audioData.toString());

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
          outputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;

            recorder.stop();

            recorder.release();

            recorder = null;

            recordingThread = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void PlayAudioFile(String path) throws IOException
    {
        int intSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                       RECORDER_SAMPLERATE,
                                       AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                                       RECORDER_AUDIO_ENCODING,
                                       intSize,
                                       AudioTrack.MODE_STREAM);

        int count = 512 * 1024; // 512 kb

        byte[] byteData = null;

        File file = null;

        file = new File(path);

        byteData = new byte[(int)count];

        FileInputStream in = null;

        try {
            in = new FileInputStream( file );
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int bytesread = 0, ret = 0;

        int size = (int) file.length();

        audioTrack.play();

        while (bytesread < size) {
            ret = in.read(byteData, 0, count);

            if (ret != -1) { // Write the byte array to the track
                audioTrack.write(byteData, 0, ret);

                bytesread += ret;
            }
            else {
                break;
            }
        }

        in.close();

        audioTrack.stop();

        audioTrack.release();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
          /*
          case R.id.menuSettings:
            Intent intentConfig = new Intent(getApplicationContext(), ConfigActivity.class);

            startActivity(intentConfig);

            return true;
          */

          case R.id.menuAbout:

            showAbout();

            return true;

          default:

            return super.onOptionsItemSelected(item);
        }
    }

    protected void showAbout() {
      View messageView = getLayoutInflater().inflate(R.layout.about_view, null, false);

      AlertDialog.Builder dialog = new AlertDialog.Builder(this);

      dialog.setIcon(R.drawable.ic_launcher);

      dialog.setTitle(R.string.app_name);

      dialog.setView(messageView);

      dialog.create();

      dialog.show();
    }
}