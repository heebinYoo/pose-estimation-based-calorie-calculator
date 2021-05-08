package com.example.capstone2.database.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.capstone2.database.dao.WorkTimeAndCalorieDao;
import com.example.capstone2.database.vo.WorkTimeAndCalorie;

@Database(entities = {WorkTimeAndCalorie.class}, version = 1)
public abstract class WorkTimeAndCalorieDatabase extends RoomDatabase {
    public abstract WorkTimeAndCalorieDao workTimeAndCalorieDao();

    //Room Database는 싱글톤 패턴을 해야한다.
    private static WorkTimeAndCalorieDatabase INSTANCE;

    private static final Object sLock = new Object();

    public static WorkTimeAndCalorieDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(INSTANCE==null) {
                INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                        WorkTimeAndCalorieDatabase.class, "worktimeandcalorie")
                        .build();
            }
            return INSTANCE;
        }
    }

}

