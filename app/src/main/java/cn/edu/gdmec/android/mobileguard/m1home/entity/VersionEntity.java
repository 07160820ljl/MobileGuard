package cn.edu.gdmec.android.mobileguard.m1home.entity;

import android.app.Activity;
import android.net.http.HttpResponseCache;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.edu.gdmec.android.mobileguard.m1home.utils.DownLoadUtils;
import cn.edu.gdmec.android.mobileguard.m1home.utils.VersionUpdateUtils;

/**
 * Created by ASUS on 2017/9/16.
 */

public class VersionEntity {
    public String versioncode;
    public String description;
    public String apkurl;
}
