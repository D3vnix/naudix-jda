package com.nix.util;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Messages {
    
    public static void create(String text, MessageReceivedEvent _e) {
        _e.getTextChannel().sendMessage(text).queue();
    }

}
