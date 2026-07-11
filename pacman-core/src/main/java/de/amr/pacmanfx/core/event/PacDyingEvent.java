/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.model.actors.Pac;

public record PacDyingEvent(Pac pac) implements GameEvent {}
