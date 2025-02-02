package com.example.datastoremicroservice.config;

import com.example.datastoremicroservice.model.MeasurementType;

public class RedisSchema {

    //set with the keys of all the sensors
    public static String sensorKeys() {
        return KeyHelper.getKey("sensors");
    }

    //hash with summary types
    public static String summaryKey(
            long sensorId,
            MeasurementType measurementType
    ){  //app:sensors:3:voltage - example of the key
        return KeyHelper.getKey("sensors:"+sensorId+":"+measurementType.name().toLowerCase());
    }

}
