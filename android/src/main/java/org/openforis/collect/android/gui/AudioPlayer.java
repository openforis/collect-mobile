package org.openforis.collect.android.gui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.Views;

import java.io.File;

public class AudioPlayer extends RelativeLayout {

    private static final int PLAYBACK_SEEK_BAR_UPDATE_DELAY = 50;
    private static final String DEFAULT_PROGRESS_TEXT = "00:00";

    private View view;
    private File file;
    private MediaPlayer mediaPlayer;
    private Button playBtn, pauseBtn;
    private SeekBar seekBar;
    private TextView progressTextView;

    private boolean prepared = false;
    private boolean playing = false;

    private Handler playbackProgressHandler = new Handler();
    private Runnable playbackProgressUpdater = new Runnable() {
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            updateProgressText();
            playbackProgressHandler.postDelayed(this, PLAYBACK_SEEK_BAR_UPDATE_DELAY);
        }
    };

    public AudioPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        view = inflate(context, R.layout.audio_player, this);

        setupPlayButton();
        setupPausePlaybackButton();
        setupSeekBar();

        this.progressTextView = view.findViewById(R.id.playback_progress_text);
    }

    private static String toTime(int millis) {
        int m = millis / 1000 / 60;
        int s = (millis / 1000) % 60;
        return StringUtils.leftPad(String.valueOf(m), 2, '0')
                + ":"
                + StringUtils.leftPad(String.valueOf(s), 2, '0');
    }

    public boolean isPlaying() {
        return playing;
    }

    public void destroy() {
        if (mediaPlayer != null) {
            destroyMediaPlayer();
        }
    }

    private void destroyMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        prepared = false;
    }

    private void setupPlayButton() {
        playBtn = view.findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                start();
            }
        });
    }

    private void setupPausePlaybackButton() {
        pauseBtn = view.findViewById(R.id.pause_playback_btn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                pause();
            }
        });
    }

    private void setupSeekBar() {
        seekBar = view.findViewById(R.id.playback_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void setSource(File file) {
        stop();
        this.file = file;
        prepare();
    }

    public void prepare() {
        prepared = false;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stop();
                }
            });
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            prepared = true;
        } catch (Exception e) {
        }
    }

    public void start() {
        if (!prepared) {
            prepare();
        }
        if (!prepared) {
            return;
        }
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
        playbackProgressHandler.postDelayed(playbackProgressUpdater, 0);

        playing = true;

        Activities.keepScreenOn(getContext());

        updateViewState();
    }

    public void stop() {
        if (mediaPlayer != null) {
            playbackProgressHandler.removeCallbacks(playbackProgressUpdater);
            seekBar.setMax(0);
            seekBar.setProgress(0);
            progressTextView.setText(DEFAULT_PROGRESS_TEXT);
            destroyMediaPlayer();
        }
        playing = false;

        Activities.clearKeepScreenOn(getContext());

        updateViewState();
    }

    public void pause() {
        if (playing) {
            playbackProgressHandler.removeCallbacks(playbackProgressUpdater);
            mediaPlayer.pause();
            playing = false;
            updateViewState();
        }
    }

    private void updateViewState() {
        Views.toggleVisibility(playBtn, !playing);
        Views.toggleVisibility(pauseBtn, playing);
    }

    private void updateProgressText() {
        String text = mediaPlayer == null ? DEFAULT_PROGRESS_TEXT :
                toTime(mediaPlayer.getCurrentPosition()) + " / " + toTime(mediaPlayer.getDuration());
        progressTextView.setText(text);
    }
}
