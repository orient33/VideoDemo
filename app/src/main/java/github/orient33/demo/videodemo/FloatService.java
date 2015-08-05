package github.orient33.demo.videodemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.VideoView;

public class FloatService extends Service {
    /**
     * Video's uri
     */
    public static final String KEY_VIDEO_URI = "uri";
    /**
     * the position to seek to
     */
    public static final String KEY_POSITION = "position";
    WindowManager mWindowManager;
    ViewGroup mFloatView;
    VideoView mVideoView;
    WindowManager.LayoutParams mParams;
    boolean mFloating;

    public FloatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mWindowManager == null) {
            mFloatView = (ViewGroup) View.inflate(this, R.layout.video_view_float, null);
            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mVideoView = (VideoView) mFloatView.findViewById(R.id.vv);
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            mParams.width = 600;
            mParams.height = 400;
            mParams.x = 10;
            mParams.y = 50;
            mFloatView.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSelf();
                }
            });
            mFloatView.setOnTouchListener(touchListener);
        }
        if (!mFloating) {
            String uri = intent.getStringExtra(KEY_VIDEO_URI);
            int position = intent.getIntExtra(KEY_POSITION, 0);
            addView(uri, position);
        } else {
            removeView();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final View.OnTouchListener touchListener = new View.OnTouchListener() {
        int lastX, lastY, x, y;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (MotionEvent.ACTION_MASK & e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) e.getRawX();
                    lastY = (int) e.getRawY();
                    x = mParams.x;
                    y = mParams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) e.getRawX() - lastX;
                    int dy = (int) e.getRawY() - lastY;
                    mParams.x = x + dx;
                    mParams.y = y + dy;
                    mWindowManager.updateViewLayout(mFloatView, mParams);
                    break;
            }
            return false;
        }
    };

    private void addView(String uriString, int position) {
        if (mFloating) return;
        mWindowManager.addView(mFloatView, mParams);
        mVideoView.setVideoURI(Uri.parse(uriString));
        mVideoView.seekTo(position);
        mVideoView.start();
        mFloating = true;
    }

    private void removeView() {
        if (mFloating)
            mWindowManager.removeView(mFloatView);
        mVideoView.stopPlayback();
        mFloating = false;
    }

    @Override
    public void onDestroy() {
        removeView();
        super.onDestroy();
    }
}
