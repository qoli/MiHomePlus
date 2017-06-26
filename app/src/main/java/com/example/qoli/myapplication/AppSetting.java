package com.example.qoli.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppSetting extends AppCompatActivity {
    private static final String TAG = "AppSetting";
    private EditText address;
    private SharedPreferences settings;
    private static final String data = "DATA";
    private static final String addressField = "ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        settings = getSharedPreferences(data,0);
        address = (EditText)findViewById(R.id.addressField);
        address.setText(settings.getString(addressField,""));
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
        settings = getSharedPreferences(data,0);

        String text = address.getText().toString();

        if (checkAddress(text)) {
            settings.edit()
                    .putString(addressField,text)
                    .apply();

            tellUser(getString(R.string.saveDone));
        } else {
            tellUser(getString(R.string.saveAddressErrorText));
        }


    }

    private void tellUser(String s) {
        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private boolean checkAddress(String Address) {
        Pattern pattern = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");
        Matcher matcher = pattern.matcher(Address);
        return matcher.matches();

    }
}
