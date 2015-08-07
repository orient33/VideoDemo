package github.orient33.demo.videodemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class TextureViewActivity extends AppCompatActivity {
    private static final String KEY_POSITION = "position", KEY_URI = "uri";
    int position = 0;
    Uri uri;
    MyTextureView vv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_view);
        Toolbar tb = (Toolbar) findViewById(R.id.tb);
        tb.setTitle(getClass().getSimpleName());
        setSupportActionBar(tb);
        vv = (MyTextureView) findViewById(R.id.vv);

//        new TouchListen(findViewById(R.id.down));
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
                Intent intent = new Intent(TextureViewActivity.this, FloatService.class);
                intent.putExtra(FloatService.KEY_VIDEO_URI, uri.toString());
                intent.putExtra(FloatService.KEY_POSITION, vv.getCurrentPosition());
                startService(intent);
                finish();
            }
        });
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
