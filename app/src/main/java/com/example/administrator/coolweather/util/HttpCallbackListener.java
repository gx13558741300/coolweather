package com.example.administrator.coolweather.util;


public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);
}
