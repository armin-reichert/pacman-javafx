/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.actors.Ghost;

public record GhostStartsReturningHomeEvent(Ghost ghost) implements GameEvent {}
