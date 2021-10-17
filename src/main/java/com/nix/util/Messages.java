package com.nix.util;

import com.nix.Naudix;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Messages {
    
    public static void create(String text, MessageReceivedEvent _e) {
        _e.getTextChannel().sendMessage(text).queue();
    }

    public static void create(EmbedBuilder embedBuilder, MessageReceivedEvent _e) {
        _e.getTextChannel().sendMessageEmbeds(embedBuilder.build()).queue();;
    }

    public static void create(String text) {
        Naudix.bot.lastUsedChannel().sendMessage(text).queue();
    }

    public static void create(EmbedBuilder embedBuilder) {
        Naudix.bot.lastUsedChannel().sendMessageEmbeds(embedBuilder.build()).queue();
    }

}
