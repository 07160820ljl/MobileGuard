package cn.edu.gdmec.android.mobileguard.m9advancedtools.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import cn.edu.gdmec.android.mobileguard.App;
import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.AppInfoParser;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.utils.SystemInfoUtils;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.adapter.AppLockAdapter;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.db.dao.AppLockDao;

/**
 * Created by Administrator on 2017/12/14.
 */

public class AppLockFragment extends Fragment {
    private Context context;
    private TextView mLockTV;
    private ListView mLockLV;
    private CheckBox mLockCB;
    private AppLockDao dao;
    List<AppInfo> mLockApps=new ArrayList<AppInfo>();
    private AppLockAdapter adapter;
    private Uri uri=Uri.parse(App.APPLOCK_CONTENT_URI);
    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 10:
                    mLockApps.clear();
                    mLockApps.addAll((List<AppInfo>)msg.obj);
                    if (adapter==null){
                        adapter=new AppLockAdapter(mLockApps,getActivity());
                        mLockLV.setAdapter(adapter);
                    }else {
                        adapter.notifyDataSetChanged();
                    }
                    mLockTV.setText("加锁应用"+mLockApps.size()+"个");
                    break;
            }
        }
    };
    private List<AppInfo> appInfos;
    public void onAttach(Context context){
        super.onAttach(context);
        this.context=context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_app_lock,null);
        mLockTV= (TextView) view.findViewById(R.id.tv_lock);
        mLockLV= (ListView) view.findViewById(R.id.lv_lock);
        mLockCB= (CheckBox) view.findViewById(R.id.cb_applock_service);
        boolean running= SystemInfoUtils.isServiceRunning(context,"cn.edu.gdmec.t00385.android2016.myguard.m9advancedtools.service.AppLockService");
        mLockCB.setChecked(running);
        mLockCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                final Intent intent=new Intent();
                ComponentName componentName=new ComponentName("cn.edu.gdmec.android.mobileguard","cn.edu.gdmec.android.mobileguard.m9advancedtools.service.AppLockService");
                intent.setComponent(componentName);
                if (b){
                    context.startService(intent);
                }else {
                    context.startService(intent);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        dao=new AppLockDao(getActivity());
        appInfos= AppInfoParser.getAppInfos(getActivity());
        fillData();
        initListener();
        getActivity().getContentResolver().registerContentObserver(uri, true, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                fillData();
            }
        });
        super.onResume();
    }

    private void initListener() {
        mLockLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int i, long l) {
                TranslateAnimation ta=new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,
                        Animation.RELATIVE_TO_SELF,-1.0f,Animation.RELATIVE_TO_SELF,0,
                        Animation.RELATIVE_TO_SELF,0);
                ta.setDuration(300);
                view.startAnimation(ta);
                new Thread(){
                    public void run(){
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dao.delete(mLockApps.get(i).packageName);
                                mLockApps.remove(i);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }.start();
            }
        });
    }

    private void fillData() {
        final List<AppInfo> aInfos = new ArrayList<AppInfo>();
        new Thread(){
            public void run(){
                for (AppInfo appInfo:appInfos){
                    if (dao.find(appInfo.packageName)){
                        appInfo.isLock=true;
                        aInfos.add(appInfo);
                    }
                }
                Message msg=new Message();
                msg.obj=aInfos;
                msg.what=10;
                mHandler.sendMessage(msg);
            }
        }.start();
    }
}