/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;

public record GhostStartsReturningHomeEvent(GameContext context, Ghost ghost) implements GameEvent {}
