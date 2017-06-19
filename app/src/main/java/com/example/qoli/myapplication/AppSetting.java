package com.example.qoli.myapplication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AppSetting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);
    }

    // 控件 View 的点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.SaveSetting:
                saveAction();
                break;
            default:
                break;
        }
    }

    private void saveAction() {
        tellUser("保存成功");
    }

    private void tellUser(String s) {
        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
