/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Pac;

public record PacDyingEvent(GameModel game, Pac pac) implements GameEvent {}
