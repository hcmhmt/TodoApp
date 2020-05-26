package com.hcmhmt.todoapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmhmt.todoapp.Adapter.Adapter_Todo;
import com.hcmhmt.todoapp.Classes.CurrentDayDecorator;
import com.hcmhmt.todoapp.Classes.Todo;
import com.hcmhmt.todoapp.Fragment.FragmentAddTodo;
import com.hcmhmt.todoapp.Fragment.FragmentMode;
import com.hcmhmt.todoapp.Fragment.FragmentStatistic;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    DatabaseReference _ref = FirebaseDatabase.getInstance().getReference("TodoList");
    Activity _activity;
    MaterialCalendarView _mCalendar;

    ArrayList<String> _allDaysFirst = new ArrayList<String>();
    ArrayList<String> _allDaysNew = new ArrayList<String>();
    ArrayList _allDates = new ArrayList<Todo>();

    String _recycler_todoDate;
    Button _newTodo;
    RecyclerView _rc_todoList;

    ScrollView sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBackground();

        TimeZone tz = TimeZone.getTimeZone("Turkey/Istanbul");
        _activity = this;

        _mCalendar = findViewById(R.id.cw_main);
        _newTodo = findViewById(R.id.btn_addnew_todo);
        _rc_todoList = findViewById(R.id.rw_mainActivity_todolist);
        sc = findViewById(R.id.scroll);

        setCalendarPoints();

        _mCalendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, final CalendarDay date, boolean selected) {

                showAddNewTodoButton();
                fillDataSet(date.getDay(), addOneMonthToCalendar(date), date.getYear());

                _newTodo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String todoDate;

                        if (date.getDay() < 10)
                            todoDate = "0" + date.getDay();
                        else
                            todoDate = String.valueOf(date.getDay());

                        if (addOneMonthToCalendar(date) < 10)
                            todoDate += "-0" + addOneMonthToCalendar(date) + "-" + date.getYear();
                        else
                            todoDate += addOneMonthToCalendar(date) + "-" + date.getYear();


                        Bundle bundle = new Bundle();
                        bundle.putString("todoDateFromActivity", todoDate);

                        FragmentAddTodo _dialogAddTodo = new FragmentAddTodo();
                        _dialogAddTodo.setArguments(bundle);
                        _dialogAddTodo.show(getSupportFragmentManager(), "showFragment");

                    }
                });
            }
        });

    }

    private void fillDataSet(int day, int month, int year) {

        if (day < 10)
            _recycler_todoDate = "0" + day;
        else
            _recycler_todoDate = String.valueOf(day);

        if (month < 10)
            _recycler_todoDate += "-0" + month + "-" + year;
        else
            _recycler_todoDate += month + "-" + year;

        _ref.child(_recycler_todoDate)
                .orderByKey()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        _allDates.clear();
                        for (DataSnapshot _datas : dataSnapshot.getChildren()) {
                            _allDates.add(new Todo(
                                    _datas.child("title").getValue().toString(),
                                    _datas.child("date").getValue().toString(),
                                    _datas.child("time").getValue().toString(),
                                    _datas.child("status").getValue().toString(),
                                    _datas.child("category").getValue().toString(),
                                    _datas.child("tag").getValue().toString(),
                                    _datas.child("color").getValue().toString(),
                                    _datas.child("timestamp").getValue().toString(),
                                    Integer.parseInt(_datas.child("alarmId").getValue().toString())
                            ));
                        }

                        startRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void startRecyclerView() {

        Adapter_Todo mAdapter = new Adapter_Todo(_allDates, _mCalendar, _activity, getSupportFragmentManager());
        _rc_todoList.setAdapter(mAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        _rc_todoList.setLayoutManager(linearLayoutManager);
    }

    private void setCalendarPoints() {
        _ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot _datas : dataSnapshot.getChildren()) {
                    String[] _parts = _datas.getKey().split("-");
                    _mCalendar.addDecorator(new CurrentDayDecorator(_activity, Integer.parseInt(_parts[2]), Integer.parseInt(_parts[1]), Integer.parseInt(_parts[0])));
                    _allDaysFirst.add(_datas.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /**
         *
         * hem yeni eklenen etkinlikler için günleri circle içine almak
         * hemde komple boşaltılan günler için eğer daha önceden circle içine alındıysa tekrar o circle'ları silmek için
         *
         */
        /** BU İŞLEM İLE SİLİNEN ETKİNLİKLERİN BOYANDIĞI GÜNLER NORMAL HALE GELECEK **/
        _ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _allDaysNew.clear();
                for (DataSnapshot _datas : dataSnapshot.getChildren()) {

                    _allDaysNew.add(_datas.getKey());
                    if (!isCurrentDayDecorated(_datas.getKey())) {
                        String[] _parts = _datas.getKey().split("-");
                        _mCalendar.addDecorator(new CurrentDayDecorator(_activity, Integer.parseInt(_parts[2]), Integer.parseInt(_parts[1]), Integer.parseInt(_parts[0])));
                    }
                }
                checkOldAndNew();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOldAndNew() {
        /*int index = 0;
        Iterator<String> iter = _allDaysFirst.iterator();
        while (iter.hasNext()) {
            String str = iter.next();

            boolean isDayDeleted = true;
            for(String newDate : _allDaysNew){
                if(str == newDate){
                    isDayDeleted = false;
                    break;
                }
            }
            if(isDayDeleted){
                _allDaysFirst.remove(index);
                String[] _parts = str.split("-");
                _mCalendar.addDecorator(new TransparentDayDecorator( _activity,Integer.parseInt(_parts[2]),Integer.parseInt(_parts[1]),Integer.parseInt(_parts[0])));
            }
            index++;
        }*/

    }

    private boolean isCurrentDayDecorated(String date) {
        for (String day : _allDaysFirst) {
            if (day == date)
                return true;
        }
        _allDaysFirst.add(date);
        return false;
    }

    private int addOneMonthToCalendar(CalendarDay date) {

        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonth(), date.getDay());
        cal.add(Calendar.MONTH, 1);
        return cal.get(Calendar.MONTH);

    }

    private void showAddNewTodoButton() {
        _newTodo.setVisibility(View.VISIBLE);
    }

    private void hideAddNewTodoButton() {
        _newTodo.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater _inf = getMenuInflater();
        _inf.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_settings:
                FragmentMode fragmentMode = new FragmentMode();
                fragmentMode.show(getSupportFragmentManager(), "showFragment");
                break;
            case R.id.menu_statistic:
                FragmentStatistic fragmentStatistic = new FragmentStatistic();
                fragmentStatistic.show(getSupportFragmentManager(), "showFragment");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setBackground() {
        FirebaseDatabase.getInstance().getReference().child("Mode").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().equals("Dark")){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
