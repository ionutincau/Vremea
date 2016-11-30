package com.example.ionut.vremea2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShowWeather {
    private Activity activity;
    private String[] data = {};
    private ArrayAdapter<String> data_adapter;

    ShowWeather(Activity activity) {
        this.activity = activity;
        show_list();
    }

    private View show_list() {
        List<String> data_list = new ArrayList<>(Arrays.asList(this.data));
        this.data_adapter = new ArrayAdapter<>(activity, R.layout.list_item, data_list);
        final ListView data_view = (ListView)this.activity.findViewById(R.id.list_view);
        data_view.setAdapter(data_adapter);
        data_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String)data_view.getItemAtPosition(position);
                Intent intent = new Intent(activity, DetailActivity.class).putExtra(Intent.EXTRA_TEXT, data);
                activity.startActivity(intent);
            }
        });
        return data_view;
    }

    void refresh_data() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String location = preferences.getString(activity.getString(R.string.pref_cities_key),activity.getString(R.string.pref_cities_default));
        String units = preferences.getString(activity.getString(R.string.pref_units_key),activity.getString(R.string.pref_units_default));
        FetchWeatherTask fetch_weather_data = new FetchWeatherTask();
        fetch_weather_data.execute(location, units);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;
            String api_key = "7e1c369fe2728e2e48d6e9592a5db7b5";

            try {
                // Construct the URL for the OpenWeatherMap query
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                // Construct the URL for the OpenWeatherMap query
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
            }
            catch (IOException e) {
                Log.e(this.LOG_TAG, "URL Connection Error ", e);
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (final IOException e) {
                        Log.e(this.LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            WeatherParser weatherParser= new WeatherParser();
            try {
                return weatherParser.getWeatherDataFromJson(forecastJsonStr, numDays, params[1]);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                data_adapter.clear();
                for(String dayForecastStr : result) {
                    data_adapter.add(dayForecastStr);
                }
            }
        }
    }
}
