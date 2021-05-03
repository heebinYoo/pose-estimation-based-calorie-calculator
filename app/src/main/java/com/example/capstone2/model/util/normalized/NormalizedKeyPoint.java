package com.example.capstone2.model.util.normalized;

import org.tensorflow.lite.examples.posenet.lib.BodyPart;

public class NormalizedKeyPoint {
    public BodyPart bodyPart = BodyPart.NOSE;
    public NormalizedPosition normPosition = new NormalizedPosition();
}
