package com.example.administrator.coolweather.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.coolweather.db.CoolWeatherOpenHelper;

/**
 * Created by Administrator on 2016/6/26 0026.
 */
public class CoolWeatheDB {

    /**
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";
    /**
     * 数据库版本号
     */
    public static final int VERSION = 1;

    private static CoolWeatheDB coolWeatheDB;

    private SQLiteDatabase db;

    /**
     * 将构造方法私有化
     */
    private CoolWeatheDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 获取CoolWeatherDB的实例
     */
    public synchronized static CoolWeatheDB getInstance(Context context){
        if (coolWeatheDB == null){
            coolWeatheDB = new CoolWeatheDB(context);
        }
        return coolWeatheDB;
    }



}
