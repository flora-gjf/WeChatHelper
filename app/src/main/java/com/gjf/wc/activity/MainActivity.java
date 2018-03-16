package com.gjf.wc.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gjf.wc.R;
import com.gjf.wc.observable.WechatObserver;
import com.gjf.wc.utils.WechatHelper;
import com.gjf.wc.utils.WechatInfoSPHelper;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;
    private TextView textView;

    private WechatAuthObserver mWechatAuthObserver = new WechatAuthObserver();

    private LoginHandler mHandler = new LoginHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_auth) {
            auth();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        WechatHelper.getInstance().removeWechatObserver(mWechatAuthObserver);
    }

    private void initView() {
        button = findViewById(R.id.bt_auth);
        textView = findViewById(R.id.tv_info);

        button.setOnClickListener(this);
    }

    private void auth() {
        textView.setText("");

        WechatHelper.getInstance().addWechatObserver(mWechatAuthObserver);
        WechatHelper.getInstance().authorizeByWechat();
    }

    private void wechatAuthError() {
        textView.setText("微信校验失败");
    }

    private void wechatAuthSuccess() {
        // 微信授权结束后回调到此方法中
        textView.setText("微信昵称为 :" + WechatInfoSPHelper.getWechatUserNickname());
    }

    private class WechatAuthObserver extends WechatObserver {

        @Override
        public void handleStateChange(int data) {
            mHandler.sendEmptyMessage(data);
        }
    }

    static class LoginHandler extends Handler {

        private final WeakReference<MainActivity> mActivityRef;

        LoginHandler(MainActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(final Message msg) {
            MainActivity activity = mActivityRef.get();

            if (activity == null) {
                return;
            }

            int code = msg.what;

            switch (code) {
                case WechatHelper.WECHAT_CHECK_SUCCESS:
                    WechatHelper.getInstance().removeWechatObserver(activity.mWechatAuthObserver);
                    activity.wechatAuthSuccess();
                    break;
                case WechatHelper.WECHAT_CHECK_FAILURE:
                    WechatHelper.getInstance().removeWechatObserver(activity.mWechatAuthObserver);
                    activity.wechatAuthError();
                    break;
                default:
            }
        }
    }
}
