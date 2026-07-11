/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.model.actors.Pac;

public record PacEatsFoodEvent(Pac pac, boolean energizer, boolean allPellets) implements GameEvent {}
