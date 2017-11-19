package cn.edu.gdmec.android.mobileguard.m5virusscan.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m5virusscan.entity.ScanAppInfo;

/**
 * Created by ASUS on 2017/11/18.
 */

public class ScanVirusAdapter extends BaseAdapter {
    private List<ScanAppInfo> mScanAppInfos;
    private Context context;
    public ScanVirusAdapter(List<ScanAppInfo> scanAppInfo,Context context){
        super();
        mScanAppInfos = scanAppInfo;
        this.context = context;
    }
    static class  ViewHolder{
        ImageView mAppIconImgv;
        TextView mAppNameTV;
        ImageView mScanIconImgv;
    }
    @Override
    public int getCount() {
        return mScanAppInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mScanAppInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       ViewHolder holder;
        if (view == null){
            view = view.inflate(context, R.layout.item_list_applock,null);
            holder = new ViewHolder();
            holder.mAppIconImgv = (ImageView) view.findViewById(R.id.imgv_appicon);
            holder.mAppNameTV = (TextView) view.findViewById(R.id.tv_appname);
            holder.mScanIconImgv = (ImageView) view.findViewById(R.id.imgv_lock);
            view.setTag(holder);
        }else{
            holder = (ViewHolder)view .getTag();
        }
        ScanAppInfo scanAppInfo = mScanAppInfos.get(i);
        if(!scanAppInfo.isVirus){
            holder.mScanIconImgv.setBackgroundResource(R.drawable.blue_right_icon);
            holder.mAppNameTV.setText(scanAppInfo.appName);
            holder.mAppNameTV.setTextColor(context.getResources().getColor(R.color.black));
        }else{
            holder.mAppNameTV.setTextColor(context.getResources().getColor(R.color.bright_red));
            holder.mAppNameTV.setText(scanAppInfo.appName+"("+scanAppInfo.description+")");
        }
        holder.mAppIconImgv.setImageDrawable(scanAppInfo.appicon);
        return view;
    }
}
