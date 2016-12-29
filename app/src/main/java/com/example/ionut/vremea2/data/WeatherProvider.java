package com.example.ionut.vremea2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class WeatherProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper databaseHelper;

    static final int WEATHER = 100;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static {
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                WeatherContract.LocationEntry.TABLE_NAME +
                " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                " = " + WeatherContract.LocationEntry.TABLE_NAME +
                "." + WeatherContract.LocationEntry._ID);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {// "weather/*/#"
            case WEATHER_WITH_LOCATION_AND_DATE: {
                Log.e("Weather Provider", "weather with location and date: ");
                Log.e("Weather Provider", "projection: " + projection);
                retCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION: {// "weather/*"
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            case WEATHER: {// "weather"
                retCursor = databaseHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case LOCATION: {// "location"
                retCursor = databaseHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        Log.e("Weather Provider", "location: " + locationSetting);
        Log.e("Weather Provider", "date: " + date);

        //location.location_setting = ? AND date = ?
        final String sLocationSettingAndDaySelection =
                WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";

        return sWeatherByLocationSettingQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }

    //location.location_setting = ? AND date >= ?
    static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ?
    static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        }
        else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case WEATHER: {
                long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if(_id > 0) returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if(_id > 0) returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case WEATHER: {
                rowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            case LOCATION: {
                rowsDeleted = db.delete(WeatherContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        if (null == selection) selection = "1";
        switch (match) {
            case WEATHER: {
                rowsUpdated = db.update(WeatherContract.WeatherEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case LOCATION: {
                rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public void shutdown() {
        databaseHelper.close();
        super.shutdown();
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/#", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);

        return matcher;
    }
}