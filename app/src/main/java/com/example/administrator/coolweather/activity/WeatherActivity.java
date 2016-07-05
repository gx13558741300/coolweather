package com.example.administrator.coolweather.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.administrator.coolweather.R;
import com.example.administrator.coolweather.service.AutoUpdateService;
import com.example.administrator.coolweather.util.HttpCallbackListener;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;


public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温1
     */
    private TextView tempText;
    /**
     * 用于显示气温2
     */
    private TextView temp2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button swichCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;
    /**
     * 用于显示每小时剩余访问数
     */
    private TextView countsText;
    /**
     * 用于切换是否开启后台自动更新天气服务
     */
    private Switch aSwitch;

    private boolean isUpdate;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.weather_layout);


        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        tempText = (TextView) findViewById(R.id.temp);
        currentDateText = (TextView) findViewById(R.id.current_date);
        countsText = (TextView) findViewById(R.id.counts);
        aSwitch = (Switch) findViewById(R.id.switch1);

        getSetting();
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeather(countyCode);
        }else {
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
        swichCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        swichCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        aSwitch.setChecked(isUpdate);
        aSwitch.setOnClickListener(this);
        if (isUpdate){
            aSwitch.setText("自动更新天气");
        }else {
            aSwitch.setText("不自动更新天气");
        }
    }

    private void getSetting(){
        SharedPreferences prefs = getSharedPreferences("setting",MODE_PRIVATE);
        isUpdate = prefs.getBoolean("isUpdate",false);
    }
    /**
     * 查询省级代号对应的天气代号
     */
    private void queryWeatherCode(String countyCode){
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address,"countyCode");
    }

    /**
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode){
        //该天气接口已失效，目前只是为了测试软件功能，该接口提供的数据早以不再更新了
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }


    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)){
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代码
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从服务器查询天气信息
     */
    private void queryWeather(String countyCode){
        String address = "http://api.yytianqi.com/observe?city="+ countyCode +"&key=3oie29468wabgdo8";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleNewWeatherResponse(WeatherActivity.this,response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("cityName",""));
        tempText.setText(prefs.getString("temp",""));
        weatherDespText.setText(prefs.getString("weatherDesp",""));
        publishText.setText("今天" + prefs.getString("lastUpdateTime","") +"发布");
        currentDateText.setText(prefs.getString("current_date",""));
        countsText.setText(prefs.getString("counts",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("cityId","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeather(weatherCode);
                }
                break;
            case R.id.switch1:
                isUpdate = !isUpdate;
                aSwitch.setChecked(isUpdate);
                SharedPreferences.Editor editor = getSharedPreferences("setting",MODE_PRIVATE).edit();
                editor.putBoolean("isUpdate",isUpdate);
                editor.apply();
                if (isUpdate){
                    aSwitch.setText("自动更新天气");
                    Intent intentService = new Intent(this, AutoUpdateService.class);
                    startService(intentService);
                }else {
                    aSwitch.setText("不自动更新天气");
                }
                break;
            default:
                break;
        }
    }
}
