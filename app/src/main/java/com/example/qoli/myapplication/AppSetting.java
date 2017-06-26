package com.example.qoli.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppSetting extends AppCompatActivity {
    private static final String TAG = "AppSetting";
    private static String Hosts = "http://192.168.1.100:3002";
    private EditText address;
    private SharedPreferences settings;
    private static final String data = "DATA";
    private static final String addressField = "ADDRESS";
    private static final String settingRoomField = "ROOM";
    private static final String settingDevicesField = "DEVICES";
    private boolean isSettingReadly = false;
    private String getSettingJSON = "";
    private String settingRoom = "";
    private String settingDevices = "";
    private Toast toast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);


        settings = getSharedPreferences(data, 0);
        address = (EditText) findViewById(R.id.addressField);
        address.setText(settings.getString(addressField, ""));
        TextView room = (TextView) findViewById(R.id.roomField);
        room.setText(settings.getString(settingRoomField, "房間名稱"));
        TextView devices = (TextView) findViewById(R.id.devicesField);
        devices.setText(settings.getString(settingDevicesField, "設備列表"));
    }

    // 控件 View 的点击事件
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.SaveSetting:
                saveAction();
                break;
            case R.id.getSetting:
                getSetting();
                break;
            default:
                break;
        }
    }

    private boolean getSetting() {

        int readSettingTimes = 0;

        SharedPreferences settings = getSharedPreferences(data, 0);
        Hosts = settings.getString(addressField, "");

        if (Hosts.equals("")) {
            tellUser("伺服器不能為空");
            return false;
        }

        getSettingbyServer();

        TextView room = (TextView) findViewById(R.id.roomField);
        TextView devices = (TextView) findViewById(R.id.devicesField);
        room.setText("正在重新同步...");
        devices.setText("正在重新同步...");

        tellUser("正在讀取");

        do {
            readSettingTimes = readSettingTimes + 1;
            SystemClock.sleep(1500);
            if (readSettingTimes >= 3) {
                tellUser("讀取配置超時");
                return false;
            }
        } while (!isSettingReadly);

        Log.i(TAG, "getSettingJSON: " + getSettingJSON);

        try {
            JSONObject settingData = new JSONObject(getSettingJSON);
            settingDevices = settingData.getString("devices");
            settingRoom = settingData.getString("room");

            settings.edit()
                    .putString(settingRoomField, settingRoom)
                    .putString(settingDevicesField, settingDevices)
                    .apply();

            Log.i(TAG, "settingDevices: " + settingDevices);
            Log.i(TAG, "settingRoom: " + settingRoom);


            settings = getSharedPreferences(data, 0);

            room.setText(settings.getString(settingRoomField, "房間名稱"));
            devices.setText(settings.getString(settingDevicesField, "設備列表"));

            tellUser("讀取成功");

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tellUser("讀取失敗");
        return false;
    }

    private void saveAction() {
        settings = getSharedPreferences(data, 0);

        String text = address.getText().toString();

        if (checkAddress(text)) {

            settings.edit()
                    .putString(addressField, text)
                    .apply();

            tellUser(getString(R.string.saveDone));
        } else {
            tellUser(getString(R.string.saveAddressErrorText));
        }


    }

    private void tellUser(String s) {
        if (toast != null) toast.cancel();

        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private boolean checkAddress(String Address) {
        Pattern pattern = Pattern.compile("((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?");
        Matcher matcher = pattern.matcher(Address);
        return matcher.matches();
    }

    private String getSettingbyServer() {

        Thread readConfig = new Thread(new Runnable() {
            public void run() {
                System.out.println("Server Sync ... ");
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(Hosts + "/getSetting");

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("connection", "Keep-Alive");
                    urlConnection.setRequestProperty("user-agent", "HomeKitProxy/1.0 (Android)");

                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    getSettingJSON = sb.toString();
                    isSettingReadly = true;

                    Log.i(TAG, "run: " + getSettingJSON);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            }
        });
        readConfig.start();

        return null;

    }


}

