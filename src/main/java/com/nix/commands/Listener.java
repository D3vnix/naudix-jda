package com.nix.commands;

import com.nix.Naudix;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Listener extends ListenerAdapter {
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String com = event.getMessage().getContentRaw();
        
        for(Command c : Naudix.bot.commands().get()) {
            for(String name : c.getNames()) {
                if(com.startsWith(Naudix.TOKEN + name)) {
                    c.invoke(event);

                    break;
                }
            }
        }
    }

}
