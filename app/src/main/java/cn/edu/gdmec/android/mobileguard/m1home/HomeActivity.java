package cn.edu.gdmec.android.mobileguard.m1home;

import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.w3c.dom.Text;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.adapter.HomeAdapter;

import cn.edu.gdmec.android.mobileguard.m2theftguard.LostFindActivity;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.InterPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.SetUpPasswordDialog;

import cn.edu.gdmec.android.mobileguard.m2theftguard.receiver.MyDeviceAdminReceiver;
import cn.edu.gdmec.android.mobileguard.m2theftguard.utils.MD5Utils;

public class HomeActivity extends AppCompatActivity {
    private GridView gv_home;
    private long mExitTime;
    /**存储手机防盗密码的sp  */
    private SharedPreferences msharedPreferences;
    private DevicePolicyManager policyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        msharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        gv_home = (GridView) findViewById(R.id.gv_home);
        gv_home.setAdapter(new HomeAdapter(HomeActivity.this));
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //点击手机防盗
                        if(isSetUpPassword()){
                            //弹出输入密码对话框
                            showInterPwdDialog();
                        }else{
                            //弹出设置密码对话框
                            showSetUpPwdDialog();
                        }
                        break;
                }
            }
        });
        policyManager=(DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName=new ComponentName(this, MyDeviceAdminReceiver.class);
        boolean active = policyManager.isAdminActive(componentName);
        if(!active){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"获取超级管理员权限，用于远程锁屏和清除数据");
            startActivity(intent);
        }

    }

    public void startActivity(Class<?> cls){
        Intent intent = new Intent(HomeActivity.this,cls);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if((System.currentTimeMillis()-mExitTime)<2000)
            {
                System.exit(0);
            }else{
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_LONG).show();
                mExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     *弹出设置密码对话框  本方法需要完成“手机防盗模块才能启用”
     */
    private void showSetUpPwdDialog(){
        final SetUpPasswordDialog setupPasswordDialog = new SetUpPasswordDialog(
                HomeActivity.this);
        setupPasswordDialog
                .setCallBack(new SetUpPasswordDialog.MyCallBack(){

                    @Override
                    public void ok() {
                        String firstPwd = setupPasswordDialog.mFirstPWDET
                                .getText().toString().trim();
                        String affirmPwd = setupPasswordDialog.mAffirmET
                                .getText().toString().trim();
                        if(!TextUtils.isEmpty(firstPwd)
                                && !TextUtils.isEmpty(affirmPwd)){
                            if(firstPwd.equals(affirmPwd)){
                                //两次密码一致，存储密码
                                savePwd(affirmPwd);
                                setupPasswordDialog.dismiss();
                                //显示输入密码框
                                showInterPwdDialog();
                            }else{
                                Toast.makeText(HomeActivity.this, "两次密码不一致", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void cancel() {
                        setupPasswordDialog.dismiss();
                    }
                });
        setupPasswordDialog.setCancelable(true);
        setupPasswordDialog.show();
    }

    /**
     * 弹出输入密码对话框   本方法需要完成“手机防盗模块”之后才能启用
     */
    private void showInterPwdDialog(){
        final String password = getPassword();
        final InterPasswordDialog interPasswordDialog = new InterPasswordDialog(
                HomeActivity.this);
        interPasswordDialog.setCallBack(new InterPasswordDialog.MyCallBack() {
            @Override
            public void confirm() {
                if (TextUtils.isEmpty(interPasswordDialog.getPassword())){
                    Toast.makeText(HomeActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
                }else if (password.equals(MD5Utils.encode(interPasswordDialog
                        .getPassword()))){
                    //进入防盗页面
                    interPasswordDialog.dismiss();
                    startActivity(LostFindActivity.class);
                    Toast.makeText(HomeActivity.this, "可以进入手机防盗模块", Toast.LENGTH_LONG).show();
                }else{
                    //对话框消失，弹出土司
                    interPasswordDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "密码有误，请重新输入!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void cancle() {
                interPasswordDialog.dismiss();
            }
        });
        interPasswordDialog.setCancelable(true);
        //让对话框显示
        interPasswordDialog.show();
    }


    /**
     * 保存密码   本方法需要完成“手机防盗模块”之后才能启用
     */
    private void savePwd(String affirmPwd){
        SharedPreferences.Editor edit =  msharedPreferences.edit();
        //为了防止用户隐私被泄漏,因此需要加密密码
        edit.putString("PhoneAntiTheftPWD",MD5Utils.encode(affirmPwd));
        edit.commit();
    }

    /**
     * 获取密码
     *
     * return sp存储的密码
     */
    private String getPassword(){
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",null);
        if (TextUtils.isEmpty(password)){
            return "";
        }
        return password;
    }

    /**
     * 判断用户是否设置过手机防盗密码
     */
    private boolean isSetUpPassword(){
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",null);
        if (TextUtils.isEmpty(password)){
            return false;
        }
        return true;
    }




}
