/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.event.GameEvent;

public record GhostStartsReturningHomeEvent(GameContext gameContext, Ghost ghost) implements GameEvent {}
