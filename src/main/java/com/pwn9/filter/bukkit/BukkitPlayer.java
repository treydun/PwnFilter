/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2016 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.filter.bukkit;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.pwn9.filter.engine.api.MessageAuthor;
import com.pwn9.filter.engine.rules.action.targeted.BurnTarget;
import com.pwn9.filter.engine.rules.action.targeted.FineTarget;
import com.pwn9.filter.engine.rules.action.targeted.KickTarget;
import com.pwn9.filter.engine.rules.action.targeted.KillTarget;
import com.pwn9.filter.minecraft.api.MinecraftAPI;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author of a text string sent to us by Bukkit.  This is typically a player.
 * These objects are transient, and only last for as long as the message does.
 * <p/>
 * Created by Sage905 on 15-08-31.
 */
public class BukkitPlayer implements MessageAuthor, FineTarget, BurnTarget, KillTarget, KickTarget {

    public static final int MAX_CACHE_AGE_SECS = 60 ; //

    private final MinecraftAPI minecraftAPI;
    private final UUID playerId;
    private final Stopwatch stopwatch;

    BukkitPlayer(UUID uuid, MinecraftAPI api) {
        this.playerId = uuid;
        this.minecraftAPI = api;
        this.stopwatch = Stopwatch.createStarted();
    }

    // For testing
    BukkitPlayer(UUID uuid, MinecraftAPI api, Ticker ticker) {
        this.playerId = uuid;
        this.minecraftAPI = api;
        this.stopwatch = Stopwatch.createStarted(ticker);
    }

    private final ConcurrentHashMap<String, Boolean> playerPermCache =
            new ConcurrentHashMap<>(16, 0.9f, 1); // Optimizations for Map

    @Override
    public Boolean hasPermission(String permString) {

        // We are caching permissions, so we don't have to ask the API every time,
        // as that could get expensive for complex rulechains.  Every MAX_CACHE_AGE_SECS
        // we invalidate the cache on access.  This should have us asking if a player
        // has any given perm only 1 or 2 times every MAX_CACHE_AGE_SECS

        if (stopwatch.elapsed(TimeUnit.SECONDS) > MAX_CACHE_AGE_SECS) {
            stopwatch.reset(); stopwatch.start();
            playerPermCache.clear();
        }

        return playerPermCache.computeIfAbsent(permString, (s) -> minecraftAPI.playerIdHasPermission(playerId, s));
    }

    private String playerName = "";

    @NotNull
    @Override
    public String getName() {
        if (playerName.isEmpty()) {
            String name = minecraftAPI.getPlayerName(playerId);
            if (name != null) playerName = name;
        }
        return playerName;
    }

    @NotNull
    @Override
    public UUID getId() {
        return playerId;
    }

    public MinecraftAPI getMinecraftAPI() {
        return minecraftAPI;
    }

    // Not cached.
    public String getWorldName() {
        return minecraftAPI.getPlayerWorldName(playerId);
    }


    public boolean burn(final int duration, final String messageString) {
        return minecraftAPI.burn(playerId, duration, messageString);
    }

    @Override
    public void sendMessage(final String message) {
        minecraftAPI.sendMessage(playerId, message);
    }

    @Override
    public void sendMessages(final List<String> messages) {
        minecraftAPI.sendMessages(playerId, messages);
    }

    public void executeCommand(final String command) {
        minecraftAPI.executePlayerCommand(playerId, command);
    }

    public boolean fine(final Double amount, final String messageString) {
        return minecraftAPI.withdrawMoney(playerId, amount, messageString);
    }

    public void kick(final String messageString) {
        minecraftAPI.kick(playerId, messageString);
    }

    public void kill(final String messageString) {
        minecraftAPI.kill(playerId, messageString);
    }

}
