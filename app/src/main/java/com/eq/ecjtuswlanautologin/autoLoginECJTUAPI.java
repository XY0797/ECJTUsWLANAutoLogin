package com.eq.ecjtuswlanautologin;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class autoLoginECJTUAPI {
    public String login(String studentID,String passwordECJTU,int theISP){
        if(studentID.equals("")){
            return "E3 您没有填写学号！";
        }
        if(passwordECJTU.equals("")){
            return "E3 您没有填写密码！";
        }
        //运营商取值   1：电信 2：移动 3：联通
        String str_theISP;
        if (theISP == 1) {
            str_theISP = "telecom";
        } else if (theISP == 2) {
            str_theISP = "cmcc";
        } else {
            str_theISP = "unicom";
        }
        Log.d(TAG, "开始创建OkHttpClient对象");
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .build();
        Log.d(TAG, "开始准备请求头");
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String postBody = "DDDDD=%2C0%2C" + studentID + "%40" + str_theISP + "&upass=" + passwordECJTU + "&R1=0&R2=0&R3=0&R6=0&para=00&0MKKey=123456&buttonClicked=&redirect_url=&err_flag=&username=&password=&user=&cmd=&Login=";
        Request request = new Request.Builder()
                .url("http://172.16.2.100:801/eportal/?c=ACSetting&a=Login&protocol=http:&hostname=172.16.2.100&iTermType=1&wlanacip=null&wlanacname=null&mac=00-00-00-00-00-00&enAdvert=0&queryACIP=0&loginMethod=1")
                .post(RequestBody.create(mediaType,postBody))
                .build();
        Log.d(TAG, "开始发送请求");
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            Headers headers = response.headers();
            String location = headers.get("Location");
            if (location != null) {
                if (!location.contains("RetCode=")) {
                    //成功了
                    return "登录完成";
                }
                //失败了，解析错误原因
                int startIndex = location.indexOf("RetCode=") + 8;
                int endIndex = location.indexOf("&", startIndex);
                if (startIndex >= 0 && endIndex >= 0) {
                    String extractedText = location.substring(startIndex, endIndex);
                    switch (extractedText) {
                        case "userid error1":
                            return "E3 账号不存在(或未绑定宽带账号或运营商选择有误)";
                        case "userid error2":
                            return "E3 密码错误";
                        case "512":
                            return "E3 AC认证失败(重复登录之类的)";
                        case "Rad:Oppp error: Limit Users Err":
                            return "E3 超出校园网设备数量限制";
                        default:
                            Log.d(TAG, "登录未知错误码：" + extractedText);
                            return "E4 登录失败：\n未知错误，访问" + location + "查看详情";
                    }
                }
                Log.d(TAG, "错误码为空："+headers);
                return "E2 无法解析回包数据："+headers;
            }
            Log.d(TAG, "登录返回头找不到重定向字段："+headers);
            return "E1 无法解析回包数据："+headers;
        } catch (IOException e) {
            Log.d(TAG, "登录时捕获到异常："+e);
            return "E0 发送登录请求失败，捕获到异常："+e;
        }
    }
    public int getState(){
        //返回值：1：没有联网 2：连接的不是校园网 3：连接了校园网但是没有登录 4：连接了校园网并且已经登录
        Log.d(TAG, "开始创建OkHttpClient对象");
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Log.d(TAG, "开始准备请求头");
        Request request = new Request.Builder()
                .url("http://172.16.2.100")
                .get()
                .build();
        Log.d(TAG, "开始发送请求");
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if(response.code()==200){
                //成功
                Log.d(TAG, "get请求成功");
                if (response.body().string().contains("<title>注销页</title>")) {
                    //登录了
                    return 4;
                } else {
                    //没有登录
                    return 3;
                }

            }else{
                //失败
                Log.d(TAG, "奇怪的http状态码："+response.code()+"\n"+response.headers().toString()+"\n"+response.body().string());
            }
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                return 2; //超时，说明连接的不是校园网
            } else if (e instanceof ConnectException) {
                return 1; //没有网络连接
            }
            Log.d(TAG, "奇怪的异常捕获："+e);
        }
        return 2;
    }

}
