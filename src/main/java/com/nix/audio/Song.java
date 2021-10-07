package com.nix.audio;

import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;

public class Song {
    
    private String url, name;

    public Song(String[] in) {
        try {
            if(in[1].startsWith("http")) {
                this.url = in[1];
            }   else {
                searchYT(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchYT(String[] in) throws IOException {
        String query = "";
        for(int i = 1; i < in.length; i++) {
            query += in[i] + "+";
        }

        String search = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=" + query + "&key=AIzaSyDIGjfxN7ZJbVHjqnABdbcOH95662kTRzk";
        String getJson = Jsoup.connect(search).timeout(10 * 1000).ignoreContentType(true).get().text();
        JSONObject masterObject = (JSONObject) new JSONTokener(getJson).nextValue();
        JSONObject itemsObject = masterObject.getJSONArray("items").getJSONObject(0);
        JSONObject id      = itemsObject.getJSONObject("id");
        JSONObject snippet = itemsObject.getJSONObject("snippet");

        this.name = snippet.getString("title");
        this.url  = "https://www.youtube.com/watch?v=" + id.getString("videoId");
    }

    public String getName() {
        return this.name;
    }

    public String getURL() {
        return this.url;
    }

}