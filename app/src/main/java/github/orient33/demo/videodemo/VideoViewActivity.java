package github.orient33.demo.videodemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private final String KEY_POSITION = "position";
    private final String KEY_URI = "uri";
    VideoView vv;
    Uri uri;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        Toolbar tb = (Toolbar) findViewById(R.id.tb);
        tb.setTitle(getClass().getSimpleName());
        setSupportActionBar(tb);
        vv = (VideoView) findViewById(R.id.vv);
//        new TouchListen(findViewById(R.id.down));
        vv.setMediaController(new MediaController(this));
        vv.setOnPreparedListener(this);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(KEY_POSITION);
            uri = savedInstanceState.getParcelable(KEY_URI);
        } else {
            position = 0;
            uri = getIntent().getData();
        }
        findViewById(R.id.player_float).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoViewActivity.this, FloatService.class);
                intent.putExtra(FloatService.KEY_VIDEO_URI, uri.toString());
                intent.putExtra(FloatService.KEY_POSITION, vv.getCurrentPosition());
                startService(intent);
                finish();
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        vv.seekTo(position);
        vv.start();
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
