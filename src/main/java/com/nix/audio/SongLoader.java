package com.nix.audio;

import java.io.IOException;
import java.net.MalformedURLException;

import java.net.URL;

import com.nix.util.Messages;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;

import org.apache.hc.core5.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.ArrayList;

public class SongLoader {

    private final SpotifyApi api;

    private AudioHandler handler;

    public SongLoader(AudioHandler handler) {
        this.handler = handler;

        this.api = new SpotifyApi.Builder()
                    .setClientId("no")
                    .setClientSecret("u")
                    .build();
    }

    public void loadSong(String[] msg) {
        try {
            this.api.setAccessToken(this.api.clientCredentials().build().execute().getAccessToken());

            List<Song> tracks = getSongInfo(msg);
            for (Song song : tracks) {
                this.handler.add(song);
            }
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            ex.printStackTrace();
        }
    }

    private List<Song> getSongInfo(String[] msg) throws IOException, ParseException, SpotifyWebApiException {
        List<Song> loaded = new ArrayList<>();

        if (!isURL(msg[1])) {
            String[] info = getFromYT(msg);
            if(info != null)
                loaded.add(new Song(info[1], info[0]));
            createMessage(info[0]);
            return loaded;
        }

        URL url = new URL(msg[1]);

        if (url.getHost().contains("open.spotify.com")) {
            String[] path = url.getPath().split("/");
            String id = "";
            boolean isPlaylist = false;

            for (int i = 0; i < path.length; i++) {
                if (path[i].contains("track") || path[i].contains("playlist")) {
                    id = path[i + 1];
                }
                
                if (path[i].contains("playlist"))
                    isPlaylist = true;
            }

            if (!isPlaylist) {
                Track track = api.getTrack(id).build().execute();
                String[] info = getFromYT(track.getName() + " " + track.getArtists()[0].getName());
                if(info != null)
                    loaded.add(new Song(info[1], info[0]));
                createMessage(info[0]);
            } else {
                Playlist playlist = api.getPlaylist(id).build().execute();
                PlaylistTrack[] ptracks = playlist.getTracks().getItems();
                for (PlaylistTrack ptrack : ptracks) {
                    Track track = api.getTrack(ptrack.getTrack().getId()).build().execute();
                    String[] info = getFromYT(track.getName() + "+" + track.getArtists()[0].getName() + "+");
                    if(info != null)
                        loaded.add(new Song(info[1], info[0]));
                }
                createMessage("The Playlist: " + playlist.getName());
            }
        } else if (url.getHost().contains("youtube") || url.getHost().contains("youtu.be")) {
            String[] info = getFromYT(msg);
            if(info != null)
                loaded.add(new Song(info[1], info[0]));
            createMessage(info[0]);
        }

        return loaded;
    }

    private boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private JSONObject searchYT(String[] in) throws IOException {
        String query = "";
        for(int i = 1; i < in.length; i++) {
            query += in[i] + "+";
        }

        String search = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=" + query + "&key=-";
        String getJson = Jsoup.connect(search).timeout(10000).ignoreContentType(true).get().text();
        JSONTokener tokener = new JSONTokener(getJson);

        try {
            JSONObject masterObject = (JSONObject) tokener.nextValue();
            JSONObject itemsObject = masterObject.getJSONArray("items").getJSONObject(0);
            return itemsObject;
        } catch(JSONException e) {
            Messages.create("the song isnt loading wtf why");
            return null;
        }
    }

    private JSONObject searchYT(String in) throws IOException {
        String search = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=relevance&q=" + in
                + "&key=AIzaSyDIGjfxN7ZJbVHjqnABdbcOH95662kTRzk";
        String getJson = Jsoup.connect(search).timeout(10000).ignoreContentType(true).get().text();
        JSONTokener tokener = new JSONTokener(getJson);
        
        try {
            JSONObject masterObject = (JSONObject) tokener.nextValue();
            JSONObject itemsObject = masterObject.getJSONArray("items").getJSONObject(0);
            return itemsObject;
        } catch(JSONException e) {
            Messages.create("the song isnt loading wtf why");
            return null;
        }
    }

    private String[] getFromYT(String[] msg) throws IOException {
        JSONObject details = searchYT(msg);
        
        if(details != null) {
            String[] info = { details.getJSONObject("snippet").getString("title"),
                details.getJSONObject("id").getString("videoId") };

            return info;
        }

        return null;
    }

    private String[] getFromYT(String msg) throws IOException {
        JSONObject details = searchYT(msg);

        if(details != null) {
            String[] info = { details.getJSONObject("snippet").getString("title"),
                details.getJSONObject("id").getString("videoId") };

            return info;
        }

        return null;
    }

    private void createMessage(String name) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Added to the queue: ");
        builder.addField(name, "", false);
        Messages.create(builder);
    }

}
