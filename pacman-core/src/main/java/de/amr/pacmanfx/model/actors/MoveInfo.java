/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

public class MoveInfo {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;
    public boolean teleported;

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
        teleported = false;
    }

    @Override
    public String toString() {
        final var s = new StringBuilder();
        s.append(tunnelEntered ? " entered tunnel" : "");
        s.append(tunnelLeft    ? " left tunnel" : "");
        s.append(moved         ? " moved" : "");
        s.append(teleported    ? " teleported" : "");
        return s.isEmpty() ? "" : "[" + s.toString().trim() + "]";
    }
}