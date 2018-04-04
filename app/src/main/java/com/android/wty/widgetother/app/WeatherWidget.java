package com.android.wty.widgetother.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.android.wty.R;
import com.android.wty.MainActivity;

public class WeatherWidget extends AppWidgetProvider {
    private static final String TAG = "GweatherWeatherWidgetO";

    public static final String ACTION_INIT = "action_init";
    public static final String ACTION_UPDATE = "action_update";
    public static Typeface TypeFaceYaHei;

    private ComponentName mComponentName;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        int[] newAppWidgetIds = appWidgetIds;
        if (appWidgetIds == null) {
            newAppWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(context,
                            WeatherWidget.class));
        }


        for (int appWidgetId : appWidgetIds) {
            updateClock(context, appWidgetManager, appWidgetId, 1);
        }
        startUpdateService(context, ACTION_INIT, AppWidgetManager.INVALID_APPWIDGET_ID);

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        context.stopService(new Intent(context, UpdateWidgetService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (null == intent) {
            return;
        }
        String action = intent.getAction();

        if (WeatherAction.ACTION_AUTO_REFRESH.equals(action)) {
            startUpdateService(context, action, AppWidgetManager.INVALID_APPWIDGET_ID);
        } else {
            AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);

            if (appWidgetManager != null) {
                int[] appWidgetIds = appWidgetManager
                    .getAppWidgetIds(getComponentName(context));
                for (int appWidgetId : appWidgetIds) {
                    updateClock(context, appWidgetManager, appWidgetId, 1);
                }
            }
        }
    }

    private void updateClock(Context context,
        AppWidgetManager appWidgetManager, int appWidgetId, float ratio) {


        RemoteViews widget = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        // Launch clock when clicking on the time in the widget only if not a
        // lock screen widget
        Bundle newOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (newOptions != null && newOptions.getInt(
                    AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1) != AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) {
            Log.v("guocl", "updateClock-----setOnClick ");
            Intent localIntent4 = new Intent();
            localIntent4.setComponent(new ComponentName(isAppInstalled(context, "com.android.deskclock"),
                        "com.android.deskclock.DeskClock"));
            widget.setOnClickPendingIntent(R.id.the_clock,
                    PendingIntent.getActivity(context, 0, localIntent4, 0));
            Intent localIntent5 = new Intent();
            localIntent5.setComponent(new ComponentName("com.android.calendar",
                        "com.android.calendar.AllInOneActivity"));

            PendingIntent localIntent6 = getSettingPendingIntent(context);

            widget.setOnClickPendingIntent(R.id.widget_img,
                    getWeatherActivityIntent(context));
        }

        refreshDate(context, widget);
        setClockSize(widget, context);
        // /M: change Android default design and show the AM/PM string to
        // widget. @{
        int amPmFontSize = (int) context.getResources().getDimension(
                R.dimen.main_ampm_font_size);

        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    public static String isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return packageName;
        } catch (PackageManager.NameNotFoundException e) {
            return "com.google.android.deskclock";
        }
    }

    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    public void setClockSize(RemoteViews clock, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat(get24HourMode(context) ? "HH:mm" : "hh:mm");
        String data = sdf.format(new Date());
        clock.setImageViewBitmap(R.id.the_clock, buildUpdate(data, context));

        ComponentName thisWidget = new ComponentName(context, WeatherWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, clock);
    }

    static Bitmap buildUpdate(String time, Context context) {
        Bitmap myBitmap = Bitmap.createBitmap(context.getResources().getInteger(R.integer.bitmap_width),
                context.getResources().getInteger(R.integer.bitmap_height), Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface tf= Typeface.createFromAsset(context.getAssets(),"fonts/leah_line.ttf");
        paint.setAntiAlias(true);
        paint.setTypeface(tf);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(context.getResources().getInteger(R.integer.clock_text_size));
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(time, context.getResources().getInteger(R.integer.draw_text_width),
               context.getResources().getInteger(R.integer.draw_text_height), paint);
        Paint sPaint = new Paint();
        sPaint.setAntiAlias(true);
        sPaint.setTypeface(tf);
        sPaint.setStyle(Paint.Style.STROKE);
        sPaint.setColor(Color.parseColor("#c0c0c0"));
        sPaint.setTextSize(context.getResources().getInteger(R.integer.clock_text_size));
        sPaint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(time, context.getResources().getInteger(R.integer.draw_text_width),
                context.getResources().getInteger(R.integer.draw_text_height), sPaint);
        return myBitmap;
    }

    private PendingIntent getScrollPendingIntent(Context context,
            final int appWidgetId) {
        Intent intent = new Intent(context, UpdateWidgetService.class)
            .setAction("com.weather.action.SCROLL").setData(
                    Uri.parse(String.valueOf(appWidgetId)));
        intent.putExtra("direction", "direction_next");
        return (PendingIntent.getService(context, 0, intent, 0));
    }

    private PendingIntent getWeatherActivityIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private PendingIntent getSettingPendingIntent(Context context) {
        Intent intent = new Intent("com.weather.action.SETTING");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return (PendingIntent.getActivity(context, 0, intent, 0));
    }

    private void refreshDate(Context context, RemoteViews widget) {
        Log.v(TAG, "refreshDate--- ");
        Locale locale = Locale.getDefault();
        String datePattern = DateFormat.getBestDateTimePattern(locale, "EEMMMMd");
        final Date now = new Date();
        widget.setTextViewText(R.id.date, new SimpleDateFormat(datePattern, locale).format(now));
    }

    private void startUpdateService(Context context, String action, int widgetId) {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(action);
        intent.setData(Uri.parse(String.valueOf(widgetId)));
        context.startService(intent);
    }

    /**
     * Create the component name for this class
     *
     * @param context
     *            The Context in which the widgets for this component are
     *            created
     * @return the ComponentName unique to DigitalAppWidgetProvider
     */
    private ComponentName getComponentName(Context context) {
        if (mComponentName == null) {
            mComponentName = new ComponentName(context, getClass());
            System.out.println(getClass().toString());
        }
        return mComponentName;
    }
}
