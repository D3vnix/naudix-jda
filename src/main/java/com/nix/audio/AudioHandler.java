package com.nix.audio;

import java.util.LinkedList;
import java.util.Queue;

import com.nix.util.Messages;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.dv8tion.jda.api.EmbedBuilder;

public class AudioHandler {

    private AudioPlayerManager manager;
    private AudioPlayer player;
    private AudioEvents events;
    private Provider provider;

    private Queue<Song> queue;

    private boolean isPlaying, loop;

    private int volume, curr;

    public AudioHandler() {
        this.init();
    }

    public void init() {
        this.manager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.manager);
        AudioSourceManagers.registerLocalSource(this.manager);

        this.volume = 25;

        this.player = this.manager.createPlayer();
        this.player.setVolume(this.volume);
        this.events = new AudioEvents(this.player, this);
        this.provider = new Provider(this.player);

        this.player.addListener(this.events);

        this.queue = new LinkedList<>();
        this.isPlaying = false;
        this.loop = false;
        this.curr = 0;
    }

    public void play() {
        if(this.queue.peek() != null) {
            this.manager.loadItem(this.queue.peek().getURL(), this.events);
            
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Now Playing: ");
            eb.addField(this.queue.peek().getName(), "", false);
            Messages.create(eb);
            
            this.isPlaying = true;
        }
    }

    public void playSong(int index) {
        int i = 0;

        for(Song song : this.queue) {
            if(i == index) {
                this.manager.loadItem(song.getURL(), this.events);
            
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Now Playing: ");
                eb.addField(song.getName(), "", false);
                Messages.create(eb);
            
                this.isPlaying = true;
                break;
            }

            i++ ;
        }
    }

    public void next() {
        if(this.queue.equals(null) || this.manager.equals(null) || this.player.equals(null)) this.init();

        if(!this.loop) {
            removeHead();
            play();
        } else {
            playSong(curr++);
        }

        if(this.queue.isEmpty()) this.reset();
    }

    public void reset() {
        this.manager.shutdown();
        this.init();
    }

    public void loop() {
        this.loop = !this.loop;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Turning `loop`: " + (this.loop ? "ON!" : "OFF!"));
        Messages.create(eb);
    }

    public void removeHead() {
        this.queue.remove();
    }

    public void setVolume(int vol) {
        this.volume = vol;
        this.player.setVolume(this.volume);
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

    public Queue<Song> getQueue() {
        return this.queue;
    }

    public Song getHead() {
        return this.queue.peek();
    }

}
