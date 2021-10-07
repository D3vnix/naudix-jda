package com.nix.commands;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    
    private List<Command> commands;

    public Commands() {
        this.commands = new ArrayList<Command>();
    }

    public List<Command> get() {
        return this.commands;
    }

    public void add(Command _c) {
        this.commands.add(_c);
    }

}
