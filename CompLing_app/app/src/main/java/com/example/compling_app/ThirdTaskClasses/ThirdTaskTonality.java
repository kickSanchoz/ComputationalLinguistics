package com.example.compling_app.ThirdTaskClasses;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ThirdTaskTonality {
    public String text;
    public LinkedHashMap<String, String> tonality;

    public ThirdTaskTonality(String text, LinkedHashMap<String, String> tonality){
        this.text = text;
        this.tonality = tonality;
    }
}
