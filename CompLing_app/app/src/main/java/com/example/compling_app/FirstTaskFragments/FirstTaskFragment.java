package com.example.compling_app.FirstTaskFragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.compling_app.FirstTaskClasses.FirstTaskNews;
import com.example.compling_app.GlobalClasses.ControllerAPI;
import com.example.compling_app.GlobalClasses.Toodles;
import com.example.compling_app.MainActivity;
import com.example.compling_app.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class FirstTaskFragment extends Fragment {
    View fragmentView;

    int offset;
    int offset_dp;


    private TextInputLayout ft_textInputLayout;
    private AutoCompleteTextView ft_autoCompleteTextView;
    private ScrollView firstTask_scrollView;
    private LinearLayout firstTask_scrollLayout;

    private LinearLayout ft_newsContent_layout;

    private LinearLayout ft_newsHead_layout;
    private LinearLayout ft_newsInfo_layout;
    private TextView ft_newsTitle;
    private TextView ft_newsUrl;
    private TextView ft_newsTags;

    private LinearLayout ft_newsBody_layout;
    private TextView ft_newsText;
    private LinearLayout ft_newsAttachments_layout;
    private TextView ft_textAttachments;
    private LinearLayout ft_newsImages_layout;
    private TextView ft_imgUrl;
    private TextView ft_textShowMore;

    private LinearLayout ft_newsFooter_layout;
    private LinearLayout ft_newsComments_layout;
    private ImageView ft_imgComments;
    private TextView ft_valueComments;
    private TextView ft_newsDate;

    private Button btn_loadMore;

    private ProgressBar ft_mainProgressBar;
    private ProgressBar ft_newsProgressBar;
    ControllerAPI controllerAPI;
    boolean isConnected = false;
    int fixedCount = 5;
    int totalCount = fixedCount;
    int skipNews = 0;

    CountDownTimer prepareTimer;
    CountDownTimer timer;
    Integer periodMin = null;
    Integer periodMillis = null;
    String curPeriod = "none";

    //String json = null;

    MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_first_task, container, false);

        firstTask_scrollView = fragmentView.findViewById(R.id.firstTask_scrollView);
        firstTask_scrollLayout = fragmentView.findViewById(R.id.firstTask_scrollLayout);

        ft_mainProgressBar = fragmentView.findViewById(R.id.ft_mainProgressBar);
        ft_mainProgressBar.setVisibility(View.GONE);


        ft_textInputLayout = fragmentView.findViewById(R.id.ft_textInputLayout);
        ft_autoCompleteTextView = fragmentView.findViewById(R.id.ft_autoCompleteTextView);
        String[] periods = new String[]{"2 мин", "10 мин", "15 мин"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireActivity().getApplication(),
                R.layout.dropdown_item, periods);
        ft_autoCompleteTextView.setAdapter(periodAdapter);
        ft_autoCompleteTextView.setOnItemClickListener((adapter, view, position, id) -> {
            String selectedPeriod = (String) adapter.getItemAtPosition(position);
            if (selectedPeriod != null){
                if (selectedPeriod.equals("2 мин")){
                    if (!curPeriod.equals(selectedPeriod)){ //Если тот же период не выбран повторно
                        Log.d("PERIOD", "2 min chosen");
                        curPeriod = selectedPeriod;

                        //periodMin = 2 * 1000;
                        periodMin = 2;  //Минуты для отправки на сервер
                        periodMillis = 2 * 60 * 1000;   //Милисекунды для запуска таймера

                        cancelTimers();
                        thread_updateCrawlerPeriod();
                    }
                }
                else if (selectedPeriod.equals("10 мин")){
                    if (!curPeriod.equals(selectedPeriod)){ //Если тот же период не выбран повторно
                        Log.d("PERIOD", "10 min chosen");
                        curPeriod = selectedPeriod;

                        //periodMin = 10 * 1000;
                        periodMin = 10; //Минуты для отправки на сервер
                        periodMillis = 10 * 60 * 1000;  //Милисекунды для запуска таймера

                        cancelTimers();
                        thread_updateCrawlerPeriod();
                    }
                }
                else if (selectedPeriod.equals("15 мин")){
                    if (!curPeriod.equals(selectedPeriod)){ //Если тот же период не выбран повторно
                        Log.d("PERIOD", "15 min chosen");
                        curPeriod = selectedPeriod;

                        //periodMin = 15 * 1000;
                        periodMin = 15; //Минуты для отправки на сервер
                        periodMillis = 15 * 60 * 1000;  //Милисекунды для запуска таймера

                        cancelTimers();
                        thread_updateCrawlerPeriod();
                    }
                }
                else {
                    ft_textInputLayout.setError("Данный период невозможно обработать");
                }
            }
        });



        controllerAPI = new ControllerAPI();
        ft_mainProgressBar.setVisibility(View.VISIBLE);
        thread_manualFilling();

        return fragmentView;
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//
//        if (curPeriod != null && !curPeriod.equals("none")){
//            Gson gson = new Gson();
//            json = gson.toJson(curPeriod);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (json != null && !json.isEmpty()){
//            Gson gson = new Gson();
//            curPeriod = gson.fromJson(json, String.class);
//            Log.d("CURRENT PERIOD", String.valueOf(curPeriod));
//        }
//
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelTimers();
    }

    //Ручное заполнение, при первом заходе и при нажатии кнопки
    private void thread_manualFilling(){
        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            ArrayList<FirstTaskNews> allNews = new ArrayList<>();
            final String url = MessageFormat.format("{0}?{1}={2}&{3}={4}",
                    "http://aioki.zapto.org:5000/news",
                    "count_news", fixedCount,
                    "skip_news", skipNews);
            @Override
            public void run() {
                try{
                    isConnected = Toodles.isNetworkAvailable(activity.getApplication());
                    connection = controllerAPI.connectionGet(url);
                    if (connection != null
                            && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        allNews = controllerAPI.firstTask(connection);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

                activity.runOnUiThread(() -> {
                    if (ft_mainProgressBar.getVisibility() == View.VISIBLE){
                        ft_mainProgressBar.setVisibility(View.GONE);
                    }
                    if (ft_newsProgressBar != null
                            && ft_newsProgressBar.getVisibility() == View.VISIBLE){
                        ft_newsProgressBar.setVisibility(View.GONE);
                    }
                    if (isConnected){
                        try {
                            if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                                createPageContent(allNews);
                                Log.d("Skipped_news_manually", String.valueOf(skipNews));
                            }
                            else {
                                if (skipNews != 0){
                                    skipNews -= fixedCount;
                                }
                                Toast.makeText(activity, "Failed get data from server",
                                        Toast.LENGTH_SHORT).show();
//                                Log.d("HTTP CODE", "Failed get data from server"
//                                        + connection.getResponseCode());
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(activity, "Unexpected answer from connection",
                                    Toast.LENGTH_SHORT).show();
//                            Log.d("ANSWER", "Unexpected answer from connection");
                        }
                    }
                    else {
                        Toast.makeText(activity,"No internet connection",
                                Toast.LENGTH_SHORT).show();
//                        Log.d("Connection", "No internet connection");
                    }
                });
            }
        }).start();
    }

    private void cancelTimers(){
        if (prepareTimer != null)
            prepareTimer.cancel();

        if (timer != null)
            timer.cancel();
    }

    private void thread_updateCrawlerPeriod(){
        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            final String url = MessageFormat.format("{0}?{1}={2}&{3}={4}",
                    "http://aioki.zapto.org:5000/parse_timer",
                    "interval", periodMin,
                    "type", "min");
            @Override
            public void run() {
                try {
                    isConnected = Toodles.isNetworkAvailable(activity.getApplication());
                    connection = controllerAPI.connectionPost(url);
                    connection.getResponseCode();
                } catch (IOException e){
                    e.printStackTrace();
                }

                activity.runOnUiThread(() -> {
                    if (isConnected){
                        try {
                            if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                                Toast.makeText(activity, "Период краулера успешно обновлен",
                                        Toast.LENGTH_SHORT).show();
                                setPrepareTimer();
                            }
                            else if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                                Toast.makeText(activity,
                                        "Не удалось обновить период, возможно указан недопустимый интервал",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(activity, "Unexpected answer from connection",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(activity,"No internet connection",
                                Toast.LENGTH_SHORT).show();
//                        Log.d("Connection", "No internet connection");
                    }
                });
            }
        }).start();
    }

    private void setPrepareTimer(){
        prepareTimer = new CountDownTimer(10000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Осталось подготовительного таймера:",
                        String.valueOf(millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                Log.d("Подготовительный таймер", "Окончен");
                setTimer();
            }
        }.start();
    }


    private void setTimer(){
        timer = new CountDownTimer(periodMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("Осталось до обновления страницы:",
                        String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                firstTask_scrollView.setVisibility(View.GONE);
                ft_mainProgressBar.setVisibility(View.VISIBLE);
                thread_automaticFilling();
                setTimer();
            }
        }.start();
    }


    //Автоматическое заполение по окончании таймера
    private void thread_automaticFilling(){

        totalCount = fixedCount + skipNews;
        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            ArrayList<FirstTaskNews> allNews = new ArrayList<>();
            final String url = MessageFormat.format("{0}?{1}={2}&{3}={4}",
                    "http://aioki.zapto.org:5000/news",
                    "count_news", totalCount,
                    "skip_news", 0);
            @Override
            public void run() {
                try{
                    isConnected = Toodles.isNetworkAvailable(activity.getApplication());
                    connection = controllerAPI.connectionGet(url);
                    if (connection != null
                            && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        allNews = controllerAPI.firstTask(connection);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

                activity.runOnUiThread(() -> {
                    firstTask_scrollView.setVisibility(View.VISIBLE);
                    ft_mainProgressBar.setVisibility(View.GONE);
                    if (ft_mainProgressBar.getVisibility() == View.VISIBLE){
                        ft_mainProgressBar.setVisibility(View.GONE);
                    }
                    if (ft_newsProgressBar != null
                            && ft_newsProgressBar.getVisibility() == View.VISIBLE){
                        ft_newsProgressBar.setVisibility(View.GONE);
                    }
                    if (isConnected){
                        try {
                            if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                                Log.d("Skipped_news_inAutomatically", String.valueOf(skipNews));
                                Toast.makeText(activity, "Новости успешно обновлены",
                                        Toast.LENGTH_SHORT).show();
                                firstTask_scrollLayout.removeAllViews();
                                createPageContent(allNews);
                            }
                            else {
                                Toast.makeText(activity, "Failed get data from server",
                                        Toast.LENGTH_SHORT).show();
//                                Log.d("HTTP CODE", "Failed get data from server"
//                                        + connection.getResponseCode());
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(activity, "Unexpected answer from connection",
                                    Toast.LENGTH_SHORT).show();
//                            Log.d("ANSWER", "Unexpected answer from connection");
                        }
                    }
                    else {
                        Toast.makeText(activity,"No internet connection",
                                Toast.LENGTH_SHORT).show();
//                        Log.d("Connection", "Unexpected answer from connection");
                    }
                });
            }
        }).start();
    }

    private void createPageContent(ArrayList<FirstTaskNews> allNews){
        offset = 5;
        offset_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset,
                activity.getResources().getDisplayMetrics());


        for (FirstTaskNews news : allNews){
            ft_newsContent_layout = new LinearLayout(activity);
            ft_newsContent_layout.setId(View.generateViewId());
            ft_newsContent_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsContent_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsContent_layoutParams.bottomMargin = 4 * offset_dp;
            ft_newsContent_layout.setLayoutParams(ft_newsContent_layoutParams);
            ft_newsContent_layout.setBackgroundColor(Color.parseColor("#DFDFDF"));


            //ЗАПОЛЕНЕНИЕ HEAD
            ft_newsHead_layout = new LinearLayout(activity);
            ft_newsHead_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsHead_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsHead_layout.setLayoutParams(ft_newsHead_layoutParams);
            ft_newsHead_layout.setPadding(2 * offset_dp, 2 * offset_dp,
                    2 * offset_dp, 2 * offset_dp);
            ft_newsHead_layout.setBackgroundColor(Color.parseColor("#4E8098"));


            ft_newsInfo_layout = new LinearLayout(activity);
            ft_newsInfo_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsInfo_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsInfo_layout.setLayoutParams(ft_newsInfo_layoutParams);
            ft_newsInfo_layoutParams.bottomMargin = 2 * offset_dp;


            ft_newsTitle = new TextView(activity);
            LinearLayout.LayoutParams ft_newsTitleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsTitle.setLayoutParams(ft_newsTitleParams);
            ft_newsTitle.setTextColor(Color.WHITE);
            ft_newsTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            ft_newsTitle.setTypeface(ft_newsTitle.getTypeface(), Typeface.BOLD);
            ft_newsTitle.setText(news.title);


            ft_newsUrl = new TextView(activity);
            LinearLayout.LayoutParams ft_newsUrlParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsUrl.setLayoutParams(ft_newsUrlParams);
            ft_newsUrl.setTextColor(Color.parseColor("#F8A5AF"));
            ft_newsUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            ft_newsUrl.setTypeface(ft_newsUrl.getTypeface(), Typeface.ITALIC);
            ft_newsUrl.setText(news.url);
            ft_newsUrl.setOnClickListener(v -> setAction_openURL(news.url));


            ft_newsTags = new TextView(activity);
            LinearLayout.LayoutParams ft_newsTagsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsTags.setLayoutParams(ft_newsTagsParams);
            ft_newsTags.setTextColor(Color.WHITE);
            ft_newsTags.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            StringBuilder resultTags = new StringBuilder();
            for (String tag : news.tags){
                resultTags.append("#");
                resultTags.append(tag);
                resultTags.append(" ");
            }
            ft_newsTags.setText(resultTags.toString());


            ft_newsInfo_layout.addView(ft_newsTitle);
            ft_newsInfo_layout.addView(ft_newsUrl);
            ft_newsHead_layout.addView(ft_newsInfo_layout);
            ft_newsHead_layout.addView(ft_newsTags);
            ft_newsContent_layout.addView(ft_newsHead_layout);


            //ЗАПОЛНЕНИЕ BODY
            ft_newsBody_layout = new LinearLayout(activity);
            ft_newsBody_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsBody_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsBody_layout.setLayoutParams(ft_newsBody_layoutParams);
            ft_newsBody_layout.setPadding(2 * offset_dp, 2 * offset_dp,
                    2 * offset_dp, 2 * offset_dp);


            ft_newsText = new TextView(activity);
            ft_newsText.setId(View.generateViewId());
            LinearLayout.LayoutParams ft_newsTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsTextParams.bottomMargin = offset_dp;
            ft_newsText.setLayoutParams(ft_newsTextParams);
            ft_newsText.setMaxLines(6);
            ft_newsText.setTextColor(Color.BLACK);
            ft_newsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            ft_newsText.setText(news.text);


                //ЗАПОЛЕНЕНИЕ ATTACHMENTS
            ft_newsAttachments_layout = new LinearLayout(activity);
            ft_newsAttachments_layout.setId(View.generateViewId());
            ft_newsAttachments_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsAttachments_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsAttachments_layout.setLayoutParams(ft_newsAttachments_layoutParams);
            ft_newsAttachments_layout.setVisibility(View.GONE);


            ft_textAttachments = new TextView(activity);
            LinearLayout.LayoutParams ft_textAttachmentsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_textAttachments.setLayoutParams(ft_textAttachmentsParams);
            ft_textAttachments.setTextColor(Color.BLACK);
            ft_textAttachments.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            ft_textAttachments.setText("Вложения: ");


            ft_newsImages_layout = new LinearLayout(activity);
            ft_newsImages_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams ft_newsImages_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsImages_layout.setLayoutParams(ft_newsImages_layoutParams);
            for (String image : news.images){
                ft_imgUrl = new TextView(activity);
                LinearLayout.LayoutParams ft_imgUrlParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                ft_imgUrl.setLayoutParams(ft_imgUrlParams);
                ft_imgUrl.setMaxLines(1);
                ft_imgUrl.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                ft_imgUrl.setTextColor(Color.parseColor("#2B6F77"));
                ft_imgUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                ft_imgUrl.setText(image);
                ft_imgUrl.setOnClickListener(v -> setAction_openURL(image));

                ft_newsImages_layout.addView(ft_imgUrl);
            }


            ft_textShowMore = new TextView(activity);
            ft_textShowMore.setId(View.generateViewId());
            LinearLayout.LayoutParams ft_textShowMoreParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_textShowMore.setLayoutParams(ft_textShowMoreParams);
            ft_textShowMore.setTextColor(Color.parseColor("#455DD5"));
            ft_textShowMore.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            ft_textShowMore.setText("Показать полностью...");
//            setAction_showMore(ft_newsText.getId(), ft_newsAttachments_layout.getId(),
//                    ft_textShowMore.getId());


            if (news.images.size() > 0){
                ft_newsAttachments_layout.addView(ft_textAttachments);
                ft_newsAttachments_layout.addView(ft_newsImages_layout);
            }
            ft_newsBody_layout.addView(ft_newsText);
            ft_newsBody_layout.addView(ft_newsAttachments_layout);
            ft_newsBody_layout.addView(ft_textShowMore);
            ft_newsContent_layout.addView(ft_newsBody_layout);


            //ЗАПОЛЕНЕНИЕ FOOTER
            ft_newsFooter_layout = new LinearLayout(activity);
            ft_newsFooter_layout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams ft_newsFooter_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_newsFooter_layout.setLayoutParams(ft_newsFooter_layoutParams);
            ft_newsFooter_layout.setPadding(2 * offset_dp, 2 * offset_dp,
                    2 * offset_dp, 2 * offset_dp);
            ft_newsFooter_layout.setBackgroundColor(Color.parseColor("#AEAEAE"));


            ft_newsComments_layout = new LinearLayout(activity);
            ft_newsComments_layout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams ft_newsComments_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            ft_newsComments_layout.setLayoutParams(ft_newsComments_layoutParams);
            ft_newsComments_layout.setGravity(Gravity.CENTER_VERTICAL);


            ft_imgComments = new ImageView(activity);
            LinearLayout.LayoutParams ft_imgCommentsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            ft_imgCommentsParams.rightMargin = offset_dp;
            ft_imgComments.setLayoutParams(ft_imgCommentsParams);
            ft_imgComments.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),
                    R.drawable.ic_comments_white, null));


            ft_valueComments = new TextView(activity);
            LinearLayout.LayoutParams ft_valueCommentsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            ft_valueComments.setLayoutParams(ft_valueCommentsParams);
            ft_valueComments.setTextColor(Color.WHITE);
            ft_valueComments.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            ft_valueComments.setText(String.valueOf(news.commentCount));


            ft_newsDate = new TextView(activity);
            LinearLayout.LayoutParams ft_newsDateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            );
            ft_newsDate.setLayoutParams(ft_newsDateParams);
            ft_newsDate.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            ft_newsDate.setTextColor(Color.WHITE);
            ft_newsDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            String newDate_app = parse_newsDate(news.date);
            ft_newsDate.setText(newDate_app);


            ft_newsComments_layout.addView(ft_imgComments);
            ft_newsComments_layout.addView(ft_valueComments);
            ft_newsFooter_layout.addView(ft_newsComments_layout);
            ft_newsFooter_layout.addView(ft_newsDate);
            ft_newsContent_layout.addView(ft_newsFooter_layout);


            firstTask_scrollLayout.addView(ft_newsContent_layout);


            setAction_showMore(ft_newsText.getId(), ft_newsAttachments_layout.getId(),
                    ft_textShowMore.getId());
        }

        //Кнопка в конце списка
        btn_loadMore = new Button(activity);
        btn_loadMore.setId(View.generateViewId());
        btn_loadMore.setTextColor(Color.WHITE);
        btn_loadMore.setBackgroundResource(R.drawable.rounded_corners);
        btn_loadMore.setText("Больше новостей");
        btn_loadMore.setOnClickListener(v -> setAction_loadMore());

        firstTask_scrollLayout.addView(btn_loadMore);
    }

    private void setAction_loadMore(){
        skipNews += fixedCount;
        firstTask_scrollLayout.removeView(fragmentView.findViewById(btn_loadMore.getId()));
        ft_newsProgressBar = new ProgressBar(activity);
        firstTask_scrollLayout.addView(ft_newsProgressBar);
        thread_manualFilling();
    }

    private void setAction_showMore(Integer newsTextId, Integer newsAttachmentsId,
                                    Integer textShowMoreId){
        TextView cur_newsText = fragmentView.findViewById(newsTextId);
        LinearLayout cur_newsAttachments = fragmentView.findViewById(newsAttachmentsId);
        TextView cur_textShowMore = fragmentView.findViewById(textShowMoreId);


        cur_textShowMore.setOnClickListener(v -> {
            cur_newsText.setMaxLines(Integer.MAX_VALUE);
            cur_newsAttachments.setVisibility(View.VISIBLE);
            cur_textShowMore.setVisibility(View.GONE);
        });
    }

    private void setAction_openURL(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public String parse_newsDate(String input){
        SimpleDateFormat sdfIn = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat sdfIn_mils = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S");
        SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        Date date = null;
        String output = null;

        try {
            date = sdfIn.parse(input);
            if (date != null)
                output = sdfOut.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            output = "unknown";
            try {
                date = sdfIn_mils.parse(input);
                if (date != null){
                    output = sdfOut.format(date);
                }
            } catch (ParseException e1){
                e1.printStackTrace();
                output = "unknown";
            }
        }

        return output;
    }
}