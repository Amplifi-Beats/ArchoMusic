package com.gianxd.libaudiodev.media;

import android.media.MediaPlayer;
import android.util.Log;

public class AudioSession {
    private MediaPlayer audioSession;
    private boolean sessionInitialized = false;
    private int sessionInt = 0;

    /* AudioSession class is used for creating sessions for Audio files */
    public AudioSession() {
        if (!sessionInitialized) {
            this.sessionInitialized = true;
            Log.e("A", "Archolizer: AudioSession class was initialized.");
        } else {
            throw new RuntimeException("AudioSession: Session already initialized.");
        }
    }

    public void createSession() {
        if (sessionInitialized) {
            this.audioSession = new MediaPlayer();
            this.sessionInt = this.sessionInt + 1;
        } else {
            throw new RuntimeException("AudioSession: Session not initialized.");
        }
    }

    public void createSession(MediaPlayer mediaPlayer) {
        if (sessionInitialized) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            this.audioSession = mediaPlayer;
            this.sessionInt = this.sessionInt + 1;
        } else {
            throw new RuntimeException("AudioSession: Session not initialized");
        }
    }

    public MediaPlayer getAudioSession(int session) {
        if (sessionInitialized) {
            if (sessionInt > 0) {

            }
        }
        return null;
    }

}
