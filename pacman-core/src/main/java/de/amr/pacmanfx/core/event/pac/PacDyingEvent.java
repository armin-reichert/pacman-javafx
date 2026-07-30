/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.pac;

import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.model.actors.Pac;

public record PacDyingEvent(Pac pac) implements GameEvent {}
