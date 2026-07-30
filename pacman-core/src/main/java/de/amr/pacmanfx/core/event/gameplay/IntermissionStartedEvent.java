/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.event.gameplay;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEvent;

public record IntermissionStartedEvent(GameContext gameContext, int intermissionNumber) implements GameEvent {}
