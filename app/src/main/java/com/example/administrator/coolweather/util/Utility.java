package com.example.administrator.coolweather.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.administrator.coolweather.model.City;
import com.example.administrator.coolweather.model.CoolWeatherDB;
import com.example.administrator.coolweather.model.County;
import com.example.administrator.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response){
        if (!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0){
                for (String p : allProvinces){
                    String [] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public  static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0){
                for (String c : allCities) {
                    String [] array = c.split("\\|");
                    City city = new City();
                    city.setProvinceId(provinceId);
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    //将解析的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理处理器返回的县级数据
     */
    public  static  boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0){
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    coolWeatherDB.saveCountry(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherResponse(Context context, String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherIfo = jsonObject.getJSONObject("weatherinfo");
            String cityNmae = weatherIfo.getString("city");
            String weatherCode = weatherIfo.getString("cityid");
            String temp1 = weatherIfo.getString("temp1");
            String temp2 = weatherIfo.getString("temp2");
            String weatherDesp = weatherIfo.getString("weather");
            String publishTime = weatherIfo.getString("ptime");
            saveWeatherInfo(context,cityNmae,weatherCode,temp1,temp2,weatherDesp,publishTime);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解析易用天气服务器返回的实时天气JSON数据，并将解析出的数据存储到本地
     */
    public static void handleNewWeatherResponse(Context context,String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            String counts = jsonObject.getString("counts");
            JSONObject weatherInfo = jsonObject.getJSONObject("data");
            String cityName = weatherInfo.getString("cityName");
            String lastUpdateTime = weatherInfo.getString("lastUpdate");
            String weatherDesp = weatherInfo.getString("tq");
            String temp = weatherInfo.getString("qw");
            String cityId = weatherInfo.getString("cityId");
            saveNewWeatherInfo(context,cityName,cityId,temp,lastUpdateTime,weatherDesp,counts);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 将易用服务器返回的所有天气信息存储到SharedPreferences文件中
     */
    public static void saveNewWeatherInfo(Context context, String cityName, String cityId, String temp,
                                          String lastUpdateTime, String weatherDesp, String counts){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("cityName",cityName);
        editor.putString("cityId",cityId);
        editor.putString("temp",temp);
        editor.putString("lastUpdateTime",lastUpdateTime);
        editor.putString("weatherDesp",weatherDesp);
        editor.putString("counts",counts);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();

    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中
     */

    public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1,
                                       String temp2, String weatherDesp,String publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("weather_code",weatherCode);
        editor.putString("city_name",cityName);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_desp",weatherDesp);
        editor.putString("publish_time",publishTime);

        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
    }
    /**
     * 解析服务器返回的JSON格式的城市信息并将其分别存放在数据库的三张表中
     */
    public static void handleCityInfo(CoolWeatherDB coolWeatherDB, String response){
       try {
           JSONObject china = new JSONObject(response);
           JSONArray provinceList = china.getJSONArray("list");
           int provinceId = 1;
           int cityId = 1;
           for (int i = 0; i < provinceList.length(); i++){
               JSONObject provinceJSON = provinceList.getJSONObject(i);

               Province province = new Province();
               province.setProvinceName(provinceJSON.getString("name"));
               province.setProvinceCode(provinceJSON.getString("city_id"));
               coolWeatherDB.saveProvince(province);

               JSONArray cityList = provinceJSON.getJSONArray("list");
               for (int j = 0; j < cityList.length(); j++){
                   JSONObject cityJSON = cityList.getJSONObject(j);
                   City city = new City();
                   city.setCityName(cityJSON.getString("name"));
                   city.setCityCode(cityJSON.getString("city_id"));
                   city.setProvinceId(provinceId);
                   coolWeatherDB.saveCity(city);
                   JSONArray countyList;
                   if (!cityJSON.isNull("list")) {
                       countyList = cityJSON.getJSONArray("list");
                       for (int x = 0; x < countyList.length(); x++){
                           JSONObject countyJSON = countyList.getJSONObject(x);
                           County county = new County();
                           county.setCountyName(countyJSON.getString("name"));
                           county.setCountyCode(countyJSON.getString("city_id"));
                           county.setCityId(cityId);
                           coolWeatherDB.saveCountry(county);
                       }
                   }
                   cityId++;
               }
               provinceId++;

           }

       }catch (Exception e) {
           e.printStackTrace();
       }
    }

}
