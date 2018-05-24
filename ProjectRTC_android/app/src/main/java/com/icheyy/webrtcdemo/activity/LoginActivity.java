package com.icheyy.webrtcdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.icheyy.webrtcdemo.R;
import com.icheyy.webrtcdemo.base.BaseAppActivity;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class LoginActivity extends BaseAppActivity {

    private static String TAG = LoginActivity.class.getSimpleName();

    private static final int CONNECTION_REQUEST = 1;

    private EditText et_user_name;
    private Button bt_login;
    private TextView tv_message;

    private SharedPreferences sharedPref;
    private String keyprefUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initPreference();// 参数初始化

        getView();

        initView();

        initRxPermissions();

    }


    /**
     * 权限申请
     */
    private void initRxPermissions() {
        // where this is an Activity instance
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
        )
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            Log.d(TAG, permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            Log.d(TAG, permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            Log.d(TAG, permission.name + " is denied.");
                        }
                    }
                });
    }


    private void getView() {
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        tv_message = (TextView) findViewById(R.id.tv_login_message);
        bt_login = (Button) findViewById(R.id.bt_login);
    }

    private void initView() {
        // 用户名
        String userName = sharedPref.getString(keyprefUserName, "cheyy");
        et_user_name.setText(userName);
        et_user_name.setSelection(userName.length());
        et_user_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    bt_login.performClick();//在键盘中按完成键，模拟点击添加room按钮
                    return true;
                }
                return false;
            }
        });
        et_user_name.requestFocus();

        // 登入
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getUserName();
                if (userName.isEmpty())
                    tv_message.setText("用户名不能为空");

                goToSelectCaller();
            }
        });
    }

    private void initPreference() {
        // Get setting keys.
        // 取得设置界面中setting的key值
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefUserName = getString(R.string.pref_user_name_key);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 保存userName输入框信息
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyprefUserName, getUserName());
        editor.commit();
    }


    /**
     * 跳转到选择被呼叫页面
     */
    private void goToSelectCaller() {

        Intent intent = new Intent(LoginActivity.this, SelectCallerActivity.class);
        startActivity(intent);

    }

    private String getUserName() {
        if (et_user_name == null)
            return "";

        String userName = et_user_name.getText().toString().trim();

        return userName;

    }


}
