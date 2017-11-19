package cn.edu.gdmec.android.mobileguard.m4appmanager.adapter;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m4appmanager.entity.AppInfo;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.DensityUtil;
import cn.edu.gdmec.android.mobileguard.m4appmanager.utils.EngineUtils;

/**
 * Created by as on 2017/11/6.
 */

public class AppManagerAdapter extends BaseAdapter {
    private List<AppInfo> UserAppInfos;
    private List<AppInfo> SystemAppInfos;
    private Context context;

    public AppManagerAdapter(List<AppInfo> userAppInfos, List<AppInfo> systemAppInfos, Context context) {
        UserAppInfos = userAppInfos;
        SystemAppInfos = systemAppInfos;
        this.context = context;
    }

    @Override
    public int getCount() {
        //因为有两个条目需要用于显示用户进程，系统进程因此需要加2
        return UserAppInfos.size() + SystemAppInfos.size() + 2;
    }

    @Override
    public Object getItem(int position) {
        if (position == 0){
            //第0个位置显示的应该是  用户程序的个数的标签
            return null;
        }else if (position == (UserAppInfos.size() + 1)){
            return null;
        }
        AppInfo appInfo;
        if (position < (UserAppInfos.size() +1)){
            //用户程序
            appInfo = UserAppInfos.get(position - 1); //多了一个TextView标签,位置需要减1
        }else {
            //系统程序
            int location = position - UserAppInfos.size() - 2;
            appInfo = SystemAppInfos.get(location);
        }
        return appInfo;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //如果 position 为0，则为TextView
        if (position == 0){
            TextView tv = getTextView();
            tv.setText("用户程序：" + UserAppInfos.size() + "个");
            return tv;
            //系统应用
        }else if (position == (UserAppInfos.size() + 1)){
            TextView tv = getTextView();
            tv.setText("系统程序：" + SystemAppInfos.size() + "个");
            return tv;
        }
        //获取到当前App的对象
        AppInfo appInfo;
        if (position < (UserAppInfos.size() + 1)){
            // position为0时 为TextView
            appInfo = UserAppInfos.get(position - 1);
        }else {
            //系统应用
            appInfo = SystemAppInfos.get(position - UserAppInfos.size() - 2);
        }
        ViewHolder viewHolder = null;
        if (view != null & view instanceof LinearLayout){
            viewHolder = (ViewHolder) view.getTag();
        }else {
            viewHolder = new ViewHolder();
            view = View.inflate(context, R.layout.item_appmanager_list, null);
            viewHolder.mAppIconImgv = (ImageView) view
                    .findViewById(R.id.imgv_appicon);
            viewHolder.mAppLocationTV = (TextView) view
                    .findViewById(R.id.tv_appisroom);
            viewHolder.mAppNameTV = (TextView) view
                    .findViewById(R.id.tv_appname);
            viewHolder.mAppOptionLL = (LinearLayout) view
                    .findViewById(R.id.ll_option_app);
            viewHolder.mAppSizeTV = (TextView) view
                    .findViewById(R.id.tv_appsize);
            viewHolder.mLaunchAppTV = (TextView) view
                    .findViewById(R.id.tv_launch_app);
            viewHolder.mSettingAppTV = (TextView) view
                    .findViewById(R.id.tv_setting_app);
            viewHolder.mShareAppTV = (TextView) view
                    .findViewById(R.id.tv_share_app);
            viewHolder.mUninstallTV = (TextView) view
                    .findViewById(R.id.tv_uninstall_app);
            viewHolder.mAboutAppTV = (TextView) view
                    .findViewById(R.id.tv_about_app);
            viewHolder.mActivity = (TextView) view
                    .findViewById(R.id.tv_activity_app);

            view.setTag(viewHolder);
        }
        if (appInfo != null){
            viewHolder.mAppLocationTV.setText(appInfo
                .getAppLocation(appInfo.isInRoom));
            viewHolder.mAppIconImgv.setImageDrawable(appInfo.icon);
            viewHolder.mAppSizeTV.setText(Formatter.formatFileSize(context,
                    appInfo.appSize));
            viewHolder.mAppNameTV.setText(appInfo.appName);
            if (appInfo.isSelected){
                viewHolder.mAppOptionLL.setVisibility(View.VISIBLE);
            }else {
                viewHolder.mAppOptionLL.setVisibility(View.GONE);
            }

        }
        MyClickListener listener = new MyClickListener(appInfo);
        viewHolder.mLaunchAppTV.setOnClickListener(listener);
        viewHolder.mShareAppTV.setOnClickListener(listener);
        viewHolder.mSettingAppTV.setOnClickListener(listener);
        viewHolder.mUninstallTV.setOnClickListener(listener);
        viewHolder.mAboutAppTV.setOnClickListener(listener);
        viewHolder.mActivity.setOnClickListener(listener);

        return view;
    }

    /**
     * 创建一个TextView
     * @return
     */
    private TextView getTextView(){
        TextView tv = new TextView(context);
        tv.setBackgroundColor(ContextCompat.getColor(context, R.color.graye5));
        tv.setPadding(DensityUtil.dip2px(context,5),
                DensityUtil.dip2px(context,5),
                DensityUtil.dip2px(context,5),
                DensityUtil.dip2px(context,5));
        tv.setTextColor(ContextCompat.getColor(context, R.color.black));
        return tv;
    }
    static class ViewHolder{
        /** 启动App */
        TextView mLaunchAppTV;
        /** 卸载App */
        TextView mUninstallTV;
        /** 分享App */
        TextView mShareAppTV;
        /** 设置App */
        TextView mSettingAppTV;
        /** 关于app */
        TextView mAboutAppTV;
        /** app 图标 */
        ImageView mAppIconImgv;
        /** app 位置 */
        TextView mAppLocationTV;
        /** app 大小 */
        TextView mAppSizeTV;
        /** app 名称 */
        TextView mAppNameTV;

        TextView mActivity;
        /** 操作App的线性布局 */
        LinearLayout mAppOptionLL;
    }

    class MyClickListener implements View.OnClickListener{
        private AppInfo appInfo;

        public MyClickListener(AppInfo appInfo) {
            this.appInfo = appInfo;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_launch_app:
                    //启动应用
                    EngineUtils.startApplication(context,appInfo);
                    break;
                case R.id.tv_share_app:
                    //分享应用
                    EngineUtils.shareApplication(context,appInfo);
                    break;
                case R.id.tv_setting_app:
                    //设置应用
                    EngineUtils.SettingAppDetail(context,appInfo);
                    break;
                case R.id.tv_uninstall_app:
                    //卸载应用，需要注册广播接收者
                    if (appInfo.packageName.equals(context.getPackageName())){
                        Toast.makeText(context, "您没有权限卸载此应用！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    EngineUtils.uninstallApplication(context,appInfo);
                    break;
                case R.id.tv_about_app:
                    EngineUtils.showAboutDialog(context,appInfo);
                    break;
                case R.id.tv_activity_app:
                    EngineUtils.showActivityDialog(context,appInfo);
                    break;
            }

        }
    }

}
