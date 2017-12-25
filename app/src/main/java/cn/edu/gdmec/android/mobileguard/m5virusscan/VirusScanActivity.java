
package cn.edu.gdmec.android.mobileguard.m5virusscan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.utils.VersionUpdateUtils;
import cn.edu.gdmec.android.mobileguard.m5virusscan.dao.AntiVirusDao;

public class VirusScanActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mLastTimeTV;
    private SharedPreferences mSP;
    private TextView mVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_scan);
        mSP = getSharedPreferences("config",MODE_PRIVATE);
        copyDB("antivirus.db","");
        initView();
    }
    //更新病毒库版本
    public void updateVesion(String dbVersion){
        final VersionUpdateUtils versionUpdateUtils = new VersionUpdateUtils(dbVersion,VirusScanActivity.this,downloadCallback,null);
        new Thread(){
            @Override
            public void run() {
                super.run();
                versionUpdateUtils.getCloudVersion("http://android2017.duapp.com/virusupdateinfo.html");
            }
        }.start();
    }
    VersionUpdateUtils.DownloadCallback downloadCallback = new VersionUpdateUtils.DownloadCallback() {
        @Override
        public void afterDownload(String filename) {
            copyDB("antivirus.db", Environment.getExternalStoragePublicDirectory("/download/").getPath());
        }
    };

    @Override
    protected void onResume() {
        String string = mSP.getString("lastVirusScan","你还没有查杀病毒！");
        mLastTimeTV.setText(string);
        //AntiVirusDao dao = new AntiVirusDao(VirusScanActivity.this);
        //String virusVersion = dao.getVirusVersion();
        //mVersionTV = (TextView) findViewById(R.id.tv_version);
        //mVersionTV.setText("病毒数据库版本:"+virusVersion);
        //调用版本病毒库版本
        //updateVesion(virusVersion);
        super.onResume();
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            AntiVirusDao dao = new AntiVirusDao(VirusScanActivity.this);
            String virusVersion = dao.getVirusVersion();
            mVersionTV = (TextView) findViewById(R.id.tv_version);
            mVersionTV.setText("病毒数据库版本:"+virusVersion);
            updateVesion(virusVersion);
            super.handleMessage(msg);
        }
    };

    private void copyDB(final String dbname,final String fromPath){
        new Thread(){
            public void run(){
                try{
                    File file = new File(getFilesDir(),dbname);
                    if(file.exists()&&file.length()>0&&fromPath.equals("")){
                        Log.i("VirusScanActivity","数据库已存在！");
                        handler.sendEmptyMessage(0);
                        return;
                    }
                    InputStream is = getAssets().open(dbname);
                    if (fromPath.equals("")){
                        is = getAssets().open(dbname);
                    }else{
                        file = new File(fromPath,
                                "antivirus.db");
                        is= new FileInputStream(file);
                    }
                    FileOutputStream fos = openFileOutput(dbname,MODE_PRIVATE);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while((len = is.read(buffer))!=-1){
                        fos.write(buffer,0,len);
                    }
                    is.close();
                    fos.close();
                    handler.sendEmptyMessage(0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            };
        }.start();
    }
    private void initView(){
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.light_blue));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("病毒查杀");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mLastTimeTV = (TextView) findViewById(R.id.tv_lastscantime);
        findViewById(R.id.rl_allscanvirus).setOnClickListener(this);
         findViewById(R.id.rl_cloudscanvirus).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.rl_allscanvirus:
                startActivity(new Intent(this,VirusScanSpeedActivity.class));
                break;
                 case R.id.rl_cloudscanvirus:
                Intent intent = new Intent(this,VirusScanSpeedActivity.class);
                intent.putExtra("cloud",true);
                startActivity(intent);
        }
    }

}
