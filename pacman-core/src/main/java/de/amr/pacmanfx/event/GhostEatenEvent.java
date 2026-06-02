/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.event;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;

public record GhostEatenEvent(GameModel game, Ghost ghost) implements GameEvent {}
