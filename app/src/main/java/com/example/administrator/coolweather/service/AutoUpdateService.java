package com.example.administrator.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.coolweather.receiver.AutoUpdateReceiver;
import com.example.administrator.coolweather.util.HttpCallbackListener;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;



public class AutoUpdateService extends Service {
    /**
     * 是否启动服务的开关，默认不启动
     */
    private static boolean isUpdateWeather = false;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取setting
        SharedPreferences prefs = getSharedPreferences("setting",MODE_PRIVATE);
        isUpdateWeather = prefs.getBoolean("isUpdate",false);
        Log.d("AutoUpdateService","startService");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isUpdateWeather){
                    updateWeather();
                    Log.d("AutoUpdateService","更新天气");
                }
                stopSelf();
            }
        }).start();
        if (isUpdateWeather){
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int anHour = 20 * 60 * 1000;
            long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
            Intent i = new Intent(this,AutoUpdateReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("cityId","");
        String address = " http://api.yytianqi.com/observe?city=" + weatherCode +"&key=3oie29468wabgdo8";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleNewWeatherResponse(AutoUpdateService.this,response);
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
