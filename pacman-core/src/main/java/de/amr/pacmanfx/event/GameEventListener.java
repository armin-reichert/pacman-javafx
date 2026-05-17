/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.event;

public interface GameEventListener {

    /**
     * Central event dispatcher. Implementors can override this for common handling
     * or override specific methods for targeted reactions.
     */
    void onGameEvent(GameEvent event);
}
