/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Pac;

public record PacPowerFadesEvent(Game game, Pac pac) implements GameEvent {}
