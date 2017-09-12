package br.com.olivum.recorder.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.olivum.recorder.R;
import br.com.olivum.recorder.adapter.NoteListAdapter;
import br.com.olivum.recorder.audio.AudioNote;
import br.com.olivum.recorder.note.Note;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private NoteListAdapter adapterNoteList = null;
    private ListView listViewNotes = null;

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                Intent intent = new Intent(MainActivity.this, AudioNoteNewActivity.class);

                startActivityForResult(intent, 1);
            }
        });

        // Drawer

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(toggle);

        toggle.syncState();

        // Navigation view

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        // Configure list

        listViewNotes = (ListView) findViewById(R.id.list_view_notes);

        listViewNotes.setAdapter(adapterNoteList);

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick()");

                Note item = (Note) listViewNotes.getAdapter().getItem(i);

                Log.d(TAG, "item: " + item.getText());

                Intent intent = new Intent(MainActivity.this, AudioNoteViewActivity.class);

                intent.putExtra("filename", item.getFileName());

                startActivityForResult(intent, 3);
            }
        });

        checkPermission();
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

        if (hasPermission) {
            loadList();

            return true;
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    REQUEST_WRITE_STORAGE);

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadList();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /* if (id == R.id.action_audio_note_new) {
            return true;
        } */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_audio_note) {
            Intent intent = new Intent(this, AudioNoteNewActivity.class);

            startActivityForResult(intent, 1);
        }
        else if (id == R.id.nav_video_note) {

        }
        else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);

            startActivityForResult(intent, 2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void loadList() {
        Log.d(TAG, "loadList()");

        String path = AudioNote.getAudioNotesDirPath();

        Log.d("Files", "Path: " + path);

        File directory = new File(path);

        if (!directory.exists()) {
            Log.d(TAG, "Directory doesn't exist: " + path);

            return;
        }

        if (!directory.isDirectory()) {
            Log.e(TAG, "Path is not a directory: " + path);

            return;
        }

        Log.d(TAG, "Directory: " + AudioNote.getAudioNotesDirPath());

        File[] files = directory.listFiles();

        if (files != null) {
            Log.d(TAG, "Files count: " + files.length);

            List<Note> notesList = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                Log.d(TAG, "File: " + files[i].getName());

                Note item = new Note(files[i].getName());

                item.setFileName(files[i].getName());

                notesList.add(item);
            }

            if (adapterNoteList != null) {
                adapterNoteList = null;
            }

            adapterNoteList = new NoteListAdapter(this, R.layout.note_list_item_adapter, notesList);

            listViewNotes.setAdapter(adapterNoteList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");

        switch (requestCode) {
            case 1:
            case 3:
                Log.d(TAG, "Activity view returned");

                loadList();

                break;
        }
    }

    /*
    protected void showAbout() {
      View messageView = getLayoutInflater().inflate(R.layout.about_view, null, false);

      AlertDialog.Builder dialog = new AlertDialog.Builder(this);

      dialog.setIcon(R.drawable.ic_launcher);

      dialog.setTitle(R.string.app_name);

      dialog.setView(messageView);

      dialog.create();

      dialog.show();
    }
    */
}
