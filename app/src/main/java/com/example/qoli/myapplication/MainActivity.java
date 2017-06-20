package com.example.qoli.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Socket mSocket;

    /**
     * Socket
     */
    private void initSocketHttp() {
        try {
            // TODO URL 封裝為全局函數，配置檔
            mSocket = IO.socket("http://192.168.1.104:3002");
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mSocket.emit("android", "AndroidProxy ONLINE");
            }

        }).on("update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                Log.i(TAG, "> Call: startHomePlus()");
                startHomePlus();
            }
        });

        mSocket.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onRun");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSocketHttp();
        startHomePlus();
    }

    private void startHomePlus() {
        // Code to start the Service
        startService(new Intent(getApplication(), MyAccessibility.class));
    }


    // 控件 View 的点击事件
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.Accessibility:
                Context context = getApplicationContext();
                CharSequence text = "請激活 MiHomePlus 無障礙設定";
                int duration = Toast.LENGTH_SHORT;
                Toast.makeText(context, text, duration).show();

                //打开系统无障碍设置界面
                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibleIntent);
                break;
            case R.id.AppSetting:
                intent.setClass(MainActivity.this, AppSetting.class);
                startActivity(intent);
                break;
            case R.id.Help:
                intent.setClass(MainActivity.this, Help.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }



}