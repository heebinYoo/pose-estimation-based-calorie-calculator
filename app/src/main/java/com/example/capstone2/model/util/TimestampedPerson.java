package com.example.capstone2.model.util;

import org.tensorflow.lite.examples.posenet.lib.Person;

public class TimestampedPerson  implements Comparable<TimestampedPerson>{
    public Person person;
    public long timestamp;

    public TimestampedPerson(long timestamp, Person person) {
        this.person = person;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(TimestampedPerson o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
