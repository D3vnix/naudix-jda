package com.nix.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ICommand {
    void invoke(MessageReceivedEvent _e);
}
