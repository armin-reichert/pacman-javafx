/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

//TODO integrate into WorldNavigationComp
public class WorldNavigationInfo {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;
    public boolean teleportStarted;

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
        teleportStarted = false;
    }

    @Override
    public String toString() {
        final var s = new StringBuilder();
        s.append(tunnelEntered ? " entered tunnel" : "");
        s.append(tunnelLeft    ? " left tunnel" : "");
        s.append(moved         ? " moved" : "");
        s.append(teleportStarted ? " teleport started" : "");
        return s.isEmpty() ? "" : "[" + s.toString().trim() + "]";
    }
}