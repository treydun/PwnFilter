/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.filter.engine.rules.action.minecraft;

import com.pwn9.filter.engine.api.FilterContext;
import com.pwn9.filter.engine.rules.action.InvalidActionException;
import com.pwn9.filter.minecraft.api.MinecraftConsole;
import com.pwn9.filter.engine.api.Action;
import com.pwn9.filter.util.tag.TagRegistry;

/**
 * Execute a console command
 *
 * @author Sage905
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class Console implements Action {
    private final String command;

    private Console(String s) {
        this.command = s;
    }

    /** {@inheritDoc} */
    public static Action getAction(String s) throws InvalidActionException
    {
        if (s.isEmpty()) throw new InvalidActionException("No command was provided to 'console'");
        return new Console(s);

    }

    /** {@inheritDoc} */
    public void execute(final FilterContext filterTask) {
        final String cmd = TagRegistry.replaceTags(command, filterTask);
        filterTask.addLogMessage("Sending console command: " + cmd);
        MinecraftConsole.getInstance().executeCommand(cmd);
    }
}