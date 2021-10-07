package com.nix.audio;

import java.util.LinkedList;
import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

public class AudioHandler {

    private AudioPlayerManager manager;
    private AudioPlayer player;
    private AudioEvents events;
    private Provider provider;

    private Queue<Song> queue;

    private boolean isPlaying;

    public AudioHandler() {
        this.init();
    }

    public void init() {
        this.manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.manager);
        AudioSourceManagers.registerLocalSource(this.manager);

        this.player = this.manager.createPlayer();
        this.events = new AudioEvents(this.player, this);
        this.provider = new Provider(this.player);

        this.player.addListener(this.events);

        this.queue = new LinkedList<>();
        this.isPlaying = false;
    }

    public void play() {
        if(this.queue.peek() != null) {
            this.manager.loadItem(this.queue.peek().getURL(), this.events);
            this.isPlaying = true;
        }
    }

    public void next() {
        if(this.queue.equals(null) || this.manager.equals(null) || this.player.equals(null)) this.init();

        removeHead();
        play();
    }

    public void stop() {
        this.manager.shutdown();
        this.init();
    }

    public void removeHead() {
        this.queue.remove();
    }

    public void add(Song song) {
        this.queue.add(song);
        if(!this.isPlaying) this.play();
    }

    public Provider getProvider() {
        return this.provider;
    }

    public AudioEvents getEventsHandler() {
        return this.events;
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public AudioPlayerManager getManager() {
        return this.manager;
    }

    public String getQueue() {
        String res = "[";

        for(Song s : this.queue) {
            res += s.getName() + ", ";
        }

        return res + "]";
    }

    public Song getHead() {
        return this.queue.peek();
    }

}
