/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Armin Reichert
 */
public class MoveResult {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;
    public boolean teleported;
    public final List<String> infos = new ArrayList<>(3);

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
        teleported = false;
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
        sb.append(teleported    ? " teleported" : "");
        sb.append(moved         ? " moved" : "");
        return sb.isEmpty() ? "" : "[" + sb.toString().trim() + "]";
    }
}