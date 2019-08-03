package com.mt.mapdemo;

import android.app.Application;

import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.URLConnectionNetworkExecutor;
import com.yanzhenjie.nohttp.cache.DBCacheStore;
import com.yanzhenjie.nohttp.cookie.DBCookieStore;

/**
 * @Author : ZSK
 * @Date : 2019/8/2
 * @Description :
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initNoHttp();
    }

    private void initNoHttp() {
        Logger.setDebug(true);
        Logger.setTag("photoFile");
        // 如果你需要自定义配置：
        InitializationConfig config = InitializationConfig.newBuilder(this)
                // 全局连接服务器超时时间，单位毫秒，默认10s。
                .connectionTimeout(30 * 1000)
                // 全局等待服务器响应超时时间，单位毫秒，默认10s。
                .readTimeout(30 * 1000)
                // 配置缓存，默认保存数据库DBCacheStore，保存到SD卡使用DiskCacheStore。
                .cacheStore(
                        // 如果不使用缓存，setEnable(false)禁用。
                        new DBCacheStore(this).setEnable(true)
                )
                // 配置Cookie，默认保存数据库DBCookieStore，开发者可以自己实现CookieStore接口。
                .cookieStore(
                        // 如果不维护cookie，setEnable(false)禁用。
                        new DBCookieStore(this).setEnable(true)
                )
                .networkExecutor(new URLConnectionNetworkExecutor())
                .build();
        NoHttp.initialize(config);
    }
}
