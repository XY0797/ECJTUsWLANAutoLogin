package com.eq.ecjtuswlanautologin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;//获取到轻量级存储器变量
    private SharedPreferences.Editor editor;//获取轻量级存储器的编辑器
    public static final String accountKEY = "学号";//定义一个键值对的键，通过这个来取值
    public static final String passwordKEY = "密码";//定义一个键值对的键，通过这个来取值
    public static final String ISPKEY = "运营商";//定义一个键值对的键，通过这个来取值
    EditText etAccount;
    EditText etPassword;
    RadioGroup rgISP;

    //用于判断是否需要覆写配置信息
    String accountValue_p,passwordValue_p;
    int ISPValue_p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取sharedPreferences实例
        sharedPreferences = getSharedPreferences("userInformation", MODE_PRIVATE);
        //获取sharedPreferences编辑器
        editor = sharedPreferences.edit();

        //获取控件
        etAccount=findViewById(R.id.editText_账号);
        etPassword=findViewById(R.id.editText_密码);
        rgISP=findViewById(R.id.radio_group);

        boolean isOKrun=true;

        //读取保存的账号
        accountValue_p = sharedPreferences.getString(accountKEY, "");
        if(accountValue_p.equals("")){isOKrun=false;}
        etAccount.setText(accountValue_p);

        //读取保存的密码
        passwordValue_p = sharedPreferences.getString(passwordKEY, "");
        if(passwordValue_p.equals("")){isOKrun=false;}
        etPassword.setText(passwordValue_p);

        //读取保存的运营商
        ISPValue_p = sharedPreferences.getInt(ISPKEY, 3);
        if (ISPValue_p == 1) {
            rgISP.check(findViewById(R.id.radio_telecom).getId());
        } else if (ISPValue_p == 2) {
            rgISP.check(findViewById(R.id.radio_cmcc).getId());
        } else {
            rgISP.check(findViewById(R.id.radio_unicom).getId());
        }
        //初始化单选框事件
        rgISP.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveUserInformation();
            }
        });
        if(isOKrun){
            Button loginButton = findViewById(R.id.button_登录);
            loginButton.performClick();
        }
    }
    public int getISPselect(){
        int ISPValue=3;
        int checkedRadioButtonId = rgISP.getCheckedRadioButtonId();
        if (checkedRadioButtonId == findViewById(R.id.radio_telecom).getId()){
            ISPValue=1;
        }else if (checkedRadioButtonId == findViewById(R.id.radio_cmcc).getId()) {
            ISPValue=2;
        }else if (checkedRadioButtonId == findViewById(R.id.radio_unicom).getId()) {
            ISPValue=3;
        }
        return ISPValue;
    }
    public void buttonSave(View view) {
        saveUserInformation();
    }
    public void saveUserInformation(){
        //实现默认参数
        saveUserInformation(true);
    }
    public void saveUserInformation(boolean showUI){
        //获取UI中的内容
        String accountValue = etAccount.getText().toString();
        String passwordValue = etPassword.getText().toString();
        int ISPValue=getISPselect();

        //用于检测是否有更改
        boolean isChange=false;
        //将编辑框中的内容写入到轻量级存储器中
        if(accountValue_p!=accountValue){
            accountValue_p=accountValue;
            editor.putString(accountKEY, accountValue);
            isChange=true;
        }
        if(passwordValue_p!=passwordValue){
            passwordValue_p=passwordValue;
            editor.putString(passwordKEY, passwordValue);
            isChange=true;
        }
        //保存运营商信息
        if(ISPValue_p!=ISPValue){
            ISPValue_p=ISPValue;
            editor.putInt(ISPKEY, ISPValue);
            isChange=true;
        }
        if (isChange){
            //有更改才需要提交
            editor.commit();
        }
        if(showUI){
            //UI提示保存完成
            Toast.makeText(MainActivity.this, "保存完成",Toast.LENGTH_SHORT).show();
        }
        //本方法结束
    }
    public void but_debug(View view){
        //保留给调试测试功能使用
    }
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
    public void doLogin(View view){
        // 获取登录按钮
        Button loginButton = (Button) view;
        // 禁用按钮
        loginButton.setEnabled(false);
        if(!isWifiConnected(this)){
            //不是wifi连接，不可能接入校园网
            //提示用户
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("检测到您没有连接wifi\n如果您的手机显示已经连接了wifi，您可能需要在登录前关闭移动数据(流量)的开关");
            builder.setPositiveButton("好的", null);
            builder.show();
            //解锁按钮
            loginButton.setEnabled(true);
            return;
        }
        if(etAccount.getText().toString().equals("")){
            //提示用户
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("您没有填写学号！");
            builder.setPositiveButton("好的", null);
            builder.show();
            //解锁按钮
            loginButton.setEnabled(true);
            return;
        }
        if(etPassword.getText().toString().equals("")){
            //提示用户
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("您没有填写密码！");
            builder.setPositiveButton("好的", null);
            builder.show();
            //解锁按钮
            loginButton.setEnabled(true);
            return;
        }
        //创建API对象
        autoLoginECJTUAPI ECJTUAPI=new autoLoginECJTUAPI();
        //获取运营商选择状态
        int ISPValue=getISPselect();
        // 在单独的线程中执行网络请求
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int state = ECJTUAPI.getState();
                String rstTxt;
                //返回值：1：没有联网 2：连接的不是校园网 3：连接了校园网但是没有登录 4：连接了校园网并且已经登录
                if(state==1){
                    //没有联网
                    rstTxt="您似乎没有网络连接";
                }else if(state==3){
                    //连接了校园网但是没有登录
                    rstTxt=ECJTUAPI.login(etAccount.getText().toString(),etPassword.getText().toString(),ISPValue);
                }else if(state==4){
                    //连接了校园网并且已经登录
                    rstTxt="您已经处于登录状态";
                }else{
                    //连接的不是校园网
                    rstTxt="您连接的wifi似乎不是校园网";
                }
                // 在网络请求完成后，使用Handler将结果返回到主线程
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //提示用户
                        if (rstTxt.startsWith("E")) {
                            //说明是错误消息
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            if(rstTxt.startsWith("E3")){
                                builder.setTitle("登录失败！");
                                builder.setMessage(rstTxt.substring(3));
                                builder.setPositiveButton("好的", null);
                            }else {
                                builder.setTitle("失败惹...");
                                builder.setMessage(rstTxt);
                                builder.setPositiveButton("截图后点我关闭", null);
                            }
                            builder.show();
                        } else {
                            //提示登录成功
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("提示");
                            builder.setMessage(rstTxt);
                            builder.setPositiveButton("好的", null);
                            builder.show();
                            saveUserInformation(false);
                        }
                        //解锁按钮
                        loginButton.setEnabled(true);
                    }
                });

            }
        });
        // 启动线程
        thread.start();
    }
}