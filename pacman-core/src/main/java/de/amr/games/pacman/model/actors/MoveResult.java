/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {
    public boolean moved;
    public boolean tunnelEntered;
    public boolean tunnelLeft;
    public boolean teleported;
    private final List<String> messages = new ArrayList<>(3);

    public void clear() {
        moved = false;
        tunnelEntered = false;
        tunnelLeft = false;
        teleported = false;
        messages.clear();
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public String summary() {
        return String.join(", ", messages);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(tunnelEntered ? " entered tunnel" : "");
        sb.append(tunnelLeft ? " left tunnel" : "");
        sb.append(teleported ? " teleported" : "");
        sb.append(moved ? " moved" : "");
        return sb.isEmpty() ? "" : "[" + sb.toString().trim() + "]";
    }
}