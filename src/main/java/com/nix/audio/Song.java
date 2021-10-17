package com.nix.audio;

public class Song {

    private String url, name;

    public Song(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getURL() {
        return this.url;
    }

}
