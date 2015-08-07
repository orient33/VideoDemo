package github.orient33.demo.videodemo;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements IScanVideoListener, View.OnClickListener {

    RecyclerView mRecyclerView;
    View mEmptyView;
    MyAdapter mAdapter;
    ScanVideoFile mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmptyView = findViewById(android.R.id.empty);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyAdapter(getApplicationContext(), this, mEmptyView);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTask == null) {
            mTask = new ScanVideoFile(getApplicationContext(), this);
            mTask.execute();
        }
    }

    @Override
    public void onScanCompleted(List<VideoInfo> data) {
        mAdapter.setData(data);
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null) {
            final VideoInfo data = (VideoInfo) tag;
            final String[] activities = new String[]{"VideoViewActivity", "SurfaceViewActivity", "TextureViewActivity"};
            final String pkgName = getPackageName();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title)
                    .setItems(activities, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            ComponentName cn = new ComponentName(pkgName, pkgName + "." + activities[which]);
                            intent.setComponent(cn);
                            intent.setData(Uri.fromFile(new File(data.path)));
                            startActivity(intent);
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    protected void onStop() {
        mTask.cancel(true);
        mTask = null;
        super.onStop();
    }


    static class ScanVideoFile extends AsyncTask<Void, String, List<VideoInfo>> {
        final Context mmContext;
        final IScanVideoListener mmListener;

        ScanVideoFile(Context context, IScanVideoListener lis) {
            mmContext = context;
            mmListener = lis;
        }

        @Override
        protected List<VideoInfo> doInBackground(Void... params) {
            final List<VideoInfo> result = new ArrayList<VideoInfo>();
            Uri uri = MediaStore.Video.Media.getContentUri("external");
            String pro[] = {MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.DATA};
            Cursor cursor = mmContext.getContentResolver().query(uri, pro, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    String title = cursor.getString(0);
                    String path = cursor.getString(1);
                    result.add(new VideoInfo(title, path));
                }
            }
            if (result.size() < 1) {
                File sd_root = Environment.getExternalStorageDirectory();
                scanExternal(result, sd_root, 3);
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<VideoInfo> videoInfos) {
            if (isCancelled()) return;
            if (mmListener != null)
                mmListener.onScanCompleted(videoInfos);
        }

        private boolean scanExternal(List<VideoInfo> result, File root, int depth) {
            if (depth < 0) return false;
            for (File sub : root.listFiles()) {
                if (sub.isFile()) {
                    String name = sub.getName();
                    if (name.toLowerCase(Locale.ENGLISH).endsWith("mp4") || name.toLowerCase(Locale.ENGLISH).endsWith("3gp")) {
                        result.add(new VideoInfo(name, sub.getPath()));
                        return true;
                    }
                }
            }
            for (File folder : root.listFiles()) {
                if (folder.isDirectory()) {
                    boolean find = scanExternal(result, folder, depth - 1);
                    if (find) return true;
                }
            }
            return false;
        }
    }

    static class VideoInfo {
        String title, path;

        VideoInfo(String t, String p) {
            title = t;
            path = p;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, path;

        VH(View root) {
            super(root);
            title = (TextView) root.findViewById(R.id.title);
            path = (TextView) root.findViewById(R.id.path);
        }
    }

    static class MyAdapter extends RecyclerView.Adapter<VH> implements View.OnClickListener {
        List<VideoInfo> data;
        final LayoutInflater inflater;
        final View emptyView;
        final View.OnClickListener listener;

        MyAdapter(Context context, View.OnClickListener itemClickListener, View empty) {
            inflater = LayoutInflater.from(context);
            emptyView = empty;
            listener = itemClickListener;
        }

        void setData(List<VideoInfo> d) {
            data = d;
            notifyDataSetChanged();
            if (d == null || d.size() == 0)
                emptyView.setVisibility(View.VISIBLE);
            else
                emptyView.setVisibility(View.GONE);
        }

        @Override
        public VH onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.item_video, null);
            root.setOnClickListener(this);
            return new VH(root);
        }

        @Override
        public void onBindViewHolder(VH vh, int i) {
            final VideoInfo info = data.get(i);
            vh.title.setText(info.title);
            vh.path.setText(info.path);
            vh.itemView.setTag(info);
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v);
        }
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
