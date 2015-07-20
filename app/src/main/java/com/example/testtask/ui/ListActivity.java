package com.example.testtask.ui;

import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.VideoView;

import com.example.testtask.AdvertisingModel;
import com.example.testtask.Const;
import com.example.testtask.R;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.json.JSONObject;

public class ListActivity extends AppCompatActivity {

    private int stopPosition = 0;

    private VideoView videoView;
    private View placeholder;
    private View videoContainer;
    private ScrollView scrollView;
    private AdvertisingModel advertisingModel = new AdvertisingModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (isViewVisibleMore50percent(videoContainer)) {
                    resumeVideo();
                } else {
                    pauseVideo();
                }
            }
        });

        videoContainer = findViewById(R.id.videoContainer);
        placeholder = findViewById(R.id.placeholder);
        findViewById(R.id.clickHandler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAdActivity();
            }
        });
        initVideoView();

        tryGetAd();
    }

    private void initVideoView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                placeholder.setVisibility(View.INVISIBLE);
                mp.setLooping(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        placeholder.setVisibility(View.VISIBLE);
        resumeVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseVideo();
    }

    private void resumeVideo() {
        videoView.seekTo(stopPosition);
        videoView.start();
    }

    private void pauseVideo() {
        stopPosition = videoView.getCurrentPosition();
        videoView.pause();
    }

    private void openAdActivity() {
        if (!TextUtils.isEmpty("advertisingModel.clickUrl")) {
            Intent intent = new Intent(this, AdActivity.class);
            intent.putExtra("click_url", advertisingModel.clickUrl);
            startActivity(intent);
        }
    }

    private boolean isViewVisibleMore50percent(View view) {
        Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        float top = view.getY();
        float middleLine = top + view.getHeight() / 2.0f;

        if (scrollBounds.top < middleLine && scrollBounds.bottom > middleLine) {
            return true;
        } else {
            return false;
        }
    }

    private void tryGetAd() {
        AndroidHttpClient httpClient = new AndroidHttpClient(Const.JSON_URL);
        httpClient.setMaxRetries(3);
        httpClient.get(null, null, new AsyncCallback() {
            @Override
            public void onComplete(HttpResponse httpResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(httpResponse.getBodyAsString());
                    advertisingModel.clickUrl = jsonObject.getString("click_url");
                    advertisingModel.videoSource = jsonObject.getString("video_source");
                    videoContainer.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(Uri.parse(advertisingModel.videoSource));
                    if (isViewVisibleMore50percent(videoContainer)) {
                        resumeVideo();
                    }
                } catch (Exception e) {
                    Log.e("ListActivity", "Can't get video URL", e);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("ListActivity", "Exception when get video URL", e);
            }
        });
    }

}
