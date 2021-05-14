package com.example.capstone2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstone2.database.dao.WorkTimeAndCalorieDao;
import com.example.capstone2.database.database.WorkTimeAndCalorieDatabase;
import com.example.capstone2.database.vo.WorkTimeAndCalorie;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DailyPedometerDisplay extends AppCompatActivity {
    private ListView listView;
    private listAdapter adapter;

    private Calendar startCalendar = null;
    private Calendar endCalendar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_pedometer_display);
        listView = findViewById(R.id.listview);

        Toast.makeText(this, "리스트 항목을 꾹 누르면 삭제할 수 있습니다.", Toast.LENGTH_LONG ).show();

        final AppCompatActivity activityContext = this;
        WorkTimeAndCalorieDatabase db = WorkTimeAndCalorieDatabase.getInstance(this);
        new Thread(() -> {
            WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
            AtomicReference<List<WorkTimeAndCalorie>> list = new AtomicReference<>();
            list.set(dao.getAll());
            activityContext.runOnUiThread(() -> setupView(list));
        }).start();


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                WorkTimeAndCalorie workTimeAndCalorie = ((listAdapter)(((ListView)parent).getAdapter())).lists.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);

                builder.setTitle("삭제하시겠습니까?");

                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        new Thread(() -> {
                            WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
                            dao.delete(workTimeAndCalorie);
                            AtomicReference<List<WorkTimeAndCalorie>> list = new AtomicReference<>();
                            list.set(dao.getAll());
                            activityContext.runOnUiThread(() -> setupView(list));
                        }).start();
                        Toast.makeText(activityContext, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });




        Button startEditTextDateBtn = findViewById(R.id.startEditTextDate);
        startEditTextDateBtn.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,0);

            datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {

                ((Button)v).setText(String.format("시작 : %d/%d/%d", year, month, dayOfMonth));
                startCalendar = Calendar.getInstance();
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            });
            datePickerDialog.show();
        });

        Button endEditTextDateBtn = findViewById(R.id.endEditTextDate);
        endEditTextDateBtn.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,0);

            datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {

                ((Button)v).setText(String.format("끝 : %d/%d/%d", year, month, dayOfMonth));
                endCalendar = Calendar.getInstance();
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        });
            datePickerDialog.show();
        });

     findViewById(R.id.pedeometerSearchBtn).setOnClickListener((v)->{
         if(startCalendar!=null && endCalendar!=null){
             new Thread(() -> {
                 WorkTimeAndCalorieDao dao = db.workTimeAndCalorieDao();
                 AtomicReference<List<WorkTimeAndCalorie>> list = new AtomicReference<>();
                 list.set(dao.getWithTime(startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis()));
                 activityContext.runOnUiThread(() -> setupView(list));
             }).start();
         }
         else{
             Toast.makeText(activityContext, "검색할 날짜를 모두 입력하세요.", Toast.LENGTH_SHORT ).show();
         }
     });
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
