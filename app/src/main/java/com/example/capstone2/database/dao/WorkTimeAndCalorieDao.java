package com.example.capstone2.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.capstone2.database.vo.WorkTimeAndCalorie;

import java.util.List;


@Dao
public interface WorkTimeAndCalorieDao {
    @Query("SELECT * FROM worktimeandcalorie ORDER BY createddatetime ASC")
    List<WorkTimeAndCalorie> getAll();

    @Insert
    void insert(WorkTimeAndCalorie workTimeAndCalorie);

    @Delete
    void delete(WorkTimeAndCalorie workTimeAndCalorie);
}



