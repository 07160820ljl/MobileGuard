package cn.edu.gdmec.android.mobileguard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import cn.edu.gdmec.android.mobileguard.m1home.utils.MyUtils;
import cn.edu.gdmec.android.mobileguard.m1home.utils.VersionUpdateUtils;

public class SplashActivity extends AppCompatActivity {
    private TextView mTvVersion;
    private String mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mVersion = MyUtils.getVersion(getApplicationContext());
        mTvVersion = (TextView) findViewById(R.id.tv_splash_version);
        mTvVersion.setText("版本号：" + mVersion);
        final VersionUpdateUtils versionUpdateUtils = new VersionUpdateUtils(mVersion, SplashActivity.this);
        new Thread() {
            @Override
            public void run() {
                super.run();
                versionUpdateUtils.getCloudVersion();
                System.out.println("1");
            }
        }.start();
    }
}
