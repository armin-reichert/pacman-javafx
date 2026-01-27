/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class MoveInfo {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;
    public final List<String> infos = new ArrayList<>(3);

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
        infos.clear();
    }

    public void log(String info) {
        infos.add(info);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(tunnelEntered ? " entered tunnel" : "");
        sb.append(tunnelLeft    ? " left tunnel" : "");
        sb.append(moved         ? " moved" : "");
        return sb.isEmpty() ? "" : "[" + sb.toString().trim() + "]";
    }
}