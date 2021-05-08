package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capstone2.database.dao.WorkTimeAndCalorieDao;
import com.example.capstone2.database.database.WorkTimeAndCalorieDatabase;
import com.example.capstone2.database.vo.WorkTimeAndCalorie;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DailyPedometerDisplay extends AppCompatActivity {
    ListView listView;
    listAdapter adapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_pedometer_display);
        listView = findViewById(R.id.listview);


        final AppCompatActivity activityContext = this;
        WorkTimeAndCalorieDatabase db = WorkTimeAndCalorieDatabase.getInstance(this);
        new Thread(() -> {
            WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
            AtomicReference<List<WorkTimeAndCalorie>> list = new AtomicReference<>();
            list.set(dao.getAll());
            activityContext.runOnUiThread(() -> setupView(list));
        }).start();

    }

    private void setupView(AtomicReference<List<WorkTimeAndCalorie>> list){
        adapter = new listAdapter(list.get());
        listView.setAdapter(adapter);
    }





    class listAdapter extends BaseAdapter{
        List<WorkTimeAndCalorie> lists;

        public listAdapter(List<WorkTimeAndCalorie> lists) {
            this.lists = lists;
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.list_card, null);
            }

            TextView tvTitle = v.findViewById(R.id.date);
            TextView tvContent = v.findViewById(R.id.worktime);
            TextView calorie = v.findViewById(R.id.calorie);

            WorkTimeAndCalorie data = lists.get(position);

            SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");

            tvTitle.setText(format1.format(data.datetime));
            int sec = (int) (data.mills/1000);
            tvContent.setText(String.format("%d분 %d초", sec/60, sec%60));
            calorie.setText(String.format("%.2f 키로칼로리", data.calorie));

            tvTitle.setBackgroundColor(Color.WHITE);


            return v;
        }
    }
}
