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


/**
 * Created by as on 2017/9/12.
 */

public class VersionUpdateUtils {
    /**本地版本号 */
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

    /**
     * 获取服务器版本号
     */

    public void getCloudVersion()
    {

        try {
            HttpClient httpClient = new DefaultHttpClient();
            /*连接超时*/
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),5000);
            /*请求超时*/
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
