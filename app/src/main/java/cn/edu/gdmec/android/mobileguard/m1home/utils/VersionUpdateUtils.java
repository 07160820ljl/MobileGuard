/*
package cn.edu.gdmec.android.mobileguard.m1home.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.HomeActivity;
import cn.edu.gdmec.android.mobileguard.m1home.entity.VersionEntity;


*/
/**
 * Created by as on 2017/9/12.
 *//*


public class VersionUpdateUtils {
    */
/**本地版本号 *//*

    private String mVersion;
    private Activity context;
    private VersionEntity versionEntity;

    private static final int MESSAGE_IO_ERROR = 102;
    private static final int MESSAGE_JSON_ERROR = 103;
    private static final int MESSAGE_SHOW_DIALOG = 104;
    private static final int MESSAGE_ENTERHOME = 105;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_IO_ERROR:
                    Toast.makeText(context, "IO错误", Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_JSON_ERROR:
                    Toast.makeText(context,"JSON错误解析",Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_SHOW_DIALOG:
                    showUpdateDialog(versionEntity);
                    break;
                case MESSAGE_ENTERHOME:
                    Intent intent = new Intent(context,HomeActivity.class);
                    context.startActivity(intent);
                    context.finish();
                    break;

            }
        }
    };

    public VersionUpdateUtils(String mVersion, Activity context) {
        this.mVersion = mVersion;
        this.context = context;
    }

    */
/**
     * 获取服务器版本号
     *//*


    public void getCloudVersion()
    {

        try {
            HttpClient httpClient = new DefaultHttpClient();
            */
/*连接超时*//*

            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),5000);
            */
/*请求超时*//*

            HttpConnectionParams.setSoTimeout(httpClient.getParams(),5000);
            HttpGet httpGet = new HttpGet("http://android2017.duapp.com/updateinfo.html");
            HttpResponse execute = httpClient.execute(httpGet);

            if (execute.getStatusLine().getStatusCode()==200)
            {
                //请求和响应都成功了
                HttpEntity httpEntity =execute.getEntity();
                String result = EntityUtils.toString(httpEntity,"utf-8");
                //创建jsonObject对象
                JSONObject jsonObject = new JSONObject(result);
                versionEntity = new VersionEntity();
                versionEntity.versionCode = jsonObject.getString("code");
                versionEntity.description = jsonObject.getString("des");
                versionEntity.apkurl = jsonObject.getString("apkurl");

                if(!mVersion.equals(versionEntity.versionCode))
                {
                    handler.sendEmptyMessage(MESSAGE_SHOW_DIALOG);
                }

            }
        } catch (IOException e) {
            handler.sendEmptyMessage(MESSAGE_IO_ERROR);
            e.printStackTrace();
        } catch (JSONException e) {
            handler.sendEmptyMessage(MESSAGE_JSON_ERROR);
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(final VersionEntity versionEntity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("检查到有新版本:"+versionEntity.versionCode);
        builder.setMessage(versionEntity.description);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setPositiveButton("立刻升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadNewApk(versionEntity.apkurl);
            }
        });

        builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                enterHome();
            }
        });
        builder.show();

    }
    private void enterHome()
    {
        handler.sendEmptyMessage(MESSAGE_ENTERHOME);
    }

    private void downloadNewApk(String apkurl)
    {
        DownLoadUtils downLoadUtils = new DownLoadUtils();
        downLoadUtils.downloadApk(apkurl,"mobileguard.apk",context);
    }


}
*/
package cn.edu.gdmec.android.mobileguard.m1home.utils;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.HomeActivity;
import cn.edu.gdmec.android.mobileguard.m1home.entity.VersionEntity;
import android.app.Activity;
import android.app.AlertDialog;
//import android.content.DialogInterface;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.DOWNLOAD_SERVICE;


/**
 * Created by 38322 on 2017/9/16.
 */

public class VersionUpdateUtils {


    private  String mVersion;
    private Activity context;
    private  VersionEntity versionEntity;
    //广播者
    private BroadcastReceiver broadcastReceiver;
    //暂不升级跳的下个Activity
    private Class<?> nextActivity;
    //回调
    private DownloadCallback downloadCallback;
    private long downloadId;
    private  static  final int MESSAGE_IO_ERROR=102;
    private  static  final int MESSAGE_JSON_ERROR=103;
    private  static  final int MESSAGE_SHOW_DIALOG=104;
    private  static  final int MESSAGE_ENTERHOME=105;

    private Handler handler=new Handler() {
        @Override
        public  void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_IO_ERROR:
                    Toast.makeText(context,"IO错误", Toast.LENGTH_LONG).show();
                    enterHome();
                    break;
                case  MESSAGE_JSON_ERROR:
                    Toast.makeText(context,"JSON解析错误",Toast.LENGTH_LONG).show();
                    enterHome();
                    break;
                case MESSAGE_SHOW_DIALOG:
                    showUpdateDialog(versionEntity);
                    break;

                case MESSAGE_ENTERHOME:
                   /* Intent intent=new Intent(context, HomeActivity.class);
                    context.startActivity(intent);
                    context.finish();;
                    break;*/
                    if(nextActivity!=null) {
                        Intent intent = new Intent(context, nextActivity);
                        context.startActivity(intent);
                        context.finish();
                    }
                    break;
            }
        }
    };
    /*public VersionUpdateUtils(String mVersion,Activity context){
        this.mVersion=mVersion;
        this.context=context;
    }*/
    public VersionUpdateUtils(String mVersion, Activity context,DownloadCallback downloadCallback,Class<?> nextActivity) {
        this.mVersion = mVersion;
        this.context = context;
        //新改动，构造参数
        this.nextActivity = nextActivity;
        //构造参数
        this.downloadCallback = downloadCallback;
    }
    public void getCloudVersion(String url){
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 5000);
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), 5000);
            //"http://android2017.duapp.com/updateinfo.html"
            //新改动
            HttpGet httpGet = new HttpGet(url);
            HttpResponse execute = httpClient.execute(httpGet);
            if (execute.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = execute.getEntity();
                String result = EntityUtils.toString(httpEntity, "utf-8");
                JSONObject jsonObject = new JSONObject(result);
                versionEntity = new VersionEntity();
                versionEntity.versionCode = jsonObject.getString("code");
                versionEntity.description = jsonObject.getString("des");
                versionEntity.apkurl = jsonObject.getString("apkurl");
                if (!mVersion.equals(versionEntity.versionCode)) {
                    handler.sendEmptyMessage(MESSAGE_SHOW_DIALOG);
                }
            }
        }catch (IOException e){
            handler.sendEmptyMessage(MESSAGE_IO_ERROR);
        }catch (JSONException e){
            handler.sendEmptyMessage(MESSAGE_JSON_ERROR);
        }
    }
    private void showUpdateDialog(final VersionEntity versionEntity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("检查到新版本："+versionEntity.versionCode);
        builder.setMessage(versionEntity.description);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("立刻升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadNewApk(versionEntity.apkurl);
                enterHome();
            }
        });
        builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                enterHome();
            }
        });
        builder.show();
    }
    private void enterHome(){
        handler.sendEmptyMessageDelayed(MESSAGE_ENTERHOME,2000);
    }
    private void downloadNewApk(String apkurl){
        DownLoadUtils downloadUtils = new DownLoadUtils();
        String filename = "downloadfile";
        String suffixes="avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc|apk|db";
        Pattern pat= Pattern.compile("[\\w]+[\\.]("+suffixes+")");
        Matcher mc=pat.matcher(apkurl);
        while(mc.find()){
            filename = mc.group();
        }
        downapk(apkurl, filename, context);
        //antivirus.db
        //downloadUtils.downloadApk(apkurl,"mobileguard.apk",context);
    }
    public void downapk(String url,String targetFile,Context context){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedOverRoaming(false);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir("/download/", targetFile);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);
        listener(downloadId,targetFile);
    }

    private void listener(final long Id,final String filename) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long ID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (ID == Id) {
                    Toast.makeText(context.getApplicationContext(), "下载编号:" + Id +"的"+filename+" 下载完成!", Toast.LENGTH_LONG).show();
                }
                context.unregisterReceiver(broadcastReceiver);
                downloadCallback.afterDownload(filename);
            }
        };
        context.registerReceiver(broadcastReceiver, intentFilter);

    }
    public interface DownloadCallback{
        void afterDownload(String filename);
    }
}







