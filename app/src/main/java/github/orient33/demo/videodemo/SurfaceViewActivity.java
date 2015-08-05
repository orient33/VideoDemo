package github.orient33.demo.videodemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class SurfaceViewActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        View.OnClickListener {
    private static final String KEY_POSITION = "position", KEY_URI = "uri";
    int position = 0;
    Uri uri;
    View controlDown;
    MySurfaceView vv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        Toolbar tb = (Toolbar) findViewById(R.id.tb);
        tb.setTitle(getClass().getSimpleName());
        setSupportActionBar(tb);
        vv = (MySurfaceView) findViewById(R.id.msv);
        controlDown = findViewById(R.id.down);
        TouchListen tl = new TouchListen(controlDown);
        vv.setOnPreparedListener(this);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(KEY_POSITION);
            uri = savedInstanceState.getParcelable(KEY_URI);
        } else {
            position = 0;
            uri = getIntent().getData();
        }
        findViewById(R.id.float_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SurfaceViewActivity.this, FloatService.class);
                intent.putExtra(FloatService.KEY_VIDEO_URI, uri.toString());
                intent.putExtra(FloatService.KEY_POSITION, vv.getCurrentPosition());
                startService(intent);
                finish();
            }
        });
    }

    private void downVideo() {
        ObjectAnimator oa = ObjectAnimator.ofFloat(vv, "y", vv.getY(),
                vv.getY() + 200);
        oa.setDuration(2000);
        oa.start();
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(controlDown, "y", controlDown.getY(),
                controlDown.getY() + 200);
        oa2.setDuration(2000);
        oa2.setRepeatMode(ValueAnimator.REVERSE);
        oa2.start();
        Toast.makeText(this, "animator start", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down:
                downVideo();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        vv.seekTo(position);
        mp.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vv.setVideoURI(uri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int position = vv.getCurrentPosition();
        outState.putInt(KEY_POSITION, position);
        outState.putParcelable(KEY_URI, uri);
    }
}
