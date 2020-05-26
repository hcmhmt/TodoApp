package com.hcmhmt.todoapp.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmhmt.todoapp.R;

public class FragmentStatistic extends DialogFragment {

    Context _mContext;
    private TextView tw_done,tw_missing;
    private int done = 0;
    private int missing = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_statistic, container, false);
        _mContext = getContext();
        setBackground();
        tw_done = view.findViewById(R.id.tw_statistic_done);
        tw_missing = view.findViewById(R.id.tw_statistic_missing);

        FirebaseDatabase.getInstance().getReference().child("TodoList")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot datas : dataSnapshot.getChildren() ){
                            for (DataSnapshot datas2 : datas.getChildren() ){
                                if(datas2.child("status").getValue().toString().equals("1"))
                                    done++;
                                else
                                    missing++;
                            }
                        }
                        startTextViews();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        return view;
    }

    private void startTextViews() {
        tw_done.setText(String.valueOf(done));
        tw_missing.setText(String.valueOf(missing));
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
