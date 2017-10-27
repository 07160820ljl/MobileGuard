package cn.edu.gdmec.android.mobileguard.m2theftguard;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import cn.edu.gdmec.android.mobileguard.R;

public class Setup4Activity extends BaseSetUpActivity {
    private TextView mStatusTV;
    private ToggleButton mToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_4);
        ((RadioButton) findViewById(R.id.rb_four)).setChecked(true);
        initView();
    }

    private void initView() {
        ((RadioButton) findViewById(R.id.rb_four)).setChecked(true);
        mStatusTV = (TextView) findViewById(R.id.tv_setup4_status);
        mToggleButton = (ToggleButton) findViewById(R.id.togglebtn_securityfunction);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mStatusTV.setText("防盗保护已经开启");
                } else {
                    mStatusTV.setText("防盗保护没有开启");
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("protecting", isChecked);
                editor.commit();
            }
        });
        boolean protecting = sp.getBoolean("protecting", true);
        if (protecting) {
            mStatusTV.setText("防盗保护已经开启");
            mToggleButton.setChecked(true);
        } else {
            mStatusTV.setText("防盗保护没有开启");
            mToggleButton.setChecked(false);
        }
    }

    @Override
    public void showNext() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isSetUp",true);
        editor.commit();
        startActivityAndFinishself(LostFindActivity.class);
    }

    @Override
    public void showpre() {
        startActivityAndFinishself(Setup3Activity.class);

    }
}