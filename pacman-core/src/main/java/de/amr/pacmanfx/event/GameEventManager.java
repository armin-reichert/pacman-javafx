/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

public interface GameEventManager {

    void addGameEventListener(GameEventListener listener);

    void removeGameEventListener(GameEventListener listener);

    void publishGameEvent(GameEvent event);
}
