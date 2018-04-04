package com.android.wty.location;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

public class LocationSearch extends WwoApi {
    public static final String FREE_API_ENDPOINT = "http://api.worldweatheronline.com/free/v1/search.ashx";
    public static final String PREMIUM_API_ENDPOINT = "http://api.worldweatheronline.com/premium/v1/search.ashx";

    public LocationSearch(boolean freeAPI) {
        super(freeAPI);

        if(freeAPI) {
            apiEndPoint = FREE_API_ENDPOINT;
        } else {
            apiEndPoint = PREMIUM_API_ENDPOINT;
        }
    }

    public Data callAPI(String query) {
        return getLocationSearchData(getInputStream(apiEndPoint + query));
    }

    public Data getLocationSearchData(InputStream is) {
        Data location = null;

        try {
            Log.d("WWO", "getLocationSearchData");

            XmlPullParser xpp = getXmlPullParser(is);

            location = new Data();

            location.areaName = getDecode(getTextForTag(xpp, "areaName"));
            location.country = getDecode(getTextForTag(xpp, "country"));
            location.region = getDecode(getTextForTag(xpp, "region"));
            location.latitude = getTextForTag(xpp, "latitude");
            location.longitude = getTextForTag(xpp, "longitude");
            location.population = getTextForTag(xpp, "population");
            location.weatherUrl = getDecode(getTextForTag(xpp, "weatherUrl"));

        } catch (Exception e) {

        }

        return location;
    }

    public class Params extends RootParams {
        public String query;					//required
        public String num_of_results="1";
        public String timezone;
        public String popular;
        public String format;				//default "xml"
        public String key;					//required

        public Params(String key) {
            num_of_results = "1";
            this.key = key;
        }

        public Params setQuery(String query) {
            this.query = query;
            return this;
        }

        public Params setNumOfResults(String num_of_results) {
            this.num_of_results = num_of_results;
            return this;
        }

        public Params setTimezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Params setPopular(String popular) {
            this.popular = popular;
            return this;
        }

        public Params setFormat(String format) {
            this.format = format;
            return this;
        }

        public Params setKey(String key) {
            this.key = key;
            return this;
        }
    }

    public class Data {
        public String areaName;
        public String country;
        public String region;
        public String latitude;
        public String longitude;
        public String population;
        public String weatherUrl;
    }
}

