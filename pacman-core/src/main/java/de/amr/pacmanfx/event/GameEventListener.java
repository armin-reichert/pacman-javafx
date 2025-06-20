/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

public interface GameEventListener {
    /**
     * Called when a game event is received.
     *
     * @param event a game event
     */
    void onGameEvent(GameEvent event);
}