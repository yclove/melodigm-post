package com.melodigm.post.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.melodigm.post.common.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;

public class LogUtil {
    private static final String strPrefix = " ▶ ";
    private static final String strSuffix = " ▶ ";
    private static final int LIMIT = 2000;

    public static void d(String strMsg) {
        if (Constants.DEBUG && strMsg != null) {
            try {
                throw new Exception();
            } catch (Exception e) {
                String strReferer = e.getStackTrace()[1].getClassName();
                strReferer = strReferer.substring(strReferer.lastIndexOf(".") + 1, strReferer.length());
                strReferer += "." + e.getStackTrace()[1].getMethodName() + "()";

                int size = strMsg.length() / LIMIT;
                for (int i = 0; i < size; i++) {
                    Log.d(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(i * LIMIT, (i + 1) * LIMIT));
                }
                if (strMsg.length() % LIMIT > 0) {
                    Log.d(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(size*LIMIT));
                }
            }
        }
    }

    public static void i(String strMsg) {
        if (Constants.DEBUG && strMsg != null) {
            try {
                throw new Exception();
            } catch (Exception e) {
                String strReferer = e.getStackTrace()[1].getClassName();
                strReferer = strReferer.substring(strReferer.lastIndexOf(".") + 1, strReferer.length());
                strReferer += "." + e.getStackTrace()[1].getMethodName() + "()";

                int size = strMsg.length() / LIMIT;
                for (int i = 0; i < size; i++) {
                    Log.i(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(i * LIMIT, (i + 1) * LIMIT));
                }
                if (strMsg.length() % LIMIT > 0) {
                    Log.i(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(size * LIMIT));
                }
            }
        }
    }

    public static void e(String strMsg) {
        if (Constants.DEBUG && strMsg != null) {
            try {
                throw new Exception();
            } catch (Exception e) {
                // StackTrace의 '1'번 인덱스를 가져오면 바로 상위 호출자를 가르키게 된다.
                String strReferer = e.getStackTrace()[1].getClassName();
                strReferer = strReferer.substring(strReferer.lastIndexOf(".") + 1, strReferer.length());
                strReferer += "." + e.getStackTrace()[1].getMethodName() + "(" + e.getStackTrace()[1].getLineNumber() + ")";

                int size = strMsg.length() / LIMIT;
                for (int i = 0; i < size; i++) {
                    Log.e(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(i * LIMIT, (i + 1) * LIMIT));
                }
                if (strMsg.length() % LIMIT > 0) {
                    Log.e(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(size * LIMIT));
                }
            }
        }
    }

    public static void e(String strMsg, Throwable throwable) {
        if (Constants.DEBUG && strMsg != null) {
            try {
                throw new Exception();
            } catch (Exception e) {
                // StackTrace의 '1'번 인덱스를 가져오면 바로 상위 호출자를 가르키게 된다.
                String strReferer = e.getStackTrace()[1].getClassName();
                strReferer = strReferer.substring(strReferer.lastIndexOf(".") + 1, strReferer.length());
                strReferer += "." + e.getStackTrace()[1].getMethodName() + "(" + e.getStackTrace()[1].getLineNumber() + ")";

                int size = strMsg.length() / LIMIT;
                for (int i = 0; i < size; i++) {
                    Log.e(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(i * LIMIT, (i + 1) * LIMIT), throwable);
                }
                if (strMsg.length() % LIMIT > 0) {
                    Log.e(Constants.SERVICE_TAG + strSuffix + strReferer, strPrefix + strMsg.substring(size*LIMIT), throwable);
                }
            }
        }
    }

    public static void m(Context context) {
        if (Constants.DEBUG) {
            try {
                throw new Exception();
            } catch (Exception e) {
                String strReferer = e.getStackTrace()[1].getClassName();
                strReferer = strReferer.substring(strReferer.lastIndexOf(".") + 1, strReferer.length());
                strReferer += "." + e.getStackTrace()[1].getMethodName() + "()";

                /**
                 * YCNOTE - largeHeap
                 * largeHeap : 보통 2배에서 4배까지 dalvik heap의 최대 크기가 늘어난다. (API 12부터 지원)
                 * dalvik heap과 external 영역이 합쳐졌다.
                 * 단말기의 앱 메모리(24MB, 32MB, 48MB, 64MB, 96MB, 128MB 등 다양하다.)
                 */
                int intHeapSize;
                // MB 단위로  출력된다.
                if (Build.VERSION.SDK_INT < 12) {
                    intHeapSize = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
                } else {
                    intHeapSize = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
                }

                long lngMaxMemory = Runtime.getRuntime().maxMemory();      // Dalvik heap 영역 최대 크기(프로세스당 메모리 한계)
                long lngTotalMemory = Runtime.getRuntime().totalMemory();    // Dalvik heap 영역 크기(footprint)
                long lngFreeMemory = Runtime.getRuntime().freeMemory();     // Dalvik heap free 크기
                long lngAllocated = lngTotalMemory - lngFreeMemory;               // Dalvik heap allocated 크기

                e("단말기의 앱 메모리 : " + new java.text.DecimalFormat("#,###").format(intHeapSize) + " MB");
                e("최대로 할당될 수 있는 메모리 : " + CastUtil.getFileSize(lngMaxMemory));
                e("힙에 할당된 총 메모리 : " + CastUtil.getFileSize(lngTotalMemory));
                e("할당된 힙 메모리 중 사용가능한 크기 : " + CastUtil.getFileSize(lngFreeMemory));
                e("사용중인 메모리 : " + CastUtil.getFileSize(lngAllocated));
                e("\n");
                e("총 외장 메모리 용량 : " + CastUtil.getFileSize(DeviceUtil.getTotalExternalMemorySize()));
                e("남은 외장 메모리 용량 : " + CastUtil.getFileSize(DeviceUtil.getAvailableExternalMemorySize()));
                e("총 내장 메모리 용량 : " + CastUtil.getFileSize(DeviceUtil.getTotalInternalMemorySize()));
                e("남은 내장 메모리 용량 : " + CastUtil.getFileSize(DeviceUtil.getAvailableInternalMemorySize()));
            }
        }
    }

    /**
     * YCNOTE - Internal / External Storage
     *
     * [ 내부 저장소 ]
     * 캐시(Cache) 저장 영역 - 캐시 디렉터리에는 애플리케이션에서 필요한 임시 파일들이 저장됩니다.
     * File Context.getCacheDir() - 내부 저장소의 캐시 디렉터리 경로를 반환합니다.
     * /data/data/[패키지 이름]/cache
     *
     * 데이터베이스(Database) 파일 - 애플리케이션에서 사용하는 데이터베이스 파일들이 저장됩니다.
     * File Context.getDatabasePath(String name) - 데이터베이스 파일의 경로를 반환합니다. 인자로 데이터베이스 파일의 이름을 넘겨줍니다.
     * /data/data/[패키지 이름]/databases
     *
     * 일반 파일 저장 영역 - 데이터베이스와 캐시를 제외한 애플리케이션에서 사용하는 일반 파일이 저장되는 영역입니다. 이 경로는 Context.openFIleOutput(String, int)를 사용하여 생성되는 파일이 저장되는 경로와 동일합니다.
     * File Context.getFilesDir() - 애플리케이션에서 사용하는 일반 파일들이 저장되는 경로를 반환합니다.
     * /data/data/[패키지 이름]/files
     *
     * 애플리케이션에서 사용하는 각 일반 파일들의 경로를 가져오려면 다음 메서드를 사용합니다.
     * File Context.getFileStreamPath(String name) - 일반 파일이 저장된 공간에서 특정 이름을 가지는 파일의 경로를 반환합니다. 인자로 확장자를 포함한 파일 이름을 넘겨줍니다.
     * /data/data/[패키지 이름]/files/[파일이름]
     *
     * [ 외부 저장소-공용 영역 ]
     * 최상위 경로 얻기 - 외부 저장소(일반적으로 SD카드)의 최상위 경로를 의미합니다.
     * static File Environment.getExternalStorageDirectory() - 외부 저장소의 최상위 경로를 반환합니다.
     * /mnt/sdcard
     *
     * 특정 데이터를 저장하는 영역 - 여러 애플리케이션에서 공용으로 사용할 수 있는 데이터들을 저장합니다. 데이터의 유형에 따라 별도의 디렉터리를 사용합니다.
     * static File Environment.getExternalStoragePublicDirectory(String type) - 데이터 유형에 따른 외부 저장소의 저장 공간 경로를 반환합니다. 인자로 디렉터리의 유형을 넘겨줍니다.
     * Environment.DIRECTORY_ALARMS - 알람으로 사용할 오디오 파일을 저장합니다. - /mnt/sdcard/Alarms
     * Environment.DIRECTORY_DCIM - 카메라로 촬영한 사진이 저장됩니다. - /mnt/sdcard/DCIM
     * Environment.DIRECTORY_DOWNLOADS	 다운로드한 파일이 저장됩니다. - /mnt/sdcard/Download
     * Environment.DIRECTORY_MUSIC - 음악 파일이 저장됩니다. - /mnt/sdcard/Music
     * Environment.DIRECTORY_MOVIES - 영상 파일이 저장됩니다. - /mnt/sdcard/Movies
     * Environment.DIRECTORY_NOTIFICATIONS - 알림음으로 사용할 오디오 파일을 저장합니다. - /mnt/sdcard/Notifications
     * Environment.DIRECTORY_PICTURES - 그림 파일이 저장됩니다. - /mnt/sdcard/Pictures
     * Environment.DIRECTORY_PODCASTS - 팟캐스트(Poacast) 파일이 저장됩니다. - /mnt/sdcard/Podcasts
     *
     * [ 외부 저장소-애플리케이션 고유 영역 ]
     * 특정 데이터를 저장하는 영역 - 애플리케이션 고유 영역에도 공용 영역과 마찬가지로 각 데이터 유형별로 데이터를 저장하는 표준 디렉터리를 제공합니다.
     * File Context.getExternalFilesDir(String type) - 애플리케이션 고유 영역의 데이터 유형에 따른 외부 저장소의 저장 공간 경로를 반환합니다. 인자로 디렉터리의 유형을 넘겨줍니다.
     * Environment.DIRECTORY_ALARMS - /mnt/sdcard/Android/data/[패키지 이름]/files/Alarms
     * Environment.DIRECTORY_DCIM - /mnt/sdcard/Android/data/[패키지 이름]/files/DCIM
     * Environment.DIRECTORY_DOWNLOADS - /mnt/sdcard/Android/data/[패키지 이름]/files/Downloads
     * Environment.DIRECTORY_MUSIC - /mnt/sdcard/Android/data/[패키지 이름]/files/Music
     * Environment.DIRECTORY_MOVIES - /mnt/sdcard/Android/data/[패키지 이름]/files/Movies
     * Environment.DIRECTORY_NOTIFICATIONS - /mnt/sdcard/Android/data/[패키지 이름]/files/Notifications
     * Environment.DIRECTORY_PICTURES - /mnt/sdcard/Android/data/[패키지 이름]/files/Pictures
     * Environment.DIRECTORY_PODCASTS - /mnt/sdcard/Android/data/[패키지 이름]/files/Podcasts
     * null - /mnt/sdcard/Android/data/[패키지 이름]/files
     *
     * 캐시 데이터를 저장하는 영역 - 애플리케이션에서 사용하는 임시 데이터를 외부 저장소에 저장합니다.
     * File Context.getExternalCacheDir() - 외부 저장소의 캐시 디렉터리를 반환합니다.
     * /mnt/sdcard/Android/data/[패키지 이름]/cache
     */
    public static void file(File file) {
        if (file != null) {
            e(file.getAbsolutePath() + "( " + CastUtil.getFileSize(folderSize(file)) + " )");

            File[] files = file.listFiles();
            if (files == null) {
                e("Not found files");
            } else {
                Arrays.sort(files, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        if (((File)o1).lastModified() > ((File)o2).lastModified()) {
                            return +1;
                        } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });

                for (File item : files) {
                    if (item.isDirectory()) e(item.getName() + "( Directory )");
                }

                for (File item : files) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (item.isFile()) e(item.getName() + "( " + sdf.format(item.lastModified()) + " / " + CastUtil.getFileSize(item.length()) + " )");
                }
            }
        } else {
            e("File is null");
        }
    }

    public static long folderSize(File directory) {
        long length = 0;
        try {
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
        return length;
    }
}
