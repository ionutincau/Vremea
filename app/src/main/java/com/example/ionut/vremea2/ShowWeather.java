package com.example.ionut.vremea2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ShowWeather {
    private Activity activity;
    private String[] data = {};
    private ArrayAdapter<String> data_adapter;

    ShowWeather(Activity activity) {
        this.activity = activity;
        refresh_data();
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

        /*
        //convert temperatures
        if (units.equals("imperial")) {
            double h = (Double.parseDouble(high) * 1.8) + 32;
            double l = (Double.parseDouble(low) * 1.8) + 32;
            high = Double.toString(Math.round(h * 100.0) / 100.0);
            low = Double.toString(Math.round(l * 100.0) / 100.0);
        }*/

        FetchWeatherTask fetch_weather_data = new FetchWeatherTask(activity, data_adapter);
        fetch_weather_data.execute(location);
    }

}
