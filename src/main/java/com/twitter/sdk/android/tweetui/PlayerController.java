package com.twitter.sdk.android.tweetui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.twitter.sdk.android.core.IntentUtils;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.tweetui.PlayerActivity.PlayerItem;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener.Callback;
import com.twitter.sdk.android.tweetui.internal.VideoControlView;
import com.twitter.sdk.android.tweetui.internal.VideoView;

class PlayerController {
    private static final String TAG = "PlayerController";
    final TextView callToActionView;
    final Callback callback;
    boolean isPlaying = true;
    final View rootView;
    int seekPosition;
    final VideoControlView videoControlView;
    final ProgressBar videoProgressView;
    final VideoView videoView;

    PlayerController(View view, Callback callback) {
        this.rootView = view;
        this.videoView = (VideoView) view.findViewById(R.id.video_view);
        this.videoControlView = (VideoControlView) view.findViewById(R.id.video_control_view);
        this.videoProgressView = (ProgressBar) view.findViewById(R.id.video_progress_view);
        this.callToActionView = (TextView) view.findViewById(R.id.call_to_action_view);
        this.callback = callback;
    }

    PlayerController(View view, VideoView videoView, VideoControlView videoControlView, ProgressBar progressBar, TextView textView, Callback callback) {
        this.rootView = view;
        this.videoView = videoView;
        this.videoControlView = videoControlView;
        this.videoProgressView = progressBar;
        this.callToActionView = textView;
        this.callback = callback;
    }

    /* Access modifiers changed, original: 0000 */
    public void prepare(PlayerItem playerItem) {
        try {
            setUpCallToAction(playerItem);
            setUpMediaControl(playerItem.looping, playerItem.showVideoControls);
            this.videoView.setOnTouchListener(SwipeToDismissTouchListener.createFromView(this.videoView, this.callback));
            this.videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    PlayerController.this.videoProgressView.setVisibility(8);
                }
            });
            this.videoView.setOnInfoListener(new OnInfoListener() {
                public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                    if (i == 702) {
                        PlayerController.this.videoProgressView.setVisibility(8);
                        return true;
                    } else if (i != 701) {
                        return false;
                    } else {
                        PlayerController.this.videoProgressView.setVisibility(0);
                        return true;
                    }
                }
            });
            this.videoView.setVideoURI(Uri.parse(playerItem.url), playerItem.looping);
            this.videoView.requestFocus();
        } catch (Exception e) {
            Twitter.getLogger().e(TAG, "Error occurred during video playback", e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onResume() {
        if (this.seekPosition != 0) {
            this.videoView.seekTo(this.seekPosition);
        }
        if (this.isPlaying) {
            this.videoView.start();
            this.videoControlView.update();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onPause() {
        this.isPlaying = this.videoView.isPlaying();
        this.seekPosition = this.videoView.getCurrentPosition();
        this.videoView.pause();
    }

    /* Access modifiers changed, original: 0000 */
    public void onDestroy() {
        this.videoView.stopPlayback();
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpMediaControl(boolean z, boolean z2) {
        if (!z || z2) {
            setUpMediaControl();
        } else {
            setUpLoopControl();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpLoopControl() {
        this.videoControlView.setVisibility(4);
        this.videoView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PlayerController.this.videoView.isPlaying()) {
                    PlayerController.this.videoView.pause();
                } else {
                    PlayerController.this.videoView.start();
                }
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpMediaControl() {
        this.videoView.setMediaController(this.videoControlView);
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpCallToAction(PlayerItem playerItem) {
        if (playerItem.callToActionText != null && playerItem.callToActionUrl != null) {
            this.callToActionView.setVisibility(0);
            this.callToActionView.setText(playerItem.callToActionText);
            setUpCallToActionListener(playerItem.callToActionUrl);
            setUpRootViewOnClickListener();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpCallToActionListener(final String str) {
        this.callToActionView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                IntentUtils.safeStartActivity(PlayerController.this.callToActionView.getContext(), new Intent("android.intent.action.VIEW", Uri.parse(str)));
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    public void setUpRootViewOnClickListener() {
        this.rootView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PlayerController.this.callToActionView.getVisibility() == 0) {
                    PlayerController.this.callToActionView.setVisibility(8);
                } else {
                    PlayerController.this.callToActionView.setVisibility(0);
                }
            }
        });
    }
}
