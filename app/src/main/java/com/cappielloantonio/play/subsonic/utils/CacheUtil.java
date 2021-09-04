package com.cappielloantonio.play.subsonic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.Interceptor;
import okhttp3.Request;

public class CacheUtil {
    private final Context context;
    private int maxAge; // 60 seconds
    private int maxStale; // 60 * 60 * 24 * 30 = 30 days (60 seconds * 60 minutes * 24 hours * 30 days)

    public CacheUtil(Context context, int maxAge, int maxStale) {
        this.context = context;
        this.maxAge = maxAge;
        this.maxStale = maxStale;
    }

    public Interceptor onlineInterceptor = chain -> {
        okhttp3.Response response = chain.proceed(chain.request());
        return response.newBuilder()
                .header("Cache-Control", "public, max-age=" + maxAge)
                .removeHeader("Pragma")
                .build();
    };

    public Interceptor offlineInterceptor = chain -> {
        Request request = chain.request();
        if (!isConnected()) {
            request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .removeHeader("Pragma")
                    .build();
        }
        return chain.proceed(request);
    };

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
