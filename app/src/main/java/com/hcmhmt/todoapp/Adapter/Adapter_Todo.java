package com.hcmhmt.todoapp.Adapter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.hcmhmt.todoapp.AlertReceiver;
import com.hcmhmt.todoapp.Classes.Todo;
import com.hcmhmt.todoapp.Fragment.FragmentAddTodo;
import com.hcmhmt.todoapp.Fragment.FragmentUpdateTodo;
import com.hcmhmt.todoapp.R;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.Serializable;
import java.util.ArrayList;

public class Adapter_Todo extends RecyclerView.Adapter<Adapter_Todo.AdapterTodo_ViewHolder> {

    public static ArrayList<Todo> allTimes;
    MaterialCalendarView calendarView;
    Activity activity;
    FragmentManager fragmentManager;

    public Adapter_Todo(ArrayList<Todo> allTimes, MaterialCalendarView calendarView, Activity activity, FragmentManager fragmentManager) {
        this.allTimes = allTimes;
        this.calendarView = calendarView;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public AdapterTodo_ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onerow_todo,parent,false);
        return new AdapterTodo_ViewHolder(view,calendarView,activity,fragmentManager);
    }

    @Override
    public void onBindViewHolder(AdapterTodo_ViewHolder holder, final int position) {

        TextView time = holder.time;
        TextView title = holder.title;
        TextView category = holder.category;
        TextView tag = holder.tag;
        ConstraintLayout cardView  = holder.cardViewExpandable;

        if(allTimes.get(position).getColor().equals("Kırmızı"))
            holder.cardViewExpandable.setBackgroundResource(R.drawable.border_red);
        else
            holder.cardViewExpandable.setBackgroundResource(R.drawable.border_white);

        time.setText(allTimes.get(position).getTime());
        title.setText(allTimes.get(position).getTitle());
        category.setText(allTimes.get(position).getCategory());
        tag.setText(allTimes.get(position).getTag());

        Boolean isExpanded = allTimes.get(position).getExpanded();
        if(isExpanded){
            holder.expandable.setVisibility(View.VISIBLE);
        }else{
            holder.expandable.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return allTimes.size();
    }

    public class AdapterTodo_ViewHolder extends RecyclerView.ViewHolder {

        private TextView time;
        private TextView title;
        private TextView category;
        private TextView tag;

        private MaterialCalendarView mCalendar;
        private Activity mActivity;

        private ImageButton delete;
        private ImageButton update;
        private ConstraintLayout expandable;
        private ConstraintLayout cardViewExpandable;

        private FragmentManager fragmentManager;

        public AdapterTodo_ViewHolder(final View itemView, MaterialCalendarView calendar, final Activity activity, final FragmentManager fragmentManager) {
            super(itemView);

            this.fragmentManager = fragmentManager;

            this.mCalendar = calendar;
            this.mActivity = activity;

            this.time = (TextView) itemView.findViewById(R.id.tw_todo_time);
            this.title = (TextView) itemView.findViewById(R.id.tw_todo_title);
            this.category = (TextView) itemView.findViewById(R.id.tw_todo_category);
            this.tag = (TextView) itemView.findViewById(R.id.tw_todo_tag);

            this.delete = (ImageButton) itemView.findViewById(R.id.btn_onerow_delete);
            this.update = (ImageButton) itemView.findViewById(R.id.btn_onerow_update);
            this.expandable = (ConstraintLayout) itemView.findViewById(R.id.cl_fragment_expandable);
            this.cardViewExpandable = (ConstraintLayout) itemView.findViewById(R.id.cl_cardView);

            cardViewExpandable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Todo myTodo = allTimes.get(getAdapterPosition());
                    myTodo.setExpanded(!myTodo.getExpanded());
                    Adapter_Todo.this.notifyItemChanged(getAdapterPosition());
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Todo myTodo = allTimes.get(getAdapterPosition());
                    AlertDialog.Builder _ad = new AlertDialog.Builder(activity, R.style.alertDialog);
                    _ad.setMessage("Bu etkinliği silmek istediğinize emin misiniz?")
                            .setPositiveButton("Evet", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    /** OLUŞTURULAN ETKİNLİĞİN SİLİNMESİ **/
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("TodoList")
                                            .child(myTodo.getDate())
                                            .child(myTodo.getTimestamp())
                                            .setValue(null);
                                    Toast.makeText(activity,"Etkinlik Silindi",Toast.LENGTH_LONG);

                                    /** OLUŞTURULAN ALARMIN SİLİNMESİ **/
                                    AlarmManager alarmManager = (AlarmManager) v.getContext().getSystemService(v.getContext().ALARM_SERVICE);
                                    Intent intent = new Intent(v.getContext(), AlertReceiver.class);
                                    intent.putExtra("todoTitle",myTodo.getTitle());

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(v.getContext(),myTodo.getAlarmId(), intent, 0);
                                    alarmManager.cancel(pendingIntent);
                                    Toast.makeText(v.getContext(), "Etkinlik silindi!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    _ad.show();
                }
            });

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Todo myTodo = allTimes.get(getAdapterPosition());
                    FragmentUpdateTodo _dialogFragment= new FragmentUpdateTodo();
                    Bundle bundle = new Bundle();
                    bundle.putString("myTodo", myTodo.getDate()+":"+myTodo.getTimestamp());
                    _dialogFragment.setArguments(bundle);
                    _dialogFragment.show(fragmentManager, "Dialog");

                    /*AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    Fragment myFragment = new FragmentUpdateTodo();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.constraintLayout2, myFragment).addToBackStack(null).commit();*/

                }
            });

        }

    }
}
