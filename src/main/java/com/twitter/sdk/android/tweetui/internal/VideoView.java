package com.twitter.sdk.android.tweetui.internal;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.twitter.sdk.android.tweetui.internal.VideoControlView.MediaPlayerControl;

public class VideoView extends SurfaceView implements MediaPlayerControl {
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    private String TAG;
    private GestureDetector gestureDetector;
    private int mAudioSession;
    private OnBufferingUpdateListener mBufferingUpdateListener;
    private OnCompletionListener mCompletionListener;
    private int mCurrentBufferPercentage;
    private int mCurrentState;
    private OnErrorListener mErrorListener;
    private OnInfoListener mInfoListener;
    private boolean mLooping;
    private VideoControlView mMediaController;
    private MediaPlayer mMediaPlayer;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    OnPreparedListener mPreparedListener;
    Callback mSHCallback;
    private int mSeekWhenPrepared;
    OnVideoSizeChangedListener mSizeChangedListener;
    private int mSurfaceHeight;
    private SurfaceHolder mSurfaceHolder;
    private int mSurfaceWidth;
    private int mTargetState;
    private Uri mUri;
    private int mVideoHeight;
    private int mVideoWidth;

    public VideoView(Context context) {
        super(context);
        this.TAG = "VideoView";
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mSizeChangedListener = new OnVideoSizeChangedListener() {
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2) {
                VideoView.this.mVideoWidth = mediaPlayer.getVideoWidth();
                VideoView.this.mVideoHeight = mediaPlayer.getVideoHeight();
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    VideoView.this.requestLayout();
                }
            }
        };
        this.mPreparedListener = new OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                VideoView.this.mCurrentState = 2;
                if (VideoView.this.mOnPreparedListener != null) {
                    VideoView.this.mOnPreparedListener.onPrepared(VideoView.this.mMediaPlayer);
                }
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.setEnabled(true);
                }
                VideoView.this.mVideoWidth = mediaPlayer.getVideoWidth();
                VideoView.this.mVideoHeight = mediaPlayer.getVideoHeight();
                int access$600 = VideoView.this.mSeekWhenPrepared;
                if (access$600 != 0) {
                    VideoView.this.seekTo(access$600);
                }
                if (VideoView.this.mVideoWidth != 0 && VideoView.this.mVideoHeight != 0) {
                    VideoView.this.getHolder().setFixedSize(VideoView.this.mVideoWidth, VideoView.this.mVideoHeight);
                    if (VideoView.this.mSurfaceWidth != VideoView.this.mVideoWidth || VideoView.this.mSurfaceHeight != VideoView.this.mVideoHeight) {
                        return;
                    }
                    if (VideoView.this.mTargetState == 3) {
                        VideoView.this.start();
                        if (VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show();
                        }
                    } else if (!VideoView.this.isPlaying()) {
                        if ((access$600 != 0 || VideoView.this.getCurrentPosition() > 0) && VideoView.this.mMediaController != null) {
                            VideoView.this.mMediaController.show();
                        }
                    }
                } else if (VideoView.this.mTargetState == 3) {
                    VideoView.this.start();
                }
            }
        };
        this.mCompletionListener = new OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                VideoView.this.mCurrentState = 5;
                VideoView.this.mTargetState = 5;
                if (VideoView.this.mOnCompletionListener != null) {
                    VideoView.this.mOnCompletionListener.onCompletion(VideoView.this.mMediaPlayer);
                }
            }
        };
        this.mInfoListener = new OnInfoListener() {
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                if (VideoView.this.mOnInfoListener != null) {
                    VideoView.this.mOnInfoListener.onInfo(mediaPlayer, i, i2);
                }
                return true;
            }
        };
        this.mErrorListener = new OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                String access$1200 = VideoView.this.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error: ");
                stringBuilder.append(i);
                stringBuilder.append(",");
                stringBuilder.append(i2);
                Log.d(access$1200, stringBuilder.toString());
                VideoView.this.mCurrentState = -1;
                VideoView.this.mTargetState = -1;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                return (VideoView.this.mOnErrorListener == null || VideoView.this.mOnErrorListener.onError(VideoView.this.mMediaPlayer, i, i2)) ? true : true;
            }
        };
        this.mBufferingUpdateListener = new OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                VideoView.this.mCurrentBufferPercentage = i;
            }
        };
        this.gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if (VideoView.this.isInPlaybackState() && VideoView.this.mMediaController != null) {
                    VideoView.this.toggleMediaControlsVisiblity();
                }
                return false;
            }
        });
        this.mSHCallback = new Callback() {
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                VideoView.this.mSurfaceWidth = i2;
                VideoView.this.mSurfaceHeight = i3;
                Object obj = null;
                Object obj2 = VideoView.this.mTargetState == 3 ? 1 : null;
                if (VideoView.this.mVideoWidth == i2 && VideoView.this.mVideoHeight == i3) {
                    obj = 1;
                }
                if (VideoView.this.mMediaPlayer != null && obj2 != null && obj != null) {
                    if (VideoView.this.mSeekWhenPrepared != 0) {
                        VideoView.this.seekTo(VideoView.this.mSeekWhenPrepared);
                    }
                    VideoView.this.start();
                    if (VideoView.this.mMediaController != null) {
                        VideoView.this.mMediaController.show();
                    }
                }
            }

            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                VideoView.this.mSurfaceHolder = surfaceHolder;
                VideoView.this.openVideo();
            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                VideoView.this.mSurfaceHolder = null;
                if (VideoView.this.mMediaController != null) {
                    VideoView.this.mMediaController.hide();
                }
                VideoView.this.release(true);
            }
        };
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.TAG = "VideoView";
        this.mCurrentState = 0;
        this.mTargetState = 0;
        this.mSurfaceHolder = null;
        this.mMediaPlayer = null;
        this.mSizeChangedListener = /* anonymous class already generated */;
        this.mPreparedListener = /* anonymous class already generated */;
        this.mCompletionListener = /* anonymous class already generated */;
        this.mInfoListener = /* anonymous class already generated */;
        this.mErrorListener = /* anonymous class already generated */;
        this.mBufferingUpdateListener = /* anonymous class already generated */;
        this.gestureDetector = new GestureDetector(getContext(), /* anonymous class already generated */);
        this.mSHCallback = /* anonymous class already generated */;
        initVideoView();
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:22:0x006e, code skipped:
            if (r1 > r6) goto L_0x0094;
     */
    public void onMeasure(int r6, int r7) {
        /*
        r5 = this;
        r0 = r5.mVideoWidth;
        r0 = getDefaultSize(r0, r6);
        r1 = r5.mVideoHeight;
        r1 = getDefaultSize(r1, r7);
        r2 = r5.mVideoWidth;
        if (r2 <= 0) goto L_0x0092;
    L_0x0010:
        r2 = r5.mVideoHeight;
        if (r2 <= 0) goto L_0x0092;
    L_0x0014:
        r0 = android.view.View.MeasureSpec.getMode(r6);
        r6 = android.view.View.MeasureSpec.getSize(r6);
        r1 = android.view.View.MeasureSpec.getMode(r7);
        r7 = android.view.View.MeasureSpec.getSize(r7);
        r2 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r0 != r2) goto L_0x0051;
    L_0x0028:
        if (r1 != r2) goto L_0x0051;
    L_0x002a:
        r0 = r5.mVideoWidth;
        r0 = r0 * r7;
        r1 = r5.mVideoHeight;
        r1 = r1 * r6;
        if (r0 >= r1) goto L_0x003e;
    L_0x0034:
        r6 = r5.mVideoWidth;
        r6 = r6 * r7;
        r0 = r5.mVideoHeight;
        r0 = r6 / r0;
        r6 = r0;
        goto L_0x0094;
    L_0x003e:
        r0 = r5.mVideoWidth;
        r0 = r0 * r7;
        r1 = r5.mVideoHeight;
        r1 = r1 * r6;
        if (r0 <= r1) goto L_0x0094;
    L_0x0048:
        r7 = r5.mVideoHeight;
        r7 = r7 * r6;
        r0 = r5.mVideoWidth;
        r1 = r7 / r0;
        goto L_0x0093;
    L_0x0051:
        r3 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r0 != r2) goto L_0x0063;
    L_0x0055:
        r0 = r5.mVideoHeight;
        r0 = r0 * r6;
        r2 = r5.mVideoWidth;
        r0 = r0 / r2;
        if (r1 != r3) goto L_0x0061;
    L_0x005e:
        if (r0 <= r7) goto L_0x0061;
    L_0x0060:
        goto L_0x0094;
    L_0x0061:
        r7 = r0;
        goto L_0x0094;
    L_0x0063:
        if (r1 != r2) goto L_0x0073;
    L_0x0065:
        r1 = r5.mVideoWidth;
        r1 = r1 * r7;
        r2 = r5.mVideoHeight;
        r1 = r1 / r2;
        if (r0 != r3) goto L_0x0071;
    L_0x006e:
        if (r1 <= r6) goto L_0x0071;
    L_0x0070:
        goto L_0x0094;
    L_0x0071:
        r6 = r1;
        goto L_0x0094;
    L_0x0073:
        r2 = r5.mVideoWidth;
        r4 = r5.mVideoHeight;
        if (r1 != r3) goto L_0x0083;
    L_0x0079:
        if (r4 <= r7) goto L_0x0083;
    L_0x007b:
        r1 = r5.mVideoWidth;
        r1 = r1 * r7;
        r2 = r5.mVideoHeight;
        r1 = r1 / r2;
        goto L_0x0085;
    L_0x0083:
        r1 = r2;
        r7 = r4;
    L_0x0085:
        if (r0 != r3) goto L_0x0071;
    L_0x0087:
        if (r1 <= r6) goto L_0x0071;
    L_0x0089:
        r7 = r5.mVideoHeight;
        r7 = r7 * r6;
        r0 = r5.mVideoWidth;
        r1 = r7 / r0;
        goto L_0x0093;
    L_0x0092:
        r6 = r0;
    L_0x0093:
        r7 = r1;
    L_0x0094:
        r5.setMeasuredDimension(r6, r7);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.twitter.sdk.android.tweetui.internal.VideoView.onMeasure(int, int):void");
    }

    private void initVideoView() {
        this.mVideoWidth = 0;
        this.mVideoHeight = 0;
        getHolder().addCallback(this.mSHCallback);
        getHolder().setType(3);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        requestFocus();
        this.mCurrentState = 0;
        this.mTargetState = 0;
    }

    public void setVideoURI(Uri uri, boolean z) {
        this.mUri = uri;
        this.mLooping = z;
        this.mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
            this.mTargetState = 0;
        }
    }

    private void openVideo() {
        if (this.mUri != null && this.mSurfaceHolder != null) {
            release(false);
            try {
                this.mMediaPlayer = new MediaPlayer();
                if (this.mAudioSession != 0) {
                    this.mMediaPlayer.setAudioSessionId(this.mAudioSession);
                } else {
                    this.mAudioSession = this.mMediaPlayer.getAudioSessionId();
                }
                this.mMediaPlayer.setOnPreparedListener(this.mPreparedListener);
                this.mMediaPlayer.setOnVideoSizeChangedListener(this.mSizeChangedListener);
                this.mMediaPlayer.setOnCompletionListener(this.mCompletionListener);
                this.mMediaPlayer.setOnErrorListener(this.mErrorListener);
                this.mMediaPlayer.setOnInfoListener(this.mInfoListener);
                this.mMediaPlayer.setOnBufferingUpdateListener(this.mBufferingUpdateListener);
                this.mCurrentBufferPercentage = 0;
                this.mMediaPlayer.setLooping(this.mLooping);
                this.mMediaPlayer.setDataSource(getContext(), this.mUri);
                this.mMediaPlayer.setDisplay(this.mSurfaceHolder);
                this.mMediaPlayer.setAudioStreamType(3);
                this.mMediaPlayer.setScreenOnWhilePlaying(true);
                this.mMediaPlayer.prepareAsync();
                this.mCurrentState = 1;
                attachMediaController();
            } catch (Exception e) {
                String str = this.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to open content: ");
                stringBuilder.append(this.mUri);
                Log.w(str, stringBuilder.toString(), e);
                this.mCurrentState = -1;
                this.mTargetState = -1;
                this.mErrorListener.onError(this.mMediaPlayer, 1, 0);
            }
        }
    }

    public void setMediaController(VideoControlView videoControlView) {
        if (this.mMediaController != null) {
            this.mMediaController.hide();
        }
        this.mMediaController = videoControlView;
        attachMediaController();
    }

    private void attachMediaController() {
        if (this.mMediaPlayer != null && this.mMediaController != null) {
            this.mMediaController.setMediaPlayer(this);
            this.mMediaController.setEnabled(isInPlaybackState());
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.gestureDetector.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.mOnErrorListener = onErrorListener;
    }

    public void setOnInfoListener(OnInfoListener onInfoListener) {
        this.mOnInfoListener = onInfoListener;
    }

    private void release(boolean z) {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mCurrentState = 0;
            if (z) {
                this.mTargetState = 0;
            }
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        Object obj = (i == 4 || i == 24 || i == 25 || i == 82 || i == 5 || i == 6) ? null : 1;
        if (!(!isInPlaybackState() || obj == null || this.mMediaController == null)) {
            if (i == 79 || i == 85) {
                if (this.mMediaPlayer.isPlaying()) {
                    pause();
                    this.mMediaController.show();
                } else {
                    start();
                    this.mMediaController.hide();
                }
                return true;
            } else if (i == 126) {
                if (!this.mMediaPlayer.isPlaying()) {
                    start();
                    this.mMediaController.hide();
                }
                return true;
            } else if (i == 86 || i == 127) {
                if (this.mMediaPlayer.isPlaying()) {
                    pause();
                    this.mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    private void toggleMediaControlsVisiblity() {
        if (this.mMediaController.isShowing()) {
            this.mMediaController.hide();
        } else {
            this.mMediaController.show();
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            this.mMediaPlayer.start();
            this.mCurrentState = 3;
        }
        this.mTargetState = 3;
    }

    public void pause() {
        if (isInPlaybackState() && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            this.mCurrentState = 4;
        }
        this.mTargetState = 4;
    }

    public int getDuration() {
        return isInPlaybackState() ? this.mMediaPlayer.getDuration() : -1;
    }

    public int getCurrentPosition() {
        return isInPlaybackState() ? this.mMediaPlayer.getCurrentPosition() : 0;
    }

    public void seekTo(int i) {
        if (isInPlaybackState()) {
            this.mMediaPlayer.seekTo(i);
            this.mSeekWhenPrepared = 0;
            return;
        }
        this.mSeekWhenPrepared = i;
    }

    public boolean isPlaying() {
        return isInPlaybackState() && this.mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return this.mMediaPlayer != null ? this.mCurrentBufferPercentage : 0;
    }

    private boolean isInPlaybackState() {
        return (this.mMediaPlayer == null || this.mCurrentState == -1 || this.mCurrentState == 0 || this.mCurrentState == 1) ? false : true;
    }
}
