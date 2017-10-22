 package cn.edu.gdmec.android.mobileguard.m2theftguard.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m2theftguard.entity.ContactInfo;

 public class ContactAdapter extends BaseAdapter {
    private List<ContactInfo> contactInfos;
     private Context context;
     public ContactAdapter(List<ContactInfo> contactInfos,Context context){
         super();
         this.contactInfos =contactInfos;
         this.context = context;
     }
    @Override
    public int getCount() {
        return contactInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return contactInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewgroup) {
        ViewHolder holder = null;
        if(view ==null){
            view = View.inflate(context,R.layout.item_list_contact_select,null);
            holder = new ViewHolder();
            holder.mNameTV = (TextView) view.findViewById(R.id.tv_name);
            holder.mPhoneTV = (TextView) view.findViewById(R.id.tv_phone);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.mNameTV.setText(contactInfos.get(i).name);
        holder.mPhoneTV.setText(contactInfos.get(i).phone);
        return view;
    }
    static  class ViewHolder{
        TextView mNameTV;
        TextView mPhoneTV;
    }
}
