/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2015 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.filter.util.tag;

import com.pwn9.filter.engine.api.FilterContext;
import com.pwn9.filter.engine.PointManager;

import java.text.DecimalFormat;

/**
 * Return the current points level a player has, if the PointManager is enabled.
 *
 * Created by Sage905 on 15-09-04.
 */
public class PointsTag implements Tag {

    private static final DecimalFormat df = new DecimalFormat("0.00##");

    @Override
    public String getValue(FilterContext filterTask) {
        PointManager pointManager = filterTask.getFilterClient().getFilterService().getPointManager();
        return (pointManager.isEnabled()) ?
                df.format(pointManager.getPoints(filterTask.getAuthor())) :
                "-";
    }
}