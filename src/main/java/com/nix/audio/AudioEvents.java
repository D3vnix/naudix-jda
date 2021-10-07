package com.nix.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class AudioEvents extends AudioEventAdapter implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private final AudioHandler audio;

    public AudioEvents(AudioPlayer player, AudioHandler audioHandler) {
        this.player = player;
        this.audio = audioHandler;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        player.playTrack(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        
    }

    @Override
    public void noMatches() {
        
    }

    @Override
    public void loadFailed(FriendlyException exception) {

    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        switch (endReason) {
            case FINISHED:
                audio.next();
                break;
            case STOPPED:
                audio.next();
                break;
            default:
                break;
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
    
    }
}
