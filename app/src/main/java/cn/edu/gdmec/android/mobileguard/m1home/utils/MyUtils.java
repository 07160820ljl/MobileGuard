package cn.edu.gdmec.android.mobileguard.m1home.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by 38322 on 2017/9/16.
 */

public class MyUtils {

    public static  String getVersion(Context context){

        PackageManager packageManager=context.getPackageManager();

        try{
            PackageInfo packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
            return  packageInfo.versionName;

        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return  "";
        }
    }
    public static void installApk(Activity activity, String apkFile){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(
                new File(Environment.getExternalStoragePublicDirectory("/download/").getPath()+"/"+apkFile)),"application/vnd.android.package-archive");
        activity.startActivityForResult(intent,0);
    }
}



