package com.nix;

import javax.security.auth.login.LoginException;

import com.nix.audio.AudioHandler;
import com.nix.audio.Song;
import com.nix.commands.Command;
import com.nix.commands.Commands;
import com.nix.commands.Listener;
import com.nix.util.Messages;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Naudix implements Runnable {

    public static final String TOKEN = "-";

    public static Naudix bot;

    private final Thread thread;

    private JDA client;
    private Commands commands;
    private Listener messageListener;
    private AudioHandler handler;

    private VoiceChannel current;

    private final String pass_btoken;

    public Naudix(String token) {
        this.pass_btoken = token;
        this.thread = new Thread(this, "naudix");
        this.thread.start();
    }

    @Override
    public void run() {
        this.handler = new AudioHandler();
        JDABuilder builder = JDABuilder.createDefault(this.pass_btoken);
        builder.setActivity(Activity.listening("-cmd"));
    
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
                Messages.create("Version: `0.2-a_09`\nremoved `-start`: replaced with `-p` or `-play`\n`-add` now works the exact same as `-p` or `-play`\nreplaced `-die` with `-sleep` or `-have-a-gn`\nnaudix automatically joins your current vc when calling `-p`\nnaudix automatically starts the queue when a song is added using `-p`\nadded support for `(SoundCloud, YouTube, and Vimeo)` URLs\nadded support for youtube search\nmade it so only users in the current vc can call `-p`, `-play`, `-join`, `-sleep`, `-have-a-gn`", _e);
            }
        };

        Command play = new Command(new String[] {"p", "play", "add"}) {
            @Override public void invoke(MessageReceivedEvent _e) {
                if(current == null) if(!join(_e)) return;
                if(_e.getMember().getVoiceState().getChannel() != current) return;

                String[] content = _e.getMessage().getContentRaw().split(" ");

                if(content[1].contains("open.spotify")) { Messages.create("spotify isnt supported sorry", _e); return; }
                handler.add(new Song(content));
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

                handler.stop();
            }
        };

        Command show_queue = new Command("q") {
            @Override public void invoke(MessageReceivedEvent _e) {
                Messages.create(handler.getQueue(), _e);
            }
        };

        Command next = new Command("next") {
            @Override public void invoke(MessageReceivedEvent _e) {
                handler.next();
                Messages.create("now playing: " + handler.getHead(), _e);
            }
        };

        Command help = new Command("cmd") {
            @Override public void invoke(MessageReceivedEvent _e) {
                Messages.create("All commands start with `-`:\n`cmd`: displays this list\n`p`, `play`, `add`: adds a song to the queue\n`join`: adds naudix to your vc\n`sleep`, `have-a-gn`: naudix resets the queue and leaves the vc\n`next`: skips the current song and plays the next song\n`changelog`: displays the changes to naudix since the last update", _e);
            }
        };

        this.commands.add(die);
        this.commands.add(play);
        this.commands.add(join_vc);
        this.commands.add(show_queue);
        this.commands.add(next);
        this.commands.add(help);
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
        VoiceChannel channel = _e.getMember().getVoiceState().getChannel();
        AudioManager audioManager = _e.getGuild().getAudioManager();
            
        if(channel != null) {
            audioManager.openAudioConnection(channel);
            audioManager.setSendingHandler(handler.getProvider());
        } else {
            Messages.create("join a vc first idiot", _e);
            return false;
        }

        this.current = channel;

        return true;
    }

    public VoiceChannel channel() {
        return this.current;
    }

    public boolean isInVC(MessageReceivedEvent _e) {
        return _e.getMember().getVoiceState().getChannel() == current;
    }

}
