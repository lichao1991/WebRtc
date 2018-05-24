package com.icheyy.webrtcdemo.base;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.icheyy.webrtcdemo.PeerConnectionParameters;
import com.icheyy.webrtcdemo.bean.PeerConnectionClient;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import okhttp3.OkHttpClient;

public abstract class BaseAppActivity extends Activity {

    private static final String TAG = BaseAppActivity.class.getSimpleName();
    private Toast logToast;
    public static Handler mHandler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (logToast != null) {
            logToast.cancel();
        }
        super.onDestroy();
    }



    protected void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        logToast.show();
    }
}
