package com.android.wty.location;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

public class LocalWeather extends WwoApi {
    public static final String FREE_API_ENDPOINT = "http://api.worldweatheronline.com/free/v1/weather.ashx";
    public static final String PREMIUM_API_ENDPOINT = "http://api.worldweatheronline.com/premium/v1/weather.ashx";

    public LocalWeather(boolean freeAPI) {
        super(freeAPI);
        if (freeAPI) {
            apiEndPoint = FREE_API_ENDPOINT;
        } else {
            apiEndPoint = PREMIUM_API_ENDPOINT;
        }
    }

    public Data callAPI(String query) {
        return getLocalWeatherData(getInputStream(apiEndPoint + query));
    }

    public Data getLocalWeatherData(InputStream is) {
        Data weather = null;

        try {
            Log.d("WWO", "getLocalWeatherData");

            XmlPullParser xpp = getXmlPullParser(is);
            Log.e("fantao", "xml = " + xpp);

            weather = new Data();
            CurrentCondition cc = new CurrentCondition();
            weather.current_condition = cc;

            cc.temp_C = getTextForTag(xpp, "temp_C");
            cc.weatherIconUrl = getDecode(getTextForTag(xpp, "weatherIconUrl"));
            cc.weatherDesc = getDecode(getTextForTag(xpp, "weatherDesc"));

            Log.d("WWO", "getLocalWeatherData: "+cc.temp_C);
            Log.d("WWO", "getLocalWeatherData: "+cc.weatherIconUrl);
            Log.d("WWO", "getLocalWeatherData: "+cc.weatherDesc);
        } catch (Exception e) {

        }

        return weather;
    }

    public class Params extends RootParams {
        public String q;                    //required
        public String extra;
        public String num_of_days="1";        //required
        public String date;
        public String fx="no";
        public String cc;                    //default "yes"
        public String includeLocation;        //default "no"
        public String format;                //default "xml"
        public String show_comments="no";
        public String callback;
        public String key;                    //required

        public Params(String key) {
            num_of_days = "1";
            fx = "no";
            show_comments = "no";
            this.key = key;
        }

    public Params setQ(String q) {
            this.q = q;
            return this;
        }

        public Params setExtra(String extra) {
            this.extra = extra;
            return this;
        }

        public Params setNumOfDays(String num_of_days) {
            this.num_of_days = num_of_days;
            return this;
        }

        public Params setDate(String date) {
            this.date = date;
            return this;
        }

        public Params setFx(String fx) {
            this.fx = fx;
            return this;
        }

        public Params setCc(String cc) {
            this.cc = cc;
            return this;
        }

        public Params setIncludeLocation(String includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }

        public Params setFormat(String format) {
            this.format = format;
            return this;
        }

        public Params setShowComments(String showComments) {
            this.show_comments = showComments;
            return this;
        }

        public Params setCallback(String callback) {
            this.callback = callback;
            return this;
        }

        public Params setKey(String key) {
            this.key = key;
            return this;
        }
    }

    public class Data {
        public Request request;
        public CurrentCondition current_condition;
        public Weather weather;
    }

    public class Request {
        public String type;
        public String query;
    }

    public class CurrentCondition {
        public String observation_time;
        public String temp_C;
        public String weatherCode;
        public String weatherIconUrl;
        public String weatherDesc;
        public String windspeedMiles;
        public String windspeedKmph;
        public String winddirDegree;
        public String winddir16Point;
        public String precipMM;
        public String humidity;
        public String visibility;
        public String pressure;
        public String cloudcover;
    }

    public class Weather {
        public String date;
        public String tempMaxC;
        public String tempMaxF;
        public String tempMinC;
        public String tempMinF;
        public String windspeedMiles;
        public String windspeedKmph;
        public String winddirection;
        public String weatherCode;
        public String weatherIconUrl;
        public String weatherDesc;
        public String precipMM;
    }
}

