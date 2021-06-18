package com.example.compling_app.FirstTaskClasses;

import java.util.ArrayList;

public class FirstTaskNews {
    public Integer id;
    public String title;
    public String url;
    public ArrayList<String> tags;
    public String text;
    public ArrayList<String> images;
    public Integer commentCount;
    public String date;

    public FirstTaskNews (Integer id, String title, String url, ArrayList<String> tags, String text,
                          ArrayList<String> images, Integer commentCount, String date){
        this.id = id;
        this.title = title;
        this.url = url;
        this.tags = tags;
        this.text = text;
        this.images = images;
        this.commentCount = commentCount;
        this.date = date;
    }
}
