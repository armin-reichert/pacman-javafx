/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.pac;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.GameEvent;

public record PacEatsFoodEvent(Pac pac, boolean energizer, boolean allPellets, long tick) implements GameEvent {}
