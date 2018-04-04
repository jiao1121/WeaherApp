package com.android.wty.server;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import com.android.wty.LocationApplication;
import com.android.wty.R;
import com.android.wty.location.LocalWeather;
import com.android.wty.location.LocationSearch;
import com.android.wty.location.LocationService;
import com.android.wty.widget.WeatherWidgetO;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jiaozhihao on 18-1-3.
 */

public class UpdateService extends Service {
    private static final String ACTION_TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
    private static final String ACTION_TIME_TICK = Intent.ACTION_TIME_TICK;
    private static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;
    private static final String ACTION_TIMEZONE_CHANGED = Intent.ACTION_TIMEZONE_CHANGED;
    private static final String ACTION_LOCALE_CHANGE = Intent.ACTION_LOCALE_CHANGED;
    private LocationService locationService;
    private LocalWeather.Data weather;
    private LocationSearch.Data loc;
    private Timer mTimer = null;
    private RemoteViews views = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private TimeDateReceiver mTimeDateReceiver;
    private String mDrawableName;
    private String mCountry;
    private Context mContext;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Build the widget update for today

        locationService = LocationApplication.getLocationApp().locationService;
        buildUpdate(this);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        views = new RemoteViews(this.getPackageName(), R.layout.example_appwidget);
        updateData();
        updateTime();
        mTimeDateReceiver = new TimeDateReceiver();
        IntentFilter f = new IntentFilter();
        f.addAction(ACTION_DATE_CHANGED);
        f.addAction(ACTION_TIME_CHANGED);
        f.addAction(ACTION_TIME_TICK);
        f.addAction(ACTION_TIMEZONE_CHANGED);
        f.addAction(ACTION_LOCALE_CHANGE);
        registerReceiver(mTimeDateReceiver, f);
    }

    class TimeDateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_TIME_CHANGED.equals(action) || ACTION_TIMEZONE_CHANGED.equals(action)
                    || ACTION_TIME_TICK.equals(action) || ACTION_DATE_CHANGED.equals(action)
                    || ACTION_LOCALE_CHANGE.equals(action)) {
                updateData();
                updateTime();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
                                       return null;
                                                   }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTimeDateReceiver);
    }
    /**
     * Build a widget update to show the current weather
     * Will block until the online API returns.
     */
    public void buildUpdate(Context context) {
        MyLocation a = new MyLocation();
        final MyLocation.LocationResult locationResult = a.new LocationResult() {
            @Override
            public void gotLocation(Location location) {
                Boolean Done = false;
                synchronized (Done) {
                    if (!Done) {
                        String q = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());

                        LocalWeather lw = new LocalWeather(false);
                        String query = (lw.new Params(lw.key)).setQ(q).getQueryString(LocalWeather.Params.class);
                        weather = lw.callAPI(query);

                        LocationSearch ls = new LocationSearch(false);
                        query = (ls.new Params(ls.key)).setQuery(q).getQueryString(LocationSearch.Params.class);
                        loc = ls.callAPI(query);
                        updateAppWidget(UpdateService.this);

                        Done = true;
                    }
                }
            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

//    String subUriToString(String url) {
//        android.util.Log.e("think", "url : " + url);
//        String[] temp = null;
//        if (url != null) {
//            temp = url.split("\\/");
//            if (temp.length > 1) {
//                return temp[5].substring(0, 12);
//            }
//        }
//        return "widget42_icon_rain_day";
//    }

    void updateAppWidget(Context context) {

        // Build an update that holds the updated widget contents
        views.setTextViewText(R.id.textViewTemp, " " + weather.current_condition.temp_C+"\u2103");
        views.setTextViewText(R.id.textViewLocation, mCountry);

        views.setImageViewResource(R.id.imageViewWidget, this.getResources()
                .getIdentifier("widget42_icon_rain_day", "drawable", this.getPackageName()));
        // Push update for this widget to the home screen
        ComponentName thisWidget = new ComponentName(this, WeatherWidgetO.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, views);
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    void updateTime() {
        if (checkPermissionAllGranted(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION })) {
            buildUpdate(this);
        }
        String data = sdf.format(new Date());
        views.setImageViewBitmap(R.id.clock_view, buildUpdate(data, UpdateService.this));
        ComponentName thisWidget = new ComponentName(this, WeatherWidgetO.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, views);
    }

    void updateData() {
        Locale locale = Locale.getDefault();
        String datePattern = DateFormat.getBestDateTimePattern(locale, "EEMMMMd");
        final Date now = new Date();
        views.setTextViewText(R.id.date_time, new SimpleDateFormat(datePattern, locale).format(now));
        ComponentName thisWidget = new ComponentName(this, WeatherWidgetO.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, views);
    }

    static Bitmap buildUpdate(String time, Context context){
        Bitmap myBitmap = Bitmap.createBitmap(510, 180, Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface tf= Typeface.createFromAsset(context.getAssets(),"fonts/leah_line.ttf");
        paint.setAntiAlias(true);
        paint.setTypeface(tf);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(170);
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(time, 190, 130, paint);
//        Bitmap myBitmap = Bitmap.createBitmap(240, 80, Bitmap.Config.ARGB_4444);
//        Canvas myCanvas = new Canvas(myBitmap);
//        Paint paint = new Paint();
//        Typeface tf= Typeface.createFromAsset(context.getAssets(),"fonts/leah_line.ttf");
//        paint.setAntiAlias(true);
//        paint.setTypeface(tf);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.WHITE);
//        paint.setTextSize(80);
//        paint.setTextAlign(Paint.Align.CENTER);
//        myCanvas.drawText(time, 100, 60, paint);
        return myBitmap;
    }

    public void setCountry(String s) {
        if (s != null) {
            this.mCountry = s;
        } else {
            this.mCountry = "unknow";
        }
    }

    class MyLocation {
        private LocationService locationService;
        Timer timer1;
        LocationManager lm;
        MyLocation.LocationResult locationResult;
        private String mTimer;
        private Double mLatitude;
        private Double mLongitude;
        boolean gps_enabled = false;
        boolean network_enabled = false;

        public boolean getLocation(Context context, MyLocation.LocationResult result) {
            //I use LocationResult callback class to pass location value from MyLocation to user code.
            locationResult = result;
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (lm == null)
                lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            //exceptions will be thrown if provider is not permitted.
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }
            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            //don't start listeners if no provider is enabled
            if (!gps_enabled && !network_enabled)
                return false;

            if (gps_enabled)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            if (network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            locationService = LocationApplication.getLocationApp().locationService;
            timer1 = new Timer();
            timer1.schedule(new TimerTask() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    startGps();

                    lm.removeUpdates(locationListenerGps);
                    lm.removeUpdates(locationListenerNetwork);

                    Location net_loc=null, gps_loc=null;
                    if(gps_enabled)
                        gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(network_enabled)
                        net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    //if there are both values use the latest one
                    if(gps_loc!=null && net_loc!=null){
                        if(gps_loc.getTime()>net_loc.getTime())
                            locationResult.gotLocation(gps_loc);
                        else
                            locationResult.gotLocation(net_loc);
                        return;
                    }

                    if (gps_loc!=null) {
                        locationResult.gotLocation(gps_loc);
                        return;
                    }
                    if (net_loc!=null) {
                        locationResult.gotLocation(net_loc);
                        return;
                    }
                    Location setLocation = new Location("fantao");
                    if (mTimer != null) {
                        setLocation.setLatitude(mLatitude);
                        setLocation.setLongitude(mLongitude);
                        locationResult.gotLocation(setLocation);
                        stopGps();
                    }
                }
            }, 0, (60000));
            return true;
        }

        LocationListener locationListenerGps = new LocationListener() {
            public void onLocationChanged(Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerNetwork);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        LocationListener locationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerGps);
            }
            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        public abstract class LocationResult {
             public abstract void gotLocation(Location location);
        }

        //This needs to be called onPause() if getLocation is called from onResume, to prevent crash, as stated on the same stackoverflow thread.
        public void cancelTimer() {
            timer1.cancel();
            lm.removeUpdates(locationListenerGps);
            lm.removeUpdates(locationListenerNetwork);
        }

        private void startGps() {
            // -----------location config ------------
            //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
            locationService.registerListener(mListener);
            //注册监听
            int type = new Intent().getIntExtra("from", 0);
            if (type == 0) {
                locationService.setLocationOption(locationService.getDefaultLocationClientOption());
            } else if (type == 1) {
                locationService.setLocationOption(locationService.getOption());
            }
            locationService.start();// 定位SDK
        }

        public void stopGps() {
            locationService.stop(); //停止定位服务
        }

        /*****
         *
         * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
         *
         */
        private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub
                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    StringBuffer sb = new StringBuffer(256);
                    /**
                     * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                     * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                     */

                    mTimer = location.getTime();
                    // 纬度
                    mLatitude = location.getLatitude();
                    // 经度
                    mLongitude = location.getLongitude();

                    setCountry(location.getCity() + "," + location.getCountry());

                    if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                        sb.append("\nspeed : ");
                        sb.append(location.getSpeed());// 速度 单位：km/h
                        sb.append("\nsatellite : ");
                        sb.append(location.getSatelliteNumber());// 卫星数目
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 海拔高度 单位：米
                        sb.append("\ngps status : ");
                        sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                        sb.append("\ndescribe : ");
                        sb.append("gps定位成功");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                        // 运营商信息
                        if (location.hasAltitude()) {// *****如果有海拔高度*****
                            sb.append("\nheight : ");
                            sb.append(location.getAltitude());// 单位：米
                        }
                        sb.append("\noperationers : ");// 运营商信息
                        sb.append(location.getOperators());
                        sb.append("\ndescribe : ");
                        sb.append("网络定位成功");
                    } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                        sb.append("\ndescribe : ");
                        sb.append("离线定位成功，离线定位结果也是有效的");
                    } else if (location.getLocType() == BDLocation.TypeServerError) {
                        sb.append("\ndescribe : ");
                        sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                        sb.append("\ndescribe : ");
                        sb.append("网络不同导致定位失败，请检查网络是否通畅");
                    } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                        sb.append("\ndescribe : ");
                        sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                    }
                }
            }
        };
    }

}
