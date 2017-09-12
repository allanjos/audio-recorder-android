package br.com.olivum.recorder.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import br.com.olivum.recorder.R;
import br.com.olivum.recorder.audio.AudioListener;
import br.com.olivum.recorder.audio.AudioNote;

public class AudioNoteNewActivity extends AppCompatActivity implements AudioListener {
    private static final String TAG = "AudioNoteNewActivity";
    private Button buttonRecord;
    private Button buttonPlay;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_note);

        // Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Get current configuration

        //configuration = Configuration.getInstance();

        // Action bar

        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.Audio_note);

            actionBar.setDisplayShowTitleEnabled(true);

            actionBar.setDisplayShowHomeEnabled(true);

            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        }

        //int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        // EditText - file name

        final EditText editTextBrief = (EditText) findViewById(R.id.editTextBrief);

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
                if (!AudioNote.isRecording()) {
                    Log.d(TAG, "AudioNode is not recording, starting to record...");

                    AudioNote.startRecord(AudioNoteNewActivity.this.getApplicationContext());
                }
                else {
                    Log.d(TAG, "AudioNode is recording, stopping to record...");

                    // Stop record

                    AudioNote.stopRecord();
                }
            }
        });

        buttonPlay.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (!AudioNote.isPlaying()) {
                    Log.d(TAG, "Audio is not playing");

                    AudioNote.playAudioFile();
                }
                else {
                    AudioNote.stopPlayer();
                }
            }
        });

        AudioNote.registerListener(this);
    }

    public void updateButton() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:

                finish();

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPlayStart() {
        Log.d(TAG, "onPlayStart()");

        buttonRecord.setEnabled(false);

        buttonPlay.setText("STOP");
    }

    @Override
    public void onPlayStop() {
        Log.d(TAG, "onPlayStop()");

        buttonRecord.setEnabled(true);

        buttonPlay.setText("PLAY");
    }

    @Override
    public void onRecordStart() {
        Log.d(TAG, "onRecordStart()");

        buttonPlay.setEnabled(false);

        buttonRecord.setText("STOP");
    }

    @Override
    public void onRecordStop() {
        Log.d(TAG, "onRecordStop()");

        buttonPlay.setEnabled(true);

        buttonRecord.setText("RECORD");
    }

    public void stop() {
        AudioNote.stopPlayer();

        AudioNote.stopRecord();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_audio_note_new, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stop();
    }
}