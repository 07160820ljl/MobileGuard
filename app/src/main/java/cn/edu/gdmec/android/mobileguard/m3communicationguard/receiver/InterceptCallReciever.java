package cn.edu.gdmec.android.mobileguard.m3communicationguard.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import cn.edu.gdmec.android.mobileguard.m3communicationguard.db.dao.BlackNumberDao;

/**
 * Created by ASUS on 2017/10/31.
 */

public class InterceptCallReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences mSP = context.getSharedPreferences("config",Context.MODE_PRIVATE);
        boolean BlackNumStatus = mSP.getBoolean("BlackNumStatus",true);
        if(!BlackNumStatus){
            return;
        }
        BlackNumberDao dao = new BlackNumberDao(context);
        if(!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            String mIncomingNumber = "";
            TelephonyManager tManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()){
                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    if(mIncomingNumber==null){
                        return;
                    }
                    int blackContactMode = dao.getBlackContactMode(mIncomingNumber);
                    if(blackContactMode == 1 || blackContactMode == 3){
                        Uri uri = Uri.parse("content://call_log.calls");
                        context.getContentResolver().registerContentObserver(uri,true,new CallLogObserver(new Handler(),mIncomingNumber,context));
                        endCall(context);
                    }
                    break;

            }
        }

    }
    private class CallLogObserver extends ContentObserver{
        private String incomingNumber;
        private Context context;

        public CallLogObserver(Handler handler,String incomingNumber,Context context){
            super(handler);
            this.incomingNumber = incomingNumber;
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i("CallLogObserver","呼叫记录数据库的内容变化了。");
            context.getContentResolver().unregisterContentObserver(this);
            deleteCallLog(incomingNumber,context);
            super.onChange(selfChange);
        }
    }

    private void deleteCallLog(String incomingNumber, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://call_log.calls");
        Cursor cursor = resolver.query(uri,new String[]{"_id"},"number=?",new String[]{incomingNumber},"_id desc limit l");
        if(cursor.moveToNext()){
            String id = cursor.getString(0);
            resolver.delete(uri,"_id=?",new String[]{id});
        }
    }
    public void endCall(Context context){
        try{
            Class clazz = context.getClassLoader().loadClass("android.os.ServiceManager");
            Method method = clazz.getDeclaredMethod("getService",String.class);
            IBinder iBinder = (IBinder)method.invoke(null,Context.TELECOM_SERVICE);
             ITelephony itelephony = ITelephony.Stub.asInterface(iBinder);
            itelephony.endCall();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
