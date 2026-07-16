/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event;

import de.amr.pacmanfx.core.GameContext;

public record GameStartedEvent(GameContext gameContext) implements GameEvent {}
