package com.example.ionut.vremea2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

public class WeatherContract {

    static final String CONTENT_AUTHORITY = "com.example.ionut.vremea2";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_WEATHER = "weather";
    static final String PATH_LOCATION = "location";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    static final class LocationEntry implements BaseColumns {

        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        static final String TABLE_NAME = "location";
        static final String COLUMN_LOCATION_SETTING = "location_setting";
        static final String COLUMN_CITY_NAME = "city_name";
        static final String COLUMN_COORD_LAT = "coord_lat";
        static final String COLUMN_COORD_LONG = "coord_long";

        static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    static final class WeatherEntry implements BaseColumns {

        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        static final String TABLE_NAME = "weather";
        static final String COLUMN_LOC_KEY = "location_id";
        static final String COLUMN_DATE = "date";
        static final String COLUMN_WEATHER_ID = "weather_id";
        static final String COLUMN_SHORT_DESC = "short_desc";
        static final String COLUMN_MIN_TEMP = "min";
        static final String COLUMN_MAX_TEMP = "max";
        static final String COLUMN_HUMIDITY = "humidity";
        static final String COLUMN_PRESSURE = "pressure";
        static final String COLUMN_WIND_SPEED = "wind";
        static final String COLUMN_DEGREES = "degrees";

        static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        static Uri buildWeatherLocationWithStartDate(String locationSetting, long startDate) {
            long normalizedDate = normalizeDate(startDate);
            return CONTENT_URI.buildUpon()
                    .appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate))
                    .build();
        }

        static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
            return CONTENT_URI.buildUpon()
                    .appendPath(locationSetting)
                    .appendPath(Long.toString(normalizeDate(date)))
                    .build();
        }

        static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
}
