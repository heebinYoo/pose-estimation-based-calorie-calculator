package com.example.capstone2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DailyPedometerDisplay extends AppCompatActivity {
    ListView listView;
    listAdapter adapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_pedometer_display);


        ArrayList<ListData> list = new ArrayList<>();
        list.add(new ListData(1,"제목1","내용", "내용"));
        list.add(new ListData(1, "제목2", "내용", "내용"));
        list.add(new ListData(1,"제목3", "내용", "내용"));
        list.add(new ListData(1,"제목4","내용", "내용"));

        listView = findViewById(R.id.listview);
        adapter = new listAdapter(list);

        listView.setAdapter(adapter);
    }

    class listAdapter extends BaseAdapter{
        List<ListData> lists;

        public listAdapter(List<ListData> lists) {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.list_card, null);
            }

            TextView tvTitle = v.findViewById(R.id.date);
            TextView tvContent = v.findViewById(R.id.distance);
            TextView calorie = v.findViewById(R.id.calorie);

            ListData data = lists.get(position);

            tvTitle.setText(data.getTitle());
            tvContent.setText(data.getContent());
            calorie.setText(data.getContent());

            if(data.getCategory() == 0){
                tvTitle.setBackgroundColor(Color.WHITE);

            }else {
                tvTitle.setBackgroundColor(Color.LTGRAY);
            }

            return v;
        }
    }
}
