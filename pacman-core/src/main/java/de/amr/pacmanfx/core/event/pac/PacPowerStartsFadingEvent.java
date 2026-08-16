/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.pac;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.GameEvent;

public record PacPowerStartsFadingEvent(GameEntity pac) implements GameEvent {}
