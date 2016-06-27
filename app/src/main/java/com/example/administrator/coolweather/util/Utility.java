package com.example.administrator.coolweather.util;


import android.text.TextUtils;

import com.example.administrator.coolweather.model.City;
import com.example.administrator.coolweather.model.CoolWeatherDB;
import com.example.administrator.coolweather.model.County;
import com.example.administrator.coolweather.model.Province;

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

}
