package com.example.ionut.vremea2;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

class WeatherParser {

    private String getDate(int i) {
        String[] namesOfDays =  {"Duminică", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbată"};
        String[] monthNames = {"Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, i);
        String day_of_the_week = namesOfDays[calendar.get(Calendar.DAY_OF_WEEK)-1];
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String month = monthNames[calendar.get(Calendar.MONTH)];
        return day_of_the_week + ", " + day + " " + month;
    }

    String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_WEATHER = "weather";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        String[] resultStrs = new String[numDays];
        for (int i=0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String high;
            String low;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            day = this.getDate(i);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getString(OWM_MAX);
            low = temperatureObject.getString(OWM_MIN);

            resultStrs[i] = day + " - " + description + " - " + high + "/" + low;
        }

        return resultStrs;
    }
}
