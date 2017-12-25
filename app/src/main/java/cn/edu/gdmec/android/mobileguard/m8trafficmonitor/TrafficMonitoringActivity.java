/*
package cn.edu.gdmec.android.mobileguard.m8trafficmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.db.dao.TrafficDao;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.service.TrafficMonitoringService;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.utils.SystemInfoUtils;

public class TrafficMonitoringActivity extends AppCompatActivity implements View.OnClickListener{

    private SharedPreferences mSP;
    private Button mCorrectFlowBtn;
    private TextView mTotalTV;
    private TextView mUsedTV;
    private TextView mToDayTV;
    private TrafficDao dao;
    private ImageView mRemindIMGV;
    private TextView mRemindTV;
    private CorrectFlowReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitoring);
        mSP = getSharedPreferences("config", MODE_PRIVATE);
        boolean flag = mSP.getBoolean("isset_operator", false);

        if (!flag){
            startActivity(new Intent(this, OperatorSetActivity.class));
            finish();
        }
        if (!SystemInfoUtils
                .isServiceRunning(this,
                        "cn.edu.gdmec.android.mobileguard.m8trafficmonitor.service.TrafficMonitoringService")){
            startService(new Intent(this, TrafficMonitoringService.class));
        }
        initView();
        registReceiver();
        initDate();
    }
    private void initView(){
        findViewById(R.id.rl_titlebar).setBackgroundColor(
                getResources().getColor(R.color.light_green));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("流量监控");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        ImageView mRightImgv = (ImageView) findViewById(R.id.imgv_rightbtn);
        mRightImgv.setImageResource(R.drawable.processmanager_setting_icon);
        mRightImgv.setOnClickListener(this);
        mCorrectFlowBtn = (Button) findViewById(R.id.btn_correction_flow);
        mCorrectFlowBtn.setOnClickListener(this);
        mTotalTV = (TextView) findViewById(R.id.tv_month_totalgprs);
        mUsedTV = (TextView) findViewById(R.id.tv_month_usedgprs);
        mToDayTV = (TextView) findViewById(R.id.tv_today_gprs);
        mRemindIMGV = (ImageView) findViewById(R.id.imgv_traffic_remind);
        mRemindTV = (TextView) findViewById(R.id.tv_traffic_remind);
    }

    private void initDate(){
        long totalflow = mSP.getLong("totalflow", 0 );
        long usedflow = mSP.getLong("usedflow", 0);
        if (totalflow > 0 & usedflow >= 0){
            float scale = usedflow / totalflow;
            if (scale > 0.9){
                mRemindIMGV.setEnabled(false);
                mRemindTV.setText("您的套餐流量即将用完!");
            }else {
                mRemindIMGV.setEnabled(true);
                mRemindTV.setText("本月流量充足请放心使用");
            }
        }
        mTotalTV.setText("本月流量：" + Formatter.formatFileSize(this, totalflow));
        mUsedTV.setText("本月已用：" + Formatter.formatFileSize(this, usedflow));
        dao = new TrafficDao(this);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dataString = sdf.format(date);
        long moblieGPRS = dao.getMoblieGPRS(dataString);
        if (moblieGPRS < 0){
            moblieGPRS = 0;
        }
        mToDayTV.setText("本日已用：" + Formatter.formatFileSize(this, moblieGPRS));
    }

    private void registReceiver(){
        receiver = new CorrectFlowReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.imgv_rightbtn:
                startActivity(new Intent(this,OperatorSetActivity.class));
                break;
            case R.id.btn_correction_flow:

                int i = mSP.getInt("operator", 0);
                SmsManager smsManager = SmsManager.getDefault();
                switch (i){
                    case 0:

                        Toast.makeText(this, "您还没有设置运营商信息", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:

                        smsManager.sendTextMessage("10086", null, "CXLL", null, null);
                        break;
                    case 2:

                        break;
                    case 3:

                        break;
                }
        }
    }

    class CorrectFlowReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            Object[] objs = (Object[]) intent.getExtras().get("pdus");
            for (Object obj : objs){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                String body = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();

                if (!address.equals("10086")){
                    return;
                }
                String[] split = body.split(", ");
                System.out.println(split[0]);

                long left = 0;

                long used = 0;

                long beyond = 0;
                for (int i=0; i < split.length; i++){
                    if (split[i].contains("当月常用流量已用")){

                        String usedflow = split[i].substring(9,
                                split[i].length());
                        used = getStringTofloat(usedflow);
                    }else if (split[i].contains("可用")){
                        String leftflow = split[i].substring(3,
                                split[i].length());
                        left = getStringTofloat(leftflow);
                    }else if (split[i].contains("套餐外流量")){
                        String beyondflow = split[i].substring(6,
                                split[i].length());
                        beyond = getStringTofloat(beyondflow);
                    }
                }
                SharedPreferences.Editor edit = mSP.edit();
                System.out.println("-----"+left);
                edit.putLong("totalflow", used + left);
                edit.putLong("usedflow", used + beyond);
                edit.commit();
                mTotalTV.setText("本月流量："
                        + Formatter.formatFileSize(context, (used + left)));
                mUsedTV.setText("本月已用："
                        + Formatter.formatFileSize(context, (used + beyond)));
            }
        }
    }

    private long getStringTofloat(String str){
        long flow = 0;
        if (!TextUtils.isEmpty(str)){
            if (str.contains("K")){
                String[] split = str.split("K");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024);
            }else if (str.contains("M")){
                String[] split = str.split("M");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024 * 1024);
            }else if (str.contains("G")){
                String[] split = str.split("G");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024 * 1024 * 1024);
            }
        }
        return flow;
    }
    @Override
    public void onDestroy(){
        if (receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
*/
package cn.edu.gdmec.android.mobileguard.m8trafficmonitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.db.dao.TrafficDao;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.service.TrafficMonitoringService;
import cn.edu.gdmec.android.mobileguard.m8trafficmonitor.utils.SystemInfoUtils;

public class TrafficMonitoringActivity extends AppCompatActivity implements View.OnClickListener{
    private SharedPreferences mSP;
    private Button mCorrectFlowBtn;
    private TextView mTotalTV;
    private TextView mUsedTV;
    private TextView mToDayTV;
    //    private TextView mOperatorTV;
    private TrafficDao dao;
    private ImageView mRemindIMGV;
    private TextView mRemindTV;
    private CorrectFlowReceiver receiver;
    private TrafficMonitoringService trafficMonitoringService =null;
    private boolean isBound;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isBound = true;
            TrafficMonitoringService.MyBinder binder = (TrafficMonitoringService.MyBinder) iBinder;
            trafficMonitoringService = binder.getService();
            trafficMonitoringService.getUsedFlow();
            System.out.println("Usedflow:"+trafficMonitoringService.getUsedFlow());
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic_monitoring);
        mSP = getSharedPreferences("config", MODE_PRIVATE);
        boolean flag = mSP.getBoolean("isset_operator", false);
        // 如果没有设置运营商信息则进入信息设置页面
        if (!flag) {
            startActivity(new Intent(this, OperatorSetActivity.class));
            finish();
        }
        if (!SystemInfoUtils
                .isServiceRunning(this,
                        "cn.edu.gdmec.android.mobileguard.m8trafficmonitor.service.TrafficMonitoringService")) {
            startService(new Intent(this, TrafficMonitoringService.class));
        }
        bindService(new Intent(this, TrafficMonitoringService.class),conn,BIND_AUTO_CREATE);
        initView();
        registReceiver();
        initData();
    }
    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(
                getResources().getColor(R.color.light_green));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("流量监控");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        ImageView mRightImgv = (ImageView) findViewById(R.id.imgv_rightbtn);
        mRightImgv.setImageResource(R.drawable.processmanager_setting_icon);
        mRightImgv.setOnClickListener(this);
        mCorrectFlowBtn = (Button) findViewById(R.id.btn_correction_flow);
        mCorrectFlowBtn.setOnClickListener(this);
        //Operator
//        mOperatorTV = (TextView) findViewById(R.id.tv_operators);
        mTotalTV = (TextView) findViewById(R.id.tv_month_totalgprs);
        mUsedTV = (TextView) findViewById(R.id.tv_month_usedgprs);
        mToDayTV = (TextView) findViewById(R.id.tv_today_gprs);
        mRemindIMGV = (ImageView) findViewById(R.id.imgv_traffic_remind);
        mRemindTV = (TextView) findViewById(R.id.tv_traffic_remind);
    }

    private void initData() {
        long totalflow = mSP.getLong("totalflow", 0);
        long usedflow = mSP.getLong("usedflow", 0);
        if (totalflow > 0 & usedflow >= 0) {
            float scale = usedflow / totalflow;
            if (scale > 0.9) {
                mRemindIMGV.setEnabled(false);
                mRemindTV.setText("您的套餐流量即将用完！");
            } else {
                mRemindIMGV.setEnabled(true);
                mRemindTV.setText("本月流量充足请放心使用");
            }
        }
        mTotalTV.setText("本月流量：" + Formatter.formatFileSize(this, totalflow));
        //mOperatorTV.setText("运营商是："+Formatter.formatFileSize());
        mUsedTV.setText("本月已用：" + Formatter.formatFileSize(this, usedflow));
        dao = new TrafficDao(this);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dataString = sdf.format(date);
        long moblieGPRS = dao.getMoblieGPRS(dataString);
        if (moblieGPRS < 0) {
            moblieGPRS = 0;
        }
        mToDayTV.setText("本日已用：" + Formatter.formatFileSize(this, moblieGPRS));
    }

    private void registReceiver() {
        receiver = new CorrectFlowReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.imgv_rightbtn:
                startActivity(new Intent(this,OperatorSetActivity.class));
                break;
            case R.id.btn_correction_flow:
                // 首先判断是哪个运营商，
                int i = mSP.getInt("operator", 0);
                SmsManager smsManager = SmsManager.getDefault();
                switch (i) {
                    case 0:
                        // 没有设置运营商
                        Toast.makeText(this, "您还没有设置运营商信息", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // 中国移动
                        // 发送cxll至10086
                        // 获取系统默认的短信管理器
                        smsManager.sendTextMessage("10086", null, "CXLL", null, null);
                        break;
                    case 2:
                        smsManager.sendTextMessage("10010", null, "CXLL", null, null);
                        // 中国联通
                        break;
                    case 3:
                        //smsManager.sendTextMessage("10001",null,"108",null,null);
                        // 中国电信
                        break;
                }
        }
    }

    class CorrectFlowReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] objs = (Object[]) intent.getExtras().get("pdus");
            for (Object obj : objs) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
                String body = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();
                // 以下短信分割只针对中国移动用户
                if (!address.equals("10086")) {
//                    String[] split = body.split("，");
//                    System.out.println(split[0]);
//                    // 本月剩余流量
//                    long left = 0;
//                    // 本月已用流量
//                    long used = 0;
//                    // 本月超出流量
//                    long beyond = 0;
//                    for (int i = 0; i < split.length; i++) {
//                        if (split[i].contains("本月总流量已用")) {
//                            // 套餐总量
//                            String usedflow = split[i].substring(9,
//                                    split[i].length());
//                            used = getStringTofloat(usedflow);
//                        } else if (split[i].contains("剩余")) {
//                            String leftflow = split[i].substring(3,
//                                    split[i].length());
//                            left = getStringTofloat(leftflow);
//                        } else if (split[i].contains("套餐外流量")) {
//                            String beyondflow = split[i].substring(6,
//                                    split[i].length());
//                            beyond = getStringTofloat(beyondflow);
//                        }
//                    }
//                    SharedPreferences.Editor edit = mSP.edit();
//                    System.out.println("-----"+left);
//                    edit.putLong("totalflow", used + left);
//                    edit.putLong("usedflow", used + beyond);
//                    edit.commit();
//                    //mOperatorTV.setText("运营商是：中国联通");
//                    mTotalTV.setText("本月流量："
//                            + Formatter.formatFileSize(context, (used + left)));
//                    mUsedTV.setText("本月已用："
//                            + Formatter.formatFileSize(context, (used + beyond)));
                    return;
                }
                String[] split = body.split("，");
                System.out.println(split[0]);
                // 本月剩余流量
                long left = 0;
                // 本月已用流量
                long used = 0;
                // 本月超出流量
                long beyond = 0;
                for (int i = 0; i < split.length; i++) {
                    if (split[i].contains("当月常用流量已用")) {
                        // 套餐总量
                        String usedflow = split[i].substring(9,
                                split[i].length());
                        used = getStringTofloat(usedflow);
                    } else if (split[i].contains("可用")) {
                        String leftflow = split[i].substring(3,
                                split[i].length());
                        left = getStringTofloat(leftflow);
                    } else if (split[i].contains("套餐外流量")) {
                        String beyondflow = split[i].substring(6,
                                split[i].length());
                        beyond = getStringTofloat(beyondflow);
                    }
                }
                SharedPreferences.Editor edit = mSP.edit();
                System.out.println("-----"+left);
                edit.putLong("totalflow", used + left);
                edit.putLong("usedflow", used + beyond);
                edit.commit();
                //mOperatorTV.setText("运营商是：中国移动");
                mTotalTV.setText("本月流量："
                        + Formatter.formatFileSize(context, (used + left)));
                mUsedTV.setText("本月已用："
                        + Formatter.formatFileSize(context, (used + beyond)));
            }
        }
    }
    /** 将字符串转化成Float类型数据 **/
    private long getStringTofloat(String str) {
        long flow = 0;
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("K")) {
                String[] split = str.split("K");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024);
            } else if (str.contains("M")) {
                String[] split = str.split("M");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024 * 1024);
            } else if (str.contains("G")) {
                String[] split = str.split("G");
                float m = Float.parseFloat(split[0]);
                flow = (long) (m * 1024 * 1024 * 1024);
            }
        }
        return flow;
    }
    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
        unbindService(conn);
    }
}