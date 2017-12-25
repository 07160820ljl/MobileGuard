package cn.edu.gdmec.android.mobileguard.m9advancedtools;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m9advancedtools.db.dao.NumBelongtoDao;

public class NumBelongtoActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText mNumET;
    private TextView mResultTV;
    private String dbName = "address.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_num_belongto);
        initView();
        copyDB(dbName);
    }
    private void initView(){
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.bright_red));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("号码归属地查询");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        findViewById(R.id.btn_searchnumbelongto).setOnClickListener(this);
        mNumET = (EditText) findViewById(R.id.et_num_numbelongto);
        mResultTV = (TextView) findViewById(R.id.tv_searchresult);

        mNumET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString().toString().trim();
                if(string.length() == 0){
                    mResultTV.setText("");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_searchnumbelongto:
                String phonenumber = mNumET.getText().toString().trim();
                if(!TextUtils.isEmpty(phonenumber)){
                    File file = new File(getFilesDir(),dbName);
                    if(!file.exists() || file.length() <= 0){
                        copyDB(dbName);
                    }
                    String location = NumBelongtoDao.getLocation(this,phonenumber);
                    mResultTV.setText("归属地："+location);
                }else{
                    Toast.makeText(this,"请输入需要查询的号码",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void copyDB(final String dbname){
        new Thread(){
            public void run(){
                try{
                    File file = new File(getFilesDir(),dbname);
                    if(file.exists() && file.length() >0){
                        Log.i("NumBelongtoActivity","数据库已存在");
                        return;
                    }
                    InputStream is = getAssets().open(dbname);
                    FileOutputStream fos = openFileOutput(dbname,MODE_PRIVATE);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while((len = is.read(buffer)) != -1){
                        fos.write(buffer,0,len);
                    }
                    is.close();
                    fos.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            ;
        }.start();
    }
}