package com.example.ionut.vremea2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

public class TestUtilities {

    static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    static final String TEST_LOCATION = "99705";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    private static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, TEST_DATE);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION);
        testValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);
        return testValues;
    }

    @Test
    static long insertNorthPoleLocationValues(Context context) {
        WeatherDbHelper dbHelper = new WeatherDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createNorthPoleLocationValues();

        long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

}
