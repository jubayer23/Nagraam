package com.fli_alpha_solutions_ltd.word_unlimited.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.MediaController;
import android.widget.Toast;

//---------------------------------------------------------------------------------------
public class MusicService extends Service implements OnErrorListener,

// Interface used by the visual representation of the player controls.
        MediaController.MediaPlayerControl,

        // implemented to possibly upgrade the media player interface.
        MediaPlayer.OnBufferingUpdateListener

{
    // Connect service who to call onBind
    private final IBinder mBinder = new ServiceBinder();

    // Instance of media player
    MediaPlayer mPlayer,mPlayer2;

    // Saves the data buffer of the media player (used by MediaController).
    private int mBuffer = 0;

    String music_name = "";

    public MusicService() {
    }

    // Called by the interface ServiceConnected when calling the service
    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent context) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // create();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        if (mPlayer2 != null) {
            try {
                mPlayer2.stop();
                mPlayer2.release();
            } finally {
                mPlayer2 = null;
            }
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "music player failed", Toast.LENGTH_SHORT).show();

        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        return false;
    }

    public void create() {
        mPlayer = MediaPlayer.create(
                this,
                this.getResources().getIdentifier(music_name, "raw",
                        getPackageName()));
        mPlayer.setOnErrorListener(this);
        mPlayer.setLooping(true);

        mPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onError(mPlayer, what, extra);
                return true;
            }
        });
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return mBuffer;
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        if (mPlayer != null && isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    public void seekTo(int pos) {
        mPlayer.seekTo(pos);
    }

    public void resume() {
        if (isPlaying() == false) {
            seekTo(getCurrentPosition());
            start();
        }
    }

    public void start(String music_name,boolean looping) {
        // Log.d("debug", "it's run");
        if (mPlayer == null) {
            mPlayer = MediaPlayer.create(this, this.getResources()
                    .getIdentifier(music_name, "raw", getPackageName()));
            mPlayer.setOnErrorListener(this);
            mPlayer.setLooping(looping);

            mPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onError(mPlayer, what, extra);
                    return true;
                }
            });
        } else {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;

            mPlayer = MediaPlayer.create(this, this.getResources()
                    .getIdentifier(music_name, "raw", getPackageName()));
            mPlayer.setOnErrorListener(this);
            mPlayer.setLooping(looping);

            mPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onError(mPlayer, what, extra);
                    return true;
                }
            });
        }
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                mPlayer.start();
            }
        });
        th.start();
    }

    public void start2(String music_name,boolean looping) {
        // Log.d("debug", "it's run");
        if (mPlayer2 == null) {
            mPlayer2 = MediaPlayer.create(this, this.getResources()
                    .getIdentifier(music_name, "raw", getPackageName()));

            mPlayer2.setLooping(looping);

            mPlayer2.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onError(mPlayer2, what, extra);
                    return true;
                }
            });
        } else {
            mPlayer2.stop();
            mPlayer2.release();
            mPlayer2 = null;

            mPlayer2 = MediaPlayer.create(this, this.getResources()
                    .getIdentifier(music_name, "raw", getPackageName()));

            mPlayer2.setLooping(looping);

            mPlayer2.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onError(mPlayer2, what, extra);
                    return true;
                }
            });
        }
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                mPlayer2.start();
            }
        });
        th.start();
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    public void stop2() {
        if (mPlayer2 != null) {
            mPlayer2.stop();
            mPlayer2.release();
            mPlayer2 = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mBuffer = percent;

    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }
}