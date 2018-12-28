package com.melodigm.post;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import com.crashlytics.android.Crashlytics;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.melodigm.post.common.Constants;
import com.melodigm.post.util.LocationUtil;
import com.melodigm.post.util.LogUtil;
import com.melodigm.post.util.SPUtil;
import com.melodigm.post.util.handler.IOnHandlerMessage;
import com.melodigm.post.util.handler.WeakRefHandler;
import io.fabric.sdk.android.Fabric;
import jp.line.android.sdk.LineSdkContextManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BaseApplication extends MultiDexApplication implements IOnHandlerMessage {
    private Context mContext;
    private WeakRefHandler mHandler;
    private LocationUtil mLocationUtil;
    private Tracker mTracker;
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    /**
     * YCNOTE - java.lang.NoClassDefFoundError
     * MultiDex 를 활성화 할경우 MultiDex.install을 호출하지 않으면, 안드로이드 4.4 이하 버전에서 java.lang.NoClassDefFoundError 오류가 발생한다.
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        BaseApplication app = (BaseApplication)context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .cacheDirectory(getDir(Constants.SERVICE_MUSIC_FILE_PATH, MODE_PRIVATE))
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .build();
    }

    // 액티비티, 리시버, 서비스가 생성되기 전 애플리케이션이 생성될 때 호출된다. 모든 상태변수와 리소스 초기화한다.
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new WeakRefHandler(this);

        //Fabric.with(mContext, new Crashlytics(), new TwitterCore(authConfig), new TweetComposer());
        final Fabric fabric = new Fabric.Builder(this)
                // TODO : YCLOVE
                //.kits(new Crashlytics(), new TwitterCore(authConfig), new TweetComposer())
                .kits(new Crashlytics())
                .debuggable(false)
                .build();
        Fabric.with(fabric);

/*
        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(mContext, "fonts/Roboto-Regular.ttf"))
                .addBold(Typekit.createFromAsset(mContext, "fonts/Roboto-Bold.ttf"))
                .addCustom1(Typekit.createFromAsset(mContext, "fonts/Montserrat-Regular.ttf"));

*/
        LineSdkContextManager.initialize(this);

        /**
         * YCNOTE - DP
         * Low density (120dpi) : ldpi
         * Medium density (160dpi) : mdpi
         * High density (240dpi) : hdpi
         * Extra High density (320dpi) : xdpi
         * Extra Extra High (480dpi) : xxhdpi
         * Extra Extra Extra High (640dpi) : xxxhdpi
         *
         * ldpi : 1dp = 0.75px
         * mdpi : 1dp = 1px
         * hdpi : 1dp = 1.5px
         * xdpi : 1dp = 2px;
         * xxhdpi : 1dp = 3px;
         * xxxhdpi : 1dp = 4px;
         *
         * dp(dip)와 px간의 변환
         * px = dp * (160 / dpi) = dp * density
         * dp = px / (160 / dpi) = px / density
         *
         * 여기서 density는 density = dpi / 160 계산 한다.
         * ldpi : density = 0.75
         * mdpi : density = 1
         * hdpi : density = 1.5
         * xdpi : density = 2
        */
        /*LogUtil.e("Density : " + Float.toString(getResources().getDisplayMetrics().density));
        LogUtil.e("DensityDpi : " + Integer.toString(getResources().getDisplayMetrics().densityDpi));
        printHashKey();*/

        getLocationInfo();
    }

    public void getLocationInfo() {
        mLocationUtil = new LocationUtil(mContext, mHandler);
        if (mLocationUtil.isLocationEnabled()) {
            mLocationUtil.run();
        } else {
            SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, "");
            SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, "");
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            // 위치 서비스 조회 성공 후 Handler
            case Constants.QUERY_LOCATION_CHANGE:
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LAT, msg.getData().getString("USER_LAT", ""));
                SPUtil.setSharedPreference(mContext, Constants.SP_USER_LNG, msg.getData().getString("USER_LNG", ""));
                mLocationUtil.stopLocationUpdate();
                break;
        }
    }

    public void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(Constants.SERVICE_PACKAGE, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                LogUtil.e("KeyHash : " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LogUtil.e(e.getMessage());
        }
    }

    // 애플리케이션 객체가 종료될 때 호출되는데 항상 보증하지 않는다.
    @Override
    public void onTerminate() {
        LogUtil.e(" ");
        super.onTerminate();
    }

    // 시스템이 리소스가 부족할 때 발생한다.
    @Override
    public void onLowMemory() {
        LogUtil.e(" ");
        super.onLowMemory();
    }

    // 애플리케이션은 구성변경을 위해 재시작하지 않는다. 변경이 필요하다면 이곳에서 핸들러를 재정의 하면 된다.
    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }
}
