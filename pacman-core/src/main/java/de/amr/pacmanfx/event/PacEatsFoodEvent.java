/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Pac;

public record PacEatsFoodEvent(GameModel game, Pac pac, boolean energizer, boolean allPellets) implements GameEvent {}
