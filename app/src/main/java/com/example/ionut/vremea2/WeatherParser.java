package com.example.ionut.vremea2;

import java.util.Calendar;
import java.util.GregorianCalendar;

class WeatherParser {

    private String getDate(long dt) {
        String[] namesOfDays =  {"Duminică", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbată"};
        String[] monthNames = {"Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dt * 1000);

        String day_of_the_week = namesOfDays[calendar.get(Calendar.DAY_OF_WEEK)-1];
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String month = monthNames[calendar.get(Calendar.MONTH)];
        return day_of_the_week + ", " + day + " " + month;
    }
}
