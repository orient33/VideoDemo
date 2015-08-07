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
    MyTextureView mVideoView;
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
            mVideoView = (MyTextureView) mFloatView.findViewById(R.id.vv);
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
//            .setVisibility(View.GONE);
            mFloatView.findViewById(R.id.player_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopSelf();
                }
            });
            mFloatView.findViewById(R.id.player_resize).setOnTouchListener(touchListener2);
            mFloatView.findViewById(R.id.player_float).setOnTouchListener(touchListener);
        }
        if (intent != null) {
            if (!mFloating) {
                String uri = intent.getStringExtra(KEY_VIDEO_URI);
                int position = intent.getIntExtra(KEY_POSITION, 0);
                addView(uri, position);
            } else {
                removeView();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 拖拽Video更换位置
     */
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
            return true;
        }
    };

    /**
     * 拖拽修改Video的大小
     */
    private final View.OnTouchListener touchListener2 = new View.OnTouchListener() {
        int lastX, lastY, moveX, moveY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (MotionEvent.ACTION_MASK & e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) e.getRawX();
                    lastY = (int) e.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = (int) e.getRawX();
                    moveY = (int) e.getRawY();
                    mParams.width += moveX - lastX;
                    mParams.height += moveY - lastY;
                    lastX = moveX;
                    lastY = moveY;
                    mWindowManager.updateViewLayout(mFloatView, mParams);
                    break;
            }
            return true;
        }
    };


    private void addView(String uriString, int position) {
        if (mFloating) return;
        mVideoView.setVideoURI(Uri.parse(uriString));
        mVideoView.seekTo(position);
        mParams.width = mVideoView.getmVideoWidth();//VideoView不能获取Video的宽高度
        mParams.height = mVideoView.getmVideoHeight();
        mWindowManager.addView(mFloatView, mParams);
//        mVideoView.start();
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
