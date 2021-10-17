package com.nix.commands;

import com.nix.Naudix;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
    
        if(event.getAuthor().isBot()) return;

        for(Command c : Naudix.bot.commands().get()) {
            for(String name : c.getNames()) {
                if(msg.startsWith(Naudix.TOKEN + name)) {
                    Naudix.bot.setTextChannel(event.getTextChannel());
                    c.invoke(event);
                    
                    break;
                }
            }
        }
    }

}
