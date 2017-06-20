package com.example.qoli.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onRun");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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