package cn.edu.gdmec.android.mobileguard.m5virusscan.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlClient {
    public static String UrlPost(String url, String content) {
        try {
            URL mUrl = new URL(url);
            HttpURLConnection mHttpURLConnection = (HttpURLConnection) mUrl.openConnection();
            mHttpURLConnection.setConnectTimeout(15000);
            mHttpURLConnection.setReadTimeout(15000);
            mHttpURLConnection.setRequestMethod("POST");
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            mHttpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            mHttpURLConnection.setDoInput(true);
            mHttpURLConnection.setDoOutput(true);
            mHttpURLConnection.setUseCaches(false);
            mHttpURLConnection.connect();
            OutputStream out = mHttpURLConnection.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(content);
            bw.flush();
            out.close();
            bw.close();
            int respondCode = mHttpURLConnection.getResponseCode();
            if (respondCode == 200) {
                InputStream is = mHttpURLConnection.getInputStream();
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                int len = 0;
                byte buffer[] = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    message.write(buffer, 0, len);
                }
                is.close();
                message.close();
                String msg = new String(message.toByteArray());
                return msg;
            }
            return "fail";
        }catch(Exception e){
            return "error";
        }
    }
}