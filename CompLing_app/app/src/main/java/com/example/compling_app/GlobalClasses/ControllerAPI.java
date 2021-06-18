package com.example.compling_app.GlobalClasses;

import android.util.JsonReader;

import com.example.compling_app.FirstTaskClasses.FirstTaskNews;
import com.example.compling_app.SecondTaskClasses.SecondTaskWords;
import com.example.compling_app.ThirdTaskClasses.ThirdTaskTonality;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ControllerAPI {

    /*
    !!! GET !!!
     */
    public HttpURLConnection connectionGet(String putUrl) throws IOException {
        URL url = new URL(putUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setRequestMethod("GET");
        connection.connect();

        return connection;
    }

    public ArrayList<FirstTaskNews> firstTask(HttpURLConnection connection) throws IOException{
        ArrayList<FirstTaskNews> news = new ArrayList<>();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStreamReader reader = new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8);

            Integer id;
            String title;
            String url;
            ArrayList<String> tags;
            String text;
            ArrayList<String> images;
            Integer commentCount;
            String date;

            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()){
                id = null;
                title = null;
                url = null;
                tags = null;
                text = null;
                images = null;
                commentCount = null;
                date = null;

                jsonReader.beginObject();
                while (jsonReader.hasNext()){
                    String key = jsonReader.nextName();
                    switch (key){
                        case "_id":
                            try {
                                id = jsonReader.nextInt();
                            } catch(NumberFormatException ex) {
                                ex.printStackTrace();
                                id = null;
                            }
                            break;
                        case "comment_count":
                            try {
                                commentCount = jsonReader.nextInt();
                            } catch(NumberFormatException ex) {
                                ex.printStackTrace();
                                commentCount = null;
                            }
                            break;
                        case "date":
                            try {
                                date = jsonReader.nextString();
                            } catch(NumberFormatException ex) {
                                ex.printStackTrace();
                                date = null;
                            }
                            break;
                        case "tags":
                            jsonReader.beginArray();
                            tags = new ArrayList<>();
                            while (jsonReader.hasNext()){
                                try {
                                    tags.add(jsonReader.nextString());
                                } catch(NumberFormatException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            jsonReader.endArray();
                            break;
                        case "text":
                            try {
                                text = jsonReader.nextString();
                            } catch(NumberFormatException ex){
                                ex.printStackTrace();
                                text = null;
                            }
                            break;
                        case "title":
                            try {
                                title = jsonReader.nextString();
                            } catch(NumberFormatException ex) {
                                ex.printStackTrace();
                                title = null;
                            }
                            break;
                        case "url":
                            try{
                                url = jsonReader.nextString();
                            } catch(NumberFormatException ex) {
                                ex.printStackTrace();
                                url = null;
                            }
                            break;
                        case "url_image":
                            jsonReader.beginArray();
                            images = new ArrayList<>();
                            while (jsonReader.hasNext()){
                                try {
                                    images.add(jsonReader.nextString());
                                } catch(NumberFormatException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            jsonReader.endArray();
                            break;
                        default:
                            jsonReader.skipValue();
                            break;
                    }
                }
                jsonReader.endObject();
                if (id != null && title != null && url != null && tags != null &&
                        text != null  && images != null && commentCount != null && date != null){
                    news.add(new FirstTaskNews(id, title, url, tags, text, images,
                            commentCount, date));
                }
            }
            jsonReader.endArray();

            jsonReader.close();
            connection.disconnect();
        }


        return news;
    }

//    public ArrayList<FirstTaskNews> tmpFirstTask(){
//        ArrayList<FirstTaskNews> news = new ArrayList<>();
//        ArrayList<String> tags;
//        ArrayList<String> images;
//
//        tags = new ArrayList<>();
//        tags.add("society");
//        tags.add("zenyandex");
//        images = new ArrayList<>();
//        images.add("https://s15.stc.all.kpcdn.net/share/i/12/11987833/wr-960.jpg");
//        images.add("https://s16.stc.all.kpcdn.net/share/i/12/11987577/wr-960.jpg");
//        news.add(new FirstTaskNews(1, "title1", "http://www.google.com", tags, "Днем 14 июня 2020 года на перегоне Ярыженская - Алексиково в Волгоградской области сошел с рельсов грузовой поезд. На время восстановительных работ движение на этом участке Приволжской железной дороги временно приостановлено, а все проходящие здесь поезда изменили маршруты и теперь задерживаются в пути. Как сообщили КП-Волгоград в пресс-службе ПривЖД, задерживаются пассажирские поезда дальнего следования Волгоград Москва и поезд Волгоград Москва. Составы направили в обход поврежденного участка. Кроме того, сократили маршрут пригородные поезда, соединяющие Волгоград и Урюпинск. Составы пойдут лишь до Филоново и обратно. Так, 14 июня поезд /6817 Волгоград-1 Филоново прибудет на станцию Филоново в 22:44. А 15 июня поезд /6810 Филоново Волгоград-1 отправится в областной центр в 3:35 и прибудет на станцию Волгоград-1 в 8:34 утра. В ПривЖД отмечают, что в результате схода с рельсов вагонов грузового поезда в Волгоградской области никто не пострадал, нет угрозы и для экологии. Последствия ЧП сейчас устраняют восстановительные поезда. Причину происшествия еще предстоит установить.", images, 1, "01.01.1999"));
//
//        tags = new ArrayList<>();
//        tags.add("society");
//        tags.add("zenyandex");
//        images = new ArrayList<>();
//        images.add("https://s15.stc.all.kpcdn.net/share/i/12/11987833/wr-960.jpg");
//        images.add("https://s16.stc.all.kpcdn.net/share/i/12/11987577/wr-960.jpg");
//        news.add(new FirstTaskNews(2, "title2", "http://www.google.com", tags, "text2", images, 2, "02.02.1999"));
//
//        tags = new ArrayList<>();
//        tags.add("society");
//        tags.add("zenyandex");
//        images = new ArrayList<>();
//        images.add("https://s15.stc.all.kpcdn.net/share/i/12/11987833/wr-960.jpg");
//        images.add("https://s16.stc.all.kpcdn.net/share/i/12/11987577/wr-960.jpg");
//        news.add(new FirstTaskNews(3, "title3", "http://www.google.com", tags, "text3", images, 3, "03.03.1999"));
//
//        return news;
//    }

    public SecondTaskWords secondTask(HttpURLConnection connection) throws IOException{
        SecondTaskWords words = null;

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            ArrayList<String> synonyms = new ArrayList<>();;
            ArrayList<String> neighbours = new ArrayList<>();;

            InputStreamReader reader = new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8);

            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.beginObject();
            while (jsonReader.hasNext()){
                String key = jsonReader.nextName();
                switch (key){
                    case "synonyms":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()){
                            try{
                                synonyms.add(jsonReader.nextString());
                            } catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                        jsonReader.endArray();
                        break;
                    case "neighbour":
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()){
                            try {
                                neighbours.add(jsonReader.nextString());
                            } catch (NumberFormatException e){
                                e.printStackTrace();
                            }
                        }
                        jsonReader.endArray();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();

            words = new SecondTaskWords(synonyms, neighbours);

            jsonReader.close();
            connection.disconnect();
        }

        return words;
    }

    public ArrayList<ThirdTaskTonality> thirdTask (HttpURLConnection connection) throws IOException{
        ArrayList<ThirdTaskTonality> allNews = new ArrayList<>();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            String text;
            LinkedHashMap<String, String> newsTonality;

            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.beginArray();
            while (jsonReader.hasNext()){
                text = null;
                newsTonality = new LinkedHashMap<>();
                jsonReader.beginObject();
                while (jsonReader.hasNext()){
                    String key = jsonReader.nextName();
                    switch (key){
                        case "sentence":
                            text = jsonReader.nextString();
                            break;
                        case "result":
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()){
                                newsTonality.put(jsonReader.nextName(), jsonReader.nextString());
                            }
                            jsonReader.endObject();
                            break;
                        default:
                            jsonReader.skipValue();
                            break;
                    }
                }
                jsonReader.endObject();

                if (text != null && newsTonality.size() > 0){
                    allNews.add(new ThirdTaskTonality(text, newsTonality));
                }
            }
            jsonReader.endArray();

            jsonReader.close();
            connection.disconnect();
        }


        return allNews;
    }

//    public ArrayList<ThirdTaskTonality> tmpThirdTask(){
//        ArrayList<ThirdTaskTonality> newsTonality = new ArrayList<>();
//        LinkedHashMap<String, String> tonalityResult = null;
//
//        tonalityResult = new LinkedHashMap<>();
//        tonalityResult.put("neutral", "0.46102678775787354");
//        tonalityResult.put("skip", "0.2509227991104126");
//        newsTonality.add(new ThirdTaskTonality("Приказ о назначении нового начальника отдела полиции подписал глава ГУ МВД по Волгоградской области КРАВЧЕНКОАЛЕКСАНДР.",  tonalityResult));
//
//        tonalityResult = new LinkedHashMap<>();
//        tonalityResult.put("positive", "0.39763081905722455");
//        tonalityResult.put("skip", "0.1856298642837978");
//        newsTonality.add(new ThirdTaskTonality("В настоящее время 8-летний ребенок проживает с отцом, - рассказала «КП-Волгоград» старший помощник ПРОКУРОР области Оксана Черединина.",  tonalityResult));
//
//
//        return newsTonality;
//    }

    /*
    !!! POST !!!
     */
    public HttpURLConnection connectionPost(String putUrl) throws IOException {
        URL url = new URL(putUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.connect();

        return connection;
    }
}
