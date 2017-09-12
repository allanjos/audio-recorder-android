package br.com.olivum.recorder.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import br.com.olivum.recorder.R;
import br.com.olivum.recorder.audio.AudioListener;
import br.com.olivum.recorder.audio.AudioNote;
import br.com.olivum.recorder.note.Note;

public class AudioNoteViewActivity extends AppCompatActivity implements AudioListener
{
    private static final String TAG = "AudioNoteViewActivity";

    private Button buttonPlay;

    private String fileName = "";

    private Note note = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_note_view);

        Intent intent = getIntent();

        this.fileName = intent.getStringExtra("filename");

        Log.d(TAG, "File name: " + fileName);

        note = new Note(fileName);

        // Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Get current configuration

        //configuration = Configuration.getInstance();

        // Action bar

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(R.string.Audio_note);

            actionBar.setDisplayShowTitleEnabled(true);

            actionBar.setDisplayShowHomeEnabled(true);

            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        }

        // Image view - Note type

        View viewType = findViewById(R.id.view_note_type);

        viewType.setBackgroundResource(R.drawable.ic_mic_black_24px);

        /*
        TextView textViewName = (TextView) findViewById(R.id.text_view_name);

        String text;

        if (note.getType() == Note.TYPE_AUDIO) {
            text = "Audio note";
        }
        else {
            text = "Note";
        }

        textViewName.setText(text);
        */

        TextView textViewDate = (TextView) findViewById(R.id.text_view_date);

        textViewDate.setText(note.getDate());

        TextView textViewFileName = (TextView) findViewById(R.id.text_view_filename);

        textViewFileName.setText(note.getFileName());

        // EditText - file name

        final EditText editTextBrief = (EditText) findViewById(R.id.editTextBrief);

        // Button - Play recorded file

        buttonPlay = (Button) findViewById(R.id.buttonPlay);

        // Update UI

        updateButton();

        buttonPlay.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (!AudioNote.isPlaying()) {
                    AudioNote.playAudioFile(fileName);
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

    public void stop() {
        AudioNote.stopPlayer();

        AudioNote.stopRecord();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                stop();

                finish();

                return true;

            case R.id.action_delete:

                stop();

                if (Note.deleteFile(fileName)) {
                    finish();
                }

            default:

                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPlayStart() {
        Log.d(TAG, "onPlayStart()");

        buttonPlay.setText("STOP");
    }

    @Override
    public void onPlayStop() {
        Log.d(TAG, "onPlayStop()");

        buttonPlay.setText("PLAY");
    }

    @Override
    public void onRecordStart() {
        Log.d(TAG, "onRecordStart()");
    }

    @Override
    public void onRecordStop() {
        Log.d(TAG, "onRecordStop()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_audio_note, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stop();
    }
}
