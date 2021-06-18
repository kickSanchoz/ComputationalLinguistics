package com.example.compling_app.SecondTaskFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.compling_app.GlobalClasses.ControllerAPI;
import com.example.compling_app.GlobalClasses.Toodles;
import com.example.compling_app.R;
import com.example.compling_app.SecondTaskClasses.SecondTaskWords;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.regex.Pattern;


public class SecondTaskFragment extends Fragment {
    View fragmentView;

    private ScrollView secondTask_scrollView;
    private EditText st_fieldSearch;
    private LinearLayout st_content_layout;
    private TextView st_valueSynonyms;
    private TextView st_valueNeighbours;
    private Button st_btn_find;

    private ProgressBar st_progressBar;

    private ControllerAPI controllerAPI = new ControllerAPI();
    boolean isConnected = false;
    private String enteredWord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_second_task, container, false);

        secondTask_scrollView = fragmentView.findViewById(R.id.secondTask_scrollView);
        st_progressBar = fragmentView.findViewById(R.id.st_progressBar);
        st_progressBar.setVisibility(View.GONE);

        st_fieldSearch = fragmentView.findViewById(R.id.st_fieldSearch);

        st_content_layout = fragmentView.findViewById(R.id.st_content_layout);
        st_content_layout.setVisibility(View.GONE);

        st_valueSynonyms = fragmentView.findViewById(R.id.st_valueSynonyms);
        st_valueNeighbours = fragmentView.findViewById(R.id.st_valueNeighbours);

        st_btn_find = fragmentView.findViewById(R.id.st_btn_find);
        st_btn_find.setOnClickListener(v -> {
            enteredWord = st_fieldSearch.getText().toString();
            st_content_layout.setVisibility(View.GONE);
            st_progressBar.setVisibility(View.VISIBLE);
            thread_getWords();

        });




        return fragmentView;
    }

    private void thread_getWords(){
        new Thread(new Runnable() {
            HttpURLConnection connection = null;
            SecondTaskWords words = null;
            final String url = MessageFormat.format("{0}?{1}={2}",
                    "http://aioki.zapto.org:5000/neighbour_synonymous",
                    "word", enteredWord);

            @Override
            public void run() {
                try {
                    isConnected = Toodles.isNetworkAvailable(requireActivity().getApplication());
                    connection = controllerAPI.connectionGet(url);
                    if (connection != null
                            && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        words = controllerAPI.secondTask(connection);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

                requireActivity().runOnUiThread(() -> {
                    st_progressBar.setVisibility(View.GONE);
                    if (isConnected){
                        try {
                            if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                                st_content_layout.setVisibility(View.VISIBLE);
                                fillPageContent(words);
                            }
                            else if (connection != null
                                    && connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                                st_fieldSearch.setError("Такого слова нет в словаре");
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

    private void fillPageContent(SecondTaskWords words){
        if (words.synonyms.size() == 0){
            st_valueSynonyms.setText("Синонимов не найдено");
        }
        else {
            StringBuilder resultSynonyms = new StringBuilder();
            for (int i = 0; i < words.synonyms.size(); i++){
                resultSynonyms.append(words.synonyms.get(i));
                if (i < words.synonyms.size() - 1)
                    resultSynonyms.append(", ");
            }
            st_valueSynonyms.setText(resultSynonyms.toString());
        }

        if (words.neighbours.size() == 0){
            st_valueNeighbours.setText("Соседних слов не найдено");
        }
        else {
            StringBuilder resultNeighbours = new StringBuilder();
            for (int i = 0; i < words.neighbours.size(); i++){
                resultNeighbours.append(words.neighbours.get(i));
                if (i < words.neighbours.size() - 1)
                    resultNeighbours.append(", ");
            }
            st_valueNeighbours.setText(resultNeighbours.toString());
        }

    }
}