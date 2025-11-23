/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.event;

import de.amr.pacmanfx.lib.math.Vector2i;

public interface GameEventManager {

    void addEventListener(GameEventListener listener);

    void removeEventListener(GameEventListener listener);

    void publishEvent(GameEvent event);

    void publishEvent(GameEventType type);

    void publishEvent(GameEventType type, Vector2i tile);
}