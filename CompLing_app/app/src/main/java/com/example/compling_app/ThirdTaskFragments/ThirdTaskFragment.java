package com.example.compling_app.ThirdTaskFragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.compling_app.GlobalClasses.ControllerAPI;
import com.example.compling_app.GlobalClasses.Toodles;
import com.example.compling_app.R;
import com.example.compling_app.ThirdTaskClasses.ThirdTaskTonality;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

public class ThirdTaskFragment extends Fragment {
    View fragmentView;

    private ScrollView thirdTask_scrollView;
    private LinearLayout thirdTask_scrollLayout;

    private LinearLayout tt_tonality_layout;
    private TextView tt_tonalityText;
    private TextView tt_tonalityResult;

    private ProgressBar tt_progressBar;

    private ControllerAPI controllerAPI = new ControllerAPI();
    boolean isConnected = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_third_task, container, false);

        thirdTask_scrollView = fragmentView.findViewById(R.id.thirdTask_scrollView);
        thirdTask_scrollLayout = fragmentView.findViewById(R.id.thirdTask_scrollLayout);

        tt_progressBar = fragmentView.findViewById(R.id.tt_progressBar);


        thirdTask_scrollView.setVisibility(View.GONE);
        tt_progressBar.setVisibility(View.VISIBLE);
        thread_thirdTask();

        return fragmentView;
    }

    private void thread_thirdTask(){
        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            ArrayList<ThirdTaskTonality> allNews = null;
            final String url = "http://aioki.zapto.org:5000/tonality";

            @Override
            public void run() {
                try {
                    isConnected = Toodles.isNetworkAvailable(requireActivity().getApplication());
                    connection = controllerAPI.connectionGet(url);
                    if (connection != null
                            && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        allNews = controllerAPI.thirdTask(connection);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

                requireActivity().runOnUiThread(() -> {
                    tt_progressBar.setVisibility(View.GONE);
                    if (isConnected){
                        try {
                            if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                                thirdTask_scrollView.setVisibility(View.VISIBLE);
                                createPageContent(allNews);
                            }
                            else {
                                Toast.makeText(requireContext(), "Failed get data from server",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Unexpected answer from connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(requireContext(),"No internet connection",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void createPageContent(ArrayList<ThirdTaskTonality> allNews){
        int offset = 5;
        int offset_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, offset,
                getResources().getDisplayMetrics());


        for (ThirdTaskTonality news : allNews){
            tt_tonality_layout = new LinearLayout(requireContext());
            tt_tonality_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tt_tonality_layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tt_tonality_layoutParams.setMargins(offset_dp, offset_dp, offset_dp, offset_dp);
            tt_tonality_layout.setLayoutParams(tt_tonality_layoutParams);
            tt_tonality_layout.setPadding(offset_dp, offset_dp, offset_dp, offset_dp);
            tt_tonality_layout.setBackgroundColor(Color.parseColor("#F1E6BF"));


            tt_tonalityText = new TextView(requireContext());
            LinearLayout.LayoutParams tt_tonalityTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tt_tonalityTextParams.bottomMargin = 2 * offset_dp;
            tt_tonalityText.setLayoutParams(tt_tonalityTextParams);
            tt_tonalityText.setTextColor(Color.BLACK);
            tt_tonalityText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tt_tonalityText.setTypeface(tt_tonalityText.getTypeface(), Typeface.BOLD);
            tt_tonalityText.setText(news.text);


            tt_tonality_layout.addView(tt_tonalityText);


            for (Map.Entry<String, String> pair : news.tonality.entrySet()){
                tt_tonalityResult = new TextView(requireContext());
                LinearLayout.LayoutParams tt_tonalityResultParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                tt_tonalityResultParams.bottomMargin = offset_dp;
                tt_tonalityResult.setLayoutParams(tt_tonalityResultParams);
                tt_tonalityResult.setTextColor(Color.BLACK);
                tt_tonalityResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tt_tonalityResult.setText(MessageFormat.format("{0}: {1}",
                        pair.getKey(), pair.getValue()));


                tt_tonality_layout.addView(tt_tonalityResult);
            }

            thirdTask_scrollLayout.addView(tt_tonality_layout);
        }
    }
}