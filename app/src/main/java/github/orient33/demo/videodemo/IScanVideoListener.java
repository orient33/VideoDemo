package github.orient33.demo.videodemo;

import java.util.List;

import github.orient33.demo.videodemo.MainActivity.VideoInfo;

public interface IScanVideoListener {

    void onScanCompleted(List<VideoInfo> data);
}
