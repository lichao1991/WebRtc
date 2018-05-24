package com.icheyy.webrtcdemo.bean;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;


/**
 * Created by Dell on 2017/9/13.
 */

public class RemoterPeer implements SdpObserver, PeerConnection.Observer {
    private static final String TAG = RemoterPeer.class.getSimpleName();

    /**
     * 连接通道
     */
    private PeerConnection mConnection;
    /**
     * 用户名
     */
    private String mId;
    /**
     * 通道流信息
     */
    private MediaStream mMS;

    private PeerConnectionClient.RtcListener mRtcListener;
    private io.socket.client.Socket mSocket;


    public RemoterPeer(String id, io.socket.client.Socket socket, MediaStream ms) {
        Log.d(TAG, "new RemoterPeer: " + id + "::RemoterPeer: localMS:: " + ms);
        this.mId = id;
        mSocket = socket;
        mMS = ms;
    }

    public void setRTCListener(PeerConnectionClient.RtcListener listener) {
        mRtcListener = listener;
    }

    public void setPeerConnection(PeerConnection pc) {
        this.mConnection = pc;
        mConnection.addStream(mMS);
    }

    public void dispose() {
        Log.d(TAG, "dispose: " + mId);
        if (mRtcListener != null) {
            mRtcListener = null;
        }
        if (mConnection != null) {
            if (mMS != null)
                mConnection.removeStream(mMS);
            mConnection.close();
            mConnection.dispose();
            mConnection = null;
        }
    }


    //-------------------------------------SdpObserver interface start---------------------------------------------------------------

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    @Override
    public void onCreateSuccess(final SessionDescription sdp) {// createOffer/createAnswer成功回调此方法
        if (sdp == null)
            return;
        Log.d(TAG, "onCreateSuccess: sdp.description:: \n" + sdp.description);
        Log.i(TAG, "onCreateSuccess: sdp.type.canonicalForm():: " + sdp.type.canonicalForm());
        Log.i(TAG, "onCreateSuccess: mId " + mId);

        sendSDP(sdp);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, mId + "::onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, mId + "::onCreateFailure: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, mId + "::onSetFailure: " + s);
    }

    //-------------------------------------SdpObserver interface end---------------------------------------------------------------


    //-------------------------------------PeerConnection.Observer interface start---------------------------------------------------------------

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, mId + "::onSignalingChange: " + signalingState.toString());
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, mId + "::onIceConnectionChange: " + iceConnectionState);

        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            mRtcListener.onRemoveRemoteStream();
        }
        if (mRtcListener != null) {
            mRtcListener.onStatusChanged(mId, iceConnectionState);
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG, mId + "::IceConnectionReceiving changed to " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, mId + "::onIceGatheringChange: " + iceGatheringState.toString());
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
//        if (candidate == null)
//            return;
        Log.d(TAG, mId + "::onIceCandidate: \ncandidate.sdpMLineIndex:: " + candidate.sdpMLineIndex +
                "\ncandidate.sdpMid:: " + candidate.sdpMid);
        Log.d(TAG, mId + "::onIceCandidate: candidate.sdp:: \n" + candidate.sdp);
        JSONObject payload = new JSONObject();
        try {
            payload.put("label", candidate.sdpMLineIndex);
            payload.put("id", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);
            sendMessage(mId, "candidate", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        sendCandidate(candidate);
    }


    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, mId + "::onIceCandidatesRemoved: ");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, mId + "::onAddStream " + mediaStream.label());

        if (mediaStream.videoTracks.size() == 1) {
            mRtcListener.onAddRemoteStream(mediaStream, 1);
        }

        //            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
        //            mListener.onAddRemoteStream(mediaStream, endPoint + 1);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, mId + "::onRemoveStream " + mediaStream.label());
        mConnection.removeStream(mMS);
        mRtcListener.onRemoveRemoteStream();
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
    }

    @Override
    public void onRenegotiationNeeded() {
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, mId + "::onAddTrack: " + rtpReceiver.toString());
    }
    //-------------------------------------PeerConnection.Observer interface end---------------------------------------------------------------


    private void sendSDP(SessionDescription sdp) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("type", sdp.type.canonicalForm());
            payload.put("sdp", sdp.description);
            sendMessage(mId, sdp.type.canonicalForm(), payload);
            mConnection.setLocalDescription(this, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendCandidate(IceCandidate candidate) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("sdpMLineIndex", candidate.sdpMLineIndex);
            payload.put("sdpMid", candidate.sdpMid);
            payload.put("candidate", candidate.sdp);

            JSONObject msg = new JSONObject();
            msg.put("event", "candidate");
            msg.put("connectedUser", mId);
            msg.put("candidate", payload);
            sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        mSocket.send(msg);
    }

    public void sendMessage(JSONObject jsonObject) {
        if (jsonObject == null)
            return;
        Log.i(TAG, "sendMessage: " + jsonObject.toString());
        mSocket.send(jsonObject.toString());
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

    public String getId() {
        return mId;
    }


    public PeerConnection getPeerConnection() {
        return mConnection;
    }


    @Override
    public String toString() {
        return "RemoterPeer{mConnection: " + mConnection + ", mId: " + mId + "}";
    }
}