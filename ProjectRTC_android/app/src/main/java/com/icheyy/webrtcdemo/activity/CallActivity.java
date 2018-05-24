package com.icheyy.webrtcdemo.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.ProxyRenderer;
import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.base.BaseAppActivity;
import com.icheyy.webrtcdemo.bean.RemoterPeer;
import com.icheyy.webrtcdemo.bean.PeerConnectionClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import okhttp3.OkHttpClient;


/**
 * 某些机型 默认使用听筒 而不是扩音机
 * 需要修复
 * 因为引入的包内引入了 okhttp 在集成时 看项目 适当做剔除
 */
public class CallActivity extends BaseAppActivity {

    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String EXTRA_IS_CALLED = "com.icheyy.webrtc.IS_CALLED";//是否是呼叫方唤起

    public static final String EXTRA_USER_NAME = "com.icheyy.webrtc.USER_NAME";
    public static final String EXTRA_CALLER_NAME = "com.icheyy.webrtc.CALLER_NAME";
    public static final String EXTRA_VIDEO_CALL = "com.icheyy.webrtc.VIDEO_CALL";
    public static final String EXTRA_SCREENCAPTURE = "com.icheyy.webrtc.SCREENCAPTURE";
    public static final String EXTRA_CAMERA2 = "com.icheyy.webrtc.CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "com.icheyy.webrtc.VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "com.icheyy.webrtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "com.icheyy.webrtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_BITRATE = "com.icheyy.webrtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "com.icheyy.webrtc.VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "com.icheyy.webrtc.HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "com.icheyy.webrtc.CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "com.icheyy.webrtc.FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "com.icheyy.webrtc.AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "com.icheyy.webrtc.AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =
            "com.icheyy.webrtc.NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "com.icheyy.webrtc.AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED = "com.icheyy.webrtc.OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "com.icheyy.webrtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "com.icheyy.webrtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "com.icheyy.webrtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "com.icheyy.webrtc.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =
            "com.icheyy.webrtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "com.icheyy.webrtc.DISPLAY_HUD";
    public static final String EXTRA_TRACING = "com.icheyy.webrtc.TRACING";
    public static final String EXTRA_CMDLINE = "com.icheyy.webrtc.CMDLINE";
    public static final String EXTRA_RUNTIME = "com.icheyy.webrtc.RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "com.icheyy.webrtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT =
            "com.icheyy.webrtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_USE_VALUES_FROM_INTENT =
            "com.icheyy.webrtc.USE_VALUES_FROM_INTENT";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "com.icheyy.webrtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "com.icheyy.webrtc.ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "com.icheyy.webrtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "com.icheyy.webrtc.MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "com.icheyy.webrtc.PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "com.icheyy.webrtc.NEGOTIATED";
    public static final String EXTRA_ID = "com.icheyy.webrtc.ID";

    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    protected PeerConnectionClient pcClient;
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final ProxyRenderer localProxyRenderer = new ProxyRenderer();
    private EglBase rootEglBase;
    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};
    private VideoTrack mRemoteVideoTrack;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;
    private Button btn_switch;
    private String host;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initViews();

        // Check for mandatory permissions.  检查权限
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                logAndToast("Permission " + permission + " is not granted");
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }
        initPeerConnection();
    }

    protected void initPeerConnection() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        Log.d(TAG, "init: displaySize:: x -> " + displaySize.x + ", y -> " + displaySize.y);
        PeerConnectionParameters pcParams = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP8, true, 1, AUDIO_CODEC_OPUS, true);
        pcClient = new PeerConnectionClient(getString(R.string.pref_p2p_server_url_default), getIOOptions(), pcParams, this, mRtcListener);
        pcClient.start(rootEglBase.getEglBaseContext());
    }

    private IO.Options getIOOptions() {
        SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
        X509TrustManager x509TrustManager = getX509TrustManager();
        if (sslSocketFactory == null || x509TrustManager == null) return null;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .sslSocketFactory(sslSocketFactory, x509TrustManager)
                .build();

        // default settings for all sockets
        IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
        IO.setDefaultOkHttpCallFactory(okHttpClient);

        // set as an option
        IO.Options opts = new IO.Options();
        opts.callFactory = okHttpClient;
        opts.webSocketFactory = okHttpClient;
        return opts;
    }

    private X509TrustManager getX509TrustManager() {
        TrustManager[] trustManagers = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SSLSocketFactory getSSLSocketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext == null ? null : sslContext.getSocketFactory();
    }


    private void initViews() {
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        remoteRenderers.add(remoteProxyRenderer);

        // Create video renderers.
        rootEglBase = EglBase.create();
        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        setSwappedFeeds(true /* isSwappedFeeds */);

        remoteRenderers.add(remoteProxyRenderer);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pcClient.switchCameraInternal();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            toHangUp();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }


    public void toCall(String remoterId) {
        Log.d(TAG, "toCall: ====================");
//        pcClient.setRemoterId(remoterId);
        sendCall(remoterId);

    }

    public void toHangUp() {
        Log.d(TAG, "toHangUp: ====================");
        RemoterPeer remoterPeer = pcClient.getRemoterPeer();
        if (remoterPeer != null) {
            sendHangUp(remoterPeer);
            PeerConnection pc = remoterPeer.getPeerConnection();
            pc.close();
        }
        if (!isSwappedFeeds)
            setSwappedFeeds(true);
    }

    @Override
    public void onPause() {
//        if (pcClient != null) {
//            pcClient.onPause();
//        }
        if (pcClient != null) {
            pcClient.stopVideoSource();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pcClient != null) {
            pcClient.startVideoSource();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        disconnect();
        pcClient.removeRemoterPeer();
        pcClient.onDestroy();
        super.onDestroy();
    }

    private PeerConnectionClient.RtcListener mRtcListener = new PeerConnectionClient.RtcListener() {

        @Override
        public void onConnectSocketFinish(boolean result) {
            if (result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication().getApplicationContext(), "SOCKET 链接成功", Toast.LENGTH_LONG).show();
                    }
                });
                Intent intent = getIntent();
                // Get Intent parameters. 取得需呼叫的对方流ID
                String callerName = intent.getStringExtra(EXTRA_CALLER_NAME);
                Log.d(TAG, "toCall: calleeName:: " + callerName);

                Boolean isCalled = intent.getBooleanExtra(EXTRA_IS_CALLED, false);
                if (!isCalled) {//是呼叫方
                    toCall(callerName);
                }
            }
        }

        @Override
        public void onStatusChanged(final String id, PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "onStatusChanged: id:: " + id + ", " + iceConnectionState);
            switch (iceConnectionState) {
                case DISCONNECTED:
                case CLOSED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplication().getApplicationContext(), id + " 通话关闭", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (!isSwappedFeeds)
                        setSwappedFeeds(true);
                    pipRenderer.clearImage();
                    mRemoteVideoTrack = null;
                    remoteProxyRenderer.setTarget(null);
                    finish();
                    break;
            }
        }

        @Override
        public void onLocalStream(MediaStream localStream, VideoTrack track) {
            Log.d(TAG, "onLocalStream localStream.videoTracks:: " + localStream.videoTracks);
            Log.d(TAG, "onLocalStream localProxyRenderer:: " + localProxyRenderer);
            if (localStream.videoTracks == null)
                return;
            if (track == null)
                return;
            track.addRenderer(new VideoRenderer(localProxyRenderer));

        }

        @Override
        public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
            Log.d(TAG, "onAddRemoteStream");
            if (isSwappedFeeds)
                setSwappedFeeds(false);

            mRemoteVideoTrack = remoteStream.videoTracks.get(0);
            mRemoteVideoTrack.setEnabled(true);
            for (VideoRenderer.Callbacks remoteRender : remoteRenderers) {
                mRemoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
            }

        }

        @Override
        public void onRemoveRemoteStream() {
            Log.d(TAG, "onRemoveRemoteStream");

            if (!isSwappedFeeds)
                setSwappedFeeds(true);
            pipRenderer.clearImage();
            mRemoteVideoTrack = null;
            remoteProxyRenderer.setTarget(null);
            finish();
        }
    };

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyRenderer.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        remoteProxyRenderer.setTarget(null);
        localProxyRenderer.setTarget(null);
        if (pipRenderer != null) {
            pipRenderer.clearImage();
            pipRenderer.release();
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.clearImage();
            fullscreenRenderer.release();
        }
    }

    private void sendHangUp(RemoterPeer remoterPeer) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("event", "leave");
            msg.put("connectedUser", remoterPeer.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pcClient.sendMessage(msg);
    }

    private void sendCall(String remoterId) {
        try {
            pcClient.sendMessage(remoterId, "init", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}