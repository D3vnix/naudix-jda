package com.nix.commands;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command implements ICommand {

    private List<String> names;

    public Command(String name) {
        this.names = new ArrayList<>();
        this.names.add(name);
    }

    public Command(String name[]) {
        this.names = new ArrayList<>();

        for(int i = 0; i < name.length; i ++) {
            this.names.add(name[i]);
        }
    }

    @Override
    public abstract void invoke(MessageReceivedEvent _e);

    public List<String> getNames() {
        return this.names;
    }
}
