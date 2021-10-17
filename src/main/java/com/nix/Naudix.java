package com.nix;

import com.nix.audio.AudioHandler;
import com.nix.audio.Song;
import com.nix.audio.SongLoader;
import com.nix.commands.Command;
import com.nix.commands.Commands;
import com.nix.commands.Listener;
import com.nix.util.Messages;

import java.util.Queue;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Naudix implements Runnable {

    public static final String TOKEN = "-";

    public static Naudix bot;

    private final String pass_btoken;
    private final Thread thread;

    private JDA client;
    private Commands commands;
    private Listener messageListener;
    private AudioHandler handler;
    private SongLoader loader;

    private VoiceChannel current;
    private TextChannel last;

    public Naudix(String token) {
        this.pass_btoken = token;
        this.thread = new Thread(this, "naudix");
        this.thread.start();
    }

    @Override
    public void run() {
        this.handler = new AudioHandler();
        this.loader = new SongLoader(this.handler);
        JDABuilder builder = JDABuilder.createDefault(this.pass_btoken)
            .setActivity(Activity.listening("-changelog, -cmd"));

        try {
            this.client = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        if(this.client != null) {
            createCommands();

            this.messageListener = new Listener();
            this.client.addEventListener(this.messageListener);
        }
    }

    private void createCommands() {
        this.commands = new Commands();

        Command changelog = new Command("changelog") {
            @Override public void invoke(MessageReceivedEvent _e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Changelog: ");
                eb.addField("Version", "`1.1-fr-rvn_04`", false);
                eb.addField("1.", "added support for `Spotify` links and playlists", false);
                eb.addField("2.", "added message embeds to make everything look nicer", false);
                Messages.create(eb, _e);
            }
        };

        Command play = new Command(new String[] {"p", "play", "add"}) {
            @Override public void invoke(MessageReceivedEvent _e) {
                String[] content = _e.getMessage().getContentRaw().split(" ");

                /* --- edge cases --- */
                if(current == null) if(!join(_e)) return;
                if(!isInVC(_e)) return;
                if(content.length == 1) return;
                if(content[1] == "") return;
                /* --- edge cases ---*/

                loader.loadSong(content);
            }
        };

        Command join_vc = new Command("join") {
            @Override public void invoke(MessageReceivedEvent _e) {
                if(current == null) join(_e);
            }
        };

        Command die = new Command(new String[] {"sleep", "have-a-gn"}) {
            @Override public void invoke(MessageReceivedEvent _e) {
                if(!isInVC(_e)) return;
                AudioManager audioManager = _e.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
                current = null;

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Leaving the VC");
                Messages.create(eb, _e);

                handler.reset();
            }
        };

        Command show_queue = new Command("q") {
            @Override public void invoke(MessageReceivedEvent _e) {
                EmbedBuilder eb = new EmbedBuilder();
                Queue<Song> queue = handler.getQueue();

                eb.setTitle("Current Queue:");

                int i = 0;
                for(Song song : queue) {
                    String curr = "" + (i + 1) + ". "; // why java, why?
                    eb.addField(curr, "`" + song.getName() + "`", false);
                    i ++;
                }

                Messages.create(eb, _e);
            }
        };

        Command next = new Command("next") {
            @Override public void invoke(MessageReceivedEvent _e) {
                handler.next();
            }
        };

        Command volume = new Command(new String[] {"v", "volume"}) {
            @Override public void invoke(MessageReceivedEvent _e) {
                String[] content = _e.getMessage().getContentRaw().split(" ");
                if(content.length == 1) return;

                int vol = Integer.parseInt(content[1]);
                handler.setVolume(vol);
            }
        };

        Command toggle_loop = new Command("loop") {
            @Override public void invoke(MessageReceivedEvent _e) {
                handler.loop();
            }
        };

        Command help = new Command("cmd") {
            @Override public void invoke(MessageReceivedEvent _e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Commands: ");
                eb.addField("`-cmd`", "displays a list of commands", false);
                eb.addField("`-p`, `-play`, `-add`", "adds a song to the queue", false);
                eb.addField("`-next`", "skips the current song", false);
                eb.addField("`-loop`", "toggles looping the queue on and off", false);
                eb.addField("`-v`, `-volume`", "for volume control", false);
                eb.addField("`-sleep`, `-have-a-gn`", "leaves the vc and resets the queue", false);
                eb.addField("`-q`", "display the queue", false);
                eb.addField("`-join`", "joins naudix to your vc", false);
                eb.addField("`-changelog`", "shows the changes since the last update", false);

                Messages.create(eb, _e);
            }
        };

        this.commands.add(volume);
        this.commands.add(die);
        this.commands.add(play);
        this.commands.add(join_vc);
        this.commands.add(show_queue);
        this.commands.add(next);
        this.commands.add(help);
        this.commands.add(toggle_loop);
        this.commands.add(changelog);
    }

    public static void main(String[] args) {
        Naudix.bot = new Naudix(args[0]);
    }

    public JDA getClient() {
        return this.client;
    }

    public Commands commands() {
        return this.commands;
    }

    public AudioHandler getAudioHandler() {
        return this.handler;
    }

    public boolean join(MessageReceivedEvent _e) {
        if(current != null) if(isInVC(_e)) {    
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("i am already in your vc");
            Messages.create(eb, _e);
        }

        VoiceChannel channel = _e.getMember().getVoiceState().getChannel();
        AudioManager audioManager = _e.getGuild().getAudioManager();

        if(channel != null) {
            audioManager.openAudioConnection(channel);
            audioManager.setSendingHandler(handler.getProvider());
        } else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("join a vc first idiot");
            Messages.create(eb, _e);
            return false;
        }

        this.current = channel;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Joining VC: " + current.getName());
        Messages.create(eb, _e);

        return true;
    }

    public VoiceChannel channel() {
        return this.current;
    }

    public TextChannel lastUsedChannel() {
        return this.last;
    }

    public void setTextChannel(TextChannel _t) {
        this.last = _t;
    }

    public boolean isInVC(MessageReceivedEvent _e) {
        if(current == null) return false;
        return current.getMembers().contains(_e.getMember());
    }

}
