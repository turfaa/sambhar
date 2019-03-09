package com.twitter.sdk.android.tweetui.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.twitter.sdk.android.tweetui.R;

public class VideoControlView extends FrameLayout {
    static final int FADE_DURATION_MS = 150;
    static final long PROGRESS_BAR_TICKS = 1000;
    private static final int SHOW_PROGRESS_MSG = 1001;
    TextView currentTime;
    TextView duration;
    @SuppressLint({"HandlerLeak"})
    private final Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1001 && VideoControlView.this.player != null) {
                VideoControlView.this.updateProgress();
                VideoControlView.this.updateStateControl();
                if (VideoControlView.this.isShowing() && VideoControlView.this.player.isPlaying()) {
                    sendMessageDelayed(obtainMessage(1001), 500);
                }
            }
        }
    };
    MediaPlayerControl player;
    SeekBar seekBar;
    ImageButton stateControl;

    public interface MediaPlayerControl {
        int getBufferPercentage();

        int getCurrentPosition();

        int getDuration();

        boolean isPlaying();

        void pause();

        void seekTo(int i);

        void start();
    }

    public VideoControlView(Context context) {
        super(context);
    }

    public VideoControlView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public VideoControlView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setMediaPlayer(MediaPlayerControl mediaPlayerControl) {
        this.player = mediaPlayerControl;
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        initSubviews();
    }

    /* Access modifiers changed, original: 0000 */
    public void initSubviews() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.tw__video_control, this);
        this.stateControl = (ImageButton) findViewById(R.id.tw__state_control);
        this.currentTime = (TextView) findViewById(R.id.tw__current_time);
        this.duration = (TextView) findViewById(R.id.tw__duration);
        this.seekBar = (SeekBar) findViewById(R.id.tw__progress);
        this.seekBar.setMax(1000);
        this.seekBar.setOnSeekBarChangeListener(createProgressChangeListener());
        this.stateControl.setOnClickListener(createStateControlClickListener());
        setDuration(0);
        setCurrentTime(0);
        setProgress(0, 0, 0);
    }

    /* Access modifiers changed, original: 0000 */
    public OnClickListener createStateControlClickListener() {
        return new OnClickListener() {
            public void onClick(View view) {
                if (VideoControlView.this.player.isPlaying()) {
                    VideoControlView.this.player.pause();
                } else {
                    VideoControlView.this.player.start();
                }
                VideoControlView.this.show();
            }
        };
    }

    /* Access modifiers changed, original: 0000 */
    public OnSeekBarChangeListener createProgressChangeListener() {
        return new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    int duration = (int) (((long) (VideoControlView.this.player.getDuration() * i)) / VideoControlView.PROGRESS_BAR_TICKS);
                    VideoControlView.this.player.seekTo(duration);
                    VideoControlView.this.setCurrentTime(duration);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                VideoControlView.this.handler.removeMessages(1001);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                VideoControlView.this.handler.sendEmptyMessage(1001);
            }
        };
    }

    /* Access modifiers changed, original: 0000 */
    public void updateProgress() {
        int duration = this.player.getDuration();
        int currentPosition = this.player.getCurrentPosition();
        int bufferPercentage = this.player.getBufferPercentage();
        setDuration(duration);
        setCurrentTime(currentPosition);
        setProgress(currentPosition, duration, bufferPercentage);
    }

    /* Access modifiers changed, original: 0000 */
    public void setDuration(int i) {
        this.duration.setText(MediaTimeUtils.getPlaybackTime((long) i));
    }

    /* Access modifiers changed, original: 0000 */
    public void setCurrentTime(int i) {
        this.currentTime.setText(MediaTimeUtils.getPlaybackTime((long) i));
    }

    /* Access modifiers changed, original: 0000 */
    public void setProgress(int i, int i2, int i3) {
        this.seekBar.setProgress((int) (i2 > 0 ? (((long) i) * PROGRESS_BAR_TICKS) / ((long) i2) : 0));
        this.seekBar.setSecondaryProgress(i3 * 10);
    }

    /* Access modifiers changed, original: 0000 */
    public void updateStateControl() {
        if (this.player.isPlaying()) {
            setPauseDrawable();
        } else if (this.player.getCurrentPosition() > Math.max(this.player.getDuration() - 500, 0)) {
            setReplayDrawable();
        } else {
            setPlayDrawable();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setPlayDrawable() {
        this.stateControl.setImageResource(R.drawable.tw__video_play_btn);
        this.stateControl.setContentDescription(getContext().getString(R.string.tw__play));
    }

    /* Access modifiers changed, original: 0000 */
    public void setPauseDrawable() {
        this.stateControl.setImageResource(R.drawable.tw__video_pause_btn);
        this.stateControl.setContentDescription(getContext().getString(R.string.tw__pause));
    }

    /* Access modifiers changed, original: 0000 */
    public void setReplayDrawable() {
        this.stateControl.setImageResource(R.drawable.tw__video_replay_btn);
        this.stateControl.setContentDescription(getContext().getString(R.string.tw__replay));
    }

    /* Access modifiers changed, original: 0000 */
    public void hide() {
        this.handler.removeMessages(1001);
        AnimationUtils.fadeOut(this, FADE_DURATION_MS);
    }

    /* Access modifiers changed, original: 0000 */
    public void show() {
        this.handler.sendEmptyMessage(1001);
        AnimationUtils.fadeIn(this, FADE_DURATION_MS);
    }

    public boolean isShowing() {
        return getVisibility() == 0;
    }

    public void update() {
        this.handler.sendEmptyMessage(1001);
    }
}
