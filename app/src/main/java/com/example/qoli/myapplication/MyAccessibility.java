package com.example.qoli.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MyAccessibility extends AccessibilityService {
    private static final String TAG = "MyAccessibility";
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
                mSocket.emit("android", "onAccessibilityEvent ONLINE");
            }

        }).on("update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                wakeAndUnlock(true);
                Log.i(TAG, "> Call: android action.");
                JSONObject obj = (JSONObject)args[0];
                try {
                    Log.i(TAG, "updateDevice: "+obj.get("updateDevice"));
                    Log.i(TAG, "status: "+obj.get("status"));

                    boolean onView = gotoView("AndroidAPI");

                    if (onView) {
                        nodeAction(obj.get("updateDevice").toString(),obj.get("status").toString());
                    } else {
                        Log.i(TAG, "> Call: No");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                wakeAndUnlock(false);
            }
        });

        mSocket.connect();
    }


    //锁屏、唤醒相关
    private KeyguardManager  km;
    private KeyguardLock kl;
    private PowerManager pm;
    private PowerManager.WakeLock wl;

    private void wakeAndUnlock(boolean b) {
        if(b) {
            //获取电源管理器对象
            pm=(PowerManager) getSystemService(Context.POWER_SERVICE);

            //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

            //点亮屏幕
            wl.acquire();

            //得到键盘锁管理器对象
            km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            kl = km.newKeyguardLock("unLock");

            //解锁
            kl.disableKeyguard();
        }
        else {
            //锁屏
            kl.reenableKeyguard();

            //释放wakeLock，关灯
            wl.release();
        }

    }


    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "config success!");
        startAPP("com.xiaomi.smarthome");
        tellUser("MiHomePlus already.");
        initSocketHttp();
    }

    /*
     * 启动一个app
     */
    public void startAPP(String appPackageName) {
        try {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage(appPackageName);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "没有安装", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            preProcess(event);
            Log.i(TAG, "// : onAccessibilityEvent");
        }


    }

    /*
     * 處理……
     */
    private void preProcess(AccessibilityEvent event) {

        int nextNumber = 0;

        if (event.getSource() != null) {
            nextNumber = nextNumber + 1;
        }

        if (event.getPackageName().equals("com.xiaomi.smarthome")) {
            nextNumber = nextNumber + 1;
        }

        if (nextNumber == 2) {

            boolean onView = gotoView("AndroidAPI");

            if (onView) {
                tellUser("MiHomeKit");
                // TODO 改進為配置檔形式
                nodeAction("空調伴侶","read");
                nodeAction("電腦燈","read");
                nodeAction("落地燈","read");
            } else {
                tellUser("< Mi >");
            }

        }

    }

    private boolean gotoView(String lookingTitle) {
        // TODO 如果 app 沒有在前台需要兩次發送才成功

        AccessibilityNodeInfo source = getRootInActiveWindow();

        if (!source.getPackageName().equals("com.xiaomi.smarthome")) {
            startAPP("com.xiaomi.smarthome");
        }

        List < AccessibilityNodeInfo > viewTitle = source.findAccessibilityNodeInfosByViewId("com.xiaomi.smarthome:id/module_a_2_more_title");

        if (!titleCheck(lookingTitle, viewTitle)) {
            List < AccessibilityNodeInfo > menuBtn = source.findAccessibilityNodeInfosByViewId("com.xiaomi.smarthome:id/drawer_btn");
            doClick(menuBtn);
            List < AccessibilityNodeInfo > backBtn = source.findAccessibilityNodeInfosByViewId("com.xiaomi.plugseat:id/title_bar_return");
            doClick(backBtn);
            List < AccessibilityNodeInfo > apiBtn = getRootInActiveWindow().findAccessibilityNodeInfosByText(lookingTitle);
            if (apiBtn != null)
                for (AccessibilityNodeInfo n: apiBtn) {
                    n.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            return false;
        } else {
            return true;
        }


    }

    private boolean titleCheck(String title, List < AccessibilityNodeInfo > viewTitle) {

        if (viewTitle != null && !viewTitle.isEmpty()) {
            AccessibilityNodeInfo node;
            for (int i = 0; i < viewTitle.size(); i++) {
                node = viewTitle.get(i);
                Log.i(TAG, "> Title Check: " + title + " / " + node.getText() + ", index: " + i);

                Pattern pattern = Pattern.compile("^" + title + ".*");
                Matcher matcher = pattern.matcher(node.getText());
                if (matcher.matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void doClick(List < AccessibilityNodeInfo > infos) {
        if (infos != null)
            for (AccessibilityNodeInfo info: infos) {
                if (info.isEnabled() && info.isClickable()) {
                    Log.i(TAG, "> doClick: " + info.getText());
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            }
    }


    private void nodeAction(String lookingName,String action) {

        // 查找基於關鍵字的設備
        List < AccessibilityNodeInfo > looking = getRootInActiveWindow().findAccessibilityNodeInfosByText(lookingName);

        if (looking != null && !looking.isEmpty()) {

            AccessibilityNodeInfo node;
            Log.i(TAG, "> " + lookingName + " now. Search Total: " + looking.size());

            for (int i = 0; i < looking.size(); i++) {
                node = looking.get(i);

                // 查找設備狀態
                List < AccessibilityNodeInfo > parent = node.getParent().findAccessibilityNodeInfosByViewId("com.xiaomi.smarthome:id/info_value");

                if (parent != null && !parent.isEmpty()) {

                    AccessibilityNodeInfo nodeParent;
                    for (int j = 0; j < parent.size(); j++) {
                        nodeParent = parent.get(j);
                        Log.i(TAG, node.getText() + " 狀態: " + nodeParent.getText() + " 操作: "+action);

                        // 點擊或者讀取按鈕
                        if (action.equals("read")) {
                            sync(node.getText().toString(),nodeParent.getText().toString());
                        } else {
                            if (!nodeParent.getText().toString().equals(action)) {
                                nodeParent.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                sync(node.getText().toString(),nodeParent.getText().toString());
                            }
                        }

                    }
                }

                if (i == 0) {
                    break;
                }

            }
        }

    }

    private boolean sync(final String name, final String status) {

        Thread t1 = new Thread(new Runnable(){
            public void run(){
                System.out.println("> Server Sync ...");

                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    // TODO URL 封裝為全局函數，配置檔
                    url = new URL("http://192.168.1.104:3002/sync/"+URLEncoder.encode(name, "UTF-8")+"/"+URLEncoder.encode(status, "UTF-8"));

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestProperty("accept", "*/*");
                    urlConnection.setRequestProperty("connection", "Keep-Alive");
                    urlConnection.setRequestProperty("user-agent", "HomeKitProxy/1.0 (Android)");

                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    for (;(line = in.readLine()) != null;){
                        System.out.println("> Server Sync ... " + line);
                    }

                    System.out.println("> Server Sync ... end");
                    Log.i(TAG, "> Server Sync: "+ name + " " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            }
        });
        t1.start();


        return false;

    }

    private void tellUser(String s) {
        Context context = getApplicationContext();
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onInterrupt() {}

}


