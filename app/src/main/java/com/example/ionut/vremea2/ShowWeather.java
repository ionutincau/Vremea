package com.example.ionut.vremea2;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ionut.vremea2.data.WeatherContract;

class ShowWeather implements LoaderManager.LoaderCallbacks<Cursor> {
    private Activity activity;
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter data_adapter;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    ShowWeather(Activity activity) {
        this.activity = activity;
        activity.getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        show_list();
    }

    private View show_list() {
        this.data_adapter = new ForecastAdapter(activity, null, 0);

        final ListView data_view = (ListView)this.activity.findViewById(R.id.list_view);
        data_view.setAdapter(data_adapter);
        data_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) data_view.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(activity);
                    Intent intent = new Intent(activity, DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting,
                                    cursor.getLong(COL_WEATHER_DATE)
                            ));
                    activity.startActivity(intent);
                }
            }
        });
        return data_view;
    }

    void onLocationChanged() {
        refresh_data();
        activity.getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    void refresh_data() {
        String location = Utility.getPreferredLocation(activity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String units = preferences.getString(activity.getString(R.string.pref_units_key),activity.getString(R.string.pref_units_default));

        /*
        //convert temperatures
        if (units.equals("imperial")) {
            double h = (Double.parseDouble(high) * 1.8) + 32;
            double l = (Double.parseDouble(low) * 1.8) + 32;
            high = Double.toString(Math.round(h * 100.0) / 100.0);
            low = Double.toString(Math.round(l * 100.0) / 100.0);
        }*/

        FetchWeatherTask fetch_weather_data = new FetchWeatherTask(this.activity);
        fetch_weather_data.execute(location);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(this.activity);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,
                System.currentTimeMillis() / 1000);

        return new CursorLoader(this.activity, weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.data_adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        this.data_adapter.swapCursor(null);
    }
}
