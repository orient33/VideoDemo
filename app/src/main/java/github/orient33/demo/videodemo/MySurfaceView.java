package github.orient33.demo.videodemo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;

/**
 * play video using SurfaceView
 */
public class MySurfaceView extends SurfaceView{//} implements MediaController.MediaPlayerControl {
    private static final String TAG = "MySurfaceView";
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPENDED = 6;
    private int mCurrentState, mTargetState;
    int mVideoWidth, mVideoHeight;
    int mSurfaceWidth, mSurfaceHeight;

    private int mAudioSession;
    MediaPlayer mMediaPlayer;
    Uri mUri;
    int mSeekWhenPrepared = 0;
    SurfaceHolder mSurfaceHolder;
    final Context mContext;
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            release(true);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
        }
    };

    MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    private final void init() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
//        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
//        mPendingSubtitleTracks = new Vector<Pair<InputStream, MediaFormat>>();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
//        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mContext, mUri, null);//mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
        } catch (IOException|IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    private boolean isHTTPStreaming(Uri mUri) {
        if (mUri != null) {
            String scheme = mUri.getScheme();
            if ((scheme != null) && (scheme.equals("http") || scheme.equals("https"))) {
                String path = mUri.getPath();
                if (path == null || path.endsWith(".m3u8") || path.endsWith(".m3u")
                        || path.endsWith(".mpd")) {
                    // HLS or DASH streaming source
                    return false;
                }
                // HTTP progressive download streaming source
                return true;
            }
        }
        return false;
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                requestLayout();
            }
        }
    };
    protected MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            /// M: work around for ALPS01306192 which is caused by operations
            // in the same main thread strangely @{
            try {
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();

                int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
                if (seekToPosition != 0) {
                    mMediaPlayer.seekTo(seekToPosition);
                }
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (mTargetState == STATE_PLAYING) {
                            start();
                        }
                    }
                } else {
                    // We don't know the video size yet, but should start anyway.
                    // The video size might be reported to us later.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "onPrepared IllegalStateException");
                e.printStackTrace();
            }
            /// @}
        }
    };
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, arg1, arg2);
            }
            return true;
        }
    };
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }

            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l)
    {
        mOnCompletionListener = l;
    }
    public void setOnErrorListener(MediaPlayer.OnErrorListener l)
    {
        mOnErrorListener = l;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    protected MediaPlayer.OnCompletionListener mOnCompletionListener;
    protected MediaPlayer.OnPreparedListener mOnPreparedListener;
    protected int mCurrentBufferPercentage;
    protected MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    void release(boolean clear) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
//            mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (clear) {
                mTargetState  = STATE_IDLE;
            }
        }
    }
//    @Override
    public void start() {
        if (isInPlaybackState()) {
            Log.i(TAG, "Playback Start begin");
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            Log.i(TAG, "Playback Start end");
        }
        mTargetState = STATE_PLAYING;
    }

//    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        // HTTP streaming will call MediaPlayer::suspend() for suspend operation, others will call release()
        release(false);
    }

    public void resume() {
        // HTTP streaming (with suspended status) will call MediaPlayer::resume(), others will call openVideo()
        if (mCurrentState == STATE_SUSPENDED && mMediaPlayer != null) {
            if (mSurfaceHolder != null) {
                release(false);
            } else {
                // the surface has been destroyed, resume() will be called after surface created
                return;
            }
        }
        openVideo();
    }

//    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

//    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

//    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

//    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

//    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING &&
                mCurrentState != STATE_SUSPENDED);
    }

//    @Override
//    public boolean canPause() {
//        return mCanPause;
//    }
//
//    @Override
//    public boolean canSeekBackward() {
//        return mCanSeekBack;
//    }
//
//    @Override
//    public boolean canSeekForward() {
//        return mCanSeekForward;
//    }

//    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

}
