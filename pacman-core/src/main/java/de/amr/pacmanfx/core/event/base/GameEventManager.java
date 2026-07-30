/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.base;

import de.amr.pacmanfx.core.event.GameEvent;

public interface GameEventManager {

    void addGameEventSubscriber(GameEventListener listener);

    void removeGameEventSubscriber(GameEventListener listener);

    void publishGameEvent(GameEvent event);
}
