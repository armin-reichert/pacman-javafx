/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.actors.Pac;

public record PacDeadEvent(GameContext context, Pac pac) implements GameEvent {}
