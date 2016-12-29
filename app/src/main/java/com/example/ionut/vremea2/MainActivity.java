package com.example.ionut.vremea2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    ShowWeather show_weather;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mLocation = Utility.getPreferredLocation(this);
        this.show_weather = new ShowWeather(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            this.show_weather.refresh_data();
        }
        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
        }
        if (id == R.id.action_map) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String location = preferences.getString(getString(R.string.pref_cities_key), getString(R.string.pref_cities_default));
            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", location).build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            else {
                Log.d(this.getClass().getSimpleName(), "Couldn't call " + location + ", no receiving apps installed!");
            }
        }
        if (id == R.id.database) {
            Intent dbmanager = new Intent(this,AndroidDatabaseManager.class);
            startActivity(dbmanager);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if (location != null && !location.equals(mLocation)) {
            this.show_weather.onLocationChanged();
            this.mLocation = location;
        }
    }
}
