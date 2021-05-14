package com.example.capstone2.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.capstone2.database.vo.WorkTimeAndCalorie;

import java.util.List;


@Dao
public interface WorkTimeAndCalorieDao {
    @Query("SELECT * FROM worktimeandcalorie ORDER BY createddatetime DESC")
    List<WorkTimeAndCalorie> getAll();

    @Query("SELECT * FROM worktimeandcalorie WHERE createddatetime BETWEEN :startTime and :endTime ORDER BY createddatetime DESC")
    List<WorkTimeAndCalorie> getWithTime(long startTime, long endTime);


    @Insert
    void insert(WorkTimeAndCalorie workTimeAndCalorie);

    @Delete
    void delete(WorkTimeAndCalorie workTimeAndCalorie);
}



