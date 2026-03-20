/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

public class MoveInfo {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
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