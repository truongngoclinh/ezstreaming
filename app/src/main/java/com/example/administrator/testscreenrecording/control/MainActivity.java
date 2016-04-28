package com.example.administrator.testscreenrecording.control;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.administrator.testscreenrecording.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This is screen recording sample using Media Projection
 */

public class MainActivity extends AppCompatActivity {

    /* For recording screen */
    private static final int REQUEST_CODE = 1000;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;
    private int mScreenDensity;

    private ToggleButton mBtnRecord;
    private MediaRecorder mMediaRecorder;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallBack mMediaProjectionCallback;
    private static final int REQUEST_PERMISSIONS = 10;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /* For playing youtube video by android VideoView*/
    private VideoView mVideoView;
    private MediaController mMediaController;
    private Uri mYoutubeUri;

    /* For youtube player demo */
    private Button mBtnYoutubeDemo;


    /* For streaming video to rtmp server */
    private Button nBtnStreamingDemo;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initScreenRecording();
//        initVideoView(savedInstanceState);
        initYoutubePlayer();
        initStreaming();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * for streaming to youtube channel
     * url: rtmp://a.rtmp.youtube.com/live2
     * key: svfy-fddj-r44p-dzfc
     */
    private void initStreaming() {
        nBtnStreamingDemo = (Button) findViewById(R.id.btnStreaming);
        nBtnStreamingDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent i = new Intent(MainActivity.this, StreamingActivityWithCamera2.class);
                startActivity(i);*/
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, StreamingCamera2Fragment.newInstance())
                        .commit();
                Snackbar.make(findViewById(android.R.id.content), "Open camera...", Snackbar.LENGTH_LONG).show();
            }
        });

    }


    /**
     * for youtube player api
     * key: AIzaSyBNJhSAlThppNjI47iFCtCuqdIFeMo5Vgg
     */
    private void initYoutubePlayer() {
        mBtnYoutubeDemo = (Button) findViewById(R.id.btnYoutube);
        mBtnYoutubeDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayYoutubeActivity.class);
                startActivity(intent);
                Snackbar.make(findViewById(android.R.id.content), "Start youtube player", Snackbar.LENGTH_LONG).show();
            }
        });
    }


    /**
     * for android videoView
     */
    private void initVideoView(Bundle savedInstanceState) {
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mVideoView.setVisibility(View.VISIBLE);
        mMediaController = new MediaController(this);
        mVideoView.setMediaController(mMediaController);
        mVideoView.requestFocus();

        if (savedInstanceState != null) {
            // seeking to last position of video
            int loc = savedInstanceState.getInt("Loc");
            mYoutubeUri = Uri.parse(savedInstanceState.getString("url"));
            mVideoView.setVideoURI(mYoutubeUri);
            mVideoView.seekTo(loc);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.v("onPrepared", "ok");
                    mp.start();
                }
            });

        } else {
            RTSPUrlTask fetchingTask = new RTSPUrlTask();
            fetchingTask.execute("http://www.youtube.com/watch?v=2zNSgSzhBfM");
        }
    }


    /**
     * for recording screen
     */
    private void initScreenRecording() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        mBtnRecord = (ToggleButton) findViewById(R.id.btnRecord);
        mBtnRecord.setOnClickListener(mOnRecordBtnClickListsener);
    }


    View.OnClickListener mOnRecordBtnClickListsener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* check the permission */
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale
                                (MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    mBtnRecord.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission
                                                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                            REQUEST_PERMISSIONS);
                                }
                            }).show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission
                                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                            REQUEST_PERMISSIONS);
                }
            } else {
                onToggleScreenShare(v);
            }
        }
    };

    private void onToggleScreenShare(View v) {
        Log.d("LINH", "onClick() (v.isChecked())");
        if (((ToggleButton) v).isChecked()) {
            initRecorder();
            shareScreen();
        } else {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                stopScreenSharing();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        Log.d("LINH", "shareScreen()");
        if (mMediaProjection == null) {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        Snackbar.make(findViewById(android.R.id.content), "Start recording screen", Snackbar.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        Log.d("LINH", "createVirtualDisplay()");
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                    /*Handler*/);
    }

    private void initRecorder() {
        Log.d("LINH", "initRecorder()");
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(Environment
                    .getExternalStorageDirectory() + "/test.mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);

            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing() {
        Log.d("LINH", "stopScreenSharing()");
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        Log.d("LINH", "destroyMediaProjection()");
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("LINH", "onActivityResult()");
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mMediaProjectionCallback = new MediaProjectionCallBack();
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.administrator.testscreenrecording/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.administrator.testscreenrecording/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallBack extends MediaProjection.Callback {

        @Override
        public void onStop() {
            Log.d("LINH", "onStop() callback");
            if (mBtnRecord.isChecked()) {
                mBtnRecord.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyMediaProjection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(mBtnRecord);
                } else {
                    mBtnRecord.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    private class RTSPUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = getRTSPVideoUrl(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            startPlaying(result);
        }

        public String getRTSPVideoUrl(String urlYoutube) {
            try {
                String gdy = "http://gdata.youtube.com/feeds/api/videos/";
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                String id = extractYoutubeId(urlYoutube);
                URL url = new URL(gdy + id);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                Document doc = dBuilder.parse(connection.getInputStream());
                Element el = doc.getDocumentElement();
                NodeList list = el.getElementsByTagName("media:content");
                String cursor = urlYoutube;
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node != null) {
                        NamedNodeMap nodeMap = node.getAttributes();
                        HashMap<String, String> maps = new HashMap<String, String>();
                        for (int j = 0; j < nodeMap.getLength(); j++) {
                            Attr att = (Attr) nodeMap.item(j);
                            maps.put(att.getName(), att.getValue());
                        }
                        if (maps.containsKey("yt:format")) {
                            String f = maps.get("yt:format");
                            if (maps.containsKey("url"))
                                cursor = maps.get("url");
                            if (f.equals("1"))
                                return cursor;
                        }
                    }
                }
                return cursor;
            } catch (Exception ex) {
                return urlYoutube;
            }
        }

        public String extractYoutubeId(String url) throws MalformedURLException {
            String query = new URL(url).getQuery();
            String[] param = query.split("&");
            String id = null;
            for (String row : param) {
                String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
            return id;
        }
    }

    void startPlaying(String url) {
        mYoutubeUri = Uri.parse(url);
        mVideoView.setVideoURI(mYoutubeUri);
        mVideoView.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("loc", mVideoView.getCurrentPosition());
        outState.putString("url", mYoutubeUri.toString());
    }
}
