package cn.edu.gdmec.android.mobileguard.m4appmanager.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;

/**
 * Created by as on 2017/11/6.
 *
 * 操纵应用的工具类
 */

public class EngineUtils {
    /**
     * 分享应用
     */
    public static void shareApplication(Context context, AppInfo appInfo){
        Intent intent = new Intent("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                "推荐您使用一款软件，名称叫："+ appInfo.appName
                        + "下载路径：https://play.google.com/store/apps/details?id="
                        + appInfo.packageName);
        context.startActivity(intent);
    }

    /**
     * 开启应用程序
     */
    public static void startApplication(Context context, AppInfo appInfo){
        //打开这个应用程序的入口Activity
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
        if (intent != null){
            context.startActivity(intent);
        }else {
            Toast.makeText(context, "该应用没有启动画面", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 开启应用设置界面
     * @param context
     * @param appInfo
     */
    public static void SettingAppDetail(Context context, AppInfo appInfo){
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + appInfo.packageName));
        context.startActivity(intent);
    }

    /**
     * 卸载应用
     */
    public static void uninstallApplication(Context context, AppInfo appInfo){
        if (appInfo.isUserApp){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + appInfo.packageName));
            context.startActivity(intent);
        }else {
            Toast.makeText(context, "系统应用无法卸载", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点击『关于』可以弹出一个对话框，显示每一个app的版本号，安装时间，apk证书签署者信息和权限申请信息
     */
    public static void showAboutDialog(Context context, AppInfo appInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("MobileGuard");	    //设置对话框标题
        builder.setMessage("Version："+appInfo.appVersion+"\nInstall time："+appInfo.installTime
                    +"\nCertificate issuer："+appInfo.certificateIssuer
                    +"\nPermissions："+appInfo.appPermissions);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序

                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();	//创建对话框
        dialog.show();

    }

}
