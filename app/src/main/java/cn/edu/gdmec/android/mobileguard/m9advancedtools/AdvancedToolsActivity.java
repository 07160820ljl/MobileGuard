package cn.edu.gdmec.android.mobileguard.m9advancedtools;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.widget.AdvancedToolsView;

public class AdvancedToolsActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_tools);
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(
                getResources().getColor(R.color.bright_red));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("高级工具");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        findViewById(R.id.advanceview_applock).setOnClickListener(this);
        findViewById(R.id.advanceview_numbelongs).setOnClickListener(this);
        findViewById(R.id.advanceview_smsbackup).setOnClickListener(this);
        findViewById(R.id.advanceview_smsreducition).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.advanceview_numbelongs:
                startActivity(NumBelongtoActivity.class);
                break;
            case R.id.advanceview_applock:
                startActivity(AppLockActivity.class);
                break;
        }

    }

    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }
}
