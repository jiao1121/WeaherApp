package com.android.wty.widgetother.app;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;

import com.android.wty.LocationApplication;
import com.android.wty.R;
import com.android.wty.MainActivity;
import com.android.wty.widgetother.utils.WeatherDataUtil;

public class UpdateWidgetService extends Service {
    private static final String TAG = "GweatherUpdateWidgetService";

    public static final String WOEID_GPS = "woeid_gps";
    public static final String WOEID_ALL = "woeid_all";

    private WeatherInfo mWidgetWeatherInfo;
    private List<WeatherInfo> mWeatherInfoList;
    private InternetWorker mInternetWorker;
    private RemoteViews views;
    private AppWidgetManager appWidgetManager;
    private TimeDateReceiver mTimeDateReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mTimeDateReceiver == null) {
            mTimeDateReceiver = new TimeDateReceiver();
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_TIME_TICK);
            f.addAction(Intent.ACTION_TIME_CHANGED);
            f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            f.addAction(Intent.ACTION_DATE_CHANGED);
            registerReceiver(mTimeDateReceiver, f);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        views = new RemoteViews(this.getPackageName(), R.layout.widget_layout);
        LocationApplication.mModel = WeatherModel.getInstance(getApplicationContext());
        mInternetWorker = InternetWorker.getInstance(getApplicationContext());

        int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        if (intent != null) {
            if (intent.getDataString() != null) {
                widgetId = Integer.parseInt(intent.getDataString());
            }

            String intentAction = intent.getAction();
            initWeather();

            if (AppWidgetManager.INVALID_APPWIDGET_ID == widgetId) {
                if (WeatherWidget.ACTION_INIT.equals(intentAction)) {
                    if (null != mWidgetWeatherInfo && mWidgetWeatherInfo.getForecasts().size() >= MainActivity.FORECAST_DAY) {
                        updateWidgetUI();
                    }
                } else if (WeatherWidget.ACTION_UPDATE.equals(intentAction)) {
                    if (null != mWidgetWeatherInfo && mWidgetWeatherInfo.getForecasts().size() >= MainActivity.FORECAST_DAY) {
                        updateWidgetUI();
                    } else if ("".equals(WeatherDataUtil.getInstance()
                            .getDefaultCityWoeid(UpdateWidgetService.this))) {
                        setDefaultInfo();
                    }
                } else if (WeatherAction.ACTION_AUTO_REFRESH.equals(intentAction)) {
                    mInternetWorker.updateWeather();
                } else if (WeatherAction.ACTION_WEATHER_REFRESH_CURRENT.equals(intentAction)) {

                } else if (WeatherAction.ACTION_WEATHER_REFRESH_ALL.equals(intentAction)) {

                } else if (WeatherAction.ACTION_QUERT_LOCATION.equals(intentAction)) {

                }
            }
        } else {
            if (null != mWidgetWeatherInfo && mWidgetWeatherInfo.getForecasts().size() >= MainActivity.FORECAST_DAY) {
                updateWidgetUI();
            } else if ("".equals(WeatherDataUtil.getInstance()
                    .getDefaultCityWoeid(UpdateWidgetService.this))) {
                setDefaultInfo();
            }
        }
        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.e("think", "onDestory");
    }

    class TimeDateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Intent setIntent = new Intent();
            setIntent.setAction("com.android.wty.time_changed");
            android.util.Log.e("think", "timechange");
            sendBroadcast(setIntent);
        }
    }

    private void initWeather() {
        String defWoeid = WeatherDataUtil.getInstance().getDefaultCityWoeid(
                UpdateWidgetService.this);
        mWeatherInfoList = LocationApplication.mModel.getWeatherInfos();
        if (mWeatherInfoList.isEmpty()) {
            mWidgetWeatherInfo = null;
        } else {
            if(WeatherDataUtil.DEFAULT_WOEID_GPS.equals(defWoeid)) {
                for (WeatherInfo info:mWeatherInfoList) {
                    if (info.isGps()) {
                        mWidgetWeatherInfo = info;
                    }
                }
            } else {
                for (WeatherInfo info:mWeatherInfoList) {
                    if (defWoeid.equals(info.getWoeid()) && !info.isGps()) {
                        mWidgetWeatherInfo = info;
                    }
                }
            }
        }
    }

    private void updateWidgetUI() {
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        String name = mWidgetWeatherInfo.getName();
        //views.setTextViewText(R.id.widget_weathercity, name);
        views.setTextViewText(R.id.widget_weathertemperature
                ,(name
                + " "
                + mWidgetWeatherInfo.getForecasts().get(0).getLow() + "\u2103" + "~"
                + mWidgetWeatherInfo.getForecasts().get(0).getHigh() + "\u2103"));
       // views.setTextViewText(R.id.widget_weathercondition, mWidgetWeatherInfo
       //         .getCondition().getText());

        int code = Integer
            .parseInt(mWidgetWeatherInfo.getCondition().getCode());
        int resId;
        boolean isnight = WeatherDataUtil.getInstance().isNight();
        resId = WeatherDataUtil.getInstance().getWeatherImageResourceByCode(
                code, isnight, true);
        if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId) {
            resId = WeatherDataUtil.getInstance()
                .getWeatherImageResourceByText(
                        mWidgetWeatherInfo.getCondition().getText(),
                        isnight, true);
        }

        Drawable a = getDrawable(resId);
        views.setImageViewBitmap(R.id.widget_img, drawableToBitmap(a));

        int[] appWidgetIds = appWidgetManager
            .getAppWidgetIds(new ComponentName(getApplicationContext(),
                        WeatherWidget.class));
        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.partiallyUpdateAppWidget(appWidgetIds[i], views);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        Paint sPaint = new Paint();
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.STROKE);
        sPaint.setColor(Color.parseColor("#c0c0c0"));
        canvas.drawBitmap(bitmap, 0, 0, sPaint);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private void setDefaultInfo() {
        appWidgetManager = AppWidgetManager
            .getInstance(getApplicationContext());

        String defaultData = getResources().getString(
                R.string.weather_data_default);
        views.setTextViewText(R.id.widget_weathercity, defaultData);
        views.setTextViewText(R.id.widget_weathertemperature, defaultData);
        views.setTextViewText(R.id.widget_weathercondition, defaultData);

        int resId;
        boolean isnight = WeatherDataUtil.getInstance().isNight();
        resId = WeatherDataUtil.getInstance().getWeatherImageResourceByText(
                defaultData, isnight, true);
        views.setImageViewResource(R.id.widget_img, resId);

        int[] appWidgetIds = appWidgetManager
            .getAppWidgetIds(new ComponentName(getApplicationContext(),
                        WeatherWidget.class));
        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.partiallyUpdateAppWidget(appWidgetIds[i], views);
        }
    }
}
