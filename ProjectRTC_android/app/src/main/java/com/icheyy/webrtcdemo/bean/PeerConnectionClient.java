package com.icheyy.webrtcdemo.bean;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.activity.CallActivity;
import com.icheyy.webrtcdemo.base.BaseAppActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
//import org.webrtc.VideoCapturerAndroid;

public class PeerConnectionClient {
    private final static String TAG = "PeerConnectionClient";
    private Context mContext;
    private PeerConnectionFactory factory;
    // 本地连接接口
    private Socket mSocket;
    private String mSelfId;
    private String mRemoterId;
    private RemoterPeer mRemoterPeer;
    // 本地连接参数和媒体信息
    private PeerConnectionParameters pcParams;

    private VideoSource videoSource;
    private VideoCapturer videoCapturer;
    private MediaStream mLocalMS;
    private MediaConstraints pcConstraints = new MediaConstraints();

    private RtcListener mListener;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();

    /**
     * Implement this interface to be notified of events.
     */
    public interface RtcListener {
        /**
         * socket连接完成
         *
         * @param result
         */
        void onConnectSocketFinish(boolean result);

        /**
         * rtc状态改变
         *
         * @param id
         * @param iceConnectionState
         */
        void onStatusChanged(String id, PeerConnection.IceConnectionState iceConnectionState);

        /**
         * 加载本地视频
         *
         * @param localStream
         * @param track
         */
        void onLocalStream(MediaStream localStream, VideoTrack track);

        /**
         * 加载远程视频
         *
         * @param remoteStream
         * @param endPoint
         */
        void onAddRemoteStream(MediaStream remoteStream, int endPoint);

        /**
         * 移除视频
         */
        void onRemoveRemoteStream();
    }


    public PeerConnectionClient(String host, IO.Options options,
                                PeerConnectionParameters params, final Context context,        RtcListener rtcListener) {
        Log.i(TAG, ">>>>>>> PeerConnectionClient: host:: " + host);
        mContext = context;
        pcParams = params;
       mListener=rtcListener;

        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        mSocket = getSocket(host, options);

        // creatPeerConnectionFactory
        PeerConnectionFactory.initializeAndroidGlobals(
                context/*上下文，可自定义监听*/, pcParams.videoCodecHwAcceleration/*是否支持硬件加速*/);
        PeerConnectionFactory.Options opt = null;
        if (pcParams.loopback) {
            opt = new PeerConnectionFactory.Options();
            opt.networkIgnoreMask = 0;
        }
        factory = new PeerConnectionFactory(opt);
    }

    /**
     * 初始化本地socket
     */
    private Socket getSocket(String host, IO.Options options) {

        try {
            //            mSocket = IO.socket(host);
            final Socket mSocket = IO.socket(host, options);
            mSocket.on("id", onId);
            mSocket.on("message", onMessage);
            mSocket.connect();
            Log.d(TAG, "onCreate: mSocket.connect() finish");
            return mSocket;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Boolean> mAllUsers;
    private Emitter.Listener onId = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String name = (String) args[0];
            JSONObject message = new JSONObject();
            try {
                message.put("name", name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mListener.onConnectSocketFinish(true);
            mSocket.emit("readyToStream", message);
        }
    };
    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(TAG, "call in onMessage: data:: \n" + data.toString());

            try {
                String event = (String) data.get("type");
                String from = (String) data.get("from");
                JSONObject payload = null;
                if (!event.equals("init")) {
                    payload = data.getJSONObject("payload");
                }
                if(mRemoterPeer==null){
                    mRemoterId = from;
                    createRemotePeer(mRemoterId);
                    Log.i(TAG, "handleAccept: mCallerId:: " + mRemoterPeer.getId());
                    Log.d(TAG, "handleAccept: peer:: " + mRemoterId);
                    Log.d(TAG, "handleAccept: peerConn:: " + mRemoterPeer.getPeerConnection());
                }
                 if (TextUtils.equals("offer", event)) {
                    handleOffer(payload);
                } else if (TextUtils.equals("candidate", event)) {
                    handleCandidate(payload);
                } else if (TextUtils.equals("answer", event)) {
                    handleAnswer(payload);
                }  else if (TextUtils.equals("init", event)) {
                     mRemoterPeer.getPeerConnection().createOffer(mRemoterPeer, pcConstraints);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void handleOffer(JSONObject payload) throws JSONException {
        Log.d(TAG, "handleOffer: name:: " + payload);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                payload.getString("sdp")
        );
        mRemoterPeer.getPeerConnection().setRemoteDescription(mRemoterPeer, sdp);
        mRemoterPeer.getPeerConnection().createAnswer(mRemoterPeer, pcConstraints);
    }

    private void handleAnswer(JSONObject answer) throws JSONException {
        Log.d(TAG, "handleAnswer: answer:: " + answer);
        SessionDescription sdp = new SessionDescription(
                SessionDescription.Type.fromCanonicalForm(answer.getString("type")),
                answer.getString("sdp")
        );
        mRemoterPeer.getPeerConnection().setRemoteDescription(mRemoterPeer, sdp);
        mRemoterPeer.getPeerConnection().createAnswer(mRemoterPeer, pcConstraints);
    }

    private void handleCandidate(JSONObject payload) throws JSONException {
        Log.d(TAG, "handleCandidate: candidate:: " + payload);
        if (mRemoterPeer.getPeerConnection().getRemoteDescription() != null) {
            IceCandidate candidate = new IceCandidate(
                    payload.getString("id"),
                    payload.getInt("label"),
                    payload.getString("candidate")
            );
            mRemoterPeer.getPeerConnection().addIceCandidate(candidate);
        }
    }


    public void removeRemoterPeer() {
        if (mRemoterPeer != null) {
            mRemoterPeer.dispose();
            mRemoterPeer = null;
        }
    }


    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (videoSource != null) {
            Log.d(TAG, "VideoSource dispose");
            videoSource.dispose();
            videoSource = null;
        }

        if (factory != null) {
            Log.d(TAG, "PeerConnectionFactory dispose");
            factory.dispose();
            factory = null;
        }


        if (mSocket != null) {
            Log.d(TAG, "Socket dispose");
            closeSocket();
            mSocket = null;
        }


    }

    private void closeSocket() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
    }

    /**
     * Start the client.
     * <p>
     * Set up the local stream and notify the signaling server.
     * Call this method after onCallReady.
     */
    public void start(EglBase.Context renderEGLContext) {
        initPeerConnectFactory(renderEGLContext);
    }

    private void initPeerConnectFactory(EglBase.Context renderEGLContext) {

        mLocalMS = factory.createLocalMediaStream("ARDAMS");
        Log.i(TAG, "initPeerConnectFactory: mLocalMS:: " + mLocalMS);

        VideoTrack track = null;
        if (pcParams.videoCallEnabled) {
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(pcParams.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(pcParams.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(pcParams.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(pcParams.videoFps)));

            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
            Log.d(TAG, "initPeerConnectFactory: videoCapturer:: " + videoCapturer);
            if (videoCapturer == null)
                return;
            videoSource = factory.createVideoSource(videoCapturer/*, videoConstraints*/);
            startVideoSource();
            track = factory.createVideoTrack("ARDAMSv0", videoSource);
            mLocalMS.addTrack(track);
            track.setEnabled(true);
        }

        AudioSource audioSource = factory.createAudioSource(new MediaConstraints());
        mLocalMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        Log.d(TAG, "initPeerConnectFactory: track:: " + track);
        mListener.onLocalStream(mLocalMS, track);
    }

    private boolean videoCapturerStopped = true;

    public void stopVideoSource() {
        if (videoCapturer != null && !videoCapturerStopped) {
            Log.d(TAG, "Stop video source.");
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
            }
            videoCapturer.dispose();
            videoCapturerStopped = true;
        }
    }

    public void startVideoSource() {
        if (videoCapturer != null && videoCapturerStopped) {
            Log.d(TAG, "Restart video source.");
            videoCapturer.startCapture(pcParams.videoWidth, pcParams.videoHeight, pcParams.videoFps);
            videoCapturerStopped = false;
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Log.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Log.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Log.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    private void createRemotePeer(String name) {
        if (mRemoterPeer == null) {
            mRemoterPeer = new RemoterPeer(name, mSocket, mLocalMS);
            if (factory == null) {
                Log.e(TAG, "faile createPeer " + name + ", PeerConnectionFactory is null");
                return;
            }
//            iceServers.add(new PeerConnection.IceServer("turn:192.168.109.246:3478","gongwei","123456"));
            PeerConnection pc = factory.createPeerConnection(iceServers, pcConstraints, mRemoterPeer);
            mRemoterPeer.setPeerConnection(pc);
            mRemoterPeer.setRTCListener(mListener);
        }
    }

    public void sendMessage(String msg) {
        if (mSocket != null)
            mSocket.send(msg);
    }

    public void sendMessage(JSONObject jsonObject) {
        if (jsonObject == null)
            return;
        Log.i(TAG, "sendMessage: " + jsonObject.toString());
        sendMessage(jsonObject.toString());
    }

    /**
     * Send a message through the signaling server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        mSocket.emit("message", message);
    }

    /**
     * ICE trun服务配置
     * @return
     */
    private PeerConnection.RTCConfiguration getRTCConfig() {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer("turn:192.168.109.246:3478", "gongwei", "123456"));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        return rtcConfig;
    }

    public RemoterPeer getRemoterPeer() {
        return mRemoterPeer;
    }

//    public void setRemoterId(String remoterId) {
//        mRemoterId = remoterId;
//    }

    public void switchCameraInternal() {
        if (videoCapturer instanceof CameraVideoCapturer) {
//            if (!videoCallEnabled || isError || videoCapturer == null) {
//                Log.e(TAG, "Failed to switch camera. Video: " + videoCallEnabled + ". Error : " + isError);
//                return; // No video is sent or only one camera is available or error happened.
//            }
            Log.d(TAG, "Switch camera");
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(null);
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera");
        }
    }

}
