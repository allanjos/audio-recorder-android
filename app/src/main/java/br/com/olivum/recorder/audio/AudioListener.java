package br.com.olivum.recorder.audio;

/**
 * Created by allann on 8/8/17.
 */

public interface AudioListener {
    void onPlayStart();

    void onPlayStop();

    void onRecordStart();

    void onRecordStop();
}
