package com.android.wty;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.wty.widgetother.app.CityMangerActivity;
import com.android.wty.widgetother.app.InternetWorker;
import com.android.wty.widgetother.app.UpdateWidgetService;
import com.android.wty.widgetother.app.WeatherAction;
import com.android.wty.widgetother.app.WeatherInfo;
import com.android.wty.widgetother.app.WeatherModel;
import com.android.wty.widgetother.app.WeatherProvider;
import com.android.wty.widgetother.app.WeatherWidget;
import com.android.wty.widgetother.utils.Utils;
import com.android.wty.widgetother.utils.WeatherDataUtil;
import com.android.wty.widgetother.view.ExpandView;
import com.android.wty.widgetother.view.ScrollControlLayout;
import com.android.wty.widgetother.view.WeatherInfoMainView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.android.wty.widgetother.app.CityMangerActivity.REQUEST_CODE_PERMISSION;

/*
   jiaozhihao
   2018.01.01
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "Gweather.MainActivity";

    public static final int FORECAST_DAY = 5;
    public static final String SETTINGS_SP = "settings_sp";
    public static final String SETTINGS_AUTO_REFRESH_ENABLE = "settings_auto_enable";
    public static final String SETTINGS_AUTO_REFRESH = "settings_auto_refresh";
    public static final String SETTINGS_WIFI_ONLY = "settings_wifi_only";
    public static final int SETTINGS_AUTO_REFRESH_INVALID = -1;
    public static final int SETTINGS_AUTO_REFRESH_6H = 6;
    public static final int SETTINGS_AUTO_REFRESH_12H = 12;
    public static final int SETTINGS_AUTO_REFRESH_24H = 24;

    public static final long TIME_6H = 6 * 60 * 60 * 1000L;
    public static final long TIME_12H = 12 * 60 * 60 * 1000L;
    public static final long TIME_24H = 24 * 60 * 60 * 1000L;
    private AlertDialog.Builder builder;
    private ExpandView mExpandView;

    public enum MenuState {
        OPEN, CLOSE;
    }

    private ScrollControlLayout weatherInfoMainContainer;
    private WeatherInfoMainView weatherInfoMainView;

    private View mainContentView;
    private ImageView refresh;
    private ImageView setting;
    private TextView refreshTimeText;
    private View loadProgressView;
    private TextView progressText;
    private LinearLayout indicatorBar;

    private View menuAutoRefresh;
    private Switch menuCheckAutoRefresh;
    private View menuAuto6;
    private ImageView menuCheckAuto6h;
    private View menuAuto12;
    private ImageView menuCheckAuto12h;
    private View menuAuto24;
    private ImageView menuCheckAuto24h;
    private View menuWifiOnly;
    private Switch menuCheckWifiOnly;
    private View menuSetCity;
    private View mShowMenu;
    private ImageView mExpandShow;
    private TextView mMenuText;
    private View mLine;
    private TextView mCityTitle;
    private ImageView mIsGps;

    private InternetWorker mInternetWorker;

    private List<WeatherInfo> mWeatherInfoList;

    private MenuState menuState = MenuState.CLOSE;
    private WeatherRefreshedReceiver mWeatherRefreshedReceiver;

    private int defScreen;
    private LinearLayout menuView;
    private AlertDialog dialog;

    private class WeatherRefreshedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }

            String action = intent.getAction();
            Log.d(TAG, "WeatherRefreshedReceiver, " + action);
            if (WeatherAction.ACTION_WEATHER_REFRESHED.equals(action)) {
                setWeatherFromBD();
                showLoadingProgress(false, R.string.progress_refresh);
            } else if (WeatherAction.ACTION_WEATHER_REFRESHED_ALL.equals(action)) {
                setWeatherFromBD();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        Log.d(TAG, "onCreate");
        LocationApplication.mModel = WeatherModel.getInstance(getApplicationContext());
        mInternetWorker = InternetWorker.getInstance(getApplicationContext());
        initUI();

        mWeatherRefreshedReceiver = new WeatherRefreshedReceiver();
        IntentFilter filter = new IntentFilter(
                WeatherAction.ACTION_WEATHER_REFRESHED);
        filter.addAction(WeatherAction.ACTION_WEATHER_REFRESHED_ALL);
        registerReceiver(mWeatherRefreshedReceiver, filter);

        boolean isFirstRun = isFirstRun();
        Log.d(TAG, "isFirstRun=" + isFirstRun);
        if (isFirstRun) {
            Intent intent = new Intent(MainActivity.this, CityMangerActivity.class);
            intent.putExtra("isFirstRun", isFirstRun);
            startActivity(intent);
        } else {
            setWeatherFromBD();
            WeatherDataUtil.getInstance().setNeedUpdateMainUI(MainActivity.this, false);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasPermission = PackageManager.PERMISSION_GRANTED ==
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(TAG, "ACCESS_COARSE_LOCATION, hasPermission="
                    + hasPermission);
            if (!hasPermission) {
                requestPermissions(
                        new String[] { Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.READ_PHONE_STATE
                        , Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mWeatherRefreshedReceiver);
        mWeatherRefreshedReceiver = null;
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                attributes.flags |= flagTranslucentStatus;
                window.setAttributes(attributes);
            }
        }
    }

    protected void onStart() {
        super.onStart();
        updateUi();
    }

    private boolean isFirstRun() {
        ContentResolver mContentResolver = getContentResolver();
        Uri uri = Uri.parse("content://com.gweather.app.weather/gweather");
        Cursor cursor = mContentResolver.query(uri, null, null, null, WeatherProvider.INDEX);
        if (cursor != null) {
            if (cursor.getCount() == 0) {
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private void initUI() {
        fullScreen(this);
        mainContentView = findViewById(R.id.main_content);

        builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        menuView = (LinearLayout) getLayoutInflater().inflate(R.layout.main_menu,null);
        builder.setView(menuView);
        dialog = builder.create();
        weatherInfoMainContainer = (ScrollControlLayout) findViewById(R.id.main_container);
        weatherInfoMainContainer.setOnScreenChangedListener(new ScrollControlLayout.OnScreenChangedListener() {
            @Override
            public void screenChange(int curScreen) {
                Log.d(TAG, "screenChange = " + curScreen);

                ImageView indicatorImage = (ImageView) indicatorBar.getChildAt(defScreen);
                indicatorImage.setImageResource(R.drawable.point);
                indicatorImage = (ImageView) indicatorBar.getChildAt(curScreen);
                indicatorImage.setImageResource(R.drawable.point_current);

                defScreen = curScreen;

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date mDate = new Date(mWeatherInfoList.get(defScreen).getUpdateTime());
                String refreshTime = getResources().getString(R.string.refresh_time, format.format(mDate));
                refreshTimeText.setText(refreshTime);
                mCityTitle.setText(mWeatherInfoList.get(curScreen).getName());
                android.util.Log.e("think", "mWeatherInfoList.get(curScreen).isGps() : " + mWeatherInfoList.get(curScreen).isGps());
                mIsGps.setVisibility(mWeatherInfoList.get(curScreen).isGps() ? View.VISIBLE : View.GONE);
                if (mWeatherInfoList.get(defScreen).isGps()) {
                    WeatherDataUtil.getInstance().updateDefaultCityWoeid(MainActivity.this, WeatherDataUtil.DEFAULT_WOEID_GPS);
                } else {
                    WeatherDataUtil.getInstance().updateDefaultCityWoeid(MainActivity.this, mWeatherInfoList.get(defScreen).getWoeid());
                }
                startUpdateService(MainActivity.this, WeatherWidget.ACTION_UPDATE, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        });
        refresh = (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        setting = (ImageView) findViewById(R.id.settings);
        setting.setOnClickListener(this);

        refreshTimeText = (TextView) findViewById(R.id.latest_refresh_time);
        loadProgressView = findViewById(R.id.loading_progress_view);
        progressText = (TextView) findViewById(R.id.progress_text);
        indicatorBar = (LinearLayout) findViewById(R.id.indicator_bar);
        mCityTitle = (TextView) findViewById(R.id.title);
        mIsGps = (ImageView) findViewById(R.id.main_is_gps);

    }

    private void initMenu(LinearLayout v) {
        mShowMenu = v.findViewById(R.id.show_menu);
        mShowMenu.setOnClickListener(menuItemOnClickListener);
        mExpandShow = (ImageView) v.findViewById(R.id.expand_view);
        mMenuText = (TextView) v.findViewById(R.id.menu_text);
        mLine = v.findViewById(R.id.bottom_line);

        mExpandView = (ExpandView) v.findViewById(R.id.expandView);
        mExpandView.setContentView();

        menuAutoRefresh = v.findViewById(R.id.menu_auto_refresh);
        menuAutoRefresh.setOnClickListener(menuItemOnClickListener);
        menuCheckAutoRefresh = (Switch) v.findViewById(R.id.menu_check_auto_refresh);
        menuCheckAutoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                int time;
                if (isChecked) {
                    editor.putBoolean(SETTINGS_AUTO_REFRESH_ENABLE, true);
                    time = sp.getInt(SETTINGS_AUTO_REFRESH, getResources()
                            .getInteger(R.integer.config_auto_refresh));
                } else {
                    editor.putBoolean(SETTINGS_AUTO_REFRESH_ENABLE, false);
                    if (mExpandView.isExpand()) {
                        mExpandView.collapse();
                        mExpandShow.setImageResource(R.drawable.expand);
                        mLine.setVisibility(View.GONE);
                    }
                    time = sp.getInt(SETTINGS_AUTO_REFRESH, getResources()
                            .getInteger(R.integer.config_auto_refresh));
                }
                switch (time) {
                    case SETTINGS_AUTO_REFRESH_6H:
                        if (!sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                                .getBoolean(R.bool.config_auto_refresh_enable))) {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_checked);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_normal);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_normal);
                            mMenuText.setText(R.string.menu_auto_refresh_6h);
                        } else {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_checked_disable);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            mMenuText.setText(R.string.menu_auto_refresh_on_off);
                        }
                        break;
                    case SETTINGS_AUTO_REFRESH_12H:
                        if (!sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                                .getBoolean(R.bool.config_auto_refresh_enable))) {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_normal);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_checked);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_normal);
                            mMenuText.setText(R.string.menu_auto_refresh_12h);
                        } else {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_checked_disable);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            mMenuText.setText(R.string.menu_auto_refresh_on_off);
                        }
                        break;
                    case SETTINGS_AUTO_REFRESH_24H:
                        if (!sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                                .getBoolean(R.bool.config_auto_refresh_enable))) {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_normal);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_normal);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_checked);
                            mMenuText.setText(R.string.menu_auto_refresh_24h);
                        } else {
                            menuCheckAuto6h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            menuCheckAuto12h
                                    .setImageResource(R.drawable.checkbox_normal_disable);
                            menuCheckAuto24h
                                    .setImageResource(R.drawable.checkbox_checked_disable);
                            mMenuText.setText(R.string.menu_auto_refresh_on_off);
                        }
                        break;
                    default:
                        break;
                }
                editor.commit();
                if (!sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                        .getBoolean(R.bool.config_auto_refresh_enable))) {
                    setAutoRefreshAlarm(MainActivity.this,
                            SETTINGS_AUTO_REFRESH_INVALID);
                } else {
                    setAutoRefreshAlarm(MainActivity.this, time);
                }
                Log.d(TAG, "menu_auto_refresh");
            }
        });

        menuAuto6 = v.findViewById(R.id.menu_auto_6h);
        menuAuto6.setOnClickListener(menuItemOnClickListener);
        menuCheckAuto6h = (ImageView) v.findViewById(R.id.menu_check_auto_6h);

        menuAuto12 = v.findViewById(R.id.menu_auto_12h);
        menuAuto12.setOnClickListener(menuItemOnClickListener);
        menuCheckAuto12h = (ImageView) v.findViewById(R.id.menu_check_auto_12h);

        menuAuto24 = v.findViewById(R.id.menu_auto_24h);
        menuAuto24.setOnClickListener(menuItemOnClickListener);
        menuCheckAuto24h = (ImageView) v.findViewById(R.id.menu_check_auto_24h);

        menuWifiOnly = v.findViewById(R.id.menu_wifi_only);
        menuWifiOnly.setOnClickListener(menuItemOnClickListener);

        menuCheckWifiOnly = (Switch) v.findViewById(R.id.menu_check_wifi_only);
        menuCheckWifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = getSharedPreferences(SETTINGS_SP, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                if (isChecked) {
                    menuCheckWifiOnly.setChecked(true);
                    editor.putBoolean(SETTINGS_WIFI_ONLY, true);
                } else {
                    editor.putBoolean(SETTINGS_WIFI_ONLY, false);
                    menuCheckWifiOnly.setChecked(false);
                }
                editor.commit();
            }
        });

        menuSetCity = v.findViewById(R.id.menu_set_city);
        menuSetCity.setOnClickListener(menuItemOnClickListener);

        SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                Context.MODE_PRIVATE);
        boolean isAutoRefreshEnable = sp.getBoolean(
                SETTINGS_AUTO_REFRESH_ENABLE,
                getResources().getBoolean(R.bool.config_auto_refresh_enable));
        if (isAutoRefreshEnable) {
            menuCheckAutoRefresh.setChecked(true);
        } else {
            menuCheckAutoRefresh.setChecked(false);
        }

        switch (sp.getInt(SETTINGS_AUTO_REFRESH,
                    getResources().getInteger(R.integer.config_auto_refresh))) {
            case SETTINGS_AUTO_REFRESH_6H:
                if (isAutoRefreshEnable) {
                    menuCheckAuto6h.setImageResource(R.drawable.checkbox_checked);
                    menuCheckAuto12h.setImageResource(R.drawable.checkbox_normal);
                    menuCheckAuto24h.setImageResource(R.drawable.checkbox_normal);
                    mMenuText.setText(R.string.menu_auto_refresh_6h);
                } else {
                    menuCheckAuto6h
                        .setImageResource(R.drawable.checkbox_checked_disable);
                    menuCheckAuto12h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    menuCheckAuto24h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    mMenuText.setText(R.string.menu_auto_refresh_on_off);
                }
                break;
            case SETTINGS_AUTO_REFRESH_12H:
                if (isAutoRefreshEnable) {
                    menuCheckAuto6h.setImageResource(R.drawable.checkbox_normal);
                    menuCheckAuto12h.setImageResource(R.drawable.checkbox_checked);
                    menuCheckAuto24h.setImageResource(R.drawable.checkbox_normal);
                    mMenuText.setText(R.string.menu_auto_refresh_12h);
                } else {
                    menuCheckAuto6h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    menuCheckAuto12h
                        .setImageResource(R.drawable.checkbox_checked_disable);
                    menuCheckAuto24h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    mMenuText.setText(R.string.menu_auto_refresh_on_off);
                }
                break;
            case SETTINGS_AUTO_REFRESH_24H:
                if (isAutoRefreshEnable) {
                    menuCheckAuto6h.setImageResource(R.drawable.checkbox_normal);
                    menuCheckAuto12h.setImageResource(R.drawable.checkbox_normal);
                    menuCheckAuto24h.setImageResource(R.drawable.checkbox_checked);
                    mMenuText.setText(R.string.menu_auto_refresh_24h);
                } else {
                    menuCheckAuto6h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    menuCheckAuto12h
                        .setImageResource(R.drawable.checkbox_normal_disable);
                    menuCheckAuto24h
                        .setImageResource(R.drawable.checkbox_checked_disable);
                    mMenuText.setText(R.string.menu_auto_refresh_on_off);
                }
                break;
            default:
                break;
        }

        if (sp.getBoolean(SETTINGS_WIFI_ONLY,
                    getResources().getBoolean(R.bool.config_wifi_only_enable))) {
            menuCheckWifiOnly.setChecked(true);
        } else {
            menuCheckWifiOnly.setChecked(false);
        }
    }

    private void updateUI() {
        Log.d(TAG, "MainActivity - updateUI");
        Log.d(TAG, "Weather Info Size: " + mWeatherInfoList.size());
        if (mWeatherInfoList.size() < 1
                && mWeatherInfoList.get(0).getForecasts().size() < FORECAST_DAY) {
            Log.w(TAG, "update Failed");
            return;
        }

        String temperature = "";
        String tmp = "";
        WeatherInfo info = null;

        defScreen = 0;
        String defWoeid = WeatherDataUtil.getInstance().getDefaultCityWoeid(
                MainActivity.this);
        Log.d(TAG, "defWoeid=" + defWoeid);

        weatherInfoMainContainer.removeAllViews();
        for (int i = 0; i < mWeatherInfoList.size(); i++) {
            info = mWeatherInfoList.get(i);

            if (defWoeid.equals(info.getWoeid())) {
                defScreen = i;
                Log.d(TAG, "defScreen=" + defScreen);
            }

            Locale locale = Locale.getDefault();
            String datePattern = DateFormat.getBestDateTimePattern(locale, "EEMMMMd");
            Date now = new Date();
            String date = new SimpleDateFormat(datePattern, locale).format(now) + " ";
            temperature = info.getCondition().getTemp() + "℃";
            tmp = info.getForecasts().get(0).getLow() + "℃ /"
                + info.getForecasts().get(0).getHigh() + "℃";
            String text = info.getCondition().getText();
            int code = Integer.parseInt(info.getCondition().getCode());
            int resId;
            boolean isnight = WeatherDataUtil.getInstance().isNight();
            resId = WeatherDataUtil.getInstance()
                .getWeatherImageResourceByCode(code, isnight, false);
            if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId) {
                resId = WeatherDataUtil.getInstance()
                    .getWeatherImageResourceByText(
                            info.getCondition().getText(), isnight, false);
            }

            weatherInfoMainView = new WeatherInfoMainView(MainActivity.this);
            weatherInfoMainView.bindView(date, info.getName(), resId, text,
                    temperature, tmp, info.isGps());
            for (int j = 1; j < FORECAST_DAY; j++) {
                weatherInfoMainView.updateForeCastItem(j, info.getForecasts().get(j));
            }
            weatherInfoMainContainer.addView(weatherInfoMainView);
        }

        weatherInfoMainContainer.setDefaultScreen(defScreen);

        updateIndicatorBar();


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date mDate = new Date(mWeatherInfoList.get(defScreen).getUpdateTime());
        String refreshTime = getResources().getString(R.string.refresh_time,
                format.format(mDate));
        refreshTimeText.setText(refreshTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (WeatherDataUtil.getInstance()
                .getNeedUpdateMainUI(MainActivity.this)) {
            showLoadingProgress(true, R.string.progress_refresh);
            Log.d(TAG, "Load onResume");
            WeatherDataUtil.getInstance().setNeedUpdateMainUI(
                    MainActivity.this, false);
            setWeatherFromBD();
            showLoadingProgress(false, R.string.progress_refresh);
        }

        if (menuState == MenuState.CLOSE) {
            Log.d(TAG, "MenuState.CLOSE");
            if(null == mWeatherInfoList || mWeatherInfoList.isEmpty()) {
                Log.d(TAG, "NO info");
                refresh.setEnabled(false);
                finish();
            } else if(!refresh.isEnabled()) {
                Log.d(TAG, "Enable refresh");
                if (loadProgressView.getVisibility() != View.VISIBLE) {
                    refresh.setEnabled(true);
                } else {
                    Log.d(TAG, "loading");
                }
            }
        } else {
            Log.d(TAG, "MenuState.OPEN");
            if(null == mWeatherInfoList || mWeatherInfoList.isEmpty()) {
                Log.d(TAG, "NO info");
                refresh.setEnabled(false);
                finish();
            }
        }
        updateUi();
    }

    private void updateUi() {
        SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                Context.MODE_PRIVATE);
        if (sp.getBoolean(SETTINGS_WIFI_ONLY, getResources()
                .getBoolean(R.bool.config_wifi_only_enable))) {
            if (Utils.isNetworkTypeWifi(MainActivity.this)) {
                setWeatherFromInternet();
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.toast_wifi_only_mode,
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG,
                        "SETTINGS_WIFI_ONLY, network type NOT WIFI.");
            }
        } else {
            if (Utils.isNetworkAvailable(MainActivity.this)) {
                setWeatherFromInternet();
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.toast_net_inavailable,
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Refresh BTN, network NOT available");
            }
        }
    }

    private void updateIndicatorBar() {
        indicatorBar.removeAllViews();
        ImageView indicatorImage;
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 5, 5, 5);
        for (int i = 0; i < mWeatherInfoList.size(); i++) {
            indicatorImage = new ImageView(MainActivity.this);
            indicatorImage.setLayoutParams(lp);
            if (defScreen == i) {
                indicatorImage.setImageResource(R.drawable.point_current);
                mCityTitle.setText(mWeatherInfoList.get(defScreen).getName());
                mIsGps.setVisibility(mWeatherInfoList.get(defScreen).isGps() ? View.VISIBLE : View.GONE);
            } else {
                indicatorImage.setImageResource(R.drawable.point);
            }
            indicatorBar.addView(indicatorImage, i);
        }
    }

    private void setWeatherFromInternet() {
        Log.d(TAG, "setWeatherFromInternet");
        showLoadingProgress(true, R.string.progress_refresh);
        if (mWeatherInfoList == null || mWeatherInfoList.size() == 0 || mWeatherInfoList.get(defScreen) == null) return;
        if (!mInternetWorker.updateWeather(mWeatherInfoList.get(defScreen))) {
            showLoadingProgress(false, R.string.progress_refresh);
        }

    }

    private void setWeatherFromBD() {
        Log.d(TAG, "setWeatherFromBD");

        mWeatherInfoList = LocationApplication.mModel.getWeatherInfos();
        if (mWeatherInfoList.size() > 0
                && mWeatherInfoList.get(0).getForecasts().size() >= FORECAST_DAY) {
            updateUI();
            startUpdateService(MainActivity.this, WeatherWidget.ACTION_UPDATE,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    private void showLoadingProgress(boolean show, int textId) {
        Log.d(TAG, "showLoadingProgress:" + show);
        if (show) {
            loadProgressView.setVisibility(View.VISIBLE);
            progressText.setText(textId);
            refresh.setEnabled(false);
            setting.setEnabled(false);
            weatherInfoMainContainer.setEnabled(false);
            weatherInfoMainContainer.setTouchMove(false);
        } else {
            loadProgressView.setVisibility(View.GONE);
            refresh.setEnabled(true);
            setting.setEnabled(true);
            weatherInfoMainContainer.setEnabled(true);
            weatherInfoMainContainer.setTouchMove(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && menuState == MenuState.OPEN) {
            showMenu(false);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        showMenu(false);
    }

    private void showMenu(boolean show) {
        Log.d(TAG, "showMenu:" + show);
        initMenu(menuView);
        if (show) {
            menuState = MenuState.OPEN;
            dialog.show();
        } else {
            dialog.dismiss();
            menuState = MenuState.CLOSE;
        }
    }

    private View.OnClickListener menuItemOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.show_menu: {
                    if (mExpandView.isExpand()) {
                        mExpandView.collapse();
                        mExpandShow.setImageResource(R.drawable.expand);
                        mLine.setVisibility(View.GONE);
                    } else {
                        mExpandView.expand();
                        mExpandShow.setImageResource(R.drawable.collapse);
                        mLine.setVisibility(View.VISIBLE);
                    }
                    break;
                }
                case R.id.menu_auto_6h: {
                    menuAutoReflashTimeChecked(SETTINGS_AUTO_REFRESH_6H);
                    SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                            Context.MODE_PRIVATE);
                    if (sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                            .getBoolean(R.bool.config_auto_refresh_enable))) {
                        mMenuText.setText(R.string.menu_auto_refresh_6h);
                    }
                    break;
                }
                case R.id.menu_auto_12h: {
                    menuAutoReflashTimeChecked(SETTINGS_AUTO_REFRESH_12H);
                    SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                            Context.MODE_PRIVATE);
                    if (sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                            .getBoolean(R.bool.config_auto_refresh_enable))) {
                        mMenuText.setText(R.string.menu_auto_refresh_12h);
                    }
                    break;
                }
                case R.id.menu_auto_24h: {
                    menuAutoReflashTimeChecked(SETTINGS_AUTO_REFRESH_24H);
                    SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                            Context.MODE_PRIVATE);
                    if (sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                            .getBoolean(R.bool.config_auto_refresh_enable))) {
                        mMenuText.setText(R.string.menu_auto_refresh_24h);
                    }
                    break;
                }
                case R.id.menu_wifi_only: {
                    SharedPreferences sp = getSharedPreferences(SETTINGS_SP, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    if (sp.getBoolean(SETTINGS_WIFI_ONLY, getResources()
                                .getBoolean(R.bool.config_wifi_only_enable))) {
                        editor.putBoolean(SETTINGS_WIFI_ONLY, false);
                        menuCheckWifiOnly.setChecked(false);
                    } else {
                        menuCheckWifiOnly.setChecked(true);
                        editor.putBoolean(SETTINGS_WIFI_ONLY, true);
                    }
                    editor.commit();
                    break;
                }
                case R.id.menu_set_city:
                    Intent intent = new Intent(MainActivity.this,
                            CityMangerActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    private void menuAutoReflashTimeChecked(int checkedTime) {
        SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
                Context.MODE_PRIVATE);
        if (!sp.getBoolean(SETTINGS_AUTO_REFRESH_ENABLE, getResources()
                    .getBoolean(R.bool.config_auto_refresh_enable))) {
            Log.i(TAG, "Auto reflash NOT enable.");
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        switch (checkedTime) {
            case SETTINGS_AUTO_REFRESH_6H:
                menuCheckAuto6h.setImageResource(R.drawable.checkbox_checked);
                menuCheckAuto12h.setImageResource(R.drawable.checkbox_normal);
                menuCheckAuto24h.setImageResource(R.drawable.checkbox_normal);

                editor.putInt(SETTINGS_AUTO_REFRESH, checkedTime);
                break;
            case SETTINGS_AUTO_REFRESH_12H:
                menuCheckAuto6h.setImageResource(R.drawable.checkbox_normal);
                menuCheckAuto12h.setImageResource(R.drawable.checkbox_checked);
                menuCheckAuto24h.setImageResource(R.drawable.checkbox_normal);

                editor.putInt(SETTINGS_AUTO_REFRESH, checkedTime);
                break;
            case SETTINGS_AUTO_REFRESH_24H:
                menuCheckAuto6h.setImageResource(R.drawable.checkbox_normal);
                menuCheckAuto12h.setImageResource(R.drawable.checkbox_normal);
                menuCheckAuto24h.setImageResource(R.drawable.checkbox_checked);

                editor.putInt(SETTINGS_AUTO_REFRESH, checkedTime);
                break;

            default:
                break;
        }

        editor.commit();

        setAutoRefreshAlarm(MainActivity.this, checkedTime);
    }

    private void setAutoRefreshAlarm(Context context, int time) {
        Log.d(TAG, "setAutoRefreshAlarm, " + time);

        long deltaTime = WeatherDataUtil.getRefreshDelta(MainActivity.this,
                time);

        AlarmManager alarmManager = (AlarmManager) context
            .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(WeatherAction.ACTION_AUTO_REFRESH);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0,
                intent, 0);

        switch (time) {
            case SETTINGS_AUTO_REFRESH_6H:
            case SETTINGS_AUTO_REFRESH_12H:
            case SETTINGS_AUTO_REFRESH_24H:
                alarmManager.cancel(operation);
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + deltaTime, operation);
                break;

            default:
                Log.d(TAG, "setAutoRefreshAlarm, " + time);
                alarmManager.cancel(operation);
        }
    }

    private void startUpdateService(Context context, String action, int widgetId) {
        Log.d(TAG, "startUpdateService");
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(action);
        intent.setData(Uri.parse(String.valueOf(widgetId)));
        context.startService(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.refresh:
                updateUi();
                break;
            case R.id.settings:
                showMenu(true);
                break;
            default:
                break;
        }
    }
}
