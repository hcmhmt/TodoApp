package com.hcmhmt.todoapp.Classes;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.hcmhmt.todoapp.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;
import java.util.Date;

public class CurrentDayDecorator implements DayViewDecorator {

    private int year;
    private int month;
    private int day;

    private CalendarDay calendar ;
    //private CalendarDay calendar = CalendarDay.from(new Date());
    private Drawable drawable;

    public CurrentDayDecorator(Activity context , int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;


        Calendar cal = Calendar.getInstance();
        cal.set(year,month,day);
        cal.add(Calendar.MONTH, -1);

        calendar = CalendarDay.from(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));

        drawable = ContextCompat.getDrawable(context, R.drawable.first_day_month);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(calendar);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}
