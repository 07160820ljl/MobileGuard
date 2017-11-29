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
            //设置链接超时时间
            mHttpURLConnection.setConnectTimeout(15000);
            //设置读取超时时间
            mHttpURLConnection.setReadTimeout(15000);
            //设置请求参数
            mHttpURLConnection.setRequestMethod("POST");
            //添加Header
            mHttpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            //设置消息的类型
            mHttpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            //接收输入流
            mHttpURLConnection.setDoInput(true);
            //传递参数时需要开启
            mHttpURLConnection.setDoOutput(true);
            //Post方式不能缓存,需手动设置为false
            mHttpURLConnection.setUseCaches(false);

            mHttpURLConnection.connect();
            //输出流，用来发送请求，http请求实际上直到这个函数里面才正式发送出去
            OutputStream out = mHttpURLConnection.getOutputStream();
            //创建字符流对象并用高效缓冲流包装它，便获得最高的效率,发送的是字符串推荐用字符流，其它数据就用字节流
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            //把json字符串写入缓冲区中
            bw.write(content);
            //刷新缓冲区，把数据发送出去，这步很重要
            bw.flush();
            out.close();
            //使用完关闭,http请求即发送完成。
            bw.close();

            // 获取代码返回值
            int respondCode = mHttpURLConnection.getResponseCode();
            //Log.d("respondCode","respondCode="+respondCode );
            // 获取返回内容类型
            String type = mHttpURLConnection.getContentType();
            //Log.d("type", "type="+type);
            // 获取返回内容的字符编码
            String encoding = mHttpURLConnection.getContentEncoding();
            //Log.d("encoding", "encoding="+encoding);
            // 获取返回内容长度，单位字节
            int length = mHttpURLConnection.getContentLength();
            //Log.d("length", "length=" + length);

            if (respondCode == 200) {
                // 获取响应的输入流对象
                InputStream is = mHttpURLConnection.getInputStream();
                // 创建字节输出流对象
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                // 定义读取的长度
                int len = 0;
                // 定义缓冲区
                byte buffer[] = new byte[1024];
                // 按照缓冲区的大小，循环读取
                while ((len = is.read(buffer)) != -1) {
                    // 根据读取的长度写入到os对象中
                    message.write(buffer, 0, len);
                }
                // 释放资源
                is.close();
                message.close();
                // 返回字符串
                String msg = new String(message.toByteArray());
                return msg;
            }
            return "fail";
        }catch(Exception e){
            return "error";
        }
    }
}
