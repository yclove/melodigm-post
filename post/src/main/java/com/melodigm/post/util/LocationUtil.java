package com.melodigm.post.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.ContextCompat;

import com.melodigm.post.common.Constants;
import com.melodigm.post.util.handler.WeakRefHandler;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtil {
    private Context mContext;
    private double mLat, mLng;
    private LocationManager mLocationManager;
    private WeakRefHandler mHandler;

    public LocationUtil(Context context, WeakRefHandler handler) {
        mContext = context;
        mHandler = handler;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    private Location mLocation;

    public void run() {
        if (mLocationManager == null)
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // 정확도
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // 전원 소비량
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 고도, 높이 값을 얻어 올지를 결정
        criteria.setAltitudeRequired(false);
        // provider 기본 정보(방위, 방향)
        criteria.setBearingRequired(false);
        // 속도
        criteria.setSpeedRequired(false);
        // 위치 정보를 얻어 오는데 들어가는 금전적 비용
        criteria.setCostAllowed(true);

        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        LogUtil.e("Best Provider : " + bestProvider);
        /*
		 * YCLOVE : Provider 가 없을 경우 Crash 발생 현상 수정
		 */
        if ("".equals(bestProvider) || bestProvider == null) {
            LogUtil.e("Not found location rovider");
        } else {
            //mLocation = mLocationManager.getLastKnownLocation(bestProvider);
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(bestProvider, 1 * 1000, 0.01f, mLocationListener);
            }
        }
    }

    public boolean isLocationEnabled() {
        Boolean isGPSEnabled, isNetworkEnabled;
        if (mLocationManager == null) mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        LogUtil.e("GPS 프로바이더 사용가능여부 : " + isGPSEnabled);
        LogUtil.e("네트워크 프로바이더 사용가능여부 : " + isNetworkEnabled);

        if (isGPSEnabled || isNetworkEnabled) {
            return true;
        } else {
            return false;
        }
    }

    public Location getmLocation() {
        return mLocation;
    }

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 수신기가 GPS에서 위치 업데이트 알림을 호출 할 때
            LogUtil.e("Latitude : " + location.getLatitude());
            LogUtil.e("Longitude : " + location.getLongitude());
            mLocation = location;

            Bundle data = new Bundle();
            data.putString("USER_LAT", String.valueOf(location.getLatitude()));
            data.putString("USER_LNG", String.valueOf(location.getLongitude()));
            Message msg = new Message();
            msg.setData(data);
            msg.what = Constants.QUERY_LOCATION_CHANGE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // GPS 공급자가 해제 될 때 호출 (사용자는 휴대 전화에 GPS를 해제)
            LogUtil.e("Provider : " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            // GPS 공급자가 설정 될 때 호출 (사용자가 휴대 전화에 GPS를 켜기)
            LogUtil.e("Provider : " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // GPS 공급자의 상태가 변경 될 때 호출
            LogUtil.e("Provider : " + provider + "(" + status + ")");
        }
    };

    public String getAddress(Context context, double lat, double lng) {
        String addressString = "주소정보가 없습니다.";
        Geocoder gc = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gc.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                addressString = address.getAddressLine(0);
                addressString = addressString.substring(addressString.indexOf(" ") + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressString;
    }

    public void stopLocationUpdate() {
        LogUtil.e(" ");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}