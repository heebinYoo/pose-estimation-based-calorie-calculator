package com.example.capstone2.database.vo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "worktimeandcalorie")
public class WorkTimeAndCalorie {
    @PrimaryKey(autoGenerate = true)
    public int idx;
    @ColumnInfo(name = "mills")
    public long mills;
    @ColumnInfo(name = "calorie")
    public double calorie;

    @ColumnInfo(name = "createddatetime")
    public long datetime;

}
