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

public class FragmentUpdateTodo extends DialogFragment {

    Activity _mActivity;
    Context _mContext;

    private Spinner _s_category, _s_color,_s_status;
    private TextView _updateTodo, _cancel, _time;
    private EditText _title, _tag;

    private Todo _myTodo = new Todo();
    String getBundle;
    DatabaseReference _ref = FirebaseDatabase.getInstance().getReference().child("TodoList");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_update_todo, container, false);
        getBundle = getArguments().getString("myTodo");
        getMyTodoFromFirebase();
        setBackground();
        _mActivity = getActivity();
        _mContext = getContext();


        _s_category = view.findViewById(R.id.sp_fragmentUpdateTodo_todo_category);
        _s_color = view.findViewById(R.id.sp_fragmentUpdateTodo_todo_color);
        _s_status = view.findViewById(R.id.sp_fragmentUpdateTodo_todo_status);

        _title = view.findViewById(R.id.et_fragmentUpdateTodo_title);
        _tag = view.findViewById(R.id.et_fragmentUpdateTodo_tag);

        _updateTodo = view.findViewById(R.id.tw_fragmentUpdateTodo_UpdateTodo);
        _cancel = view.findViewById(R.id.tw_fragmentUpdateTodo_cancel);
        _time = view.findViewById(R.id.tw_fragmentUpdateTodo_time);


        _updateTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllInputsFilled()) {
                    addTodoFirebase();
                } else {
                    Toast.makeText(_mContext, "Lütfen tüm alanları doldurunuz!", Toast.LENGTH_LONG).show();
                }
            }
        });

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
        return view;
    }

    private void addTodoFirebase() {

        String[] _partDate = _myTodo.getDate().split("-");
        String[] _partTime = String.valueOf(_time.getText()).split(":");

        String _timestamp = _partDate[2]+_partDate[1]+_partDate[0]+_partTime[0]+_partTime[1];


        _myTodo.setTime(String.valueOf(_time.getText()));

        String oldTimestamp = _myTodo.getTimestamp();

        _myTodo.setTimestamp(_timestamp);


        _ref.child(_myTodo.getDate())
                .child(oldTimestamp)
                .setValue(null);


        /** ETKİNLİK TAMAMLANDI İSE ALARM SİLİNİR TAMAMLANMADI İSE TEKRARDAN OLUŞTURULUR **/
        if(_myTodo.getStatus().equals("1"))
            deleteAlarm();
        else
            startAlarm();


        _ref.child(_myTodo.getDate()).child(_timestamp)
                .setValue(_myTodo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(_mContext, "Etkinliğiniz başarılı bir şekilde güncellenmiştir!", Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }else{
                            Toast.makeText(_mContext, "Etkinliğinizi güncelleştirirken bir sorun oluştu. Lütfen tekrar deneyiniz", Toast.LENGTH_LONG).show();
                            getDialog().dismiss();
                        }
                    }
                });

    }

    private void deleteAlarm(){
        /** OLUŞTURULAN ALARMIN SİLİNMESİ **/
        AlarmManager alarmManagerDELETED = (AlarmManager) _mContext.getSystemService(_mContext.ALARM_SERVICE);
        Intent intentDELETED = new Intent(_mContext, AlertReceiver.class);
        intentDELETED.putExtra("todoTitle",_myTodo.getTitle());

        PendingIntent pendingIntentDELETED = PendingIntent.getBroadcast(_mContext,_myTodo.getAlarmId(), intentDELETED, 0);
        alarmManagerDELETED.cancel(pendingIntentDELETED);
    }

    private void startAlarm() {

        /** OLUŞTURULAN ALARMIN SİLİNMESİ **/
        AlarmManager alarmManagerDELETED = (AlarmManager) _mContext.getSystemService(_mContext.ALARM_SERVICE);
        Intent intentDELETED = new Intent(_mContext, AlertReceiver.class);
        intentDELETED.putExtra("todoTitle",_myTodo.getTitle());

        PendingIntent pendingIntentDELETED = PendingIntent.getBroadcast(_mContext,_myTodo.getAlarmId(), intentDELETED, 0);
        alarmManagerDELETED.cancel(pendingIntentDELETED);

        /** YENİ ALARMIN OLUŞTURULMASI **/
        AlarmManager alarmManager = (AlarmManager) _mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(_mContext, AlertReceiver.class);
        intent.putExtra("todoTitle",_myTodo.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(_mContext,_myTodo.getAlarmId(), intent, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date = null;
        try {
            date = sdf.parse(_myTodo.getDate()+" "+_time.getText());
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

    }

    private void getMyTodoFromFirebase() {
        String[] _parts = getBundle.split(":");

        _ref.child(_parts[0]).child(_parts[1]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _myTodo.setDate(dataSnapshot.child("date").getValue().toString());
                _myTodo.setCategory(dataSnapshot.child("category").getValue().toString());
                _myTodo.setColor(dataSnapshot.child("color").getValue().toString());
                _myTodo.setStatus(dataSnapshot.child("status").getValue().toString());
                _myTodo.setTag(dataSnapshot.child("tag").getValue().toString());
                _myTodo.setTime(dataSnapshot.child("time").getValue().toString());
                _myTodo.setTimestamp(dataSnapshot.child("timestamp").getValue().toString());
                _myTodo.setTitle(dataSnapshot.child("title").getValue().toString());
                _myTodo.setAlarmId(Integer.parseInt(dataSnapshot.child("alarmId").getValue().toString()));
                fillFragmentInputs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void fillFragmentInputs() {

        _title.setText(_myTodo.getTitle());
        _time.setText(_myTodo.getTime());
        _tag.setText(_myTodo.getTag());
        startSpinnerCategory();
        startSpinnerColor();
        startSpinnerStatus();
    }
    private void startSpinnerCategory() {
        List<String> list = new ArrayList<String>();

        if(_myTodo.getCategory().equals("Genel")){
            list.add("Ders");
            list.add("İş");
            list.add("Özel");
            list.add("Aile");
        }else if(_myTodo.getCategory().equals("Ders")){
            list.add("Ders");
            list.add("Genel");
            list.add("İş");
            list.add("Özel");
            list.add("Aile");
        }else if(_myTodo.getCategory().equals("İş")){
            list.add("İş");
            list.add("Genel");
            list.add("Ders");
            list.add("Özel");
            list.add("Aile");
        }else if(_myTodo.getCategory().equals("Özel")){
            list.add("Özel");
            list.add("Genel");
            list.add("Ders");
            list.add("İş");
            list.add("Aile");
        }else{
            list.add("Aile");
            list.add("Genel");
            list.add("Ders");
            list.add("İş");
            list.add("Özel");
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_mContext,R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        _s_category.setAdapter(dataAdapter);
    }
    private void startSpinnerColor() {
        List<String> list = new ArrayList<String>();

        if(_myTodo.getColor().equals("Kırmızı")){
            list.add("Kırmızı");
            list.add("Beyaz");
        }else{
            list.add("Beyaz");
            list.add("Kırmızı");
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_mContext,R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        _s_color.setAdapter(dataAdapter);
    }
    private void startSpinnerStatus() {
        List<String> list = new ArrayList<String>();

        if(_myTodo.getStatus().equals("0")){
            list.add("Tamamlanmadı");
            list.add("Tamamlandı");
        }else{
            list.add("Tamamlandı");
            list.add("Tamamlanmadı");
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(_mContext, R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        _s_status.setAdapter(dataAdapter);
    }
    private boolean isAllInputsFilled() {

        _myTodo.setTitle(_title.getText().toString().trim());
        _myTodo.setTag(_tag.getText().toString().trim());
        _myTodo.setCategory(_s_category.getSelectedItem().toString());
        _myTodo.setColor(_s_color.getSelectedItem().toString());
        _myTodo.setStatus(_s_status.getSelectedItem().toString());

        if(_s_status.getSelectedItem().toString().equals("Tamamlandı"))
            _myTodo.setStatus("1");
        else{
            _myTodo.setStatus("0");
        }

        return !_myTodo.getTitle().isEmpty() && !_myTodo.getTag().isEmpty() && !_myTodo.getTag().isEmpty() && !_myTodo.getColor().isEmpty() && !_myTodo.getStatus().isEmpty() && !String.valueOf(_time.getText()).equals("Etkinlik Tarihi");
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
