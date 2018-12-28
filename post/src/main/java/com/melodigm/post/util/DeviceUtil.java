package com.melodigm.post.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.melodigm.post.PostActivity;
import com.melodigm.post.R;
import com.melodigm.post.common.Constants;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class DeviceUtil {

    /**
     * UUID - Universally unique identifier : 범용고유식별자
     * 기기의 ID와, 시리얼 넘버, 그리고 안드로이드 시큐리티(Security) ID 이 세 가지의 해시 값을 사용하여 UUID 생성
     * @param mContext
     * @return
     */
    public static String getDevicesUUID(Context mContext) {
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice = "" + tm.getDeviceId();
        final String tmSerial = "" + tm.getSimSerialNumber();
        final String androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }

    public static String getUniversalId(Context appContext) {
        String id = "";
        TelephonyManager telManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager.getDeviceId() == null || telManager.getDeviceId().trim().length() == 0) {
            String serialnum = null;
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class, String.class);
                serialnum = (String) (get.invoke(c, "ro.serialno", "unknown"));
            } catch (Exception ignored) {
            }
            if (serialnum.equalsIgnoreCase("unknown")) {
                id = "Z" + DeviceUtil.getAndroidId(appContext);
            } else {
                id = "Y" + serialnum;
            }
        } else {
            id = telManager.getDeviceId();
        }
        return id;
    }

    public static String getAndroidId(Context context) {
        String mAndroidId = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        if (mAndroidId == null) {
            mAndroidId = android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.System.ANDROID_ID);
        }
        return mAndroidId;
    }

    public static int getStatusBarSize(Context context) {
        int rtValue = 0;
        try {
            Rect rectgle= new Rect();
            Window window= ((Activity)context).getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
            int StatusBarHeight= rectgle.top;
            int contentViewTop= window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int TitleBarHeight= contentViewTop - StatusBarHeight;

            LogUtil.e("StatusBarHeight : " + StatusBarHeight);
            LogUtil.e("TitleBarHeight : " + TitleBarHeight);
        } catch (Exception e) {
            LogUtil.e("ERROR : " + e.getMessage());
        }

        return rtValue;
    }

    public static void showToast(Context context, String strMessage) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, strMessage, duration);
        //toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public static void showAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.confirm), null)
                .show();
    }

    public static boolean isOnline(Context mContext) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI && mNetworkInfo.isConnectedOrConnecting()) {
                LogUtil.e("isOnline ▶ WIFI 연결");
                PlayerConstants.MOBILE_NETWORK = false;
                return true;
            } else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE && mNetworkInfo.isConnectedOrConnecting()) {
                LogUtil.e("isOnline ▶ 모바일 네트워크 연결");
                PlayerConstants.MOBILE_NETWORK = true;
                return true;
            } else {
                LogUtil.e("isOnline ▶ 네트워크 오프라인");
                PlayerConstants.MOBILE_NETWORK = false;
                return false;
            }
        }
        PlayerConstants.MOBILE_NETWORK = false;
        return false;
    }

    public static boolean isServiceRunning(String serviceName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isForegroundActivity(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo running = info.get(0);
        ComponentName componentName = running.topActivity;

        return packageName.equals(componentName.getPackageName());
    }

    public static boolean currentVersionSupportBigNotification() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN){
            return true;
        }
        return false;
    }

    public static boolean currentVersionSupportLockScreenControls() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        if(sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            return true;
        }
        return false;
    }

    // Custom method to get screen width in dp/dip using Context object
    public static int getScreenWidthInDPs(Context context){
        /*
            DisplayMetrics
                A structure describing general information about a display,
                such as its size, density, and font scaling.
        */
        DisplayMetrics dm = new DisplayMetrics();

        /*
            WindowManager
                The interface that apps use to talk to the window manager.
                Use Context.getSystemService(Context.WINDOW_SERVICE) to get one of these.
        */

        /*
            public abstract Object getSystemService (String name)
                Return the handle to a system-level service by name. The class of the returned
                object varies by the requested name. Currently available names are:

                WINDOW_SERVICE ("window")
                    The top-level window manager in which you can place custom windows.
                    The returned object is a WindowManager.
        */

        /*
            public abstract Display getDefaultDisplay ()

                Returns the Display upon which this WindowManager instance will create new windows.

                Returns
                The display that this window manager is managing.
        */

        /*
            public void getMetrics (DisplayMetrics outMetrics)
                Gets display metrics that describe the size and density of this display.
                The size is adjusted based on the current rotation of the display.

                Parameters
                outMetrics A DisplayMetrics object to receive the metrics.
        */
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int widthInDP = Math.round(dm.widthPixels / dm.density);
        return widthInDP;
    }

    // Custom method to get screen height in dp/dip using Context object
    public static int getScreenHeightInDPs(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        /*
            In this example code we converted the float value
            to nearest whole integer number. But, you can get the actual height in dp
            by removing the Math.round method. Then, it will return a float value, you should
            also make the necessary changes.
        */

        /*
            public int heightPixels
                The absolute height of the display in pixels.

            public float density
             The logical density of the display.
        */
        int heightInDP = Math.round(dm.heightPixels / dm.density);
        return heightInDP;
    }

    public static int getScreenWidthInPXs(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.x;
    }

    public static int getScreenHeightInPXs(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        return p.y;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    // 총 외장 메모리 용량 구하기
    public static long getTotalExternalMemorySize() {
        if(externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    // 남은 외장 메모리 용량 구하기
    public static long getAvailableExternalMemorySize() {
        long nSize = -1;

        if(externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            nSize =  availableBlocks * blockSize;
        }
        return nSize;
    }

    // 총 내장 메모리 용량 구하기
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    // 남은 내장 메모리 용량 구하기
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public static void removeFiles(File file) {
        if (file != null) {
            File[] files = file.listFiles();
            if (files == null) {
                LogUtil.e("Not found files");
            } else {
                for (File item : files) {
                    item.delete();
                }
            }
        } else {
            LogUtil.e("File is null");
        }
    }

    public static void removeFiles(String path, String extension) {
        ExtensionFilter filter = new ExtensionFilter(extension);
        File dir = new File(path);

        String[] list = dir.list(filter);
        File file;
        if (list.length == 0) {
            return;
        }

        for (int i = 0; i < list.length; i++) {
            file = new File(path + list[i]);
            boolean isDeleted =  file.delete();
            if (isDeleted) {
                LogUtil.e("파일 삭제 - " + file.getAbsolutePath());
            } else {
                LogUtil.e("파일을 삭제 중 실패하였습니다.");
            }
        }
    }

    static class ExtensionFilter implements FilenameFilter {

        private String extension;

        public ExtensionFilter( String extension ) {
            this.extension = extension;
        }
        public boolean accept(File dir, String name) {
            return (name.endsWith(extension));
        }
    }

    public static final int NOTIFICATION_ID = 1;
    public static void sendNotification(Context context, Bundle data) {
        String action = data.getString("action", "");
        String poi = data.getString("poi", "");
        String uai = data.getString("uai", "");
        String ori = data.getString("ori", "");
        String oti = data.getString("oti", "");
        String ticker = data.getString("ticker", "");
        String title = data.getString("title", "");
        String message = data.getString("message", "");
        String sound = data.getString("sound", "");

        String UAI = SPUtil.getSharedPreference(context, Constants.SP_USER_ID);
        if (uai.equals(UAI)) {
            NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent actionIntent = new Intent(context, PostActivity.class);
            actionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            actionIntent.setData(Uri.parse(Constants.SERVICE_SCHEME + "://" + Constants.SERVICE_SCHEME_HOST + "?action=" + action + "&poi=" + poi + "&ori=" + ori + "&oti=" + oti));
            PendingIntent contentIntent = PendingIntent.getActivity(context, 8888, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            /**
             * YCNOTE - Notification
             *
             * setSmallIcon : 아이콘입니다 구 소스의 icon이랑 같습니다
             * setTicker : 알림이 뜰때 잠깐 표시되는 Text이며, 구 소스의 tickerText이랑 같습니다
             * setWhen : 알림이 표시되는 시간이며, 구 소스의 when이랑 같습니다
             * setNumber : 미확인 알림의 개수이며, 구 소스의 notification.number랑 같습니다
             * setContentTitle : 상단바 알림 제목이며, 구 소스의 contentTitle랑 같습니다
             * setContentText : 상단바 알림 내용이며, 구 소스의 contentText랑 같습니다
             * setDefaults : 기본 설정이며, 구 소스의 notification.defaults랑 같습니다
             * setContentIntent : 실행할 작업이 담긴 PendingIntent이며, 구 소스의 contentIntent랑 같습니다.
             * setAutoCancel : 터치하면 자동으로 지워지도록 설정하는 것이며, 구 소스의 FLAG_AUTO_CANCEL랑 같습니다
             * setPriority : 우선순위입니다, 구 소스의 notification.priority랑 같습니다만 구글 개발자 API를 보면 API 16이상부터 사용이 가능하다고 합니다
             * setOngoing : 진행중알림 이며, 구 소스의 FLAG_ONGOING_EVENT랑 같습니다
             * 	- 알림의 지속성 : 알림 리스트에서 사용자가 그것을 클릭하거나 좌우로 드래그해도 사라지지 않음
             * addAction : 알림에서 바로 어떤 활동을 할지 선택하는 것이며, 스샷찍은다음 삭제/공유 같은것이 이에 해당합니다
             */
            NotificationCompat.Builder mBuilder = null;
            if ("00.caf".equalsIgnoreCase(sound)) {
                mBuilder = new NotificationCompat.Builder(context)
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker(ticker)
                        .setWhen(System.currentTimeMillis())
                        //.setNumber(16)
                        .setContentTitle(title)
                        .setContentText(message)
                        //.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOngoing(false);
            } else if ("01.caf".equalsIgnoreCase(sound)) {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker(ticker)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOngoing(false)
                        .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" +R.raw.double_click))
                        .setVibrate(new long[] { 0, 500 });
            } else {
                mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker(ticker)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setOngoing(false)
                        .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" +R.raw.double_click))
                        .setVibrate(new long[] { 0, 500 });
            }

            if (mBuilder != null) {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }

            int badgeCount = SPUtil.getIntSharedPreference(context, Constants.SP_BADGE_COUNT);
            SPUtil.setSharedPreference(context, Constants.SP_BADGE_COUNT, badgeCount + 1);
            DeviceUtil.updateBadgeCount(context, badgeCount + 1);
        }
    }

    public static void updateBadgeCount(Context context, int count) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");

        // Component를 정의
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName(context));

        // 카운트를 넣어준다.
        intent.putExtra("badge_count", count);

        // Version이 3.1이상일 경우에는 Flags를 설정하여 준다.
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            intent.setFlags(0x00000020);
        }

        // send
        context.sendBroadcast(intent);
    }

    private static String getLauncherClassName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(context.getPackageName());

        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
        if(resolveInfoList != null && resolveInfoList.size() > 0) {
            return resolveInfoList.get(0).activityInfo.name;
        }
        return "";
    }
}
