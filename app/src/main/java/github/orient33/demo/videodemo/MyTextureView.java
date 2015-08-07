package github.orient33.demo.videodemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.Map;

public class MyTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "MyTextureView";
    Uri mUri;
    Surface mSurface;
    MediaPlayer mMediaPlayer;
    int mAudioSession;

    int mVideoWidth, mVideoHeight;
    public MyTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setSurfaceTextureListener(this);
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
        i("onMeasure() width=" + width + ", height=" + height);
        setMeasuredDimension(width, height);
    }
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        i("onSurface..Avilable() w="+width+", height="+height);
        mSurface = new Surface(surface);
//        mSurface.
        openVideo();
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        i("onSurface..SizeChanged() w=" + width + ", height=" + height);
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        i("onSurface..Destroyed()");
        release(true);
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
//        mHeaders = headers;
//        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    void openVideo() {
        if (mUri == null || mSurface == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
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
//            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
//            mMediaPlayer.setOnCompletionListener(mCompletionListener);
//            mMediaPlayer.setOnErrorListener(mErrorListener);
//            mMediaPlayer.setOnInfoListener(mInfoListener);
//            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
//            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(getContext(), mUri, null);//mHeaders);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
//            mCurrentState = STATE_PREPARING;
        } catch (IOException | IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
//            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }
    void release(boolean clear) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
//            mPendingSubtitleTracks.clear();
//            mCurrentState = STATE_IDLE;
//            if (clear) {
//                mTargetState  = STATE_IDLE;
//            }
        }
    }
    void stopPlayback(){
        if(mMediaPlayer!=null)
            mMediaPlayer.stop();
        release(false);
    }
    public void seekTo(int msec) {
        if (mMediaPlayer!=null) {
            mMediaPlayer.seekTo(msec);
        }
    }
    public int getmVideoWidth(){
        return mMediaPlayer!=null?mMediaPlayer.getVideoWidth():400;
    }
    public int getmVideoHeight(){
        return mMediaPlayer!=null?mMediaPlayer.getVideoHeight():300;
    }
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            i("onPrepared() vW="+mVideoHeight+", vH="+mVideoHeight);
            invalidate();
            mp.start();
        }
    };
    public int getCurrentPosition(){
        if(mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        return 0;
    }

    void i(String i){
        Log.i(TAG,i);
    }
}
