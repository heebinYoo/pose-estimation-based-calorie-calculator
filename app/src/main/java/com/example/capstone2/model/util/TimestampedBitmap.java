package com.example.capstone2.model.util;

import android.graphics.Bitmap;

public class TimestampedBitmap  implements Comparable<TimestampedBitmap>{
    public Bitmap bitmap;
    public long timestamp;

    public TimestampedBitmap(long timeInMillis, Bitmap imageBitmap) {
        this.timestamp = timeInMillis;
        this.bitmap = imageBitmap;
    }

    @Override
    public int compareTo(TimestampedBitmap o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
