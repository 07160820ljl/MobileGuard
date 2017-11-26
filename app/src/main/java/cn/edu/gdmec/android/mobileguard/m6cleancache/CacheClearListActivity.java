package cn.edu.gdmec.android.mobileguard.m6cleancache;

import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.text.format.Formatter;
import java.util.List;
import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m6cleancache.adapter.CacheCleanAdapter;
import cn.edu.gdmec.android.mobileguard.m6cleancache.entity.CacheInfo;

public class CacheClearListActivity extends AppCompatActivity implements View.OnClickListener {
    protected static final int SCANNING = 100;
    protected static final int FINISH = 101;
    private AnimationDrawable animation;
    private TextView mRecomandTV;
    private TextView mCanCleanTV;
    private long cacheMemory;
    private List<CacheInfo> cacheInfos = new ArrayList<CacheInfo>();
    private List<CacheInfo> mCacheInfos = new ArrayList<CacheInfo>();
    private PackageManager pm;
    private CacheCleanAdapter adapter;
    private ListView mCacheLv;
    private Button mCacheBtn;
    private Thread thread;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SCANNING:
                    PackageInfo info = (PackageInfo) msg.obj;
                    mRecomandTV.setText("正在扫描："+info.packageName);
                    mCanCleanTV.setText("已扫描缓存："+
                            Formatter.formatFileSize(CacheClearListActivity.this,cacheMemory));
                    mCacheInfos.clear();
                    mCacheInfos.addAll(cacheInfos);
                    adapter.notifyDataSetChanged();
                    mCacheLv.setSelection(mCacheInfos.size());
                    break;
                case FINISH:
                    animation .stop();
                    if (cacheMemory > 0) {
                        mCacheBtn.setEnabled(true);
                    }else{
                        mCacheBtn.setEnabled(false);
                        Toast.makeText(CacheClearListActivity.this,
                                "您的手机洁净如新",Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_clear_list);
        pm = getPackageManager();
        initView();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(
                getResources().getColor(R.color.rose_red));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        ((TextView) findViewById(R.id.tv_title)).setText("缓存扫描");
        mRecomandTV = (TextView) findViewById(R.id.tv_recommend_clean);
        mCanCleanTV = (TextView) findViewById(R.id.tv_can_clean);
        mCacheLv = (ListView) findViewById(R.id.lv_scancache);
        mCacheBtn = (Button) findViewById(R.id.btn_cleanall);
        mCacheBtn.setOnClickListener(this);
        animation = (AnimationDrawable) findViewById(R.id.imgv_broom).getBackground();
        animation.setOneShot(false);
        animation.start();
        adapter = new CacheCleanAdapter(this,mCacheInfos);
        mCacheLv.setAdapter(adapter);
        fillData();
    }

    private void fillData() {
        thread = new Thread(){
            public void run(){
                cacheInfos.clear();
                List<PackageInfo> infos = pm.getInstalledPackages(0);
                for(PackageInfo info : infos){
                    getCacheSize(info);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    msg.obj = info;
                    msg.what = SCANNING;
                    handler.sendMessage(msg);
                }
                Message msg = Message.obtain();
                msg.what = FINISH;
                handler.sendMessage(msg);
                };
            };
            thread.start();
        }


    private void getCacheSize(PackageInfo info) {
        try {
            Method method = PackageManager.class.getDeclaredMethod(
                    "getPackageSizeInfo", String.class,
                    IPackageStatsObserver.class);
            method.invoke(pm,info.packageName,new MyPackObserver(info));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class  MyPackObserver extends IPackageStatsObserver.Stub{
        private PackageInfo info;
        public MyPackObserver(PackageInfo info){
            this.info = info;
        }

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            long cachesize = pStats.cacheSize;
            if (cachesize >= 0){
                CacheInfo cacheInfo = new CacheInfo();
                cacheInfo.cacheSize = cachesize;
                cacheInfo.packagename = info.packageName;
                cacheInfo.appName = info.applicationInfo.loadLabel(pm).toString();
                cacheInfo.appIcon = info.applicationInfo.loadIcon(pm);
                cacheInfos.add(cacheInfo);
                cacheMemory += cachesize;
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_cleanall:
                if (cacheMemory > 0){
                    Intent intent = new Intent(this,CleanCacheActivity.class);
                    intent.putExtra("cacheMemory",cacheMemory);
                    startActivity(intent);
                    finish();
                }
                break;
        }
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animation.stop();
        if (thread != null){
            thread.interrupt();
        }
    }
}
