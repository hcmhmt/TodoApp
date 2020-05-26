package com.hcmhmt.todoapp.Fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmhmt.todoapp.AlertReceiver;
import com.hcmhmt.todoapp.Classes.Todo;
import com.hcmhmt.todoapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.lang.Integer.parseInt;

public class FragmentAddTodo extends DialogFragment {

    Activity _mActivity;
    Context _mContext;

    private Spinner _s_category, _s_color;
    private TextView _addTodo, _cancel, _time;
    private EditText _title, _tag;
    private Todo myTodo = new Todo();

    private String _activityDate;
    DatabaseReference _ref = FirebaseDatabase.getInstance().getReference("TodoList");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_todo, container, false);
        setBackground();
        _mActivity = getActivity();
        _mContext = getContext();

        _s_category = view.findViewById(R.id.sp_fragmentAddTodo_todo_category);
        _s_color = view.findViewById(R.id.sp_fragmentAddTodo_todo_color);

        _title = view.findViewById(R.id.et_fragmentAddTodo_title);
        _tag = view.findViewById(R.id.et_fragmentAddTodo_tag);

        _addTodo = view.findViewById(R.id.tw_fragmentAddTodo_addTodo);
        _cancel = view.findViewById(R.id.tw_fragmentAddTodo_cancel);
        _time = view.findViewById(R.id.tw_fragmentAddTodo_time);

        _activityDate = getArguments().getString("todoDateFromActivity");


        _time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar _calendar = Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(
                        _mContext,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                _calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                _calendar.set(Calendar.MINUTE, minute);

                                _time.setText(new SimpleDateFormat("HH:mm").format(_calendar.getTime()));
                            }
                        },
                        _calendar.get(Calendar.HOUR_OF_DAY),
                        _calendar.get(Calendar.MINUTE),
                        true
                );
                dialog.show();
            }
        });

        _cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        _addTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllInputsFilled()) {
                    addTodoFirebase();
                } else {
                    Toast.makeText(_mContext, "Lütfen tüm alanları doldurunuz!", Toast.LENGTH_LONG).show();
                }
            }
        });


        startSpinnerCategory();
        startSpinnerColor();

        return view;
    }

    private void addTodoFirebase() {

        String[] _partDate = _activityDate.split("-");
        String[] _partTime = String.valueOf(_time.getText()).split(":");

        String _timestamp = _partDate[2]+_partDate[1]+_partDate[0]+_partTime[0]+_partTime[1];

        myTodo.setDate(_activityDate);
        myTodo.setTime(String.valueOf(_time.getText()));
        myTodo.setStatus("0");
        myTodo.setTimestamp(_timestamp);
        myTodo.setAlarmId(startAlarm());

        _ref.child(_activityDate).child(_timestamp)
                .setValue(myTodo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(_mContext, "Etkinliğiniz başarılı bir şekilde oluşturulmuştur!", Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }else{
                            Toast.makeText(_mContext, "Etkinliğinizi oluştururken bir sorun oluştu lütfen tekrar deneyiniz", Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }
                    }
                });

    }

    private int startAlarm() {

        AlarmManager alarmManager = (AlarmManager) _mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(_mContext, AlertReceiver.class);
        intent.putExtra("todoTitle",myTodo.getTitle());

        Long l= System.currentTimeMillis();
        int requestId=l.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(_mContext,requestId, intent, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date = null;
        try {
            date = sdf.parse(_activityDate+" "+_time.getText());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(date);

        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DATE, 1);
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

        return requestId;
    }

    private boolean isAllInputsFilled() {

        myTodo.setTitle(_title.getText().toString().trim());
        myTodo.setTag(_tag.getText().toString().trim());
        myTodo.setCategory(_s_category.getSelectedItem().toString());
        myTodo.setColor(_s_color.getSelectedItem().toString());

        return !myTodo.getTitle().isEmpty() && !myTodo.getTag().isEmpty() && !myTodo.getTag().isEmpty() && !myTodo.getColor().isEmpty() && !String.valueOf(_time.getText()).equals("Etkinlik Tarihi");
    }
    private void startSpinnerCategory() {
        List<String> list = new ArrayList<String>();

        list.add("Genel");
        list.add("Ders");
        list.add("İş");
        list.add("Özel");
        list.add("Aile");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_mContext, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        _s_category.setAdapter(dataAdapter);
    }
    private void startSpinnerColor() {
        List<String> list = new ArrayList<String>();

        list.add("Kırmızı");
        list.add("Beyaz");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_mContext, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        _s_color.setAdapter(dataAdapter);
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
