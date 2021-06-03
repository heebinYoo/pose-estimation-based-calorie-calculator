package org.tensorflow.lite.examples.posenet.lib

import android.content.Context

class Armworknet @JvmOverloads constructor(context: Context, device: Device = Device.CPU) : Worknet(context, device), AutoCloseable {
    override val filename: String = "barbel-model.tflite"
}