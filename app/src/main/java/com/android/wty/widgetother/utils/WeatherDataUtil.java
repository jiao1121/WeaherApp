package com.android.wty.widgetother.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.wty.R;
import com.android.wty.MainActivity;

public class WeatherDataUtil {
    private static final String TAG = "Gweather.WeatherDataUtil";

    public final static String WEATHER_SP = "gweather";

    public static final float INVALID_LOCATION = -1000;

    public static final long AUTO_REFRESH_TIME_1 = 5 * 60 * 60 * 1000l;
    public static final long AUTO_REFRESH_TIME_2 = 11 * 60 * 60 * 1000l;
    public static final long AUTO_REFRESH_TIME_3 = 17 * 60 * 60 * 1000l;
    public static final long AUTO_REFRESH_TIME_4 = 23 * 60 * 60 * 1000l;

    public static final long TIME_ONE_DAY = 24 * 60 * 60 * 1000l;

    private static final boolean isDebugImg = false;

    public static final int INVALID_WEAHTER_RESOURCE = -1;
    public static final String DEFAULT_WOEID_GPS = "woeid_gps";

    public static final int CODE_THUNDERSTORMS = 4;
    public static final int CODE_RAIN_AND_SNOW = 5;
    public static final int CODE_SHOWERS = 11;
    public static final int CODE_RAIN = 12;
    public static final int CODE_SNOW_SHOWERS = 14;
    public static final int CODE_BREEZY = 23;//有微风
    public static final int CODE_WINDY = 24;//有风
    public static final int CODE_CLOUDY = 26;
    public static final int CODE_MOSTLY_CLOUDY = 28;
    public static final int CODE_PARTLY_CLOUDY = 29;
    public static final int CODE_PARTLY_CLOUDY_2 = 30;
    public static final int CODE_CLEAR = 31;
    public static final int CODE_SUNNY = 32;
    public static final int CODE_MOSTLY_CLEAR = 33;
    public static final int CODE_MOSTLY_SUNNY = 34;
    public static final int CODE_SCATTERED_SHOWERS = 39;
    //public static final int CODE_SCATTERED_SHOWERS = 45;//零星阵雨
    public static final int CODE_SCATTERED_THUNDERSTORMS = 47;//零星雷雨

    public static final int CODE_TORNADO = 0;
    public static final int CODE_STORM= 1;
    public static final int CODE_HURRICANE = 2;
    public static final int CODE_SEVERE_THUNDERSTORMS = 3;
    public static final int CODE_MIXED_RAIN_SLEET = 6;
    public static final int CODE_MIXED_SNOW_SLEET = 7;
    public static final int CODE_FREEZING_DRIZZLE = 8;
    public static final int CODE_DRIZZLE = 9;
    public static final int CODE_FREEZING_RAIN = 10;
    public static final int CODE_BLOWING_SNOW = 15;
    public static final int CODE_SNOW = 16;
    public static final int CODE_HAIL = 17;
    public static final int CODE_SLEET = 18;
    public static final int CODE_DUST = 19;
    public static final int CODE_FOGGY = 20;
    public static final int CODE_HAZE = 21;
    public static final int CODE_SMOKY = 22;
    public static final int CODE_COLD = 25;
    public static final int CODE_MIXED_RAIN_HAIL = 35;
    public static final int CODE_HOT = 36;

    public static final String TEXT_CLOUDY = "cloudy";
    public static final String TEXT_SUNNY = "sunny";
    public static final String TEXT_CLEAR = "clear";
    public static final String TEXT_SHOWERS = "showers";
    public static final String TEXT_THUNDERSTORMS = "thunderstorms";
    public static final String TEXT_RAIN = "rain";
    //下面都是自己猜的
    public static final String TEXT_FOG = "fog";
    public static final String TEXT_SNOW = "snow";
    public static final String TEXT_SLEET = "sleet";
    public static final String TEXT_SAND = "sand";

    public static final String TEXT_TORNADO = "tornado";
    public static final String TEXT_STORM= "tropical storm";
    public static final String TEXT_HURRICANE = "hurricane";
    public static final String TEXT_SEVERE_THUNDERSTORMS = "severe thunderstorms";
    public static final String TEXT_MIXED_RAIN_SLEET = "mixed rain and sleet";
    public static final String TEXT_MIXED_SNOW_SLEET = "mixed snow and sleet";
    public static final String TEXT_FREEZING_DRIZZLE = "freezing drizzle";
    public static final String TEXT_DRIZZLE = "drizzle";
    public static final String TEXT_FREEZING_RAIN = "freezing rain";
    public static final String TEXT_BLOWING_SNOW = "snow";
    public static final String TEXT_HAIL = "hail";
    public static final String TEXT_DUST = "sleet";
    public static final String TEXT_FOGGY = "dust";
    public static final String TEXT_HAZE = "foggy";
    public static final String TEXT_SMOKY = "haze";
    public static final String TEXT_COLD = "cold";
    public static final String TEXT_MIXED_RAIN_HAIL = "mixed rain and hail";
    public static final String TEXT_HOT = "hot";
    public static final String TEXT_SCATTERED_THUNDERSTORMS = "scattered thundershowers";
    public static final String TEXT_SCATTERED_SNOW_THUNDERSTORMS = "scattered snow showers";

    private static WeatherDataUtil mWeatherDataUtil;
    private WeatherDataUtil(){}
    public static WeatherDataUtil getInstance() {
        if(mWeatherDataUtil == null) {
            mWeatherDataUtil = new WeatherDataUtil();
        }
        return mWeatherDataUtil;
    }

    public int getWeatherImageResourceByCode(int code, boolean isnight, boolean isWidget) {
        if (isDebugImg) {
            isnight = true;
            if (isWidget) {
                if (isnight) {
                    return R.drawable.widget42_icon_sun_day_night;
                } else {
                    return R.drawable.widget42_icon_sun_day;
                }
            } else {
                if (isnight) {
                    return R.drawable.weather_icon_sun_day_night;
                } else {
                    return R.drawable.weather_icon_sun_day;
                }
            }
        }
        switch (code) {
            case CODE_CLOUDY:
            case CODE_MOSTLY_CLOUDY:
            case CODE_PARTLY_CLOUDY:
            case CODE_PARTLY_CLOUDY_2:
                if (isWidget) {
                    return R.drawable.widget42_icon_cloudy_day;
                } else {
                    return R.drawable.weather_icon_cloudy_day;
                }
            case CODE_BREEZY:
            case CODE_WINDY:
                if (isWidget) {
                    return R.drawable.widget42_icon_windy_day;
                } else {
                    return R.drawable.weather_icon_windy_day;
                }
            case CODE_SUNNY:
            case CODE_MOSTLY_SUNNY:
            case CODE_CLEAR:
            case CODE_MOSTLY_CLEAR:
                if (isWidget) {
                    if (isnight) {
                        return R.drawable.widget42_icon_sun_day_night;
                    } else {
                        return R.drawable.widget42_icon_sun_day;
                    }
                } else {
                    if (isnight) {
                        return R.drawable.weather_icon_sun_day_night;
                    } else {
                        return R.drawable.weather_icon_sun_day;
                    }
                }
            case CODE_SCATTERED_SHOWERS:
            case CODE_SHOWERS:
                if (isWidget) {
                    return R.drawable.widget42_icon_dayu_day;
                } else {
                    return R.drawable.weather_icon_dayu_day;
                }
            case CODE_SNOW_SHOWERS:
                if (isWidget) {
                    return R.drawable.widget42_icon_daxue_day;
                } else {
                    return R.drawable.weather_icon_daxue_day;
                }
            case CODE_THUNDERSTORMS:
            case CODE_SCATTERED_THUNDERSTORMS:
                if (isWidget) {
                    return R.drawable.widget42_icon_leizhenyu_day;
                } else {
                    return R.drawable.weather_icon_leizhenyu_day;
                }
            case CODE_RAIN:
                if (isWidget) {
                    return R.drawable.widget42_icon_rain_day;
                } else {
                    return R.drawable.weather_icon_rain_day;
                }
            case CODE_RAIN_AND_SNOW:
                if (isWidget) {
                    return R.drawable.widget42_icon_yujiaxue_day;
                } else {
                    return R.drawable.weather_icon_yujiaxue_day;
                }
            case CODE_TORNADO:
                return R.drawable.weather_icon_tornado;
            case CODE_STORM:
                return R.drawable.weather_icon_storm;
            case CODE_HURRICANE:
                return R.drawable.weather_icon_hurricane;
            case CODE_SEVERE_THUNDERSTORMS:
                return R.drawable.wearher_icon_thunderstorms;
            case CODE_MIXED_RAIN_SLEET:
                return R.drawable.weather_icon_rain_sleet;
            case CODE_MIXED_SNOW_SLEET:
                return R.drawable.weather_icon_snow_sleet;
            case CODE_FREEZING_DRIZZLE:
                return R.drawable.weather_icon_fdrizzle;
            case CODE_DRIZZLE:
                return R.drawable.weather_icon_drizzle;
            case CODE_FREEZING_RAIN:
                return R.drawable.weather_icon_freezing_rain;
            case CODE_BLOWING_SNOW:
                return R.drawable.weather_icon_blowing_sonw;
            case CODE_SNOW:
                return R.drawable.weather_icon_snow;
            case CODE_HAIL:
                return R.drawable.weather_icon_hail;
            case CODE_DUST:
                return R.drawable.weather_icon_dust;
            case CODE_FOGGY:
                return R.drawable.weather_icon_foggy;
            case CODE_HAZE:
                return R.drawable.weather_icon_haze;
            case CODE_SMOKY:
                return R.drawable.weather_icon_smoky;
            case CODE_COLD:
                return R.drawable.weather_icon_cold;
            case CODE_MIXED_RAIN_HAIL:
                return R.drawable.weather_icon_rain_hail;
            case CODE_HOT:
                return R.drawable.weather_icon_hot;
            default:
                return INVALID_WEAHTER_RESOURCE;
        }

    }


    public int getWeatherTextResByCode(int code) {
        if (isDebugImg) {

        }
        switch (code) {
            case CODE_CLOUDY:
            case CODE_MOSTLY_CLOUDY:
            case CODE_PARTLY_CLOUDY:
            case CODE_PARTLY_CLOUDY_2:
                return R.string.weather_cloudy;
            case CODE_BREEZY:
                return R.string.weather_breezy;
            case CODE_WINDY:
                return R.string.weather_windy;
            case CODE_SUNNY:
            case CODE_MOSTLY_SUNNY:
                return R.string.weather_sunny;
            case CODE_CLEAR:
            case CODE_MOSTLY_CLEAR:
                return R.string.weather_clear;
            case CODE_SCATTERED_SHOWERS:
            case CODE_SHOWERS:
                return R.string.weather_showers;
            case CODE_SNOW_SHOWERS:
                return R.string.weather_snow_showers;
            case CODE_THUNDERSTORMS:
            case CODE_SCATTERED_THUNDERSTORMS:
                return R.string.weather_thunderstorms;
            case CODE_RAIN:
                return R.string.weather_rain;
            case CODE_RAIN_AND_SNOW:
                return R.string.weather_rain_and_snow;
            case CODE_TORNADO:
                return R.string.weather_tornado;
            case CODE_STORM:
                return R.string.weather_tropical;
            case CODE_HURRICANE:
                return R.string.weather_hurricane;
            case CODE_SEVERE_THUNDERSTORMS:
                return R.string.weather_severe;
            case CODE_MIXED_RAIN_SLEET:
                return R.string.weather_rain_sleet;
            case CODE_MIXED_SNOW_SLEET:
                return R.string.weather_snow_sleet;
            case CODE_FREEZING_DRIZZLE:
                return R.string.weather_fdrizzle;
            case CODE_DRIZZLE:
                return R.string.weather_drizzle;
            case CODE_FREEZING_RAIN:
                return R.string.weather_freezing;
            case CODE_BLOWING_SNOW:
                return R.string.weather_blowing;
            case CODE_SNOW:
                return R.string.weather_snow;
            case CODE_HAIL:
                return R.string.weather_hail;
            case CODE_SLEET:
                return R.string.weather_sleet;
            case CODE_DUST:
                return R.string.weather_dust;
            case CODE_FOGGY:
                return R.string.weather_foggy;
            case CODE_HAZE:
                return R.string.weather_haze;
            case CODE_SMOKY:
                return R.string.weather_smoky;
            case CODE_COLD:
                return R.string.weather_cold;
            case CODE_MIXED_RAIN_HAIL:
                return R.string.weather_mixed;
            case CODE_HOT:
                return R.string.weather_hot;
            default:
                return INVALID_WEAHTER_RESOURCE;
        }

    }

    public int getWeatherImageResourceByText(String text, boolean isnight, boolean isWidget) {
        if (isCondition(TEXT_SUNNY, text) || isCondition(TEXT_CLEAR, text)) {
            if (isWidget) {
                if (isnight) {
                    return R.drawable.widget42_icon_sun_day_night;
                } else {
                    return R.drawable.widget42_icon_sun_day;
                }
            } else {
                if (isnight) {
                    return R.drawable.weather_icon_sun_day_night;
                } else {
                    return R.drawable.weather_icon_sun_day;
                }
            }
        } else if (isCondition(TEXT_CLOUDY, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_cloudy_day;
            } else {
                return R.drawable.weather_icon_cloudy_day;
            }
        } else if (isCondition(TEXT_THUNDERSTORMS, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_leizhenyu_day;
            } else {
                return R.drawable.weather_icon_leizhenyu_day;
            }
        } else if (isCondition(TEXT_SHOWERS, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_dayu_day;
            } else {
                return R.drawable.weather_icon_dayu_day;
            }
        } else if (isCondition(TEXT_RAIN, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_rain_day;
            } else {
                return R.drawable.weather_icon_rain_day;
            }
        } else if (isCondition(TEXT_SNOW, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_xue_day;
            } else {
                return R.drawable.weather_icon_xue_day;
            }
        } else if (isCondition(TEXT_FOG, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_fog_day;
            } else {
                return R.drawable.weather_icon_fog_day;
            }
        } else if (isCondition(TEXT_SLEET, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_yujiaxue_day;
            } else {
                return R.drawable.weather_icon_yujiaxue_day;
            }
        } else if (isCondition(TEXT_SAND, text)) {
            if (isWidget) {
                return R.drawable.widget42_icon_sandstorm_day;
            } else {
                return R.drawable.weather_icon_sandstorm_day;
            }
        } else if (isCondition(TEXT_TORNADO, text)) {
            return R.drawable.weather_icon_tornado;
        } else if (isCondition(TEXT_STORM, text)) {
            return R.drawable.weather_icon_storm;
        } else if (isCondition(TEXT_HURRICANE, text)) {
            return R.drawable.weather_icon_hurricane;
        } else if (isCondition(TEXT_SEVERE_THUNDERSTORMS, text)) {
            return R.drawable.wearher_icon_thunderstorms;
        } else if (isCondition(TEXT_MIXED_RAIN_SLEET, text)) {
            return R.drawable.weather_icon_rain_sleet;
        } else if (isCondition(TEXT_MIXED_SNOW_SLEET, text)) {
            return R.drawable.weather_icon_snow_sleet;
        } else if (isCondition(TEXT_FREEZING_DRIZZLE, text)) {
            return R.drawable.weather_icon_fdrizzle;
        } else if (isCondition(TEXT_DRIZZLE, text)) {
            return R.drawable.weather_icon_drizzle;
        } else if (isCondition(TEXT_FREEZING_RAIN, text)) {
            return R.drawable.weather_icon_freezing_rain;
        } else if (isCondition(TEXT_BLOWING_SNOW, text)) {
            return R.drawable.weather_icon_blowing_sonw;
        } else if (isCondition(TEXT_HAIL, text)) {
            return R.drawable.weather_icon_hail;
        } else if (isCondition(TEXT_DUST, text)) {
            return R.drawable.weather_icon_dust;
        } else if (isCondition(TEXT_FOGGY, text)) {
            return R.drawable.weather_icon_foggy;
        } else if (isCondition(TEXT_HAZE, text)) {
            return R.drawable.weather_icon_haze;
        } else if (isCondition(TEXT_SMOKY, text)) {
            return R.drawable.weather_icon_smoky;
        } else if (isCondition(TEXT_COLD, text)) {
            return R.drawable.weather_icon_cold;
        } else if (isCondition(TEXT_HOT, text)) {
            return R.drawable.weather_icon_hot;
        } else if (isCondition(TEXT_MIXED_RAIN_HAIL, text)) {
            return  R.drawable.weather_icon_rain_day;
        } else if (isCondition(TEXT_SCATTERED_THUNDERSTORMS, text)) {
            return R.drawable.weather_icon_leizhenyu_day;
        } else if (isCondition(TEXT_SCATTERED_SNOW_THUNDERSTORMS, text)) {
            return R.drawable.weather_icon_blowing_sonw;
        }
        if (isWidget) {
            return R.drawable.widget42_icon_nodata;
        } else {
            return R.drawable.weather_icon_nodata;
        }
    }

    private boolean isCondition(String condition, String text) {
        text = text.toLowerCase();
        if (text.contains(condition)) {
            return true;
        }
        return false;
    }

    public boolean isNight() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String hour = sdf.format(new Date());
        int h = Integer.parseInt(hour);

        if(h < 7 || h > 18) {
            return true;
        }

        return false;
    }

    public String getDefaultCityWoeid(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE);
        return sp.getString("woeid", "");
    }

    public void updateDefaultCityWoeid(Context context, String woeid) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE).edit();
        editor.putString("woeid", woeid);
        editor.commit();
    }

    public boolean getNeedUpdateMainUI(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE);
        return sp.getBoolean("main_update", false);
    }

    public void setNeedUpdateMainUI(Context context, boolean needUpdate) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE).edit();
        editor.putBoolean("main_update", needUpdate);
        editor.commit();
    }

    public long getRefreshTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE);
        return sp.getLong("refresh_time", -1l);
    }

    public void setRefreshTime(Context context, long time) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WEATHER_SP, Context.MODE_PRIVATE).edit();
        editor.putLong("refresh_time", time);
        editor.commit();
    }

    /*public static void updateLocationCityInfo(Context context, CityInfo info) {
      ContentResolver mContentResolver = context.getContentResolver();
      Uri uri = Uri.parse("content://com.gweather.app.weather/gcity");
      Cursor cursor = mContentResolver.query(uri, null, null, null, null);
      String woeid = "";
      boolean hasInfo = false;
      if (cursor != null) {
      if (cursor.moveToFirst()) {
      int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
      woeid = cursor.getString(cursorIndex);
      hasInfo = true;
      }
      cursor.close();
      }
      Log.d(TAG, "updateLocationCityInfo, hasInfo=" + hasInfo);
      ContentValues values;
      values = new ContentValues();
      values.put(WeatherProvider.CITY_WOEID, info.getWoeid());
      values.put(WeatherProvider.CITY_NAME, info.getName());
      values.put(WeatherProvider.CITY_LAT, info.getLocationInfo().getLat());
      values.put(WeatherProvider.CITY_LON, info.getLocationInfo().getLon());
      values.put(WeatherProvider.CITY_SWLAT, info.getLocationInfo().getSouthWestLat());
      values.put(WeatherProvider.CITY_SWLON, info.getLocationInfo().getSouthWestLon());
      values.put(WeatherProvider.CITY_NELAT, info.getLocationInfo().getNorthEastLat());
      values.put(WeatherProvider.CITY_NELON, info.getLocationInfo().getNorthEastLon());
      if (hasInfo) {
      mContentResolver.update(
      uri,
      values,
      WeatherProvider.WOEID+"=?",
      new String[] {woeid});
      } else {
      mContentResolver.insert(uri, values);
      }
      }

      public static CityInfo getLocationCityInfo(Context context) {
      CityInfo info = new CityInfo();
      ContentResolver mContentResolver = context.getContentResolver();
      Uri uri = Uri.parse("content://com.gweather.app.weather/gcity");
      Cursor cursor = mContentResolver.query(uri, null, null, null, null);
      if (cursor != null) {
      if(cursor.moveToFirst()) {
      int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NAME);
      info.setName(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
      info.setWoeid(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_LAT);
      info.getLocationInfo().setLat(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_LON);
      info.getLocationInfo().setLon(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_SWLAT);
      info.getLocationInfo().setSouthWestLat(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_SWLON);
      info.getLocationInfo().setSouthWestLon(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NELAT);
      info.getLocationInfo().setNorthEastLat(cursor.getString(cursorIndex));
      cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NELON);
      info.getLocationInfo().setNorthEastLon(cursor.getString(cursorIndex));
      }

      cursor.close();
      }

      return info;
      }*/

    public static long getRefreshDelta(Context context, int time) {
        long currentTime = System.currentTimeMillis();
        long refreshtimeOld = WeatherDataUtil.getInstance().getRefreshTime(context);

        SimpleDateFormat sdf = new SimpleDateFormat("HH,mm,ss");
        String date = sdf.format(new Date());
        System.out.println("date=" + date);
        String[] s = date.split(",");
        long now = (((Integer.parseInt(s[0]) * 60) + Integer
                    .parseInt(s[1])) * 60 + Integer.parseInt(s[2])) * 1000l;

        long deltaTime = 0l;
        switch (time) {
            case MainActivity.SETTINGS_AUTO_REFRESH_6H:
                if (currentTime - refreshtimeOld > MainActivity.TIME_6H) {
                    deltaTime = 0;
                } else if (now >= AUTO_REFRESH_TIME_1 && now < AUTO_REFRESH_TIME_2) {
                    deltaTime = AUTO_REFRESH_TIME_2 - now;
                } else if (now >= AUTO_REFRESH_TIME_2 && now < AUTO_REFRESH_TIME_3) {
                    deltaTime = AUTO_REFRESH_TIME_3 - now;
                } else if (now >= AUTO_REFRESH_TIME_3 && now < AUTO_REFRESH_TIME_4) {
                    deltaTime = AUTO_REFRESH_TIME_4 - now;
                } else if (now >= AUTO_REFRESH_TIME_4) {
                    deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
                } else {
                    deltaTime = AUTO_REFRESH_TIME_1 - now;
                }
                if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
                    deltaTime = MainActivity.TIME_6H;
                }
                break;
            case MainActivity.SETTINGS_AUTO_REFRESH_12H:
                if (currentTime - refreshtimeOld > MainActivity.TIME_12H) {
                    deltaTime = 0;
                } else if (now >= AUTO_REFRESH_TIME_1 && now < AUTO_REFRESH_TIME_3) {
                    deltaTime = AUTO_REFRESH_TIME_3 - now;
                } else if (now >= AUTO_REFRESH_TIME_3) {
                    deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
                } else {
                    deltaTime = AUTO_REFRESH_TIME_1 - now;
                }
                if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
                    deltaTime = MainActivity.TIME_12H;
                }
                break;
            case MainActivity.SETTINGS_AUTO_REFRESH_24H:
                if (currentTime - refreshtimeOld > MainActivity.TIME_24H) {
                    deltaTime = 0;
                } else if (now >= AUTO_REFRESH_TIME_1) {
                    deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
                } else {
                    deltaTime = AUTO_REFRESH_TIME_1 - now;
                }

                if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
                    deltaTime = MainActivity.TIME_24H;
                }
                break;
            default:
                return MainActivity.TIME_24H;
        }
        return deltaTime;
    }
}
