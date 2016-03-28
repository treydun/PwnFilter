

/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.filter.engine;

import com.google.common.collect.Sets;
import com.pwn9.filter.engine.api.FilterClient;
import com.pwn9.filter.engine.api.StatsTracker;
import com.pwn9.filter.engine.config.FilterConfig;
import com.pwn9.filter.engine.rules.action.ActionFactory;
import com.pwn9.filter.engine.rules.chain.InvalidChainException;
import com.pwn9.filter.engine.rules.chain.RuleChain;
import com.pwn9.filter.engine.rules.parser.TextConfigParser;
import com.pwn9.filter.util.PwnFormatter;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;


/**
 * Handle Startup / Shutdown / Configuration of our PwnFilter Clients
 * User: Sage905
 * Date: 13-09-29
 * Time: 9:25 AM
 * To change this template use File | Settings | File Templates.
 *
 * @author Sage905
 * @version $Id: $Id
 */
@SuppressWarnings("UnusedDeclaration")
public class FilterService {

    private final StatsTracker statsTracker;
    private final FilterConfig config;
    private final Set<FilterClient> registeredClients = Sets.newConcurrentHashSet();
    private final ActionFactory actionFactory;
    private final Logger logger = Logger.getLogger("com.pwn9.filter");
    public PointManager getPointManager() {
        return pointManager;
    }

    private final PointManager pointManager = new PointManager();

    private FileHandler logfileHandler;

    public FilterService(StatsTracker statsTracker) {

        this.statsTracker = statsTracker;
        this.config = new FilterConfig();
        this.actionFactory = new ActionFactory(this);
    }

    @SuppressWarnings("WeakerAccess")
    public Set<FilterClient> getActiveClients() {
        return registeredClients.stream().filter(FilterClient::isActive).
                collect(Collectors.toSet());
    }

    public void shutdown() {
        unregisterAllClients();
        clearLogFileHandler();
    }

    /**
     * <p>Getter for the field <code>registeredClients</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Set<FilterClient> getRegisteredClients() {
        return registeredClients;
    }
    public FilterConfig getConfig() {
        return config;
    }

    public void enableClients() {
        registeredClients.forEach(FilterClient::activate);

    }

    public void disableClients() {
        getActiveClients().forEach(FilterClient::shutdown);
    }

    /**
     * Add a listener to the PwnFilter ListenerManager.  This allows PwnFilter
     * to notify the listener when it should try to activate or shutdown.
     * PwnFilter will call the activate / shutdown methods when reloading
     * rules configs.
     *
     * The FilterListener must register *before* attempting to use any other
     * PwnFilter resources.
     *
     * @param f FilterListener instance
     */
    public void registerClient(FilterClient f) {
        if (registeredClients.contains(f)) {
            return;
        }
        registeredClients.add(f);
        statsTracker.updateClients(getActiveClients());
    }

    /**
     * Remove a listener from the PwnFilter ListenerManager.
     * The listener will no longer be activated / deactivated when PwnFilter
     * reloads configs, rules, etc.
     * IMPORTANT: Before de-registering, the FilterListener must remove all
     * references to RuleSets.
     *
     * @param f FilterListener to remove.
     * @return true if the listener was previously registered and successfully
     * removed. False if it was not registered.
     */
    public boolean unregisterClient(FilterClient f) {
        if (registeredClients.contains(f)) {
            registeredClients.remove(f);
            statsTracker.updateClients(getActiveClients());
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>unregisterAllClients.</p>
     */
    private void unregisterAllClients() {
        for (FilterClient f : registeredClients ) {
            f.shutdown();
            registeredClients.remove(f);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        disableClients();
    }

    /*
     * Logfile Handling
     * TODO: Maybe these should be moved into their own class in the future.
     */

    public void setLogFileHandler(File logFile) {
        try {
            // For now, one logfile, like the old way.
            String fileName =  logFile.toString();
            logfileHandler = new FileHandler(fileName, true);
            SimpleFormatter f = new PwnFormatter();
            logfileHandler.setFormatter(f);
            logfileHandler.setLevel(Level.FINEST); // Catch all log messages
            logger.addHandler(logfileHandler);
            logger.info("Now logging to " + fileName );

        } catch (IOException e) {
            logger.warning("Unable to open logfile.");
        } catch (SecurityException e) {
            logger.warning("Security Exception while trying to add file Handler");
        }
    }

    public void clearLogFileHandler() {
        if (logfileHandler != null) {
            logger.info("Closing Logfile");
            logfileHandler.close();
            logger.removeHandler(logfileHandler);
            logfileHandler = null;
        }
    }

    public RuleChain parseRules(File ruleFile) throws InvalidChainException {
        TextConfigParser parser = new TextConfigParser(this);

        return parser.parse(ruleFile);
    }
    /*
     * Set the level that the LogFile will listen to, based on the Debug
     * setting.
     */
    public void setDebugMode(String s) {
        switch (s.toLowerCase()) {
            case "high": logfileHandler.setLevel(Level.FINEST);
            case "medium": logfileHandler.setLevel(Level.FINER);
            case "low": logfileHandler.setLevel(Level.FINE);
        }
    }

    /*
     * GETTERS
     */
    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    public Logger getLogger() { return logger; }

}
