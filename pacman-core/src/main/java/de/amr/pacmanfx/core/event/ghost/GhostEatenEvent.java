/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.ghost;

import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.event.GameEvent;

public record GhostEatenEvent(Ghost ghost) implements GameEvent {}
